package cn.v5.trade.bean;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by fangliang on 16/9/5.
 */
public enum FeeType {

    CNY(1, "CNY"),

    USD(2, "USD"),

    HKD(3, "HKD");

    FeeType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    private int type;

    private String name;

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    static Map<Integer, FeeType> MAP = new HashedMap();

    static {

        for (FeeType feeType : FeeType.values()) {
            MAP.put(feeType.getType(), feeType);

        }

    }

    public static FeeType getFeeType(Integer type) {
        return MAP.get(type);
    }

}
