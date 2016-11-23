package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-29 上午11:46
 */
public class AppVersionKey {
    @Column(name = "device_type")
    @Order(1)
    private Integer deviceType;

    @Column(name = "cert")
    @Order(2)
    private String cert;

    @Column(name = "app_id")
    @Order(3)
    private Integer appId;

    public AppVersionKey() {
    }

    public AppVersionKey(Integer appId, Integer deviceType, String cert) {
        this.appId = appId;
        this.deviceType = deviceType;
        this.cert = cert;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }
}
