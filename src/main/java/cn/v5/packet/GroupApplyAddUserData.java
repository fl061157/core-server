package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Group;

/**
 * 通过申请加入群组，群组其他人收到的消息
 * Created by haoWang on 2016/1/7.
 */
public class GroupApplyAddUserData extends NotifyData {
    public GroupApplyAddUserData(Group group, String desc, String from) {
        super(NotifyMsgType.GROUP_APPLY_ADDUSER);

        NotifyInfo notifyInfo = new NotifyInfo(group, desc);
        notifyInfo.setFrom(from);
        this.info = notifyInfo;

        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, group.getId());

    }
}

