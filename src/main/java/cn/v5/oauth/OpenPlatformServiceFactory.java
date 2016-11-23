package cn.v5.oauth;

import cn.v5.util.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by fangliang on 16/5/7.
 */

@Service
public class OpenPlatformServiceFactory implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenPlatformServiceFactory.class);

    @Value("${open.service.map}")
    private String openServiceConfig;

    private Map<OpenPlatFormInfo, OpenPlatformService> serviceMap = new HashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {
//        openServiceConfig = "10,0,wx64cfc06c669abca6,eb470839f1d924f7adaad434cd691f09|11,1,1105289083,f4de6f1d790ac4e5d46bbc37682eca39|12,0,wx2080cb38d8b29e04,2aef2418b4822bdd03c266135a080057|13,0,1616926665297539,2eab46ea06b7b9141caefb131b7bab33";
        try {
            if (StringUtils.isNotBlank(openServiceConfig)) {
                Stream.of(openServiceConfig.split("\\|")).forEach(str -> {
                    String[] items = str.split(",");
                    Openplatform openPlatForm = Openplatform.getOpenplatform(Integer.parseInt(items[0]));
                    if (openPlatForm != null) {
                        Integer appId = Integer.parseInt(items[1]);
                        String clientId = items[2];
                        String clientSecret = items[3];
                        OpenPlatFormInfo openPlatFormInfo = new OpenPlatFormInfo(openPlatForm, appId);
                        OpenPlatformService openPlatformService = openPlatForm.createService(clientId, clientSecret);
                        serviceMap.put(openPlatFormInfo, openPlatformService);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(String.format("fail to parse OpenServiceConfig: %s", openServiceConfig), e);
        }
    }


    public OpenPlatformService findOpenPlatformService(Openplatform openPlatform, Integer appId) {
        OpenPlatFormInfo openPlatFormInfo = new OpenPlatFormInfo(openPlatform, appId);
        return serviceMap.get(openPlatFormInfo);
    }


    public class OpenPlatFormInfo {
        private Openplatform openPlatform;
        private Integer appId;

        public OpenPlatFormInfo(Openplatform openPlatform, Integer appId) {
            this.openPlatform = openPlatform;
            this.appId = appId;
        }

        public Openplatform getOpenPlatform() {
            return openPlatform;
        }

        public Integer getAppId() {
            return appId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof OpenPlatFormInfo) {
                OpenPlatFormInfo other = (OpenPlatFormInfo) obj;
                return other.getOpenPlatform() == this.openPlatform && other.getAppId().equals(this.appId);
            }
            return false;
        }

        public int hashCode() {
            return openPlatform.hashCode() * 37 + appId;
        }

    }

}
