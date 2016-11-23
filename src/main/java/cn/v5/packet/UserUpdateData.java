package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.entity.vo.UserVo;

/**
 * 用户数据变更系统消息
 */
public class UserUpdateData extends NotifyData {


    public UserUpdateData(UserVo user) {
        super(NotifyMsgType.USER_UPDATE);
        NotifyInfo info = new NotifyInfo();
        info.setUser(user);
        this.info = info;
    }
}
