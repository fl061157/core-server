package cn.v5.service;

import cn.v5.entity.thirdapp.CgThirdAppUser;
import cn.v5.entity.thirdapp.CgThirdAppUserKey;
import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.thirdapp.ThirdAppCgUserKey;
import cn.v5.oauth.Openplatform;
import cn.v5.oauth.WeiXinAuthService;
import cn.v5.util.LoggerFactory;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by fangliang on 16/5/9.
 */


@Service
public class BindService {

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    private static Logger LOGGER = LoggerFactory.getLogger(BindService.class);

    public ThirdAppCgUser findBindUser(String thirdUserID, Openplatform openplatform) {
        Object[] parameters = new Object[]{String.valueOf(openplatform.getType()), thirdUserID};
        List<ThirdAppCgUser> list = manager.sliceQuery(ThirdAppCgUser.class).forSelect().withPartitionComponents(parameters).get();
        return (list != null && list.size() > 0) ? list.get(0) : null;
    }

    public void createThirdAppCgUser(String thirdUserID, Openplatform openplatform, String userID, String token) {
        ThirdAppCgUser thirdAppCgUser = new ThirdAppCgUser();
        ThirdAppCgUserKey thirdAppCgUserKey = new ThirdAppCgUserKey();
        thirdAppCgUserKey.setThirdAppUserId(thirdUserID);
        thirdAppCgUserKey.setType(String.valueOf(openplatform.getType()));
        thirdAppCgUserKey.setUserId(userID);
        thirdAppCgUser.setKey(thirdAppCgUserKey);
        thirdAppCgUser.setAccessToken(token);
        manager.insertOrUpdate(thirdAppCgUser);
    }


    public void updayeThirdAppCgUserToken(String thirdUserID, Openplatform openplatform, String userID, String token) {
        ThirdAppCgUserKey thirdAppCgUserKey = new ThirdAppCgUserKey();
        thirdAppCgUserKey.setThirdAppUserId(thirdUserID);
        thirdAppCgUserKey.setType(String.valueOf(openplatform.getType()));
        thirdAppCgUserKey.setUserId(userID);

        try {
            ThirdAppCgUser thirdAppCgUser = manager.forUpdate(ThirdAppCgUser.class, thirdAppCgUserKey);
            thirdAppCgUser.setAccessToken(token);
            manager.update(thirdAppCgUser);
        } catch (Exception e) {
            LOGGER.error("[OAuth] update token error thirdUserID:{} , userID:{} , token:{} ", thirdAppCgUserKey, userID, token, e);
        }
    }

    public void createCgThirdAppUser(String userID, Openplatform openplatform, String thirdUserID) {
        CgThirdAppUser cgThirdAppUser = new CgThirdAppUser();
        CgThirdAppUserKey cgThirdAppUserKey = new CgThirdAppUserKey();
        cgThirdAppUserKey.setType(String.valueOf(openplatform.getType()));
        cgThirdAppUserKey.setUserId(userID);
        cgThirdAppUser.setKey(cgThirdAppUserKey);
        cgThirdAppUser.setThirdAppUserId(thirdUserID);
        manager.insertOrUpdate(cgThirdAppUser);
    }


}
