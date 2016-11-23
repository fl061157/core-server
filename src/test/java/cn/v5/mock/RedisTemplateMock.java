package cn.v5.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by piguangtao on 15/2/5.
 */
public class RedisTemplateMock<K, V> extends RedisTemplate<K, V> {
    private static Logger LOGGER = LoggerFactory.getLogger(RedisTemplateMock.class);

    private ConcurrentMap<K, V> memPool;
    private ConcurrentMap<Object,ConcurrentMap<String,Long>> hashPool;
    private DefaultValueOperation operation = new DefaultValueOperation();
    private HashOperations hashOpertion = new HashValueOperation();

    @Override
    public void afterPropertiesSet() {
        LOGGER.debug("enter redis template mock");
        memPool = new ConcurrentHashMap<>();
        hashPool=new ConcurrentHashMap<>();
    }

    @Override
    public <T> T execute(RedisCallback<T> action, boolean exposeConnection, boolean pipeline) {
        return null;
    }

    @Override
    public List<Object> executePipelined(final SessionCallback<?> session, final RedisSerializer<?> resultSerializer) {
        return super.executePipelined(session, resultSerializer);
    }

    @Override
    public List<Object> executePipelined(final RedisCallback<?> action, final RedisSerializer<?> resultSerializer) {
        return null;
    }

    @Override
    public void delete(K key) {
        memPool.remove(key);
    }

    @Override
    public void delete(Collection<K> keys) {
        super.delete(keys);
    }

    @Override
    public Boolean hasKey(K key) {
        return super.hasKey(key);
    }

    @Override
    public Boolean expire(K key, final long timeout, final TimeUnit unit) {
        return false;
    }

    @Override
    public Boolean expireAt(K key, final Date date) {
        return super.expireAt(key, date);
    }

    @Override
    public void restore(K key, final byte[] value, long timeToLive, TimeUnit unit) {
        super.restore(key, value, timeToLive, unit);
    }

    @Override
    public Set<K> keys(K pattern) {
        Set<K> ret = new HashSet<>();
        for (K entry : memPool.keySet()) {
            if(match(entry.toString(),pattern.toString()))
                ret.add(entry);
        }
        return ret;
    }

    private boolean match(String str,String pattern) {
        String[] parts=pattern.split("\\*");
        int tmpIndex;
        for(int j=0,i=0;j<parts.length;j++) {
            if(i>=str.length()) {
                return false;
            }else {
                tmpIndex = str.indexOf(parts[j],i);
                if(tmpIndex==-1){
                    return false;
                }else {
                    i+=parts[j].length();
                }
            }
        }
        return true;
    }

    public ValueOperations opsForValue() {
        return operation;
    }
    public HashOperations opsForHash(){
        return hashOpertion;
    }

    @SuppressWarnings("unchecked")
    public class HashValueOperation implements HashOperations {

        @Override
        public void delete(Object o, Object... objects) {
            ConcurrentMap internMap=hashPool.get(o);
            if(internMap==null)
                return ;
            for(Object object:objects) {
                internMap.remove(object);
            }
        }

        @Override
        public Boolean hasKey(Object o, Object o2) {
            return null;
        }

        @Override
        public Object get(Object o, Object o2) {
            ConcurrentMap internMap=hashPool.get(o);
            if(internMap==null){
                return null;
            }
            else{
                return internMap.get(o2);
            }
        }

        @Override
        public List multiGet(Object o, Collection collection) {
            return null;
        }

        @Override
        public Long increment(Object o, Object o2, long l) {
            return null;
        }

        @Override
        public Double increment(Object o, Object o2, double v) {
            return null;
        }

        @Override
        public Set keys(Object o) {
            return null;
        }

        @Override
        public Long size(Object o) {
            return null;
        }

        @Override
        public void putAll(Object o, Map map) {

        }

        @Override
        public void put(Object key, Object hashKey, Object value) {
            ConcurrentMap<String,Long> internMap= hashPool.get(key);
            if(internMap==null){
                internMap=hashPool.putIfAbsent(key, new ConcurrentHashMap<String, Long>());
                if(internMap==null){
                    internMap=hashPool.get(key);
                }
            }
            internMap.put((String)hashKey,(Long)value);
        }

//        @Override
//        public void put(Objects o, Object o2, Object o3) {
//            ConcurrentMap<String,Long> internMap= hashPool.get(o);
//            if(internMap==null){
////                internMap=hashPool.putIfAbsent(o, new ConcurrentHashMap<String, Long>());
//            }
////            internMap.put(o2,o3);
//        }

        @Override
        public Boolean putIfAbsent(Object o, Object o2, Object o3) {
            return null;
        }

        @Override
        public List values(Object o) {
            return null;
        }

        @Override
        public Map entries(Object o) {
            ConcurrentMap internMap= hashPool.get(o);
            return internMap;
        }

        @Override
        public RedisOperations getOperations() {
            return null;
        }

        @Override
        public Cursor<Map.Entry> scan(Object o, ScanOptions scanOptions) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public class DefaultValueOperation implements ValueOperations {

        @Override
        public void set(Object o, Object o2) {
            memPool.put((K) o, (V) o2);
        }

        @Override
        public void set(Object o, Object o2, long l, TimeUnit timeUnit) {

        }

        @Override
        public Boolean setIfAbsent(Object o, Object o2) {
            return null;
        }

        @Override
        public void multiSet(Map map) {

        }

        @Override
        public Boolean multiSetIfAbsent(Map map) {
            return null;
        }

        @Override
        public Object get(Object o) {
            return memPool.get(o);
        }

        @Override
        public Object getAndSet(Object o, Object o2) {
            return null;
        }

        @Override
        public List multiGet(Collection collection) {
            List list = new LinkedList();
            for (Object s : collection) {
                list.add(memPool.get(s));
            }
            return list;
        }

        @Override
        public Long increment(Object o, long l) {
            return null;
        }

        @Override
        public Double increment(Object o, double v) {
            return null;
        }

        @Override
        public Integer append(Object o, String s) {
            return null;
        }

        @Override
        public String get(Object o, long l, long l1) {
            return null;
        }

        @Override
        public void set(Object o, Object o2, long l) {

        }

        @Override
        public Long size(Object o) {
            return null;
        }

        @Override
        public RedisOperations getOperations() {
            return null;
        }
    }
}
