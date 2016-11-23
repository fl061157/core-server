package cn.v5.entity.game.clean;

import cn.v5.entity.game.GamePlayer;
import cn.v5.entity.game.GamePlayerKey;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import info.archinnov.achilles.annotations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangwei on 14-9-19.
 */
@Entity(table="user_role",keyspace = "faceshow")
public class UserRole {
    @EmbeddedId
    private PlayerRoleKey key;

    @Column
    private Long start;

    @Column
    private Long end;

    @Column
    private Map<String, String> attr;

    public UserRole() {
    }

    public UserRole(PlayerRoleKey key) {
        this.key = key;
    }

    public PlayerRoleKey getKey() {
        return key;
    }

    public void setKey(PlayerRoleKey key) {
        this.key = key;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    public static class PlayerRoleKey {
        @PartitionKey
        @Order(1)
        private String uid;

        @PartitionKey
        @Column(name="game_id")
        @Order(2)
        private Integer gameId;

        @Column(name="role_id")
        @Order(3)
        private Integer roleId;

        public PlayerRoleKey() {
        }

        public PlayerRoleKey(String uid, Integer gameId) {
            this.uid = uid;
            this.gameId = gameId;
        }

        public PlayerRoleKey(String uid, Integer gameId, Integer roleId) {
            this.uid = uid;
            this.gameId = gameId;
            this.roleId = roleId;
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

        public Integer getRoleId() {
            return roleId;
        }

        public void setRoleId(Integer roleId) {
            this.roleId = roleId;
        }
    }
}
