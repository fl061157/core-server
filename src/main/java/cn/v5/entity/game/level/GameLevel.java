package cn.v5.entity.game.level;

import info.archinnov.achilles.annotations.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 上午10:10
 */
@Entity(table = "game_levels",keyspace = "faceshow")
public class GameLevel {
    @EmbeddedId
    private GameLevelKey level;

    @Column
    private Integer experience;

    @Column(name="single_experience")
    private Integer singleExperience;

    @Column(name="two_experience")
    private Integer twoExperience;

    @Column
    private Integer addition;

    public GameLevel() {
    }

    public GameLevelKey getLevel() {
        return level;
    }

    public void setLevel(GameLevelKey level) {
        this.level = level;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getSingleExperience() {
        return singleExperience;
    }

    public void setSingleExperience(Integer singleExperience) {
        this.singleExperience = singleExperience;
    }

    public Integer getAddition() {
        return addition;
    }

    public void setAddition(Integer addition) {
        this.addition = addition;
    }

    public Integer getTwoExperience() {
        return twoExperience;
    }

    public void setTwoExperience(Integer twoExperience) {
        this.twoExperience = twoExperience;
    }

    public static class GameLevelKey {
        @Column(name = "app_id")
        @Order(1)
        private Integer appId;

        @Column
        @Order(2)
        private Integer level;

        public GameLevelKey() {
        }

        public GameLevelKey(Integer appId, Integer level) {
            this.appId = appId;
            this.level = level;
        }

        public Integer getAppId() {
            return appId;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }
}
