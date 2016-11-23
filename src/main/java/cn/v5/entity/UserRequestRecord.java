package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by lb on 14/11/4.
 */
@Entity(table = "user_request_records",keyspace = "faceshow")
public class UserRequestRecord {
    @Id(name = "user_id")
    private String userId;

    @Column(name = "last_request_at")
    private long lastRequestTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }

    public UserRequestRecord() {

    }

    public UserRequestRecord(String userId) {
        this.userId = userId;
        this.lastRequestTime = System.currentTimeMillis();
    }
}
