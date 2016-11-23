package cn.v5.entity.game.song;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 下午3:25
 */
//@Entity(table = "game_song_bags")
public class GameSongBag {
    @Id
    private Integer id;

    @Column
    private String name;

    /**
     * 歌曲id，用，分割
     */
    @Column(name="song_ids")
    private String songIds;

    /**
     * 歌曲包的限制，json str，根据具体业务实现设置
     * 类似 {"level":10, "stars":100, "experience":5000}
     */
    @Column(name="bag_limit")
    private String bagLimit;

    @Column
    private String title;

    public GameSongBag() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBagLimit() {
        return bagLimit;
    }

    public void setBagLimit(String bagLimit) {
        this.bagLimit = bagLimit;
    }

    public String getSongIds() {
        return songIds;
    }

    public void setSongIds(String songIds) {
        this.songIds = songIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
