package cl.bcs.socionegocio.rel;

import java.math.BigDecimal;
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

public class CrearRelacionPersonaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearRelacionPersonaMasivo.class);
	
	private final String SQL_SELECT_VALIDAR_PERSONA_EXISTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_SelectValidacionPersonaExiste.sql");
	private final String SQL_SELECT_VALIDAR_RELACION_EXISTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_SelectValidacionRelacionExiste.sql");
	private final String SQL_SELECT_VALIDAR_RELACION_ARBITRADOR_EXISTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_SelectValidacionRelacionArbitradorExiste.sql");
	private final String SQL_SELECT_VALIDAR_EMPLEADO_RELACION = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_SelectValidacionEmpleadoRelacion.sql");
	private final String SQL_SELECT_REPRESENTANTE_LEGAL = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_SelectRepresentanteLegal.sql");
	private final String SQL_UPDATE_REPRESENTANTE_LEGAL = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_UpdateRepresentanteLegal.sql");
	private final String SQL_INSERT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersonaMasivo_InsertRelacionPersona.sql");
	
	private final String ID_CONCEPTO = "NUEREL";
	
	public static class EntradaCrearRelacionPersonaMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<RelacionPersona> p_RelacionesPersona;
		
		public EntradaCrearRelacionPersonaMasivo() {
			this.p_RelacionesPersona = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearRelacionPersonaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearRelacionPersonaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
		
		public void agregarValidaciones(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				if(validacion.EsError) {
					this.P_OUT_ERRORESCONTADOR += 1L;
					this.P_OUT_MENSAJES.add(validacion.MensajeError);
				}
			}
		}
	}
	
	public static class RelacionPersona extends BcsPojo {
		public Long IdInterno;
		public Long IdRelacionPersona;
		public String IdTipoRelacion;
		public Long IdPersonaOrigen;
		public Long IdPersonaDestino;
		public String IdEstado;
		public String FechaInicioRelacion;
		public String FechaFinRelacion;
		public BigDecimal PorcentajeParticipacion;
		public String RetiraDocumentos;
		public String FinalidadRelacion;
		public String CondicionesRelacion;
		public Long IdGradoVinculacionParental;
		public String Ciudad;
		public String Calle;
		public String Numero;
		public String Telefono;
		public String Email;
		public String Complemento;
		public Long IdUsuarioCreador;
		public String IdEstadoAutorizacion;
		//wbs
		public String EstaInsertada;
		//wbs
		public String NombresPersonaRelacion;
		public String ApellidoPaternoPersonaRelacion;
		public String ApellidoMaternoPersonaRelacion;
		public String DocumentoIdentidadRelacion;
		public String TipoDocumentoIdentidadRelacion;
		public RelacionPersona() {
			this.IdRelacionPersona = 0L;
			this.NombresPersonaRelacion = "";
			this.ApellidoPaternoPersonaRelacion = "";
			this.ApellidoMaternoPersonaRelacion = "";
			this.TipoDocumentoIdentidadRelacion = "R";
			this.EstaInsertada = "S";
		}
		
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	public static class Persona extends BcsPojo{
		public Long IdPersona;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearRelacionPersonaMasivo jin = gson.fromJson(in.toString(), EntradaCrearRelacionPersonaMasivo.class);
		SalidaCrearRelacionPersonaMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public SalidaCrearRelacionPersonaMasivo logica(Connection con, EntradaCrearRelacionPersonaMasivo entrada) throws Exception {
		logger.info("INICIO::CrearRelacionPersonaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearRelacionPersonaMasivo out = new SalidaCrearRelacionPersonaMasivo();
		List<Validacion> validacionPersona = validarPersona(con, entrada);
		List<Validacion> validacionRelacionExiste = validarRelacionExiste(con, entrada);
		List<Validacion> validacionRelacion = validarRelacion(con, entrada);
		List<Validacion> validacionRelacionArbitradorExiste = validarRelacionArbitradorExiste(con, entrada);
		List<Validacion> validacionEmpleado = validarEmpleado(con, entrada);
		
		out.agregarValidaciones(validacionPersona);
		out.agregarValidaciones(validacionRelacionExiste);
		out.agregarValidaciones(validacionRelacion);
		out.agregarValidaciones(validacionRelacionArbitradorExiste);
		out.agregarValidaciones(validacionEmpleado);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		actualizarRepresentantesLegales(con, entrada.p_RelacionesPersona);
		List<RelacionPersona> relacionesPersona = obtenerRelacionesPersona(con, entrada.p_RelacionesPersona, esOperacionConAutorizacion);
		insertarRelacionesPersona(con, relacionesPersona);
		
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, relacionesPersona);
		} else {
			agregarLog(con, entrada, relacionesPersona);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(relacionesPersona);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearRelacionPersonaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<Validacion> validarPersona(Connection con, EntradaCrearRelacionPersonaMasivo entrada) throws Exception {
		logger.debug("INICIO::CrearRelacionPersonaMasivo.validarPersona");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>();
		
		List<Long> idsPersonaDestino = entrada.p_RelacionesPersona
				.parallelStream()
				.map(relacion->relacion.IdPersonaDestino)
				.distinct()
				.collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersona", idsPersonaDestino);
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_VALIDAR_PERSONA_EXISTE, parametros, Persona.class);
		
		for(RelacionPersona relacionPersona : entrada.p_RelacionesPersona) {
			Persona persona = personas
					.parallelStream()
					.filter(personaEncontrada->personaEncontrada.IdPersona.equals(relacionPersona.IdPersonaDestino))
					.findFirst()
					.orElse(null);
			
			if(persona == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(relacionPersona.IdInterno,MensajeSalida.MENSAJE_ERROR,"PERSONA_RELACIONADA_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersonaMasivo.validarPersona", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarRelacionExiste(Connection con, EntradaCrearRelacionPersonaMasivo entrada) throws Exception {
		logger.debug("INICIO::CrearRelacionPersonaMasivo.validarRelacionExiste");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>();
		
		List<Long> idsPersonaOrigen = entrada.p_RelacionesPersona
				.parallelStream()
				.map(relacion->relacion.IdPersonaOrigen)
				.distinct()
				.collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersonaOrigen", idsPersonaOrigen);
		
		SQL sql = new SQL();
		List<RelacionPersona> relacionesPersona = sql.query(con, SQL_SELECT_VALIDAR_RELACION_EXISTE, parametros, RelacionPersona.class);
		
		for(RelacionPersona relacionPersona : entrada.p_RelacionesPersona) {
			RelacionPersona relacionPersonaExistente = relacionesPersona
					.parallelStream()
					.filter(relacionPersonaEncontrada->esRelacionEquivalente(relacionPersona, relacionPersonaEncontrada))
					.findFirst()
					.orElse(null);
			if(relacionPersonaExistente != null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(relacionPersona.IdInterno,MensajeSalida.MENSAJE_ERROR,"RELACION_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersonaMasivo.validarRelacionExiste", fin - inicio);
		return validaciones;
	}
	
	private Boolean esRelacionEquivalente(RelacionPersona relacionNueva, RelacionPersona relacionExistente){
		logger.trace("INICIO::CrearRelacionPersonaMasivo.esRelacionEquivalente");
		long inicio = System.currentTimeMillis();
		Boolean esRelacionEquivalente = relacionNueva.IdPersonaOrigen.equals(relacionExistente.IdPersonaOrigen) && relacionNueva.IdPersonaDestino.equals(relacionExistente.IdPersonaDestino) && relacionNueva.IdTipoRelacion.equals(relacionExistente.IdTipoRelacion);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.esRelacionEquivalente", fin - inicio);
		return esRelacionEquivalente;
	}
	
	private List<Validacion> validarRelacion(Connection con, EntradaCrearRelacionPersonaMasivo entrada) throws Exception {
		logger.trace("INICIO::CrearRelacionPersonaMasivo.validarRelacion");
		long inicio = System.currentTimeMillis();
		List<Validacion> validaciones = new ArrayList<>();
		
		for(RelacionPersona relacionPersona : entrada.p_RelacionesPersona) {
			if(relacionPersona.IdPersonaDestino.equals(relacionPersona.IdPersonaOrigen)) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(relacionPersona.IdInterno,MensajeSalida.MENSAJE_ERROR,"RELACION_INVALIDA",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.validarRelacion", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarRelacionArbitradorExiste(Connection con, EntradaCrearRelacionPersonaMasivo entrada)  throws Exception {
		logger.debug("INICIO::CrearRelacionPersonaMasivo.validarRelacionArbitradorExiste");
		long inicio = System.currentTimeMillis();
		List<Validacion> validaciones = new ArrayList<>();
		
		List<Long> idsPersonaOrigen = entrada.p_RelacionesPersona
				.parallelStream()
				.map(relacion->relacion.IdPersonaOrigen)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersonaOrigen", idsPersonaOrigen);
		
		SQL sql = new SQL();
		List<RelacionPersona> relacionesPersona = sql.query(con, SQL_SELECT_VALIDAR_RELACION_ARBITRADOR_EXISTE, parametros, RelacionPersona.class);
		
		for(RelacionPersona relacionPersona : entrada.p_RelacionesPersona) {
			RelacionPersona relacionPersonaExistente = relacionesPersona
					.parallelStream()
					.filter(relacionPersonaEncontrada->esRelacionArbitradorEquivalente(relacionPersona, relacionPersonaEncontrada))
					.findFirst()
					.orElse(null);
			if(relacionPersonaExistente != null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(relacionPersona.IdInterno,MensajeSalida.MENSAJE_ERROR,"RELACION_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersonaMasivo.validarRelacionArbitradorExiste", fin - inicio);
		return validaciones;
	}
	
	private Boolean esRelacionArbitradorEquivalente(RelacionPersona relacionNueva, RelacionPersona relacionExistente){
		logger.trace("INICIO::CrearRelacionPersonaMasivo.esRelacionArbitradorEquivalente");
		long inicio = System.currentTimeMillis();
		Boolean esRelacionEquivalente = relacionNueva.IdPersonaOrigen.equals(relacionExistente.IdPersonaOrigen) && relacionNueva.IdTipoRelacion.equals(relacionExistente.IdTipoRelacion);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.esRelacionArbitradorEquivalente", fin - inicio);
		return esRelacionEquivalente;
	}
	
	private List<Validacion> validarEmpleado(Connection con, EntradaCrearRelacionPersonaMasivo entrada) throws Exception {
		logger.debug("INICIO::CrearRelacionPersonaMasivo.validarRelacionArbitradorExiste");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>();
		List<RelacionPersona> relacionesPersonaArbitrador = entrada.p_RelacionesPersona
				.parallelStream()
				.filter(relacionPersona->esRelacionArbitrador(relacionPersona.IdTipoRelacion))
				.collect(Collectors.toList());
		
		List<Long> idsPersonaDestino = relacionesPersonaArbitrador
				.parallelStream()
				.map(relacion->relacion.IdPersonaDestino)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idsPersonaDestino);
		SQL sql = new SQL();
		List<Persona> empleados = sql.query(con, SQL_SELECT_VALIDAR_EMPLEADO_RELACION, parametros, Persona.class);
		
		for(RelacionPersona relacionPersona : relacionesPersonaArbitrador) {
			Persona persona = empleados
					.parallelStream()
					.filter(personaEncontrada->personaEncontrada.IdPersona.equals(relacionPersona.IdPersonaOrigen))
					.findFirst()
					.orElse(null);
			if(persona == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERSONA_RELACIONADA_EMPLEADO",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersonaMasivo.validarRelacionArbitradorExiste", fin - inicio);
		return validaciones;
	}
	
	private Boolean esRelacionArbitrador(String idTipoRelacion) {
		logger.trace("INICIO::CrearRelacionPersonaMasivo.esRelacionArbitrador");
		long inicio = System.currentTimeMillis();
		Boolean esRelacionArbitrador = "AR1".equals(idTipoRelacion) || "AR2".equals(idTipoRelacion);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.esRelacionArbitrador", fin - inicio);
		return esRelacionArbitrador;
	}
	
	private void actualizarRepresentantesLegales(Connection con, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.debug("INICIO::CrearRelacionPersonaMasivo.actualizarRepresentantesLegales");
		long inicio = System.currentTimeMillis();
		
		List<RelacionPersona> relacionesPersonaRepresentante = relacionesPersona
				.parallelStream()
				.filter(relacionPersona->"REP".equals(relacionPersona.IdTipoRelacion))
				.collect(Collectors.toList());
		
		List<Long> idsPersonaOrigen = relacionesPersonaRepresentante
				.parallelStream()
				.map(relacion->relacion.IdPersonaOrigen)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersonaOrigen", idsPersonaOrigen);
		SQL sql = new SQL();
		List<RelacionPersona> relacionesConRepresentanteLegal = sql.query(con, SQL_SELECT_REPRESENTANTE_LEGAL, parametros, RelacionPersona.class);
		
		List<RelacionPersona> representatesLegales = new ArrayList<>();
		for(RelacionPersona relacionPersona : relacionesPersonaRepresentante) {
			RelacionPersona relacionRepresentanteLegal = relacionesConRepresentanteLegal
					.parallelStream()
					.filter(relacionEncontrada->relacionEncontrada.IdPersonaOrigen.equals(relacionPersona.IdPersonaOrigen))
					.findFirst()
					.orElse(null);
			
			if(relacionRepresentanteLegal == null) {
				RelacionPersona representanteLegal = new RelacionPersona();
				representanteLegal.IdPersonaDestino = relacionPersona.IdPersonaDestino;
				representanteLegal.IdPersonaOrigen = relacionPersona.IdPersonaOrigen;
				representatesLegales.add(representanteLegal);
			}
		}
		
		sql.batch(con, SQL_UPDATE_REPRESENTANTE_LEGAL, representatesLegales, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersonaMasivo.actualizarRepresentantesLegales", fin - inicio);
	}
	
	private List<RelacionPersona> obtenerRelacionesPersona(Connection con, List<RelacionPersona> entradaRelacionesPersona, Boolean esOperacionConAutorizacion) throws Exception {
		logger.trace("INICIO::CrearRelacionPersonaMasivo.obtenerRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsRelacionesPersona = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona", Long.valueOf(entradaRelacionesPersona.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<RelacionPersona> relacionesPersona = entradaRelacionesPersona
				.stream()
				.map(entradaRelacionPersona->{
					Long idRelacionPersona = idsRelacionesPersona.get(index.getAndIncrement());
					RelacionPersona relacionPersona = obtenerEntidadRelacionPersona(entradaRelacionPersona, idRelacionPersona, esOperacionConAutorizacion);
					return relacionPersona;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.obtenerRelacionesPersona", fin - inicio);
		return relacionesPersona;
	}
	
	private RelacionPersona obtenerEntidadRelacionPersona(RelacionPersona entradaRelacionPersona, Long idRelacionPersona, Boolean esOperacionConAutorizacion) {
		logger.trace("INICIO::CrearRelacionPersonaMasivo.obtenerEntidadRelacionPersona");
		long inicio = System.currentTimeMillis();
		
		RelacionPersona relacionPersona = new RelacionPersona();
		relacionPersona.IdInterno = entradaRelacionPersona.IdInterno;
		relacionPersona.IdRelacionPersona = idRelacionPersona;
		relacionPersona.IdTipoRelacion = entradaRelacionPersona.IdTipoRelacion;
		relacionPersona.IdPersonaOrigen = entradaRelacionPersona.IdPersonaOrigen;
		relacionPersona.IdPersonaDestino = entradaRelacionPersona.IdPersonaDestino;
		relacionPersona.FechaInicioRelacion = entradaRelacionPersona.FechaInicioRelacion;
		relacionPersona.FechaFinRelacion = entradaRelacionPersona.FechaFinRelacion;
		relacionPersona.PorcentajeParticipacion = entradaRelacionPersona.PorcentajeParticipacion;
		relacionPersona.RetiraDocumentos = entradaRelacionPersona.RetiraDocumentos;
		relacionPersona.FinalidadRelacion = entradaRelacionPersona.FinalidadRelacion;
		relacionPersona.CondicionesRelacion = entradaRelacionPersona.CondicionesRelacion;
		relacionPersona.IdGradoVinculacionParental = entradaRelacionPersona.IdGradoVinculacionParental;
		relacionPersona.Ciudad = entradaRelacionPersona.Ciudad;
		relacionPersona.Calle = entradaRelacionPersona.Calle;
		relacionPersona.Numero = entradaRelacionPersona.Numero;
		relacionPersona.Telefono = entradaRelacionPersona.Telefono;
		relacionPersona.Email = entradaRelacionPersona.Email;
		relacionPersona.Complemento = entradaRelacionPersona.Complemento;
		relacionPersona.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaRelacionPersona.IdEstado, "ACT");;
		relacionPersona.IdUsuarioCreador = entradaRelacionPersona.IdUsuarioCreador;
		relacionPersona.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.obtenerEntidadRelacionPersona", fin - inicio);
		
		return relacionPersona;
	}
	
	private void insertarRelacionesPersona(Connection con, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.trace("INICIO::CrearRelacionPersonaMasivo.insertarRelacionesPersona");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_RELACION_PERSONA, relacionesPersona, RelacionPersona.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.insertarRelacionesPersona", fin - inicio);
	}
	
	private SalidaCrearRelacionPersonaMasivo crearEventoAutorizacion(Connection con, EntradaCrearRelacionPersonaMasivo entrada, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.debug("INICIO::CrearRelacionPersonaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearRelacionPersonaMasivo salidaCrearRelacionPersonaMasivo = new SalidaCrearRelacionPersonaMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = relacionesPersona
				.parallelStream()
				.map(relacionPersona->relacionPersona.IdPersonaOrigen)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = relacionesPersona.stream().map(relacionPersona->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(relacionPersona.IdPersonaOrigen);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(relacionPersona.IdRelacionPersona);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = relacionPersona.IdPersonaOrigen;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearRelacionPersonaMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaCrearRelacionPersonaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaCrearRelacionPersonaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaCrearRelacionPersonaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearRelacionPersonaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersonaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaCrearRelacionPersonaMasivo;	
	}
	
	private void agregarLog(Connection con, EntradaCrearRelacionPersonaMasivo entrada, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.trace("INICIO::CrearRelacionPersonaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(RelacionPersona relacionPersona : relacionesPersona) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "IdTipoRelacion", "", relacionPersona.IdTipoRelacion));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "IdPersonaOrigen", "", String.valueOf(relacionPersona.IdPersonaOrigen)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "IdPersonaDestino", "", String.valueOf(relacionPersona.IdPersonaDestino)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "IdEstado", "", relacionPersona.IdEstado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "FechaInicioRelacion", "", relacionPersona.FechaInicioRelacion));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "FechaFinRelacion", "", relacionPersona.FechaFinRelacion));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "PorcentajeParticipacion", "", relacionPersona.PorcentajeParticipacion.toPlainString()));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "RetiraDocumentos", "", relacionPersona.RetiraDocumentos));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "FinalidadRelacion", "", relacionPersona.FinalidadRelacion));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "CondicionesRelacion", "", relacionPersona.CondicionesRelacion));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "IdGradoVinculacionParental", "", String.valueOf(relacionPersona.IdGradoVinculacionParental)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "Ciudad", "", relacionPersona.Ciudad));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "Calle", "", relacionPersona.Calle));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "Numero", "", relacionPersona.Numero));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "Telefono", "", relacionPersona.Telefono));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "Email", "", relacionPersona.Email));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(relacionPersona.IdRelacionPersona, "Complemento", "", relacionPersona.Complemento));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = relacionPersona.IdRelacionPersona;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = relacionPersona.IdRelacionPersona;
			log.IdTipoRegistro = "CREACION_RELACION_PERSONA";
			log.EstadoRegistro = relacionPersona.IdEstadoAutorizacion;
			log.EstadoProceso = relacionPersona.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Relacion Persona";
			log.IdRegistro = String.valueOf(relacionPersona.IdRelacionPersona);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<RelacionPersona> relacionesPersona){
		logger.trace("INICIO::CrearRelacionPersonaMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = relacionesPersona
				.parallelStream()
				.map(relacionPersona->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = relacionPersona.IdInterno;
					entidad.IdEntidad = relacionPersona.IdRelacionPersona;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersonaMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}

}
