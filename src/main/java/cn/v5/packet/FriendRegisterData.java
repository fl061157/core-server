package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.entity.vo.UserVo;

/**
 * 好友注册通知
 */
public class FriendRegisterData extends NotifyData {


    public FriendRegisterData(UserVo user, String desc) {
        super(NotifyMsgType.USER_REGISTER);
        this.info = new NotifyInfo(user, desc);
    }
}
