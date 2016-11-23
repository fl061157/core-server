package cn.v5.packet;

import cn.v5.entity.Group;
import cn.v5.entity.vo.BaseUserVo;
import cn.v5.entity.vo.UserVo;
import cn.v5.packet.notify.INotifyInfo;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-20 下午6:40
 */
public class NotifyInfo implements INotifyInfo {
    private UserVo user;
    private Group group;
    private String desc;
    private String groupId;
    private String userId;

    private String msgType;
    private String from;

    private String source;

    private BaseUserVo base_user;

    private int number;

    public NotifyInfo() {
    }

    public NotifyInfo(String groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public NotifyInfo(String groupId, String userId, String desc) {
        this.desc = desc;
        this.groupId = groupId;
        this.userId = userId;
    }

    public NotifyInfo(Group group, String desc) {
        this.group = group;
        this.desc = desc;
    }

    public NotifyInfo(UserVo user, String desc) {
        this.user = user;
        this.desc = desc;
    }

    public NotifyInfo(String desc, String from, String msgType, String userId) {
        this.desc = desc;
        this.msgType = msgType;
        this.from = from;
        this.userId = userId;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BaseUserVo getBaseUserVo() {
        return base_user;
    }

    public void setBaseUserVo(BaseUserVo baseUserVo) {
        this.base_user = baseUserVo;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
