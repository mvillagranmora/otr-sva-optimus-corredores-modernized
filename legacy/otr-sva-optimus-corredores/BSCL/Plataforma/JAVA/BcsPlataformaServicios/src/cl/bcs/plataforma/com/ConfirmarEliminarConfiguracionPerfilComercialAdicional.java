package cl.bcs.plataforma.com;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
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

import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio.EntradaConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio.SalidaConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarEliminarConfiguracionPerfilComercialAdicional implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConfirmarEliminarConfiguracionPerfilComercialAdicional.class);
	
	private final String SQL_DELETE_MATRIZ_CONFIGURACION_ENROLAMIENTO = loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarEliminarConfiguracionPerfilComercialAdicional_DeleteMatrizConfiguracionEnrolamiento.sql");
    
	private final String ID_TIPO_ENTIDAD = "CLIENTE";

	public static class EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InPerfilComercial> p_IdsPerfilComercialAdicional;
		public String p_IdEstadoAutorizacion;

		public EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional() {
		    this.p_IdsPerfilComercialAdicional = new ArrayList<InPerfilComercial>();
		}
	}
	
	public static class InPerfilComercial extends BcsPojo {
		
		public Long IdPerfilComercialAdicional;

		public InPerfilComercial(Long idPerfilComercialAdicional) {
			IdPerfilComercialAdicional = idPerfilComercialAdicional;
		}
		
	}
	
	public static class SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional extends BcsPojo {
		
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional () {
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
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
		EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional jin = gson.fromJson(in.toString(), EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional.class);
		SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional out = logica(conn, jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}


	public SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional logica(Connection conn, EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional entrada) throws Exception {
		
		logger.debug("INICIO::ConfirmarEliminarConfiguracionPerfilComercialAdicional");
		
		long inicio = System.currentTimeMillis();
		SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional salida = new SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional();
		
		if (entrada.p_IdsPerfilComercialAdicional.isEmpty()) {
		    salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_PERFIL_COMERCIAL_ADICIONAL", null, conn, entrada.p_IdEmpresa));
		    return salida;
		}
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(conn, entrada);
	
		if (solicitudesCambios.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SOLICITUD_NO_EXISTE", null, conn, entrada.p_IdEmpresa));
		    return salida;
		}
		
		List<ConfiguracionPerfilComercialAdicional> configuracionPerfilComercialAdicionalEliminar = obtenerEntidadesConfiguracionPerfilComercialAdicional(solicitudesCambios, conn, entrada);

		salida = eliminarPerfilComercialAdicional(conn, configuracionPerfilComercialAdicionalEliminar, entrada.p_IdEmpresa, salida);
	
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio(conn, entrada, solicitudesCambios);
		salida.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		if( MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0 ) return salida;
				
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_PERFIL_COMERCIAL_ADICIONAL_ELIMINADO",new HashMap<>(),conn,entrada.p_IdEmpresa));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarEliminarConfiguracionPerfilComercialAdicional", fin - inicio);
		return salida;
		
	}

	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional entrada) throws Exception {
		
		Long IdPerfilComercialAdicional = entrada.p_IdsPerfilComercialAdicional.stream().findFirst().get().IdPerfilComercialAdicional;
	
		ConsultarRegistroSolicitudCambio consultarSolicitudes = new ConsultarRegistroSolicitudCambio();
		EntradaConsultarRegistroSolicitudCambio entradaConsultarRegistroSolicitud = new EntradaConsultarRegistroSolicitudCambio();
		entradaConsultarRegistroSolicitud.p_IdEntidadOrigen = IdPerfilComercialAdicional.toString();
		entradaConsultarRegistroSolicitud.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		SalidaConsultarRegistroSolicitudCambio salidaConsultarRegistroSolicitud = consultarSolicitudes.logica(con, entradaConsultarRegistroSolicitud);
	
		return salidaConsultarRegistroSolicitud.P_OUT_SOLICITUDES_CAMBIO;
		
    }
	
	private List<ConfiguracionPerfilComercialAdicional> obtenerEntidadesConfiguracionPerfilComercialAdicional(List<SolicitudCambio> solicitudesCambios, Connection con, EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional entrada) throws Exception {
		
		logger.info("INICIO::ConfirmarEliminarConfiguracionPerfilComercialAdicional.obtenerEntidadesConfiguracionPerfilComercialAdicional");
		long inicio = System.currentTimeMillis();
	
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio -> solicitudCambio.IdEntidadOrigenLong));
	
		List<ConfiguracionPerfilComercialAdicional> datosAActualizar = new ArrayList<>();
		ConfiguracionPerfilComercialAdicional datosConfiguracionPerfilComercialAdicional = new ConfiguracionPerfilComercialAdicional();
		for (Entry<Long, List<SolicitudCambio>> registro : solicitudesPorIdEntidad.entrySet()) {
		    List<SolicitudCambio> cambiosConfiguracion = registro.getValue();
		    datosConfiguracionPerfilComercialAdicional.IdPerfilComercialAdicional = (Long) cambiosConfiguracion.parallelStream().findFirst().get().IdEntidadOrigenLong;
	
		    for (SolicitudCambio solicitudCambio : cambiosConfiguracion) {
				Field campo = datosConfiguracionPerfilComercialAdicional.getClass().getField(solicitudCambio.CampoModificado);
				if (campo.getType().isAssignableFrom(String.class)) {
				    campo.set(datosConfiguracionPerfilComercialAdicional, solicitudCambio.ValorNuevo);
				    continue;
				}
				if (campo.getType().isAssignableFrom(Long.class)) {
				    campo.set(datosConfiguracionPerfilComercialAdicional, solicitudCambio.ValorNuevo == null ? null : Long.parseLong(solicitudCambio.ValorNuevo));
				    continue;
				}
				if (campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
				    campo.set(datosConfiguracionPerfilComercialAdicional, Fecha.toDateTime(solicitudCambio.ValorNuevo));
				    continue;
				}
				if (campo.getType().isAssignableFrom(java.sql.Date.class)) {
				    campo.set(datosConfiguracionPerfilComercialAdicional, Fecha.toDate(solicitudCambio.ValorNuevo));
				    continue;
				}
		    }
		    datosAActualizar.add(datosConfiguracionPerfilComercialAdicional);
		}
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarEliminarConfiguracionPerfilComercialAdicional.obtenerEntidadesConfiguracionPerfilComercialAdicional", fin - inicio);
	
		return datosAActualizar;
    }

	private SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional eliminarPerfilComercialAdicional( Connection conn, List<ConfiguracionPerfilComercialAdicional> configuracionPerfilComercialAdicionalEliminar, Long p_IdEmpresa, SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional salida ) throws Exception {
		
		logger.debug("INICIO::ConfirmarEliminarConfiguracionPerfilComercialAdicional.eliminarPerfilComercialAdicional");
		long inicio = System.currentTimeMillis();
	    SQL sql 	= new SQL();

		if( configuracionPerfilComercialAdicionalEliminar.size() > 0 ) 
			sql.batch(conn, SQL_DELETE_MATRIZ_CONFIGURACION_ENROLAMIENTO, configuracionPerfilComercialAdicionalEliminar, ConfiguracionPerfilComercialAdicional.class);	
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarEliminarConfiguracionPerfilComercialAdicional.eliminarPerfilComercialAdicional", fin - inicio);
		return salida;
	}
	
	private SalidaModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		
		logger.debug("INICIO::ConfirmarEliminarConfiguracionPerfilComercialAdicional.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		ModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio 				= new ModificarEstadoRegistroSolicitudCambio();
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificarEstadoRegistroSolicitudCambio = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdSolicitudCambio = solicitudesCambios.stream().findFirst().get().IdSolicitudCambio;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio.logica(con, entradaModificarEstadoRegistroSolicitudCambio);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarEliminarConfiguracionPerfilComercialAdicional.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
		
    }
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
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
			String lala = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_ConfiguracionesPerfilComercialAdicional\":[{\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdFormaCobro\":\"OPCION 6\",\"CodigoPortafolio\":\"APV\",\"IdPerfilComercialBase\":\"EXPRESS ARRIESGADO\",\"IdPerfilCobros\":\"000011112222\",\"IdPerfilProductos\":\"PRUEBA AAA\"}]}";
			EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional ObjIn = gson.fromJson(lala, EntradaConfirmarEliminarConfiguracionPerfilComercialAdicional.class);
			ConfirmarEliminarConfiguracionPerfilComercialAdicional demo = new ConfirmarEliminarConfiguracionPerfilComercialAdicional();
			try {
				SalidaConfirmarEliminarConfiguracionPerfilComercialAdicional ObjOut = demo.logica(con, ObjIn);
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
