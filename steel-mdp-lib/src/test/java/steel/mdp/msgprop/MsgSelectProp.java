package steel.mdp.msgprop;

import java.io.Serializable;

public class MsgSelectProp implements Serializable {

	private String sourceId;
	
	private String msg;

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
