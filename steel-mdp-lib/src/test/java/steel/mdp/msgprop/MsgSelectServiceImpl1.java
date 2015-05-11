package steel.mdp.msgprop;

import steel.spring.mdp.annotation.MdpService;

@MdpService(serviceInterfaceClass=MsgSelectService.class)
public class MsgSelectServiceImpl1 implements MsgSelectService {

	@Override
	public String echo(MsgSelectProp prop) {
		return this.getClass().getName()+" - "+ prop.getSourceId() +" - "+prop.getMsg();
	}

}
