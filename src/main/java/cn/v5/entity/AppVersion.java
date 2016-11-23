package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-27 下午3:23
 */
@Entity(table = "client_versions",keyspace = "faceshow")
public class AppVersion {
    @EmbeddedId
    private AppVersionKey id;

    @Column(name = "version_desc")
    private String desc;

    @Column(name = "download_url")
    private String downloadUrl;

    public AppVersionKey getId() {
        return id;
    }

    public void setId(AppVersionKey id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
