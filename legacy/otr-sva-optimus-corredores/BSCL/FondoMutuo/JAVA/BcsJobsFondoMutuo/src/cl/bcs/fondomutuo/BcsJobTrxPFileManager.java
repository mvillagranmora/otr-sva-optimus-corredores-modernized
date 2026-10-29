package cl.bcs.fondomutuo;

import java.sql.Connection;

import cl.bcs.fondomutuo.TrxPFileManager.EntradaTrxPFileManager;
import cl.bcs.fondomutuo.TrxPFileManager.SalidaTrxPFileManager;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobTrxPFileManager extends BcsJob{
	
	private final Log log = new Log("FMM.BcsJobTrxPFileManager");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn,false);
	}
	
	public boolean go(Connection con, boolean debug) throws InterruptedException, Throwable {
		TrxPFileManager trxP = new TrxPFileManager();
		EntradaTrxPFileManager entrada = new EntradaTrxPFileManager();
		if(debug){
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
		}else{
			entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		}
		
		SalidaTrxPFileManager salida = trxP.logica(con, entrada);
		
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
		Connection con = PlataformaUtils.getDESAConnection();
		BcsJobTrxPFileManager apf = new BcsJobTrxPFileManager();		
		System.out.println(apf.go(con,true));	      
		con.commit();
		System.out.println("== END ==");
	}
}
