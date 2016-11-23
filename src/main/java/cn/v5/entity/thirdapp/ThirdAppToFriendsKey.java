package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/7/27.
 */
public class ThirdAppToFriendsKey {

    @PartitionKey(1)
    @Column(name = "third_app_user_id")
    private String thirdAppUserId;

    @PartitionKey(2)
    @Column(name = "source")
    private String source;

    /**
     * 保存该第三方用户为好友的用户ID
     */
    @ClusteringColumn
    @Column(name = "user_id")
    private String userId;


    public String getThirdAppUserId() {
        return thirdAppUserId;
    }

    public void setThirdAppUserId(String thirdAppUserId) {
        this.thirdAppUserId = thirdAppUserId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThirdAppToFriendsKey{");
        sb.append("thirdAppUserId='").append(thirdAppUserId).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
