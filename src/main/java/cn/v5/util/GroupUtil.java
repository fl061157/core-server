package cn.v5.util;

import cn.v5.code.SystemConstants;
import info.archinnov.achilles.internal.utils.UUIDGen;
import org.apache.commons.lang.StringUtils;

/**
 * Created by fangliang on 16/9/26.
 */
public class GroupUtil {

    public static String genGroupID(Integer appID) {

        String gID = UUIDGen.getTimeUUID().toString().replaceAll("\\-", "");

//        if (appID > SystemConstants.CG_APP_ID_MAX) {
//            return String.format("%s@{%d}", gID, appID);
//        }

        return gID;
    }


    public static String openID(String id, Integer appID) {

        if (StringUtils.isBlank(id) || appID <= SystemConstants.CG_APP_ID_MAX) return id;
        int index = id.lastIndexOf("@{");
        if (index <= 0) return id;

        return id.substring(0, index);
    }


    public static String transOpenID(String id, Integer appID) {

        if (StringUtils.isBlank(id) || appID <= SystemConstants.CG_APP_ID_MAX) return id;
        int index = id.lastIndexOf("@{");
        if (index <= 0) return String.format("%s@{%d}", id, appID);
        return id.substring(0, index);
    }


}
