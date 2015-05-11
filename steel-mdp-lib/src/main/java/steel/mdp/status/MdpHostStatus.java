package steel.mdp.status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @project: Steel
 * @description: Mdp主机状态
 */
public class MdpHostStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主机名
	 */
	private String host;

	/**
	 * ip地址
	 */
	private String ip;

	/**
	 * 进程号
	 */
	private long pid;

	/**
	 * 进入状态集
	 */
	private List<Incoming> incomings = new ArrayList<Incoming>();

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<Incoming> getIncomings() {
		return incomings;
	}

	public void setIncomings(List<Incoming> incomings) {
		this.incomings = incomings;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}
}