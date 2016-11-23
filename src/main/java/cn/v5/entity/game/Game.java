package cn.v5.entity.game;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Order;
import org.springframework.util.DigestUtils;

import java.util.Map;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-10 上午11:49
 */
@Entity(table = "game_info",keyspace = "faceshow")
public class Game {
    @EmbeddedId
    private GameKey id;

    @Column
    private String classification;

    @Column
    private String description;

    @Column(name="avatar_url")
    private String avatarUrl;

    @Column(name="version")
    private String version;

    @Column(name="bbs_comments")
    private Integer bbsComments;

    @Column(name="desc_image_url")
    private String descImageUrl;

    @Column(name="desc_video_url")
    private String descVideoUrl;

    @Column(name="game_images_url")
    private String gameImagesUrl;

    @Column(name="video_res")
    private String videoRes;

    @Column
    private String name;

    @Column(name="download_url")
    private String downloadUrl;

    @Column(name="media_type")
    private Integer mediaType;

    @Column(name="inc_update")
    private Map<String, String> incUpdate;

    @Column(name="announcement")
    private String announcement;

    public Game() {
    }

    public Game(GameKey id, String classification, String desc, String avatarUrl, String version,
                Integer bbsComments, String descImageUrl, String descVideoUrl, String gameImagesUrl,
                String videoRes, String name, String downloadUrl, Integer mediaType) {
        this.id = id;
        this.classification = classification;
        this.description = desc;
        this.avatarUrl = avatarUrl;
        this.version = version;
        this.bbsComments = bbsComments;
        this.descImageUrl = descImageUrl;
        this.descVideoUrl = descVideoUrl;
        this.gameImagesUrl = gameImagesUrl;
        this.videoRes = videoRes;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.mediaType = mediaType;
    }

    public GameKey getId() {
        return id;
    }

    public void setId(GameKey id) {
        this.id = id;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getGameImagesUrl() {
        return gameImagesUrl;
    }

    public void setGameImagesUrl(String gameImagesUrl) {
        this.gameImagesUrl = gameImagesUrl;
    }

    public String getVideoRes() {
        return videoRes;
    }

    public void setVideoRes(String videoRes) {
        this.videoRes = videoRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Map<String, String> getIncUpdate() {
        return incUpdate;
    }

    public void setIncUpdate(Map<String, String> incUpdate) {
        this.incUpdate = incUpdate;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public static class GameKey {
        @Column
        @Order(1)
        private Integer id;

        @Column(name = "country_code")
        @Order(2)
        private String countryCode;

        public GameKey() {
        }

        public GameKey(Integer id, String countryCode) {
            this.id = id;
            this.countryCode = countryCode;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

    }

    public static void main(String[] args) {
        String str = "d399640d0e4d47cfb9ffd5793d6ab0c1";
        System.out.println(DigestUtils.md5DigestAsHex((str + "13711011519").getBytes()));
    }
}
