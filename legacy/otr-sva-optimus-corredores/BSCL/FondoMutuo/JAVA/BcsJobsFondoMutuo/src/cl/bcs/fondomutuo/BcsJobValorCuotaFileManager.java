package cl.bcs.fondomutuo;

import java.sql.Connection;

import cl.bcs.fondomutuo.ValorCuotaFileManager.EntradaValorCuotaFileManager;
import cl.bcs.fondomutuo.ValorCuotaFileManager.SalidaValorCuotaFileManager;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobValorCuotaFileManager extends BcsJob{
	private final Log log = new Log("FMM.BcsJobValorCuotaFileManager");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn,false);
	}
	
	public boolean go(Connection con, boolean debug) throws InterruptedException, Throwable {
		ValorCuotaFileManager vc = new ValorCuotaFileManager();
		EntradaValorCuotaFileManager entrada = new EntradaValorCuotaFileManager();
		if(debug){
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
		}else{
			entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		}
		
		SalidaValorCuotaFileManager salida = vc.logica(con, entrada);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0) throw new Exception(salida.P_OUT_ERRORESDETALLE.toString());
		
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
		Connection con = PlataformaUtils.getBolsaConnection();
		BcsJobValorCuotaFileManager apf = new BcsJobValorCuotaFileManager();		
		System.out.println(apf.go(con,true));	      
		con.rollback();
		System.out.println("== END ==");
	}
}
