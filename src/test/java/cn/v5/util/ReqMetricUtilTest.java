package cn.v5.util;

import org.junit.Test;

/**
 * Created by piguangtao on 15/11/26.
 */
public class ReqMetricUtilTest {

    @Test
    public void testDelKeywords() throws Exception {
        String test = "aaafd[fdada|dafdsa]dfafa{fasa}";
        ReqMetricUtil reqMetricUtil = new ReqMetricUtil();
        System.out.println(reqMetricUtil.delKeywords(test));
    }
}