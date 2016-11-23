package cn.v5.util;

/**
 * Created by yangwei on 14-9-19.
 */
public class IntegerUtils {
    public static Integer sum(Integer... ins) {
        int sum = 0;
        for (Integer i : ins) {
            if (i == null) {
                continue;
            }
            sum += i;
        }
        return sum;
    }
}
