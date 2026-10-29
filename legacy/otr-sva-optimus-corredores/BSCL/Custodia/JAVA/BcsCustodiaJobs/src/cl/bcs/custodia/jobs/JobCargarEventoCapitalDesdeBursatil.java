
package cl.bcs.custodia.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.custodia.CargarEventoCapitalDesdeBursatil;
import cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Entrada;
import cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.Salida;
import cl.bcs.custodia.evc.CrearEventoCapital;



public class JobCargarEventoCapitalDesdeBursatil extends BcsJob implements JavaCall{

	private final Log log = new Log("CUS.JobCargarEventoCapitalDesdeBursatil");
	
	private final String SQL_UPDATE_ID_PROGRAMA =  loadResourceToString("cl/bcs/custodia/jobs/sql/Insert_RutaLog_EventosErroneos.sql");
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}

	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");

    	
    	CargarEventoCapitalDesdeBursatil servicioCargarEventoCapitalDesdeBursatil =  new CargarEventoCapitalDesdeBursatil();
    	Entrada entradaCargarEventoCapitalDesdeBursatil = new Entrada();
    	Salida salidaCargarEventoCapitalDesdeBursatil = new Salida();
	
    	entradaCargarEventoCapitalDesdeBursatil.p_IdEmpresa		= Long.parseLong(this.getParam("IdEmpresa"));
    	entradaCargarEventoCapitalDesdeBursatil.p_IdUsuario		= Long.parseLong(this.getParam("IdUsuario"));
    	entradaCargarEventoCapitalDesdeBursatil.p_IdModulo		= this.getParam("IdModulo");
    	entradaCargarEventoCapitalDesdeBursatil.p_FechaSistema	= this.getParam("FechaSistema");
    	
    	String parRuta =  Utils.getConfiguracion(con, Utils.getSchema(), entradaCargarEventoCapitalDesdeBursatil.p_IdEmpresa, "PLA.RutaBaseArchivos");
		SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmmss");
		String P_OUT_NOMBREARCHIVO="LOG_EVENTOS_CAPITAL_ERRONEOS_"+entradaCargarEventoCapitalDesdeBursatil.p_IdEmpresa+"_"+fm.format(new java.util.Date())+".txt";
		String P_OUT_RUTAARCHIVOLOG=parRuta+"/"+P_OUT_NOMBREARCHIVO;
		
		File archivoLog = new File (P_OUT_RUTAARCHIVOLOG);
		BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(archivoLog));
		
		SQL sql=new SQL();
		List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
		actualizar.add(new ActualizarPrograma(P_OUT_NOMBREARCHIVO, this.getJobId()));
		sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
		
		bw.write( "*CargarEventoCapitalDesdeBursatil* Preparando llamada\n");
		
		bw.write( "*CargarEventoCapitalDesdeBursatil* Se ejecuta llamada con "+ salidaCargarEventoCapitalDesdeBursatil.P_OUT_EVENTOS_ERRORES.size() + " registros erroneos a procesar\n");		
		
		try{			
			salidaCargarEventoCapitalDesdeBursatil = servicioCargarEventoCapitalDesdeBursatil.logica(con, entradaCargarEventoCapitalDesdeBursatil);
			log.write(salidaCargarEventoCapitalDesdeBursatil.toString());
			
			for(CargarEventoCapitalDesdeBursatil.EventoCapital_Con_Error evErr: salidaCargarEventoCapitalDesdeBursatil.P_OUT_EVENTOS_ERRORES) {
				bw.write( "*CargarEventoCapitalDesdeBursatil* == Evento Erroneo ==\n");
				bw.write( "*CargarEventoCapitalDesdeBursatil* NEMO: "+ evErr.p_Nemo +"\n");
				bw.write( "*CargarEventoCapitalDesdeBursatil* TIPO: "+ evErr.p_IdTipoEventoCapital +"\n");
				bw.write( "*CargarEventoCapitalDesdeBursatil* FECHA LIMITE: "+ evErr.p_FechaLimite +"\n");
				bw.write( "*CargarEventoCapitalDesdeBursatil* FECHA PAGO: "+ evErr.p_FechaPago +"\n");
				bw.write( "*CargarEventoCapitalDesdeBursatil* MENSAJE: "+ evErr.p_Mensaje +"\n");
				bw.write( "*CargarEventoCapitalDesdeBursatil* ====================\n");
				}
			bw.write( "*CargarEventoCapitalDesdeBursatil* == Termino llamada ==\n");
			bw.close();
			
			if(salidaCargarEventoCapitalDesdeBursatil.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				con.commit();
				return true;
			}
			else
			{
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
    	
    	System.out.println("== INICIO PRUEBA ==");
    	
        try {
        	Connection con = PlataformaUtils.getDESAConnection();
        	JobCargarEventoCapitalDesdeBursatil demo = new JobCargarEventoCapitalDesdeBursatil();
        	demo.logica(con,true);
        
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("== FIN PRUEBA ==");
    }

    
}
