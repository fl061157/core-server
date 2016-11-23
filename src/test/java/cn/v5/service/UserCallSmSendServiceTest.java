package cn.v5.service;

import cn.v5.entity.UserCallSmSend;
import cn.v5.entity.UserCallSmSendKey;
import cn.v5.test.TestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by piguangtao on 15/9/7.
 */
public class UserCallSmSendServiceTest extends TestTemplate {
    private String receiverId;
    private String fromId;
    private String fromId2;
    private String receiverId2;

    @Before
    public void before() {
        receiverId = "abcd";
        fromId = "efgh";

        receiverId2 = "abcedfg";
        fromId2 = "efgghaaa";
    }

    @Autowired
    private UserCallSmSendService callSmSendService;

    @Test
    public void testResetSmCount() throws Exception {
        callSmSendService.incUserSmCount(receiverId, fromId);
        UserCallSmSendKey key = new UserCallSmSendKey();
        key.setReceiverId(receiverId);
        key.setFromId(fromId);

        UserCallSmSend userCallSmSend = manager.find(UserCallSmSend.class, key);
        assertNotNull(userCallSmSend);

        assertEquals(1, userCallSmSend.getCount().intValue());
        assertEquals(receiverId, userCallSmSend.getKey().getReceiverId());

        callSmSendService.resetSmCount(receiverId);
        userCallSmSend = manager.find(UserCallSmSend.class, key);
        assertNull(userCallSmSend);

    }

    @Test
    public void testIncUserSmCount() throws Exception {
        callSmSendService.incUserSmCount(receiverId, fromId);
        UserCallSmSendKey key = new UserCallSmSendKey();
        key.setReceiverId(receiverId);
        key.setFromId(fromId);

        UserCallSmSend userCallSmSend = manager.find(UserCallSmSend.class, key);
        assertNotNull(userCallSmSend);

        assertEquals(1, userCallSmSend.getCount().intValue());
        assertEquals(receiverId, userCallSmSend.getKey().getReceiverId());

        callSmSendService.incUserSmCount(receiverId, fromId);

        userCallSmSend = manager.find(UserCallSmSend.class, key);
        assertNotNull(userCallSmSend);

        assertEquals(2, userCallSmSend.getCount().intValue());

    }

    @Test
    public void testSendCallSmForIos() throws Exception {


    }
}