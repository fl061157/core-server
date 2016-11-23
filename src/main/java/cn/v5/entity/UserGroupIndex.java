package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

/**
 * 用户拥有的群组列表
 */
@Entity(table = "user_group_index",keyspace = "faceshow", comment = "用户加入的群组表")
public class UserGroupIndex {
    @EmbeddedId
    private UserGroupIndexKey id;

    public UserGroupIndex() {

    }

    public UserGroupIndex(UserGroupIndexKey id) {
        this.id = id;
    }

    public UserGroupIndexKey getId() {
        return id;
    }

    public void setId(UserGroupIndexKey id) {
        this.id = id;
    }
}
