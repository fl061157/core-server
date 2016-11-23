package cn.v5;

import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by roger on 14-3-5.
 */
public class Fixtures {
    public static void loadCql(PersistenceManager manager, String name) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Fixtures.class.getClassLoader().getResourceAsStream(name)));
        try{
            while(true) {
                try {
                    String sql = reader.readLine();
                    if(sql == null) break;
                    if(sql.trim().length() < 1) continue;
                    System.out.println("executing " + sql);
                    manager.getNativeSession().execute(sql);
                }catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }
}