package cn.v5.v5protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piguangtao on 15/12/17.
 */
public class GroupMessage {
    private String traceId;
    private String groupId;
    private String sendId;
    private int appID;

    /**
     * 接受方
     */
    private List<String> receivers = new ArrayList<>();

    /**
     * 需要存储时，接受方可以从离线消息中获取
     */
    private Boolean needStore = Boolean.TRUE;

    /**
     * 接受方不在线时 是否需要推送
     */
    private Boolean needPush = Boolean.FALSE;

    /**
     * 推送消息是否增加1（适用于ios离线消息）
     */
    private Boolean pushIncr = Boolean.TRUE;

    /**
     * 是否确保消息可达 以便跨区消息 进行重发
     */
    private Boolean ensureArrive = Boolean.TRUE;

    /**
     * 点击推送栏是否进入回话界面
     */
    private Boolean enterConversationForPush = Boolean.FALSE;

    private String pushContent;

    private String msgBody;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public Boolean getNeedStore() {
        return needStore;
    }

    public void setNeedStore(Boolean needStore) {
        this.needStore = needStore;
    }

    public Boolean getNeedPush() {
        return needPush;
    }

    public void setNeedPush(Boolean needPush) {
        this.needPush = needPush;
    }

    public Boolean getPushIncr() {
        return pushIncr;
    }

    public void setPushIncr(Boolean pushIncr) {
        this.pushIncr = pushIncr;
    }

    public Boolean getEnsureArrive() {
        return ensureArrive;
    }

    public void setEnsureArrive(Boolean ensureArrive) {
        this.ensureArrive = ensureArrive;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Boolean getEnterConversationForPush() {
        return enterConversationForPush;
    }

    public void setEnterConversationForPush(Boolean enterConversationForPush) {
        this.enterConversationForPush = enterConversationForPush;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupMessage{");
        sb.append("traceId='").append(traceId).append('\'');
        sb.append(", groupId='").append(groupId).append('\'');
        sb.append(", sendId='").append(sendId).append('\'');
        sb.append(", receivers=").append(receivers);
        sb.append(", needStore=").append(needStore);
        sb.append(", needPush=").append(needPush);
        sb.append(", pushIncr=").append(pushIncr);
        sb.append(", ensureArrive=").append(ensureArrive);
        sb.append(", enterConversationForPush=").append(enterConversationForPush);
        sb.append(", pushContent='").append(pushContent).append('\'');
        sb.append(", msgBody='").append(msgBody).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
