package steel.mdp;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class MdpServerMain{
	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:steel/mdp/testMdpLibServerSpringContext.xml");
		System.out.println("Started");
		while (true) {
			Thread.sleep(300 * 1000);
		}
	}

}
