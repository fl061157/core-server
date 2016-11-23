package cn.v5.service;

import cn.v5.bean.group.GroupUser;
import cn.v5.entity.Group;
import cn.v5.entity.User;
import cn.v5.test.TestTemplate;
import com.datastax.driver.core.Session;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-10
 * Time: 下午3:26
 * To change this template use File | Settings | File Templates.
 */
public class GroupServiceTest extends TestTemplate {
    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConversationService conversationService;

    @Before
    public void setUp() {
        Session session = manager.getNativeSession();
        session.execute("insert into users(id,nickname,app_id,countrycode,mobile) values('9ea391a0a8df11e38b7c63fbf5d6fe5b','user2',0,'0086','13800000000')");
        session.execute("insert into users(id,nickname,app_id,countrycode,mobile) values('008b20c0a8e211e38b7c63fbf5d6fe5b','user2',0,'0086','13800000000')");
        session.execute("insert into users(id,nickname,app_id,countrycode,mobile) values('e631bec0a8df11e38b7c63fbf5d6fe5b','user2',0,'0086','13800000000')");
        session.execute("insert into users(id,nickname,app_id,countrycode,mobile) values('f9253f90a8e211e3882863fbf5d6fe5b','user2',0,'0086','13800000000')");


        session.execute("insert into groups(id,creator,update_time) values('a3f17c60a8eb11e3992bbdebcb59dddc','f9253f90a8e211e3882863fbf5d6fe5b',0)");
        session.execute("insert into groups(id,creator,update_time) values('dada5190aa6011e3a15eab46942f854b','f9253f90a8e211e3882863fbf5d6fe5b',0)");


    }


    @Test
    public void testCreateGroup() throws Exception {
        String adminId = "9ea391a0a8df11e38b7c63fbf5d6fe5b";
        User admin = userService.findById(0, adminId);
        assertThat(admin).isNotNull();
        Group group = new Group();
        group.setName("new group0000000");
        group = groupService.createGroup(group, admin, ImmutableSet.of("008b20c0a8e211e38b7c63fbf5d6fe5b", "e631bec0a8df11e38b7c63fbf5d6fe5b"));
        List<GroupUser> members = group.getMembers();
        assertThat(members).isNotEmpty();

    }

    @Test
    public void testExitGroup() throws Exception {
        String groupId = "a3f17c60a8eb11e3992bbdebcb59dddc";
        Group group = groupService.findGroupInfo(groupId);
        assertThat(group).isNotNull();
        String userId = "008b20c0a8e211e38b7c63fbf5d6fe5b";
        User user = userService.findById(0, userId);
        assertThat(user).isNotNull();
        groupService.exit(user, group);
    }

    @Test
    public void testInviteGroup() throws Exception {
        String groupId = "dada5190aa6011e3a15eab46942f854b";
        Group group = groupService.findGroupInfo(groupId);
        //List<User> members = group.getMembers();
        //assertThat(members).isNotEmpty();
        assertThat(group).isNotNull();
        String userId = "f9253f90a8e211e3882863fbf5d6fe5b";
        User user = userService.findById(0, userId);
        assertThat(user).isNotNull();
        List<String> memberIds = new ArrayList<>();
        //memberIds.add("e631bec0a8df11e38b7c63fbf5d6fe5b");
        //assertThat(userService.findById("e631bec0a8df11e38b7c63fbf5d6fe5b")).isNotNull();
//        groupService.invite(user, group, memberIds, user.getId());
    }

    @Test
    public void testGenerateGroupAccount() throws InterruptedException {
        String countryCode = "0086";
        String groupId = "e631bec0a8df11e38b7c63fbf5d6fe51";
        final CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            new Thread() {
                public void run() {
                    try {
                        String account = groupService.generateGroupAccount(countryCode, groupId);
                        System.out.println(String.format("=======countryCode:%s,groupId:%s,account:%s", countryCode, groupId, account));
                    } finally {
                        latch.countDown();
                    }
                }
            }.start();
        }
        latch.await();
    }


    @Test
    public void testGroupMemberOverLimit() {
        String groupId = "e631bec0a8df11e38b7c63fbf5d6fe51";
        System.out.println(groupService.groupMemberOverLimit(groupId, 10));
    }


    @Test
    public void testGetGroupInfoFromOtherRegion() {
        String groupId = "6a8762d0950911e58d39559833872414";
        String region = "";
        String userId = "cc0b33a08da311e58726559833872414";
        Group group = groupService.getGroupInfoFromOtherRegion(groupId, region, userId, 0);
        assertNotNull(group);
    }


}
