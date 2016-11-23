package cn.v5.entity.group;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/11/20.
 * 用于创建群组的幂等请求操作
 */
@Entity(table = "group_create_req_index")
public class GroupCreateReqIndex {

    @PartitionKey
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "group_id")
    private String groupId;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
