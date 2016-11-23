package cn.v5.bean.openplatform;

/**
 * Created by haoWang on 2015/7/24.
 */
public class PushEvent {
    private Long appId;
    private String config;


    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "PushEvent{" +
                "appId=" + appId +
                ", config='" + config + '\'' +
                '}';
    }
}
