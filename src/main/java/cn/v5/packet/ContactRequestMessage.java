package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.entity.User;
import cn.v5.entity.vo.UserVo;

/**
 * Created by sunhao on 14-11-5.
 */
public class ContactRequestMessage extends NotifyMessage {
    public ContactRequestMessage(User promoter, User toUser, String msg, String pushMsg) {
        this.from = promoter.getId();
        this.to = toUser.getId();
        this.ackFlag = true;
        this.pushFlag = true;
        this.pushContent = pushMsg;

        this.data = new NotifyData(NotifyMsgType.FRIEND_CONTACT_REQUEST);
        ExtendsNotifyInfo info = new ExtendsNotifyInfo();
        info.setUser(UserVo.createFromUser(promoter));
        info.msg = msg;
        this.data.setInfo(info);
    }

    private class ExtendsNotifyInfo extends NotifyInfo {
        private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
