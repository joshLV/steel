package steel.mdp;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import steel.mdp.status.MdpHostStatus;
/**
 * @project: Steel
 * @description: Mdp状态报告器实现类
 */
public class MdpStatusReporter {
    private Log logger = LogFactory.getLog(this.getClass());
    
    /**
     * 尝试重连的基本间隔时间（毫秒）
     */
    private long baseInterval = 1000 * 30;

	private JmsTemplate jmsTemplate;

	private Thread worker;

	private MdpStatusCollector mdpStatusCollector;

	private ConnectionFactory connectionFactory;

	public MdpStatusReporter(ConnectionFactory cf, MdpStatusCollector collector) {
		this.mdpStatusCollector = collector;
		this.connectionFactory = cf;
		jmsTemplate = new JmsTemplate(cf);
		jmsTemplate.setPubSubDomain(true);
		jmsTemplate.setDefaultDestinationName(QueueNames.MDP_STATUS);
		worker = new Thread() {
			public void run() {
				while (true) {
					try {
						Message msg = jmsTemplate.receive();
						Destination replyTo = msg.getJMSReplyTo();
						if (replyTo == null) {
							logger.warn("MdpStatus replyTo is null.");
							return;
						}

						final MdpHostStatus status = mdpStatusCollector.getStatus(connectionFactory);
						jmsTemplate.send(replyTo, new MessageCreator() {
							public Message createMessage(Session sess) throws JMSException {
								return sess.createObjectMessage(status);
							}
						});

						logger.info("Client collected mdpStatus.");
					} catch (Exception e) {
						logger.error("MdpStatusReport meet error.", e);
						
						try {
							Thread.sleep(baseInterval);
						} catch (InterruptedException e1) {
							logger.error(e1.getMessage(),e1);
						}
					}
				}
			}
		};

		worker.setDaemon(true);
		worker.start();

		logger.info("MdpStatusReporter started.");
	}

	public long getBaseInterval() {
		return baseInterval;
	}

	public void setBaseInterval(long baseInterval) {
		this.baseInterval = baseInterval;
	}
}
