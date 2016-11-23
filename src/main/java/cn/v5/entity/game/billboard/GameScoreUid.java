package cn.v5.entity.game.billboard;

import info.archinnov.achilles.annotations.*;

import java.util.Comparator;
import java.util.Date;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 下午4:38
 */
@Entity(table = "game_score_uid",keyspace = "faceshow")
public class GameScoreUid {
    @EmbeddedId
    private GameSongScoreUidKey key;

    public GameScoreUid() {
    }

    public GameScoreUid(GameSongScoreUidKey key) {
        this.key = key;
    }

    public GameSongScoreUidKey getKey() {
        return key;
    }

    public void setKey(GameSongScoreUidKey key) {
        this.key = key;
    }

    public static class GameSongScoreUidKey {
        @PartitionKey
        @Column
        @Order(2)
        private Date date;

        @PartitionKey
        @Column(name = "app_id")
        @Order(1)
        private Integer appId;

        @Column
        @Order(3)
        private Integer score;

        @Column
        @Order(4)
        private String uid;

        public GameSongScoreUidKey() {
        }

        public GameSongScoreUidKey(Date date, Integer appId, Integer score, String uid) {
            this.date = date;
            this.appId = appId;
            this.score = score;
            this.uid = uid;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Integer getAppId() {
            return appId;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }
    }

    public static class ScoreComparator implements Comparator<GameScoreUid> {
        @Override
        public int compare(GameScoreUid o1, GameScoreUid o2) {
            return Integer.compare(o1.getKey().getScore(), o2.getKey().getScore());
        }
    }

    @Override
    public String toString() {
        return this.getKey().getUid() + "," + this.getKey().getDate() + "," + this.getKey().getScore();
    }
}
