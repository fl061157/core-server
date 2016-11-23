package cn.v5.service;

import cn.v5.bean.RecommandUserWithOtherApp;
import cn.v5.code.NotifyMsgType;
import cn.v5.code.SystemConstants;
import cn.v5.entity.*;
import cn.v5.entity.thirdapp.*;
import cn.v5.entity.vo.BaseUserVo;
import cn.v5.entity.vo.ThirdAppRecommendUser;
import cn.v5.metric.LogUtil;
import cn.v5.packet.NewPeopleMayKnownNotifyData;
import cn.v5.packet.NotifyMessage;
import cn.v5.util.LoggerFactory;
import cn.v5.util.ReqMetricUtil;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.collect.FluentIterable;
import info.archinnov.achilles.persistence.Batch;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static cn.v5.metric.LogUtil.LOG_SPLIT_CHAR;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

/**
 * 好友服务
 * 添加、删除、推荐
 */
@Service
public class FriendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendService.class);

    @Inject
    private PhoneBookService phoneBookService;
    @Inject
    private UserService userService;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private FriendRecommendService friendRecommendService;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private ReqMetricUtil reqMetricUtil;

    private SimpleDateFormat requestDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public FriendService() {
        requestDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * 后期做专门的账号体系（不使用nosql时）可以优化
     * TODO 根据接口性能进行优化
     *
     * @param userId
     * @param mobile
     * @param countryCode
     * @param lastCreatTimeReq
     * @return
     */
    public Object[] recommend(String userId, String mobile, String countryCode, final long lastCreatTimeReq, String source, int appID) {
        Object[] result = new Object[2];

        //查找通讯录中存在该号码的人，时间为通讯录上传本号码的人
        Long startTime = System.currentTimeMillis();

        List<PhoneBook> phoneBooks = new ArrayList<>();
        List<PhoneBook> phoneBooks1 = manager.sliceQuery(PhoneBook.class).forSelect().withPartitionComponents(mobile.toLowerCase()).fromClusterings(countryCode).toClusterings(countryCode).get(5000);
        List<PhoneBook> phoneBooks2 = manager.sliceQuery(PhoneBook.class).forSelect().withPartitionComponents(mobile.toUpperCase()).fromClusterings(countryCode).toClusterings(countryCode).get(5000);
        if (null != phoneBooks1 && phoneBooks1.size() > 0) {
            phoneBooks.addAll(phoneBooks1);
        }

        if (null != phoneBooks2 && phoneBooks2.size() > 0) {
            phoneBooks.addAll(phoneBooks2);
        }

        reqMetricUtil.addReqStepInfo("query phone book in bach.", String.format("mobile:%s,size:%s", mobile, null != phoneBooks ? phoneBooks.size() : ""), startTime, System.currentTimeMillis());

        long lastCreateTimeFinal = lastCreatTimeReq;
        if (lastCreatTimeReq <= 0) {
            lastCreateTimeFinal = Long.MAX_VALUE;
        }

        List<RecommandUserWithOtherApp> recommandUserWithOtherApps = recommendFromOtherApps(userId, source);
        //本地通讯录和第三方可能认识的合并 排序和去除userId(时间小的数据)
        if ((null == phoneBooks || phoneBooks.size() < 1) && (null == recommandUserWithOtherApps && recommandUserWithOtherApps.size() < 1)) {
            result[0] = 0;
            result[1] = new ArrayList();
            return result;
        }

        Map<String, RecommandUserWithOtherApp> userIdRecommandMap = new HashMap<>();

        if (null != phoneBooks && phoneBooks.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[phone book] recommand user. userId:{},phonebooks:{}", userId, Arrays.toString(phoneBooks.toArray()));
            }
            FluentIterable.from(phoneBooks).forEach(phoneBook -> {
                String phoneBookUserId = phoneBook.getId().getUserId();
                RecommandUserWithOtherApp newRecommandUser = new RecommandUserWithOtherApp();
                newRecommandUser.setAppUserId(null);
                newRecommandUser.setUserId(phoneBookUserId);
                newRecommandUser.setSource(SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK);
                newRecommandUser.setUpdateTime(phoneBook.getCreateTime());
                if (userIdRecommandMap.containsKey(phoneBookUserId)) {
                    RecommandUserWithOtherApp recommandUser = userIdRecommandMap.get(phoneBookUserId);
                    Long updateTime = recommandUser.getUpdateTime();
                    if (phoneBook.getCreateTime() > updateTime) {
                        userIdRecommandMap.put(phoneBookUserId, newRecommandUser);
                    }
                } else {
                    userIdRecommandMap.put(phoneBookUserId, newRecommandUser);
                }
            });
        }

        if (null != recommandUserWithOtherApps && recommandUserWithOtherApps.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[recommand other app] recommand user. userId:{}, recommandUsers:{}", userId, Arrays.toString(recommandUserWithOtherApps.toArray()));
            }
            FluentIterable.from(recommandUserWithOtherApps).forEach(recommandUserWithOtherApp -> {
                String recommandUserId = recommandUserWithOtherApp.getUserId();
                if (userIdRecommandMap.containsKey(recommandUserId)) {
                    RecommandUserWithOtherApp recommandUser = userIdRecommandMap.get(recommandUserId);
                    Long updateTime = recommandUser.getUpdateTime();
                    if (recommandUserWithOtherApp.getUpdateTime() > updateTime) {
                        userIdRecommandMap.put(recommandUserId, recommandUserWithOtherApp);
                    }
                } else {
                    userIdRecommandMap.put(recommandUserId, recommandUserWithOtherApp);
                }
            });
        }

        if (null == userIdRecommandMap || userIdRecommandMap.size() < 1) {
            result[0] = 0;
            result[1] = new ArrayList();
            return result;
        }

        //排序 根据实际从高到低排序
        RecommandUserWithOtherApp[] recommandUserWithOtherAppList = userIdRecommandMap.values().toArray(new RecommandUserWithOtherApp[]{});

        Arrays.sort(recommandUserWithOtherAppList, (o1, o2) -> o1.getUpdateTime() - o2.getUpdateTime() > 0 ? -1 : 1);


        final Set<String> initRecommendIdList = userIdRecommandMap.keySet();


        //查找本人删除的人，便于过滤
        final List<String> removedFriendIdList = phoneBookService.findRemoveFriendIdList(userId);

        List<String> recommendUserIds = FluentIterable.from(initRecommendIdList).filter(s -> !removedFriendIdList.contains(s)).toList();

        //查找好友列表，便于过滤
        List<Friend> friendList = userService.findFriendsByUserId(userId, 0);         //我的好友列表

        if (null == friendList) {
            friendList = new ArrayList<>();
        }

        final Set<String> friendIdList = FluentIterable.from(friendList).transform(input -> input.getId().getFriendId()).filter(input -> input != null).toSet();

        //全部可能认识的人
        List<String> userIdList = null;
        if (null != recommendUserIds) {
            userIdList = FluentIterable.from(recommendUserIds).filter(input -> !friendIdList.contains(input)).toList();
        }

        if (null == userIdList || userIdList.size() < 1) {
            result[0] = 0;
            result[1] = new ArrayList();
            return result;
        }


        int maxCount = 20;
        List<String> responseUserIdList = new ArrayList();

        final Map<String, RecommandUserWithOtherApp> userIdRecommandInfoMap = new HashMap();

        int totalUser = 0;

        //在所有可能认识的人进行过
        //userIdList过滤完成后 最终的名单
        //如果加入时间相同 则可能超过返回的人数限制（避免分批显示时 多个时间相同的人 有部分用户返回不了）
        Long lastAddTime = Long.MAX_VALUE;
        for (RecommandUserWithOtherApp recommandUser : recommandUserWithOtherAppList) {
            if (recommandUser.getUpdateTime() >= lastCreateTimeFinal) {
                continue;
            }
            if (userIdList.contains(recommandUser.getUserId())) {
                totalUser++;
                if (recommandUser.getUpdateTime() != lastAddTime && responseUserIdList.size() == maxCount) {
                    continue;
                } else {
                    responseUserIdList.add(recommandUser.getUserId());
                    userIdRecommandInfoMap.put(recommandUser.getUserId(), recommandUser);
                    lastAddTime = recommandUser.getUpdateTime();
                }
            }
        }

        List<User> userList = userService.findByIdList(responseUserIdList);

        List<BaseUserVo> recommendFriends = null;
        if (null != userList && userList.size() > 0) {

            userList = userList.stream().filter(u -> (u.getAppId() == appID)).collect(Collectors.toList());


            recommendFriends = FluentIterable.from(userList).transform(input -> {
                BaseUserVo baseUserVo = BaseUserVo.createFromUser(input);
                baseUserVo.setCreateTime(userIdRecommandInfoMap.get(input.getId()).getUpdateTime());
                baseUserVo.setSource(userIdRecommandInfoMap.get(input.getId()).getSource());
                //设置第三方好友的第三方名称
                if ((!SystemConstants.FRIEND_SOURCE_CG.equalsIgnoreCase(baseUserVo.getSource())) &&
                        (!SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK.equalsIgnoreCase(baseUserVo.getSource()))
                        ) {
                    List<ThirdAppToFriend> thirdAppUser = manager.sliceQuery(ThirdAppToFriend.class).forSelect()
                            .withPartitionComponents(userIdRecommandInfoMap.get(input.getId()).getAppUserId(), baseUserVo.getSource())
                            .withConsistency(info.archinnov.achilles.type.ConsistencyLevel.LOCAL_QUORUM)
                            .get(1);
                    if (null != thirdAppUser && thirdAppUser.size() > 0) {
                        baseUserVo.setAppUserName(thirdAppUser.get(0).getAppUserName());
                    }
                }
                return baseUserVo;
            }).filter(input -> input != null).toList();
        }


        if (null == recommendFriends || recommendFriends.size() < 1) {
            result[0] = 0;
            result[1] = new ArrayList();
            return result;
        }


        result[0] = totalUser;
        result[1] = recommendFriends;
        return result;

    }


    private List<RecommandUserWithOtherApp> recommendFromOtherApps(String userId, String source) {
        List<RecommandUserWithOtherApp> recommandUserWithOtherApps = new ArrayList<>();
        try {
            Long startTimeLong = System.currentTimeMillis();
            if ("all".equalsIgnoreCase(source)) {
                //获取第三方可能认识的人
                Long startTime = System.currentTimeMillis();
                List<CgThirdAppFriend> thirdFriends = manager.sliceQuery(CgThirdAppFriend.class).forSelect()
                        .withPartitionComponents(userId)
                        .withConsistency(info.archinnov.achilles.type.ConsistencyLevel.LOCAL_QUORUM)
                        .get(200);
                reqMetricUtil.addReqStepInfo("query CgThirdAppFriend batch",
                        String.format("userId:%s,size:%s", userId, null != thirdFriends ? thirdFriends.size() : ""),
                        startTime, System.currentTimeMillis());

                if (null != thirdFriends && thirdFriends.size() > 0) {
                    Map<String, List<String>> sourceUserIds = new HashMap<>();
                    Map<String, Long> recommandAppUserIdUpdateTime = new HashMap<>();

                    thirdFriends.stream().forEach(thirdFriend -> {
                        sourceUserIds.putIfAbsent(thirdFriend.getKey().getSource(), new ArrayList<>());
                        sourceUserIds.get(thirdFriend.getKey().getSource()).add(thirdFriend.getKey().getAppUserId());
                        recommandAppUserIdUpdateTime.put(String.format("%s_%s", thirdFriend.getKey().getSource(),
                                thirdFriend.getKey().getAppUserId()), thirdFriend.getUpdateTime());
                    });

                    if (sourceUserIds.size() > 0) {
                        //对每一个第三方可能认识的人进行处理
                        sourceUserIds.entrySet().stream().forEach(entity -> {
                            String userSource = entity.getKey();
                            List<String> userIds = entity.getValue();
                            //第三方可能认识的人
                            //根据第三方的id 获取cg的id 并且对用户的创建时间进行过滤
                            Long startTime1 = System.currentTimeMillis();
                            List<ThirdAppCgUser> thirdAppCgUsers = manager.typedQuery(ThirdAppCgUser.class,
                                    select().from("third_app_users_cg").where(QueryBuilder.eq("type", userSource)).and(QueryBuilder.in("third_app_user_id", userIds.toArray()))
                                            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                            ).get();
                            reqMetricUtil.addReqStepInfo("query ThirdAppCgUser batch.",
                                    String.format("type:%s,userIdSize:%s", userSource, null != userIds ? userIds.size() : ""), startTime1, System.currentTimeMillis());
                            if (null != thirdAppCgUsers && thirdAppCgUsers.size() > 0) {
                                thirdAppCgUsers.stream().forEach(thirdAppCgUser -> {
                                    RecommandUserWithOtherApp recommandUserWithOtherApp = new RecommandUserWithOtherApp();
                                    recommandUserWithOtherApp.setUpdateTime(recommandAppUserIdUpdateTime.get(String.format("%s_%s", thirdAppCgUser.getKey().getType(), thirdAppCgUser.getKey().getThirdAppUserId())));
                                    recommandUserWithOtherApp.setAppUserId(thirdAppCgUser.getKey().getThirdAppUserId());
                                    recommandUserWithOtherApp.setSource(thirdAppCgUser.getKey().getType());
                                    recommandUserWithOtherApp.setUserId(thirdAppCgUser.getKey().getUserId());
                                    recommandUserWithOtherApps.add(recommandUserWithOtherApp);
                                });
                            }
                        });
                    }
                }
            }
            reqMetricUtil.addReqStepInfo("recommand from other app.",
                    String.format("source:%s", null != source ? source : "")
                    , startTimeLong, System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.error("fails to handle recommand users from third app.", e);
        }
        return recommandUserWithOtherApps;
    }


    public void addTwoFriend(User oppositeUser, User reqUser, String source, String msg) {
        boolean toIsFriend = isFriendOfUser(oppositeUser.getId(), oppositeUser.getAppId(), reqUser.getId());
        boolean fromIsFriend = isFriendOfUser(reqUser.getId(), reqUser.getAppId(), reqUser.getId());
        if (toIsFriend && fromIsFriend) {
            LOGGER.info("user=[{}] is alreay friend of user2=[{}] and also ...", reqUser.getId(), oppositeUser.getId());
            return;
        }
        makeFriend(reqUser, oppositeUser, null, source);
        makeFriend(oppositeUser, reqUser, null, source);
        messageQueueService.sendToUserContactSuccess(reqUser, oppositeUser);
    }


    public void addFriend(User oppositeUser, User reqUser, String source, String msg) {
        //请求方已经是接受方的好友
        if (isFriendOfUser(oppositeUser.getId(), oppositeUser.getAppId(), reqUser.getId())) {
            LOGGER.debug("user=[{}] is alreay friend of user2=[{}].", reqUser.getId(), oppositeUser.getId());
            //添加好友需要区分source
            makeFriend(reqUser, oppositeUser, null, source);
            messageQueueService.sendContactSuccess(reqUser, oppositeUser);
        }
        //接受方本地通讯录中含有请求方
        else if (isUserInPhoneBook(reqUser, oppositeUser)) {
            LOGGER.debug("user=[{}] in  user2=[{}] local address book.", reqUser.getId(), oppositeUser.getId());
            //添加好友需要区分source
            makeFriend(reqUser, oppositeUser, null, source);
            makeFriend(oppositeUser, reqUser, null, SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK);
            messageQueueService.sendContactSuccess(reqUser, oppositeUser);
        } else {
            ContactRequest contactRequest = getUserContactRequest(oppositeUser.getId(), reqUser.getId());
            //接受方已经发送过好友请求
            if (null != contactRequest) {
                //添加好友需要区分source
                String oppositeSource = contactRequest.getSource();
                LOGGER.debug(" user2=[{}] has sent add friend req to user1:[{}],source:[{}]", oppositeUser.getId(), reqUser.getId(), oppositeSource);

                makeFriend(reqUser, oppositeUser, null, source);
                makeFriend(oppositeUser, reqUser, null, StringUtils.isNotBlank(oppositeSource) ? oppositeSource : SystemConstants.FRIEND_SOURCE_CG);
                messageQueueService.sendContactSuccess(reqUser, oppositeUser);
            }
            //没有添加好友请求
            else {
                LOGGER.debug("user=[{}] send request to user=[{}].", reqUser.getId(), oppositeUser.getId());
                addToContactRequest(reqUser.getId(), oppositeUser.getId(), msg, source);
                if (msg == null) {
                    msg = "";
                }
                messageQueueService.sendContactRequest(reqUser, oppositeUser, msg);
            }
        }
    }

//    /**
//     * 添加好友
//     *
//     * @param from 添加人
//     * @param to   好友
//     */
//    private void addUserToFriend(User from, User to,String source) {
//        LOGGER.debug("add user {} to friends", to.getId());
//        addUserToFriend(from.getId(), from.getAppId(), to.getId());
//    }

//    /**
//     * @param from
//     * @param to
//     */
//    public void makeFriend(User from, User to) {
//        addUserToFriend(from, to);
//        addUserToFriend(to, from);
//
//        manager.sliceQuery(RemovedFriend.class).forDelete().withPartitionComponents(from.getId()).deleteMatching(to.getMobile());
//        manager.sliceQuery(RemovedFriend.class).forDelete().withPartitionComponents(to.getId()).deleteMatching(from.getMobile());
//
//
//    }

    /**
     * @param from
     * @param to
     */
    public void makeFriend(User from, User to, String contactName, String source) {
        addUserToFriend(from, to, contactName, source);

        manager.sliceQuery(RemovedFriend.class).forDelete().withPartitionComponents(from.getId()).deleteMatching(to.getMobile());

    }

//    /**
//     * 添加好友
//     *
//     * @param from  添加人
//     * @param appId appid
//     * @param to    好友
//     */
//    public void addUserToFriend(String from, Integer appId, String to) {
//        UserKey key = new UserKey(from, appId, to);
//        Friend friend = new Friend();
//        friend.setId(key);
//        friend.setResourceAppId(appId);
//        friend.setUpdateTime(System.currentTimeMillis());
//        userService.saveFriend(friend);
//    }

    /**
     * 添加好友
     *
     * @param fromUser 添加人
     * @param toUser   好友
     */
    private void addUserToFriend(User fromUser, User toUser, String contactName, String source) {
        String from = fromUser.getId();
        Integer appId = fromUser.getAppId();
        String to = toUser.getId();
        UserKey key = new UserKey(from, appId, to);
        Friend friend = new Friend();
        friend.setId(key);
        friend.setResourceAppId(appId);
        friend.setUpdateTime(System.currentTimeMillis());
        friend.setContactName(contactName);
        friend.setSource(source);
        userService.saveFriend(friend);

        try {
            //增加添加好友来源的话单统计
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("user_add_friend").append(LOG_SPLIT_CHAR)
                    .append(from).append(LOG_SPLIT_CHAR)
                    .append(fromUser.getSex()).append(LOG_SPLIT_CHAR)
                    .append(to).append(LOG_SPLIT_CHAR)
                    .append(toUser.getSex()).append(LOG_SPLIT_CHAR)
                    .append(contactName).append(LOG_SPLIT_CHAR)
                    .append(source);
            logUtil.logReq(stringBuilder.toString());
        } catch (Exception e) {
            //ignore
        }
    }

//    /**
//     * 添加好友
//     *
//     * @param from 添加人
//     * @param to   好友
//     */
//    public void addUserToFriendAsync(User from, User to) {
//        LOGGER.debug("add user {} to friends", to.getId());
//        addUserToFriend(from.getId(), from.getAppId(), to.getId());
//    }

//    /**
//     * 添加好友
//     *
//     * @param from  添加人
//     * @param appId appid
//     * @param to    好友
//     */
//    public void addUserToFriendAsync(String from, Integer appId, String to) {
//        UserKey key = new UserKey(from, appId, to);
//        Friend friend = new Friend();
//        friend.setId(key);
//        friend.setResourceAppId(appId);
//        friend.setUpdateTime(System.currentTimeMillis());
//        userService.saveFriendAsync(friend);
//    }

    /**
     * 添加好友
     *
     * @param fromUser 添加人
     * @param appId    appid
     * @param to       好友
     */
    public void addUserToFriendAsync(User fromUser, Integer appId, String to, String contactName, String source) {
        UserKey key = new UserKey(fromUser.getId(), appId, to);

        Friend friend = userService.findFriendInfo(key);
        if (null != friend) {
            String friendContactName = friend.getContactName();
            if (StringUtils.isBlank(friendContactName)) {
                friend.setContactName(contactName);
                friend.setSource(source);
                userService.updateFriend(friend);
                LOGGER.info("from:{}.appId:{},to:{}.contactName:{}. be friends already and update contact name", friend, appId, to, contactName);
            }
        } else {
            friend = new Friend();
            friend.setId(key);
            friend.setResourceAppId(appId);
            friend.setUpdateTime(System.currentTimeMillis());
            friend.setContactName(contactName);
            friend.setSource(source);
            userService.saveFriendAsync(friend);

            try {
                //增加添加好友来源的话单统计
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                        .append("user_add_friend").append(LOG_SPLIT_CHAR)
                        .append(fromUser.getId()).append(LOG_SPLIT_CHAR)
                        .append(fromUser.getSex()).append(LOG_SPLIT_CHAR)
                        .append(to).append(LOG_SPLIT_CHAR)
                        .append("").append(LOG_SPLIT_CHAR)
                        .append(contactName).append(LOG_SPLIT_CHAR)
                        .append(source);
                logUtil.logReq(stringBuilder.toString());
            } catch (Exception e) {
                //ignore
            }
        }
    }

//    /**
//     * 将请求加入到请求表中
//     *
//     * @param from 添加人
//     * @param to   好友
//     */
//    public void addToContactRequest(User from, User to, String msg, String source) {
//        addToContactRequest(from.getId(), to.getId(), msg, source);
//    }

    /**
     * 将请求加入到请求表中
     *
     * @param from 添加人
     * @param to   好友
     */
    public void addToContactRequest(String from, String to, String msg, String source) {
        friendRequestCountIncrement(from, to);
        ContactRequestKey key = new ContactRequestKey(from, to);
        ContactRequest request = new ContactRequest(key);
        request.setLastTime(System.currentTimeMillis());
        request.setMsg(msg);
        request.setSource(source);
        manager.insert(request);
    }

    /**
     * 查找好友请求列表对应项
     *
     * @param key 对应项主键
     * @return 好友请求对应项
     */
    public ContactRequest findContactRequest(ContactRequestKey key) {
        return manager.find(ContactRequest.class, key);
    }

    /**
     * 判断用户是否是另一个用户的好友
     *
     * @param user       用户
     * @param toTestUser 待判断的用户
     * @return 结果
     */
    public boolean isFriendOfUser(String user, Integer appId, String toTestUser) {
        UserKey key = new UserKey(user, appId, toTestUser);
        return userService.findFriendInfo(key) != null;
    }

    /**
     * 判断用户是否在另一个用户的请求列表中
     *
     * @param user       用户
     * @param toTestUser 待判断的用户
     * @return 结果
     */
    public boolean isUserInContactRequestList(User user, User toTestUser) {
        return getUserContactRequest(user.getId(), toTestUser.getId()) != null;
    }

    /**
     * 判断用户是否在另一个用户的请求列表中
     *
     * @param userId       用户Id
     * @param toTestUserId 待判断的用户Id
     * @return 结果
     */
    public boolean isUserInContactRequestList(String userId, String toTestUserId) {
        ContactRequestKey key = new ContactRequestKey(userId, toTestUserId);
        return findContactRequest(key) != null;
    }

    public ContactRequest getUserContactRequest(String userId, String toTestUserId) {
        ContactRequestKey key = new ContactRequestKey(userId, toTestUserId);
        return findContactRequest(key);
    }


    /**
     * 判断自己是否被指定用户请求过或已经加入到了好友列表
     *
     * @param self       自己
     * @param toTestUser 指定用户
     * @return 结果
     */
    public boolean isUserSentRequestOrFriend(User self, User toTestUser) {
        return isFriendOfUser(toTestUser.getId(), toTestUser.getAppId(), self.getId()) || isUserInContactRequestList(toTestUser, self) || isUserInPhoneBook(self, toTestUser);
    }

    /**
     * 判断自己是否在指定用户的通讯录里
     *
     * @param self       自己
     * @param toTestUser 指定用户
     * @return 结果
     */
    public boolean isUserInPhoneBook(User self, User toTestUser) {
        return phoneBookService.findUserPhoneBook(self.getCountrycode(), self.getMobile(), toTestUser.getId()) != null;
    }

    /**
     * 用户的好友被请求数+1
     *
     * @param promoter 请求人
     * @param toUser   被请求人，计数+1的人
     */
    public void friendRequestCountIncrement(String promoter, String toUser) {
        if (isUserInContactRequestList(promoter, toUser)) {
            return;
        }
        FriendRequestCounter friendRequestCounter = manager.find(FriendRequestCounter.class, toUser);
        Date date = new Date(System.currentTimeMillis());
        String dateString = requestDateFormatter.format(date);
        if (friendRequestCounter == null) {
            friendRequestCounter = new FriendRequestCounter();
            friendRequestCounter.setUserId(toUser);
            friendRequestCounter.setCount(1);
            friendRequestCounter.setPushDate(dateString);
            manager.insert(friendRequestCounter);
        } else {
            friendRequestCounter.setCount(friendRequestCounter.getCount() + 1);
            friendRequestCounter.setPushDate(dateString);
            manager.update(friendRequestCounter);
        }
    }

    /**
     * 清空用户的好友请求数
     *
     * @param user 用户
     */
    public void clearFriendRequest(User user) {
        FriendRequestCounter friendRequestCounter = manager.find(FriendRequestCounter.class, user.getId());
        if (friendRequestCounter != null) {
            friendRequestCounter.setCount(0);
            Date date = new Date(System.currentTimeMillis());
            String dateString = requestDateFormatter.format(date);
            friendRequestCounter.setPushDate(dateString);
            manager.update(friendRequestCounter);
        }
    }


    /**
     * 判断fromUser是否为toUser可能认识的人
     *
     * @param fromUser
     * @param toUser
     * @return
     * @see List<BaseUserVo> recommend(String userId, String mobile, String countryCode, final long lastCreatTimeReq)
     */
    public boolean isRecommand(String fromUser, String toUser) {
        boolean result = true;
        //查找toUser删除的人，便于过滤
        final List<String> removedFriendIdList = phoneBookService.findRemoveFriendIdList(toUser);
        if (null != removedFriendIdList && removedFriendIdList.contains(fromUser)) {
            LOGGER.debug("{} in {} removed friend list.", fromUser, toUser);
            result = false;
            return result;
        }

        //查找好友列表，便于过滤
        List<Friend> friendList = userService.findFriendsByUserId(toUser, 0);
        if (null != friendList && friendList.contains(fromUser)) {
            LOGGER.debug("{} in {} friend list.", fromUser, toUser);
            result = false;
            return result;
        }

        return result;
    }

    /**
     * 新增的第三方好友通知
     *
     * @param user
     * @param source
     */
    public void handleOtherAppNewRecommendFriends(User user, String source) {
        //获取第三方的好友
        CgThirdAppUserKey key = new CgThirdAppUserKey();
        key.setType(source);
        key.setUserId(user.getId());

        CgThirdAppUser cgThirdAppUser = manager.find(CgThirdAppUser.class, key);
        if (null == cgThirdAppUser) {
            LOGGER.warn("user:{} . source:{} has no app user info", user, source);
            return;
        }

        ThirdAppCgUserKey thirdAppCgUserKey = new ThirdAppCgUserKey();
        thirdAppCgUserKey.setType(source);
        thirdAppCgUserKey.setThirdAppUserId(cgThirdAppUser.getThirdAppUserId());
        thirdAppCgUserKey.setUserId(user.getId());
        manager.sliceQuery(ThirdAppCgUser.class).forSelect().withPartitionComponents(new Object[]{21, 32}).limit(1);
        ThirdAppCgUser thirdAppCgUser = manager.find(ThirdAppCgUser.class, thirdAppCgUserKey);
        if (null == thirdAppCgUser) {
            LOGGER.warn("user:{} . source:{} has no app user info", user, source);
            return;
        }

        //获取该用户的第三方好友
        List<ThirdAppRecommendUser> thirdAppUsers;
        try {
            thirdAppUsers = friendRecommendService.getRecommendFromThirdApp(thirdAppCgUser);
        } catch (Exception e) {
            LOGGER.warn(String.format("userId:{} source:{} fails to get user friends.", user, source), e);
            return;
        }

        if (null == thirdAppUsers || thirdAppUsers.size() < 1) {
            return;
        }

        //查找新增的用户
        ThirdAppToFriendsKey toFriendKey = new ThirdAppToFriendsKey();
        toFriendKey.setThirdAppUserId(thirdAppCgUser.getKey().getThirdAppUserId());
        toFriendKey.setSource(thirdAppCgUser.getKey().getType());

        //用户之前的第三方好友
        List<CgThirdAppFriend> oldFriends = new ArrayList(manager.typedQuery(CgThirdAppFriend.class, select().from("cg_third_app_friend").where(eq("userId", user.getId())).and(eq("source", thirdAppCgUser.getKey().getType()))).get());

        final Map<String, Boolean> oldFriendIds = new HashMap<>();
        if (null != oldFriends) {
            oldFriends.stream().forEach(thirdAppToFriend -> oldFriendIds.put(thirdAppToFriend.getKey().getAppUserId(), true));
        }

        //新的好友关系入库
        saveThirdAppFriends(user, thirdAppUsers, source);

        //查找新增的好友
        for (ThirdAppRecommendUser thirdAppRecommendUser : thirdAppUsers) {
            if (!oldFriendIds.containsKey(thirdAppRecommendUser.getAppUserId())) {
                //新增的第三方好友是否为cg用户

                List<ThirdAppCgUser> thirdAppCgUsers = new ArrayList(manager.typedQuery(ThirdAppCgUser.class, select().from("third_app_users_cg").where(eq("type", source)).and(eq("third_app_user_id", thirdAppRecommendUser.getAppUserId()))).get());
                ThirdAppCgUser thirdAppCgUser1 = null;

                if (null != thirdAppCgUsers && thirdAppCgUsers.size() > 0) {
                    thirdAppCgUser1 = thirdAppCgUsers.get(0);
                }

                if (null != thirdAppCgUser1) {
                    if (isRecommand(thirdAppCgUser1.getKey().getUserId(), user.getId())) {
                        //发送系统通知
                        LOGGER.debug("user1:{}, user2:{} source:{} recommand.", thirdAppCgUser1.getKey().getUserId(), user.getId(), source);
                        NotifyMessage notifyMessage = new NotifyMessage();
                        notifyMessage.setType(NotifyMsgType.COMMAND_PEOPLE_YOU_MAY_KNOWN);
                        notifyMessage.setAckFlag(true);
                        notifyMessage.setCmsgId(UUID.randomUUID().toString());
                        notifyMessage.setFrom(SystemConstants.SYSTEM_ACCOUNT_SECRETARY);
                        notifyMessage.setTo(user.getId());
                        notifyMessage.setData(new NewPeopleMayKnownNotifyData(source));
                        notifyMessage.setIncrOfflineCount(false);
                        notifyMessage.setPushFlag(false);
                        messageQueueService.sendSysMsg(notifyMessage);
                        break;
                    } else {
                        LOGGER.debug("user1:{}, user2:{} source:{} not recommand.", thirdAppCgUser1.getKey().getUserId(), user.getId(), source);
                    }
                }
            }
        }
    }

    /**
     * 用户的第三方好友入库
     * 保存正反关系
     *
     * @param currentUser
     * @param thirdAppUsers
     * @param source
     */
    public void saveThirdAppFriends(User currentUser, List<ThirdAppRecommendUser> thirdAppUsers, String source) {
        final Batch batch = manager.createBatch();
        batch.startBatch();
        thirdAppUsers.forEach(thirdAppRecommendUser -> {
            ThirdAppToFriendsKey key = new ThirdAppToFriendsKey();
            key.setSource(source);
            key.setThirdAppUserId(thirdAppRecommendUser.getAppUserId());
            key.setUserId(currentUser.getId());

            ThirdAppToFriend friend = new ThirdAppToFriend();
            friend.setAppId(currentUser.getAppId());
            friend.setAppUserName(thirdAppRecommendUser.getAppUserName());
            friend.setKey(key);

            batch.insertOrUpdate(friend);

        });
        batch.endBatch();
        batch.cleanBatch();

        final Batch batch1 = manager.createBatch();
        batch1.startBatch();
        thirdAppUsers.forEach(thirdAppRecommendUser -> {
            CgThirdAppFriendKey key = new CgThirdAppFriendKey();
            key.setSource(source);
            key.setUserId(currentUser.getId());
            key.setAppUserId(thirdAppRecommendUser.getAppUserId());

            CgThirdAppFriend friend = new CgThirdAppFriend();
            friend.setKey(key);
            friend.setUpdateTime(System.currentTimeMillis());
            batch1.insertOrUpdate(friend);

        });
        batch1.endBatch();
        batch1.cleanBatch();
    }


}
