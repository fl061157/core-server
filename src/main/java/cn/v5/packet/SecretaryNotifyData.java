package cn.v5.packet;

import cn.v5.code.NotifyMsgType;

/**
 * Created by hi on 14-4-15.
 */
public class SecretaryNotifyData extends NotifyData {



    public SecretaryNotifyData(String desc,String from,String msgType,String userId) {
        super(NotifyMsgType.SECRETARY_NOTIFY);
        this.info = new NotifyInfo(desc,from,msgType,userId);
    }
}
