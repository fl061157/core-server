package cn.v5.entity.game.song;

import info.archinnov.achilles.annotations.*;

import java.util.Date;

/**
 * Created by yangwei on 14-9-5.
 */
//@Entity(table="daily_song")
public class DailySong {
    @EmbeddedId
    private DailySongKey dailySongKey;

    @Column(name = "song_id")
    private Integer songId;

    public Integer getSongId() {
        return songId;
    }

    public void setSongId(Integer songId) {
        this.songId = songId;
    }

    public DailySongKey getDailySongKey() {
        return dailySongKey;
    }

    public void setDailySongKey(DailySongKey dailySongKey) {
        this.dailySongKey = dailySongKey;
    }

    public DailySong() {
    }

    public DailySong(DailySongKey dailySongKey) {
        this.dailySongKey = dailySongKey;
    }

    public static class DailySongKey {
        @Column(name = "app_id")
        @Order(1)
        private Integer appId;

        @Column
        @Order(2)
        private Date date;

        public DailySongKey() {
        }

        public DailySongKey(Integer appId, Date date) {
            this.appId = appId;
            this.date = date;
        }

        public Integer getAppId() {
            return appId;
        }

        public Date getDate() {
            return date;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
