package cn.v5.oauth;

import cn.v5.code.StatusCode;
import cn.v5.util.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.Verb;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by haoWang on 2016/5/26.
 */
public class QQApi extends DefaultApi20 {
    private static final Logger LOGGER = LoggerFactory.getLogger(QQApi.class);
    private static final String OPEN_ID = "openid";
    private static final String USER_INFO_URL = "https://graph.qq.com/user/get_simple_userinfo?access_token=%s&oauth_consumer_key=%s&openid=%s";
    private static final String ERRORCODE = "ret";
    private static final String HEADIMGURL = "figureurl_qq_1";
    private static final String NICKNAME = "nickname";
    private static final String GENDER = "gender";

    private static final String OPEN_ID_URL = "https://graph.qq.com/oauth2.0/me?access_token=%s";

    final String accessTokenURL = "https://graph.qq.com/oauth2.0/token";
    final String callBack = "http://www.zhangying.mobi/";
//    final String appID = "1105289083";
//    final String appSecrect = "f4de6f1d790ac4e5d46bbc37682eca39";

    protected QQApi() {

    }

    private static class InstanceHolder {

        private static final QQApi INSTANCE = new QQApi();
    }

    public static QQApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return accessTokenURL;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }


    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return null;
//        return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), ThreadLocalRandom.current().ints(1, 999999));
    }

    public String getOpenId(OAuth2AccessToken auth2AccessToken) throws OAuthException {

        String openIDURL = String.format(OPEN_ID_URL, auth2AccessToken.getAccessToken());

        HttpClient httpClient = HttpClients.createDefault();
        HttpRequestBase httpRequest = new HttpGet(openIDURL);
        String rawResponse;
        try {
            HttpResponse response = httpClient.execute(httpRequest);
            if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                LOGGER.error("fail to connect...,statusCode{}", response.getStatusLine().getStatusCode());
                throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_OPENID);
            }
            rawResponse = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_OPENID);
        } finally {
            httpRequest.releaseConnection();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[OAuth] RawResponse Is:{}", rawResponse);
        }

        if (StringUtils.isBlank(rawResponse)) {
            throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_RESPONSE_EMPTY);
        }
        try {
            int startIndex = rawResponse.indexOf("(") + 1;
            int endIndex = rawResponse.indexOf(")");
            Map<String, Object> map = JSON.parseObject(rawResponse.substring(startIndex, endIndex), Map.class);
            Object oOpenID = map.get(OPEN_ID);
            if (oOpenID == null) {
                throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_OPENID);
            }
            return oOpenID.toString();
        } catch (Exception e) {
            throw new OAuthException(StatusCode.OAUTH_ACCESSTOKEN_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_TOKEN_FORMAT);
        }
    }

    public String userInfoUrl(String openId, String accessToken, String clientId) {
        return String.format(USER_INFO_URL, accessToken, clientId, openId);
    }

    /**
     * {
     * <p>
     * "ret":0,
     * <p>
     * "msg":"",
     * <p>
     * "nickname":"Peter",
     * <p>
     * "figureurl":"http://qzapp.qlogo.cn/qzapp/111111/942FEA70050EEAFBD4DCE2C1FC775E56/30",
     * <p>
     * "figureurl_1":"http://qzapp.qlogo.cn/qzapp/111111/942FEA70050EEAFBD4DCE2C1FC775E56/50",
     * <p>
     * "figureurl_2":"http://qzapp.qlogo.cn/qzapp/111111/942FEA70050EEAFBD4DCE2C1FC775E56/100",
     * <p>
     * "figureurl_qq_1":"http://q.qlogo.cn/qqapp/100312990/DE1931D5330620DBD07FB4A5422917B6/40",
     * <p>
     * "figureurl_qq_2":"http://q.qlogo.cn/qqapp/100312990/DE1931D5330620DBD07FB4A5422917B6/100",
     * <p>
     * "gender":"男",
     * <p>
     * "is_yellow_vip":"1",
     * <p>
     * "vip":"1",
     * <p>
     * "yellow_vip_level":"7",
     * <p>
     * "level":"7",
     * <p>
     * "is_yellow_year_vip":"1"
     * <p>
     * }
     */
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

        LOGGER.info("QQ oauth response {}", m);

        Object oErrorCode = m.get(ERRORCODE);

        if (oErrorCode != null && Integer.parseInt(oErrorCode.toString()) != 0) {
            LOGGER.error("[OAuth] Request errorcode:{} ", oErrorCode.toString());
            throw new OAuthException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE);
        }

        OAuthUser oAuthUser = new OAuthUser();


        Object oNickName = m.get(NICKNAME);
        oAuthUser.setNickName(oNickName != null ? oNickName.toString() : null);
        Object oSex = m.get(GENDER);
        int sex = 0;
        if (oSex != null && oSex.toString().equals("女")) {
            sex = 1;
        }
        oAuthUser.setSex(sex);
        Object oHeadImg = m.get(HEADIMGURL);
        oAuthUser.setHeadimgurl(oHeadImg != null ? oHeadImg.toString() : null);
        return oAuthUser;

    }

}
