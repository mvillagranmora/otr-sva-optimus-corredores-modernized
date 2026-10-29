package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearConfiguracionPerfilComercialAdicional implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearConfiguracionPerfilComercialAdicional.class);
	
	private final String SQL_INSERT_CONFIGURACIONES_TABLA_INTERMEDIA 	= loadResourceToString("cl/bcs/plataforma/com/sql/CrearConfiguracionPerfilComercialAdicional_InsertConfiguracionesTablaIntermedia.sql");
	private final String SQL_SELECT_CONFIGURACIONES_EXISTENTES = loadResourceToString("cl/bcs/plataforma/com/sql/CrearConfiguracionPerfilComercialAdicional_SelectConfiguracionesExistentes.sql");
	private final String SQL_INSERT_CONFIGURACIONES = loadResourceToString("cl/bcs/plataforma/com/sql/CrearConfiguracionPerfilComercialAdicional_InsertConfiguraciones.sql");
	
	public static class EntradaCrearConfiguracionPerfilComercialAdicional extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<ConfiguracionPerfilComercialAdicional> p_ConfiguracionesPerfilComercialAdicional;
	}
	
	public static class SalidaCrearConfiguracionPerfilComercialAdicional extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaCrearConfiguracionPerfilComercialAdicional () {
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
		public Long IdTrabajo;
		
		public ConfiguracionPerfilComercialAdicional () {
			this.IdCategoriaInversionista = "";
			this.IdFormaCobro = "";
			this.CodigoPortafolio = "";
			this.IdPerfilComercialBase = "";
			this.IdPerfilCobros = "";
			this.IdPerfilProductos = "";
			this.PorDefecto = "N";
			this.IdEstado = "ACT";
			this.IdEmpresa = 0L;
			this.IdTrabajo = 0L;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearConfiguracionPerfilComercialAdicional jin = gson.fromJson(in.toString(), EntradaCrearConfiguracionPerfilComercialAdicional.class);
		SalidaCrearConfiguracionPerfilComercialAdicional out = logica(conn, jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private SalidaCrearConfiguracionPerfilComercialAdicional logica(Connection conn, EntradaCrearConfiguracionPerfilComercialAdicional entrada) throws Exception {
		logger.debug("INICIO::CrearConfiguracionPerfilComercialAdicional");
		long inicio = System.currentTimeMillis();
		
		SalidaCrearConfiguracionPerfilComercialAdicional salida = new SalidaCrearConfiguracionPerfilComercialAdicional();
		
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		List<ConfiguracionPerfilComercialAdicional> configuraciones = obtenerEntidadesConfiguraciones( conn, entrada, idTrabajo);
		
		if(configuraciones.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_NO_SE_INGRESARON_PERFILES_COMERCIALES_ADICIONALES",new HashMap<>(),conn,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
				
		List<ConfiguracionPerfilComercialAdicional> configuracionesExistentes = consultarConfiguracionesPerfilComercialAdicional(conn, configuraciones, idTrabajo);
		
		if(configuracionesExistentes.size() > 0) {
			HashMap<String,String> mapMsg = new HashMap<>(3);
			for(ConfiguracionPerfilComercialAdicional existente : configuracionesExistentes) {
				mapMsg.put("IdCategoriaInversionista", existente.IdCategoriaInversionista);
				mapMsg.put("IdFormaCobro", existente.IdFormaCobro);
				mapMsg.put("CodigoPortafolio", existente.CodigoPortafolio);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_PERFIL_COMERCIAL_ADICIONAL_EXISTENTE",mapMsg,conn,entrada.p_IdEmpresa));
				mapMsg.clear();
			}
			
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		insertarConfiguracionesPerfilComercialAdicional(conn, configuraciones);
		
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_PERFIL_COMERCIAL_ADICIONAL_INSERTADO",new HashMap<>(),conn,entrada.p_IdEmpresa));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearConfiguracionPerfilComercialAdicional {}", fin - inicio);
		return salida;
	}


	private List<ConfiguracionPerfilComercialAdicional> obtenerEntidadesConfiguraciones(Connection conn, EntradaCrearConfiguracionPerfilComercialAdicional entrada, Long idTrabajo) throws Exception {
		logger.debug("INICIO::CrearConfiguracionPerfilComercialAdicional.insertarConfiguracionesPerfilComercialAdicionalTablaIntermedia");
		
		List<ConfiguracionPerfilComercialAdicional> configuraciones = new ArrayList<ConfiguracionPerfilComercialAdicional>();
		
		configuraciones = entrada.p_ConfiguracionesPerfilComercialAdicional.stream().map(conf -> {
			ConfiguracionPerfilComercialAdicional configuracion = new ConfiguracionPerfilComercialAdicional();
			List<Long> idsPerfilComercialAdicional;
			try {
				idsPerfilComercialAdicional = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PerfilComercialAdicional", 1L);
				Long idPerfilComercialAdicional = idsPerfilComercialAdicional.get(0);
				configuracion.IdPerfilComercialAdicional = idPerfilComercialAdicional;
				configuracion.IdCategoriaInversionista = conf.IdCategoriaInversionista;
				configuracion.IdFormaCobro  = conf.IdFormaCobro;
				configuracion.CodigoPortafolio  = conf.CodigoPortafolio;
				configuracion.IdPerfilComercialBase = conf.IdPerfilComercialBase;
				configuracion.IdPerfilCobros = conf.IdPerfilCobros;
				configuracion.IdPerfilProductos = conf.IdPerfilProductos;
				configuracion.IdTrabajo = idTrabajo;
				configuracion.IdEstado = "ACT";
				configuracion.IdEmpresa = entrada.p_IdEmpresa;
				configuracion.PorDefecto = "N";
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return configuracion;
		}).collect(Collectors.toList());
		logger.debug("FIN::CrearConfiguracionPerfilComercialAdicional.insertarConfiguracionesPerfilComercialAdicionalTablaIntermedia {}");
		return configuraciones;
	}

	private void insertarConfiguracionesPerfilComercialAdicional(Connection conn, List<ConfiguracionPerfilComercialAdicional> configuraciones) throws Exception {
		logger.debug("INICIO::CrearConfiguracionPerfilComercialAdicional.insertarConfiguracionesPerfilComercialAdicional");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn,SQL_INSERT_CONFIGURACIONES,configuraciones,ConfiguracionPerfilComercialAdicional.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearConfiguracionPerfilComercialAdicional.insertarConfiguracionesPerfilComercialAdicional {}", fin - inicio);
	}
	
	private List<ConfiguracionPerfilComercialAdicional> consultarConfiguracionesPerfilComercialAdicional( Connection conn, List<ConfiguracionPerfilComercialAdicional> configuraciones, Long idTrabajo) throws Exception {
		logger.debug("INICIO::CrearConfiguracionPerfilComercialAdicional.consultarConfiguracionesPerfilComercialAdicional");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();

		sql.batch(conn, SQL_INSERT_CONFIGURACIONES_TABLA_INTERMEDIA, configuraciones, ConfiguracionPerfilComercialAdicional.class);
		
		HashMap<String,Object> params = new HashMap<>(1);
		params.put("IdTrabajo", idTrabajo);
		List<ConfiguracionPerfilComercialAdicional> existentes = sql.query(conn, SQL_SELECT_CONFIGURACIONES_EXISTENTES, params, ConfiguracionPerfilComercialAdicional.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearConfiguracionPerfilComercialAdicional.consultarConfiguracionesPerfilComercialAdicional {}", fin - inicio);
		
		return existentes;
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
			String lala = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_ConfiguracionesPerfilComercialAdicional\":[{\"IdCategoriaInversionista\":\"VISIONARIO\",\"IdFormaCobro\":\"OPCIÓN 1\",\"CodigoPortafolio\":\"0\",\"IdPerfilComercialBase\":\"MODELO_VISIONARIO\",\"IdPerfilCobros\":\"PERFIL VISIONARIO\",\"IdPerfilProductos\":\"VISIONARIO FULL\"}]}";
			EntradaCrearConfiguracionPerfilComercialAdicional ObjIn = gson.fromJson(lala, EntradaCrearConfiguracionPerfilComercialAdicional.class);
			CrearConfiguracionPerfilComercialAdicional demo = new CrearConfiguracionPerfilComercialAdicional();
			try {
				SalidaCrearConfiguracionPerfilComercialAdicional ObjOut = demo.logica(con, ObjIn);
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
