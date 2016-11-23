package cn.v5.web;


import cn.v5.Fixtures;
import cn.v5.cache.CacheService;
import cn.v5.entity.*;
import cn.v5.service.MessageService;
import cn.v5.service.UserService;
import cn.v5.test.TestTemplate;
import cn.v5.web.controller.UserController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by lb on 14/10/31.
 */
public class UserControllerTestCase extends TestTemplate {
    /**
     * 采用mock替换某个类属性
     */
    @InjectMocks
    @Autowired
    private UserController userController;

    @Value("${salt}")
    private String salt;

    @Spy
    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Autowired
    private WebApplicationContext ctx;

    @Inject
    private UserService userService;//用于制造测试数据

    @Inject
    private MessageService messageService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        //reload data file to remove dirty data.
        Fixtures.loadCql(manager, "data.cql");
    }

    @Test()
    public void testAccount() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());


        mockMvc.perform(get("/api/user/account_exist?account={account}", "aaa" + userLogin.getAccount())
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_exist", is(0))); //

        mockMvc.perform(get("/api/user/account_exist?account={account}", userLogin.getAccount())
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_exist", is(1)));
        mockMvc.perform(post("/api/user/update?account={account}&nickName={nickName}", "3wfqe", "dfsafasdf")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        mockMvc.perform(post("/api/user/update?account={account}&nickName={nickName}", "23DSFOI", "dfsafasdf")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4034))); //send the verify code.
        mockMvc.perform(post("/api/user/update?account={account}&nickName={nickName}", "%&*-", "dfsafasdf")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4034))); //send the verify code.
        mockMvc.perform(post("/api/user/update?account={account}&nickName={nickName}", "3wfqe", "dfsafasdf")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4033))); //send the verify code.
        mockMvc.perform(get("/api/user/account_exist?account={account}", userLogin.getAccount())
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_exist", is(0)));
        mockMvc.perform(get("/api/user/account_exist?account={account}", "3wfqe")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_exist", is(1)));
        Assert.assertFalse(userService.canBeModified(userLogin.getId()));
        Assert.assertTrue(userService.canBeModified("ppoo"));
        Assert.assertTrue(userService.canBeModified("0cb6d443a4f811e3ac7abfd52f77bd18"));
    }

    @Test
    public void testDigitalRadar() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());

        String session2 = "12345678999987654321";
        User user2 = userService.createNewUser("88888","88888", "en_US", "0001", "chatgame-1.1", "nick-sadflogin", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(user2.getId(), user2.getAppId(), session2);
        //保存session对应的user_Id
        userService.saveUserSession(session2, user2.getId(), user2.getAppId());

        User user = new User();
        user.setId(user2.getId());
        user.setNickname(user2.getNickname());
        CurrentUser.user(user);
        mockMvc.perform(post("/api/user/radar/join?dig={dig}", "12")
                .header("client-session", session2)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        user.setId(userLogin.getId());
        user.setNickname(userLogin.getNickname());
        CurrentUser.user(user);
        mockMvc.perform(post("/api/user/radar/join?dig={dig}", "12")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
//                .andDo(print());
                .andExpect(jsonPath("$.users[0].nickname", is("nick-sadflogin")));

        mockMvc.perform(post("/api/user/radar/exit?dig={dig}", "12")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000))); //send the verify code.

    }

    @Test
    public void testBindDevice() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());

        mockMvc.perform(post("/api/user/bind/device?mobile={mobile}&app_id={app_id}&device_type={device_type}&countrycode=0001", "13111111111", 0, 1)
                .header("client-version", "1-1.0.0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4002))); //send the verify code.

        mockMvc.perform(post("/api/user/bind/device?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}&countrycode=0001", "13111111111", 0, 1, 0000)
                .header("client-version", "1-1.0.0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4003)));

        CurrentUser.user(userLogin);

        cacheService.set("13111111111", "8888");
        String saltMobile = DigestUtils.md5DigestAsHex((salt + "13111111111").getBytes());
        mockMvc.perform(post("/api/user/bind/device?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}&countrycode=0001", "13111111111", 0, 1, 8888)
                .header("client-version", "1-1.0.0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobile", is(saltMobile)))
                .andExpect(jsonPath("$.can_be_modified", is(1)));

        mockMvc.perform(post("/api/user/bind/device?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}", "13111111111", 0, 1, 8888)
                .header("client-version", "1-1.0.0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobile", is(saltMobile)))
                .andExpect(jsonPath("$.can_be_modified", is(1)));
    }

    @Test
    public void testUserInfo() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());

        // requirement: account comes primarily from nickname
        User user = userService.createNewUser("99999","12345672", "en_US", "0001", "chatgame-1.1", "12345671", null, 1);
        MobileIndex mobileIndex = new MobileIndex();
        mobileIndex.setMobileKey(new MobileKey("12345672", "0001"));
        mobileIndex.setUserId(user.getId());
        manager.insert(mobileIndex);
        User user2 = userService.createNewUser("99999","12345671", "en_US", "0001", "chatgame-1.1", "12345672", null, 1);
        mobileIndex = new MobileIndex();
        mobileIndex.setMobileKey(new MobileKey("12345671", "0001"));
        mobileIndex.setUserId(user2.getId());
        manager.insert(mobileIndex);

        // test if account result comes before mobile list results
        mockMvc.perform(get("/api/user/mobile?mobile=" + user.getMobile())
                .header("client-session", session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].mobile", is("12345671")));
        mockMvc.perform(get("/api/user/mobile?mobile=" + user2.getMobile())
                .header("client-session", session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].mobile", is("12345672")));

        String saltMobile = DigestUtils.md5DigestAsHex((salt + "13800000000").getBytes());
        mockMvc.perform(get("/api/user?id=" + "0cb6d440a4f811e3ac7abfd52f77bd17")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobile", is(saltMobile)));
    }

    @Test
    public void testUserRegister() throws Exception {

        cacheService.set("13111111111", "8888");
        mockMvc.perform(post("/api/user/register?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}&" +
                "countrycode={countrycode}&sex={sex}", "13111111111", 0, 1, 8888, "0086", 1)
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4001)));
        mockMvc.perform(post("/api/user/register?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}&" +
                "countrycode={countrycode}&sex={sex}&nickname={nickname}", "13111111111", 0, 1, 1234, "0086", 1, "Jack")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4003)));
        mockMvc.perform(post("/api/user/register?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}&" +
                "countrycode={countrycode}&sex={sex}", "13111111111", 0, 1, null, "0086", 1)
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4002)));
        mockMvc.perform(post("/api/user/register?mobile={mobile}&app_id={app_id}&device_type={device_type}&authcode={authcode}&" +
                "countrycode={countrycode}&sex={sex}&nickname={nickname}", "13111111111", 0, 1, 8888, "0086", 1, "Jack")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", is("Jack")))
                .andExpect(jsonPath("$.can_be_modified", is(1)));
    }

    @Test
    public void testLogout() throws Exception {
        mockMvc.perform(get("/api/user/logout").header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
    }

    @Test
    public void testAuth() throws Exception {
        mockMvc.perform(get("/api/user/auth").header("client-session", "0"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserInfoByMo() throws Exception {
        mockMvc.perform(get("/api/user/exist?mobile={mobile}&countrycode={countrycode}", "13800000000", "0086")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(1)));
        mockMvc.perform(get("/api/user/exist?mobile={mobile}&countrycode={countrycode}", "13800000000", "0011")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(0)));
    }


    @Test
    public void testModifyMobile() throws Exception {
        mockMvc.perform(post("/api/user/modify_mobile?mobile={mobile}&countrycode={countrycode}&authcode={authcode}",
                "099891fc266cad3d6dae315d9518dd32", "8888", "0086")
                .header("client-session", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4024)));
        mockMvc.perform(post("/api/user/modify_mobile?mobile={mobile}&countrycode={countrycode}&authcode={authcode}",
                "099891fc266cad3d6dae315d9518dd32", "0086", "")
                .header("client-session", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4025)));
        mockMvc.perform(post("/api/user/modify_mobile?mobile={mobile}&countrycode={countrycode}&authcode={authcode}",
                "13800000009", "0086", "")
                .header("client-session", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4002)));
        mockMvc.perform(post("/api/user/modify_mobile?mobile={mobile}&countrycode={countrycode}&authcode={authcode}",
                "13800000009", "0086", "123")
                .header("client-session", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4003)));

        cacheService.set("13800000009", "8888");
        mockMvc.perform(post("/api/user/modify_mobile?mobile={mobile}&countrycode={countrycode}&authcode={authcode}",
                "13800000009", "0086", "8888")
                .header("client-session", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", is("user7")));

    }

    @Test
    public void testGetServerAddr() throws Exception {
        mockMvc.perform(get("/api/server/addr")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tcp_server", notNullValue()));
    }

    @Test
    public void testDisturb() throws Exception {
        mockMvc.perform(post("/api/user/disturb?disable={disable}&time={time}", "yes", "1:00-2:00")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
    }

    @Test
    public void testUpdateUser() throws Exception {
        mockMvc.perform(post("/api/user/upload?nickname={nickname}", "ha")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        mockMvc.perform(get("/api/user?id={id}", "0cb6d440a4f811e3ac7abfd52f77bd17")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", is("ha")));
    }

    @Test
    public void testUserConversation() throws Exception {
        mockMvc.perform(post("/api/user/conversation/add?entity_id={entity_id}&type={type}", "123", "1")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        mockMvc.perform(post("/api/user/conversation/remove?entity_id={entity_id}&type={type}", "123", "1")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
    }

    @Test
    public void testMsg() throws Exception {
        mockMvc.perform(get("/api/user/message/msg_snap?last_message_id={last_message_id}&length={length}", 2, 5)
                .header("client-session", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".$messages[0].id", is(5)));
    }

    @Test
    public void testSetUnreadMessageCont() throws Exception {
        mockMvc.perform(post("/api/user/message/unread?unread={unread}", 5)
                .header("client-session", "1"))
                .andExpect(status().isOk());
        Assert.assertTrue(messageService.findUserLocalMsgCount("0cb6d443a4f811e3ac7abfd52f77bd18").getCounter().get() == 5);
    }

    @Test
    public void testUploadContactsWithName() throws Exception {
        mockMvc.perform(post("/api/contacts/upload_with_name?contact={contact}&app_id={app_id}", "[[\"008613800000001\",\"小西\"],[\"008613800000002\",\"小刀\"]]", 0)
                .header("client-session", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person[0].nickname", is("user3")))
                .andExpect(jsonPath("$.person[1].nickname", is("user4")));
    }

    @Test
    public void testUploadContacts() throws Exception {
        mockMvc.perform(post("/api/contacts/upload?phone={phone}&app_id={app_id}", "008613800000001,008613800000002", 0)
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person[0].nickname", is("user3")))
                .andExpect(jsonPath("$.person[1].nickname", is("user4")));
    }

    @Test
    public void testContacts() throws Exception {
        mockMvc.perform(get("/api/contacts")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andDo(print());
//                .andExpect(jsonPath("$.person[0].nickname", is("user3")));
        mockMvc.perform(get("/api/app_contacts?resource_app_id={resource_app_id}", 0)
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person[0].nickname", is("user3")));
        mockMvc.perform(post("/api/contact/addByUid?uids={uids}", "0cb6d440a4f811e3ac7abfd52f77bd17")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        mockMvc.perform(post("/api/contact/add?key={key}&countrycode={countrycode}", "13800000001", "0086")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        mockMvc.perform(post("/api/contact/add?key={key}&countrycode={countrycode}", "123", "0086")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4018)));
        mockMvc.perform(post("/api/contact/add?key={key}&countrycode={countrycode}", "13800000000", "0086")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(4021)));
        mockMvc.perform(post("/api/contact/del?name={name}&app_id={app_id}", "0cb6d440a4f811e3ac7abfd52f77bd17", 0)
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        mockMvc.perform(post("/api/contact/name?friend_id={friend_id}&app_id={app_id}&contact_name={contact_name}", "0cb6d442a4f811e3ac7abfd52f77bd17", 0, "NewName")
                .header("client-session", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code", is(2000)));
        Assert.assertEquals(userService.findFriendInfo(new UserKey("0cb6d440a4f811e3ac7abfd52f77bd17", 0, "0cb6d442a4f811e3ac7abfd52f77bd17")).getContactName(), "NewName");
    }

    @Test
    public void testGetExpressionList() throws Exception {
        mockMvc.perform(get("/api/user/expression_list")
                .header("client-session", "0"))
                .andExpect(jsonPath("$.expression", notNullValue()));
    }
}
