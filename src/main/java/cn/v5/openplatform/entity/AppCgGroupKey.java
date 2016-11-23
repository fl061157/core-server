package cn.v5.openplatform.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by haoWang on 2016/6/22.
 */
public class AppCgGroupKey {
    @PartitionKey(1)
    @Column(name = "app_key")
    private Integer appKey;

    @PartitionKey(2)
    @Column(name = "app_group_id")
    private String appGoupId;

    public Integer getAppKey() {
        return appKey;
    }

    public void setAppKey(Integer appKey) {
        this.appKey = appKey;
    }

    public String getAppGoupId() {
        return appGoupId;
    }

    public void setAppGoupId(String appGoupId) {
        this.appGoupId = appGoupId;
    }

    @Override
    public String toString() {
        return "AppCgGroupKey{" +
                "appKey=" + appKey +
                ", appGoupId='" + appGoupId + '\'' +
                '}';
    }
}
