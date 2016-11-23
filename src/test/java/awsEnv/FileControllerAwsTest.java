package awsEnv;

import cn.v5.service.FileStoreService;
import cn.v5.service.MessageService;
import cn.v5.service.UserService;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.Map;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created by handwin on 2015/2/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "classpath:application-test.xml"
})
@WebAppConfiguration
public class FileControllerAwsTest {
    @Autowired
    private WebApplicationContext ctx;

    @Inject
    private UserService userService;
    @Inject
    private FileStoreService fileStoreService;
    @Inject
    private MessageService messageService;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    private MockMvc mockMvc;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "aws");
    }
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    public void testFileUpload() throws Exception {
        FileInputStream inputStream=new FileInputStream(this.getClass().getClassLoader().getResource("robot.png").getPath());
        MvcResult result=mockMvc.perform(fileUpload("/api/file/upload?uploadType={uploadType}", "feedback").file(new MockMultipartFile("file", inputStream))
                .header("client-session", "0")
                .accept(MediaType.ALL))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(CoreMatchers.instanceOf(Map.class)))
                .andReturn();
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_id", notNullValue()))
                .andExpect(jsonPath("$.file_url", notNullValue()))
                .andDo(print());
        IOUtils.closeQuietly(inputStream);
    }
    @Test
    public void testAvatarUpload() throws Exception {
        FileInputStream inputStream = new FileInputStream(this.getClass().getClassLoader().getResource("robot.png").getPath());
        MvcResult result = mockMvc.perform(fileUpload("/api/user/avatar/upload").file(new MockMultipartFile("file", inputStream))
                .header("client-session", "0")
                .accept(MediaType.ALL))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(CoreMatchers.instanceOf(Map.class)))
                .andReturn();
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file_id", notNullValue()))
                .andExpect(jsonPath("$.file_url", notNullValue()))
                .andDo(print());
        IOUtils.closeQuietly(inputStream);
    }
//    @Test
//    public void testFileDownload() throws Exception {
//        User user = new User();
//        user.setId("123");
//        CurrentUser.user(user);
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("robot.png");
//
//        String key = fileStoreService.storeFile(inputStream, "robot.png", inputStream.available());
//
//        MvcResult result=mockMvc.perform(get("/api/file/download/{fileId}",key)
//                .header("client-session", "0")
//                .accept(MediaType.ALL))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//        mockMvc.perform(asyncDispatch(result))
//                .andExpect(status().isOk())
//                .andDo(print());
//
//        IOUtils.closeQuietly(inputStream);
//    }
}
