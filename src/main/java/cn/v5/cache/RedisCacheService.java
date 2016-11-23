package cn.v5.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 3/3/15.
 */

@Service("redisCacheService")
public class RedisCacheService implements CacheService {

    @Autowired
    private BinaryJedisCluster binaryJedisCluster;

    private static Logger LOGGER = LoggerFactory.getLogger(RedisCacheService.class);

    @Override
    public String get(String key) {
        String value = binaryJedisCluster.get(key);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get key: {} , value:{}  ", key, value);
        }
        return value;
    }

    @Override
    public byte[] get(byte[] key) {
        byte[] value = binaryJedisCluster.get(key);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Get key:{} ", new String(key));
        }
        return value;
    }

    @Override
    public String set(String key, String value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set key:{} , value:{} ", key, value);
        }
        return binaryJedisCluster.set(key, value);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Set key:{} ", new String(key));
        }
        return binaryJedisCluster.set(key, value);
    }


    @Override
    public Long del(String key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Del key:{} ", key);
        }
        return binaryJedisCluster.del(key);
    }

    @Override
    public Long del(byte[] key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Del key:{}", new String(key));
        }
        return binaryJedisCluster.del(key);
    }


    @Override
    public Long incBy(String key, int incr) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("IncrBy key:{} , incr:{}", key, incr);
        }
        return binaryJedisCluster.incrBy(key, incr);
    }

    @Override
    public Long incBy(byte[] key, int incr) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("IncrBy key:{} , incr:{}", new String(key), incr);
        }
        return binaryJedisCluster.incrBy(key, incr);
    }

    @Override
    public Long expire(String key, int seconds) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Expire key:{} ,seconds:{}", key, seconds);
        }
        return binaryJedisCluster.expire(key, seconds);
    }

    @Override
    public Long expire(byte[] key, int seconds) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Expire key:{} ,seconds:{}", new String(key), seconds);
        }
        return binaryJedisCluster.expire(key, seconds);
    }

    @Override
    public String setEx(String key, int seconds, String value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SetEx key:{} , seconds:{} , value:{} ", key, seconds, value);
        }
        return binaryJedisCluster.setex(key, seconds, value);
    }

    @Override
    public String setEx(byte[] key, int seconds, byte[] value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("setEx key:{} , seconds:{} ", new String(key), seconds);
        }
        return binaryJedisCluster.setex(key, seconds, value);
    }


    @Override
    public Long hDel(String key, String... fields) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hDel key:{} ", key);
        }
        return binaryJedisCluster.hdel(key, fields);
    }

    @Override
    public Long hDel(byte[] key, byte[]... fields) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hDel key:{} ", new String(key));
        }
        return binaryJedisCluster.hdel(key, fields);
    }


    @Override
    public Long hSet(String key, String field, String value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hSet key:{} , field:{} , value:{}", key, field, value);
        }
        return binaryJedisCluster.hset(key, field, value);
    }

    @Override
    public Long hSet(byte[] key, byte[] field, byte[] value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hSet key:{} , field:{} , value:{}", new String(key), new String(field), new String(value));
        }
        return binaryJedisCluster.hset(key, field, value);
    }

    @Override
    public String hGet(String key, String field) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hGet key:{} , field:{}", key, field);
        }
        return binaryJedisCluster.hget(key, field);
    }

    @Override
    public byte[] hGet(byte[] key, byte[] field) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hGet key:{} , field:{}", new String(key), new String(field));
        }
        return binaryJedisCluster.hget(key, field);
    }

    @Override
    public Long ttl(String key) {
        return binaryJedisCluster.ttl(key);
    }
}
