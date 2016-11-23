package cn.v5.metric;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by piguangtao on 15/1/29.
 */
public class FileStoreMetric implements InitializingBean {
    private MetricRegistry metricRegistry;

    private Timer reqTimer;

    private Meter errorMeter;

    @Override
    public void afterPropertiesSet() {
        if (null != metricRegistry) {
            reqTimer = metricRegistry.timer("file-store-timer");
            errorMeter = metricRegistry.meter("file-store-error-meter");
        }
    }

    public Timer getReqTimer() {
        return reqTimer;
    }

    public Meter getErrorMeter() {
        return errorMeter;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
}
