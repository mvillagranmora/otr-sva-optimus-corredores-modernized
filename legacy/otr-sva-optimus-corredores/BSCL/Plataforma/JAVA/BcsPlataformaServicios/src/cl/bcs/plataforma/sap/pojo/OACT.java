package cl.bcs.plataforma.sap.pojo;

import cl.bcs.utils.Utils;

public class OACT {
	public String LocManTran;
	public String AcctCode;
	public String Segment_0;
	public String AcctName;
	public Short GroupMask;
	public String ActType;
	public String FatherNum;
	
	public String toString(boolean pretty) {
		return Utils.printJson(this, pretty);
	}
	
	public String toString() {
		return Utils.printJson(this, true);
	}
	
}