package cn.v5.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by piguangtao on 15/9/28.
 */
public class PhoneFormatUtilTest {

    @Test
    public void testInitCountryPhoneFormat() throws Exception {

        PhoneFormatUtil util = new PhoneFormatUtil();
        util.initCountryPhoneFormat();
        System.out.println(util.printCountryPhoneFormat());

        String mobile = "612345678";
        String country = "0066";
        assertTrue(util.validatePhone(country, mobile));

        mobile = "812345678";
        assertTrue(util.validatePhone(country, mobile));

        mobile = "912345678";
        assertTrue(util.validatePhone(country, mobile));


        mobile = "112345678";
        assertFalse(util.validatePhone(country, mobile));

        mobile = "212345678";
        assertFalse(util.validatePhone(country, mobile));

        mobile = "6123456781";
        assertFalse(util.validatePhone(country, mobile));

        mobile = "81234567811";
        assertFalse(util.validatePhone(country, mobile));

        mobile = "91234567";
        assertFalse(util.validatePhone(country, mobile));
    }
}