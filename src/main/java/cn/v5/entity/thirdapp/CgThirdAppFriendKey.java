package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/7/27.
 */
public class CgThirdAppFriendKey {
    @PartitionKey
    private String userId;
    @ClusteringColumn(value = 1)
    private String source;

    @ClusteringColumn(value = 2)
    @Column(name = "app_user_id")
    private String appUserId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CgThirdAppFriendKey{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", appUserId='").append(appUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
