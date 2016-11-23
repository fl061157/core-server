package cn.v5.bean.openplatform;

/**
 * Created by piguangtao on 15/7/8.
 */
public class UserVo {
    private String id;
    private String sessionId;

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserVo withId(String id){
        this.id = id;
        return this;
    }

    public UserVo withSessionId(String sessionId){
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserVo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
