package cn.v5.entity.vo.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by yangwei on 14-9-19.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CleanPlayerVo {
    private String id;
    private Integer win;
    private Integer total;
    private Long gold;
    private Integer level;
    @JsonProperty("next_level_needed")
    private Integer nextExp;
    @JsonProperty("next_level_experience")
    private Integer totalExp;
    @JsonProperty("max_straight")
    private Integer maxStraight;
    @JsonProperty("cur_straight")
    private Integer curStraight;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String nickname;
    private Integer sex;
    private String countryCode;

    private Map<Integer, Map<String, String>> roles;

    private Map<String, String> attr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getWin() {
        return win;
    }

    public void setWin(Integer win) {
        this.win = win;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Long getGold() {
        return gold;
    }

    public void setGold(Long gold) {
        this.gold = gold;
    }

    public Map<Integer, Map<String, String>> getRoles() {
        return roles;
    }

    public void setRoles(Map<Integer, Map<String, String>> roles) {
        this.roles = roles;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getNextExp() {
        return nextExp;
    }

    public void setNextExp(Integer nextExp) {
        this.nextExp = nextExp;
    }

    public Integer getMaxStraight() {
        return maxStraight;
    }

    public void setMaxStraight(Integer maxStraight) {
        this.maxStraight = maxStraight;
    }

    public Integer getCurStraight() {
        return curStraight;
    }

    public void setCurStraight(Integer curStraight) {
        this.curStraight = curStraight;
    }

    public Integer getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(Integer totalExp) {
        this.totalExp = totalExp;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
