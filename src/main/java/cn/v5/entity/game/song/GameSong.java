package cn.v5.entity.game.song;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-19 上午10:12
 */
//@Entity(table = "game_songs")
public class GameSong {
    @Id
    private Integer id;

    @Column
    private String name;

    @Column
    private String singer;

    @Column
    private String remark;

    @Column(name="download_url")
    private String downloadUrl;

    @Column(name="page_url")
    private String pageUrl;

    public GameSong() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
}
