package cn.v5.trade.util;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by fangliang on 16/9/2.
 */
public class XmlUtil {

    public static Map<String, String> parseXML(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashedMap();
        InputStream inputStream = request.getInputStream();
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> elementList = root.elements();
            for (Element element : elementList) {
                map.put(element.getName(), element.getText());
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
        return map;
    }


    static char[] cc = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static void main(String[] args) {
        System.out.println(StringUtils.replace("aaa_ddd", "_", ""));

        System.out.println(MD5Util.md5("handwin!@#"));

        Random r = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 43; i++) {
            builder.append(cc[Math.abs(r.nextInt()) % 16]);
        }

        System.out.println(builder.toString());


    }


}
