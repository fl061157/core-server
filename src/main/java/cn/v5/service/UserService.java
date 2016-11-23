package cn.v5.service;


import cn.v5.bean.UserLoginEvent;
import cn.v5.cache.CacheService;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.constant.RedisCacheKey;
import cn.v5.entity.*;
import cn.v5.entity.game.Account;
import cn.v5.entity.game.UserReport;
import cn.v5.entity.game.billboard.GameScore;
import cn.v5.entity.thirdapp.CgThirdAppUser;
import cn.v5.entity.thirdapp.CgThirdAppUserKey;
import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.thirdapp.ThirdAppCgUserKey;
import cn.v5.entity.vo.UserVo;
import cn.v5.metric.LogUtil;
import cn.v5.util.*;
import cn.v5.web.controller.ServerException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.CounterBuilder;
import info.archinnov.achilles.type.IndexCondition;
import info.archinnov.achilles.type.OptionsBuilder;
import org.apache.commons.lang.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.v5.metric.LogUtil.LOG_SPLIT_CHAR;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Service
public class UserService implements InitializingBean {
    private static String FOLLOWERS_KEY = "|FOLLOWERS";
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    @Qualifier("opManager")
    private PersistenceManager opManager;

    @Inject
    private ConversationService conversationService;

    @Inject
    private PhoneBookService phoneBookService;

    @Inject
    private TaskService taskService;

    @Inject
    private MessageQueueService messageQueueService;

    @Autowired
    private MessageSourceService messageSourceService;

    @Inject
    private TableCountService tcService;

    @Inject
    private SmsService smsService;

    @Autowired
    private RequestUtils requestUtils;

    @Inject
    private HttpService httpService;

    @Value("${dispatch.tcpServer}")
    private String dispatchTcpServer;

    @Value("${dispatch.tcpServerV2}")
    private String dispatchTcpServerV2;

    @Value("${idc.multi.test}")
    private String idcMultiTest;

    @Value("${dudu.user.id}")
    private String duduId;

    @Value("${hide.send.time}")
    private String hideSendTime;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${cdn.url}")
    private String cdnUrl;

    @Value("${send.sms.second}")
    private int sendSmsSecond;

    @Value("${user.avatar.default}")
    private String defaultAvatars;

    @Value("${account.region}")
    private String region;

    @Value("${account.offset}")
    private Long offset;

    @Value("${tcp.server.default}")
    private String tcpServerDefault;

    @Inject
    private MobileUtils mobileUtils;

    @Autowired
    private MessageQueueService queueService;

    @Autowired
    private SystemCmdService systemCmdService;

    @Autowired
    private UserCallSmSendService callSmSendService;

    @Autowired
    private UserCallSmSendService smSendService;

    @Autowired
    private PhoneFormatUtil phoneFormatUtil;

    @Autowired
    private DelayTaskService delayTaskService;


    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Autowired
    private PhoneUtil phoneUtil;
    @Autowired
    private LogUtil logUtil;

    @Autowired
    private ReqMetricUtil reqMetricUtil;


    private PreparedStatement pstmtFriend;

    private static final int BOARDNUM = 20;

    private final static String selectScoreInStmt = "select * from game_score where uid in ? and app_id=? and date=?";

    private Select.Where selectScoreInStmt(List<String> uid, Object appId, Object date) {
        return select().from("game_score").where(in("uid", uid.toArray())).and(eq("app_id", appId)).and(eq("date", date));
    }

    private final static String selectUserInStmt = "select * from users where id in ?";

    private Select.Where selectUserInStmt(List idList) {
        Object[] arrays = new Object[]{};
        if (idList != null) {
            arrays = idList.toArray();
        }
        return select().from("users").where(in("id", arrays));
    }

    private Function<Friend, User> frindsToUser = new Function<Friend, User>() {
        @Override
        public User apply(Friend input) {
            User user = findById(0, input.getId().getFriendId());
            return user;
        }
    };

    private final static Function<Friend, String> friendsIdTransformer = new Function<Friend, String>() {
        @Override
        public String apply(Friend input) {
            return input.getId().getFriendId();
        }
    };

    private final static Function<GameScore, String> scoreToIdTransformer = new Function<GameScore, String>() {
        @Override
        public String apply(GameScore input) {
            return input.getKey().getUid();
        }
    };

    private Function<MobileIndex, User> findMobileIndexToUser = new Function<MobileIndex, User>() {
        @Override
        public User apply(MobileIndex input) {
            User user = findById(0, input.getUserId());
            return user;
        }
    };


    @Override
    public void afterPropertiesSet() throws Exception {
        pstmtFriend = manager.getNativeSession().prepare("insert into friends(user_id, app_id, friend_id, update_time, contact_name, resource_app_id) values(?,?,?,?,?,?)");
    }

    /**
     * 根据登录会话token获得用户信息
     *
     * @param sessionId
     * @return
     */
    public User authorize(String sessionId, int appId) {
        PersistenceManager realManager = appId < SystemConstants.CG_APP_ID_MAX ? manager : opManager;
        if (StringUtils.isBlank(sessionId)) {
            return null;
        }
        UserSession session = realManager.find(UserSession.class, sessionId);
        if (session == null) {
            return null;
        }
        User user = realManager.find(User.class, session.getUserId());
        if (null == user) {
            log.warn("sessionId:{} has no correct user.", sessionId);
            return user;
        }

        UserSessionIndex latestSession = findUserSessionIndexByKey(user.getId(), user.getAppId());
        if (latestSession != null && latestSession.getSessionId().equals(sessionId)) {
            //放到redis
//            saveUserSessionToRedis(user, sessionId, session.getAppId());
            return user;
        } else {
            log.warn("clean old session {}.latestSession:{}. userId:{}", sessionId, null != latestSession ? latestSession.getSessionId() : "", user.getId());
            cleanUserDataByUnusedSession(sessionId, appId);
            return null;
        }
    }


    /**
     * 查询某手机号是否注册
     *
     * @param mobile
     * @return
     */
    public MobileIndex isExistMobile(String mobile, String countrycode) {
        return isExistMobile(mobile, countrycode, 0);
    }

    public MobileIndex isExistMobile(String mobile, String countrycode, Integer appId) {
        if (mobile == null) {
            return null;
        }
        MobileIndex mobileIndex = manager.find(MobileIndex.class, new MobileKey(StringUtil.combinedMobileKey(mobile, appId), countrycode));
        return mobileIndex;
    }

    public Account isExistUserName(String userName, Integer appId) {
        return manager.find(Account.class, userName);
    }

    public Account isExistUserById(String fbId, String prefix) {
        List<Account> accounts = manager.indexedQuery(Account.class, new IndexCondition(prefix + "_id", prefix + "_" + fbId)).get();
        if (accounts.size() == 0) {
            return null;
        } else if (accounts.size() == 1) {
            return accounts.get(0);
        } else {
            return accounts.get(0);
        }
    }

    public void addAccount(String userName, String passwd, String userId, Integer appId, String prefix, String oauthId) {
        Account account = new Account(userName);
        account.setPassword(BCrypt.hashpw(passwd, BCrypt.gensalt(10)));
        account.setUserId(userId);
        ReflectUtil.setFieldValue(account, prefix, String.class, oauthId);
        manager.insert(account);
    }

    public void addAccount(String userName, String passwd, String userId, Integer appId) {
        Account account = new Account(userName);
        account.setPassword(BCrypt.hashpw(passwd, BCrypt.gensalt(10)));
        account.setUserId(userId);
        manager.insert(account);
    }

    /**
     * 跟进用户id列表返回用户信息列表
     *
     * @param memberIds
     * @return
     */
    public List<User> findUserListByNames(Collection<String> memberIds) {
        if (null == memberIds) {
            return new ArrayList<>();
        }
        return findByIdList(FluentIterable.from(memberIds).filter(input -> input != null).toList());
    }

    /**
     * 根据手机号码返回用户
     *
     * @param mobile
     * @return
     */
    public MobileIndex findMobileIndex(String mobile, String countrycode, Integer appId) {
        //手机号码加密后为小写
        if (StringUtils.isBlank(mobile)) {
            return null;
        }
        return manager.find(MobileIndex.class, new MobileKey(StringUtil.combinedMobileKey(mobile, appId), countrycode));
    }

    public MobileIndex findMobileIndex(String mobile, String countrycode) {
        return findMobileIndex(mobile, countrycode, 0);
    }

    public List<MobileIndex> findMobileIndexList(Collection<String> mobiles, Map<String, String> mobileCodeMap, Integer appId) {
        String[] mobileArray = mobiles.stream().map(m -> StringUtil.combinedMobileKey(m, appId)).toArray(String[]::new);
        List<MobileIndex> indexList = manager.typedQuery(MobileIndex.class,
                select().from("user_mobiles").where(in("mobile", mobileArray))
                        .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)).get();
        List<MobileIndex> result = Lists.newArrayList();
        for (MobileIndex index : indexList) {
            MobileKey key = index.getMobileKey();
            String expectCode = mobileCodeMap.get(key.getMobile());
            if (key.getCountrycode().equals(expectCode)) {
                result.add(index);
            }
        }
        return result;
    }

    public List<MobileIndex> findMobileIndexList(Collection<String> mobiles, Map<String, String> mobileCodeMap) {
        return findMobileIndexList(mobiles, mobileCodeMap, 0);
    }

    public List<User> findUserByMobileIndex(String mobile) {
        return findUserByMobileIndex(mobile, 0);
    }

    public List<User> findUserByMobileIndex(String mobile, Integer appId) {
        List<MobileIndex> listMobiles = manager.sliceQuery(MobileIndex.class).forSelect()
                .withPartitionComponents(StringUtil.combinedMobileKey(mobile, appId)).get();
        if (null == listMobiles) {
            return new ArrayList<>();
        }
        return FluentIterable.from(listMobiles).transform(findMobileIndexToUser).filter(input -> input != null).toList();
    }

    /**
     * 根据用户id获得用户
     *
     * @param id
     * @return
     */
    public User findById(int appId, String id) {
        PersistenceManager realManager = getRealManager(appId);
        return realManager.find(User.class, id);
    }

    public User findById(String id) {
        PersistenceManager realManager = CurrentUser.db();
        return realManager.find(User.class, id);
    }

    public List<User> findByIdList(List<String> idList) {
        Long startTime = System.currentTimeMillis();
        List<User> result = new ArrayList<>(CurrentUser.db().typedQuery(User.class, select().from("users").where(in("id", idList.toArray()))
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)).get());
        reqMetricUtil.addReqStepInfo("find user info by idlist", String.format("idlist size:%s", null != idList ? idList.size() : ""), startTime, System.currentTimeMillis());
        return result;
    }


    /**
     * String[]
     * map.put("tcp_server", ips);
     * map.put("file_server", ips);
     *
     * @param regionCode
     * @param ip
     * @return
     */

    public Map<String, String[]> findServerAddress(String regionCode, String ip) {
        if (log.isDebugEnabled()) {
            log.debug("[findServerAddress],ip:{}, regionCode:{}", ip, regionCode);
        }
        Map<String, String> params = Maps.newHashMap();
        params.put("ip", ip);
        params.put("countryCode", regionCode);
        if (log.isDebugEnabled()) {
            log.debug("[findServerAddress],dispatch.tcpServer:{}", dispatchTcpServerV2);
        }
        String res = null;
        try {
            res = this.httpService.doGet(dispatchTcpServerV2, params);
            if (log.isDebugEnabled()) {
                log.debug("[findServerAddress],res:{}", res);
            }
        } catch (Exception e) {
            log.error(String.format("fals to get tcpServer,server:%s", dispatchTcpServerV2), e);
        }


        Map<String, String[]> ret = null;
        if (StringUtils.isNotBlank(res)) {
            try {
                ret = JSON.parseObject(res, new TypeReference<Map<String, String[]>>() {
                });
            } catch (Exception e) {
                log.error("[findServerAddress] error res:{}", res);
            }
        }

        if (ret == null || ret.size() == 0) {
            log.error(String.format("cat not get tcp server. server:%s. use default tcp server:%s", dispatchTcpServerV2, tcpServerDefault));
            String[] tcpIPs = JSON.parseObject(tcpServerDefault, String[].class);
            ret = Maps.newHashMap();
            ret.put("tcp_server", tcpIPs);
            ret.put("file_server", tcpIPs);
        }

        return ret;
    }


    public Map<String, String[]> findServerAddress(User user, String ip) {
        return findServerAddress(user.getCountrycode(), ip);
    }


    public String findServerAddr(String countryCode, String ip) {

        if (log.isDebugEnabled()) {
            log.debug("[findServerAddr],ip:{}", ip);
        }
        Map<String, String> params = Maps.newHashMap();
        params.put("ip", ip);
        params.put("countryCode", countryCode);
        if (log.isDebugEnabled()) {
            log.debug("[findServerAddr],dispatch.tcpServer:{}", dispatchTcpServer);
        }
        String res = null;
        try {
            res = this.httpService.doGet(dispatchTcpServer, params);
            if (log.isDebugEnabled()) {
                log.debug("[findServerAddr],res:{}", res);
            }
        } catch (Exception e) {
            log.error(String.format("fals to get tcpServer,server:%s", dispatchTcpServer), e);
        }

        if (StringUtils.isBlank(res)) {
            log.error(String.format("cat not get tcp server. server:%s. use default tcp server:%s", dispatchTcpServer, tcpServerDefault));
            res = tcpServerDefault;
        }
        return res;
    }

    public String findServerAddr(User user, String ip) {
        return findServerAddr(user.getCountrycode(), ip);
    }

    /**
     * 根据用户id和app id获得用户会话
     *
     * @param user_id
     * @param app_id
     * @return
     */
    public UserSessionIndex findUserSessionIndexByKey(String user_id, Integer app_id) {
        log.debug("find user session. userId:{},appId:{}", user_id, app_id);
        PersistenceManager realManager = getRealManager(app_id);
        return realManager.find(UserSessionIndex.class, new UserSessionIndexKey(user_id, app_id));
    }

    /**
     * 删除用户会话信息
     *
     * @param session_id
     */
    public void removeUserSession(String session_id, int appId) {
        log.debug("remove user session from UserSession. session_id:{},appId:{}", session_id, appId);
        PersistenceManager realManager = getRealManager(appId);
        realManager.deleteById(UserSession.class, session_id);
    }

    /**
     * 生成会话
     *
     * @param user_id
     * @param app_id
     * @param session_id
     * @return
     */
    public UserSessionIndex saveUserSessionIndex(String user_id, Integer app_id, String session_id) {
        UserSessionIndex userSessionIndex = new UserSessionIndex();
        userSessionIndex.setSessionId(session_id);
        userSessionIndex.setId(new UserSessionIndexKey(user_id, app_id));
        PersistenceManager realManager = getRealManager(app_id);
        return realManager.insert(userSessionIndex);
    }

    /**
     * 保存好友
     *
     * @param friend
     * @return
     */
    public Friend saveFriend(Friend friend) {
        UserKey id = friend.getId();
        manager.insert(new Friend(new UserKey(id.getFriendId() + FOLLOWERS_KEY, friend.getId().getAppId(), friend.getId().getUserId()), ""));
        return manager.insert(friend);
    }

    public void saveFriendAsync(Friend friend) {
        UserKey id = friend.getId();
        Session session = manager.getNativeSession();
        session.executeAsync(new BoundStatement(pstmtFriend).bind(id.getFriendId() + FOLLOWERS_KEY, id.getAppId(), id.getUserId(), System.currentTimeMillis(), "", friend.getResourceAppId())
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM));
        session.executeAsync(new BoundStatement(pstmtFriend).bind(id.getUserId(), id.getAppId(), id.getFriendId(), System.currentTimeMillis(), "", friend.getResourceAppId())
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM));
    }


    public void updateFriend(Friend friend) {
        manager.insertOrUpdate(friend);
    }

    /**
     * 删除好友关系
     *
     * @param app_id
     * @param user
     * @param friendUser
     */
    public void removeFriend(Integer app_id, User user, User friendUser) {
        Friend friend = findFriendInfo(new UserKey(user.getId(), (app_id == null) ? 0 : app_id, friendUser.getId()));
        if (friend != null && friendUser.getUserType() == 0) { //系统好友不给删除
            UserKey userKey = friend.getId();
            manager.deleteById(Friend.class, new UserKey(userKey.getFriendId() + FOLLOWERS_KEY, userKey.getAppId(), userKey.getUserId()));
            manager.deleteById(Friend.class, userKey);
            manager.deleteById(ContactRequest.class, new ContactRequestKey(user.getId(), friendUser.getId()));
            manager.deleteById(ContactRequest.class, new ContactRequestKey(friendUser.getId(), user.getId()));
            addFriendToRemovedList(user, friendUser);

            try {
                //增加删除好友来源的话单统计
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                        .append("user_remove_friend").append(LOG_SPLIT_CHAR)
                        .append(user.getId()).append(LOG_SPLIT_CHAR)
                        .append(user.getSex()).append(LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(user.getAccount())).append(LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(user.getMobilePlaintext())).append(LOG_SPLIT_CHAR)
                        .append(friendUser.getId()).append(LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(friendUser.getAccount())).append(LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(friendUser.getMobilePlaintext()));
                logUtil.logReq(stringBuilder.toString());
            } catch (Exception e) {
                //ignore
            }
        }

    }


    /**
     * 查询好友
     *
     * @param id
     * @return
     */
    public Friend findFriendInfo(UserKey id) {
        Friend friend = manager.find(Friend.class, id);
        return friend;
    }

    /**
     * 根据给定的用户返回此用户的好友列表,返回对象是Friend
     *
     * @param userId
     * @param appId
     * @return
     */
    public List<Friend> findFriendsByUserId(String userId, Integer appId) {
        return manager.sliceQuery(Friend.class).forSelect().withPartitionComponents(userId, appId).get();
    }

    public Map<String, Integer> addRobotInConversations(Map<String, Integer> con) {
        con.put(duduId, 2);
        return con;
    }

    public void excludeBlackFriends(List<Friend> friends, Map<String, Integer> conversations) {
        Iterator<Friend> iter = friends.iterator();
        while (iter.hasNext()) {
            Friend f = iter.next();
            Integer type = conversations.get(f.getId().getFriendId());
            if (type != null && type.intValue() == 2) {
                log.debug("friend {} in black list", f.getId().getFriendId());
                iter.remove();
            }
        }
    }

    public void excludeBlackGameScores(List<GameScore> gameScores, Map<String, Integer> conversations) {
        Iterator<GameScore> iter = gameScores.iterator();
        while (iter.hasNext()) {
            GameScore f = iter.next();
            Integer type = conversations.get(f.getKey().getUid());
            if (type != null && type.intValue() == 2) {
                log.debug("friend {} in black list", f.getKey().getUid());
                iter.remove();
            }
        }
    }

    public List<Friend> findFollowers(String userId, Integer appId) {
        return manager.sliceQuery(Friend.class).forSelect().withPartitionComponents(userId + FOLLOWERS_KEY, appId).get();
    }

    /**
     * 根据给定的用户返回此用户的好友列表
     *
     * @param userId
     * @param appId
     * @return
     */
    public List<UserVo> findFriendUserList(final User currentUser, String userId, Integer appId) {
        List<Friend> friends = findFriendsByUserId(userId, appId);
        final Map<String, Friend> friendMap = Maps.uniqueIndex(friends, new Function<Friend, String>() {
            @Override
            public String apply(Friend input) {
                return input.getId().getFriendId();
            }
        });

        final Map<String, Conversation> conversationMap = conversationService.findByUserId(userId);
        if (null == friends) {
            friends = new ArrayList<>();
        }

        List<String> idList = FluentIterable.from(friends).transform(new Function<Friend, String>() {
            @Override
            public String apply(Friend input) {
                return input.getId().getFriendId();
            }
        }).filter(input -> input != null).toList();


        List<String> fIdList = new ArrayList<>(idList);

        if (!fIdList.contains(duduId)) {
            addRobotToFriend(currentUser.getId(), appId);
            fIdList.add(0, duduId);
        }


        List<User> userList = findByIdList(fIdList);
        if (null == userList) {
            userList = new ArrayList<>();
        }

        return FluentIterable.from(userList).transform(user -> {
            UserVo vo = UserVo.createFromUser(user);
            Conversation c = conversationMap.get(user.getId());
            vo.setConversation(c == null ? 0 : c.getType());
            Friend friendInfo = friendMap.get(user.getId());
            vo.setContactName(friendInfo == null ? null : friendInfo.getContactName());
            if (SystemConstants.SYSTEM_ACCOUNT_SECRETARY.equals(user.getId())) {
                vo.setContactName("");
                vo.setNickname(messageSourceService.getMessageSource(currentUser.getAppId()).getMessage("secretary.name", new Object[]{}, currentUser.getLocale()));
            }

            //兼容Andriod 1.x版本，如果没有该字段，则Andriod客户端无法登陆
            if (null == vo.getNickname()) {
                vo.setNickname("");
            }
            String source = friendInfo.getSource();
            vo.setSource(StringUtils.isNotBlank(source) ? source : SystemConstants.FRIEND_SOURCE_CG);
            return vo;
        }).filter(input -> input != null).toList();


    }

    /**
     * 查询chatgame好友和指定resourceAppId的好友
     *
     * @param userId
     * @param appId
     * @param resourceAppId
     * @return
     */
    public List<User> findFriendUserList(String userId, Integer appId, Integer resourceAppId) {
        List<Friend> friends = findFriendsByUserId(userId, appId);
        // TODO: 因为resource_app_id是新增字段，所以默认是null，所以先直接去除该字段不是null且不为resource_app_id的项
        List<Friend> newFriends = Lists.newArrayList();
        for (Friend f : friends) {
            if (f.getResourceAppId() == null || f.getResourceAppId().equals(resourceAppId)) {
                newFriends.add(f);
            }
        }
        final Map<String, Conversation> conversationMap = conversationService.findByUserId(userId);

        return FluentIterable.from(newFriends).transform(new Function<Friend, User>() {
            @Override
            public User apply(Friend friend) {
                User user = findById(appId, friend.getId().getFriendId());
                Conversation c = conversationMap.get(user.getId());
                user.setConversation(c == null ? 0 : c.getType());
                return user;
            }
        }).filter(input -> input != null).toList();
    }


    public void saveMobile(MobileIndex mobileIndex) {
        manager.insert(mobileIndex);
    }

    public void removeMobileIndexMobile(String mobile, String countrycode) {
        manager.deleteById(MobileIndex.class, new MobileKey(mobile, countrycode));
    }


    public UserToken saveToken(String device_token, Integer app_id, String userId, Integer device_type, String provider) {
        PersistenceManager realManager = CurrentUser.db();

        TokenToUser tokenToUser = realManager.find(TokenToUser.class, device_token);

        if (tokenToUser == null) {
            //将最新的Token对应关系存入库中
            tokenToUser = new TokenToUser();
            tokenToUser.setToken(device_token);
            tokenToUser.setUserId(userId);
            realManager.insert(tokenToUser);
            log.debug("save tokenToUser userId:{} change to {}", userId, device_token);
        } else if (!tokenToUser.getUserId().equals(userId)) {
            log.debug("update tokenToUser userId:{} change to {}", tokenToUser.getUserId(), userId);
//            cacheService.hDel(RedisCacheKey.USER_DEVICE_INFO, userId);
            UserToken userToken = getTokenInfo(tokenToUser.getUserId(), app_id);
            if (null != userToken && userToken.getToken().equals(device_token)) {
                log.debug("remove userToken. userId:{}, tokenId:{}", tokenToUser.getUserId(), device_token);
                realManager.deleteById(UserToken.class, new TokenKey(tokenToUser.getUserId(), app_id));
            }
            tokenToUser.setUserId(userId);
            realManager.update(tokenToUser);
        }

        UserToken userToken = new UserToken();
        userToken.setToken(device_token);
        userToken.setId(new TokenKey(userId, app_id));
        userToken.setDeviceType(device_type);
        userToken.setProvider(provider);
        userToken = realManager.insert(userToken);
        log.debug("save userToken :{} to {}", userId, device_token);

        try {
            String deviceJson = JsonUtil.toJson(userToken);
            log.debug("saveToUser userToken:{}", deviceJson);
            //推送时Tcp Server使用,登录时可能还没有，所以这边也设置
//            cacheService.hSet(RedisCacheKey.USER_DEVICE_INFO, userId, deviceJson);
        } catch (IOException e) {
            log.error("error when save user token", e);
        }

        return userToken;
    }


    public void removeToken(String sessionId) {
        log.debug("removeToken : sessionId - >" + sessionId);
        if (sessionId == null) return;
        UserSession userSession = manager.find(UserSession.class, sessionId);
        if (userSession != null) {
            UserToken userToken = getTokenInfo(userSession.getUserId(), userSession.getAppId());
            if (null != userToken) {
                log.debug("removeToken : userId - {},sessionId - {},tokenId - {}", userSession.getUserId(), sessionId, userToken.getToken());
                removeToken(userSession.getUserId(), userSession.getAppId());
            } else {
                log.debug("user token does not exist.");
            }
        }

    }

    public void removeToken(String userId, Integer appId) {
        log.debug("removeToken : userId:{}. appId:{}.", userId, appId);
        if (userId == null || null == appId) return;
        PersistenceManager realManager = getRealManager(appId);
        realManager.deleteById(UserToken.class, new TokenKey(userId, appId));

        UserToken userToken = getTokenInfo(userId, appId);
        if (userToken != null) {
            TokenToUser tokenToUser = realManager.find(TokenToUser.class, userToken.getToken());
            if (null != tokenToUser && userId.equals(tokenToUser.getUserId())) {
                log.debug("removeToken2User : userId - {},tokenId - {}", userId, userToken.getToken());
                realManager.deleteById(TokenToUser.class, tokenToUser.getToken());
            }
        }
    }


    public void updateToken(UserToken userToken) {
        manager.update(userToken);
    }

    @Deprecated
    public VersionControl getVersionInfo(Integer deviceType, Integer app_id, String cert) {
        VersionKey key = new VersionKey(deviceType, app_id, cert);
        VersionControl versionControl = manager.find(VersionControl.class, key);
        return versionControl;
    }

    public AppVersion getAppVersion(AppVersionKey key) {
        return manager.find(AppVersion.class, key);
    }

    public void updateAppVersion(AppVersion version) {
        manager.update(version);
    }

    public VersionControl saveVersionInfo(VersionControl vc) {
        return manager.insert(vc);
    }

    public UserToken getTokenInfo(String user_id, Integer app_id) {
        PersistenceManager realManager = getRealManager(app_id);
        return realManager.find(UserToken.class, new TokenKey(user_id, app_id == null ? 0 : app_id));
    }

    public UserSession findUserSession(String session_id) {
        return manager.find(UserSession.class, session_id);
    }

    public void saveUserSession(String session_id, String user_id, Integer app_id) {
        UserSession userSession = new UserSession();
        userSession.setUserId(user_id);
        userSession.setSessionId(session_id);
        userSession.setAppId(app_id);

        PersistenceManager realManager = getRealManager(app_id);
        realManager.insert(userSession);


    }

    public void setUserDisturb(User user, String disable, String time, String clientSession) {
        if ("yes".equals(disable)) {
            user.setHideTime((time == null || "".equals(time.trim())) ? hideSendTime : time);
            manager.insert(new UserDisturb(user.getId(), user.getHideTime(), user.getLanguage()));
        } else {
            manager.deleteById(UserDisturb.class, user.getId());
            user.setHideTime(null);
        }

        updateUser(user, clientSession);
    }


    /**
     * @param user
     * @todo 增加appID
     */
    public void uploadUserInfo(User user, String clientVersion, String avatar_url, String nickname, Integer sex, String clientSession) {

        user.setRegSource(clientVersion == null ? "未知版本" : clientVersion);
        if (avatar_url != null) {
//            // 先按照新URL进行获取（若不匹配，则返回null）
//            String avatarKey = getYearMonthDayHourKey(avatar_url);
//
//            // 若返回null，说明是老版本的url地址，从最后一个/开始截取
//            if (avatarKey == null) {
//                int len = avatar_url.lastIndexOf("/");
//                if (len != -1) {
//                    avatarKey = avatar_url.substring(len + "/".length());
//                }
//            }
            user.setAvatar(avatar_url);
            if (isThirdPart(user)) {
                user.setAvatar_url(avatar_url);
            }
//            user.setAvatar_url(avatar_url);
//            user.setAvatar_url(cdnUrl + "/api/avatar/" + user.getAvatar());
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }

        if (sex == null) {
            sex = 2;//未知
        }

        user.setSex(sex);
        updateUser(user, clientSession);

    }

    public void modifyUser(User user) {
        manager.update(user);
    }

    public boolean updateContactName(User user, String friendId, Integer appId, String contactName) {
        //不能更改小秘书的备注
        if (SystemConstants.SYSTEM_ACCOUNT_SECRETARY.equals(friendId)) {
            return true;
        }
        Friend friend = manager.find(Friend.class, new UserKey(user.getId(), appId, friendId));
        if (friend == null) return false;
        friend.setContactName(contactName);
        friend.setUpdateTime(System.currentTimeMillis());
        manager.update(friend);
        return true;
    }

    /**
     * @param user
     * @todo 修改用户信息接口
     */
    public void updateUser(User user, String clientSession) {

        user.setLastUpdateTime(System.currentTimeMillis());
        if (clientSession != null) {

            user.setSessionId(clientSession);

//            User redisUser = loadFromRedis(clientSession);
//
//            if (redisUser != null) {
//                user.setSessionId(clientSession);
//                user.setTcpServer(redisUser.getTcpServer());
//            }

        }

//        saveToRedis(user);
        manager.update(user);

        userChange(user);
//        final User toUser = user;
//        this.taskService.execute(new Runnable() { //@todo: 获取用户在线状态，只投递在线的用户
//            @Override
//            public void run() {
//                List<Friend> friends = findFollowers(toUser.getId(), toUser.getAppId());
//                messageQueueService.sendUserUpdateMsg(friends, toUser);
//            }
//        });


    }


    public void updateLanguage(User user, String locale, String country) {
        if (StringUtils.isNotBlank(locale)) {
            String language = StringUtils.isNotBlank(country) ? String.format("%s_%s", locale, country) : locale;
            Locale l = LocaleUtils.parseLocaleString(language);
            if (l != null) {
                user.setLanguage(l.toString());
                manager.update(user);
            }
        }
    }


    public void updateUser(User user) {
        try {
            manager.update(user);
        } catch (Exception e) {
            log.error("[UpdateUser] Error", e);
        }
    }


//    /**
//     * 创建唯一帐号
//     *
//     * @param userId
//     * @param nickname
//     * @param mobile
//     */
//    public AccountIndex createAccount(String userId, String nickname, String mobile) {
//
//        String account = nickname;
//        int size = 4;
//        boolean founded = false;
//        while (true) {
//            if (size > mobile.length()) break;
//            AccountIndex accountIndex = manager.find(AccountIndex.class, account);
//            if (accountIndex == null) {
//                founded = true;
//                break;
//            }
//            account = nickname + randomString(mobile, size++);
//
//        }
//
//        if (!founded) {
//            while (true) {
//                AccountIndex accountIndex = manager.find(AccountIndex.class, account);
//                if (accountIndex == null) break;
//                account += Math.abs(random.nextInt(10));
//            }
//        }
//
//        AccountIndex result = new AccountIndex(account, userId);
//        manager.insert(result);
//
//        AccountHistory accountHistory = new AccountHistory(userId, account);
//        manager.insert(accountHistory);
//
//        return result;
//
//    }

    /**
     * 第三方登陆
     *
     * @param account
     * @param userId
     * @return
     */
    public AccountIndex createAccountForThridParty(String account, String userId) {
        AccountIndex accountIndex = null;
        try {
            accountIndex = createAccountIndex(account, userId, account);
        } catch (Exception e) {
            log.warn(String.format("account conflict.userId:%s,account:%s", userId, account), e);
        }
        return accountIndex;
    }


    public AccountIndex createAccount(String userId, String countryCode) {
        boolean found = false;
        AccountIndex accountIndex = null;
        do {
            AccountCount accountCount = manager.find(AccountCount.class, region);
            if (accountCount == null) {
                accountCount = new AccountCount(region);
                accountCount.setCount(CounterBuilder.incr(offset));
                try {
                    manager.insert(accountCount);
                } catch (Exception e) {
                    log.warn("fails to init basic value", e);
                    //忽略此异常，有可能存在并发操作，值已经存在
                }
                continue;
            }
            accountCount.getCount().incr();
            manager.update(accountCount);
            found = true;
            try {
                accountIndex = createAccountIndex(genAccount(accountCount.getCount().get(), countryCode), userId, null);
            } catch (Exception e) {
                found = false;
                log.warn(String.format("account conflick.userId:%s,account:%s", userId, accountCount.getCount().get()), e);
            }
        } while (!found);
        return accountIndex;
    }

    protected String genAccount(Long basicValue, String countryCode) {
        if (null == basicValue || StringUtils.isBlank(countryCode)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            return uuid.substring(uuid.length() - 6, uuid.length());
        }

        String formatBasicValue = basicValue < 10000 && basicValue > 0 ? String.format("%05d", basicValue) : String.valueOf(basicValue);

        int firstIndex = 0;
        for (int i = 0; i < countryCode.length(); i++) {
            if (countryCode.charAt(i) > '0' && countryCode.charAt(i) <= '9') {
                firstIndex = i;
                break;
            }
        }
        String formatCountryCode = countryCode.substring(firstIndex, countryCode.length());
        return String.format("%s%s", formatCountryCode, formatBasicValue);
    }

    /**
     * 创建账号索引
     */
    public AccountIndex createAccountIndex(String account, String userId, String pwd) {
        AccountIndex accountIndex = new AccountIndex(account, userId);
        accountIndex.setPassword(pwd);
        manager.insert(accountIndex, OptionsBuilder.ifNotExists().lwtLocalSerial());
        return accountIndex;
    }

    /**
     * 修改账号的历史记录
     */
    public void createAccountHistory(String userId, String account) {
        AccountHistory accountHistory = new AccountHistory(userId, account);
        manager.insert(accountHistory);
    }

    /**
     * 删除账号索引
     */
    public void removeAccountIndex(String account) {
        manager.deleteById(AccountIndex.class, account);
    }

    /**
     * 账号是否能修改
     */
    public boolean canBeModified(String userId) {
        List<AccountHistory> histories = manager.sliceQuery(AccountHistory.class).
                forSelect()
                .withPartitionComponents(userId)
                .limit(5).get();
        return histories.size() < 1;
    }

    static SecureRandom random = new SecureRandom();

    public static String randomString(String str, int length) {
        if (str.length() < length) return "";
        return str.substring(str.length() - length);
    }

    /**
     * @param combinedMobileKey
     * @param language
     * @param countrycode
     * @param clientVersion
     * @return
     * @todo 根据用户的手机号，从PhoneBook中查找已经注册为会员的用户，然后在对方的好友列表中加入当前用户
     */
    public User createNewUser(final String mobilePlaintext, final String combinedMobileKey, String language, final String countrycode, String clientVersion, String nickname, String fileUrl, Integer sex) {
        return createNewUser(mobilePlaintext, combinedMobileKey, language, countrycode, clientVersion, nickname, fileUrl, sex, 0);
    }

    /**
     * UpdateUserAccount
     *
     * @param user
     * @return
     */
    public User updateUserAccount(User user) {
        if (StringUtils.isBlank(user.getAccount())) {
            AccountIndex accountIndex = createAccount(user.getId(), user.getCountrycode());
            user.setAccount(accountIndex.getAccount());
            PersistenceManager realManager = getRealManager(user.getAppId());
            realManager.update(user);
        }
        return user;
    }


    public User createNewUser(final String mobilePlaintext, final String combinedMobileKey, String language, final String countrycode, String clientVersion, String nickname, String fileUrl, Integer sex, Integer appId) {

        User user = new User();
        user.setId(User.createUUID());

        if (null != nickname) {
            user.setNickname(nickname);
        } else {
            user.setNickname(getDefaultNickName(countrycode));
        }

//        AccountIndex accountIndex = createAccount(user.getId(), user.getNickname(), saltHashMobile);
        AccountIndex accountIndex;
        if (isThirdPart(user)) {
            accountIndex = createAccountForThridParty(mobilePlaintext, user.getId());
        } else {
            accountIndex = createAccount(user.getId(), countrycode);
        }
        user.setAccount(accountIndex.getAccount());

        user.setCreateTime(new Date());
        user.setMobile(combinedMobileKey);

        user.setSex(sex == null || sex < 0 || sex > 2 ? 2 : sex);

        user.setMobileVerify(1);//默认都是短信验证校验
        user.setAvatar(fileUrl == null ? "default" : fileUrl);
        user.setRegSource(clientVersion == null ? "未知版本" : clientVersion);
        user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
        user.setLastUpdateTime(System.currentTimeMillis());
        user.setUserType(0); //普通用户
        user.setCountrycode(countrycode);
        user.setLanguage(language);
        user.setMobilePlaintext(mobilePlaintext);
        user.setAppId(appId);
        if (isThirdPart(user)) {
            user.setAvatar_url(fileUrl);
        }
        user = manager.insert(user);
        //如果通过第三方登陆的，没有电话号码
        if (!isThirdPart(user)) {
            MobileIndex mobileIndex = new MobileIndex();
            mobileIndex.setMobileKey(new MobileKey(combinedMobileKey, countrycode));
            mobileIndex.setUserId(user.getId());
            //客户端同一个用户并发调用此接口，需要检查手机号码已经存在，则表示用户新增成功
            try {
                manager.insert(mobileIndex);
            } catch (Exception e) {
                log.error(String.format("fails to insert new mobile index. mobile:%s,countryCode:%s", combinedMobileKey, countrycode), e);
                throw new ServerException(StatusCode.INNER_ERROR, "fails to create user");
            }

            //成员注册成功之后 要将嘟嘟小秘书加为好友
            addRobotToFriend(user.getId(), appId);


            final User friend = user; //发送好友注册信息
            this.taskService.execute(new Runnable() {
                @Override
                public void run() {
                    tcService.incr("users", 1);
                    List<String> userList = phoneBookService.findUserListByMobile(combinedMobileKey, countrycode);
                    Set<String> exists = new HashSet<String>();
                    exists.add(friend.getId());
                    for (String userId : userList) {
                        if (exists.contains(userId)) continue;
                        exists.add(userId);

                        User toAdd = findById(0, userId);

                        messageQueueService.sendUserRegistMsg(toAdd, friend);
                    }

                }
            });
        }

        return user;

    }

    //加嘟嘟小秘书为好友功能
    public void addRobotToFriend(String userId) {
        User duUser = findById(0, duduId);
        if (duUser != null) {
            UserKey userKey = new UserKey(userId, 0, duUser.getId());
            saveFriend(new Friend(userKey, duUser.getNickname()));
        }

    }


    public void addRobotToFriend(String userID, Integer appID) {
        User duUser = findById(0, duduId);
        if (duUser != null) {
            UserKey userKey = new UserKey(userID, appID, duUser.getId());
            saveFriend(new Friend(userKey, duUser.getNickname()));
        }
    }


    /**
     * 发送验证码
     *
     * @param mobile
     * @return
     */
    public boolean sendAuthCode(String ua, String clientVersion, String mobile, String countrycode, String real_send, Locale locale, String type, Integer appID) {
        boolean result;

        boolean isLessThan3018ForAndriod = false;
        if (StringUtils.isNotBlank(ua)) {
            if (ua.toLowerCase().contains("android")) {
                if (StringUtils.isNotBlank(clientVersion)) {
                    if (clientVersion.toLowerCase().compareTo("chatgame-3.0.20") < 0) {
                        isLessThan3018ForAndriod = true;
                    }
                }
            }
        }

        //需要对Andriod客户端做兼容，否则老Andriod客户端 获取验证码时 会一致转圈
        if (!phoneFormatUtil.validatePhone(countrycode, mobile)) {
            //号码 格式 不正确,不需要发送手机号码验证码,客户端也不需要通知
            log.info("[phone valid] countryCode:{},mobile:{},result:{}. no send auth code", countrycode, mobile, false);
            if (isLessThan3018ForAndriod) {
                result = true;
                return result;
            } else {
                throw new ServerException(StatusCode.INVALID_PHONE, messageSourceService.getMessageSource(appID).getMessage("phone.invalid.format", new Object[]{}, locale));
            }
        }

        if (!phoneUtil.canAndIncySendNumberForMobile(countrycode, mobile)) {
            log.info("[phone over limit].mobile:{} countrycode:{}", mobile, countrycode);
            if (isLessThan3018ForAndriod) {
                result = true;
                return result;
            } else {
                throw new ServerException(StatusCode.OVER_LIMIT, messageSourceService.getMessageSource(appID).getMessage("request.limit", new Object[]{}, locale));
            }
        }

        if ("yes".equals(real_send)) {

            log.debug("send sms to phone. mobile=[{}], countryCode=[{}]", mobile, countrycode);

            result = this.smsService.sendNewSMS(mobile, countrycode, messageSourceService.getMessageSource(appID), locale, type);

        } else {
            //限制次数的
            cacheService.setEx(mobile, sendSmsSecond, "8888");
            log.debug("send.sms.real != yes  redis value is 8888! mobile=[{}], countryCode=[{}]", mobile, countrycode);
            result = true;
        }
        return result;
    }

    /**
     * 保存用户数据到redis中,同时生成用户对应的session信息
     *
     * @param user
     * @param app_id
     */

    public void saveUserLoginState(User user, final Integer app_id, String public_key, String oldSessionId, String ip, final int newUser, final String userAgentString, String clientVersion) {

        //removeToken(oldSessionId);
        cleanUserDataByUnusedSession(oldSessionId, app_id);
        cleanUserDataByUserId(user.getId(), app_id, false);
        //removeToken(user.getId(), app_id);

        final String session_id = UUID.randomUUID().toString().replaceAll("\\-", "");
        //保存人 最新的session_Id
        saveUserSessionIndex(user.getId(), app_id, session_id);
        //保存session对应的user_Id
        saveUserSession(session_id, user.getId(), app_id);
        user.setSessionId(session_id);

//        String tcps = findServerAddr(user, ip);
//        String[] ips;
//        try {
//            ips = JsonUtil.fromJson(tcps, String[].class);
//        } catch (IOException e) {
//            log.error("[saveUserLoginState],error:{}", e);
//            throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, "Connection error.Please try again later.");
//        }

        Map<String, String[]> serverInfos = findServerAddress(user, ip);
        user.setTcpServer(serverInfos.get("tcp_server")); //TODO
        user.setFileServer(serverInfos.get("file_server")); //TODO

        user.setPublicKey(public_key);

        //将相关的数据写入缓存

        log.debug("user = {}", user);

        manager.update(user);

//        saveUserSessionToRedis(user, session_id, app_id);

//        final User toUser = user;
//        this.taskService.schedule(new Runnable() {
//            @Override
//            public void run() {
//                messageQueueService.robotSendUserMsg1(toUser, newUser == 1 ? "robot.toUser.msg1" : "welcome.msg");
//                if (userAgentString != null) {
//                    userAgentService.informOfPushSettings(toUser, userAgentString);
//                }
//            }
//        }, new Date(System.currentTimeMillis() + (newUser == 1 ? 5000 : 10000)));
        userLoginEvent(user, clientVersion, userAgentString, newUser == 1);

    }


    public void updateUserLocalTimezone(String uid, Integer appId, String local, String timezone) {
        log.debug("update user local timezone. uid:{},appId:{},local:{},timezome:{}", uid, appId, local, timezone);
        User user = manager.find(User.class, uid);
        user.setLanguage(local);
        user.setTimezone(timezone);
        manager.update(user);
    }

    public String getUserDefaultAvatarAccessKey() {
        String accesskey = "default";
        if (StringUtils.isNotBlank(defaultAvatars)) {
            String[] avatars = defaultAvatars.trim().split(",");
            Random random = new Random();
            accesskey = avatars[random.nextInt(avatars.length)];
        }
        return accesskey;
    }

    /**
     * 将好友放入已经删除的列表中
     *
     * @param you               用户
     * @param toBeRemovedFriend 要删除的好友
     */
    public void addFriendToRemovedList(User you, User toBeRemovedFriend) {
        RemovedFriend removedFriend = new RemovedFriend(new RemovedFriendKey(you.getId(), toBeRemovedFriend.getCountrycode() + toBeRemovedFriend.getMobile()), toBeRemovedFriend.getId());
        manager.insert(removedFriend);
    }

    /**
     * 根据account去查找对应的用户account信息
     *
     * @param account 要查的account
     * @return account信息
     */
    public AccountIndex getAccountIndexByAccountId(String account) {
        return this.manager.find(AccountIndex.class, account);
    }

    /**
     * 根据account查找用户信息
     *
     * @param account 要查的account
     * @return user信息
     */
    public User getUserByAccoundId(String account) {
        AccountIndex accountIndex = getAccountIndexByAccountId(account);
        if (accountIndex == null) {
            return null;
        }
        return findById(0, accountIndex.getUserId());
    }


    public User getUserByAccountID(String account, Integer appID) {
        AccountIndex accountIndex = getAccountIndexByAccountId(account);
        if (accountIndex == null) {
            return null;
        }
        User user = findById(appID, accountIndex.getUserId());
        if (user.getAppId() != null && appID != null
                && !user.getAppId().equals(appID) && !user.getId().equals(duduId)) {
            log.error("[GetUserByAccountID Error] user.appID:{} , appID:{}, user.id:{} ", user.getAppId(), appID, user.getId());
            return null;
        }
        return (user != null) ? user : null;
    }


    public void updateScore(Integer appId, User you, Date today, Optional<Integer> opScore) {
        GameScore gameScore = manager.find(GameScore.class, new GameScore.GameSongScoreKey(you.getId(), appId, today));
        Integer score = 100;
        if (gameScore != null) {
            gameScore.setScore(gameScore.getScore() + score);
            manager.update(gameScore);
        } else {
            manager.insert(new GameScore(new GameScore.GameSongScoreKey(you.getId(), appId, today), score));
        }
        if (you.getTouch() == null) {
            you.setTouch(score);
        } else {
            you.setTouch(you.getTouch() + score);
        }
        manager.update(you);
    }

    /*
     * the top 20, must including myself
     */
    public List<GameScore> getBoard(Integer appId, User you, Date today) {
        Object components[] = {you.getId(), 0};
        List<Friend> friends = manager.sliceQuery(Friend.class).forSelect().withPartitionComponents(components).get();
        Map<String, Integer> conversations = conversationService.getConversations(you.getId(), 0);
        conversations = addRobotInConversations(conversations);
        excludeBlackFriends(friends, conversations);

        String uid = you.getId();
        List<String> allUids = Lists.newArrayList();
        allUids.add(uid);
        if (null == friends) {
            friends = new ArrayList<>();
        }
        allUids.addAll(FluentIterable.from(friends).transform(friendsIdTransformer).filter(input -> input != null).toList());

        // 对list 根据score排序
        List<GameScore> immutableList = manager.typedQuery(GameScore.class, selectScoreInStmt(allUids, appId, today)).get();
        List<GameScore> list = new ArrayList<>(immutableList);
        excludeBlackGameScores(list, conversations);
        Collections.sort(list, new GameScore.ScoreComparator());
        int index = -1;
        int i = 0;
        for (GameScore l : list) {
            if (l.getKey().getUid().compareTo(uid) == 0) {
                index = i;
            }
            allUids.remove(l.getKey().getUid());
            i++;
        }
        if (i >= BOARDNUM) {
            GameScore tmp = null;
            if (index != -1 && index >= BOARDNUM) {
                tmp = list.get(index);
            } else if (index == -1) {
                tmp = new GameScore(new GameScore.GameSongScoreKey(uid, appId, today), 0);
            }
            if (tmp != null) {
                list.set(BOARDNUM - 1, tmp);
            }
            return list.subList(0, BOARDNUM);
        } else {
            if (index == -1) {
                list.add(new GameScore(new GameScore.GameSongScoreKey(uid, appId, today), 0));
                allUids.remove(uid);
            }
            int random = BOARDNUM - list.size();
            if (list.size() == BOARDNUM) {
                return list;
            } else if (allUids.size() <= random) {
                // keep the board same otherwise client sees different ranks each time
                random = allUids.size();
            } else {
                // pick random persons
                Collections.shuffle(allUids);
            }
            for (String padUid : allUids.subList(0, random)) {
                list.add(new GameScore(new GameScore.GameSongScoreKey(padUid, appId, today), 0));
            }
        }
        return list;
    }

    public List<String> scoreToIds(List<GameScore> gameScores) {
        if (null == gameScores) {
            return new ArrayList<>();
        }
        return FluentIterable.from(gameScores).transform(scoreToIdTransformer).filter(input -> input != null).toList();
    }

    public boolean isReportedMuch(String uid, Integer appId) {
        Object components[] = {uid, appId};
        List<UserReport> reports = manager.sliceQuery(UserReport.class).forSelect().
                withPartitionComponents(components).get();
        if (reports.size() > 3) {
            return true;
        }
        return false;
    }

    /**
     * 删除用户session信息
     *
     * @param user_id
     * @param app_id
     */
    public void removeUserSessionIndex(String user_id, Integer app_id) {
        log.debug("remove user session from UserSessionIndex.userId:{},app_id:{}", user_id, app_id);
        PersistenceManager realManager = getRealManager(app_id);
        realManager.deleteById(UserSessionIndex.class, new UserSessionIndexKey(user_id, app_id));
    }

    /**
     * 请求不使用session的相关信息
     * 包括user和session的关联 以及用户token的关联
     *
     * @param sessionId
     */
    public void cleanUserDataByUnusedSession(String sessionId, int appId) {
        if (StringUtils.isBlank(sessionId)) return;
        removeUserSession(sessionId, appId);
    }


    /**
     * 清理用户相关的数据 包括用户session和用户token之间的关联
     *
     * @param userId
     * @param appId
     */
    public void cleanUserDataByUserId(final String userId, final Integer appId, boolean deleteUser2Session) {
        if (StringUtils.isBlank(userId) || null == appId) return;

        log.debug("begin to clean user data. userId:{},appId:{}", userId, appId);

        final UserSessionIndex userSessionIndex = findUserSessionIndexByKey(userId, appId);

        if (null != userSessionIndex) {
            log.debug("remove user session.userId:{}.sessionId:{}", userId, userSessionIndex.getSessionId());
            //session2user
            removeUserSession(userSessionIndex.getSessionId(), appId);

        }

        //user2session
        //绑定设备时，会经常出现user2Session表为空，先清除，用插入数据覆盖掉即可
        if (deleteUser2Session) {
            removeUserSessionIndex(userId, appId);
        }
        //删除待清理用户的token
        removeToken(userId, appId);
    }

//    /**
//     * 当chatgame1.x版本不使用时，可以删除
//     *
//     * @param request
//     * @param mobile
//     * @param countrycode
//     * @return
//     */
//    @Deprecated
//    public boolean isChatgameV1FirstUsePhone(HttpServletRequest request, String mobile, String countrycode) {
//        boolean result = false;
//        boolean isChatgameV1 = requestUtils.isChatgameV1(request);
//        if (isChatgameV1) {
//            String saltHashMobile = mobileUtils.saltHash(mobile);
//            MobileIndex mobileIndex = isExistMobile(saltHashMobile, countrycode);
//
//            //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
//            if (null == mobileIndex) {
//                //采用明文查找手机号码是否存在
//                mobileIndex = isExistMobile(mobile, countrycode);
//            }
//
//            if (null == mobileIndex) {
//                result = true;
//            }
//        }
//        return result;
//    }

//    /**
//     * 客户端版本升级采用cdn进行配置，不需要服务端做
//     * @param device_type
//     * @param app_id
//     * @param cert
//     * @param oldVersion
//     * @param locale
//     */
//    public void checkVersion(Integer device_type, Integer app_id, String cert, Integer oldVersion, Locale locale) {
//        VersionControl versionControl = getVersionInfo(device_type, app_id, cert == null ? "0" : cert);
//        if (versionControl != null) {
//            if (oldVersion != 0) {
//                String newVersion = versionControl.getClientVersion();
//                int i = newVersion.indexOf(".");
//                if (i != -1) {
//
//                    if (Integer.parseInt(newVersion.substring(0, i)) != oldVersion) {
//                        throw new ServerException(StatusCode.AUTH_CODE_FORCE_UPGRADE, messageSource.getMessage("version.old", new Object[]{}, locale));
//                    }
//                }
//            }
//
//        }
//    }

    /**
     * IOS 1.x版本调错接口 兼容处理
     */
    @Deprecated
    public void activeUserByModifyPhone(User user) {
        if (user != null && user.getMobileVerify() == 0) {
            user.setMobileVerify(1);//已经校验
            tcService.incr("users_activate", 1);//激活累计
            manager.update(user);
        }
    }


    public String saltMobile(String mobile, String countryCode) {
        String result = mobile;
        if (StringUtils.isNotBlank(mobile)) {
            if (mobile.length() < 32) {
//                mobile = StringUtil.fixMobile(countryCode, StringUtil.clearMobileNo(mobile));
                result = mobileUtils.saltHash(mobile);
            } else {
                result = mobile.toLowerCase();
            }
        }
        return result;
    }

    public Collection<User> getRandomPlayer(int num) {
        long r = (long) Math.floor(Math.random() * (Long.MAX_VALUE + 1));
        boolean negative = new Random().nextBoolean();
        if (negative) {
            r = -r - 1;
        }
        Select s = select().from("users").where(gte("token(id)", r)).limit(num).allowFiltering();
        List<User> players = manager.typedQuery(User.class, s).get();
        if (players.size() < num) {
            log.info("Not found enough random player gt {}, get first one instead", r);
            Select selectFromStart = select().from("users").where(gte("token(id)", Long.MIN_VALUE)).limit(num - players.size()).allowFiltering();
            players.addAll(manager.typedQuery(User.class, selectFromStart).get());
        }
        return Collections2.filter(players, new Predicate<User>() {
            @Override
            public boolean apply(User user) {
                return null != user.getNickname() && (!user.getNickname().startsWith("test"));
            }
        });
    }


    public void addUserBehaviourAttr(UserBehaviouAttr userBehaviouAttr, Integer appId) {
        PersistenceManager realManager = getRealManager(appId);
        if (null == userBehaviouAttr) return;

        try {
            UserBehaviouAttr savedUserBehaviouAttr = realManager.find(UserBehaviouAttr.class, userBehaviouAttr.getUserAppIdkey());
            if (null == savedUserBehaviouAttr) {
                realManager.insert(userBehaviouAttr);
                log.debug("add user behaviou attr.{}", userBehaviouAttr);
            } else if ((null != userBehaviouAttr.getClientVersion() &&
                    !userBehaviouAttr.getClientVersion().equals(savedUserBehaviouAttr.getClientVersion())) &&
                    (null != userBehaviouAttr.getUa() && !userBehaviouAttr.getUa().equalsIgnoreCase(savedUserBehaviouAttr.getUa()))
                    ) {
                savedUserBehaviouAttr.setClientVersion(userBehaviouAttr.getClientVersion());
                savedUserBehaviouAttr.setUa(userBehaviouAttr.getUa());
                realManager.update(savedUserBehaviouAttr);
                log.debug("update user behaviou attr.{}", userBehaviouAttr);
            }
        } catch (Exception e) {
            log.error(String.format("fails to save user behaviour attr. %s", userBehaviouAttr), e);
        }

    }

    public User getUserInfoBySession(Integer appId, String sessionId) {
        PersistenceManager realManager = getRealManager(appId);
        if (StringUtils.isBlank(sessionId)) {
            return null;
        }
        UserSession session = realManager.find(UserSession.class, sessionId);
        if (session == null) {
            return null;
        }
        User user = realManager.find(User.class, session.getUserId());
        if (null == user) {
            log.warn("sessionId:{} has no correct user.", sessionId);
            return user;
        }

        return user;
    }

    public String getDefaultNickName(String countryCode) {
        String nickName;
        if ("0086".equals(countryCode) || "86".equals(countryCode) || "+86".equals(countryCode)) {
            nickName = "@无名氏";
        } else {
            nickName = "@Anonymous";
        }
        return nickName;
    }

    /**
     * 按照新URL进行获取文件key。
     *
     * @param url 可能的情况例如
     *            http://127.0.0.1:8080/api/avatar/2015/6/18/14/550ef306-cc80-49cc-9ca6-ce787c7100d2
     *            http://127.0.0.1:8080/api/file/download/2015/6/10/cece7e4d-a1e6-4b4a-8c84-971b6ffa6a96
     *            http://127.0.0.1:8080/api/avatar/550ef306-cc80-49cc-9ca6-ce787c7100d2
     *            http://127.0.0.1:8080/api/file/download/cece7e4d-a1e6-4b4a-8c84-971b6ffa6a96
     * @return 若匹配，返回完整key。若不匹配，返回null。
     */
    private String getYearMonthDayHourKey(String url) {
        String reg = "\\d+\\/\\d+\\/\\d+\\/\\d+\\/[\\s\\S]*";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }


    public UserVo getUserInfoIncludeRelation(User currentUser, String toUserId, Integer appId) {
        String fromUserId = currentUser.getId();
        UserVo result = null;
        User userInfo = findById(appId, toUserId);
        if (null != userInfo) {
            result = UserVo.createFromUser(userInfo);

            Conversation conversation = conversationService.findConversationByEntityId(fromUserId, toUserId, appId);
            if (null != conversation) {
                result.setConversation(conversation.getType());
            }
            UserKey userKey = new UserKey();
            userKey.setAppId(appId);
            userKey.setUserId(fromUserId);
            userKey.setFriendId(toUserId);
            Friend friend = findFriendInfo(userKey);
            if (null != friend) {
                result.setContactName(friend.getContactName());
            }

            if (SystemConstants.SYSTEM_ACCOUNT_SECRETARY.equals(toUserId)) {
                result.setContactName("");
                result.setNickname(messageSourceService.getMessageSource(currentUser.getAppId()).getMessage("secretary.name", new Object[]{}, currentUser.getLocale()));
            }
        }
        return result;
    }


    public ThirdAppCgUser initThirdAppUserInfo(String userId, String thirdAppUserId, String accessToken, String source) {
        CgThirdAppUser cgThirdAppUser = null;
        CgThirdAppUserKey key = new CgThirdAppUserKey();
        key.setUserId(userId);
        key.setType(source);
        if (StringUtils.isNotBlank(thirdAppUserId)) {
            //更新
            cgThirdAppUser = new CgThirdAppUser();
            cgThirdAppUser.setThirdAppUserId(thirdAppUserId);

            cgThirdAppUser.setKey(key);

            try {
                manager.insertOrUpdate(cgThirdAppUser);
            } catch (AchillesLightWeightTransactionException transactionException) {
                //ignore
            }

        } else {
            cgThirdAppUser = manager.find(CgThirdAppUser.class, key);
            if (null == cgThirdAppUser) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, "no third app user id. req should bbe with third app user id");
            }
        }

        ThirdAppCgUser thirdAppCgUser;
        ThirdAppCgUserKey thirdAppCgUserKey = new ThirdAppCgUserKey();
        thirdAppCgUserKey.setType(source);
        thirdAppCgUserKey.setThirdAppUserId(cgThirdAppUser.getThirdAppUserId());
        thirdAppCgUserKey.setUserId(userId);

        if (StringUtils.isNotBlank(accessToken)) {
            //更新token
//            thirdAppCgUser = new ThirdAppCgUser();
//            thirdAppCgUser.setAccessToken(accessToken);
//            thirdAppCgUser.setKey(thirdAppCgUserKey);

            //是否存在
            thirdAppCgUser = manager.find(ThirdAppCgUser.class, thirdAppCgUserKey);
            //第一次绑定第三方app
            if (null == thirdAppCgUser) {
                thirdAppCgUser = new ThirdAppCgUser();
                systemCmdService.userFristBindThirdApp(userId, source, cgThirdAppUser.getThirdAppUserId());
                thirdAppCgUser.setKey(thirdAppCgUserKey);
            }
            thirdAppCgUser.setAccessToken(accessToken);
            manager.insertOrUpdate(thirdAppCgUser);
        } else {
            thirdAppCgUser = manager.find(ThirdAppCgUser.class, thirdAppCgUserKey);
            if (null == thirdAppCgUser) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, "no access token. req should bbe with access token");
            }
        }

        return thirdAppCgUser;
    }

    public User findUserByMobile(String mobile, String countryCode, Integer appId) {
        User user = null;
        String mobilePar = mobile;
        mobile = mobileUtils.saltHash(mobilePar);
        MobileIndex mobileIndex = findMobileIndex(mobile, countryCode, appId);


        if (null != mobileIndex) {
            user = findById(mobileIndex.getUserId());
        }

        return user;


    }

    public void userRegister(User user) {
        try {
            this.taskService.execute(() -> {
                //发布用户注册的事件
                if (null != user) {
                    String routeKey = "user.register.us";
                    if ("0086".equals(user.getCountrycode())) {
                        routeKey = "user.register.cn";
                    }
                    String eventType = "user.register";
                    queueService.sendEvent(routeKey, eventType, user);
                }
            });
        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            log.error(e.getMessage(), e);
        }
    }

    public void userLoginEvent(User user, String clientVersion, String ua, Boolean isNew) {
        try {
            this.taskService.execute(() -> {
                //发布用户登录的事件
                if (null != user) {
                    String routeKey = "user.login.us";
                    if ("0086".equals(user.getCountrycode())) {
                        routeKey = "user.login.cn";
                    }
                    String eventType = "user.login";

                    UserLoginEvent userLoginEvent = new UserLoginEvent();
                    BeanUtils.copyProperties(user, userLoginEvent);
                    userLoginEvent.setClientVersion(clientVersion);
                    userLoginEvent.setUa(ua);
                    userLoginEvent.setIsNew(isNew);
                    userLoginEvent.setEventId(UUID.randomUUID().toString());
                    userLoginEvent.setEventTime(System.currentTimeMillis());
                    userLoginEvent.setEventTimeStr(Instant.now().toString());
                    userLoginEvent.setAppId(user.getAppId());

                    queueService.sendEvent(routeKey, eventType, userLoginEvent);
                }
                //用户覆盖安装时 不会进行重新登陆 因此需要修改为在拉取离线消息时 更新
//                UserBehaviouAttr userBehaviouAttr = new UserBehaviouAttr();
//                userBehaviouAttr.setUserAppIdkey(new UserBehaviouAttr.UserAppIdKey(user.getId(), user.getAppId()));
//                userBehaviouAttr.setClientVersion(clientVersion);
//                userBehaviouAttr.setUa(ua);
//                addUserBehaviourAttr(userBehaviouAttr);
            });

        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            log.error(e.getMessage(), e);
        }
    }

    public void userChange(final User user) {
        if (null == user) return;
        try {
            this.taskService.execute(() -> {
                List<Friend> friends = findFollowers(user.getId(), user.getAppId());
                messageQueueService.sendUserUpdateMsg(friends, user);
            });
        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            log.error(e.getMessage(), e);
        }

        try {
            this.taskService.execute(() -> {
                //发布用户注册的事件
                if (null != user) {
                    String routeKey = "user.modify.us";
                    if ("0086".equals(user.getCountrycode())) {
                        routeKey = "user.modify.cn";
                    }
                    String eventType = "user.modify";
                    queueService.sendEvent(routeKey, eventType, user);
                }
            });
        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            log.error(e.getMessage(), e);
        }
    }


    public void sendUserCallSm(final User toUser, final User fromUser, final String type, final Long delayTimeInSecond) {
        try {
            if (null != delayTimeInSecond) {
                this.delayTaskService.submitDelayTask(delayTimeInSecond * 1000, () -> {
                    //需要判断用户在延迟的时间内，接受有没有登陆过，如果登陆过，则不发送短信
                    Long lastLoginTime = getUserLastLoginTime(toUser.getId());
                    if (null != lastLoginTime) {
                        //用户在延迟的时间内 没有登陆过 在执行发送短信
                        if (System.currentTimeMillis() - lastLoginTime > delayTimeInSecond * 1000) {
                            callSmSendService.sendCallSmForIos(toUser, fromUser, type);
                        } else {
                            log.info("userId:{} has loggined. time:{} .no send sm.", toUser.getId(), lastLoginTime);
                        }
                    } else {
                        //用户还没有登陆过，发送消息
                        callSmSendService.sendCallSmForIos(toUser, fromUser, type);
                    }
                });
            } else {
                this.taskService.execute(() -> callSmSendService.sendCallSmForIos(toUser, fromUser, type));
            }
        } catch (Exception e) {
            log.error("fails to send user call sm. toUser:{},fromUser:{},type:{}", toUser, fromUser, type);
        }
    }

    public void handleUserGetMsg(String userId, Integer appId, String ua, String clientVersion) {
        try {
            if (null != userId) {
                this.taskService.execute(() -> smSendService.resetSmCount(userId));
                this.taskService.execute(() -> saveUserLastLoginTime(userId, System.currentTimeMillis()));
                this.taskService.execute(() -> {
                    //用户版本自动升级时，不会重新登录
                    UserBehaviouAttr userBehaviouAttr = new UserBehaviouAttr();
                    userBehaviouAttr.setUa(ua);
                    userBehaviouAttr.setClientVersion(clientVersion);

                    UserBehaviouAttr.UserAppIdKey key = new UserBehaviouAttr.UserAppIdKey();
                    key.setAppId(appId);
                    key.setUserId(userId);

                    userBehaviouAttr.setUserAppIdkey(key);

                    addUserBehaviourAttr(userBehaviouAttr, appId);
                });
            }
        } catch (Exception e) {
            log.error(String.format("fails to rest suer sm count. userId:{}", userId), e);
        }
    }

    public void addUserBehaviouAttrAsync(String userId, Integer appId, String ua, String clientVersion) {
        try {
            if (null != userId) {
                this.taskService.execute(() -> {
                    //用户版本自动升级时，不会重新登录
                    UserBehaviouAttr userBehaviouAttr = new UserBehaviouAttr();
                    userBehaviouAttr.setUa(ua);
                    userBehaviouAttr.setClientVersion(clientVersion);

                    UserBehaviouAttr.UserAppIdKey key = new UserBehaviouAttr.UserAppIdKey();
                    key.setAppId(appId);
                    key.setUserId(userId);

                    userBehaviouAttr.setUserAppIdkey(key);

                    addUserBehaviourAttr(userBehaviouAttr, appId);
                });
            }
        } catch (Exception e) {
            log.error(String.format("fails to rest suer sm count. userId:{}", userId), e);
        }
    }

    /**
     * 优先级顺序，备注名称、通讯录名称、发送方的昵称
     *
     * @param user
     * @param friendId
     * @param appId
     * @return
     * @throws ServerException
     */
    public String getFriendNickname(User user, String friendId, int appId) throws ServerException {

        Friend friend = null;
        try {
            friend = manager.find(Friend.class, new UserKey(friendId, appId, user.getId()));
        } catch (Exception e) {
            log.error("fails to get friend.", e);
        }

        if (friend != null && StringUtils.isNotBlank(friend.getContactName())) {
            log.debug("friend remark name. userId:{},otherUserId:{},remark:{}", friendId, user.getId(), friend.getContactName());
            return friend.getContactName();
        }

        PhoneBook phoneBook = null;
        try {
            phoneBook = manager.find(PhoneBook.class,
                    new PhoneKey(user.getCountrycode(), user.getMobile(), friendId));

            //fixed:2016-01-18号之前 用户通讯录的手机号码有的为大写 有的为小写
            if (null == phoneBook) {
                phoneBook = manager.find(PhoneBook.class,
                        new PhoneKey(user.getCountrycode(), user.getMobile().toUpperCase(), friendId));
            }
        } catch (Exception e) {
            log.error("fails to get phonebook.", e);
        }
        if (phoneBook != null && StringUtils.isNotBlank(phoneBook.getName())) {
            return phoneBook.getName();
        }

        String nickName = user.getNickname();
        if (null != nickName && StringUtils.isNotBlank(nickName)) {
            return nickName;
        }

        return "Unknown".intern();
    }


    /**
     * 设置用户的上次登陆时间
     * 用户发送短信 需要判断在特定的时间内是否登陆过
     * 需要设置过期时间，防止数据过多
     *
     * @param userId
     * @param timeMillSecond
     */
    public void saveUserLastLoginTime(String userId, Long timeMillSecond) {
        if (null != userId && null != timeMillSecond) {
            cacheService.setEx(getUserLastLoginExpireKey(userId), 10 * 60, String.valueOf(timeMillSecond));
        }
    }

    public Long getUserLastLoginTime(String userId) {
        Long result = null;
        try {
            String loginTime = cacheService.get(getUserLastLoginExpireKey(userId));
            result = Long.valueOf(loginTime);
        } catch (Exception e) {
            log.warn("fails to get user last login time (expire)", e);
        }
        return result;
    }

    public PersistenceManager getRealManager(int appId) {
        return appId > SystemConstants.CG_APP_ID_MAX ? opManager : manager;
    }

    private String getUserLastLoginExpireKey(String userId) {
        return String.format("%s%s", RedisCacheKey.USER_LAST_LOGIN_EXPIRE_PREFIX, userId);
    }


    public void logUserAuthCodeResult(String countryCode, String mobile, String inputAuthCode, String storedAuthCode, Boolean success, String from) {
        try {
            //增加添加好友来源的话单统计
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("user_authcode").append(LOG_SPLIT_CHAR)
                    .append(countryCode).append(LOG_SPLIT_CHAR)
                    .append(mobile).append(LOG_SPLIT_CHAR)
                    .append(inputAuthCode).append(LOG_SPLIT_CHAR)
                    .append(storedAuthCode).append(LOG_SPLIT_CHAR)
                    .append(success ? "1" : "0").append(LOG_SPLIT_CHAR)
                    .append(from);
            logUtil.logReq(stringBuilder.toString());
        } catch (Exception e) {
            //ignore
        }
    }

    public UserBehaviouAttr getUserBehaviou(String userId, Integer appId) {
        UserBehaviouAttr.UserAppIdKey key = new UserBehaviouAttr.UserAppIdKey(userId, appId);
        UserBehaviouAttr userBehaviouAttr = manager.find(UserBehaviouAttr.class, key);
        return userBehaviouAttr;
    }

    private boolean isThirdPart(User user) {
        return SystemConstants.THIRD_PART_LOGIN.equals(user.getMobile());
    }

    public static void main(String args[]) {
        UserService userService = new UserService();

        String url = "http://cn.file.chatgame.me/api/file/download/2015/6/7/3/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/6/7/13/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/6/17/3/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/11/7/3/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/11/12/3/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/11/7/13/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/1/17/13/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));

        url = "http://cn.file.chatgame.me/api/file/download/2015/11/17/13/d7c7b466-9d89-4108-b0ff-f7dfdde03265";
        System.out.println(userService.getYearMonthDayHourKey(url));


    }
}
