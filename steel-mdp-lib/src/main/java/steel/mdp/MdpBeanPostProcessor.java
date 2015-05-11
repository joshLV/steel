package steel.mdp;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PreDestroy;
import javax.jms.ConnectionFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

import steel.mdp.status.Incoming;
import steel.mdp.status.MdpHostStatus;
import steel.spring.mdp.annotation.MdpService;
import steel.spring.mdp.annotation.Mdpwired;
import steel.spring.mdp.annotation.UnknownClass;

/**
 * @project: Steel
 * @description: MdpBean Spring容器事件处理器实现类
 */
public class MdpBeanPostProcessor implements BeanPostProcessor, SmartLifecycle, PriorityOrdered, ApplicationContextAware,
        MdpStatusCollector {
    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * 缺省Jms连接工厂
     */
    private ConnectionFactory connectionFactory;

    /**
     * Jms连接工厂映射表（队列名字->队列配置）
     */
    private Map<String, QueueConfig> queueConfigs = new TreeMap<String, QueueConfig>();

    /**
     * Mdp请求器映射表（连接工厂hashcode->Mdp请求器）
     */
    private Map<Integer, MdpRequestor> mdpRequestors = new TreeMap<Integer, MdpRequestor>();

    /**
     * 队列名称后缀
     */
    private String queueNameSuffix;

    /**
     * 缺省并发消费者数量
     */
    private int concurrentConsumers = MdpConfig.DEFAULT_CONCURRENT_CONSUMERS;

    /**
     * mdp服务总线导出器
     */
    private Map<String, MdpServiceBusExporter> mdpServiceBusExporters = new TreeMap<String, MdpServiceBusExporter>();

    /**
     * Mdp消息监听容器
     */
    private Map<String, SimpleMessageListenerContainer> mdpMessageListenerContainers = new TreeMap<String, SimpleMessageListenerContainer>();

    /**
     * Mdp呼叫代理工厂对象
     */
    private Map<String, MdpInvokerProxyFactoryBean> mdpInvokerProxyFactoryBeans = new TreeMap<String, MdpInvokerProxyFactoryBean>();

    /**
     * 缺省队列
     */
    private String defaultQueue;

    /**
     * 启动顺序号
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 2;

    /**
     * 忽略超时
     */
    private boolean ignoreTimeout;

    /**
     * Bean名称队列
     */
    private List<BeanNameQueue> beanNameQueues = new ArrayList<BeanNameQueue>();

    /**
     * mdp状态报告器
     */
    private Map<ConnectionFactory, MdpStatusReporter> mdpStatusReporters = new HashMap<ConnectionFactory, MdpStatusReporter>();

    protected final Object lifecycleMonitor = new Object();
    
    private boolean running = false;
    
    private ApplicationContext applicationContext;
	
	private class BeanNameQueue {
		private String beanName;

		private String[] queueNames;
	}

	private Object getQueueConfigPara(String queueName, String paraName) {
		QueueConfig qcfg = queueConfigs.get(queueName);
		if (qcfg == null) {
			return null;
		}

		JXPathContext ctx = JXPathContext.newContext(qcfg);
		return ctx.getValue(paraName);
	}

	private String getFullQueueName(String queueName) {
		String aqn = (String) getQueueConfigPara(queueName, "realQueueName");
		if (aqn != null) {
			return aqn.trim();
		}

		if (queueNameSuffix == null) {
			return queueName;
		}

		return queueName + queueNameSuffix;
	}

	private ConnectionFactory getConnectionFactory(String queueName) {
		ConnectionFactory cf = (ConnectionFactory) getQueueConfigPara(queueName, "connectionFactory");
		if (cf != null) {
			return cf;
		}

		return connectionFactory;
	}

	private MdpRequestor getMdpRequestor(String queueName) {
		ConnectionFactory cf = getConnectionFactory(queueName);
		MdpRequestor requestor = mdpRequestors.get(cf.hashCode());
		if (requestor != null) {
			return requestor;
		}

		requestor = new MdpRequestor();
		requestor.setConnectionFactory(cf);
		requestor.setIgnoreTimeout(ignoreTimeout);
		try {
			requestor.afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		mdpRequestors.put(cf.hashCode(), requestor);

		return requestor;
	}

	private MdpServiceBusExporter getMdpServiceBusExporter(String queueName) {
		MdpServiceBusExporter exporter = mdpServiceBusExporters.get(queueName);
		if (exporter != null) {
			return exporter;
		}

		exporter = new MdpServiceBusExporter();
		exporter.setIgnoreTimeout(ignoreTimeout);
		mdpServiceBusExporters.put(queueName, exporter);
		return exporter;
	}

	private int getConcurrentConsumers(String queueName) {
		Integer cc = (Integer) getQueueConfigPara(queueName, "concurrentConsumers");
		if (cc != null) {
			return cc;
		}

		return concurrentConsumers;
	}

	private String getMessageSelector(String queueName) {
		return (String) getQueueConfigPara(queueName, "messageSelector");
	}

	@SuppressWarnings("unchecked")
	private MdpInvokerProxyFactoryBean getMdpInvokerProxyFactoryBean(String queueName, Class<?> mdpBeanClass) {
		StringBuffer key = new StringBuffer(queueName);
		key.append("/");
		key.append(mdpBeanClass.getName());
		String k = key.toString();

		MdpInvokerProxyFactoryBean mdpBean = mdpInvokerProxyFactoryBeans.get(k);
		if (mdpBean != null) {
			return mdpBean;
		}

		MdpRequestor mdpRequestor = getMdpRequestor(queueName);
		mdpBean = new MdpInvokerProxyFactoryBean();
		mdpBean.setMdpRequestor(mdpRequestor);
		mdpBean.setQueue(getFullQueueName(queueName));
		mdpBean.setServiceInterface((Class<Object>) mdpBeanClass);
		try {
			mdpBean.afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		mdpInvokerProxyFactoryBeans.put(k, mdpBean);

		return mdpBean;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	private void wireMdpBean(Object bean, String beanName) throws BeansException {
		Class<?> clazz = bean.getClass();
		BeanInfo bInfo = null;

		try {
			bInfo = java.beans.Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new RuntimeException("Wiring bean=[" + beanName + "] meet error.", e);
		}

		for (PropertyDescriptor pd : bInfo.getPropertyDescriptors()) {
			Method m = pd.getWriteMethod();
			if (m == null) {
				continue;
			}

			if (m.isAnnotationPresent(Mdpwired.class) == false) {
				continue;
			}

			Method rm = pd.getReadMethod();
			if (rm != null) {
				try {
					if (rm.invoke(bean) != null) {
						continue;
					}
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}

			Class<?> mdpBeanClass = pd.getPropertyType();
			if (mdpBeanClass.isAnnotationPresent(MdpService.class) == false) {
				// ����MdpBean
				throw new RuntimeException("Dependency bean isn't MdpService, dbean=[" + beanName + "." + pd.getName()
						+ "].");
			}

			MdpService mdpServiceAnno = mdpBeanClass.getAnnotation(MdpService.class);

			Mdpwired mwiredAnno = m.getAnnotation(Mdpwired.class);
			String queueName = mwiredAnno.queueName();
			if (StringUtils.isBlank(queueName)) {
				for (String q : mdpServiceAnno.queueNames()) {
					
					if (StringUtils.isNotBlank(q)) {
						queueName = q;
						break;
					}
				}
			}

			if (queueName == null) {
				throw new RuntimeException("The queue name required, dbean=[" + beanName + "." + pd.getName() + "].");
			}

			try {
				MdpInvokerProxyFactoryBean mdpBean = getMdpInvokerProxyFactoryBean(queueName, mdpBeanClass);
				m.invoke(bean, mdpBean.getObject());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	private void registerMdpService(Object bean, String beanName) throws BeansException {
		Class<?> clazz = bean.getClass();
		if (clazz.isAnnotationPresent(MdpService.class) == false) {
			return;
		}

		MdpService mdpServiceAnno = clazz.getAnnotation(MdpService.class);
		Class<?> mdpServiceClazz = mdpServiceAnno.serviceInterfaceClass();
		String[] queueNames = mdpServiceAnno.queueNames();
		if (queueNames != null) {
			if (queueNames.length == 0) {
				queueNames = null;
			} else if (queueNames.length == 1) {
				if (StringUtils.isBlank(queueNames[0])) {
					queueNames = null;
				}
			}
		}

		if (mdpServiceClazz.equals(UnknownClass.class)) {
			String si = mdpServiceAnno.serviceInterface();
			if (si.equals("")) {
				return;
			}

			try {
				mdpServiceClazz = Class.forName(si);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		if (mdpServiceClazz.isAnnotationPresent(MdpService.class) == false) {
			throw new RuntimeException("The class=[" + mdpServiceClazz.getName() + "] isn't the MdpService.");
		}

		mdpServiceAnno = mdpServiceClazz.getAnnotation(MdpService.class);
		BeanNameQueue bnq = new BeanNameQueue();
		bnq.beanName = beanName;
		if (queueNames == null) {
			bnq.queueNames = mdpServiceAnno.queueNames();
		} else {
			bnq.queueNames = queueNames;
		}

		beanNameQueues.add(bnq);
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.info("Wiring the bean=[" + beanName + "], bean=[" + bean + "].");
		}

		if (bean instanceof MdpServiceInvoker) {
			MdpServiceInvoker invoker = (MdpServiceInvoker) bean;
			Class<?> mdpBeanClass = null;
			try {
				mdpBeanClass = Class.forName(invoker.getServiceInterface());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}

			if (mdpBeanClass.isAnnotationPresent(MdpService.class) == false) {
				// ����MdpBean
				throw new RuntimeException("Bean isn't MdpService, className=[" + invoker.getServiceInterface() + "].");
			}

			MdpService mdpServiceAnno = mdpBeanClass.getAnnotation(MdpService.class);
			String queueName = invoker.getQueueName();
			if (queueName == null) {
				for (String q : mdpServiceAnno.queueNames()) {
					
					if (StringUtils.isNotBlank(q)) {
						queueName = q;
						break;
					}
				}
			}

			if (queueName == null) {
				throw new RuntimeException("The queue name required, dbean=[" + beanName + "].");
			}

			return getMdpInvokerProxyFactoryBean(queueName, mdpBeanClass);
		}

		wireMdpBean(bean, beanName);
		registerMdpService(bean, beanName);
		return bean;
	}

	@PreDestroy
	public void destroy() {
		for ( Map.Entry<String, SimpleMessageListenerContainer> entry : mdpMessageListenerContainers.entrySet()) {
			entry.getValue().destroy();
		}
	}
	
	public boolean isAutoStartup() {
		return true;
	}

	public void stop(Runnable callback) {
		for ( Map.Entry<String, SimpleMessageListenerContainer> entry : mdpMessageListenerContainers.entrySet()) {
			entry.getValue().stop();
		}
		
		callback.run();
	}

	public boolean isRunning() {
		synchronized(this.lifecycleMonitor) {
			return running;
		}
	}

	public void start() {
		startupMdpContainers(applicationContext);

		for ( Map.Entry<String, SimpleMessageListenerContainer> entry : mdpMessageListenerContainers.entrySet()) {
			entry.getValue().start();
		}
		
		synchronized(this.lifecycleMonitor) {
			running = true;
		}
	}

	public void stop() {
		for ( Map.Entry<String, SimpleMessageListenerContainer> entry : mdpMessageListenerContainers.entrySet()) {
			entry.getValue().stop();
		}
		
		synchronized(this.lifecycleMonitor) {
			running = false;
		}
	}

	public int getPhase() {
		return Integer.MAX_VALUE;
	}
	
	private void startupMdpContainers(ApplicationContext ctx) throws BeansException {
		for (BeanNameQueue bnq : beanNameQueues) {
			Object bean = ctx.getBean(bnq.beanName);

			for (String q : bnq.queueNames) {
				
				if (StringUtils.isBlank(q) && defaultQueue != null) {
					q = defaultQueue;
				}

				if (q != null) {
					MdpServiceBusExporter exporter = getMdpServiceBusExporter(q);
					try {
						exporter.registerMdpService(bean);
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			}
		}

		for (Map.Entry<String, MdpServiceBusExporter> entry : mdpServiceBusExporters.entrySet()) {
			String queueName = entry.getKey();
			MdpServiceBusExporter exporter = entry.getValue();
			SimpleMessageListenerContainer container = new MdpSimpleMessageListenerContainer();// �޸�Ϊ�Զ���ListenerContainer
			container.setConnectionFactory(getConnectionFactory(queueName));
			container.setDestinationName(getFullQueueName(queueName));
			container.setConcurrentConsumers(getConcurrentConsumers(queueName));
			container.setMessageListener(exporter);
			container.setMessageSelector(getMessageSelector(queueName));

			container.afterPropertiesSet();

			mdpMessageListenerContainers.put(queueName, container);

			logger.info("The MdpMessageListenerContainer for queue=[" + getFullQueueName(queueName) + "] started.");

			MdpStatusReporter reporter = mdpStatusReporters.get(container.getConnectionFactory());
			if (reporter == null) {
				reporter = new MdpStatusReporter(container.getConnectionFactory(), this);
				mdpStatusReporters.put(container.getConnectionFactory(), reporter);
			}
		}
	}

	public MdpHostStatus getStatus(ConnectionFactory cf) {
		MdpHostStatus status = new MdpHostStatus();
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			status.setHost(localHost.getHostName());
			status.setIp(localHost.getHostAddress());
			status.setPid(GetProcessIdUtil.getCurrentPid());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		for (Map.Entry<String, MdpServiceBusExporter> entry : mdpServiceBusExporters.entrySet()) {
			String queueName = entry.getKey();
			ConnectionFactory cf2 = getConnectionFactory(queueName);
			if (cf != cf2) {
				continue;
			}

			Incoming incoming = new Incoming();
			incoming.setConcurrentConsumers(getConcurrentConsumers(queueName));
			incoming.setQueue(getFullQueueName(queueName));
			incoming.setMsgSelector(getMessageSelector(queueName));
			MdpServiceBusExporter exporter = entry.getValue();
			Set<String> set = new TreeSet<String>();
			set.addAll(exporter.getServiceExportersByInterface().keySet());
			incoming.setMdpServiceInterfaces(set);

			status.getIncomings().add(incoming);
		}

		return status;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public String getQueueNameSuffix() {
		return queueNameSuffix;
	}

	public void setQueueNameSuffix(String queueNameSuffix) {
		this.queueNameSuffix = queueNameSuffix;
	}

	public int getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public boolean isIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(boolean ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}

	public String getDefaultQueue() {
		return defaultQueue;
	}

	public void setDefaultQueue(String defaultQueue) {
		this.defaultQueue = defaultQueue;
	}

	public Map<String, QueueConfig> getQueueConfigs() {
		return queueConfigs;
	}

	public void setQueueConfigs(Map<String, QueueConfig> queueConfigs) {
		this.queueConfigs = queueConfigs;
	}

	public void setApplicationContext(ApplicationContext appCtx)
			throws BeansException {
		this.applicationContext = appCtx;
	}

}
