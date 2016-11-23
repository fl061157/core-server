package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.entity.User;
import cn.v5.entity.vo.UserVo;

/**
 * 添加好友成功的系统消息
 */
public class ContactSuccessMessage extends NotifyMessage {
    public ContactSuccessMessage(User sender, User receiver) {
        this.from = sender.getId();
        this.to = receiver.getId();
        this.ackFlag = true;
        this.pushFlag = false;

        this.data = new NotifyData(NotifyMsgType.FRIEND_CONTACT_REQUEST_SUCCESS);
        NotifyInfo info = new NotifyInfo();
        info.setUser(UserVo.createFromUser(sender));
        this.data.setInfo(info);
    }
}
