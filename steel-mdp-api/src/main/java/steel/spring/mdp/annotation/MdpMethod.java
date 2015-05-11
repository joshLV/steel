package steel.spring.mdp.annotation;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.jms.DeliveryMode;
import javax.jms.Message;

@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface MdpMethod {
	/**
	 * 缺省超时时间（毫秒）
	 */
	public static final long DEFAULT_TIMEOUT = 60*1000;

	/**
	 * 异步方法标志
	 * @return
	 */
	boolean asynchronous() default false;

	/**
	 * 调用超时时间
	 * @return
	 */
	long timeout() default DEFAULT_TIMEOUT;

	/**
	 * 调用优先级
	 * @return
	 */
	int priority() default Message.DEFAULT_PRIORITY;

	/**
	 * 交付模式
	 * @return
	 */
	int deliveryMode() default DeliveryMode.NON_PERSISTENT;

	/**
	 * 事务标志
	 * @return
	 */
	boolean transacted() default false;
}
