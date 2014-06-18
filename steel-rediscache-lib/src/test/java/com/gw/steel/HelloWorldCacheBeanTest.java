package com.gw.steel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/RedisCachePoxyTestContext.xml" })
public class HelloWorldCacheBeanTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public void testHelloWorld() {
		bean.putHelloWorld("My Test");
		String cacheStr = bean.getHelloWorld("My Test");
		Assert.assertEquals(cacheStr, "My Test");
		bean.removeHelloWorld("My Test");
		cacheStr = bean.getHelloWorld("My Test");
		Assert.assertEquals(cacheStr, "hello World");
	}
	@Test
	public void testUset(){
		User u = new User("Tid-123");
		u.setAge(20);
		u.setName("John");		
		//bean.putUser(u);		
		User u2 = bean.getUser(u);
		Assert.assertEquals(u.getId(), u2.getId());
		//bean.removeUser(u);
		u2=bean.getUser(u);
		Assert.assertNotEquals(u.getId(), u2.getId());
	}
	@Autowired
	private IHelloWorldCacheBean bean;
}
