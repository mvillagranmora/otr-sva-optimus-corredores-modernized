package cl.bcs.custodia;

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

public class GenerarReporteCuadratura implements JavaCall {
	
	private final Log log = new Log("CUS.GenerarReporteCuadratura");
	
	private final String SQL_SELECT_REPORTE = loadResourceToString("cl/bcs/custodia/sql/GenerarReporteCuadratura_SelectTemplateCuadraturaCustodia.sql");
	private final String SQL_CALL_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/GenerarReporteCuadratura_CallCuadraturaCustodia.sql");
	private final String SQL_INSERT_CIRCULAR = loadResourceToString("cl/bcs/custodia/sql/GenerarReporteCuadratura_InsertCircularCuadraturaCustodia.sql");
	
	/********************************************************************
	 * 								CLASES								*
	 ********************************************************************/
	
	public static class Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaCuadratura;
		public String p_IdBoveda;
		public DataSource p_Datasource;
		public String p_Ruta;
		public Long p_IdPrograma;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Reporte{
		public Long   CompanyId;
		public String Id;
		public String ReportName;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida{
		public Long   P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormatoArchivoDetalle{
		public Long    IdFormatoArchivoDetalle;
		public Integer TipoRegistro;	
		public String  Campo;	
		public String  TipoDato;	
		public Integer Largo;	
		public Integer Decimales;	
		public String  ValorDefecto;	
		public Integer Orden;	
		public String  Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudCircular{
		public String IdTipoCircular;
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
	 * 								LOGICA								*
	 ********************************************************************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		
		SQL sql=new SQL();	
		Salida salida= new Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_MENSAJE = "";
		CallableStatement spexe = null;
		try{
			
			if (!new File(entrada.p_Ruta).exists()){
        		File theDir = new File(entrada.p_Ruta);
        		theDir.mkdir();
        	}

			List<Reporte> infoReporte = sql.query(con, SQL_SELECT_REPORTE, new HashMap<String,Object>(), Reporte.class);
			Reporte datosReporte = infoReporte.get(0);
			
			ReportDocument report = new ReportDocument(Utils.getSchema(),entrada.p_Datasource);			
			
			if(!report.readDocument(datosReporte.Id)){
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJE = "Reporte no existe";
				log.write(salida);
				return salida;
			}
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			List<SolicitudCircular> solicitudesCircular = new ArrayList<SolicitudCircular>();
			SolicitudCircular solicitudCircular = new SolicitudCircular();
			solicitudCircular.IdTipoCircular = "CUADRACUSTODIA";
			solicitudCircular.FechaIngresoSolicitud = fechaSistema;
			solicitudCircular.FechaVencimientoSolicitud = fechaSistema;
			solicitudCircular.IdUsuario = entrada.p_IdUsuario;
			solicitudCircular.FechaInicio =  entrada.p_FechaCuadratura;
			solicitudCircular.FechaFin =  entrada.p_FechaCuadratura;
			solicitudCircular.IdProceso = entrada.p_IdPrograma;
			try{
				spexe = con.prepareCall(SQL_CALL_CUADRATURA_CUSTODIA);
				spexe.setString(1, entrada.p_FechaCuadratura);
				spexe.setString(2, entrada.p_IdBoveda);
				boolean isResultSet =spexe.execute();
				
				if(isResultSet){
					ResultSet rsSP = spexe.getResultSet();
					log.write("INFORME EJECUCION: Servicio Crystal Resulset  ->  " + isResultSet);
					report.setDocumentResultSet(rsSP);
					log.write("INFORME EJECUCION: modifica Resulset en crystal");
					log.write("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");
					try(InputStream inStreamRpt = report.exportDocument("excel")){
						
						String NombreArchivo = infoReporte.get(0).ReportName+"_"+entrada.p_FechaCuadratura+"_"+".xls";
						
						FileUtils.copyInputStreamToFile(inStreamRpt, new File(entrada.p_Ruta + "nor/", NombreArchivo));
						IOUtils.closeQuietly(inStreamRpt);
						solicitudCircular.RutaArchivo = entrada.p_Ruta+ "nor/" + NombreArchivo;
						solicitudCircular.NombreArchivo = NombreArchivo;
						
						int size = 0;
						//Si hay una manera mas elegante de contar favor cambiar
						isResultSet =spexe.execute();
						if(isResultSet){
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
			}catch(Exception e){
				log.write("Error",e);
			}finally{
				if(spexe != null){
					spexe.close();
				}
			}
		}
		catch (Exception e){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = "Error al generar reporte de cuadratura";
			log.write("Error",e);			
		}
		
		return salida;
		
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	
}
