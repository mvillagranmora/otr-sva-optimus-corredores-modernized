package cl.bcs.plataforma.jobs;

import java.sql.Connection;

import cl.bcs.plataforma.ftp.FileManager;
import cl.bcs.plataforma.ftp.FileManager.EntradaFileManager;
import cl.bcs.plataforma.ftp.FileManager.SalidaFileManager;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class FileManagerJob extends BcsJob{
	private final Log log = new Log("FTP.FileManagerJob");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn,false);
	}
	
	public boolean go(Connection con, boolean debug) throws InterruptedException, Throwable {
		FileManager fileManager = new FileManager();
		EntradaFileManager entrada = new EntradaFileManager();
		if(debug){
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
		}else{
			entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		}
		
		SalidaFileManager salida = fileManager.logica(con, entrada);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0) return false;
		
		return true;
		
	}
	
	@Override
    public void onError(Throwable t) {
        log.write("****** ERROR  *******",t);
    }
	
	@Override
	public void pause() {}
	
	//solo para test
	public static void main(String[] args) throws Throwable {
		System.out.println("== START ==");
		Connection con = PlataformaUtils.getUATConnection();
		FileManagerJob apf = new FileManagerJob();		
		System.out.println(apf.go(con,true));	      
		con.commit();
		System.out.println("== END ==");
	}
}
