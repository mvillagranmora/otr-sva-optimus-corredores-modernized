
/**
 * Proposito: Crea un archivo csv con las solicitudes de aporte rescate. Proceso que se ejecuta Batch para simular exportacion masiva.
 *            El rango de fechas de consulta se particiona en rangos de 30 dias para no colapsar el servidor.
 */
package cl.bcs.fondomutuo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.EntradaConsultarSolicitudAporteRescateUI;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.SalidaConsultarSolicitudAporteRescateUI;
import cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.Solicitud;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.bat.AgregarLogPrograma;



public class GenerarArchivoSolicitudAporteRescate implements JavaCall {

	static final Logger logger = LogManager.getLogger(GenerarArchivoSolicitudAporteRescate.class);

	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarArchivoSolicitudAporteRescate_SelectConfiguracion.sql");

	// ----------------------------------------------------
	// CLASES DE ENTRADA SALIDA
	// ----------------------------------------------------
	public static class GenerarArchivoSolicitudAporteRescate_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;

		public String p_IdTipoMovimientoFondoMutuo;
		public String p_Estado;
		public String p_IdEmisor;
		public String p_IdSerieFondo;
		public Long p_IdCliente;
		public Long p_IdProductoAgrupador;
		public Long p_IdPortafolio;
		public String p_TipoSolicitud;
		public String p_IdMoneda;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class GenerarArchivoSolicitudAporteRescate_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_RUTAARCHIVO;

		public GenerarArchivoSolicitudAporteRescate_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// ----------------------------------------------------
	// CLASES DE PROCESO
	// ----------------------------------------------------

	public static class Configuracion {
		public String RutaBase;
		public String RutaFMM;
		public String RutaArchivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// ----------------------------------------------------
	// CALL
	// ----------------------------------------------------

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();

		GenerarArchivoSolicitudAporteRescate_Entrada in = gson.fromJson(jin.toString(),GenerarArchivoSolicitudAporteRescate_Entrada.class);
		GenerarArchivoSolicitudAporteRescate_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	// ----------------------------------------------------
	// METODOS COMPLEMENTARIOS
	// ----------------------------------------------------
	// Genera el nombre de un arhivo , con el formato:
	// <nombreArhivoSalida>_<fechaHora>.<extension>
	private String generarNombreArchivo(String nombre, String extension) {
		String nombreArchivo = "";

		try {
			Date fechaActual = new Date();
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fechaHora = formatoFecha.format(fechaActual);
			nombreArchivo = nombre + "_" + fechaHora + "." + extension;
		} catch (Exception ex) {
			nombreArchivo = "";
		}

		return nombreArchivo;
	}

	// ----------------------------------------------------
	// LOGICA DEL PROGRAMA
	// ----------------------------------------------------
	public GenerarArchivoSolicitudAporteRescate_Salida logica(Connection con,
			GenerarArchivoSolicitudAporteRescate_Entrada in) throws Exception {

		logger.info("INICIO::GenerarArchivoSolicitudAporteRescate");

		GenerarArchivoSolicitudAporteRescate_Salida out = new GenerarArchivoSolicitudAporteRescate_Salida();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		HashMap<String, String> codigo = new HashMap<String, String>();
		String rutaArchivo = "";
		String nombreArchivo = "";
		String rutaFMM = "";
		String rutaCompleta = ""; // ruta + nombre archivo
		ConsultarSolicitudAporteRescateUI servicioConsultarSolicitudAporteRescate = new ConsultarSolicitudAporteRescateUI();
		EntradaConsultarSolicitudAporteRescateUI entradaConsultarSolicitudAporteRescate = new EntradaConsultarSolicitudAporteRescateUI();
		
		// ------------------------------------------------------------------------
		// OBTIENE CONFIGURACION (nombre y ruta para guardar archivo)
		List<Configuracion> lstConfiguracion = new ArrayList<Configuracion>();
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);
		lstConfiguracion = sql.query(con, SQL_SELECT_CONFIGURACION, params, Configuracion.class);

		if (!lstConfiguracion.isEmpty()) {
			for (Configuracion conf : lstConfiguracion) {
				rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
				rutaFMM = conf.RutaFMM != null ? conf.RutaFMM : "";
			}
		}

		if (rutaArchivo.equalsIgnoreCase("") || rutaFMM.equalsIgnoreCase("")) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_RUTA_ARCHIVO", null, con, in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}

		nombreArchivo = generarNombreArchivo("SolicitudAporteRescate", "csv");
		rutaCompleta = rutaArchivo + nombreArchivo; // Ruta completa donde generar el archivo
		rutaFMM = rutaFMM + nombreArchivo; // Ruta FFMM para devolver y desde donde leer el archivo

		logger.info("NOMBRE ARCHIVO > " + nombreArchivo);
		logger.info("RUTA > " + rutaCompleta);

		
		// ---------------------------------------------------------------------------------
		// SE CREA ARCHIVO CSV CON LA PRIMERA LINEA DE ENCABEZADO

		BufferedWriter bwArchivo;
		//File archivo = new File("C:\\__ARCHIVO_PRUEBA\\" + nombreArchivo); // >> Ruta para pruebas locales
		File archivo = new File(rutaCompleta); // >> PARA SERVIDOR (cuando se libera debe ser con esta ruta)
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		String linea = "";
		
		// Linea para encabezado del archivo csv
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_CANAL", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FOLIO_CONTRATO", codigo) + ";");

		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FOLIO_CONTRATO_OPERACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TIPO_CONTRATO_OPERACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SECUENCIA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_REGISTRO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_CURSE", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_CERTIFICACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_LIQUIDACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_OPERACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TIPO_SOLICITUD", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_ESTADO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_ESTADO_LIQUIDACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FOLIO_CARTA_LIQUIDACION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_EMISOR", codigo) + ";");

		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FONDO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_NOMBRE_FONDO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_INSTRUMENTO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SERIE", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_CLIENTE", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_PORTAFOLIO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONEDA_INSTRUMENTO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_VALOR_CUOTA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_VALOR_CUOTA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_CANTIDAD", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONTO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONTO_RETENCION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONTO_COMISION", codigo) + ";");

		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONEDA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TIPO_CAMBIO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONTO_MONEDA_ORIGEN", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_COSTO_UF", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONTO_MONEDA_SISTEMA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONTO_MONEDA_SISTEMA_INICIAL", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FORMA_DE_PAGO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SPREAD", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_RENTABILIDAD_REAL", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_INSTRUMENTO_DESTINO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_EMISOR_DESTINO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SWITCH", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_PRODUCTO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SECUENCIA_TESORERIA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_ESTADO_TESORERIA_SPOT", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_LIQUIDACION_TESORERIA", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FOLIO_SPOT", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_ESTADO_SPOT", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TIPO_OPERACION_SPOT", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FOLIO_OMNIBUS", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_DIVIDENDO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FOLIO_AGRUPADOR", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REGIMEN_TRIBUTARIO", codigo) + ";");

		bwArchivo.write(linea);
		bwArchivo.newLine();
			
		
		
		// ---------------------------------------------------------------------------------
		// ACORTAR RANGO DE FECHAS DE CONSULTA PERIODOS DE 30 DIAS (para no colapsar el servidor)
				final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
				int cantidadDiasEntreDesdeHasta = 30;  //cantidad maxima de dias entre fecha desda y fecha hasta
				

				java.sql.Date sqlFechaDesde = Fecha.toDate(in.p_FechaDesde); 
				java.sql.Date sqlFechaHasta = Fecha.toDate(in.p_FechaHasta);
				java.sql.Date sqlFechaHastaOriginal = Fecha.toDate(in.p_FechaHasta);
				Boolean acortarRangoFechas = true;
				
				while (acortarRangoFechas)
				{	//Diferencia en dias entre fechas		
					sqlFechaHasta = Fecha.addDays(sqlFechaDesde, cantidadDiasEntreDesdeHasta);

					if (sqlFechaHastaOriginal.compareTo(sqlFechaHasta) > 0)
					{				
						acortarRangoFechas = true;
					}
					else
					{
						sqlFechaHasta = sqlFechaHastaOriginal;
						acortarRangoFechas = false;
					}

					
					System.out.println("COSNULTA RANGO FECHA >> FechaDesde: " + sqlFechaDesde + " - FechaHasta: " + sqlFechaHasta);
					logger.info("COSNULTA RANGO FECHA >> FechaDesde: " + sqlFechaDesde + " - FechaHasta: " + sqlFechaHasta);
					
					
			// ---------------------------------------------------------------------
			// CONSULTAR OPERACIONES SOLICITUD APORTE Y RESCATE POR RANGO ACOTADOS
			
			entradaConsultarSolicitudAporteRescate.p_IdEmpresa 				= in.p_IdEmpresa;
			entradaConsultarSolicitudAporteRescate.p_IdUsuario 				= in.p_IdUsuario;
			entradaConsultarSolicitudAporteRescate.p_IdModulo 				= in.p_IdModulo;
			entradaConsultarSolicitudAporteRescate.p_FechaAceptacionDesde 	= sqlFechaDesde.toString();
			entradaConsultarSolicitudAporteRescate.p_FechaAceptacionHasta 	= sqlFechaHasta.toString();

			entradaConsultarSolicitudAporteRescate.p_IdTipoMovimientoFondoMutuo = in.p_IdTipoMovimientoFondoMutuo;
			entradaConsultarSolicitudAporteRescate.p_Estado 				= in.p_Estado;
			entradaConsultarSolicitudAporteRescate.p_IdEmisor 				= in.p_IdEmisor;
			entradaConsultarSolicitudAporteRescate.p_IdSerieFondo 			= in.p_IdSerieFondo;
			entradaConsultarSolicitudAporteRescate.p_IdCliente 				= in.p_IdCliente;
			entradaConsultarSolicitudAporteRescate.p_IdPortafolio 			= in.p_IdPortafolio;
			entradaConsultarSolicitudAporteRescate.p_IdProductoAgrupador 	= in.p_IdProductoAgrupador; // Solo para APV
			entradaConsultarSolicitudAporteRescate.p_TipoSolicitud 			= in.p_TipoSolicitud;
			entradaConsultarSolicitudAporteRescate.p_IdMoneda 				= in.p_IdMoneda;
			entradaConsultarSolicitudAporteRescate.p_IdSolicitudAporteRescate = 0L;

			SalidaConsultarSolicitudAporteRescateUI salidaConsultarSolicitudAporteRescate = servicioConsultarSolicitudAporteRescate.logica(con, entradaConsultarSolicitudAporteRescate);

			if (salidaConsultarSolicitudAporteRescate.P_OUT_RESULTADO.size() > 0) {
				logger.info("CANTIDAD REGISTROS > " + salidaConsultarSolicitudAporteRescate.P_OUT_RESULTADO.size());
				

				for (Solicitud solicitud : salidaConsultarSolicitudAporteRescate.P_OUT_RESULTADO) {
					// Linea con data de las solicitudes de aporte rescate
					linea = "";
					linea = linea.concat(solicitud.IdTipoCanalIngreso + ";");
					linea = linea.concat(solicitud.FolioContrato + ";");

					linea = linea.concat(solicitud.FolioContratoOperacion + ";");
					linea = linea.concat(solicitud.TipoContratoOperacion + ";");
					linea = linea.concat(solicitud.IdSolicitudAporteRescate + ";");				
					linea = linea.concat(Fecha.toFormat(solicitud.FechaIngresoRegistro, "dd-MM-yyyy") + ";");
					linea = linea.concat(Fecha.toFormat(solicitud.FechaAceptacion, "dd-MM-yyyy") + ";");
					linea = linea.concat(Fecha.toFormat(solicitud.FechaCertificacion, "dd-MM-yyyy") + ";");
					linea = linea.concat(Fecha.toFormat(solicitud.FechaLiquidacion, "dd-MM-yyyy") + ";");				
					linea = linea.concat(solicitud.DescripcionTipoMovimiento + ";");
					linea = linea.concat(solicitud.DescripcionTipoSolicitud + ";");
					linea = linea.concat(solicitud.NombreEstado + ";");
					linea = linea.concat(solicitud.NombreEstadoLiquidacion + ";");
					linea = linea.concat(solicitud.FolioCartaLiquidacion + ";");
					linea = linea.concat(solicitud.IdEmisor + ";");


					linea = linea.concat(solicitud.IdFondo + ";");
					linea = linea.concat(solicitud.NombreFondoReal + ";");
					linea = linea.concat(solicitud.IdSerieFondo + ";");
					linea = linea.concat(solicitud.NombreSerieFondo + ";");
					linea = linea.concat(solicitud.DocumentoIdentidad + ";");
					linea = linea.concat(solicitud.NombreCompleto + ";");
					linea = linea.concat(solicitud.CodigoPortafolio + ";");
					linea = linea.concat(solicitud.IdMonedaFondo + ";");
					linea = linea.concat(solicitud.ValorCuota.toString().replace(".", ",") + ";");				
					linea = linea.concat(Fecha.toFormat(solicitud.FechaValorCuota, "dd-MM-yyyy") + ";");					
					linea = linea.concat((solicitud.NumeroCuotas == null ? "0" : solicitud.NumeroCuotas.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.Monto == null ? "0" : solicitud.Monto.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.MontoRetencion == null ? "0" : solicitud.MontoRetencion.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.MontoComision == null ? "0" : solicitud.MontoComision.toString().replace(".", ","))	+ ";");

					linea = linea.concat(solicitud.IdMoneda + ";");
					linea = linea.concat((solicitud.ParidadFechaInversion == null ? "0"	: solicitud.ParidadFechaInversion.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.CostoMonedaLocal == null ? "0" : solicitud.CostoMonedaLocal.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.CostoUF == null ? "0" : solicitud.CostoUF.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.MontoMonedaSistema == null ? "0" : solicitud.MontoMonedaSistema.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.MontoMonedaSistemaInicial == null ? "0"	: solicitud.MontoMonedaSistemaInicial.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.TextoPreferenciaPago == null ? "" : solicitud.TextoPreferenciaPago) + ";");
					linea = linea.concat((solicitud.Spread == null ? "0" : solicitud.Spread.toString().replace(".", ",")) + ";");
					linea = linea.concat((solicitud.RentabilidadReal == null ? "0" : solicitud.RentabilidadReal.toString().replace(".", ",")) + ";");
					linea = linea.concat(solicitud.IdSerieFondoDestino + ";");
					linea = linea.concat(solicitud.IdEmisorDestino + ";");
					linea = linea.concat((solicitud.SwitchUI == null ? "" : Diccionario.getMensaje(con, in.p_IdEmpresa, solicitud.SwitchUI, codigo)) + ";");
					linea = linea.concat((solicitud.Producto == null ? "" : solicitud.Producto) + ";");
					linea = linea.concat((solicitud.IdIngresoEgreso == null ? "0" : solicitud.IdIngresoEgreso) + ";");
					linea = linea.concat((solicitud.NombreEstadoTesoreria == null ? "" : solicitud.NombreEstadoTesoreria) + ";");
					linea = linea.concat((solicitud.FechaLiquidacionTesoreria == null ? "" : solicitud.FechaLiquidacionTesoreria) + ";");
					linea = linea.concat((solicitud.IdOperacionSpot == null ? "0" : solicitud.IdOperacionSpot) + ";");
					linea = linea.concat(solicitud.NombreEstadoSpot + ";");
					linea = linea.concat(solicitud.NombreTipoOperacionSpot + ";");
					linea = linea.concat((solicitud.IdOmnibus == null ? "0" : solicitud.IdOmnibus) + ";");
					linea = linea.concat((solicitud.IdDividendo == null ? "0" : solicitud.IdDividendo) + ";");
					linea = linea.concat((solicitud.IdAgrupador == null ? "0" : solicitud.IdAgrupador) + ";");
					linea = linea.concat(solicitud.DescripcionRegimenTributario);

					bwArchivo.write(linea);
					bwArchivo.newLine();
				}
			} 

        
			//Avanza 1 dia para el siguiente rango de fechas
		    sqlFechaDesde = Fecha.addDays(sqlFechaHasta, 1);

		}
		bwArchivo.flush();
		bwArchivo.close();

		
		out.P_OUT_RUTAARCHIVO = rutaFMM; // Ej FMM/SolicitudAporteRescate_20200421_135102.csv
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("SALIDA > " + out);

		logger.info("FIN::GenerarArchivoSolicitudAporteRescate");

		return out;
	}

	// -------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"FMM\",\"p_FechaDesde\":\"2020-08-21\",\"p_FechaHasta\":\"2020-08-21\"}";

			GenerarArchivoSolicitudAporteRescate_Entrada Objin = gson.fromJson(json,
					GenerarArchivoSolicitudAporteRescate_Entrada.class);
			GenerarArchivoSolicitudAporteRescate demo = new GenerarArchivoSolicitudAporteRescate();
			System.out.println("ENTRADA:" + Objin.toString());

			try {
				GenerarArchivoSolicitudAporteRescate_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
				// con.rollback();
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				// con.commit();
			}

			con.close();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
