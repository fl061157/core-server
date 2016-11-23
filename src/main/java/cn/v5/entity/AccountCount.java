package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.type.Counter;

/**
 * Created by haoWang on 2015/4/20.
 */
@Entity(table = "account_count",keyspace = "faceshow")
public class AccountCount {
    @Id
    private String region;
    @Column
    private Counter count;

    public AccountCount(){

    }
    public AccountCount(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Counter getCount() {
        return count;
    }

    public void setCount(Counter count) {
        this.count = count;
    }
}
