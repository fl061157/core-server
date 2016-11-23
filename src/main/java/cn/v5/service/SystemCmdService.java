package cn.v5.service;

import cn.v5.bean.msg.SystemMessage;
import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.User;
import cn.v5.entity.thirdapp.ThirdAppToFriend;
import cn.v5.packet.NewPeopleMayKnownNotifyData;
import cn.v5.packet.NotifyMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by piguangtao on 15/9/23.
 */
@Service
public class SystemCmdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemCmdService.class);

    @Inject
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FriendService friendService;

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    private MessageSourceService messageSourceService ;


    @Value("${software.upgrade.tip}")
    private String upgradeTipEnable;


    public void handleSystemCmd(User user, String type, String info) {
        LOGGER.debug("[system cmd] user:{} type:{} info:{}", user, type, info);
        if (null == user || StringUtils.isBlank(type)) return;
        try {
            final ObjectMapper finalObjectMapper = objectMapper;
            this.taskService.execute(() -> {
                switch (type) {
                    //触发更新第三方好友信息
                    case "other_app_friend":
                        if (StringUtils.isBlank(info)) return;
                        JsonNode jsonNode = null;
                        try {
                            jsonNode = finalObjectMapper.readTree(info);
                            String source = jsonNode.get("source").textValue();
                            if (StringUtils.isNotBlank(source)) {
                                friendService.handleOtherAppNewRecommendFriends(user, source);
                            }
                        } catch (IOException e) {
                            LOGGER.error(String.format("fails to parse json data:", info), e);
                        }

                        break;
                    //软件升级
                    case "software_update":
                        if (!"yes".equalsIgnoreCase(upgradeTipEnable)) {
                            LOGGER.info("disable upgrade tip");
                            return;
                        }

                        if (StringUtils.isBlank(info)) return;
                        String replacedInfo = info.replaceAll("\\\\", "");
                        JsonNode infoNode = null;
                        try {
                            infoNode = finalObjectMapper.readTree(replacedInfo);
                            String from = infoNode.get("from").textValue();
                            String to = infoNode.get("to").textValue();
                            if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
                                handleSoftwareUpgrade(user, from, to);
                            }
                        } catch (Exception e) {
                            LOGGER.error(String.format("fails to parse json data:", info), e);
                        }

                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void userFristBindThirdApp(final String userId, String source, String thirdAppUserId) {

        //初次绑定时，需要加起为好友的人，发送可能认识的人通知
        try {
            this.taskService.execute(() -> {
                List<ThirdAppToFriend> toFriends = manager.sliceQuery(ThirdAppToFriend.class).forSelect().withPartitionComponents(thirdAppUserId, source).get();


                if (null != toFriends && toFriends.size() > 0) {
                    toFriends.stream().forEach(thirdAppToFriend -> {
                        //判断user是否为phone可能认识的人
                        if (friendService.isRecommand(userId, thirdAppToFriend.getKey().getUserId())) {
                            LOGGER.debug("fromUser:{},toUser:{} , recommand", userId, thirdAppToFriend);
                            //发送系统通知
                            NotifyMessage notifyMessage = new NotifyMessage();
                            notifyMessage.setType(NotifyMsgType.COMMAND_PEOPLE_YOU_MAY_KNOWN);
                            notifyMessage.setAckFlag(true);
                            notifyMessage.setCmsgId(UUID.randomUUID().toString());
                            notifyMessage.setFrom(SystemConstants.SYSTEM_ACCOUNT_SECRETARY);
                            notifyMessage.setTo(thirdAppToFriend.getKey().getUserId());
                            notifyMessage.setData(new NewPeopleMayKnownNotifyData(source));
                            notifyMessage.setIncrOfflineCount(false);
                            notifyMessage.setPushFlag(false);
                            messageQueueService.sendSysMsg(notifyMessage);
                        }
                    });
                }
            });
        } catch (Exception e) {
            LOGGER.error("fails to bind user " + userId + " from " + source, e);
        }
    }


    public void handleSoftwareUpgrade(User user, String fromVersion, String toVersion) {
        if (!(fromVersion.startsWith("2.") && toVersion.startsWith("3."))) {
            return;
        }

        String content = messageSourceService.getMessageSource( user.getAppId() ).getMessage("software.3.0.upgrade.tip", new Object[]{}, user.getLocale());
        if (StringUtils.isBlank(content)) {
            return;
        }

        SystemMessage message = new SystemMessage();
        message.setCmsgId(UUID.randomUUID().toString());
        message.setFrom(SystemConstants.SYSTEM_ACCOUNT_SECRETARY);
        message.setMsgBody(content);
        message.setMsgSrvTyp((byte) 0x01);

        //表示发送图片
        if (content.startsWith("http")) {
            message.setMsgType((byte) 0x03);
        } else {
            message.setMsgType((byte) 0x01);
        }

        message.setTo(user.getId());

        messageQueueService.sendSysMsg(message);

        LOGGER.debug("[software tip] fromVersion:{} toVersion:{}  content:{} , userId:{}", fromVersion, toVersion, content, user.getId());

    }
}
