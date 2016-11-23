package cn.v5.trade.service.impl;

import cn.v5.entity.User;
import cn.v5.trade.TradeException;
import cn.v5.trade.bean.*;
import cn.v5.trade.mapper.PlamwinGoodsGearPriceMapper;
import cn.v5.trade.mapper.PlamwinGoodsMapper;
import cn.v5.trade.mapper.PlamwinGoodsOrderMapper;
import cn.v5.trade.service.PlamwinOrderService;
import cn.v5.trade.util.IDUtil;
import cn.v5.util.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fangliang on 16/8/31.
 */

@Service
public class GooglePlayOrderService extends AbstractOrderService {

    @Autowired
    private PlamwinGoodsOrderMapper plamwinGoodsOrderMapper;

    @Autowired
    private PlamwinGoodsMapper plamwinGoodsMapper;

    @Autowired
    private PlamwinGoodsGearPriceMapper plamwinGoodsGearPriceMapper; //TODO Cache

    private static Logger LOGGER = LoggerFactory.getLogger(AppleStoreOrderService.class);

    @Override
    public Map<String, String> order(String userID, int deviceType, String itemID, int feeType, int itemSize, int appID, Map<String, String> tradeInfo) throws TradeException {

        LOGGER.debug("tradeInfo: {}", tradeInfo);

        feeType = FeeType.CNY.getType(); // GooglePlay 直接转 人民币


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] IOS Before 1  create order itemID:{}", itemID);
        }

        PlamwinGoods plamwinGoods;
        try {
            plamwinGoods = plamwinGoodsMapper.get(itemID);
        } catch (Exception e) {
            LOGGER.error("[Trade] Internal error itemID:{}", itemID, e);
            throw new TradeException.TradeInterException(e.getMessage(), e);
        }
        if (plamwinGoods == null) {
            LOGGER.error("[Trade] Goods not exists itemID:{}", itemID);
            throw new TradeException.TradeInterException(String.format("good item:%s not exists", itemID), null);
        }

        PlamwinGoodsGearPrice pggp;
        try {
            pggp = plamwinGoodsGearPriceMapper.get(itemID, TradePlatform.AppleStore.getPlatform(), feeType);
        } catch (Exception e) {
            LOGGER.error("[Trade] Internal error itemID:{}", itemID, e);
            throw new TradeException.TradeInterException(e.getMessage(), e);
        }
        if (pggp == null) {
            LOGGER.error("[Trade] GoodPrice not exists itemID:{}", itemID);
        }

        String tradeNo = IDUtil.create32ID();

        TradeStatus tradeStatus = TradeStatus.PrepareTrade;
        PlamwinGoodsOrder plamwinGoodsOrder = new PlamwinGoodsOrder();
        plamwinGoodsOrder.setAppID(appID);
        plamwinGoodsOrder.setUserID(userID);
        plamwinGoodsOrder.setCreateIP("default");
        plamwinGoodsOrder.setDeviceType(deviceType);
        plamwinGoodsOrder.setFeeType(feeType);
        plamwinGoodsOrder.setItemID(itemID);
        plamwinGoodsOrder.setItemPrice(pggp.getPrice());
        plamwinGoodsOrder.setItemSize(itemSize);
        plamwinGoodsOrder.setTradeNo(tradeNo);
        plamwinGoodsOrder.setTradeStatus(tradeStatus.getStatus());

        try {
            plamwinGoodsOrderMapper.create(plamwinGoodsOrder);
        } catch (Exception e) {
            LOGGER.error("[Trade] Googleplay create order fail tradeNo:{} , itemID:{} , user.id:{} ", tradeNo, itemID, userID);
            throw new TradeException.TradeInterException(e.getMessage(), e);
        }

        Map<String, String> result = new HashMap<>();
        result.put("out_trade_no", plamwinGoodsOrder.getTradeNo());
        result.put("item_id", plamwinGoodsOrder.getItemID());
        result.put("trade_status", String.valueOf(true));

        return result;
    }

    @Override
    public Map<String, String> accomplishOrder(String tradeNo, TradeStatus tradeStatus, Map<String, String> tradeInfo, User user) throws TradeException {

        PlamwinGoodsOrder plamwinGoodsOrder;
        try {
            plamwinGoodsOrder = plamwinGoodsOrderMapper.get(tradeNo);
        } catch (Exception e) {
            LOGGER.error("[Trade] googlePlay query order database error tradeNo:{}", tradeNo, e);
            throw new TradeException.TradeInterException(String.format("query order database error tradeNo:%s", tradeNo), e);
        }

        if (plamwinGoodsOrder == null) {
            LOGGER.error("[Trade] trade  googlePlay not exists tradeNo:{}", tradeNo);
            throw new TradeException.TradeNotExistsException(String.format("trade not exists tradeNo:%s", tradeNo));
        }

        TradeStatus ts = TradeStatus.getTradeStatus(plamwinGoodsOrder.getTradeStatus());

        if (ts == TradeStatus.TradeFail || ts == TradeStatus.TradeSucess) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[Trade] googlePlay trade already accomplished tradeNo:{}", tradeNo);
            }
            if (user != null) {
                this.notifyClient(tradeNo, user, tradeInfo, ts == TradeStatus.TradeFail ? false : true);
            }
            return accompilshResult(tradeNo, plamwinGoodsOrder.getItemID(), ts == TradeStatus.TradeSucess ? true : false, 100 * plamwinGoodsOrder.getItemSize() * plamwinGoodsOrder.getItemPrice());
        }

        try {
            plamwinGoodsOrderMapper.update(tradeNo, TradeStatus.TradeSucess.getStatus(), System.currentTimeMillis());
            if (user != null) {
                this.notifyClient(tradeNo, user, tradeInfo, true);
            }
        } catch (Exception e) {
            LOGGER.error("[Trade] googlePlay update order status:{} error , tradeNo:{}", TradeStatus.TradeSucess.name(), tradeNo);
        }

        return accompilshResult(tradeNo, plamwinGoodsOrder.getItemID(), true, 100 * plamwinGoodsOrder.getItemSize() * plamwinGoodsOrder.getItemPrice());

    }


    @Override
    public void notifyClient(String tradeNo, User user, Map<String, String> tradeInfo, boolean success) {
        //Empty
    }

    @Override
    public Map<String, String> verfiyOrder(String tradeNo, Map<String, String> tradeInfo) throws TradeException {
        return null;
    }

}
