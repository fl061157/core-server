package cn.v5.packet;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.User;
import cn.v5.packet.notify.INotifyInfo;

/**
 * Created by haoWang on 2016/1/6.
 */
public class GroupApplyAuditData extends NotifyData {

    public GroupApplyAuditData(String groupId, User applicant, String content) {
        super(NotifyMsgType.GROUP_APPLY);
        Info info = new Info(groupId, applicant, content);
        this.info = info;
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_TYPE, SystemConstants.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP);
        this.getExtra().put(SystemConstants.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID, groupId);
    }

    public static class Info implements INotifyInfo {
        private String groupId;
        private User applicant;
        private String content;

        public Info(String groupId, User applicant, String content) {
            this.groupId = groupId;
            this.applicant = applicant;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public User getApplicant() {
            return applicant;
        }

        public void setApplicant(User applicant) {
            this.applicant = applicant;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "groupId='" + groupId + '\'' +
                    ", applicant='" + applicant.getNickname() + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }

    }

}

