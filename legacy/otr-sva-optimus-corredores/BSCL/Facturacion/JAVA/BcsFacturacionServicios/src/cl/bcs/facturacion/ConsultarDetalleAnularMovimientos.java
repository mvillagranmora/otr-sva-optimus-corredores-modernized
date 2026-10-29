package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarDetalleAnularMovimientos implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarDetalleAnularMovimientos.class);

	public final String SQL_SELECT_DETALLE_MOVIMIENTOS_ANULAR = loadResourceToString("cl/bcs/facturacion/sql/ConsultarDetalleAnularMovimientos_SelectMovimientosAnular.sql");
	
	public static class ConsultarDetalleAnularMovimientos_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_EsIngresoAutomatico = "N";
		public String p_Glosa;
		public String p_Fecha;
		public String p_IdClasificacionTipoInstrumento ;
		public String p_Operacion ;
		public Long p_IdLibroOrdenes;
		public Long p_SecuenciaOrden;
		public String p_HoraDesde;
		public String p_HoraHasta;
		public String p_IdTipoInstrumento;
		public String p_IdCondicionDeLiquidacion;
		public String p_Bolsa;
		public String p_FolioTransaccion ;
		public Long p_IdInstrumento;
		public String p_IdTipoCanalIngreso ;
		public String p_MarcaAsignacion;
		public Long p_IdPortafolio;
		public String p_FiltraPorAsignacionMasiva;

		public List<AsignacionMasiva> p_IdAsignacionMasiva;
	}
	
	public static class AsignacionMasiva {
		public Long p_IdAsignacionMasiva;
	}
	
	public static class DetalleMovimientoAnular  {
		
		public Long 		SecuenciaMovimiento;
		public String 		IdClasificacionTipoInstrumento;
		public String		TipoComprobante;
		public String		Concepto;
		public String		Estado;
		public String		FolioTransaccion;
		public String 		FechaTransaccion;
		public Long			SecuenciaOrden;
		public String		Cliente;
		public String		CodigoPortafolio;
		public String		Operacion;
		public String		TipoOperacion;
		public String		Nemotecnico;
		public String		Emisor;
		public BigDecimal	Cantidad;
		public String		CondicionLiquidacion;
		public String		MarcaAsignacion;
		public Long			IdAsignacionMasiva;
		public String		IdMoneda;
		public String		IdTipoInstrumento;
		public Long			SecuenciaAsignacion;
	}
	
	public static class ConsultarDetalleAnularMovimientos_Salida {
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<DetalleMovimientoAnular> P_OUT_DETALLE_MOVIMIENTOS;
		
		public ConsultarDetalleAnularMovimientos_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_DETALLE_MOVIMIENTOS = new ArrayList<DetalleMovimientoAnular>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarDetalleAnularMovimientos_Entrada in = gson.fromJson(jin.toString(), ConsultarDetalleAnularMovimientos_Entrada.class);
		ConsultarDetalleAnularMovimientos_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public void ValidarDatosEntrada(Connection conn,ConsultarDetalleAnularMovimientos_Entrada entrada,ConsultarDetalleAnularMovimientos_Salida salida) throws Exception {
		
		if(entrada.p_FiltraPorAsignacionMasiva == null
		        || entrada.p_FiltraPorAsignacionMasiva.trim().isEmpty()
		        || (!"N".equals(entrada.p_FiltraPorAsignacionMasiva)
		            && !"S".equals(entrada.p_FiltraPorAsignacionMasiva))) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
					"UI_PARAMETRO_FILTRA_POR_ASIGNACION_MASIVA_INVALIDO", null, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
		}
		
		if("S".equals(entrada.p_FiltraPorAsignacionMasiva) && (entrada.p_IdAsignacionMasiva == null || entrada.p_IdAsignacionMasiva.size() == 0)) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
					"UI_NO_SE_INGRESO_ID_ASIGNACION_MASIVA", null, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
		}
		
		if("N".equals(entrada.p_FiltraPorAsignacionMasiva) && (entrada.p_Fecha == null || "".equals(entrada.p_Fecha))) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
					"UI_FECHA_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR", null, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
		}

		if("N".equals(entrada.p_FiltraPorAsignacionMasiva) && (entrada.p_HoraHasta == null || "".equals(entrada.p_HoraHasta) || entrada.p_HoraDesde == null || "".equals(entrada.p_HoraDesde))) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
					"UI_HORA_HASTA_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR", null, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
		}
	}
	
	@Trace
	public ConsultarDetalleAnularMovimientos_Salida logica(Connection conn, ConsultarDetalleAnularMovimientos_Entrada in) throws Exception {
		
		logger.info("INICIO::ConsultarDetalleAnularMovimientos");
		long inicio = System.currentTimeMillis();
		
		ConsultarDetalleAnularMovimientos_Salida salida = new ConsultarDetalleAnularMovimientos_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		this.ValidarDatosEntrada(conn, in, salida);
		
		if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
			return salida;
		}
		
		Long localOffset = ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
		
		LocalDate fecha;
		LocalTime horaDesde ;
		LocalTime horaHasta ;
		LocalDateTime fechaDesde = null;
		LocalDateTime fechaHasta = null;
		
		if("N".equals(in.p_FiltraPorAsignacionMasiva)) {
			try {
				fecha = LocalDate.parse(in.p_Fecha);
			} catch(Exception e) {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FORMATO_FECHA", new HashMap<String,String>(0), conn, in.p_IdEmpresa));
				return salida;
			}
		
			try {
				horaDesde = LocalTime.parse(in.p_HoraDesde);
				horaHasta = LocalTime.parse(in.p_HoraHasta);
			} catch(Exception e) {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FORMATO_HORA", new HashMap<String,String>(0), conn, in.p_IdEmpresa));
				return salida;
			}

			fechaDesde = LocalDateTime.of(fecha, horaDesde).plusSeconds(localOffset * -1);
			fechaHasta = LocalDateTime.of(fecha, horaHasta).plusSeconds(localOffset * -1);
		}
		
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FiltraPorAsignacionMasiva", in.p_FiltraPorAsignacionMasiva);
		params.put("IdAsignacionMasiva", in.p_IdAsignacionMasiva.stream().map(x -> x.p_IdAsignacionMasiva).collect(Collectors.toList()));
		params.put("FechaDesde", fechaDesde);
		params.put("FechaHasta", fechaHasta);
		params.put("IdClasificacionTipoInstrumento", in.p_IdClasificacionTipoInstrumento);
		params.put("Operacion", in.p_Operacion);
		params.put("IdLibroOrdenes", in.p_IdLibroOrdenes);
		params.put("SecuenciaOrden", in.p_SecuenciaOrden);
		params.put("IdTipoInstrumento", in.p_IdTipoInstrumento);
		params.put("IdCondicionDeLiquidacion", in.p_IdCondicionDeLiquidacion);
		params.put("CodigoBolsa", in.p_Bolsa);
		params.put("FolioTransaccion", in.p_FolioTransaccion);
		params.put("IdInstrumento", in.p_IdInstrumento);
		params.put("IdTipoCanalIngreso", in.p_IdTipoCanalIngreso);
		params.put("IdPortafolio", in.p_IdPortafolio);
		params.put("MarcaAsignacion", in.p_MarcaAsignacion);
		
		List<DetalleMovimientoAnular> movimientosPorAnular = sql.query(conn, SQL_SELECT_DETALLE_MOVIMIENTOS_ANULAR, params, DetalleMovimientoAnular.class);
		
		if (movimientosPorAnular == null) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
					"UI_ERROR_CONSULTAR_DETALLE_MOVIMIENTOS_ANULAR_VACIO", new HashMap<String,String>(0), conn, in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		
		salida.P_OUT_DETALLE_MOVIMIENTOS.addAll(movimientosPorAnular);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarDetalleAnularMovimientos: {}", fin - inicio);
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties");
		
		Gson gson = Utils.getGsonBuilder();
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9652,\"p_IdModulo\":\"FAC\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_Glosa\":\"\",\"p_IdAsignacionMasiva\":[],\"p_FiltraPorAsignacionMasiva\":\"N\",\"p_Fecha\":\"2026-08-27\",\"p_HoraDesde\":\"00:00:00\",\"p_HoraHasta\":\"11:54:16\"}\r\n"
				+ "";
		
		ConsultarDetalleAnularMovimientos_Entrada Objin = gson.fromJson(json, ConsultarDetalleAnularMovimientos_Entrada.class);
		ConsultarDetalleAnularMovimientos servicio = new ConsultarDetalleAnularMovimientos();
		
		try {
			ConsultarDetalleAnularMovimientos_Salida ObjOut = servicio.logica(conn, Objin);
			System.out.println(ObjOut.toString());
		} catch(Exception er) {
			er.printStackTrace();
			conn.rollback();
		}
		
		System.out.println("== END ==");
	}
	
}