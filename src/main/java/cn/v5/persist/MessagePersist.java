package cn.v5.persist;

import cn.v5.localentity.Message;
import cn.v5.localentity.UserLocalMsgCounter;

import java.util.List;

/**
 * Created by fangliang on 16/2/15.
 */
public interface MessagePersist {

    public List<Message> findMessagesByUserId(String userId, long lastMessageId, int length);

    public List<Message> findMessageByUserId(String userId, int length, List<Long> updateMessageIdList);

    public UserLocalMsgCounter findUserLocalMsgCount(String userId);

    public UserLocalMsgCounter saveUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount);

    public void upateUserLocalMsgCount(UserLocalMsgCounter userLocalMsgCount);

    public void removeMessageByUserGroupId(String userId, String groupId);

    public UserLocalMsgCounter updateUnreadLocalCount(String userId, int delta, boolean operate);

    public boolean hasNewMessage(String userId);

    public Message getMessage(String userId, long messageId);

    public boolean removeMessage(String userId, Long messageId);

    public void removeMessage(String userId, List<Long> messageIdList);


}
