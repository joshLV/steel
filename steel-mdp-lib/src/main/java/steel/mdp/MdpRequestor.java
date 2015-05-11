package steel.mdp;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import steel.spring.mdp.annotation.MdpMethod;
import steel.spring.mdp.annotation.MessageProperties;
import steel.spring.mdp.annotation.MessageProperty;

/**
 * @project: Steel
 * @description: Mdp请求实现类
 
 */
public class MdpRequestor implements InitializingBean, ExceptionListener {
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * 连接工厂
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * 目标解析器
	 */
	private DestinationResolver destinationResolver = new DynamicDestinationResolver();

	/**
	 * 队列连接
	 */
	private Connection connection = null;

	/**
	 * 应答队列
	 */
	private TemporaryQueue responseQueue = null;

	/**
	 * 会话集
	 */
	private List<Session> sessions;

	/**
	 * 消费者集
	 */
	private List<MessageConsumer> consumers;

	/**
	 * 滞后标志
	 */
	private boolean lazy = true;

	/**
	 * 计数器
	 */
	private long counter;

	/**
	 * 并发的应答消费者数量
	 */
	private int concurrentConsumers = 1;

	/**
	 * 应答桶集
	 */
	private ConcurrentMap<String, ResponseTub> responseTubs = new ConcurrentHashMap<String, ResponseTub>();

	/**
	 * 忽略超时
	 */
	private boolean ignoreTimeout = false;

	private class ResponseTub {
		/**
		 * 应答消息
		 */
		private Message responseMessage;
	}

	/**
	 * Creates a new correlation ID. Note that because the correlationID is used
	 * on a per-temporary destination basis, it does not need to be unique
	 * across more than one destination. So a simple counter will suffice.
	 * 
	 * @return
	 */
	public String createCorrelationID() {
		return Long.toString(nextCounter());
	}

	protected synchronized long nextCounter() {
		return ++counter;
	}

	private void freeJmsResourceIfError() {
		consumers = null;
		responseQueue = null;

		for (Session session : sessions) {
			JmsUtils.closeSession(session);
		}

		sessions = null;

		if (connection != null) {
			JmsUtils.closeConnection(connection, true);
			connection = null;
		}
	}

	private void processResponseMessage(Message message) {
		try {
			String correlationID = message.getJMSCorrelationID();
			if (correlationID == null) {
				onInvalidResponse(message);
				return;
			}

			ResponseTub respTub = responseTubs.remove(correlationID);
			if (respTub == null) {
				onInvalidResponse(message);
				return;
			}

			synchronized (respTub) {
				respTub.responseMessage = message;
				respTub.notify();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initJmsResource() throws JMSException {
		sessions = new ArrayList<Session>(concurrentConsumers);
		consumers = new ArrayList<MessageConsumer>(concurrentConsumers);

		try {
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(this);// 增加异常处理监听
			connection.start();

			for (int i = 0; i < concurrentConsumers; i++) {
				Session session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				sessions.add(session);

				if (responseQueue == null) {
					responseQueue = session.createTemporaryQueue();
				}

				MessageConsumer consumer = session
						.createConsumer(responseQueue);
				consumer.setMessageListener(new MessageListener() {
					public void onMessage(final Message message) {
						processResponseMessage(message);
					}
				});

				consumers.add(consumer);
			}
		} catch (RuntimeException e) {
			freeJmsResourceIfError();
			throw e;
		} catch (JMSException e) {
			freeJmsResourceIfError();
			throw e;
		}
	}

	/**
	 * 连接发生异常时的处理
	 */
	public void onException(JMSException exp) {
		logger.error(exp.getMessage(), exp);

		freeJmsResourceIfError();
	}

	private void initIfNecessary() throws JMSException {
		if (lazy == false) {
			return;
		}

		if (connection != null) {
			return;
		}

		synchronized (this) {
			if (connection != null) {
				return;
			}

			initJmsResource();
		}
	}

	public void afterPropertiesSet() throws Exception {
		if (getConnectionFactory() == null) {
			throw new IllegalArgumentException(
					"Property 'connectionFactory' is required");
		}

		if (lazy == false) {
			initJmsResource();
		}
	}

	/**
	 * Resolve the given queue name into a JMS {@link javax.jms.Queue}, via this
	 * accessor's {@link DestinationResolver}.
	 * 
	 * @param session
	 *            the current JMS Session
	 * @param queueName
	 *            the name of the queue
	 * @return the located Queue
	 * @throws JMSException
	 *             if resolution failed
	 * @see #setDestinationResolver
	 */
	protected Queue resolveQueueName(Session session, String queueName)
			throws JMSException {
		return (Queue) destinationResolver.resolveDestinationName(session,
				queueName, false);
	}

	/**
	 * Resolve this accessor's target queue.
	 * 
	 * @param session
	 *            the current JMS Session
	 * @return the resolved target Queue
	 * @throws JMSException
	 *             if resolution failed
	 */
	protected Queue resolveQueue(Session session, Object queue)
			throws JMSException {
		if (queue instanceof Queue) {
			return (Queue) queue;
		} else if (queue instanceof String) {
			return resolveQueueName(session, (String) queue);
		} else {
			throw new javax.jms.IllegalStateException(
					"Queue object ["
							+ queue
							+ "] is neither a [javax.jms.Queue] nor a queue name String");
		}
	}

	/**
	 * Create the invoker request message.
	 * <p>
	 * The default implementation creates a JMS ObjectMessage for the given
	 * RemoteInvocation object.
	 * 
	 * @param session
	 *            the current JMS Session
	 * @param invocation
	 *            the remote invocation to send
	 * @return the JMS Message to send
	 * @throws JMSException
	 *             if the message could not be created
	 */
	protected Message createRequestMessage(Session session,
			RemoteInvocation invocation, MdpInvokerProxyFactoryBean proxy)
			throws JMSException {
		return proxy.getMessageConverter().toMessage(invocation, session);
	}

	/**
	 * Extract the invocation result from the response message.
	 * <p>
	 * The default implementation expects a JMS ObjectMessage carrying a
	 * RemoteInvocationResult object. If an invalid response message is
	 * encountered, the <code>onInvalidResponse</code> callback gets invoked.
	 * 
	 * @param responseMessage
	 *            the response message
	 * @return the invocation result
	 * @throws JMSException
	 *             is thrown if a JMS exception occurs
	 * @see #onInvalidResponse
	 */
	protected RemoteInvocationResult extractInvocationResult(
			MdpInvokerProxyFactoryBean proxy, Message responseMessage)
			throws JMSException {
		Object content = proxy.getMessageConverter().fromMessage(
				responseMessage);
		if (content instanceof RemoteInvocationResult) {
			return (RemoteInvocationResult) content;
		}

		return onInvalidResponse(responseMessage);
	}

	/**
	 * Callback that is invoked by <code>extractInvocationResult</code> when it
	 * encounters an invalid response message.
	 * <p>
	 * The default implementation throws a MessageFormatException.
	 * 
	 * @param responseMessage
	 *            the invalid response message
	 * @return an alternative invocation result that should be returned to the
	 *         caller (if desired)
	 * @throws JMSException
	 *             if the invalid response should lead to an infrastructure
	 *             exception propagated to the caller
	 * @see #extractInvocationResult
	 */
	protected RemoteInvocationResult onInvalidResponse(Message responseMessage)
			throws JMSException {
		throw new MessageFormatException("Invalid response message: "
				+ responseMessage);
	}

	protected void processMessageProperty(Message requestMessage,
			MessageProperty msgProperty, Object arg) throws JMSException {
		if (logger.isDebugEnabled()) {
			logger.debug("Set the message property, name=["
					+ msgProperty.name() + "].");
		}

		if (arg == null) {
			requestMessage.setObjectProperty(msgProperty.name(), null);
			return;
		}

		String jxpath = msgProperty.jxpath();
		if (StringUtils.isBlank(jxpath)) {
			requestMessage.setObjectProperty(msgProperty.name(), arg);
		} else {
			JXPathContext jxpathContext = JXPathContext.newContext(arg);
			jxpathContext.setLenient(true);
			requestMessage.setObjectProperty(msgProperty.name(), jxpathContext
					.getValue(jxpath));
		}
	}

	public RemoteInvocationResult executeRequest(RemoteInvocationContext ric)
			throws JMSException {
		initIfNecessary();

		MdpInvokerProxyFactoryBean proxy = ric.getProxy();
		RemoteInvocation remoteInvocation = ric.getRemoteInvocation();
		MethodInvocation methodInvocation = ric.getMethodInvocation();

		Method method = methodInvocation.getMethod();
		long timeout;
		int priority;
		int deliveryMode;
		boolean asynchronous = false;
		boolean transacted = false;
		if (method.isAnnotationPresent(MdpMethod.class)) {
			MdpMethod mdpMethod = method.getAnnotation(MdpMethod.class);
			timeout = mdpMethod.timeout();
			priority = mdpMethod.priority();
			asynchronous = mdpMethod.asynchronous();
			deliveryMode = mdpMethod.deliveryMode();
			transacted = mdpMethod.transacted();
		} else {
			timeout = MdpMethod.DEFAULT_TIMEOUT;
			priority = Message.DEFAULT_PRIORITY;
			deliveryMode = DeliveryMode.NON_PERSISTENT;
		}

		String correlationID = createCorrelationID();

		Session session = connection.createSession(transacted,
				Session.AUTO_ACKNOWLEDGE);
		Queue queueToUse = resolveQueue(session, proxy.getQueue());
		Message requestMessage = createRequestMessage(session,
				remoteInvocation, proxy);

		MessageProducer producer = null;
		ResponseTub respTub = null;
		try {
			producer = session.createProducer(queueToUse);
			if (asynchronous == false) {
				requestMessage.setJMSReplyTo(responseQueue);
			}

			requestMessage.setJMSCorrelationID(correlationID);
			requestMessage.setStringProperty(
					MessageReservePropertyNames.MSB_MDP_SERVICE_INTERFACE,
					proxy.getServiceInterface().getName());
			requestMessage.setStringProperty(
					MessageReservePropertyNames.MSB_MDP_SERVICE_NAME, proxy
							.getServiceName());
			requestMessage
					.setLongProperty(
							MessageReservePropertyNames.MSB_MDP_METHOD_TIMEOUT,
							timeout);

			Annotation[][] paraAnnox = methodInvocation.getMethod()
					.getParameterAnnotations();
			Object[] args = methodInvocation.getArguments();

			for (int i = 0; i < paraAnnox.length; i++) {
				Annotation[] paraAnnos = paraAnnox[i];
				Object arg = args[i];

				for (Annotation paraAnno : paraAnnos) {
					if (paraAnno instanceof MessageProperties) {
						MessageProperties msgProperties = (MessageProperties) paraAnno;
						for (MessageProperty msgProp : msgProperties.value()) {
							processMessageProperty(requestMessage, msgProp, arg);
						}
					} else if (paraAnno instanceof MessageProperty) {
						MessageProperty msgProp = (MessageProperty) paraAnno;
						processMessageProperty(requestMessage, msgProp, arg);
					}
				}
			}

			if (asynchronous == false) {
				respTub = new ResponseTub();
				responseTubs.put(correlationID, respTub);
			}

			if (ignoreTimeout == false) {
				producer.send(requestMessage, deliveryMode, priority, timeout);
			} else {
				producer.send(requestMessage, deliveryMode, priority, 0);
			}
		} catch (JMSException e) {
			if (asynchronous == false) {
				responseTubs.remove(correlationID);
			}

			throw e;
		} catch (RuntimeException e) {
			if (asynchronous == false) {
				responseTubs.remove(correlationID);
			}

			throw e;
		} finally {
			if (producer != null) {
				JmsUtils.closeMessageProducer(producer);
			}

			JmsUtils.closeSession(session);
		}

		if (asynchronous) {
			return null;
		}

		synchronized (respTub) {
			if (respTub.responseMessage == null) {
				try {
					respTub.wait(timeout);
				} catch (InterruptedException e) {
				}

				responseTubs.remove(correlationID);

				if (respTub.responseMessage == null) {
					throw new RuntimeException(
							"Mdp invoke timeout, serviceInterface=["
									+ proxy.getServiceInterface().getName()
									+ "], method=["
									+ remoteInvocation.getMethodName() + "].");
				}
			} else {
				responseTubs.remove(correlationID);
			}
		}

		return extractInvocationResult(proxy, respTub.responseMessage);
	}

	public DestinationResolver getDestinationResolver() {
		return destinationResolver;
	}

	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public boolean isIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(boolean ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

}
