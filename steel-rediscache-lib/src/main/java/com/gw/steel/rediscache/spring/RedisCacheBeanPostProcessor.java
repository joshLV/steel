package com.gw.steel.rediscache.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.gw.steel.spring.rediscache.annotation.CacheGet;
import com.gw.steel.spring.rediscache.annotation.CachePut;
import com.gw.steel.spring.rediscache.annotation.CacheRemove;
import com.gw.steel.spring.rediscache.annotation.Cacheable;
import com.gw.steel.spring.rediscache.annotation.Keyable;
import com.gw.steel.spring.rediscache.annotation.RedisCacheService;
import com.gw.steel.spring.rediscache.annotation.UnknownClass;
/**
 * RedisCacheBean Spring容器事件处理器实现类
 * @author Dongpo.wu 
 * 
 *
 */
public class RedisCacheBeanPostProcessor implements BeanPostProcessor, PriorityOrdered, ApplicationContextAware {
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * 启动顺序号
	 */
	private int order = Ordered.LOWEST_PRECEDENCE - 2;
	/**
	 * 
	 */
	private ApplicationContext applicationContext;
	
	/* (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {		
		return order;
	}	

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {		
		this.applicationContext = applicationContext;
	}
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {		
		return bean;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		Class<?> clazz = bean.getClass();
		if (clazz.isAnnotationPresent(RedisCacheService.class)) {// class presenet with RedisCacheService
			RedisCacheService serviceAnno = clazz.getAnnotation(RedisCacheService.class);
			//decompose the method & parameter annotation
			Map<Method, RedisCacheAnnoMethodDetail<Cacheable>> cachePutAnnotMap = new HashMap<Method, RedisCacheAnnoMethodDetail<Cacheable>>();
			Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheGetAnnotMap = new HashMap<Method, RedisCacheAnnoMethodDetail<Keyable>>();
			Map<Method, RedisCacheAnnoMethodDetail<Keyable>> cacheRemoveAnnotMap = new HashMap<Method, RedisCacheAnnoMethodDetail<Keyable>>();
			for(Method m : clazz.getMethods()){				
				if(m.isAnnotationPresent(CachePut.class)){
					RedisCacheAnnoMethodDetail<Cacheable> md  = new RedisCacheAnnoMethodDetail<Cacheable>();
					decomposeCacheableParaAnn(m, md, CachePut.class);
					if(md.getParaAnnoMap()==null||md.getParaAnnoMap().isEmpty()){
						throw new IllegalArgumentException("Must be a argument annotated with Cacheable, refer to :"+m);
					}
					cachePutAnnotMap.put(m, md);					
				}else if(m.isAnnotationPresent(CacheGet.class)){
					RedisCacheAnnoMethodDetail<Keyable> md  = new RedisCacheAnnoMethodDetail<Keyable>();
					CacheGet cacheGet = m.getAnnotation(CacheGet.class);
					decomposeKeyableParaAnn(m, md, CacheGet.class);
					if((md.getParaAnnoMap()==null||md.getParaAnnoMap().isEmpty()) && StringUtils.isBlank(cacheGet.idStr())){
						throw new IllegalArgumentException("The idStr of CacheGet must be assign when Keyable was missing, refer to :"+m);
					}
					cacheGetAnnotMap.put(m, md);
				}else if(m.isAnnotationPresent(CacheRemove.class)){
					RedisCacheAnnoMethodDetail<Keyable> md  = new RedisCacheAnnoMethodDetail<Keyable>();
					CacheRemove cacheRemove = m.getAnnotation(CacheRemove.class);
					decomposeKeyableParaAnn(m, md, CacheRemove.class);
					if((md.getParaAnnoMap()==null||md.getParaAnnoMap().isEmpty()) && StringUtils.isBlank(cacheRemove.idStr())){
						throw new IllegalArgumentException("The idStr of CacheGet must be assign when Keyable was missing, refer to :"+m);
					}
					cacheRemoveAnnotMap.put(m, md);
				}else {
					if(logger.isDebugEnabled()){
						logger.debug("There is no cache annotation apply for " + m);
					}
				}
			}
			
			Class<?> siClazz = serviceAnno.serviceInterfaceClass();
			RedisCacheProxyBeanFactory rcBean = new RedisCacheProxyBeanFactory();
			if(siClazz.equals(UnknownClass.class)){
				if(logger.isInfoEnabled()){
					logger.info(clazz.getName()+" didn't present with RedisCacheService.serviceInterfaceClass");
				}
			}else{
				rcBean.setProxyInterfaces(new Class<?>[]{siClazz});
			}
			rcBean.setTarget(bean);
			rcBean.setApplicationContext(applicationContext);
			rcBean.setRedisCacheService(serviceAnno);
			rcBean.setCacheGetAnnotMap(cacheGetAnnotMap);
			rcBean.setCachePutAnnotMap(cachePutAnnotMap);
			rcBean.setCacheRemoveAnnotMap(cacheRemoveAnnotMap);
			try {
				rcBean.afterPropertiesSet();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return rcBean;
		}		
		return bean;
	}
	
	private  <T extends Annotation> void  decomposeCacheableParaAnn(Method m, RedisCacheAnnoMethodDetail<Cacheable> md,  Class<T> methodAnn){
		T cacheAnn = m.getAnnotation(methodAnn);
		md.setMethodAnno(cacheAnn);
		Annotation[][] paramAnns = m.getParameterAnnotations();
		for (int i = 0; i < paramAnns.length; i++) {
			for (int j = 0; j < paramAnns[i].length; j++) {
				Annotation ann = paramAnns[i][j];
				if(ann!=null && ann instanceof Cacheable){//get the Cacheable para
					Cacheable cacheable = (Cacheable)ann;
					if(cacheable.idPropertyNames().length < 1 && StringUtils.isBlank(cacheable.idStr())){
						throw new IllegalArgumentException("Property 'idPropertyName' & 'idStr' can't be all empty, refer to Method :"+m);
					}
					md.addParaAnno(i, cacheable);
					break;
				}
			}
		}
	}
	
	private  <T extends Annotation> void  decomposeKeyableParaAnn(Method m, RedisCacheAnnoMethodDetail<Keyable> md,  Class<T> methodAnn){
		T cacheAnn = m.getAnnotation(methodAnn);
		md.setMethodAnno(cacheAnn);
		Annotation[][] paramAnns = m.getParameterAnnotations();
		for (int i = 0; i < paramAnns.length; i++) {
			for (int j = 0; j < paramAnns[i].length; j++) {
				Annotation ann = paramAnns[i][j];
				if(ann!=null && ann instanceof Keyable){//get the Cacheable para
					Keyable cacheable = (Keyable)ann;
//					if(cacheable.idPropertyNames().length < 1 && StringUtils.isNotBlank(cacheable.idStr())){
//						throw new IllegalArgumentException("Property 'idPropertyName' & 'idStr' can't be all empty, refer to Method :"+m);
//					}
					md.addParaAnno(i, cacheable);
					break;
				}
			}
		}
	}
	
}
