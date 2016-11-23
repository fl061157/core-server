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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fangliang on 16/5/10.
 */
public class FaceBookAuthService implements OpenPlatformService {


    private final OAuth20Service oAuth20Service;

    static final String SECRECT_PREFIX = "CGSECRECT_";

    private static Logger LOGGER = LoggerFactory.getLogger(FaceBookAuthService.class);

    static final int OK_200 = 200;

    public FaceBookAuthService(String key, String secret) {

        FaceBookApi faceBookApi = FaceBookApi.instance();

        oAuth20Service = new ServiceBuilder().apiKey(key)
                .apiSecret(secret).state(String.format("%s%d", SECRECT_PREFIX, ThreadLocalRandom.current().nextInt(333333, 999999)))
                .connectTimeout(5 * 1000)
                .callback(faceBookApi.callBack).build(faceBookApi);
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
        String userInfoURL = FaceBookApi.instance().getUserInfoUrl(token);
        Response response = doRequest(userInfoURL, token);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[OAuth] findOAuthUser Response.code:{} , Response.body:{}", response.getCode(), response.getBody());
        }

        int code = response.getCode();

        if (code != OK_200) {
            throw new OAuthException(StatusCode.OAUTH_CONNECTION_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST);
        }

        OAuthUser oAuthUser = FaceBookApi.instance().parse(response.getBody());

        if (oAuthUser != null) {
            oAuthUser.setToken(token.getAccessToken());
            oAuthUser.setCountryCode("0086");
        }

        return oAuthUser;
    }
}
