package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.entity.*;
import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.vo.ThirdAppRecommendUser;
import cn.v5.entity.vo.UserVo;
import cn.v5.service.*;
import cn.v5.util.*;
import cn.v5.validation.Validate;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.sf.oval.constraint.NotEmpty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "/api", produces = "application/json")
@Validate
public class FriendController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @Inject
    private UserService userService;

    @Inject
    private FriendService friendService;

    @Inject
    private MessageQueueService messageQueueService;

    @Autowired
    private MessageSourceService messageSourceService;


    @Inject
    private PhoneBookService phoneBookService;

    @Inject
    private ConversationService conversationService;

    @Inject
    private MobileUtils mobileUtils;

    @Autowired
    private RequestUtils requestUtils;

    @Autowired
    private FriendRecommendService friendRecommendService;

    @Autowired
    private GroupService groupService;


    /**
     * 增量上传联系人，判断去重
     * 包含联系人的名称
     *
     * @param contact [["00101396458933","小西"],["00861396458943","小刀"]]
     * @param app_id
     * @return
     */
    @RequestMapping(value = "/contacts/upload_with_name", method = RequestMethod.POST)
    @ResponseBody
    public Map uploadContactsWithName(@NotNull @NotEmpty String contact, @NotNull Integer app_id, Integer hashed) {
        User you = CurrentUser.user();

        List<List<String>> mobileNames;
        try {
            contact.replaceAll("\n", " ").replaceAll("\\\\", "");
            mobileNames = JsonUtil.fromJson(contact, List.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "非法参数");
        }

        List<User> userList = phoneBookService.uploadPhoneBookWithName(you, mobileNames, app_id, hashed);

        return contactResult(you, userList);
    }

    /**
     * 增量上传联系人，判断去重
     *
     * @return
     */
    @RequestMapping(value = "/contacts/upload", method = RequestMethod.POST)
    @ResponseBody
    public Map uploadContacts(@NotNull @NotEmpty String phone, @NotNull Integer app_id, Integer hashed) {
        User you = CurrentUser.user();
        List<User> userList = phoneBookService.uploadPhoneBook(you, ImmutableList.copyOf(phone.split(",")), app_id, hashed);

        return contactResult(you, userList);
    }

    private Map<String, List<UserVo>> contactResult(User you, List<User> userList) {
        final Map<String, Conversation> conversationMap = conversationService.findByUserId(you.getId());

        if (null == userList) {
            userList = new ArrayList<>();
        }

        final List<UserVo> userVoList = FluentIterable.from(userList).transform(new Function<User, UserVo>() {
            @Override
            public UserVo apply(User input) {
                UserVo vo = new UserVo();
                Conversation c = conversationMap.get(input.getId());
                input.setConversation(c == null ? 0 : c.getType());

                BeanUtils.copyProperties(input, vo);
                return vo;
            }
        }).filter(input -> input != null).toList();

        return new HashMap<String, List<UserVo>>() {{
            put("person", userVoList);
        }};
    }

    /**
     * 从服务器获取嘟嘟好友
     *
     * @return
     */
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List> contacts(HttpServletRequest request, Integer app_id) {
        User you = CurrentUser.user();

        if (app_id == null || app_id == 0) {
            Integer appID = RequestUtils.getAppId(request);
            if (appID != null && appID != 0) {
                app_id = appID;
            } else {
                app_id = 0;
            }
        }

        List<UserVo> friends = userService.findFriendUserList(you, you.getId(), app_id);
        //群组这一期隐藏
        List<Group> groups = groupService.findGroupsByUserId(you.getId());
        Map<String, List> mapResult = new HashMap<>();
        mapResult.put("person", friends);
        if (null == groups || groups.size() < 1) {
            mapResult.put("group", null);
        } else {
            mapResult.put("group", groups);
        }

        return mapResult;
    }

    /**
     * 从服务器获取嘟嘟好友 和指定resource app id的好友
     *
     * @return
     */
    @RequestMapping(value = "/app_contacts", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List> contacts(Integer app_id, Integer resource_app_id) {
        User you = CurrentUser.user();
        List<User> friends = userService.findFriendUserList(you.getId(), (app_id == null) ? 0 : app_id, resource_app_id);

        Map<String, List> mapResult = new HashMap<>();
        mapResult.put("person", friends);
        mapResult.put("group", null);
        return mapResult;
    }

    /**
     * 根据用户的md5，添加联系人
     *
     * @param uids
     * @param src_app_id 好友来源的appid
     * @param dst_app_id 添加到dst_app_id这个应用的联系人列表
     * @return
     */
    @RequestMapping(value = "/contact/addByUid", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> addContactByUid(HttpServletRequest request, String uids, Integer src_app_id, Integer dst_app_id, String msg) {
        if (requestUtils.isChatgameV1(request)) {
            return addContactByUidV1(uids, src_app_id, dst_app_id);
        }

        User you = CurrentUser.user();
        LOGGER.debug("add friends {}", uids);
        String[] ids = StringUtils.split(uids, ",");

        for (String id : ids) {
            if (id.equals(you.getId()))
                continue;
            User user = userService.findById(you.getAppId(), id);
            LOGGER.debug("add friend {} is null {} ? ", id, user == null);
            if (null != user) {
                LOGGER.debug("send contact request to user=[{}]", user.getId());
                if (friendService.isUserSentRequestOrFriend(you, user)) {
                    friendService.makeFriend(you, user, null, SystemConstants.FRIEND_SOURCE_CG);
                    friendService.makeFriend(user, you, null, SystemConstants.FRIEND_SOURCE_CG);
                    messageQueueService.sendContactSuccess(you, user);
                } else {
                    if (msg == null) {
                        msg = "";
                    }
                    friendService.addToContactRequest(you.getId(), user.getId(), msg, SystemConstants.FRIEND_SOURCE_CG);
                    messageQueueService.sendContactRequest(you, user, msg);
                }
            }
        }
        return SUCCESS_CODE;
    }


    /**
     * 1.x版本兼容，等升级到2.0版本需要删除此方法
     *
     * @param uids
     * @param src_app_id
     * @param dst_app_id
     * @return
     * @deprecated
     */
    private Map<String, Integer> addContactByUidV1(String uids, Integer src_app_id, Integer dst_app_id) {
        User you = CurrentUser.user();
        LOGGER.debug("add friends {}", uids);
        String[] ids = StringUtils.split(uids, ",");

        for (String id : ids) {
            if (id.equals(you.getId()))
                continue;
            User user = userService.findById(you.getAppId(), id);
            LOGGER.debug("add friend {} is null {} ? ", id, user == null);
            if (null != user) {
                LOGGER.debug("add user {} to friends", user.getId());
                UserKey key = new UserKey(you.getId(), (dst_app_id == null) ? 0 : dst_app_id, user.getId());
                Friend friend = new Friend();
                friend.setId(key);
                friend.setResourceAppId(src_app_id);
                friend.setUpdateTime(System.currentTimeMillis());
                userService.saveFriend(friend);
            }
        }
        return SUCCESS_CODE;
    }

    /**
     * 添加嘟嘟好友
     * source: 添加好友的来源
     *
     * @return
     */
    @RequestMapping(value = "/contact/add", method = RequestMethod.POST)
    @ResponseBody
    public Object addContact(HttpServletRequest request, @NotNull @NotEmpty String key, @NotNull @NotEmpty String countrycode, String source, String msg) {

        if (StringUtils.isBlank(source)) {
            source = SystemConstants.FRIEND_SOURCE_CG;
        }

        User you = CurrentUser.user();

        //如果多个手机号则取第一个
        int idx = key.indexOf(",");
        if (idx != -1) {
            key = key.substring(0, idx);
        }
        String saltHashMobile = key;
        if (key.length() < 32) {
            LOGGER.warn("add contact mobile {} len lt 32", key);
            key = StringUtil.clearMobileNo(key);
            saltHashMobile = mobileUtils.saltHash(key);
        }
        User user = null;
        MobileIndex mobileIndex = userService.findMobileIndex(saltHashMobile, countrycode, you.getAppId());

        //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
        if (mobileIndex == null) {
            mobileIndex = userService.findMobileIndex(key, countrycode, you.getAppId());
        }

        if (mobileIndex == null) {
            user = userService.findById(you.getAppId(), key);
            if (user == null) {
                throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, messageSourceService.getMessageSource(you.getAppId()).getMessage("user.not.exist", new Object[]{}, you.getLocale()));
            }
        }

        if (user == null && mobileIndex != null) {
            user = userService.findById(you.getAppId(), mobileIndex.getUserId());
        }

        if (!user.getId().equals(you.getId())) {
            friendService.addFriend(user, you, source, msg);
            return SUCCESS_CODE;

        } else {

            throw new ServerException(StatusCode.FRIEND_NOT_ADD_OWN, messageSourceService.getMessageSource(you.getAppId()).getMessage("no.allow.add", new Object[]{}, you.getLocale()));
        }

    }


    @RequestMapping(value = "/contact/addFriend", method = RequestMethod.POST)
    @ResponseBody
    public Object addContact(HttpServletRequest request, @NotNull @NotEmpty String id, String source, String message) {
        if (StringUtils.isBlank(source)) {
            source = SystemConstants.FRIEND_SOURCE_CG;
        }

        User you = CurrentUser.user();
        User user = userService.findById(you.getAppId(), id);
        if (user == null) {

            user = this.userService.getUserByAccoundId(id);
            if (user == null) {
                throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "User Not exists user.id: " + id);
            }
        }

        String msg = StringUtils.isNotBlank(message) ? message : String.format("%s添加你为好友!", user.getNickname());

        if (!user.getId().equals(you.getId())) {
            friendService.addTwoFriend(user, you, source, msg);
            return SUCCESS_CODE;
        } else {
            throw new ServerException(StatusCode.FRIEND_NOT_ADD_OWN, messageSourceService.getMessageSource(you.getAppId()).getMessage("no.allow.add", new Object[]{}, you.getLocale()));
        }
    }


    /**
     * 删除嘟嘟好友
     *
     * @return
     */
    @RequestMapping(value = "/contact/del", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> contactDel(@NotNull @NotEmpty String name, @NotNull Integer app_id) {
        User you = CurrentUser.user();
        User user = userService.findById(you.getAppId(), name);

        if (user != null) {
            userService.removeFriend(app_id, you, user);

        }
        return SUCCESS_CODE;
    }

    /**
     * @param friend_id
     * @param contact_name 为空时表示删除备注
     * @param app_id
     * @return
     */
    @RequestMapping(value = "/contact/name", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> updateContact(HttpServletRequest request, @NotNull @NotEmpty String friend_id, String contact_name, Integer app_id) {
        User loggedUser = CurrentUser.user();

        Integer appID;
        if (app_id == null || app_id == 0) {
            appID = RequestUtils.getAppId(request);
        } else {
            appID = app_id;
        }

        if (!userService.updateContactName(loggedUser, friend_id, appID, contact_name)) {
            LOGGER.warn("update contact name failure,user.id = {}, friend_id = {}, contact_name = {}", loggedUser.getId(), friend_id, contact_name);
        }
        return SUCCESS_CODE;
    }


    /**
     * @param timestamp
     * @param source    可能认识的人 all表示所有可能认识的人 包括本地通讯录、第三方app如微博、facetime等
     * @return
     */
    @RequestMapping(value = "/friend/recommend", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> recommend(Long timestamp, String source, HttpServletRequest request) {

        int appID = RequestUtils.getAppId(request);

        User user = CurrentUser.user();
        Object[] recommendFriends = friendService.recommend(user.getId(), user.getMobile(), user.getCountrycode(), timestamp == null ? 0 : timestamp, source, appID);

        Map<String, Object> result = Maps.newHashMap();
        result.put("total", recommendFriends[0]);
        result.put("person", recommendFriends[1]);
        return result;
    }

    @RequestMapping(value = "/friend/accept", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> friendRequestAccept(@NotNull @NotEmpty String user) {
        User you = CurrentUser.user();
        String selfUserId = you.getId();
        String promoterId = user;

        User promoter = userService.findById(you.getAppId(), promoterId);
        LOGGER.debug("request accepted by {} and add friend {} is null {} ? ", selfUserId, promoterId, user == null);
        // 同时，检查是否已经加入到了请求列表
        if (null != user) {
            ContactRequest contactRequest = friendService.getUserContactRequest(promoter.getId(), you.getId());
            String source = SystemConstants.FRIEND_SOURCE_UNKNOWN;

            //fixed 好友邀请方和邀请接受方不在同一区时，好友请求的数据可能还没有同步过来 可以作为降级处理 避免数据同步影响
            if (null != contactRequest) {
                source = contactRequest.getSource();
            }
            friendService.makeFriend(promoter, you, null, StringUtils.isNotBlank(source) ? source : SystemConstants.FRIEND_SOURCE_CG);

            //添加好友请求方在接受方的本地通讯录中
            if (friendService.isUserInPhoneBook(promoter, you)) {
                LOGGER.debug("user:{} in user:{} local address book", user, you.getId());
                friendService.makeFriend(you, promoter, null, SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK);
            } else {
                friendService.makeFriend(you, promoter, null,
                        StringUtils.isNotBlank(source) && (!SystemConstants.FRIEND_SOURCE_LOCAL_ADDRESS_BOOK.equalsIgnoreCase(source)) ? source : SystemConstants.FRIEND_SOURCE_CG);
            }
            // 发送系统消息
            messageQueueService.sendContactSuccess(promoter, you);
        }

        return SUCCESS_CODE;
    }

    @RequestMapping(value = "/friend/request/clear", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> clearFriendRequest() {
        User you = CurrentUser.user();
        friendService.clearFriendRequest(you);
        return SUCCESS_CODE;
    }

    /**
     * 获取第三方可能认识的人
     *
     * @param source
     * @param access_token
     * @param other_app_user_id
     * @param page
     * @param count
     * @return
     */
    @RequestMapping(value = "/friend/recommend/otherApp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> recommendFromOtherApp(@NotNull @NotEmpty String source, String access_token, String other_app_user_id, Integer page, Integer count) {
        User currentUser = CurrentUser.user();
        Map<String, Object> result = new HashMap();

        //用户的token入库 或者更新
        ThirdAppCgUser thirdAppCgUser = userService.initThirdAppUserInfo(currentUser.getId(), other_app_user_id, access_token, source);
        LOGGER.debug("[recommendFromOtherApp].source:{},access_token:{},other_app_user_id:{},thirdAppCgUser:{}", source, access_token, other_app_user_id, thirdAppCgUser);
        if (null == thirdAppCgUser) {
            throw new ServerException(StatusCode.PARAMETER_ERROR, "no corresponding user");
        }
        List<ThirdAppRecommendUser> thirdAppUsers = friendRecommendService.getRecommendFromThirdApp(thirdAppCgUser);

        //发布获取第三方用户的好友事件
        friendRecommendService.thirdAppFriends(currentUser, thirdAppUsers, source);

        if (null != thirdAppUsers && thirdAppUsers.size() > 0) {
            //获取可能认识的人
            Object[] userAndTotal = friendRecommendService.recommendByThirdApp(currentUser, thirdAppUsers, source, page, count);
            if (userAndTotal != null && userAndTotal.length == 2) {
                result.put("total", userAndTotal[0]);
                result.put("person", userAndTotal[1]);
            }
        } else {
            result.put("total", 0);
        }
        return result;
    }

}
