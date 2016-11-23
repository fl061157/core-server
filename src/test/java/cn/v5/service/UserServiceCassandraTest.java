package cn.v5.service;

import cn.v5.entity.RemovedFriend;
import cn.v5.entity.RemovedFriendKey;
import cn.v5.test.TestTemplate;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
/**
 * Created by sunhao on 14-8-11.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserServiceCassandraTest extends TestTemplate {

    @Test
    public void test01InsertRemovedFriend() {
        RemovedFriend removedFriend = new RemovedFriend(new RemovedFriendKey("00000000000000000000000000000000", "008618888888888"), "00000000000000000000000000000000");
        manager.insert(removedFriend);

        RemovedFriend removedFriend1 = manager.find(RemovedFriend.class, new RemovedFriendKey("00000000000000000000000000000000", "008618888888888"));
        System.out.println(removedFriend1);
        Assert.assertEquals(removedFriend, removedFriend1);

        removedFriend = new RemovedFriend(new RemovedFriendKey("00000000000000000000000000000000", "008616666666666"), "00000000000000000000000000000000");
        manager.insert(removedFriend);

        RemovedFriend removedFriend2 = manager.find(RemovedFriend.class, new RemovedFriendKey("00000000000000000000000000000000", "008616666666666"));
        System.out.println(removedFriend2);
        Assert.assertEquals(removedFriend, removedFriend2);

        List<RemovedFriend> list = manager.typedQuery(RemovedFriend.class, select().from("removed_friends").where(eq("user_id", "00000000000000000000000000000000"))).get();
        System.out.println(list);
        Assert.assertEquals(2, list.size());

        list = manager.typedQuery(RemovedFriend.class, select().from("removed_friends").where(eq("user_id", "11111111111111111111111111111111"))).get();
        System.out.println(list);
        Assert.assertEquals(0, list.size());
    }
}
