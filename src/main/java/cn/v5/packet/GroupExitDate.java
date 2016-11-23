package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;

/**
 * 成员退出群组消息
 */
public class GroupExitDate extends NotifyData {
    public GroupExitDate(String groupId, String userId, String desc, int number) {
        super(NotifyMsgType.GROUP_EXIT);
        NotifyInfo notifyInfo = new NotifyInfo(groupId, userId, desc);
        notifyInfo.setNumber(number);
        this.info = notifyInfo;
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, groupId);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_NUMBER, number);

    }
}