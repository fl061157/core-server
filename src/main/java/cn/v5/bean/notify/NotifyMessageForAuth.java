package cn.v5.bean.notify;

/**
 * Created by piguangtao on 15/6/8.
 */
public class NotifyMessageForAuth {
    private byte msgType;
    private boolean store;
    private boolean replyRead;
    private boolean push;
    private boolean increadByOneFromPush;
    /*
     * 是否确保消息接受
     */
    private boolean ensureReceive;
    private String from;

    /**
     * 多个接受方之间采用逗号作为分隔符
     */
    private String to;
    private String pushBody;
    private String messageBody;

    /**
     * 客户端生成的消息唯一标示
     */
    private String cmsgId;

    private int appID;

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public boolean isStore() {
        return store;
    }

    public void setStore(boolean store) {
        this.store = store;
    }

    public boolean isReplyRead() {
        return replyRead;
    }

    public void setReplyRead(boolean replyRead) {
        this.replyRead = replyRead;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public boolean isIncreadByOneFromPush() {
        return increadByOneFromPush;
    }

    public void setIncreadByOneFromPush(boolean increadByOneFromPush) {
        this.increadByOneFromPush = increadByOneFromPush;
    }

    public boolean isEnsureReceive() {
        return ensureReceive;
    }

    public void setEnsureReceive(boolean ensureReceive) {
        this.ensureReceive = ensureReceive;
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

    public String getPushBody() {
        return pushBody;
    }

    public void setPushBody(String pushBody) {
        this.pushBody = pushBody;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getCmsgId() {
        return cmsgId;
    }

    public void setCmsgId(String cmsgId) {
        this.cmsgId = cmsgId;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotifyMessage{");
        sb.append("msgType=").append(msgType);
        sb.append(", store=").append(store);
        sb.append(", replyRead=").append(replyRead);
        sb.append(", push=").append(push);
        sb.append(", increadByOneFromPush=").append(increadByOneFromPush);
        sb.append(", ensureReceive=").append(ensureReceive);
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", pushBody='").append(pushBody).append('\'');
        sb.append(", messageBody='").append(messageBody).append('\'');
        sb.append(", cmsgId='").append(cmsgId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
