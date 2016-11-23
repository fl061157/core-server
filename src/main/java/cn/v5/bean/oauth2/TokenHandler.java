package cn.v5.bean.oauth2;

/**
 * Created by haoWang on 2016/7/18.
 */
public interface TokenHandler {
    //过期token失效
    void invalidToken(String clientId);
    //通过token获取AppId
    String getAppId(String tokenValue);
}
