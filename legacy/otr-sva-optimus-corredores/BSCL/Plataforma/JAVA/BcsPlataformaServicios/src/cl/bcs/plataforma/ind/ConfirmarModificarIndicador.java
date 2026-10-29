package cl.bcs.plataforma.ind;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarIndicador implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarIndicador.class);
	
	private final String SQL_SELECT_SOLICITUD_CAMBIO_VALOR_INDICADOR = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_SelectSolicitudCambioValorIndicador.sql");
	private final String SQL_SELECT_SOLICITUD_CAMBIO_FECHA_VENCIMIENTO = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_SelectSolicitudCambioFechaVencimiento.sql");
	private final String SQL_SELECT_INDICADOR_CLIENTE_NG380 = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_SelectIndicadorClienteNG380.sql");
	private final String SQL_SELECT_INDICADOR_CLIENTE_RELCORR = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_SelectIndicadorClienteRELCORR.sql");
	private final String SQL_SELECT_ID_PORTAFOLIO_APV = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_SelectIdPortafolioAPV.sql");
	private final String SQL_UPDATE_INDICADOR_VALOR_INDICADOR = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_UpdateIndicadorValorIndicador.sql");
	private final String SQL_UPDATE_INDICADOR_FECHA_VENCIMIENTO = loadResourceToString("cl/bcs/plataforma/ind/sql/ConfirmarModificarIndicador_UpdateIndicadorFechaVencimiento.sql");
	
	public static class EntradaConfirmarModificarIndicador extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdSolicitudCambio;
		public String p_IdEstadoAutorizacion;
		public String p_IdTipoEntidad;
		public Long p_IdEntidad;
	}
	
	public static class SalidaConfirmarModificarIndicador extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConfirmarModificarIndicador() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SolicitudCambio extends BcsPojo{
		public Long IdSolicitudCambio;
		public String ValorNuevo;
		public String ValorAntiguo;
		public String IdEntidadOrigen;
		public String CampoModificado;
		public String NombreTipoIndicador;
	}
	
	public static class Indicador extends BcsPojo{
		public Long IdIndicador;
		public String ValorIndicador;
		public String FechaVencimiento;
		public String IdEstadoAutorizacion;
	}
	
	public static class Portafolio extends BcsPojo{
		public Long IdPortafolio;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarModificarIndicador jin = gson.fromJson(in.toString(), EntradaConfirmarModificarIndicador.class);
		SalidaConfirmarModificarIndicador out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarModificarIndicador logica(Connection con, EntradaConfirmarModificarIndicador entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarIndicador");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarModificarIndicador out = new SalidaConfirmarModificarIndicador();
		
		List<SolicitudCambio> solicitudesCambioValorIndicador = obtenerSolicitudCambioValorIndicador(con, entrada.p_IdSolicitudCambio);
		List<SolicitudCambio> solicitudesCambioFechaVencimiento = obtenerSolicitudCambioFechaVencimiento(con, entrada.p_IdSolicitudCambio);
		
		actualizarIndicadorValorIndicador(con, solicitudesCambioValorIndicador, entrada.p_IdEstadoAutorizacion);
		actualizarIndicadorFechaVencimiento(con, solicitudesCambioFechaVencimiento, entrada.p_IdEstadoAutorizacion);
		
		replicarIndicadorNormativoCliente(con, entrada.p_IdSolicitudCambio, entrada.p_IdEstadoAutorizacion);
		
		SalidaReplicaPortafolioAPV salidaReplicaPortafolioAPV = replicarPortafolioAPV(con, entrada);
		out.P_OUT_ERRORESCONTADOR += salidaReplicaPortafolioAPV.P_OUT_ERRORESCONTADOR;
		
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio(con, entrada);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		agregarLog(con, entrada, solicitudesCambioValorIndicador, solicitudesCambioFechaVencimiento);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarIndicador", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudCambioValorIndicador(Connection con, Long idSolicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.obtenerSolicitudCambioValorIndicador");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdSolicitudCambio", idSolicitudCambio);
		List<SolicitudCambio> solicitudesCambio = sql.query(con, SQL_SELECT_SOLICITUD_CAMBIO_VALOR_INDICADOR, parametros, SolicitudCambio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.obtenerSolicitudCambioValorIndicador", fin - inicio);
		return solicitudesCambio;
	}
	
	private List<SolicitudCambio> obtenerSolicitudCambioFechaVencimiento(Connection con, Long idSolicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.obtenerSolicitudCambioFechaVencimiento");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdSolicitudCambio", idSolicitudCambio);
		List<SolicitudCambio> solicitudesCambio = sql.query(con, SQL_SELECT_SOLICITUD_CAMBIO_FECHA_VENCIMIENTO, parametros, SolicitudCambio.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.obtenerSolicitudCambioFechaVencimiento", fin - inicio);
		return solicitudesCambio;
	}
	
	private void actualizarIndicadorValorIndicador(Connection con, List<SolicitudCambio> solicitudesCambio, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.actualizarIndicadorValorIndicador");
		long inicio = System.currentTimeMillis();
		List<Indicador> indicadores = solicitudesCambio.parallelStream().map(solicitud->{
			Indicador indicador = new Indicador();
			indicador.IdIndicador = Long.valueOf(solicitud.IdEntidadOrigen);
			indicador.ValorIndicador = solicitud.ValorNuevo;
			indicador.IdEstadoAutorizacion = idEstadoAutorizacion;
			return indicador;
		}).collect(Collectors.toList());
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_INDICADOR_VALOR_INDICADOR, indicadores, Indicador.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.actualizarIndicadorValorIndicador", fin - inicio);
	}
	
	private void actualizarIndicadorFechaVencimiento(Connection con, List<SolicitudCambio> solicitudesCambio, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.actualizarIndicadorFechaVencimiento");
		long inicio = System.currentTimeMillis();
		List<Indicador> indicadores = solicitudesCambio.parallelStream().map(solicitud->{
			Indicador indicador = new Indicador();
			indicador.IdIndicador = Long.valueOf(solicitud.IdEntidadOrigen);
			indicador.FechaVencimiento = solicitud.ValorNuevo;
			indicador.IdEstadoAutorizacion = idEstadoAutorizacion;
			return indicador;
		}).collect(Collectors.toList());
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_INDICADOR_FECHA_VENCIMIENTO, indicadores, Indicador.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.actualizarIndicadorFechaVencimiento", fin - inicio);
	}
	
	private void replicarIndicadorNormativoCliente(Connection con, Long idSolicitudCambio, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.replicarIndicadorNormativoCliente");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdSolicitudCambio", idSolicitudCambio);
		parametros.put("IdEstadoAutorizacion", idEstadoAutorizacion);
		SQL sql = new SQL();
		List<Indicador> indicadoresNG380 = sql.query(con, SQL_SELECT_INDICADOR_CLIENTE_NG380, parametros, Indicador.class);
		List<Indicador> indicadoresRELCORR = sql.query(con, SQL_SELECT_INDICADOR_CLIENTE_RELCORR, parametros, Indicador.class);
		List<Indicador> indicadores = new ArrayList<Indicador>();
		indicadores.addAll(indicadoresNG380);
		indicadores.addAll(indicadoresRELCORR);
		sql.batch(con, SQL_UPDATE_INDICADOR_VALOR_INDICADOR, indicadores, Indicador.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.replicarIndicadorNormativoCliente", fin - inicio);
	}
	
	private SalidaReplicaPortafolioAPV replicarPortafolioAPV(Connection con, EntradaConfirmarModificarIndicador entrada) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.replicarPortafolioAPV");
		long inicio = System.currentTimeMillis();
		SalidaReplicaPortafolioAPV salidaReplicaPortafolioAPV = new SalidaReplicaPortafolioAPV();
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdSolicitudCambio", entrada.p_IdSolicitudCambio);
		SQL sql = new SQL();
		List<Portafolio> portafoliosAPV = sql.query(con, SQL_SELECT_ID_PORTAFOLIO_APV, parametros, Portafolio.class);
		Portafolio portafolioAPV = portafoliosAPV.parallelStream().findFirst().orElse(null);
		if(portafolioAPV != null) {
			ReplicaPortafolioAPV replicaPortafolioAPV = new ReplicaPortafolioAPV();
			EntradaReplicaPortafolioAPV entradaReplicaPortafolioAPV = new EntradaReplicaPortafolioAPV();
			entradaReplicaPortafolioAPV.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaReplicaPortafolioAPV.p_IdUsuario = entrada.p_IdUsuario;
			entradaReplicaPortafolioAPV.p_IdModulo = entrada.p_IdModulo;
			entradaReplicaPortafolioAPV.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaReplicaPortafolioAPV.p_Portafolio = portafolioAPV.IdPortafolio;
			entradaReplicaPortafolioAPV.p_IdEntidad = entrada.p_IdSolicitudCambio;
			entradaReplicaPortafolioAPV.p_TipoEntidad = "INDICADORES";
			entradaReplicaPortafolioAPV.p_TipoOperacion = "U";
			salidaReplicaPortafolioAPV = replicaPortafolioAPV.logica(con, entradaReplicaPortafolioAPV);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.replicarPortafolioAPV", fin - inicio);
		return salidaReplicaPortafolioAPV;
	}
	
	private SalidaModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarModificarIndicador entrada) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		ModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio = new ModificarEstadoRegistroSolicitudCambio();
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificarEstadoRegistroSolicitudCambio = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdSolicitudCambio = entrada.p_IdSolicitudCambio;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio.logica(con, entradaModificarEstadoRegistroSolicitudCambio);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
	}
	
	private void agregarLog(Connection con, EntradaConfirmarModificarIndicador entrada, List<SolicitudCambio> solicitudesCambioValorIndicador, List<SolicitudCambio> solicitudesCambioFechaVencimiento) throws Exception {
		logger.debug("INICIO::ConfirmarModificarIndicador.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogValorIndicador = solicitudesCambioValorIndicador.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdCabeceraLog = Long.valueOf(solicitud.IdEntidadOrigen);
			detalle.SubRegistro = solicitud.IdEntidadOrigen;
			detalle.CampoRegistro = solicitud.CampoModificado + "|" + solicitud.NombreTipoIndicador;
			detalle.CampoRegistro = obtenerCampoRegistroTruncado(detalle.CampoRegistro);
			detalle.ValorAntiguo = solicitud.ValorAntiguo;
			detalle.ValorNuevo = solicitud.ValorNuevo;
			return detalle;
		}).collect(Collectors.toList());
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogFechaVencimiento = solicitudesCambioFechaVencimiento.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdCabeceraLog = Long.valueOf(solicitud.IdEntidadOrigen);
			detalle.SubRegistro = solicitud.IdEntidadOrigen;
			detalle.CampoRegistro = solicitud.CampoModificado + "|" + solicitud.NombreTipoIndicador;
			detalle.CampoRegistro = obtenerCampoRegistroTruncado(detalle.CampoRegistro);
			detalle.ValorAntiguo = solicitud.ValorAntiguo;
			detalle.ValorNuevo = solicitud.ValorNuevo;
			return detalle;
		}).collect(Collectors.toList());
		
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogValorIndicador);
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogFechaVencimiento);
		
		entradaAgregarLog.p_Log = solicitudesCambioValorIndicador.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = Long.valueOf(solicitud.IdEntidadOrigen);
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
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
			log.IdTipoRegistro = solicitud.IdEntidadOrigen;
			log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
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
			log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
			log.NombreProceso = "Confirmar Cambio valor Indicador";		
			log.IdRegistro = solicitud.IdEntidadOrigen;
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarIndicador.agregarLog", fin - inicio);
	}
	
	private String obtenerCampoRegistroTruncado(String campoRegistro) {
		logger.trace("INICIO::ConfirmarModificarIndicador.obtenerCampoRegistroTruncado");
		long inicio = System.currentTimeMillis();
		campoRegistro = campoRegistro.length() > 40 ? campoRegistro.substring(0, 40) : campoRegistro;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarModificarIndicador.obtenerCampoRegistroTruncado", fin - inicio);
		return campoRegistro;
	}
	
}
