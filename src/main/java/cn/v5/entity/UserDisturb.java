package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * 用户免打扰设置
 */
@Entity(table = "user_disturbs",keyspace = "faceshow", comment = "用户免打扰设置表")
public class UserDisturb {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "hide_time")
    private String hideTime;

    @Column
    private String language;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHideTime() {
        return hideTime;
    }

    public void setHideTime(String hideTime) {
        this.hideTime = hideTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UserDisturb() {

    }

    public UserDisturb(String userId, String hideTime,String language) {
        this.userId = userId;
        this.hideTime = hideTime;
        this.language = language;
    }
}
