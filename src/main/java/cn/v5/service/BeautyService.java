package cn.v5.service;

import cn.v5.entity.BeautyRegionConfig;
import cn.v5.util.LoggerFactory;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 20/10/15.
 */
@Service
public class BeautyService {

    private static final Logger log = LoggerFactory.getLogger(BeautyService.class);

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    public BeautyRegionConfig findBeautyRegion(String countryCode) {

        BeautyRegionConfig config = null;

        try {
            config = manager.find(BeautyRegionConfig.class, countryCode);
        } catch (Exception e) {
            log.error("Find BeautyRegionConfig Fail countrycode:{}", countryCode, e);
        }

        return config;
    }


}
