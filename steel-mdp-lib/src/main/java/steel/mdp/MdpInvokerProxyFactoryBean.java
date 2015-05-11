package steel.mdp;

import javax.jms.JMSException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.ClassUtils;

import steel.spring.mdp.annotation.MdpService;

/**
 * @project: Steel
 * @description: Mdp呼叫代理工厂Bean类

 */
public class MdpInvokerProxyFactoryBean implements FactoryBean, MethodInterceptor, InitializingBean, BeanClassLoaderAware {
	/**
	 * Mdp请求器
	 */
	private MdpRequestor mdpRequestor;
	
	/**
	 * 队列
	 */
	private Object queue;

	/**
	 * 远过程调用工厂
	 */
	private RemoteInvocationFactory remoteInvocationFactory = new DefaultRemoteInvocationFactory();

	/**
	 * 消息转换器
	 */
	private MessageConverter messageConverter = new SimpleMessageConverter();

	/**
	 * 服务接口类
	 */
	private Class<Object> serviceInterface;
	
	/**
	 * 服务名字
	 */
	private String serviceName;

	/**
	 * Bean类加载器
	 */
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/**
	 * 服务代理对象
	 */
	private Object serviceProxy;
	
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			return "JMS invoker proxy for queue [" + this.queue + "]";
		}

		RemoteInvocation invocation = createRemoteInvocation(methodInvocation);
		RemoteInvocationContext ric = new RemoteInvocationContext();
		ric.setProxy(this);
		ric.setMethodInvocation(methodInvocation);
		ric.setRemoteInvocation(invocation);
		
		RemoteInvocationResult result = null;
		try {
			result = mdpRequestor.executeRequest(ric);
		} catch (JMSException ex) {
			throw convertJmsInvokerAccessException(ex);
		}

		if ( result == null ) {
			// asynchornous call
			return null;
		}
		
		try {
			return recreateRemoteInvocationResult(result);
		} catch (Throwable ex) {
			if (result.hasInvocationTargetException()) {
				throw ex;
			} else {
				throw new RemoteInvocationFailureException("Invocation of method [" + methodInvocation.getMethod()
						+ "] failed in JMS invoker remote service at queue [" + this.queue + "]", ex);
			}
		}
	}

	/**
	 * Recreate the invocation result contained in the given RemoteInvocationResult
	 * object. The default implementation calls the default recreate method.
	 * <p>Can be overridden in subclass to provide custom recreation, potentially
	 * processing the returned result object.
	 * @param result the RemoteInvocationResult to recreate
	 * @return a return value if the invocation result is a successful return
	 * @throws Throwable if the invocation result is an exception
	 * @see org.springframework.remoting.support.RemoteInvocationResult#recreate()
	 */
	protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
		return result.recreate();
	}
	
	/**
	 * Convert the given JMS invoker access exception to an appropriate
	 * Spring RemoteAccessException.
	 * @param ex the exception to convert
	 * @return the RemoteAccessException to throw
	 */
	protected RemoteAccessException convertJmsInvokerAccessException(JMSException ex) {
		throw new RemoteAccessException("Could not access JMS invoker queue [" + this.queue + "]", ex);
	}
	
	/**
	 * Create a new RemoteInvocation object for the given AOP method invocation.
	 * The default implementation delegates to the RemoteInvocationFactory.
	 * <p>Can be overridden in subclasses to provide custom RemoteInvocation
	 * subclasses, containing additional invocation parameters like user credentials.
	 * Note that it is preferable to use a custom RemoteInvocationFactory which
	 * is a reusable strategy.
	 * @param methodInvocation the current AOP method invocation
	 * @return the RemoteInvocation object
	 * @see RemoteInvocationFactory#createRemoteInvocation
	 */
	protected RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
		return this.remoteInvocationFactory.createRemoteInvocation(methodInvocation);
	}

	public void afterPropertiesSet() throws Exception {
		if (getMdpRequestor() == null) {
			throw new IllegalArgumentException("Property 'mdpRequestor' is required");
		}

		if (this.queue == null) {
			throw new IllegalArgumentException("'queue' or 'queueName' is required");
		}

		if (this.serviceInterface == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}
		
		if (serviceInterface.isAnnotationPresent(MdpService.class) == false) {
			throw new IllegalArgumentException("serviceInterface isn't annotation MdpService present.");
		}

		MdpService mdpServiceAnno = serviceInterface.getAnnotation(MdpService.class);
		if (StringUtils.isNotBlank(mdpServiceAnno.messageConverter())) {
			messageConverter = (MessageConverter) Class.forName(mdpServiceAnno.messageConverter()).newInstance();
		}

		if (StringUtils.isNotBlank(mdpServiceAnno.remoteInvocationFactory())) {
			remoteInvocationFactory = (RemoteInvocationFactory) Class.forName(mdpServiceAnno.remoteInvocationFactory())
					.newInstance();
		}
		
		this.serviceProxy = new ProxyFactory(this.serviceInterface, this).getProxy(this.beanClassLoader);
	}

	public Object getQueue() {
		return queue;
	}

	public void setQueue(Object queue) {
		this.queue = queue;
	}

	public MdpRequestor getMdpRequestor() {
		return mdpRequestor;
	}

	public void setMdpRequestor(MdpRequestor mdpRequestor) {
		this.mdpRequestor = mdpRequestor;
	}

	public MessageConverter getMessageConverter() {
		return messageConverter;
	}

	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public RemoteInvocationFactory getRemoteInvocationFactory() {
		return remoteInvocationFactory;
	}

	public void setRemoteInvocationFactory(RemoteInvocationFactory remoteInvocationFactory) {
		this.remoteInvocationFactory = remoteInvocationFactory;
	}

	public Class<Object> getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class<Object> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return this.serviceInterface;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
