package cn.v5.util;

import org.slf4j.Logger;
/**
 * Created by piguangtao on 15/2/4.
 */
public class LoggerFactory {
    /**
     * Return a logger named corresponding to the class passed as parameter, using
     * the statically bound {@link org.slf4j.ILoggerFactory} instance.
     *
     * @param clazz the returned logger will be named after clazz
     * @return logger
     */
    public static Logger getLogger(Class clazz) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(clazz.getName());
        return new LoggerWrapper(logger);
    }
}
