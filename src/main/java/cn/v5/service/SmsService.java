package cn.v5.service;

import cn.v5.cache.CacheService;
import cn.v5.util.Base62;
import cn.v5.util.LocaleUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by sunhao on 15-2-4.
 */
@Service
public class SmsService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private static final Integer TEST_PHONE_AUTH_CODE = 8068;

    public static final String KEY_LIMIT_REQ = "LIMIT_REQ_";
    public static final String VERIFY_CODE_TEMPLATE = "verifycode.msg";

    @Value("${new.send.sms.url}")
    public String newserverUrl;

    @Value("${code.invalid.second}")
    private int invalidSecond;

    @Value("${send.sms.key}")
    private String smsKey;

    @Value("${base.host}")
    private String baseHost;

    @Value("${mobile.send.second}")
    private int sendSecond;

    @Inject
    private HttpService httpService;

    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Value("${test.phone.white.list}")
    private String phoneWhiteListConfig;

    @Value("${send.sms.exclude.auth.url}")
    private String smsExcludeAuthUrl;


    private List<String> phoneWhiteList;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("sms service initialized. sms_server_url=[{}]", newserverUrl);
        if (StringUtils.isNotBlank(phoneWhiteListConfig)) {
            phoneWhiteList = Arrays.asList(phoneWhiteListConfig.trim().split(","));
        }

    }

    public String getAuthCode(String mobile) {
        int checkCode = getRandomCode(mobile);
        String code = String.valueOf(checkCode);
        try {
            Object sysCode = cacheService.get(mobile);
            if (sysCode != null) {
                code = String.valueOf(sysCode);
            }
        } catch (Exception e) {
            log.error(String.format("fails to get authcode from cache.mobile:%s", mobile), e);
        }

        return code;
    }

    public boolean saveAuthCode(String mobile, String authcode) {
        boolean result = true;
        try {
            String key = KEY_LIMIT_REQ + mobile;
            //限制请求频率
            cacheService.setEx(key, sendSecond, authcode);
            cacheService.setEx(mobile, invalidSecond, authcode);
        } catch (Exception e) {
            result = false;
            log.error(String.format("fails to save authcode into cache. mobile:%s", mobile), e);
        }
        return result;
    }

    public boolean sendAuthcodeToSp(String authcode, String mobile, String countrycode, MessageSource messageSource, Locale locale, String type) {
        boolean result = false;
        try {
            long currentTime = System.currentTimeMillis();
            String authKey = DigestUtils.md5Hex(smsKey + mobile + currentTime);
            String randomCode = Base62.encode(Math.abs(UUID.randomUUID().getMostSignificantBits()));
            String verifyUrl = baseHost + "/v/" + randomCode;
            cacheService.setEx(randomCode, invalidSecond, mobile + "," + authcode);

            Locale msgLocale = locale;
            //首先根据用户的国家码决定接受方接受的语言内容
            if (StringUtils.isNotBlank(countrycode)) {
                if ("0086".equals(countrycode) || "86".equals(countrycode) || "+86".equals(countrycode)) {
                    msgLocale = Locale.CHINA;
                } else if ("0066".equals(countrycode) || "66".equals(countrycode) || "+66".equals(countrycode)) {
                    msgLocale = LocaleUtils.parseLocaleString("th");
                } else {
                    msgLocale = Locale.US;
                }
            }

            if (null == msgLocale) {
                msgLocale = Locale.US;
            }

            String msg = messageSource.getMessage(VERIFY_CODE_TEMPLATE, new Object[]{authcode, verifyUrl}, msgLocale);
            Map<String, String> map = new HashMap<String, String>();
            map.put("mobile", mobile);
            map.put("code", authcode);
            map.put("currentTime", currentTime + "");
            map.put("authkey", authKey);
            map.put("countrycode", countrycode);
            if (StringUtils.isNotBlank(type)) {
                map.put("type", type);
            }


            if (msg != null && !msg.isEmpty()) {
                map.put("msg", msg);
            }

            String temp = this.httpService.doPost(newserverUrl, map, "UTF-8");
            log.debug("result: {}", temp);

            if (temp != null) {
                if (log.isDebugEnabled()) {
                    log.debug("send sms ok. code : {}, mobile : {}", authcode, mobile);
                }
                result = true;
            } else {
                log.error("send sms error : mobile : {}", mobile);
            }

        } catch (Exception e) {
            log.error("send sms error: ", e);
        }
        return result;
    }


    protected boolean sendNewSMS(String mobile, String countrycode, MessageSource messageSource, Locale locale, String type) {
        String authCode = getAuthCode(mobile);
        boolean result = sendAuthcodeToSp(authCode, mobile, countrycode, messageSource, locale, type);
        if (result) {
            result = saveAuthCode(mobile, authCode);
        }
        return result;
    }


    /**
     * 直接调用短信发送模块 发送短信
     *
     * @param mobile
     * @param countryCode
     * @param msg
     */
    public void sendSmsExcludeAuth(String mobile, String countryCode, String msg) {
        long currentTime = System.currentTimeMillis();
        String authKey = DigestUtils.md5Hex(smsKey + mobile + currentTime);
        Map<String, String> map = new HashMap<>();
        map.put("mobile", mobile);
        map.put("msg", msg);
        map.put("currentTime", currentTime + "");

        map.put("authkey", authKey);
        map.put("countrycode", countryCode);
        map.put("type", "general");

        boolean isSendOk = false;
        for (int i = 0; i < 3 && !isSendOk; i++) {
            try {
                String temp = this.httpService.doPost(smsExcludeAuthUrl, map, "UTF-8");
                log.debug("result: {}", temp);
                isSendOk = true;
            } catch (Exception e) {
                log.error(String.format("fails to send sm.mobile:%s, countryCode:%s, msg:%s", mobile, countryCode, msg), e);
                isSendOk = false;
            }
        }
    }


    protected int getRandomCode(String mobile) {
        int result = (int) (Math.random() * 9000 + 1000);
        if (null != phoneWhiteList && phoneWhiteList.size() > 0) {
            if (phoneWhiteList.contains(mobile)) {
                result = TEST_PHONE_AUTH_CODE;
            }
        }
        return result;
    }


    /**
     * 兼容andriod的bug，调用注册时发送了短信验证码，又立即调用了发送验证码接口，
     * andriod手机客户端会提示 服务器繁忙
     * 7s之内重复调用 验证码接口，算作成功
     *
     * @param key
     * @return
     */
    public boolean isResend(String key) {
        boolean result = false;
        Long leftTTl = cacheService.ttl(key);
        if (null != leftTTl) {
            log.debug("[auth code ttl] key:{},left ttl:{}", key, leftTTl);
            if (leftTTl >= sendSecond - 7) {
                result = true;
            }
        }
        return result;
    }

}
