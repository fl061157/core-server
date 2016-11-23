package cn.v5.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Created by piguangtao on 15/12/3.
 * 用于获取跨区的coreServer请求url
 */
@Service
public class CoreServerReqUtil implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreServerReqUtil.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${local.countryCode}")
    private String localCountryCode;

    @Value("${local.countryCode.coreServer.map}")
    private String coreServerMapConfig;

    private Map<String, String[]> coreServerMap = new HashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            List<CoreServerReqUtil.CoreServerMap> result = objectMapper.readValue(coreServerMapConfig, new TypeReference<List<CoreServerMap>>() {
            });
            if (null != result) {
                result.stream().forEach(coreServer -> coreServerMap.put(coreServer.getCountryCode(), coreServer.getCoreServer()));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("fails to parse coreServerMap.%s", coreServerMap), e);
        }
    }


    /**
     * 获取国家码对应的coreServer地址,且不是本区的coreServer地址
     *
     * @param countryCode
     * @return
     */
    public String[] getCoreServerUrlWithoutLocalUrl(String countryCode) {
        //如果countryCode 则获取非本区的coreServer地址
        if (StringUtils.isBlank(countryCode)) {
            String[] result = null;
            Iterator<Map.Entry<String, String[]>> entityIterator = coreServerMap.entrySet().iterator();
            while (entityIterator.hasNext()) {
                Map.Entry<String, String[]> entry = entityIterator.next();
                if (localCountryCode.contains(entry.getKey())) {
                    continue;
                }
                result = entry.getValue();
            }
            return result;
        }

        //如果是本区支持的国家码 则不需要请求对方区的信息
        if (localCountryCode.contains(countryCode)) {
            return null;
        }

        if (coreServerMap.containsKey(countryCode)) {
            return coreServerMap.get(countryCode);
        }

        //如果本区不支持缺省值,则获取缺省值
        if (null != coreServerMapConfig &&
                !coreServerMapConfig.contains("-1")) {
            return coreServerMap.get("-1");
        }
        return null;
    }


    public static class CoreServerMap {
        private String countryCode;
        private String[] coreServer;

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String[] getCoreServer() {
            return coreServer;
        }

        public void setCoreServer(String[] coreServer) {
            this.coreServer = coreServer;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CoreServerMap{");
            sb.append("countryCode='").append(countryCode).append('\'');
            sb.append(", coreServer=").append(Arrays.toString(coreServer));
            sb.append('}');
            return sb.toString();
        }
    }
}
