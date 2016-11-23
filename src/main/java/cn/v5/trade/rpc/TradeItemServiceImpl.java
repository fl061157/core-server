package cn.v5.trade.rpc;

import cn.v5.trade.bean.*;
import cn.v5.trade.mapper.PlamwinGoodsGearPriceMapper;
import cn.v5.trade.mapper.PlamwinGoodsOrderMapper;
import cn.v5.trade.util.CollectionUtil;
import cn.v5.util.LoggerFactory;
import com.handwin.api.trade.bean.TradeItem;
import com.handwin.api.trade.service.TradeItemService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fangliang on 16/9/6.
 */
public class TradeItemServiceImpl implements TradeItemService {

    @Autowired
    private PlamwinGoodsOrderMapper orderMapper;

    @Autowired
    private PlamwinGoodsGearPriceMapper priceMapper;

    private static Logger LOGGER = LoggerFactory.getLogger(TradeItemServiceImpl.class);

    @Override
    public List<TradeItem> findBuyedTradeItem(String userID, List<String> itemList, int deviceType) {

        DeviceType dT = DeviceType.getDeviceType(deviceType);
        if (dT == null) return null;

        Map<String, Double> priceMap = new HashedMap();
        if (dT == DeviceType.Android) {
            try {
                List<PlamwinGoodsGearPrice> priceList = priceMapper.find(TradePlatform.WeiXin.getPlatform(), FeeType.CNY.getType(), itemList);
                if (CollectionUtils.isNotEmpty(priceList)) {
                    for (PlamwinGoodsGearPrice pgp : priceList) {
                        priceMap.put(pgp.getItemID(), pgp.getPrice());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[Trade] find priceList error userID:{}", userID, e);
            }
        }


        Map<String, PlamwinGoodsOrder> dmo = new HashedMap();
        try {
            List<PlamwinGoodsOrder> orderList = orderMapper.find(userID, itemList, TradeStatus.TradeSucess.getStatus());
            if (CollectionUtils.isNotEmpty(orderList)) {
                for (PlamwinGoodsOrder pgo : orderList) {
                    if (pgo.getDeviceType() == deviceType) {
                        dmo.put(pgo.getItemID(), pgo);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Trade] find orderList error userID:{}", userID, e);
        }

        List<TradeItem> response = new ArrayList<>();


        if (dT == DeviceType.Android) {
            for (String itemID : itemList) {
                TradeItem ti = new TradeItem();
                ti.setIetmID(itemID);
                Double price = priceMap.get(itemID);
                ti.setPrice(price != null ? price : 0);
                PlamwinGoodsOrder pgo = dmo.get(itemID);
                ti.setBuyed(pgo != null ? true : false);
                response.add(ti);
            }

        } else {
            for (String itemID : itemList) {
                TradeItem ti = new TradeItem();
                ti.setIetmID(itemID);
                PlamwinGoodsOrder pgo = dmo.get(itemID);
                ti.setBuyed(pgo != null ? true : false);
                response.add(ti);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[Trade] rpcList :{} ", CollectionUtil.printList(response));
        }

        return response;
    }
}
