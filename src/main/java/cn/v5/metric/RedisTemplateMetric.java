package cn.v5.metric;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by piguangtao on 15/1/22.
 */
public class RedisTemplateMetric<K, V> extends RedisTemplate<K, V> {

    private MetricRegistry metricRegistry;

    private Timer reqTimer;

    private Meter errorMeter;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (null != metricRegistry) {
            reqTimer = metricRegistry.timer("redis-req-timer");
            errorMeter = metricRegistry.meter("redis-error-meter");
        }
    }

    @Override
    public <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline) {
        Timer.Context context = before();
        try {
            return super.execute(action, exposeConnection, pipeline);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public List<Object> executePipelined(final SessionCallback<?> session, final RedisSerializer<?> resultSerializer){
        Timer.Context context = before();
        try {
            return super.executePipelined(session, resultSerializer);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public List<Object> executePipelined(final RedisCallback<?> action, final RedisSerializer<?> resultSerializer){
        Timer.Context context = before();
        try {
            return super.executePipelined(action, resultSerializer);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public void delete(K key){
        Timer.Context context = before();
        try {
            super.delete(key);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public void delete(Collection<K> keys){
        Timer.Context context = before();
        try {
            super.delete(keys);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public Boolean hasKey(K key){
        Timer.Context context = before();
        try {
            return super.hasKey(key);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public Boolean expire(K key, final long timeout, final TimeUnit unit){
        Timer.Context context = before();
        try {
            return super.expire(key, timeout, unit);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public Boolean expireAt(K key, final Date date){
        Timer.Context context = before();
        try {
            return super.expireAt(key, date);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    @Override
    public void restore(K key, final byte[] value, long timeToLive, TimeUnit unit){
        Timer.Context context = before();
        try {
            super.restore(key, value,timeToLive,unit);
        } catch (Throwable e) {
            handleError(e);
            throw e;
        } finally {
            after(context);
        }
    }

    private Timer.Context before(){
        Timer.Context context = null;
        if (null != reqTimer) {
            context = reqTimer.time();
        }
        return context;
    }

    private void after(Timer.Context context){
        if (null != context) {
            context.close();
        }
    }

    private void handleError(Throwable e){
        if (null != errorMeter) {
            errorMeter.mark();
        }

    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
}
