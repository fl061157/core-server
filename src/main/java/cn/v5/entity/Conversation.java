package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

@Entity(table = "user_conversations",comment = "用户会话表/灰、黑名单、置顶保存")
public class Conversation {
	public Conversation() {
		
	}
    @EmbeddedId
    private ConversationKey id;

    @Column
    private int type; //1:grey 2:black 4:top 8:加星 16:免打（在线 没有声音;离线 不push）

    @Column(name = "create_time")
    private long createTime;


    public ConversationKey getId() {
        return id;
    }

    public void setId(ConversationKey id) {
        this.id = id;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", type=" + type +
                ", createTime=" + createTime +
                '}';
    }
}
