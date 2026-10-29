package cl.bcs.custodia.jobs;

import java.sql.Connection;

import javax.sql.DataSource;

import cl.bcs.custodia.GenerarReporteCuadratura;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class ReporteCuadratura extends BcsJob{
	private final Log log = new Log("CUS.JOB.ReporteCuadratura");
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
        Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
        String fechaCuadratura = this.getParam("FechaCuadratura");
        String idBoveda = this.getParam("IdBoveda");
        Long idPrograma = this.getJobId();
        String ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
        DataSource datasource = super.getDataSource();
        try{
        	GenerarReporteCuadratura generarReporteCuadratura = new GenerarReporteCuadratura();
        	GenerarReporteCuadratura.Entrada entrada = new GenerarReporteCuadratura.Entrada();
        	entrada.p_IdEmpresa = idEmpresa;
        	entrada.p_IdUsuario = idUsuario;
        	entrada.p_FechaCuadratura = fechaCuadratura;
        	entrada.p_IdBoveda = idBoveda;
        	entrada.p_IdPrograma = idPrograma;
        	entrada.p_Ruta = ruta;
        	entrada.p_Datasource = datasource;
        	
        	GenerarReporteCuadratura.Salida salida = generarReporteCuadratura.logica(con, entrada);
        	if(new Long(0).compareTo(salida.P_OUT_ERRORESCONTADOR) == 0) con.commit();
        	else con.rollback();
	        return true;
        }catch (Exception e) {
			log.write(e);
			con.rollback();
			throw new Throwable(e);
		}
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
}
