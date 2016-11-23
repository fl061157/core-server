package cn.v5.trade.util;


import cn.v5.util.LoggerFactory;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fangliang on 16/9/1.
 */
public class WeiXinOrderUtil {

    final static char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static Logger LOGGER = LoggerFactory.getLogger(WeiXinOrderUtil.class);

    public static String sign(Map<String, String> request, String key) {
        if (MapUtils.isEmpty(request)) return null;
        List<Map.Entry<String, String>> list = new ArrayList<>(request.entrySet());
        Collections.sort(list, (o1, o2) -> o1.getKey().toString().compareTo(o2.getKey().toString()));
        StringBuilder buf = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = list.iterator();
        Map.Entry<String, String> entry = iterator.next();
        buf.append(entry.getKey() + "=" + entry.getValue());
        while (iterator.hasNext()) {
            entry = iterator.next();
            buf.append("&" + entry.getKey() + "=" + entry.getValue());
        }
        String sign = String.format("%s&key=%s", buf.toString(), key);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SIGN URL PARAMETERS:{}", sign);
        }

        sign = MD5Util.md5(sign).toUpperCase();
        return sign;
    }

    public static String randomString32() {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        int len = DIGITS.length;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            builder.append(DIGITS[(Math.abs(random.nextInt()) % len)]);
        }

        return builder.toString();
    }


    public static String map2Xml(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) return null;

        StringBuilder builder = new StringBuilder();
        builder.append("<xml>\n");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append("  ").append("<").append(entry.getKey()).append(">")
                    .append("<![CDATA[").append(entry.getValue()).append("]]>")
                    .append("</").append(entry.getKey()).append(">").append("\n");
        }
        builder.append("</xml>");
        return builder.toString();
    }


    public static Map<String, String> xml2Map(String xml) throws Exception {

        Map<String, String> map = new HashedMap();

        Document document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();
        for (Element element : elementList) {
            map.put(element.getName(), element.getText());
        }
        return map;
    }

    public static Map<String, String> copy(Map<String, String> map) {
        Map<String, String> result = new HashedMap();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    public static void main(String[] args) throws Exception {

//        Map<String, String> request = new HashedMap();
//        request.put("appid", "wxd678efh567hg6787");
//        request.put("mch_id", "1230000109");
//        request.put("device_info", "013467007045764");
//        request.put("nonce_str", randomString32());
//        request.put("body", "腾讯充值中心-QQ会员充值");
//        request.put("out_trade_no", "20150806125346");
//        request.put("fee_type", "CNY");
//        request.put("total_fee", "888");
//        request.put("spbill_create_ip", "123.12.12.123");
//        request.put("notify_url", "http://www.weixin.qq.com/wxpay/pay.php");
//        request.put("trade_type", "APP");
//
//        String sign = sign(request, "192006250b4c09247ec02edce69f6a2d");
//        request.put("sign", sign);
//        String xml = map2Xml(request);
//
//        Map<String, String> response = xml2Map(xml);
//
//        for (Map.Entry<String, String> entry : response.entrySet()) {
//            System.out.println(entry.getKey() + " = " + entry.getValue());
//        }

        for( int i = 0 ; i < 10 ; i++ ) {
            System.out.println( IDUtil.create32ID() );
        }

//
//        Map<String, String> m = new HashedMap();
//        m.put("itemID", "123456");
//        m.put("tradeNo", "789012");
//        m.put("tradeStatus", String.valueOf(true));
//        m.put("fee", String.valueOf(100));
//        System.out.println(JSON.toJSONString(m));
    }


}
