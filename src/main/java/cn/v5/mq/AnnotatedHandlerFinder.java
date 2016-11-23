package cn.v5.mq;


import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangwei on 15-4-9.
 */
public class AnnotatedHandlerFinder {
    public Multimap<String, EventMessageHandler> findAllHandlers(Object listener) {
        Multimap<String, EventMessageHandler> methodsInListener = HashMultimap.create();
        Class<?> clazz = listener.getClass();
        for (Method method : getAnnotatedMethods(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> eventClass = parameterTypes[0];
            String type = method.getAnnotation(MqSubscribe.class).type();
            methodsInListener.put(type, makeHandler(listener, method, eventClass));
        }
        return methodsInListener;
    }

    private ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
        Set<? extends Class<?>> supers = TypeToken.of(clazz).getTypes().rawTypes();
        Map<MethodIdentifier, Method> identifiers = Maps.newHashMap();
        for (Class<?> superClazz : supers) {
            for (Method superClazzMethod : superClazz.getMethods()) {
                if (superClazzMethod.isAnnotationPresent(MqSubscribe.class)) {
                    Class<?>[] parameterTypes = superClazzMethod.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        throw new IllegalArgumentException("Method " + superClazzMethod + " requires " + parameterTypes.length +
                            " arguments. Event message handler must require only one argument");
                    }
                    MethodIdentifier ident = new MethodIdentifier(superClazzMethod);
                    if (!identifiers.containsKey(ident)) {
                        identifiers.put(ident, superClazzMethod);
                    }
                }
            }
        }
        return ImmutableList.copyOf(identifiers.values());
    }

    private EventMessageHandler makeHandler(Object listener, Method method, Class<?> eventClass) {
        return new EventMessageHandler(eventClass, method, listener);
    }

    private static final class MethodIdentifier {
        private final String name;
        private final List<Class<?>> parameterTypes;

        private MethodIdentifier(Method method) {
            this.name = method.getName();
            this.parameterTypes = Arrays.asList(method.getParameterTypes());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, parameterTypes);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MethodIdentifier) {
                MethodIdentifier ident = (MethodIdentifier)obj;
                return name.equals(ident.name) && parameterTypes.equals(((MethodIdentifier) obj).parameterTypes);
            }
            return false;
        }
    }
}
