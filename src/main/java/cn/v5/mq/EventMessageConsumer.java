package cn.v5.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Created by yangwei on 15-4-9.
 */
public class EventMessageConsumer extends MqMessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventMessageConsumer.class);

    private final AnnotatedHandlerFinder finder = new AnnotatedHandlerFinder();

    private final SetMultimap<String, EventMessageHandler> handlersByType = HashMultimap.create();

    @Autowired
    private ObjectMapper objectMapper;


    public synchronized void register(Object object) {
        Multimap<String, EventMessageHandler> methodsInListener = finder.findAllHandlers(object);
        handlersByType.putAll(methodsInListener);
    }

    @Override
    public void onMessage(byte[] message) throws Exception {
        if (null == message) return;
        try {
            String messageStr = new String(message, StandardCharsets.UTF_8);
            LOGGER.debug("receiver msg:{}", messageStr);

            JsonNode jsonNode = objectMapper.readTree(message);
            String type = jsonNode.get("type").textValue();
            JsonNode content = jsonNode.get("content");

            Set<EventMessageHandler> wrappers = handlersByType.get(type);
            if (!wrappers.isEmpty()) {
                for (EventMessageHandler handler : wrappers) {
                    try {
                        Object msg = objectMapper.readValue(content.toString(), handler.getEventClass());
                        handler.handleEvent(msg);
                    } catch (Exception e) {
                        LOGGER.error("event message handler " + handler + " handle " + content.toString() +
                                " error: ", e);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("fails to handle message.", e);
        }


    }
}
