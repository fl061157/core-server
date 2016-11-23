package cn.v5.oauth;

import cn.v5.code.StatusCode;
import cn.v5.util.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.utils.OAuthEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fangliang on 16/5/6.
 */
public class WeiXinApi extends DefaultApi20 {


    private static String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s";

    private String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?grant_type=authorization_code&appid=%s&secret=%s";
//    private static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?grant_type=authorization_code&appid=%s&secret=%s";

    private static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    String appID = "wx64cfc06c669abca6";

    String appSecrect = "eb470839f1d924f7adaad434cd691f09";

    public static final String OPEN_ID = "openid";

    public static final String NICKNAME = "nickname";

    public static final String SEX = "sex";

    public static final String PROVINCE = "province";

    public static final String COUNTRY = "country";

    public static final String CITY = "city";

    public static final String HEADIMGURL = "headimgurl";

    public static final String PRIVILEGE = "privilege";

    public static final String UNIONID = "unionid";


    public static final String ERRORCODE = "errcode";

    static final String SECRECT_PREFIX = "CGSECRECT_";

    static final String CALLBACK = "http://www.zhangying.mobi/";

    private static Logger LOGGER = LoggerFactory.getLogger(WeiXinApi.class);


    public WeiXinApi(String appID, String appSecrect) {
        this.appID = appID;
        this.appSecrect = appSecrect;
    }

    public WeiXinApi() {

    }

    private static class InstanceHolder {

        private static final WeiXinApi INSTANCE = new WeiXinApi();
    }

    public static WeiXinApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return String.format(ACCESS_TOKEN_URL, appID, appSecrect);
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), ThreadLocalRandom.current().ints(1, 999999));
    }

    public String getUserInfoUrl(OAuth2AccessToken auth2AccessToken) throws OAuthException {

        String rawResponse = auth2AccessToken.getRawResponse();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[OAuth] RawResponse Is:{}", rawResponse);
        }

        if (StringUtils.isBlank(rawResponse)) {
            throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_RESPONSE_EMPTY);
        }
        try {
            Map<String, Object> map = JSON.parseObject(rawResponse, Map.class);
            String accessToken = auth2AccessToken.getAccessToken();
            Object oOpenID = map.get(OPEN_ID);
            if (oOpenID == null) {
                throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_OPENID);
            }
            return String.format(USER_INFO_URL, accessToken, oOpenID.toString());

        } catch (Exception e) {
            throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_FORMAT);
        }

    }


    public OAuthUser parse(String response) throws OAuthException {

        Map<String, Object> m;
        try {
            m = JSON.parseObject(response);
        } catch (Exception e) {
            throw new OAuthException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE, e.getCause());
        }

        if (m == null) {
            throw new OAuthException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE);
        }

        LOGGER.info("WeiXin oauth response {}", m);

        Object oErrorCode = m.get(ERRORCODE);

        if (oErrorCode != null && Integer.parseInt(oErrorCode.toString()) != 0) {
            LOGGER.error("[OAuth] Request errorcode:{} ", oErrorCode.toString());
            throw new OAuthException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE);
        }

        OAuthUser oAuthUser = new OAuthUser();

        Object oOpenID = m.get(OPEN_ID);
        oAuthUser.setOpenID(oOpenID != null ? oOpenID.toString() : null);
        Object oNickName = m.get(NICKNAME);
        oAuthUser.setNickName(oNickName != null ? oNickName.toString() : null);
        Object oSex = m.get(SEX);
        oAuthUser.setSex(oSex != null && StringUtils.trim(oSex.toString()).equals("1") ? 0 : 1);
        Object oCity = m.get(CITY);
        oAuthUser.setCity(oCity != null ? oCity.toString() : null);
        Object oProvince = m.get(PROVINCE);
        oAuthUser.setProvince(oProvince != null ? oProvince.toString() : null);
        Object oCountry = m.get(COUNTRY);
        oAuthUser.setCountry(oCountry != null ? oCountry.toString() : null);
        Object oHeadImg = m.get(HEADIMGURL);
        oAuthUser.setHeadimgurl(oHeadImg != null ? oHeadImg.toString() : null);
        Object oUnionid = m.get(UNIONID);
        oAuthUser.setUnionID(oUnionid != null ? oUnionid.toString() : null);
        return oAuthUser;

    }


    public void setAppID(String appID) {
        this.appID = appID;
    }

    public void setAppSecrect(String appSecrect) {
        this.appSecrect = appSecrect;
    }
}
