package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/11/26.
 */
@Entity(table = "group_account_index")
public class GroupAccountIndex {

    @PartitionKey
    @Column(name = "account")
    private String account;

    @Column(name = "group_id")
    private String groupId;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupAccountIndex{");
        sb.append("account='").append(account).append('\'');
        sb.append(", groupId='").append(groupId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
