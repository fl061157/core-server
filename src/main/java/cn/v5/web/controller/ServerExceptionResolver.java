package cn.v5.web.controller;

import cn.v5.metric.LogUtil;
import cn.v5.util.LoggerFactory;
import cn.v5.util.ReqMetricUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-11 下午7:17
 */
public class ServerExceptionResolver extends SimpleMappingExceptionResolver {
    private static final Logger log = LoggerFactory.getLogger(ServerExceptionResolver.class);

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private ReqMetricUtil reqMetricUtil;

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String space = " ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(space);
        stringBuilder.append("[response] error").append(space);
        stringBuilder.append("url:").append(request.getRequestURI()).append(space);

        MappingJackson2JsonView view = new MappingJackson2JsonView();
        ModelAndView modelAndView = new ModelAndView();


        if (ex instanceof ServerException) {
            ServerException e = (ServerException) ex;
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("error_code", e.getErrorCode());
            attributes.put("error", e.getError());

            if (ex instanceof ExtraInfoServerException) {
                ExtraInfoServerException extraInfoServerException = (ExtraInfoServerException) ex;
                if (null != extraInfoServerException.getExtraInfo() && extraInfoServerException.getExtraInfo().size() > 0) {
                    attributes.putAll(extraInfoServerException.getExtraInfo());
                }
            }
            view.setAttributesMap(attributes);
            view.setUpdateContentLength(true);
            modelAndView.setView(view);


            attributes.entrySet().stream().forEach(entry -> stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(space));
//            stringBuilder.append("error_code:").append(e.getErrorCode()).append(space);
//            stringBuilder.append("error:").append(e.getError());
            logFailToRegister(request, e.getErrorCode(), e.getError());
            reqMetricUtil.end(System.currentTimeMillis(), e.getErrorCode());
        } else {
            log.error("", ex);
            response.setStatus(500);
            stringBuilder.append("error_code:").append(500).append(space);
            stringBuilder.append("error:").append("internal error");
            logFailToRegister(request, 500, "Internal error");
            reqMetricUtil.end(System.currentTimeMillis(), 500);
        }

        log.info(stringBuilder.toString());
        return modelAndView;
    }

    private void logFailToRegister(HttpServletRequest request, Integer error, String desc) {
        try {
            String uri = request.getRequestURI();
            if (StringUtils.isNotBlank(uri) && uri.endsWith("/api/user/register")) {
                StringBuilder sb = new StringBuilder();
                String mobile = request.getParameter("mobile");
                String countrycode = request.getParameter("countrycode");
                String authcode = request.getParameter("authcode");
                String device_type = request.getParameter("device_type");
                sb.append(System.currentTimeMillis()).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords("/api/user/fail_register")).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(request.getHeader("user-agent"))).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(countrycode)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(mobile)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(device_type)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(authcode)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(error).append(logUtil.LOG_SPLIT_CHAR)
                        .append(logUtil.delKeywords(desc));
                logUtil.logReq(sb.toString());
            }
        } catch (Exception e) {
            log.error("fails to log register log.", e);
        }
    }
}
