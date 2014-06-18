package com.gw.steel.rediscache.spring.transaction;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.gw.steel.helper.RedisCachedKeyHolder;
import com.gw.steel.spring.rediscache.RedisCacheEngine;
/**
 * DB Transaction with Redis. 
 * When the DB rollback， the redis Cache item within the same transaction should be removed.
 * @author Dongpo.wu
 *
 */
public class DBWithRedisTransactionManager implements PlatformTransactionManager{
	private static Log logger = LogFactory.getLog(DBWithRedisTransactionManager.class);
	
	private PlatformTransactionManager txManager;
	
	private RedisCacheEngine redisCacheEngine;
	
	public void commit(TransactionStatus status) throws TransactionException {
		try{
			txManager.commit(status);
		}catch(TransactionException e){
			if(logger.isDebugEnabled()){
				logger.debug("Rollback the redis keys which were set in this transaction when the transaction commit fail");
			}
			rollbackRedis(RedisCachedKeyHolder.getRedisCacheKeys());
			if(logger.isDebugEnabled()){
				logger.debug("Rollback the redis keys which were set in this transaction -- done");
			}
			throw e;
		}
	}
	
	public void rollback(TransactionStatus status) throws TransactionException {
		try{
			txManager.rollback(status);
		}finally{
			if(logger.isDebugEnabled()){
				logger.debug("Rollback the redis keys which were set in this transaction when the transaction rollback executed");
			}
			rollbackRedis(RedisCachedKeyHolder.getRedisCacheKeys());
			if(logger.isDebugEnabled()){
				logger.debug("Rollback the redis keys which were set in this transaction when the transaction rollback executed - done");
			}
		}
	}
	
	public TransactionStatus getTransaction(TransactionDefinition definition)
			throws TransactionException {		
		return txManager.getTransaction(definition);
	}	
	
	private void rollbackRedis(Set<String> keys){
		if(keys==null || keys.isEmpty()) return;
		
		ShardedJedis jedis = null;
    	ShardedJedisPool pool = redisCacheEngine.getPool();
    	try{
    		jedis = pool.getResource();    	
    		for(String key : keys){    	
    			if(logger.isDebugEnabled()){
    				logger.debug("remove the cached key "+key +" from redis");
    			}
    			jedis.del(key);
    		}
    	}finally{
    		if(jedis!=null)
    			pool.returnResource(jedis);
    	}        
	}
	
	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}
	
	@Autowired
	public void setRedisCacheEngine(RedisCacheEngine redisCacheEngine) {
		this.redisCacheEngine = redisCacheEngine;
	}
	
}
