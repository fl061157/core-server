package cn.v5.service;

import org.junit.Assert;
import org.junit.Test;


public class UserServiceTest2 {

    UserService userService = new UserService();

    @Test
    public void testGenAccount() throws Exception {
        System.out.println(userService.genAccount(null, ""));

        long basicValue = 1;
        String countryCode = "0086";
        Assert.assertEquals("8600001", userService.genAccount(basicValue, countryCode));

        countryCode = "+86";
        Assert.assertEquals("8600001", userService.genAccount(basicValue, countryCode));

        countryCode = "+0086";
        Assert.assertEquals("8600001", userService.genAccount(basicValue, countryCode));


        countryCode = "0001";
        Assert.assertEquals("100001", userService.genAccount(basicValue, countryCode));

        countryCode = "0002";
        Assert.assertEquals("200001", userService.genAccount(basicValue, countryCode));


        countryCode = "0954";
        Assert.assertEquals("95400001", userService.genAccount(basicValue, countryCode));

        countryCode = "+954";
        Assert.assertEquals("95400001", userService.genAccount(basicValue, countryCode));

        basicValue = 100;
        Assert.assertEquals("95400100", userService.genAccount(basicValue, countryCode));

        basicValue = 1000;
        Assert.assertEquals("95401000", userService.genAccount(basicValue, countryCode));

        basicValue = 10000;
        Assert.assertEquals("95410000", userService.genAccount(basicValue, countryCode));

        basicValue = 12345;
        Assert.assertEquals("95412345", userService.genAccount(basicValue, countryCode));

        basicValue = 123456;
        Assert.assertEquals("954123456", userService.genAccount(basicValue, countryCode));

    }

    @Test
    public void testGetDefaultNickName() {
        String countryCode = "0086";
        Assert.assertEquals("@无名氏", userService.getDefaultNickName(countryCode));

        countryCode = "0001";
        Assert.assertEquals("@Anonymous", userService.getDefaultNickName(countryCode));
    }
}