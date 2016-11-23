package cn.v5.trade.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fangliang on 16/9/5.
 */
public class CollectionUtil {


    final static String EMPTY_STR = "";

    public static Map<String, Object> copy(Map<String, String> map) {
        Map<String, Object> result = new HashedMap();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    public static String printMap(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            return EMPTY_STR;
        }
        StringBuilder builder = new StringBuilder();

        Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();

        Map.Entry<String, String> item = iter.next();

        builder.append(item.getKey() + " : " + item.getValue());

        while (iter.hasNext()) {
            item = iter.next();
            builder.append(" , ");
            builder.append(item.getKey() + ":" + item.getValue());
        }

        return builder.toString();
    }


    public static <T extends Object> String printList(List<T> objectList) {
        if (CollectionUtils.isEmpty(objectList)) return "[]";
        StringBuilder builder = new StringBuilder();
        Iterator<T> iterator = objectList.iterator();
        T o = iterator.next();
        builder.append("[ " + ReflectionToStringBuilder.toString(o));
        if (iterator.hasNext()) {
            o = iterator.next();
            builder.append(" , ");
            builder.append(ReflectionToStringBuilder.toString(o));
        }
        builder.append(" ]");
        return builder.toString();
    }


}
