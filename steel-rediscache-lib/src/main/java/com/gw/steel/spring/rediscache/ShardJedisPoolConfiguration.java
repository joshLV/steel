package com.gw.steel.spring.rediscache;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Shard Jedis pool configuration 
 * @author Dongpo.wu
 *
 */
public class ShardJedisPoolConfiguration {	
	
	private int maxActive=10;
	
	private int minIdle=10;
	
	private int maxIdle = 100;
	
	private int maxWait = 10000;
	
	private boolean testOnBorrow = false;
	
	private boolean testOnReturn=false;	

	/**
	 * @return the maxActive
	 */
	public int getMaxActive() {
		return maxActive;
	}

	/**
	 * @param maxActive the maxActive to set
	 */
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	/**
	 * @return the minIdle
	 */
	public int getMinIdle() {
		return minIdle;
	}

	/**
	 * @param minIdle the minIdle to set
	 */
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	/**
	 * @return the maxIdle
	 */
	public int getMaxIdle() {
		return maxIdle;
	}

	/**
	 * @param maxIdle the maxIdle to set
	 */
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	/**
	 * @return the maxWait
	 */
	public int getMaxWait() {
		return maxWait;
	}

	/**
	 * @param maxWait the maxWait to set
	 */
	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	/**
	 * @return the testOnBorrow
	 */
	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	/**
	 * @param testOnBorrow the testOnBorrow to set
	 */
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	/**
	 * @return the testOnReturn
	 */
	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	/**
	 * @param testOnReturn the testOnReturn to set
	 */
	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
