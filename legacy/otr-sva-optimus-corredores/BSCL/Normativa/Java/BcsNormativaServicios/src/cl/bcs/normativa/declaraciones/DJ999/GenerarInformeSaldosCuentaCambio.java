package cl.bcs.normativa.declaraciones.DJ999;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

public class GenerarInformeSaldosCuentaCambio implements JavaCall{

	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/normativa/declaraciones/DJ999/sql/InformeSaldosCuentaCambio_SelectMovimientos.sql");
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeSaldosCuentaCambio.class);
	
	private static final String CARPETA_DEFAULT = "SPT";
	private static final String NOMBRE_ARCHIVO = "RSC";
	public final String ERROR_SIN_INFORMACION = "No hay informacion para el periodo seleccionado.";
	
	public static class GenerarInformeSaldosCuentaCambio_Entrada{
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
		
	public static class GenerarInformeSaldosCuentaCambio_Salida{
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public GenerarInformeSaldosCuentaCambio_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionMensual{				
		public String RutInstitucion;
		public Date FechaPosicion;
		public Long Concepto;
		public Long Moneda;
		public BigDecimal Paridad;
		public BigDecimal MontoDolar;	
		public BigDecimal MontoMonedaPrincipal;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarInformeSaldosCuentaCambio_Entrada in = gson.fromJson(jin.toString(), GenerarInformeSaldosCuentaCambio_Entrada.class);
		GenerarInformeSaldosCuentaCambio_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public GenerarInformeSaldosCuentaCambio_Salida logica(Connection conn, GenerarInformeSaldosCuentaCambio_Entrada entrada) throws Exception{
		logger.info("Inicio de servicio GenerarInformeSaldosCuentaCambio");
		String mensajeGeneracionArchivo = "ARCHIVO_GENERADO_EXITOSAMENTE";
		GenerarInformeSaldosCuentaCambio_Salida salida = new GenerarInformeSaldosCuentaCambio_Salida();
		
		String periodo = entrada.p_Periodo.replace("/", "");
				
		String nombreArchivo = NOMBRE_ARCHIVO;
		salida.P_OUT_NOMBRE_ARCHIVO = nombreArchivo + periodo + "." + entrada.p_FormatoArchivo;	
		salida.P_OUT_RUTA_ARCHIVO ="";
		
		try {
			SQL _sql = new SQL();

			HashMap<String, Object> params = new HashMap<>();

			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("Periodo", entrada.p_Periodo);

			logger.debug("INICIO - Select Obtener informacion operaciones");
			List<OperacionMensual> operaciones = _sql.query(conn, SQL_SELECT_MOVIMIENTOS, params, OperacionMensual.class);
			logger.debug("FIN - Select Obtener informacion operaciones");

			if (operaciones.size() == 0) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_COMENTARIO = ERROR_SIN_INFORMACION;
				return salida;

			}

			salida.P_OUT_RUTA_ARCHIVO = CARPETA_DEFAULT + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
			String rutaCompletaArchivo = entrada.p_Ruta + salida.P_OUT_RUTA_ARCHIVO;

			if (!generarArchivo(rutaCompletaArchivo, operaciones, entrada.p_Periodo, entrada.p_FormatoArchivo)) {
				mensajeGeneracionArchivo = "ERROR_ARCHIVO_GENERADO";
			}

			salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, mensajeGeneracionArchivo).msg();

			logger.info("Fin de servicio GenerarInformeSaldosCuentaCambio");

		} catch (SQLException e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "ERROR_ARCHIVO_GENERADO").msg();
		} catch (NullPointerException e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "VALOR_NULO").msg();
		} catch (Exception e) {
			salida.P_OUT_COMENTARIO = e.getMessage();
			salida.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
		}
		return salida;
	}
	
	public boolean generarArchivo(String rutaArchivo, List<OperacionMensual> operaciones,  String periodo, String formatoArchivo){	
		List<String[]> lineasCSVOperaciones = new ArrayList<String[]>();
		
		try (FileOutputStream fos = new FileOutputStream(rutaArchivo); 
			OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			
			CSVWriter csvWriter = new CSVWriter(osw, ';', CSVWriter.NO_QUOTE_CHARACTER);

			if (operaciones.size() > 0) {
				for (OperacionMensual operacion : operaciones) {

					if (operacion.Concepto == 1) {
						String[] linea = {

								String.format("%10s", operacion.RutInstitucion).replace("-", "").trim(),
								String.format("%8s",  operacion.FechaPosicion).replace("-", ""),
								String.format("%1s",  operacion.Concepto).trim(),
								String.format("%3s",  operacion.Moneda).trim(),
								String.format("%10s", String.format("%.2f", operacion.Paridad).replace(",", ".")).trim(),
								String.format("%20s", String.format("%.2f", operacion.MontoMonedaPrincipal).replace(",", ".")).trim(),
								String.format("%20s", String.format("%.2f", operacion.MontoDolar).replace(",", ".")).trim()
						};

						lineasCSVOperaciones.add(linea);
					} else {
						String[] linea = {

								String.format("%10s", operacion.RutInstitucion).replace("-", "").trim(),
								String.format("%8s",  operacion.FechaPosicion).replace("-", ""),
								String.format("%1s",  operacion.Concepto).trim(), 
								String.format("%3s", "").trim(),
								String.format("%10s", "").trim(), 
								String.format("%20s", "").trim(),
								String.format("%20s", String.format("%.2f", operacion.MontoDolar).replace(",", ".")).trim()
						};

						lineasCSVOperaciones.add(linea);
					}

				}
			}
			
			csvWriter.writeAll(lineasCSVOperaciones);
			csvWriter.close();
				
			return true;    
		}catch(Exception e) {
			logger.error("Error en la creacion de Informe de Saldos de Cuenta Cambio");
			logger.error(e.getMessage());
			return false;
		}
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			System.out.println("============INICIO DEBUG GENERACION INFORME SALDOS============");
			Gson gson = Utils.getGsonBuilder();
	
			String json = "{\"p_IdEmpresa\":1,\"p_Ruta\":\"\",\"p_IdUsuario\":0,\"p_IdModulo\":\"MSP\",\"p_Periodo\":\"2022-10-12\",\"p_FormatoArchivo\":\"CSV\"}";
			GenerarInformeSaldosCuentaCambio_Entrada Objin = gson.fromJson(json, GenerarInformeSaldosCuentaCambio_Entrada.class);
			GenerarInformeSaldosCuentaCambio demo = new GenerarInformeSaldosCuentaCambio();
			try 
			{
				GenerarInformeSaldosCuentaCambio_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_COMENTARIO);
				
				System.out.println("============INICIO DEBUG GENERACION INFORME SALDOS============");
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
		
}


