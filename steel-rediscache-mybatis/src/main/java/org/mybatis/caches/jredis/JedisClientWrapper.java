
package org.mybatis.caches.jredis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.mybatis.caches.jredis.help.ParameterizedTypParseHelper;
import org.mybatis.caches.jredis.help.ParameterizedType;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.gw.inetact.cache.redis.RedisCacheEngine;

/**
 * @author Dongpo.wu
 */
public final class JedisClientWrapper {


    private final Log log = LogFactory.getLog(JedisClientWrapper.class);

    private final RedisCacheEngine cacheEngine;

    public JedisClientWrapper() {
    	if(log.isDebugEnabled()){
    		log.debug("Begin to init the jedis client wrapper");
    	}
    	cacheEngine = RedisCacheEngine.getInstance();       
    }

    /**
     * Converts the MyBatis object key in the proper string representation.
     * 
     * @param key the MyBatis object key.
     * @return the proper byte[] representation.
     */
    private String toKeyString(final Object key, String id) {
        String keyString = "_mybatis_" + id + "-" + DigestUtils.sha1Hex(key.toString()); 
        if (log.isDebugEnabled()) {
            log.debug("Object key '" + key + "' converted in '" + keyString + "'");
        }
        
        return keyString;
    }

    /**
     *
     * @param key
     * @return
     */
    public Object getObject(Object key, String id) {
    	String keyString = toKeyString(key, id);
        if(log.isDebugEnabled()){
        	log.debug(("Retriving key : "+key+"  keyString= "+keyString));
        }
    	
        Object ret = retrieve(keyString);       

        if (log.isDebugEnabled()) {
            log.debug("Retrived object ("+ keyString + ", " + ret + ")");
        }

        return ret;
    }    
   
    /**
     * Return the stored group in Redis identified by the specified key.
     *
     * @param groupKey the group key.
     * @return the group if was previously stored, null otherwise.
     */
    @SuppressWarnings("unchecked")
    private Set<String> getGroup(String groupKey) {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving group with id '" + groupKey + "'");
        }

        Object groups = null;
        try {
            groups = retrieve(groupKey);
        } catch (Exception e) {
            log.error("Impossible to retrieve group '" + groupKey + "' see nested exceptions", e);
        }

        if (groups == null) {
            if (log.isDebugEnabled()) {
                log.debug("Group '" + groupKey + "' not previously stored");
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved group '" + groupKey + "' with values " + groups);
        }
        return (Set<String>) groups;
    }

    /**
     *
     *
     * @param keyString
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Object retrieve(final String keyString) {    	      
        ShardedJedis jedis = null;
        String value;
        ShardedJedisPool pool = cacheEngine.getPool();
		try {
			jedis = pool.getResource();
			value = jedis.get(keyString); 
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}
		Object retrieved = null;
		if(value!=null){
			try{
				RedisCacheMapperItem cache = JSON.parseObject(value, RedisCacheMapperItem.class);
				if(log.isDebugEnabled()){
					log.debug("Cache value : "+ JSON.parseObject(cache.getCacheValue(),String.class));
				}
				ParameterizedType pType = cache.getParameterizedType();
				if(pType ==null||pType.getActualTypeArguments()==null||pType.getActualTypeArguments().length<1)
					retrieved = JSON.parseObject(JSON.parseObject(cache.getCacheValue(),String.class), cache.getRawType());
				else{
					ParameterizedTypeImpl fastJsonPtype = new ParameterizedTypeImpl(pType.getActualTypeArguments(), pType.getOwnerType(), pType.getRawType());
					retrieved = JSON.parseObject(JSON.parseObject(cache.getCacheValue(),String.class), fastJsonPtype);
				}
			}catch(Exception ex){
				log.error("Retrieve value from redis cash error ", ex);
				retrieved=null;
			}
		
		}
        return retrieved;
    }
    
    
    
    public void putObject(Object key, Object value, String id) {
        String keyString = toKeyString(key, id);
        String groupKey = toKeyString(id, id);
        if (log.isDebugEnabled()) {
            log.debug("Putting object (" + keyString + ", " + value + ")");
        }
        storeInRedis(keyString, value);        
        Set<String> group = getGroup(groupKey);
        if (group == null) {
            group = new HashSet<String>();
        }
        group.add(keyString);

        if (log.isDebugEnabled()) {
            log.debug("Insert/Updating object (" + groupKey + ", " + group + ")");
        }

        storeInRedis(groupKey, group);
    }

    /**
     * Stores an object identified by a key in Redis.
     *
     * @param keyString the object key
     * @param value the object has to be stored.
     */
    private void storeInRedis(String keyString, Object value) {
    	RedisCacheMapperItem item = null;
    	ParameterizedType paraType = ParameterizedTypParseHelper.parse(value);
    	
    	item = new RedisCacheMapperItem(value, value.getClass(), paraType);
    	
    	ShardedJedis jedis = null;
    	ShardedJedisPool pool = cacheEngine.getPool();
    	String objSer = JSON.toJSONString(item);
    	try{
    		jedis = pool.getResource();
    		jedis.set(keyString, objSer);
    	}finally{
    		if(jedis!=null)
    			pool.returnResource(jedis);
    	}
    }

    public Object removeObject(Object key, String id) {
        String keyString = toKeyString(key, id);

        if (log.isDebugEnabled()) {
            log.debug("Removing object '"  + keyString  + "'");
        }
        Object result = getObject(key, id);
        ShardedJedis jedis = null;
    	ShardedJedisPool pool = cacheEngine.getPool();
    	try{
    		jedis = pool.getResource();
    		jedis.del(keyString);
    	}finally{
    		if(jedis!=null)
    			pool.returnResource(jedis);
    	}
        return result;
    }

    public void removeGroup(String id) {
        String groupKey = toKeyString(id, id);

        Set<String> group = getGroup(groupKey);

        if (group == null) {
            if (log.isDebugEnabled()) {
                log.debug("No need to flush cached entries for group '" + id  + "' because is empty");
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Flushing keys: " + group);
        }
        
        ShardedJedis jedis = null;
    	ShardedJedisPool pool = cacheEngine.getPool();
    	try{
    		jedis = pool.getResource();
    		for (String key : group) {
    			jedis.del(key);
            }
    		if (log.isDebugEnabled()) {
	            log.debug("Flushing group: " + groupKey);
	        }
    		jedis.del(groupKey);
    	}finally{
    		if(jedis!=null)
    			pool.returnResource(jedis);
    	}
    }   
    
    public static void main(String[] args) {
		List<String>  a = new ArrayList<String>();
		System.out.println(a instanceof Collection<?>);
	}
    
}
