package com.gw.inetact.cache.redis;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ShardedJedisPool;

public class RedisCacheEngine {	
	private static Log log = LogFactory.getLog(RedisCacheEngine.class);	
	
	protected ShardedJedisPool pool = null;
	
	protected boolean initialized = false;	

	private static RedisCacheEngine instance = null;

	private RedisCacheEngine() {
		ShardJedisPoolConfigBuilder pb = ShardJedisPoolConfigBuilder.getInstance();
		ShardJedisConfigBuilder b = ShardJedisConfigBuilder.getInstance();
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		try {
			BeanUtils.copyProperties(jedisPoolConfig, pb.getConfiguration());
		} catch (IllegalAccessException e) {
			log.warn("copy the jedisPool configuration error", e);
		} catch (InvocationTargetException e) {
			log.warn("copy the jedisPool configuration error", e);
		}
		pool = new ShardedJedisPool(jedisPoolConfig, b.getShards());
	}

	public static RedisCacheEngine getInstance() {
		if (instance == null) {
			instance = new RedisCacheEngine();
		}
		return instance;
	}
	
	public ShardedJedisPool getPool(){
		return pool;
	}
}
