package cn.v5.bean.group;

/**
 * Created by piguangtao on 15/11/18.
 */
public class FailUserInfo {
    private String userId;
    private String os;
    private String clientVersion;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FailUserInfo{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", os='").append(os).append('\'');
        sb.append(", clientVersion='").append(clientVersion).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
