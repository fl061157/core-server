package cn.v5.service;

import cn.v5.code.NotifyMsgType;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Friend;
import cn.v5.entity.MobileIndex;
import cn.v5.entity.User;
import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.vo.BaseUserVo;
import cn.v5.entity.vo.ThirdAppRecommendUser;
import cn.v5.packet.NewPeopleMayKnownNotifyData;
import cn.v5.packet.NotifyMessage;
import cn.v5.web.controller.ServerException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.FluentIterable;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * Created by piguangtao on 15/7/8.
 */
@Service
public class FriendRecommendService {

    public static final Logger LOGGER = LoggerFactory.getLogger(FriendRecommendService.class);

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Inject
    private PhoneBookService phoneBookService;

    @Inject
    private UserService userService;

    @Value("${weibo.friends.bilateral}")
    private String weiboRecommendUrl;

    @Value("${facebook.friends}")
    private String faceBookRecommendUrl;

    @Autowired
    private HttpService httpService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FriendService friendService;

    @Autowired
    private MessageQueueService messageQueueService;

    @Inject
    private TaskService taskService;

    public List<ThirdAppRecommendUser> getRecommendFromThirdApp(ThirdAppCgUser thirdAppCgUser) {
        List<ThirdAppRecommendUser> thirdAppUsers = null;
        switch (thirdAppCgUser.getKey().getType()) {
            case SystemConstants.FRIEND_SOURCE_WEIBO: {
                thirdAppUsers = getRecommendFromWeibo(thirdAppCgUser);
                break;
            }
            case SystemConstants.FRIEND_SOURCE_FACEBOOK: {
                thirdAppUsers = getRecommendFromFaceBook(thirdAppCgUser);
                break;
            }
            default: {
                LOGGER.warn("recommand no support for soure:{}", thirdAppCgUser.getKey().getType());
                break;
            }
        }
        return thirdAppUsers;
    }

    /**
     * 获取第三方应用的可能认识的人ID列表
     * weibo 互相关注的人 最多一次返回2000条记录
     *
     * @return
     */
    protected List<ThirdAppRecommendUser> getRecommendFromWeibo(ThirdAppCgUser appCgUser) {
        final List<ThirdAppRecommendUser> result = new ArrayList<>();

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("access_token", appCgUser.getAccessToken());
        parameter.put("uid", appCgUser.getKey().getThirdAppUserId());
        parameter.put("count", 200);
        String weiboResult = httpService.doGet(weiboRecommendUrl, parameter);

        if (StringUtils.isNoneBlank(weiboResult)) {
            try {
                JsonNode weiboJson = objectMapper.readTree(weiboResult);
                JsonNode errorCodeNode = weiboJson.get("error_code");
                if (null != errorCodeNode) {
                    LOGGER.error("fails to invoke weibo interface. result:{}", weiboResult);
                    String errorCode = errorCodeNode.asText();
                    //http://open.weibo.com/wiki/Error_code
                    if ("21314".equalsIgnoreCase(errorCode) || "21315".equalsIgnoreCase(errorCode) || "21316".equalsIgnoreCase(errorCode) || "21317".equalsIgnoreCase(errorCode) || "21327".equalsIgnoreCase(errorCode)) {
                        throw new ServerException(StatusCode.INVALID_TOKEN, "invalid access token");
                    }
                } else {
                    ArrayNode arrayNode = (ArrayNode) weiboJson.get("users");
                    if (null != arrayNode && arrayNode.size() > 0) {
                        arrayNode.elements().forEachRemaining(jsonNode -> {
                            String screenName = jsonNode.get("screen_name").asText();
                            String name = jsonNode.get("name").asText();
                            String id = jsonNode.get("id").asText();
                            ThirdAppRecommendUser user = new ThirdAppRecommendUser();
                            user.setAppUserId(id);
                            user.setAppUserName(org.apache.commons.lang.StringUtils.isBlank(name) ? screenName : name);
                            result.add(user);
                        });
                    }
                }
            } catch (IOException e) {
                LOGGER.error("fails to parse weibo result. {}", weiboResult);
            }
        }

        LOGGER.debug("[weibo] user:{},result:{}", appCgUser, weiboResult);
        return result;
    }


    /**
     * 获取第三方应用的可能认识的人ID列表
     * https://developers.facebook.com/docs/graph-api/using-graph-api/v2.4
     *
     * @return
     */
    protected List<ThirdAppRecommendUser> getRecommendFromFaceBook(ThirdAppCgUser appCgUser) {
        final List<ThirdAppRecommendUser> result = new ArrayList<>();

        Map<String, String> parameter = new HashMap<>();
        parameter.put("access_token", appCgUser.getAccessToken());
        parameter.put("fields", "id,last_name,first_name,middle_name,name,name_format");

        String faceBookResult = httpService.doGet(faceBookRecommendUrl, parameter);

        if (StringUtils.isNoneBlank(faceBookResult)) {
            try {
                JsonNode faceBookJson = objectMapper.readTree(faceBookResult);
                JsonNode errorCodeNode = faceBookJson.get("error");
                if (null != errorCodeNode) {
                    LOGGER.error("fails to invoke weibo interface. user:{} , result:{}", appCgUser, faceBookResult);

                    JsonNode type_error = errorCodeNode.get("type");
                    if (null != type_error) {
                        String type = type_error.asText();
                        if ("OAuthException".equalsIgnoreCase(type)) {
                            throw new ServerException(StatusCode.INVALID_TOKEN, "invalid access token");
                        }
                    }

                    JsonNode error_subcode = errorCodeNode.get("error_subcode");
                    if (null != error_subcode) {
                        int errorCode = error_subcode.asInt();
                        //http://open.weibo.com/wiki/Error_code
                        if (467 == errorCode || 463 == errorCode) {
                            throw new ServerException(StatusCode.INVALID_TOKEN, "invalid access token");
                        }
                    }

                } else {
                    ArrayNode arrayNode = (ArrayNode) faceBookJson.get("data");
                    if (null != arrayNode && arrayNode.size() > 0) {
                        arrayNode.elements().forEachRemaining(jsonNode -> {
                            String name = jsonNode.get("name").asText();
                            String id = jsonNode.get("id").asText();
                            ThirdAppRecommendUser user = new ThirdAppRecommendUser();
                            user.setAppUserId(id);
                            user.setAppUserName(name);
                            result.add(user);
                        });
                    }
                }
            } catch (IOException e) {
                LOGGER.error("fails to parse weibo result. {}", faceBookResult);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[facebook] user:{},faceBookResult:{},result:{}", appCgUser, faceBookResult, null != result && result.size() > 0 ? Arrays.toString(result.toArray()) : "");
        }
        return result;
    }

    public Object[] recommendByThirdApp(User user, List<ThirdAppRecommendUser> thirdAppUsers, String source, Integer page, Integer count) {
        Object[] result = new Object[2];

        List<String> thirdAppUserIds = new ArrayList<>();
        Map<String, ThirdAppRecommendUser> thirdAppUserIdMap = new HashMap<>();

        thirdAppUsers.forEach(thirdAppUser -> {
            thirdAppUserIds.add(thirdAppUser.getAppUserId());
            thirdAppUserIdMap.put(thirdAppUser.getAppUserId(), thirdAppUser);
        });

        //获取第三方userId列表对应的cg userId 列表
        List<ThirdAppCgUser> thirdAppCgUsers = manager.sliceQuery(ThirdAppCgUser.class).forSelect().withPartitionComponents(source).andPartitionComponentsIN(thirdAppUserIds.toArray()).get();

        Map<String, String> cgUserIdMap = new HashMap<>();
        final List<String> userIds = new ArrayList<>();
        if (thirdAppCgUsers.size() > 0) {
            thirdAppCgUsers.stream().forEach(thirdAppCgUser -> {
                userIds.add(thirdAppCgUser.getKey().getUserId());
                cgUserIdMap.put(thirdAppCgUser.getKey().getUserId(), thirdAppCgUser.getKey().getThirdAppUserId());
            });
        }


        //查找本人删除的人，便于过滤
        final List<String> removedFriendIdList = phoneBookService.findRemoveFriendIdList(user.getId());

        //查找好友列表，便于过滤
        List<Friend> friendList = userService.findFriendsByUserId(user.getId(), 0);

        if (null == friendList) {
            friendList = new ArrayList<>();
        }
        final Set<String> friendIdList = FluentIterable.from(friendList).transform(input -> input.getId().getFriendId()).filter(input -> input != null).toSet();

        final List<String> validUserIds = FluentIterable.from(userIds).filter(s ->
                        (!removedFriendIdList.contains(s)) && (!friendIdList.contains(s))
        ).filter(input -> input != null).toList();

        if (null == validUserIds || validUserIds.size() < 1) {
            result[0] = 0;
            result[1] = new ArrayList();
            return result;
        }

        List<String> responseUserIdList = new ArrayList();

        //按照一定的数量返回
        if (null == page) {
            page = 1;
        }

        if (page < 1) {
            page = 1;
        }

        if (null == count) {
            count = 20;
        }

        int start = (page - 1) * count;
        int end = page * count;

        if (validUserIds.size() > start) {
            end = end < validUserIds.size() ? end : validUserIds.size();
            responseUserIdList = validUserIds.subList(start, end);
        }
        List<ThirdAppRecommendUser> recommendFriends = new ArrayList<>();
        if (responseUserIdList.size() > 0) {
            List<User> friends = userService.findByIdList(responseUserIdList);

            if (null == friends) {
                friends = new ArrayList<>();
            }
            recommendFriends = FluentIterable.from(friends).transform(input -> {
                BaseUserVo baseUserVo = BaseUserVo.createFromUser(input);
                ThirdAppRecommendUser thirdAppRecommendUser = ThirdAppRecommendUser.createFromUser(baseUserVo);
                thirdAppRecommendUser.setAppUserName(thirdAppUserIdMap.get(cgUserIdMap.get(input.getId())).getAppUserName());
                return thirdAppRecommendUser;
            }).filter(input -> input != null).toList();
        }
        result[0] = validUserIds.size();
        result[1] = recommendFriends;

        return result;
    }

    public void uploadContactWithToBeRecomand(User user, Collection<String[]> toBeRecommands) {
        if (null == user || null == toBeRecommands) return;
        try {
            this.taskService.execute(() -> toBeRecommands.stream().forEach(countryCodeAndPhone -> {
                if (null == countryCodeAndPhone || countryCodeAndPhone.length != 2) return;

                String countryCode = countryCodeAndPhone[0];
                String phone = countryCodeAndPhone[1];

                MobileIndex mobileIndex = userService.findMobileIndex(phone, countryCode,user.getAppId());

                if (null == mobileIndex) {
                    LOGGER.debug("countryCode:{},phone:{} no cg user.", countryCode, phone);
                    return;
                }
                String toUserId = mobileIndex.getUserId();

                //判断user是否为phone可能认识的人
                if (friendService.isRecommand(user.getId(), toUserId)) {
                    LOGGER.debug("fromUser:{},phone:{},countryCode:{}, toUser:{} , recommand", user.getId(), phone, countryCode, toUserId);
                    //发送系统通知
                    NotifyMessage notifyMessage = new NotifyMessage();
                    notifyMessage.setType(NotifyMsgType.COMMAND_PEOPLE_YOU_MAY_KNOWN);
                    notifyMessage.setAckFlag(true);
                    notifyMessage.setCmsgId(UUID.randomUUID().toString());
                    notifyMessage.setFrom(SystemConstants.SYSTEM_ACCOUNT_SECRETARY);
                    notifyMessage.setTo(toUserId);
                    notifyMessage.setData(new NewPeopleMayKnownNotifyData(SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK));
                    notifyMessage.setIncrOfflineCount(false);
                    notifyMessage.setPushFlag(false);
                    messageQueueService.sendSysMsg(notifyMessage);
                }
            }));
        } catch (Exception e) {
            LOGGER.error("upload contact user " + user.getId(), e);
        }
    }

    public void thirdAppFriends(User currentUser, List<ThirdAppRecommendUser> thirdAppUsers, String source) {
        if (null == currentUser || null == thirdAppUsers || thirdAppUsers.size() < 1 || org.apache.commons.lang.StringUtils.isBlank(source))
            return;

        try {
            this.taskService.execute(() -> friendService.saveThirdAppFriends(currentUser, thirdAppUsers, source));
        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            LOGGER.error(e.getMessage(), e);
        }

    }


}
