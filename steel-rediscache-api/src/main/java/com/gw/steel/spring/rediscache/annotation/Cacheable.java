package com.gw.steel.spring.rediscache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
public @interface Cacheable {
	
	//String idPropertyName() default "";
	/**
	 * ID字符串
	 */
	String idStr() default "";
	/**
	 * ID对应Object的属性值列表, 优于IdStr
	 * @return
	 */
	String[] idPropertyNames() default {};
}
