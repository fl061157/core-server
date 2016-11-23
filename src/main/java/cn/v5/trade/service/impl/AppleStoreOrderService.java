package cn.v5.trade.service.impl;

import cn.v5.entity.User;
import cn.v5.trade.TradeException;
import cn.v5.trade.bean.*;
import cn.v5.trade.mapper.PlamwinGoodsGearPriceMapper;
import cn.v5.trade.mapper.PlamwinGoodsMapper;
import cn.v5.trade.mapper.PlamwinGoodsOrderMapper;
import cn.v5.trade.util.HttpUtil;
import cn.v5.trade.util.IDUtil;
import cn.v5.util.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fangliang on 16/8/31.
 */
@Service
public class AppleStoreOrderService extends AbstractOrderService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlamwinGoodsOrderMapper plamwinGoodsOrderMapper;

    @Autowired
    private PlamwinGoodsMapper plamwinGoodsMapper;

    @Autowired
    private PlamwinGoodsGearPriceMapper plamwinGoodsGearPriceMapper; //TODO Cache

    private static Logger LOGGER = LoggerFactory.getLogger(AppleStoreOrderService.class);

    @Value("${apple.verify.url}")
    private String appleVerifyUrl;

    @Override
    public Map<String, String> order(String userID, int deviceType, String itemID, int feeType, int itemSize, int appID, Map<String, String> tradeInfo) throws TradeException.TradeInterException {

        LOGGER.debug("tradeInfo: {}", tradeInfo);

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
        fillGoodsOrder(plamwinGoodsOrder, tradeInfo);
        plamwinGoodsOrder.setDeviceType(deviceType);
        plamwinGoodsOrder.setFeeType(feeType);
        plamwinGoodsOrder.setItemID(itemID);
        plamwinGoodsOrder.setItemPrice(pggp.getPrice());
        plamwinGoodsOrder.setItemSize(itemSize);
        plamwinGoodsOrder.setTradeNo(tradeNo);
        plamwinGoodsOrder.setTradeStatus(tradeStatus.getStatus());

        plamwinGoodsOrderMapper.create(plamwinGoodsOrder);

        LOGGER.error("[Trade] success to create order for itemID:{}, tradeNo:{}", itemID, plamwinGoodsOrder.getTradeNo());

        Map<String, String> result = new HashMap<>();
        result.put("trade_no", plamwinGoodsOrder.getTradeNo());
        return result;
    }

    @Override
    public Map<String, String> accomplishOrder(String tradeNo, TradeStatus tradeStatus, Map<String, String> tradeInfo, User user) throws TradeException.TradeInterException, TradeException.TradeFailException, TradeException.TradeNotExistsException {
        // 先查询状态 , 如果已经成功或者失败则不需要继续更新了

        PlamwinGoodsOrder plamwinGoodsOrder;
        try {
            plamwinGoodsOrder = plamwinGoodsOrderMapper.get(tradeNo);
        } catch (Exception e) {
            LOGGER.error("[Trade] trade not exists tradeNo:{}", tradeNo, e);
            throw new TradeException.TradeNotExistsException(String.format("trade not exists tradeNo:%s", tradeNo));
        }

        TradeStatus ts = TradeStatus.getTradeStatus(plamwinGoodsOrder.getTradeStatus());

        if (ts == TradeStatus.TradeFail) {
            LOGGER.error("[Trade] trade already accomplished trade, and status is fail");
            throw new TradeException.TradeFailException("[AppleStore Order] trade already accomplished trade, and status is fail");
        } else if (ts != TradeStatus.TradeSucess) {
            String status = tradeInfo.get("status");
            if (status.equals("0")) {
                plamwinGoodsOrderMapper.update(tradeNo, TradeStatus.TradeSucess.getStatus(), System.currentTimeMillis());
            } else {
                plamwinGoodsOrderMapper.update(tradeNo, TradeStatus.TradeFail.getStatus(), System.currentTimeMillis());
                LOGGER.error("[Trade] Trade order error");
                throw new TradeException.TradeFailException("[AppleStore Order] Trade order error");
            }
        }

        return tradeInfo;
    }

    @Override
    public void notifyClient(String tradeNo, User user, Map<String, String> tradeInfo, boolean success) {
        //TODO
    }

    @Override
    public Map<String, String> verfiyOrder(String tradeNo, Map<String, String> tradeInfo) throws TradeException {
        String receiptData = tradeInfo.get("receipt");

        LOGGER.debug("[Trade] receipt: {}", receiptData);

        Map<String, String> content = new HashMap<>();
        content.put("receipt-data", receiptData);

        try {
            String json = objectMapper.writeValueAsString(content);

            String response = HttpUtil.doPost(appleVerifyUrl, json);

            LOGGER.debug("[Trade] receipt-data check response: {}", response);

            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);

            Integer status = (Integer) responseMap.get("status");

            Map<String, String> result = new HashMap<>();
            result.put("trade_no", tradeNo);
            result.put("status", String.valueOf(status));
            return result;
        } catch (Exception e) {
            LOGGER.error("[Trade] ios trade internal error tradeNo:{}", tradeNo, e);
            throw new TradeException.TradeInterException(String.format("[Trade] ios trade internal error tradeNo:%s", tradeNo), e);
        }
    }

    private static void fillGoodsOrder(PlamwinGoodsOrder plamwinGoodsOrder, Map<String, String> tradeInfo) {

    }

    public static void main(String args[]) throws Exception {
        String postContent = "{\"receipt-data\":\"MIITsgYJKoZIhvcNAQcCoIITozCCE58CAQExCzAJBgUrDgMCGgUAMIIDUwYJKoZIhvcNAQcBoIIDRASCA0AxggM8MAoCAQgCAQEEAhYAMAoCARQCAQEEAgwAMAsCAQECAQEEAwIBADALAgEDAgEBBAMMATAwCwIBCwIBAQQDAgEAMAsCAQ4CAQEEAwIBajALAgEPAgEBBAMCAQAwCwIBEAIBAQQDAgEAMAsCARkCAQEEAwIBAzAMAgEKAgEBBAQWAjQrMA0CAQ0CAQEEBQIDAWDAMA0CARMCAQEEBQwDMS4wMA4CAQkCAQEEBgIEUDI0NzAYAgEEAgECBBCr2EAdDk+RqgYbFGhf0Cg1MBsCAQACAQEEEwwRUHJvZHVjdGlvblNhbmRib3gwHAIBBQIBAQQUONPhip0l1dnW06Gi3Qr6or4iA74wHgIBAgIBAQQWDBRtZS5jaGF0Z2FtZS5tb2JpbGVjZzAeAgEMAgEBBBYWFDIwMTYtMDktMDVUMTI6MjI6MTJaMB4CARICAQEEFhYUMjAxMy0wOC0wMVQwNzowMDowMFowPgIBBwIBAQQ2lYwG2SeGM2lfz3l5hk+xPKppmICGAgzayx4Pm4kOZOZDG0oOyxgZUYsgj0LybRo3ccEXcK9PMEACAQYCAQEEOB5vdlmdWaKXG8Ng0LOW8yxE+i9w0yHmwMnC2hBFLtoz7n7fXcLJk30esbu\\/xUHbNXeF+9O3ANqJMIIBUgIBEQIBAQSCAUgxggFEMAsCAgasAgEBBAIWADALAgIGrQIBAQQCDAAwCwICBrACAQEEAhYAMAsCAgayAgEBBAIMADALAgIGswIBAQQCDAAwCwICBrQCAQEEAgwAMAsCAga1AgEBBAIMADALAgIGtgIBAQQCDAAwDAICBqUCAQEEAwIBATAMAgIGqwIBAQQDAgEAMAwCAgauAgEBBAMCAQAwDAICBq8CAQEEAwIBADAMAgIGsQIBAQQDAgEAMBgCAgamAgEBBA8MDWdhbWVnb29kc18wMDEwGwICBqcCAQEEEgwQMTAwMDAwMDIzMzIxMTkxOTAbAgIGqQIBAQQSDBAxMDAwMDAwMjMzMjExOTE5MB8CAgaoAgEBBBYWFDIwMTYtMDktMDFUMDY6NTY6MjdaMB8CAgaqAgEBBBYWFDIwMTYtMDktMDFUMDY6NTY6MjdaoIIOZTCCBXwwggRkoAMCAQICCA7rV4fnngmNMA0GCSqGSIb3DQEBBQUAMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECgwKQXBwbGUgSW5jLjEsMCoGA1UECwwjQXBwbGUgV29ybGR3aWRlIERldmVsb3BlciBSZWxhdGlvbnMxRDBCBgNVBAMMO0FwcGxlIFdvcmxkd2lkZSBEZXZlbG9wZXIgUmVsYXRpb25zIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MB4XDTE1MTExMzAyMTUwOVoXDTIzMDIwNzIxNDg0N1owgYkxNzA1BgNVBAMMLk1hYyBBcHAgU3RvcmUgYW5kIGlUdW5lcyBTdG9yZSBSZWNlaXB0IFNpZ25pbmcxLDAqBgNVBAsMI0FwcGxlIFdvcmxkd2lkZSBEZXZlbG9wZXIgUmVsYXRpb25zMRMwEQYDVQQKDApBcHBsZSBJbmMuMQswCQYDVQQGEwJVUzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKXPgf0looFb1oftI9ozHI7iI8ClxCbLPcaf7EoNVYb\\/pALXl8o5VG19f7JUGJ3ELFJxjmR7gs6JuknWCOW0iHHPP1tGLsbEHbgDqViiBD4heNXbt9COEo2DTFsqaDeTwvK9HsTSoQxKWFKrEuPt3R+YFZA1LcLMEsqNSIH3WHhUa+iMMTYfSgYMR1TzN5C4spKJfV+khUrhwJzguqS7gpdj9CuTwf0+b8rB9Typj1IawCUKdg7e\\/pn+\\/8Jr9VterHNRSQhWicxDkMyOgQLQoJe2XLGhaWmHkBBoJiY5uB0Qc7AKXcVz0N92O9gt2Yge4+wHz+KO0NP6JlWB7+IDSSMCAwEAAaOCAdcwggHTMD8GCCsGAQUFBwEBBDMwMTAvBggrBgEFBQcwAYYjaHR0cDovL29jc3AuYXBwbGUuY29tL29jc3AwMy13d2RyMDQwHQYDVR0OBBYEFJGknPzEdrefoIr0TfWPNl3tKwSFMAwGA1UdEwEB\\/wQCMAAwHwYDVR0jBBgwFoAUiCcXCam2GGCL7Ou69kdZxVJUo7cwggEeBgNVHSAEggEVMIIBETCCAQ0GCiqGSIb3Y2QFBgEwgf4wgcMGCCsGAQUFBwICMIG2DIGzUmVsaWFuY2Ugb24gdGhpcyBjZXJ0aWZpY2F0ZSBieSBhbnkgcGFydHkgYXNzdW1lcyBhY2NlcHRhbmNlIG9mIHRoZSB0aGVuIGFwcGxpY2FibGUgc3RhbmRhcmQgdGVybXMgYW5kIGNvbmRpdGlvbnMgb2YgdXNlLCBjZXJ0aWZpY2F0ZSBwb2xpY3kgYW5kIGNlcnRpZmljYXRpb24gcHJhY3RpY2Ugc3RhdGVtZW50cy4wNgYIKwYBBQUHAgEWKmh0dHA6Ly93d3cuYXBwbGUuY29tL2NlcnRpZmljYXRlYXV0aG9yaXR5LzAOBgNVHQ8BAf8EBAMCB4AwEAYKKoZIhvdjZAYLAQQCBQAwDQYJKoZIhvcNAQEFBQADggEBAA2mG9MuPeNbKwduQpZs0+iMQzCCX+Bc0Y2+vQ+9GvwlktuMhcOAWd\\/j4tcuBRSsDdu2uP78NS58y60Xa45\\/H+R3ubFnlbQTXqYZhnb4WiCV52OMD3P86O3GH66Z+GVIXKDgKDrAEDctuaAEOR9zucgF\\/fLefxoqKm4rAfygIFzZ630npjP49ZjgvkTbsUxn\\/G4KT8niBqjSl\\/OnjmtRolqEdWXRFgRi48Ff9Qipz2jZkgDJwYyz+I0AZLpYYMB8r491ymm5WyrWHWhumEL1TKc3GZvMOxx6GUPzo22\\/SGAGDDaSK+zeGLUR2i0j0I78oGmcFxuegHs5R0UwYS\\/HE6gwggQiMIIDCqADAgECAggB3rzEOW2gEDANBgkqhkiG9w0BAQUFADBiMQswCQYDVQQGEwJVUzETMBEGA1UEChMKQXBwbGUgSW5jLjEmMCQGA1UECxMdQXBwbGUgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkxFjAUBgNVBAMTDUFwcGxlIFJvb3QgQ0EwHhcNMTMwMjA3MjE0ODQ3WhcNMjMwMjA3MjE0ODQ3WjCBljELMAkGA1UEBhMCVVMxEzARBgNVBAoMCkFwcGxlIEluYy4xLDAqBgNVBAsMI0FwcGxlIFdvcmxkd2lkZSBEZXZlbG9wZXIgUmVsYXRpb25zMUQwQgYDVQQDDDtBcHBsZSBXb3JsZHdpZGUgRGV2ZWxvcGVyIFJlbGF0aW9ucyBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMo4VKbLVqrIJDlI6Yzu7F+4fyaRvDRTes58Y4Bhd2RepQcjtjn+UC0VVlhwLX7EbsFKhT4v8N6EGqFXya97GP9q+hUSSRUIGayq2yoy7ZZjaFIVPYyK7L9rGJXgA6wBfZcFZ84OhZU3au0Jtq5nzVFkn8Zc0bxXbmc1gHY2pIeBbjiP2CsVTnsl2Fq\\/ToPBjdKT1RpxtWCcnTNOVfkSWAyGuBYNweV3RY1QSLorLeSUheHoxJ3GaKWwo\\/xnfnC6AllLd0KRObn1zeFM78A7SIym5SFd\\/Wpqu6cWNWDS5q3zRinJ6MOL6XnAamFnFbLw\\/eVovGJfbs+Z3e8bY\\/6SZasCAwEAAaOBpjCBozAdBgNVHQ4EFgQUiCcXCam2GGCL7Ou69kdZxVJUo7cwDwYDVR0TAQH\\/BAUwAwEB\\/zAfBgNVHSMEGDAWgBQr0GlHlHYJ\\/vRrjS5ApvdHTX8IXjAuBgNVHR8EJzAlMCOgIaAfhh1odHRwOi8vY3JsLmFwcGxlLmNvbS9yb290LmNybDAOBgNVHQ8BAf8EBAMCAYYwEAYKKoZIhvdjZAYCAQQCBQAwDQYJKoZIhvcNAQEFBQADggEBAE\\/P71m+LPWybC+P7hOHMugFNahui33JaQy52Re8dyzUZ+L9mm06WVzfgwG9sq4qYXKxr83DRTCPo4MNzh1HtPGTiqN0m6TDmHKHOz6vRQuSVLkyu5AYU2sKThC22R1QbCGAColOV4xrWzw9pv3e9w0jHQtKJoc\\/upGSTKQZEhltV\\/V6WId7aIrkhoxK6+JJFKql3VUAqa67SzCu4aCxvCmA5gl35b40ogHKf9ziCuY7uLvsumKV8wVjQYLNDzsdTJWk26v5yZXpT+RN5yaZgem8+bQp0gF6ZuEujPYhisX4eOGBrr\\/TkJ2prfOv\\/TgalmcwHFGlXOxxioK0bA8MFR8wggS7MIIDo6ADAgECAgECMA0GCSqGSIb3DQEBBQUAMGIxCzAJBgNVBAYTAlVTMRMwEQYDVQQKEwpBcHBsZSBJbmMuMSYwJAYDVQQLEx1BcHBsZSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEWMBQGA1UEAxMNQXBwbGUgUm9vdCBDQTAeFw0wNjA0MjUyMTQwMzZaFw0zNTAyMDkyMTQwMzZaMGIxCzAJBgNVBAYTAlVTMRMwEQYDVQQKEwpBcHBsZSBJbmMuMSYwJAYDVQQLEx1BcHBsZSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEWMBQGA1UEAxMNQXBwbGUgUm9vdCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOSRqQkfkdseR1DrBe1eeYQt6zaiV0xV7IsZid75S2z1B6siMALoGD74UAnTf0GomPnRymacJGsR0KO75Bsqwx+VnnoMpEeLW9QWNzPLxA9NzhRp0ckZcvVdDtV\\/X5vyJQO6VY9NXQ3xZDUjFUsVWR2zlPf2nJ7PULrBWFBnjwi0IPfLrCwgb3C2PwEwjLdDzw+dPfMrSSgayP7OtbkO2V4c1ss9tTqt9A8OAJILsSEWLnTVPA3bYharo3GSR1NVwa8vQbP4++NwzeajTEV+H0xrUJZBicR0YgsQg0GHM4qBsTBY7FoEMoxos48d3mVz\\/2deZbxJ2HafMxRloXeUyS0CAwEAAaOCAXowggF2MA4GA1UdDwEB\\/wQEAwIBBjAPBgNVHRMBAf8EBTADAQH\\/MB0GA1UdDgQWBBQr0GlHlHYJ\\/vRrjS5ApvdHTX8IXjAfBgNVHSMEGDAWgBQr0GlHlHYJ\\/vRrjS5ApvdHTX8IXjCCAREGA1UdIASCAQgwggEEMIIBAAYJKoZIhvdjZAUBMIHyMCoGCCsGAQUFBwIBFh5odHRwczovL3d3dy5hcHBsZS5jb20vYXBwbGVjYS8wgcMGCCsGAQUFBwICMIG2GoGzUmVsaWFuY2Ugb24gdGhpcyBjZXJ0aWZpY2F0ZSBieSBhbnkgcGFydHkgYXNzdW1lcyBhY2NlcHRhbmNlIG9mIHRoZSB0aGVuIGFwcGxpY2FibGUgc3RhbmRhcmQgdGVybXMgYW5kIGNvbmRpdGlvbnMgb2YgdXNlLCBjZXJ0aWZpY2F0ZSBwb2xpY3kgYW5kIGNlcnRpZmljYXRpb24gcHJhY3RpY2Ugc3RhdGVtZW50cy4wDQYJKoZIhvcNAQEFBQADggEBAFw2mUwteLftjJvc83eb8nbSdzBPwR+Fg4UbmT1HN\\/Kpm0COLNSxkBLYvvRzm+7SZA\\/LeU802KI++Xj\\/a8gH7H05g4tTINM4xLG\\/mk8Ka\\/8r\\/FmnBQl8F0BWER5007eLIztHo9VvJOLr0bdw3w9F4SfK8W147ee1Fxeo3H4iNcol1dkP1mvUoiQjEfehrI9zgWDGG1sJL5Ky+ERI8GA4nhX1PSZnIIozavcNgs\\/e66Mv+VNqW2TAYzN39zoHLFbr2g8hDtq6cxlPtdk2f8GHVdmnmbkyQvvY1XGefqFStxu9k0IkEirHDx22TZxeY8hLgBdQqorV2uT80AkHN7B1dSExggHLMIIBxwIBATCBozCBljELMAkGA1UEBhMCVVMxEzARBgNVBAoMCkFwcGxlIEluYy4xLDAqBgNVBAsMI0FwcGxlIFdvcmxkd2lkZSBEZXZlbG9wZXIgUmVsYXRpb25zMUQwQgYDVQQDDDtBcHBsZSBXb3JsZHdpZGUgRGV2ZWxvcGVyIFJlbGF0aW9ucyBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eQIIDutXh+eeCY0wCQYFKw4DAhoFADANBgkqhkiG9w0BAQEFAASCAQB6AHAWHMheOMw9NE4vlWwS69fyEXEM18tD72GO2BjWvrOqJuVCzb5NW7kvusugh78wrBMqTNfhQemnFHx+GFp3uJio40DZNuXk3PA4LWTei+pUE5cjNBiqjuYgDSyipyKXgw55ywFL9Lii9BN+LW1YgV8Q4EG1l6G4JXcjmyvLCGEc+I99Qt9Y6zcem54AhPtw33z5DGp3HSsm5Hr4\\/h5ax9RyvyENOTMBtSxVFyff0ajdqp2iHoAm5iKV+AwEAppEDcxz99mCmUQ6u1RvicWmJxAr2Cwj0hajFhy5URJAkssKLQe6VbmPMPSLVU4ApfclvRQw26s7Y+gWMdXORhWM\"}";
        String response = HttpUtil.doPost("https://sandbox.itunes.apple.com/verifyReceipt", postContent);
        System.out.println(response);
    }
}

