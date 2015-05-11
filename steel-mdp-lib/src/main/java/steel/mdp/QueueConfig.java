package steel.mdp;

import javax.jms.ConnectionFactory;

/**
 * @project: Steel
 * @description: 队列配置类
 
 */
public class QueueConfig {
	/**
	 * 消息选择语句
	 */
	private String messageSelector;

	/**
	 * 并发消费者数量（Mdp并发线程数）
	 */
	private Integer concurrentConsumers;

	/**
	 * 实际队列名称
	 */
	private String realQueueName;

	/**
	 * 连接工厂
	 */
	private ConnectionFactory connectionFactory;

	public String getMessageSelector() {
		return messageSelector;
	}

	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	public Integer getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public void setConcurrentConsumers(Integer concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public String getRealQueueName() {
		return realQueueName;
	}

	public void setRealQueueName(String realQueueName) {
		this.realQueueName = realQueueName;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

}
