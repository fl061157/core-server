package cn.v5.entity.vo.game;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-11 上午9:55
 */
public class GameOnlineVo {
    private Integer id;
    private Long count;

    public GameOnlineVo(Integer id, Long count) {
        this.count = count;
        this.id = id;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
