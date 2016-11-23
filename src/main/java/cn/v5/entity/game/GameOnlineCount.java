package cn.v5.entity.game;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.type.Counter;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-10 下午3:45
 * 游戏在线人数
 */
@Entity(table = "game_online_counter",keyspace = "faceshow")
public class GameOnlineCount {
    @Id
    private Integer id;

    @Column
    private Counter counter;

    @Column(name="player_num")
    private Counter playerNum;

    public GameOnlineCount() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    public Counter getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(Counter playerNum) {
        this.playerNum = playerNum;
    }
}
