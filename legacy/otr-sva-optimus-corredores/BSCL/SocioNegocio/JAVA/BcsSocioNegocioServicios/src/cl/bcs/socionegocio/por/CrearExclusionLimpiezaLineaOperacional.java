package cl.bcs.socionegocio.por;

import java.sql.Connection;
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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearExclusionLimpiezaLineaOperacional implements JavaCall{

	static final Logger logger = LogManager.getLogger(CrearExclusionLimpiezaLineaOperacional.class);

	private final String SQL_SELECT_EXCLUSION_LIMPIEZA_LINEA_OPERACIONAL = loadResourceToString("cl/bcs/socionegocio/por/sql/CrearExclusionLimpiezaLineaOperacional_SelectExclusionLimpieza.sql");
	private final String SQL_UPDATE_EXCLUSION_LIMPIEZA_LINEA_OPERACIONAL = loadResourceToString("cl/bcs/socionegocio/por/sql/CrearExclusionLimpiezaLineaOperacional_UpdateExclusionLimpieza.sql");
	
	public static class EntradaCrearExclusionLimpiezaLineaOperacional extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Portafolio> p_Portafolios;
		
		public EntradaCrearExclusionLimpiezaLineaOperacional () {
			this.p_IdEmpresa = 1L;
			this.p_IdModulo = "SOC";
			this.p_IdUsuario = 0L;
			this.p_UsaReglasNegocio = "N";
			this.p_Portafolios = new ArrayList<Portafolio>();
		}
	}
	
	public static class Portafolio extends BcsPojo {
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		
		public Portafolio () {
			this.IdPortafolio = 0L;
			this.CodigoPortafolio = "";
			this.DocumentoIdentidad = "";
			this.NombreCompleto = "";
		}
	}
	
	public static class SalidaCrearExclusionLimpiezaLineaOperacional extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearExclusionLimpiezaLineaOperacional () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public SalidaCrearExclusionLimpiezaLineaOperacional logica(Connection con, EntradaCrearExclusionLimpiezaLineaOperacional entrada) throws Exception {
		logger.info("INICIO::CrearExclusionLimpiezaLineaOperacional");
	    long inicio = System.currentTimeMillis();
	    
	    SalidaCrearExclusionLimpiezaLineaOperacional salida = new SalidaCrearExclusionLimpiezaLineaOperacional();
		
	    if(entrada.p_Portafolios.isEmpty()) {
	    	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ENTRADA_SIN_PORTAFOLIOS", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
	    	salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
	    	long fin = System.currentTimeMillis();
	    	logger.info("FIN::CrearExclusionLimpiezaLineaOperacional", fin - inicio );
			return salida;
	    }
	    
	    salida.P_OUT_MENSAJES.addAll(consultarExclusiones(con, entrada.p_IdEmpresa ,entrada.p_Portafolios));
	    
	    if(!salida.P_OUT_MENSAJES.isEmpty()) {
	    	salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
	    	long fin = System.currentTimeMillis();
	    	logger.info("FIN::CrearExclusionLimpiezaLineaOperacional", fin - inicio );
			return salida;
	    }
	    
	    insertarExclusiones(con, entrada.p_Portafolios);
	    
	    salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_OK, "UI_PROCESO_EJECUTADO_EXITO", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
	    
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::CrearExclusionLimpiezaLineaOperacional", fin - inicio );
		return salida;
	}
	
	private void insertarExclusiones(Connection con, List<Portafolio> portafolios) throws Exception {
		logger.info("INICIO::CrearExclusionLimpiezaLineaOperacional.insertarExclusiones");
	    long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
	    
	    sql.batch(con, SQL_UPDATE_EXCLUSION_LIMPIEZA_LINEA_OPERACIONAL, portafolios, Portafolio.class);
	    
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::CrearExclusionLimpiezaLineaOperacional.insertarExclusiones", fin - inicio );
	}

	private List<MensajeSalida> consultarExclusiones(Connection con, Long idEmpresa, List<Portafolio> p_Portafolios) throws Exception {
		logger.info("INICIO::CrearExclusionLimpiezaLineaOperacional.consultarExclusiones");
	    long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
	    
		List<Long> idsPortafolio = p_Portafolios.stream().map(port -> port.IdPortafolio).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("idsPortafolio", idsPortafolio);
		
		List<Portafolio> existentes = sql.query(con, SQL_SELECT_EXCLUSION_LIMPIEZA_LINEA_OPERACIONAL, parametros, Portafolio.class);
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		HashMap<String, String > paramMjs = new HashMap<String, String>(3);
		for(Portafolio excl : existentes) {
			paramMjs.put("CodigoPortafolio", excl.CodigoPortafolio);
    		paramMjs.put("DocumentoIdentidad", excl.DocumentoIdentidad);
    		paramMjs.put("NombreCompleto", excl.NombreCompleto);
    		mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_PORTAFOLIO_YA_EXCLUIDO", paramMjs, con, idEmpresa));
    		paramMjs.clear();
		}
		
		long fin = System.currentTimeMillis();
	    logger.info("FIN::CrearExclusionLimpiezaLineaOperacional.consultarExclusiones", fin - inicio );
		return mensajes;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			CrearExclusionLimpiezaLineaOperacional demo = new CrearExclusionLimpiezaLineaOperacional();
			
			String entrada = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"SOC\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_Portafolios\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdPortafolio\": 953\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			EntradaCrearExclusionLimpiezaLineaOperacional Objin = gson.fromJson(entrada, EntradaCrearExclusionLimpiezaLineaOperacional.class);
			
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			try {
				SalidaCrearExclusionLimpiezaLineaOperacional salida = demo.logica(con, Objin);
				System.out.println("SALIDA");
				System.out.println(salida.toString());
				
				con.commit();
			}catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
		} catch(Exception er) {
			er.printStackTrace();
		}
		
		System.out.println("== END ==");

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearExclusionLimpiezaLineaOperacional jin = gson.fromJson(in.toString(), EntradaCrearExclusionLimpiezaLineaOperacional.class);
		SalidaCrearExclusionLimpiezaLineaOperacional out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

}