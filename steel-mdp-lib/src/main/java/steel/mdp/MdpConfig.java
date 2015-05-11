package steel.mdp;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.Ordered;

/**
 * Mdp配置类。
 *
 */
public class MdpConfig {
    public static final int DEFAULT_CONCURRENT_CONSUMERS = 5;
    
    /**
     * Jms连接工厂映射表（队列名字->队列配置）
     */
    private Map<String, QueueConfig> queueConfigs = new TreeMap<String, QueueConfig>();

    /**
     * 队列名称后缀
     */
    private String queueNameSuffix;

    /**
     * 缺省并发消费者数量
     */
    private int concurrentConsumers = DEFAULT_CONCURRENT_CONSUMERS;

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

	public Map<String, QueueConfig> getQueueConfigs() {
		return queueConfigs;
	}

	public void setQueueConfigs(Map<String, QueueConfig> queueConfigs) {
		this.queueConfigs = queueConfigs;
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

	public String getDefaultQueue() {
		return defaultQueue;
	}

	public void setDefaultQueue(String defaultQueue) {
		this.defaultQueue = defaultQueue;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(boolean ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}
}
