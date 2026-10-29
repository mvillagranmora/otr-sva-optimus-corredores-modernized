package cl.bcs.plataforma.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

public class VencimientoClavesJob extends BcsJob {
	
	
	public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
		System.out.println(":VENCIMIENTOCLAVES:  ****** INICIO  *******");
		CallableStatement cstmt = null;	
        final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		try{
			String callSpCrearAsuntoPendiente = "{CALL \"BSCL.Plataforma.BD.Servicios::ACC.VencimientoClaves\" (?,?, ?,?,?,?,?)}";
			cstmt = dbConnection.prepareCall(callSpCrearAsuntoPendiente);
			cstmt.setLong(1, idEmpresa);
			cstmt.setString(2, "PLA");
			cstmt.registerOutParameter(3, Types.INTEGER);
			cstmt.registerOutParameter(4, Types.INTEGER);
			cstmt.registerOutParameter(5, Types.INTEGER);
			cstmt.execute();
			System.out.println("CLAVES DE USUARIO VENCIDAS: " + cstmt.getInt(3));				
			cstmt.close();

			System.out.println(":VENCIMIENTOCLAVES:  ****** COMMIT  *******");
			return true;
    	}catch(Exception ex){
    		ex.printStackTrace();
    		System.out.println(":VENCIMIENTOCLAVES:  ****** ROLLBACK  *******");  
    	}
		return false;
	}
	 
	@Override
	public void onError(Throwable t) {
	    System.out.println(":VENCIMIENTOCLAVES:  ****** ERROR  *******");  
	    t.printStackTrace();
	}
	
	@Override
	public void pause() {
	
	}

}