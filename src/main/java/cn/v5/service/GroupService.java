package cn.v5.service;


import cn.v5.bean.group.FailUserInfo;
import cn.v5.bean.group.GroupContainFailUserVo;
import cn.v5.bean.group.GroupUser;
import cn.v5.bean.openplatform.GroupVo;
import cn.v5.bean.openplatform.OpenGroupUser;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.entity.*;
import cn.v5.entity.group.GroupCreateReqIndex;
import cn.v5.metric.LogUtil;
import cn.v5.util.*;
import cn.v5.web.controller.ServerException;
import com.datastax.driver.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.CounterBuilder;
import info.archinnov.achilles.type.OptionsBuilder;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class GroupService implements InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(GroupService.class);
    public static final int MAX_VALUE = 0x7fffffff;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    @Qualifier("opManager")
    private PersistenceManager opManager;

    @Inject
    private UserService userService;

    @Inject
    private MessageQueueService queueService;

    @Inject
    private MessageService messageService;

    @Inject
    private TaskService taskService;

    @Inject
    private ConversationService conversationService;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${group.avatar.default}")
    private String defaultAvatars;

    @Value("${local.idc.region}")
    private String localIdc;

    @Autowired
    private GroupSeqService groupSeqService;

    @Autowired
    private ReqMetricUtil reqMetricUtil;

    @Value("${group.member.count.limit}")
    private int groupMemberCountLimit;

    @Autowired
    private CoreServerReqUtil coreServerReqUtil;

    private String[] groupDefaultAvatars;

    private AtomicInteger currentGroupIndex = new AtomicInteger(1);

    private PreparedStatement groupMemberCountStatement;
    private PreparedStatement groupMemberCountStatementOfOp;

    @Autowired
    private MessageSourceService messageSourceService;

    @Autowired
    private HttpService httpService;

    @Value("${auth.key}")
    private String authKey;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogUtil logUtil;

    public GroupService() {
        LOG.debug("init here");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isNotBlank(defaultAvatars)) {
            groupDefaultAvatars = defaultAvatars.split(",");
        }
        groupMemberCountStatement = manager.getNativeSession().prepare("select count(*) from group_members where group_id = ?");
        groupMemberCountStatementOfOp = opManager.getNativeSession().prepare("select count(*) from group_members where group_id = ?");
    }

    /**
     * @param admin
     * @param group
     * @param memberList 新成员user对象
     * @return 返回向群组添加成功的成员信息
     */
    private List<GroupUser> addUsersToGroupForCreate(final User admin, final Group group, List<User> memberList) {
        List<GroupUser> result = new ArrayList<>();
        final StringBuilder tips = new StringBuilder();
        for (int i = 0; i < memberList.size(); i++) {
            tips.append(",").append(memberList.get(i).getNickname());
            if (i == 4) {
                break;
            }
        }

        // 给新成员发送通知
        //把创建者添加到群组中
        Long startTime = System.currentTimeMillis();
        result.add(addUserToGroup(group, admin));
        for (User user : memberList) {
            result.add(addUserToGroup(group, user));
        }
        reqMetricUtil.addReqStepInfo("add user to group for create", String.format("users size is %s", null != memberList ? memberList.size() + 1 : 1), startTime, System.currentTimeMillis());

        startTime = System.currentTimeMillis();
        queueService.sendGroupCreateMsg(admin, memberList, group);
        reqMetricUtil.addReqStepInfo("send system notify for creating group.", String.format("size:%s", null != memberList ? memberList.size() : ""), startTime, System.currentTimeMillis());
        return result;
    }

    /**
     * 将申请用户加入群组
     *
     * @param applicant
     * @param group
     */
    private GroupUser addUserToGroupForApply(final User applicant, final Group group, final String operationUserId) {
        //原先的群组成员
        final List<GroupUser> oldUserList = findGroupUserList(group.getId(), null);

        Long startTime = System.currentTimeMillis();
        GroupUser ret = addUserToGroup(group, applicant);
        reqMetricUtil.addReqStepInfo("add user to group for apply", String.format("applicant id is %s", applicant.getId()), startTime, System.currentTimeMillis());

        if (null != oldUserList) {
            //给群组原有成员发送系统通知

            startTime = System.currentTimeMillis();
            for (GroupUser user : oldUserList) {
//                if (user.getId().equals(group.getCreator())) {
//                    continue;
//                }
                //群组原有成员需要接受到push推送 只发送增量的用户 客户端需要从服务端拉取成员
                //推送描述语 xxxx(群组名):xxx(申请人的名字)加入了群聊
                String pushContent = messageSourceService.getMessageSource(user.getAppId()).getMessage("group.apply.accept.msg.1", new Object[]{group.getName(), applicant.getNickname()}, user.getLocale());
                if (supportGroupApply(user.getAppId(), user.getId())) {
                    queueService.sendGroupJoinMsgV1(group, user, pushContent, applicant.getId(), true);
                } else {
                    queueService.sendGroupJoinMsgV0(group, user, pushContent, applicant.getId(), true);
                }
            }
            reqMetricUtil.addReqStepInfo("send group accept msg to old users", String.format("oldList size:%s", oldUserList.size()), startTime,
                    System.currentTimeMillis());
        }
        //给申请加入群聊的用户发送 推送消息；不必做版本校验
        startTime = System.currentTimeMillis();
        //xxx(群名称):你加入了群聊
        String pushContent = messageSourceService.getMessageSource(applicant.getAppId()).getMessage("group.apply.accept.msg.2", new Object[]{group.getName()}, applicant.getLocale());

        // 给自己发送消息
        queueService.sendGroupJoinMsgV1(group, applicant, pushContent, operationUserId, true);
        reqMetricUtil.addReqStepInfo("send group accept msg to new users", String.format("applicant id  is:%s", applicant.getId()), startTime,
                System.currentTimeMillis());
        return ret;
    }

    /**
     * @param inviteUser
     * @param group
     * @param memberList 新成员user对象
     * @return 返回向群组添加成功的成员信息
     * 系统通知只需要包含增量
     */
    private List<GroupUser> addUsersToGroupForInvite(final User inviteUser, final Group group, List<User> memberList, String systemNotifyExcludeUserId, String reqUserId) {

        //成功增加的成员
        List<GroupUser> result = new ArrayList<>();

        //获取群组原有的成员
        final List<GroupUser> oldUserList = findGroupUserList(group.getId(), null);


        // 增加新成员
        //TODO 批量增加成员 减少io操作
        Long startTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < memberList.size(); i++) {
            User user = memberList.get(i);
            result.add(addUserToGroup(group, user));
            if (i != 0) {
                sb.append(",");
            }
            sb.append(user.getNickname());
        }

        reqMetricUtil.addReqStepInfo("add user to group", String.format("memberList size:%s", null != memberList ? memberList.size() : ""), startTime,
                System.currentTimeMillis());

        if (null != oldUserList) {
            //给群组原有成员发送系统通知

            startTime = System.currentTimeMillis();
            for (GroupUser user : oldUserList) {
                //排除不接受系统通知的用户
//                if (user.getId().equalsIgnoreCase(systemNotifyExcludeUserId)) {
//                    continue;
//                }
                //群组原有成员需要接受到push推送 只发送增量的用户 客户端需要从服务端拉取成员
                //推送描述语 xxxx(群组名):xxx(邀请人的名字)邀请xxx,xxx,xxx加入了群聊
                String pushContent = messageSourceService.getMessageSource(user.getAppId()).getMessage("group.invite.1", new Object[]{group.getName(), inviteUser.getNickname(), sb.toString()}, user.getLocale());
                queueService.sendGroupInviteMsg(group, user, pushContent, inviteUser.getId(), true, reqUserId);
            }
            reqMetricUtil.addReqStepInfo("send group invite msg to old users", String.format("memberList size:%s", null != oldUserList ? oldUserList.size() : ""), startTime,
                    System.currentTimeMillis());
        }

        //给新成员发送 group 所有的成员
        startTime = System.currentTimeMillis();
        for (User user : memberList) {
            //通知被邀请人 push 消息
            //xxx(群名称):xxx(邀请人的名字)邀请你加入了群聊
            String pushContent = messageSourceService.getMessageSource(user.getAppId()).getMessage("group.invite.2", new Object[]{group.getName(), inviteUser.getNickname()}, user.getLocale());
            queueService.sendGroupInviteMsg(group, user, pushContent, inviteUser.getId(), true, reqUserId);
        }

        reqMetricUtil.addReqStepInfo("send group invite msg to new users", String.format("memberList size:%s", null != memberList ? memberList.size() : ""), startTime,
                System.currentTimeMillis());
        return result;
    }


    /**
     * 创建群组
     *
     * @param group
     * @param admin     群组创建者
     * @param memberIds
     * @return
     */
    public Group createGroup(Group group, User admin, final Set<String> memberIds) {
        CurrentUser.db().insert(group);
        logUtil.logCreateGroup(group, admin);
        Long startTime = System.currentTimeMillis();
        List<User> memberList = userService.findUserListByNames(memberIds);
        reqMetricUtil.addReqStepInfo("find users.", String.format("user size is %s", null != memberIds ? memberIds.size() : ""), startTime, System.currentTimeMillis());
        addUsersToGroupForCreate(admin, group, memberList);
        return group;
    }


    public void update(final Group group) {
        updateGroupInfo(group);

        // 发送通知
        this.taskService.execute(() -> {
            List<GroupUser> users = findGroupUserList(group.getId(), null);
            queueService.sendGroupUpdateMsg(group.getCreator(), users, group);
        });

    }

    /**
     * 获得群组信息
     *
     * @param groupId
     * @return
     */
    public Group findGroupInfo(String groupId) {
        Long startTime = System.currentTimeMillis();
        PersistenceManager realManager = CurrentUser.db();
        Group group = realManager.find(Group.class, groupId);
        reqMetricUtil.addReqStepInfo("find group info.", "by groupId", startTime, System.currentTimeMillis());
        if (group == null) {
            return null;
        }
        return group;
    }

    public Group findGroupInfo(String groupId, Integer appId) {
        Long startTime = System.currentTimeMillis();
        PersistenceManager realManager = getRealManager(appId);
        Group group = realManager.find(Group.class, groupId);
        reqMetricUtil.addReqStepInfo("find group info.", "by groupId", startTime, System.currentTimeMillis());
        if (appId > SystemConstants.CG_APP_ID_MAX) {
            String gID = GroupUtil.transOpenID(groupId, appId);
            group = realManager.find(Group.class, gID);
        }
        return group;
    }

    public Group getGroupByAccount(String account, Integer appID) {
        GroupAccountIndex groupAccountIndex = manager.find(GroupAccountIndex.class, account);
        if (groupAccountIndex == null) {
            return null;
        }
        Group group = findGroupInfo(groupAccountIndex.getGroupId());
        if (group != null) {
            User creator = userService.findById(group.getCreator());
            if (creator != null && creator.getAppId() != null && appID != null && !creator.getAppId().equals(appID)) {
                LOG.error("[getGroupByAccount error] user.appId:{} , appID:{} , group.id:{} ", creator.getAppId(), appID, group.getId());
                return null;
            }
        }
        return group;
    }


    public Group getGroupByAccount(String account) {
        GroupAccountIndex groupAccountIndex = manager.find(GroupAccountIndex.class, account);
        if (groupAccountIndex == null) {
            return null;
        }
        return findGroupInfo(groupAccountIndex.getGroupId());
    }

    public void updateGroupInfo(Group group) {
        Long startTime = System.currentTimeMillis();
        CurrentUser.db().update(group);
        reqMetricUtil.addReqStepInfo("update group info.", "", startTime, System.currentTimeMillis());
    }

    /**
     * 将某个用户加入群组
     *
     * @param group
     * @param user
     */


    public GroupUser addUserToGroup(Group group, User user) {
        GroupMember member = new GroupMember();
        member.setId(new GroupKey(group.getId(), user.getId()));
        member.setCreateTime(new Date());
        member.setStatus(0);
        member.setName(user.getNickname());
        member.setSeq(groupSeqService.getGroupMemberNextNumber(group.getId()));
        member.setIdc(localIdc);

        GroupUser groupUser = GroupUser.createFromUser(user);
        groupUser.setSeq(member.getSeq());
        groupUser.setId(user.getId());
        group.addMember(groupUser);
        CurrentUser.db().insert(member);

        CurrentUser.db().insert(new UserGroupIndex(new UserGroupIndexKey(user.getId(), group.getId())));
        logUtil.logAddGroupMember(group, user.getId());
        return groupUser;
    }

    /**
     * 用户推出群组时 需要进行个人群组的资源清理
     *
     * @param group
     * @param userId
     */
    private void removeUserFromGroup(Group group, String userId) {
        PersistenceManager realManager = CurrentUser.db();
        realManager.deleteById(UserGroupIndex.class, new UserGroupIndexKey(userId, group.getId()));
        realManager.deleteById(GroupMember.class, new GroupKey(group.getId(), userId));

        //删除群组的离线消息
        messageService.removeMessageByUserGroupId(userId, group.getId()); //删除群组离线消息

        //删除个人的群组设置
        conversationService.delete(userId, group.getId());
        logUtil.logRemoveGroupMember(group, userId);
    }


    public void remove(String currentUserId, String delMembers, Group group, List<String> successMembers, List<String> failMembers) {
        String[] members = delMembers.split(",");
        final Set<String> set = new TreeSet<String>();
        List<GroupMember> groupMembers = findMembersByGroupId(group.getId());
        for (GroupMember groupMember : groupMembers) {
            //不需要给自己发送系统
            if (groupMember.getId().getUserId().equalsIgnoreCase(currentUserId)) {
                continue;
            }
            set.add(groupMember.getId().getUserId());
        }

        Long startTime = System.currentTimeMillis();


        int number = groupMembers != null ? groupMembers.size() : 0;

        for (String groupMember : members) {
            User user = userService.findById(groupMember);
            if (user == null) {
                failMembers.add(groupMember);
                continue;
            }

            //自己不能删除自己
            if (user.getId().equals(currentUserId)) {
                failMembers.add(groupMember);
                continue;
            }
            try {
                removeUserFromGroup(group, user.getId());

                //给群组原有成员发送系统通知 不需要发送push通知
                //set包含了删除成员

                number--;
                queueService.sendGroupRemoveUserMsgToOtherMembers(set, group.getId(), user, currentUserId, number);

                successMembers.add(groupMember);
                set.remove(groupMember);

                //给本删除人发送系统通知
                queueService.sendGroupRemoveUserMsgToRemovedMembers(group, user, currentUserId, number);
            } catch (Exception e) {
                failMembers.add(groupMember);
            }
        }


        reqMetricUtil.addReqStepInfo("del group members.", String.format("user size:%s", null != members ? members.length : ""), startTime, System.currentTimeMillis());
        group.setUpdateTime(System.currentTimeMillis());
        updateGroupInfo(group);
    }


    /**
     * 获得群组成员列表
     *
     * @param groupId 群组主键
     * @return
     */
    public List<GroupUser> findGroupUserList(String groupId, final String excludeUserId) {

        List<GroupUser> result = null;

        List<GroupMember> members = findMembersByGroupId(groupId);

        if (null == members) {
            members = new ArrayList<>();
        }
        Map<String, Integer> memberNumberMap = new HashMap<>();

        List<String> userIdList = FluentIterable.from(members).filter(groupMember -> !groupMember.getId().getUserId().equals(excludeUserId))
                .transform(groupMember -> {
                    memberNumberMap.put(groupMember.getId().getUserId(), groupMember.getSeq());
                    return groupMember.getId().getUserId();
                }).toList();

        List<User> users = userService.findByIdList(userIdList);

        if (null != users) {
            result = new ArrayList<>();
            for (User user : users) {
                GroupUser groupUser = GroupUser.createFromUser(user);
                groupUser.setSeq(memberNumberMap.get(groupUser.getId()));
                result.add(groupUser);
            }
        }
        return result;
    }

    /**
     * @param groupId
     * @return
     */
    public List<GroupMember> findMembersByGroupId(String groupId) {
        Long startTime = System.currentTimeMillis();
        List<GroupMember> result = CurrentUser.db()
                .sliceQuery(GroupMember.class)
                .forSelect()
                .withPartitionComponents(groupId)
                .get(MAX_VALUE);
        reqMetricUtil.addReqStepInfo("find group members", String.format("groupId:%s", groupId), startTime, System.currentTimeMillis());
        return result;
    }


    /**
     * 邀请某个成员加入群组
     *
     * @param inviter                   邀请人
     * @param group                     群组
     * @param memberIds                 被邀请成员用户主键列表
     * @param systemNotifyExcludeUserId 排除发送系统通知的用户
     */
    public List<GroupUser> invite(final User inviter, final Group group, List<String> memberIds, String systemNotifyExcludeUserId, String reqUserId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invite...");
        }
        List<User> memberList = userService.findUserListByNames(memberIds);
        //如果用户已经在了群组中 则应该预先排除
        List<User> validMemberList = memberList.stream()
                .filter(u -> (findGroupMemberByKey(group.getId(), u.getId()) == null))
                .collect(Collectors.toList());
        return inviteAddUser(inviter, group, validMemberList, systemNotifyExcludeUserId, reqUserId);
    }

    public List<GroupUser> inviteAddUser(final User inviter, final Group group, List<User> validMemberList, String systemNotifyExcludeUserId,
                                         String reqUserId) {
        List<GroupUser> groupUsers = addUsersToGroupForInvite(inviter, group, validMemberList, systemNotifyExcludeUserId, reqUserId);

        group.setUpdateTime(System.currentTimeMillis());
        updateGroupInfo(group);
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("[group invite] invite:{},memberIds:{}, memberList size:{}, validMemberList size:{},groupId:{},groupUsers size:{}",
//                    null != inviter ? inviter.getId() : "", null != memberIds ? Arrays.toString(memberIds.toArray()) : "",
//                    null != memberList ? memberList.size() : "",
//                    null != validMemberList ? validMemberList.size() : "",
//                    null != group ? group.getId() : "", null != groupUsers ? groupUsers.size() : "");
//        }
        return groupUsers;
    }

    /**
     * 加入群组
     *
     * @param applicant 申请人
     * @param group     申请加入的群组
     */
    public GroupUser join(final User applicant, final Group group, final String operationUserId) {
        GroupUser ret = addUserToGroupForApply(applicant, group, operationUserId);
        group.setUpdateTime(System.currentTimeMillis());
        updateGroupInfo(group);
        return ret;
    }


    /**
     * 退出群组
     *
     * @param group
     */
    public void exit(final User user, final Group group) {
        this.taskService.execute(() -> {
            if (group.getCreator().equals(user.getId())) { //解散群组

                List<GroupMember> members = findMembersByGroupId(group.getId());

                group.setNumber(members != null ? members.size() : 0);
                try {
                    for (GroupMember member : members) {
                        removeUserFromGroup(group, member.getId().getUserId());
                        if (user.getId().equals(member.getId().getUserId()))
                            continue;

                        queueService.sendGroupDismissMsg(group.getCreator(), member.getId().getUserId(), group); // appId
                    }
                } catch (Exception e) {
                    LOG.error(e.toString(), e);
                }
                CurrentUser.db().delete(group);
                logUtil.logDelGroup(group, user);
            } else {
                removeUserFromGroup(group, user.getId());

                List<GroupMember> members = findMembersByGroupId(group.getId());
                final Set<String> set = new TreeSet<>();
                for (GroupMember member : members) {
                    set.add(member.getId().getUserId());
                }

                group.setNumber(members != null ? members.size() : 0);
                //发送通知
                queueService.sendGroupExitMsg(set, group, user);

                //修改头像
                group.setUpdateTime(System.currentTimeMillis());
                updateGroupInfo(group);
            }
        });

    }

    /**
     * 跟进某个用户获得用户当前加入的群组列表
     *
     * @param userId
     * @return
     */
    public List<Group> findGroupsByUserId(String userId) {
        final List<UserGroupIndex> indexes = manager.sliceQuery(UserGroupIndex.class)
                .forSelect()
                .withPartitionComponents(userId)
                .get(MAX_VALUE);

        List<Group> listGroup = new ArrayList();
        final Map<String, Conversation> conversationMap = conversationService.findByUserId(userId);

        for (UserGroupIndex userGroupIndex : indexes) {
            Conversation c = conversationMap.get(userGroupIndex.getId().getGroupId());
            Group group = findGroupInfo(userGroupIndex.getId().getGroupId());
            if (group != null) {
                group.setConversation(c == null ? 0 : c.getType());
                listGroup.add(group);
            }

        }
        return listGroup;
    }


    public GroupMember findGroupMemberByKey(String groupId, String userId) {
        return CurrentUser.db().find(GroupMember.class, new GroupKey(groupId, userId));
    }

    public String getGroupDafaultAvatarByRandom() {
        String result = "";
        if (null != groupDefaultAvatars) {
            result = groupDefaultAvatars[currentGroupIndex.getAndIncrement() % groupDefaultAvatars.length];
        }
        return result;
    }

    public void handleGroupMember(Integer appId, String memers, List<String> canAddToGroupMembers, List<FailUserInfo> failAddToGroupMembers) {
        String[] members = memers.split(",");
        Long startTime = System.currentTimeMillis();

        if (appId == null || appId == 0) {
            for (String toAddUser : members) {
                boolean notSupportGroup = false;
                String ua = null;
                String clientVersion = null;
                //TODO 批量获取待添加成员的版本号
                UserBehaviouAttr userBehaviouAttr = userService.getUserBehaviou(toAddUser, appId);
                if (null == userBehaviouAttr) {
                    notSupportGroup = true;
                } else {
                    //android判断
                    ua = userBehaviouAttr.getUa();
                    clientVersion = userBehaviouAttr.getClientVersion();
                    if (null != clientVersion) {
                        if ("chatgame-3.1".compareTo(clientVersion) > 0) {
                            notSupportGroup = true;
                        }
                    } else {
                        notSupportGroup = true;
                    }
                }

                if (notSupportGroup) {
                    FailUserInfo failUserInfo = new FailUserInfo();
                    failUserInfo.setUserId(UserUtils.genOpenUserId(toAddUser, appId));
                    if (null != ua) {
                        if (ua.toLowerCase().contains("android")) {
                            failUserInfo.setOs("android");
                        } else {
                            failUserInfo.setOs("iso");
                        }
                    }
                    if (null != clientVersion) {
                        failUserInfo.setClientVersion(clientVersion);
                    }
                    failAddToGroupMembers.add(failUserInfo);
                } else {
                    //发送消息使用,所以不需要转换结果
                    canAddToGroupMembers.add(toAddUser);
                }
            }
        } else {
            for (String member : members) {
                canAddToGroupMembers.add(member);
            }
        }

        reqMetricUtil.addReqStepInfo("handle group member", "some members of low version can not support group.", startTime, System.currentTimeMillis());
    }

    /**
     * 校验用户的版本是否支持加群组
     */
    public boolean supportGroup(Integer appId, String userId) {
        return support(appId, userId, "chatgame-3.1");
    }

    public boolean supportGroupApply(Integer appId, String userId) {
        return support(appId, userId, "chatgame-3.3");
    }

    private boolean support(Integer appId, String userId, String leastVersion) {
        if (appId != null && appId != 0) {
            return true;
        }
        if (null == leastVersion) {
            return false;
        }
        UserBehaviouAttr userBehaviouAttr = userService.getUserBehaviou(userId, appId);
        if (null == userBehaviouAttr) {
            return false;
        } else {
            String clientVersion = userBehaviouAttr.getClientVersion();
            if (null != clientVersion) {
                if (leastVersion.compareTo(clientVersion) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param inviter
     * @param members
     */
    public void sendGroupInviteAuditSystemNotify(String inviter, List<String> members, String groupCreatorId, Group group) {
        List<User> users = userService.findByIdList(members);
        User groupCreator = userService.findById(groupCreatorId);
        if (null != users && users.size() > 0 && null != groupCreator) {
            Long startTime = System.currentTimeMillis();
            queueService.sendGroupInviteAuditMsg(inviter, users, groupCreator, group);
            reqMetricUtil.addReqStepInfo("send group invite audit msg.", "", startTime, System.currentTimeMillis());
        }
    }

    /**
     * 给群主发送的申请信息
     *
     * @param content 申请理由
     */
    public void sendGroupApplyAuditSystemNotify(User applicant, Group group, String content) {
        User groupCreator = userService.findById(group.getCreator());
        if (null != groupCreator) {
            Long startTime = System.currentTimeMillis();
            //新老版本区分
            if (supportGroupApply(groupCreator.getAppId(), groupCreator.getId())) {
                //新版本发送内容要包括申请理由
                queueService.sendGroupApplyAuditMsg(applicant, groupCreator, group, content);
            } else {
                //老版本发送内容使用邀请协议，邀请人和被邀请人都是申请人
                List<User> members = Lists.newArrayList(applicant);
                queueService.sendGroupInviteAuditMsg(applicant.getId(), members, groupCreator, group);
            }
            reqMetricUtil.addReqStepInfo("send group apply audit msg.", "", startTime, System.currentTimeMillis());
        }
    }

    /**
     * 用户重复创建群组(支持创建群组的幂等操作)
     *
     * @param group
     * @param currentUser
     * @param name
     * @param member
     * @param avatar
     * @param enableValidate
     * @param desc
     * @return
     */
    public GroupContainFailUserVo reCreateGroup(Group group, User currentUser, String name, String member, String avatar, String enableValidate, String desc) {
        GroupContainFailUserVo vo;
        //修改群组的元信息
        if (null != name) {
            group.setName(name);
        }
        if (null != avatar) {
            group.setAvatarUrl(avatar);
        }

        if (null != enableValidate) {
            if ("yes".equalsIgnoreCase(enableValidate)) {
                group.setEnableValidate("yes");
            } else {
                group.setEnableValidate("no");
            }
        }

        if (null != desc) {
            group.setDesc(desc);
        }

        //更新群组
        updateGroupInfo(group);
        //成员版本判断
        List<String> canAddToGroupMembers = new ArrayList<>();
        List<FailUserInfo> failAddToGroupMembers = new ArrayList<>();
        handleGroupMember(currentUser.getAppId(), member, canAddToGroupMembers, failAddToGroupMembers);

        //排除群组中已经存在的成员
        List<GroupMember> groupMembers = findMembersByGroupId(group.getId());
        List<String> canStillAddGroupUsers = null;
        if (null != canAddToGroupMembers && canAddToGroupMembers.size() > 0) {
            final List<String> addUsersExcludesGroupMembers = new ArrayList<>();
            if (null != groupMembers && groupMembers.size() > 0) {
                canAddToGroupMembers.stream().filter(userId -> {
                    boolean result = true;
                    for (GroupMember groupMember : groupMembers) {
                        if (userId.equals(groupMember.getId().getUserId())) {
                            result = false;
                            break;
                        }
                    }
                    return result;
                }).forEach(useId -> addUsersExcludesGroupMembers.add(useId));
                canStillAddGroupUsers = addUsersExcludesGroupMembers;
            } else {
                canStillAddGroupUsers = canAddToGroupMembers;
            }
        }

        if (null != canStillAddGroupUsers && canStillAddGroupUsers.size() > 0) {
            //继续添加成员
            invite(currentUser, group, canStillAddGroupUsers, currentUser.getId(), currentUser.getId());
        }

        //给创建者返回群组信息 包含添加失败
        vo = GroupContainFailUserVo.createFromGroup(findGroupInfo(group.getId()));
        vo.setMembers(findGroupUserList(group.getId(), null));
        vo.setFail(failAddToGroupMembers);
        return vo;
    }


    /**
     * 用户创建群组
     * 给被添加的用户发送群组创建系统通知
     *
     * @param currentUser
     * @param name
     * @param member
     * @param avatar_url
     * @param enable_validate
     * @param desc
     * @return
     */
    public GroupContainFailUserVo createGroup(User currentUser, String name, String member, String avatar_url, String enable_validate, String desc) {
        Group group = new Group();
        group.setId(GroupUtil.genGroupID(currentUser.getAppId()));
        group.setName(StringUtils.isNotBlank(name) && name.length() > 100 ? name.substring(0, 90) + "..." : name);
        group.setCreator(currentUser.getId());
        group.setCreateTime(new Date());
        group.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotBlank(avatar_url)) {
            group.setAvatarUrl(avatar_url);
        } else {
            //设置默认头像
            group.setAvatarUrl(getGroupDafaultAvatarByRandom());
        }

        if ("yes".equalsIgnoreCase(enable_validate)) {
            group.setEnableValidate("yes");
        } else {
            group.setEnableValidate("no");
        }

        if (StringUtils.isNotBlank(desc)) {
            group.setDesc(desc);
        } else {
            //使用默认的简介
            group.setDesc(messageSourceService.getMessageSource(currentUser.getAppId()).getMessage("group.desc.default", new Object[]{}, currentUser.getLocale()));
        }

        group.setRegion(currentUser.getCountrycode());
        group.setAccount(generateGroupAccount(currentUser.getCountrycode(), group.getId()));


        //成员版本判断
        List<String> canAddToGroupMembers = new ArrayList<>();
        List<FailUserInfo> failAddToGroupMembers = new ArrayList<>();
        handleGroupMember(currentUser.getAppId(), member, canAddToGroupMembers, failAddToGroupMembers);

        //对添加成员的校验
        createGroup(group, currentUser, ImmutableSet.copyOf(canAddToGroupMembers));

        GroupContainFailUserVo vo = GroupContainFailUserVo.createFromGroup(group);
        if (null != failAddToGroupMembers && failAddToGroupMembers.size() > 0) {
            vo.setFail(failAddToGroupMembers);
        }
        List<GroupUser> openGroupUsers = group.getMembers().stream().map(groupUser -> {
            groupUser.setId(UserUtils.genOpenUserId(groupUser.getId(), currentUser.getAppId()));
            return groupUser;
        }).collect(Collectors.toList());
        vo.setMembers(openGroupUsers);
        vo.setCreator(UserUtils.genOpenUserId(vo.getCreator()));
        return vo;
    }

    public GroupVo createOpenGroup(User owner, String name, String member, String desc, String avatar, String groupId) {
        Group group = new Group();
        if (Strings.isEmpty(groupId)) {
            groupId = GroupUtil.genGroupID(owner.getAppId());
        }
        group.setId(UserUtils.genInternalUserId(groupId, owner.getAppId()));
        group.setName(StringUtils.isNotBlank(name) && name.length() > 100 ? name.substring(0, 90) + "..." : name);
        group.setCreator(owner.getId());
        group.setCreateTime(new Date());
        group.setUpdateTime(System.currentTimeMillis());
        group.setEnableValidate("no");
        if (StringUtils.isNotBlank(avatar)) {
            group.setAvatarUrl(avatar);
        }
        if (StringUtils.isNotBlank(desc)) {
            group.setDesc(desc);
        } else {
            //使用默认的简介
            group.setDesc(messageSourceService.getMessageSource(owner.getAppId()).getMessage("group.desc.default", new Object[]{}, owner.getLocale()));
        }

        group.setRegion(owner.getCountrycode());
        group.setAccount(generateGroupAccount(owner.getCountrycode(), group.getId()));
        Set<String> groupMember = Stream.of(member.split(",")).collect(Collectors.toSet());
        createGroup(group, owner, ImmutableSet.copyOf(groupMember));
        group.setNumber(groupMember.size() + 1);
        GroupVo groupVo = new GroupVo();
        BeanUtils.copyProperties(group, groupVo);
        List<OpenGroupUser> openGroupUsers = group.getMembers().stream().map(groupUser -> {
            OpenGroupUser openOpenGroupUser = new OpenGroupUser();
            BeanUtils.copyProperties(groupUser, openOpenGroupUser);
            openOpenGroupUser.setId(UserUtils.genOpenUserId(openOpenGroupUser.getId(), owner.getAppId()));
            return openOpenGroupUser;
        }).collect(Collectors.toList());
        groupVo.setId(UserUtils.genOpenUserId(groupVo.getId(), owner.getAppId()));
        groupVo.setCreator(UserUtils.genOpenUserId(groupVo.getCreator(), owner.getAppId()));
        groupVo.setMember(openGroupUsers);
        groupVo.setId(GroupUtil.openID(groupVo.getId(), owner.getAppId()));
        return groupVo;
    }


    /**
     * @param uuid
     * @return
     */
    public Group getGroupByCreateReqUuid(String uuid) {
        PersistenceManager realManager = CurrentUser.db();
        Group result = null;
        GroupCreateReqIndex groupCreateReqIndex = realManager.find(GroupCreateReqIndex.class, uuid);
        if (null != groupCreateReqIndex) {
            result = findGroupInfo(groupCreateReqIndex.getGroupId());
        }
        return result;
    }


    public void saveGroupCreateReqIndex(String uuid, String groupId) {
        GroupCreateReqIndex index = new GroupCreateReqIndex();
        index.setUuid(uuid);
        index.setGroupId(groupId);
        manager.insert(index);
    }


    /**
     * 获取群组对应的账号 按照国家唯一
     *
     * @param countryCode
     * @param groupId
     * @return
     */
    public String generateGroupAccount(String countryCode, String groupId) {

        String shortCountryCode = getShortCountryCode(countryCode);
        String groupAccount = null;

        Long startTime = System.currentTimeMillis();
        //循环次数
        int countNum = 0;
        for (; countNum < 5; countNum++) {
            GroupAccountCount accountCount = manager.find(GroupAccountCount.class, shortCountryCode);
            if (accountCount == null) {
                accountCount = new GroupAccountCount();
                accountCount.setCountry(shortCountryCode);
                accountCount.setCount(CounterBuilder.incr(1));
                try {
                    manager.insert(accountCount);
                } catch (Exception e) {
                    LOG.warn("fails to init basic value", e);
                    //忽略此异常，有可能存在并发操作，值已经存在
                }
                continue;
            }
            accountCount.getCount().incr();
            manager.update(accountCount);
            //检测碰撞
            try {
                GroupAccountIndex groupAccountIndex = createGroupAccountIndex(genGroupAccount(accountCount.getCount().get(), shortCountryCode), groupId);
                groupAccount = groupAccountIndex.getAccount();
                break;
            } catch (Exception e) {
                LOG.warn(String.format("group account conflick. countryCode:%s, groupId:%s,account:%s", countryCode, groupId, accountCount.getCount().get()), e);
                continue;
            }
        }
        reqMetricUtil.addReqStepInfo("generate group account.", String.format("groupId:%s,times:%s", groupId, countNum), startTime, System.currentTimeMillis());

        if (StringUtils.isBlank(groupAccount)) {
            throw new ServerException(StatusCode.INNER_ERROR, "can not generate group accout.");
        }
        return groupAccount;
    }


    /**
     * 创建账号索引
     */
    public GroupAccountIndex createGroupAccountIndex(String account, String groupId) {
        GroupAccountIndex accountIndex = new GroupAccountIndex();
        accountIndex.setAccount(account);
        accountIndex.setGroupId(groupId);
        manager.insert(accountIndex, OptionsBuilder.ifNotExists().lwtLocalSerial());
        return accountIndex;
    }

    protected String genGroupAccount(Long basicValue, String shortCountryCode) {
        if (null == basicValue || StringUtils.isBlank(shortCountryCode)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            return uuid.substring(uuid.length() - 6, uuid.length());
        }

        String formatBasicValue = basicValue < 10000 && basicValue > 0 ? String.format("%05d", basicValue) : String.valueOf(basicValue);


        return String.format("%s%s", shortCountryCode, formatBasicValue);
    }

    protected String getShortCountryCode(String countryCode) {
        int firstIndex = 0;
        for (int i = 0; i < countryCode.length(); i++) {
            if (countryCode.charAt(i) > '0' && countryCode.charAt(i) <= '9') {
                firstIndex = i;
                break;
            }
        }
        String formatCountryCode = countryCode.substring(firstIndex, countryCode.length());
        return formatCountryCode;
    }


    public boolean groupMemberOverLimit(String groupId, int count) {
        PersistenceManager realManager = CurrentUser.db();
        User user = CurrentUser.user();
        PreparedStatement ps = user.getAppId() > SystemConstants.CG_APP_ID_MAX ? groupMemberCountStatementOfOp : groupMemberCountStatement;
        boolean result = false;
        long memberCount = 0;
        try {
            if (StringUtils.isNotBlank(groupId)) {
                ResultSet resultSet = realManager.getNativeSession().execute(new BoundStatement(ps).bind(groupId)
                        .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM));
                if (null != resultSet) {
                    Row row = resultSet.one();
                    if (null != row) {
                        memberCount = row.getLong(0);
                    }
                }
            }
            LOG.debug(String.format("group member count.groupId:%s, count:%d", groupId, memberCount));
        } catch (Exception e) {
            LOG.error(String.format("fails to get group member count.groupId:%s", groupId), e);
        }
        if (memberCount + count > groupMemberCountLimit) {
            result = true;
        }

        return result;
    }


    public Group getGroupInfo(String userId, String groupId, String exclude, Integer appId) {
        //查询群组
        Group group = findGroupInfo(groupId, appId);
        if (null == group) {
            return group;
        }

        List<GroupUser> member = findGroupUserList(groupId, "");
        if (exclude == null || "".equals(exclude) || !exclude.contains("member")) {
            group.setMembers(member);
        }
        group.setNumber(member.size());

        //conversation
        Conversation conversation = conversationService.findConversationByEntityId(userId, groupId);
        if (null == conversation) {
            group.setConversation(0);
        } else {
            group.setConversation(conversation.getType());
        }
        return group;
    }

    public Group getGroupInfo(String userId, String groupId, String exclude) {
        //查询群组
        Group group = findGroupInfo(groupId);
        if (null == group) {
            return group;
        }

        List<GroupUser> member = findGroupUserList(groupId, "");
        if (exclude == null || "".equals(exclude) || !exclude.contains("member")) {
            group.setMembers(member);
        }
        group.setNumber(member.size());

        //conversation
        Conversation conversation = conversationService.findConversationByEntityId(userId, groupId);
        if (null == conversation) {
            group.setConversation(0);
        } else {
            group.setConversation(conversation.getType());
        }
        return group;
    }

    public Group getGroupInfoFromOtherRegion(String groupId, String region, String userId, Integer appId) {
        Group group = null;

        //客户端兼容 后期客户端升级后 可以只根据region获取群组所在的区域信息
        String[] serverUrls = coreServerReqUtil.getCoreServerUrlWithoutLocalUrl(region);
        LOG.info("other region coreServer url. region:{},serverUrl:{}", region, Arrays.toString(serverUrls)
        );

        if (null != serverUrls) {
            Map<String, String> parMap = new HashMap<>();
            parMap.put("groupId", groupId);
            parMap.put("reqUserId", userId);
            parMap.put("appId", String.valueOf(appId));
            parMap.put("auth", DigestUtils.md5DigestAsHex(String.format("%s%s", groupId, authKey).getBytes()));
            for (int i = 0; i < serverUrls.length; i++) {
                if (i >= 2) {
                    //最多请求两次
                    break;
                }
                String serverUrl = serverUrls[i];
                String result;
                try {
                    result = httpService.doGet(String.format("%s%s", serverUrl, "/api/auth/group/get"), parMap);
                } catch (Throwable e) {
                    LOG.warn(String.format("fails to req", String.format("%s%s", serverUrl, "/api/auth/group/get")), e);
                    //抛异常 再请求一次
                    continue;
                }
                LOG.debug("req:{} info:{}", String.format("%s%s", serverUrl, "/api/auth/group/get"), result);
                //没有异常 说明请求正常
                //返回的不是group对象 则说明群组不存在
                try {
                    group = objectMapper.readValue(result, Group.class);
                    if (null != group) {
                        if (StringUtils.isBlank(group.getId())) {
                            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
                        }
                    }
                } catch (Exception e) {
                    LOG.warn(String.format("fails to parse result.%s", result), e);
                    throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
                }
                break;
            }
        }
        return group;
    }

    public PersistenceManager getRealManager(int appId) {
        return appId > SystemConstants.CG_APP_ID_MAX ? opManager : manager;
    }
}
