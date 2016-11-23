package cn.v5.entity;

import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.internal.utils.UUIDGen;

import java.util.UUID;

/**
 * Created by yangwei on 14-11-28.
 */
@Entity(table = "account_history",keyspace = "faceshow")
public class AccountHistory {
    @EmbeddedId
    private UserAccountKey userAccountKey;
    @Column
    private String account;

    public UserAccountKey getUserAccountKey() {
        return userAccountKey;
    }

    public void setUserAccountKey(UserAccountKey userAccountKey) {
        this.userAccountKey = userAccountKey;
    }


    public static class UserAccountKey {
        @Column
        @Order(1)
        private String userId;

        @TimeUUID
        @Column
        @Order(2)
        private UUID time;

        public UserAccountKey(String userId, UUID time) {
            this.userId = userId;
            this.time = time;
        }

        public UserAccountKey() {

        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public UUID getTime() {
            return time;
        }

        public void setTime(UUID time) {
            this.time = time;
        }
    }


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public AccountHistory() {
    }

    public AccountHistory(UserAccountKey userAccountKey, String account) {
        this.userAccountKey = userAccountKey;
        this.account = account;
    }

    public AccountHistory(String userId, String account) {
        this.userAccountKey = new UserAccountKey(userId, UUIDGen.getTimeUUID());
        this.account = account;
    }
}