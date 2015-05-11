package steel.mdp.msgprop;

import steel.spring.mdp.annotation.MdpService;
import steel.spring.mdp.annotation.MessageProperty;

@MdpService(queueNames="steel.mdp.MRequestQueue")
public interface MsgSelectService {
	public String echo(@MessageProperty(name = "sourceId", jxpath = "sourceId") MsgSelectProp msgProp) ;
}
