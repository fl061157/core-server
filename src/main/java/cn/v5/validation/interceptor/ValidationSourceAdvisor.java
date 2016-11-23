package cn.v5.validation.interceptor;

import cn.v5.validation.Validate;
import cn.v5.validation.Validator;
import net.sf.oval.configuration.Configurer;
import net.sf.oval.configuration.annotation.AnnotationsConfigurer;
import net.sf.oval.configuration.annotation.BeanValidationAnnotationsConfigurer;
import net.sf.oval.configuration.annotation.JPAAnnotationsConfigurer;
import net.sf.oval.exception.ExceptionTranslator;
import net.sf.oval.guard.GuardInterceptor;
import net.sf.oval.guard.Guarded;
import net.sf.oval.internal.Log;
import net.sf.oval.logging.LoggerFactorySLF4JImpl;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * 校验拦截器
 */
public class ValidationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor implements InitializingBean {
    private Validator validator;
    private ExceptionTranslator exceptionTranslator;
    private LinkedList<Configurer> configurers = new LinkedList<Configurer>();
    private boolean enableAll;


    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setExceptionTranslator(ExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = exceptionTranslator;
    }

    public void setConfigurers(List<Configurer> configurers) {
        this.configurers.addAll(configurers);
    }

    public void setEnableAll(boolean enableAll) {
        this.enableAll = enableAll;
    }

    private final Pointcut pointcut = new StaticMethodMatcherPointcut() {

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (enableAll || targetClass.isAnnotationPresent(Guarded.class) || targetClass.isAnnotationPresent(Validate.class)) && validator.needValidate(method, targetClass);
        }
    };

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Log.setLoggerFactory(new LoggerFactorySLF4JImpl());
        if (validator == null) {
            configurers.addFirst(new BeanValidationAnnotationsConfigurer());

            configurers.addFirst(new AnnotationsConfigurer());
            validator = new Validator(configurers);
            if (exceptionTranslator == null) {
                exceptionTranslator = new ServerExceptionTranslator();
            }
            validator.setExceptionTranslator(exceptionTranslator);
        }
        if (getAdvice() == null && getAdviceBeanName() == null) {
            setAdvice(new GuardInterceptor(validator));
        }
    }
}
