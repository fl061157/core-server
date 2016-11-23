package cn.v5.entity.vo.game;

import cn.v5.entity.vo.UserVo;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 下午5:07
 */
public class SongBoardVo {
    private String id;
    private String nickname;
    private String avatarUrl;
    private Integer sex;
    private Integer score;

    public SongBoardVo(String uid, String nickname, String avatarUrl, Integer sex, Integer score) {
        this.id = uid;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.sex = sex;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String uid) {
        this.id = uid;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
