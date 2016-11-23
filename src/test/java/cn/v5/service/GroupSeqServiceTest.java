package cn.v5.service;

import cn.v5.entity.Group;
import cn.v5.entity.User;
import cn.v5.test.TestTemplate;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by piguangtao on 15/11/12.
 */
public class GroupSeqServiceTest extends TestTemplate {
    @Autowired
    private GroupSeqService groupSeqService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    String groupId = Group.createUUID();

    @Before
    public void before() {
        createGroup();
    }

    @Test
    public void testGetGroupMemberNextNumberOnce() throws Exception {
        System.out.println("groupId:" + groupId);

//        Integer number = groupSeqService.getGroupMemberNextNumberOnce(groupId);
//        System.out.println("number" + number);

    }


    private void createGroup() {
        String user1 = "0cb6d440a4f811e3ac7abfd52f77bd17";
        String user2 = "0cb6d443a4f811e3ac7abfd52f77bd18";
        String user3 = "0cb6d442a4f811e3ac7abfd52f77bd17";

        Group group = new Group();
        group.setId(groupId);
        group.setName(String.format("%s_%d", "test", System.currentTimeMillis()));
        group.setCreator(user1);
        group.setCreateTime(new Date());
        group.setUpdateTime(System.currentTimeMillis());
        group.setEnableValidate("no");
        group.setRegion("0086");

        User creator = userService.findById(user1);

        try {
            groupService.createGroup(group, creator, ImmutableSet.copyOf(new String[]{user2, user3}));
        } catch (Exception e) {
            //ignore
        }
    }
}