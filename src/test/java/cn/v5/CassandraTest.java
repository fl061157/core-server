package cn.v5;
import cn.v5.code.StatusCode;
import cn.v5.entity.MobileIndex;
import cn.v5.entity.MobileKey;
import cn.v5.entity.User;
import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.thirdapp.ThirdAppCgUserKey;
import cn.v5.entity.thirdapp.ThirdAppToFriend;
import cn.v5.entity.thirdapp.ThirdAppToFriendsKey;
import cn.v5.entity.vo.ThirdAppRecommendUser;
import cn.v5.web.controller.ServerException;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import info.archinnov.achilles.configuration.ConfigurationParameters;
import info.archinnov.achilles.persistence.Batch;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.type.OptionsBuilder;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

/**
 * Created by piguangtao on 15/7/8.
 */
public class CassandraTest {

    private PersistenceManager manager;

    @Before
    public void before() {
        Map<ConfigurationParameters, Object> configMap = new HashMap<>();
        configMap.put(ConfigurationParameters.ENTITY_PACKAGES, "cn.v5.entity.thirdapp,cn.v5.entity");
        configMap.put(ConfigurationParameters.KEYSPACE_NAME, "faceshow");
        configMap.put(ConfigurationParameters.FORCE_TABLE_CREATION, true);

        Cluster cluster = Cluster.builder().addContactPoints("114.215.204.97".split(",")).withLoadBalancingPolicy(new RoundRobinPolicy()).build();

        PersistenceManagerFactory pmf = PersistenceManagerFactory.PersistenceManagerFactoryBuilder.build(cluster, configMap);
        manager = pmf.createPersistenceManager();
    }

    @Test
    public void testThirdAppCgUser() {

        ThirdAppCgUserKey key = new ThirdAppCgUserKey();
        key.setThirdAppUserId("1111");
        key.setType("12");
        key.setUserId("xxxxxxxxx");

        ThirdAppCgUser user = new ThirdAppCgUser();
        user.setKey(key);
        user.setAccessToken("13431134123413bbbbbb");

        manager.insertOrUpdate(user);


        String source = "12";
        List<String> thirdAppUsers = new ArrayList<>();
        thirdAppUsers.add("1111");
        thirdAppUsers.add("2222");
        List<ThirdAppCgUser> thirdAppCgUsers = manager.sliceQuery(ThirdAppCgUser.class).forSelect().withPartitionComponents(source).andPartitionComponentsIN(thirdAppUsers.toArray()).get();
//        Select.Where select = select().from("third_app_users_cg").where(eq("type", source)).and(in("third_app_user_id",thirdAppUsers));
//        List<ThirdAppCgUser> thirdAppCgUsers = manager.typedQuery(ThirdAppCgUser.class, select).get();

        Assert.assertTrue(null != thirdAppCgUsers);
    }

    @Test
    public void testUserLWT() {
        String userId = "22222222222222222222222222222222";
        String mobile = "22222222222222222222222222222222";
        User user = new User();
        user.setId(userId);
        user.setMobile(mobile);

        user = manager.insert(user);


        MobileIndex mobileIndex = new MobileIndex();
        mobileIndex.setMobileKey(new MobileKey(mobile, "0086"));
        mobileIndex.setUserId(user.getId());
        //客户端同一个用户并发调用此接口，需要检查手机号码已经存在，则表示用户新增成功
        try {
            manager.insert(mobileIndex, OptionsBuilder.ifNotExists());
        } catch (Exception e) {
            e.printStackTrace();
            //回滚数据
            try {
                manager.delete(user, OptionsBuilder.ifExists());
            } catch (Exception e1) {
                //ignore
            }
            throw new ServerException(StatusCode.ACCOUNT_EXIST, "mobile already exist");
        }
    }

    @Test
    public void batchInsert() {
        String source = "20";
        List<ThirdAppRecommendUser> thirdAppUsers = new ArrayList<>();
        String[] appUserId = new String[]{"14283c23-28f5-488b-af82-f39b6fa5bc5e", "c040ef03-0b3e-4616-8313-30458e00bfff", "f6769981-d4f2-4067-a9ee-1fde45dea5c6","ddda13333"};
        for (int i = 0; i < appUserId.length; i++) {
            ThirdAppRecommendUser recommendUser = new ThirdAppRecommendUser();
            recommendUser.setAppUserId(appUserId[i]);
            recommendUser.setAppUserName("test" + i);
            thirdAppUsers.add(recommendUser);
        }

        User currentUser = new User();
        currentUser.setId("dddafda343143dadfadafad");
        currentUser.setAppId(44444);


        Batch batch = manager.createBatch();
        batch.startBatch();

        for (int i =0;i<thirdAppUsers.size();i++){
            ThirdAppRecommendUser thirdAppRecommendUser = thirdAppUsers.get(i);
            ThirdAppToFriendsKey key = new ThirdAppToFriendsKey();
            key.setSource(source);
            key.setThirdAppUserId(thirdAppRecommendUser.getAppUserId());
            key.setUserId(currentUser.getId());

            ThirdAppToFriend friend = new ThirdAppToFriend();
            friend.setAppId(currentUser.getAppId());
            friend.setAppUserName(thirdAppRecommendUser.getAppUserName());
            friend.setKey(key);

            batch.insert(friend);
        }
//        thirdAppUsers.forEach(thirdAppRecommendUser -> {
//            ThirdAppToFriendsKey key = new ThirdAppToFriendsKey();
//            key.setSource(source);
//            key.setThirdAppUserId(thirdAppRecommendUser.getAppUserId());
//            key.setUserId(currentUser.getId());
//
//            ThirdAppToFriend friend = new ThirdAppToFriend();
//            friend.setAppId(currentUser.getAppId());
//            friend.setAppUserName(thirdAppRecommendUser.getAppUserName());
//            friend.setKey(key);
//
//            batch.insert(friend);
//
//        });
        batch.endBatch();
    }

    @Test
    public void testTypeQuery(){
        ThirdAppCgUser thirdAppCgUser = new ThirdAppCgUser();

        ThirdAppCgUserKey key = new ThirdAppCgUserKey();
        key.setThirdAppUserId("14283c23-28f5-488b-af82-f39b6fa5bc5e");
        key.setType("20");

        thirdAppCgUser.setKey(key);


        List<ThirdAppToFriend> oldFriends = new ArrayList(manager.typedQuery(ThirdAppToFriend.class, select().from("third_app_to_friend").where(eq("third_app_user_id", thirdAppCgUser.getKey().getThirdAppUserId())).and(eq("source", thirdAppCgUser.getKey().getType()))).get());

        Assert.assertTrue(null != oldFriends);
    }

}
