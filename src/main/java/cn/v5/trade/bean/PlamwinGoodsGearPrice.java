package cn.v5.trade.bean;

import java.io.Serializable;

/**
 * Created by fangliang on 16/8/31.
 */
public class PlamwinGoodsGearPrice implements Serializable {

    private String itemID;

    private int tradePlatform ; //交易平台

    private int gear; // 档位

    private int feeType; //币种

    private double price;

    private int appID;

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getTradePlatform() {
        return tradePlatform;
    }

    public void setTradePlatform(int tradePlatform) {
        this.tradePlatform = tradePlatform;
    }

    public int getGear() {
        return gear;
    }

    public void setGear(int gear) {
        this.gear = gear;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getFeeType() {
        return feeType;
    }

    public void setFeeType(int feeType) {
        this.feeType = feeType;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }
}
