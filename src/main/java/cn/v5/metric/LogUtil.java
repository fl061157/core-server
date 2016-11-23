package cn.v5.metric;

import cn.v5.entity.Group;
import cn.v5.entity.User;
import cn.v5.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Created by piguangtao on 15/4/9.
 */
@Service
public class LogUtil {

    private static Logger LOGGER_ANALYZE_USER = LoggerFactory.getLogger("cn.v5.metric.ReqAnalyzeInterceptor_USER");

    private static Logger LOGGER_ANALYZE_NO_USER = LoggerFactory.getLogger("cn.v5.metric.ReqAnalyzeInterceptor_NOUSER");

    public static final String LOG_SPLIT_CHAR = "|";

    private static final DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public void logReq(User user, HttpServletRequest request) {
        logReq(user, request, null, null);
    }

    public void logReq(User user, HttpServletRequest request, List<String> customFields, String url) {
        if (null != user) {
            String loggerHead;
            if (request != null && StringUtils.isNotBlank(loggerHead = (String) request.getAttribute(ReqAnalyzeInterceptor.PREFIX))) {
                try {
                    MDC.put(ReqAnalyzeInterceptor.PREFIX, loggerHead);
                    LOGGER_ANALYZE_USER.info(formLogInfo(user, request, customFields, url));
                } finally {
                    MDC.remove(ReqAnalyzeInterceptor.PREFIX);
                }
            } else {
                LOGGER_ANALYZE_USER.info(formLogInfo(user, request, customFields, url));
            }
        } else {
            LOGGER_ANALYZE_NO_USER.info(formLogInfo(null, request, customFields, url));
        }
    }

    public void logReq(String record) {
        if (org.apache.commons.lang.StringUtils.isBlank(record)) return;
        try {
            MDC.put(ReqAnalyzeInterceptor.PREFIX, ReqAnalyzeInterceptor.getLogPrefix());
            LOGGER_ANALYZE_USER.info(record);
        } finally {
            MDC.remove(ReqAnalyzeInterceptor.PREFIX);
        }

    }

    /**
     * 分析日志格式
     * 时间|uri|userId|account|nickName|mobile|countryCode
     *
     * @param request
     * @return
     */
    public String formLogInfo(User user, HttpServletRequest request, List<String> customFields, String uri) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != user) {
            String userId = "";
            String mobile = "";
            String nickName = "";
            String countryCode = "";
            String account = "";
            String createTime = "";
            long lCreateTime = 0L;
            String gender = "";
            if (null != user) {
                userId = user.getId();
                mobile = user.getMobilePlaintext();
                nickName = user.getNickname();
                nickName = delKeywords(nickName);
                countryCode = user.getCountrycode();
                account = user.getAccount();
                account = delKeywords(account);
                if (null != user.getCreateTime()) {
                    createTime = logDateFormat.format(user.getCreateTime());
                    lCreateTime = user.getCreateTime().getTime();
                }
                gender = null != user.getSex() ? String.valueOf(user.getSex()) : "";
            }

            String remoteIp = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(remoteIp)) {
                remoteIp = request.getHeader("remote-host");
                if (null == remoteIp) {
                    remoteIp = "";
                }
            }

            String ua = request.getHeader("user-agent");

            if (null == ua) {
                ua = "";
            }

            String clientVersion = request.getHeader("client-version");
            if (null == clientVersion) {
                clientVersion = "";
            }

            String clientSession = request.getHeader("client-session");
            if (null == clientSession) {
                clientSession = "";
            }

            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(org.apache.commons.lang.StringUtils.isBlank(uri) ? request.getRequestURI() : uri)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(userId)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(clientSession)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(account)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(nickName)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(mobile)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(countryCode)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(ua)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(clientVersion)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(remoteIp)).append(LOG_SPLIT_CHAR)
                    .append(lCreateTime).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(createTime)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(gender));
            if (null != customFields && customFields.size() > 0) {
                for (String field : customFields) {
                    stringBuilder.append(LOG_SPLIT_CHAR)
                            .append(field);
                }
            }
            stringBuilder.append(LOG_SPLIT_CHAR).append(user.getAppId());


        } else {
            //TODO 记录请求头和请求参数
            if (!(request.getRequestURI().endsWith("/api/file/resume"))) {

                stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                        .append(request.getRequestURI()).append(LOG_SPLIT_CHAR);

                Enumeration enumeration = request.getHeaderNames();
                stringBuilder.append("Header:{");
                while (enumeration.hasMoreElements()) {
                    Object obj = enumeration.nextElement();
                    stringBuilder.append(obj).append(" = ").append(request.getHeader(obj.toString())).append(",");
                }
                stringBuilder.append("}").append(LOG_SPLIT_CHAR);
                stringBuilder.append("Parameter:{");
                for (Map.Entry<String, String[]> sub : ((Map<String, String[]>) request.getParameterMap()).entrySet()) {
                    stringBuilder.append(sub.getKey()).append(" = ").append(Arrays.toString(sub.getValue())).append(",");
                }
                stringBuilder.append("}").append(LOG_SPLIT_CHAR).append(RequestUtils.getAppId(request));
            }

        }
        return stringBuilder.toString();
    }

    /**
     * 记录创建群组的话单
     *
     * @param group
     */
    public void logCreateGroup(Group group , User user ) {
        try {
            MDC.put(ReqAnalyzeInterceptor.PREFIX, ReqAnalyzeInterceptor.getLogPrefix());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("group_new").append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getId())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getAccount())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getCreator())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getRegion())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(null != group.getCreateTime() ? String.valueOf(group.getCreateTime().getTime()) : "")).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getDesc())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getName())).append(LOG_SPLIT_CHAR).append( user.getAppId() ).append( LOG_SPLIT_CHAR );
            LOGGER_ANALYZE_USER.info(stringBuilder.toString());
        } finally {
            MDC.remove(ReqAnalyzeInterceptor.PREFIX);
        }
    }

    /**
     * 记录群组解散
     *
     * @param group
     */
    public void logDelGroup(Group group , User user ) {
        try {
            MDC.put(ReqAnalyzeInterceptor.PREFIX, ReqAnalyzeInterceptor.getLogPrefix());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("group_del").append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getId())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getAccount())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getCreator())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getRegion())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(null != group.getCreateTime() ? String.valueOf(group.getCreateTime().getTime()) : "")).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getDesc())).append(LOG_SPLIT_CHAR).append( user.getAppId() ).append( LOG_SPLIT_CHAR );
            LOGGER_ANALYZE_USER.info(stringBuilder.toString());
        } finally {
            MDC.remove(ReqAnalyzeInterceptor.PREFIX);
        }

    }

    /**
     * 记录删除群组成员
     *
     * @param group
     * @param memberId
     */
    public void logRemoveGroupMember(Group group, String memberId) {
        try {
            MDC.put(ReqAnalyzeInterceptor.PREFIX, ReqAnalyzeInterceptor.getLogPrefix());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("group_remove_member").append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getId())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getAccount())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getCreator())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getRegion())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(null != group.getCreateTime() ? String.valueOf(group.getCreateTime().getTime()) : "")).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getDesc())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(memberId)).append(LOG_SPLIT_CHAR).append( group.getAppID() ).append( LOG_SPLIT_CHAR );
            LOGGER_ANALYZE_USER.info(stringBuilder.toString());
        } finally {
            MDC.remove(ReqAnalyzeInterceptor.PREFIX);
        }
    }

    /**
     * 记录添加成员
     *
     * @param group
     * @param memberId
     */
    public void logAddGroupMember(Group group, String memberId) {
        try {
            MDC.put(ReqAnalyzeInterceptor.PREFIX, ReqAnalyzeInterceptor.getLogPrefix());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("group_add_member").append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getId())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getAccount())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getCreator())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getRegion())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(null != group.getCreateTime() ? String.valueOf(group.getCreateTime().getTime()) : "")).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getDesc())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(memberId)).append(LOG_SPLIT_CHAR).append( group.getAppID() ).append( LOG_SPLIT_CHAR );
            LOGGER_ANALYZE_USER.info(stringBuilder.toString());
        } finally {
            MDC.remove(ReqAnalyzeInterceptor.PREFIX);
        }
    }


    /**
     * 记录添加群组成员失败
     *
     * @param group
     * @param memberId
     */
    public void logFailsToAddGroupMemberForOverlimit(Group group, String memberId, String scene) {
        try {
            MDC.put(ReqAnalyzeInterceptor.PREFIX, ReqAnalyzeInterceptor.getLogPrefix());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                    .append("group_fails_add_member").append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getId())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getAccount())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getCreator())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getRegion())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(null != group.getCreateTime() ? String.valueOf(group.getCreateTime().getTime()) : "")).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getDesc())).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(memberId)).append(LOG_SPLIT_CHAR)
                    .append(delKeywords(group.getName())).append(LOG_SPLIT_CHAR)
                    .append("over_limit").append(LOG_SPLIT_CHAR)
                    .append(delKeywords(scene));
            LOGGER_ANALYZE_USER.info(stringBuilder.toString());
        } finally {
            MDC.remove(ReqAnalyzeInterceptor.PREFIX);
        }
    }


    public String delKeywords(String name) {
        return null != name ? name.replaceAll("\\|", "") : "";
    }
}
