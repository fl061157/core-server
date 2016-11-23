package cn.v5.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sunhao on 15-1-8.
 */
public class UserAgentUtils {
    /**
     * NOTE: 需求见http://192.168.1.181/mediawiki/index.php/User-Agent%E8%A7%84%E5%88%99
     */
    public static class UserAgent {
        private String softVersion;
        private String systemType;
        private String systemVersion;
        private String vendorType;
        private String deviceType;
        private String osType;
        private String language;

        public String getSoftVersion() {
            return softVersion;
        }

        public String getSystemType() {
            return systemType;
        }

        public String getSystemVersion() {
            return systemVersion;
        }

        public String getVendorType() {
            return vendorType;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getOsType() {
            return osType;
        }

        public String getLanguage() {
            return language;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UserAgent{");
            sb.append("softVersion='").append(softVersion).append('\'');
            sb.append(", systemType='").append(systemType).append('\'');
            sb.append(", systemVersion='").append(systemVersion).append('\'');
            sb.append(", vendorType='").append(vendorType).append('\'');
            sb.append(", deviceType='").append(deviceType).append('\'');
            sb.append(", osType='").append(osType).append('\'');
            sb.append(", language='").append(language).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static UserAgent analyseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }
        if (!userAgent.startsWith("CG")) {
            return null;
        }
        String[] dataArr = userAgent.split("_");
        if (dataArr.length >= 7) {
            // android specifed
            UserAgent ua = new UserAgent();
            ua.softVersion = dataArr[0];
            ua.systemType = dataArr[1];
            ua.systemVersion = dataArr[2];
            ua.vendorType = dataArr[3];
            ua.deviceType = dataArr[4];
            ua.osType = dataArr[5];
            ua.language = dataArr[6];
            return ua;
        } else {
            return null;
        }
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static void main(String[] args) {
        String iosUaStr = "CG2.0.31_IOS_8.10_Apple_iPhone7_ios_zh-CN";
        UserAgent iosUa = analyseUserAgent(iosUaStr);
        System.out.println(iosUa);

        String androidUaStr = "CG2.0.44_Android_4.4.2_HUAWEI_HUAWEI D2-2010_EmotionUI-3.0_zh-CN";
        UserAgent androidUa = analyseUserAgent(androidUaStr);
        System.out.println(androidUa);
    }
}
