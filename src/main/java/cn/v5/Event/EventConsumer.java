package cn.v5.Event;

import cn.v5.service.MessageQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Created by piguangtao on 15/4/9.
 */
@Service("coreServerEventConsumer")
public class EventConsumer {
    private static Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageQueueService queueService;


    @Value("${mq.http.push.exchange}")
    private String pushExchange;

//    @On(value= EventPath.PUSH_CHANGE)
//    public void  modifyPushInfo(PushEvent pushEvent){
//        try{
//            String eventType="push.change";
//            queueService.sendEvent(pushExchange,"",eventType,pushEvent);
//        }catch (Exception e){
//            LOGGER.error("modifyPushInfo error",e);
//        }
//    }

}
