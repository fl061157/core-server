package cn.v5.cassandra;

import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 内嵌式cassandra server，专门用于单元测试
 */
public class EmbeddedCassandraFactoryBean extends AbstractFactoryBean<PersistenceManager> {
    private static Logger LOGGER = LoggerFactory.getLogger(EmbeddedCassandraFactoryBean.class);
    private static PersistenceManager manager;

    private static AtomicBoolean dataLoaded = new AtomicBoolean(false);

    static {
        System.setProperty("cassandra.native.epoll.enabled", "false");
        manager = CassandraEmbeddedServerBuilder
                .withEntityPackages("cn.v5")
                .withKeyspaceName("faceshow")
                .cleanDataFilesAtStartup(true)
                .withCQLPort(9261).withThriftPort(9341)
                .withDurableWrite(true)
                .buildPersistenceManager();
        loadData(manager);
    }

    @Override
    public Class<?> getObjectType() {
        return PersistenceManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected PersistenceManager createInstance() throws Exception {
        return manager;
    }

    private static void loadData(PersistenceManager manager){
        if(dataLoaded.compareAndSet(false,true)){
            InputStream inputStream = EmbeddedCassandraFactoryBean.class.getClassLoader().getResourceAsStream("data.cql");
            loadCql(manager, inputStream);
        }
    }

    public static void loadCql(PersistenceManager manager, InputStream inputStream) {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while(true) {
                try {
                    String sql = reader.readLine();
                    if(sql == null) break;
                    if(sql.trim().length() < 1) continue;
                    System.out.println("executing " + sql);
                    manager.getNativeSession().execute(sql);
                }catch (IOException e) {
                    LOGGER.error("fails to load cql data");
                    break;
                }
            }
        }catch (Exception e){
            LOGGER.error("fails to init cql data",e);
        }finally {
            if(null != reader){
                IOUtils.closeQuietly(reader);
            }
        }
    }
}