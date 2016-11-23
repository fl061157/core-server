package cn.v5.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Objects;

/**
 * Created by sunhao on 14-8-12.
 */
@Service
public class MobileUtils {

    @Value("${salt}")
    private String salt;

    @Value("${mobile.encrypted}")
    private String mobileEncrypted;


    /**
     * 去掉前置空格
     *
     * @param mobile
     * @return
     */
    public String extractNumbersFromMobile(String mobile) {
        String result = null;
        if (StringUtils.isBlank(mobile)) {
            return result;
        }
        mobile = removeLeadingCharacter(mobile);
        result = mobile.replaceAll(" ", "");
        return result;
//        int idx = 0;
//        int processed = 0;
//
//        byte[] mobileBytes = mobile.getBytes();
//        for (; processed < mobileBytes.length; processed++) {
//            byte curByte = mobileBytes[processed];
//            if (curByte >= '0' && curByte <= '9') {
//                mobileBytes[idx++] = curByte;
//            }
//        }
//        return new String(mobileBytes, 0, idx);
    }

    public String removeLeadingCharacter(String mobile) {
        Objects.requireNonNull(mobile);
        if (mobile.charAt(0) == '+') {
            mobile = mobile.substring(1);
        }

        //不删除前置0
//        int idx = 0;
//        do {
//            if (idx >= mobile.length() || mobile.charAt(idx) != '0') {
//                break;
//            }
//            idx++;
//        } while (true);
//        if (idx >= mobile.length()) {
//            return "";
//        } else {
//            return mobile.substring(idx);
//        }
        return mobile;
    }

    /**
     * 提取手机号（无视国家码规则，即剔除开头的1-4个字符）
     *
     * @param mobile 经过处理的手机号
     * @return 提取后的mobile {国家码，手机号}
     */
    public String[][] extractMobileWithoutCountryCode(String mobile) {
        if (!StringUtil.isDigital(mobile)) {
            return new String[0][];
        }

        switch (mobile.length()) {
            default:
                return new String[][]{
                        {"", mobile},
                        {completeFullCountryCode(mobile.substring(0, 1)), mobile.substring(1)},
                        {completeFullCountryCode(mobile.substring(0, 2)), mobile.substring(2)},
                        {completeFullCountryCode(mobile.substring(0, 3)), mobile.substring(3)},
                        {completeFullCountryCode(mobile.substring(0, 4)), mobile.substring(4)},
                };
            case 0:
                return new String[0][];
            case 1:
                return new String[][]{
                        {"", mobile},
                };
            case 2:
                return new String[][]{
                        {"", mobile},
                        {completeFullCountryCode(mobile.substring(0, 1)), mobile.substring(1)},
                };
            case 3:
                return new String[][]{
                        {"", mobile},
                        {completeFullCountryCode(mobile.substring(0, 1)), mobile.substring(1)},
                        {completeFullCountryCode(mobile.substring(0, 2)), mobile.substring(2)},
                };
            case 4:
                return new String[][]{
                        {"", mobile},
                        {completeFullCountryCode(mobile.substring(0, 1)), mobile.substring(1)},
                        {completeFullCountryCode(mobile.substring(0, 2)), mobile.substring(2)},
                        {completeFullCountryCode(mobile.substring(0, 3)), mobile.substring(3)},
                };
        }
    }

    private String completeFullCountryCode(String countryCode) {
        switch (countryCode.length()) {
            case 1:
                return "000" + countryCode;
            case 2:
                return "00" + countryCode;
            case 3:
                return "0" + countryCode;
            default:
                return countryCode;
        }
    }

    /**
     * 加密的手机号码全部为小写
     *
     * @param mobile
     * @return
     */
    public String saltHash(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return mobile;
        }
        if ("yes".equalsIgnoreCase(mobileEncrypted)) {
            if (StringUtils.isNotBlank(mobile) && mobile.length() < 32) {
                String str = salt + mobile;
                return DigestUtils.md5DigestAsHex(str.getBytes()).toLowerCase();
            } else {
                return mobile.toLowerCase();
            }
        } else {
            return mobile;
        }
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public static void main(String[] args) {

    }
}
