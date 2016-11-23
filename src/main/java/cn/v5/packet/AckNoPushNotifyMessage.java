package cn.v5.packet;

/**
 * Created by piguangtao on 15/11/28.
 */
public class AckNoPushNotifyMessage extends NotifyMessage {
    public AckNoPushNotifyMessage(String from, String to, NotifyData data) {
        this.ackFlag = true;
        this.pushFlag = false;
        this.from = from;
        this.to = to;
        this.data = data;
    }

    public AckNoPushNotifyMessage(String from, String to, NotifyData data, Integer appId) {
        this.appId = appId;
        this.ackFlag = true;
        this.pushFlag = false;
        this.from = from;
        this.to = to;
        this.data = data;
    }
}
