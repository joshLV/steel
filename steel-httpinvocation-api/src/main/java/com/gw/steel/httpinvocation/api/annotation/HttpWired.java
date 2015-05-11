package com.gw.steel.httpinvocation.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @project Steel-httpinvocation-api
 * @author Dongpo.wu
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpWired {
	/**
	 * 服务名称, 如果为空， 取接口上面的ServiceName
	 */
	String serviceName() default "";
	
	String serviceUrl() default "";
}
