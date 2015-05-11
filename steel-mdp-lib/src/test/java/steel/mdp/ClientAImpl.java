package steel.mdp;

import steel.spring.mdp.annotation.Mdpwired;

public class ClientAImpl implements ClientA {
	private ServiceA serviceA;
	
	public String echo(String str) {
		return serviceA.echo(str);
	}

	@Mdpwired(queueName="steel.mdp.RequestQueue")
	public void setServiceA(ServiceA serviceA) {
		this.serviceA = serviceA;
	}

}
