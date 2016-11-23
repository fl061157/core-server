package cn.v5.openplatform.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by haoWang on 2016/6/22.
 */
@Entity(table = "app_cg_group")
public class AppCgGroup {
    @CompoundPrimaryKey
    private AppCgGroupKey appCgGroupKey;

    @Column(name="group_id")
    private String GroupId;

    public AppCgGroupKey getAppCgGroupKey() {
        return appCgGroupKey;
    }

    public void setAppCgGroupKey(AppCgGroupKey appCgGroupKey) {
        this.appCgGroupKey = appCgGroupKey;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    @Override
    public String toString() {
        return "AppCgGroup{" +
                "appCgGroupKey=" + appCgGroupKey +
                ", GroupId='" + GroupId + '\'' +
                '}';
    }
}
