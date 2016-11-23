package cn.v5.entity.vo.game;

import cn.v5.entity.game.song.GameSong;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;
import java.util.Map;

/**
 * Created by ganqin on 14-6-18.
 */
public class SongBagVo {
    private Integer id;
    private String name;
    private boolean unlocked;
    @JsonIgnore
    private List<GameSong> songs;

    private List<Integer> songIds;

    private Map<String, Integer> limit;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GameSong> getSongs() {
        return songs;
    }

    public void setSongs(List<GameSong> songs) {
        this.songs = songs;
    }

    public Map<String, Integer> getLimit() {
        return limit;
    }

    public void setLimit(Map<String, Integer> limit) {
        this.limit = limit;
    }

    public List<Integer> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<Integer> songIds) {
        this.songIds = songIds;
    }
}
