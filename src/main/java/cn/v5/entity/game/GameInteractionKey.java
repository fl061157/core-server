package cn.v5.entity.game;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by xzhang on 2015/3/9.
 */
public class GameInteractionKey {

    public GameInteractionKey() {
    }

    public GameInteractionKey(String uid, Integer gameId, String partner) {
        this.uid = uid;
        this.gameId = gameId;
        this.partner = partner;
    }

    @PartitionKey
    @Order(1)
    private String uid;

    @PartitionKey
    @Column(name = "game_id")
    @Order(2)
    private Integer gameId;

    @Column
    @Order(3)
    private String partner;

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

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }
}