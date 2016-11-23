package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by piguangtao on 15/7/27.
 * <p/>
 * 第三方用户是哪些cg用户的第三方好友
 */
@Entity(table = "third_app_to_friend")
public class ThirdAppToFriend {

    @CompoundPrimaryKey
    private ThirdAppToFriendsKey key;

    /**
     * 该第三方用户的名称
     */
    @Column(name = "app_user_name")
    private String appUserName;


    @Column(name = "app_id")
    private Integer appId;

    public ThirdAppToFriendsKey getKey() {
        return key;
    }

    public void setKey(ThirdAppToFriendsKey key) {
        this.key = key;
    }

    public String getAppUserName() {
        return appUserName;
    }

    public void setAppUserName(String appUserName) {
        this.appUserName = appUserName;
    }


    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThirdAppToFriend{");
        sb.append("key=").append(key);
        sb.append(", appUserName='").append(appUserName).append('\'');
        sb.append(", appId=").append(appId);
        sb.append('}');
        return sb.toString();
    }
}
