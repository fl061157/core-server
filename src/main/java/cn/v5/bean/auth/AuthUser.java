package cn.v5.bean.auth;

import cn.v5.InitBean;
import cn.v5.entity.User;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.util.Date;


public class AuthUser {

    private String id;

    private String nickname;

    private String mobile;

    private Integer sex;

    private String avatar;

    private String avatar_url;

    private String language;

    private Integer userType;

    private String hideTime;

    private String timezone;

    private String countrycode;

    private Integer mobileVerify;

    private Date createTime;

    private Timestamp lastLoginTime;

    private Long lastUpdateTime;

    private Integer appId = 0;

    private String account;

    public String getAvatar_url() {

        if (null == avatar_url || "".equals(avatar_url.trim())) {
            if (null != avatar && !"".equals(avatar.trim())) {
                if ("0086".equals(countrycode) || "+86".equals(countrycode) || "86".equals(countrycode)) {
                    return InitBean.getCDNUrl() + "/api/avatar/" + avatar;
                } else {
                    return InitBean.getUsCDNUrl() + "/api/avatar/" + avatar;
                }
            }
        } else {
            return avatar_url;
        }
        return "".intern();
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }


    public AuthUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getHideTime() {
        return hideTime;
    }

    public void setHideTime(String hideTime) {
        this.hideTime = hideTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public Integer getMobileVerify() {
        return mobileVerify;
    }

    public void setMobileVerify(Integer mobileVerify) {
        this.mobileVerify = mobileVerify;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public static AuthUser formAuthUser(User user){
        AuthUser authUser = new AuthUser();
        BeanUtils.copyProperties(user,authUser);
        return authUser;
    }

}
