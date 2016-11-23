package cn.v5.service;

import cn.v5.entity.thirdapp.ThirdAppCgUser;
import cn.v5.entity.thirdapp.ThirdAppCgUserKey;
import cn.v5.entity.vo.ThirdAppRecommendUser;
import cn.v5.test.TestTemplate;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class FriendRecommendServiceTest2 extends TestTemplate {
    @Autowired
    private FriendRecommendService friendRecommendService;

    private String accessToken;

    private String suid;

    @Before
    public void before() {
//        accessToken = "2.00LNXC1BWptC5E8fbe510a4d0SjcZj";
//        suid = "1212263877";
        accessToken = "CAAWlk1X3qFIBAIJgZCvMMZArwC5qfivXi3IPJyvwdPtiWeLViq3xNOOZAAv37OGZBhCQuCO0fufqILyZCA5qUDOow28UUOyrQ4ifrZBbOi23hZBFev0ANblVA5VaRAHvqYaMaFItPPfynb6IdVU6y4jmkZBV56vU8WdNuwr78jb19XvrMWmz5Yi06fxeQkmO1uKvDzVXKWEbA9Kyb6yhoTKZA6z6zPZBkygWgZD";
        suid = "598569113618678";
    }

    @Test
    public void testGetRecommendFromWeibo() throws Exception {

        ThirdAppCgUser thirdAppCgUser = new ThirdAppCgUser();
        thirdAppCgUser.setAccessToken(accessToken);

        ThirdAppCgUserKey key = new ThirdAppCgUserKey();
        key.setType("12");
        key.setThirdAppUserId(suid);

        thirdAppCgUser.setKey(key);

        List<ThirdAppRecommendUser> results = friendRecommendService.getRecommendFromWeibo(thirdAppCgUser);

        Assert.assertTrue(null != results);
        System.out.println(Arrays.toString(results.toArray()));

    }

    @Test
    public void testGetRecommendFromFaceBook() throws Exception {

        ThirdAppCgUser thirdAppCgUser = new ThirdAppCgUser();
        thirdAppCgUser.setAccessToken(accessToken);

        ThirdAppCgUserKey key = new ThirdAppCgUserKey();
        key.setType("13");
        key.setThirdAppUserId(suid);

        thirdAppCgUser.setKey(key);

        List<ThirdAppRecommendUser> results = friendRecommendService.getRecommendFromFaceBook(thirdAppCgUser);

        Assert.assertTrue(null != results);
        System.out.println(Arrays.toString(results.toArray()));

    }
}