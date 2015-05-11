package com.gw.steel.httpinvocation.api.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @project steel-httpinvoaction-api
 * @author Dongpo.wu
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface HttpService {
	/**
	 * 服务接口
	 * @return
	 */
	Class<?> serviceInterfaceClass() default UnknownClass.class;
	
	/**
	 * 服务名称
	 * @return
	 */
	String serviceName() default "";	
}
