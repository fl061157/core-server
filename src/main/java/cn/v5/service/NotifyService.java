package cn.v5.service;

import cn.v5.code.StatusCode;
import cn.v5.entity.MobileIndex;
import cn.v5.entity.User;
import cn.v5.packet.NotifyData;
import cn.v5.packet.NotifyMessage;
import cn.v5.util.StringUtil;
import cn.v5.web.controller.ServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by piguangtao on 15/5/13.
 */
@Service
public class NotifyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyService.class);

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;


    public void sendUserChangeNotify(String sender) {
        if (StringUtils.isBlank(sender)) {
            LOGGER.warn("sender should not be blank");
            throw new ServerException(StatusCode.PARAMETER_ERROR, "sender should not be blank");
        }
        User user = userService.findById(0, sender);
        if (null != user) {
            userService.userChange(user);
        }
    }

    public void sendSystemNotify(String sender, String toMobile, String pushContent, String message, int serviceType) {
        if (StringUtils.isBlank(toMobile) || StringUtils.isBlank(message)) {
            LOGGER.warn("reciever or message should not be blank");
            throw new ServerException(StatusCode.PARAMETER_ERROR, "reciever or message should not be blank");
        }
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setAckFlag(((byte) serviceType & 0x01) == 0x01);
        notifyMessage.setPushFlag(((byte) serviceType & 0x04) == 0x04);
        notifyMessage.setIncrOfflineCount(((byte) serviceType & 0x08) == 0x08);

        notifyMessage.setFrom(sender);

        if (StringUtils.isNotBlank(pushContent)) {
            notifyMessage.setPushContent(pushContent);
        }

        if (StringUtils.isNotBlank(message)) {
            try {
                NotifyData notifyData = objectMapper.readValue(message, NotifyData.class);
                notifyMessage.setData(notifyData);
            } catch (IOException e) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, "message format is wrong");
            }
        }

        String[] toMobiles = toMobile.split(",");
        List<String> mobiles = new ArrayList();
        Map<String, String> mobileCodeMap = new HashMap<>();
        for (String mobile : toMobiles) {
            if (mobile.length() < 5) {
                LOGGER.info("moible:{} is not correct.", mobile);
                continue;
            }
            String phone = mobile.substring(4);
            mobiles.add(phone);
            mobileCodeMap.put(phone, StringUtil.combinedMobileKey(mobile.substring(0, 4),0));
        }

        if (mobiles.size() > 100) {
            throw new ServerException(StatusCode.PARAMETER_ERROR, "the receiver count should <= 100.");
        }


        List<MobileIndex> mobileIndexes = userService.findMobileIndexList(mobiles, mobileCodeMap);

        List<String> toUserIds = new ArrayList<>();

        if (null != mobileIndexes && mobileIndexes.size() > 1) {
            toUserIds.addAll(mobileIndexes.stream().map(MobileIndex::getUserId).collect(Collectors.toList()));
        }

        if (toUserIds.size() > 1) {
            for (String userId : toUserIds) {
                notifyMessage.setTo(userId);
                messageQueueService.sendSysMsg(notifyMessage);
            }
        }
    }
}
