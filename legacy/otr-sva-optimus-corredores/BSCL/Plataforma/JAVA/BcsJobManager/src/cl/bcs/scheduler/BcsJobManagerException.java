package cl.bcs.scheduler;

 
public class BcsJobManagerException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BcsJobManagerException(Throwable t) {
		super(t);
	}

	public BcsJobManagerException(String msg) {
		super(msg);
	}

	public BcsJobManagerException(Throwable t, String msg) {
		super(msg,t);
	}
 
}
