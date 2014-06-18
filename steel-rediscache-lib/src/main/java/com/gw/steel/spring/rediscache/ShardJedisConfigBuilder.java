package com.gw.steel.spring.rediscache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.JedisShardInfo;

import com.gw.steel.helper.Loader;

/**
 * Load the shard jedis from properties
 * @author admin
 *
 */
public class ShardJedisConfigBuilder {
		
	private static final String SYSTEM_PROPERTY_JEDIS_SHARD_PROPERTIES_FILENAME = "jedisshard.properties.filename";
	
	/**
    * 默认的 Redis shard config
    */
   private static String DEFAULT_SHARD_RESOURCE = "jedis-shard.properties";

   private  String configFile;
   
   private List<JedisShardInfo> shards = null;
	/**
	 * should be call first and once
	 */
	public void init() {
		if(StringUtils.isBlank(configFile)){			
			configFile = System.getProperty(SYSTEM_PROPERTY_JEDIS_SHARD_PROPERTIES_FILENAME);
		}
		Properties p = loadConfiguration();
		shards = parseConfiguration(p);
	}
		

   public List<JedisShardInfo> getShards() {
	return shards;
   }
   
  private Properties loadConfiguration() {
   	Properties config = new Properties();   	
   	InputStream input = null ;
   	if(StringUtils.isBlank(configFile)){//load the default config
   		input = Loader.getResourceAsStream(DEFAULT_SHARD_RESOURCE);
   	}else{   		
   		try {
   			input = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			input = Loader.getResourceAsStream(configFile);
		} 
   	}   	
   	
   		
	try{	   		
   		config.load(input);
	}catch(Exception ex){
	 String prop = StringUtils.isBlank(configFile)?DEFAULT_SHARD_RESOURCE:configFile;
	 throw new RuntimeException("An error occurred while reading redis shard property '"
             + prop
             + "', see nested exceptions", ex);
	}
	finally{   			
		try {
			if(input!=null)
				input.close();
        } catch (IOException e) {
            // close quietly
        }
	}
   	 
  return config;      
 }
   
   private List<JedisShardInfo> parseConfiguration(Properties config){
	   int line = 0;
	   Map<String , RedisInfo> redisInfoMap = new HashMap<String, RedisInfo>();
	   for (Entry<Object, Object> pair : config.entrySet()) {
		   ++line;
		   String key = (String) pair.getKey();
		   String value = (String) pair.getValue();
		   
		   String[] items = key.split("\\.");
		   String name = items[1];
		   String redisKey = items[2];
		   RedisInfo redis = null;
		   if ((redis = redisInfoMap.get(name)) == null) {
			   redis = new RedisInfo();
			   redisInfoMap.put(name, redis);
		   }
		   try {
			   BeanUtils.setProperty(redis, redisKey, value);
			   //redis.getClass().getme
			   
		   } catch (Exception e) {
			   String msg = MessageFormat.format("Error Occured while proccessing line:{0},{1}={2}",	line, key, value);
			   throw new RuntimeException(msg);
		   }
	   }
	   // done
	   
	   return transform(redisInfoMap);
   }
   /**
    * Transform the redisInfoMap to List<JedisShardInfo>
    * @param redisInfoMap
    */
   private List<JedisShardInfo> transform(Map<String , RedisInfo> redisInfoMap) {
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		for (Entry<String, RedisInfo> entry : redisInfoMap.entrySet()) {
			RedisInfo info = entry.getValue();
			JedisShardInfo shard = new JedisShardInfo(info.getHost(),
					Integer.valueOf(info.getPort()), Integer.valueOf(info.getTimeout()), Integer.valueOf(info
							.getWeight()));
			shard.setPassword(info.getPassword());
			shards.add(shard);
		}
		return shards;
	}
   /**
    * 注入 Redis shard 配置文件
    * @param propertiesFilename
    */
   public void setConfigFile(String configFile) {
	this.configFile = configFile;
}
}
