package com.gw.steel.spring.rediscache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * 从Cache中获取对象:
 * 	1. 如果Cache返回为空, 则在主程序返回对象后, cache对象.
 *  2. 如果Cache有对象返回,直接返回cache对象.
 * @author Dongpo.wu
 *
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface CacheGet {
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
	/**
	 * 从cache未获取到对象, 在主程序返回后, 是否cache 对象
	 * @return
	 */
	boolean cacheWhenNull() default true;
	
	/**
	 * cache 对象存活时间, 单位分钟
	 * @return
	 */
	int expire() default 30;
}
