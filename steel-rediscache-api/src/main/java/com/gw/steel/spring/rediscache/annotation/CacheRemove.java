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
public @interface CacheRemove {	
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
	 * 指定id值
	 */
	String idStr() default "";
}
