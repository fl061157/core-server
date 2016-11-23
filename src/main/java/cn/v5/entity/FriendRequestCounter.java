package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by sunhao on 14-11-6.
 */
@Entity(table = "friend_request_counter",keyspace = "faceshow")
public class FriendRequestCounter {
    @Id(name = "user_id")
    private String userId;

    @Column(name = "push_date")
    private String pushDate;

    @Column(name = "count")
    private int count;

    public FriendRequestCounter() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPushDate() {
        return pushDate;
    }

    public void setPushDate(String pushDate) {
        this.pushDate = pushDate;
    }
}
