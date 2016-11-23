package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by piguangtao on 15/7/27.
 * 用户的好友
 * key: cg user Id
 * value: 该用户第三方好友ID
 */
@Entity(table = "cg_user_third_app_friends")
public class CgThirdAppFriend {
    @CompoundPrimaryKey
    private CgThirdAppFriendKey key;

    @Column(name = "update_time")
    private Long updateTime;

    public CgThirdAppFriendKey getKey() {
        return key;
    }

    public void setKey(CgThirdAppFriendKey key) {
        this.key = key;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CgThirdAppFriend{");
        sb.append("key=").append(key);
        sb.append(", updateTime=").append(updateTime);
        sb.append('}');
        return sb.toString();
    }
}
