package cn.v5.entity.thirdapp;

import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/7/7.
 */
public class CgThirdAppUserKey {
    @PartitionKey(value = 1)
    @Column(name = "user_id")
    private String userId;

    @ClusteringColumn
    @Column(name = "type")
    private String type;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CgThirdAppUserKey{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
