package cn.v5.entity;

import cn.v5.localentity.Message;
import cn.v5.localentity.MessageIndex;
import cn.v5.localentity.MessageIndexKey;
import cn.v5.service.MessageService;
import cn.v5.service.UserService;
import cn.v5.test.TestTemplate;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class MessageServiceTest extends TestTemplate {
    @Inject
    private MessageService messageService;
    @Inject
    private UserService userService;


    @Test
    public void findMessages() {
        String uid = "0cb6d440a4f811e3ac7abfd52f77bd17";

        for (int i = 0; i < 20; i++) {
            Message message = new Message();

            message.setId((long) i);
            message.setSender("0cb6d440a4f811e3ac7abfd52f77bd17");
            message.setReceiver("0cb6d440a4f811e3ac7abfd52f77bd17");
            message.setCreateTime(System.currentTimeMillis());

            MessageIndex mi = new MessageIndex(new MessageIndexKey("0cb6d440a4f811e3ac7abfd52f77bd17", (long) i), message);
            mi.setContent("test".getBytes());


            manager.insert(mi);

        }

        List<Message> messageList = messageService.findMessagesByUserId(uid, 0, 5);

        assertThat(messageList.size()).isEqualTo(5);
        assertThat(messageList.get(0).getId()).isEqualTo(1);
        messageList = messageService.findMessagesByUserId(uid, 5, 5);

        assertThat(messageList.size()).isEqualTo(5);
        assertThat(messageList.get(0).getId()).isEqualTo(6);
        messageList = messageService.findMessagesByUserId(uid, 0, 5);

        assertThat(messageList.size()).isEqualTo(5);
        assertThat(messageList.get(0).getId()).isEqualTo(6);
    }


}
