package cn.v5.entity.game.billboard;

import info.archinnov.achilles.annotations.*;

import java.util.Comparator;
import java.util.Date;

/**
 * 音乐游戏的歌曲
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 下午2:00
 */
@Entity(table = "game_score",keyspace = "faceshow")
public class GameScore {
    private static final int def = 0;

    @EmbeddedId
    private GameSongScoreKey key;

    /**
     * 每天只记录玩的最高的成绩
     */
    @Column
    private Integer score;

    public GameScore() {
    }

    public GameScore(GameSongScoreKey key) {
        this.key = key;
        this.score = def;
    }

    public GameScore(GameSongScoreKey key, Integer score) {
        this.key = key;
        this.score = score;
    }

    public GameSongScoreKey getKey() {
        return key;
    }

    public void setKey(GameSongScoreKey key) {
        this.key = key;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public static class GameSongScoreKey {
        @PartitionKey
        @Column
        @Order(2)
        private String uid;

        @PartitionKey
        @Column(name = "app_id")
        @Order(1)
        private Integer appId;

        @Column
        @Order(3)
        private Date date;

        public GameSongScoreKey() {
        }

        public GameSongScoreKey(String uid, Integer appId, Date date) {
            this.uid = uid;
            this.appId = appId;
            this.date = date;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Integer getAppId() {
            return appId;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }
    }

    public static class ScoreComparator implements Comparator<GameScore> {
        @Override
        public int compare(GameScore o1, GameScore o2) {
            return Integer.compare(o2.getScore(), o1.getScore());
        }
    }

    @Override
    public String toString() {
        return this.key.getUid() + "," + this.key.getDate() + "," + this.score;
    }
}
