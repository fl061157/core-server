package cn.v5;

import cn.v5.entity.User;
import cn.v5.util.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by sunhao on 15-2-3.
 */
public class InitBean implements InitializingBean {
    private static InitBean instance;

    @Value("${cn.cdn.url}")
    private String cnCDNUrl;

    @Value("${us.cdn.url}")
    private String usCDNUrl;

    private static final Logger logger = LoggerFactory.getLogger(InitBean.class);

    private final static String EMPTY_STRING = "";

    private final static String MODULE = "CoreServer";

    @Override
    public void afterPropertiesSet() throws Exception {
        User.init(cnCDNUrl, usCDNUrl);
        InitBean.instance = this;
    }

    public static String getCDNUrl() {
        return instance.cnCDNUrl;
    }

    public static String getUsCDNUrl() {
        return instance.usCDNUrl;
    }
}
