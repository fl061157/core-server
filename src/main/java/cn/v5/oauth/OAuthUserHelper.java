package cn.v5.oauth;

/**
 * Created by fangliang on 16/6/7.
 */
public class OAuthUserHelper {


    public static void wrapperOAuthUser(OAuthUser authUser, Integer appID) {

        if (appID != null && !appID.equals(1)) {
            String uid = String.format("APPID_%d_%s", appID, authUser.getUnionID());
            authUser.setUnionID(uid);
        }

    }

}
