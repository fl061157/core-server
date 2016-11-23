package cn.v5.util;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * User: chenxy
 * Date: 13-5-16
 * Time: 上午1:00
 */
public class StringUtil {

    private static Pattern phonePattern = Pattern.compile("^((13[0-9])|(147)|(15[^4,\\D])|(18[0-9]))\\d{8}$");

    private static Pattern digitalPatten = Pattern.compile("[0-9]*");
    private static final String MOBILE_SPLIT = "@";
    private static final Integer CG_APP_ID = 0;

    public static boolean isMobileNO(String mobile) {
        Matcher m = phonePattern.matcher(mobile);
        return m.matches();
    }

    public static String clearMobileNo(String mobile) {
        return mobile.replaceAll("(\\s+|-|\\+86)", "");
    }

    //手机号码前缀0不再去掉
//    public static String fixMobile(String countryCode, String mobile) {
//        return !COUNTRY_PATTERN.matcher(countryCode).matches() && mobile.startsWith("0") ? RM_LEADING_ZERO_PATTERN.matcher(mobile).replaceFirst("") : mobile;
//    }

    public static String combinedMobileKey(String mobileKey, Integer appId) {
        appId = appId == null ? 0 : appId;
        if (mobileKey == null) {
            return null;
        } else if (Objects.equals(CG_APP_ID, appId)) {
            return mobileKey.toLowerCase();
        }
        return mobileKey.toLowerCase() + MOBILE_SPLIT + String.valueOf(appId);
    }

    public static String fixCountryCode(String countryCode) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < countryCode.length(); i++) {
            char c = countryCode.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }

        String code = sb.toString();
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < 4 - code.length(); i++) {
            padding.append("0");
        }
        return padding.append(code).toString();


    }

    private static final Pattern RM_LEADING_ZERO_PATTERN = Pattern.compile("^(0+)");

    public final static Pattern COUNTRY_PATTERN = Pattern.compile("^0*(227|501|82|679|676|39|250|81|241|378)");


    public static boolean isDigital(String value) {
        boolean result = false;
        if (StringUtils.isNotBlank(value)) {
            result = digitalPatten.matcher(value).matches();
        }
        return result;
    }


    public static String hideMsgContent(String content) {
        String result = content;
        if (StringUtils.isBlank(content)) return result;

        try {
            //非贪婪匹配
            String pattern = "\"content\":\".*?\"";

            Pattern msgPattern = Pattern.compile(pattern);

            Matcher matcher = msgPattern.matcher(content);
            if (matcher.find()) {
                result = matcher.replaceAll("\"content\":\"***\"");
            }
        } catch (Exception e) {
            //ignore
        }


        return result;
    }

    /**
     * 版本格式为 X.X1.X2,且X2<999,X1<99
     *
     * @param targetVersion
     * @return
     */
    public static boolean versionNoLessThan(String version, String targetVersion) {
        if (version.contains("-")) {
            version = version.substring(version.indexOf("-") + 1);
        }
        return !(versionValue(version) < versionValue(targetVersion));
    }

    private static int versionValue(String version) {
        int[] versionInt = Stream.of(version.split("\\.")).mapToInt(k -> Integer.valueOf(k) + 1).toArray();
        return (versionInt[0] << 17) + (versionInt[1] << 10) + versionInt[2];
    }

    public static void main(String[] args) {
        System.out.println(clearMobileNo("00861-395-506-8517"));
        System.out.println(isMobileNO("15813933298"));
        System.out.println(isMobileNO("10813933298"));

    }

}
