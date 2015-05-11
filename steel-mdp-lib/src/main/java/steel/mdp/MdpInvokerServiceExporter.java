package steel.mdp;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * @project: Steel
 * @description: Mdp调用服务导出器实现类
 
 *
 */
public class MdpInvokerServiceExporter extends RemoteInvocationBasedExporter implements SessionAwareMessageListener,
		InitializingBean {
	private MessageConverter messageConverter = new SimpleMessageConverter();

	private boolean ignoreInvalidRequests = true;

	private Object proxy;
	
	private boolean ignoreTimeout = false;

	/**
	 * Specify the MessageConverter to use for turning request messages into
	 * {@link org.springframework.remoting.support.RemoteInvocation} objects,
	 * as well as {@link org.springframework.remoting.support.RemoteInvocationResult}
	 * objects into response messages.
	 * <p>Default is a {@link org.springframework.jms.support.converter.SimpleMessageConverter},
	 * using a standard JMS {@link javax.jms.ObjectMessage} for each invocation /
	 * invocation result object.
	 * <p>Custom implementations may generally adapt Serializables into
	 * special kinds of messages, or might be specifically tailored for
	 * translating RemoteInvocation(Result)s into specific kinds of messages.
	 */
	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = (messageConverter != null ? messageConverter : new SimpleMessageConverter());
	}

	/**
	 * Set whether invalidly formatted messages should be discarded.
	 * Default is "true".
	 * <p>Switch this flag to "false" to throw an exception back to the
	 * listener container. This will typically lead to redelivery of
	 * the message, which is usually undesirable - since the message
	 * content will be the same (that is, still invalid).
	 */
	public void setIgnoreInvalidRequests(boolean ignoreInvalidRequests) {
		this.ignoreInvalidRequests = ignoreInvalidRequests;
	}

	public void afterPropertiesSet() {
		this.proxy = getProxyForService();
	}

	public void onMessage(Message requestMessage, Session session) throws JMSException {
		RemoteInvocation invocation = readRemoteInvocation(requestMessage);
		if (invocation != null) {
			RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
			writeRemoteInvocationResult(requestMessage, session, result);
		}
	}

	/**
	 * Read a RemoteInvocation from the given JMS message.
	 * @param requestMessage current request message
	 * @return the RemoteInvocation object (or <code>null</code>
	 * in case of an invalid message that will simply be ignored)
	 * @throws javax.jms.JMSException in case of message access failure
	 */
	protected RemoteInvocation readRemoteInvocation(Message requestMessage) throws JMSException {
		Object content = this.messageConverter.fromMessage(requestMessage);
		if (content instanceof RemoteInvocation) {
			return (RemoteInvocation) content;
		}

		return onInvalidRequest(requestMessage);
	}

	/**
	 * Send the given RemoteInvocationResult as a JMS message to the originator.
	 * @param requestMessage current request message
	 * @param session the JMS Session to use
	 * @param result the RemoteInvocationResult object
	 * @throws javax.jms.JMSException if thrown by trying to send the message
	 */
	protected void writeRemoteInvocationResult(Message requestMessage, Session session, RemoteInvocationResult result)
			throws JMSException {
		if (requestMessage.getJMSReplyTo() == null) {
			// asynchronous call
			if (result.getException() != null) {
				logger.error(result.getException().getMessage(), result.getException());
			}

			return;
		}

		Message response = createResponseMessage(requestMessage, session, result);
		MessageProducer producer = session.createProducer(requestMessage.getJMSReplyTo());
		long timeout = requestMessage.getLongProperty(MessageReservePropertyNames.MSB_MDP_METHOD_TIMEOUT);
		try {
			if ( ignoreTimeout == false ) {
				producer.send(response, DeliveryMode.NON_PERSISTENT, requestMessage.getJMSPriority(), timeout);
			} else {
				producer.send(response, DeliveryMode.NON_PERSISTENT, requestMessage.getJMSPriority(), 0);
			}
		} finally {
			JmsUtils.closeMessageProducer(producer);
		}
	}

	/**
	 * Create the invocation result response message.
	 * <p>The default implementation creates a JMS ObjectMessage
	 * for the given RemoteInvocationResult object.
	 * @param requestMessage the original request message
	 * @param session the JMS session to use
	 * @param result the invocation result
	 * @return the message response to send
	 * @throws javax.jms.JMSException if creating the messsage failed
	 */
	protected Message createResponseMessage(Message requestMessage, Session session, RemoteInvocationResult result)
			throws JMSException {

		Message response = this.messageConverter.toMessage(result, session);
		response.setJMSCorrelationID(requestMessage.getJMSCorrelationID());
		return response;
	}

	/**
	 * Callback that is invoked by {@link #readRemoteInvocation}
	 * when it encounters an invalid request message.
	 * <p>The default implementation either discards the invalid message or
	 * throws a MessageFormatException - according to the "ignoreInvalidRequests"
	 * flag, which is set to "true" (that is, discard invalid messages) by default.
	 * @param requestMessage the invalid request message
	 * @return the RemoteInvocation to expose for the invalid request (typically
	 * <code>null</code> in case of an invalid message that will simply be ignored)
	 * @throws javax.jms.JMSException in case of the invalid request supposed
	 * to lead to an exception (instead of ignoring it)
	 * @see #readRemoteInvocation
	 * @see #setIgnoreInvalidRequests
	 */
	protected RemoteInvocation onInvalidRequest(Message requestMessage) throws JMSException {
		if (this.ignoreInvalidRequests) {
			if (logger.isWarnEnabled()) {
				logger.warn("Invalid request message will be discarded: " + requestMessage);
			}

			return null;
		} else {
			throw new MessageFormatException("Invalid request message: " + requestMessage);
		}
	}

	public boolean isIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(boolean ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}
}
