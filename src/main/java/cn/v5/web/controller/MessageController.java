package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.localentity.Message;
import cn.v5.service.MessageService;
import cn.v5.service.UserService;
import cn.v5.util.RequestUtils;
import cn.v5.util.UserUtils;
import cn.v5.validation.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by piguangtao on 15/2/6.
 */
@Controller
@RequestMapping(value = "/api", produces = "application/json")
@Validate
public class MessageController {
    private static Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @Inject
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestUtils requestUtils;

    /**
     * 用户网络重连时，会调用此接口，直到拉取的离线消息数目为0
     *
     * @param last_message_id
     * @param length
     * @param readMessageIdList
     * @return
     */
    //读取离线未读消息
    @RequestMapping(value = "/user/message/msg_snap", method = RequestMethod.GET)
    @ResponseBody
    public Map msgSnap(HttpServletRequest request, Long last_message_id, Integer length, @RequestParam(value = "read_message_ids", required = false) List<Long> readMessageIdList) {
        User userInfo = CurrentUser.user();
        if (length == null || length > 100 || length < 0) {
            length = 100;
        }
        List<Message> list;
        if (readMessageIdList != null && readMessageIdList.size() > 0) {
            list = messageService.findMessageByUserId(userInfo.getId(), readMessageIdList, length);
        } else {
            list = messageService.findMessagesByUserId(userInfo.getId(), last_message_id == null ? 0 : last_message_id, length);
        }

        userService.handleUserGetMsg(userInfo.getId(), userInfo.getAppId(), requestUtils.getUA(request), requestUtils.getClientVersion(request));

        if (list != null) {
            list = list.stream().map(m -> {
                m.setReceiver(UserUtils.genOpenUserId(m.getReceiver(), userInfo.getAppId()));
                m.setSender(UserUtils.genOpenUserId(m.getSender(), userInfo.getAppId()));
                m.setConversationId(UserUtils.genOpenUserId(m.getConversationId(), userInfo.getAppId()));
                return m;
            }).collect(Collectors.toList());
        }
        final List<Message> result = list;

        return new HashMap<String, List<Message>>() {{
            if (result != null) {
                put("messages", result);
            } else {
                put("messages", Collections.emptyList());
            }
        }};
    }


    /**
     * 设置本地未读的消息
     *
     * @param unread 用户本地未读的消息
     */
    @RequestMapping(value = "/user/message/unread", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> setUnreadMessageCont(@NotNull Integer unread) {
        User userInfo = CurrentUser.user();
        //设置用户本地未读的消息数目
        messageService.updateUnreadLocalCount(userInfo.getId(), unread, false);
        return SUCCESS_CODE;
    }
}
