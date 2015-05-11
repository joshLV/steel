package steel.mdp;

import javax.jms.ConnectionFactory;

import steel.mdp.status.MdpHostStatus;

/**
 * @project: Steel
 * @description: Mdp状态收集器接口定义类
 */
public interface MdpStatusCollector {
	/**
	 * 获得Mdp主机状态
	 * @param cf
	 * @return
	 */
	public MdpHostStatus getStatus(ConnectionFactory cf);
}
