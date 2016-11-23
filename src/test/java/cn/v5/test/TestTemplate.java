package cn.v5.test;

/**
 * Created by piguangtao on 15/2/5.
 */

import info.archinnov.achilles.persistence.PersistenceManager;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * 对Redis、Cassandra、Mq进行不同的模拟
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "classpath:application-test.xml"
})
@WebAppConfiguration
public class TestTemplate extends TestCase{
    @Autowired
    @Qualifier("manager")
    public PersistenceManager manager;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
        System.setProperty("cassandra.native.epoll.enabled","false");
    }

    @Test
    public void test(){
        Assert.assertEquals(1,1);
    }
}
