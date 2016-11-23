package cn.v5.entity.game.clean;

import cn.v5.entity.game.GameRoleKey;
import cn.v5.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by yangwei on 14-9-19.
 */
@Entity(table = "role", keyspace = "faceshow")
public class Role {
    @EmbeddedId
    GameRoleKey roleKey;

    @Column
    private Map<String, String> attr;

    public GameRoleKey getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(GameRoleKey roleKey) {
        this.roleKey = roleKey;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    public Role() {
    }

    public String getName() {
        return attr.get("name");
    }

    public Integer getPrice() {
        if (attr.get("price") == null) {
            return 0;
        }
        return Integer.valueOf(attr.get("price"));
    }

    public Map<Integer, Long> getProbability() {
        String pro = attr.get("pro");
        if (pro == null) {
            return null;
        }
        try {
            return JsonUtil.fromJson(pro, new TypeReference<Map<Integer, Long>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
