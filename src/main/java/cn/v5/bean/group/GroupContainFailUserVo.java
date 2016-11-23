package cn.v5.bean.group;

import cn.v5.entity.Group;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piguangtao on 15/11/18.
 */
public class GroupContainFailUserVo extends Group {
    private List<FailUserInfo> fail = new ArrayList<>();

    public List<FailUserInfo> getFail() {
        return fail;
    }

    public void setFail(List<FailUserInfo> fail) {
        this.fail = fail;
    }

    public static GroupContainFailUserVo createFromGroup(Group group) {
        GroupContainFailUserVo vo = new GroupContainFailUserVo();
        BeanUtils.copyProperties(group, vo);
        return vo;
    }
}
