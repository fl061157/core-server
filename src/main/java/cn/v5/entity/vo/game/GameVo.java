package cn.v5.entity.vo.game;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 上午10:56
 */
public class GameVo {

    private String classification;

    private String name;

    private String desc;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("app_id")
    private Integer appId;

    private String version;

    @JsonProperty("bbscomments")
    private Integer bbsComments;

    @JsonProperty("desc_image_url")
    private String descImageUrl;

    @JsonProperty("desc_video_url")
    private String descVideoUrl;

    @JsonProperty("game_images_url")
    private List<String> gameImagesUrl;

    @JsonProperty("video_res")
    private String videoRes;

    @JsonProperty("download_url")
    private String downloadUrl;

    @JsonProperty("media_type")
    private Integer mediaType;

    @JsonProperty("min_ver")
    private String minVer;

    @JsonProperty("update_url")
    private String updateUrl;

    private List<String> contacts;

    private Long online;

    public GameVo(String classification, String name, String desc, String avatarUrl, Integer appId, String version,
                  Integer bbsComments, String descImageUrl, String descVideoUrl, List<String> gameImagesUrl,
                  String videoRes, String downloadUrl, List<String> contacts, Integer mediaType) {
        this.classification = classification;
        this.name = name;
        this.desc = desc;
        this.avatarUrl = avatarUrl;
        this.appId = appId;
        this.version = version;
        this.bbsComments = bbsComments;
        this.descImageUrl = descImageUrl;
        this.descVideoUrl = descVideoUrl;
        this.gameImagesUrl = gameImagesUrl;
        this.videoRes = videoRes;
        this.downloadUrl = downloadUrl;
        this.contacts = contacts;
        this.mediaType = mediaType;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getBbsComments() {
        return bbsComments;
    }

    public void setBbsComments(Integer bbsComments) {
        this.bbsComments = bbsComments;
    }

    public String getDescImageUrl() {
        return descImageUrl;
    }

    public void setDescImageUrl(String descImageUrl) {
        this.descImageUrl = descImageUrl;
    }

    public String getDescVideoUrl() {
        return descVideoUrl;
    }

    public void setDescVideoUrl(String descVideoUrl) {
        this.descVideoUrl = descVideoUrl;
    }

    public List<String> getGameImagesUrl() {
        return gameImagesUrl;
    }

    public void setGameImagesUrl(List<String> gameImagesUrl) {
        this.gameImagesUrl = gameImagesUrl;
    }

    public String getVideoRes() {
        return videoRes;
    }

    public void setVideoRes(String videoRes) {
        this.videoRes = videoRes;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Integer getMediaType() {
        return mediaType;
    }

    public void setMediaType(Integer mediaType) {
        this.mediaType = mediaType;
    }

    public String getMinVer() {
        return minVer;
    }

    public void setMinVer(String minVer) {
        this.minVer = minVer;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public Long getOnline() {
        return online;
    }

    public void setOnline(Long online) {
        this.online = online;
    }
}
