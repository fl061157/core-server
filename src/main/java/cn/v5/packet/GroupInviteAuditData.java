package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.User;
import cn.v5.packet.notify.INotifyInfo;

import java.util.List;

/**
 * Created by piguangtao on 15/11/19.
 */
public class GroupInviteAuditData extends NotifyData {

    public GroupInviteAuditData(String groupId, String inviter, List<User> members) {
        super(NotifyMsgType.GROUP_INVITE_AUDIT);
        Info info = new Info(groupId, inviter, members);
        this.info = info;
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, groupId);
    }

    public static class Info implements INotifyInfo {
        private String groupId;
        private String inviter;
        private List<User> members;

        public Info(String groupId, String inviter, List<User> members) {
            this.groupId = groupId;
            this.inviter = inviter;
            this.members = members;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getInviter() {
            return inviter;
        }

        public void setInviter(String inviter) {
            this.inviter = inviter;
        }

        public List<User> getMembers() {
            return members;
        }

        public void setMembers(List<User> members) {
            this.members = members;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Info{");
            sb.append("groupId='").append(groupId).append('\'');
            sb.append(", inviter='").append(inviter).append('\'');
            sb.append(", members=").append(members);
            sb.append('}');
            return sb.toString();
        }
    }

}
