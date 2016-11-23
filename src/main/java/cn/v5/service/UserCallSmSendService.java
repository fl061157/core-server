package cn.v5.service;

import cn.v5.entity.*;
import cn.v5.util.LocaleUtils;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Locale;


/**
 * Created by piguangtao on 15/9/7.
 */
@Service
public class UserCallSmSendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCallSmSendService.class);

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSourceService messageSourceService ;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PhoneBookService phoneBookService;


    public void resetSmCount(String receiverId) {
        try {
            LOGGER.debug("[UserCallSm]receiver:{} reset count.", receiverId);
            if (StringUtils.isBlank(receiverId)) {
                return;
            }
            UserCallSmSendKey key = new UserCallSmSendKey();
            key.setReceiverId(receiverId);

            manager.getNativeSession().execute("delete from user_call_sm_send where receiver_id = ?", receiverId);

        } catch (Exception e) {
            LOGGER.error(String.format("fails to reset user call sm send. receiverId:%s", receiverId), e);
        }

    }

    /**
     * @param receiverId 呼叫的接受方
     * @param fromId     呼叫的发送方
     */
    public void incUserSmCount(String receiverId, String fromId) {
        try {
            LOGGER.debug("[UserCallSm]receiverId:{},fromId:{} count increase by 1", receiverId, fromId);
            UserCallSmSendKey key = new UserCallSmSendKey();
            key.setReceiverId(receiverId);
            key.setFromId(fromId);

            //先查询，再增加1, 客户端不会并发调用此接口，不需要考虑并发情况
            UserCallSmSend smSend = manager.find(UserCallSmSend.class, key);
            if (null == smSend) {
                smSend = new UserCallSmSend();
                smSend.setKey(key);
                smSend.setCount(1);
            } else {
                smSend.setCount(smSend.getCount() + 1);
            }
            smSend.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            manager.insertOrUpdate(smSend);
        } catch (Exception e) {
            LOGGER.error(String.format("fails to increase user call sm send.receiverId:%s,fromId:%s", receiverId, fromId), e);
        }
    }

    public void sendCallSmForIos(User toUser, User fromUser, String type) {
        if (null == toUser || null == fromUser) {
            return;
        }
        if (StringUtils.isBlank(toUser.getMobilePlaintext())) {
            LOGGER.debug("[sm]no send. userId:{} has no plain text mobile", toUser);
            return;
        }
//        String fromUserMobile = fromUser.getMobile();
//        String fromCountryCode = fromUser.getCountrycode();
//        String toUserId = toUser.getId();

//        PhoneBook phoneBook = phoneBookService.findUserPhoneBook(fromCountryCode, fromUserMobile, toUserId);
//        if (null == phoneBook) {
//            LOGGER.debug("[send call sm] fromCountryCode:{},fromUserMobile:{} not in toUserId:{} phone book. no send sm", fromCountryCode, fromUserMobile, toUserId);
//            return;
//        }

        //对方为IOS
        UserCallSmSendKey key = new UserCallSmSendKey();
        key.setFromId(fromUser.getId());
        key.setReceiverId(toUser.getId());

        UserCallSmSend smSend = manager.find(UserCallSmSend.class, key);
        if (null == smSend) {
            //表示第一次发送短，需要发送短信提示语
            //发送方是否在接受方的本地通讯录中
            Locale locale = LocaleUtils.parseLocaleString(toUser.getLanguage());
            String resourceName = null;
            switch (type) {
                case "call": {
                    resourceName = "calling.sm";
                    break;
                }
                case "missed": {
                    resourceName = "missed.call.sm";
                    break;
                }
            }
            if (StringUtils.isBlank(resourceName)) {
                LOGGER.warn("[send call sm]. type:{} no support", type);
                return;
            }

            String sendName = userService.getFriendNickname(fromUser, toUser.getId(), toUser.getAppId());
            String msgContent = messageSourceService.getMessageSource(toUser.getAppId()).getMessage(resourceName, new Object[]{sendName}, locale);
            smsService.sendSmsExcludeAuth(toUser.getMobilePlaintext(), toUser.getCountrycode(), msgContent);
            LOGGER.debug("[user call sm send] first time. send sm. toUser:{},fromUser:{},type:{},content:{}", toUser, fromUser, type, msgContent);
        } else {
            LOGGER.debug("[user call sm send]. not first time, no send sm. receiverId:{},fromId:{}", toUser.getId(), fromUser.getId());
        }
        incUserSmCount(toUser.getId(), fromUser.getId());
    }

}
