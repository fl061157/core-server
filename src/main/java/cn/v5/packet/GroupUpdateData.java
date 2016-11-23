package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Group;

/**
 * 群组更新消息
 */
public class GroupUpdateData extends NotifyData {
    public GroupUpdateData(Group group) {
        super(NotifyMsgType.GROUP_UPDATE);
        this.info = new NotifyInfo(group, "");
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, group.getId());
    }
}