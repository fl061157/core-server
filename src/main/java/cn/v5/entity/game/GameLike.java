package cn.v5.entity.game;

/**
 * Created by yangwei on 15-3-9.
 */

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

@Entity(table = "game_like")
public class GameLike {
    @EmbeddedId
    private GameInteractionKey key;

    @Column
    private Integer like;

    public GameLike() {
    }

    public GameLike(GameInteractionKey key, Integer like) {
        this.key = key;
        this.like = like;
    }

    public GameInteractionKey getKey() {
        return key;
    }

    public void setKey(GameInteractionKey key) {
        this.key = key;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }
}
