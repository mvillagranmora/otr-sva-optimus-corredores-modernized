package cl.bcs.mesa.SPT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarReporteMensualOperaciones  implements JavaCall{

	private final String SQL_SELECT_OPERACIONES_MENSUALES = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultaReporteMensualOperaciones_SelectOperaciones.sql");
	
	private final static Logger logger = LogManager.getLogger(GenerarReporteMensualOperaciones.class);
	
	private static final String CARPETA_DEFAULT = "SPT";
	private static final String NOMBRE_ARCHIVO = "RMO";
	private static final String CODIGO_INSTITUCION_SURA = "125";
	private static final String SECTOR_ECONOMICO_BENEFICIARIO_DEFAULT = "81";
	private static final String SECTOR_ECONOMICO_INVERSIONISTA_DEFAULT = "81";
	private static final String NOMBRE_FINANCISTA_EXTERNO_DEFAULT = "xxxxxx";
	private static final List<Long> CODIGOS_COMERCIO_SURA = Arrays.asList(new Long[]{11260l, 21260l});
			
	public static class GenerarReporteMensualOperaciones_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Ruta;
	
		public String p_Periodo;
		public String p_FormatoArchivo;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class GenerarReporteMensualOperaciones_Salida{
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String               P_OUT_NOMBRE_ARCHIVO_LOG;
		public String				P_OUT_RUTA_ARCHIVO_LOG; 
		public String 				P_OUT_COMENTARIO;
		
		public GenerarReporteMensualOperaciones_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionMensual{				
		public String EventoDeReporte;
		public String CodigoInstitucion;
		public Long NumeroOperacion;
		public String FechaOperacion;
		public Integer TipoDocumento;
		public Long CodOpeCambios;	
		public String IdPais;
		public Integer MonedaOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoDolar;	  
		public BigDecimal TipoCambio;
		public BigDecimal MontoEquiClp;
		public Integer AfectoADerivado;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String RazonSocial;
		public String TipoAutorizacion;
		public String NumeroAutorizacion;
		public String FechaAutorizacion;
		public String SectorEconomicoBeneficiario;
		public String NumeroInscripcion;
		public String FechaDesembolso;
		public String SectorEconomicoInversionista;
		public String NombreFinancistaExtranjero;
		public String FechaVencimientoCapital;
		public String NroContDerivado;
		public String FechaSuscDerivado;
		public String FechaVencDerivado;
		public String Observacion;
		public Integer InformaReg20;
		public Integer InformaReg21;
		public Integer InformaReg60;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarReporteMensualOperaciones_Entrada in = gson.fromJson(jin.toString(), GenerarReporteMensualOperaciones_Entrada.class);
		GenerarReporteMensualOperaciones_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public GenerarReporteMensualOperaciones_Salida logica(Connection conn, GenerarReporteMensualOperaciones_Entrada entrada) throws Exception{
		logger.info("Inicio de servicio GenerarReporteMensualOperaciones");
		String mensajeGeneracionArchivo = "ARCHIVO_GENERADO_EXITOSAMENTE";
		GenerarReporteMensualOperaciones_Salida salida = new GenerarReporteMensualOperaciones_Salida();
		try {
		SQL _sql = new SQL();

		String fechaPeriodo = entrada.p_Periodo;
		Date pFecha = new SimpleDateFormat("yyyy-MM").parse(fechaPeriodo.toString());	
		SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMM"); 
		String periodo = formatoFecha.format(pFecha);
		
		HashMap<String,Object> params =  new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("Periodo", periodo);
		
		logger.debug("INICIO - Select Obtener informacion operaciones");
		List<OperacionMensual> operaciones = _sql.query(conn, SQL_SELECT_OPERACIONES_MENSUALES,params, OperacionMensual.class);
		logger.debug("FIN - Select Obtener informacion operaciones"); 
		
		operaciones = operaciones.stream().map(operacion -> {
			if (operacion.InformaReg20 == 1 && CODIGO_INSTITUCION_SURA.equals(operacion.CodigoInstitucion) && CODIGOS_COMERCIO_SURA.contains(operacion.CodOpeCambios)) {
				operacion.SectorEconomicoBeneficiario = SECTOR_ECONOMICO_BENEFICIARIO_DEFAULT;
				operacion.SectorEconomicoInversionista = SECTOR_ECONOMICO_INVERSIONISTA_DEFAULT;
				operacion.NombreFinancistaExtranjero = NOMBRE_FINANCISTA_EXTERNO_DEFAULT;
			}
			return operacion;
		}).collect(Collectors.toList());
		
		String nombreArchivo = NOMBRE_ARCHIVO;
		salida.P_OUT_NOMBRE_ARCHIVO = nombreArchivo + periodo + "." + entrada.p_FormatoArchivo;	
		salida.P_OUT_RUTA_ARCHIVO = CARPETA_DEFAULT + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		salida.P_OUT_NOMBRE_ARCHIVO_LOG = nombreArchivo + periodo + ".log";
		salida.P_OUT_RUTA_ARCHIVO_LOG = CARPETA_DEFAULT + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		String rutaCompletaArchivo = entrada.p_Ruta + salida.P_OUT_RUTA_ARCHIVO;
		String rutaCompletaArchivoLog = entrada.p_Ruta + salida.P_OUT_RUTA_ARCHIVO_LOG;
		
		if (!generarArchivo(rutaCompletaArchivo,rutaCompletaArchivoLog, operaciones, periodo, entrada.p_FormatoArchivo)) {
			mensajeGeneracionArchivo = "ERROR_ARCHIVO_GENERADO";
		}
		
		salida.P_OUT_COMENTARIO =  Diccionario.getMensaje(conn, entrada.p_IdEmpresa, mensajeGeneracionArchivo).msg();
		
		logger.info("Fin de servicio GenerarReporteMensualOperaciones");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	
	public boolean generarArchivo(String rutaArchivo, String rutaArchivoLog, List<OperacionMensual> operaciones,  String periodo, String formatoArchivo){	
		List<String[]> lineasCSVOperaciones = new ArrayList<String[]>();
			
		try (FileOutputStream fos = new FileOutputStream(rutaArchivo); 
			OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			
			CSVWriter csvWriter = new CSVWriter(osw, ';', CSVWriter.NO_QUOTE_CHARACTER);

			if(operaciones.size() > 0) {
				for (OperacionMensual operacion : operaciones) {					
					String[] linea = {
					String.format("%1s", operacion.EventoDeReporte).trim(),
					String.format("%3s", operacion.CodigoInstitucion),				
					String.format("%12s", operacion.NumeroOperacion).trim(),
					String.format("%8s", operacion.FechaOperacion).replace("-", ""),
					String.format("%1s", operacion.TipoDocumento).trim(),
					String.format("%5s", operacion.CodOpeCambios).trim(),
					String.format("%3s", operacion.IdPais).trim(),				
					String.format("%3s", operacion.MonedaOperacion).trim(),						
					String.format("%23s", String.format("%.2f",operacion.MontoOperacion).replace(",", ".")).trim(),
					String.format("%23s", String.format("%.2f",operacion.MontoDolar).replace(",", ".")).trim(),
					String.format("%13s", String.format("%.2f",operacion.TipoCambio).replace(",", ".")).trim(),
					String.format("%23s", String.format("%.2f",operacion.MontoEquiClp).replace(",", ".")).trim(),
					String.format("%1s", operacion.AfectoADerivado).trim(),
					String.format("%10s", operacion.DocumentoIdentidad).replace("-", "").trim(),
					String.format("%100s", operacion.NombreCompleto).trim(),
					String.format("%2s", operacion.TipoAutorizacion).trim(),
					String.format("%6s", operacion.NumeroAutorizacion).trim(),
					String.format("%8s", operacion.FechaAutorizacion).trim(),
					String.format("%2s", operacion.SectorEconomicoBeneficiario).trim(),
					String.format("%8s", operacion.NumeroInscripcion).trim(),
					String.format("%8s", operacion.FechaDesembolso).trim(),
					String.format("%2s", operacion.SectorEconomicoInversionista).trim(),
					String.format("%100s", operacion.NombreFinancistaExtranjero).trim(),
					String.format("%8s", operacion.FechaVencimientoCapital).trim(),
					String.format("%52s", operacion.NroContDerivado).trim().trim(),
					String.format("%8s", operacion.FechaSuscDerivado).replace("-", "").trim(),
					String.format("%8s", operacion.FechaVencDerivado).replace("-", "").trim(),
					String.format("%250s", operacion.Observacion).replace("\n", " ").trim()
					};
					
					lineasCSVOperaciones.add(linea);
				}
			}
			
			csvWriter.writeAll(lineasCSVOperaciones);
			csvWriter.close();
			
			logValidacionCamposPorCodigoComercio(operaciones, rutaArchivoLog);
				
			return true;    
		}catch(Exception e) {
			logger.error("Error en la creacion de Reporte Mensual de Operaciones");
			logger.error(e.getMessage());
			return false;
		}
	}
	
	public void logValidacionCamposPorCodigoComercio(List<OperacionMensual> operaciones, String rutaArchivoLog){		
		File archivoLog = new File(rutaArchivoLog);
		
		String camposReg20y21 = "19-20-21-22-23-24"; 
		String camposReg50 = "25-26"; //Afecto a Derivados
		String camposReg60 = "28"; 
		String foliosPlanillaRegistro20y21 = "";
		String foliosPlanillaRegistro50 = "";
		String foliosPlanillaRegistro60 = "";
		
		try (BufferedWriter bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog))) {
			for(OperacionMensual operacion: operaciones) {
				if (operacion.InformaReg20 == 1 || operacion.InformaReg21 == 1) {
					if(operacion.SectorEconomicoBeneficiario.trim().isEmpty() || operacion.NumeroInscripcion.trim().isEmpty()
						|| operacion.FechaDesembolso.trim().isEmpty() || operacion.SectorEconomicoInversionista.trim().isEmpty()
						|| operacion.NombreFinancistaExtranjero.trim().isEmpty() || operacion.FechaVencimientoCapital.trim().isEmpty()) {
						
						String guion = foliosPlanillaRegistro20y21.isEmpty() ? "" : "-";
						foliosPlanillaRegistro20y21 += guion + operacion.NumeroOperacion.toString();
					}
				}	
				
				if(operacion.AfectoADerivado == 1) {
					if(operacion.NroContDerivado.trim().isEmpty() || operacion.FechaSuscDerivado.trim().isEmpty()) {
						String guion = foliosPlanillaRegistro50.isEmpty() ? "" : "-";
						foliosPlanillaRegistro50+= guion + operacion.NumeroOperacion.toString();
					}				
				}
				
				if(operacion.InformaReg60 == 1) {
					if(operacion.Observacion.trim().isEmpty()) {
						String guion = foliosPlanillaRegistro60.isEmpty() ? "" : "-";
						foliosPlanillaRegistro60+= guion + operacion.NumeroOperacion.toString();
					}
				}
			}
			
			if(foliosPlanillaRegistro20y21.equals("") && foliosPlanillaRegistro50.equals("") && foliosPlanillaRegistro60.equals("")) {
				bwArchivoLog.write("No requiere información adicional que completar asociada a los códigos de comercio de las operaciones.");
				bwArchivoLog.newLine();
			}else {
				bwArchivoLog.write("Información:");
				bwArchivoLog.newLine();
				
				if(!foliosPlanillaRegistro20y21.equals("")) {
					bwArchivoLog.write("De acuerdo al campo código operación de cambio ingresado debe informar los campos: " + camposReg20y21 + ", a los folio planilla: ");
					bwArchivoLog.newLine();
					bwArchivoLog.write(foliosPlanillaRegistro20y21);
					bwArchivoLog.newLine();
				}
				
				if(!foliosPlanillaRegistro50.equals("")) {
					bwArchivoLog.write("De acuerdo al campo código operación de cambio ingresado debe informar los campos: " + camposReg50 + ", a los folio planilla: ");
					bwArchivoLog.newLine();
					bwArchivoLog.write(foliosPlanillaRegistro50);
					bwArchivoLog.newLine();
				}
				
				if(!foliosPlanillaRegistro60.equals("")) {
					bwArchivoLog.write("De acuerdo al campo código operación de cambio ingresado debe informar el campo: " + camposReg60 + ", a los folio planilla: ");
					bwArchivoLog.newLine();
					bwArchivoLog.write(foliosPlanillaRegistro60);
					bwArchivoLog.newLine();
				}
			}
			
		} catch (IOException e) {
			logger.error("Error en la creacion de archivo log para Reporte Mensual de Operaciones");
			logger.error(e.getMessage());
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			System.out.println("============INICIO============");
			Gson gson = Utils.getGsonBuilder();
	
			String json = "{\"p_IdEmpresa\":1,\"p_Ruta\":\"\",\"p_IdUsuario\":0,\"p_IdModulo\":\"MSP\",\"p_Periodo\":\"2023-03-29\",\"p_FormatoArchivo\":\"CSV\"}";
			GenerarReporteMensualOperaciones_Entrada Objin = gson.fromJson(json, GenerarReporteMensualOperaciones_Entrada.class);
			GenerarReporteMensualOperaciones demo = new GenerarReporteMensualOperaciones();
			try 
			{
				GenerarReporteMensualOperaciones_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_COMENTARIO);
				
				System.out.println("============FIN============");
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
		
}


