package cn.v5.entity.game;

import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.internal.utils.UUIDGen;

import java.util.Map;
import java.util.UUID;

/**
 * Created by yangwei on 14-12-2.
 */
@Entity(table="user_report")
public class UserReport {
    @EmbeddedId
    private UserReportKey urk;

    @Column
    private Map<String, String> attr;

    public UserReportKey getUrk() {
        return urk;
    }

    public void setUrk(UserReportKey urk) {
        this.urk = urk;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    public UserReport() {
    }

    public UserReport(UserReportKey urk) {
        this.urk = urk;
    }

    public static class UserReportKey {
        @PartitionKey
        @Order(1)
        private String uid;

        @PartitionKey
        @Column(name="game_id")
        @Order(2)
        private Integer gameId;


        @TimeUUID
        @Column
        @Order(3)
        private UUID time;

        public UserReportKey() {
        }

        public UserReportKey(String uid, Integer gameId) {
            this.uid = uid;
            this.gameId = gameId;
            this.time = UUIDGen.getTimeUUID();
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Integer getGameId() {
            return gameId;
        }

        public void setGameId(Integer gameId) {
            this.gameId = gameId;
        }

        public UUID getTime() {
            return time;
        }

        public void setTime(UUID time) {
            this.time = time;
        }
    }
}
