package cn.v5.web;

import cn.v5.entity.User;
import cn.v5.entity.game.Game;
import cn.v5.service.MessageService;
import cn.v5.service.UserService;
import cn.v5.test.TestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by yangwei on 14-11-25.
 */
@WebAppConfiguration
public class GameControllerTest extends TestTemplate {
    @Autowired
    private WebApplicationContext ctx;

    @Inject
    private UserService userService;//用于制造测试数据

    @Inject
    private MessageService messageService;

    @Value("${game.static.url}")
    private String gameStaticUrl;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        manager.insert(new Game(new Game.GameKey(12, "0086"), "social activities", "Elimination of the game is a casual game,with simple and",
                "avatar", "20141023", 1, "desc_image", "desc_video", "game_images", "16*9", "U We Go", "", 1));
    }

    @Test
    public void testGameList() throws Exception {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999","99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());

        mockMvc.perform(get("/api/game/list")
                .header("client-session", session)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.game_info", hasSize(1)))
                .andExpect(jsonPath("$.game_info[0].classification", is("social activities")))
                .andExpect(jsonPath("$.game_info[0].name", is("U We Go")))
                .andExpect(jsonPath("$.game_info[0].desc", is("Elimination of the game is a casual game,with simple and")))
//                .andExpect(jsonPath("$.game_info[0].avatar_url", is(gameStaticUrl + "/avatar")))
                .andExpect(jsonPath("$.game_info[0].app_id", is(12)))
                .andExpect(jsonPath("$.game_info[0].version", is("20141023")))
                .andExpect(jsonPath("$.game_info[0].bbscomments", is(1)))
                .andExpect(jsonPath("$.game_info[0].desc_image_url", is("desc_image")))
                .andExpect(jsonPath("$.game_info[0].desc_video_url", is("desc_video")))
                .andExpect(jsonPath("$.game_info[0].game_images_url[0]", is("game_images")))
                .andExpect(jsonPath("$.game_info[0].video_res", is("16*9"))); //
    }
}
