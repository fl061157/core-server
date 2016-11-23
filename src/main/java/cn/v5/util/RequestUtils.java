package cn.v5.util;

import cn.v5.bean.oauth2.TokenCasStore;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.web.controller.ServerException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Service
public class RequestUtils implements InitializingBean {

    public static ThreadLocal<String> traceIdTheadLocal = new ThreadLocal<>();

    public static final String CHAT_GAME_V1_PERFIX = "chatgame-1";

    @Autowired
    @Qualifier("tokenStore")
    private TokenCasStore tokenCasStore;


    private Cache<String, String> tokenCache;

    private UrlPathHelper urlPathHelper = new UrlPathHelper() {
        @Override
        public String getLookupPathForRequest(HttpServletRequest request) {
            String key = request.getRequestURI() + "_lookupPath";
            String path = (String) request.getAttribute(key);
            if (path == null) {
                request.setAttribute(key, path = super.getLookupPathForRequest(request));
            }
            return path;
        }
    };

    private PathMatcher pathMatcher = new AntPathMatcher();

    public String getClientIP(HttpServletRequest request) {
        String xForwardedFor;
        xForwardedFor = StringUtils.trimToNull(request.getHeader("$wsra"));
        if (xForwardedFor != null) {
            return xForwardedFor;
        }
        xForwardedFor = StringUtils.trimToNull(request.getHeader("X-Forwarded-For"));
        if (xForwardedFor != null) {
            int spaceIndex = xForwardedFor.indexOf(',');
            if (spaceIndex > 0) {
                return xForwardedFor.substring(0, spaceIndex);
            } else {
                return xForwardedFor;
            }
        }
        xForwardedFor = StringUtils.trimToNull(request.getHeader("X-Real-IP"));
        if (xForwardedFor != null) {
            return xForwardedFor;
        }

        return request.getRemoteAddr();
    }

    public String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        int end = url.indexOf(".");
        if (end == -1)
            return "";
        int start = url.indexOf("//");
        return url.substring(start + 2, end);
    }

    public boolean matchAny(HttpServletRequest request, UrlPathHelper urlPathHelper, PathMatcher pathMatcher, String[] patterns) {
        if (ArrayUtils.isNotEmpty(patterns)) {
            String lookupPath = urlPathHelper.getLookupPathForRequest(request);
            for (String pattern : patterns) {
                if (pathMatcher.match(pattern, lookupPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchAll(HttpServletRequest request, UrlPathHelper urlPathHelper, PathMatcher pathMatcher, String[] patterns) {
        if (ArrayUtils.isNotEmpty(patterns)) {
            String lookupPath = urlPathHelper.getLookupPathForRequest(request);
            for (String pattern : patterns) {
                if (!pathMatcher.match(pattern, lookupPath)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isChatgameV1(HttpServletRequest request) {
        boolean result = false;
        if (null != request) {
            String clientVersion = request.getHeader("client-version");
            if (!StringUtils.isBlank(clientVersion)) {
                result = clientVersion.startsWith(CHAT_GAME_V1_PERFIX);
            }
        }
        return result;
    }


    /**
     * 下载文件时，获取range header
     *
     * @param request
     * @return
     */
    public Long getRange(HttpServletRequest request) {
        Long range = 0l;
        String rangeStr = request.getHeader("range");
        if (!StringUtils.isBlank(rangeStr) && rangeStr.startsWith("bytes=")) {
            int minus = rangeStr.indexOf('-');
            if (minus > -1) {
                rangeStr = rangeStr.substring(6, minus);
            }
            try {
                range = Long.parseLong(rangeStr);
            } catch (NumberFormatException ignored) {
            }
        }

        return range;
    }

    public String getRemoteAddr(HttpServletRequest request) {
        String[] headers = new String[]{"X-Real-IP", "x-forwarded-for"};
        for (String header : headers) {
            String realIP = request.getHeader(header);
            if (realIP != null && realIP.trim().length() > 0) return realIP;
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取客户端版本大版本号
     *
     * @return
     */
    public Integer getCurrentClientVersion(HttpServletRequest request) {
        String clientVersion = request.getHeader(SystemConstants.CLIENT_VERSION);
        if (null != clientVersion) {
            clientVersion = clientVersion.split("-")[1];
            int i = clientVersion.indexOf(".");
            if (i != -1) {
                return Integer.parseInt(clientVersion.substring(0, i));
            }
        }
        return 0;
    }


    /**
     * 获取客户端软件名称（不包括软件版本）
     *
     * @return 软件名称
     */
    public static String getClientSoftName(HttpServletRequest request) {
        String result = null;
        String clientVersion = request.getHeader(SystemConstants.CLIENT_VERSION);
        if (null != clientVersion) {
            result = clientVersion.split("-")[0];
        }
        return result;
    }

    public String getClientVersion(HttpServletRequest request) {
        return null != request ? request.getHeader(SystemConstants.CLIENT_VERSION) : "";
    }


    public UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

    public PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    public String getUA(HttpServletRequest request) {
        return request.getHeader("user-agent");
    }


    public String getRemoteIp(HttpServletRequest request) {
        String remoteIp = request.getHeader("x-forwarded-for");
        if (org.apache.commons.lang3.StringUtils.isBlank(remoteIp)) {
            remoteIp = request.getHeader("remote-host");
            if (null == remoteIp) {
                remoteIp = "";
            }
        }
        return remoteIp;
    }


    /**
     * 2.x添加好友版本1.x版本
     * 后期全部升级到2.x版本后，可以去掉此逻辑
     *
     * @param source
     * @return
     */
    @Deprecated
    public boolean isContactAddByV1(String source) {
        boolean result = false;
        //非游戏添加好友方式
        if (StringUtils.isNotBlank(source) && "1".equalsIgnoreCase(source)) {
            result = true;
        }
        return result;
    }

    public static String getSession(HttpServletRequest request) {
        return request.getHeader(SystemConstants.CLIENT_SESSION);
    }

    public static int getAppId(HttpServletRequest request) {
        int appId = request.getIntHeader("App-ID");
        if (appId < 0) {
            appId = request.getIntHeader("app-id");
        }
        if (appId < 0) {
            appId = request.getIntHeader("App-Key");
        }
        //不存在取默认值
        if (appId < 0) {
            appId = 0;
        }

        return appId;
    }

    public Integer getAppIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split(OAuth2AccessToken.BEARER_TYPE)[1].trim();
        String appId;
        try {
            appId = tokenCache.get(token, () -> tokenCasStore.getAppId(token));
        } catch (ExecutionException e) {
            throw new ServerException(StatusCode.INNER_ERROR, String.format("fail to get AppId from token %s", token));
        }
        return Integer.valueOf(appId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tokenCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).maximumSize(2000).build();
    }
}

