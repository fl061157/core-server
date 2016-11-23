package cn.v5.entity.vo;

import org.springframework.beans.BeanUtils;

/**
 * Created by piguangtao on 15/7/14.
 */
public class ThirdAppRecommendUser extends BaseUserVo {
    private String appUserId;
    private String appUserName;

    public String getAppUserName() {
        return appUserName;
    }

    public void setAppUserName(String appUserName) {
        this.appUserName = appUserName;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public static ThirdAppRecommendUser createFromUser(BaseUserVo user) {
        ThirdAppRecommendUser vo = new ThirdAppRecommendUser();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public String toString() {
        return "ThirdAppRecommendUser{" +
                "appUserId='" + appUserId + '\'' +
                ", appUserName='" + appUserName + '\'' +
                "} " + super.toString();
    }
}
