package cn.v5.service;

import cn.v5.bean.group.GroupUser;
import cn.v5.bean.msg.SystemMessage;
import cn.v5.bean.notify.NotifyMessageForAuth;
import cn.v5.entity.Friend;
import cn.v5.entity.Group;
import cn.v5.entity.PhoneBook;
import cn.v5.entity.User;
import cn.v5.entity.vo.UserVo;
import cn.v5.packet.*;
import cn.v5.util.LoggerFactory;
import cn.v5.util.UserUtils;
import cn.v5.v5protocol.GroupMessage;
import cn.v5.v5protocol.V5protocolUtil;
import com.alibaba.fastjson.JSON;
import com.chatgame.protobuf.TcpBiz;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.handwin.api.sysmsg.bean.SimpleMessage;
import com.handwin.api.sysmsg.bean.SysMessage;
import com.handwin.api.sysmsg.service.SysMessageServiceAsync;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MessageQueueService {
    private static final Logger log = LoggerFactory.getLogger(MessageQueueService.class);
    @Inject
    private ObjectMapper mapper;

    @Inject
    private AmqpTemplate amqpTemplate;


    @Autowired
    private MessageSourceService messageSourceService;

    @Inject
    private UserService userService;

    @Inject
    private PhoneBookService phoneBookService;


    @Value("${mq.system.msg.queue}")
    private String sysMsgQueue;

//    @Value("${mq.game.match.msg.queue}")
//    private String gameMatchMsgQueue;

    @Value("${dudu.user.id}")
    private String duduID;

    @Value("${event.pub.exchange}")
    private String eventExchange;

    @Value("${mq.system.v5protocol.queue}")
    private String v5ProtocolQueue;

    @Autowired
    private V5protocolUtil v5protocolUtil;

    @Autowired
    @Qualifier(value = "rpcSysMessageServiceAsync")
    private SysMessageServiceAsync sysMessageService;

    @Value("${local.idc.region}")
    private String localRegion;

    @Value("${mq.game.match.msg.queue}")
    private String gameMatchMsgQueue;

    public byte[] writePacket(ObjectMapper mapper, NotifyMessage message) {


        ByteBuf body = Unpooled.buffer();
        body.writeByte(message.getMsgTye()); //system type
        byte flag = 0;
        if (message.getAckFlag()) {
            flag |= 1;
        }
        if (message.getPushFlag()) {
            flag |= 4;
        }

        if (message.isIncrOfflineCount()) { /*推送是否需要更新显示数字*/
            flag |= 8;
        }
        body.writeByte(flag);
        body.writeBytes(message.getFrom().getBytes());
        body.writeBytes(message.getTo().getBytes());
        body.writeLong(0);

        byte[] push_content_bytes = new byte[0];
        try {
            if (null != message.getPushContent()) {
                push_content_bytes = message.getPushContent().getBytes("UTF-8");
            }
        } catch (Exception e) {

        }
        body.writeShort(push_content_bytes.length);
        body.writeBytes(push_content_bytes);

        byte[] dataJson = new byte[0];
        try {
            dataJson = mapper.writeValueAsBytes(message.getData());
        } catch (Exception e) {

        }
        body.writeShort(dataJson.length);
        body.writeBytes(dataJson);
        body.writeLong(0);

        if (StringUtils.isNotBlank(message.getCmsgId())) {
            body.writeShort(message.getCmsgId().length());
            body.writeBytes(message.getCmsgId().getBytes(StandardCharsets.UTF_8));
        }


        Map<String, Object> extra = message.getData().getExtra();
        String notifyType = message.getNotifyTye();
//        log.debug("notifyType:%s,data:%s, dataType:%s", notifyType, message.getData(), null != message.getData() ? message.getData().getType() : "");
        if (StringUtils.isNotBlank(notifyType)) {
            if (null == extra) {
                extra = new HashMap<>();
            }
            extra.put("notifyType", notifyType);
        }
        if (null != extra && extra.size() > 0) {
            try {
                byte[] extraBytes = mapper.writeValueAsBytes(extra);
                body.writeShort(extraBytes.length);
                body.writeBytes(extraBytes);
            } catch (JsonProcessingException e) {

            }
        }

        //log.debug("body size is {}", body.writableBytes());
        //log.debug("body capacity is {}", body.capacity());

        ByteBuf header = Unpooled.buffer();
        header.writeByte(0xb7);
        header.writeByte(0);
        header.writeByte(8);
        header.writeByte(0);
        header.writeShort((int) (System.currentTimeMillis() >> 32));
        header.writeInt((int) System.currentTimeMillis());
        header.writeShort(0);
        header.writeShort(body.readableBytes());
        header.writeShort(0);

        ByteBuf packet = Unpooled.wrappedBuffer(header, body);
        byte[] data = new byte[packet.readableBytes()];
        packet.readBytes(data);


        packet.release();
        if (log.isDebugEnabled()) {
            log.debug("toUser is {}, body is {}", message.getTo(), new String(data));
        }

        TcpBiz.Tcp2BizReq.Builder builder = TcpBiz.Tcp2BizReq.newBuilder();
        builder.setSource(TcpBiz.SourceType.CORE_SERVER).setAppId(0).setUserId(message.getFrom()).setTraceId(UUID.randomUUID().toString()).setMsgBody(ByteString.copyFrom(data));
        return builder.build().toByteArray();
    }

    public void sendSysMsg(SystemMessage message) {
        if (null == message) return;
//        String toIds = message.getTo();
//        if (StringUtils.isBlank(toIds)) return;
//        String[] ids = toIds.split(",");


        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setFrom(message.getFrom());
        simpleMessage.setTo(message.getTo());
        simpleMessage.setMsgType(message.getMsgType());
        simpleMessage.setMsgServerType(message.getMsgSrvTyp());
        simpleMessage.setAppID(message.getAppID());
        simpleMessage.setCmsgID(message.getCmsgId());
        simpleMessage.setTraceID(UUID.randomUUID().toString());
        simpleMessage.setMsgFlag((byte) 0);
        simpleMessage.setContent(message.getMsgBody());

        if (log.isDebugEnabled()) {
            log.debug("[sendSysMsg] toUser : {}, appID: {}", message.getTo(), message.getAppID());
        }

        sysMessageService.sendSimple(simpleMessage);


//        for (String id : ids) {
//            ByteBuf body = Unpooled.buffer();
//            body.writeByte(message.getMsgType()); //system type
//            body.writeByte(message.getMsgSrvTyp());
//
//            body.writeBytes(id.getBytes());
//
//            byte[] content_bytes = new byte[0];
//            try {
//                if (null != message.getMsgBody()) {
//                    content_bytes = message.getMsgBody().getBytes("UTF-8");
//                }
//            } catch (Exception e) {
//                log.error(String.format("fails to send system msg. message:%s. id: %s", message, id), e);
//                continue;
//            }
//            body.writeShort(content_bytes.length);
//            body.writeBytes(content_bytes);
//            body.writeByte(0);
//            if (StringUtils.isNotBlank(message.getCmsgId())) {
//                //重新生成cmsgId 防止群发时 cmsgId相同
//                String cmsgId = UUID.randomUUID().toString();
//                body.writeShort(cmsgId.length());
//                body.writeBytes(cmsgId.getBytes(StandardCharsets.UTF_8));
//            }
//
//            ByteBuf header = Unpooled.buffer();
//            header.writeByte(0xb7);
//            header.writeByte(0);
//            header.writeByte(5);
//            header.writeByte(0);
//            header.writeShort((int) (System.currentTimeMillis() >> 32));
//            header.writeInt((int) System.currentTimeMillis());
//            header.writeShort(0);
//            header.writeShort(body.readableBytes());
//            header.writeShort(0);
//
//            ByteBuf packet = Unpooled.wrappedBuffer(header, body);
//            byte[] data = new byte[packet.readableBytes()];
//            packet.readBytes(data);
//            if (log.isDebugEnabled()) {
//                log.debug("toUser is {}, body is {}", message.getTo(), new String(data));
//            }
//
//            TcpBiz.Tcp2BizReq.Builder builder = TcpBiz.Tcp2BizReq.newBuilder();
//            builder.setSource(TcpBiz.SourceType.CORE_SERVER).setAppId(0).setUserId(message.getFrom()).setTraceId(UUID.randomUUID().toString()).setMsgBody(ByteString.copyFrom(data));
//            byte[] mqContent = builder.build().toByteArray();
//            amqpTemplate.convertAndSend(sysMsgQueue, mqContent);
//
//            try {
//                packet.release();
//                body.release();
//            } catch (Exception e) {
//                //ignore
//            }
//        }
    }

    public void sendSysMsg(NotifyMessageForAuth message) {

        if (null == message) return;

        String toIds = message.getTo();

        if (StringUtils.isBlank(toIds)) return;

        String[] ids = toIds.split(",");


        SysMessage sysMessage = new SysMessage();
        sysMessage.setFrom(message.getFrom());
        sysMessage.setMsgType(message.getMsgType());
        sysMessage.setStore(message.isStore());
        sysMessage.setReplyRead(message.isReplyRead());
        sysMessage.setAppID(message.getAppID());
        sysMessage.setCmsgID(message.getCmsgId());
        sysMessage.setPush(message.isPush());
        sysMessage.setIncreadByOneFromPush(message.isIncreadByOneFromPush());
        sysMessage.setPushContent(message.getPushBody());
        sysMessage.setTo(message.getTo());
        sysMessage.setTraceID(UUID.randomUUID().toString());


        try {
            String content = StringUtils.isNotBlank(message.getMessageBody()) ? message.getMessageBody() : "";
            if (log.isDebugEnabled()) {
                log.debug("[SendSysMsg] content:{} ", content);
            }
            sysMessageService.send(sysMessage, ids, content, "");
        } catch (Exception e) {
            log.error("[SendSysMsg] Error from:{} , to:{} , cmsgID:{}", message.getFrom(), message.getTo(), message.getCmsgId());
        }


//        for (String id : ids) {
//            ByteBuf body = Unpooled.buffer();
//            body.writeByte(message.getMsgType()); //system type
//            byte flag = 0;
//            if (message.isStore()) {
//                flag |= 1;
//            }
//
//            if (message.isReplyRead()) {
//                flag |= 2;
//            }
//
//            if (message.isPush()) {
//                flag |= 4;
//            }
//
//            if (message.isIncreadByOneFromPush()) { /*推送是否需要更新显示数字*/
//                flag |= 8;
//            }
//
//            if (message.isEnsureReceive()) {
//                flag |= 12;
//            }
//
//            body.writeByte(flag);
//            body.writeBytes(message.getFrom().getBytes());
//            body.writeBytes(id.getBytes());
//            body.writeLong(0);
//
//            byte[] push_content_bytes = new byte[0];
//            try {
//                if (null != message.getPushBody()) {
//                    push_content_bytes = message.getPushBody().getBytes("UTF-8");
//                }
//            } catch (Exception e) {
//                log.error(String.format("fails to send system notify. message:%s. id: %s", message, id), e);
//                continue;
//            }
//            body.writeShort(push_content_bytes.length);
//            body.writeBytes(push_content_bytes);
//
//            byte[] msgContent = message.getMessageBody().getBytes(StandardCharsets.UTF_8);
//            body.writeShort(msgContent.length);
//            body.writeBytes(msgContent);
//            body.writeLong(0);
//
//            if (StringUtils.isNotBlank(message.getCmsgId())) {
//                String cmsgId = UUID.randomUUID().toString();
//                body.writeShort(cmsgId.length());
//                body.writeBytes(cmsgId.getBytes(StandardCharsets.UTF_8));
//            }
//
//            ByteBuf header = Unpooled.buffer();
//            header.writeByte(0xb7);
//            header.writeByte(0);
//            header.writeByte(8);
//            header.writeByte(0);
//            header.writeShort((int) (System.currentTimeMillis() >> 32));
//            header.writeInt((int) System.currentTimeMillis());
//            header.writeShort(0);
//            header.writeShort(body.readableBytes());
//            header.writeShort(0);
//
//            ByteBuf packet = Unpooled.wrappedBuffer(header, body);
//            byte[] data = new byte[packet.readableBytes()];
//            packet.readBytes(data);
//
//            if (log.isDebugEnabled()) {
//                log.debug("toUser is {}, body is {}", message.getTo(), new String(data));
//            }
//
//            TcpBiz.Tcp2BizReq.Builder builder = TcpBiz.Tcp2BizReq.newBuilder();
//            builder.setSource(TcpBiz.SourceType.CORE_SERVER).setAppId(0).setUserId(message.getFrom()).setTraceId(UUID.randomUUID().toString()).setMsgBody(ByteString.copyFrom(data));
//            byte[] mqContent = builder.build().toByteArray();
//            amqpTemplate.convertAndSend(sysMsgQueue, mqContent);
//
//            try {
//                packet.release();
//                body.release();
//            } catch (Exception e) {
//                //ignore
//            }
//        }
    }


    public void sendSysMsg(NotifyMessage message) {


        SysMessage sysMessage = new SysMessage();
        sysMessage.setFrom(message.getFrom());
        sysMessage.setMsgType(message.getMsgTye());
        sysMessage.setAck(message.getAckFlag());
        sysMessage.setAppID(message.getAppId());
        sysMessage.setCmsgID(message.getCmsgId());
        sysMessage.setGroup(message.isGroupFlag());
        sysMessage.setIncrOfflineCount(message.isIncrOfflineCount());
        sysMessage.setPush(message.getPushFlag());
        sysMessage.setPushContent(message.getPushContent());
        sysMessage.setTo(message.getTo());
        sysMessage.setTraceID(UUID.randomUUID().toString());


        try {

            String content = mapper.writeValueAsString(message.getData());

            Map<String, Object> extra = message.getData().getExtra();
            if (StringUtils.isNotBlank(message.getNotifyTye())) {
                if (extra == null) {
                    extra = new HashMap<>();
                }
                extra.put("notifyTye", message.getNotifyTye());
            }

            if (log.isDebugEnabled()) {
                log.debug("[SendSysMsg] content:{} ", new String(content));
            }

            sysMessageService.send(sysMessage, content, extra != null ? JSON.toJSONString(extra) : "");


        } catch (Exception e) {
            log.error("[SendSysMsg] Error from:{} , to:{} , cmsgID:{}", message.getFrom(), message.getTo(), message.getCmsgId());
        }


//        try {
//            amqpTemplate.convertAndSend(sysMsgQueue, writePacket(mapper, message));
//            log.debug("sysMsgQueue:" + sysMsgQueue + ",message:" + message);
//        } catch (Exception ignore) {
//            log.error(ignore.getMessage(), ignore);
//        }

    }


    /**
     * 用户注册消息
     *
     * @param toUser 消息推送人
     * @param friend 好友信息
     */
    public void sendUserRegistMsg(User toUser, User friend) {
        if (toUser == null || friend == null) {
            log.warn("sendUserRegistMsg failure,toUser = {}, friend = {}", toUser, friend);
            return;
        }
        /**
         * toUser.getId(), friend.getMobile()
         * 到PhoneBook中查询好友在这个用户通讯录里的名称
         */

        PhoneBook phoneBook = phoneBookService.findUserPhoneBook(friend.getCountrycode(), friend.getMobile(), toUser.getId());
        String name = friend.getNickname();
        boolean isPhoneBookFriend = false;
        if (phoneBook != null && StringUtils.isNotBlank(phoneBook.getName())) {
            name = phoneBook.getName();
            isPhoneBookFriend = true;
        }

        //小秘书发送消息
        String pushContent;
        String desc;
        //desc 客户端显示时，需要做链接
        if (isPhoneBookFriend) {
            pushContent = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("user.newfriend", new Object[]{name}, toUser.getLocale());
            desc = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("user.newfriend.desc", new Object[]{}, toUser.getLocale());
        } else {
            pushContent = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("user.newfriend1", new Object[]{name}, toUser.getLocale());
            desc = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("user.newfriend.desc1", new Object[]{}, toUser.getLocale());
        }
        UserVo vo = UserVo.createFromUser(friend);
        AckNotifyMessage anm = new AckNotifyMessage(duduID, toUser.getId(), pushContent, new FriendRegisterData(vo, desc));//new SecretaryNotifyData(desc,duduID, "CONTENT_USER_REGISTER",null, vo));
        anm.setIncrOfflineCount(true);
        sendSysMsg(anm);
    }


    public void friendAutoSayHello(User oldUser, User newUser, String nickName) {
        nickName = Strings.isNullOrEmpty(nickName) ? oldUser.getNickname() : nickName;
        String content = messageSourceService.getMessageSource(newUser.getAppId()).getMessage("friend.register.a", new Object[]{nickName}, newUser.getLocale());
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setMsgSrvTyp((byte) 0x01);
        byte textType = 0x01;
        systemMessage.setMsgType(textType);
        systemMessage.setCmsgId(UUID.randomUUID().toString());
        systemMessage.setFrom(oldUser.getId());
        systemMessage.setTo(newUser.getId());
        systemMessage.setMsgBody(content);
        sendSysMsg(systemMessage);

    }

    public void robotSendCustomMsg(User toUser, String pushContent) {
        User duUser = userService.findById(0, duduID);
        sendSysMsg(new AckNotifyMessage(duUser.getId(), toUser.getId(), pushContent, new SecretaryNotifyData(pushContent, duUser.getId(), "TEXT", null)));
    }


    /**
     * 群组创建消息
     *
     * @param admin
     * @param users
     * @param group
     */
    public void sendGroupCreateMsg(User admin, List<User> users, Group group) {
        for (User user : users) {
            //群组创建者 不需要接受系统通知
            if (user.getId().equalsIgnoreCase(admin.getId())) {
                continue;
            }

            //创建群组的push栏 不展示 创建群组，而展示邀请成员
            String prompt = messageSourceService.getMessageSource(user.getAppId()).getMessage("group.invite.2", new Object[]{group.getName(), admin.getNickname()}, user.getLocale());
            AckNotifyMessage msg = new AckNotifyMessage(admin.getId(), user.getId(), prompt, new GroupCreateData(UserUtils.genOpenGroup(group, user.getAppId()), prompt), user.getAppId());
            this.sendSysMsg(msg);
        }
    }

    /**
     * 群组邀请消息
     * 跨区消息的from 需要设置请求的发送方
     *
     * @param group
     * @param toUser
     * @param inviteUserId 群组的邀请方
     * @param reqUserId    请求方userId bizServer需要判断跨区转发使用
     */
    public void sendGroupInviteMsg(Group group, User toUser, String pushContent, String inviteUserId, Boolean push, String reqUserId) {
        NotifyMessage msg;
        Group openGroup = UserUtils.genOpenGroup(group, toUser.getAppId());
        String openInvitedUserId = UserUtils.genOpenUserId(inviteUserId, toUser.getAppId());
        if (push) {
            msg = new AckNotifyMessage(reqUserId, toUser.getId(), pushContent, new GroupAddUserData(openGroup, pushContent, openInvitedUserId), toUser.getAppId());
        } else {
            msg = new AckNoPushNotifyMessage(reqUserId, toUser.getId(), new GroupAddUserData(openGroup, "", openInvitedUserId), toUser.getAppId());
        }
        this.sendSysMsg(msg);
    }

    /**
     * 新版本
     * 给群内成员推送新成员加入群组
     *
     * @param group
     * @param toUser
     * @param pushContent
     * @param applicantId 申请加入群的人的id
     * @param push
     */
    public void sendGroupJoinMsgV1(Group group, User toUser, String pushContent, String applicantId, Boolean push) {
        NotifyMessage msg;
        if (push) {
            msg = new AckNotifyMessage(applicantId, toUser.getId(), pushContent, new GroupApplyAddUserData(group, pushContent, applicantId));
        } else {
            msg = new AckNoPushNotifyMessage(applicantId, toUser.getId(), new GroupApplyAddUserData(group, "", applicantId));
        }
        this.sendSysMsg(msg);
    }

    /**
     * 老版本
     * 给群内成员推送新成员加入群组
     *
     * @param group
     * @param toUser
     * @param pushContent
     * @param applicantId
     * @param push
     */
    public void sendGroupJoinMsgV0(Group group, User toUser, String pushContent, String applicantId, Boolean push) {
        NotifyMessage msg;
        if (push) {
            msg = new AckNotifyMessage(applicantId, toUser.getId(), pushContent, new GroupAddUserData(group, pushContent, group.getCreator()));
        } else {
            msg = new AckNoPushNotifyMessage(applicantId, toUser.getId(), new GroupAddUserData(group, "", group.getCreator()));
        }
        this.sendSysMsg(msg);
    }

    /**
     * 群组更新消息
     *
     * @param fromUserId
     * @param users
     * @param group
     * @param users
     */
    public void sendGroupUpdateMsg(String fromUserId, List<GroupUser> users, Group group) {
        List<UserVo> members = Lists.newArrayList();
        for (User user : users) {
            UserVo vo = UserVo.createFromUser(user);
            members.add(vo);
        }

        for (User user : users) {
            String userId = user.getId();
            if (userId.equals(group.getCreator()))
                continue;
            PresenceNotifyMessage msg = new PresenceNotifyMessage(fromUserId, userId, new GroupUpdateData(group));
            this.sendSysMsg(msg);
        }
    }

    /**
     * 群组删除成员消息
     * 群组删除成员不需要发送push栏消息
     *
     * @param toUserIds
     * @param groupId
     * @param toDelUser 被删除用户
     * @param reqUserId 当前用户Id
     */
    public void sendGroupRemoveUserMsgToOtherMembers(Set<String> toUserIds, String groupId, User toDelUser, String reqUserId, int number) {
        String openGroupId = UserUtils.genOpenUserId(groupId, toDelUser.getAppId());
        String opentoDelUserId = UserUtils.genOpenUserId(toDelUser.getId(), toDelUser.getAppId());
        for (String userId : toUserIds) {
            if (userId.equals(toDelUser.getId())) {
                continue;
            }
            NotifyMessage msg = new AckNoPushNotifyMessage(reqUserId, userId, new GroupRemoveUserData(openGroupId, opentoDelUserId, "", number));
            this.sendSysMsg(msg);
        }
    }

    /**
     * 给被删除人发送删除系统通知
     *
     * @param reqUserId
     * @param group
     * @param toUser
     */
    public void sendGroupRemoveUserMsgToRemovedMembers(Group group, User toUser, String reqUserId, int number) {
        String openGroupId = UserUtils.genOpenUserId(group.getId(), toUser.getAppId());
        String openToUserId = UserUtils.genOpenUserId(toUser.getId(), toUser.getAppId());
        String prompt = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("group.user.remove", new Object[]{group.getName()}, toUser.getLocale());

        NotifyMessage msg = new AckNotifyMessage(reqUserId, toUser.getId(), prompt, new GroupRemoveUserData(openGroupId, openToUserId, prompt, number));
        this.sendSysMsg(msg);
    }

    /**
     * 群组解散消息
     *
     * @param fromUserId
     * @param toUserId
     * @param group
     */
    public void sendGroupDismissMsg(String fromUserId, String toUserId, Group group) {
        User user = userService.findById(toUserId);
        if (null == user) {
            return;
        }
        final String prompt = messageSourceService.getMessageSource(user.getAppId()).getMessage("group.destroy", new Object[]{group.getName()}, user.getLocale());
        AckNotifyMessage msg = new AckNotifyMessage(fromUserId, user.getId(), prompt, new GroupDismissData(group, prompt));
        this.sendSysMsg(msg);
    }

    /**
     * 群组成员退出消息 只需要给群主发送push
     *
     * @param toUserIds
     * @param group
     * @param reqUser   退出成员
     */
    public void sendGroupExitMsg(Set<String> toUserIds, Group group, User reqUser) {
        String groupId = UserUtils.genOpenUserId(group.getId(), reqUser.getAppId());
        String reqUserId = UserUtils.genOpenUserId(reqUser.getId(), reqUser.getAppId());

        for (String userId : toUserIds) {
            User receiverUser = userService.findById(userId);
            if (null == receiverUser) {
                continue;
            }
            NotifyMessage msg;
            //只有群主才接受到用户退出push栏
            if (userId.equals(group.getCreator())) {
                //String prompt = messageSource.getMessage("group.exit", new Object[]{group.getName(), reqUser.getNickname()}, receiverUser.getLocale());

                String prompt = messageSourceService.getMessageSource(receiverUser.getAppId()).getMessage("group.exit", new Object[]{reqUser.getNickname(), group.getName()}, receiverUser.getLocale());
                msg = new AckNotifyMessage(reqUser.getId(), userId, prompt, new GroupExitDate(groupId, reqUserId, prompt, group.getNumber()));
            } else {
                msg = new AckNoPushNotifyMessage(reqUser.getId(), userId, new GroupExitDate(groupId, reqUserId, "", group.getNumber()));
            }
            this.sendSysMsg(msg);
        }
    }

    /**
     * 发送用户信息变更消息
     *
     * @param friends
     * @param updateUser
     */
    public void sendUserUpdateMsg(List<Friend> friends, User updateUser) {
        UserVo vo = UserVo.createFromUser(updateUser);
        for (Friend friend : friends) {
            // 嘟嘟小秘书不需要发送
            if (friend.getId().getFriendId().equals(duduID))
                continue;
            PresenceNotifyMessage msg = new PresenceNotifyMessage(updateUser.getId(), friend.getId().getFriendId(), new UserUpdateData(vo));

            sendSysMsg(msg);
        }
    }


    /**
     * 给群组创建者 离线发送审批通知
     *
     * @param inviter
     * @param members
     * @param groupCreator
     */
    public void sendGroupInviteAuditMsg(String inviter, List<User> members, User groupCreator, Group group) {
        NotifyMessage msg = new NotifyMessage();
        msg.setAckFlag(true);
        msg.setPushFlag(true);
        msg.setIncrOfflineCount(false);
        msg.setFrom(inviter);
        msg.setTo(groupCreator.getId());
        msg.setAppId(groupCreator.getAppId());
        String pushContent = messageSourceService.getMessageSource(groupCreator.getAppId()).getMessage("group.invite.audit.msg", new Object[]{group.getName()}, groupCreator.getLocale());
        msg.setPushContent(pushContent);
        msg.setData(new GroupInviteAuditData(group.getId(), inviter, members));
        this.sendSysMsg(msg);
        log.debug("[group invite audit] groupId:{} inivter:{} ", group.getId(), inviter);
    }

    /**
     * 申请加入群组
     *
     * @param content 申请理由
     */
    public void sendGroupApplyAuditMsg(User applicant, User groupCreator, Group group, String content) {
        NotifyMessage msg = new NotifyMessage();
        msg.setAckFlag(true);
        msg.setPushFlag(true);
        msg.setIncrOfflineCount(false);
        msg.setFrom(applicant.getId());
        msg.setTo(groupCreator.getId());
        msg.setAppId(groupCreator.getAppId());
        String pushContent = messageSourceService.getMessageSource(groupCreator.getAppId()).getMessage("group.apply.audit.msg", new Object[]{group.getName()}, groupCreator.getLocale());
        msg.setPushContent(pushContent);
        //兼容老版本，使用和邀请同样的消息通知群主
        msg.setData(new GroupApplyAuditData(group.getId(), applicant, content));
        this.sendSysMsg(msg);
        log.debug("[group apply audit] groupId:{} applicant:{} ", group.getId(), applicant.getId());
    }

    /**
     * 向对方用户发送添加好友请求
     *
     * @param promoter 请求人
     * @param toUser   对方用户
     */
    public void sendContactRequest(User promoter, User toUser, String msg) {
        String pushMsg = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("contactrequest.pushmsg", new Object[]{}, toUser.getLocale());
        sendSysMsg(new ContactRequestMessage(promoter, toUser, msg, pushMsg));
    }

    /**
     * 向双方发送已成为好友的信息
     *
     * @param promoter 请求者
     * @param toUser   对方用户
     */
    public void sendContactSuccess(User promoter, User toUser) {

        //设置push消息内容
        ContactSuccessMessage message1 = new ContactSuccessMessage(promoter, toUser);
        ContactSuccessMessage message2 = new ContactSuccessMessage(toUser, promoter);

//        try {
//            String pushContent1 = messageSource.getMessage("contact.add.push", new Object[]{promoter.getNickname()}, toUser.getLocale());
//            message1.setPushContent(pushContent1);
//
//            String pushContent2 = messageSource.getMessage("contact.add.push", new Object[]{toUser.getNickname()}, promoter.getLocale());
//            message2.setPushContent(pushContent2);
//        } catch (Exception e) {
//            log.error("fails to get push content", e);
//        }

        sendSysMsg(message1);
        sendSysMsg(message2);
    }

    public void sendToUserContactSuccess(User promoter, User toUser) {

        //设置push消息内容
        ContactSuccessMessage message1 = new ContactSuccessMessage(promoter, toUser);
        sendSysMsg(message1);
    }


    /**
     * 向一方发送已经成为好友的信息
     *
     * @param promoter 发送者
     * @param toUser   接受者
     */
    public void sendContactSuccessToOneSide(User promoter, User toUser) {
        ContactSuccessMessage message = new ContactSuccessMessage(promoter, toUser);
//        try {
//            message.setPushContent(messageSource.getMessage("contact.add.push", new Object[]{promoter.getNickname()}, toUser.getLocale()));
//        } catch (Exception e) {
//            log.error("fails to get push content ", e);
//        }
        sendSysMsg(message);
    }


    public void sendEvent(String routeKey, String eventType, Object event) {
        sendEvent(eventExchange, routeKey, eventType, event);
    }

    public void sendEvent(String eventExchange, String routeKey, String eventType, Object event) {
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("type", eventType);
        contentMap.put("content", event);
        try {
            String contentJson = mapper.writeValueAsString(contentMap);
            try {
                amqpTemplate.convertAndSend(eventExchange, routeKey, contentJson.getBytes(StandardCharsets.UTF_8));
                log.debug(String.format("success to pub message.eventExchange:%s,routeKey:%s,enventType:%s,event:%s", eventExchange, routeKey, eventType, event));
            } catch (Exception e1) {
                log.error(String.format("fails to pub message.eventExchange:%s,routeKey:%s,enventType:%s,event:%s", eventExchange, routeKey, eventType, event), e1);
            }
        } catch (JsonProcessingException e) {
            log.error(String.format("fails to parse event.%s", event), e);
        }
    }


    public void sendMsgByV5Protocol(GroupMessage groupMessage) {
        try {

            SysMessage sysMessage = new SysMessage();
            sysMessage.setFrom(groupMessage.getSendId());
            sysMessage.setAppID(groupMessage.getAppID());
            sysMessage.setFormRegion(localRegion);
            sysMessage.setToRegion(localRegion);
            sysMessage.setServerReceiveConfirm(Boolean.FALSE);

            sysMessage.setStore(groupMessage.getNeedStore());
            sysMessage.setPush(groupMessage.getNeedPush());
            sysMessage.setPushContent(groupMessage.getPushContent());
            sysMessage.setIncrOfflineCount(groupMessage.getPushIncr());
            sysMessage.setEnsureArrive(groupMessage.getEnsureArrive());

            sysMessage.setCmsgID(UUID.randomUUID().toString());
            sysMessage.setTraceID(groupMessage.getTraceId());
            sysMessage.setService("/group/system/notify");

            Map<String, Object> extraMap = new HashedMap();
            extraMap.put("group_id", groupMessage.getGroupId());
            extraMap.put("enter_conversation", groupMessage.getEnterConversationForPush());
            String extra = JSON.toJSONString(extraMap);

            sysMessageService.sendV5(sysMessage, groupMessage.getReceivers().toArray(new String[]{}), groupMessage.getMsgBody(), extra);

//            amqpTemplate.convertAndSend(v5ProtocolQueue, v5protocolUtil.encode(groupMessage));
            log.debug("v5ProtocolQueue:" + v5ProtocolQueue + ",message:" + groupMessage);
        } catch (Exception ignore) {
            log.error(ignore.getMessage(), ignore);
        }
    }

    public void sendGameMatchMsg(String type, Map<String, Object> data) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", type);
            msg.put("data", data);
            msg.put("timestamp", System.currentTimeMillis());
            byte[] dataJson = new byte[0];
            dataJson = mapper.writeValueAsBytes(msg);
            amqpTemplate.convertAndSend(gameMatchMsgQueue, dataJson);
            log.debug("gameMatchMsgQueue:" + gameMatchMsgQueue + ",message:" + msg);
        } catch (Exception ignore) {
            log.error(ignore.getMessage(), ignore);
        }
    }


    public static void main(String[] args) {
        ByteBuf b1 = Unpooled.buffer();
        ByteBuf b2 = Unpooled.buffer();
        b1.writeBytes("test".getBytes());
        ByteBuf b = Unpooled.wrappedBuffer(b1, b2);
        b.release();
        System.out.println(b1.refCnt());
        System.out.println(b2.refCnt());
    }

}
