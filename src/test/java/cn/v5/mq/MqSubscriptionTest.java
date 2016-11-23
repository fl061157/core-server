package cn.v5.mq;

import cn.v5.test.TestTemplate;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by yangwei on 15-4-9.
 */
public class MqSubscriptionTest extends TestTemplate {
    private List<String> events = Lists.newArrayList();

    @Autowired
    private EventMessageConsumer consumer;

    @MqSubscribe(type="test")
    public void mqSubscribe(String event) {
        events.add(event);
    }

    @Before
    public void register() {
        consumer.register(this);
    }

    @Test
    public void testMqSubcription() throws Exception {
        String event = "{\"type\":\"test\", \"content\":\"event\"}";
        consumer.onMessage(event.getBytes());
        assert events.size() == 1;
        assert events.get(0).compareTo("event") == 0;
    }
}