package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by piguangtao on 15/7/7.
 * 主键为第三方的id  value为cg的用户信息
 */
@Entity(table = "third_app_users_cg")
public class ThirdAppCgUser {

    @CompoundPrimaryKey
    private ThirdAppCgUserKey key;

    @Column(name = "access_token")
    private String accessToken;

    public ThirdAppCgUserKey getKey() {
        return key;
    }

    public void setKey(ThirdAppCgUserKey key) {
        this.key = key;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThirdAppCgUser{");
        sb.append("key=").append(key);
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
