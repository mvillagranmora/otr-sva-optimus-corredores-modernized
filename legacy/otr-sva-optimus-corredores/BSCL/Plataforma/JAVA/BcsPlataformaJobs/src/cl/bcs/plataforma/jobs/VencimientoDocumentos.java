package cl.bcs.plataforma.jobs;
  

import java.sql.CallableStatement;
import java.sql.Connection;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class VencimientoDocumentos extends BcsJob{
	
	public boolean logica(Connection dbConnection, Long idEmpresa) throws InterruptedException, Throwable {
		try {
			String callSp = "CALL \""+Utils.getSchema()+"\".\"BSCL.Plataforma.BD.Servicios::DOC.DocumentosPorVencer\"(?, ?, ?, ?,?,?,?)";
			System.out.println(":DOCUMENTOSPORVENCER:  ****** GET_DATASOURCE  *******");  
			try(CallableStatement cstmt = dbConnection.prepareCall(callSp)){
				cstmt.setLong(1, idEmpresa);
				cstmt.setLong(2, 0); //system
				cstmt.setString(3, "PLA");
				
				cstmt.execute();
								
				//OK, Grabar operacion			
				System.out.println(":DOCUMENTOSPORVENCER:  ****** COMMIT  *******");
				
	    	}
	        
			callSp = "CALL \"BSCL.Plataforma.BD.Servicios::DOC.VencerDocumentos\"(?, ?, ?, ?,?,?,?)";								
			System.out.println(":VENCIMIENTODOCUMENTOS:  ****** INICIO  *******"); 
	    	try (CallableStatement sp=dbConnection.prepareCall(callSp);){
	    		sp.setLong(1, idEmpresa);
	    		sp.setLong(2, 1);//idusuario
	    		sp.setString(3, "PLA");
	    		sp.execute();
	    	}
	    	
			return true;
		}catch (Exception e) {
			System.out.println(":DOCUMENTOSPORVENCER:  ****** ERROR AL OBTENER DATASOURCE  *******");  
			e.printStackTrace();
		} 
		return false;
	}
	
	public static boolean DEBUG = true;
	
	@Override
	public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
		if(DEBUG){
			final long idEmpresa = 1;
			return logica(dbConnection, idEmpresa);			
		}else{
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			return logica(dbConnection, idEmpresa);
		}
	}
	
	@Override
    public void onError(Throwable t) {
        System.out.println(":VENCIMIENTODOCUMENTOS:  ****** ERROR  *******");  
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
				VencimientoDocumentos apf = new VencimientoDocumentos();
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