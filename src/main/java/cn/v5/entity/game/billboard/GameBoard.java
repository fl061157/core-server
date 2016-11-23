package cn.v5.entity.game.billboard;

import info.archinnov.achilles.annotations.*;

import java.util.Date;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 上午11:36
 */
@Entity(table="game_board",keyspace = "faceshow")
public class GameBoard {
    @EmbeddedId
    private GameSongBoardKey key;

    @Column
    private String members;

    public GameBoard() {
    }

    public GameBoard(GameSongBoardKey key, String members) {
        this.key = key;
        this.members = members;
    }

    public GameSongBoardKey getKey() {
        return key;
    }

    public void setKey(GameSongBoardKey key) {
        this.key = key;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public static class GameSongBoardKey {
        /*
         * Partition KEY part uid cannot be restricted by IN relation
         * (only the last part of the partition key can)
         */
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

        public GameSongBoardKey() {
        }

        public GameSongBoardKey(String uid, Integer appId, Date date) {
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
}
