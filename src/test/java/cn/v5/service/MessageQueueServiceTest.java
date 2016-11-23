package cn.v5.service;

import cn.v5.bean.group.GroupUser;
import cn.v5.entity.Group;
import cn.v5.entity.User;
import cn.v5.test.TestTemplate;
import cn.v5.v5protocol.GroupMessage;
import com.datastax.driver.core.Session;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-13 下午3:09
 */
public class MessageQueueServiceTest extends TestTemplate {
    @Autowired
    private MessageQueueService queueService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    private String toUserId = "f9253f90a8e211e3882863fbf5d6fe5b";
    private String fromUserId = "9ea391a0a8df11e38b7c63fbf5d6fe5b";
    private String groupId = "dada5190aa6011e3a15eab46942f854b";
    private String appId = "0";
    private Set<String> memberIds = Sets.newHashSet();

    @Before
    public void setUp() {
        Session session = manager.getNativeSession();
        session.execute("insert into users(id,nickname,app_id,countrycode,mobile) values('f9253f90a8e211e3882863fbf5d6fe5b','user2',0,'0086','13800000000')");
        session.execute("insert into users(id,nickname,app_id,countrycode,mobile) values('9ea391a0a8df11e38b7c63fbf5d6fe5b','user2',0,'0086','13800000000')");
        session.execute("insert into groups(id,creator,update_time) values('dada5190aa6011e3a15eab46942f854b','f9253f90a8e211e3882863fbf5d6fe5b',0)");
    }

    @Test
    public void testSendGroupCreateMsg() {
        Set<String> set = Sets.newHashSet();
        set.add(toUserId);
        List<User> memberList = userService.findUserListByNames(set);
//        queueService.sendGroupCreateMsg(userService.findById(fromUserId), memberList, groupService.findGroupInfo(groupId), appId);
    }

    @Test
    public void testSendGroupInviteMsg() {
        memberIds.add(toUserId);
        memberIds.add(fromUserId);
        User user = userService.findById(0, toUserId);
        User fromUser = userService.findById(0, fromUserId);
        String tip = fromUser.getNickname() + "," + user.getNickname();
        List<User> users = Lists.newArrayList();
        users.add(userService.findById(toUserId));
        users.add(userService.findById(fromUserId));
//        queueService.sendGroupInviteMsg(groupService.findGroupInfo(groupId), user, appId, fromUser.getNickname(), tip, users);

    }

    @Test
    public void testSendGroupUpdateMsg() {
        Group group = groupService.findGroupInfo(groupId);
        User user = userService.findById(toUserId);
        List<GroupUser> users = Lists.newArrayList();
        GroupUser groupUser = GroupUser.createFromUser(user);
        groupUser.setSeq(1);
        users.add(groupUser);
        queueService.sendGroupUpdateMsg(fromUserId, users, group);
    }

    @Test
    public void testSendGroupRemoveUserMsg() {
        Set<String> set = Sets.newHashSet();
        set.add(toUserId);

//        queueService.sendGroupRemoveUserMsg(fromUserId, set, groupId, appId, userService.findById(toUserId));
    }

    @Test
    public void testSendGroupExitMsg() {
        Set<String> set = Sets.newHashSet();
        set.add(toUserId);
//        queueService.sendGroupExitMsg(fromUserId, set, groupId, appId, userService.findById(toUserId));
    }

    @Test
    public void testSendGroupDismissMsg() {

//        queueService.sendGroupDismissMsg(fromUserId, userService.findById(toUserId).getId(), groupId, appId);
    }

    @Test
    public void testSendUserRegistMsg() {
        User user = userService.findById(toUserId);
        queueService.sendUserRegistMsg(userService.findById(toUserId), user);
    }

    @Test
    public void testSendUserUpdateMsg() {
        queueService.sendUserUpdateMsg(userService.findFriendsByUserId(fromUserId, 0), userService.findById(0, fromUserId));
    }

    @Test
    public void testSendEvent() {
        User user = new User();
        user.setCountrycode("0086");
        user.setId(User.createUUID());
        user.setMobile("13770508301");
        String routeKey = "user.register.us";
        if ("0086".equals(user.getCountrycode())) {
            routeKey = "user.register.cn";
        }
        String eventType = "user.register";
        queueService.sendEvent(routeKey, eventType, user);

//        CountDownLatch latch = new CountDownLatch(1);
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


    @Test
    public void testSendV5ProtocolGroupMessage() {
        GroupMessage message = new GroupMessage();
        message.setTraceId(UUID.randomUUID().toString());
        message.setGroupId("abbccd");
        message.setSendId("dddddd");
        message.getReceivers().add("dddadeeee");
        message.setNeedPush(Boolean.TRUE);
        message.setPushContent("push test");
        message.setEnterConversationForPush(Boolean.TRUE);
        message.setMsgBody("msg test");
        queueService.sendMsgByV5Protocol(message);
        assertTrue(Boolean.TRUE);

    }

}
