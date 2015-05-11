package steel.mdp.status;

import java.io.Serializable;
import java.util.Set;

/**
 * @project: Steel
 * @description: 进入状态类
 */
public class Incoming implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 队列名称
	 */
    private String queue;

    /**
     * 消息选择器
     */
    private String msgSelector;

    /**
     * 并发消费者数量
     */
    private int concurrentConsumers;

    /**
     * mdp服务接口集
     */
    private Set<String> mdpServiceInterfaces;

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getMsgSelector() {
		return msgSelector;
	}

	public void setMsgSelector(String msgSelector) {
		this.msgSelector = msgSelector;
	}

	public int getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public Set<String> getMdpServiceInterfaces() {
		return mdpServiceInterfaces;
	}

	public void setMdpServiceInterfaces(Set<String> mdpServiceInterfaces) {
		this.mdpServiceInterfaces = mdpServiceInterfaces;
	}
}