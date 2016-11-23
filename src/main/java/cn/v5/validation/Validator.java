package cn.v5.validation;

import net.sf.oval.configuration.Configurer;
import net.sf.oval.guard.Guard;
import net.sf.oval.internal.ClassChecks;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 参数校验
 */
public class Validator extends Guard {
    public Validator() {
        super();
    }

    public Validator(Collection<Configurer> configurers) {
        super(configurers);
    }

    public Validator(Configurer... configurers) {
        super(configurers);
    }

    public boolean needValidate(Method method, Class<?> targetClass) {
        ClassChecks cc = getClassChecks(targetClass);
        return cc.checksForMethodParameters.containsKey(method) ||
                cc.checksForMethodReturnValues.containsKey(method) ||
                cc.checksForMethodsPreExecution.containsKey(method) ||
                cc.checksForMethodsPostExcecution.containsKey(method);
    }
}
