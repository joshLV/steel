package steel.mdp;

import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.listener.SessionAwareMessageListener;

public class ExampleListener implements SessionAwareMessageListener<Message> {

	public void onMessage(Message arg0, Session sess) {
		System.out.println("onMessage");
	}

}
