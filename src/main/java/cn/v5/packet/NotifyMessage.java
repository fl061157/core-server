package cn.v5.packet;

import cn.v5.code.NotifyMsgType;

import java.util.UUID;

/**
 * 系统消息基类
 */
public class NotifyMessage {
    protected String type = "system";
    protected String from;
    protected String to;
    /**
     * 是否需要离线存储，确认收到
     */
    protected boolean ackFlag;
    /**
     * 离线下是否发送push
     */
    protected boolean pushFlag;

    protected boolean groupFlag = false ;

    protected String pushContent;
    protected NotifyData data;
    protected int appId = 0;
    protected boolean incrOfflineCount = false;

    /*
     * 消息id的长度为36个字节
     */
    protected String cmsgId = UUID.randomUUID().toString();

    public boolean isIncrOfflineCount() {
        return incrOfflineCount;
    }

    public void setIncrOfflineCount(boolean incrOfflineCount) {
        this.incrOfflineCount = incrOfflineCount;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean getAckFlag() {
        return ackFlag;
    }

    public void setAckFlag(boolean ackFlag) {
        this.ackFlag = ackFlag;
    }

    public boolean getPushFlag() {
        return pushFlag;
    }

    public void setPushFlag(boolean pushFlag) {
        this.pushFlag = pushFlag;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public NotifyData getData() {
        return data;
    }

    public void setData(NotifyData data) {
        this.data = data;
    }

    public String getCmsgId() {
        return cmsgId;
    }

    public void setCmsgId(String cmsgId) {
        this.cmsgId = cmsgId;
    }

    public boolean isGroupFlag() {
        return groupFlag;
    }

    public void setGroupFlag(boolean groupFlag) {
        this.groupFlag = groupFlag;
    }

    public byte getMsgTye() {
        byte result = 0x01;
        if (null != data) {
            switch (data.getType()) {
                case NotifyMsgType.USER_REGISTER:
                    result = (byte) 0x02;
                    break;
                case NotifyMsgType.DIGITAL_RADAR_ADDUSER:
                    result = (byte) 0x0a;
                    break;
                case NotifyMsgType.DIGITAL_RADAR_REMOVEUSER:
                    result = (byte) 0x0b;
                    break;
                case NotifyMsgType.GROUP_ADDUSER:
                case NotifyMsgType.GROUP_CREATE:
                case NotifyMsgType.GROUP_DISMISS:
                case NotifyMsgType.GROUP_EXIT:
                case NotifyMsgType.GROUP_UPDATE:
                case NotifyMsgType.GROUP_REMOVEUSER:
                case NotifyMsgType.GROUP_INVITE_AUDIT:
                    result = (byte) 0x01;
                    break;
                case NotifyMsgType.SECRETARY_NOTIFY:
                case NotifyMsgType.COMMAND_PEOPLE_YOU_MAY_KNOWN:
                    result = (byte) 0x02;
                    break;
                case NotifyMsgType.USER_UPDATE:
                    result = (byte) 0x02;
                    break;
                case NotifyMsgType.FRIEND_CONTACT_REQUEST:
                    result = (byte) 0x0c;
                    break;
                case NotifyMsgType.FRIEND_CONTACT_REQUEST_SUCCESS:
                    result = (byte) 0x0d;
                    break;
                case NotifyMsgType.DIGITAL_NOTIFY_FIND_RADAR:
                    result = (byte) 0x08;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * 获取系统通知的离线push通知类型
     * 以便客户端(IOS)根据不同的通知类型 进入到不同的界面
     *
     * @return
     */
    public String getNotifyTye() {
        String result = null;
        if (null != data) {
            switch (data.getType()) {
                case NotifyMsgType.USER_REGISTER:
                    result = "1";
                    break;
                case NotifyMsgType.DIGITAL_RADAR_ADDUSER:
                    result = "2";
                    break;
                case NotifyMsgType.DIGITAL_RADAR_REMOVEUSER:
                    result = "3";
                    break;
                case NotifyMsgType.GROUP_ADDUSER:
                    result = "4";
                    break;
                case NotifyMsgType.GROUP_CREATE:
                    result = "5";
                    break;
                case NotifyMsgType.GROUP_DISMISS:
                    result = "6";
                    break;
                case NotifyMsgType.GROUP_EXIT:
                    result = "7";
                    break;
                case NotifyMsgType.GROUP_UPDATE:
                    result = "8";
                    break;
                case NotifyMsgType.GROUP_REMOVEUSER:
                    result = "9";
                    break;
                case NotifyMsgType.GROUP_INVITE_AUDIT:
                    result = "10";
                    break;
                case NotifyMsgType.SECRETARY_NOTIFY:
                    result = "11";
                    break;
                case NotifyMsgType.COMMAND_PEOPLE_YOU_MAY_KNOWN:
                    result = "12";
                    break;
                case NotifyMsgType.USER_UPDATE:
                    result = "13";
                    break;
                case NotifyMsgType.FRIEND_CONTACT_REQUEST:
                    result = "14";
                    break;
                case NotifyMsgType.FRIEND_CONTACT_REQUEST_SUCCESS:
                    result = "15";
                    break;
                case NotifyMsgType.DIGITAL_NOTIFY_FIND_RADAR:
                    result = "16";
                    break;
                case NotifyMsgType.GROUP_APPLY_ADDUSER:
                    result = "17";
                    break;
                case NotifyMsgType.GROUP_APPLY:
                    result = "18";
                    break;
                default:
                    break;
            }
        }
        return result;
    }
}
