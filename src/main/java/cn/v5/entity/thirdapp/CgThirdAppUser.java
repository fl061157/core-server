package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by piguangtao on 15/7/7.
 */
@Entity(table = "cg_third_app_users")
public class CgThirdAppUser {

    @CompoundPrimaryKey
    private CgThirdAppUserKey key;

    @Column(name = "third_app_user_id")
    private String thirdAppUserId;


    public CgThirdAppUserKey getKey() {
        return key;
    }

    public void setKey(CgThirdAppUserKey key) {
        this.key = key;
    }

    public String getThirdAppUserId() {
        return thirdAppUserId;
    }

    public void setThirdAppUserId(String thirdAppUserId) {
        this.thirdAppUserId = thirdAppUserId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CgThirdAppUser{");
        sb.append("key=").append(key);
        sb.append(", thirdAppUserId='").append(thirdAppUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
