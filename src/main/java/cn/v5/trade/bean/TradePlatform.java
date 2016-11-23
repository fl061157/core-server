package cn.v5.trade.bean;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by fangliang on 16/8/31.
 */
public enum TradePlatform {

    WeiXin(0),
    GooglePlay(1),
    AppleStore(2);

    private int platform;

    TradePlatform(int platform) {
        this.platform = platform;
    }

    public int getPlatform() {
        return platform;
    }

    static Map<Integer, TradePlatform> MAP = new HashedMap();

    static {
        for (TradePlatform tp : TradePlatform.values()) {
            MAP.put(tp.getPlatform(), tp);
        }
    }

    public static TradePlatform getTradePlatform(int platform) {
        return MAP.get(platform);
    }

}
