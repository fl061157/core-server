package cn.v5.entity.game.song;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Order;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-19 下午4:40
 */
//@Entity(table = "game_song_stars")
public class GameSongStar {
    @EmbeddedId
    private GameSongStarKey id;

    @Column
    private Integer stars;

    public GameSongStar() {
    }

    public GameSongStar(GameSongStarKey id, Integer stars) {
        this.id = id;
        this.stars = stars;
    }

    public GameSongStarKey getId() {
        return id;
    }

    public void setId(GameSongStarKey id) {
        this.id = id;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public static class GameSongStarKey {
        @Column
        @Order(1)
        private String uid;

        @Column(name="song_id")
        @Order(2)
        private Integer songId;

        @Column
        @Order(3)
        private Integer mode;

        public GameSongStarKey() {
        }

        public GameSongStarKey(Integer songId, String uid, Integer mode) {
            this.songId = songId;
            this.uid = uid;
            this.mode = mode;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Integer getSongId() {
            return songId;
        }

        public void setSongId(Integer songId) {
            this.songId = songId;
        }

        public Integer getMode() {
            return mode;
        }

        public void setMode(Integer mode) {
            this.mode = mode;
        }
    }
}
