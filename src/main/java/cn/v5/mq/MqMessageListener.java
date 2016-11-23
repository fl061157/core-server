package cn.v5.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yangwei on 15-4-9.
 */
public abstract class MqMessageListener {

    public abstract void onMessage(byte[] message) throws Exception;

    private static final Logger logger = LoggerFactory.getLogger(MqMessageListener.class);

    private static AtomicLong isTaskHanding = new AtomicLong(0);

    public void onMessage(Message message) throws Exception {
        try {
            isTaskHanding.incrementAndGet();

            byte[] body = message.getBody();

            if (body != null && body.length > 0) {
                onMessage(body);
            } else {
                logger.error("Message is empty !");
            }
        } catch (Exception e) {
            logger.error("onMessage error ", e);
        } finally {
            isTaskHanding.decrementAndGet();
        }
    }

    public static AtomicLong getIsTaskHanding() {
        return isTaskHanding;
    }
}
