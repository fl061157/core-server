package cn.v5.entity.vo;

import cn.v5.entity.User;
import org.springframework.beans.BeanUtils;

/**
 * Created by yangwei on 14-11-12.
 */
public class BoardUserVo {
    protected String id;
    private String mobile;
    protected String nickname;
    protected Integer sex;
    protected String avatar_url;
    protected String account;
    protected Integer score;
    protected Integer totalScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public static BoardUserVo createFromUser(User user) {
        BoardUserVo vo = new BoardUserVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
