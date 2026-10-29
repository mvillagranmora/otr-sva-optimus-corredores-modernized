package cl.bcs.utils;

public abstract class BcsPojo {
	
	public String toString() {
		return Utils.printJson(this, false);
	}
		
}
