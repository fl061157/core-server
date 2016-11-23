package cn.v5.trade.service;

import cn.v5.trade.bean.TradePlatform;
import cn.v5.trade.service.impl.AppleStoreOrderService;
import cn.v5.trade.service.impl.GooglePlayOrderService;
import cn.v5.trade.service.impl.WeiXinOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 16/8/31.
 */

@Service
public class PlamwinOrderServiceFacrory {

    @Autowired
    private WeiXinOrderService weiXinOrderService;

    @Autowired
    private GooglePlayOrderService googlePlayOrderService;

    @Autowired
    private AppleStoreOrderService appleStoreOrderService;

    public PlamwinOrderService getPlamwinOrderService(TradePlatform tradePlatform) {
        switch (tradePlatform) {
            case WeiXin:
                return weiXinOrderService;
            case GooglePlay:
                return googlePlayOrderService;
            case AppleStore:
                return appleStoreOrderService;
        }
        return null;
    }

}
