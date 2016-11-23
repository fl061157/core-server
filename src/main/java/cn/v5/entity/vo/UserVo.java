package cn.v5.entity.vo;

import cn.v5.entity.User;
import org.springframework.beans.BeanUtils;


public class UserVo extends BaseUserVo {

    private Integer userType;
    private Integer mobileVerify;
    private String publicKey;
    private Integer conversation;
    private String contactName;
    private String source;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Integer getConversation() {
        return conversation;
    }

    public void setConversation(Integer conversation) {
        this.conversation = conversation;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static UserVo createFromUser(User user) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);
//        if(StringUtils.isBlank(vo.getAvatar_url())) {
//            if(StringUtils.isBlank(user.getAvatar())) {
//                vo.setAvatar_url(ConfigUtils.getString("base.url") + "/api/avatar/default");
//            } else {
//                vo.setAvatar_url(ConfigUtils.getString("base.url") + "/api/avatar/" + user.getAvatar());
//            }
//        }

        return vo;
    }


    public static void main(String[] args) {
        User user = new User();
        user.setId("1");
//        user.setAvatar_url("1");

        UserVo vo = createFromUser(user);
        System.out.println(vo.getId());
    }
}
