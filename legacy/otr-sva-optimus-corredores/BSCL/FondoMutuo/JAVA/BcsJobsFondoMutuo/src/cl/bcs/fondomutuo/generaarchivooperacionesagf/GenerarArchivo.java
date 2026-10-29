package cl.bcs.fondomutuo.generaarchivooperacionesagf;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.GeneracionArchivoAGF;
import cl.bcs.fondomutuo.GeneracionArchivoAGF.InGeneracionArchivoAGF;
import cl.bcs.fondomutuo.GeneracionArchivoAGF.OutGeneracionArchivoAGF;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivo extends BcsJob implements JavaCall{

	private final Log log = new Log("GenerarArchivo");
	
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/fondomutuo/generaarchivooperacionesagf/sql/GenerarArchivo_update.sql");
	
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

    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	GeneracionArchivoAGF generacionArchivoAGF = new  GeneracionArchivoAGF();
    	InGeneracionArchivoAGF entrada= new InGeneracionArchivoAGF();
		
		entrada.p_IdEmpresa=Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_IdUsuario=Long.parseLong(this.getParam("IdUsuario"));
		entrada.p_IdModulo = this.getParam("IdModulo");
		entrada.p_FechaDesde = this.getParam("FechaDesde");
		entrada.p_FechaHasta = this.getParam("FechaHasta");
		entrada.p_IdAmbiente = this.getParam("IdAmbiente");
		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");

		try{
			OutGeneracionArchivoAGF out= generacionArchivoAGF.logica(con,entrada);
			SQL sql=new SQL();
			if(out.p_out_NombreArchivo != null){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.p_out_NombreArchivo,out.p_out_NombreArchivoLog,this.getJobId()));
				sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
				con.commit();
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
    }

    // @Override
    public void onError(Throwable t) {
    	log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
    }

    // @Override
    public void pause() {

    }
    
    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

    public static void main(String[] args) throws IOException {
        GenerarArchivo generarArchivo = new GenerarArchivo();
        try {
            generarArchivo.transaction(null);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
