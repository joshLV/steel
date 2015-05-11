package steel.mdp;

/**
 * @project: Steel
 * @description: Mdp服务注册器类
 
 */
public class MdpServiceInvoker {
	/**
	 * 队列名称
	 */
	private String queueName;

	/**
	 * 服务接口
	 */
	private String serviceInterface;

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

}
