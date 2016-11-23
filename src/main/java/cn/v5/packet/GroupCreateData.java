package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Group;


/**
 * 创建群组系统消息
 */
public class GroupCreateData extends NotifyData {
    public GroupCreateData(Group group, String desc) {
        super(NotifyMsgType.GROUP_CREATE);
        this.info = new NotifyInfo(group, desc);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, group.getId());
    }
}
