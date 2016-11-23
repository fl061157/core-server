package cn.v5.service;


import cn.v5.entity.ServerPool;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by hi on 14-3-12.
 */
@Service
public class ConfigService {
    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    public ServerPool  findServerPoolByType(String countryCode) {
        return manager.find(ServerPool.class,countryCode);
    }

}
