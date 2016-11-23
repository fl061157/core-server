package cn.v5.packet;

import cn.v5.packet.notify.INotifyInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据包体
 */
public class NotifyData {
    protected String type;
    protected INotifyInfo info;

    @JsonIgnore
    private Map<String, Object> extra = new HashMap<>();

    public NotifyData(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public INotifyInfo getInfo() {
        return info;
    }

    public void setInfo(NotifyInfo info) {
        this.info = info;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}

