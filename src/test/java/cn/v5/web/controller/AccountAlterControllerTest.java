package cn.v5.web.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccountAlterControllerTest {

    AccountAlterController controller = new AccountAlterController();

    @Test
    public void testGetDefaultNickName() throws Exception {
        String countryCode = "0086";

        assertEquals("@无名氏_1", controller.getDefaultNickName(countryCode));

        countryCode = "0001";
        assertEquals("@Anonymous_2", controller.getDefaultNickName(countryCode));

    }

    @Test
    public void testNeedToChange() {
        String nickName = "U_AMdYDv";
        assertTrue(controller.needToChange(nickName));

        nickName = "USA Test 20";
        assertFalse(controller.needToChange(nickName));


    }
}