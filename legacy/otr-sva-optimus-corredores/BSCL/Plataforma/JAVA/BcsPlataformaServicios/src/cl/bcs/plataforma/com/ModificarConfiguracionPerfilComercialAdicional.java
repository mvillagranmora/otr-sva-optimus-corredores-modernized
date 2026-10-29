package cl.bcs.plataforma.com;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.com.ConfirmarModificarConfiguracionPerfilComercialAdicional.ConfiguracionPerfilComercialAdicional;
import cl.bcs.plataforma.com.ConfirmarModificarConfiguracionPerfilComercialAdicional.EntradaConfirmarModificarConfiguracionPerfilComercialAdicional;
import cl.bcs.plataforma.com.ConfirmarModificarConfiguracionPerfilComercialAdicional.InPerfilComercial;
import cl.bcs.plataforma.com.ConfirmarModificarConfiguracionPerfilComercialAdicional.SalidaConfirmarModificarConfiguracionPerfilComercialAdicional;

import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ConfirmarModificarClienteDatosGeneralesMasivo.EntradaConfirmarModificarClienteDatosGeneralesMasivo;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.RegistroCambio;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.SalidaModificarClienteDatosGenerales;
import cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarConfiguracionPerfilComercialAdicional implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarConfiguracionPerfilComercialAdicional.class);
	
	private final String SQL_SELECT_MATRIZ_CONFIGURACION_ENROLAMIENTO 			= loadResourceToString("cl/bcs/plataforma/com/sql/ModificarConfiguracionPerfilComercialAdicional_SelectMatrizConfiguracionEnrolamiento.sql");
	private final String SQL_SELECT_VALIDAR_CONFIGURACION_MATRIZ_ENROLAMIENTO 	= loadResourceToString("cl/bcs/plataforma/com/sql/ModificarConfiguracionPerfilComercialAdicional_SelectValidarConfiguracionMatrizEnrolamiento.sql");
	
	private final String ID_CONCEPTO = "MODMCE";
    private final String ID_ESTADO_AUTORIZACION = "PEN";
    private final String ID_TIPO_ENTIDAD = "CLIENTE";
	
	public static class EntradaModificarConfiguracionPerfilComercialAdicional extends BcsPojo {
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPerfilComercialAdicional;
		public List<RegistroCambio> p_RegistroCambio;

	}
	
	public static class SalidaModificarConfiguracionPerfilComercialAdicional extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_REFID;
		
		public SalidaModificarConfiguracionPerfilComercialAdicional () {
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REFID = 0L; 
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class ConfiguracionPerfilComercialAdicional extends BcsPojo {
    	public Long IdPerfilComercialAdicional;
		public String IdCategoriaInversionista;
		public String IdFormaCobro;
		public String CodigoPortafolio;
		public String IdPerfilComercialBase;
		public String IdPerfilCobros;
		public String IdPerfilProductos;
		public String PorDefecto;
		public String IdEstado;
		public Long IdEmpresa;
		
		public ConfiguracionPerfilComercialAdicional () {
			this.PorDefecto = "N";
			this.IdEstado = "ACT";
			this.IdEmpresa = 1L;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarConfiguracionPerfilComercialAdicional jin = gson.fromJson(in.toString(), EntradaModificarConfiguracionPerfilComercialAdicional.class);
		SalidaModificarConfiguracionPerfilComercialAdicional out = logica(conn, jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	private SalidaModificarConfiguracionPerfilComercialAdicional logica(Connection conn, EntradaModificarConfiguracionPerfilComercialAdicional entrada) throws Exception {
		
		logger.debug("INICIO::ModificarConfiguracionPerfilComercialAdicional");
		
		long inicio 															= System.currentTimeMillis();
		SalidaModificarConfiguracionPerfilComercialAdicional salida 			= new SalidaModificarConfiguracionPerfilComercialAdicional();
		List<ConfiguracionPerfilComercialAdicional> perfilComercialModificar 	= obtenerPerfilComercialModificar( conn, entrada.p_IdPerfilComercialAdicional );
		ConfiguracionPerfilComercialAdicional perfilComercialModificarAValidar	= new ConfiguracionPerfilComercialAdicional();
		
		if (perfilComercialModificar.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ENTIDAD_NO_EXISTE", null, conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		if (entrada.p_RegistroCambio.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", null, conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		perfilComercialModificarAValidar = Utils.copyPojo(perfilComercialModificar.parallelStream().findFirst().orElse(null), perfilComercialModificarAValidar);
		if( validarConfiguracionAInsertar(conn, entrada.p_RegistroCambio, perfilComercialModificarAValidar) ) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_PERFIL_COMERCIAL_ADICIONAL_EXISTENTE", null, conn, entrada.p_IdEmpresa));
			return salida;
		} 
		
		List<Long> idSolicitudesCambio = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = idSolicitudesCambio.get(0);
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(conn, idSolicitudCambio, perfilComercialModificar, entrada);

		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();

		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;

		servicioCrearRegistroSolicitudCambio.logica(conn, inCrearRegistroSolicitudCambio);

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, entrada.p_IdEmpresa, ID_CONCEPTO);
	
		if ("S".equals(requiereAutorizacion)) {
			salida = crearEventoAutorizacion(conn, entrada, perfilComercialModificar.parallelStream().findFirst().orElse(null), salida);
		} else {
			salida = modificarPerfilComercial(conn, entrada, salida);
		}

		salida.P_OUT_REFID = idSolicitudCambio;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConfiguracionPerfilComercialAdicional", fin - inicio);
		return salida;
	}

	private List<ConfiguracionPerfilComercialAdicional> obtenerPerfilComercialModificar( Connection con, Long p_IdPerfilComercialAdicional) throws SQLException {

		logger.debug("INICIO::ModificarConfiguracionPerfilComercialAdicional.obtenerPerfilComercialModificar");
		
		SQL sql 																= new SQL();
		long inicio 															= System.currentTimeMillis();
		HashMap<String, Object> params 											= new HashMap<String, Object>(1);
		params.put("IdPerfilComercialAdicional", p_IdPerfilComercialAdicional);
		List<ConfiguracionPerfilComercialAdicional> perfilComercialModificar 	= sql.query(con, SQL_SELECT_MATRIZ_CONFIGURACION_ENROLAMIENTO, params, ConfiguracionPerfilComercialAdicional.class);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarConfiguracionPerfilComercialAdicional.obtenerPerfilComercialModificar", fin - inicio);
		
		return perfilComercialModificar;
	}
	
	private boolean validarConfiguracionAInsertar( Connection con, List<RegistroCambio> solicitudesCambios, ConfiguracionPerfilComercialAdicional datosConfiguracionPerfilComercialAdicional ) throws Exception {
		
		logger.info("INICIO::ModificarConfiguracionPerfilComercialAdicional.validarConfiguracionAInsertar");
		long inicio 					= System.currentTimeMillis();
		SQL sql 						= new SQL();
		HashMap<String, Object> params 	= new HashMap<String, Object>(1);


		for ( RegistroCambio registro : solicitudesCambios) {
			Field campo = datosConfiguracionPerfilComercialAdicional.getClass().getField(registro.CampoModificado);
			if (campo.getType().isAssignableFrom(String.class)) {
			    campo.set(datosConfiguracionPerfilComercialAdicional, registro.ValorNuevo);
			    continue;
			}
			if (campo.getType().isAssignableFrom(Long.class)) {
			    campo.set(datosConfiguracionPerfilComercialAdicional, registro.ValorNuevo == null ? null : Long.parseLong(registro.ValorNuevo));
			    continue;
			}
			if (campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
			    campo.set(datosConfiguracionPerfilComercialAdicional, Fecha.toDateTime(registro.ValorNuevo));
			    continue;
			}
			if (campo.getType().isAssignableFrom(java.sql.Date.class)) {
			    campo.set(datosConfiguracionPerfilComercialAdicional, Fecha.toDate(registro.ValorNuevo));
			    continue;
			}
		}
		
		params.put("IdPerfilComercialAdicional", datosConfiguracionPerfilComercialAdicional.IdPerfilComercialAdicional);
		params.put("CodigoPortafolio", datosConfiguracionPerfilComercialAdicional.CodigoPortafolio);
		params.put("IdCategoriaInversionista", datosConfiguracionPerfilComercialAdicional.IdCategoriaInversionista);
		params.put("IdFormaCobro", datosConfiguracionPerfilComercialAdicional.IdFormaCobro);

		List<ConfiguracionPerfilComercialAdicional> perfilComercialModificarAValidar = sql.query(con, SQL_SELECT_VALIDAR_CONFIGURACION_MATRIZ_ENROLAMIENTO, params, ConfiguracionPerfilComercialAdicional.class);
		
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConfiguracionPerfilComercialAdicional.validarConfiguracionAInsertar", fin - inicio);
	
		return perfilComercialModificarAValidar.size() > 0;
    }
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<ConfiguracionPerfilComercialAdicional> perfilComercialModificar, EntradaModificarConfiguracionPerfilComercialAdicional entrada) throws Exception {
		
		logger.trace("INICIO::ModificarConfiguracionPerfilComercialAdicional.obtenerRegistroSolicitudesCambio");
		
		long inicio 											= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		for (RegistroCambio registroCambio : entrada.p_RegistroCambio) {
			
			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(perfilComercialModificar.get(0), ConfiguracionPerfilComercialAdicional.class, registroCambio.CampoModificado);
			if (valorAntiguo.Valor.equalsIgnoreCase(registroCambio.ValorNuevo)) continue; 
			
		    RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
		    registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
		    registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
		    registroSolicitudCambio.IdEntidadOrigen = String.valueOf(entrada.p_IdPerfilComercialAdicional);
		    registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
		    
		    
			
		    registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
		    registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
		    registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
		    registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
	
		    registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarConfiguracionPerfilComercialAdicional.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
		
    }

	private SalidaModificarConfiguracionPerfilComercialAdicional crearEventoAutorizacion(Connection con, EntradaModificarConfiguracionPerfilComercialAdicional entrada, ConfiguracionPerfilComercialAdicional perfilComercialModificar, SalidaModificarConfiguracionPerfilComercialAdicional salida) throws Exception {
		
		logger.debug("INICIO::ModificarConfiguracionPerfilComercialAdicional.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
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
		evento.IdCliente = perfilComercialModificar.IdPerfilComercialAdicional;
		evento.FechaOperacionConcepto = fechaSistema;
		evento.Glosa = "Modificacion en Matriz Configuracion Enrolamiento";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salida.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salida.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salida.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConfiguracionPerfilComercialAdicional.crearEventoAutorizacion", fin - inicio);
		return salida;
    }
	
	private SalidaModificarConfiguracionPerfilComercialAdicional modificarPerfilComercial( Connection con, EntradaModificarConfiguracionPerfilComercialAdicional entrada, SalidaModificarConfiguracionPerfilComercialAdicional salida ) throws Exception {
		
		logger.debug("INICIO::ModificarConfiguracionPerfilComercialAdicional.modificarPerfilComercial");
		
		long inicio = System.currentTimeMillis();
		EntradaConfirmarModificarConfiguracionPerfilComercialAdicional entradaConfiguracionPerfilComercialAdicional = new EntradaConfirmarModificarConfiguracionPerfilComercialAdicional();
		InPerfilComercial entradaIdPerfilComercialAdicional = new InPerfilComercial(entrada.p_IdPerfilComercialAdicional);
		
		entradaConfiguracionPerfilComercialAdicional.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfiguracionPerfilComercialAdicional.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfiguracionPerfilComercialAdicional.p_IdModulo = entrada.p_IdModulo;
		entradaConfiguracionPerfilComercialAdicional.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfiguracionPerfilComercialAdicional.p_IdsPerfilComercialAdicional.add(entradaIdPerfilComercialAdicional);
		entradaConfiguracionPerfilComercialAdicional.p_IdEstadoAutorizacion = "MOD";
		
		ConfirmarModificarConfiguracionPerfilComercialAdicional confirmarModificarConfiguracionPerfilComercialAdicional = new ConfirmarModificarConfiguracionPerfilComercialAdicional();
		
		SalidaConfirmarModificarConfiguracionPerfilComercialAdicional salidaConfirmarModificarConfiguracionPerfilComercialAdicional = confirmarModificarConfiguracionPerfilComercialAdicional.logica(con, entradaConfiguracionPerfilComercialAdicional);
		
		salida.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarConfiguracionPerfilComercialAdicional.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES = salidaConfirmarModificarConfiguracionPerfilComercialAdicional.P_OUT_MENSAJES;
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarConfiguracionPerfilComercialAdicional.modificarPerfilComercial", fin - inicio);
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String request = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9,\r\n"
					+ "  \"p_IdModulo\": \"SOC\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "  \"p_ConfiguracionesPerfilComercialAdicional\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdCategoriaInversionista\": \"ARRIESGADO\",\r\n"
					+ "      \"IdFormaCobro\": \"Opcion 1\",\r\n"
					+ "      \"IdPerfilComercialBase\": \"NO APV_OPCION 1_ARRIESGADO\",\r\n"
					+ "      \"IdPerfilCobros\": \"COBRO 0,10% - SIN GASTO\",\r\n"
					+ "      \"IdPerfilProductos\": \"REG GENERAL_ARRIESGADO\",\r\n"
					+ "      \"IdEstado\": \"ACR\",\r\n"
					+ "      \"PorDefecto\": \"N\",\r\n"
					+ "      \"CodigoPortafolio\": \"0\"\r\n"
					+ "    }\r\n"
					+ "  ]\r\n"
					+ "}";
			String lala = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPerfilComercialAdicional\":42,\"p_RegistroCambio\":[{\"CampoModificado\":\"IdPerfilComercialBase\",\"ValorNuevo\":\"EXPRESS ARRIESGADO\"}]}";
			EntradaModificarConfiguracionPerfilComercialAdicional ObjIn = gson.fromJson(lala, EntradaModificarConfiguracionPerfilComercialAdicional.class);
			ModificarConfiguracionPerfilComercialAdicional demo = new ModificarConfiguracionPerfilComercialAdicional();
			try {
				SalidaModificarConfiguracionPerfilComercialAdicional ObjOut = demo.logica(con, ObjIn);
				//con.commit();
				con.rollback();
 				System.out.println(ObjOut.toString());
			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}
