package cn.v5.packet;

/**
 * 需要ACK回应确认的消息
 */
public class AckNotifyMessage extends NotifyMessage {
    public AckNotifyMessage(String from, String to, String pushContent, NotifyData data) {
        this.ackFlag = true;
        this.pushFlag = true;
        this.from = from;
        this.to = to;
        this.pushContent = pushContent;
        this.data = data;
    }

    public AckNotifyMessage(String from, String to, String pushContent, NotifyData data, Integer appId) {
        this.ackFlag = true;
        this.pushFlag = true;
        this.from = from;
        this.to = to;
        this.pushContent = pushContent;
        this.data = data;
        this.appId = appId;
    }
}
