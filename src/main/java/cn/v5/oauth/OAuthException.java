package cn.v5.oauth;

/**
 * Created by fangliang on 16/5/9.
 */
public class OAuthException extends Exception {


    public static final String OAUTH_ERROR_MESSAGE_CONNECT = "Oauth connect error !";

    public static final String OAUTH_ERROR_MESSAGE_TOKEN_RESPONSE_EMPTY = "Oauth token response body empty !";

    public static final String OAUTH_ERROR_MESSAGE_TOKEN_FORMAT = "Oauth token response format error !";

    public static final String OAUTH_ERROR_MESSAGE_TOKEN_OPENID = "Oauth token response openid error !";

    public static final String OAUTH_ERROR_MESSAGE_REQUEST = "Oauth request connect error !";

    public static final String OAUTH_ERROR_MESSAGE_REQUEST_RESPONSE = "Oauth request response error !";

    private int code;

    public OAuthException(int code, String message) {
        super(message);
        this.code = code;
    }


    public OAuthException(int code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }


    public OAuthException(int code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
