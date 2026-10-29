package cl.bcs.socionegocio.por;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.SocioNegocioUtils.Portafolio;

public class CrearPreferenciaPagoPortafolioMasivo implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearPreferenciaPagoPortafolioMasivo.class);
	
	private String SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/por/sql/CrearPreferenciaPagoPortafolioMasivo_InsertPreferenciaPagoPortafolioMasivo.sql");
	private final String ID_CONCEPTO = "NCPPAG";
    private final String PROPOSITO_APV = "PROPOSITOAPV";
    private final String ID_TIPO_ENTIDAD_REPLICA = "PREFERENCIASDEPAGO";
    private final String ID_TIPO_OPERACION = "I";

	public static class EntradaCrearPreferenciaPagoPortafolioMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<PreferenciaPagoPortafolio> p_PreferenciasPagoPortafolio;

		public EntradaCrearPreferenciaPagoPortafolioMasivo() {
			this.p_PreferenciasPagoPortafolio = new ArrayList<PreferenciaPagoPortafolio>();
		}
	}

	public static class SalidaCrearPreferenciaPagoPortafolioMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
        public SalidaCrearPreferenciaPagoPortafolioMasivo() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<>();
        }
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearPreferenciaPagoPortafolioMasivo jin = gson.fromJson(in.toString(), EntradaCrearPreferenciaPagoPortafolioMasivo.class);
		SalidaCrearPreferenciaPagoPortafolioMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearPreferenciaPagoPortafolioMasivo logica(Connection con, EntradaCrearPreferenciaPagoPortafolioMasivo entrada) throws Exception{
		logger.info("INICIO::CrearPreferenciaPagoPortafolioMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearPreferenciaPagoPortafolioMasivo out = new SalidaCrearPreferenciaPagoPortafolioMasivo();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = obtenerPreferenciasPagoPortafolio(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_PreferenciasPagoPortafolio, esOperacionConAutorizacion);
		
		insertarPreferenciaPagoPortafolio(con, preferenciasPagoPortafolio);
		
        Map<Long, SocioNegocioUtils.Portafolio> portafoliosPorIdPortafolio = obtenerPortafoliosPorIdPortafolio(con, preferenciasPagoPortafolio);
		
		if(esOperacionConAutorizacion) {
            out = crearEventoAutorizacion(con, entrada, preferenciasPagoPortafolio, portafoliosPorIdPortafolio);
		} else {
			out = confirmarCrearPreferenciaPago(con, entrada, preferenciasPagoPortafolio);
			agregarLog(con, entrada, preferenciasPagoPortafolio, portafoliosPorIdPortafolio);
		}
		
        replicarPortafoliosAPV(con, entrada, preferenciasPagoPortafolio, portafoliosPorIdPortafolio);

        out.P_OUT_REFID = obtenerEntidadesInsertadas(preferenciasPagoPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPreferenciaPagoPortafolioMasivo", finServicio - inicioServicio);
		return out;
	}
	
	public List<PreferenciaPagoPortafolio> obtenerPreferenciasPagoPortafolio(Connection con, Long idEmpresa, Long idUsuario, List<PreferenciaPagoPortafolio> entradaPreferenciasPagoPortafolio, Boolean esOperacionConAutorizacion) throws Exception {
		logger.trace("INICIO::CrearPreferenciaPagoPortafolioMasivo.obtenerPreferenciasPago");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPreferenciaPagoPortafolio = Utils.getSecuencia(con, "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio", Long.valueOf(entradaPreferenciasPagoPortafolio.size()));
		String horaSistemaUTC = FechaDB.ObtenerFechaHoraSistemaUTC(con, idEmpresa);
		AtomicInteger index = new AtomicInteger();
		
		List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = entradaPreferenciasPagoPortafolio
				.stream()
				.map(entradaPreferenciaPagoPortafolio->{
					Long idPreferenciaPagoPortafolio = idsPreferenciaPagoPortafolio.get(index.getAndIncrement());
					PreferenciaPagoPortafolio preferenciaPagoPortafolio = obtenerEntidadPreferenciaPagoPortafolio(entradaPreferenciaPagoPortafolio,idEmpresa, idUsuario, idPreferenciaPagoPortafolio, esOperacionConAutorizacion, horaSistemaUTC);
					return preferenciaPagoPortafolio;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPreferenciaPagoPortafolioMasivo.obtenerPreferenciasPago", fin - inicio);
		return preferenciasPagoPortafolio;
	}
	
	private PreferenciaPagoPortafolio obtenerEntidadPreferenciaPagoPortafolio(PreferenciaPagoPortafolio entradaPreferenciaPagoPortafolio,Long idEmpresa, Long idUsuario, Long idPreferenciaPagoPortafolio, Boolean esOperacionConAutorizacion, String horaSistemaUTC) {
		logger.trace("INICIO::CrearPreferenciaPagoPortafolioMasivo.obtenerEntidadPreferenciaPagoPortafolio");
		long inicio = System.currentTimeMillis();
		
		PreferenciaPagoPortafolio preferenciaPagoPortafolio = new PreferenciaPagoPortafolio();
		preferenciaPagoPortafolio.IdInterno = entradaPreferenciaPagoPortafolio.IdInterno;
		preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio = idPreferenciaPagoPortafolio;
		preferenciaPagoPortafolio.IdPortafolio = entradaPreferenciaPagoPortafolio.IdPortafolio;
		preferenciaPagoPortafolio.IdPreferenciaPago = entradaPreferenciaPagoPortafolio.IdPreferenciaPago;
		preferenciaPagoPortafolio.IdPersonaRelacionada = entradaPreferenciaPagoPortafolio.IdPersonaRelacionada;
		preferenciaPagoPortafolio.IdCuentaBancaria = entradaPreferenciaPagoPortafolio.IdCuentaBancaria;
		preferenciaPagoPortafolio.FechaCreacion = Fecha.toTimeStamp(horaSistemaUTC);
		preferenciaPagoPortafolio.FechaModificacion = Fecha.toTimeStamp(horaSistemaUTC);
		preferenciaPagoPortafolio.IdUsuarioCreador = idUsuario;
		preferenciaPagoPortafolio.IdEstado = entradaPreferenciaPagoPortafolio.IdEstado;
		preferenciaPagoPortafolio.IdEmpresa = idEmpresa;
		preferenciaPagoPortafolio.IdTarjetaBancaria = entradaPreferenciaPagoPortafolio.IdTarjetaBancaria;
		preferenciaPagoPortafolio.FechaIngreso = Fecha.toTimeStamp(horaSistemaUTC);
		preferenciaPagoPortafolio.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		preferenciaPagoPortafolio.PorDefecto = entradaPreferenciaPagoPortafolio.PorDefecto;
		preferenciaPagoPortafolio.IdCarga = entradaPreferenciaPagoPortafolio.IdCarga;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPreferenciaPagoPortafolioMasivo.obtenerEntidadDireccionFisica", fin - inicio);
		return preferenciaPagoPortafolio;
	}
	
	private void insertarPreferenciaPagoPortafolio(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws Exception {
		logger.trace("INICIO::CrearPreferenciaPagoPortafolioMasivo.insertarPreferenciaPagoPortafolio");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasPagoPortafolio, PreferenciaPagoPortafolio.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPreferenciaPagoPortafolioMasivo.insertarPreferenciaPagoPortafolio", fin - inicio);
	}
	
    private Map<Long, SocioNegocioUtils.Portafolio> obtenerPortafoliosPorIdPortafolio(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws SQLException {
        List<Long> idsPortafolio = preferenciasPagoPortafolio
                .parallelStream()
                .map(preferenciaPagoPortafolio -> preferenciaPagoPortafolio.IdPortafolio)
                .collect(Collectors.toList());

        return SocioNegocioUtils.obtenerDatosPortafolioPorIdPortafolio(con, idsPortafolio);
    }
	
	private SalidaCrearPreferenciaPagoPortafolioMasivo crearEventoAutorizacion(Connection con, EntradaCrearPreferenciaPagoPortafolioMasivo entrada, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio, Map<Long, SocioNegocioUtils.Portafolio> portafoliosPorIdPortafolio) throws Exception {
		logger.trace("INICIO::CrearPreferenciaPagoPortafolioMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		SalidaCrearPreferenciaPagoPortafolioMasivo out = new SalidaCrearPreferenciaPagoPortafolioMasivo();
		
        CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        
        CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
        entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
        entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
        
        entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = preferenciasPagoPortafolio
                .stream()
                .map(preferenciaPagoPortafolio -> {
                    SocioNegocioUtils.Portafolio portafolio = portafoliosPorIdPortafolio.get(preferenciaPagoPortafolio.IdPortafolio);
                    CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
                    evento.IdTipoEvento = ID_CONCEPTO;
                    evento.IdModuloSolicita = entrada.p_IdModulo;
                    evento.IdUsuarioSolicita = entrada.p_IdUsuario;
                    evento.Fecha = fechaSistema;
                    evento.IdConcepto = ID_CONCEPTO;
                    evento.NumeroOperacionConcepto = String.valueOf(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio);
                    evento.IdCliente = portafolio.IdCliente;
                    evento.Glosa = portafolio.DocumentoIdentidad + " / " + portafolio.NombreCompleto + " / CodPort: " + portafolio.CodigoPortafolio + " / Preferencia Pago: " + preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;

                    return evento;
                })
                .collect(Collectors.toList());
        CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);

        out.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
        out.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));

        if (out.P_OUT_ERRORESCONTADOR.equals(0L)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), con, entrada.p_IdEmpresa));
        }
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPreferenciaPagoPortafolioMasivo.crearEventoAutorizacion", fin - inicio);
		return out;
	}
	

	private SalidaCrearPreferenciaPagoPortafolioMasivo confirmarCrearPreferenciaPago(Connection con, EntradaCrearPreferenciaPagoPortafolioMasivo entrada, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws Exception {
		logger.debug("INICIO::CrearCuentaCustodioMasivo.confirmarCrearPreferenciaPago");
		long inicio = System.currentTimeMillis();
		SalidaCrearPreferenciaPagoPortafolioMasivo out = new SalidaCrearPreferenciaPagoPortafolioMasivo();
		for (PreferenciaPagoPortafolio preferenciaPagoPortafolio : preferenciasPagoPortafolio) {
			ConfirmarCrearPreferenciaPago.EntradaConfirmarCrearPreferenciaPago entradaConfirmarCrearPreferenciaPago = new ConfirmarCrearPreferenciaPago.EntradaConfirmarCrearPreferenciaPago();
			entradaConfirmarCrearPreferenciaPago.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmarCrearPreferenciaPago.p_IdUsuario = entrada.p_IdUsuario;
			entradaConfirmarCrearPreferenciaPago.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmarCrearPreferenciaPago.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaConfirmarCrearPreferenciaPago.p_IdPreferenciaPagoPortafolio = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
			entradaConfirmarCrearPreferenciaPago.p_IdEstadoAutorizacion = preferenciaPagoPortafolio.IdEstadoAutorizacion;
			ConfirmarCrearPreferenciaPago confirmarCrearPreferenciaPago = new ConfirmarCrearPreferenciaPago();
			ConfirmarCrearPreferenciaPago.SalidaConfirmarCrearPreferenciaPago salidaConfirmarCrearPreferenciaPago = confirmarCrearPreferenciaPago.logica(con, entradaConfirmarCrearPreferenciaPago);
			out.P_OUT_ERRORESCONTADOR += salidaConfirmarCrearPreferenciaPago.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaConfirmarCrearPreferenciaPago.P_OUT_MENSAJES);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCuentaCustodioMasivo.confirmarCrearPreferenciaPago", fin - inicio);
		return out;
	}

	
	
	private void agregarLog(Connection con, EntradaCrearPreferenciaPagoPortafolioMasivo entrada, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio, Map<Long, SocioNegocioUtils.Portafolio> portafoliosPorIdPortafolio) throws Exception {
		logger.trace("INICIO::CrearPreferenciaPagoPortafolioMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(PreferenciaPagoPortafolio preferenciaPagoPortafolio : preferenciasPagoPortafolio){
			
			SocioNegocioUtils.Portafolio portafolio = portafoliosPorIdPortafolio.get(preferenciaPagoPortafolio.IdPortafolio);	
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdPortafolio", "", String.valueOf(preferenciaPagoPortafolio.IdPortafolio)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdPreferenciaPago", "", preferenciaPagoPortafolio.IdPreferenciaPago));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdPersonaRelacionada", "", String.valueOf(preferenciaPagoPortafolio.IdPersonaRelacionada)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdCuentaBancaria", "", String.valueOf(preferenciaPagoPortafolio.IdCuentaBancaria)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "FechaCreacion", "",  String.valueOf(preferenciaPagoPortafolio.FechaCreacion)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "FechaModificacion", "", String.valueOf(preferenciaPagoPortafolio.FechaModificacion)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdUsuarioCreador", "", String.valueOf(preferenciaPagoPortafolio.IdUsuarioCreador)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdEstado", "", preferenciaPagoPortafolio.IdEstado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdEmpresa", "",  String.valueOf(preferenciaPagoPortafolio.IdEmpresa)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdTarjetaBancaria", "", String.valueOf(preferenciaPagoPortafolio.IdTarjetaBancaria)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "FechaIngreso", "", String.valueOf(preferenciaPagoPortafolio.FechaIngreso)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "PorDefecto", "", preferenciaPagoPortafolio.PorDefecto));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, "IdCarga", "", String.valueOf(preferenciaPagoPortafolio.IdCarga)));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = portafolio.IdCliente;
			log.IdTipoRegistro = "CREACION_PREFERENCIA_PAGO_PORTAFOLIO";
			log.EstadoRegistro = preferenciaPagoPortafolio.IdEstadoAutorizacion;
			log.EstadoProceso = preferenciaPagoPortafolio.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Preferencia Pago Portafolio";		
			log.IdRegistro = String.valueOf(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio);
			
			entradaAgregarLog.p_Log.add(log);
		}
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPreferenciaPagoPortafolioMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertadas(List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio){
		logger.trace("INICIO::CrearPreferenciaPagoPortafolioMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = preferenciasPagoPortafolio
				.parallelStream()
				.map(preferenciaPagoPortafolio->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = preferenciaPagoPortafolio.IdInterno;
					entidad.IdEntidad = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPreferenciaPagoPortafolioMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}
	
    private SalidaCrearPreferenciaPagoPortafolioMasivo replicarPortafoliosAPV(Connection con, EntradaCrearPreferenciaPagoPortafolioMasivo entrada, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio, Map<Long, Portafolio> portafoliosPorIdPortafolio) throws Exception {
        logger.debug("INICIO::CrearCuentaCustodioMasivo.replicarPortafoliosAPV");
        long inicio = System.currentTimeMillis();

        SalidaCrearPreferenciaPagoPortafolioMasivo out = new SalidaCrearPreferenciaPagoPortafolioMasivo();

        for (PreferenciaPagoPortafolio preferenciaPagoPortafolio : preferenciasPagoPortafolio) {
            SocioNegocioUtils.Portafolio portafolio = portafoliosPorIdPortafolio.get(preferenciaPagoPortafolio.IdPortafolio);
            boolean esPropositoAPV = portafolio != null && portafolio.Proposito.equals(PROPOSITO_APV);

            if (esPropositoAPV) {
                ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV entradaReplicaPortafolioAPV = new ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV();
                entradaReplicaPortafolioAPV.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaReplicaPortafolioAPV.p_IdUsuario = entrada.p_IdUsuario;
                entradaReplicaPortafolioAPV.p_IdModulo = entrada.p_IdModulo;
                entradaReplicaPortafolioAPV.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
                entradaReplicaPortafolioAPV.p_Portafolio = preferenciaPagoPortafolio.IdPortafolio;
                entradaReplicaPortafolioAPV.p_IdEntidad = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
                entradaReplicaPortafolioAPV.p_TipoEntidad = ID_TIPO_ENTIDAD_REPLICA;
                entradaReplicaPortafolioAPV.p_TipoOperacion = ID_TIPO_OPERACION;

                ReplicaPortafolioAPV replicaPortafolioAPV = new ReplicaPortafolioAPV();
                ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV salidaReplicaPortafolioAPV = replicaPortafolioAPV.logica(con, entradaReplicaPortafolioAPV);

                out.P_OUT_ERRORESCONTADOR += salidaReplicaPortafolioAPV.P_OUT_ERRORESCONTADOR;
                out.P_OUT_MENSAJES.addAll(salidaReplicaPortafolioAPV.P_OUT_MENSAJES);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearCuentaCustodioMasivo.replicarPortafoliosAPV", fin - inicio);
        return out;
    }
    
    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");
        String json = "{\r\n"
        		+ "   \"p_IdEmpresa\":1,\r\n"
        		+ "   \"p_IdUsuario\":4,\r\n"
        		+ "   \"p_IdModulo\":\"PLA\",\r\n"
        		+ "   \"p_UsaReglasNegocio\":\"N\",\r\n"
        		+ "   \"p_PreferenciasPagoPortafolio\":[\r\n"
        		+ "      {\r\n"
        		+ "         \"IdUsuario\":9,\r\n"
        		+ "         \"IdModulo\":\"SOC\",\r\n"
        		+ "         \"UsaReglasNegocio\":\"S\",\r\n"
        		+ "         \"IdPortafolio\":1451179,\r\n"
        		+ "         \"IdPreferenciaPago\":\"VENACN_EBA_CLP\",\r\n"
        		+ "         \"IdCuentaBancaria\":0,\r\n"
        		+ "         \"IdTarjetaBancaria\":0,\r\n"
        		+ "         \"IdEstado\":\"ACT\",\r\n"
        		+ "         \"PorDefecto\":\"S\"\r\n"
        		+ "      }\r\n"
        		+ "   ]\r\n"
        		+ "}";
        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaCrearPreferenciaPagoPortafolioMasivo entrada = gson.fromJson(json, EntradaCrearPreferenciaPagoPortafolioMasivo.class);
            CrearPreferenciaPagoPortafolioMasivo crearPreferenciaPagoPortafolioMasivo = new CrearPreferenciaPagoPortafolioMasivo();
            try {
            	SalidaCrearPreferenciaPagoPortafolioMasivo out = crearPreferenciaPagoPortafolioMasivo.logica(conn, entrada);
                System.out.println(out);
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
