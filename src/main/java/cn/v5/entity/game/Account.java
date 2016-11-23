package cn.v5.entity.game;

import info.archinnov.achilles.annotations.*;

/**
 * Created by yangwei on 14-9-15.
 */
@Entity(table = "account",keyspace = "faceshow")
public class Account {
    @Id
    private String userName;
    @Column(name = "password_hash")
    private String password;
    @Column
    private String userId;
    @Column(name="facebook_id")
    @Index(name="facebook_id")
    private String facebookId;
    @Column(name="twitter_id")
    @Index(name="twitter_id")
    private String twitterId;
    @Column(name="weibo_id")
    @Index(name="weibo_id")
    private String weiboId;


    public Account() {
    }

    public Account(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public String getWeiboId() {
        return weiboId;
    }

    public void setWeiboId(String weiboId) {
        this.weiboId = weiboId;
    }

}
