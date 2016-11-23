package cn.v5.oauth;

import cn.v5.code.StatusCode;
import cn.v5.util.LocaleUtils;
import cn.v5.util.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;


/**
 * Created by fangliang on 16/5/10.
 */
public class FaceBookApi extends DefaultApi20 {

    //    final String appID = "1616926665297539";
//    final String appSecrect = "2eab46ea06b7b9141caefb131b7bab33";
    final String callBack = "http://www.zhangying.mobi/";
    final String accessTokenURL = "https://graph.facebook.com/v2.5/oauth/access_token";
//    final String accessTokenURL = "https://graph.facebook.com/v2.5/oauth/access_token?client_id=%s&client_secret=%s&redirect_uri=%s";

    private static final String AUTHORIZE_URL
            = "https://www.facebook.com/v2.5/dialog/oauth?client_id=%s&redirect_uri=%s";


    private static Logger LOGGER = LoggerFactory.getLogger(FaceBookApi.class);

    private static final String USER_INFO_URL = "https://graph.facebook.com/v2.5/me?fields=accounts,gender,id,name,languages,locale,timezone,email,picture&access_token=%s";


    protected FaceBookApi() {

    }

    private static class InstanceHolder {

        private static final FaceBookApi INSTANCE = new FaceBookApi();
    }

    public static FaceBookApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return accessTokenURL;
    }

    @Override
    public String getRefreshTokenEndpoint() {
        throw new UnsupportedOperationException("Facebook doesn't support refershing tokens");
    }


    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Preconditions.checkValidUrl(config.getCallback(),
                "Must provide a valid url as callback. Facebook does not support OOB");
        final StringBuilder sb = new StringBuilder(String.format(AUTHORIZE_URL, config.getApiKey(),
                OAuthEncoder.encode(config.getCallback())));
        if (config.hasScope()) {
            sb.append('&').append(OAuthConstants.SCOPE).append('=').append(OAuthEncoder.encode(config.getScope()));
        }
        final String state = config.getState();
        if (state != null) {
            sb.append('&').append(OAuthConstants.STATE).append('=').append(OAuthEncoder.encode(state));
        }
        return sb.toString();
    }


    public String getUserInfoUrl(OAuth2AccessToken auth2AccessToken) throws OAuthException {
        return String.format(USER_INFO_URL, auth2AccessToken.getAccessToken());
    }

    /**
     *
     *
     * {"gender":"male",
     * "id":"521480371358455",
     * "locale":"zh_CN",
     * "timezone":8,
     * "email":"llc_729\u0040hotmail.com",
     * "picture":{
     * "data":
     * {"is_silhouette":true,
     * "url":"https:\/\/scontent.xx.fbcdn.net\/v\/t1.0-1\/c15.0.50.50\/p50x50\/10354686_10150004552801856_220367501106153455_n.jpg?oh=b9886b51fbe1a2e256724fb9392c8005&oe=57D4EC2F"}}}
     *
     */


    /**
     * {"email":"llc_729@hotmail.com",
     * "gender":"male",
     * "id":"521480371358455",
     * "locale":"zh_CN",
     * "name":"亮方",
     * "picture":{
     * "data":
     * {"url":"https://scontent.xx.fbcdn.net/v/t1.0-1/c15.0.50.50/p50x50/10354686_10150004552801856_220367501106153455_n.jpg?oh=b9886b51fbe1a2e256724fb9392c8005&oe=57D4EC2F"}},
     * "timezone":8}
     *
     *
     *
     */


    /**
     * {
     * "gender": "male",
     * "id": "625292307635013",
     * "locale": "en_US",
     * "timezone": 8,
     * "email": "fl061157@gmail.com",
     * "name": "亮方",
     * "picture": {
     * "data": {
     * "is_silhouette": false,
     * "url": "https://scontent.xx.fbcdn.net/v/t1.0-1/c0.0.50.50/p50x50/10410585_323099687854278_4549484591529964789_n.jpg?oh=27f96aefee9dc080bb549de28e58bf62&oe=57B039CB"
     * }
     * }
     * }
     *
     * @param response
     * @return
     * @throws OAuthException
     */
    public OAuthUser parse(String response) throws OAuthException {
        FaceBookUser faceBookUser;
        try {
            faceBookUser = JSON.parseObject(response, FaceBookUser.class);
        } catch (Exception e) {
            throw new OAuthException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE, e.getCause());
        }

        if (faceBookUser == null) {
            throw new OAuthException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE);
        }

        OAuthUser oAuthUser = new OAuthUser();

        try {
            oAuthUser.setCountryCode(LocalMapper.getRegionCode(LocaleUtils.parseLocaleString(faceBookUser.getLocale()).getCountry()));
        } catch (Exception e) {
            oAuthUser.setCountryCode(LocalMapper.DEFAULT_COUNTRY_CODE);
        }
        LOGGER.info("Facebook oauth response {}", faceBookUser);
        oAuthUser.setCountry(faceBookUser.getLocale());
        oAuthUser.setLanguage(faceBookUser.getLocale());

        oAuthUser.setUnionID(faceBookUser.getId());
        oAuthUser.setHeadimgurl(faceBookUser.getPicture().getData().getUrl());
        oAuthUser.setNickName(faceBookUser.getName());
        oAuthUser.setSex(StringUtils.isNotBlank(faceBookUser.getGender()) ? (faceBookUser.getGender().toLowerCase().equals("male") ? 0 : 1) : 0);
        LOGGER.info("[FaceBookApi] sex:{} , gender:{} ", oAuthUser.getSex(), faceBookUser.getGender());
        return oAuthUser;


    }


    public static class FaceBookUser {

        private String gender;
        private String id;
        private String locale;
        private int timezone;
        private String email;
        private String name;
        private Picture picture;

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getTimezone() {
            return timezone;
        }

        public void setTimezone(int timezone) {
            this.timezone = timezone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Picture getPicture() {
            return picture;
        }

        public void setPicture(Picture picture) {
            this.picture = picture;
        }
    }

    public static class Picture {
        private Data data;

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    public static class Data {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
