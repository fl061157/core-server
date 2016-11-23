package awsEnv;

import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.file.FileInfo;
import cn.v5.service.FileStoreService;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.InputStream;


/**
 * Created by hi on 14-4-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "classpath:application-test.xml"
})
public class FileServiceAwsTest {
    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "aws");
    }

    @Inject
    private FileStoreService fileStoreService;

    @Test
    public void testUploadAwsWithType() {
        User user = new User();
        user.setId("123");
        CurrentUser.user(user);
        String key;
        String[] type = {"default", "feedback", "avatar"};
        for (String entry : type) {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("robot.png");
            try {
                FileInfo info = fileStoreService.storeFile(inputStream, "robot.png", inputStream.available(), entry);
                key = info.getAccessKey();
                Assert.assertNotNull(key);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    @Test
    public void testUploadDownload() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        User user = new User();
        user.setId("123");
        CurrentUser.user(user);
        String key;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("robot.png");
        try {
            FileInfo info = fileStoreService.storeFile(inputStream, "robot.png", inputStream.available());
            key = info.getAccessKey();
            Assert.assertNotNull(key);
            fileStoreService.fetchFile(response, key, null, null, null, false);
            Assert.assertEquals(response.getContentType(), "image/png");
            Assert.assertEquals(response.getContentLength(), 46690);
            Assert.assertEquals(response.getStatus(), 200);
            response = new MockHttpServletResponse();
            if (key != null) {
                fileStoreService.fetchFile(response, key.concat("!30"), null, null, null, false);
                Assert.assertEquals(response.getContentType(), "image/png");
                Assert.assertEquals(response.getContentLength(), 672);
                Assert.assertEquals(response.getStatus(), 200);
            }
            fileStoreService.fetchFile(response, key, "bytes=2-4", null, null, false);
            Assert.assertEquals(response.getContentType(), "image/png");
            Assert.assertEquals(response.getContentLength(), 3);
            Assert.assertEquals(response.getHeader("Content-Range"), "bytes 2-4/46690");
            Assert.assertEquals(response.getStatus(), 206);
            response = new MockHttpServletResponse();
            fileStoreService.fetchFile(response, key, "bytes=46680-", null, null, false);
            Assert.assertEquals(response.getContentType(), "image/png");
            Assert.assertEquals(response.getContentLength(), 10);
            Assert.assertEquals(response.getHeader("Content-Range"), "bytes 46680-46689/46690");
            Assert.assertEquals(response.getStatus(), 206);
            response = new MockHttpServletResponse();
            fileStoreService.fetchFile(response, key, null, null, null, true);
            Assert.assertEquals(response.getContentType(), "image/png");
            Assert.assertEquals(response.getContentLength(), 46690);
            Assert.assertEquals(response.getStatus(), 200);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}