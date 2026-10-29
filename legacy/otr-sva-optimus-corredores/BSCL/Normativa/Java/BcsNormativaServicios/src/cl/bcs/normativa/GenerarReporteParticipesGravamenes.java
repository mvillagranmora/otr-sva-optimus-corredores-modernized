package cl.bcs.normativa;

import java.io.File;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarReporteParticipesGravamenes implements JavaCall {

    private final Log log = new Log("NOR.GenerarReporteParticipesGravamenes");

    private final String SQL_SELECT_REPORTE_PARTICIPES = loadResourceToString("cl/bcs/normativa/sql/GenerarReporteParticipesGravamenes_SelectTemplateParticipes.sql");
    private final String SQL_CALL_REPORTE_PARTICIPES = loadResourceToString("cl/bcs/normativa/sql/GenerarReporteParticipesGravamenes_CallParticipesReport.sql");
    private final String SQL_SELECT_REPORTE_GRAVAMENES = loadResourceToString("cl/bcs/normativa/sql/GenerarReporteParticipesGravamenes_SelectTemplateGravamenes.sql");
    private final String SQL_CALL_REPORTE_GRAVAMENES = loadResourceToString("cl/bcs/normativa/sql/GenerarReporteParticipesGravamenes_CallGravamenesReport.sql");
    private final String SQL_INSERT_CIRCULAR = loadResourceToString("cl/bcs/normativa/sql/GenerarReporteParticipesGravamenes_InsertCircularParticipesGravamenes.sql");

    /********************************************************************
     * CLASES *
     ********************************************************************/

    public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_registroDate;
		public String p_IdFondo;
		public String p_IdSerie;
		public Long p_IdCliente;
		public DataSource p_Datasource;
		public String p_Ruta;
		public Long p_IdPrograma;
		public String reportType;
	
		public String toString() {
		    return Utils.printJson(this, false);
		}
    }

    public static class Reporte {
		public Long CompanyId;
		public String Id;
		public String ReportName;
	
		public String toString() {
		    return Utils.printJson(this, false);
		}
    }

    public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	
		public String toString() {
		    return Utils.printJson(this, false);
		}
    }

    public static class SolicitudCircular {
		public String IdTipoCircular;
		public String IdTipoDocumento;
		public String FechaIngresoSolicitud;
		public String FechaVencimientoSolicitud;
		public Long IdUsuario;
		public String FechaInicio;
		public String FechaFin;
		public String RutaArchivo;
		public String NombreArchivo;
		public String NumeroRegistros;
		public Long IdProceso;
	
		public String toString() {
		    return Utils.printJson(this, false);
		}
    }

    /********************************************************************
     * LOGICA *
     ********************************************************************/
    public Salida logica(Connection con, Entrada entrada) throws Exception {
		SQL sql = new SQL();
		Salida salida = new Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_MENSAJE = "";
		CallableStatement spexe = null;
	
		try {
		    if(!new File(entrada.p_Ruta).exists()){
				File theDir = new File(entrada.p_Ruta);
				theDir.mkdir();
		    }
	
		    if("NGC368PART".equals(entrada.reportType)){
				List<Reporte> infoReporte = sql.query(con, SQL_SELECT_REPORTE_PARTICIPES, new HashMap<String, Object>(),
					Reporte.class);
				Reporte datosReporte = infoReporte.get(0);
		
				ReportDocument report = new ReportDocument(Utils.getSchema(), entrada.p_Datasource);
		
				if(!report.readDocument(datosReporte.Id)){
				    salida.P_OUT_ERRORESCONTADOR = 1L;
				    salida.P_OUT_MENSAJE = "Reporte no existe";
				    log.write(salida);
				    return salida;
				}
		
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
					"PLATAFORMA", "PLA.FechaSistema");
				List<SolicitudCircular> solicitudesCircular = new ArrayList<SolicitudCircular>();
				SolicitudCircular solicitudCircular = new SolicitudCircular();
				solicitudCircular.IdTipoCircular = "NCG368";
				solicitudCircular.IdTipoDocumento = "NGC368PART";
				solicitudCircular.FechaIngresoSolicitud = fechaSistema;
				solicitudCircular.FechaVencimientoSolicitud = fechaSistema;
				solicitudCircular.IdUsuario = entrada.p_IdUsuario;
				solicitudCircular.IdProceso = entrada.p_IdPrograma;
		
				try {
				    spexe = con.prepareCall(SQL_CALL_REPORTE_PARTICIPES);
				    spexe.setLong(1, entrada.p_IdEmpresa);
				    spexe.setString(2, entrada.p_registroDate);
				    spexe.setString(3, entrada.p_IdFondo);
				    spexe.setString(4, entrada.p_IdSerie);
				    spexe.setLong(5, entrada.p_IdCliente);
				    boolean isResultSet = spexe.execute();
		
				    if(isResultSet){
						ResultSet rsSP = spexe.getResultSet();
						log.write("INFORME EJECUCION: Servicio Crystal Resulset  ->  " + isResultSet);
						report.setDocumentResultSet(rsSP);
						log.write("INFORME EJECUCION: modifica Resulset en crystal");
						log.write("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");
			
						try(InputStream inStreamRpt = report.exportDocument("pdf")){
						    String NombreArchivo = infoReporte.get(0).ReportName + "_" + entrada.p_registroDate + ".pdf";
						    FileUtils.copyInputStreamToFile(inStreamRpt,
							    new File(entrada.p_Ruta + "nor/", NombreArchivo));
						    IOUtils.closeQuietly(inStreamRpt);
						    solicitudCircular.RutaArchivo = entrada.p_Ruta + "nor/" + NombreArchivo;
						    solicitudCircular.NombreArchivo = NombreArchivo;
			
						    int size = 0;
						    // Si hay una manera mas elegante de contar favor cambiar
						    isResultSet = spexe.execute();
						    if (isResultSet) {
								rsSP = spexe.getResultSet();
								while (rsSP.next()) {
								    ++size;
								}
						    }
						    log.write("REGISTROS ->  " + size);
						    solicitudCircular.NumeroRegistros = String.valueOf(size);
						    log.write("INFORME EJECUCION: ARCHIVO CREADO ->  " + solicitudCircular.RutaArchivo);
						    log.write("**  ARCHIVO CREADO :  " + solicitudCircular.RutaArchivo);
						    solicitudesCircular.add(solicitudCircular);
						    sql.batch(con, SQL_INSERT_CIRCULAR, solicitudesCircular, SolicitudCircular.class);
						}
				    }
				} catch(Exception e){
				    log.write("Error", e);
				} finally{
				    if(spexe != null){
					spexe.close();
				    }
				}
		    } else{
				List<Reporte> infoReporte = sql.query(con, SQL_SELECT_REPORTE_GRAVAMENES, new HashMap<String, Object>(),
						Reporte.class);
				Reporte datosReporte = infoReporte.get(0);

				ReportDocument report = new ReportDocument(Utils.getSchema(), entrada.p_Datasource);

				if (!report.readDocument(datosReporte.Id)) {
					salida.P_OUT_ERRORESCONTADOR = 1L;
					salida.P_OUT_MENSAJE = "Reporte no existe";
					log.write(salida);
					return salida;
				}

				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
						"PLATAFORMA", "PLA.FechaSistema");
				List<SolicitudCircular> solicitudesCircular = new ArrayList<SolicitudCircular>();
				SolicitudCircular solicitudCircular = new SolicitudCircular();
				solicitudCircular.IdTipoCircular = "NCG368";
				solicitudCircular.IdTipoDocumento = "NGC368GRAV";
				solicitudCircular.FechaIngresoSolicitud = fechaSistema;
				solicitudCircular.FechaVencimientoSolicitud = fechaSistema;
				solicitudCircular.IdUsuario = entrada.p_IdUsuario;
				solicitudCircular.FechaInicio = entrada.p_registroDate;
				solicitudCircular.FechaFin = entrada.p_registroDate;
				solicitudCircular.IdProceso = entrada.p_IdPrograma;

				try {
					spexe = con.prepareCall(SQL_CALL_REPORTE_GRAVAMENES);
					spexe.setLong(1, entrada.p_IdEmpresa);
					spexe.setString(2, entrada.p_registroDate);
					spexe.setString(3, entrada.p_IdFondo);
					spexe.setString(4, entrada.p_IdSerie);
					spexe.setLong(5, entrada.p_IdCliente);
					boolean isResultSet = spexe.execute();

					if (isResultSet) {
						ResultSet rsSP = spexe.getResultSet();
						log.write("INFORME EJECUCION: Servicio Crystal Resulset  ->  " + isResultSet);
						report.setDocumentResultSet(rsSP);
						log.write("INFORME EJECUCION: modifica Resulset en crystal");
						log.write("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");

						try (InputStream inStreamRpt = report.exportDocument("pdf")) {
							String NombreArchivo = infoReporte.get(0).ReportName + "_" + entrada.p_registroDate
									+ ".pdf";
							FileUtils.copyInputStreamToFile(inStreamRpt,
									new File(entrada.p_Ruta + "nor/", NombreArchivo));
							IOUtils.closeQuietly(inStreamRpt);
							solicitudCircular.RutaArchivo = entrada.p_Ruta + "nor/" + NombreArchivo;
							solicitudCircular.NombreArchivo = NombreArchivo;

							int size = 0;
							// Si hay una manera mas elegante de contar favor cambiar
							isResultSet = spexe.execute();
							if (isResultSet) {
								rsSP = spexe.getResultSet();
								while (rsSP.next()) {
									++size;
								}
							}
							log.write("REGISTROS ->  " + size);
							solicitudCircular.NumeroRegistros = String.valueOf(size);
							log.write("INFORME EJECUCION: ARCHIVO CREADO ->  " + solicitudCircular.RutaArchivo);
							log.write("**  ARCHIVO CREADO :  " + solicitudCircular.RutaArchivo);
							solicitudesCircular.add(solicitudCircular);
							sql.batch(con, SQL_INSERT_CIRCULAR, solicitudesCircular, SolicitudCircular.class);
						}
					}
				} catch (Exception e) {
					log.write("Error", e);
				} finally {
					if (spexe != null) {
						spexe.close();
					}
				}
		    }
		} catch (Exception e){
		    salida.P_OUT_ERRORESCONTADOR = 1L;
		    salida.P_OUT_MENSAJE = "Error al generar: " + entrada.reportType;
		    log.write("Error", e);
		}	
		return salida;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
    }

}
