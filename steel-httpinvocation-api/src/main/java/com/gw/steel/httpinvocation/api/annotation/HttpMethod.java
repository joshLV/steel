package com.gw.steel.httpinvocation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @project steel-httpinvocation-api
 * @author Dongpo.wu
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethod {
	/**
	 * 缺省超时时间（毫秒）
	 */
	public static final long DEFAULT_TIMEOUT = 60*1000;
	
	/**
	 * 调用超时时间
	 * @return
	 */
	long timeout() default DEFAULT_TIMEOUT;	
}
