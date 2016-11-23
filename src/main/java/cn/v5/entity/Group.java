package cn.v5.entity;

import cn.v5.bean.group.GroupUser;
import com.google.common.collect.Lists;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.internal.utils.UUIDGen;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;
import java.util.List;


@Entity(table = "groups", keyspace = "faceshow", comment = "群组表")
public class Group {
    @Id
    private String id;

    @Column
    private String creator;

    @Column(name = "avatar")
    private String avatarUrl;

    @JsonIgnore
    @Column(name = "create_time")
    private Date createTime;

    @JsonIgnore
    @Column(name = "update_time")
    private long updateTime;

    @Column(name = "validate")
    /**
     * 是否开启群组校验 如果开启了群组校验 则非创建者添加成员时 需要创建者同意
     */
    private String enableValidate;

    @Column(name = "desc_info")
    /**
     * 群组简介
     */
    private String desc;

    /**
     * 群组所属的区域
     */
    @Column
    private String region;

    @Column(name = "account")
    private String account;


    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Column
    private String name;

    private Integer number;

    private List<GroupUser> members = Lists.newArrayList();

    public Integer conversation;

    public Integer getConversation() {
        return conversation;
    }

    public void setConversation(Integer conversation) {
        this.conversation = conversation;
    }


    private Integer appID;

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
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


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GroupUser> getMembers() {
        return members;
    }

    public void setMembers(List<GroupUser> members) {
        this.members = members;
    }

    public void addMember(GroupUser user) {
        if (members == null)
            members = Lists.newArrayList();
        members.add(user);
    }

    public static String createUUID() {
        return UUIDGen.getTimeUUID().toString().replaceAll("\\-", "");
    }


    public Group() {

    }

    public String getEnableValidate() {
        return enableValidate;
    }

    public void setEnableValidate(String enableValidate) {
        this.enableValidate = enableValidate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Group{");
        sb.append("id='").append(id).append('\'');
        sb.append(", creator='").append(creator).append('\'');
        sb.append(", avatarUrl='").append(avatarUrl).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", enableValidate='").append(enableValidate).append('\'');
        sb.append(", desc='").append(desc).append('\'');
        sb.append(", region='").append(region).append('\'');
        sb.append(", account='").append(account).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", number=").append(number);
        sb.append(", members=").append(members);
        sb.append(", conversation=").append(conversation);
        sb.append('}');
        return sb.toString();
    }
}
