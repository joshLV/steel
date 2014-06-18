/**
 * 
 */
package com.gw.steel.spring.rediscache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.gw.steel.helper.Loader;


/**
 * @author Dongpo.wu
 *
 */
public class ShardJedisPoolConfigBuilder {	
	

	private static final String SYSTEM_PROPERTY_SHARD_JEDIS_POOL_PROPERTIES_FILENAME = "shardjedispool.properties.filename";
	
	
   private static final String DEFAULT_POOL_RESOURCE = "jedis-pool.properties";

   private  String poolPropertiesFilename;
   
   private  ShardJedisPoolConfiguration configuration;
   
   /**
    * The setters used to extract properties.
    */
   private final List<AbstractPropertySetter<?,ShardJedisPoolConfiguration>> settersRegistry = new ArrayList<AbstractPropertySetter<?,ShardJedisPoolConfiguration>>();

   /**
    * Return this class instance.
    *
    * @return this class instance.
    */
  
   public ShardJedisPoolConfiguration getConfiguration(){
	   return configuration;
   }
	/**
	 * Private default constructor:
	 * pre-handle the property file and init all eligible setter.
	 */
	public  void init() {
		if(StringUtils.isBlank(poolPropertiesFilename)){
			poolPropertiesFilename = System.getProperty(SYSTEM_PROPERTY_SHARD_JEDIS_POOL_PROPERTIES_FILENAME);
		}
		settersRegistry.add(new IntegerPropertySetter<ShardJedisPoolConfiguration>("redis.pool.maxActive", "maxActive", 10));

        settersRegistry.add(new IntegerPropertySetter<ShardJedisPoolConfiguration>("redis.pool.minIdle", "minIdle", 10));
        settersRegistry.add(new IntegerPropertySetter<ShardJedisPoolConfiguration>("redis.pool.maxIdle", "maxIdle", 100));
        settersRegistry.add(new IntegerPropertySetter<ShardJedisPoolConfiguration>("redis.pool.maxWait", "maxWait", 10000));
        settersRegistry.add(new BooleanPropertySetter<ShardJedisPoolConfiguration>("redis.pool.testOnBorrow", "testOnBorrow", false));
        settersRegistry.add(new BooleanPropertySetter<ShardJedisPoolConfiguration>("redis.pool.testOnReturn", "testOnReturn", false));
        configuration = parseConfiguration();
	}
	
	/**
     * Parses the Config and builds a new {@link ShardJedisPoolConfiguration}.
     *
     * @return the converted {@link ShardJedisPoolConfiguration}.
     */
    private ShardJedisPoolConfiguration parseConfiguration() {
    	Properties config = new Properties();
    	InputStream input = null;
        if(StringUtils.isBlank(poolPropertiesFilename)){
        	input = Loader.getResourceAsStream(DEFAULT_POOL_RESOURCE);
        }else{
        	try {
				input = new FileInputStream(poolPropertiesFilename);
			} catch (FileNotFoundException e) {
				input = Loader.getResourceAsStream(poolPropertiesFilename);
			}
        }
         // if Input is null , return default instance.
        if(input==null){
        	return new ShardJedisPoolConfiguration();
        }
       
        try {
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading classpath property '"
                    + poolPropertiesFilename
                    + "', see nested exceptions", e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                // close quietly
            }
        }

        ShardJedisPoolConfiguration configuration = new ShardJedisPoolConfiguration();

        for (AbstractPropertySetter<?,ShardJedisPoolConfiguration> setter : settersRegistry) {
            setter.set(config, configuration);
        }

        return configuration;
    }
    /**
     * Jedis 配置池信息
     * @param poolPropertiesFilename
     */
    public void setPoolPropertiesFilename(String poolPropertiesFilename) {
		this.poolPropertiesFilename = poolPropertiesFilename;
	}

}
