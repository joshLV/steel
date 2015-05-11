package steel.spring.mdp.annotation;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface Mdpwired {
	/**
	 * 访问的队列名称
	 * @return
	 */
	public String queueName() default "";
}
