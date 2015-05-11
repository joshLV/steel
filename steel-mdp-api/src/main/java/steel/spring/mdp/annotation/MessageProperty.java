package steel.spring.mdp.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RUNTIME)
public @interface MessageProperty {
	String jxpath() default "";
	
	String name();
}
