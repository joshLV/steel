
package org.mybatis.caches.jredis;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.cache.Cache;

/**
 * The JredisPool-based Cache implementation.
 *
 * @author Dongpo wu 
 */
public final class JedisPoolCache implements Cache {

    private static final JedisClientWrapper wrapper = new JedisClientWrapper();
    
    private static Log logger = LogFactory.getLog(JedisPoolCache.class);
    /**
     * The {@link ReadWriteLock}.
     */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * The cache id.
     */
    private final String id;
    
    private Class mapperClazz=null;;

    /**
     * Builds a new Redis-based Cache.
     *
     * @param id the Mapper id.
     */
    public JedisPoolCache(final String id) {
        this.id = id;
        try {
			mapperClazz = Class.forName(id);
		} catch (ClassNotFoundException e) {
			logger.error("Id should be a class name");
		}
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
    	wrapper.removeGroup(this.id);
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject(Object key) {
    	if(mapperClazz ==null) return null;
    	
    	Object rtn = wrapper.getObject(key, this.id);    	
    	return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }

    /**
     * {@inheritDoc}
     */
    public int getSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    public void putObject(Object key, Object value) {
    	wrapper.putObject(key, value, this.id);
    }

    /**
     * {@inheritDoc}
     */
    public Object removeObject(Object key) {
        return wrapper.removeObject(key, this.id);
    }

}
