package cn.v5.bean;

/**
 * Created by piguangtao on 15/12/11.
 */
public class RecommandUserWithOtherApp {
    private String userId;
    private String appUserId;
    private String source;
    private Long updateTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecommandUserWithOtherApp{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", appUserId='").append(appUserId).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", updateTime=").append(updateTime);
        sb.append('}');
        return sb.toString();
    }
}
