package cn.v5.util;

import cn.v5.bean.metric.ReqMetricInfo;
import cn.v5.metric.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by piguangtao on 15/11/26.
 * 度量每个请求的时间 超过一定的阈值 记录话单
 */
@Service
public class ReqMetricUtil {

    private ThreadLocal<ReqMetricInfo> reqMetricInfoThreadLocal = new InheritableThreadLocal<>();

    @Autowired
    private LogUtil logUtil;

    @Value("${req.metric.threshold.milliseconds}")
    private int reqMetricThresholdMilli;

    public void start(ReqMetricInfo.ReqInfo reqInfo) {
        if (null == reqInfo) return;
        reqMetricInfoThreadLocal.set(new ReqMetricInfo());
        reqMetricInfoThreadLocal.get().setStartTime(System.currentTimeMillis());
        reqMetricInfoThreadLocal.get().setReqInfo(reqInfo);
    }

    public void end(Long endTime, Integer statusCode) {
        if (null == reqMetricInfoThreadLocal.get()) return;
        if (null != endTime) {
            reqMetricInfoThreadLocal.get().setEndTime(endTime);
        } else {
            reqMetricInfoThreadLocal.get().setEndTime(System.currentTimeMillis());
        }
        reqMetricInfoThreadLocal.get().setStatusCode(statusCode);
        log();
        reqMetricInfoThreadLocal.set(null);
    }

    public void addReqStepInfo(String title, String desc, Long startTime, Long endTime) {
        ReqMetricInfo.ReqStepInfo reqStepInfo = new ReqMetricInfo.ReqStepInfo();
        reqStepInfo.withTitle(title).withDesc(desc)
                .withStartTime(startTime)
                .withEndTime(endTime);
        addReqStepInfo(reqStepInfo);
    }

    protected void addReqStepInfo(ReqMetricInfo.ReqStepInfo reqStepInfo) {
        if (null == reqMetricInfoThreadLocal.get()) return;
        reqMetricInfoThreadLocal.get().getStepInfoList().add(reqStepInfo);
    }

    private void log() {
        ReqMetricInfo reqMetricInfo = reqMetricInfoThreadLocal.get();
        if (null != reqMetricInfo) {
            Long startTime = reqMetricInfo.getStartTime();
            Long endTime = reqMetricInfo.getEndTime();
            if (null != startTime && null != endTime && (endTime - startTime > reqMetricThresholdMilli)) {
                String uri = "";
                String ua = "";
                String ip = "";
                String traceId = "";

                ReqMetricInfo.ReqInfo reqInfo = reqMetricInfo.getReqInfo();

                if (null != reqInfo) {
                    uri = reqInfo.getUri();
                    ua = reqInfo.getUa();
                    ip = reqInfo.getIp();
                    traceId = reqInfo.getTraceId();
                }

                StringBuilder sb = new StringBuilder();
                sb.append(System.currentTimeMillis()).append(logUtil.LOG_SPLIT_CHAR)
                        .append("req_time_metric").append(logUtil.LOG_SPLIT_CHAR)
                        .append(delKeywords(uri)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(delKeywords(ua)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(delKeywords(ip)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(delKeywords(traceId)).append(logUtil.LOG_SPLIT_CHAR)
                        .append(reqMetricInfo.getStatusCode()).append(logUtil.LOG_SPLIT_CHAR)
                        .append(startTime).append(logUtil.LOG_SPLIT_CHAR)
                        .append(endTime).append(logUtil.LOG_SPLIT_CHAR)
                        .append(null != startTime && null != endTime ? endTime - startTime : "").append(logUtil.LOG_SPLIT_CHAR);
                List<ReqMetricInfo.ReqStepInfo> reqStepInfos = reqMetricInfo.getStepInfoList();
                if (null != reqMetricInfo) {
                    reqStepInfos.stream().forEach(reqStepInfo -> sb.append("{")
                            .append("[").append(delKeywords(reqStepInfo.getTitle())).append("]")
                            .append("[").append(delKeywords(reqStepInfo.getDesc())).append("]")
                            .append("[").append(reqStepInfo.getStartTime()).append("]")
                            .append("[").append(reqStepInfo.getEndTime()).append("]")
                            .append("[").append(null != reqStepInfo.getStartTime() && null != reqStepInfo.getEndTime() ?
                                    reqStepInfo.getEndTime() - reqStepInfo.getStartTime() : "").append("]")
                            .append("}"));
                }

                logUtil.logReq(sb.toString());
            }
        }
    }

    protected String delKeywords(String info) {
        return StringUtils.isNoneBlank(info) ? info.replaceAll("[\\{\\}\\[\\]\\|]", " ") : " ";
    }
}
