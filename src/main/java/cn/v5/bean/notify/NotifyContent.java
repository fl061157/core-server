package cn.v5.bean.notify;

/**
 * Created by piguangtao on 15/12/21.
 * 系统通知的消息体内容
 */
public class NotifyContent {
    private String type;
    private Object info;

    public NotifyContent(String type, Object info) {
        this.type = type;
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotifyContent{");
        sb.append("type='").append(type).append('\'');
        sb.append(", info=").append(info);
        sb.append('}');
        return sb.toString();
    }
}
