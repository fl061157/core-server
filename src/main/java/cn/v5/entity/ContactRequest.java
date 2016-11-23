package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

/**
 * 请求添加为联系人的记录
 */
@Entity(table = "contact_request",keyspace = "faceshow")
public class ContactRequest {
    @EmbeddedId
    private ContactRequestKey contactRequestKey;

    @Column(name = "last_time")
    private long lastTime;

    @Column(name = "msg")
    private String msg;

    @Column(name = "source")
    private String source;

    public ContactRequest() {
    }

    public ContactRequest(ContactRequestKey contactRequestKey) {
        this.contactRequestKey = contactRequestKey;
    }

    public ContactRequestKey getContactRequestKey() {
        return contactRequestKey;
    }

    public void setContactRequestKey(ContactRequestKey contactRequestKey) {
        this.contactRequestKey = contactRequestKey;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ContactRequest{");
        sb.append("contactRequestKey=").append(contactRequestKey);
        sb.append(", lastTime=").append(lastTime);
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
