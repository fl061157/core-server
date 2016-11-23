package cn.v5.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sunhao on 14-8-12.
 */
public class MobileUtilsTest {
    private MobileUtils mobileUtils = new MobileUtils();

    @Test
    public void testRemoveLeadingCharacter() {
        assertEquals("", mobileUtils.removeLeadingCharacter("+000000000000"));
        assertEquals("1", mobileUtils.removeLeadingCharacter("+0000001"));
        assertEquals("152", mobileUtils.removeLeadingCharacter("0000152"));
        assertEquals("77745654", mobileUtils.removeLeadingCharacter("+77745654"));
        assertEquals("77745654", mobileUtils.removeLeadingCharacter("77745654"));
    }

    @Test
    public void testExtractNumbersFromMobile() {
        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("1234567890"));
        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+1234567890"));
//        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+0001234567890"));
//        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+123456+++789@#$%^&*0"));
//        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+00012345 6789 0"));
//        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+1234 567 89 0    !@#$%^&*("));
        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+12345 6789 0"));
        assertEquals("1234567890", mobileUtils.extractNumbersFromMobile("+12345    6789 0"));
    }
    @Test
    public void testSaltMobile(){
        String salt = "d399640d0e4d47cfb9ffd5793d6ab0c1";
        mobileUtils.setSalt(salt);
        String mobile = "15901640069";
        System.out.println(mobileUtils.saltHash(mobile));

        mobile = "18626469444";
        System.out.println(mobileUtils.saltHash(mobile));
    }
}
