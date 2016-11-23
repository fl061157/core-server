package cn.v5.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by piguangtao on 15/9/24.
 */
@Service
public class CountryAuthTypeUtil implements InitializingBean {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CountryAuthTypeUtil.class);

    private static final String AUTH_TYPE_DEFAULT = "text";

    private static final String CONFIG_FILE = "country_auth_type.yaml";

    private Map<String, String> countryAuthTypeMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        loadCountryAuthConfig();
    }

    protected void loadCountryAuthConfig() {
        try {
            Constructor constructor = new Constructor(CountryAuthTypeConfig.class);
            TypeDescription configDes = new TypeDescription(CountryAuthTypeConfig.class);
            configDes.putListPropertyType("config", CountryAuthTypeConfig.CountryAuthType.class);
            constructor.addTypeDescription(configDes);
            Yaml yaml = new Yaml(constructor);
            CountryAuthTypeConfig config = (CountryAuthTypeConfig) yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE));

            if (null != config) {
                List<CountryAuthTypeConfig.CountryAuthType> authTypes = config.getConfig();
                authTypes.stream().forEach(countryAuthType -> {
                    String countryStr = countryAuthType.getCountry();
                    String[] countries = countryStr.split(",");
                    for (String country : countries) {
                        countryAuthTypeMap.put(country, countryAuthType.getType());
                    }
                });
            }
            LOGGER.info("country auth type." + printCountryAuth());
        } catch (Exception e) {
            LOGGER.error("fails to parse country_auth_type.yaml", e);
        }

    }

    public String getAuthType(String countryCode) {
        String result = AUTH_TYPE_DEFAULT;
        if (StringUtils.isNotBlank(countryCode)) {
            String type = countryAuthTypeMap.get(countryCode);
            result = StringUtils.isNotBlank(type) ? type : AUTH_TYPE_DEFAULT;
        }
        return result;
    }

    protected String printCountryAuth() {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<Map.Entry<String, String>> iterator = countryAuthTypeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            stringBuilder.append("country:").append(entry.getKey()).append(", type:").append(entry.getValue());
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }


    public static class CountryAuthTypeConfig {
        private List<CountryAuthType> config;

        public List<CountryAuthType> getConfig() {
            return config;
        }

        public void setConfig(List<CountryAuthType> config) {
            this.config = config;
        }

        public static class CountryAuthType {
            private String country;
            private String type;

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("CountryAuthType{");
                sb.append("country='").append(country).append('\'');
                sb.append(", type='").append(type).append('\'');
                sb.append('}');
                return sb.toString();
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CountryAuthTypeConfig{");
            sb.append("config=").append(config);
            sb.append('}');
            return sb.toString();
        }
    }


}
