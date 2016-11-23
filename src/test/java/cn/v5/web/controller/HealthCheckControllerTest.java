package cn.v5.web.controller;

import cn.v5.test.TestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Properties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class HealthCheckControllerTest extends TestTemplate {

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    /**
     * 采用mock替换某个类属性
     */
    @InjectMocks
    @Autowired
    private HealthCheckController healthCheckController;


    @Value("${avatar.storage.path}")
    private String avatarStoragePath;


    @Resource(name = "appConfig")
    private Properties appProperties;


    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    public void testCassandraHealthCheck() throws Exception {
        System.out.println(appProperties);
        System.out.println(avatarStoragePath);
        mockMvc.perform(get("/api/health_check/cassandra").header("client-session", "0"))
                .andExpect(status().isOk())
                .andDo(print());
    }

}