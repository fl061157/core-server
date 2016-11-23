package cn.v5.packet;

/**
 * Created by handwin on 2014/11/6.
 */
public class BroadcastNotifyMessage extends NotifyMessage{
    public BroadcastNotifyMessage(String from,String to,NotifyData data) {
        this.from = from;
        this.to = to;
        this.data = data;
        this.ackFlag = false;
        this.pushFlag = false;
    }
}
