package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * token索引表
 */
@Entity(table = "token_user",keyspace = "faceshow",comment = "token用户索引表")
public class TokenToUser {

    @Id
    @Column(name = "token_code")
    private String token;

    @Column(name = "user_id")
    private String userId;

    public TokenToUser(){

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
