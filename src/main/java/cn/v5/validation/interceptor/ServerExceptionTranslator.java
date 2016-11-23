package cn.v5.validation.interceptor;

import cn.v5.code.StatusCode;
import cn.v5.web.controller.ServerException;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.context.*;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.exception.ExceptionTranslator;
import net.sf.oval.exception.OValException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;


public class ServerExceptionTranslator implements ExceptionTranslator {
    @Override
    public RuntimeException translateException(OValException ex) {
        throw new ServerException(StatusCode.PARAMETER_ERROR, "非法参数");
    }
}
