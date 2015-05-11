package steel.spring.mdp.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface MdpService {
	/**
	 * 服务接口
	 * @return
	 */
	String serviceInterface() default "";

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

	/**
	 * 绑定队列名称列表
	 * @return
	 */
	String[] queueNames() default "";

	/**
	 * 消息转换器类名称
	 * @return
	 */
	String messageConverter() default "";

	/**
	 * 注册跟踪拦截器标志
	 * @return
	 */
	boolean registerTraceInterceptor() default true;

	/**
	 * 远程调用执行器类名称
	 * @return
	 */
	String remoteInvocationExecutor() default "";

	/**
	 * 远程调用工厂类名称
	 * @return
	 */
	String remoteInvocationFactory() default "";
}
