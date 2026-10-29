package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.custodia.CargaArchivoBONY;
import cl.bcs.custodia.CargaArchivoCavali;
import cl.bcs.custodia.CargaArchivoLEK;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarArchivoCustodia extends BcsJob implements JavaCall{
	
	private final Log log = new Log("CUS.CargarArchivoCustodia");
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/custodia/jobs/sql/CargarArchivoCustodia_update.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
        SQL sql=new SQL();
        Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
        String modulo = this.getParam("IdModulo");
        String tipoArchivo = this.getParam("TipoArchivo");
        String contenidoArchivo = this.getParam("Archivo");
        String rutaLog = "";
        try{
	        if ("CARGA_POSICIONES_CAV".equalsIgnoreCase(tipoArchivo)){
	        	CargaArchivoCavali cargaArchivoCavali = new CargaArchivoCavali();
	        	CargaArchivoCavali.InCargaArchivoCavali inCargaArchivoCavali = new CargaArchivoCavali.InCargaArchivoCavali();
	        	inCargaArchivoCavali.p_IdEmpresa = idEmpresa;
	        	inCargaArchivoCavali.p_IdUsuario = idUsuario;
	        	inCargaArchivoCavali.p_IdModulo = modulo;
	        	inCargaArchivoCavali.p_Archivo = contenidoArchivo;
	        	inCargaArchivoCavali.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
	        	CargaArchivoCavali.OutCargaArchivoCavali out = cargaArchivoCavali.logica(con, inCargaArchivoCavali);
	        	rutaLog = out.p_out_NombreArchivoLog;
	        }
	        if ("CARGA_POSICIONES_LEK".equalsIgnoreCase(tipoArchivo)){
	        	CargaArchivoLEK cargaArchivoLEK = new CargaArchivoLEK();
	        	CargaArchivoLEK.InCargaArchivoLEK inCargaArchivoLEK = new CargaArchivoLEK.InCargaArchivoLEK();
	        	inCargaArchivoLEK.p_IdEmpresa = idEmpresa;
	        	inCargaArchivoLEK.p_IdUsuario = idUsuario;
	        	inCargaArchivoLEK.p_IdModulo = modulo;
	        	inCargaArchivoLEK.p_Archivo = contenidoArchivo;
	        	inCargaArchivoLEK.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
	        	CargaArchivoLEK.OutCargaArchivoLEK out = cargaArchivoLEK.logica(con, inCargaArchivoLEK);
	        	rutaLog = out.p_out_NombreArchivoLog;
	        }
	        if ("CARGA_POSICIONES_BONY".equalsIgnoreCase(tipoArchivo)){
	        	CargaArchivoBONY cargaArchivoLEK = new CargaArchivoBONY();
	        	CargaArchivoBONY.InCargaArchivoBONY inCargaArchivoBONY = new CargaArchivoBONY.InCargaArchivoBONY();
	        	inCargaArchivoBONY.p_IdEmpresa = idEmpresa;
	        	inCargaArchivoBONY.p_IdUsuario = idUsuario;
	        	inCargaArchivoBONY.p_IdModulo = modulo;
	        	inCargaArchivoBONY.p_Archivo = contenidoArchivo;
	        	inCargaArchivoBONY.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
	        	CargaArchivoBONY.OutCargaArchivoBONY out = cargaArchivoLEK.logica(con, inCargaArchivoBONY);
	        	rutaLog = out.p_out_NombreArchivoLog;
	        }
	        List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
			actualizar.add(new ActualizarPrograma("",rutaLog,this.getJobId()));
			sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
	        con.commit();
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
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
