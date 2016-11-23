package cn.v5.entity.game;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

/**
 * Created by yangwei on 14-9-19.
 */
public class GameRoleKey {
    @Column(name = "app_id")
    @Order(1)
    private Integer appId;

    @Column(name="id")
    @Order(2)
    private Integer id;

    public GameRoleKey() {
    }

    public GameRoleKey(Integer appId, Integer id) {
        this.appId = appId;
        this.id = id;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
