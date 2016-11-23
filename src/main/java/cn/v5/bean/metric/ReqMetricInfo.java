package cn.v5.bean.metric;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piguangtao on 15/11/26.
 * 度量一次http请求的各个里程碑事件
 */
public class ReqMetricInfo {
    private Long startTime;
    private Long endTime;
    private Integer statusCode;
    private Long spentTime;
    private ReqInfo reqInfo;
    private List<ReqStepInfo> stepInfoList = new ArrayList<>();

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getSpentTime() {
        return spentTime;
    }

    public void setSpentTime(Long spentTime) {
        this.spentTime = spentTime;
    }

    public ReqInfo getReqInfo() {
        return reqInfo;
    }

    public void setReqInfo(ReqInfo reqInfo) {
        this.reqInfo = reqInfo;
    }

    public List<ReqStepInfo> getStepInfoList() {
        return stepInfoList;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public void setStepInfoList(List<ReqStepInfo> stepInfoList) {
        this.stepInfoList = stepInfoList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReqMetricInfo{");
        sb.append("startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", statusCode='").append(statusCode).append('\'');
        sb.append(", spentTime=").append(spentTime);
        sb.append(", reqInfo=").append(reqInfo);
        sb.append(", stepInfoList=").append(stepInfoList);
        sb.append('}');
        return sb.toString();
    }

    public static class ReqInfo {
        private String traceId;
        private String uri;
        private String ip;
        private String ua;

        public ReqInfo withTraceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public ReqInfo withUri(String uri) {
            this.uri = uri;
            return this;
        }

        public ReqInfo withIp(String ip) {
            this.ip = ip;
            return this;
        }

        public ReqInfo withUa(String ua) {
            this.ua = ua;
            return this;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getUri() {
            return uri;
        }

        public String getIp() {
            return ip;
        }

        public String getUa() {
            return ua;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ReqInfo{");
            sb.append("traceId='").append(traceId).append('\'');
            sb.append(", uri='").append(uri).append('\'');
            sb.append(", ip='").append(ip).append('\'');
            sb.append(", ua='").append(ua).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public static class ReqStepInfo {
        private String title;
        private String desc;
        private Long startTime;
        private Long endTime;

        public ReqStepInfo() {
        }

        public ReqStepInfo withTitle(String title) {
            this.title = title;
            return this;
        }

        public ReqStepInfo withDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public ReqStepInfo withStartTime(Long startTime) {
            this.startTime = startTime;
            return this;
        }

        public ReqStepInfo withEndTime(Long endTime) {
            this.endTime = endTime;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public String getDesc() {
            return desc;
        }

        public Long getStartTime() {
            return startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ReqStepInfo{");
            sb.append("title='").append(title).append('\'');
            sb.append(", desc='").append(desc).append('\'');
            sb.append(", startTime=").append(startTime);
            sb.append(", endTime=").append(endTime);
            sb.append('}');
            return sb.toString();
        }
    }

}
