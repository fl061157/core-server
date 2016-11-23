package cn.v5.bean.openplatform;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

/**
 * Created by haoWang on 2016/6/22.
 */
public class GroupVo {
    private String id;
    private String creator;
    private Date createTime;
    private long updateTime;
    private String desc;
    private String name;
    private Integer errorCode = 2000;

    private Integer number;

    private List<OpenGroupUser> member = Lists.newArrayList();

    public Integer conversation;

    @Override
    public String toString() {
        return "GroupVo{" +
                "id='" + id + '\'' +
                ", creator='" + creator + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", desc='" + desc + '\'' +
                ", name='" + name + '\'' +
                ", number=" + number +
                ", members=" + member +
                ", conversation=" + conversation +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<OpenGroupUser> getMember() {
        return member;
    }

    public void setMember(List<OpenGroupUser> member) {
        this.member = member;
    }

    public Integer getConversation() {
        return conversation;
    }

    public void setConversation(Integer conversation) {
        this.conversation = conversation;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
