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
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaTelefonoMasivo.Cliente;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.per.pojo.Telefono;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearTelefonoMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearTelefonoMasivo.class);
	
	private final String SQL_INSERT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearTelefonoMasivo_InsertTelefono.sql");
	private String SQL_UPDATE_TELEFONO_NO_PREDETERMINADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_UpdateTelefonoNoPredeterminado.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_SelectCliente.sql");
	private String SQL_UPDATE_TELEFONO_PORTAFOLIO= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTelefonoMasivo_UpdateTelefonoPortafolio.sql");
	private String SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearTelefonoMasivo_UpdateTelefonoPrincipalTablaPersona.sql");
	
	private final String ID_CONCEPTO = "NUETEL";
	
	public static class EntradaCrearTelefonoMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<Telefono> p_Telefonos;
		
		public EntradaCrearTelefonoMasivo() {
			this.p_Telefonos = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearTelefonoMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearTelefonoMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}
	
	public static class Telefono extends BcsPojo {
		public Long IdInterno;
		public Long IdTelefono;
		public Long IdPais;
		public String IdTipoTelefono;
		public String Numero;
		public String Anexo;
		public String CodigoProposito;
		public String Comentario;
		public String IdEstado;
		public Long IdPersona;
		public String Predeterminado;
		public Long IdUsuarioCreador;
		public String IdEstadoAutorizacion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearTelefonoMasivo jin = gson.fromJson(in.toString(), EntradaCrearTelefonoMasivo.class);
		SalidaCrearTelefonoMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public SalidaCrearTelefonoMasivo logica(Connection con, EntradaCrearTelefonoMasivo entrada) throws Exception {
		logger.info("INICIO::CrearTelefonoMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearTelefonoMasivo out = new SalidaCrearTelefonoMasivo();
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<Telefono> telefonos = obtenerTelefonos(con, entrada.p_Telefonos, esOperacionConAutorizacion);
		insertarTelefonos(con, telefonos);
		predeterminarTelefonoPortafolio(con, telefonos);
		
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, telefonos);
		} else {
			agregarLog(con, entrada, telefonos);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(telefonos);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearTelefonoMasivo", finServicio - inicioServicio);
		return out;
	}
		
	private List<Telefono> obtenerTelefonos(Connection con, List<Telefono> entradaTelefonos, Boolean esOperacionConAutorizacion) throws Exception {
		logger.trace("INICIO::CrearTelefonoMasivo.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsTelefonos = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::Telefono", Long.valueOf(entradaTelefonos.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<Telefono> telefonos = entradaTelefonos
				.stream()
				.map(entradaTelefono->{
					Long idTelefono = idsTelefonos.get(index.getAndIncrement());
					Telefono telefono = obtenerEntidadDireccionTelefono(entradaTelefono, idTelefono, esOperacionConAutorizacion);
					return telefono;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTelefonoMasivo.obtenerTelefonos", fin - inicio);
		return telefonos;
	}
	
	private Telefono obtenerEntidadDireccionTelefono(Telefono entradaTelefono, Long idTelefono, Boolean esOperacionConAutorizacion) {
		logger.trace("INICIO::CrearTelefonoMasivo.obtenerEntidadDireccionTelefono");
		long inicio = System.currentTimeMillis();
		
		Telefono telefono = new Telefono();
		telefono.IdInterno = entradaTelefono.IdInterno;
		telefono.IdTelefono = idTelefono;
		telefono.IdPais = entradaTelefono.IdPais;
		telefono.IdTipoTelefono = entradaTelefono.IdTipoTelefono;
		telefono.Numero = entradaTelefono.Numero;
		telefono.Anexo = SocioNegocioUtils.obtenerValorNoNulo(entradaTelefono.Anexo, "");
		telefono.CodigoProposito = entradaTelefono.CodigoProposito;
		telefono.Comentario = SocioNegocioUtils.obtenerValorNoNulo(entradaTelefono.Comentario, "");
		telefono.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaTelefono.IdEstado, "ACT");
		telefono.IdPersona = entradaTelefono.IdPersona;
		telefono.Predeterminado = entradaTelefono.Predeterminado;
		telefono.IdUsuarioCreador = entradaTelefono.IdUsuarioCreador;
		telefono.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTelefonoMasivo.obtenerEntidadDireccionTelefono", fin - inicio);
		
		return telefono;
	}
	
	private void insertarTelefonos(Connection con, List<Telefono> telefonos) throws Exception {
		logger.trace("INICIO::CrearTelefonoMasivo.insertarTelefonos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_TELEFONO, telefonos, Telefono.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTelefonoMasivo.insertarTelefonos", fin - inicio);
	}
	
	private void predeterminarTelefonoPortafolio(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::CrearTelefonoMasivo.predeterminarTelefonoPortafolio");
		long inicio = System.currentTimeMillis();
		List<Telefono> telefonoPredeterminado = telefonos.stream()
				.filter(tel->tel.Predeterminado.equals("S"))
				.collect(Collectors.toList());
		
		if(telefonos.size() > 0 ) {
			SQL sql = new SQL();		
			if(telefonoPredeterminado.size()>0) {
				HashMap<String, Object> parametros = new HashMap<String, Object>();
				Long idTelefonoPredeterminado = telefonoPredeterminado.get(0).IdTelefono;
				parametros.put("IdTelefono", idTelefonoPredeterminado);
				List<Cliente> cliente = sql.query(con, SQL_SELECT_CLIENTE, parametros, Cliente.class);
				Long idCliente = cliente.get(0).IdCliente;
				Long idPersona = cliente.get(0).IdPersona;
				SQL_UPDATE_TELEFONO_NO_PREDETERMINADO = SQL_UPDATE_TELEFONO_NO_PREDETERMINADO.replace("(:IdTelefono)", idTelefonoPredeterminado.toString()).replace("(:IdPersona)", idPersona.toString());
				SQL.exec(con, SQL_UPDATE_TELEFONO_NO_PREDETERMINADO);
				SQL_UPDATE_TELEFONO_PORTAFOLIO  = SQL_UPDATE_TELEFONO_PORTAFOLIO.replace("(:IdTelefono)", idTelefonoPredeterminado.toString()).replace("(:IdCliente)", idCliente.toString());
				SQL.exec(con, SQL_UPDATE_TELEFONO_PORTAFOLIO);
				
				//Actualizar telefono principal en tabla PERSONA
				SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA = SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA.replace("(:IdTelefono)", idTelefonoPredeterminado.toString()).replace("(:IdPersona)", idPersona.toString());
				SQL.exec(con, SQL_UPDATE_TELEFONO_PRINCIPAL_TABLA_PERSONA);
				
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTelefonoMasivo.predeterminarTelefonoPortafolio", fin - inicio);
	}

	
	private SalidaCrearTelefonoMasivo crearEventoAutorizacion(Connection con, EntradaCrearTelefonoMasivo entrada, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::CrearTelefonoMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearTelefonoMasivo salidaDireccionTelefonoMasiva = new SalidaCrearTelefonoMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = telefonos
				.parallelStream()
				.map(telefono->telefono.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = telefonos.stream().map(telefono->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(telefono.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(telefono.IdTelefono);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = telefono.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " / NroTel: " + telefono.Numero;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaDireccionTelefonoMasiva.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaDireccionTelefonoMasiva.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaDireccionTelefonoMasiva.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaDireccionTelefonoMasiva.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaDireccionTelefonoMasiva.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTelefonoMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaDireccionTelefonoMasiva;	
	}
	
	private void agregarLog(Connection con, EntradaCrearTelefonoMasivo entrada, List<Telefono> telefonos) throws Exception {
		logger.trace("INICIO::CrearTelefonoMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(Telefono telefono : telefonos) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "IdPais", "", String.valueOf(telefono.IdPais)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "IdTipoTelefono", "", telefono.IdTipoTelefono));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "Numero", "", telefono.Numero));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "Anexo", "", telefono.Anexo));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "CodigoProposito", "", telefono.CodigoProposito));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "Comentario", "", telefono.Comentario));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "IdEstado", "", telefono.IdEstado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(telefono.IdTelefono, "Predeterminado", "", telefono.Predeterminado));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = telefono.IdTelefono;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = telefono.IdPersona;
			log.IdTipoRegistro = "CREACION_DIRECCION_TELEFONO";
			log.EstadoRegistro = telefono.IdEstadoAutorizacion;
			log.EstadoProceso = telefono.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Direccion Telefono";
			log.IdRegistro = String.valueOf(telefono.IdTelefono);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTelefonoMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<Telefono> telefonos){
		logger.trace("INICIO::CrearTelefonoMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = telefonos
				.parallelStream()
				.map(telefono->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = telefono.IdInterno;
					entidad.IdEntidad = telefono.IdTelefono;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearTelefonoMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}

}
