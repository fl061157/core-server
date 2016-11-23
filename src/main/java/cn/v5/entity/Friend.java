package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Index;


@Entity(table = "friends", keyspace = "faceshow", comment = "chatgame好友表")
public class Friend {
    @EmbeddedId
    private UserKey id;

    @Column(name = "update_time")
    private Long updateTime;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "resource_app_id")
    @Index
    private Integer resourceAppId;  // 来源app id

    @Column(name = "source")
    private String source;


    public UserKey getId() {
        return id;
    }

    public void setId(UserKey id) {
        this.id = id;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Integer getResourceAppId() {
        return resourceAppId;
    }

    public void setResourceAppId(Integer resourceAppId) {
        this.resourceAppId = resourceAppId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Friend() {
        this.updateTime = System.currentTimeMillis();
    }

    public Friend(UserKey id, String contactName) {
        this.id = id;
        this.contactName = contactName;
        this.updateTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Friend{");
        sb.append("id=").append(id);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", contactName='").append(contactName).append('\'');
        sb.append(", resourceAppId=").append(resourceAppId);
        sb.append(", source='").append(source).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
