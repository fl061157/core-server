package cn.v5.interceptor;

import cn.v5.bean.metric.ReqMetricInfo;
import cn.v5.code.SystemConstants;
import cn.v5.util.LoggerFactory;
import cn.v5.util.ReqMetricUtil;
import cn.v5.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-10
 * Time: 下午2:34
 * To change this template use File | Settings | File Templates.
 */
public class LogInterceptor extends ConfigurableInterceptor {
    private static Logger log = LoggerFactory.getLogger(LogInterceptor.class);

    @Autowired
    private ReqMetricUtil reqMetricUtil;

    @Autowired
    private RequestUtils requestUtils;

    public void printRequest(HttpServletRequest request, HttpServletResponse response) {

        String traceId = UUID.randomUUID().toString();
        RequestUtils.traceIdTheadLocal.set(traceId);

        MDC.remove("Uid");
        String clientSession = request.getHeader(SystemConstants.CLIENT_SESSION);
        MDC.put("Usession", clientSession);

        MDC.put("TraceId", traceId);
        if (log.isInfoEnabled() && !(request.getRequestURI().endsWith("/api/file/resume"))) {
            String newLine = " ==== ";// System.getProperty("line.separator");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(newLine);

            stringBuilder.append(request.getMethod()).append(request.getRequestURI()).append(newLine);
            stringBuilder.append("thread id : ").append(Thread.currentThread().getName()).append("|").append(request.getMethod()).append("");

            Enumeration enumeration = request.getHeaderNames();
            stringBuilder.append("Header:{");
            while (enumeration.hasMoreElements()) {
                Object obj = enumeration.nextElement();
                stringBuilder.append(obj).append(" = ").append(request.getHeader(obj.toString())).append(",");
            }
            stringBuilder.append("}").append(newLine);
            stringBuilder.append("Parameter:{");
            for (Map.Entry<String, String[]> sub : ((Map<String, String[]>) request.getParameterMap()).entrySet()) {
                stringBuilder.append(sub.getKey()).append(" = ").append(Arrays.toString(sub.getValue())).append(",");
            }
            stringBuilder.append("}").append(newLine);

            log.info(stringBuilder.toString());

            ReqMetricInfo.ReqInfo reqInfo = new ReqMetricInfo.ReqInfo();

            //度量请求开始
            reqInfo.withIp(requestUtils.getClientIP(request))
                    .withTraceId(RequestUtils.traceIdTheadLocal.get())
                    .withUa(requestUtils.getUA(request))
                    .withUri(request.getRequestURI());
            reqMetricUtil.start(reqInfo);
        }
    }


    @Override
    public boolean internalPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        printRequest(request, response);
        return true;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return super.preHandle(request, response, handler);
    }

    @Override
    public void internalAfterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.internalAfterCompletion(request, response, handler, ex);
    }
}
