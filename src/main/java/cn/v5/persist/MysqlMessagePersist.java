package cn.v5.persist;

import cn.v5.localentity.Message;
import cn.v5.localentity.UserLocalMsgCounter;
import cn.v5.util.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handwin.database.bean.MessageCount;
import com.handwin.message.MessageException;
import com.handwin.message.bean.MessageStatus;
import com.handwin.message.service.MessageService;
import info.archinnov.achilles.type.CounterBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by fangliang on 16/2/15.
 */

@Service("mysqlMessagePersist")
public class MysqlMessagePersist implements MessagePersist {


    @Autowired
    @Qualifier(value = "rpcMessageService")
    private MessageService rpcMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    protected final static String SYSTEM_NOTIFY_TYPE = "SYSTEM_NOTIFY";

    private static final Logger log = LoggerFactory.getLogger(MysqlMessagePersist.class);

    @Override
    public List<Message> findMessagesByUserId(String userId, long lastMessageId, int length) {
        try {
            List<com.handwin.database.bean.Message> messageList = rpcMessageService.findUnReadMessage(userId, length, lastMessageId);
            if (messageList != null && messageList.size() > 0) {
                return messageList.stream().map(mm -> parse(mm)).collect(Collectors.toList());
            }
        } catch (MessageException e) {
            log.error("findMessagesByUserId error userId:{} , lastMessageId:{} , length:{}", userId, lastMessageId, length, e);
            return null;
        }
        return null;
    }


    @Override
    public List<Message> findMessageByUserId(String userId, int length, List<Long> updateMessageIdList) {
        try {
            List<com.handwin.database.bean.Message> messageList = rpcMessageService.findUnReadMessage(userId, updateMessageIdList, length);
            if (messageList != null && messageList.size() > 0) {
                return messageList.stream().map(mm -> parse(mm)).collect(Collectors.toList());
            }
        } catch (MessageException e) {
            log.error("findMessagesByUserId error userId:{} , length:{}", userId, length, e);
            return null;
        }
        return null;
    }

    @Override
    public UserLocalMsgCounter findUserLocalMsgCount(String userId) {

        try {
            MessageCount messageCount = rpcMessageService.getUnReadCount(userId);
            UserLocalMsgCounter userLocalMsgCounter = new UserLocalMsgCounter();
            userLocalMsgCounter.setUserId(userId);
            userLocalMsgCounter.setCounter(new CounterBuilder().incr(messageCount.getLocalUnReadCount()));
            return userLocalMsgCounter;
        } catch (MessageException e) {
            log.error("findUserLocalMsgCount error userId:{}", userId, e);
            return null;
        }
    }

    @Override
    public UserLocalMsgCounter saveUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount) {

        MessageCount messageCount = new MessageCount();
        messageCount.setLocalUnReadCount(userLocalMsgCount.getCounter().get().intValue());
        try {
            rpcMessageService.updateMessageCount(userLocalMsgCount.getUserId(), messageCount);
        } catch (MessageException e) {
            log.error("saveUserLocalMsgCount error userId:{}", userLocalMsgCount.getUserId(), e);
            return null;
        }
        return userLocalMsgCount;
    }

    @Override
    public void upateUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount) {
        MessageCount messageCount = new MessageCount();
        messageCount.setLocalUnReadCount(userLocalMsgCount.getCounter().get().intValue());
        try {
            rpcMessageService.updateMessageCount(userLocalMsgCount.getUserId(), messageCount);
        } catch (MessageException e) {
            log.error("saveUserLocalMsgCount error userId:{}", userLocalMsgCount.getUserId(), e);
        }
    }

    @Override
    public void removeMessageByUserGroupId(String userId, String groupId) {
        //TODO 后面添加 暂时没用到相关逻辑
        try {
            List<com.handwin.database.bean.Message> messageList = rpcMessageService.findUnReadMessage(userId, Integer.MAX_VALUE);
            if (messageList != null && messageList.size() > 0) {
                messageList.stream().forEach(mm -> {
                    if (StringUtils.isNotBlank(mm.getGroupID()) && mm.getGroupID().equals(groupId)) {
                        try {
                            rpcMessageService.onlineReceive(userId, mm.getMessageID());
                        } catch (MessageException e) {
                            log.error("removeMessageByUserGroupId error userId:{} , messageID:{} ", userId, mm.getMessageID());
                        }
                    }
                });
            }
        } catch (MessageException e) {
            log.error("removeMessageByUserGroupId error userId:{} , groupId:{} ", userId, groupId);
        }
    }

    @Override
    public UserLocalMsgCounter updateUnreadLocalCount(String userId, int delta, boolean operate) {
        UserLocalMsgCounter userLocalMsgCounter = new UserLocalMsgCounter(); //Operate 无意义
        userLocalMsgCounter.setUserId(userId);
        userLocalMsgCounter.setCounter(CounterBuilder.incr(delta));
        saveUserLocalMsgCount(userLocalMsgCounter);
        return userLocalMsgCounter;
    }

    @Override
    public boolean hasNewMessage(String userId) {
        try {
            List<com.handwin.database.bean.Message> list = rpcMessageService.findUnReadMessage(userId, 1);
            return list != null && list.size() > 0;
        } catch (MessageException e) {
            return false;
        }
    }

    @Override
    public Message getMessage(String userId, long messageId) {
        try {
            com.handwin.database.bean.Message mm = rpcMessageService.getMessage(userId, messageId);
            return parse(mm);
        } catch (MessageException e) {
            log.error("getMessage error userId:{} , messageId:{} ", userId, messageId, e);
            return null;
        }
    }

    @Override
    public boolean removeMessage(String userId, Long messageId) {
        try {
            rpcMessageService.updateMessageList(userId, Arrays.asList(messageId), MessageStatus.PULL_ACK);
        } catch (MessageException e) {
            log.error("removeMessage error userId:{} , messageId:{} ", userId, messageId, e);
            return false;
        }
        return true;
    }


    @Override
    public void removeMessage(String userId, List<Long> messageIdList) {
        try {
            rpcMessageService.updateMessageList(userId, messageIdList, MessageStatus.PULL_ACK);
        } catch (MessageException e) {
            log.error("removeMessage error userId:{} ", userId, e);
        }
    }

    protected Message parse(com.handwin.database.bean.Message mm) {
        Message message = new Message();
        message.setId(mm.getMessageID());
        message.setConversationId(mm.getConversationID());
        message.setReceiver(mm.getUserID());
        message.setCreateTime(mm.getCreateTime());
        message.setRoomId(mm.getRoomID());
        message.setReceiverType(mm.getReceiveType());
        message.setSecret(mm.getSecrect());
        message.setSender(mm.getSender());
        message.setType(MessageType.findByValue(mm.getMessageType()).name());
        if (mm.getSecrect() == 1 || message.getType().equals(SYSTEM_NOTIFY_TYPE)) { // byte[] 外层 JSON 会转为 Base64 编码
            message.setContent(mm.getContent());
        } else {
            if (mm.getContent() != null && mm.getContent().length > 0) {
                message.setContent(new String(mm.getContent()));
            }
        }

        //处理消息的元数据
        if (StringUtils.isNotBlank(mm.getMeta())) {
            log.debug("mysqlMessage:{} meta:{}", mm, mm.getMeta());
            try {
                message.setMeta(objectMapper.readValue(mm.getMeta().getBytes(StandardCharsets.UTF_8), new TypeReference<Map<String, Object>>() {
                }));
            } catch (IOException e) {
                log.error(String.format("fails get parse message meta. %s", mm.getMeta()), e);
            }
        }
        return message;
    }


    public static enum MessageType {

        TEXT(1),
        CARDNAME(2),
        PICURL(3),
        VOICE_TYPE(5),
        VIDEO_TYPE(6),
        SYSTEM_NOTIFY(7),
        VIDEO_CALL(8),
        AUDIO_CALL(9),
        DEFAULT_TYPE(10),
        CMD(11),
        GAME_AUDIO_CALL(12),
        GAME_VIDEO_CALL(13),
        UNKOWN(-99);

        private final int value;

        private MessageType(int value) {
            this.value = value;
        }

        /**
         * Get the integer value of this enum value, as defined in the Thrift IDL.
         */
        public int getValue() {
            return value;
        }

        /**
         * Find a the enum type by its integer value, as defined in the Thrift IDL.
         *
         * @return null if the value is not found.
         */
        public static MessageType findByValue(int value) {
            switch (value) {
                case 1:
                    return TEXT;
                case 2:
                    return CARDNAME;
                case 3:
                    return PICURL;
                case 5:
                    return VOICE_TYPE;
                case 6:
                    return VIDEO_TYPE;
                case 7:
                    return SYSTEM_NOTIFY;
                case 8:
                    return VIDEO_CALL;
                case 9:
                    return AUDIO_CALL;
                case 11:
                    return CMD;
                case 12:
                    return GAME_AUDIO_CALL;
                case 13:
                    return GAME_VIDEO_CALL;
                default:
                    return UNKOWN;
            }
        }


        private static Map<String, MessageType> MAP = new HashMap<String, MessageType>();

        static {
            for (MessageType messageType : MessageType.values()) {
                MAP.put(messageType.name(), messageType);
            }
        }

        public static MessageType getMessageType(String messageTypeName) {
            return MAP.get(messageTypeName);
        }

    }


}
