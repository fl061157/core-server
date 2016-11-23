package cn.v5.trade.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.trade.TradeException;
import cn.v5.trade.bean.TradePlatform;
import cn.v5.trade.bean.TradeStatus;
import cn.v5.trade.service.PlamwinOrderService;
import cn.v5.trade.service.PlamwinOrderServiceFacrory;
import cn.v5.trade.service.impl.WeiXinOrderService;
import cn.v5.trade.util.CollectionUtil;
import cn.v5.trade.util.RequestUtil;
import cn.v5.trade.util.WeiXinOrderUtil;
import cn.v5.trade.util.XmlUtil;
import cn.v5.util.RequestUtils;
import cn.v5.web.controller.ServerException;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fangliang on 16/9/1.
 */
@Controller
@RequestMapping("/api/trade")
public class TradeController {


    @Autowired
    private PlamwinOrderServiceFacrory serviceFacrory;

    private static Logger LOGGER = LoggerFactory.getLogger(TradeController.class);


    @RequestMapping(value = "/order", method = RequestMethod.POST)
    @ResponseBody
    public Map order(HttpServletRequest request, String itemID, int tradePlatform, int feeType, int itemSize, String tradeInfo) {


        Map<String, String> tInfo = null;
        if (StringUtils.isNotBlank(tradeInfo)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[Trade] tradeInfo:{}", tradeInfo);
            }
            tInfo = JSON.parseObject(tradeInfo, Map.class);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] order query itemID:{}  , tradePlatform:{} , feeType:{} , tradeInfo:{}", itemID, tradePlatform, feeType, CollectionUtil.printMap(tInfo));
        }

        TradePlatform tPlatform = TradePlatform.getTradePlatform(tradePlatform);

        if (tPlatform == null) {
            LOGGER.error("[Trade] tradePlatform:{} no exists ", tradePlatform);
            throw new ServerException(StatusCode.PARAMETER_ERROR, String.format("Trade parameter error ietemID:%s  ", itemID));
        }

        User user = CurrentUser.user();

        PlamwinOrderService plamwinOrderService = serviceFacrory.getPlamwinOrderService(tPlatform);

        int appID = RequestUtils.getAppId(request);

        int deviceType = RequestUtil.getDeviceType(request);

        try {
            Map<String, String> m = plamwinOrderService.order(user.getId(), deviceType, itemID, feeType, itemSize, appID, tInfo);
            Map<String, Object> response = CollectionUtil.copy(m);
            response.put("error_code", StatusCode.SUCCESS); //TODO 一层结构 ???
            return response;
        } catch (TradeException e) {
            LOGGER.error("[Trade] order error itemID:{} , userID:{}", itemID, user.getId(), e);
            throw new ServerException(StatusCode.INNER_ERROR, String.format("Trade error ietemID:%s , userID:%s", itemID, user.getId()));
        } catch (Exception e) {
            LOGGER.error("[Trade] error ietmID:{}", itemID, e);
            throw new ServerException(StatusCode.INNER_ERROR, String.format("Trade parameter error ietemID:%s  ", itemID));
        }
    }

    /**
     * 此接口作为微信通知使用
     *
     * @param request
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.POST})
    public void tradeResultNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> requestMap = XmlUtil.parseXML(request);
            if (requestMap == null) {
                LOGGER.error("[Trade] notify requestMap is empty !");
                return;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[Trade] notify requestMap:{}", CollectionUtil.printMap(requestMap));
            }

            PlamwinOrderService plamwinOrderService = serviceFacrory.getPlamwinOrderService(TradePlatform.WeiXin);

            String retrunCode = requestMap.get(WeiXinOrderService.WeiXinConstants.RETURN_CODE);

            if (StringUtils.isEmpty(retrunCode) || !retrunCode.equals(WeiXinOrderService.WeiXinConstants.RETURN_CODE_SUCCESS)) {
                LOGGER.error("[Trade] error returnCode:{} , returnMsg:{}", retrunCode, requestMap.get(WeiXinOrderService.WeiXinConstants.RETURN_MSG));
                return;
            }

            String resultCode = requestMap.get(WeiXinOrderService.WeiXinConstants.RESULT_CODE);

            if (StringUtils.isNotBlank(resultCode) && resultCode.equals(WeiXinOrderService.WeiXinConstants.RESULT_CODE_SUCCESS)) {
                String tradeNo = requestMap.get(WeiXinOrderService.WeiXinConstants.OUT_TRADE_NO); //TODO 需要验证签名

                requestMap.put(WeiXinOrderService.WeiXinConstants.TRADE_STATE, WeiXinOrderService.WeiXinConstants.TRADE_STATE_SUCESS);

                plamwinOrderService.accomplishOrder(tradeNo, TradeStatus.TradeSucess, requestMap, null);


            } else {
                LOGGER.error("[Trade] notify fail resultCode:{} ", resultCode);
            }
        } catch (Throwable e) {
            LOGGER.error("[Trade] notify error", e);
        } finally {

            Map<String, String> responseMap = new HashedMap() {
                {
                    this.put(WeiXinOrderService.WeiXinConstants.RETURN_CODE, WeiXinOrderService.WeiXinConstants.RESULT_CODE_SUCCESS);
                    this.put("return_msg", "OK");
                }
            };
            String reps = WeiXinOrderUtil.map2Xml(responseMap);
            try {
                response.getWriter().write(reps);
            } catch (IOException e) {
                LOGGER.error("[WeiXin Trade] notify error ", e);
            }

        }
    }


    @RequestMapping(value = "/trade_confirm", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> tradeConfirm(HttpServletRequest request, String tradeNo, int tradePlatform, String tradeInfo) {

        Map<String, String> tInfo = null;
        if (StringUtils.isNotBlank(tradeInfo)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[Trade] tradeInfo:{}", tradeInfo);
            }
            tInfo = JSON.parseObject(tradeInfo, Map.class);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] order confirm tradeNo:{}  , tradePlatform:{} , tradeInfo:{}", tradeNo, tradePlatform, tInfo);
        }

        TradePlatform tPlatform = TradePlatform.getTradePlatform(tradePlatform);
        if (tPlatform == null) {
            throw new ServerException(StatusCode.INNER_ERROR, String.format("Trade parameter tradePlatform not exists :%d  ", tradePlatform));
        }

        User user = CurrentUser.user();

        PlamwinOrderService plamwinOrderService = serviceFacrory.getPlamwinOrderService(tPlatform);

        try {
            Map<String, String> verfiyResponse = plamwinOrderService.verfiyOrder(tradeNo, tInfo);
            TradeStatus tradeStatus = TradeStatus.TradeSucess; // TODO  Result Judge Status
            Map<String, String> ors = plamwinOrderService.accomplishOrder(tradeNo, tradeStatus, verfiyResponse, user);
            Map<String, Object> response = CollectionUtil.copy(ors);
            response.put("error_code", String.valueOf(StatusCode.SUCCESS));
            return response;
        } catch (TradeException e) {

            if (e instanceof TradeException.TradeFailException) {
                throw new ServerException(StatusCode.TRADE_FAIL_ERROR, e.getMessage());
            }

            if (e instanceof TradeException.TradeInterException) {
                throw new ServerException(StatusCode.INNER_ERROR, "服务器内部错误!");
            }

            if (e instanceof TradeException.TradeNotExistsException) {
                throw new ServerException(StatusCode.TRADE_NOT_EXISTS, "交易不存在!");
            }

            throw new ServerException(StatusCode.INNER_ERROR, e.getMessage());
        }
    }
}
