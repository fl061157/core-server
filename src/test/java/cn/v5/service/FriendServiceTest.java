package cn.v5.service;

import cn.v5.entity.FriendRequestCounter;
import cn.v5.entity.RemovedFriend;
import cn.v5.entity.RemovedFriendKey;
import cn.v5.entity.User;
import cn.v5.entity.thirdapp.ThirdAppToFriend;
import cn.v5.entity.vo.BaseUserVo;
import cn.v5.test.TestTemplate;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class FriendServiceTest extends TestTemplate {
    @Inject
    private FriendService friendService;
    @Inject
    private UserService userService;

    @Test
    public void testRecommend() {
//        List<BaseUserVo> recommendList = friendService.recommend("0cb6d440a4f811e3ac7abfd52f77bd17", "099891fc266cad3d6dae315d9518dd32", "0086", 0);
//        assertThat(recommendList.size()).isEqualTo(2);
//
//        recommendList = friendService.recommend("0cb6d440a4f811e3ac7abfd52f77bd17", "099891fc266cad3d6dae315d9518dd32", "0086", 1415357108097l);
//        assertThat(recommendList.size()).isEqualTo(0);

        String userId = "ec7b74f0326f11e48ccb559833872414";
        String mobile = "fa1fc49b2b5a8de27e2b3e9f850f7f80";
        String countryCode = "0086";
        long lastCreatTimeReq = 0;

        List<BaseUserVo> recommendList = null;

        while (true) {
            long startTime = System.currentTimeMillis();
            recommendList = (List<BaseUserVo>) friendService.recommend(userId, mobile, countryCode, lastCreatTimeReq, null, 0 )[1];
            long endTime = System.currentTimeMillis();

            System.out.println("spend time:" + (endTime - startTime));
            if (null == recommendList || recommendList.size() == 0) break;
            BaseUserVo lastUserVo = recommendList.get(recommendList.size() - 1);
            System.out.println(String.format("size:%d,createTime:%d", recommendList.size(), lastUserVo.getCreateTime()));

            lastCreatTimeReq = lastUserVo.getCreateTime();
        }

    }

    @Test
    public void testFriendRequestCountIncrement() {
        String promoter = "12345678901234567890123456789012";
        String promoter2 = "34623572389573757389579375739345";
        String toUser = "09876543210987654321098765432109";

        FriendRequestCounter counter = manager.find(FriendRequestCounter.class, toUser);
        assertThat(counter).isNull();

        friendService.friendRequestCountIncrement(promoter, toUser);

        counter = manager.find(FriendRequestCounter.class, toUser);
        assertThat(counter).isNotNull();
        assertThat(counter.getCount()).isEqualTo(1);

        friendService.friendRequestCountIncrement(promoter2, toUser);
        counter = manager.find(FriendRequestCounter.class, toUser);
        assertThat(counter).isNotNull();
        assertThat(counter.getCount()).isEqualTo(2);
    }

    @Test
    public void testClearFriendRequest() {
        FriendRequestCounter friendRequestCounter = new FriendRequestCounter();
        friendRequestCounter.setCount(12);
        friendRequestCounter.setPushDate("2000-03-01");
        friendRequestCounter.setUserId("12345678901234567890123456789012");
        manager.insert(friendRequestCounter);
        friendRequestCounter = new FriendRequestCounter();
        friendRequestCounter.setCount(45);
        friendRequestCounter.setPushDate("2000-01-01");
        friendRequestCounter.setUserId("09876543210987654321098765432109");
        manager.insert(friendRequestCounter);

        User user = new User();
        user.setId("09876543210987654321098765432109");
        friendService.clearFriendRequest(user);

        FriendRequestCounter result = manager.find(FriendRequestCounter.class, "09876543210987654321098765432109");
        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(0);
        assertThat(result.getPushDate()).isNotEqualTo("2000-01-01").isNotEqualTo("2000-03-01");

        result = manager.find(FriendRequestCounter.class, "12345678901234567890123456789012");
        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(12);
        assertThat(result.getPushDate()).isEqualTo("2000-03-01");
    }

    @Test
    public void testMakeFriend() {
        String user1 = "0cb6d444a4f811e3ac7abfd52f77bd17";
        String user2 = "0cb6d443a4f811e3ac7abfd52f77bd17";
        String mobile1 = "4c3937ee17250c507f4669290033bcac";
        String mobile2 = "1f4483b21850816720f47450bdd50d3f";

        RemovedFriend rf = new RemovedFriend(new RemovedFriendKey("0cb6d444a4f811e3ac7abfd52f77bd17", "1f4483b21850816720f47450bdd50d3f"), "0cb6d443a4f811e3ac7abfd52f77bd17");
        manager.insert(rf);

        User u1 = userService.findById(0, "0cb6d444a4f811e3ac7abfd52f77bd17");
        User u2 = userService.findById(0, "0cb6d443a4f811e3ac7abfd52f77bd17");

        friendService.makeFriend(u1, u2, null, null);

        rf = manager.find(RemovedFriend.class, new RemovedFriendKey("0cb6d444a4f811e3ac7abfd52f77bd17", "1f4483b21850816720f47450bdd50d3f"));
        assertThat(rf).isNull();

    }

    @Test
    public void testThirdAppCgUser() {

//
//        List<ThirdAppCgUser> thirdAppCgUsers = new ArrayList(manager.typedQuery(ThirdAppCgUser.class, select().from("third_app_users_cg").where(eq("type", "12")).and(eq("third_app_user_id", "3292704935"))).get());
//
//        List<ThirdAppToFriend> toFriends = manager.sliceQuery(ThirdAppToFriend.class).forSelect().withPartitionComponents("3292704935", "12").get();
//
//
//        System.out.println(thirdAppCgUsers);
//        System.out.println(toFriends);
//
//
//        List<CgThirdAppFriend> thirdFriends = manager.sliceQuery(CgThirdAppFriend.class).forSelect()
//                .withPartitionComponents("1111")
//                .withConsistency(info.archinnov.achilles.type.ConsistencyLevel.LOCAL_ONE)
//                .get(200);
//        System.out.println(thirdFriends);
//
//
//        thirdAppCgUsers = manager.typedQuery(ThirdAppCgUser.class,
//                select().from("third_app_users_cg").where(QueryBuilder.eq("type", SystemConstants.FRIEND_SOURCE_WEIBO)).and(QueryBuilder.in("third_app_user_id", new String[]{"11111", "2222"}))
//                        .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
//        ).get();
//
//        System.out.println(thirdAppCgUsers);
//

        List<ThirdAppToFriend> thirdAppUser = manager.sliceQuery(ThirdAppToFriend.class).forSelect()
                .withPartitionComponents("dafadfadafd", "12")
                .withConsistency(info.archinnov.achilles.type.ConsistencyLevel.LOCAL_QUORUM)
                .get(1);
        System.out.println(thirdAppUser);

    }
}
