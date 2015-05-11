package com.gw.steel.httpinvocation.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import com.gw.steel.httpinvocation.api.annotation.HttpService;
import com.gw.steel.httpinvocation.api.annotation.UnknownClass;
/**
 * Http Invocation Bean Spring容器事件处理器实现类
 * @author Dongpo.wu
 *
 */
public class HttpServiceExportBeanPostProcessor implements BeanPostProcessor,  PriorityOrdered, ApplicationContextAware   {
	
	private static Logger logger = LoggerFactory.getLogger(HttpServiceExportBeanPostProcessor.class);
	
	/**
	 * 启动顺序号
	 */
	private int order = Ordered.LOWEST_PRECEDENCE - 2;
	/**
	 * 应用Spring上下文
	 */
	private ConfigurableApplicationContext applicationContext;

	
	@Override
	public int getOrder() {		
		return order;
	}
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext)applicationContext;
	}	

	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {	
		return exportHttpService(bean, beanName);
	}	
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {	
		
		return bean;
	}
	
	private Object exportHttpService(Object bean, String beanName){		
		
		if(!(bean instanceof SimpleUrlHandlerMapping)){
			return bean;
		}
		System.err.println("simpleUrl ----->");
		// 有SimpleUrlHandlerMapping定义
		Map<String, Object> httpServiceBeans =applicationContext.getBeansWithAnnotation(HttpService.class);
		ApplicationContext parent = applicationContext.getParent();
		Map<String, Object> parentHttpServiceBeans =parent.getBeansWithAnnotation(HttpService.class);
		
		if(httpServiceBeans!=null && httpServiceBeans.size()>0)
			httpServiceBeans.putAll(parentHttpServiceBeans);
		else{
			httpServiceBeans = parentHttpServiceBeans;
		}
		
		Map<String, String> urlMap = new HashMap<String, String>();
		for(String key : httpServiceBeans.keySet()){
			Object httpServiceBean = httpServiceBeans.get(key);
			
			//Class<?> clazz = httpServiceBean.getClass();
			//查看是否是代理类，如果是代理则获取target对象。
			boolean isProxy = AopUtils.isAopProxy(httpServiceBean);
			Class<?> clazz = null;
			if (isProxy) {
				clazz = AopUtils.getTargetClass(httpServiceBean);
			} else {
				clazz = httpServiceBean.getClass();
			}
			
			HttpService httpServiceAnno = clazz.getAnnotation(HttpService.class);
			Class<?> httpServiceInfClazz = httpServiceAnno.serviceInterfaceClass();//实现类上指定的服务接口
			String serviceName = httpServiceAnno.serviceName(); //实现类上服务名称
			if(httpServiceInfClazz.equals(UnknownClass.class)){
				throw new RuntimeException("The serviceInterfaceClass of HttpService is required on concrete class [" + clazz + " ] ");
			}
			
			if (httpServiceInfClazz.isAnnotationPresent(HttpService.class) == false) {
				throw new RuntimeException("The class=[" + httpServiceInfClazz.getName() + "] isn't the HttpService.");
			}
			// validate the serviceName
			if(serviceName==null||"".equals(serviceName)){
				serviceName =  httpServiceInfClazz.getAnnotation(HttpService.class).serviceName();
			}
			if(serviceName==null||"".equals(serviceName)){
				throw new RuntimeException("The serviceName is required. class=[" + clazz.getName() + "]");
			}
			
			
			String exportbeanName = serviceName+"_service";
//			DefaultListableBeanFactory  beanFactory = (DefaultListableBeanFactory)applicationContext.getBeanFactory();
//			BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(HttpInvokerServiceExporter.class);
//		     
//		    beanFactory.registerBeanDefinition(exportbeanName,  bdb.getBeanDefinition());
		    
			HttpInvokerServiceExporter export = new HttpInvokerServiceExporter();
			export.setService(httpServiceBean);
			export.setServiceInterface(httpServiceInfClazz);
			export.prepare();
			applicationContext.getBeanFactory().registerSingleton(exportbeanName, export);	
			
			
			urlMap.put("/"+serviceName, exportbeanName);			
		}		
		SimpleUrlHandlerMapping urlMapping = (SimpleUrlHandlerMapping)bean;
		urlMapping.setUrlMap(urlMap);
		urlMapping.initApplicationContext();
		return bean;
		
	}
}
