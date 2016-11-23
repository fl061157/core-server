package cn.v5.trade.bean;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by fangliang on 16/9/6.
 */
public enum DeviceType {

    Android(2),
    Ios(1);

    DeviceType(int type) {
        this.type = type;
    }

    private int type;

    public int getType() {
        return type;
    }

    static Map<Integer, DeviceType> MAP = new HashedMap();

    static {

        for (DeviceType dT : DeviceType.values()) {
            MAP.put(dT.getType(), dT);
        }

    }

    public static DeviceType getDeviceType(int type) {
        return MAP.get(type);
    }


}
