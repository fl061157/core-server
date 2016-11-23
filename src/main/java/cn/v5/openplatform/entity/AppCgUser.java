package cn.v5.openplatform.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by piguangtao on 15/7/8.
 */
@Entity(table = "app_user_cg_user")
public class AppCgUser {

    @CompoundPrimaryKey
    private AppCgUserKey key;

    @Column(name = "user_id")
    private String userId;

    public AppCgUserKey getKey() {
        return key;
    }

    public void setKey(AppCgUserKey key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppCgUser{");
        sb.append("key=").append(key);
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
