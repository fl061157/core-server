package cn.v5.packet;

/**
 * 临时状态消息
 * 不发送PUSH
 */
public class PresenceNotifyMessage extends NotifyMessage {
    public PresenceNotifyMessage(String from,String to,NotifyData data) {
        this.from = from;
        this.to = to;
        this.data = data;
        this.ackFlag = true;
        this.pushFlag = false;
    }
}
