package cn.v5.web.controller;

import cn.v5.bean.auth.AuthUser;
import cn.v5.bean.msg.SystemMessage;
import cn.v5.bean.notify.NotifyMessageForAuth;
import cn.v5.cache.CacheService;
import cn.v5.code.StatusCode;
import cn.v5.entity.*;
import cn.v5.service.GroupService;
import cn.v5.service.MessageQueueService;
import cn.v5.service.UserService;
import cn.v5.util.RequestUtils;
import cn.v5.validation.Validate;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.oval.constraint.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;

/**
 * Created by piguangtao on 15/4/7.
 */
@Controller
@RequestMapping(value = "/api")
@Validate
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @Value("${auth.key}")
    private String authKey;

    @Resource
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageQueueService messageQueueService;


    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Autowired
    private GroupService groupService;


    @RequestMapping(value = "/user/auth", method = RequestMethod.GET)
    @ResponseBody
    public AuthUser auth(@NotNull @NotEmpty String sessionId, @NotNull @NotEmpty String auth, Integer appId) {
        if (null == appId) {
            appId = 0;
        }

        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", sessionId, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        User user = userService.getUserInfoBySession(appId, sessionId);

        if (null == user) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "use does not exist.");
        }

        AuthUser authUser = AuthUser.formAuthUser(user);

        return authUser;
    }

    @RequestMapping(value = "/user/auth/userByMobile", method = RequestMethod.GET)
    @ResponseBody
    public AuthUser getUserByMobile(HttpServletRequest request, @NotNull @NotEmpty String mobile, @NotNull @NotEmpty String countryCode, @NotNull @NotEmpty String auth) {

        User user;

        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s%s", countryCode, mobile, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }
        Integer appId = RequestUtils.getAppId(request);
        user = userService.findUserByMobile(mobile, countryCode, appId);
        if (null == user) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "use does not exist.");
        }

        AuthUser authUser = AuthUser.formAuthUser(user);
        return authUser;
    }


    @RequestMapping(value = "/user/auth/friends", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getFriends(@NotEmpty @NotNull String userId, @NotEmpty @NotNull Integer appId, @NotEmpty @NotNull String auth) {
        List<Friend> friendList = userService.findFriendsByUserId(userId, appId);

        List<User> users = new ArrayList<>();

        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s%s", String.valueOf(appId), userId, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        if (null == friendList) {
            throw new ServerException(StatusCode.RESULT_EMPUTY, "no data");
        }

        friendList.stream().forEach(friend -> users.add(userService.findById(appId, friend.getId().getFriendId())));

        return users;
    }


    @RequestMapping(value = "/user/auth/userByAccount", method = RequestMethod.GET)
    @ResponseBody
    public AuthUser getUserByMobile(@NotNull @NotEmpty String account, @NotNull @NotEmpty String auth) {

        User user = null;

        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", account, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        AccountIndex accountIndex = userService.getAccountIndexByAccountId(account);

        if (null != accountIndex) {
            user = userService.findById(0, accountIndex.getUserId());
        }

        if (null == user) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "use does not exist.");
        }
        AuthUser authUser = AuthUser.formAuthUser(user);
        return authUser;
    }

    @RequestMapping(value = "/user/auth/userById", method = RequestMethod.GET)
    @ResponseBody
    public AuthUser getUserById(@NotEmpty @NotNull String id, @NotEmpty @NotNull String auth, Integer appId) {
        User user;

        if (null == appId) {
            appId = 0;
        }

        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", id, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }
        user = userService.findById(appId, id);

        if (null == user) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "use does not exist.");
        }
        AuthUser authUser = AuthUser.formAuthUser(user);
        return authUser;
    }


    @RequestMapping(value = "/user/auth/userByIds", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getUserByIds(@NotNull @NotEmpty String ids, @NotNull @NotEmpty String auth) {
        List<User> result = null;
        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", ids, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        List<String> idList = new ArrayList<>();
        Collections.addAll(idList, ids.split(","));

        result = userService.findByIdList(idList);

        if (null == result) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "use does not exist.");
        }

        return result;
    }

    @RequestMapping(value = "/auth/system/notify/send", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> sendNotify(@NotEmpty @NotNull String body, @NotEmpty @NotNull String auth) {
        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", body, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        try {
            NotifyMessageForAuth notifyMessage = objectMapper.readValue(body, NotifyMessageForAuth.class);
            messageQueueService.sendSysMsg(notifyMessage);

        } catch (IOException e) {
            LOGGER.error(String.format("fails to parse notify. %s", body), e);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to parse body parameter.");
        }
        return SUCCESS_CODE;
    }

    @RequestMapping(value = "/auth/system/msg/send", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> sendSystemMsg(@NotEmpty @NotNull String body, @NotEmpty @NotNull String auth) {
        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", body, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        try {
            SystemMessage message = objectMapper.readValue(body, SystemMessage.class);

            message.setPushContentBody(message.getMsgBody());

            messageQueueService.sendSysMsg(message);

        } catch (IOException e) {
            LOGGER.error(String.format("fails to parse notify. %s", body), e);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to parse body parameter.");
        }
        return SUCCESS_CODE;
    }

    @RequestMapping(value = "/auth/set/authcode", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> setAuthCode(@NotEmpty @NotNull String mobile, @NotEmpty @NotNull String auth) {
        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", mobile, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }

        for (String singleMobile : mobile.split(",")) {
            //获取手机号码的后4位
            if (singleMobile.length() < 5) {
                continue;
            }
            String authCode = singleMobile.substring(singleMobile.length() - 4);

            cacheService.setEx(singleMobile, 10 * 3600 + 1800, authCode);

        }


        return SUCCESS_CODE;
    }

    @RequestMapping(value = "/auth/group/get", method = RequestMethod.GET)
    @ResponseBody
    public Group getGroupInfo(@NotEmpty @NotNull String groupId, @NotEmpty @NotNull String reqUserId, @NotEmpty @NotNull String auth, Integer appId) {
        String expected = DigestUtils.md5DigestAsHex(String.format("%s%s", groupId, authKey).getBytes());
        if (!auth.equalsIgnoreCase(expected)) {
            LOGGER.warn("expected:{},real:{}", expected, auth);
            throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to auth user");
        }
        if (appId == null) {
            appId = 0;
        }
        User user = userService.findById(appId, reqUserId);
        if (null == user) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, "user not exist");
        }
        Group group = groupService.getGroupInfo(reqUserId, groupId, null, appId);
        if (null == group) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }
        return group;
    }

    @RequestMapping(value = "/auth/group/basic/info", method = RequestMethod.GET)
    @ResponseBody
    public Group getBasicGroupInfoWithoutAuth(@NotEmpty @NotNull String groupId) {
        Group group = groupService.findGroupInfo(groupId);
        if (null == group) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }
        return group;
    }

}
