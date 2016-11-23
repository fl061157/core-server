package cn.v5.test;

import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * Created by piguangtao on 15/2/5.
 */
public class JettyForTest {
    public static void main(String[] args) {
        System.setProperty("cassandra.native.epoll.enabled","false");
        System.setProperty("spring.profiles.active", "dev");
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

        context.load("jetty.xml");
        context.refresh();


    }

}
