package com.gw.steel.helper;

import java.util.HashSet;
import java.util.Set;

public class RedisCachedKeyHolder {
	private static final  ThreadLocal<Set<String>> redisCachedKeysHolder = new ThreadLocal<Set<String>>();
	
	public static Set<String> getRedisCacheKeys() {
		return redisCachedKeysHolder.get();
	}
	
	public static void setRedisCacheKeys(Set<String> keys) {
		redisCachedKeysHolder.set(keys);
	}
	
	public static void addRedisCacheKeys(String key) {
		Set<String> keys = redisCachedKeysHolder.get();
		if(keys==null ||keys.isEmpty()){
			keys = new HashSet<String>();
		}
		keys.add(key);
		setRedisCacheKeys(keys);
	}
}
