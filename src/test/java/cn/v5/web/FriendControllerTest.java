package cn.v5.web;

import cn.v5.entity.User;
import cn.v5.service.MessageQueueService;
import cn.v5.service.UserService;
import cn.v5.test.TestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by lb on 14/11/5.
 */
@WebAppConfiguration
public class FriendControllerTest extends TestTemplate {
    @Autowired
    private WebApplicationContext ctx;

    @Inject
    private UserService userService;

    @Mock
    private MessageQueueService messageQueueService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
//        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();

    }

    @Test
    public void testRecommend() throws Exception {
        mockMvc.perform(get("/api/friend/recommend").header("client-session", "0"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testClearFriendRequest() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());

        mockMvc.perform(post("/api/friend/request/clear").header("client-session", session))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testAddContact() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());

        mockMvc.perform(post("/api/contact/add?key=111111&countrycode=0086").header("client-session", session))
                .andExpect(status().isOk())
                .andDo(print());

    }
}
