package cn.v5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by piguangtao on 15/9/28.
 */
public class TestFormat {

    @org.junit.Test
    public void test964Format() {
        Pattern pattern = Pattern.compile("^7[0-9]{9}");

        //7开头 10位数字
        String phone = "7830072933";

        Matcher matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        //7开头 9位数字
        phone = "783007293";

        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

        //7开头 11位数字
        phone = "78300729311";

        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

    }

    @org.junit.Test
    public void test20Format() {
        Pattern pattern = Pattern.compile("^1[0,1,2]\\d{8}");
        String phone = "1200082527";

        Matcher matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "1000082527";

        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "1100082527";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "120008252";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

        phone = "12000825271";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

        phone = "2100082527";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

    }

    @org.junit.Test
    public void test95Format() {
        Pattern pattern = Pattern.compile("^9\\d{7,10}");
        String phone = "9979993743";

        Matcher matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "99799937433";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "99799937";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "997999371";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "197999371";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());
    }


    @org.junit.Test
    public void test212Format() {
        Pattern pattern = Pattern.compile("^6\\d{8}");
        String phone = "699997732";

        Matcher matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "799997732";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

        phone = "6999977321";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());

        phone = "6999977";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());


    }

    @org.junit.Test
    public void test213Format() {
        Pattern pattern = Pattern.compile("^[5,6,7,8,9]\\d{8,9}");
        String phone = "699923278";

        Matcher matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "6999232781";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "5999232781";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "599923278";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "799923278";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "7999232781";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "899923278";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "8999232781";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "999923278";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());


        phone = "9999232781";
        matcher = pattern.matcher(phone);
        assertTrue(matcher.matches());

        phone = "1999232781";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());


        phone = "9999232781111";
        matcher = pattern.matcher(phone);
        assertFalse(matcher.matches());


    }


}
