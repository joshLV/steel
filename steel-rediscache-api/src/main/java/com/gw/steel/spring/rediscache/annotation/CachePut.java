package com.gw.steel.spring.rediscache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author admin
 *
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface CachePut {	
	/**
	 * 空时, 从Service继承
	 * @return
	 */
	String  appKey() default "";
	/**
	 * 空时, 从Service继承
	 * @return
	 */
	String  sectionKey() default "";
	/**
	 * 缓存对象过期时间, 为0 时从Service继承 , 单位分钟
	 * @return
	 */
	int expire() default 0;	
}
