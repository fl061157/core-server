package cn.v5.bean.game;

/**
 * Created by yangwei on 15-4-9.
 */
public class InvitationOrSharingEvent {
    private String fromUserId;
    private String receiverId;
    private String source;
    private String clickIp;

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getClickIp() {
        return clickIp;
    }

    public void setClickIp(String clickIp) {
        this.clickIp = clickIp;
    }

    @Override
    public String toString() {
        return "InvitationOrSharingEvent{" +
                "fromUserId='" + fromUserId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", source='" + source + '\'' +
                ", clickIp='" + clickIp + '\'' +
                '}';
    }
}
