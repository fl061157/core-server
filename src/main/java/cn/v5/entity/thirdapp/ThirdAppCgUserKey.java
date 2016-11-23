package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/7/7.
 * 支持slice query
 */
public class ThirdAppCgUserKey {

    @PartitionKey(1)
    @Column(name = "type")
    private String type;

    @PartitionKey(2)
    @Column(name = "third_app_user_id")
    private String thirdAppUserId;

    @ClusteringColumn
    @Column(name = "user_id")
    private String userId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThirdAppUserId() {
        return thirdAppUserId;
    }

    public void setThirdAppUserId(String thirdAppUserId) {
        this.thirdAppUserId = thirdAppUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThirdAppCgUserKey{");
        sb.append("type='").append(type).append('\'');
        sb.append(", thirdAppUserId='").append(thirdAppUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
