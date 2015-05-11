package steel.mdp;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.RemoteInvocation;

/**
 * @project: Steel
 * @description: 远程调用上下文类

 *
 */
public class RemoteInvocationContext {
	/**
	 * 代理对象
	 */
	private MdpInvokerProxyFactoryBean proxy;
	
	/**
	 * 远程调用对象
	 */
	private RemoteInvocation remoteInvocation;
	
	/**
	 * 方法调用
	 */
	private MethodInvocation methodInvocation;

	public MdpInvokerProxyFactoryBean getProxy() {
		return proxy;
	}

	public void setProxy(MdpInvokerProxyFactoryBean proxy) {
		this.proxy = proxy;
	}

	public RemoteInvocation getRemoteInvocation() {
		return remoteInvocation;
	}

	public void setRemoteInvocation(RemoteInvocation remoteInvocation) {
		this.remoteInvocation = remoteInvocation;
	}

	public MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public void setMethodInvocation(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
	}

}