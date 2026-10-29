package cl.bcs.socionegocio.per;

import java.sql.Connection;
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

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPersonaTarjetaBancariaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearPersonaTarjetaBancariaMasivo.class);
	
	private final String SQL_INSERT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaTarjetaBancariaMasivo_InsertTarjetaBancaria.sql");
	
	private final String ID_CONCEPTO = "NUETJB";
	
	public static class EntradaCrearPersonaTarjetaBancariaMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
		public EntradaCrearPersonaTarjetaBancariaMasivo() {
			this.p_TarjetasBancarias = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearPersonaTarjetaBancariaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearPersonaTarjetaBancariaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}
	
	public static class TarjetaBancaria extends BcsPojo {
		public Long IdInterno;
		public Long IdTarjetaBancaria;
		public Long IdBanco;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public Long IdPersona;
		public String FechaVencimiento;
		public Long CodigoVerificacion;
		public String IdEstado;
		public Long IdUsuarioCreador;
		public String IdEstadoAutorizacion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearPersonaTarjetaBancariaMasivo jin = gson.fromJson(in.toString(), EntradaCrearPersonaTarjetaBancariaMasivo.class);
		SalidaCrearPersonaTarjetaBancariaMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public SalidaCrearPersonaTarjetaBancariaMasivo logica(Connection con, EntradaCrearPersonaTarjetaBancariaMasivo entrada) throws Exception {
		logger.info("INICIO::CrearPersonaTarjetaBancariaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearPersonaTarjetaBancariaMasivo out = new SalidaCrearPersonaTarjetaBancariaMasivo();
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<TarjetaBancaria> tarjetasBancarias = obtenerTarjetasBancarias(con, entrada.p_TarjetasBancarias, esOperacionConAutorizacion);
		insertarTarjetasBancarias(con, tarjetasBancarias);
		
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, tarjetasBancarias);
		} else {
			agregarLog(con, entrada, tarjetasBancarias);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(tarjetasBancarias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaTarjetaBancariaMasivo", finServicio - inicioServicio);
		return out;
	}
		
	private List<TarjetaBancaria> obtenerTarjetasBancarias(Connection con, List<TarjetaBancaria> entradaTarjetasBancarias, Boolean esOperacionConAutorizacion) throws Exception {
		logger.trace("INICIO::CrearPersonaTarjetaBancariaMasivo.obtenerTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsTarjetasBancarias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::TarjetaBancaria", Long.valueOf(entradaTarjetasBancarias.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<TarjetaBancaria> tarjetasBancarias = entradaTarjetasBancarias
				.stream()
				.map(entradaTarjetaBancaria->{
					Long idTarjetaBancaria = idsTarjetasBancarias.get(index.getAndIncrement());
					TarjetaBancaria tarjetaBancaria = obtenerEntidadTarjetaBancaria(entradaTarjetaBancaria, idTarjetaBancaria, esOperacionConAutorizacion);
					return tarjetaBancaria;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaTarjetaBancariaMasivo.obtenerTarjetasBancarias", fin - inicio);
		return tarjetasBancarias;
	}
	
	private TarjetaBancaria obtenerEntidadTarjetaBancaria(TarjetaBancaria entradaTarjetaBancaria, Long idTarjetaBancaria, Boolean esOperacionConAutorizacion) {
		logger.trace("INICIO::CrearPersonaTarjetaBancariaMasivo.obtenerEntidadTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		
		TarjetaBancaria tarjetaBancaria = new TarjetaBancaria();
		tarjetaBancaria.IdInterno = entradaTarjetaBancaria.IdInterno;
		tarjetaBancaria.IdTarjetaBancaria = idTarjetaBancaria;
		tarjetaBancaria.IdBanco = entradaTarjetaBancaria.IdBanco;
		tarjetaBancaria.IdTipoTarjetaBancaria = entradaTarjetaBancaria.IdTipoTarjetaBancaria;
		tarjetaBancaria.NumeroTarjetaBancaria = entradaTarjetaBancaria.NumeroTarjetaBancaria;
		tarjetaBancaria.IdPersona = entradaTarjetaBancaria.IdPersona;
		tarjetaBancaria.FechaVencimiento = entradaTarjetaBancaria.FechaVencimiento;
		tarjetaBancaria.CodigoVerificacion = entradaTarjetaBancaria.CodigoVerificacion;
		tarjetaBancaria.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaTarjetaBancaria.IdEstado, "ACT");;
		tarjetaBancaria.IdUsuarioCreador = entradaTarjetaBancaria.IdUsuarioCreador;
		tarjetaBancaria.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaTarjetaBancariaMasivo.obtenerEntidadTarjetaBancaria", fin - inicio);
		
		return tarjetaBancaria;
	}
	
	private void insertarTarjetasBancarias(Connection con, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.trace("INICIO::CrearPersonaTarjetaBancariaMasivo.insertarTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_TARJETA_BANCARIA, tarjetasBancarias, TarjetaBancaria.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaTarjetaBancariaMasivo.insertarTarjetasBancarias", fin - inicio);
	}
	
	private SalidaCrearPersonaTarjetaBancariaMasivo crearEventoAutorizacion(Connection con, EntradaCrearPersonaTarjetaBancariaMasivo entrada, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancariaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearPersonaTarjetaBancariaMasivo salidaCrearPersonaTarjetaBancariaMasivo = new SalidaCrearPersonaTarjetaBancariaMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = tarjetasBancarias
				.parallelStream()
				.map(tarjetaBancaria->tarjetaBancaria.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = tarjetasBancarias.stream().map(tarjetaBancaria->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(tarjetaBancaria.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(tarjetaBancaria.IdTarjetaBancaria);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = tarjetaBancaria.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " /NroTarjeta: " + tarjetaBancaria.NumeroTarjetaBancaria;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearPersonaTarjetaBancariaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancariaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaCrearPersonaTarjetaBancariaMasivo;	
	}
	
	private void agregarLog(Connection con, EntradaCrearPersonaTarjetaBancariaMasivo entrada, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.trace("INICIO::CrearPersonaTarjetaBancariaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(TarjetaBancaria tarjetaBancaria : tarjetasBancarias) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(tarjetaBancaria.IdTarjetaBancaria, "IdBanco", "", String.valueOf(tarjetaBancaria.IdBanco)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(tarjetaBancaria.IdTarjetaBancaria, "IdTipoTarjetaBancaria", "", tarjetaBancaria.IdTipoTarjetaBancaria));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(tarjetaBancaria.IdTarjetaBancaria, "NumeroTarjetaBancaria", "", tarjetaBancaria.NumeroTarjetaBancaria));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(tarjetaBancaria.IdTarjetaBancaria, "FechaVencimiento", "", tarjetaBancaria.FechaVencimiento));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(tarjetaBancaria.IdTarjetaBancaria, "CodigoVerificacion", "", String.valueOf(tarjetaBancaria.CodigoVerificacion)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(tarjetaBancaria.IdTarjetaBancaria, "IdEstado", "", tarjetaBancaria.IdEstado));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = tarjetaBancaria.IdTarjetaBancaria;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = tarjetaBancaria.IdPersona;
			log.IdTipoRegistro = "CREACION_TARJETA_BANCARIA";
			log.EstadoRegistro = tarjetaBancaria.IdEstadoAutorizacion;
			log.EstadoProceso = tarjetaBancaria.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Tarjeta Bancaria";
			log.IdRegistro = String.valueOf(tarjetaBancaria.IdTarjetaBancaria);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaTarjetaBancariaMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<TarjetaBancaria> tarjetasBancarias){
		logger.trace("INICIO::CrearPersonaTarjetaBancariaMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = tarjetasBancarias
				.parallelStream()
				.map(tarjetaBancaria->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = tarjetaBancaria.IdInterno;
					entidad.IdEntidad = tarjetaBancaria.IdTarjetaBancaria;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaTarjetaBancariaMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}

}
