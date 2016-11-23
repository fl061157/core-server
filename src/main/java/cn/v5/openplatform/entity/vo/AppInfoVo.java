package cn.v5.openplatform.entity.vo;

import java.io.Serializable;

/**
 * Created by haowang on 16/9/18.
 */
public class AppInfoVo implements Serializable {

    private static final Long serialVersionUID = 1L;

    private Integer key;
    private String name;
    private String logo;
    private String webUrl;
    private String desc;

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "AppInfoVo{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", webUrl='" + webUrl + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    private AppInfoVo() {

    }

    public static AppInfoVo create() {
        return new AppInfoVo();
    }

    public AppInfoVo key(Integer key) {
        this.key = key;
        return this;
    }

    public AppInfoVo name(String name) {
        this.name = name;
        return this;
    }

    public AppInfoVo desc(String desc) {
        this.desc = desc;
        return this;
    }

    public AppInfoVo webUrl(String webUrl) {
        this.webUrl = webUrl;
        return this;
    }

    public AppInfoVo logo(String logo) {
        this.logo = logo;
        return this;
    }

    public AppInfoVo build() {
        return this;
    }
}
