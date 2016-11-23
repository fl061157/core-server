package cn.v5.bean;

import cn.v5.util.LocaleUtils;
import info.archinnov.achilles.internal.utils.UUIDGen;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

/**
 * Created by piguangtao on 15/6/1.
 */
public class UserLoginEvent {

    private String eventId;

    private Long eventTime;

    private String eventTimeStr;

    private String id;


    private String nickname;


    private String mobile;


    private Integer sex;

    private String avatar;


    private String avatar_url;


    private String regSource;


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


    private String publicKey;


    private String account;

    private Integer conversation;


    private Integer touch;

    private String clientVersion;

    private String ua;

    /**
     * 是否为新用户
     */
    private Boolean isNew;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getMobileVerify() {
        return mobileVerify;
    }

    public void setMobileVerify(Integer mobileVerify) {
        this.mobileVerify = mobileVerify;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getHideTime() {
        return hideTime;
    }

    public void setHideTime(String hideTime) {
        this.hideTime = hideTime;
    }


    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEventTimeStr() {
        return eventTimeStr;
    }

    public void setEventTimeStr(String eventTimeStr) {
        this.eventTimeStr = eventTimeStr;
    }

    public Integer getAppId() {
        return appId;
    }

    /**
     * Proxy User will set field value appId directly
     * This method only has effect for the json deserialization
     *
     * @param appId
     */
    public void setAppId(Integer appId) {
        this.appId = appId;
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


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public static String createUUID() {
        return UUIDGen.getTimeUUID().toString().replaceAll("\\-", "");
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

    public String getRegSource() {
        return regSource;
    }

    public void setRegSource(String regSource) {
        this.regSource = regSource;
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

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
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

    public static String genPassword(String password, String salt) {
        return DigestUtils.md5Hex(password + salt);
    }

    public static String genSalt() {
        return DigestUtils.sha256Hex(Long.toHexString(System.currentTimeMillis()));
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


    public Integer getConversation() {
        return conversation;
    }

    public void setConversation(Integer conversation) {
        this.conversation = conversation;
    }

    public Locale getLocale() {
        return LocaleUtils.parseLocaleString(language);
    }

    public Integer getTouch() {
        return touch;
    }

    public void setTouch(Integer touch) {
        this.touch = touch;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserLoginEvent{");
        sb.append("eventId='").append(eventId).append('\'');
        sb.append(", eventTime=").append(eventTime);
        sb.append(", eventTimeStr='").append(eventTimeStr).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", mobile='").append(mobile).append('\'');
        sb.append(", sex=").append(sex);
        sb.append(", avatar='").append(avatar).append('\'');
        sb.append(", avatar_url='").append(avatar_url).append('\'');
        sb.append(", regSource='").append(regSource).append('\'');
        sb.append(", language='").append(language).append('\'');
        sb.append(", userType=").append(userType);
        sb.append(", hideTime='").append(hideTime).append('\'');
        sb.append(", timezone='").append(timezone).append('\'');
        sb.append(", countrycode='").append(countrycode).append('\'');
        sb.append(", mobileVerify=").append(mobileVerify);
        sb.append(", createTime=").append(createTime);
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append(", appId=").append(appId);
        sb.append(", publicKey='").append(publicKey).append('\'');
        sb.append(", account='").append(account).append('\'');
        sb.append(", conversation=").append(conversation);
        sb.append(", touch=").append(touch);
        sb.append(", clientVersion='").append(clientVersion).append('\'');
        sb.append(", ua='").append(ua).append('\'');
        sb.append(", isNew=").append(isNew);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
