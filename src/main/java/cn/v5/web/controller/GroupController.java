package cn.v5.web.controller;

import cn.v5.bean.group.FailUserInfo;
import cn.v5.bean.group.GroupContainFailUserVo;
import cn.v5.bean.group.GroupUser;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.entity.*;
import cn.v5.metric.LogUtil;
import cn.v5.service.ConversationService;
import cn.v5.service.GroupService;
import cn.v5.service.MessageSourceService;
import cn.v5.service.UserService;
import cn.v5.util.GroupUtil;
import cn.v5.util.ReqMetricUtil;
import cn.v5.util.UserUtils;
import cn.v5.validation.Validate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sf.oval.constraint.NotEmpty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hi on 14-3-7.
 */
@Controller
@Validate
@RequestMapping(value = "/api", produces = "application/json")
public class GroupController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);
    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @Inject
    private GroupService groupService;

    @Autowired
    private MessageSourceService messageSourceService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReqMetricUtil reqMetricUtil;

    @Value("${group.member.count.limit}")
    private int groupMemberCountLimit;

    @Autowired
    private LogUtil logUtil;


    /**
     * 创建群组
     * 携带了uuid 接口，需要保证此接口的幂等性 根据uuid找到群组 然后把不同的用户添加到群组中
     */
    @RequestMapping(value = "/group/create", method = RequestMethod.POST)
    @ResponseBody
    public GroupContainFailUserVo create(String name, @NotNull @NotEmpty String member, String avatar_url, String enable_validate, String desc, String uuid) {
        User currentUser = CurrentUser.user();
        member = Stream.of(member.split(",")).map(k -> UserUtils.genInternalUserId(k, currentUser.getAppId())).collect(Collectors.joining(","));

        GroupContainFailUserVo vo = null;
        boolean groupExist = false;
        if (StringUtils.isNotBlank(uuid)) {
            Long startTime = System.currentTimeMillis();
            Group group = groupService.getGroupByCreateReqUuid(uuid);
            reqMetricUtil.addReqStepInfo("get group info", "find create req index by uuid. then get group info", startTime, System.currentTimeMillis());
            if (null != group) {
                groupExist = true;
                //重复创建 群组 支持幂等操作
                vo = groupService.reCreateGroup(group, currentUser, name, member, avatar_url, enable_validate, desc);
            }
        }

        // 首次创建群组
        if (!groupExist) {
            if (groupService.groupMemberOverLimit(null, member.split(",").length + 1)) {
                throw new ExtraInfoServerException(StatusCode.GROUP_MEMBER_LIMIT, "group member count over limit").withInfo("limit", groupMemberCountLimit);
            }

            vo = groupService.createGroup(currentUser, name, member, avatar_url, enable_validate, desc);
            if (StringUtils.isNotBlank(uuid)) {
                try {
                    Long startTime = System.currentTimeMillis();
                    groupService.saveGroupCreateReqIndex(uuid, vo.getId());
                    reqMetricUtil.addReqStepInfo("save create group index", "save uuid --> groupId", startTime, System.currentTimeMillis());
                } catch (Exception e) {
                    //出错不影响业务运行
                    LOGGER.error(String.format("fails to save group create req index. uuid:%s,groupId:%s", uuid, vo.getId()), e);
                }
            }
        }

        if (vo != null) {
            vo.setCreator(GroupUtil.openID(currentUser.getId(), currentUser.getAppId()));
            vo.setId(GroupUtil.openID(vo.getId(), currentUser.getAppId()));
        }

        return vo;
    }


    /**
     * 更新群组信息：名称
     *
     * @param groupId 组ID
     * @param name    组名
     */
    @RequestMapping(value = "/group/update", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> update(@NotNull String groupId, String name, String avatar_url, String enable_validate, String desc, String top, String no_disturb) {
        if (null == name && null == avatar_url && null == enable_validate && null == desc && null == top && null == no_disturb) {
            return SUCCESS_CODE;
        }

        User currentUser = CurrentUser.user();
        Group group = groupService.findGroupInfo(groupId);
        if (group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST,
                    "the group does not exist.");
        }

        boolean modifyGroupInfo = false;
        if (null != name) {
            group.setName(name);
            modifyGroupInfo = true;
        }

        if (null != avatar_url) {
            group.setAvatarUrl(avatar_url);
            modifyGroupInfo = true;
        }
        if (null != enable_validate) {
            if ("yes".equalsIgnoreCase(enable_validate)) {
                group.setEnableValidate("yes");
            } else {
                group.setEnableValidate("no");
            }
            modifyGroupInfo = true;
        }
        if (null != desc) {
            group.setDesc(desc);
            modifyGroupInfo = true;
        }

        if (modifyGroupInfo) {
            //不是群组的创建者不能修改群组基本信息
            if (!group.getCreator().equals(currentUser.getId())) {
                throw new ServerException(StatusCode.GROUP_OPT_NOT_ALLOW, messageSourceService.getMessageSource(currentUser.getAppId()).getMessage("permission.denied", new Object[]{}, currentUser.getLocale()));
            } else {
                group.setUpdateTime(System.currentTimeMillis());

                Long startTime = System.currentTimeMillis();
                groupService.update(group);
                reqMetricUtil.addReqStepInfo("update group", "", startTime, System.currentTimeMillis());
            }
        }

        //修改群组的个人设置
        if (null != top || null != no_disturb) {
            //修改群组的conversation
            Long startTime = System.currentTimeMillis();
            Conversation conversation = conversationService.findConversationByEntityId(currentUser.getId(), group.getId());
            reqMetricUtil.addReqStepInfo("find group conversation", "", startTime, System.currentTimeMillis());
            if (null == conversation) {
                conversation = new Conversation();
                ConversationKey key = new ConversationKey();
                key.setUserId(currentUser.getId());
                key.setEntityId(group.getId());
                conversation.setId(key);
                conversation.setType(0);
                conversation.setCreateTime(System.currentTimeMillis());

            }
            int type = conversation.getType();
            if (null != top) {
                //设置置顶
                if ("yes".equalsIgnoreCase(top)) {
                    type = type | 0x04;
                } else {
                    //设置为不置顶
                    type = type & (~0x04);
                }
            }
            if (null != no_disturb) {
                //设置免打扰
                if ("yes".equalsIgnoreCase(no_disturb)) {
                    type = type | 0x10;
                } else {
                    //设置为不置顶
                    type = type & (~0x10);
                }
            }
            conversation.setType(type);

            startTime = System.currentTimeMillis();
            conversationService.saveOrUpdateConversation(conversation);
            reqMetricUtil.addReqStepInfo("update group conversation", "", startTime, System.currentTimeMillis());
        }

        return SUCCESS_CODE;
    }


    /**
     * 获取群组信息。群组名、头像、成员列表
     *
     * @param groupId 组ID
     * @param region
     */
    @RequestMapping(value = "/group/get", method = RequestMethod.GET)
    @ResponseBody
    public Group get(@NotNull @NotEmpty String groupId, String exclude, String region) {
        User currentUser = CurrentUser.user();
        Integer appId = currentUser.getAppId();
        // FIXME: 16/8/11
        if (appId > SystemConstants.CG_APP_ID_MAX) {
            groupId = UserUtils.genInternalUserId(groupId, appId);
        }

        Group group = groupService.getGroupInfo(currentUser.getId(), groupId, exclude);

        if (group == null) {
            Group groupFromOtherRegion = groupService.getGroupInfoFromOtherRegion(groupId, region, currentUser.getId(), appId);
            LOGGER.debug("[group info] from other region. groupId:{}, group:{}", groupId, groupFromOtherRegion);
            if (null == groupFromOtherRegion) {
                throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
            } else {
                return groupFromOtherRegion;
            }
        }
        return UserUtils.genOpenGroup(group, currentUser.getAppId());

    }


    /**
     * 退出群组，如果群主退出，则注销群
     */
    @RequestMapping(value = "/group/exit", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> exit(@NotNull String groupId) {
        User you = CurrentUser.user();
        groupId = UserUtils.genInternalUserId(groupId, you.getAppId());
        //查询群组
        Group group = groupService.findGroupInfo(groupId);
        if (groupId == null || group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }

        groupService.exit(you, group);
        return SUCCESS_CODE;
    }


    /**
     * 邀请新用户加入群组
     * 给新用户发送群组全部的成员信息
     * 给老用户发送新增加的成员信息
     */
    @RequestMapping(value = "/group/invite", method = RequestMethod.POST)
    @ResponseBody

    public Map<String, Object> invite(@NotNull @NotEmpty String groupId, @NotNull @NotEmpty String member) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = CurrentUser.user();
        Integer appId = currentUser.getAppId();
        //群组基本信息
        groupId = UserUtils.genInternalUserId(groupId, appId);
        member = Stream.of(member.split(",")).map(m -> UserUtils.genInternalUserId(m, appId)).collect(Collectors.joining(","));

        Group group = groupService.findGroupInfo(groupId);
        if (groupId == null || group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }

        GroupMember groupMember = groupService.findGroupMemberByKey(groupId, currentUser.getId());
        if (groupMember == null) {
            throw new ServerException(StatusCode.GROUP_OPT_NOT_ALLOW, messageSourceService.getMessageSource(appId).getMessage("permission.denied", new Object[]{}, currentUser.getLocale()));
        }

        if (groupService.groupMemberOverLimit(groupId, member.split(",").length)) {
            logUtil.logFailsToAddGroupMemberForOverlimit(group, member, "/group/invite");
            throw new ExtraInfoServerException(StatusCode.GROUP_MEMBER_LIMIT, "group member count over limit").withInfo("limit", groupMemberCountLimit);
        }


        List<GroupMember> gms = groupService.findMembersByGroupId(groupId);

        int numbers = gms != null ? gms.size() : 0;

        group.setNumber(numbers + 1);


        //成员版本判断
        List<String> canAddToGroupMembers = new ArrayList<>();
        List<FailUserInfo> failAddToGroupMembers = new ArrayList<>();
        groupService.handleGroupMember(currentUser.getAppId(), member, canAddToGroupMembers, failAddToGroupMembers);

        //开启了群组校验 且不是群组创建者添加成员时
        if ("yes".equalsIgnoreCase(group.getEnableValidate()) && (!currentUser.getId().equals(group.getCreator()))) {
            result.put("error_code", StatusCode.GROUP_VERIFY);
            result.put("error", "group need to verify");
            result.put("fail", failAddToGroupMembers.toArray());
            //开启了群组校验 需要给群组创建者 发送校验系统通知
            groupService.sendGroupInviteAuditSystemNotify(currentUser.getId(), canAddToGroupMembers, group.getCreator(), group);
        } else {
            List<GroupUser> sucessToAddGroupUsers = null;
            if (null != canAddToGroupMembers && canAddToGroupMembers.size() > 0) {
                sucessToAddGroupUsers = groupService.invite(currentUser, group, canAddToGroupMembers, currentUser.getId(), currentUser.getId());
            }
            result.put("error_code", 2000);
            failAddToGroupMembers = failAddToGroupMembers.stream().map(m -> {
                m.setUserId(UserUtils.genOpenUserId(m.getUserId(), appId));
                return m;
            }).collect(Collectors.toList());
            if (null != failAddToGroupMembers && failAddToGroupMembers.size() > 0) {
                result.put("fail", failAddToGroupMembers.toArray());
            }
            sucessToAddGroupUsers = sucessToAddGroupUsers.stream().map(m -> {
                m.setId(UserUtils.genOpenUserId(m.getId(), appId));
                return m;
            }).collect(Collectors.toList());
            if (null != sucessToAddGroupUsers && sucessToAddGroupUsers.size() > 0) {
                result.put("success", sucessToAddGroupUsers.toArray());
            }
        }
        return result;
    }

    /**
     * 移除用户
     *
     * @param groupId 组ID
     */
    @RequestMapping(value = "/group/remove", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> remove(@NotNull String groupId, String member) {
        User you = CurrentUser.user();
        groupId = UserUtils.genInternalUserId(groupId, you.getAppId());
        member = Stream.of(member.split(",")).map(m -> UserUtils.genInternalUserId(m, you.getAppId())).collect(Collectors.joining(","));

        //查询群组
        Group group = groupService.findGroupInfo(groupId);
        if (group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }

        if (!group.getCreator().equals(you.getId())) {
            throw new ServerException(StatusCode.GROUP_OPT_NOT_ALLOW, messageSourceService.getMessageSource(you.getAppId()).getMessage("permission.denied", new Object[]{}, you.getLocale()));
        }

        List<String> successMembers = new ArrayList<>();
        List<String> failMembers = new ArrayList<>();


        if (StringUtils.isNotBlank(member)) {
            groupService.remove(you.getId(), member, group, successMembers, failMembers);

        }

        Map<String, Object> result = new HashMap<>();
        result.put("error_code", StatusCode.SUCCESS);
        successMembers = successMembers.stream().map(m -> {
            m = UserUtils.genOpenUserId(m, you.getAppId());
            return m;
        }).collect(Collectors.toList());

        if (null != successMembers && successMembers.size() > 0) {
            result.put("success", successMembers);
        }
        failMembers = failMembers.stream().map(m -> {
            m = UserUtils.genOpenUserId(m, you.getAppId());
            return m;
        }).collect(Collectors.toList());

        if (null != failMembers && failMembers.size() > 0) {
            result.put("fail", failMembers);
        }

        return result;
    }

    /**
     * 群主审查被他人邀请的新成员
     *
     * @param groupId
     * @param approve
     * @param refuse
     * @param inviter
     * @return
     */
    @RequestMapping(value = "/group/audit", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> inviteAudit(@NotEmpty @NotNull String groupId, String approve, String refuse, @NotEmpty @NotNull String inviter) {
        User currentUser = CurrentUser.user();

        if (StringUtils.isBlank(approve) && StringUtils.isBlank(refuse)) {
            throw new ServerException(StatusCode.PARAMETER_ERROR, "approve and refuse should not be empty together");
        }

        Group group = groupService.findGroupInfo(groupId);
        if (null == group) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "group not exist.");
        }

        User inviteUser = userService.findById(inviter);
        if (null == inviter) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "inviter not exist.");
        }

        if (groupService.groupMemberOverLimit(groupId, approve.split(",").length)) {
            logUtil.logFailsToAddGroupMemberForOverlimit(group, approve, "/group/audit");
            throw new ExtraInfoServerException(StatusCode.GROUP_MEMBER_LIMIT, "group member count over limit").withInfo("limit", groupMemberCountLimit);
        }


        Map<String, Object> result = new HashMap<>();
        result.put("error_code", StatusCode.SUCCESS);
        if (StringUtils.isNotBlank(approve)) {
            List<User> memberList = userService.findUserListByNames(Arrays.asList(approve.split(",")));
            List<User> validMemberList = Lists.newArrayList();
            List<GroupUser> oldMembers = Lists.newArrayList();
            for (User u : memberList) {
                GroupMember groupMember = groupService.findGroupMemberByKey(group.getId(), u.getId());
                if (Objects.isNull(groupMember)) {
                    validMemberList.add(u);
                } else {
                    GroupUser groupUser = GroupUser.createFromUser(u);
                    groupUser.setSeq(groupMember.getSeq());
                    oldMembers.add(groupUser);
                }
            }

            List<GroupUser> sucessToAddGroupUsers =
                    groupService.inviteAddUser(inviteUser, group, validMemberList, currentUser.
                            getId(), currentUser.getId());
            result.put("members", sucessToAddGroupUsers);
            //返回群组中已经存在的用户
            result.put("old_members", oldMembers);
        }

        result.put("inviter", inviter);
        return result;
    }

    /**
     * 群主同意申请
     *
     * @param groupId     群组id
     * @param applicantId 申请人
     * @return
     */
    @RequestMapping(value = "/group/audit/apply", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> acceptApply(@NotEmpty @NotNull String groupId, @NotEmpty @NotNull String applicantId) {
        Map<String, Object> ret = Maps.newHashMap();
        User you = CurrentUser.user();
        //validate group info
        Group group = groupService.findGroupInfo(groupId);
        if (null == group) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "group not exist.");
        }
        if (!you.getId().equalsIgnoreCase(group.getCreator())) {
            throw new ServerException(StatusCode.GROUP_OPT_NOT_ALLOW, "you do not have the authority");
        }
        if (groupService.groupMemberOverLimit(groupId, 1)) {
            logUtil.logFailsToAddGroupMemberForOverlimit(group, applicantId, "/group/audit/apply");
            throw new ExtraInfoServerException(StatusCode.GROUP_MEMBER_LIMIT, "group member count over limit").withInfo("limit", groupMemberCountLimit);
        }
        //validate applicant info
        User applicant = userService.findById(applicantId);
        if (applicant == null) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "invalid user id");
        }
        GroupMember groupMember = groupService.findGroupMemberByKey(groupId, applicant.getId());
        if (groupMember != null) {
            throw new ServerException(StatusCode.USER_ALREADY_IN_GROUP, "user has already joined the group");
        }
        GroupUser groupUser = groupService.join(applicant, group, you.getId());
        ret.put("error_code", StatusCode.SUCCESS);
        List<GroupUser> members = Lists.newArrayList(groupUser);
        ret.put("members", members);
        ret.put("inviter", applicantId);
        return ret;
    }

    /**
     * 申请加入群组
     *
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/group/apply", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> apply(@NotNull @NotEmpty String groupId, String content) {
        //检查群是否存在
        Group group = groupService.findGroupInfo(groupId);
        if (group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exits.");
        }
        //检查用户是否已经在群里
        User you = CurrentUser.user();
        GroupMember groupMember = groupService.findGroupMemberByKey(groupId, you.getId());
        if (groupMember != null) {
            throw new ServerException(StatusCode.USER_ALREADY_IN_GROUP, "user has already joined the group");
        }
        //检查用户版本是否满足条件
        if (!groupService.supportGroup(you.getAppId(), you.getId())) {
            throw new ServerException(StatusCode.CLIENT_NEEDS_UPGRADE, "current version does not support group feature");
        }
        //申请入群一直需要校验
        groupService.sendGroupApplyAuditSystemNotify(you, group, content);
        return SUCCESS_CODE;
    }


}
