package cn.v5.oauth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;

/**
 * Created by fangliang on 16/5/7.
 */
public interface OpenPlatformService {

    public OAuth2AccessToken getAccessToken(String code);

    public Response doRequest(String uri, OAuth2AccessToken token) throws OAuthException;

    public OAuthUser findOAuthUser(OAuth2AccessToken token) throws OAuthException;


}
