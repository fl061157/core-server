package cn.v5.interceptor;

import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.openplatform.entity.AppKeyInfo;
import cn.v5.service.OpManagerService;
import cn.v5.service.UserService;
import cn.v5.util.LoggerFactory;
import cn.v5.util.RequestUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * @author qgan
 * @version 2014年2月20日 上午11:05:26
 *          支持开放平台鉴权和CG鉴权
 */
public class SecInterceptor extends ConfigurableInterceptor {

    private Map<String, User> map = Collections.synchronizedMap(new LRUMap(10 * 1000));


    @Resource
    private UserService userService;

    @Autowired
    private RequestUtils requestUtils;

    @Autowired
    private OpManagerService opManagerService;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    @Qualifier("opManager")
    private PersistenceManager opManager;

    private MappingJackson2JsonView jsonView = new MappingJackson2JsonView();


    private static final Logger log = LoggerFactory.getLogger(SecInterceptor.class);

    @Override
    public boolean internalPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean result;
        if (oauthRequest(request)) {
            result = oauthAuthorize(request, response, handler);
        } else {
            result = cgAuthorize(request, response, handler);
        }
        return result;
    }

    protected boolean cgAuthorize(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        log.debug("token =  {}", clientSession);
        if (StringUtils.isBlank(clientSession)) {
            forbidden(clientSession, request, response);
            return false;
        }
        User loggedUser;

        int appId = RequestUtils.getAppId(request);

        if ("/api/user/message/msg_snap".equalsIgnoreCase(request.getRequestURI())) {
//            log.debug("LRU map");
            loggedUser = map.get(clientSession);
            if (null == loggedUser) {
                loggedUser = userService.authorize(clientSession, appId);
                if (null != loggedUser) {
                    loggedUser.setPublicKey(null);
                    loggedUser.setAvatar(null);
                    map.put(clientSession, loggedUser);
                }
            }
        } else {
            loggedUser = userService.authorize(clientSession, appId);
        }

        if (loggedUser == null) {
            forbidden(clientSession, request, response);
            return false;
        }
        MDC.put("Uid", loggedUser.getId());
        CurrentUser.user(loggedUser);
        PersistenceManager db = getRealManager(appId);
        CurrentUser.setDB(db);
        return true;
    }

    protected boolean oauthAuthorize(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Integer appId = requestUtils.getAppIdFromToken(request);
        log.debug("oauth appId: {}", appId);
        if (appId > SystemConstants.CG_APP_ID_MAX) {
            AppKeyInfo appKeyInfo = opManagerService.getAppKeyInfo(appId);
//            if (appKeyInfo.getStatus().equals(AppKeyInfo.STATUS_TO_AUDIT)) {
//                try {
//                    jsonView.render(ImmutableMap.of("error_code", StatusCode.APP_NOT_AUDIT, "error", "this app needs to be audited"), request, response);
//                } catch (Exception e) {
//                    logger.error(e.toString(), e);
//                }
//            } else
            //检测应用有没有被停用
            if (appKeyInfo.getStatus().equals(AppKeyInfo.STATUS_STOPPED)) {
                forbidden(appId, request, response, "this app has stopped");
                return false;
            } else {
                if (isAppKeySecretAuth(request)) {
                    return true;
                }
                String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
                User loggedUser = userService.authorize(clientSession, appId);
                MDC.put("Uid", loggedUser.getId());
                CurrentUser.user(loggedUser);
                PersistenceManager db = getRealManager(appId);
                CurrentUser.setDB(db);
                return true;
            }
        }

        return false;
//        int appId = requestUtils.getAppId(request);
//        if (isAppKeySecretAuth(request)) {
//            String nonce = request.getHeader("nonce");
//            String signature = request.getHeader("signature");
//            if (StringUtils.isBlank(nonce) || StringUtils.isBlank(signature)) {
//                log.warn("has no nonce or signature for uri:{}", request.getRequestURI());
//                forbidden(String.valueOf(appId), request, response);
//                return false;
//            }
//
//            AppKeyInfo appKeyInfo = opManagerService.getAppKeyInfo(appId);
//            if (null == appKeyInfo) {
//                log.warn("app key:{} has no app secret.", appId);
//                forbidden(String.valueOf(appId), request, response);
//                return false;
//            }
//
//            String expectedSignature = DigestUtils.md5DigestAsHex(String.format("%s%s", appKeyInfo.getAppSecret(), nonce).getBytes());
//
//            if (!signature.equalsIgnoreCase(expectedSignature)) {
//                log.warn("app key:{} has no wrong signature. expected:{},real:{}.", appId, expectedSignature, signature);
//                forbidden(String.valueOf(appId), request, response);
//                return false;
//            }
//            if (SystemConstants.APP_STATUS_STOP.equals(appKeyInfo.getStatus())) {
//                log.warn("app key:{} has stopped", appId);
//                forbidden(String.valueOf(appId), request, response, "app has been stopped");
//                return false;
//            }
//            //携带了client-session 需要查找该session对应的用户信息
//            String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
//            if (StringUtils.isNotBlank(clientSession)) {
//                return cgAuthorize(request, response, handler);
//            }
//        } else {
//            cgAuthorize(request, response, handler);
//        }
//        return true;
    }

    private void forbidden(String token, HttpServletRequest request, HttpServletResponse response) throws Exception {
        forbidden(token, request, response, "unauthorized");
    }

    private void forbidden(String token, HttpServletRequest request, HttpServletResponse response, String reason) throws Exception {
        log.info("token {} is unauthorized", token);
        jsonView.setUpdateContentLength(true);
        jsonView.render(ImmutableMap.of("error_code", StatusCode.UNAUTHORIZED_CODE, "error", reason), request, response);
    }

    private void forbidden(Integer appId, HttpServletRequest request, HttpServletResponse response, String reason) throws Exception {
        log.info("appId {} is not available", appId);
        jsonView.setUpdateContentLength(true);
        jsonView.render(ImmutableMap.of("error_code", StatusCode.APP_STATUS_INVALID, "error", reason), request, response);
    }

    /**
     * 业务controller如果使用异步处理方法
     * 如FileController#    public WebAsyncTask<Map<String, String>> fileUpload(@NotNull final MultipartFile file,
     * final String uploadType) ,不会执行此方法
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    public void internalPostHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        response.setHeader("Cache-Control", "no-cache");
        CurrentUser.clearUser();
    }

    private boolean oauthRequest(HttpServletRequest request) {
//        return requestUtils.getAppId(request) > SystemConstants.CG_APP_ID_MAX;
        log.debug("check if it was oauth request,{}", request.getHeader("Authorization"));
        return !Strings.isNullOrEmpty(request.getHeader("Authorization"));
    }

    private boolean isAppKeySecretAuth(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] authUris = getAppKeySecretAuth();
        return null != authUris && ArrayUtils.contains(authUris, uri);

    }

    private PersistenceManager getRealManager(int appId) {
        return appId > SystemConstants.CG_APP_ID_MAX ? opManager : manager;
    }

}
