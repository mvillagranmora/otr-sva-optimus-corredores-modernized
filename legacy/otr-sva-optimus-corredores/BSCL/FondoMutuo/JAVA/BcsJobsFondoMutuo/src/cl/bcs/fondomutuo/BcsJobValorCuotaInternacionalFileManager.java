package cl.bcs.fondomutuo;

import java.sql.Connection;

import cl.bcs.fondomutuo.ValorCuotaInternacionalFileManager.EntradaValorCuotaInternacionalFileManager;
import cl.bcs.fondomutuo.ValorCuotaInternacionalFileManager.SalidaValorCuotaInternacionalFileManager;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobValorCuotaInternacionalFileManager extends BcsJob{
	private final Log log = new Log("FMM.BcsJobValorCuotaInternacionalFileManager");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn,false);
	}
	
	public boolean go(Connection con, boolean debug) throws InterruptedException, Throwable {
		ValorCuotaInternacionalFileManager vc = new ValorCuotaInternacionalFileManager();
		EntradaValorCuotaInternacionalFileManager entrada = new EntradaValorCuotaInternacionalFileManager();
		if(debug){
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
		}else{
			entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		}
		
		SalidaValorCuotaInternacionalFileManager salida = vc.logica(con, entrada);
		
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
		Connection con = PlataformaUtils.getBolsaConnection();
		BcsJobValorCuotaInternacionalFileManager apf = new BcsJobValorCuotaInternacionalFileManager();		
		System.out.println(apf.go(con,true));	      
		con.commit();
		System.out.println("== END ==");
	}
}

