package cn.v5.service;

import cn.v5.test.TestTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SmsServiceTest extends TestTemplate {

    @Autowired
    private SmsService smsService;

    @Test
    public void testGetRandomCode() throws Exception {
        String mobile = "12311111111";
        System.out.println(smsService.getRandomCode(mobile));
    }

}