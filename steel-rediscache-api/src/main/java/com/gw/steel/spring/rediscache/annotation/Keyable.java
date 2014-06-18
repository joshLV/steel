package com.gw.steel.spring.rediscache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
public @interface Keyable {
	/**
	 * 生成Key对应的属性值列表, 对应annotation对象中的属性名称
	 * @return
	 */
	String[] idPropertyNames() default {};
}
