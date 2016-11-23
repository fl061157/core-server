package cn.v5.util;

import cn.v5.cache.CacheService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 15/11/5.
 */
@Service
public class PhoneUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneUtil.class);
    //手机号码 发送次数限制
    private static final String NO_MOBILE_SEND_PREFIEX = "NO_MOBILE_SEND_PREFIEX";

    private static final String PHONE_AUTHCODE_RETRY_PREFIX = "PHONE_AUTHCODE_RETRY_PREFIX_";

    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Value("${phone.authcode.send.no.limit.inseconds}")
    private int sendValidTimeSeconds;

    @Value("${authcode.retry.interval.seconds}")
    private int authCodeIntervalSecondes;

    @Value("${authcode.count.for.peroid}")
    private int authCodeCountForPeriod;

    /**
     * 手机号码在10小时内 只发送3次
     *
     * @param countryCode
     * @param mobile
     * @return
     */
    public boolean canAndIncySendNumberForMobile(String countryCode, String mobile) {
        long sendCount = mobileIncr(countryCode, mobile);
        boolean canSend = sendCount < 4;
        LOGGER.debug("[mobile send count] countryCode:{},mobile:{} count:{}. canSend:{}", countryCode, mobile, sendCount, canSend);
        return canSend;
    }

    public long mobileIncr(String countryCode, String mobile) {
        long result = 0;
        String key = getMobileSendKey(countryCode, mobile);
        try {
            result = cacheService.incBy(key, 1);
            Long ttl = cacheService.ttl(key);
            //没有设置ttl ，则设置ttl
            if (null == ttl || ttl < 0) {
                cacheService.expire(key, sendValidTimeSeconds);
                LOGGER.debug("[mobile] set key ttl. countryCode:{},mobile:{}", countryCode, mobile);
            }
            LOGGER.debug("set moible. key:{}", key);
        } catch (Exception e) {
            LOGGER.error(String.format("set  moible. key:{}", key), e);
        }

        return result;

    }


    /**
     * 检查在一段时间内输入验证码的次数 防止暴力破解
     *
     * @param countryCode
     * @param mobile
     * @return
     */
    public boolean authCodeRetryOverLimit(String countryCode, String mobile) {
        boolean result = false;
        long count = phoneAuthCodeInc(countryCode, mobile);
        if (count > authCodeCountForPeriod) {
            LOGGER.warn("authcode retry count overlimit.countryCode:{},mobile:{},count:{}", countryCode, mobile, count);
            result = true;
        }
        return result;
    }


    protected long phoneAuthCodeInc(String countryCode, String mobile) {
        long result = 0;
        try {
            String key = getMobileAuthCodeRetryKey(countryCode, mobile);
            result = cacheService.incBy(key, 1);
            Long ttl = cacheService.ttl(key);
            //没有设置ttl ，则设置ttl
            if (null == ttl || ttl < 0) {
                cacheService.expire(key, authCodeIntervalSecondes);
                LOGGER.debug("[mobile] set key {} countryCode:{},mobile:{}", key, countryCode, mobile);
            }
//            LOGGER.debug("set moible. key:{}", key);
        } catch (Exception e) {
            LOGGER.error("fails to handle phone authcode retry count  ", e);
        }

        return result;

    }

    public String getMobileSendKey(String countryCode, String mobile) {
        return String.format("%s%s%s", NO_MOBILE_SEND_PREFIEX, countryCode, mobile);
    }

    public String getMobileAuthCodeRetryKey(String countryCode, String mobile) {
        return String.format("%s%s%s", PHONE_AUTHCODE_RETRY_PREFIX, countryCode, mobile);
    }


}
