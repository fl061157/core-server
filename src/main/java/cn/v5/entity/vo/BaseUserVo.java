package cn.v5.entity.vo;

import cn.v5.entity.User;

/**
 * VO,json序列化后输出
 */
public class BaseUserVo {
    protected String id;
    protected String nickname;
    protected Integer sex;
    protected String avatar_url;
    protected String account;
    protected String mobile;
    protected String countrycode;
    protected Long createTime;
    private String timezone;
    private String language;
    private String source;
    private String appUserName;


    protected BaseUserVo() {
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

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public void setCreateTime(Long creatTime) {
        this.createTime = creatTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAppUserName() {
        return appUserName;
    }

    public void setAppUserName(String appUserName) {
        this.appUserName = appUserName;
    }

    public static BaseUserVo createFromUser(User user) {
        if (null == user) return null;
        BaseUserVo vo = new BaseUserVo();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setSex(user.getSex());
        vo.setAvatar_url(user.getAvatar_url());
        vo.setAccount(user.getAccount());
        vo.setMobile(user.getMobile());
        vo.setCountrycode(user.getCountrycode());
        vo.setTimezone(user.getTimezone());
        vo.setLanguage(user.getLanguage());
        return vo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseUserVo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", sex=").append(sex);
        sb.append(", avatar_url='").append(avatar_url).append('\'');
        sb.append(", account='").append(account).append('\'');
        sb.append(", mobile='").append(mobile).append('\'');
        sb.append(", countrycode='").append(countrycode).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", timezone='").append(timezone).append('\'');
        sb.append(", language='").append(language).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", appUserName='").append(appUserName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
