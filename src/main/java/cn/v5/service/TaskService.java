package cn.v5.service;

import cn.v5.entity.CurrentUser;
import cn.v5.util.LoggerFactory;
import cn.v5.util.RequestUtils;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by sunhao on 15-2-5.
 */
@Service
public class TaskService implements InitializingBean {
    private static Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    @Inject
    private TaskExecutor taskExecutor;

    @Inject
    private TaskScheduler taskScheduler;

    public void execute(Runnable runnable) {
        this.taskExecutor.execute(new InternalRunnable(getTraceId(), runnable));
    }

    public ScheduledFuture schedule(Runnable task, Trigger trigger) {
        return this.taskScheduler.schedule(new InternalRunnable(getTraceId(), task), trigger);
    }

    public ScheduledFuture schedule(Runnable task, Date startTime) {
        return this.taskScheduler.schedule(new InternalRunnable(getTraceId(), task), startTime);
    }

    public ScheduledFuture scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return this.taskScheduler.scheduleAtFixedRate(new InternalRunnable(getTraceId(), task), startTime, period);
    }

    public ScheduledFuture scheduleAtFixedRate(Runnable task, long period) {
        return this.taskScheduler.scheduleAtFixedRate(new InternalRunnable(getTraceId(), task), period);
    }

    public ScheduledFuture scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return this.taskScheduler.scheduleWithFixedDelay(new InternalRunnable(getTraceId(), task), startTime, delay);
    }

    public ScheduledFuture scheduleWithFixedDelay(Runnable task, long delay) {
        return this.taskScheduler.scheduleWithFixedDelay(new InternalRunnable(getTraceId(), task), delay);
    }

    public <V> WebAsyncTask<V> newWebAsyncTask(Callable<V> callable) {
        return new WebAsyncTask<>(new InternalCallback<>(getTraceId(), callable));
    }

    public <V> WebAsyncTask<V> newWebAsyncTask(long timeout, Callable<V> callable) {
        return new WebAsyncTask<>(timeout, new InternalCallback<>(getTraceId(), callable));
    }

    public <V> WebAsyncTask<V> newWebAsyncTask(Long timeout, String executorName, Callable<V> callable) {
        return new WebAsyncTask<>(timeout, executorName, new InternalCallback<>(getTraceId(), callable));
    }

    public <V> WebAsyncTask<V> newWebAsyncTask(Long timeout, AsyncTaskExecutor executor, Callable<V> callable) {
        return new WebAsyncTask<>(timeout, executor, new InternalCallback<>(getTraceId(), callable));
    }

    private String getTraceId() {
        return RequestUtils.traceIdTheadLocal.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.taskExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = ((ThreadPoolTaskExecutor) this.taskExecutor);
            RejectedExecutionHandler handler = executor.getThreadPoolExecutor().getRejectedExecutionHandler();
            if (null != handler) {
                executor.getThreadPoolExecutor().setRejectedExecutionHandler((r, executor1) -> {
                    LOGGER.error("[taskExecutor] begin to reject execution!!!!!!");
                    handler.rejectedExecution(r, executor1);
                });
            } else {
                LOGGER.warn("[taskExecutor]. has not set rejected handler.");
            }
        } else {
            LOGGER.warn("[taskExecutor] instance is no ThreadPoolTaskExecutor. class:{}", this.taskExecutor.getClass());
        }
    }

    private static class InternalRunnable implements Runnable {
        private String traceId;
        private Runnable runnable;
        private PersistenceManager db;
        public InternalRunnable(String traceId, Runnable runnable) {
            this.traceId = traceId;
            this.runnable = Objects.requireNonNull(runnable, "runnable is null");
            this.db = CurrentUser.db();
        }

        @Override
        public void run() {
            if (traceId != null) {
                RequestUtils.traceIdTheadLocal.set(traceId);
            } else {
                RequestUtils.traceIdTheadLocal.remove();
            }
            CurrentUser.setDB(db);
            this.runnable.run();
        }
    }

    private static class InternalCallback<V> implements Callable<V> {
        private String traceId;
        private Callable<V> callback;
        private PersistenceManager db;

        public InternalCallback(String traceId, Callable<V> callback) {
            this.traceId = traceId;
            this.callback = Objects.requireNonNull(callback, "callback is null");
            this.db = CurrentUser.db();
        }

        @Override
        public V call() throws Exception {
            if (traceId != null) {
                RequestUtils.traceIdTheadLocal.set(traceId);
            } else {
                RequestUtils.traceIdTheadLocal.remove();
            }
            CurrentUser.setDB(db);
            return this.callback.call();
        }
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }
}
