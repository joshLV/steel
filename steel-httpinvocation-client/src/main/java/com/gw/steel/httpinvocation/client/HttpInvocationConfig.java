package com.gw.steel.httpinvocation.client;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;

/**
 * Http Invocation 配置类
 * @author Dongpo.wu
 *
 */
public class HttpInvocationConfig {
	/**
	 * http服务请求URL, 追加servieName 形成一个完整的请求URL。
	 */
	private String serviceUrl ;
	/**
	 * httpRequestExecutor spring BEAN id
	 */
	private HttpInvokerRequestExecutor httpRequestExecutor;
	
	/**
	 * 应用服务与应用服务URL映射表（ServiceName->ServiceUrl）
	 */
	private Map<String, String> serviceConfigs = new TreeMap<String, String>();

	/**
	 * @return the serviceUrl
	 */
	public String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * @param serviceUrl the serviceUrl to set
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * @return the serviceConfigs
	 */
	public Map<String, String> getServiceConfigs() {
		return serviceConfigs;
	}

	/**
	 * @param serviceConfigs the queueConfigs to set
	 */
	public void setServiceConfigs(Map<String, String> serviceConfigs) {
		this.serviceConfigs = serviceConfigs;
	}

	/**
	 * @return the httpRequestExecutor
	 */
	public HttpInvokerRequestExecutor getHttpRequestExecutor() {
		return httpRequestExecutor;
	}

	/**
	 * @param httpRequestExecutor the httpRequestExecutor to set
	 */
	public void setHttpRequestExecutor(
			HttpInvokerRequestExecutor httpRequestExecutor) {
		this.httpRequestExecutor = httpRequestExecutor;
	}

	
}
