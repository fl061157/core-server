package cn.v5.openplatform.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/7/8.
 */
public class AppCgUserKey {
    @PartitionKey(1)
    @Column(name = "app_key")
    private Integer appKey;

    @PartitionKey(2)
    @Column(name = "app_user_id")
    private String appUserId;

    public Integer getAppKey() {
        return appKey;
    }

    public void setAppKey(Integer appKey) {
        this.appKey = appKey;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppCgUserKey{");
        sb.append("appKey=").append(appKey);
        sb.append(", appUserId='").append(appUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
