package com.gw.steel.impl;



import com.gw.steel.IHelloWorldCacheBean;
import com.gw.steel.User;
import com.gw.steel.spring.rediscache.annotation.CacheGet;
import com.gw.steel.spring.rediscache.annotation.CachePut;
import com.gw.steel.spring.rediscache.annotation.CacheRemove;
import com.gw.steel.spring.rediscache.annotation.Cacheable;
import com.gw.steel.spring.rediscache.annotation.Keyable;
import com.gw.steel.spring.rediscache.annotation.RedisCacheService;

@RedisCacheService(appKey="Redis-Cache-TestAPP", sectionKey="itatis")
public class HelloWorldCacheBean implements IHelloWorldCacheBean {
	/* (non-Javadoc)
	 * @see com.gw.steel.impl.IHelloWorldCacheBean#helloWorld()
	 */
	@CacheGet(idStr="Hello-World")
	public String getHelloWorld(String key){
		return "hello World";
	}
	
	@CachePut
	public void putHelloWorld(@Cacheable(idStr="Hello-World") String key){
		
	}
	@CacheRemove(idStr="Hello-World")
	public void removeHelloWorld(String key) {		
		
	}
	
	@CacheGet()
	public User getUser(@Keyable(idPropertyNames={"id",}) User u) {
		
		return new User("in-class-1");
	}
	@CachePut(expire=30)
	public void putUser(@Cacheable(idPropertyNames={"id"}) User u) {
		
	}
	@CacheRemove
	public void removeUser(@Keyable(idPropertyNames={"id"})User u) {
				
	}
}
