package cl.bcs.socionegocio.rel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.rel.ConfirmarCrearRelacionPersona.EntradaConfirmarCrearRelacionPersona;
import cl.bcs.socionegocio.rel.ConfirmarCrearRelacionPersona.SalidaConfirmarCrearRelacionPersona;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearRelacionPersona implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearRelacionPersona.class);
	
	private final String SQL_SELECT_VALIDAR_PERSONA_EXISTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_SelectValidacionPersonaExiste.sql");
	private final String SQL_SELECT_VALIDAR_RELACION_EXISTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_SelectValidacionRelacionExiste.sql");
	private final String SQL_SELECT_VALIDAR_RELACION_ARBITRADOR_EXISTE = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_SelectValidacionRelacionArbitradorExiste.sql");
	private final String SQL_SELECT_VALIDAR_EMPLEADO_RELACION = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_SelectValidacionEmpleadoRelacion.sql");
	private final String SQL_SELECT_REPRESENTANTE_LEGAL = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_SelectRepresentanteLegal.sql");
	private final String SQL_UPDATE_REPRESENTANTE_LEGAL = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_UpdateRepresentanteLegal.sql");
	private final String SQL_INSERT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_InsertRelacionPersona.sql");
	private final String SQL_SELECT_DATOS_PERSONA = loadResourceToString("cl/bcs/socionegocio/rel/sql/CrearRelacionPersona_SelectDatosPersona.sql");
	
	private final String ID_CONCEPTO = "NUEREL";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	
	public static class EntradaCrearRelacionPersona extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoRelacion;
		public Long p_IdPersonaOrigen;
		public Long p_IdPersonaDestino;
		public String p_IdEstado;
		public String p_FechaInicioRelacion;
		public String p_FechaFinRelacion;
		public BigDecimal p_PorcentajeParticipacion;
		public String p_RetiraDocumentos;
		public String p_FinalidadRelacion;
		public String p_CondicionesRelacion;
		public Long p_IdGradoVinculacionParental;
		public String p_Ciudad;
		public String p_Calle;
		public String p_Numero;
		public String p_Telefono;
		public String p_Email;
		public String p_Complemento;
		
		public EntradaCrearRelacionPersona() {
			this.p_RetiraDocumentos = "";
			this.p_FinalidadRelacion = "";
			this.p_CondicionesRelacion = "";
			this.p_IdGradoVinculacionParental = 0L;
			this.p_Ciudad = "";
			this.p_Calle = "";
			this.p_Numero = "";
			this.p_Telefono = "";
			this.p_Email = "";
			this.p_Complemento = "";
		}
	}
	
	public static class SalidaCrearRelacionPersona extends BcsPojo{
		public Long P_OUT_REFID;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearRelacionPersona() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	public static class RelacionPersona extends BcsPojo{
		public Long IdRelacionPersona;
		public String IdTipoRelacion;
		public Long IdPersonaOrigen;
		public Long IdPersonaDestino;
		public String IdEstado;
		public java.sql.Timestamp FechaInicioRelacion;
		public java.sql.Timestamp FechaFinRelacion;
		public java.sql.Timestamp FechaIngreso;
		public Long IdUsuarioCreador;
		public String IdEstadoAutorizacion;
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
	}
	
	public static class Persona extends BcsPojo{
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearRelacionPersona jin = gson.fromJson(in.toString(), EntradaCrearRelacionPersona.class);
		SalidaCrearRelacionPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearRelacionPersona logica(Connection con, EntradaCrearRelacionPersona entrada) throws Exception {
		logger.info("INICIO::CrearRelacionPersona");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearRelacionPersona out = new SalidaCrearRelacionPersona();
		
		Validacion validacionPersona = validarPersona(con, entrada.p_IdEmpresa, entrada.p_IdPersonaDestino);
		Validacion validacionRelacionExiste = validarRelacionExiste(con, entrada);
		Validacion validacionRelacion = validarRelacion(con, entrada.p_IdEmpresa, entrada.p_IdPersonaOrigen, entrada.p_IdPersonaDestino);
		Validacion validacionRelacionArbitradorExiste = validarRelacionArbitradorExiste(con, entrada.p_IdEmpresa, entrada.p_IdPersonaOrigen, entrada.p_IdTipoRelacion);
		Validacion validacionEmpleado = validarEmpleado(con, entrada.p_IdEmpresa, entrada.p_IdPersonaDestino, entrada.p_IdTipoRelacion);
		
		out = agregarValidacionSalida(out, validacionPersona);
		out = agregarValidacionSalida(out, validacionRelacionExiste);
		out = agregarValidacionSalida(out, validacionRelacion);
		out = agregarValidacionSalida(out, validacionRelacionArbitradorExiste);
		out = agregarValidacionSalida(out, validacionEmpleado);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		actualizarRepresentanteLegal(con, entrada.p_IdPersonaOrigen, entrada.p_IdPersonaDestino, entrada.p_IdTipoRelacion);
		Long idRelacionPersona = insertarRelacionPersona(con, entrada);
		
		String requiereAutorizacion = obtenerRequiereAutorizacion(con, entrada.p_IdEmpresa);
		
		if("S".equals(requiereAutorizacion)) {
			out = crearEventoAutorizacion(con, entrada, idRelacionPersona);
		} else {
			out = confirmarCrearRelacionPersona(con, entrada, idRelacionPersona);
		}
		
		out.P_OUT_REFID = idRelacionPersona;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearRelacionPersona", finServicio - inicioServicio);
		return out;
	}
	
	private Validacion validarPersona(Connection con, Long idEmpresa, Long idPersonaDestino) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.validarPersona");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idPersonaDestino);
		SQL sql = new SQL();
		Long countPersona = sql.countQuery(con, SQL_SELECT_VALIDAR_PERSONA_EXISTE, parametros);
		
		Validacion validacion = new Validacion();
		if(countPersona.equals(0L)) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERSONA_RELACIONADA_NO_EXISTE",new HashMap<String,String>(),con,idEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.validarPersona", fin - inicio);
		return validacion;
	}
	
	private Validacion validarRelacionExiste(Connection con, EntradaCrearRelacionPersona entrada) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.validarRelacionExiste");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("IdPersonaOrigen", entrada.p_IdPersonaOrigen);
		parametros.put("IdPersonaDestino", entrada.p_IdPersonaDestino);
		parametros.put("IdTipoRelacion", entrada.p_IdTipoRelacion);
		
		SQL sql = new SQL();
		Long countRelacion = sql.countQuery(con, SQL_SELECT_VALIDAR_RELACION_EXISTE, parametros);
		
		Validacion validacion = new Validacion();
		if(countRelacion > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"RELACION_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.validarRelacionExiste", fin - inicio);
		return validacion;
	}
	
	private Validacion validarRelacion(Connection con, Long idEmpresa, Long idPersonaOrigen, Long idPersonaDestino) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.validarRelacion");
		long inicio = System.currentTimeMillis();
		
		Validacion validacion = new Validacion();
		if(idPersonaOrigen.equals(idPersonaDestino)) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"RELACION_INVALIDA",new HashMap<String,String>(),con,idEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.validarRelacion", fin - inicio);
		return validacion;
	}
	
	private Validacion validarRelacionArbitradorExiste(Connection con, Long idEmpresa, Long idPersonaOrigen, String idTipoRelacion) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.validarRelacionArbitradorExiste");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdPersonaOrigen", idPersonaOrigen);
		parametros.put("IdTipoRelacion", idTipoRelacion);
		SQL sql = new SQL();
		Long countRelacion = sql.countQuery(con, SQL_SELECT_VALIDAR_RELACION_ARBITRADOR_EXISTE, parametros);
		
		Validacion validacion = new Validacion();
		if(countRelacion > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"RELACION_YA_EXISTE",new HashMap<String,String>(),con,idEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.validarRelacionArbitradorExiste", fin - inicio);
		return validacion;
	}
	
	private Validacion validarEmpleado(Connection con, Long idEmpresa, Long idPersonaDestino, String idTipoRelacion) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.validarEmpleado");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idPersonaDestino);
		SQL sql = new SQL();
		Long countEmpleado = sql.countQuery(con, SQL_SELECT_VALIDAR_EMPLEADO_RELACION, parametros);
		
		Validacion validacion = new Validacion();
		if(countEmpleado.equals(0L) && esRelacionArbitrador(idTipoRelacion)) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERSONA_RELACIONADA_EMPLEADO",new HashMap<String,String>(),con,idEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.validarEmpleado", fin - inicio);
		return validacion;
	}
	
	private Boolean esRelacionArbitrador(String idTipoRelacion) {
		logger.trace("INICIO::CrearRelacionPersona.esRelacionArbitrador");
		long inicio = System.currentTimeMillis();
		Boolean esRelacionArbitrador = "AR1".equals(idTipoRelacion) || "AR2".equals(idTipoRelacion);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearRelacionPersona.esRelacionArbitrador", fin - inicio);
		return esRelacionArbitrador;
	}
	
	private SalidaCrearRelacionPersona agregarValidacionSalida(SalidaCrearRelacionPersona salida, Validacion validacion) {
		logger.trace("INICIO::CrearRelacionPersona.agregarValidacionSalida");
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		logger.trace("FIN::CrearRelacionPersona.agregarValidacionSalida");
		return salida;
	}
	
	private void actualizarRepresentanteLegal(Connection con, Long idPersonaOrigen, Long idPersonaDestino, String idTipoRelacion) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.actualizarRepresentanteLegal");
		long inicio = System.currentTimeMillis();
		if("REP".equals(idTipoRelacion)) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(1);
			parametros.put("IdPersonaOrigen", idPersonaOrigen);
			SQL sql = new SQL();
			Long countRepresentanteLegal = sql.countQuery(con, SQL_SELECT_REPRESENTANTE_LEGAL, parametros);
			
			if(countRepresentanteLegal.equals(0L)) {
				RelacionPersona relacionRepresentanteLegal = new RelacionPersona();
				relacionRepresentanteLegal.IdPersonaDestino = idPersonaDestino;
				relacionRepresentanteLegal.IdPersonaOrigen = idPersonaOrigen;
				List<RelacionPersona> relaciones = Arrays.asList(relacionRepresentanteLegal);
				sql.batch(con, SQL_UPDATE_REPRESENTANTE_LEGAL, relaciones, RelacionPersona.class);
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.actualizarRepresentanteLegal", fin - inicio);
	}
	
	private Long insertarRelacionPersona(Connection con, EntradaCrearRelacionPersona entrada) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.insertarRelacionPersona");
		long inicio = System.currentTimeMillis();
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		List<Long> secuenciasRelacionPersona = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona", 1L);
		Long idRelacionPersona = secuenciasRelacionPersona.get(0);
		
		RelacionPersona relacionPersona = new RelacionPersona();
		relacionPersona.IdRelacionPersona = idRelacionPersona;
		relacionPersona.IdTipoRelacion = entrada.p_IdTipoRelacion;
		relacionPersona.IdPersonaOrigen = entrada.p_IdPersonaOrigen;
		relacionPersona.IdPersonaDestino = entrada.p_IdPersonaDestino;
		relacionPersona.IdEstado = entrada.p_IdEstado;
		relacionPersona.FechaInicioRelacion = Fecha.toDateTime(entrada.p_FechaInicioRelacion);
		relacionPersona.FechaFinRelacion = Fecha.toDateTime(entrada.p_FechaFinRelacion);
		relacionPersona.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
		relacionPersona.IdUsuarioCreador = entrada.p_IdUsuario;
		relacionPersona.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
		relacionPersona.PorcentajeParticipacion = entrada.p_PorcentajeParticipacion;
		relacionPersona.RetiraDocumentos = entrada.p_RetiraDocumentos;
		relacionPersona.FinalidadRelacion = entrada.p_FinalidadRelacion;
		relacionPersona.CondicionesRelacion = entrada.p_CondicionesRelacion;
		relacionPersona.IdGradoVinculacionParental = entrada.p_IdGradoVinculacionParental;
		relacionPersona.Ciudad = entrada.p_Ciudad;
		relacionPersona.Calle = entrada.p_Calle;
		relacionPersona.Numero = entrada.p_Numero;
		relacionPersona.Telefono = entrada.p_Telefono;
		relacionPersona.Email = entrada.p_Email;
		relacionPersona.Complemento = entrada.p_Complemento;
		
		List<RelacionPersona> relacionesPersona = Arrays.asList(relacionPersona);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_RELACION_PERSONA, relacionesPersona, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.insertarRelacionPersona", fin - inicio);
		return idRelacionPersona;
	}
	
	private String obtenerRequiereAutorizacion(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.obtenerRequiereAutorizacion");
		long inicio = System.currentTimeMillis();
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO);
		if(requiereAutorizacion == null) requiereAutorizacion = "N";
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.obtenerRequiereAutorizacion", fin - inicio);
		return requiereAutorizacion;
	}
	
	private SalidaCrearRelacionPersona crearEventoAutorizacion(Connection con, EntradaCrearRelacionPersona entrada, Long idRelacionPersona) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		Persona datosPersona = obtenerDatosPersona(con, entrada.p_IdPersonaDestino);
		SalidaCrearRelacionPersona salidaCrearRelacionPersona = new SalidaCrearRelacionPersona();
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdTipoEvento = ID_CONCEPTO;
		evento.IdModuloSolicita = entrada.p_IdModulo;
		evento.IdUsuarioSolicita = entrada.p_IdUsuario;
		evento.Fecha = fechaSistema;
		evento.IdConcepto = ID_CONCEPTO;
		evento.NumeroOperacionConcepto = String.valueOf(idRelacionPersona);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = entrada.p_IdPersonaOrigen;
		evento.Glosa = datosPersona.DocumentoIdentidad + "/" + datosPersona.NombreCompleto;
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearRelacionPersona.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaCrearRelacionPersona.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaCrearRelacionPersona.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaCrearRelacionPersona.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearRelacionPersona.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.crearEventoAutorizacion", fin - inicio);
		return salidaCrearRelacionPersona;	
	}
	
	private Persona obtenerDatosPersona(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.obtenerDatosPersona");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idPersona);
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_DATOS_PERSONA, parametros, Persona.class);
		Persona persona = personas.parallelStream().findFirst().orElse(new Persona());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.obtenerDatosPersona", fin - inicio);
		return persona;
		
	}
	
	private SalidaCrearRelacionPersona confirmarCrearRelacionPersona(Connection con, EntradaCrearRelacionPersona entrada, Long idRelacionPersona) throws Exception {
		logger.debug("INICIO::CrearRelacionPersona.confirmarCrearRelacionPersona");
		long inicio = System.currentTimeMillis();
		EntradaConfirmarCrearRelacionPersona entradaConfirmarCrearRelacionPersona = new EntradaConfirmarCrearRelacionPersona();
		entradaConfirmarCrearRelacionPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarCrearRelacionPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarCrearRelacionPersona.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarCrearRelacionPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarCrearRelacionPersona.p_IdRelacionPersona = idRelacionPersona;
		entradaConfirmarCrearRelacionPersona.p_IdEstadoAutorizacion = "ING";
		
		ConfirmarCrearRelacionPersona confirmarCrearRelacionPersona = new ConfirmarCrearRelacionPersona();
		SalidaConfirmarCrearRelacionPersona salidaConfirmarCrearRelacionPersona = confirmarCrearRelacionPersona.logica(con, entradaConfirmarCrearRelacionPersona);
		
		SalidaCrearRelacionPersona salidaCrearRelacionPersona = new SalidaCrearRelacionPersona();
		salidaCrearRelacionPersona.P_OUT_ERRORESCONTADOR = salidaConfirmarCrearRelacionPersona.P_OUT_ERRORESCONTADOR;
		salidaCrearRelacionPersona.P_OUT_MENSAJES = salidaConfirmarCrearRelacionPersona.P_OUT_MENSAJES;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacionPersona.confirmarCrearRelacionPersona", fin - inicio);
		return salidaCrearRelacionPersona;
	}
	
	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoRelacion\":\"AR2\",\"p_IdPersonaOrigen\":214068,\"p_IdPersonaDestino\":1752527,\"p_IdEstado\":\"ACT\",\"p_FechaInicioRelacion\":\"2022-10-10 03:00:00\",\"p_FechaFinRelacion\":\"2023-10-09 03:00:00\",\"p_PorcentajeParticipacion\":0,\"p_RetiraDocumentos\":\"N\",\"p_FinalidadRelacion\":\"\",\"p_CondicionesRelacion\":\"\",\"p_IdGradoVinculacionParental\":0,\"p_Ciudad\":\"SANTIAGO\",\"p_Calle\":\"AVENIDA EL BOSQUE NORTE\",\"p_Numero\":\"0177\",\"p_Telefono\":\"+562 2450 1600\",\"p_Email\":\"BLOCKCHAINUSR2@BCSBLOCK.COM\",\"p_Complemento\":\"\",\"p_TipoRelacionUsaPorcentajeParticipacion\":\"N\",\"p_TipoRelacionEsIndefinida\":\"N\",\"p_TipoRelacionUsaFinalidadRelacion\":\"N\",\"p_TipoRelacionUsaCondicionesRelacion\":\"N\",\"p_TipoRelacionUsaGradoVinculacionParental\":\"N\"}";
			
			EntradaCrearRelacionPersona Objin = gson.fromJson(json, EntradaCrearRelacionPersona.class);
			CrearRelacionPersona demo = new CrearRelacionPersona();

			System.out.println("ENTRADA:" + Objin.toString());

			try {
				SalidaCrearRelacionPersona ObjOut = demo.logica(conn, Objin);
				System.out.println("SALIDA:" + ObjOut);
				conn.commit();
				//conn.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				conn.rollback();
			}
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}
