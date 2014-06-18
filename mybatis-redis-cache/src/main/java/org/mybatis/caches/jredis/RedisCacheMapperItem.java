package org.mybatis.caches.jredis;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mybatis.caches.jredis.help.ParameterizedType;

import com.alibaba.fastjson.JSON;


public class RedisCacheMapperItem implements Serializable {

	/**
	 * Auto generate by eclipse
	 */
	private static final long serialVersionUID = 4429099617175386198L;
	/**
	 * Must be same value with the clazzType parameter
	 */
	public static final String KEY_CLAZZ_TYPE="clazzType";
	/**
	 * Must be same value with the cacheValue parameter
	 */
	public static final String KEY_CACHE_VALUE="cacheValue";

	private  Class rawType;
	
	private ParameterizedType parameterizedType;
	
	private  String cacheValue;	
	
	public RedisCacheMapperItem() {
		
	}
	
	public RedisCacheMapperItem(Object value) {
		this(value, value.getClass());
	}
	
	public RedisCacheMapperItem(Object value, Class clazz) {
		this.cacheValue = JSON.toJSONString(value);
		this.rawType = clazz;
	}
	public RedisCacheMapperItem(Object value, Class clazz, ParameterizedType parameterizedType) {
		this.cacheValue = JSON.toJSONString(value);
		this.rawType = clazz;
		this.parameterizedType = parameterizedType;
	}	

	/**
	 * @return the rawType
	 */
	public Class getRawType() {
		return rawType;
	}

	/**
	 * @param rawType the rawType to set
	 */
	public void setRawType(Class rawType) {
		this.rawType = rawType;
	}

	/**
	 * @return the parameterizedType
	 */
	public ParameterizedType getParameterizedType() {
		return parameterizedType;
	}

	/**
	 * @param parameterizedType the parameterizedType to set
	 */
	public void setParameterizedType(ParameterizedType parameterizedType) {
		this.parameterizedType = parameterizedType;
	}

	/**
	 * @return the cacheValue
	 */
	public String getCacheValue() {
		return cacheValue;
	}

	/**
	 * @param cacheValue the cacheValue to set
	 */
	public void setCacheValue(Object cacheValue) {
		this.cacheValue = JSON.toJSONString(cacheValue);
	}
	
	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this);
	}
	
	@Override
	public int hashCode() {		
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
}
