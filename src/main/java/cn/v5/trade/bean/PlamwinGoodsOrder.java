package cn.v5.trade.bean;

import java.io.Serializable;

/**
 * Created by fangliang on 16/8/31.
 */
public class PlamwinGoodsOrder implements Serializable {

    private String tradeNo; //订单号

    private String userID;

    private String itemID;

    private int itemSize;

    private double itemPrice;

    private int tradeStatus;

    private long tradeTimeStart;

    private long tradeTimeExpire;

    private String createIP;

    private int deviceType;

    private int appID;

    private int feeType;

    private int tradePlatform;

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getItemSize() {
        return itemSize;
    }

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(int tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public long getTradeTimeStart() {
        return tradeTimeStart;
    }

    public void setTradeTimeStart(long tradeTimeStart) {
        this.tradeTimeStart = tradeTimeStart;
    }

    public long getTradeTimeExpire() {
        return tradeTimeExpire;
    }

    public void setTradeTimeExpire(long tradeTimeExpire) {
        this.tradeTimeExpire = tradeTimeExpire;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getCreateIP() {
        return createIP;
    }

    public void setCreateIP(String createIP) {
        this.createIP = createIP;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getFeeType() {
        return feeType;
    }

    public void setFeeType(int feeType) {
        this.feeType = feeType;
    }

    public int getTradePlatform() {
        return tradePlatform;
    }

    public void setTradePlatform(int tradePlatform) {
        this.tradePlatform = tradePlatform;
    }
}
