package cn.v5.packet;

import cn.v5.code.NotifyMsgType;

/**
 * Created by piguangtao on 15/5/19.
 */
public class NewPeopleMayKnownNotifyData extends NotifyData {
    public NewPeopleMayKnownNotifyData(String source) {
        super(NotifyMsgType.COMMAND_PEOPLE_YOU_MAY_KNOWN);
        NotifyInfo notifyInfo = new NotifyInfo();
        notifyInfo.setSource(source);
        this.setInfo(notifyInfo);
    }
}
