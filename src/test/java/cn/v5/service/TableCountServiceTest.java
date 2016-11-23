package cn.v5.service;

import cn.v5.entity.TableCount;
import cn.v5.test.TestTemplate;
import info.archinnov.achilles.type.CounterBuilder;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.inject.Inject;

/**
 * Created by wyang on 2014/6/18.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TableCountServiceTest extends TestTemplate {
    @Inject
    TableCountService tableCountService;

    @Before
    public void init() {
        TableCount tc = new TableCount();
        tc.setTable("yw1");
        tc.setCount(CounterBuilder.incr(5));
        manager.insert(tc);
    }

    @Test
    public void test001Incr() {
        TableCount tc = new TableCount();
        tc.setTable("yw1");
        tc.setCount(CounterBuilder.incr(101));
        tc = manager.insert(tc);
        assertSame((long) 106, tc.getCount().get());

        tc = manager.find(TableCount.class, "yw1");
        assertSame((long) 106, tc.getCount().get());

        tableCountService.incr("yw1", 1);
        tc = manager.find(TableCount.class, "yw1");
        assertSame((long) 107, tc.getCount().get());
    }

    @Test
    public void test002Set() throws InterruptedException {
        TableCount tc = manager.find(TableCount.class, "yw1");
        assertNotNull(tc);

        long expected = 15;
        long current = tc.getCount().get();
        if (current > expected) {
            tc.getCount().decr(current - expected);
        } else if (current < expected) {
            tc.getCount().incr(expected - current);
        } else {
            return;
        }
        manager.update(tc);
        tc = manager.find(TableCount.class, "yw1");
        assertSame(expected, tc.getCount().get());
    }
}
