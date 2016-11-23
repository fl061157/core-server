package cn.v5.web.controller;

/**
 * Created with IntelliJ IDEA.
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-11
 * Time: 下午7:13
 * To change this template use File | Settings | File Templates.
 */
public class ServerException extends RuntimeException {
    private int errorCode;
    private String error;

    public ServerException(int errorCode, String error) {
        this.error = error;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
