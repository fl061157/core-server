package cn.v5.service;

import cn.v5.localentity.Message;
import cn.v5.localentity.UserLocalMsgCounter;
import cn.v5.persist.MessagePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    @Qualifier("mysqlMessagePersist")
    private MessagePersist messagePersist;

    public List<Message> findMessagesByUserId(String userId, long lastMessageId, int length) {
        return messagePersist.findMessagesByUserId(userId, lastMessageId, length);
    }

    public List<Message> findMessageByUserId(String userID, List<Long> readMessageIDList, int length) {
        return messagePersist.findMessageByUserId(userID, length, readMessageIDList);
    }


    public UserLocalMsgCounter findUserLocalMsgCount(String userId) {
        return messagePersist.findUserLocalMsgCount(userId);
    }

    public UserLocalMsgCounter saveUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount) {
        return messagePersist.saveUserLocalMsgCount(userLocalMsgCount);
    }

    public void upateUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount) {
        messagePersist.upateUserLocalMsgCount(userLocalMsgCount);
    }

    public void removeMessageByUserGroupId(String userId, String groupId) {
        messagePersist.removeMessageByUserGroupId(userId, groupId);
    }

    /**
     * 更新本机未读消息
     *
     * @param userId
     * @param delta
     */
    public UserLocalMsgCounter updateUnreadLocalCount(String userId, int delta, boolean operate) {
        return messagePersist.updateUnreadLocalCount(userId, delta, operate);
    }

    /**
     * 跟进消息ID删除消息,userId为rowkey
     *
     * @param userId
     * @param messageId
     * @return
     */
    public boolean removeMessage(String userId, Long messageId) {
        return messagePersist.removeMessage(userId, messageId);
    }

    /**
     * 查看某个用户是否有新的消息(包含了离线)
     *
     * @param userId
     * @return
     */
    public boolean hasNewMessage(String userId) {
        return messagePersist.hasNewMessage(userId);
    }

    public Message getMessage(String userId, long messageId) {
        return messagePersist.getMessage(userId, messageId);
    }

}
