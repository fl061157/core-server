package cn.v5.util;

import cn.v5.code.StatusCode;
import cn.v5.web.controller.ServerException;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by yangwei on 14-10-11.
 */
public class ReflectUtil {
    public static <T> void setFieldValue(T target, String fName, Class fType, Object fValue) {
        if (target == null || StringUtils.isEmpty(fName) ||
                (fValue != null && !fType.isAssignableFrom(fValue.getClass()))) {
            return;
        }
        Method[] allMethods = target.getClass().getDeclaredMethods();
        for (Method m : allMethods) {
            String mName = m.getName();
            if (!mName.toLowerCase().contains(fName)) {
                continue;
            }
            Type[] pType = m.getParameterTypes();
            if (pType.length != 1 || pType[0].getClass().isAssignableFrom(fType)) {
                continue;
            }
            m.setAccessible(true);
            try {
                m.invoke(target, fValue);
                return;
            } catch (IllegalAccessException e) {
                throw new ServerException(StatusCode.INNER_ERROR, e.getMessage());
            } catch (InvocationTargetException e) {
                throw new ServerException(StatusCode.INNER_ERROR, e.getMessage());
            }
        }
        throw new ServerException(StatusCode.INNER_ERROR, "set field " + fName + " not found setting methods");
    }
}
