package steel.spring.mdp.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @project: Steel
 * @description: 消息属性映射Anno
 * 
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RUNTIME)
public @interface MessageProperties {
	MessageProperty[] value();
}
