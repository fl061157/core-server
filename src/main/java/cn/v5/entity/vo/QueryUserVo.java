package cn.v5.entity.vo;

import cn.v5.entity.User;
import org.springframework.beans.BeanUtils;

public class QueryUserVo extends User {
    private boolean byMobile;

    public boolean isByMobile() {
        return byMobile;
    }

    public void setByMobile(boolean byMobile) {
        this.byMobile = byMobile;
    }

    public static QueryUserVo createFromUser(User user,boolean byMobile) {
        QueryUserVo result=new QueryUserVo();
        BeanUtils.copyProperties(user, result);
        result.byMobile= byMobile;
        return result;
    }
}
