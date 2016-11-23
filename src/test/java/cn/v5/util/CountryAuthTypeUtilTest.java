package cn.v5.util;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by piguangtao on 15/9/24.
 */
public class CountryAuthTypeUtilTest {

    CountryAuthTypeUtil util = new CountryAuthTypeUtil();

    @Before
    public void before() {
        util.loadCountryAuthConfig();
    }

    @Test
    public void testLoadCountryAuthConfig() throws Exception {
        System.out.println(util.printCountryAuth());
    }

    @Test
    public void testGetAuthType() throws Exception {

    }
}