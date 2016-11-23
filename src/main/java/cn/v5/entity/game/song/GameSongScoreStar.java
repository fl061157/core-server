package cn.v5.entity.game.song;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-18 上午11:37
 */
//@Entity(table = "game_song_score_stars")
public class GameSongScoreStar {
    @Id
    private Integer score;

    @Column
    private Integer stars;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }
}
