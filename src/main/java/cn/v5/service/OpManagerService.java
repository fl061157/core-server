package cn.v5.service;

import cn.v5.code.StatusCode;
import cn.v5.entity.User;
import cn.v5.metric.LogUtil;
import cn.v5.openplatform.entity.AppCgUser;
import cn.v5.openplatform.entity.AppCgUserKey;
import cn.v5.openplatform.entity.AppKeyInfo;
import cn.v5.openplatform.entity.vo.AppInfoVo;
import cn.v5.util.UserUtils;
import cn.v5.web.controller.ServerException;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by piguangtao on 15/7/8.
 */
@Service
public class OpManagerService implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpManagerService.class);
    @Autowired
    @Qualifier("opManager")
    private PersistenceManager opManager;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserService userService;

    @Value("${local.idc.countryCode}")
    private String localCountryCode;

    private Cache<Integer, AppKeyInfo> appInfoCache;
    @Autowired
    private LogUtil logUtil;

    private PreparedStatement preparedStatement;

    public AppKeyInfo getAppKeyInfo(int appKey) {
        AppKeyInfo appKeyInfo = appInfoCache.getIfPresent(appKey);
        if (appKeyInfo == null) {
            appKeyInfo = opManager.find(AppKeyInfo.class, appKey);
            appInfoCache.put(appKey, appKeyInfo);
        }
        return appKeyInfo;
    }

    public void updateAppKeySecret(int appkey, String appsecret, String status, String name, String logo, String webUrl, String desc) {

        AppKeyInfo appKeyInfo = opManager.find(AppKeyInfo.class, appkey);
        if (null == appKeyInfo) {
            //表示新增
            if (StringUtils.isBlank(appsecret)) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, "appsecret should not be emputy.");
            }

            if (StringUtils.isBlank(status)) {
                //默认为待审核
                status = "1";
            }
            appKeyInfo = new AppKeyInfo();
            appKeyInfo.setAppKey(appkey);
            appKeyInfo.setAppSecret(appsecret);
            appKeyInfo.setStatus(status);
            appKeyInfo.setName(name);
            appKeyInfo.setLogo(logo);
            appKeyInfo.setWebUrl(webUrl);
            appKeyInfo.setDesc(desc);
        } else {

            if (StringUtils.isBlank(appsecret) && StringUtils.isBlank(status)) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, "appsecret and status should not be empty.");
            }

            //更新
            if (StringUtils.isNotBlank(appsecret)) {
                appKeyInfo.setAppSecret(appsecret);
            }

            if (StringUtils.isNotBlank(status)) {
                appKeyInfo.setStatus(status);
            }
            if (StringUtils.isNotBlank(name)) {
                appKeyInfo.setName(name);
            }
            if (StringUtils.isNotBlank(logo)) {
                appKeyInfo.setLogo(logo);
            }
            if (StringUtils.isNotBlank(desc)) {
                appKeyInfo.setDesc(desc);
            }
            if (StringUtils.isNotBlank(webUrl)) {
                appKeyInfo.setWebUrl(webUrl);
            }
        }
        opManager.insertOrUpdate(appKeyInfo);
    }

    public void updateAppKeySecret(int appkey, String appsecret, String status) {

        AppKeyInfo appKeyInfo = opManager.find(AppKeyInfo.class, appkey);

        if (StringUtils.isBlank(appsecret) && StringUtils.isBlank(status)) {
            throw new ServerException(StatusCode.PARAMETER_ERROR, "appsecret and status should not be empty.");
        }

        //更新
        if (StringUtils.isNotBlank(appsecret)) {
            appKeyInfo.setAppSecret(appsecret);
        }

        if (StringUtils.isNotBlank(status)) {
            appKeyInfo.setStatus(status);
        }

        opManager.insertOrUpdate(appKeyInfo);
    }

    public String v5userIdFromAppUserId(int appKey, String thirdUserId) {
        AppCgUserKey userKey = new AppCgUserKey();
        userKey.setAppKey(appKey);
        userKey.setAppUserId(thirdUserId);
        AppCgUser cgUser = opManager.find(AppCgUser.class, userKey);
        if (cgUser == null) {
            return null;
        }
        return cgUser.getUserId();
    }

    public User generateUser(HttpServletRequest request, int appkey, String thirdUserId, String nickName, String avatar) {
        AppCgUserKey userKey = new AppCgUserKey();
        userKey.setAppKey(appkey);
        userKey.setAppUserId(thirdUserId);

        AppCgUser cgUser = opManager.find(AppCgUser.class, userKey);
        if (null == cgUser) {
//            String userId = User.createUUID();
            String userId = UserUtils.genInternalUserId(thirdUserId, String.valueOf(appkey));
            cgUser = new AppCgUser();
            cgUser.setKey(userKey);
            cgUser.setUserId(userId);
            opManager.insert(cgUser, OptionsBuilder.ifNotExists());
        }
        //保存session信息
        String sessionId = userUtils.generateNewSessionId();

        //保存用户信息
        User user = opManager.find(User.class, cgUser.getUserId());
        if (null == user) {
            user = new User();
            user.setId(cgUser.getUserId());
            user.setAppId(appkey);
            user.setNickname(nickName);
            user.setAvatar(avatar);
            user.setCountrycode(localCountryCode);
            user.setCreateTime(new Date());
            user.setUserType(0);

            logUtil.logReq(user, request, null, "/api/user/register");
        } else {
            //需要更新昵称
            if (null != nickName) {
                user.setNickname(nickName);
            }
            if (null != avatar) {
                user.setAvatar(avatar);
            }
        }
        user.setSessionId(sessionId);
        opManager.insertOrUpdate(user);

        userService.cleanUserDataByUserId(user.getId(), appkey, false);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(user.getId(), appkey, sessionId);
        //保存session对应的user_Id
        userService.saveUserSession(sessionId, user.getId(), appkey);

        return user;
    }

    public User getIfPresent(HttpServletRequest request, int appkey, String thirdUserId, String nickName, String avatar) {
        String userId = UserUtils.genInternalUserId(thirdUserId, String.valueOf(appkey));
        User user = opManager.find(User.class, userId);
        if (user != null) {
            user.setSessionId(userService.findUserSessionIndexByKey(userId, appkey).getSessionId());
            return user;
        }
        //保存session信息
        String sessionId = userUtils.generateNewSessionId();
        //保存用户信息
        user = new User();
        user.setId(userId);
        user.setAppId(appkey);
        user.setNickname(nickName);
        user.setAvatar(avatar);
        user.setCountrycode(localCountryCode);
        user.setCreateTime(new Date());
        user.setUserType(0);

        logUtil.logReq(user, request);
        user.setSessionId(sessionId);
        opManager.insertOrUpdate(user);

        userService.cleanUserDataByUserId(user.getId(), appkey, false);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(user.getId(), appkey, sessionId);
        //保存session对应的user_Id
        userService.saveUserSession(sessionId, user.getId(), appkey);

        return user;

    }

    public User getNewSession(int appkey, String userId) {
        //保存session信息
        String sessionId = userUtils.generateNewSessionId();

        //保存用户信息
        User user = opManager.find(User.class, userId);
        if (null == user) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "userId does not exist");
        }
        user.setSessionId(sessionId);

        userService.cleanUserDataByUserId(user.getId(), appkey, false);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(user.getId(), appkey, sessionId);
        //保存session对应的user_Id
        userService.saveUserSession(sessionId, user.getId(), appkey);
        return user;
    }

    public void updateUserInfo(User user, String nickName, String avatar) {
        if (StringUtils.isNotEmpty(nickName)) {
            user.setNickname(nickName);
        }
        if (StringUtils.isNotEmpty(avatar)) {
            //remove old avatarUrl
            user.setAvatar_url(null);
            user.setAvatar(avatar);
        }
        opManager.update(user);
    }

    public User getById(String id) {
        User user = opManager.find(User.class, id);
        return user;
    }

    public void updateAppPushTip(int appKey, String type, String locale, String tip) {
//TODO
//        AppPushTipInfoKey key = new AppPushTipInfoKey();
//        key.setAppKey(appKey);
//        key.setType(type);
//        key.setLocale(locale);
//
//        AppPushTipInfo info = new AppPushTipInfo();
//        info.setKey(key);
//        info.setTip(tip);
//
//        opManager.insertOrUpdate(info);
    }

    public List<AppInfoVo> getApps(List<Integer> keys) {
        //more effective than 'in' statement;
        List<ResultSetFuture> futures = keys.stream().map(k ->
                opManager.getNativeSession().executeAsync(preparedStatement.bind(k))
        ).collect(Collectors.toList());
        return futures.stream().map(f -> {
            ResultSet rows = f.getUninterruptibly();
            Row row = rows.one();
            if (row != null) {
                return AppInfoVo.create()
                        .name(row.getString("name")).desc(row.getString("remark"))
                        .key(row.getInt("app_key")).logo(row.getString("logo"))
                        .webUrl(row.getString("web_url"));
            }
            return null;
        }).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        appInfoCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(2000).build();
        preparedStatement = opManager.getNativeSession().prepare(
                "SELECT * FROM app_key_info where app_key = ?");
    }
}
