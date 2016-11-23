package cn.v5.service;

import cn.v5.entity.Configuration;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用元数据保存服务
 */
@Service
public class ConfigurationService {
    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    public Map<String,String> findById(String key) {
        Configuration configuration =  manager.find(Configuration.class, key);
        return configuration != null ? configuration.getAttrs() : new HashMap<String, String>();
    }

    public void create(String key, Map<String,String> attrs) {
        Configuration configuration = new Configuration();
        configuration.setKey(key);
        configuration.setAttrs(attrs);

        manager.insert(configuration);
    }
}
