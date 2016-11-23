package cn.v5.service;

import cn.v5.entity.TableCount;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.CounterBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class TableCountService implements InitializingBean {
    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    private PreparedStatement pstmt ;

    @Override
    public void afterPropertiesSet() throws Exception {
        pstmt = manager.getNativeSession().prepare("update table_counter set count = count+? where table_name = ?");

    }

    public void incr(String table, long delta) {
        Session session = manager.getNativeSession();
        session.executeAsync(new BoundStatement(pstmt).bind(delta, table));
    }



}
