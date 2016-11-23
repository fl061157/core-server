package cn.v5.web.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-11
 * Time: 下午7:13
 * To change this template use File | Settings | File Templates.
 */
public class ExtraInfoServerException extends ServerException {

    private Map<String, Object> extraInfo = new HashMap<>();

    public ExtraInfoServerException(int errorCode, String error) {
        super(errorCode, error);
    }


    public ExtraInfoServerException withInfo(String key, Object value) {
        extraInfo.put(key, value);
        return this;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }
}
