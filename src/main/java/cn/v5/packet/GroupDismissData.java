package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Group;

/**
 * 群组解散消息
 */
public class GroupDismissData extends NotifyData {
    public GroupDismissData(Group group, String desc) {
        super(NotifyMsgType.GROUP_DISMISS);
        NotifyInfo info = new NotifyInfo();
        info.setGroupId(group.getId());
        info.setDesc(desc);
        info.setNumber( group.getNumber() );
        this.info = info;
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, group.getId());
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_MESSAGE_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRA_GROUP_MESSAGE_TYPE_DISMISS);

        //点击群组不进去会话界面
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION, Boolean.FALSE);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_NUMBER, group.getNumber());
    }
}
