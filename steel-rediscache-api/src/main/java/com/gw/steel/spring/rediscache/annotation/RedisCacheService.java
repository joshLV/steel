package com.gw.steel.spring.rediscache.annotation;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author Dongpo.wu
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface RedisCacheService {
	/**
	 * 服务接口
	 * @return
	 */
	Class<?> serviceInterfaceClass() default UnknownClass.class;
	
	String  appKey();
	
	String  sectionKey();
	/**
	 * 缓存对象过期时间, 单位分钟
	 * @return
	 */
	int expire() default 0;
}
