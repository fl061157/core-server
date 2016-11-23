package cn.v5.service;

import cn.v5.bean.notify.NotifyContent;
import cn.v5.code.NotifyMsgType;
import cn.v5.entity.Group;
import cn.v5.entity.User;
import cn.v5.v5protocol.GroupMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by piguangtao on 15/12/21.
 */
//@Service
public class MessageQueueServiceWithV5Protocol extends MessageQueueService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueServiceWithV5Protocol.class);


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

    /**
     * 先改造群组创建消息
     *
     * @param admin
     * @param users
     * @param group
     */
    @Override
    public void sendGroupCreateMsg(User admin, List<User> users, Group group) {
        Map<Locale, List<String>> userLocaleMap = new HashMap<>();
        for (User user : users) {
            //群组创建者 不需要接受系统通知
            if (user.getId().equalsIgnoreCase(admin.getId())) {
                continue;
            }
            userLocaleMap.putIfAbsent(user.getLocale(), new ArrayList<>());
            userLocaleMap.get(user.getLocale()).add(user.getId());
        }

        if (userLocaleMap.size() > 0) {
            userLocaleMap.entrySet().stream().forEach(entry -> {
                Locale locale = entry.getKey();
                List<String> receivers = entry.getValue();
                //创建群组的push栏 不展示 创建群组，而展示邀请成员
                String prompt = messageSourceService.getMessageSource(admin.getAppId()).getMessage("group.invite.2", new Object[]{group.getName(), admin.getNickname()}, locale);

                GroupMessage groupMessage = new GroupMessage();
                groupMessage.setGroupId(group.getId());
                groupMessage.setNeedPush(true);
                groupMessage.setPushContent(prompt);
                groupMessage.setTraceId(UUID.randomUUID().toString());
                groupMessage.setEnterConversationForPush(true);
                groupMessage.setEnsureArrive(true);
                groupMessage.setNeedStore(true);
                groupMessage.setPushIncr(true);
                groupMessage.setSendId(admin.getId());

                NotifyContent content = new NotifyContent(NotifyMsgType.GROUP_CREATE, group);

                try {
                    groupMessage.setMsgBody(mapper.writeValueAsString(content));
                    LOGGER.debug("[group message]group create.{}", groupMessage);
                } catch (JsonProcessingException e) {
                    LOGGER.error("fails to parse msg content.", e);
                    throw new RuntimeException("fails to parse group message content", e);
                }

                groupMessage.setReceivers(receivers);

                this.sendMsgByV5Protocol(groupMessage);

//        AckNotifyMessage msg = new AckNotifyMessage(admin.getId(), user.getId(), prompt, new GroupCreateData(group, prompt));
//        this.sendSysMsg(msg);
            });
        }
    }
}
