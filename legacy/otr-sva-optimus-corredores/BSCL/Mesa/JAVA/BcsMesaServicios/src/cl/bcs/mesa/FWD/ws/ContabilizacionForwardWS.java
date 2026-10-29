package cl.bcs.mesa.FWD.ws;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.AnularCentralizacion;
import cl.bcs.contabilidad.AnularCentralizacion.EntradaAnularCentralizacion;
import cl.bcs.contabilidad.AnularCentralizacion.SalidaAnularCentralizacion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.EntradaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.InTiposCentralizacion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.SalidaCentralizaMultiSeleccion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ContabilizacionForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ContabilizacionForwardWS.class);
	
	private String SQL_SELECT_CENTRALIZACION_TIPO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_SelectCentralizacionTipo.sql");
	private String SQL_SELECT_PRECOMPROBANTES = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_SelectPrecomprobantes.sql");
	private String SQL_ELIMINAR_PRECOMPROBANTES = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_EliminarPrecomprobantes.sql");
	private String SQL_SELECT_CENTRALIZACION = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/ContabilizacionForwardWS_SelectCentralizacion.sql");
	
	public static class EntradaContabilizacionForwardWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Fecha;
	}
	
	public static class SalidaContabilizacionForwardWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaContabilizacionForwardWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class CentralizacionTipo extends BcsPojo{
		public Long IdTipoCentralizacion;
		public String Nombre;
	}
	
	public static class Centralizacion extends BcsPojo {
		public Long IdCentralizacion;
	}
	
	public static class Precomprobante extends BcsPojo {
		public Long IdPreComprobante;
	}
	
	public static class AdvertenciasDetalle extends BcsPojo {
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	public static class ErrorDetalle extends BcsPojo {
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaContabilizacionForwardWS jin = gson.fromJson(in.toString(), EntradaContabilizacionForwardWS.class);
		SalidaContabilizacionForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaContabilizacionForwardWS logica(Connection con, EntradaContabilizacionForwardWS entrada) throws Exception {
		logger.info("INICIO::ContabilizacionForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaContabilizacionForwardWS out = new SalidaContabilizacionForwardWS();
		
		CentralizacionTipo centralizacionCarteraForward = obtenerCentralizacionCarteraForward(con);
		if(centralizacionCarteraForward.IdTipoCentralizacion == null) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CONTABILIZACION_FORWARD_WS_CENTRALIZACION_NO_EXISTE", new HashMap<String, String>(1), con, entrada.p_IdEmpresa));
			return out;
		}
		
		List<Long> idsPrecomprobantes = obtenerIdsPrecomprobante(con, entrada.p_IdEmpresa, centralizacionCarteraForward.IdTipoCentralizacion, entrada.p_Fecha);
		if(idsPrecomprobantes.size() > 0) {
			out = anularPrecomprobantes(con, entrada, idsPrecomprobantes);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
		}
		
		Long idCentralizacionVigente = obtenerIdCentralizacionVigente(con, entrada.p_IdEmpresa, centralizacionCarteraForward.IdTipoCentralizacion, entrada.p_Fecha);
		if(idCentralizacionVigente != null) {
			out = anularCentralizacion(con, entrada, idCentralizacionVigente);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
		}
		
		out = centraliza(con, entrada, centralizacionCarteraForward);
		agregarLog(con, entrada);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ContabilizacionForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	private CentralizacionTipo obtenerCentralizacionCarteraForward(Connection con) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.obtenerCentralizacionCarteraForward");
		
		SQL sql = new SQL();
		List<CentralizacionTipo> centralizacionesTipo = sql.query(con, SQL_SELECT_CENTRALIZACION_TIPO, new HashMap<String, Object>(1), CentralizacionTipo.class);
		CentralizacionTipo centralizacionCarteraForward = centralizacionesTipo.stream().findFirst().orElse(new CentralizacionTipo());
		
		logger.trace("FIN::ContabilizacionForwardWS.obtenerCentralizacionCarteraForward");
		return centralizacionCarteraForward;
	}
	
	private Long obtenerIdCentralizacionVigente(Connection con, Long idEmpresa, Long idCentralizacionTipoCarteraForward, String fecha) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.obtenerIdCentralizacionVigente");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("IdTipoCentralizacion", idCentralizacionTipoCarteraForward);
		parametros.put("FechaContable", fecha);
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL sql = new SQL();
		List<Centralizacion> centralizaciones = sql.query(con, SQL_SELECT_CENTRALIZACION, parametros, Centralizacion.class);
		Long idCentralizacion = centralizaciones.stream().findFirst().orElse(new Centralizacion()).IdCentralizacion;
		
		logger.trace("FIN::ContabilizacionForwardWS.obtenerIdCentralizacionVigente");
		return idCentralizacion;
	}
	
	private List<Long> obtenerIdsPrecomprobante(Connection con, Long idEmpresa, Long idCentralizacionTipoCarteraForward, String fecha) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.obtenerIdsPrecomprobante");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdTipoCentralizacion", idCentralizacionTipoCarteraForward);
		parametros.put("FechaContable", fecha);
		parametros.put("IdEmpresa", idEmpresa);
		
		SQL sql = new SQL();
		List<Precomprobante> procombrobantes = sql.query(con, SQL_SELECT_PRECOMPROBANTES, parametros, Precomprobante.class);
		List<Long> idsPrecomprobantes = procombrobantes.stream().map(procomprobante->procomprobante.IdPreComprobante).distinct().collect(Collectors.toList());
		
		logger.trace("FIN::ContabilizacionForwardWS.obtenerIdsPrecomprobante");
		return idsPrecomprobantes;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SalidaContabilizacionForwardWS anularPrecomprobantes(Connection con, EntradaContabilizacionForwardWS entrada, List<Long> idsPrecomprobantes) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.anularPrecomprobantes");
		SalidaContabilizacionForwardWS out = new SalidaContabilizacionForwardWS();
		
		for(Long idComprobante : idsPrecomprobantes) {
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(4);
			parametrosEntrada.put("IdEmpresa", entrada.p_IdEmpresa);
			parametrosEntrada.put("IdUsuario", entrada.p_IdUsuario);
			parametrosEntrada.put("IdPreComprobante", idComprobante);
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(5);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    SQL sql = new SQL();
		    HashMap<String, Object> respuestaEliminarComprobante = sql.call(con, SQL_ELIMINAR_PRECOMPROBANTES, parametrosEntrada, parametrosSalida);
		    out.P_OUT_ERRORESCONTADOR += (Long) respuestaEliminarComprobante.get("p_out_ErroresContador");
		    List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaEliminarComprobante.get("p_out_ErroresDetalle"); 
			out.P_OUT_MENSAJES.addAll(erroresDetalle
					.stream()
					.map(errorDetalle -> new MensajeSalida(errorDetalle.IdInterno, MensajeSalida.MENSAJE_ERROR, errorDetalle.Codigo, errorDetalle.Descripcion))
					.collect(Collectors.toList()));
			 List<AdvertenciasDetalle> advertenciasDetalle = (List<AdvertenciasDetalle>) respuestaEliminarComprobante.get("p_out_AdvertenciasDetalle"); 
				out.P_OUT_MENSAJES.addAll(advertenciasDetalle
						.stream()
						.map(advertenciaDetalle -> new MensajeSalida(advertenciaDetalle.IdInterno, "S".equals(advertenciaDetalle.RequiereAutorizacion) ? MensajeSalida.MENSAJE_AUTORIZACION : MensajeSalida.MENSAJE_ADVERTENCIA, advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion))
						.collect(Collectors.toList()));
		}
		
		logger.trace("FIN::ContabilizacionForwardWS.anularPrecomprobantes");
		return out;
	}
	
	private SalidaContabilizacionForwardWS anularCentralizacion(Connection con, EntradaContabilizacionForwardWS entrada, Long idCentralizacionVigente) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.anularCentralizacion");
		
		EntradaAnularCentralizacion entradaAnularCentralizacion = new EntradaAnularCentralizacion();
		entradaAnularCentralizacion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnularCentralizacion.p_IdUsuario = entrada.p_IdUsuario;
		entradaAnularCentralizacion.p_IdModulo = "MFW";
		entradaAnularCentralizacion.p_IdCentralizacion = idCentralizacionVigente;
		
		AnularCentralizacion anularCentralizacion = new AnularCentralizacion();
		SalidaAnularCentralizacion salidaAnularCentralizacion = anularCentralizacion.logica(con, entradaAnularCentralizacion);
		
		SalidaContabilizacionForwardWS out = new SalidaContabilizacionForwardWS();
		out.P_OUT_ERRORESCONTADOR = salidaAnularCentralizacion.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaAnularCentralizacion.P_OUT_MENSAJES;
		
		logger.trace("FIN::ContabilizacionForwardWS.anularCentralizacion");
		return out;
	}
	
	private SalidaContabilizacionForwardWS centraliza(Connection con, EntradaContabilizacionForwardWS entrada, CentralizacionTipo centralizacionCarteraForward) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.centraliza");
		
		EntradaCentralizaMultiSeleccion entradaCentralizaMultiSeleccion = new EntradaCentralizaMultiSeleccion();
		entradaCentralizaMultiSeleccion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCentralizaMultiSeleccion.p_IdUsuario = entrada.p_IdUsuario;
		entradaCentralizaMultiSeleccion.p_IdModulo = "MFW";
		entradaCentralizaMultiSeleccion.p_FechaContable = entrada.p_Fecha;
		entradaCentralizaMultiSeleccion.p_Ejercicio = obtenerAnio(entrada.p_Fecha);
		entradaCentralizaMultiSeleccion.p_SimulaOperacion = "N";
		
		InTiposCentralizacion tipoCentralizacion = new InTiposCentralizacion();
		tipoCentralizacion.IdTipoCentralizacion = centralizacionCarteraForward.IdTipoCentralizacion;
		entradaCentralizaMultiSeleccion.p_in_TiposCentralizacion = Arrays.asList(tipoCentralizacion);
		
		CentralizaMultiSeleccion centralizaMultiSeleccion = new CentralizaMultiSeleccion();
		SalidaCentralizaMultiSeleccion salidaCentralizaGenerico = centralizaMultiSeleccion.logica(con, entradaCentralizaMultiSeleccion);
		
		SalidaContabilizacionForwardWS out = new SalidaContabilizacionForwardWS();
		out.P_OUT_ERRORESCONTADOR = salidaCentralizaGenerico.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaCentralizaGenerico.P_OUT_MENSAJES;
		
		logger.trace("FIN::ContabilizacionForwardWS.centraliza");
		return out;
	}
		
	private Long obtenerAnio(String fecha) {
		logger.trace("INICIO::ContabilizacionForwardWS.obtenerAnio");
		
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(Fecha.toDate(fecha));
		Long anio = Long.valueOf(calendario.get(Calendar.YEAR));
		
		logger.trace("FIN::ContabilizacionForwardWS.obtenerAnio");
		return anio;
	}
	
	private void agregarLog(Connection con, EntradaContabilizacionForwardWS entrada) throws Exception {
		logger.trace("INICIO::ContabilizacionForwardWS.agregarLog");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "INGRESO";
		log.IdModulo = "CON";
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "CONTABILIDAD";
		log.EstadoRegistro = "VIG";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "VIG";
		log.NombreProceso = "Contabilizar Forward Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::ContabilizacionForwardWS.agregarLog");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"9\",\"p_Fecha\":\"2023-02-27\"}";
		EntradaContabilizacionForwardWS Objin = gson.fromJson(json, EntradaContabilizacionForwardWS.class);
		ContabilizacionForwardWS demo = new ContabilizacionForwardWS();				
		try {
			SalidaContabilizacionForwardWS ObjOut = demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}
}
