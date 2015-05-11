package steel.mdp;

import steel.spring.mdp.annotation.MdpService;

@MdpService(serviceInterfaceClass = ServiceA.class)
public class ServiceAImpl implements ServiceA {
	public String echo(String str) {
		return str;
	}

}
