package cn.v5.web.controller;

import cn.v5.cache.CacheService;
import cn.v5.code.StatusCode;
import cn.v5.entity.HealthCheck;
import cn.v5.entity.User;
import cn.v5.validation.Validate;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunhao on 15-1-13.
 */
@Controller
@Validate
@RequestMapping(value = "/api", produces = "application/json")
public class HealthCheckController {
    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;


    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    private static final String REDIS_CHECK_KEY = "HEALTH_CHECK_COMMON_KEY";

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @RequestMapping(value = "/health_check/cassandra", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> cassandraHealthCheck() {
        String key = String.valueOf(System.currentTimeMillis());
        manager.insert(new HealthCheck(key), OptionsBuilder.withTtl(60 * 10));
        manager.find(User.class, "88888888888888888888888888888888");
        return SUCCESS_CODE;
    }

    @RequestMapping(value = "/health_check/redis", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> redisHealthCheck() {
        cacheService.setEx(REDIS_CHECK_KEY, 60, String.valueOf(System.currentTimeMillis()));
        cacheService.get(REDIS_CHECK_KEY);
        return SUCCESS_CODE;
    }
}
