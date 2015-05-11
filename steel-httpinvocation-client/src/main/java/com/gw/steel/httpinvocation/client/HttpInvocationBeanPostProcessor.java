package com.gw.steel.httpinvocation.client;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;

import com.gw.steel.httpinvocation.api.annotation.HttpService;
import com.gw.steel.httpinvocation.api.annotation.HttpWired;
/**
 * Http Invocation Bean Spring容器事件处理器实现类
 * @author Dongpo.wu
 *
 */
public class HttpInvocationBeanPostProcessor implements BeanPostProcessor,  PriorityOrdered  {
	
	private static Logger logger = LoggerFactory.getLogger(HttpInvocationBeanPostProcessor.class);
	
	/**
	 * 启动顺序号
	 */
	private int order = Ordered.LOWEST_PRECEDENCE - 2;
	
	
	/**
	 * 不带服务请求名称。
	 */
	private String defaultServiceUrl;
	
	private ApplicationContext applicationContext;
	/**
	 * HTTP Invoker Request Executor BeanID
	*/
	private HttpInvokerRequestExecutor httpRequestExecutor;
	
	/**
	 * 应用服务与应用服务URL映射表（ServiceName->ServiceUrl）
	 */
	private Map<String, String> serviceConfigs = new TreeMap<String, String>();

	
	//@Override
	public int getOrder() {		
		return order;
	}
	
	//@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {		
		//wired http Serivce
		wireHttpBean(bean, beanName);	
		
		return bean;
	}	
	
	//@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {	
		
		return bean;
	}	
	
	private void wireHttpBean(Object bean, String beanName){
		Class<?> clazz = bean.getClass();
		BeanInfo bInfo = null;

		try {
			bInfo = java.beans.Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new RuntimeException("Wiring bean=[" + beanName + "] meet error.", e);
		}
		
		for (PropertyDescriptor pd : bInfo.getPropertyDescriptors()) {
			Method m = pd.getWriteMethod();
			if (m == null) {
				continue;
			}

			if (m.isAnnotationPresent(HttpWired.class) == false) {
				continue;
			}
			Method rm = pd.getReadMethod();
			if (rm != null) {
				try {
					if (rm.invoke(bean) != null) {
						continue;
					}
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		 
			Class<?> httpBeanClass = pd.getPropertyType(); // 接口			
			if (httpBeanClass.isAnnotationPresent(HttpService.class) == false) {
				// 不是HttpBean
				throw new RuntimeException("Dependency bean isn't HttpService, dbean=[" + beanName + "." + pd.getName()
						+ "].");
			}else{
				HttpService httpServiceAnno =  httpBeanClass.getAnnotation(HttpService.class);
				HttpWired httpWiredAnno =  m.getAnnotation(HttpWired.class);
				//获取Servicename
				String serviceName = httpWiredAnno.serviceName()!=null&&!"".equals(httpWiredAnno.serviceName())?httpWiredAnno.serviceName():httpServiceAnno.serviceName();
				if(serviceName==null&& "".equals(serviceName)){
					throw new RuntimeException("The service name is required, dbean=[" + beanName + "." + pd.getName() + "].");
				}				
				String serviceUrl = retrievalServiceUrl(httpWiredAnno, httpServiceAnno);
				
				HttpInvokerProxyFactoryBean httpProxy = new HttpInvokerProxyFactoryBean();
				httpProxy.setServiceInterface(httpBeanClass);
				httpProxy.setServiceUrl(serviceUrl);
				if(httpRequestExecutor!=null){					
					httpProxy.setHttpInvokerRequestExecutor(httpRequestExecutor);
				}
				
				httpProxy.afterPropertiesSet();
				try {
					m.invoke(bean, httpProxy.getObject());
				}catch(Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}	
		}
	}
	/**
	 * 获取serviceUrl
	 * @param httpWired
	 * @return
	 */
	private String retrievalServiceUrl(HttpWired httpWired, HttpService httpServiceAnno){
		String serviceName = httpWired.serviceName();
		if(serviceName==null||"".equals(serviceName)){
			serviceName = httpServiceAnno.serviceName();
		}
		if(logger.isDebugEnabled()){
			logger.debug("The serviceName is "+serviceName);
		}
		String serviceUrl = httpWired.serviceUrl();
		if(serviceUrl==null || "".equals(serviceUrl)){
			serviceUrl = serviceConfigs.get(serviceName);
		}
		if(serviceUrl==null || "".equals(serviceUrl)){
			serviceUrl = defaultServiceUrl;
		}
		if(serviceUrl==null||"".equals(serviceUrl)){
			throw new RuntimeException("The service URL is required, serviceName is[" + serviceName +"].");
		}
		serviceUrl = serviceUrl.endsWith("/")?serviceUrl:serviceUrl+"/";
		return serviceUrl+serviceName;
	}
	
	/**
	 * set the default service url
	 * @param defaultServiceUrl
	 */
	public void setDefaultServiceUrl(String defaultServiceUrl) {
		this.defaultServiceUrl = defaultServiceUrl;
	}
	/**
	 * 
	 * @param serviceConfigs
	 */
	public void setServiceConfigs(Map<String, String> serviceConfigs) {
		this.serviceConfigs = serviceConfigs;
	}

	/**
	 * @param httpRequestExecutor the httpRequestExecutor to set
	 */
	public void setHttpRequestExecutor(
			HttpInvokerRequestExecutor httpRequestExecutor) {
		this.httpRequestExecutor = httpRequestExecutor;
	}
	
}
