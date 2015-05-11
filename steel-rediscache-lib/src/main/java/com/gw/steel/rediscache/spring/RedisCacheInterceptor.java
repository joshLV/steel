package com.gw.steel.rediscache.spring;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.alibaba.fastjson.JSON;
import com.gw.steel.helper.RedisCachedKeyHolder;
import com.gw.steel.helper.RedisKeyUtil;
import com.gw.steel.spring.rediscache.RedisCacheEngine;
import com.gw.steel.spring.rediscache.annotation.CacheGet;
import com.gw.steel.spring.rediscache.annotation.CachePut;
import com.gw.steel.spring.rediscache.annotation.CacheRemove;
import com.gw.steel.spring.rediscache.annotation.Cacheable;
import com.gw.steel.spring.rediscache.annotation.Keyable;
import com.gw.steel.spring.rediscache.annotation.RedisCacheService;
/**
 * Redis Cache Method Interceptor
 * @author admin
 *
 */
public class RedisCacheInterceptor implements  MethodInterceptor, InitializingBean, ApplicationContextAware {
	private Log logger = LogFactory.getLog(RedisCacheInterceptor.class);
	
	private ApplicationContext applicationContext;
	
	private RedisCacheEngine redisCacheEngine;
	
	private RedisCacheService redisCacheService;
	
	private Map<Method, RedisCacheAnnoMethodDetail<Cacheable>> cachePutAnnotMap;
	
	private Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheGetAnnotMap;
	
	private Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheRemoveAnnotMap;
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(logger.isDebugEnabled()){
			logger.debug("Proxy the Method invoke "+invocation);
		}
		boolean hitInCache = true;
		String cacheGetKey ="";
		//Handel CacheGet
		Method m = invocation.getMethod();
		if(m.isAnnotationPresent(CacheGet.class)){
			try{
				cacheGetKey =resolveCacheGetKey(invocation);
				invocation.getMethod().getParameterTypes();
				Class<?> returnType = invocation.getMethod().getReturnType();
			    
				Object cacheItem = retrieveObject(cacheGetKey, returnType, invocation.getMethod().getGenericReturnType());
				
				if(cacheItem!=null){
					if(logger.isDebugEnabled()){
						logger.debug("Load the object from cache : "+cacheItem);
					}					
					return cacheItem;
				}else{
					hitInCache = false;
				}
			}catch(Exception ex){
				logger.warn("Load the item from cache error", ex);
			}
		}
		
		Object rtn = invocation.proceed();
		
		if(!hitInCache){
			RedisCacheAnnoMethodDetail<Keyable> md = this.cacheGetAnnotMap.get(invocation.getMethod());
			CacheGet getAnno = (CacheGet)md.getMethodAnno();
			if(getAnno.cacheWhenNull()&&rtn!=null)//返回值非空， 并且标记为cache当cache没有命中时。
				putObject(cacheGetKey, rtn, getAnno.expire());
		}
		
		try{
			cacheRemove(invocation);
			cachePut(invocation);
		}catch(Exception ex){
			logger.warn("put/remove object error: ", ex);
		}
		
		return rtn;
	}
	
	private String resolveCacheGetKey(MethodInvocation invocation) {
		Method m = invocation.getMethod();
		if(!m.isAnnotationPresent(CacheGet.class)){
			return null;
		}		
		Class<?> rtnType = m.getReturnType();
		if(rtnType==null){
			logger.warn("The return of the " + m.getName()+" method is void, ignore ... ");
			return null;
		}
		Object[] args = invocation.getArguments();		
		
		RedisCacheAnnoMethodDetail<Keyable> md = this.cacheGetAnnotMap.get(m);
		CacheGet getAnno = (CacheGet)md.getMethodAnno();
		Keyable keyable =null ;
		String key = "";
		if(md.getParaAnnoMap()!=null&&!md.getParaAnnoMap().isEmpty()){//参数有Cache Annotation
			Set<Integer> paIdxs = md.getParaAnnoMap().keySet();
			for(Integer idx : paIdxs){//解析参数Annotation
				keyable=(Keyable)md.getParaAnnoMap().get(idx);
				String[] idPropertyNames = keyable.idPropertyNames();
				if(idPropertyNames==null || idPropertyNames.length<1){//未指定属性名， 去参数本身值
					key = keyAppend(key, args[idx].toString());
					continue;
				}
				for(String propertyName : idPropertyNames){
					try {
						key = keyAppend(key, BeanUtils.getProperty(args[idx], propertyName));
					} catch (Exception e) {					
						logger.warn("get key by "+propertyName+" error ",e);
					}
				}
			}
			
		}else{//参数不带Cache Annotation, 取方法体。
			key=getAnno.idStr();
		}
		String appKey = StringUtils.isBlank(getAnno.appKey())? redisCacheService.appKey() :getAnno.appKey();
		String sectionKey = StringUtils.isBlank(getAnno.sectionKey())? redisCacheService.sectionKey() :getAnno.sectionKey();
		
			
		
		String cacheKey = RedisKeyUtil.getKey(appKey, sectionKey, key);
		return cacheKey;
	}
	
	private void cacheRemove(MethodInvocation invocation) {
		Method m = invocation.getMethod();
		if(!m.isAnnotationPresent(CacheRemove.class)){
			return ;
		}		
		Class<?> rtnType = m.getReturnType();
		if(rtnType==null){
			logger.warn("The return of the " + m.getName()+" method is void, ignore ... ");
			return ;
		}
		Object[] args = invocation.getArguments();
		
		RedisCacheAnnoMethodDetail<Keyable> md = this.cacheRemoveAnnotMap.get(m);
		CacheRemove getAnno = (CacheRemove)md.getMethodAnno();
		Keyable parAnno =null ;
		String key = "";
		if(md.getParaAnnoMap()!=null&&!md.getParaAnnoMap().isEmpty()){//
			Set<Integer> paIdxs = md.getParaAnnoMap().keySet();
			for(Integer idx : paIdxs){
				parAnno=(Keyable)md.getParaAnnoMap().get(idx);
				String[] idPropertyNames = parAnno.idPropertyNames();
				if(idPropertyNames==null||idPropertyNames.length<1){
					key=keyAppend(key, args[idx].toString());					
					continue;
				}
				for(String propertyName : idPropertyNames){
					try {
						key=keyAppend(key,  BeanUtils.getProperty(args[idx], propertyName));	
					} catch (Exception e) {					
						logger.warn("get key by "+propertyName+" error ",e);
					}
				}
			}
			
		}else{
			key=getAnno.idStr();
		}
		String appKey = StringUtils.isBlank(getAnno.appKey())? redisCacheService.appKey() :getAnno.appKey();
		String sectionKey = StringUtils.isBlank(getAnno.sectionKey())? redisCacheService.sectionKey() :getAnno.sectionKey();
		
		
		String cacheKey = RedisKeyUtil.getKey(appKey, sectionKey, key);
		removeObject(cacheKey);
		
	}
	
	private void cachePut(MethodInvocation invocation) {
		Method m = invocation.getMethod();
		if(!m.isAnnotationPresent(CachePut.class)){
			return ;
		}		
		Object[] args = invocation.getArguments();
		if(args==null || args.length==0){
			logger.warn("The Args of " + m.getName()+" is less than one, ignore ... ");
			return;
		}		
		RedisCacheAnnoMethodDetail<Cacheable> md = this.cachePutAnnotMap.get(m);
		CachePut putAnno = m.getAnnotation(CachePut.class);
		String appKey = StringUtils.isBlank(putAnno.appKey())? redisCacheService.appKey() :putAnno.appKey();
		String sectionKey = StringUtils.isBlank(putAnno.sectionKey())? redisCacheService.sectionKey() :putAnno.sectionKey();
		
		int expire = putAnno.expire();
		Map<Integer, Cacheable> paraAnnMap = md.getParaAnnoMap();
		
		for(Integer idx: paraAnnMap.keySet()){
			String key = "";
			Cacheable cacheable = paraAnnMap.get(idx);
			String[] idPropertyNames = cacheable.idPropertyNames();
			
			if(idPropertyNames !=null && idPropertyNames.length>0){
				for(String idPropertyName : idPropertyNames){
					try {
						key = keyAppend(key, BeanUtils.getProperty(args[idx], idPropertyName));						
					} catch (Exception e) {
						logger.warn("get key by "+idPropertyName+" error ",e);
					}
				}
			}else{
				key=cacheable.idStr();
			}
			if(StringUtils.isNotEmpty(key)){
				putObject(RedisKeyUtil.getKey(appKey, sectionKey, key), args[idx], expire);
			}else{
				logger.warn("key was empty, ignore to put object into cache :" + args[idx]);
			}
		}	
		
	}
	
	public void afterPropertiesSet() throws Exception {
		if(redisCacheEngine==null)
			redisCacheEngine = applicationContext.getBean("com.gw.steel.spring.rediscache.RedisCacheEngine", RedisCacheEngine.class);
		
		if (this.applicationContext == null ) {
			throw new IllegalStateException("Setting the property 'applicationContext' is required");
		}
		if (this.redisCacheEngine == null) {
			throw new IllegalStateException(" 'redisCacheEngine'  is required: " );
		}
		if(this.redisCacheService == null){
			throw new IllegalStateException(" 'redisCacheService'  is required: " );
		}
		
	}
	
	 private void putObject(String keyString, Object value, int expire) {	
    	ShardedJedis jedis = null;
    	ShardedJedisPool pool = redisCacheEngine.getPool();
    	String objSer = JSON.toJSONString(value);
    	try{
    		jedis = pool.getResource();
    		jedis.set(keyString, objSer);
    		if(expire >0){
    			jedis.expire(keyString, expire*60);
    		}
    		// add the  key to thread local
    		RedisCachedKeyHolder.addRedisCacheKeys(keyString);
    	}finally{
    		if(jedis!=null)
    			pool.returnResource(jedis);
    	}
    }
	
	 private Object retrieveObject(final String keyString, Class<?> returnClass, Type parameterizedType) {    	      
        ShardedJedis jedis = null;
        String value;
        ShardedJedisPool pool = redisCacheEngine.getPool();       
		try {
			jedis = pool.getResource();
			value = jedis.get(keyString); 
		} finally {
			if (jedis != null) {
				pool.returnResource(jedis);
			}
		}			
		if (Collection.class.isAssignableFrom(returnClass)) {
			if(parameterizedType instanceof ParameterizedType) {
				Class<?> elementClass = (Class<?>) ((ParameterizedType) parameterizedType).getActualTypeArguments()[0];
				return JSON.parseArray(value, elementClass);
			}
		}
		
        return JSON.parseObject(value, returnClass);
    }
	 
	 private String keyAppend(String key, String append){
		 if(StringUtils.isNotBlank(key))
			key += "&"+ append;
		else
			key += append;
		 
		 return key;
	 }
	 
	 public void removeObject(String key) { 
        if (logger.isDebugEnabled()) {
        	logger.debug("Removing object '"  + key  + "'");
        }        
        ShardedJedis jedis = null;
    	ShardedJedisPool pool = redisCacheEngine.getPool();
    	try{
    		jedis = pool.getResource();
    		jedis.del(key);
    	}finally{
    		if(jedis!=null)
    			pool.returnResource(jedis);
    	}        
    }
	 
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;		
	}
	
	public void setRedisCacheEngine(RedisCacheEngine redisCacheEngine) {
		this.redisCacheEngine = redisCacheEngine;
	}
	
	public void setRedisCacheService(RedisCacheService redisCacheService) {
		this.redisCacheService = redisCacheService;
	}
	/**
	 * @param cachePutAnnotMap the cachePutAnnotMap to set
	 */
	public void setCachePutAnnotMap(
			Map<Method, RedisCacheAnnoMethodDetail<Cacheable>> cachePutAnnotMap) {
		this.cachePutAnnotMap = cachePutAnnotMap;
	}
	/**
	 * @param cacheGutAnnotMap the cacheGutAnnotMap to set
	 */
	public void setCacheGetAnnotMap(
			Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheGetAnnotMap) {
		this.cacheGetAnnotMap = cacheGetAnnotMap;
	}
	/**
	 * @param cacheRemoveAnnotMap the cacheRemoveAnnotMap to set
	 */
	public void setCacheRemoveAnnotMap(
			Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheRemoveAnnotMap) {
		this.cacheRemoveAnnotMap = cacheRemoveAnnotMap;
	}
	
	
}
