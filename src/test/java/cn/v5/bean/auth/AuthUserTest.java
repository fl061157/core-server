package cn.v5.bean.auth;

import cn.v5.entity.User;
import cn.v5.json.ObjectMapperFactoryBean;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class AuthUserTest {

    @Test
    public void testFormAuthUser() throws Exception {
        User user = new User();
        user.setId("aaaaaa");
//        user.setAvatar_url("http://127.0.0.1/api/avartar/cccccc");
        user.setCreateTime(new Date());
        AuthUser authUser = AuthUser.formAuthUser(user);
        assertNotNull(authUser);
        assertEquals(user.getId(),authUser.getId());
        assertEquals(user.getAvatar_url(),authUser.getAvatar_url());

        ObjectMapperFactoryBean bean = new ObjectMapperFactoryBean();
        String authUserStr = bean.getObject().writeValueAsString(authUser);
        System.out.println(authUserStr);


    }
}