package cn.v5.bean.openplatform;

import java.util.Date;

/**
 * Created by haoWang on 2016/6/30.
 */
public class OpenGroupUser {
    private String id;
    private String nickname;

    private Date createTime;

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


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
