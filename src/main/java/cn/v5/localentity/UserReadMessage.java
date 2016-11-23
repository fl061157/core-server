package cn.v5.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * 记录用户上次读取的消息最大ID和可能已经读取的消息最大ID
 */
@Entity(table = "user_read_messages", comment = "用户已经读取和可能读取的最大消息ID")
public class UserReadMessage {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}
