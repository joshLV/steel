package steel.mdp;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:com/bill99/steel/mdp/testMdpLibSpringContext.xml" })
public class MdpLoopbackTest extends TestCase {
	private ClientA clientA;
	
	@Test
	public void test() throws Exception {
		System.out.println(clientA.echo("Hello mdp!"));
	}

	@Autowired
	public void setClientA(ClientA clientA) {
		this.clientA = clientA;
	}

}
