package cl.bcs.fondomutuo.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.fondomutuo.GenerarArchivoMovimientos;
import cl.bcs.fondomutuo.GenerarArchivoMovimientos.GenerarArchivoMovimientos_Entrada;
import cl.bcs.fondomutuo.GenerarArchivoMovimientos.GenerarArchivoMovimientos_Salida;
import cl.bcs.fondomutuo.GenerarArchivoParticipes;
import cl.bcs.fondomutuo.GenerarArchivoParticipes.GenerarArchivoParticipes_Entrada;
import cl.bcs.fondomutuo.GenerarArchivoParticipes.GenerarArchivoParticipes_Salida;

public class BcsJobGenerarArchivosFFMM extends BcsJob implements JavaCall {

	private final Log log = new Log("FMM.GenerarArchivosFFMMJob");
	
	private final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH =
		    "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" " +
		    "SET \"RutaSalida\" = :RutaArchivo, " +
		    "    \"RutaLog\" = :RutaLog, " +
		    "    \"Comentario\" = :Comentario " +
		    "WHERE \"IdPrograma\" = :IdPrograma";
	
	public static class ProgramaBatch {
		public Long IdPrograma;
		public String RutaArchivo;
		public String RutaLog;
		public String Comentario;

		public ProgramaBatch(Long IdPrograma, String RutaArchivo, String RutaLog, String Comentario) {
			this.IdPrograma = IdPrograma;
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.Comentario = Comentario;
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
	    log.write("INICIO JOB");

	    try {

	        Long IdEmpresa;
	        Long IdUsuario;
	        String FechaCurse;
	        String EstadoMovimiento;
	        String IdArchivoFondoMutuo;
	        String IdEmisor;
	        String IdMoneda;
	        String FondoMutuo;
	        String TipoSolicitud;
	        String IdModulo;

	        if (debug) {
	            IdEmpresa = 1L;
	            IdUsuario = 9784L;
	            FechaCurse = "2026-03-31";
	            EstadoMovimiento = "TODOS";
	            IdArchivoFondoMutuo = "INFORME_MOV_FFMM";
	            IdMoneda = "CLP";
	            FondoMutuo = "";
	            TipoSolicitud = "RES";
	            IdEmisor = "";
	            IdModulo = "FFMM";
	        } else {
	            IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	            IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
	            FechaCurse = this.getParam("FechaCurse");
	            EstadoMovimiento = this.getParam("EstadoMovimiento");
	            IdArchivoFondoMutuo = this.getParam("IdArchivoFondoMutuo");
	            IdMoneda = this.getParam("IdMoneda");
	            FondoMutuo = this.getParam("FondoMutuo");
	            TipoSolicitud = this.getParam("TipoSolicitud");
	            IdEmisor = this.getParam("IdEmisor");
	            IdModulo = "FFMM";
	        }

	        GenerarArchivoMovimientos_Entrada entradaMov = new GenerarArchivoMovimientos_Entrada();
	        GenerarArchivoParticipes_Entrada entradaPart = new GenerarArchivoParticipes_Entrada();

	        GenerarArchivoMovimientos_Salida salidaMov = new GenerarArchivoMovimientos_Salida();
	        GenerarArchivoParticipes_Salida salidaPart = new GenerarArchivoParticipes_Salida();
	        SQL sql = new SQL();
	        List<ProgramaBatch> programaBatch = new ArrayList<>();

	        if ("INFORME_MOV_FFMM".equals(IdArchivoFondoMutuo)) {	        // Movimientos
		        entradaMov.p_IdEmpresa = IdEmpresa;
		        entradaMov.p_IdUsuario = IdUsuario;
		        entradaMov.p_FechaCurse = FechaCurse;
		        entradaMov.p_FondoMutuo = FondoMutuo;
		        entradaMov.p_Emisor = IdEmisor;
		        entradaMov.p_Estado = EstadoMovimiento;
		        entradaMov.p_TipoSolicitud = TipoSolicitud;
		        entradaMov.p_IdMoneda = IdMoneda;
	            GenerarArchivoMovimientos service = new GenerarArchivoMovimientos();
	            salidaMov = service.logica(con, entradaMov);
	            if (salidaMov.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
	                return false;
	            }

	            if (!debug) {
	                programaBatch.add(new ProgramaBatch(this.getJobId(), salidaMov.P_OUT_RUTA_ARCHIVO, salidaMov.P_OUT_RUTA_ARCHIVO_LOG, salidaMov.P_OUT_COMENTARIO));
	                sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch, ProgramaBatch.class);
	            }
	        } else if ("INFORME_PARTICIPE_FFMM".equals(IdArchivoFondoMutuo)) {	        // Participes
		        entradaPart.p_IdEmpresa = IdEmpresa;
		        entradaPart.p_IdUsuario = IdUsuario;
		        entradaPart.p_FechaCurse = FechaCurse;
		        entradaPart.p_FondoMutuo = FondoMutuo;
		        entradaPart.p_Emisor = IdEmisor;
		        entradaPart.p_Estado = EstadoMovimiento;
		        entradaPart.p_TipoSolicitud = TipoSolicitud;
		        entradaPart.p_IdModulo = IdModulo;
	            GenerarArchivoParticipes service = new GenerarArchivoParticipes();
	            salidaPart = service.logica(con, entradaPart);
	            if (salidaPart.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
	                return false;
	            }

	            if (!debug) {
	                programaBatch.add(new ProgramaBatch(this.getJobId(),salidaPart.P_OUT_RUTA_ARCHIVO, salidaPart.P_OUT_RUTA_ARCHIVO_LOG, salidaPart.P_OUT_COMENTARIO));
	                sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch, ProgramaBatch.class);
	            }
	        } else {
	            throw new Exception("Tipo de archivo no válido: " + IdArchivoFondoMutuo);
	        }

	        con.commit();
	        return true;

	    } catch (Exception e) {
	        con.rollback();
	        log.write("ERROR EN JOB");
	        log.write(e);
	        e.printStackTrace();
	        throw e;
	    }
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	@Override
	public void onError(Throwable t) {
		log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();		
	}

	@Override
	public void pause() {
		
	}
	
	public static void main(String[] args) throws IOException { 
    	
    	System.out.println("== INICIO PRUEBA ==");
    	
        try {
        	Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
        	BcsJobGenerarArchivosFFMM demo = new BcsJobGenerarArchivosFFMM();
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