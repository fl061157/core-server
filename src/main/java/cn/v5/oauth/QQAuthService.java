package cn.v5.oauth;

import cn.v5.code.StatusCode;
import cn.v5.util.LoggerFactory;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by haoWang on 2016/5/26.
 */
public class QQAuthService implements OpenPlatformService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QQAuthService.class);
    private final OAuth20Service oAuth20Service;
    static final String SECRECT_PREFIX = "CGSECRECT_";
    static final String SCOPE = "get_user_info";
    static final String GRANT_TYPE = "authorization_code";

    private String clientKey;

    public QQAuthService(String key, String secret) {
        QQApi qqApi = cn.v5.oauth.QQApi.instance();
        oAuth20Service = new ServiceBuilder().apiKey(key)
                .apiSecret(secret).state(String.format("%s%d", SECRECT_PREFIX, ThreadLocalRandom.current().nextInt(333333, 999999)))
                .connectTimeout(5 * 1000).scope(SCOPE).grantType(GRANT_TYPE)
                .callback(qqApi.callBack).build(qqApi);
        this.clientKey = key;
    }

    @Override
    public OAuth2AccessToken getAccessToken(String code) {
        return oAuth20Service.getAccessToken(code);
    }

    @Override
    public Response doRequest(String uri, OAuth2AccessToken token) throws OAuthException {
        OAuthRequest request = new OAuthRequest(Verb.GET, uri, this.oAuth20Service);
        this.oAuth20Service.signRequest(token, request);
        try {
            Response response = request.send();
            return response;
        } catch (Throwable e) {
            LOGGER.error("[OAuth] doRequest Error uri:{} , token:{}", uri, token.getAccessToken(), e);
            throw new OAuthException(StatusCode.OAUTH_CONNECTION_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_CONNECT, e);
        }
    }

    @Override
    public OAuthUser findOAuthUser(OAuth2AccessToken token) throws OAuthException {
        String openId = QQApi.instance().getOpenId(token);
        String userInfoURL = QQApi.instance().userInfoUrl(openId, token.getAccessToken(), clientKey);
        Response response = doRequest(userInfoURL, token);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[OAuth] findOAuthUser Response.code:{} , Response.body:{}", response.getCode(), response.getBody());
        }
        int code = response.getCode();

        if (code != HttpServletResponse.SC_OK) {
            throw new OAuthException(StatusCode.OAUTH_CONNECTION_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST);
        }

        OAuthUser oAuthUser = QQApi.instance().parse(response.getBody());

        if (oAuthUser != null) {
            oAuthUser.setToken(token.getAccessToken());
            oAuthUser.setCountryCode("0086");
            oAuthUser.setOpenID(openId);
            oAuthUser.setUnionID(openId);
        }
        return oAuthUser;
    }

    public static void main(String[] args) throws Exception {
        OpenPlatformServiceFactory openPlatformServiceFactory = new OpenPlatformServiceFactory();
        openPlatformServiceFactory.afterPropertiesSet();
        OpenPlatformService openPlatformService = openPlatformServiceFactory.findOpenPlatformService(Openplatform.QQ, 1);
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken("1E9BF2F40D9FA22A39B7FB4F67AEC6FF");
        System.out.println(openPlatformService.findOAuthUser(oAuth2AccessToken));
    }
}
