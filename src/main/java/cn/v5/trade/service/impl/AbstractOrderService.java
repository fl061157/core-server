package cn.v5.trade.service.impl;

import cn.v5.entity.User;
import cn.v5.service.MessageSourceService;
import cn.v5.trade.service.PlamwinOrderService;
import cn.v5.util.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.handwin.api.sysmsg.bean.SysMessage;
import com.handwin.api.sysmsg.service.SysMessageServiceAsync;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.UUID;

/**
 * Created by fangliang on 16/9/6.
 */
public abstract class AbstractOrderService implements PlamwinOrderService {

    private static Logger LOGGER = LoggerFactory.getLogger(AbstractOrderService.class);

    private final static byte TRADE_MSG_TYPE = 0x71;

    private final static String PUSH_TRADE_SUCCESS_KEY = "trade.notify.success";

    private final static String PUSH_TRADE_FAIL_KEY = "trade.notify.fail";

    private final static String TRADE_NOTIFY_TYPE = "trade_result";

    @Autowired
    @Qualifier(value = "rpcSysMessageServiceAsync")
    private SysMessageServiceAsync sysMessageService;

    @Autowired
    private MessageSourceService messageSourceService;

    @Value("${local.idc.region}")
    private String localRegion;

    @Value("${dudu.user.id}")
    private String duduID;


    public void notifyClient(String tradeNo, User user, Map<String, String> tradeInfo, boolean success) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setMsgType(TRADE_MSG_TYPE);
        sysMessage.setIncreadByOneFromPush(true);
        sysMessage.setIncrOfflineCount(true);
        String traceID = UUID.randomUUID().toString();
        sysMessage.setTraceID(traceID);
        String pushKey = success ? PUSH_TRADE_SUCCESS_KEY : PUSH_TRADE_FAIL_KEY;
        String cmsgID = UUID.randomUUID().toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] notifyClient traceID:{}", traceID);
        }
        sysMessage.setPush(true);
        String pushMsg = messageSourceService.getMessageSource(user.getAppId()).getMessage(pushKey, new Object[]{}, user.getLocale());
        sysMessage.setPushContent(pushMsg);
        sysMessage.setTo(user.getId());
        sysMessage.setToRegion(localRegion);
        sysMessage.setGroup(false);
        sysMessage.setCmsgID(cmsgID);
        sysMessage.setAppID(user.getAppId());
        sysMessage.setEnsureArrive(true);
        sysMessage.setFormRegion(localRegion);
        sysMessage.setFrom(duduID);
        sysMessage.setReplyRead(true);
        sysMessage.setServerReceiveConfirm(true);
        sysMessage.setStore(true);
        sysMessageService.send(sysMessage, buildContent(tradeNo, user, tradeInfo, success), "");
    }


    private String buildContent(String tradeNo, User user, Map<String, String> tradeInfo, boolean success) {

        if (tradeInfo == null) {
            tradeInfo = new HashedMap();
        }
        tradeInfo.put("result", String.valueOf(success));
        tradeInfo.put("tradeNo", tradeNo);
        tradeInfo.put("to", user.getId());
        String info = JSON.toJSONString(tradeInfo);

        Map<String, String> cM = new HashedMap();
        cM.put("type", TRADE_NOTIFY_TYPE);
        cM.put("info", info);


        return JSON.toJSONString(cM);
    }



    protected Map<String, String> accompilshResult(String tradeNo, String itemID, boolean success, double fee) {
        Map<String, String> response = new HashedMap();
        response.put("tradeStatus", String.valueOf(success));
        response.put("itemID", itemID);
        response.put("tradeNo", tradeNo);
        response.put("fee", String.valueOf(fee));
        return response;
    }



}
