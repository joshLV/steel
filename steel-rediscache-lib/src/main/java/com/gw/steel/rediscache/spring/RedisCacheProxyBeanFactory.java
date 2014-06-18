package com.gw.steel.rediscache.spring;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import com.gw.steel.spring.rediscache.annotation.Cacheable;
import com.gw.steel.spring.rediscache.annotation.Keyable;
import com.gw.steel.spring.rediscache.annotation.RedisCacheService;

public class RedisCacheProxyBeanFactory implements FactoryBean<Object>, InitializingBean, BeanClassLoaderAware {
	private static final Log logger = LogFactory.getLog(RedisCacheProxyBeanFactory.class);
	
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private Object target;
	
	private transient ClassLoader proxyClassLoader;

	private Object proxy;
	
	private Class<?>[] proxyInterfaces;
	
	private final RedisCacheInterceptor redisCacheInterceptor = new RedisCacheInterceptor();
	
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;		
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("begin to execute afterPropertiesSet");
		}
		if (this.target == null) {
			throw new IllegalArgumentException("Property 'target' is required");
		}
		if (this.target instanceof String) {
			throw new IllegalArgumentException("'target' needs to be a bean reference, not a bean name as value");
		}
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
		}
		
		redisCacheInterceptor.afterPropertiesSet();
		
		ProxyFactory proxyFactory = new ProxyFactory();
		if(proxyInterfaces!=null){
			proxyFactory.setInterfaces(proxyInterfaces);
		}
		proxyFactory.addAdvice(redisCacheInterceptor);
		
		TargetSource targetSource = createTargetSource(this.target);
		proxyFactory.setTargetSource(targetSource);		
		this.proxy = proxyFactory.getProxy(this.beanClassLoader);
		
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<?> getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}		
		if (this.target instanceof TargetSource) {
			return ((TargetSource) this.target).getTargetClass();
		}
		if (this.target != null) {
			return this.target.getClass();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {		
		return false;
	}
	
	private TargetSource createTargetSource(Object target) {
		if (target instanceof TargetSource) {
			return (TargetSource) target;
		}
		else {
			return new SingletonTargetSource(target);
		}
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext){
		redisCacheInterceptor.setApplicationContext(applicationContext);
	}
	
	public void setRedisCacheService(RedisCacheService redisCacheService) {
		redisCacheInterceptor.setRedisCacheService(redisCacheService);
	}
	
	/**
	 * @param cachePutAnnotMap the cachePutAnnotMap to set
	 */
	public void setCachePutAnnotMap(
			Map<Method, RedisCacheAnnoMethodDetail<Cacheable>> cachePutAnnotMap) {
		redisCacheInterceptor.setCachePutAnnotMap(cachePutAnnotMap);
	}
	/**
	 * @param cacheGutAnnotMap the cacheGutAnnotMap to set
	 */
	public void setCacheGetAnnotMap(
			Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheGetAnnotMap) {
		redisCacheInterceptor.setCacheGetAnnotMap(cacheGetAnnotMap);
	}
	/**
	 * @param cacheRemoveAnnotMap the cacheRemoveAnnotMap to set
	 */
	public void setCacheRemoveAnnotMap(
			Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheRemoveAnnotMap) {
		redisCacheInterceptor.setCacheRemoveAnnotMap(cacheRemoveAnnotMap);
	}
}
