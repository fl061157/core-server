package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.entity.User;
import cn.v5.entity.vo.BaseUserVo;

/**
 * Created by sunhao on 14-11-6.
 */
public class NeighbourNotifyMessage extends NotifyMessage {
    public NeighbourNotifyMessage(String toUser, User user) {
        this.from = user.getId();
        this.to = toUser;
        this.ackFlag = true;
        this.pushFlag = false;

        this.data = new NotifyData(NotifyMsgType.DIGITAL_NOTIFY_FIND_RADAR);
        NotifyInfo notifyInfo = new NotifyInfo();
        notifyInfo.setBaseUserVo(BaseUserVo.createFromUser(user));
        this.data.setInfo(notifyInfo);

    }
}
