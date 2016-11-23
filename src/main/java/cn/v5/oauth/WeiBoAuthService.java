package cn.v5.oauth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fangliang on 16/5/7.
 */
public class WeiBoAuthService implements OpenPlatformService {

    //TODO Code
    static final String appID = "wx2080cb38d8b29e04";
    //TODO Code
    static final String appSecrect = "2aef2418b4822bdd03c266135a080057";

    //TODO Public
    static final String SECRECT_PREFIX = "CGSECRECT_";


    static final String CALLBACK = "";

    //TODO Public

    private final String authorizationUrl;

    private final OAuth20Service oAuth20Service;


    public WeiBoAuthService(String key, String secret) {

        oAuth20Service = new ServiceBuilder().apiKey(key)
                .apiSecret(secret).state(String.format("%s%d", SECRECT_PREFIX, ThreadLocalRandom.current().nextInt(333333, 999999)))
                .callback(CALLBACK).build(WeiXinApi.instance());

        this.authorizationUrl = oAuth20Service.getAuthorizationUrl();
    }

    //TODO 抽象出来
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
        } catch (Exception e) {
            throw new OAuthException(0, e.getCause());//TODO
        }
    }

    @Override
    public OAuthUser findOAuthUser(OAuth2AccessToken token) throws OAuthException {
        return null;
    }
}
