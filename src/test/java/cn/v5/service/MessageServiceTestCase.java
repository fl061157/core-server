package cn.v5.service;

import cn.v5.localentity.Message;
import cn.v5.localentity.MessageIndex;
import cn.v5.localentity.MessageIndexKey;
import cn.v5.localentity.UserReadMessage;
import cn.v5.test.TestTemplate;
import com.datastax.driver.core.Session;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class MessageServiceTestCase extends TestTemplate {
    @Autowired
    private MessageService messageService;


    @Test
    public void testMsgSnap() {
        Session session = manager.getNativeSession();

        String userId = "0";
        for (int i = 1; i <= 200; i++) {
            MessageIndexKey id = new MessageIndexKey();
            id.setUserId(userId);
            id.setMessageId(Long.valueOf(i));

            MessageIndex mi = new MessageIndex();
            mi.setId(id);
            Message message = new Message();
            message.setId(Long.valueOf(i));
            message.setType("TEXT");
            mi.setMessage(message);
            mi.setContent("test".getBytes());

            manager.insert(mi);
        }

        List<Message> messages = messageService.findMessagesByUserId(userId, 0, 20);
        assertThat(messages.size()).isEqualTo(20);
        Message lastMessage = messages.get(messages.size() - 1);

        assertThat(lastMessage.getId()).isEqualTo(20l);
        UserReadMessage um = manager.find(UserReadMessage.class, userId);

        assertThat(um.getLastMessageId()).isEqualTo(20L);
        assertThat(um.getLastReadMessageId()).isEqualTo(0L);

        messages = messageService.findMessagesByUserId(userId, 20, 20);
        lastMessage = messages.get(messages.size() - 1);
        assertThat(lastMessage.getId()).isEqualTo(40l);
        um = manager.find(UserReadMessage.class, userId);
        assertThat(um.getLastMessageId()).isEqualTo(40L);
        assertThat(um.getLastReadMessageId()).isEqualTo(20L);


        messages = messageService.findMessagesByUserId(userId, 0, 20);
        lastMessage = messages.get(messages.size() - 1);
        assertThat(lastMessage.getId()).isEqualTo(40l);
        um = manager.find(UserReadMessage.class, userId);
        assertThat(um.getLastMessageId()).isEqualTo(40L);
        assertThat(um.getLastReadMessageId()).isEqualTo(20L);


        messages = messageService.findMessagesByUserId(userId, 0, 20);
        lastMessage = messages.get(messages.size() - 1);
        assertThat(lastMessage.getId()).isEqualTo(40l);
        um = manager.find(UserReadMessage.class, userId);
        assertThat(um.getLastMessageId()).isEqualTo(40L);
        assertThat(um.getLastReadMessageId()).isEqualTo(20L);

    }

}
