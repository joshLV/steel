package steel.mdp;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.remoting.support.RemoteInvocationExecutor;

import steel.spring.mdp.annotation.MdpService;
import steel.spring.mdp.annotation.UnknownClass;

/**
 * @project: Steel
 * @description: MessageDrivenPojo服务总线导出器实现类
 *
 */
public class MdpServiceBusExporter implements SessionAwareMessageListener<Message>, BeanClassLoaderAware, InitializingBean {
    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * 服务Beans集
     */
    private Set<Object> services = new HashSet<Object>();

    /**
     * 服务入口映射表（按照接口名称）
     */
    private Map<String, MdpInvokerServiceExporter> serviceExportersByInterface = new HashMap<String, MdpInvokerServiceExporter>();

    /**
     * 服务入口映射表（按照服务名称）
     */
    private Map<String, MdpInvokerServiceExporter> serviceExportersByName = new HashMap<String, MdpInvokerServiceExporter>();

    /**
     * 忽略无效请求标志
     */
    private boolean ignoreInvalidRequests = true;

    /**
     * Bean类装载器
     */
    private ClassLoader beanClassLoader = null;

    /**
     * 忽略序列化检查的类名称集
     */
    private static Set<String> ignoreSerializableCheckClassNames = new HashSet<String>();

    private boolean ignoreTimeout = false;

	static {
		ignoreSerializableCheckClassNames.add("void");
		ignoreSerializableCheckClassNames.add("java.util.List");
		ignoreSerializableCheckClassNames.add("java.util.Map");
		ignoreSerializableCheckClassNames.add("java.util.Set");
		ignoreSerializableCheckClassNames.add("java.util.Collection");
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	public Object getUserTarget(Object bean) throws Exception {
		if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
			Advised advised = (Advised) bean;
			bean = advised.getTargetSource().getTarget();
			return getUserTarget(bean);
		}
		return bean;
	}

	@SuppressWarnings("unchecked")
	public void registerMdpService(Object service) throws Exception {
		Object srv = getUserTarget(service);
		Class<Object> clazz = (Class<Object>) srv.getClass();
		if (clazz.isAnnotationPresent(MdpService.class) == false) {
			logger.warn("The service isn't a mdpService, className=[" + clazz.getName() + "].");
			throw new RuntimeException("Not found the serviceInterface, className=[" + clazz.getName() + "].");
		}

		MdpService mdpServiceAnno = clazz.getAnnotation(MdpService.class);
		Class<?> interfaceClazz = null;
		String serviceName = null;

		if (StringUtils.isBlank((mdpServiceAnno.serviceInterface()))) {
			if (mdpServiceAnno.serviceInterfaceClass().equals(UnknownClass.class)) {
				logger.error("Not found the serviceInterface, className=[" + clazz.getName() + "].");
				throw new RuntimeException("Not found the serviceInterface, className=[" + clazz.getName() + "].");
			} else {
				interfaceClazz = mdpServiceAnno.serviceInterfaceClass();
			}
		} else {
			interfaceClazz = Class.forName(mdpServiceAnno.serviceInterface());
		}

		if (StringUtils.isNotBlank(mdpServiceAnno.serviceName())) {
			serviceName = mdpServiceAnno.serviceName();
		}

		if (interfaceClazz.isAnnotationPresent(MdpService.class) == false) {
			logger.error("The serviceInterface isn't a mdpService, className=[" + interfaceClazz.getName() + "].");
			throw new RuntimeException("The serviceInterface isn't a mdpService, className=["
					+ interfaceClazz.getName() + "].");
		}

		Method[] methods = interfaceClazz.getDeclaredMethods();

		for (Method m : methods) {
			if (m.getReturnType() != null) {
				if (m.getReturnType().isPrimitive() == false
						&& ignoreSerializableCheckClassNames.contains(m.getReturnType().getName()) == false) {
					if (Serializable.class.isAssignableFrom(m.getReturnType()) == false) {
						throw new RuntimeException("The return type isn't serializable, class=["
								+ interfaceClazz.getName() + "], method=[" + m.getName() + "], return=["
								+ m.getReturnType().getName() + "].");
					}
				}
			}

			for (Class<?> type : m.getParameterTypes()) {
				if (type.isPrimitive() == false && ignoreSerializableCheckClassNames.contains(type.getName()) == false) {
					if (Serializable.class.isAssignableFrom(type) == false) {
						throw new RuntimeException("The para type isn't serializable, class=["
								+ interfaceClazz.getName() + "], para=[" + type.getName() + "].");
					}
				}
			}
		}

		mdpServiceAnno = interfaceClazz.getAnnotation(MdpService.class);

		if (serviceName == null) {
			if (StringUtils.isNotBlank(mdpServiceAnno.serviceName())) {
				serviceName = mdpServiceAnno.serviceName();
			}
		}

		MessageConverter mc = null;
		if (StringUtils.isNotBlank(mdpServiceAnno.messageConverter()) ) {
			mc = (MessageConverter) Class.forName(mdpServiceAnno.messageConverter()).newInstance();
		}

		RemoteInvocationExecutor riExe = null;
		if (StringUtils.isNotBlank(mdpServiceAnno.remoteInvocationExecutor())) {
			riExe = (RemoteInvocationExecutor) Class.forName(mdpServiceAnno.remoteInvocationExecutor()).newInstance();
		}

		MdpInvokerServiceExporter exporter = new MdpInvokerServiceExporter();
		if (beanClassLoader != null) {
			exporter.setBeanClassLoader(beanClassLoader);
		}

		if (mc != null) {
			exporter.setMessageConverter(mc);
		}

		if (riExe != null) {
			exporter.setRemoteInvocationExecutor(riExe);
		}

		exporter.setRegisterTraceInterceptor(mdpServiceAnno.registerTraceInterceptor());
		exporter.setService(service);
		exporter.setServiceInterface(interfaceClazz);
		exporter.setIgnoreInvalidRequests(ignoreInvalidRequests);
		exporter.setIgnoreTimeout(ignoreTimeout);

		exporter.afterPropertiesSet();

		serviceExportersByInterface.put(interfaceClazz.getName(), exporter);

		if (serviceName != null) {
			serviceExportersByName.put(serviceName, exporter);
		}

		logger.info("Exposing " + srv.getClass().getName() + " as MdpService[" + interfaceClazz.getName() + "].");
	}
	
	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		if (services == null) {
			return;
		}

		for (Object service : services) {
			registerMdpService(service);
		}
	}

	protected void onInvalidRequest(Message requestMessage) throws JMSException {
		if (this.ignoreInvalidRequests) {
			if (logger.isWarnEnabled()) {
				logger.warn("Invalid request message will be discarded: " + requestMessage);
			}
		} else {
			throw new MessageFormatException("Invalid request message: " + requestMessage);
		}
	}

	protected MdpInvokerServiceExporter getServiceExporterByInterface(String interfaceName) {
		return serviceExportersByInterface.get(interfaceName);
	}

	protected MdpInvokerServiceExporter getServiceExporterByName(String srvName) {
		return serviceExportersByName.get(srvName);
	}

	public void onMessage(Message requestMessage, Session session) throws JMSException {
		try {
			if (requestMessage.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT) {
				if (requestMessage.getJMSRedelivered()) {
					logger.warn("Ignore redelivered non persistent message.");
					return;
				}
			}

			MdpInvokerServiceExporter srvExporter = null;
			String serviceName = requestMessage.getStringProperty(MessageReservePropertyNames.MSB_MDP_SERVICE_NAME);
			String interfaceName = requestMessage
					.getStringProperty(MessageReservePropertyNames.MSB_MDP_SERVICE_INTERFACE);

			if (serviceName != null) {
				srvExporter = getServiceExporterByName(serviceName);
			}

			if (srvExporter == null) {
				if (interfaceName != null) {
					srvExporter = getServiceExporterByInterface(interfaceName);
				}
			}

			if (srvExporter == null) {
				onInvalidRequest(requestMessage);
				return;
			}

			srvExporter.onMessage(requestMessage, session);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public Set<Object> getServices() {
		return services;
	}

	public void setServices(Set<Object> serviceBeans) {
		this.services = serviceBeans;
	}

	public boolean isIgnoreInvalidRequests() {
		return ignoreInvalidRequests;
	}

	public void setIgnoreInvalidRequests(boolean ignoreInvalidRequests) {
		this.ignoreInvalidRequests = ignoreInvalidRequests;
	}

	public boolean isIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(boolean ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}

	public Map<String, MdpInvokerServiceExporter> getServiceExportersByInterface() {
		return serviceExportersByInterface;
	}

}
