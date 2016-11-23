package cn.v5.trade.service;

import cn.v5.entity.User;
import cn.v5.trade.TradeException;
import cn.v5.trade.bean.TradeStatus;

import java.util.Map;

/**
 * Created by fangliang on 16/8/31.
 */
public interface PlamwinOrderService {

    /**
     * 生成订单
     *
     * @param userID
     * @param itemID    商品ID
     * @param feeType   币种
     * @param itemSize  商品数量
     * @param tradeInfo 调用第三方接口需要的信息
     * @return
     */
    Map<String, String> order(String userID, int deviceType, String itemID, int feeType, int itemSize, int appID, Map<String, String> tradeInfo) throws TradeException;


    Map<String, String> accomplishOrder(String tradeNo, TradeStatus tradeStatus, Map<String, String> tradeInfo , User user) throws TradeException;


    Map<String, String> verfiyOrder(String tradeNo, Map<String, String> tradeInfo) throws TradeException ;


    void notifyClient(String tradeNo, User user, Map<String, String> tradeInfo, boolean success) throws Exception;


}
