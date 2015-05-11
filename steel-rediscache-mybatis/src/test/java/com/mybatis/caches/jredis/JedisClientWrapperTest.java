package com.mybatis.caches.jredis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.caches.jredis.JedisClientWrapper;

import com.mybatis.caches.serial.User;


public class JedisClientWrapperTest {
	private JedisClientWrapper wrapper;
	
	@Before
	public void setUp() throws Exception {
		wrapper = new JedisClientWrapper();
	}

	@After
	public void tearDown() throws Exception {
		wrapper = null;
	}	

	@Test
	public void testPutGetObject() {
		User u = new User();
		u.setAge("13");
		u.setName("John");
		wrapper.putObject("first cache item", u, "myIbtatis");
		
		Object rtn = wrapper.getObject("first cache item", "User");
		assertEquals(User.class, rtn.getClass());
	}
	//@Test
	public void testRemoveObject() {
		wrapper.putObject("Remove item", "something should be removed", "myIbtatis");
		
		//remove act
		Object rtn = wrapper.getObject("Remove item", "User");
		assertEquals("something should be removed", rtn);
		rtn = wrapper.getObject("Remove item", "User");
		assertNull(rtn);
		
	}

	//@Test
	public void testRemoveGroup() {
		String groupKey =  "_mybatis_myIbtatis_" + DigestUtils.sha1Hex("myIbtatis");
		System.out.println(wrapper.getObject(groupKey,"myIbtatis" ));
		wrapper.removeGroup("myIbtatis");
		Object group = wrapper.getObject(groupKey,"myIbtatis");
		assertNull(group);
	}
	
	

}
