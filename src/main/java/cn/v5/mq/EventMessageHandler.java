package cn.v5.mq;

import com.google.common.base.Objects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by yangwei on 15-4-9.
 */
public class EventMessageHandler {
    private final Object target;
    private final Method method;
    private final Class<?> eventClass;

    public EventMessageHandler(Class<?> eventClass, Method method, Object target) {
        this.eventClass = eventClass;
        this.method = method;
        this.target = target;
        method.setAccessible(true);
    }

    public Class<?> getEventClass() {
        return eventClass;
    }

    public void handleEvent(Object event) throws InvocationTargetException {
        checkNotNull(event);
        try {
            method.invoke(target, new Object[] {event});
        } catch(IllegalArgumentException e) {
            throw new Error("Method rejected target/argument: " + event, e);
        } catch (IllegalAccessException e) {
            throw new Error("Method became inaccessible: " + event, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error)e.getCause();
            }
            throw e;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventClass, method, target);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventMessageHandler) {
            EventMessageHandler that = (EventMessageHandler)obj;
            return target.equals(that.target) && method.equals(that.method) && eventClass.equals(that.eventClass);
        }
        return false;
    }

    @Override
    public String toString() {
        return "EventMessageHandler{" +
                "target=" + target.getClass().getCanonicalName() +
                ", method=" + method.getName() +
                ", eventClass=" + eventClass.getCanonicalName() +
                '}';
    }
}
