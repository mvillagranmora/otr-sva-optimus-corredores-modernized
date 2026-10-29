package cl.bcs.scheduler;

public interface BcsJobListener {
	public void status(long jobId, boolean isError,String msg);
	public void updateDaemon(long jobId);
	
}
