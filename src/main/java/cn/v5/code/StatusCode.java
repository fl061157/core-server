package cn.v5.code;

/**
 * Created by hi on 14-3-8.
 */
public interface StatusCode {
    int UNAUTHORIZED_CODE = 401;
    int APP_STATUS_INVALID = 402;
    int INNER_ERROR = 500; // 内部错误
    int SUCCESS = 2000;   //成功操作返回
    int PARAMETER_ERROR = 4001; //参数错误

    //群组相关状态码
    int CHECK_MOBLIE_CODE = 4002;//验证手机短信码
    int CHECK_CODE_FAIL = 4003;  //验证失败
    int UPLOAD_USER_INFO_OK = 4004;
    int UPLOAD_AVATAR_OK = 4005;
    int UPLOAD_AVATAR_FAIL = 4006;
    int DEL_CONTACT_OK = 4007;
    int ADD_CONTACT_FAIL = 4008;
    int ADD_DEVICE_TOKEN_OK = 4009;
    int OVER_LIMIT = 4010;       //超过次数限制
    int OBJECT_NOT_FOUND = 4011; //对象不存在
    int LOGIN_ACCOUNT_ERROR = 4012;//登录账号错误，需要绑定
    int AUTH_CODE_SEND_FAIL = 4013; //短信验证码发送失败
    int AUTH_CODE_FORCE_UPGRADE = 4014; //版本太旧，需要强制升级到最新版
    int TCP_ADDR_NOT_EXISTS = 4015; //TCP地址没有获取到
    int UPLOAD_CRASH_LOG_OK = 4016;  //
    int UPLOAD_CRASH_LOG_FAIL = 4017;  //
    int USER_INFO_NOT_EXIST = 4018;  //用户信息不存在
    int GROUP_INFO_NOT_EXIST = 4019;  //群信息不存在
    int GROUP_OPT_NOT_ALLOW = 4020;  //没有权限
    int GROUP_NOT_REMOVE_OWN = 4021;  //不能移除自己
    int FRIEND_NOT_ADD_OWN = 4021;  //不能移除自己
    int UPLOAD_FILE_EMPTY = 4022;
    int UPLOAD_FILE_FAILED = 4023;
    int USER_ALREADY_VERIFY = 4024;
    int MOBILE_ALREADY_EXISTS = 4025;
    int GET_FILE_SIZE_ERROR = 4026;   // 获取文件大小错误
    int GET_COUNTRY_CODE_ERROR = 4027;   // 获取不到国家码

    int GET_VERSION_INFO_ERROR = 4028;   // 获取版本信息错误

    int RADAR_ADD_ERROR = 4029;            //加入数字雷达失败
    int RADAR_EXIT_ERROR = 4030;

    int GET_GEO_LOCATION_ERROR = 4031;   // 获取不到地理位置信息

    int ACCOUNT_NOT_FOUND = 4032;  //账户不存在
    int ACCOUNT_MODIFIED_ERROR = 4033;  //账户超过可修改次数
    int ACCOUNT_INVALID = 4034;  //账户超过可修改次数
    int ACCOUNT_EXIST = 4035;

    int GROUP_VERIFY = 4041; //开启了群组校验

    int GROUP_MEMBER_LIMIT = 4042; //超过了群组成员数目

    int USER_ALREADY_IN_GROUP = 4043; //用户已经加入了该群组
    int CLIENT_NEEDS_UPGRADE = 4044; //版本太旧 需要更新

    int INVALID_TOKEN = 4100;

    int INVALID_PHONE = 4101;


    int INVALID_OPEN_AUTHCODE = 5001;
    int INVALID_OPEN_TOKEN = 5002;

    int OAUTH_CONNECTION_ERROR = 5004;
    int OAUTH_ACCESSTOKEN_ERROR = 5005;
    int OAUTH_REQUEST_ERROR = 5006;


    // 游戏错误码
    int GAME_NOT_EXIST = 6000;
    int GAME_PLAYER_NOT_EXIST = 6001;
    int GAME_PLAYER_LACK_POWER = 6002;
    int GAME_USER_NOT_EXIST = 6020;
    int GAME_PASSWD_ERR = 6021;
    int GAME_LOGIN_SUC = 6022;
    int GAME_LOGIN_FAILED = 6023;

    // 音乐游戏
    int GAME_SONG_GET_SONGBAG_ERR = 6050;
    int GAME_SONG_UPDATE_SCORE_ERR = 6051;

    int RESULT_EMPUTY = 7001;
    int OAUTH_CLIENT_NOT_EXISTS = 7050;
    int APP_STOPPED = 7051;
    int APP_NOT_AUDIT = 7052;

    int TRADE_FAIL_ERROR = 8001;
    int TRADE_NOT_EXISTS = 8002;


}
