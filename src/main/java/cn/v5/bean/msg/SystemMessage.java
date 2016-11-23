package cn.v5.bean.msg;

/**
 * Created by haoWang on 2015/8/4.
 */
public class SystemMessage {
    private byte msgType;
    private byte msgSrvTyp;
    /**
     * 多个接受方之间采用逗号作为分隔符
     */
    private String to;
    private String cmsgId;
    private String msgBody;
    private String from;
    private int appID;

    private String pushContentBody;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte getMsgSrvTyp() {
        return msgSrvTyp;
    }

    public void setMsgSrvTyp(byte msgSrvTyp) {
        this.msgSrvTyp = msgSrvTyp;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCmsgId() {
        return cmsgId;
    }

    public void setCmsgId(String cmsgId) {
        this.cmsgId = cmsgId;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public String getPushContentBody() {
        return pushContentBody;
    }

    public void setPushContentBody(String pushContentBody) {
        this.pushContentBody = pushContentBody;
    }
}
