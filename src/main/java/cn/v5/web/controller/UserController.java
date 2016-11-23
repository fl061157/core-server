package cn.v5.web.controller;

import cn.v5.bean.AvatarList;
import cn.v5.bean.Expression;
import cn.v5.bean.user.LoginSource;
import cn.v5.cache.CacheService;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.entity.*;
import cn.v5.entity.game.Account;
import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.vo.AccountUserVo;
import cn.v5.entity.vo.QueryUserVo;
import cn.v5.entity.vo.UserVo;
import cn.v5.file.FileInfo;
import cn.v5.metric.LogUtil;
import cn.v5.oauth.*;
import cn.v5.service.*;
import cn.v5.util.*;
import cn.v5.validation.Validate;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import net.sf.oval.constraint.NotEmpty;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.common.jackson.dataformat.yaml.snakeyaml.util.UriEncoder;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/api", produces = "application/json")
@Validate
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @Inject
    private UserService userService;

    @Inject
    private GroupService groupService;

    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Inject
    private TableCountService tcService;

    @Inject
    private ConversationService conversationService;

    @Inject
    private FileStoreService fileStoreService;


//    @Inject
//    private MessageSource messageSource;

    @Autowired
    private MessageSourceService messageSourceService;

    @Inject
    private CpuService cpuService;

    @Autowired
    private RequestUtils requestUtils;

    @Inject
    private HttpService httpService;

    @Inject
    private MobileUtils mobileUtils;

    @Value("${send.sms.real}")
    private String realSend;

    @Value("${get.ip.server}")
    private String ipServer;

    @Value("${auth.key.sms}")
    private String authStr;

    @Value("${day.ip.num}")
    private String dayIpNum;

    @Value("${user.expression.list}")
    private String userExpressions;

    @Value("${user.expression.prefix}")
    private String userExpressionPrefix;

    @Value("${user.avatar.list}")
    private String avatars;

    @Value("${async.timeout}")
    private long asyncTimeout;


    @Autowired
    private LogUtil logUtil;

    @Autowired
    private SmsService smsService;

    @Autowired
    private CountryAuthTypeUtil authTypeUtil;

    @Autowired
    private PhoneFormatUtil phoneFormatUtil;

    @Value("${phone.authcode.enable.localnumber}")
    private String phoneEnableLocalnumer;

    @Autowired
    private PhoneUtil phoneUtil;

    @Autowired
    private OpenPlatformServiceFactory openPlatformServiceFactory;

    @Autowired
    private BindService bindService;

    @Autowired
    private OAuthUploadService oAuthUploadService;


    @RequestMapping(value = "/user/bind/device", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public AccountUserVo bindDevice(HttpServletRequest request, @NotNull @NotEmpty String mobile, @NotNull Integer app_id, @NotNull Integer device_type, String authcode,
                                    String language, String countrycode, Integer sex, String os_version, String public_key, String cert) {
        User user = null;

        if (countrycode == null) countrycode = "0086";
//        mobile = StringUtil.fixMobile(countrycode, StringUtil.clearMobileNo(mobile));
        language = language == null ? "en" : language.replace("-", "_");
        Locale locale = LocaleUtils.parseLocaleString(language);
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        String clientVersion = RequestUtils.getClientSoftName(request);

//        userService.checkVersion(device_type, app_id, cert, requestUtils.getCurrentClientVersion(request), locale);
        int newUser = 0;

        if (authcode == null || "".equals(authcode)) {
            userService.sendAuthCode(requestUtils.getUA(request), requestUtils.getClientVersion(request), mobile, countrycode, realSend, locale, null, app_id);
            throw new ServerException(StatusCode.CHECK_MOBLIE_CODE, messageSourceService.getMessageSource(app_id).getMessage("code.verify", new Object[]{}, locale));
        }

        //输入验证码在一段时间内次数限制
        if (phoneUtil.authCodeRetryOverLimit(countrycode, mobile)) {
            throw new ServerException(StatusCode.OVER_LIMIT, messageSourceService.getMessageSource(app_id).getMessage("request.limit", new Object[]{}, locale));
        }

        String storedAuthcode = cacheService.get(mobile);
        log.info("obtained authcode is: {}", storedAuthcode);
        if (!authcode.equals(storedAuthcode)) {
            userService.logUserAuthCodeResult(countrycode, mobile, authcode, storedAuthcode, false, "bind");
            throw new ServerException(StatusCode.CHECK_CODE_FAIL, messageSourceService.getMessageSource(app_id).getMessage("code.verify.failed", new Object[]{}, locale));
        } else {
            userService.logUserAuthCodeResult(countrycode, mobile, authcode, storedAuthcode, true, "bind");
        }
        String saltHashMobile = mobileUtils.saltHash(mobile);

        MobileIndex mobileIndex = userService.isExistMobile(saltHashMobile, countrycode, app_id);

        //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
        if (null == mobileIndex) {
            //采用明文查找手机号码是否存在
            mobileIndex = userService.isExistMobile(mobile, countrycode, app_id);
        }

        if (null != mobileIndex) {
            user = userService.findById(app_id, mobileIndex.getUserId());
            //用户手机号码表存在，但是用户表没有该数据时，需要重新生成用户数据
            if (null == user) {
                mobileIndex = null;
            } else {
                user.setLanguage(language);
            }
        }

        //切记，不能使用else
        if (mobileIndex == null) {
            String combinedMobileKey = StringUtil.combinedMobileKey(saltHashMobile, app_id);
            user = userService.createNewUser(mobile, combinedMobileKey, language, countrycode, clientVersion, null, userService.getUserDefaultAvatarAccessKey(), sex, app_id);
            tcService.incr("users_activate", 1);//激活累计
            newUser = 1;
            userService.userRegister(user);
            //记录用户注册话单,报表统计的注册用户的话单 根据的url为/api/user/register,需要记录的url需要是这一个
            String record = logUtil.formLogInfo(user, request, null, "/api/user/register");
            logUtil.logReq(record);
        } else {
            //记录用户重新登陆的话单
            String record = logUtil.formLogInfo(user, request, null, "/api/user/bind/device");
            logUtil.logReq(record);
        }

        if (user != null) {
            String ip = requestUtils.getClientIP(request);
            if (log.isDebugEnabled()) {
                log.debug("[UserController bindDevice],ip:{}", ip);
            }
            userService.saveUserLoginState(user, app_id == null ? 0 : app_id, public_key, clientSession, ip, newUser, UserAgentUtils.getUserAgent(request), requestUtils.getClientVersion(request));
            if (null == user.getTcpServer()) {
                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSourceService.getMessageSource(app_id).getMessage("tcp.addr.failed", new Object[]{}, locale));
            }
        }
        AccountUserVo accountUserVo = AccountUserVo.createFromUser(user);
        if (userService.canBeModified(user.getId()))
            accountUserVo.setCanBeModified(1);
        else
            accountUserVo.setCanBeModified(0);
        return accountUserVo;
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    @ResponseBody
    public AccountUserVo register(HttpServletRequest request, @NotNull @NotEmpty String mobile, @NotNull Integer app_id, @NotNull Integer device_type, String authcode,
                                  String language, String countrycode, Integer sex, String os_version, String public_key, String cert, String nickname) {
        MultipartFile file = null;
        String contentType = request.getContentType();
        Integer appID = RequestUtils.getAppId(request);
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            log.debug("multipart, need get MultipartFile from request");
            MultipartHttpServletRequest multipartRequest =
                    WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
            file = multipartRequest.getFile("file");
        }

        User user = null;

        if (countrycode == null || "".equalsIgnoreCase(countrycode)) countrycode = "0086";
//        mobile = StringUtil.fixMobile(countrycode, StringUtil.clearMobileNo(mobile));
        language = language == null ? "en" : language.replace("-", "_");
        Locale locale = LocaleUtils.parseLocaleString(language);
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        String clientVersion = requestUtils.getClientSoftName(request);

//        userService.checkVersion(device_type, app_id, cert, requestUtils.getCurrentClientVersion(request), locale);
        int newUser = 0;

        if (authcode == null || "".equals(authcode)) { //发送短信验证码校验
            userService.sendAuthCode(requestUtils.getUA(request), requestUtils.getClientVersion(request), mobile, countrycode, realSend, locale, null, appID);
            throw new ServerException(StatusCode.CHECK_MOBLIE_CODE, messageSourceService.getMessageSource(appID).getMessage("code.verify", new Object[]{}, locale));
        }

        //输入验证码在一段时间内次数限制
        if (phoneUtil.authCodeRetryOverLimit(countrycode, mobile)) {
            throw new ServerException(StatusCode.OVER_LIMIT, messageSourceService.getMessageSource(appID).getMessage("request.limit", new Object[]{}, locale));
        }

        String storedAuthcode = cacheService.get(mobile);
        if (!authcode.equals(storedAuthcode)) {
            userService.logUserAuthCodeResult(countrycode, mobile, authcode, storedAuthcode, false, "register");
            throw new ServerException(StatusCode.CHECK_CODE_FAIL, messageSourceService.getMessageSource(appID).getMessage("code.verify.failed", new Object[]{}, locale));
        } else {
            userService.logUserAuthCodeResult(countrycode, mobile, authcode, storedAuthcode, true, "register");
        }

        String saltHashMobile = mobileUtils.saltHash(mobile);
        MobileIndex mobileIndex = userService.isExistMobile(saltHashMobile, countrycode, app_id);

        //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
        if (null == mobileIndex) {
            //采用明文查找手机号码是否存在
            mobileIndex = userService.isExistMobile(mobile, countrycode, app_id);
        }

        if (null != mobileIndex) {
            user = userService.findById(mobileIndex.getUserId());
            //用户手机号码表存在，但是用户表没有该数据时，需要重新生成用户数据
            if (null == user) {
                mobileIndex = null;
            } else {
                user.setLanguage(language);
            }
        }

        if (mobileIndex == null) { //第一次使用,新用户
            if (StringUtils.isBlank(nickname)) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, messageSourceService.getMessageSource(appID).getMessage("missing.parameter", new Object[]{}, locale));
            }
            FileInfo fileInfo = null;
            String fileUrl = null;
            if (file != null) {
                try {
//                    fileInfo = fileStoreService.storeAvatar(file.getInputStream(), file.getOriginalFilename(), file.getSize());
//                    if (fileInfo != null) {
//                        accesskey = fileInfo.getAccessKey();
//                    }

                    fileUrl = fileStoreService.storeAvatarToFileServer(file, null, countrycode);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new ServerException(StatusCode.UPLOAD_FILE_FAILED, messageSourceService.getMessageSource(appID).getMessage("upload.file.failed", new Object[]{}, locale));
                }
            } else {
                //没有传头像时，使用默认的头像
                fileUrl = userService.getUserDefaultAvatarAccessKey();
            }
            //如果手机号为空 直接帮助其注册然后返回
            String combinedMobileKey = StringUtil.combinedMobileKey(saltHashMobile, app_id);
            user = userService.createNewUser(mobile, combinedMobileKey, language, countrycode, clientVersion, nickname, fileUrl, sex, app_id);

//            //保存头像信息入库
//            if (fileInfo != null) {
//                fileStoreService.storeAvatarPersistentFile(fileInfo, user.getId());
//            }
            //新用户标记
            newUser = 1;
            tcService.incr("users_activate", 1);//激活累计
            userService.userRegister(user);
            //记录用户注册话单
            logUtil.logReq(user, request);
            userService.addUserBehaviouAttrAsync(user.getId(), user.getAppId(), requestUtils.getUA(request), requestUtils.getClientVersion(request));
        }

        if (user != null) {
            userService.saveUserLoginState(user, app_id == null ? 0 : app_id, public_key, clientSession, requestUtils.getClientIP(request), newUser, UserAgentUtils.getUserAgent(request), requestUtils.getClientVersion(request));
            if (null == user.getTcpServer()) {
                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSourceService.getMessageSource(appID).getMessage("tcp.addr.failed", new Object[]{}, locale));
            }
        }
        AccountUserVo accountUserVo = AccountUserVo.createFromUser(user);

        if (userService.canBeModified(user.getId())) {
            accountUserVo.setCanBeModified(1);
        } else {
            accountUserVo.setCanBeModified(0);
        }
        return accountUserVo;

    }


    @RequestMapping(value = "/user/oauth_login", method = RequestMethod.POST)
    @ResponseBody
    public User oauthLogin(HttpServletRequest request, @NotNull @NotEmpty Integer app_id, @NotNull @NotEmpty String id,
                           @NotNull @NotEmpty String nickname, @NotNull @NotEmpty String prefix, @NotNull Integer device_type,
                           String language, String countrycode,
                           String os_version, String public_key, String cert, Integer sex) {
        Account account = userService.isExistUserById(id, prefix);
        User user;
        language = StringUtils.isEmpty(language) ? "en" : language;
        Locale locale = LocaleUtils.parseLocaleString(language);
        String userName = prefix + "_" + id;
        String passwd = userName;
        if (account == null) {
            //如果手机号为空 直接帮助其注册然后返回
            user = userService.createNewUser("", "game", language, countrycode, null, nickname, null, sex, app_id);
            log.info("app_id[{}] create new user[{}] device_type[{}] cert[{}] language[{}] countrycode[{}] nickname[{}] sex[{}] prefix[{}]",
                    app_id, userName, device_type, cert, language, countrycode, nickname, sex, prefix);
            userService.addAccount(userName, passwd, user.getId(), app_id, prefix, userName);
        } else {
            if (userService.isReportedMuch(account.getUserId(), app_id)) {
                throw new ServerException(StatusCode.GAME_LOGIN_FAILED, "you have been reported by someone");
            }
            user = userService.findById(0, account.getUserId());
            user.setLanguage(language);
            user.setNickname(nickname);
        }
        if (user != null) {
            userService.saveUserLoginState(user, app_id == null ? 0 : app_id, null, null, requestUtils.getClientIP(request), 0, UserAgentUtils.getUserAgent(request), requestUtils.getClientVersion(request));
            if (null == user.getTcpServer()) {
                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSourceService.getMessageSource(app_id).getMessage("tcp.addr.failed", new Object[]{}, locale));
            }
        }
        return user;
    }

    @RequestMapping(value = "/user/oauth2_login", method = RequestMethod.POST)
    @ResponseBody
    public User oauth2Login(HttpServletRequest request, @NotNull @NotEmpty Integer which, @NotNull @NotEmpty String code, String public_key, Integer useToken) {

        Openplatform openplatform = Openplatform.getOpenplatform(which);

        Integer app_id = RequestUtils.getAppId(request);
        OpenPlatformService openPlatformService = openPlatformServiceFactory.findOpenPlatformService(openplatform, app_id);

        if (openplatform == null) {
            log.error("NotSupport this type:{} of platform", which);
            throw new ServerException(StatusCode.INNER_ERROR, String.format("NotSupport this type:%d of platform", which));
        }

        String clientSession = RequestUtils.getSession(request);
        String clientVersion = RequestUtils.getClientSoftName(request);


        OAuth2AccessToken accessToken;

        if (useToken != null && useToken == 1) {
//            if (openplatform != Openplatform.FaceBook) {
//                throw new ServerException(StatusCode.INNER_ERROR, String.format("NotSupport userToken which:%d of platform", which));
//            }
            accessToken = new OAuth2AccessToken(code);
        } else {
            try {
                accessToken = openPlatformService.getAccessToken(code);
            } catch (Exception e) {
                log.error("access token error platform:{} , code:{} ", which, code, e);
                throw new ServerException(StatusCode.INVALID_OPEN_AUTHCODE, String.format("access token type:%d of platform error", which));
            }
        }

        if (accessToken == null) {
            log.error("access token type:{} of platform error , code:{} ", which, code);
            throw new ServerException(StatusCode.INVALID_OPEN_AUTHCODE, String.format("access token type:%d of platform error", which));
        }

        OAuthUser oAuthUser;

        try {
            oAuthUser = openPlatformService.findOAuthUser(accessToken);
        } catch (OAuthException e) {
            log.error("[OAuth] findOAuthUser error code:{} , message:{} ", e.getCode(), e.getMessage());
            throw new ServerException(e.getCode(), e.getMessage());
        }

        if (oAuthUser == null) {
            log.error("[OAuth] findOAuthUser empty !");
            throw new ServerException(StatusCode.OAUTH_REQUEST_ERROR, OAuthException.OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE);
        }

        OAuthUserHelper.wrapperOAuthUser(oAuthUser, app_id);

        ThirdAppCgUser thirdAppCgUser = bindService.findBindUser(oAuthUser.getUnionID(), Openplatform.getOpenplatform(which));
        User user;

        String headURL = null;
        try {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(oAuthUser.getHeadimgurl())) {
                headURL = oAuthUploadService.upload(oAuthUser.getHeadimgurl(), oAuthUser.getUnionID(), oAuthUser.getCountryCode(), "jpeg");
            }
        } catch (Exception e) {
            log.error("Upload HeadUrl:{} error ", oAuthUser.getHeadimgurl(), e);
        }

        if (thirdAppCgUser == null) {

            user = userService.createNewUser(openplatform.formatUnioID(oAuthUser.getUnionID()), SystemConstants.THIRD_PART_LOGIN, oAuthUser.getLanguage(),
                    oAuthUser.getCountryCode(), clientVersion, oAuthUser.getNickName(),
                    StringUtils.isNotBlank(headURL) ? headURL : oAuthUser.getHeadimgurl(), oAuthUser.getSex(), app_id);

            String record = logUtil.formLogInfo(user, request, null, "/api/user/register");
            logUtil.logReq(record);

            bindService.createThirdAppCgUser(oAuthUser.getUnionID(), openplatform, user.getId(), accessToken.getAccessToken());
            bindService.createCgThirdAppUser(user.getId(), openplatform, oAuthUser.getUnionID());
        } else {
            user = userService.findById(app_id, thirdAppCgUser.getKey().getUserId());

            if (StringUtils.isBlank(user.getAccount())) {
                try {
                    user = userService.updateUserAccount(user);
                    if (log.isInfoEnabled()) {
                        log.info("[UpdateUserAccount] user.id:{} , user.account:{}", user.getId(), user.getAccount());
                    }
                } catch (Exception e) {
                    log.error("[UpdateUserAccount] fail", e);
                }
            }

            if (org.apache.commons.lang3.StringUtils.isBlank(user.getLanguage())) {
                user.setLanguage(oAuthUser.getLanguage());
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(user.getNickname())) {
                user.setNickname(oAuthUser.getNickName());
            }

            if (StringUtils.isNotBlank(headURL)) {
                user.setAvatar(headURL);
            }

            user.setSex(oAuthUser.getSex());

            userService.updateUser(user);

            bindService.updayeThirdAppCgUserToken(oAuthUser.getUnionID(), openplatform, user.getId(), accessToken.getAccessToken());
        }

        if (user != null) {
            userService.saveUserLoginState(user, app_id, public_key, clientSession, requestUtils.getClientIP(request), 0, UserAgentUtils.getUserAgent(request), requestUtils.getClientVersion(request));
            if (null == user.getTcpServer()) {
                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSourceService.getMessageSource(app_id).getMessage("tcp.addr.failed", new Object[]{}, LocaleUtils.parseLocaleString(user.getLanguage())));
            }

            user.setThirdPartID(oAuthUser.getUnionID()); //返回第三方ID


        } else {
            log.error("[OAuth] user empty oAuthUser.nickname:{} , openplatform:{} ", oAuthUser.getNickName(), openplatform.name());
        }

        return user;
    }


    /**
     * 通过第三方登陆接口
     *
     * @param id         在所在的第三方中唯一的ID
     * @param source     第三方
     * @param avatar_url 第三方头像地址
     */
    @RequestMapping(value = "/user/oauth/login", method = RequestMethod.POST)
    @ResponseBody
    public User thirdPartLogin(HttpServletRequest request, @NotNull @NotEmpty String id,
                               @NotNull @NotEmpty String nickname, @NotNull @NotEmpty Integer source, @NotNull Integer device_type,
                               String language, String countrycode,
                               String os_version, String public_key, String cert, Integer sex, String avatar_url) {
        Integer app_id = RequestUtils.getAppId(request);
        String clientSession = RequestUtils.getSession(request);
        String clientVersion = RequestUtils.getClientSoftName(request);
        String prefix = LoginSource.getPrefix(source);
        Account account = userService.isExistUserById(id, prefix);
        User user;
        language = StringUtils.isEmpty(language) ? "en" : language;
        Locale locale = LocaleUtils.parseLocaleString(language);
        String userName = prefix + "_" + id;
        String passwd = userName;
        if (account == null) {
            //为复用接口第三方登陆的情况将account 从mobile中传入
            user = userService.createNewUser(userName, SystemConstants.THIRD_PART_LOGIN, language, countrycode, clientVersion, nickname, avatar_url, sex, app_id);
            log.info("app_id[{}] create new user[{}] device_type[{}] cert[{}] language[{}] countrycode[{}] nickname[{}] sex[{}] prefix[{}]",
                    app_id, userName, device_type, cert, language, countrycode, nickname, sex, prefix);
            userService.addAccount(userName, passwd, user.getId(), app_id, prefix, userName);
        } else {
            if (userService.isReportedMuch(account.getUserId(), app_id)) {
                throw new ServerException(StatusCode.GAME_LOGIN_FAILED, "you have been reported by someone");
            }
            user = userService.findById(app_id, account.getUserId());
            user.setLanguage(language);
            user.setNickname(nickname);
        }
        if (user != null) {
            userService.saveUserLoginState(user, app_id, public_key, clientSession, requestUtils.getClientIP(request), 0, UserAgentUtils.getUserAgent(request), requestUtils.getClientVersion(request));
            if (null == user.getTcpServer()) {
                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSourceService.getMessageSource(app_id).getMessage("tcp.addr.failed", new Object[]{}, locale));
            }
        }
        return user;
    }

    /**
     * 用户退出设置
     */
    @RequestMapping(value = "/user/logout", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> logout(HttpServletRequest request) {
        User user = CurrentUser.user();
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        //移除之前的用户的token
//        userService.removeToken(clientSession);
        userService.cleanUserDataByUnusedSession(clientSession, requestUtils.getAppId(request));
        userService.cleanUserDataByUserId(user.getId(), user.getAppId(), true);
        return SUCCESS_CODE;
    }

    /**
     * 第三方应用根据用户的token，进行授权，成功返回user
     *
     * @return
     */
    @RequestMapping(value = "/user/auth", method = RequestMethod.GET)
    @ResponseBody
    public User auth(HttpServletRequest request) {
        User user = CurrentUser.user();
        String ip = requestUtils.getClientIP(request);

        if (ArrayUtils.isEmpty(user.getTcpServer()) || ArrayUtils.isEmpty(user.getFileServer())) {
//            String tcps = userService.findServerAddr(user, ip);
//            String[] ips;
//            try {
//                ips = JsonUtil.fromJson(tcps, String[].class);
//            } catch (IOException e) {
//                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, "Connection error.Please try again later.");
//            }

            Map<String, String[]> serverIPS = userService.findServerAddress(user, ip);

            user.setTcpServer(serverIPS.get("tcp_server")); //TODO
            user.setFileServer(serverIPS.get("file_server")); //TODO
        }

        return user;
    }

    /**
     * 获得用户信息
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public UserVo userInfo(HttpServletRequest request, @NotNull @NotEmpty String id) {
        User you = CurrentUser.user();
        Integer appID = RequestUtils.getAppId(request);
        if (appID > SystemConstants.CG_APP_ID_MAX) {
            id = UserUtils.genInternalUserId(id, appID);
        }
        UserVo result = userService.getUserInfoIncludeRelation(you, id, you.getAppId());

        if (result == null) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, messageSourceService.getMessageSource(appID).getMessage("user.not.exist", new Object[]{}, you.getLocale()));
        }
        if (appID > SystemConstants.CG_APP_ID_MAX) {
            result.setId(UserUtils.genOpenUserId(result.getId()));
        }
        return result;
    }


    @RequestMapping(value = "/user/account", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> findUser(HttpServletRequest request, @NotNull @NotEmpty String account) {
        Integer appID = RequestUtils.getAppId(request);
        User user = this.userService.getUserByAccountID(account, appID);
        Group group = this.groupService.getGroupByAccount(account, appID);
        if (user == null && group == null) {
            log.error("account:{}  not exists", account);
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, String.format("account:%s", account));
        }
        Map<String, Object> ret = new HashedMap();
        ret.put("error_code", StatusCode.SUCCESS);
        if (user != null) {
            ret.put("user", user);
        }
        if (group != null) {
            ret.put("group", group);
        }
        return ret;
    }


    /**
     * 获得用户或群组的信息
     * 传入参数可能是用户mobile account 或群组account
     */
    @RequestMapping(value = "/user/mobile", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> findUser(HttpServletRequest request, @NotNull @NotEmpty String mobile, String range) {
        Map<String, Object> ret = Maps.newHashMap();

        int appID = RequestUtils.getAppId(request);

        // 根据account查找是否有匹配
        if (StringUtils.isNotBlank(mobile)) {
            log.debug(String.format("mobile or account:%s", mobile));
            mobile = UriEncoder.decode(mobile);
            log.debug(String.format("mobile or account after encode:%s", mobile));
        }
        User you = CurrentUser.user();
        final List<User> userListTmp = new ArrayList<>();
        // 处理mobile，去掉前导加号和0，抽取所有数字
        String[][] probMobiles = mobileUtils.extractMobileWithoutCountryCode(mobileUtils.extractNumbersFromMobile(mobile));
        for (String[] mob : probMobiles) {
            List<User> ul = userService.findUserByMobileIndex(mobileUtils.saltHash(mob[1]), you.getAppId());

            //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
            if (ul == null || ul.isEmpty()) {
                ul = userService.findUserByMobileIndex(mob[1], you.getAppId());
            }

            if (ul != null && !ul.isEmpty()) {
                String countryCode = mob[0];
                if (countryCode.isEmpty()) {
                    userListTmp.addAll(ul);
                } else {
                    for (User u : ul) {
                        if (countryCode.equalsIgnoreCase(u.getCountrycode())) {
                            userListTmp.add(u);
                        }
                    }
                }
            }
        }

        User accountMatchedUser = this.userService.getUserByAccoundId(mobile);

        Set<String> userMobileSet = new HashSet();
        final List<QueryUserVo> userList = new ArrayList<>();
        // 加入到结果列表中
        if (accountMatchedUser != null) {
            QueryUserVo userVo = QueryUserVo.createFromUser(accountMatchedUser, false);
            if (!SystemConstants.SYSTEM_ACCOUNT_SECRETARY.equals(userVo.getId())) {
                userList.add(userVo);
                userMobileSet.add(accountMatchedUser.getCountrycode() + ":" + accountMatchedUser.getMobile());
            }
        }
        // 排除小秘书
        for (User u : userListTmp) {
            if (!userMobileSet.contains(u.getCountrycode() + ":" + u.getMobile())) {
                QueryUserVo userVo = QueryUserVo.createFromUser(u, true);
                if (!SystemConstants.SYSTEM_ACCOUNT_SECRETARY.equals(userVo.getId())) {
                    userList.add(userVo);
                    userMobileSet.add(u.getCountrycode() + ":" + u.getMobile());
                }
            }
        }
        if (userList.isEmpty() && !SystemConstants.SEARCH_RANGE_ALL.equals(range)) {
            throw new ServerException(StatusCode.USER_INFO_NOT_EXIST, messageSourceService.getMessageSource(you.getAppId()).getMessage("user.not.exist", new Object[]{}, you.getLocale()));
        }


        List<QueryUserVo> users = userList.stream().filter(user -> user != null && (user.getAppId() == null || user.getAppId().equals(appID))).collect(Collectors.toList());
        ret.put("users", users);


        //搜索符合条件的群组
        if (StringUtils.isNotEmpty(range) && SystemConstants.SEARCH_RANGE_ALL.equals(range)) {
            Group group = this.groupService.getGroupByAccount(mobile);
            List<Group> groups = Lists.newArrayList();
            if (group != null) {

                try {
                    User creator = userService.findById(group.getCreator());
                    if (creator != null) {
                        if (creator.getAppId() != null && !(creator.getAppId() == appID)) {
                            log.info("[Not This AppId User] user:{} , group:{} ", creator.getAppId(), group.getId());
                        } else {
                            groups.add(group);
                        }
                    } else {
                        groups.add(group);
                    }
                } catch (Exception e) {
                    groups.add(group);
                }
            }
            ret.put("groups", groups);
        }
        return ret;

    }

//    /**
//     * 获得用户Session id    TODO: 根据用户的手机号获取用户的session id，这个只用于测试用，正式环境一定要去掉
//     */
//    @RequestMapping(value = "/user/sessionid", method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String, String> userSessionId(@NotNull @NotEmpty String mobile) {
//        User you = CurrentUser.user();
//        String sessionId = "0";
//        final List<User> userList = userService.findUserByMobileIndex(mobile);
//        if (!userList.isEmpty()) {
//            User user = userList.get(0);
//            final String userKey = RedisCacheKey.USER_NAMEMD5_SESSION + user.getId() + "_" + 0;
//            sessionId = cacheService.get(userKey);
//        }
//
//        Map<String, String> res = Maps.newHashMap();
//        res.put("session_id", sessionId);
//        return res;
//    }

    /**
     * 通过手机号判断是否注册过
     */
    @RequestMapping(value = "/user/exist", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> userInfoByMo(@NotNull @NotEmpty String mobile, @NotNull @NotEmpty String
            countrycode, HttpServletRequest request) {
        Integer appId = RequestUtils.getAppId(request);
        String saltHashMobile = userService.saltMobile(mobile, countrycode);
        Map<String, Object> result = new HashMap();
        MobileIndex mobileIndex = userService.isExistMobile(saltHashMobile, countrycode, appId);

//        //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
//        if (null == mobileIndex) {
//            //采用明文查找手机号码是否存在
//            mobileIndex = userService.isExistMobile(mobile, countrycode);
//        }

        if (mobileIndex == null) {
            result.put("status", 0);
        } else {
            result.put("status", 1);
        }

        //获取手机号码支持的短信验证码方式
        String smTypes = authTypeUtil.getAuthType(countrycode);

        if (StringUtils.isNotBlank(smTypes)) {
            result.put("sm_type", smTypes);
        }

        return result;

    }


    @RequestMapping(value = "/user/change_mobile", method = RequestMethod.POST)
    @ResponseBody
    public User changeMobile(HttpServletRequest request, @NotNull @NotEmpty String mobile, String authcode, String
            countrycode) {

        User you = CurrentUser.user();
        Integer appID = RequestUtils.getAppId(request);
        countrycode = StringUtils.isBlank(countrycode) ? you.getCountrycode() : countrycode;
        String saltMobile = userService.saltMobile(mobile, countrycode);
        MobileIndex mobileIndex = userService.isExistMobile(saltMobile, countrycode, you.getAppId());
        mobileIndex = mobileIndex != null ? mobileIndex : userService.isExistMobile(mobile, countrycode, you.getAppId());
        if (mobileIndex != null) {
            throw new ServerException(StatusCode.MOBILE_ALREADY_EXISTS, messageSourceService.getMessageSource(appID).getMessage("mobile.already.exist", new Object[]{}, you.getLocale()));
        }
        if (StringUtils.isBlank(authcode)) {
            userService.sendAuthCode(requestUtils.getUA(request), requestUtils.getClientVersion(request), mobile, you.getCountrycode(), realSend, you.getLocale(), null, appID);
            throw new ServerException(StatusCode.CHECK_MOBLIE_CODE, messageSourceService.getMessageSource(appID).getMessage("mobile.exist", new Object[]{}, you.getLocale()));
        }
        if (!authcode.equals(cacheService.get(mobile))) {
            throw new ServerException(StatusCode.CHECK_CODE_FAIL, messageSourceService.getMessageSource(appID).getMessage("code.verify.failed", new Object[]{}, you.getLocale()));
        }

        User user = userService.findById(you.getAppId(), you.getId());
        String oldmobile = user.getMobile();
        String oldcountry = user.getCountrycode();
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        user.setMobile(saltMobile);
        user.setCountrycode(countrycode);
        user.setMobileVerify(1);
        user.setMobilePlaintext(mobile);
        userService.updateUser(user, clientSession);
        if (!saltMobile.equals(oldmobile) || !countrycode.equals(oldcountry)) {
            userService.removeMobileIndexMobile(oldmobile, you.getCountrycode());
            mobileIndex = new MobileIndex();
            mobileIndex.setMobileKey(new MobileKey(saltMobile, countrycode));
            mobileIndex.setUserId(user.getId());
            userService.saveMobile(mobileIndex);

        }
        userService.userChange(user);
        return user;
    }

    /**
     * 获得用户信息
     */
    @Deprecated
    @RequestMapping(value = "/user/modify_mobile", method = RequestMethod.POST)
    @ResponseBody
    public User modifyMobile(HttpServletRequest request, @NotNull @NotEmpty String mobile, String authcode, String
            countrycode) {
        User you = CurrentUser.user();
        User user = userService.findById(you.getAppId(), you.getId());
        Integer appID = RequestUtils.getAppId(request);
        if (user != null && user.getMobileVerify() == 1) {

            throw new ServerException(StatusCode.USER_ALREADY_VERIFY, messageSourceService.getMessageSource(appID).getMessage("user.already.verify", new Object[]{}, you.getLocale()));

        }

        if (StringUtils.isBlank(countrycode)) {
            countrycode = you.getCountrycode();
        }

        String saltMobile = userService.saltMobile(mobile, countrycode);

        MobileIndex mobileIndex = userService.isExistMobile(saltMobile, countrycode, you.getAppId());
        //数据库 手机号码加密数据切割 兼容 ，待数据切割完成后，再删除
        if (null == mobileIndex) {
            //采用明文查找手机号码是否存在
            mobileIndex = userService.isExistMobile(mobile, countrycode, you.getAppId());
        }

        //1.2 IOS版本兼容处理
        if (null != mobileIndex && null != authcode && !"".equals(authcode) && authcode.equals(cacheService.get(mobile))) {
            log.debug("active user.userId:{}", null != user ? user.getId() : null);
            userService.activeUserByModifyPhone(user);
            return user;
        }

        if (mobileIndex != null) {//注意：只要是存在此手机号，就不能修改。否则会使用当前用户替换此手机号的原用户，造成用户信息丢失。

            throw new ServerException(StatusCode.MOBILE_ALREADY_EXISTS, messageSourceService.getMessageSource(appID).getMessage("mobile.already.exist", new Object[]{}, you.getLocale()));

        } else if (authcode == null || "".equals(authcode)) {

            userService.sendAuthCode(requestUtils.getUA(request), requestUtils.getClientVersion(request), mobile, you.getCountrycode(), realSend, you.getLocale(), null, appID);
            throw new ServerException(StatusCode.CHECK_MOBLIE_CODE, messageSourceService.getMessageSource(appID).getMessage("mobile.exist", new Object[]{}, you.getLocale()));

        }

        if (!authcode.equals(cacheService.get(mobile))) {

            throw new ServerException(StatusCode.CHECK_CODE_FAIL, messageSourceService.getMessageSource(appID).getMessage("code.verify.failed", new Object[]{}, you.getLocale()));

        }

        String oldmobile = user.getMobile();
        String oldcountry = user.getCountrycode();

        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        user.setMobile(saltMobile);
        user.setCountrycode(countrycode);
        user.setMobileVerify(1);
        user.setMobilePlaintext(mobile);

        //激活累计
        tcService.incr("users_activate", 1);

        userService.updateUser(user, clientSession);


        if (!saltMobile.equals(oldmobile) || !countrycode.equals(oldcountry)) {

            //移除之前的手机号
            userService.removeMobileIndexMobile(oldmobile, you.getCountrycode());

            mobileIndex = new MobileIndex();
            mobileIndex.setMobileKey(new MobileKey(saltMobile, countrycode));
            mobileIndex.setUserId(user.getId());

            userService.saveMobile(mobileIndex);

        }
        userService.userChange(user);

//        final User friend = user; //发送好友注册信息
//        this.taskService.execute(new Runnable() {
//            @Override
//            public void run() {
//                List<Friend> friends = userService.findFollowers(friend.getId(), friend.getAppId());
//                messageQueueService.sendUserUpdateMsg(friends, friend);
//            }
//        });


        return user;
    }


    /**
     * 根据用户的ip地址来选择，而不是简单通过国家区号来判断
     * 获得用户信息
     */
    @RequestMapping(value = "/server/addr", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String[]> getServerAddr(HttpServletRequest request) {
        String ip = requestUtils.getClientIP(request);
        if (StringUtils.isBlank(ip)) {
            throw new ServerException(StatusCode.PARAMETER_ERROR, "非法参数");
        }
        String countryCode = request.getHeader("region-code");
//        String res = userService.findServerAddr(countryCode, ip);
//        if (StringUtils.isBlank(res) || "[]".equals(res)) {
//            throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSource.getMessage("tcp.addr.failed", new Object[]{}, CurrentUser.user().getLocale()));
//        }
//
//        String[] ips;
//        try {
//            ips = JsonUtil.fromJson(res, String[].class);
//        } catch (IOException e) {
//            throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSource.getMessage("tcp.addr.failed", new Object[]{}, CurrentUser.user().getLocale()));
//        }
//        Map<String, String[]> map = new HashMap<String, String[]>();
//
//        map.put("tcp_server", ips);
//        map.put("file_server", ips);

        return userService.findServerAddress(countryCode, ip);
    }


    /**
     * 用户免打扰设置
     * （必填）disable=yes 是否禁止打扰。yes 禁止打扰 no 不启动免打扰功能
     * （可选）time=01:00-02:20 免打扰的时间端，如果不携带此参数，以服务器的设置时间段为准。
     */
    @RequestMapping(value = "/user/disturb", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> disturb(HttpServletRequest request, String disable, String time) {
//        String clientSession = request.getHeader(RedisCacheKey.CLIENT_SESSION);
//        User user = CurrentUser.user();
//        userService.setUserDisturb(user, disable, time, clientSession);
        //2.x版本不存在免打扰设置，并且1.x版本的免打扰设置也需要屏蔽掉
        return SUCCESS_CODE;
    }


    /**
     * 更新用户信息
     */
    @RequestMapping(value = "/user/upload", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> uploadUserInfo(HttpServletRequest request, String nickname, String
            avatar_url, Integer sex) {
        User user = CurrentUser.user();
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        String clientVersion = requestUtils.getClientSoftName(request);
        userService.uploadUserInfo(user, clientVersion, avatar_url, nickname, sex, clientSession);
        return SUCCESS_CODE;
    }


    //发送手机验证码
    @RequestMapping(value = "/phone/authcode", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> sendCode(HttpServletRequest request, @NotNull @NotEmpty String mobile,
                                        @NotNull @NotEmpty String currentTime, @NotNull @NotEmpty String authkey, String countrycode, String
                                                real_send,
                                        String localeCode, String language, String type, String localnumber) {
        Map<String, Object> result = new HashMap<>();
        result.put("error_code", StatusCode.SUCCESS);
        Integer appID = RequestUtils.getAppId(request);

        Locale locale = LocaleUtils.parseLocaleString(language != null && !language.isEmpty() ? language : localeCode, Locale.US);
        countrycode = countrycode == null ? "0086" : countrycode;
        String authStrBuild = authStr + mobile + currentTime;
        String sendSmsKey = DigestUtils.md5Hex(authStrBuild);
        log.debug("authStrBuild:{},sendSmsKey:{},authKey:{}, type:{}", authStrBuild, sendSmsKey, authkey, type);
        if (!sendSmsKey.equalsIgnoreCase(authkey)) {
            throw new ServerException(StatusCode.UPLOAD_AVATAR_FAIL, messageSourceService.getMessageSource(appID).getMessage("key.verify.failed", new Object[]{}, locale));
        }

        if (real_send != null) {
            realSend = "yes";
        }


        //是否直接返回验证码
        if (StringUtils.isNotEmpty(localnumber)) {
            if ("yes".equalsIgnoreCase(phoneEnableLocalnumer)) {
                log.debug("[authcode] local number. mobile:{} ,countrycode:{}, localnumber:{}", mobile, countrycode, localnumber);
                if (localnumber.endsWith(mobile)) {
                    String authcode = smsService.getAuthCode(mobile);
                    smsService.saveAuthCode(mobile, authcode);
                    result.put("verify_code", authcode);
                    log.debug("[authcode] local number. mobile:{} ,countrycode:{}, localnumber:{}, return authcode:{}", mobile, countrycode, localnumber, authcode);
                    return result;
                }
            }
        }


        String key = SmsService.KEY_LIMIT_REQ + mobile;

        if (smsService.isResend(key)) {
            return result;
        }

        if ("yes".equals(realSend) && cacheService.get(key) != null) {
            throw new ServerException(StatusCode.OVER_LIMIT, messageSourceService.getMessageSource(appID).getMessage("request.limit", new Object[]{}, locale));
        }

        String ipaddr = requestUtils.getClientIP(request);
        log.debug("analyse request ip is {}", ipaddr);
        long ipNum = cacheService.incBy(ipaddr, 1);
        if (ipNum == 1) {
            cacheService.expire(ipaddr, 24 * 60 * 60);
        }

        String dayNum = dayIpNum == null ? "50" : dayIpNum;
        if (ipNum >= Long.valueOf(dayNum) && "yes".equals(realSend)) {
            log.warn("sendCode: get user locale=[{}]", locale);
            throw new ServerException(StatusCode.OVER_LIMIT, messageSourceService.getMessageSource(appID).getMessage("ip.request.limit", new Object[]{}, locale));
        }
        log.info("ip:" + ipaddr + "request num=" + ipNum);
        if (!userService.sendAuthCode(requestUtils.getUA(request), requestUtils.getClientVersion(request), mobile, countrycode, realSend, locale, type, appID)) {
            throw new ServerException(StatusCode.AUTH_CODE_SEND_FAIL, messageSourceService.getMessageSource(appID).getMessage("sms.send.failed", new Object[]{}, locale));
        }

        return result;
    }

    /**
     * 把用户/群加入/移除出灰名单，接收消息，但离线时不推送
     *
     * @param entity_id
     * @param type      1:grey 2:black 4:top
     */
    @RequestMapping(value = "/user/conversation/add", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> userAddConversation(@NotNull @NotEmpty String entity_id, @NotNull Integer type) {
        User user = CurrentUser.user();

        conversationService.add(user.getId(), entity_id, type, user.getAppId());
        return SUCCESS_CODE;
    }

    /**
     * 把 用户/群 移除出灰名单，接收消息，但离线时不推送
     *
     * @param entity_id
     */
    @RequestMapping(value = "/user/conversation/remove", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> userRemoveConversation(@NotNull @NotEmpty String entity_id, @NotNull Integer type) {
        User user = CurrentUser.user();
        conversationService.remove(user.getId(), entity_id, type, user.getAppId());
        return SUCCESS_CODE;
    }

    /**
     * 更新推送Token
     *
     * @param app_id       来自哪个应用ID
     * @param device_token 推送Token
     */
    @RequestMapping(value = "/device/token", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> deviceToken(@NotNull @NotEmpty String device_token, Integer
            app_id, @NotNull Integer device_type, String provider) {
        User you = CurrentUser.user();
        if (null == app_id) {
            app_id = you.getAppId();
        }
        userService.saveToken(device_token, app_id, you.getId(), device_type, provider);
        return SUCCESS_CODE;
    }

    /**
     * 检查更新，获取最新的客户端版本号
     *
     * @param app_id      1:露脸 2:嘟嘟视频
     * @param device_type 1:IOS 2:Android
     */
    @RequestMapping(value = "/client/version", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public VersionControl clientVersion(@NotNull Integer app_id, @NotNull Integer device_type, String cert) {
        if (StringUtils.isBlank(cert)) {
            cert = "0";
        }
        return userService.getVersionInfo(device_type, app_id, cert);
    }


    /**
     * 判断Account是否存在 以及是否可修改
     */
    @RequestMapping(value = "/user/account_exist", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> accountCheck(@NotEmpty String account) {
        Map<String, Integer> ret = Maps.newHashMap();
        if (userService.getAccountIndexByAccountId(account) != null) {
            ret.put("is_exist", 1);
        } else
            ret.put("is_exist", 0);
        return ret;
    }

    @RequestMapping(value = "/user/updatelocale", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> updateLocale(@NotEmpty String language, @NotEmpty String country) {
        User you = CurrentUser.user();
        try {
            userService.updateLanguage(you, language, country);
        } catch (Exception e) {
            log.error("update locale error user.id:{} ", you.getId(), e);
            Map<String, Integer> ret = Maps.newHashMap();
            ret.put("error_code", StatusCode.INNER_ERROR);
        }
        return SUCCESS_CODE;
    }


    /**
     * 更新用户Account
     */
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> updateUser(@NotEmpty String account) {
        User you = CurrentUser.user();
        Map<String, Integer> ret = Maps.newHashMap();
        //不验证
//        if (you.getAccount()==null||userService.getAccountIndexByAccountId(you.getAccount()) == null) {
//            log.error("user account [{}] does not exist. user=[{}]", you.getAccount(), you.getId());
//            ret.put("error_code",StatusCode.ACCOUNT_NOT_FOUND);
//            return ret;
//        }
        if (!account.matches("^[a-z_0-9]+$")) {
            ret.put("error_code", StatusCode.ACCOUNT_INVALID);
            return ret;
        }
        if (!userService.canBeModified(you.getId())) {
            ret.put("error_code", StatusCode.ACCOUNT_MODIFIED_ERROR);
            return ret;
        }

        AccountIndex accountIndex = null;
        if (you.getAccount() != null) {
            accountIndex = userService.getAccountIndexByAccountId(you.getAccount());
        }
        String pwd = null;
        if (accountIndex != null) {
            pwd = accountIndex.getPassword();
        }

        String oldAccount = you.getAccount();

        try {
            userService.createAccountIndex(account, you.getId(), pwd);
        } catch (AchillesLightWeightTransactionException e) {
            throw new ServerException(StatusCode.ACCOUNT_EXIST, "account already exist.");
        } catch (Exception e1) {
            throw new ServerException(StatusCode.ACCOUNT_MODIFIED_ERROR, "fails to modifiy user account");
        }

        try {
            you.setAccount(account);
            userService.modifyUser(you);
            userService.userChange(you);
        } catch (Exception e1) {
            throw new ServerException(StatusCode.ACCOUNT_MODIFIED_ERROR, "fails to modifiy user account");
        }

        try {
            if (StringUtils.isNotBlank(oldAccount) && !oldAccount.equals(account)) {
                userService.removeAccountIndex(oldAccount);
            }
            userService.createAccountHistory(you.getId(), account);
        } catch (Exception e1) {
            log.error("fails to operate db.", e1);
            //忽略异常，用户信息已经更新
        }

        return SUCCESS_CODE;
    }


    @RequestMapping(value = "/user/expression_list", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List> getExpressionList() {
        Map<String, List> ret = Maps.newHashMap();
        List<Expression> expressionList = Lists.newArrayList();
        String listData = userExpressions;
        String prefix = userExpressionPrefix;
        if (listData != null && prefix != null) {
            String[] names = listData.split(";");
            int i = 0;
            String urlIOS = null;
            String urlAndroid = null;
            String urlData = null;
            String dataid = null;
            for (String e : names) {
                dataid = String.format("%032d", i);
                urlIOS = String.format("%sios/%032d.png", prefix, i);
                urlAndroid = String.format("%sandroid/%032d.png", prefix, i);
                urlData = String.format("%s%032d.zip", prefix, i);
                expressionList.add(new Expression(dataid, e.trim(), urlAndroid, urlIOS, urlData));
                i++;
            }
        }
        ret.put("expression", expressionList);
        return ret;
    }


    /**
     * 获取一个模型列表并且可以下载
     *
     * @return 模型列表
     */
    @RequestMapping(value = "/user/avatarlist", method = RequestMethod.GET)
    @ResponseBody
    public Object getUserAvatarList() {
        AvatarList al = null;
        if (AvatarList.getAvatarList() == null) {
            String listData = avatars;
            if (listData != null) {
                String[] list = listData.split(";");
                List<AvatarList.AvatarInfo> infoList = new ArrayList<>();
                for (String e : list) {
                    String[] parts = e.trim().split(",");
                    infoList.add(new AvatarList.AvatarInfo(parts[0], parts[1], parts[2]));
                }
                AvatarList avatarList = new AvatarList(infoList.toArray(new AvatarList.AvatarInfo[1]));
                AvatarList.setAvatarList(avatarList);
            }
        }
        al = AvatarList.getAvatarList();
        return al;
    }

    @RequestMapping(value = "/cpu", method = RequestMethod.GET)
    @ResponseBody
    public Integer cpu(@NotNull Integer num, @NotNull Integer hz) {
        return cpuService.getCpu(num, hz);
    }


    @RequestMapping(value = "/user/game_login", method = RequestMethod.POST)
    @ResponseBody
    public User gameLogin(HttpServletRequest request, @NotNull @NotEmpty String user_name, @NotNull Integer app_id,
                          @NotNull String passwd, @NotNull Integer device_type, String language, String countrycode,
                          String os_version, String public_key, String cert, String nickname, Integer sex) {
        MultipartFile file = null;
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            log.debug("multipart, need get MultipartFile from request");
            MultipartHttpServletRequest multipartRequest =
                    WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
            file = multipartRequest.getFile("file");
        }

        String ip = requestUtils.getClientIP(request);
        countrycode = "0086";
        if (StringUtils.isNotBlank(ip)) {
            Map<String, ?> map = Maps.newHashMap();
            String res = this.httpService.doGet(ipServer + "/" + ip, map);
            if (StringUtils.isNotBlank(res)) {
                countrycode = StringUtils.substring(res, 11, res.length() - 2);
            }
        }
        language = language == null ? "en" : language.replace("-", "_");
        Locale locale = LocaleUtils.parseLocaleString(language);
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        String clientVersion = requestUtils.getClientSoftName(request);
//        userService.checkVersion(device_type, app_id, cert, requestUtils.getCurrentClientVersion(request), locale);

        User user = null;
        Account account = userService.isExistUserName(user_name, app_id);
        // login operation
        if (StringUtils.isBlank(nickname)) {
            if (account == null) {
                throw new ServerException(StatusCode.GAME_USER_NOT_EXIST, messageSourceService.getMessageSource(app_id).getMessage("game.user.not.exist", new Object[]{}, locale));
            } else if (account.getPassword() != null && !BCrypt.checkpw(passwd, account.getPassword())) {
                throw new ServerException(StatusCode.GAME_PASSWD_ERR, messageSourceService.getMessageSource(app_id).getMessage("game.passwd.error", new Object[]{}, locale));
            } else {
                if (userService.isReportedMuch(account.getUserId(), app_id)) {
                    throw new ServerException(StatusCode.GAME_LOGIN_FAILED, "you have been reported by someone");
                }
                user = userService.findById(app_id, account.getUserId());
                user.setLanguage(language);
            }
        } else {
            if (account == null) {
                FileInfo fileInfo = null;
                String fileUrl = null;
                if (file != null) {
                    try {
//                        fileInfo = fileStoreService.storeAvatar(file.getInputStream(), file.getOriginalFilename(), file.getSize());
//                        if (fileInfo != null) {
//                            accesskey = fileInfo.getAccessKey();
//                        }

                        // Account为空，没有用户ID
                        fileUrl = fileStoreService.storeAvatarToFileServer(file, null, countrycode);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw new ServerException(StatusCode.UPLOAD_FILE_FAILED, messageSourceService.getMessageSource(app_id).getMessage("upload.file.failed", new Object[]{}, locale));
                    }
                } else {
                    //没有传头像时，使用默认的头像
                    fileUrl = userService.getUserDefaultAvatarAccessKey();
                }
                //如果手机号为空 直接帮助其注册然后返回
                log.info("app_id[{}] create new user[{}] device_type[{}] cert[{}] language[{}] countrycode[{}] nickname[{}] sex[{}] prefix[{}]",
                        app_id, user_name, device_type, cert, language, countrycode, nickname, sex, "");
                user = userService.createNewUser("", "game" + UUIDGen.getTimeUUID().toString(), language, countrycode, clientVersion, nickname, fileUrl, sex);

                userService.addAccount(user_name, passwd, user.getId(), app_id);

//                //保存头像信息入库
//                if (fileInfo != null)
//                    fileStoreService.storeAvatarPersistentFile(fileInfo, user.getId());
            } else {
                throw new ServerException(StatusCode.GAME_USER_NOT_EXIST, messageSourceService.getMessageSource(app_id).getMessage("game.user.exist", new Object[]{}, locale));
            }
        }

        if (user != null) {
            userService.saveUserLoginState(user, app_id == null ? 0 : app_id, public_key, clientSession, requestUtils.getClientIP(request), 0, UserAgentUtils.getUserAgent(request), requestUtils.getClientVersion(request));
            if (null == user.getTcpServer()) {
                throw new ServerException(StatusCode.TCP_ADDR_NOT_EXISTS, messageSourceService.getMessageSource(app_id).getMessage("tcp.addr.failed", new Object[]{}, locale));
            }
        }

        return user;
    }
}
