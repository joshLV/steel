package steel.mdp.msgprop;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class MdpMsgSelectServerMain{
	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:steel/msg1/testMsgSelectMdpServer1SpringContext.xml");
		System.out.println("Started");
		while (true) {
			Thread.sleep(300 * 1000);
		}
	}

}
