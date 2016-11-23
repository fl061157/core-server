package cn.v5.entity.oauth;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Index;
import info.archinnov.achilles.annotations.PartitionKey;

import java.util.Arrays;

/**
 * Created by haowang on 16/9/8.
 */
@Entity(table = "oauth_access_token")
public class AccessToken {
    @PartitionKey
    @Column(name = "token_id")
    private String tokenId;

    @Column
    private byte[] authentication;

    @Column(name = "authentication_id")
    @Index
    private String authenticationId;

    @Column(name = "client_id")
    @Index
    private String clientId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_info")
    private byte[] tokenInfo;

    @Column(name = "user_name")
    private String userName;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public byte[] getAuthentication() {
        return authentication;
    }

    public void setAuthentication(byte[] authentication) {
        this.authentication = authentication;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public void setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public byte[] getTokenInfo() {
        return tokenInfo;
    }

    public void setTokenInfo(byte[] tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "tokenId='" + tokenId + '\'' +
                ", authentication=" + Arrays.toString(authentication) +
                ", authenticationId='" + authenticationId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenInfo=" + Arrays.toString(tokenInfo) +
                ", userName='" + userName + '\'' +
                '}';
    }
}
