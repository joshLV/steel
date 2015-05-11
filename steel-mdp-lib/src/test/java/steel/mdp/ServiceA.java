package steel.mdp;

import steel.spring.mdp.annotation.MdpService;

@MdpService
public interface ServiceA {
	public String echo(String str);
}
