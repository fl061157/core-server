package cn.v5.metric;

import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by piguangtao on 15/1/22.
 */
public class ReqAnalyzeInterceptor extends HandlerInterceptorAdapter implements InitializingBean {
    private static Logger LOGGER = LoggerFactory.getLogger(ReqAnalyzeInterceptor.class);


    private MetricRegistry metricRegistry;

    private static final String METRIC_KEY = "metric-context";

    private ConcurrentHashMap<String, Timer> reqTimers = new ConcurrentHashMap<>();

    @Inject
    @Qualifier("reqMetricMap")
    protected Properties reqMetricMap;

    @Autowired
    private LogUtil logUtil;

    @Value("${behaviour.interceptor.excludes}")
    private String excludesConfig;

    private List<String> excludes;


    final static String nodeId = System.getProperty("node.id");
    final static String nodeIp = System.getProperty("node.ip");
    final static String Module = "CoreServer";

    public final static String PREFIX = "PREFIX";

    private static String logPrefix = "";

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isNotBlank(excludesConfig)) {
            excludes = Arrays.asList(excludesConfig.split(","));
        }

        logPrefix = String.format("%s|%s|%s", nodeId, nodeIp, Module);

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String loggerHead = String.format("%s|%s|%s", nodeId, nodeIp, Module);
        if (request != null) {
            request.setAttribute(PREFIX, loggerHead);
        }

        try {
            //controller 的异步执行会调用两次preHandle,忽略第二次的进入
            if (null == request.getAttribute(METRIC_KEY)) {
                String requestURI = request.getRequestURI();
                Timer reqTimer = getReqServletTimer(requestURI);
                if (null != reqTimer) {
                    Timer.Context context = reqTimer.time();
                    request.setAttribute(METRIC_KEY, context);
                }
                logReq(request);
            }
        } catch (Throwable e) {
            LOGGER.error("fails to handle metirc.", e);
        }
        return true;
    }


    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        try {
            Object context = request.getAttribute(METRIC_KEY);
            if (null != context) {
                Timer.Context timerContext = (Timer.Context) context;
                timerContext.close();
            }
        } catch (Throwable e) {
            LOGGER.error("fails to handle metric", e);
        } finally {
        }
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Timer getReqServletTimer(final String requestURIPar) {
        String requestURI = requestURIPar;
        Timer result;
        if (null == requestURI || "".equals(requestURI) || null == reqMetricMap) return null;
        String metricCondition = reqMetricMap.getProperty(requestURI);

        //首次匹配，不需要判断 匹配条件
        String metricName = getMetricName(metricCondition, true);

        while (null == metricName) {
            int lastIndex = requestURI.lastIndexOf("/");
            if (lastIndex > 0) {
                requestURI = requestURI.substring(0, lastIndex);
                metricCondition = reqMetricMap.getProperty(requestURI);
                metricName = getMetricName(metricCondition, false);
            } else {
                break;
            }
        }

        if (null == metricName) {
            metricName = reqMetricMap.getProperty("default");
        }
        reqTimers.putIfAbsent(metricName, metricRegistry.timer(metricName));
        result = reqTimers.get(metricName);

        return result;
    }

    private String getMetricName(String metricCondition, boolean isFirstMatch) {
        String metricName;
        if (null == metricCondition || "".equals(metricCondition)) {
            return null;
        }

        String[] matchCondition = metricCondition.split(",");
        boolean isFullMatch = true;
        if (matchCondition.length > 1) {
            isFullMatch = "no".equalsIgnoreCase(matchCondition[0]) ? false : true;
            metricName = matchCondition[1];
        } else {
            metricName = matchCondition[0];
        }

        //不是初次匹配，并且meticname要求全匹配是，表示没有找到配置的度量
        if (!isFirstMatch && isFullMatch) {
            metricName = null;
        }
        return metricName;
    }


    private void logReq(HttpServletRequest request) {
        User user = CurrentUser.user();
        if (needToLogBehaviour(request.getRequestURI())) {
            logUtil.logReq(user, request);
        }
    }


    private boolean needToLogBehaviour(String uri) {
        boolean result = true;
        if (null != excludes && excludes.size() > 0) {
            result = !excludes.contains(uri);
        }
        return result;
    }

    public static String getLogPrefix() {
        return logPrefix;
    }
}
