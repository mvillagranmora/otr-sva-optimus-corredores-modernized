package cl.bcs.normativa.jobs;
  
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.normativa.circulares.GenerarAnexo1Circular20Detalle;
import cl.bcs.normativa.circulares.GenerarAnexo1Circular20Detalle.GenerarAnexo1Circular20Detalle_Entrada;
import cl.bcs.normativa.circulares.GenerarAnexo1Circular20Detalle.GenerarAnexo1Circular20Detalle_Salida;
import cl.bcs.normativa.jobs.BcsJobGenerarAnexo1Circular20.CrearDetallesEntrada;
import cl.bcs.scheduler.BcsJob;

public class BcsJobGenerarAnexo1Circular20Detalle extends BcsJob{

	
	private static final Logger LOGGER = LogManager.getLogger(BcsJobGenerarAnexo1Circular20Detalle.class);	
	
	AtomicBoolean running=new AtomicBoolean(false); 
	
	public static class CrearDetallesEntrada {
		public Long IdEmpresa;
		public String FechaEjecucion;
		public String FechaInicioBusqueda;
		public String FechaFinBusqueda;
		public Long SecuenciaSolicitud;
		public Long IdUsuario;
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	

	private static FileLoader loader = new FileLoader();
	private static String SQL_INSERT_INFO_CIRCULAR = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarAnexo1Circular20_INS_TraspasoCuentaInversion.sql");

	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection dbcon,boolean debug) throws InterruptedException, Throwable {
    	running.set(true);
        
		final String IdTipoTarea = this.getParam("IdTipoTarea");			
		try {
			SQL sql = new SQL();
			//Se obtienen parametros de entrada del JOB
			long idEmpresa = 0L;
			String fechaSistema = "2021-01-27";
			String fechaInicio ="2020-12-01";
			String fechaFin = "2020-12-31";
			long secuenciaSolicitud = 0L;
			long idUsuario = 0L;
			String ruta = "C:/mnt/optimus/des/empresa1/nor/";
						
	        if (debug) {
	        	idEmpresa = 1L;
	        	secuenciaSolicitud = 1119L;
	        } else{
	        	idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	        	fechaSistema = String.valueOf(this.getParam("FechaSistema"));
	        	fechaInicio	= String.valueOf(this.getParam("FechaInicio"));
	        	fechaFin	= String.valueOf(this.getParam("FechaFin"));
	        	secuenciaSolicitud = Long.parseLong(this.getParam("IdSolicitudCircular"));
	        	idUsuario = Long.parseLong(this.getParam("idUsuario"));
	        	String confRutaArchivos = ConfiguracionSistema.getParametroConfiguracionString(dbcon,idEmpresa,"PLATAFORMA","PLA.RutaBaseArchivos");
				String confSubRutaRutaArchivos = ConfiguracionSistema.getParametroConfiguracionString(dbcon,idEmpresa,"NORMATIVA","SubRutaArchivosModulo");
				ruta = confRutaArchivos.concat(confSubRutaRutaArchivos);
	        }
	        
	        
	      LOGGER.info("GenerarAnexo1Circular20Detalle: Inicio del proceso");	
			
	      	//creacion registros
		    CrearDetallesEntrada pCrear = new CrearDetallesEntrada();
		    pCrear.IdEmpresa = idEmpresa;
		    pCrear.FechaEjecucion = fechaSistema;
		    pCrear.FechaInicioBusqueda = fechaInicio;
		    pCrear.FechaFinBusqueda = fechaFin;
		    pCrear.SecuenciaSolicitud = secuenciaSolicitud;
		    pCrear.IdUsuario = idUsuario;

		    LOGGER.info("GenerarAnexo1Circular20: Llenado tabla circular entre fechas "+ fechaInicio + " Y Fecha "+ fechaFin);	
			List<CrearDetallesEntrada> crear = new ArrayList<CrearDetallesEntrada>();
			crear.add(pCrear);
			sql.batch(dbcon, SQL_INSERT_INFO_CIRCULAR,crear, CrearDetallesEntrada.class);
			
			System.out.println(crear);
			

		  try{
    			
				
				LOGGER.info("GenerarAnexo1Circular20Detalle:En Ejecucion");	
				GenerarAnexo1Circular20Detalle_Entrada entrada = new GenerarAnexo1Circular20Detalle_Entrada();
    			entrada.p_IdEmpresa = idEmpresa;
    			entrada.p_IdModulo = "NOR";
    			entrada.p_IdUsuario = idUsuario;
    			entrada.p_FechaSistema = fechaSistema;
    			entrada.p_FechaInicio = fechaInicio;
    			entrada.p_FechaFin = fechaFin;
    			entrada.p_Ruta = ruta;
    			entrada.p_SecuenciaSolicitud = secuenciaSolicitud;
    			LOGGER.info("GenerarAnexo1Circular20: Llamado java que genera archivo circular");	
    			GenerarAnexo1Circular20Detalle_Salida Salida = new GenerarAnexo1Circular20Detalle_Salida();
    			GenerarAnexo1Circular20Detalle sp = new GenerarAnexo1Circular20Detalle();
    			Salida = sp.logica(dbcon,entrada);
    			
    			System.out.println(Salida.P_OUT_ERRORESCONTADOR);
    			long numRegistros = 0L;
    			String nombreArchivo = "";
    			
    			if(Salida.P_OUT_ERRORESCONTADOR == 0){
    				numRegistros = 1L;
    				nombreArchivo = Salida.P_OUT_NOMBRE_ARCHIVO;
    			}                                                        
    			
    			ActualizarSolicitud(secuenciaSolicitud, nombreArchivo, ruta, numRegistros, dbcon, getJobId());
        		
        		LOGGER.info("GenerarAnexo1Circular20: finalizado con exito");
        	}catch(Exception ex){
				System.out.println("GenerarAnexo1Circular20Detalle: EXCEPTION AL EJECUTAR: Anexo 1 Circular20Detalle");
        		LOGGER.info("Excepcion al Ejecutar GenerarAnexo1Circular20Detalle IdEmpresa: "+idEmpresa+" IdTipoTarea: " +IdTipoTarea, ex);
        		ex.printStackTrace();
        	}
        	
        	
		} catch (Exception e) {
			//logPauta(dbConnection,getJobId(),IdTipoTarea,ejecucion,confFechaSistema,"ERR","Error de Conexion");
			LOGGER.info("GenerarAnexo1Circular20Detalle(): ERROR AL OBTENER DB CONNECTION", e);
			System.out.println(e.getMessage());
		}
		running.set(false);
		return true;
	}
	
	private String ActualizarSolicitud(final long p_IdSolicitud, String p_NombreArchivo, String p_RutaArchivo,
			final long p_NumRegistros, Connection conn, long jobId) {
		
		String sqlCall = "";

		String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
		p_NombreArchivo = "'" + p_NombreArchivo + "'";
		p_RutaArchivo = "'" + p_RutaArchivo + "'";
		sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
				getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdSolicitud, p_NombreArchivo, p_RutaArchivo,
				p_NumRegistros, jobId);
		System.out.println( sqlCall);

		try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
			conn.setAutoCommit(false);

			if (cstmt.execute()) {
				conn.commit();

				try (final ResultSet rs = cstmt.getResultSet()) {
					if (rs.next()) {
						LOGGER.info("Proceso de Actualizacion de Solicitud exitoso");
						return "OK";
					} else {
						LOGGER.info("Error durante proceso de Actualizacion de Solicitud, no RS");
						return "NOK";

					} 

				} catch (Exception ex) {

					LOGGER.info("Error durante proceso de Actualizacion de Solicitud", ex);
					return "NOK";
				}

			} else {

				LOGGER.info("Error no se pudo ejecutar servicio actualizacion");
				return "NOK";
			}

		} catch (Exception ex) {

			LOGGER.info("Error durante proceso de Actualizacion de Solicitud", ex);
			return "NOK";
		}

	}
	
	private static String getQuotedString(final String name) {
        return "\"" + name + "\"";
    }
	
		
	@Override
    public void onError(Throwable t) {
        System.out.println(":GenerarAnexo1Circular20Detalle:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args){
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			BcsJobGenerarAnexo1Circular20Detalle sp = new BcsJobGenerarAnexo1Circular20Detalle();
			sp.logica(con,true);
			//con.commit();
		} catch (Throwable e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");		
	}
}