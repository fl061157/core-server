package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

public class UserGroupIndexKey {
    @Column(name = "user_id")
    @Order(1)
    private String userId;


    @Column(name = "group_id")
    @Order(2)
    private String groupId;

    public UserGroupIndexKey() {

    }

    public UserGroupIndexKey(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
