package cn.v5.entity;

import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by sunhao on 15-1-13.
 */
@Entity(table = "health_check", keyspace = "faceshow", comment = "health_check表")
public class HealthCheck {
    @Id
    private String key;

    public HealthCheck() {
    }

    public HealthCheck(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthCheck{");
        sb.append("key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
