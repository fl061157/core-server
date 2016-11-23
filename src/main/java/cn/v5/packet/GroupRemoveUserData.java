package cn.v5.packet;


import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;

/**
 * 群组删除成员消息
 */
public class GroupRemoveUserData extends NotifyData {
    public GroupRemoveUserData(String groupId, String userId, String desc, int number) {
        super(NotifyMsgType.GROUP_REMOVEUSER);
        NotifyInfo nInfo = new NotifyInfo(groupId, userId, desc);
        nInfo.setNumber( number );
        this.info = nInfo ;
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, groupId);
        //点击群组不进去会话界面
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION, Boolean.FALSE);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_NUMBER, number);
    }
}
