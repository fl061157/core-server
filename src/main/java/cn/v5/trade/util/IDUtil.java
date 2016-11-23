package cn.v5.trade.util;

import info.archinnov.achilles.internal.utils.UUIDGen;

/**
 * Created by fangliang on 16/8/31.
 */
public class IDUtil {

    public static String create32ID() {
        return UUIDGen.getTimeUUID().toString().replaceAll("\\-", "");
    }

}
