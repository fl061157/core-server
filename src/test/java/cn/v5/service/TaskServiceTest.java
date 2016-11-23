package cn.v5.service;

import cn.v5.test.TestTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by piguangtao on 15/9/23.
 */
public class TaskServiceTest extends TestTemplate {

    @Autowired
    private TaskService taskService;

    @Test
    public void testDiscard() throws InterruptedException {
        int count = 10000 + 10000;
        for (int i = 0; i < count; i++) {
            final int j = i;
            taskService.execute(() -> {
                System.out.println("no:" + j);
                try {
                    Thread.sleep(3600 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(3600*1000);
    }

}