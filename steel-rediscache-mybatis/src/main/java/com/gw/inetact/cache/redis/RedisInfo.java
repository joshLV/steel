package com.gw.inetact.cache.redis;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RedisInfo {
	private static Log log = LogFactory.getLog(RedisInfo.class);

	private String host;
	private String port;
	private String timeout;
	private String weight;
	private String password;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		
		try {
			Integer.valueOf(port);
			this.port=port;
		} catch (NumberFormatException e) {
			String msg = MessageFormat.format(
					"Invalid value {0} for redis.port, use default value 6379",
					port);
			log.error(msg);
		}
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		try {
			 Integer.valueOf(timeout);
			 this.timeout = timeout;
		} catch (NumberFormatException e) {
			String msg = MessageFormat
					.format("Invalid value {0} for redis.timeout, use default value 2000",
							timeout);
			log.error(msg);
		}
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		try {
			Integer.valueOf(weight);
			this.weight = weight;
		} catch (NumberFormatException e) {
			String msg = MessageFormat.format(
					"Invalid value {0} for redis.weight, use default value 1",
					weight);
			log.error(msg);
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}