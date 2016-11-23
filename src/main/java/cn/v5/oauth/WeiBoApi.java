package cn.v5.oauth;

import com.alibaba.fastjson.JSON;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.utils.OAuthEncoder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by fangliang on 16/5/7.
 */
public class WeiBoApi extends DefaultApi20 {

    private static final String AUTHORIZE_URL
            = "https://api.weibo.com/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";


    protected WeiBoApi() {
    }

    private static class InstanceHolder {
        private static final WeiBoApi INSTANCE = new WeiBoApi();
    }

    public static WeiBoApi instance() {
        return WeiBoApi.InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.weibo.com/oauth2/access_token?grant_type=" + OAuthConstants.AUTHORIZATION_CODE;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        // Append scope if present
        if (config.hasScope()) {
            return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()),
                    OAuthEncoder.encode(config.getScope()));
        } else {
            return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
        }
    }



}
