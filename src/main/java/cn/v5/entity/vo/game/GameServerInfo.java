package cn.v5.entity.vo.game;

/**
 * Created by yangwei on 14-9-18.
 */
public class GameServerInfo {
    private final static String defaultPort = "19000";
    private String ip;
    private String port;
    private Integer num;
    private String area;

    public GameServerInfo(String ip, String port, Integer num, String area) {
        this.ip = ip;
        this.port = port;
        this.num = num;
        this.area = area;
    }

    public GameServerInfo(String ip, Integer num, String area) {
        this.ip = ip;
        this.num = num;
        this.area = area;
        this.port = defaultPort;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public Integer getNum() {
        return num;
    }

    public String getArea() {
        return area;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
