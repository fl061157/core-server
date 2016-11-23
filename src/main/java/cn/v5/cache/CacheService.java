package cn.v5.cache;

/**
 * Created by fangliang on 3/3/15.
 */
public interface CacheService {

    public String get(String key);

    public byte[] get(byte[] key);

    public String set(String key, String value);

    public String set(byte[] key, byte[] value);

    public Long del(String key);

    public Long del(byte[] key);

    public Long incBy(String key, int incr);

    public Long incBy(byte[] key, int incr);

    public Long expire(String key, int seconds);

    public Long expire(byte[] key, int seconds);

    public String setEx(String key, int seconds, String value);

    public String setEx(byte[] key, int seconds, byte[] value);

    public Long hDel(String key, String... fields);

    public Long hDel(byte[] key, byte[]... fields);

    public Long hSet(String key, String field, String value);

    public Long hSet(byte[] key, byte[] field, byte[] value);

    public String hGet(String key, String field);

    public byte[] hGet(byte[] key, byte[] field);

    public Long ttl(String key);

}
