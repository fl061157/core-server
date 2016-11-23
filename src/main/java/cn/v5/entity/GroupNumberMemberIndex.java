package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.CompoundPrimaryKey;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/11/12.
 */
@Entity(table = "group_number_member_index")
public class GroupNumberMemberIndex {

    @CompoundPrimaryKey
    private GroupNumberMemberIndexKey key;

    @Column(name = "timestamp")
    private Long timestamp;

    public GroupNumberMemberIndexKey getKey() {
        return key;
    }

    public void setKey(GroupNumberMemberIndexKey key) {
        this.key = key;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupNumberMemberIndex{");
        sb.append("key=").append(key);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

    public static class GroupNumberMemberIndexKey {
        @PartitionKey(value = 1)
        private String groupId;
        @PartitionKey(value = 2)
        private Integer number;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "GroupMemberNumberKey{" +
                    "groupId='" + groupId + '\'' +
                    ", number='" + number + '\'' +
                    '}';
        }
    }


}
