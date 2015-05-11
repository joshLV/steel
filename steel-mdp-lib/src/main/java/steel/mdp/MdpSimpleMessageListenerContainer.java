package steel.mdp;

import javax.jms.JMSException;

import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * @Project: Steel_trunk
 * @Description: MDP扩展的SimpleMessageListenerContainer，用于处理connection连接中断后，自动重连
 
 */
public class MdpSimpleMessageListenerContainer extends SimpleMessageListenerContainer {
	/**
	 * 是否允许尝试重新连接
	 */
	private boolean retryAllow = true;

	/**
	 * 尝试重连的基本间隔时间（毫秒）
	 */
	private long baseInterval = 5000;

	/**
	 * 处理因为connection中断导致的Listener失效的情况
	 */
	@Override
	public void onException(JMSException ex) {
		super.onException(ex);

		if (retryAllow == false) {
			return;
		}

		try {
			this.getSharedConnection().getClientID();
			this.logger.info("Recovered from crash!");
		} 
		catch (JMSException e) {
			this.logger.error("ErrorCode=" + e.getErrorCode() + ", " + e.getMessage(), e);

			// 连接失败
			if ("-22".equals(e.getErrorCode())) {
				Long tid = Thread.currentThread().getId();
				logger.warn("[Begin] ThreadId="+tid+" Trying to recover from JMS Connection, Destination="
						+ super.getDestinationName());

				try {
					Thread.sleep(baseInterval);
					this.onException(ex);
				} catch (InterruptedException e1) {
					this.logger.error(e1.getMessage(), e1);
				}
				
				logger.warn("[End] ThreadId="+tid+" Trying to recover from JMS Connection, Destination="
						+ super.getDestinationName());
			}
		} 
		catch (Exception exp) {
			// when other exception occurs
			logger.warn("MQ CONN CRASH:: Trying to recover from JMS Connection, Destination="
					+ super.getDestinationName());
			try {
				Thread.sleep(baseInterval);
				this.onException(ex);
			} catch (InterruptedException e1) {
				this.logger.error(e1.getMessage(), e1);
			}
		}
	}

	public long getBaseInterval() {
		return baseInterval;
	}

	public void setBaseInterval(long baseInterval) {
		this.baseInterval = baseInterval;
	}

	public boolean isRetryAllow() {
		return retryAllow;
	}

	public void setRetryAllow(boolean retryAllow) {
		this.retryAllow = retryAllow;
	}
}