package cn.v5.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

import java.util.Date;

/**
 * Created by hi on 14-3-5.
 */
@Entity(table = "version_manager", keyspace = "faceshow", comment = "table for app version")
public class VersionControl  {

    @JsonIgnore
    @EmbeddedId
    private VersionKey id;

    /**
     * 用户ID
     */
    @Column(name = "version_desc")
    public String versionDesc;

    /**
     * 设备号，使用设备登录
     */
    @Column(name = "download_url")
    public String downloadUrl;

    /**
     * 最近登录时间
     */
    @Column(name = "client_date")
    public Date clientDate;


    @Column(name = "client_version")
    public String clientVersion;


    @Column(name = "update_time")
    public Integer updateTime;

    public VersionKey getId() {
        return id;
    }

    public void setId(VersionKey id) {
        this.id = id;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getClientDate() {
        return clientDate;
    }

    public void setClientDate(Date clientDate) {
        this.clientDate = clientDate;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Integer getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }
    public VersionControl() {
    	
    }
}

