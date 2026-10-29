package cl.bcs.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.sql2o.Sql2o;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public abstract class BcsJob implements Runnable {

	private long jobId;
	private boolean isDameon;
	protected Map<String, String> parameters;
	protected BcsJobListener jobListener = null;
	private DataSource datasource = null;
	private long delay;
	
	public BcsJob() {
		this.jobId = 0;
		this.parameters = new HashMap<String, String>();
		this.datasource = null;
	}

	public final void setParameters(long jobId, Map<String, String> parameters, long milisecondStarted,boolean isDaemon,DataSource datasource,String minuto) {
		this.jobId = jobId;
		this.parameters = parameters;
		this.datasource = datasource;
		this.isDameon = isDaemon;
		if (minuto.equalsIgnoreCase("**")) {
			this.delay = 5000;//en milisegundos, comportamiento actual
		}
		else {
			try {
				this.delay = Integer.parseInt(minuto)*60*1000;
			}
			catch(Exception e) {
				this.delay = 5000;
			}
		}
		
	}

	public final Integer getParamInteger(String key) {
		try {
			return Integer.parseInt(parameters.get(key));
		} catch (NumberFormatException e) {
			LogManager.getLogger(this.getClass()).error(e);
			return null;
		}
	}

	public final DataSource getDataSource() {
		return datasource;
	}
	
	public final Sql2o getSql2o() {
		return new Sql2o(datasource);
	}
	
	public final String getParam(String key) {
		return parameters.get(key);
	}
	
	public final Optional<String> getOptionalParam(String key){
		return Optional.ofNullable(parameters.get(key));
	}
	
	public abstract boolean transaction(Connection con) throws InterruptedException, Throwable;

	public abstract void onError(Throwable t);
	
	public abstract void pause();

	@Override
	public void run() {
		Logger logger = LogManager.getLogger(this.getClass());
		ThreadContext.put("jobid", jobId+"");
		try {			
			if (this.isDameon) {
				logger.info(":BCSJOB: DAEMON-JOB INICIADO #"+jobId);
				while (true) {
					try {
						jobListener.updateDaemon(jobId);
						Connection con=null;
						try {
							con=datasource.getConnection();
							SQL.exec(con, "SET SCHEMA "+Utils.getSchema());
							SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
							if (transaction(con)) {
								con.commit();
								logger.info(":BCSJOB: DAEMON-JOB COMMIT #"+jobId);	
							}else {
								con.rollback();
								logger.error(":BCSJOB: DAEMON-JOB ROLLBACK #"+jobId);								
							}
						}catch (Exception err) {
							if (con!=null)  {
								try {con.rollback();}catch(Exception er) { }								
							}
							throw err;
						}finally {
							if (con!=null) {								
								try { con.close();}catch(Exception er) { }
							}
						}			          
					}catch (Throwable e) {
						logger.error(":BCSJOB: DAEMON-JOB EXCEPTION #"+jobId+" ERROR:"+e.getMessage());						
						Thread.sleep(1000*60);									
					}
					if (this.delay > 60*1000) {
						logger.info(":BCSJOB: DAEMON-JOB #"+jobId+ " SLEEPING FOR " + this.delay/1000 + "SECONDS" ) ;		
					}
					Thread.sleep(this.delay);
				}				
			}else {
				logger.info(":BCSJOB: JOB INICIADO #"+jobId);
				Connection con=null;
				try {
					con=datasource.getConnection();
					SQL.exec(con, "SET SCHEMA "+Utils.getSchema());
					SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
					if (transaction(con)) {
						con.commit();
						logger.info(":BCSJOB: JOB COMMIT #"+jobId);	
					}else {
						con.rollback();
						logger.error(":BCSJOB: JOB ROLLBACK #"+jobId);								
					}
				}catch (Exception err) {
					if (con!=null)  {
						try {con.rollback();}catch(Exception er) { }								
					}
					throw err;
				}finally {
					if (con!=null) {								
						try { con.close();}catch(Exception er) { }
					}
				}
	            logger.info(":BCSJOB: JOB TERMINADO #"+jobId);
	            jobListener.status(jobId, false,"");				
			}
			ThreadContext.clearMap();
		} catch (Throwable e) {
			ThreadContext.clearMap();
            logger.error(":BCSJOB: JOB EXCEPTION #"+jobId +" ERROR:");
            e.printStackTrace();
            jobListener.status(jobId, true,e.getMessage());			
			onError(e);
		}
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobListener(BcsJobListener jobListener) {
		this.jobListener = jobListener;
	}
	
	private static final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	public static class BcsJobUpdate{
		public String RutaSalida;
		public Long IdPrograma;
		
		public BcsJobUpdate(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static void updateRutaSalida(Connection con, String rutaSalida, Long idPrograma) throws SQLException {
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_ID_PROGRAMA, Arrays.asList(new BcsJobUpdate(rutaSalida, idPrograma)), BcsJobUpdate.class);
	}
	

}
