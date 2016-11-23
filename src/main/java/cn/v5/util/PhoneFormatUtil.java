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
import java.util.regex.Pattern;

/**
 * Created by piguangtao on 15/9/28.
 */
@Service
public class PhoneFormatUtil implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneFormatUtil.class);
    private static final String FILE_NAME = "phone_format.yaml";
    private Map<String, CountryPhoneFormat> countryPhoneFormatMap = new ConcurrentHashMap<>();

    public Boolean validatePhone(String countryCode, String mobile) {
        Boolean result = true;
        try {
            CountryPhoneFormat phoneFormat = countryPhoneFormatMap.get(countryCode);
            if (null != phoneFormat) {
                result = phoneFormat.match(mobile);
            }
        } catch (Exception e) {
            LOGGER.error("fails to valid phone :{} for country:{}", mobile, countryCode);
        }

        LOGGER.debug("[phone valid] countryCode:{},mobile:{},result:{}", countryCode, mobile, result);
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initCountryPhoneFormat();
    }

    protected void initCountryPhoneFormat() {
        try {
            Map<String, CountryPhoneFormat> phoneFormatMap = new ConcurrentHashMap<>();

            Constructor constructor = new Constructor(PhoneFormatConfig.class);
            TypeDescription configDes = new TypeDescription(PhoneFormatConfig.class);
            configDes.putListPropertyType("config", CountryPhoneFormat.class);
            constructor.addTypeDescription(configDes);
            Yaml yaml = new Yaml(constructor);
            PhoneFormatConfig config = (PhoneFormatConfig) yaml.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME));

            if (null != config) {
                //启动配置
                if ("yes".equalsIgnoreCase(config.getEnable())) {
                    List<CountryPhoneFormat> phoneFormats = config.getConfig();
                    phoneFormats.stream().filter(phoneFormat -> null != phoneFormat && "yes".equalsIgnoreCase(phoneFormat.getEnable())).forEach(phoneFormat -> {
                        String countryStr = phoneFormat.getCountry();
                        String[] countries = countryStr.split(",");
                        for (String country : countries) {
                            phoneFormatMap.put(country, phoneFormat);
                        }
                    });
                }

            }
            countryPhoneFormatMap = phoneFormatMap;
            LOGGER.info("country phone format." + printCountryPhoneFormat());
        } catch (Exception e) {
            LOGGER.error("fails to parse country_auth_type.yaml", e);
        }
    }

    protected String printCountryPhoneFormat() {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<Map.Entry<String, CountryPhoneFormat>> iterator = countryPhoneFormatMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CountryPhoneFormat> entry = iterator.next();
            stringBuilder.append("country:").append(entry.getKey()).append(", format:").append(entry.getValue());
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }


    public static class PhoneFormatConfig {
        private String enable;
        private List<CountryPhoneFormat> config;

        public String getEnable() {
            return enable;
        }

        public void setEnable(String enable) {
            this.enable = enable;
        }

        public List<CountryPhoneFormat> getConfig() {
            return config;
        }

        public void setConfig(List<CountryPhoneFormat> config) {
            this.config = config;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PhoneFormatConfig{");
            sb.append("enable='").append(enable).append('\'');
            sb.append(", config=").append(config);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class CountryPhoneFormat {
        private String country;
        private String reg;
        private String enable;
        private Pattern pattern;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getReg() {
            return reg;
        }

        public void setReg(String reg) {
            this.reg = reg;
            if (StringUtils.isNotBlank(reg)) {
                pattern = Pattern.compile(reg);
            }
        }

        public String getEnable() {
            return enable;
        }

        public void setEnable(String enable) {
            this.enable = enable;
        }


        public boolean match(String phone) {
            boolean result = true;
            if (StringUtils.isNotBlank(phone)) {
                result = pattern.matcher(phone).matches();
            }
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CountryPhoneFormat{");
            sb.append("country='").append(country).append('\'');
            sb.append(", reg='").append(reg).append('\'');
            sb.append(", enable='").append(enable).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }


}
