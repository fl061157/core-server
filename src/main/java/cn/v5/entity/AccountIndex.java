package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by lb on 14/10/29.
 */
@Entity(table = "account_indexes",keyspace = "faceshow")
public class AccountIndex {
    @Id
    private String account;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "password_hash")
    private String password;

    public AccountIndex() {

    }

    public AccountIndex(String account, String userId) {
        this.account = account;
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
