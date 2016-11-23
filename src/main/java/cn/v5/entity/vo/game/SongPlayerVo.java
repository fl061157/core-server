package cn.v5.entity.vo.game;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-24 下午3:38
 */
public class SongPlayerVo {
    private String id;
    private String nickname;
    private String mobile;
    private String avatarUrl;
    private int sex;
    private String countryCode;
    private Integer level;
    private Integer stars;
    private Integer experience;
    private Integer nextLevelExperience;
    private Integer threeStarNum;
    private Integer record;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNextLevelExperience() {
        return nextLevelExperience;
    }

    public void setNextLevelExperience(Integer nextLevelExperience) {
        this.nextLevelExperience = nextLevelExperience;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getThreeStarNum() {
        return threeStarNum;
    }

    public void setThreeStarNum(Integer threeStarNum) {
        this.threeStarNum = threeStarNum;
    }

    public Integer getRecord() {
        return record;
    }

    public void setRecord(Integer record) {
        this.record = record;
    }
}
