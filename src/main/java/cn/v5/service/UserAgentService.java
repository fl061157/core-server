package cn.v5.service;

import cn.v5.entity.User;
import cn.v5.util.UserAgentUtils;
import org.slf4j.Logger;
import cn.v5.util.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by sunhao on 15-1-8.
 */
@Service
public class UserAgentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAgentService.class);

    @Inject
    private MessageQueueService messageQueueService;

    @Autowired
    private MessageSourceService messageSourceService;

    public void informOfPushSettings(User toUser, String userAgentString) {
        UserAgentUtils.UserAgent userAgent = UserAgentUtils.analyseUserAgent(userAgentString);
        if (userAgent == null) {
            LOGGER.debug("user agent=[{}] is invalid of user=[{}].", userAgentString, toUser);
            return;
        }

        /**
         * 参见：http://192.168.1.181/mediawiki/index.php/%E9%9C%80%E8%A6%81%E5%AF%B9CG%E8%AE%BE%E7%BD%AE%E7%89%B9%E6%9D%83%E6%89%8D%E8%83%BD%E6%8E%A8%E9%80%81%E7%9A%84%E6%89%8B%E6%9C%BA%E8%A7%84%E5%88%99
         * 页面地址：http://www.chatgame.me/push_manual/mi_zh.html
         */

        String pageType = null;
        String showLang = null;

        // language
        try {
            if ("zh".equalsIgnoreCase(userAgent.getLanguage().split("-")[0])) {
                showLang = "zh";
            } else {
                showLang = "en";
            }
        } catch (Exception e) {
            LOGGER.error("user agent language error. use default=[en].", e);
        }

        // device
        try {
            if ("android".equalsIgnoreCase(userAgent.getSystemType())) {
                if ("huawei".equalsIgnoreCase(userAgent.getVendorType())) {
                    if (userAgent.getOsType().toLowerCase().contains("emotionui")) {
                        String version = userAgent.getOsType().split("-")[1];
                        String major = version.split("\\.")[0];
                        if (major.length() > 0 && major.charAt(0) >= '2') {
                            pageType = "hw";
                        }
                    }
                } else if (userAgent.getOsType().toLowerCase().contains("coloros")) {
                    pageType = "n1";
                } else if (userAgent.getOsType().toLowerCase().contains("miui")) {
                    pageType = "mi";
                }
            }
        } catch (Exception e) {
            LOGGER.error("informOfPushSettings occurs an error. user_agent=[" + userAgent + "]", e);
        }

        if (pageType != null && showLang != null) {
            String pushContent = messageSourceService.getMessageSource(toUser.getAppId()).getMessage("push.manual.msg." + pageType, new Object[]{pageType, showLang}, toUser.getLocale());
            messageQueueService.robotSendCustomMsg(toUser, pushContent);
            LOGGER.debug("pageType=[{}], showLang=[{}], userAgentString=[{}], toUser=[{}]", pageType, showLang, userAgentString, toUser);
        } else {
            LOGGER.debug("either pageType=[{}] or showLang=[{}] is null. userAgentString=[{}], toUser=[{}]", pageType, showLang, userAgentString, toUser);
        }
    }
}
