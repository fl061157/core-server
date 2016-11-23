package cn.v5.code;

import java.util.regex.Pattern;

/**
 * @author qgan
 * @version 2014年2月20日 下午5:16:55
 */
public interface SystemConstants {
    String CLIENT_SESSION = "client-session";
    String USER_SESSION = "USER_SESSION_";

    String CLIENT_VERSION = "client-version";
    String USER_AGENT = "user-agent";
    /**
     * 服务端接口的api版本
     */
    String API_VERSION = "api-version";
    /**
     * 待发送消息表中 发送的消息接受类型 个人
     */
    String TO_SEND_MESSAGE_PERSION = "1";

    /**
     * 待发送消息表中 发送的消息接受类型 群组
     */
    String TO_SEND_MESSAGE_GROUP = "2";

    /**
     * 匹配一个或用,分割的多个手机号码
     */
    Pattern PHONE_PATTERN = Pattern
            .compile("^((13[0-9])|(147)|(15[^4,\\D])|(18[0,5-9]))\\d{8}(,((13[0-9])|(147)|(15[^4,\\D])|(18[0,5-9]))\\d{8})*$");

    /**
     * 账号登录标示
     */
    Integer LOGIN_FLAG_USERNAME = 0;

    /**
     * 设备登录标示
     */
    Integer LOGIN_FLAG_DEVICE = 1;

    /**
     * 用户状态-有效
     */
    String USER_STATUS_VALID = "1";

    /**
     * 用户状态-删除
     */
    String USER_STATUS_DELETE = "-1";

    /**
     * 修改用户的mq消息
     */
    byte MQ_TYPE_USER_MODIFY = 1;

    String MESSAGE_TYPE_TEXT = "text";

    String MESSAGE_TYPE_CARDNAME = "cardname";

    String MESSAGE_TYPE_PICURL = "picurl";

    String MESSAGE_TYPE_audio_sm = "audio_sm";

    String MESSAGE_TYPE_video_sm = "video_sm";

    String MESSAGE_TYPE_VIDEO_CALL = "video_call";

    String MESSAGE_TYPE_AUDIO_CALL = "audio_call";

    String REQUEST_TRACE_ID = "trace_id";

    String SYSTEM_ACCOUNT_SECRETARY = "88888888888888888888888888888888";

    String FRIEND_SOURCE_CG = "10";

    String FRIEND_SOURCE_LOCAL_ADDRESS_BOOK = "11";

    String FRIEND_SOURCE_WEIBO = "12";

    int CG_APP_ID_MAX = 65535;

    String FRIEND_SOURCE_FACEBOOK = "13";

    String FRIEND_SOURCE_UNKNOWN = "99";

    String SYSTEM_NOTIFY_EXTRA_KEY_TYPE = "type";

    String SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP = "group";

    String SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID = "group-id";

    String SYSTEM_NOTIFY_EXTRA_KEY_GROUP_MESSAGE_TYPE = "msg-type";

    String SYSTEM_NOTIFY_NUMBER = "number";


    String SYSTEM_NOTIFY_EXTRA_GROUP_MESSAGE_TYPE_DISMISS = "dismiss";

    String SEARCH_RANGE_ALL = "all";
    /**
     * 点击push栏是否进去会话界面
     */
    String SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION = "enter-conversation";

    /**
     *
     */
    String THIRD_PART_LOGIN = "third-part";

    String APP_STATUS_START = "start";

    String APP_STATUS_STOP = "stop";


    String DEFAULT_AUTHORIZED_GRANT_TYPES="client_credentials";
    String DEFAULT_SCOPE="read";
    String DEFAULT_RESOURCE_IDS="open-resource";
    String DEFAULT_AUTHORITIES="ROLE_CLIENT";

}
