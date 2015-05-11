package steel.mdp.msgprop;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class MdpMsgSelectServer2Main{
	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:steel/msg2/testMsgSelectMdpServer2SpringContext.xml");
		System.out.println("Started");
		while (true) {
			Thread.sleep(300 * 1000);
		}
	}

}
