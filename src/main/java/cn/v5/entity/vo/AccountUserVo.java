package cn.v5.entity.vo;

import cn.v5.entity.User;
import org.springframework.beans.BeanUtils;

/**
 * Created by handwin on 2014/11/28.
 */
public class AccountUserVo extends User {
    private Integer canBeModified;

    public Integer getCanBeModified() {
        return canBeModified;
    }
    public void setCanBeModified(Integer canBeModified) {
        this.canBeModified = canBeModified;
    }
    public static AccountUserVo createFromUser(User user) {
        AccountUserVo accountUserVo=new AccountUserVo();
        BeanUtils.copyProperties(user, accountUserVo);
        return accountUserVo;
    }

}
