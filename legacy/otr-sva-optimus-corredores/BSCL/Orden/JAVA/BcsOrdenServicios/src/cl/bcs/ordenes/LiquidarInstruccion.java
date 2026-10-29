package cl.bcs.ordenes;

import cl.bcs.ordenes.pojo.Validacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LiquidarInstruccion implements JavaCall { 
	
	static final Logger logger = LogManager.getLogger(AnularTransaccionMasiva.class);

	private final String SQL_UPDATE_INSTRUCCIONES = loadResourceToString("cl/bcs/ordenes/sql/LiquidarInstruccion_UpdateEstadoInstruccion.sql");
	private final List<String> LIBRES_DE_PAGO = Arrays.asList("ENTIRV","RECIRV","ENTIRF","RECIRF","ENTIFI","RECIFI");
	
	public static class EntradaLiquidarInstruccion extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		
		public String p_ComentarioLiquidacion;
		public List<Long> p_IdsInstrucciones;
		
		public EntradaLiquidarInstruccion() {
			p_ComentarioLiquidacion = "";
			p_IdsInstrucciones = new ArrayList<>();
		}
	}
	
	public static class SalidaLiquidarInstruccion extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public SalidaLiquidarInstruccion() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}

		private void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
			}
			P_OUT_MENSAJES.add(validacion.Mensaje);
		}

		public void agregarValidaciones(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacion(validacion);
			}
		}
	}
	
	public static class InstruccionProceso extends BcsPojo {
		public Long IdInstruccion;
		public Long IdCliente;

		public Long IdPortafolioCliente;
		public String IdTipoOperacion;
		public String IdTipoOperacionInstruccion;
		public String Nemotecnico;
		public String IdMonedaInstrumento;
		public String IdMonedaLiquidacion;
		public BigDecimal Cantidad;
		public BigDecimal TirTasa;
		public BigDecimal MontoNeto;
		public String IdEstadoPreLiquidacion;
		public String IdEstadoPostLiquidacion;
		public java.sql.Date FechaModificacion;
	}
	
	public static class ResultadoObtenerInstrucciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<InstruccionProceso> p_out_Instrucciones;

		public ResultadoObtenerInstrucciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Instrucciones = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaLiquidarInstruccion entrada = gson.fromJson(in, EntradaLiquidarInstruccion.class);
		SalidaLiquidarInstruccion salida = logica(conn, entrada);
		JsonParser jsonParser = new JsonParser();

		return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
	}
	
	private SalidaLiquidarInstruccion logica(Connection conn, EntradaLiquidarInstruccion entrada) throws Exception {
		logger.info("INICIO::LiquidarInstruccion");
		long inicio = System.currentTimeMillis();

		java.sql.Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		SalidaLiquidarInstruccion salida = new SalidaLiquidarInstruccion();

		List<Long> idsInstrucciones = entrada.p_IdsInstrucciones.stream().distinct().collect(Collectors.toList());
		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = obtenerInstrucciones(conn, entrada, idsInstrucciones, fechaHoraSistema);
		salida.P_OUT_ERRORESCONTADOR += resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoObtenerInstrucciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		List<Validacion> validacionesInstrucciones = validarInstrucciones(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones);
		salida.agregarValidaciones(validacionesInstrucciones);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		LiquidarInstrucciones(conn, resultadoObtenerInstrucciones.p_out_Instrucciones);
		agregarBitacoras(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones);
		agregarLogs(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones, fechaHoraSistema);

		long fin = System.currentTimeMillis();
		logger.info("FIN::LiquidarInstruccion {}", fin - inicio);

		return salida;
	}
	
	private ResultadoObtenerInstrucciones obtenerInstrucciones(Connection conn, EntradaLiquidarInstruccion entrada, List<Long> idsInstrucciones, java.sql.Timestamp fechaHoraSistema) throws Exception {
		logger.debug("INICIO::LiquidarInstruccion.obtenerInstrucciones");
		long inicio = System.currentTimeMillis();

		ConsultarInstruccionMasivo.EntradaConsultarInstruccionMasivo entradaConsultarInstruccionMasivo = new ConsultarInstruccionMasivo.EntradaConsultarInstruccionMasivo();
		entradaConsultarInstruccionMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarInstruccionMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarInstruccionMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarInstruccionMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConsultarInstruccionMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaConsultarInstruccionMasivo.p_IdsInstrucciones = idsInstrucciones;

		ConsultarInstruccionMasivo consultarInstruccionMasivo = new ConsultarInstruccionMasivo();
		ConsultarInstruccionMasivo.SalidaConsultarInstruccionMasivo salidaConsultarInstruccionMasivo = consultarInstruccionMasivo.logica(conn, entradaConsultarInstruccionMasivo);

		java.sql.Date fechaSistemaDate = Fecha.toDate(Fecha.toStringDate(fechaHoraSistema));
		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = new ResultadoObtenerInstrucciones();
		resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR += salidaConsultarInstruccionMasivo.P_OUT_ERRORESCONTADOR;
		resultadoObtenerInstrucciones.P_OUT_MENSAJES.addAll(salidaConsultarInstruccionMasivo.P_OUT_MENSAJES);
		resultadoObtenerInstrucciones.p_out_Instrucciones = salidaConsultarInstruccionMasivo.p_out_Instrucciones.stream().map(ins -> {
			InstruccionProceso instruccionProceso = new InstruccionProceso();
			instruccionProceso.IdInstruccion = ins.IdInstruccion;
			instruccionProceso.IdEstadoPreLiquidacion = ins.IdEstadoInstruccion;
			instruccionProceso.IdEstadoPostLiquidacion = "LIQ";
			instruccionProceso.IdTipoOperacionInstruccion = ins.IdTipoOperacionInstruccion;
			instruccionProceso.FechaModificacion = fechaSistemaDate;

			return instruccionProceso;
		}).collect(Collectors.toList());

		if (resultadoObtenerInstrucciones.p_out_Instrucciones.isEmpty()) {
			resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR++;
			resultadoObtenerInstrucciones.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_HAY_INSTRUCCIONES_POR_LIQUIDAR", new HashMap<>(), conn, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarInstruccion.obtenerInstrucciones {}", fin - inicio);

		return resultadoObtenerInstrucciones;
	}
	
	private List<Validacion> validarInstrucciones(Connection conn, EntradaLiquidarInstruccion entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::LiquidarInstruccion.validarInstrucciones");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();
		HashMap<String, String> mensajeParams = new HashMap<>();

		for (InstruccionProceso instruccion : instrucciones) {
			boolean estadoDistintoConfirmada = !"CNF".equals(instruccion.IdEstadoPreLiquidacion);
			boolean libresDePago = (LIBRES_DE_PAGO.contains(instruccion.IdTipoOperacionInstruccion));
			if (estadoDistintoConfirmada) {
				if(!libresDePago) {
					Validacion validacion = new Validacion();
					validacion.EsError = true;
					validacion.Mensaje = new PlataformaUtils.MensajeSalida(instruccion.IdInstruccion, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_SOLO_PERMITIDO_ANULAR_INSTRUCCION_CONFIRMADA", mensajeParams, conn, entrada.p_IdEmpresa);
					validaciones.add(validacion);					
				}
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarInstruccion.validarInstrucciones {}", fin - inicio);

		return validaciones;
	}
	
	private void LiquidarInstrucciones(Connection conn, List<InstruccionProceso> instrucciones) throws SQLException {
		logger.debug("INICIO::LiquidarInstruccion.LiquidarInstrucciones");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_INSTRUCCIONES, instrucciones, InstruccionProceso.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarInstruccion.LiquidarInstrucciones {}", fin - inicio);
	}
	
	private void agregarBitacoras(Connection conn, EntradaLiquidarInstruccion entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::LiquidarInstruccion.agregarBitacoras");
		long inicio = System.currentTimeMillis();

		AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo entradaAgregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo();
		entradaAgregarEventoBitacoraMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarEventoBitacoraMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarEventoBitacoraMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaAgregarEventoBitacoraMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaAgregarEventoBitacoraMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaAgregarEventoBitacoraMasivo.p_eventos = instrucciones.stream().map(instruccion -> {
			AgregarEventoBitacoraMasivo.EventoBitacora eventoBitacora = new AgregarEventoBitacoraMasivo.EventoBitacora();
			eventoBitacora.IdInterno = instruccion.IdInstruccion;
			eventoBitacora.NombreEntidad = "INSTRUCCION";
			eventoBitacora.IdEntidad = instruccion.IdInstruccion;
			eventoBitacora.IdOrden = 0L;
			eventoBitacora.IdAsignacion = 0L;
			eventoBitacora.IdTransaccion = 0L;
			eventoBitacora.MensajeEventoBitacora = entrada.p_ComentarioLiquidacion;
			eventoBitacora.IdTipoEventoBitacora = "INSTRUCCION_LIQUIDADA";

			return eventoBitacora;
		}).collect(Collectors.toList());

		AgregarEventoBitacoraMasivo agregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo();
		agregarEventoBitacoraMasivo.logica(conn, entradaAgregarEventoBitacoraMasivo);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarInstruccion.agregarBitacoras {}", fin - inicio);
	}
	
	private void agregarLogs(Connection conn, EntradaLiquidarInstruccion entrada, List<InstruccionProceso> instrucciones, java.sql.Timestamp fechaHoraSistema) throws Exception {
		logger.debug("INICIO::LiquidarInstruccion.agregarLogs");
		long inicio = System.currentTimeMillis();

		long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
		String fechaSistema = Fecha.toStringDate(fechaHoraSistema);

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = idTrabajo;

		for (InstruccionProceso instruccion : instrucciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = instruccion.IdInstruccion;
			log.IdTipoLog = "LIQUIDAR";
			log.IdModulo = "ORD";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = instruccion.IdCliente;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = instruccion.IdPortafolioCliente;
			log.IdTipoRegistro = "LIQUIDAR INSTRUCCION";
			log.IdTipoOperacion = instruccion.IdTipoOperacion;
			log.Folio = instruccion.IdInstruccion;
			log.Cantidad = instruccion.Cantidad;
			log.PrecioTasa = instruccion.TirTasa;
			log.Monto = instruccion.MontoNeto;
			log.Nemo = instruccion.Nemotecnico;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";
			log.IdMonedaOrigen = instruccion.IdMonedaInstrumento;
			log.IdMonedaLiquidacion = instruccion.IdMonedaLiquidacion;
			log.TipoCambio = "";
			log.IdPersona = 0L;

			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog estadoInstruccionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			estadoInstruccionDetalleLog.IdTrabajo = idTrabajo;
			estadoInstruccionDetalleLog.IdCabeceraLog = instruccion.IdInstruccion;
			estadoInstruccionDetalleLog.SubRegistro = "Instruccion";
			estadoInstruccionDetalleLog.CampoRegistro = "IdEstadoInstruccion";
			estadoInstruccionDetalleLog.ValorAntiguo = instruccion.IdEstadoPreLiquidacion;
			estadoInstruccionDetalleLog.ValorNuevo = instruccion.IdEstadoPostLiquidacion;

			entradaAgregarLog.p_LogDetalle.add(estadoInstruccionDetalleLog);
		}

		AgregarLog agregarLog = new AgregarLog();
		agregarLog.logica(conn, entradaAgregarLog);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarInstruccion.agregarLogs {}", fin - inicio);
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_ComentarioLiquidacion\":\"PORQUE QUIERO\",\"p_IdsInstrucciones\":[158]}";
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaLiquidarInstruccion entrada = gson.fromJson(jsonRequest, EntradaLiquidarInstruccion.class);
			System.out.println("ENTRADA");
			System.out.println(entrada);

			try {
				LiquidarInstruccion liquidarInstruccion = new LiquidarInstruccion();
				SalidaLiquidarInstruccion salida = liquidarInstruccion.logica(conn, entrada);
				System.out.println("SALIDA");
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
	
		
			
}

	

