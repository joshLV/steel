package steel.mdp.msgprop;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import steel.spring.mdp.annotation.Mdpwired;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:steel/msg1/testMsgSelectMdpClientSpringContext.xml" })
public class MsgSelectClientInvokerTest extends TestCase {
	private MsgSelectService clientA;
	
	@Test
	public void test() throws Exception {
		MsgSelectProp prop = new MsgSelectProp();
		prop.setSourceId("msg1");
		prop.setMsg("hello world");
		System.out.println(clientA.echo(prop));
		
		prop.setSourceId("msg2");
		prop.setMsg("hello worlds");
		System.out.println(clientA.echo(prop));
	}

	@Mdpwired
	public void setClientA(MsgSelectService clientA) {
		this.clientA = clientA;
	}

}
