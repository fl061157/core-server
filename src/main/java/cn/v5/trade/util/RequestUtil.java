package cn.v5.trade.util;

import cn.v5.trade.bean.DeviceType;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by fangliang on 16/9/5.
 */
public class RequestUtil {


    public static int getDeviceType(HttpServletRequest request) {

        String ua = request.getHeader("user-agent");

        if (StringUtils.isBlank(ua)) {
            return DeviceType.Android.getType();
        }

        if (ua.toLowerCase().contains("ios")) {
            return DeviceType.Ios.getType();
        }

        return DeviceType.Android.getType();

    }


}
