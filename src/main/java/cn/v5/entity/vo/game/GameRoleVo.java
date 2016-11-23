package cn.v5.entity.vo.game;

import java.util.Map;

/**
 * Created by yangwei on 14-9-19.
 */
public class GameRoleVo {
    private Integer id;
    private Map<String, String> attr;

    public GameRoleVo(Integer id, Map<String, String> attr) {
        this.id = id;
        this.attr = attr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }
}
