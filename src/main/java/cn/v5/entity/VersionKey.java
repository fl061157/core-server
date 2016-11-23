package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

/**
 * Created by hi on 14-3-6.
 */
public class VersionKey {
    @Column(name = "device_type")
    @Order(1)
    private Integer deviceType;

    @Column(name = "app_id")
    @Order(2)
    private Integer appId;

    @Column(name = "cert")
    @Order(3)
    private String cert;

    public VersionKey() {

    }

    public VersionKey(int deviceType, int appId,String cert) {
        this.deviceType = deviceType;
        this.appId = appId;
        this.cert  = cert;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }
}
