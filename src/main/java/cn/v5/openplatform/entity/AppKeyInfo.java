package cn.v5.openplatform.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by piguangtao on 15/7/8.
 */
@Entity(table = "app_key_info")
public class AppKeyInfo {

    public static String STATUS_STARTED = "1";

//    public static String STATUS_AUDITED = "0";

    public static String STATUS_STOPPED = "-1";

    @PartitionKey
    @Column(name = "app_key")
    private Integer appKey;

    @Column(name = "app_secret")
    private String appSecret;

    @Column(name = "status")
    private String status;

    @Column(name = "remark")
    private String desc;

    @Column(name = "logo")
    private String logo;

    @Column(name = "name")
    private String name;


    @Column(name = "web_url")
    private String webUrl;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Integer getAppKey() {
        return appKey;
    }

    public void setAppKey(Integer appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "AppKeyInfo{" +
                "appKey=" + appKey +
                ", appSecret='" + appSecret + '\'' +
                ", status='" + status + '\'' +
                ", desc='" + desc + '\'' +
                ", logo='" + logo + '\'' +
                ", name='" + name + '\'' +
                ", webUrl='" + webUrl + '\'' +
                '}';
    }
}
