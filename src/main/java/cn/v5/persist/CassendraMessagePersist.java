package cn.v5.persist;

import cn.v5.localentity.*;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.CounterBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

/**
 * Created by fangliang on 16/2/15.
 */

@Service("cassendraMessagePersist")
public class CassendraMessagePersist implements MessagePersist, InitializingBean {


    @Autowired
    @Qualifier("localManager")
    private PersistenceManager manager;

    @Inject
    private ObjectMapper mapper;

    @Value("${dudu.user.id}")
    private String duduID;

    private Session session;

    private PreparedStatement removeStatement;

    private PreparedStatement messageStatement;

    @Override
    public void afterPropertiesSet() throws Exception {
        session = manager.getNativeSession();
        removeStatement = session.prepare("DELETE FROM user_messages WHERE user_id = ? and message_id = ?");
        messageStatement = session.prepare("INSERT INTO user_read_messages(user_id,last_message_id,last_read_message_id) VALUES(?,?,?)");
    }

    @Override
    public List<Message> findMessagesByUserId(String userId, long lastMessageId, int length) {
        UserReadMessage lastMessage = manager.find(UserReadMessage.class, userId);
        long lastReadMessageId = (lastMessage != null && lastMessage.getLastReadMessageId() != null) ? lastMessage.getLastReadMessageId() : 0;

        if (lastReadMessageId < lastMessageId) {

            List<MessageIndex> oldMessageIndexes = manager.sliceQuery(MessageIndex.class).forSelect().withPartitionComponents(userId)
                    .fromClusterings(lastReadMessageId).toClusterings(lastMessageId).orderByAscending().get();
            for (MessageIndex mi : oldMessageIndexes) {
                removeMessage(userId, mi.getId().getMessageId());
            }
        } else {
            lastMessageId = lastReadMessageId;
        }

        long lastId = lastMessageId;
        List<MessageIndex> indexes = new ArrayList<>();
        int totalCount = 0;
        int itemSize = 0;
        do {
            List<MessageIndex> items = manager.sliceQuery(MessageIndex.class)
                    .forSelect()
                    .withPartitionComponents(userId)
                    .fromClusterings(lastId + 1)
                    .orderByAscending()
                    .get(length - totalCount);
            itemSize = items != null ? items.size() : 0;
            totalCount += itemSize;
            if (itemSize > 0) {
                lastId = items.get(items.size() - 1).getId().getMessageId();
            }
            indexes.addAll(mergeItems(items));
        } while (totalCount < length && itemSize > 0);

        if (lastMessage == null || lastReadMessageId != lastMessageId || lastId != lastMessageId) {
            BoundStatement bs = new BoundStatement(messageStatement);
            bs.bind(userId, lastId, lastMessageId);
            session.executeAsync(bs);
        }


        return FluentIterable.from(indexes).transform(indexToMessage).toList();
    }


    @Override
    public List<Message> findMessageByUserId(String userId, int length, List<Long> updateMessageIdList) {
        return null;
    }

    @Override
    public UserLocalMsgCounter findUserLocalMsgCount(String userId) {
        return manager.find(UserLocalMsgCounter.class, userId);
    }

    @Override
    public UserLocalMsgCounter saveUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount) {
        return manager.insert(userLocalMsgCount);
    }

    @Override
    public void upateUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount) {
        manager.update(userLocalMsgCount);
    }

    @Override
    public void removeMessageByUserGroupId(String userId, String groupId) {
        List<MessageIndex> indexes = manager.typedQuery(MessageIndex.class,
                select().from("user_messages").where(eq("user_id", userId))).get();
        for (MessageIndex index : indexes) {
            if (groupId.equals(index.getGroupId())) {
                removeMessage(userId, index.getId().getMessageId());
            }

        }
    }

    @Override
    public UserLocalMsgCounter updateUnreadLocalCount(String userId, int delta, boolean operate) {
        UserLocalMsgCounter localMsgCount;
        if (operate || (localMsgCount = manager.find(UserLocalMsgCounter.class, userId)) == null) {
            localMsgCount = new UserLocalMsgCounter();
            localMsgCount.setUserId(userId);
            localMsgCount.setCounter(CounterBuilder.incr(delta));
            localMsgCount = manager.insert(localMsgCount);
        } else {
            long current = localMsgCount.getCounter().get();
            if (current == delta) {
                return localMsgCount;
            } else if (delta - current > 0) {
                localMsgCount.getCounter().incr(delta - current);
            } else {
                localMsgCount.getCounter().decr(current - delta);
            }
            manager.update(localMsgCount);
        }
        return localMsgCount;
    }

    @Override
    public boolean hasNewMessage(String userId) {
        return manager.sliceQuery(MessageIndex.class).forSelect().withPartitionComponents(userId).get(3).size() > 0;
    }

    @Override
    public Message getMessage(String userId, long messageId) {
        MessageIndex mi = manager.find(MessageIndex.class, new MessageIndexKey(userId, messageId));
        return mi != null ? mi.getMessage() : null;
    }

    @Override
    public boolean removeMessage(String userId, Long messageId) {
        BoundStatement boundStatement = new BoundStatement(removeStatement);
        session.executeAsync(boundStatement.bind(userId, messageId));
        return true;
    }


    private Function<MessageIndex, Message> indexToMessage = new Function<MessageIndex, Message>() {
        @Override
        public Message apply(MessageIndex input) {
            Message message = input.getMessage();

            if (message.getSecret() == 1) {
                message.setContent(input.getContent());
            } else {
                if ("SYSTEM_NOTIFY".equalsIgnoreCase(message.getType())) {
                    message.setContent(input.getContent());
                } else {
                    message.setContent(new String(input.getContent(), Charset.forName("utf-8")));
                }
            }
            return message;
        }
    };


    private List<MessageIndex> mergeItems(List<MessageIndex> items) {
        List<MessageIndex> result = Lists.newArrayList();
        Set<String> filters = new HashSet<>();
        for (int i = items.size() - 1; i >= 0; i--) {
            MessageIndex item = items.get(i);
            if (!"SYSTEM_NOTIFY".equalsIgnoreCase(item.getMessage().getType()) || item.getMessage().getSender().equals(duduID)) { //非系统通知或者是嘟嘟小秘书发送的不剔除多余的
                result.add(item);
                continue;
            }

            String sender = item.getMessage().getSender(); //根据发送人排重
            try {
                JsonNode node = mapper.readTree(item.getContent());
                String type = node.get("type").asText();
                if ("user_update".equalsIgnoreCase(type)) { //用户更新昵称更新头像只需要发送最新一条消息即可
                    if (!filters.contains(sender)) {
                        filters.add(sender);
                        result.add(item);
                    }
                } else {
                    result.add(item);
                }
            } catch (Exception e) { //非json数据体或者没有type字段
                result.add(item);
            }

        }
        return Lists.reverse(result); //倒排变成正排
    }

    @Override
    public void removeMessage(String userId, List<Long> messageIdList) {
        //TODO
    }
}
