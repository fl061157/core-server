package cn.v5.entity.vo.game;

import java.util.Map;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-19 下午4:58
 */
public class SongStarVo {
    private Integer songId;

    private Map<Integer, Integer> stars;

    public SongStarVo(Integer songId, Map<Integer, Integer> stars) {
        this.songId = songId;
        this.stars = stars;
    }

    public Integer getSongId() {
        return songId;
    }

    public void setSongId(Integer songId) {
        this.songId = songId;
    }

    public Map<Integer, Integer> getStars() {
        return stars;
    }

    public void setStars(Map<Integer, Integer> stars) {
        this.stars = stars;
    }
}
