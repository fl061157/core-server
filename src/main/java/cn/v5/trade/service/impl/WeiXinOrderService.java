package cn.v5.trade.service.impl;

import cn.v5.entity.User;
import cn.v5.service.MessageQueueService;
import cn.v5.trade.TradeException;
import cn.v5.trade.bean.*;
import cn.v5.trade.mapper.PlamwinGoodsGearPriceMapper;
import cn.v5.trade.mapper.PlamwinGoodsMapper;
import cn.v5.trade.mapper.PlamwinGoodsOrderMapper;
import cn.v5.trade.service.PlamwinOrderService;
import cn.v5.trade.util.HttpUtil;
import cn.v5.trade.util.IDUtil;
import cn.v5.trade.util.WeiXinOrderUtil;
import cn.v5.util.LoggerFactory;
import okhttp3.*;
import okio.BufferedSink;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Created by fangliang on 16/8/31.
 */

@Service
public class WeiXinOrderService extends AbstractOrderService { //TODO Need AbstractClass


    @Autowired
    private PlamwinGoodsMapper plamwinGoodsMapper;

    @Autowired
    private PlamwinGoodsGearPriceMapper plamwinGoodsGearPriceMapper; //TODO Cache

    @Autowired
    private PlamwinGoodsOrderMapper plamwinGoodsOrderMapper;

    @Value("${weixin.order.url}")
    private String weixinOrderUrl;

    @Value("${weixin.order.query.url}")
    private String weixinOrderQueryUrl;

    @Value("${weixin.callback.url}")
    private String weixinCallbackUrl;

    @Value("${weixin.order.key}")
    private String weixinOrderKey;


    //private static final String WEIXIN_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    //private static final String WEIXIN_QUERY_ORDER_URL = "https://api.mch.weixin.qq.com/pay/orderquery";

    //private static final String NOTIFY_URL = "http://test.v5.cn/api/trade/notify"; //TODO Config

    //private String key = "d788198cc23f4999b773735eb96bf205"; //TODO Config

    private static Logger LOGGER = LoggerFactory.getLogger(WeiXinOrderService.class);

    @Override
    public Map<String, String> order(String userID, int deviceType, String itemID, int feeType, int itemSize, int appID, Map<String, String> tradeInfo) throws TradeException {

        Map<String, String> result;
        String tradeNo = IDUtil.create32ID();

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
            pggp = plamwinGoodsGearPriceMapper.get(itemID, TradePlatform.WeiXin.getPlatform(), feeType);
        } catch (Exception e) {
            LOGGER.error("[Trade] Internal error itemID:{}", itemID, e);
            throw new TradeException.TradeInterException(e.getMessage(), e);
        }

        if (pggp == null) {
            LOGGER.error("[Trade] GoodPrice not exists itemID:{}", itemID);
            throw new TradeException.TradeInterException("GoodPrice not exists itemID:" + itemID, null);
        }

        TradeStatus tradeStatus = TradeStatus.BeforeTrade;
        PlamwinGoodsOrder plamwinGoodsOrder = new PlamwinGoodsOrder();
        plamwinGoodsOrder.setAppID(appID);
        plamwinGoodsOrder.setUserID(userID);
        fillGoodsOrder(plamwinGoodsOrder, tradeInfo);
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
            LOGGER.error("[Trade] generate order fail itemID:{} ", itemID, e);
            throw new TradeException.TradeInterException(e.getMessage(), e);
        }

        Map<String, String> requestMap = fillTradeInfo(tradeInfo, plamwinGoodsOrder, plamwinGoods, feeType);

        String postXml = WeiXinOrderUtil.map2Xml(requestMap);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] Order request.xml:{}", postXml);
        }

        String responseXml;
        try {
            responseXml = HttpUtil.doPost(weixinOrderUrl, postXml);
        } catch (Exception e) {
            LOGGER.error("[Trade] order request error tradeNo:{}", plamwinGoodsOrder.getTradeNo(), e);
            throw new TradeException.TradeInterException(String.format("Trade order request weixin error tradeNo:%s ", tradeNo), e);
        }

        if (StringUtils.isBlank(responseXml)) {
            LOGGER.error("[Trade] order request responseXml empty tradeNo:{}", plamwinGoodsOrder.getTradeNo());
            throw new TradeException.TradeInterException(String.format("Trade order request weixin responseXml empty tradeNo:%s ", tradeNo), null);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[Trade] order request responseXml:{} ", responseXml);
            }
        }


        Map<String, String> responseMap;

        try {
            responseMap = WeiXinOrderUtil.xml2Map(responseXml);
        } catch (Exception e) {
            LOGGER.error("[Trade] order request parse responseXml empty tradeNo:{}", plamwinGoodsOrder.getTradeNo());
            throw new TradeException.TradeInterException(String.format("Trade order request parse responseXml empty tradeNo:%s ", tradeNo), e);
        }


        String returnCode = responseMap.get(WeiXinConstants.RETURN_CODE);
        String returnMsg = responseMap.get(WeiXinConstants.RETURN_MSG);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[WeiXin Order] TradeNo:{} , returnCode:{} ", tradeNo, returnCode);
        }

        if (!returnCode.equals(WeiXinConstants.RETURN_CODE_SUCCESS)) {
            LOGGER.error("[Trade] order error returnCode:{} , returnMsg:{}", returnCode, returnMsg);
            throw new TradeException.TradeFailException(String.format("Trade order error returnCode:%s, returnMsg:%s", returnCode, returnMsg));
        }

        String resultCode = responseMap.get(WeiXinConstants.RESULT_CODE);

        if (!resultCode.equals(WeiXinConstants.RESULT_CODE_SUCCESS)) {
            LOGGER.error("[Trade] order error resultCode:{}", resultCode);
            throw new TradeException.TradeFailException(String.format("Trade order error resultCode:%s", returnCode));
        }

        TradeStatus ts = TradeStatus.PrepareTrade;
        result = WeiXinOrderUtil.copy(responseMap);
        result.put(WeiXinConstants.OUT_TRADE_NO, plamwinGoodsOrder.getTradeNo());
        result.put(WeiXinConstants.TRADE_STATUS, String.valueOf(ts.getStatus()));
        result.put(WeiXinConstants.TRADE_EXTEND_FIELD, "Sign=WXPay");
        resignResponseMap(result);


        try {
            plamwinGoodsOrderMapper.update(plamwinGoodsOrder.getTradeNo(), ts.getStatus(), System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.error("[Trade] generate order update tradeStatus fail itemID:{} ", itemID, e);
            throw new TradeException.TradeInterException(String.format("[Trade] generate order update tradeStatus fail itemID:%s, error:%s", itemID, e.getMessage()), e);
        }
        return result;
    }

    private void resignResponseMap(Map<String, String> responseMap) {

        Map<String, String> parameters = new HashedMap();
        parameters.put(StringUtils.replace(WeiXinConstants.APPID, "_", ""), responseMap.get(WeiXinConstants.APPID));
        parameters.put(StringUtils.replace(WeiXinConstants.PARTNERID, "_", ""), responseMap.get(WeiXinConstants.MCHID));
        parameters.put(StringUtils.replace(WeiXinConstants.PREPAY_ID, "_", ""), responseMap.get(WeiXinConstants.PREPAY_ID));
        parameters.put(StringUtils.replace(WeiXinConstants.TRADE_EXTEND_FIELD, "_", ""), responseMap.get(WeiXinConstants.TRADE_EXTEND_FIELD));
        String nonstr = WeiXinOrderUtil.randomString32();
        responseMap.put(WeiXinConstants.NONCE_STR, nonstr);
        parameters.put(StringUtils.replace(WeiXinConstants.NONCE_STR, "_", ""), nonstr);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        responseMap.put(WeiXinConstants.TRADE_TIMESTAMP, timestamp);
        parameters.put(StringUtils.replace(WeiXinConstants.TRADE_TIMESTAMP, "_", ""), timestamp);
        String sign = WeiXinOrderUtil.sign(parameters, weixinOrderKey);
        responseMap.put(WeiXinConstants.SIGN, sign);
    }


    /**
     * @param tradeNo
     * @param tradeStatus
     * @param tradeInfo
     * @return
     * @throws TradeException InterException and so on need execute again util success if also fail we need log it
     */
    @Override
    public Map<String, String> accomplishOrder(String tradeNo, TradeStatus tradeStatus, Map<String, String> tradeInfo, User user) throws TradeException {

        PlamwinGoodsOrder plamwinGoodsOrder;
        try {
            plamwinGoodsOrder = plamwinGoodsOrderMapper.get(tradeNo);
        } catch (Exception e) {
            LOGGER.error("[Trade] query order database error tradeNo:{}", tradeNo, e);
            throw new TradeException.TradeInterException(String.format("query order database error tradeNo:%s", tradeNo), e);
        }

        if (plamwinGoodsOrder == null) {
            LOGGER.error("[Trade] trade not exists tradeNo:{}", tradeNo);
            throw new TradeException.TradeNotExistsException(String.format("trade not exists tradeNo:%s", tradeNo));
        }

        TradeStatus ts = TradeStatus.getTradeStatus(plamwinGoodsOrder.getTradeStatus());

        if (ts == TradeStatus.TradeSucess) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[Trade] trade already accomplished and success tradeNo:{}", tradeNo);
            }
            if (user != null) {
                this.notifyClient(tradeNo, user, tradeInfo, true);
            }
            return accompilshResult(tradeNo, plamwinGoodsOrder.getItemID(), true, 100 * plamwinGoodsOrder.getItemSize() * plamwinGoodsOrder.getItemPrice());
        }

        String reurnCode = tradeInfo.get(WeiXinConstants.RETURN_CODE);
        String resultCode = null;
        boolean success = false;

        if (StringUtils.isNotBlank(reurnCode) && reurnCode.equals(WeiXinConstants.RETURN_CODE_SUCCESS)) {
            resultCode = tradeInfo.get(WeiXinConstants.RESULT_CODE);
            String tradeState = tradeInfo.get(WeiXinConstants.TRADE_STATE);
            if (StringUtils.isNotBlank(resultCode) && resultCode.equals(WeiXinConstants.RESULT_CODE_SUCCESS) &&
                    StringUtils.isNotBlank(tradeState) && tradeState.toUpperCase().equals(WeiXinConstants.TRADE_STATE_SUCESS)) {
                success = true;
            }
        }

        if (!success) {
            LOGGER.error("[Trade] trade accomplish fail for weixin response error returnCode:{} ,  returnMsg:{} , resultCode:{}", reurnCode, tradeInfo.get(WeiXinConstants.RETURN_CODE), resultCode);
            try {
                plamwinGoodsOrderMapper.update(tradeNo, TradeStatus.TradeFail.getStatus(), System.currentTimeMillis());
                if (user != null) {
                    this.notifyClient(tradeNo, user, tradeInfo, false);
                }
            } catch (Exception e) {
                LOGGER.error("[Trade] update order status:{} error , tradeNo:{}", TradeStatus.TradeFail.name(), tradeNo);
            }
        } else {
            try {
                plamwinGoodsOrderMapper.update(tradeNo, TradeStatus.TradeSucess.getStatus(), System.currentTimeMillis());
                if (user != null) {
                    this.notifyClient(tradeNo, user, tradeInfo, true);
                }
            } catch (Exception e) {
                LOGGER.error("[Trade] update order status:{} error , tradeNo:{}", TradeStatus.TradeSucess.name(), tradeNo);
            }
        }
        return accompilshResult(tradeNo, plamwinGoodsOrder.getItemID(), success, 100 * plamwinGoodsOrder.getItemSize() * plamwinGoodsOrder.getItemPrice());
    }


//    private Map<String, String> accompilshResult(String tradeNo, String itemID, boolean success, double fee) {
//        Map<String, String> response = new HashedMap();
//        response.put("tradeStatus", String.valueOf(success));
//        response.put("itemID", itemID);
//        response.put("tradeNo", tradeNo);
//        response.put("fee", String.valueOf(fee));
//        return response;
//    }


    @Override
    public void notifyClient(String tradeNo, User user, Map<String, String> tradeInfo, boolean success) {
        //super.notifyClient(tradeNo, user, tradeInfo, success);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] notify tradeNo:{} , user.id:{}", tradeInfo, user.getId());
        }
    }

    /**
     * @param tradeNo
     * @param tradeInfo Map={appid:"wxd678efh567hg6787",mch_id:"1230000109 ","transaction_id":"1009660380201506130728806387"  }
     * @return
     * @throws Exception
     */

    @Override
    public Map<String, String> verfiyOrder(String tradeNo, Map<String, String> tradeInfo) throws TradeException {

        tradeInfo.put(WeiXinConstants.OUT_TRADE_NO, tradeNo);
        String nonceStr = WeiXinOrderUtil.randomString32();
        tradeInfo.put(WeiXinConstants.NONCE_STR, nonceStr);
        String sign = WeiXinOrderUtil.sign(tradeInfo, weixinOrderKey);
        tradeInfo.put(WeiXinConstants.SIGN, sign);
        String postXML = WeiXinOrderUtil.map2Xml(tradeInfo);

        String response;
        try {
            response = HttpUtil.doPost(weixinOrderQueryUrl, postXML);
        } catch (Exception e) {
            LOGGER.error("[Trade] trade weixin httpResponse error tradeNo:{}", tradeNo, e);
            throw new TradeException.TradeInterException(String.format("[Trade] weixin trade internal exception tradeNo:%s", tradeNo), e);
        }

        if (StringUtils.isBlank(response)) {
            LOGGER.error("[Trade] trade weixin httpResponse empty tradeNo:{}", tradeNo);
            throw new TradeException.TradeInterException(String.format("[Trade] weixin httpResponse empty tradeNo:%s", tradeNo), null);
        }

        Map<String, String> responseMap;
        try {
            responseMap = WeiXinOrderUtil.xml2Map(response);
            if (MapUtils.isEmpty(responseMap)) {
                throw new Exception("parse responseMap empty !");
            }
        } catch (Exception e) {
            LOGGER.error("[Trade] trade parse weixin responseXml error response:{}", response, e);
            throw new TradeException.TradeInterException(String.format("[Trade] trade parse responseXml error tradeNo:%s", tradeNo), e);
        }


        String returnCode = responseMap.get(WeiXinConstants.RETURN_CODE);
        String resultCode = null;

        if (StringUtils.isBlank(returnCode) || !returnCode.equals(returnCode) || StringUtils.isBlank((resultCode = responseMap.get(WeiXinConstants.RESULT_CODE)))
                || !resultCode.equals(WeiXinConstants.RESULT_CODE_SUCCESS)) {
            LOGGER.error("[WeiXin Order] query failt tradeNo:{} , returnCode:{} , returnMsg:{} , resultCode:{} ", returnCode, responseMap.get(WeiXinConstants.RETURN_MSG), resultCode);
            throw new TradeException.TradeFailException(String.format("[Trade] query order fail tradeNo:%s , returnCode:%s", tradeNo, returnCode));
        }

        return responseMap;
    }

    private Map<String, String> fillTradeInfo(Map<String, String> tradeInfo, PlamwinGoodsOrder goodsOrder, PlamwinGoods plamwinGoods, int feeType) {
        Map<String, String> request = new HashedMap();
        tradeInfo.entrySet().stream().filter(entry -> StringUtils.isNotBlank(entry.getValue())).forEach(entry ->
                request.put(entry.getKey(), entry.getValue()));
        request.put(WeiXinConstants.OUT_TRADE_NO, goodsOrder.getTradeNo());
        request.put(WeiXinConstants.GOODS_DESC, plamwinGoods.getItemDesc());
        request.put(WeiXinConstants.FEE_TYPE, FeeType.getFeeType(feeType).getName());
        request.put(WeiXinConstants.TOTAL_FEE, String.valueOf(((int) (goodsOrder.getItemPrice() * goodsOrder.getItemSize() * 100))));
        request.put(WeiXinConstants.NOTIFY_URL, weixinCallbackUrl);
        request.put(WeiXinConstants.NONCE_STR, WeiXinOrderUtil.randomString32());
        String sign = WeiXinOrderUtil.sign(request, weixinOrderKey);
        request.put(WeiXinConstants.SIGN, sign);
        return request;
    }


    private static void fillGoodsOrder(PlamwinGoodsOrder plamwinGoodsOrder, Map<String, String> tradeInfo) {

        if (MapUtils.isNotEmpty(tradeInfo)) {
            String oCreateIP = tradeInfo.get(WeiXinConstants.CREATE_IP);
            plamwinGoodsOrder.setCreateIP(oCreateIP != null ? oCreateIP : "");
        }


    }

    public static class WeiXinConstants {

        public static final String CREATE_IP = "spbill_create_ip";

        public static final String NONCE_STR = "nonce_str";

        public static final String OUT_TRADE_NO = "out_trade_no";

        public static final String GOODS_DESC = "body"; //商品描述

        public static final String FEE_TYPE = "fee_type";

        public static final String TOTAL_FEE = "total_fee";

        public static final String NOTIFY_URL = "notify_url";

        public static final String SIGN = "sign";

        public static final String RETURN_CODE = "return_code";

        public static final String RETURN_CODE_SUCCESS = "SUCCESS";

        public static final String RETURN_MSG = "return_msg";

        public static final String PREPAY_ID = "prepay_id";

        public static final String RESULT_CODE = "result_code";

        public static final String RESULT_CODE_SUCCESS = "SUCCESS";

        public static final String TRADE_STATUS = "trade_status";

        public static final String TRADE_EXTEND_FIELD = "package";

        public static final String TRADE_TIMESTAMP = "timestamp";

        public static final String APPID = "appid";

        public static final String PARTNERID = "partnerid";

        public static final String MCHID = "mch_id";

        public static final String TRADE_STATE = "trade_state";

        public static final String TRADE_STATE_SUCESS = "SUCCESS";

    }

}
