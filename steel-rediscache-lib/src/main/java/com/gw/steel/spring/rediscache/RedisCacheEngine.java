package com.gw.steel.spring.rediscache;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ShardedJedisPool;

public class RedisCacheEngine {	
	private static Log log = LogFactory.getLog(RedisCacheEngine.class);	
	
	private ShardedJedisPool pool = null;	
	
	private ShardJedisPoolConfigBuilder shardJedisPoolConfigBuilder ;
	private ShardJedisConfigBuilder shardJedisConfigBuilder;
	
	public void init () {		
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		ShardJedisPoolConfiguration poolConfig ;
		if(shardJedisPoolConfigBuilder==null){
			poolConfig= new ShardJedisPoolConfiguration();
		}else{
			poolConfig=shardJedisPoolConfigBuilder.getConfiguration();
		}
		try {
			
			BeanUtils.copyProperties(jedisPoolConfig, poolConfig);
		} catch (IllegalAccessException e) {
			log.warn("copy the jedisPool configuration error", e);
		} catch (InvocationTargetException e) {
			log.warn("copy the jedisPool configuration error", e);
		}
		pool = new ShardedJedisPool(jedisPoolConfig, shardJedisConfigBuilder.getShards());
	}
	
	public ShardedJedisPool getPool(){
		return pool;
	}
	
	@Autowired
	public void setShardJedisConfigBuilder(
			ShardJedisConfigBuilder shardJedisConfigBuilder) {
		this.shardJedisConfigBuilder = shardJedisConfigBuilder;
	}
	@Autowired(required=false)
	public void setShardJedisPoolConfigBuilder(
			ShardJedisPoolConfigBuilder shardJedisPoolConfigBuilder) {
		this.shardJedisPoolConfigBuilder = shardJedisPoolConfigBuilder;
	}
}
