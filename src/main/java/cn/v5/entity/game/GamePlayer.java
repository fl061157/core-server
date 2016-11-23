package cn.v5.entity.game;

import cn.v5.util.Constants;
import cn.v5.util.IntegerUtils;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

import java.util.Map;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 下午4:47
 */
@Entity(table = "game_players",keyspace = "faceshow")
public class GamePlayer {
    @EmbeddedId
    private GamePlayerKey key;

    @Column
    private Integer level;

    /**
     * 总星星数
     */
    @Column
    private Integer stars;

    @Column
    private Integer experience;

    @Column
    private Integer title;

    /**
     * 对战赢得场次
     */
    @Column(name = "win_num")
    private Integer winNum;

    /**
     * 对战平局场次
     */
    @Column(name = "tie_num")
    private Integer tieNum;

    /**
     * 对战输掉场次
     */
    @Column(name = "lose_num")
    private Integer loseNum;

    /**
     * 获得三星的场次
     */
    @Column(name = "three_star_num")
    private Integer threeStarNum;

    /**
     * 体力
     */
    @Column
    private Integer power;

    /**
     * 体力的消耗时间（秒），记录第一次消耗体力的时间
     */
    @Column(name = "power_consume_time")
    private Long powerConsumeTime;

    /**
     * 体力的恢复时间（秒），记录上一次恢复体力的时间
     */
    @Column(name = "power_recover_time")
    private Long powerRecoverTime;

    /**
     * highest score
     */
    @Column
    private Integer record;

    // straight victory related for a moment
    @Column
    private Map<String, String> attr;

    public GamePlayer() {
    }

    public GamePlayer(GamePlayerKey id, Integer level, Integer stars, Integer experience, Integer title, Integer winNum) {
        this.key = id;
        this.level = level;
        this.stars = stars;
        this.experience = experience;
        this.title = title;
        this.winNum = winNum;
    }


    public Long getPowerRecoverTime() {
        return powerRecoverTime;
    }

    public void setPowerRecoverTime(Long powerRecoverTime) {
        this.powerRecoverTime = powerRecoverTime;
    }

    public Long getPowerConsumeTime() {
        return powerConsumeTime;
    }

    public void setPowerConsumeTime(Long powerConsumeTime) {
        this.powerConsumeTime = powerConsumeTime;
    }

    public Integer getThreeStarNum() {
        return threeStarNum;
    }

    public void setThreeStarNum(Integer threeStarNum) {
        this.threeStarNum = threeStarNum;
    }

    public Integer getWinNum() {
        return winNum;
    }

    public void setWinNum(Integer winNum) {
        this.winNum = winNum;
    }

    public GamePlayerKey getKey() {
        return key;
    }

    public void setKey(GamePlayerKey key) {
        this.key = key;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getTitle() {
        return title;
    }

    public void setTitle(Integer title) {
        this.title = title;
    }

    public Integer getTieNum() {
        return tieNum;
    }

    public void setTieNum(Integer tieNum) {
        this.tieNum = tieNum;
    }

    public Integer getLoseNum() {
        return loseNum;
    }

    public void setLoseNum(Integer loseNum) {
        this.loseNum = loseNum;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public Integer getRecord() {
        return record;
    }

    public void setRecord(Integer record) {
        this.record = record;
    }

    public void setDecision(Integer win) {
        if(win.equals(Constants.GAME_WIN))
            setWinNum(IntegerUtils.sum(getWinNum(), 1));
        else if(win.equals(Constants.GAME_LOSE))
            setLoseNum(IntegerUtils.sum(getLoseNum(), 1));
        else
            setTieNum(IntegerUtils.sum(getTieNum() + 1));
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    /**
     * for u me go
     * @return
     */
    public Integer getMaxStraight() {
        if (getAttr() == null || getAttr().get("max_straight") == null) {
            return 0;
        } else {
            return Integer.valueOf(getAttr().get("max_straight"));
        }
    }

    /**
     * for u me go
     * @return
     */
    public Integer getCurStraight() {
        if (getAttr() == null || getAttr().get("cur_straight") == null) {
            return 0;
        } else {
            return Integer.valueOf(getAttr().get("cur_straight"));
        }
    }

    public Long getGold(){
        if (getAttr() == null || getAttr().get("gold") == null) {
            return 0L;
        } else {
            return Long.valueOf(getAttr().get("gold"));
        }
    }
}
