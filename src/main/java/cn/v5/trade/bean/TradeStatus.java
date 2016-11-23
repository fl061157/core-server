package cn.v5.trade.bean;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by fangliang on 16/8/31.
 */
public enum TradeStatus {

    BeforeTrade(0),
    PrepareTrade(1),
    TradeSucess(2),
    TradeFail(3);

    private int status;

    TradeStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    static Map<Integer, TradeStatus> MAP = new HashedMap();

    static {

        for (TradeStatus ts : TradeStatus.values()) {
            MAP.put(ts.getStatus(), ts);
        }
    }

    public static TradeStatus getTradeStatus(int status) {
        return MAP.get(status);
    }

}
