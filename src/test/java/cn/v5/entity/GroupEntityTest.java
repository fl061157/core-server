package cn.v5.entity;

import cn.v5.bean.group.GroupUser;
import cn.v5.service.GroupService;
import cn.v5.service.MessageQueueService;
import cn.v5.service.UserService;
import cn.v5.test.TestTemplate;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class GroupEntityTest extends TestTemplate {
    @Inject
    private GroupService groupService;
    @Inject
    private UserService userService;
    @InjectMocks
    private MessageQueueService messageQueueService;

    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void groupMembers() {
        String adminId = "0cb6d440a4f811e3ac7abfd52f77bd17";
        User admin = userService.findById(0,adminId);
        assertThat(admin).isNotNull();
        assertThat(admin.getId()).isEqualTo(adminId);

        Group group = new Group();
        group.setName("handwin company");
        groupService.createGroup(group, admin, ImmutableSet.of("0cb6d442a4f811e3ac7abfd52f77bd17", "0cb6d444a4f811e3ac7abfd52f77bd17", "0cb6d443a4f811e3ac7abfd52f77bd17"));
        List<GroupUser> memberList = group.getMembers();
        assertThat(memberList.size()).isEqualTo(4);
        assertThat(memberList.contains(admin)).isEqualTo(true);

        User user = manager.find(User.class, "0cb6d442a4f811e3ac7abfd52f77bd17");
        assertThat(memberList.contains(user)).isEqualTo(true);

        List<Group> groups = groupService.findGroupsByUserId("0cb6d442a4f811e3ac7abfd52f77bd17");
        assertThat(groups.size()).isEqualTo(1);


    }


}
