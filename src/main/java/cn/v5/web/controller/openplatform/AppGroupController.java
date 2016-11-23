package cn.v5.web.controller.openplatform;

import cn.v5.bean.group.FailUserInfo;
import cn.v5.bean.group.GroupUser;
import cn.v5.bean.openplatform.GroupVo;
import cn.v5.bean.openplatform.OpenGroupUser;
import cn.v5.code.StatusCode;
import cn.v5.entity.*;
import cn.v5.metric.LogUtil;
import cn.v5.service.ConversationService;
import cn.v5.service.GroupService;
import cn.v5.service.MessageSourceService;
import cn.v5.service.UserService;
import cn.v5.util.ReqMetricUtil;
import cn.v5.util.UserUtils;
import cn.v5.validation.Validate;
import cn.v5.web.controller.ExtraInfoServerException;
import cn.v5.web.controller.ServerException;
import net.sf.oval.constraint.NotEmpty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 只提供基本的群组关系映射，没有加入过多的逻辑
 * Created by haoWang on 2016/6/22.
 */
@Controller
@Validate
@RequestMapping(value = "/open/api", produces = "application/json")
public class AppGroupController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppGroupController.class);
    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

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
    @Autowired
    private GroupService groupService;

    /**
     * 验证功能由第三方自身实现
     * 创建群组
     */
    @RequestMapping(value = "/group/create", method = RequestMethod.POST)
    @ResponseBody
    public GroupVo create(String name, @NotNull @NotEmpty String member, String desc, String avatar, String group_id) {
        User currentUser = CurrentUser.user();
        if (groupService.groupMemberOverLimit(null, member.split(",").length + 1)) {
            throw new ExtraInfoServerException(StatusCode.GROUP_MEMBER_LIMIT, "group member count over limit").withInfo("limit", groupMemberCountLimit);
        }
        member = Stream.of(member.split(",")).map(k -> UserUtils.genInternalUserId(k, currentUser.getAppId())).collect(Collectors.joining(","));
        return groupService.createOpenGroup(currentUser, name, member, desc, avatar, group_id);
    }

    /**
     * 更新群组信息：名称
     *
     * @param group_id 组ID
     * @param name     组名
     * @param desc     描述
     */
    @RequestMapping(value = "/group/update", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> update(@NotNull String group_id, String name, String desc, String no_disturb, String top) {
        if (null == name && null == no_disturb) {
            return SUCCESS_CODE;
        }

        User currentUser = CurrentUser.user();
        Group group = groupService.findGroupInfo(UserUtils.genInternalUserId(group_id, currentUser.getAppId()));
        if (group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST,
                    "the group does not exist.");
        }

        boolean modifyGroupInfo = false;
        if (null != name) {
            group.setName(name);
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
     * ps.所有用户群组信息只存在于自身的keyspace中 不同步
     *
     * @param group_id 组ID
     */
    @RequestMapping(value = "/group/get", method = RequestMethod.GET)
    @ResponseBody
    public GroupVo get(@NotNull @NotEmpty String group_id, Integer detail) {
        User currentUser = CurrentUser.user();
        String exclude = "";
        if (detail == null || detail == 0) {
            exclude = "member";
        }
        group_id = UserUtils.genInternalUserId(group_id, currentUser.getAppId());
        Group group = groupService.getGroupInfo(currentUser.getId(), group_id, exclude);
        if (group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "group not exits");
        }
        GroupVo groupVo = new GroupVo();
        BeanUtils.copyProperties(group, groupVo);
        List<OpenGroupUser> openGroupUsers = group.getMembers().stream().map(groupUser -> {
            OpenGroupUser openOpenGroupUser = new OpenGroupUser();
            BeanUtils.copyProperties(groupUser, openOpenGroupUser);
            openOpenGroupUser.setId(UserUtils.genOpenUserId(openOpenGroupUser.getId()));
            return openOpenGroupUser;
        }).collect(Collectors.toList());
        groupVo.setId(UserUtils.genOpenUserId(groupVo.getId()));
        groupVo.setCreator(UserUtils.genOpenUserId(groupVo.getCreator()));
        groupVo.setMember(openGroupUsers);
        return groupVo;
    }


    /**
     * 退出群组，如果群主退出，则注销群
     */
    @RequestMapping(value = "/group/exit", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> exit(@NotNull String group_id) {
        User you = CurrentUser.user();
        group_id = UserUtils.genInternalUserId(group_id, you.getAppId());

        //查询群组
        Group group = groupService.findGroupInfo(group_id);
        if (group_id == null || group == null) {
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
    @RequestMapping(value = "/group/join", method = RequestMethod.POST)
    @ResponseBody

    public Map<String, Object> join(@NotNull @NotEmpty String group_id, @NotNull @NotEmpty String member) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = CurrentUser.user();
        member = Stream.of(member.split(",")).map(k -> UserUtils.genInternalUserId(k, currentUser.getAppId())).collect(Collectors.joining(","));
        //群组基本信息
        group_id = UserUtils.genInternalUserId(group_id, currentUser.getAppId());
        Group group = groupService.findGroupInfo(group_id);
        if (group_id == null || group == null) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }

        GroupMember groupMember = groupService.findGroupMemberByKey(group_id, currentUser.getId());
        if (groupMember == null) {
            throw new ServerException(StatusCode.GROUP_OPT_NOT_ALLOW, messageSourceService.getMessageSource(currentUser.getAppId()).getMessage("permission.denied", new Object[]{}, currentUser.getLocale()));
        }

        if (groupService.groupMemberOverLimit(group_id, member.split(",").length)) {
            logUtil.logFailsToAddGroupMemberForOverlimit(group, member, "/group/invite");
            throw new ExtraInfoServerException(StatusCode.GROUP_MEMBER_LIMIT, "group member count over limit").withInfo("limit", groupMemberCountLimit);
        }


        List<GroupMember> gms = groupService.findMembersByGroupId(group_id);

        int numbers = gms != null ? gms.size() : 0;

        group.setNumber(numbers + 1);


        //成员版本判断
        List<String> canAddToGroupMembers = new ArrayList<>();
        List<FailUserInfo> failAddToGroupMembers = new ArrayList<>();
        String finalGroup_id = group_id;
        Stream.of(member.split(","))
                .filter(eachMember -> groupService.findGroupMemberByKey(finalGroup_id, eachMember) == null)
                .forEach(canAddToGroupMembers::add);
        //开启了群组校验 且不是群组创建者添加成员时
        if ("yes".equalsIgnoreCase(group.getEnableValidate()) && (!currentUser.getId().equals(group.getCreator()))) {
            result.put("error_code", StatusCode.GROUP_VERIFY);
            result.put("error", "group need to verify");
            result.put("fail", failAddToGroupMembers.toArray());
            //开启了群组校验 需要给群组创建者 发送校验系统通知
            groupService.sendGroupInviteAuditSystemNotify(currentUser.getId(), canAddToGroupMembers, group.getCreator(), group);
        } else {
//            List<GroupUser> sucessToAddGroupUsers = null;
//            if (null != canAddToGroupMembers && canAddToGroupMembers.size() > 0) {
//                //TODO check 原来发系统通知消息逻辑比较复杂，可以考虑不发让第三方server实现。
            List<OpenGroupUser> openGroupUsers = canAddToGroupMembers.stream().map(k -> {
                GroupUser groupUser = groupService.addUserToGroup(group, userService.findById(k));
                OpenGroupUser openGroupUser = new OpenGroupUser();
                BeanUtils.copyProperties(groupUser, openGroupUser);
                openGroupUser.setId(UserUtils.genOpenUserId(openGroupUser.getId()));
                return openGroupUser;
            }).collect(Collectors.toList());
            groupService.invite(currentUser, group, canAddToGroupMembers, currentUser.getId(), currentUser.getId());
//            }
            result.put("error_code", 2000);
            result.put("join_members", openGroupUsers);
//            //TODO　不一定要返回成功发送的系统通知
//            if (null != failAddToGroupMembers && failAddToGroupMembers.size() > 0) {
//                result.put("fail", failAddToGroupMembers.toArray());
//            }
//            if (null != sucessToAddGroupUsers && sucessToAddGroupUsers.size() > 0) {
//                result.put("success", sucessToAddGroupUsers.toArray());
//            }
        }
        return result;
    }

    /**
     * 移除用户
     *
     * @param group_id 组ID
     */
    @RequestMapping(value = "/group/remove", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> remove(@NotNull String group_id, String member) {
        User you = CurrentUser.user();
        member = Stream.of(member.split(",")).map(k -> UserUtils.genInternalUserId(k, you.getAppId())).collect(Collectors.joining(","));
        group_id = UserUtils.genInternalUserId(group_id, you.getAppId());

        //查询群组
        Group group = groupService.findGroupInfo(group_id);
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
//        if (null != successMembers && successMembers.size() > 0) {
//            result.put("success", successMembers);
//        }
//
//        if (null != failMembers && failMembers.size() > 0) {
//            result.put("fail", failMembers);
//        }

        return result;
    }
}