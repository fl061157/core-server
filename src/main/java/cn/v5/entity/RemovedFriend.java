package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

import java.util.Date;

@Entity(table = "removed_friends", keyspace = "faceshow", comment = "已删除的好友")
public class RemovedFriend {
    @EmbeddedId
    private RemovedFriendKey id;

    @Column(name = "deleted_time")
    private Date deletedTime;

    @Column(name = "friend_id")
    private String friendId;

    public RemovedFriendKey getId() {
        return id;
    }

    public void setId(RemovedFriendKey id) {
        this.id = id;
    }

    public Date getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    public RemovedFriend() {
    }

    public RemovedFriend(RemovedFriendKey id, String friendId) {
        this.id = id;
        this.friendId = friendId;
        this.deletedTime = new Date();
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof RemovedFriend)) return false;

        RemovedFriend that = (RemovedFriend) o;

        if (deletedTime != null ? !deletedTime.equals(that.deletedTime) : that.deletedTime != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "RemovedFriend{" +
                "id=" + id +
                ", deletedTime=" + deletedTime +
                '}';
    }
}
