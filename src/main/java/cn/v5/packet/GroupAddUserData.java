package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Group;
import org.apache.commons.lang.StringUtils;

/**
 * 通过邀请加入群组，群组其他人收到的消息
 */
public class GroupAddUserData extends NotifyData {

    /**
     * 添加群组成员系统通知
     *
     * @param group
     * @param desc
     * @param from  群组的邀请方
     */
    public GroupAddUserData(Group group, String desc, String from) {
        super(NotifyMsgType.GROUP_ADDUSER);

        NotifyInfo notifyInfo = new NotifyInfo(group, desc);
        //客户端复用了代码，不允许传from参数
        //群组外用户直接申请加入的时候 不需要传递from参数
        //群组内用户邀请群组外用户加入群组时，需要添加该参数表示邀请方
        if (StringUtils.isNotBlank(from)) {
            //客户端复用了代码，不允许传from参数
            notifyInfo.setFrom(from);
        }
        this.info = notifyInfo;

        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, group.getId());

    }
}
