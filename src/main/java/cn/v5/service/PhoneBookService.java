package cn.v5.service;


import cn.v5.code.SystemConstants;
import cn.v5.entity.*;
import cn.v5.util.LoggerFactory;
import cn.v5.util.MobileUtils;
import cn.v5.util.StringUtil;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * 手机通讯录服务
 */
@Service
public class PhoneBookService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(PhoneBookService.class);
    public static final int MAX_VALUE = 0x7fffffff;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Inject
    private UserService userService;

    @Inject
    private TableCountService tcService;

    @Inject
    private TaskService taskService;

    @Inject
    private FriendService friendService;

    @Inject
    private MessageQueueService messageQueueService;

    @Inject
    private MobileUtils mobileUtils;

    private PreparedStatement pstmt;


    @Autowired
    private FriendRecommendService friendRecommendService;

    private Function<PhoneBook, String> pbKeyToIdFunction = new Function<PhoneBook, String>() {
        @Override
        public String apply(PhoneBook input) {
            return input.getId().getUserId();
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        pstmt = manager.getNativeSession().prepare("insert into phone_new_books(mobile, country_code, userid, create_time, name) values(?,?,?,?,?)");
    }

    public List<User> uploadPhoneBook(User user, List<String> mobileList, Integer appId, Integer hashed) {
        List<List<String>> list = Lists.newArrayList();
        for (String mobile : mobileList) {
            List<String> subList = Lists.newArrayList();
            subList.add(mobile);
            subList.add("");
            list.add(subList);
        }

        return uploadPhoneBookWithName(user, list, appId, hashed);
    }


    public Set<String> findRemovedFriends(String userId) {
        List<RemovedFriend> list = manager.sliceQuery(RemovedFriend.class).forSelect().withPartitionComponents(userId).get();
        Set<String> result = new HashSet<>();// item: countryCode + mobile
        for (RemovedFriend r : list) {
            result.add(r.getId().getMobile());
        }
        return result;
    }

    public List<String> findRemoveFriendIdList(String userId) {
        List<RemovedFriend> removedFriends = manager.sliceQuery(RemovedFriend.class).forSelect().withPartitionComponents(userId).get();
        List<String> friendIdList = Lists.newArrayList();
        for (RemovedFriend removedFriend : removedFriends) {
            friendIdList.add(removedFriend.getFriendId());
        }
        return friendIdList;
    }

    /**
     * 用户上传手机通讯录，需要保存反向关系，建立mobile-> user list的对应关系，这样在新用户注册的时候，发送新朋友系统通知信息
     *
     * @param user        用户
     * @param nameMobiles 手机号列表
     * @param appId       appId
     */
    public List<User> uploadPhoneBookWithName(final User user, List<List<String>> nameMobiles, final Integer appId, Integer hashed) {
        //fix  对于过多联系人的不予处理 以防止性能降低
        if (nameMobiles.size() > 1000) {
            log.info("user {} try to upload {} contacts", user.getId(), nameMobiles.size());
            return null;
        }

        // 获取此人已经删除的列表
        Set<String> deletedFriends = findRemovedFriends(user.getId());

        //本人可能成为其它用户可能认识的人
        Map<String, String[]> addToRecommends = new HashMap<>();

        List<String> mobileList = Lists.newArrayList();
        List<String> codeList = Lists.newArrayList();
        Map<String, String> mobileCodeMap = Maps.newHashMap();
        Map<String, String> mobileNameMap = Maps.newHashMap();
        Session session = manager.getNativeSession();

        for (List<String> mobileinfo : nameMobiles) {
            String mobile = mobileinfo.get(0);
            mobile = StringUtil.clearMobileNo(mobile);
            if (mobile.length() < 5) {
                log.warn("invalidate mobile no {} for user {}", mobile, user.getId());
                continue;
            }
            mobile = mobile.substring(0, 4) + mobileUtils.saltHash(mobile.substring(4));
            if (mobile.equals(user.getCountrycode() + user.getMobile())) {
                log.warn("invalidate mobile no {}. userId:{}", mobile, user.getId());
                continue;
            }
            String name = mobileinfo.get(1);
            String countryCode = mobile.substring(0, 4);
            mobile = mobile.substring(4);
            mobileCodeMap.put(StringUtil.combinedMobileKey(mobile, appId), countryCode);
            mobileNameMap.put(StringUtil.combinedMobileKey(mobile, appId), name);
            codeList.add(countryCode);
            mobileList.add(mobile);
            session.executeAsync(new BoundStatement(pstmt).bind(mobile, countryCode, user.getId(), System.currentTimeMillis(), name)
                    .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM));

            if (!deletedFriends.contains(mobile)) {
                addToRecommends.put(String.format("%s_%s", countryCode, mobile), new String[]{countryCode, mobile});
            } else {
                log.warn("this mobile:{} is already deleted by user {}", mobile, user.getId());
            }
        }

//        tcService.incr("contacts", mobileList.size());
        List<MobileIndex> mobileIndexList = userService.findMobileIndexList(mobileList, mobileCodeMap, appId);
        List<String> friendIdList = Lists.newArrayList();
        //存储联系人Id和name的map表
        Map<String, String> uidNameMap = Maps.newHashMap();

        for (MobileIndex mobileIndex : mobileIndexList) {
            String contactUid = mobileIndex.getUserId();
            String contactName = mobileNameMap.get(mobileIndex.getMobileKey().getMobile());
            //可能存在重复上传的情况，所以先判断是否还没有成为好友
            //这个步骤可以保证userList均还未成为好友,防止重复发送系统消息
            if (!friendService.isFriendOfUser(user.getId(), user.getAppId(), contactUid)) {
                friendService.addUserToFriendAsync(user, appId, contactUid, contactName, SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK);
                uidNameMap.put(contactUid, contactName);
                friendIdList.add(mobileIndex.getUserId());
            }
        }

        final List<User> userList = userService.findByIdList(friendIdList);
        // 异步处理添加好友
        this.taskService.execute(() -> {
            for (User oppoUser : userList) {
                friendService.addToContactRequest(user.getId(), oppoUser.getId(), "", SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK);

                // 如果对方的通信录里面有自己，则把自己加入到对方的好友列表中，并通知对方
                if (findUserPhoneBook(user.getCountrycode(), user.getMobile(), oppoUser.getId()) != null) {
                    messageQueueService.sendContactSuccessToOneSide(user, oppoUser);
                    friendService.addUserToFriendAsync(oppoUser, appId, user.getId(), "", SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK);
                    messageQueueService.friendAutoSayHello(oppoUser, user, uidNameMap.get(oppoUser.getId()));
                    if (addToRecommends.containsKey(String.format("%s_%s", oppoUser.getCountrycode(), oppoUser.getMobile()))) {
                        addToRecommends.remove(String.format("%s_%s", oppoUser.getCountrycode(), oppoUser.getMobile()));
                    }
                }
            }

            Collection<String[]> toBeRecommands = addToRecommends.values();
            log.debug("toBeRecommands after upload phone book. user:{},toBeRecommands:{}", user, (null != toBeRecommands) ? toBeRecommands : "");
            if (null != toBeRecommands && toBeRecommands.size() > 0) {
                friendRecommendService.uploadContactWithToBeRecomand(user, toBeRecommands);
            }
        });

        return userList;


    }

    /**
     * fixed:2016-01-18号之前 用户本地通讯录中存储的手机号码加密后 有的为小写，有的大小
     *
     * @param countryCode
     * @param mobile
     * @param userId
     * @return
     */
    public PhoneBook findUserPhoneBook(String countryCode, String mobile, String userId) {
        if (StringUtils.isBlank(mobile)) {
            return null;
        }
        PhoneBook result = manager.find(PhoneBook.class, new PhoneKey(countryCode, mobile.toLowerCase(), userId));
        if (null == result) {
            result = manager.find(PhoneBook.class, new PhoneKey(countryCode, mobile.toUpperCase(), userId));
        }
        return result;
    }

    /**
     * 根据号码查询这个号码对应的用户ID
     *
     * @param mobile 手机号
     * @return 用户列表
     */
    public List<String> findUserListByMobile(String mobile, String countryCode) {
        if (StringUtils.isBlank(mobile)) {
            return new ArrayList<>();
        }

        List<PhoneBook> phoneBooks1 = manager.sliceQuery(PhoneBook.class).forSelect().withPartitionComponents(mobile.toLowerCase()).fromClusterings(countryCode).toClusterings(countryCode).get(MAX_VALUE);

        List<PhoneBook> phoneBooks2 = manager.sliceQuery(PhoneBook.class).forSelect().withPartitionComponents(mobile.toUpperCase()).fromClusterings(countryCode).toClusterings(countryCode).get(MAX_VALUE);

        List<PhoneBook> phoneBooks = new ArrayList();

        if (null != phoneBooks1 && phoneBooks1.size() > 0) {
            phoneBooks.addAll(phoneBooks1);
        }

        if (null != phoneBooks2 && phoneBooks2.size() > 0) {
            phoneBooks.addAll(phoneBooks2);
        }

        return FluentIterable.from(phoneBooks)
                .transform(pbKeyToIdFunction)
                .filter(input -> input != null).toList();
    }


}
