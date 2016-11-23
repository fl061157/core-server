package cn.v5.persist;

import cn.v5.localentity.Message;
import cn.v5.test.TestTemplate;
import cn.v5.util.StringUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by piguangtao on 15/11/19.
 */
public class MysqlMessagePersistTest extends TestTemplate {

    @Autowired
    private MysqlMessagePersist mysqlMessagePersist;

    ObjectMapper debugMapper = new ObjectMapper();


    @Before
    public void init() {
        debugMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        debugMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        debugMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    @Test
    public void testParse() throws Exception {
        com.handwin.database.bean.Message mysqlMessage = new com.handwin.database.bean.Message() ;
        mysqlMessage.setRoomID("dadadafd");
        mysqlMessage.setMeta("{\"entity_format\":0,\"cmsgid\":\"8d92308602c945c483dc9b4f957e18d60009\"}");
        Message message = mysqlMessagePersist.parse(mysqlMessage);
        message.setContent("test11");

        List<Message> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        System.out.println("##########");
        System.out.println(StringUtil.hideMsgContent(debugMapper.writeValueAsString(result)));


    }
}