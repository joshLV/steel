package com.gw.steel.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RedisCachedKeyHolderTest {
	static class MyThread extends Thread{
		private Log logger = LogFactory.getLog(MyThread.class);
		@Override
		public void run() {
			logger.info("Thread["+this.getName()+"] 1. print thread local value : "+RedisCachedKeyHolder.getRedisCacheKeys());
			RedisCachedKeyHolder.addRedisCacheKeys(this.getId()+ " first");
			logger.info("Thread["+this.getName()+"] 2. print thread local value : "+RedisCachedKeyHolder.getRedisCacheKeys());
			RedisCachedKeyHolder.addRedisCacheKeys(this.getId()+ " Second");
			logger.info("Thread["+this.getName()+"] 3. print thread local value : "+RedisCachedKeyHolder.getRedisCacheKeys());
		}
	}
	
	public static void main(String[] args) {
		Thread t1 = new MyThread();
		Thread t2 = new MyThread();
		Thread t3 = new MyThread();
		Thread t4 = new MyThread();
		Thread t5 = new MyThread();
		Thread t6 = new MyThread();
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		
	}
}
