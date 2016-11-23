package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.type.Counter;

/**
 * Created by piguangtao on 15/11/26.
 * 用于生成群组账号
 */
@Entity(table = "group_account_count")
public class GroupAccountCount {

    @PartitionKey
    @Column(name = "country_code")
    /**
     * 前面不包含0
     */
    private String country;

    @Column
    private Counter count;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Counter getCount() {
        return count;
    }

    public void setCount(Counter count) {
        this.count = count;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupAccountCount{");
        sb.append("country='").append(country).append('\'');
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }
}
