package cl.bcs.socionegocio.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DocumentosClientePorVencerJob extends BcsJob {
	public static boolean DEBUG = false;
	
	public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
		if(DEBUG){
			final long idEmpresa = 1;
			return logica(dbConnection, idEmpresa);			
		}else{
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			return logica(dbConnection, idEmpresa);
		}
	}

	
	public boolean logica(Connection dbConnection, Long idEmpresa) throws InterruptedException, Throwable {
		System.out.println(":DOCUMENTOSPORVENCER:  ****** INICIO  *******");
	    		
		
		return false;
	}
	 
	@Override
	public void onError(Throwable t) {
	    System.out.println(":DOCUMENTOSPORVENCER:  ****** ERROR  *******");  
	    t.printStackTrace();
	}
	
	@Override
	public void pause() {
	
	}
	
	//solo para test
		public static void main(String[] args) {
			// Example
			System.out.println("== START ==");
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				con.setAutoCommit(false);
				SQL.exec(con, "set schema BSCL");
				SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
				// ---------------------------------
				try {
					DocumentosClientePorVencerJob apf = new DocumentosClientePorVencerJob();
					DEBUG=true;
					System.out.println(apf.transaction(con));
					con.commit();
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("== END ==");
		}

}
