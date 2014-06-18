package com.gw.steel.rediscache.spring;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.gw.steel.spring.rediscache.annotation.Cacheable;
/**
 * 
 * @author Dongpo.wu
 *
 */
public class RedisCacheAnnoMethodDetail<T extends Annotation> implements Serializable {

	/**
	 * Auto gen
	 */
	private static final long serialVersionUID = 7403298776501251207L;
	
	private Annotation methodAnno;
	
	private Map<Integer, T> paraAnnoMap = new HashMap<Integer, T>();

	/**
	 * @return the methodAnno
	 */
	public Annotation getMethodAnno() {
		return methodAnno;
	}

	/**
	 * @param methodAnno the methodAnno to set
	 */
	public void setMethodAnno(Annotation methodAnno) {
		this.methodAnno = methodAnno;
	}

	/**
	 * @return the paraAnnoMap
	 */
	public Map<Integer, T> getParaAnnoMap() {
		return paraAnnoMap;
	}

	/**
	 * @param paraAnnoMap the paraAnnoMap to set
	 */
	public void setParaAnnoMap(Map<Integer, T> paraAnnoMap) {
		this.paraAnnoMap = paraAnnoMap;
	}
	
	public void addParaAnno(Integer key, T value){
		this.paraAnnoMap.put(key, value);
	}
	
}
