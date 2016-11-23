package cn.v5.trade.database;

import org.apache.ibatis.session.SqlSessionFactory;
import org.logicalcobwebs.proxool.ProxoolDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by fangliang on 25/2/15.
 */
@Configuration
@MapperScan(basePackages = "cn.v5.trade.mapper")
public class DatabaseConfiguration {

    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String userName;

    @Value("${db.password}")
    private String password;

    @Value("${db.maxidle}")
    private int maxIdle;

    @Value("${db.minidle}")
    private int minIdle;

    @Value("${db.maxtotal}")
    private int maxTotal;

    @Value("${db.initialsize}")
    private int initialsize;

    @Value("${db.maxwaitmills}")
    private int maxWaitMills;

    @Value("${db.poolpreparedstatements}")
    private boolean poolPreparedStatements;

    @Value("${db.timebetweenevictionrunsmillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${db.numtestsperevictionrun}")
    private int numTestsPerevictionRun;


    @Value("${db.minevictableidletimemillis}")
    private int minEvictableidleTimeMillis;

    @Value("${db.testwhileidle}")
    private boolean testWhileIdle;

    /**
     * @return
     * @throws Exception
     */

    @Bean(name = "mysql002.chatgame.me")
    @Qualifier("mysql002.chatgame.me")
    public ProxoolDataSource dataSource() throws Exception {
        ProxoolDataSource basicDataSource = new ProxoolDataSource();
        basicDataSource.setDriver("com.alibaba.cobar.jdbc.Driver");
        basicDataSource.setDriverUrl(url);
        basicDataSource.setUser(userName);
        basicDataSource.setPassword(password);
        basicDataSource.setMinimumConnectionCount(initialsize);
        basicDataSource.setMaximumConnectionCount(maxTotal);
        basicDataSource.setMaximumActiveTime(Long.MAX_VALUE);
        basicDataSource.setHouseKeepingSleepTime(timeBetweenEvictionRunsMillis);
        basicDataSource.setHouseKeepingTestSql("SELECT CURRENT_DATE");
        basicDataSource.setSimultaneousBuildThrottle(maxTotal);
        return basicDataSource;
    }


    @Bean(name = "sqlSessionFactory")
    @Qualifier("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysql002.chatgame.me") ProxoolDataSource basicDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(basicDataSource);
        return sqlSessionFactory.getObject();
    }


}
