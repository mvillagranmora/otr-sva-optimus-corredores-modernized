package cl.bcs.plataforma.acc;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.OpenEJBEncryption;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ValidarNuevaClave.MensajeSalida;

public class CambiarClaveUsuarioReplica implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CambiarClaveUsuarioReplica.class);

	private final String SQL_UPD_CLAVE_USUARIO_REPLICA = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_ClaveUsuarioReplica.sql");
	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/

	public static class EntradaCambiarClaveUsuarioReplica {
		public String p_ClaveNueva;
		public String p_CodigoUsuario;
		public Long p_IdEmpresa;
	}


	public static class SalidaClaveUsuarioReplica {
		public String CodigoUsuario;
		public String clave;
		public Long numClaveCoincide;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		public String P_OUT_UPDATE;

		public SalidaClaveUsuarioReplica() {
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class ClaveUsuario {
		public String CodigoUsuario;
		public String Clave;
		public Long IdUsuario;
	}

	public static class LogClaveUsuario {
		public Long NumClaveCoincide;
	}

	public static class updateClaveUsuarioReplica {
		public String CodigoUsuario;
		public String Clave;
		public Long IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public static void add(updateClaveUsuarioReplica ev) {
			// TODO Auto-generated method stub
		}
	}

	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaCambiarClaveUsuarioReplica in = gson.fromJson(jin.toString(), EntradaCambiarClaveUsuarioReplica.class);
		SalidaClaveUsuarioReplica out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	public SalidaClaveUsuarioReplica logica(Connection con, EntradaCambiarClaveUsuarioReplica entrada) throws Exception {
		SalidaClaveUsuarioReplica salida = new SalidaClaveUsuarioReplica();
		SQL sql = new SQL();

		salida.P_OUT_ERRORESCONTADOR = 0L;

    	Properties prop = new Properties();
    	InputStream input;
    	
    	// OJO: siempre debe ir en false al liberar
    	boolean isDebug = false;
    
        String key = "";
        try {
    		if(isDebug) {
                input = new FileInputStream("C:\\GIT\\OPTIMUS\\optimus-config-files\\optimus.properties");
    		} else {
        		Long p_IdEmpresa = 1L;
            	String RutaArchivosConfiguracion = Utils.getConfiguracion(con,  Utils.getSchema(), p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
                input = new FileInputStream(RutaArchivosConfiguracion+"optimus.properties");
    		}
    		prop.load(input);
        	key = prop.getProperty("replica.key"); // Obtenemos key de encriptacion de password
        }catch(Exception e) {
        	logger.info("INFORMACION: No se pudo obtener la key de encriptacion de replica, se usa key por defecto.");
        	e.printStackTrace();

        }

		HashMap<String, Object> consultaClaveUsuarioReplica = new HashMap<String, Object>();
		
		consultaClaveUsuarioReplica.put("p_IdEmpresa", entrada.p_IdEmpresa);
		consultaClaveUsuarioReplica.put("p_CodigoUsuario", entrada.p_CodigoUsuario);
		
		try {
	
			if(entrada.p_ClaveNueva.length() == 0) {
				String codigo = "CLAVE_EN_BLANCO";
				String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg();
				salida.P_OUT_ERRORESDETALLE
						.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + 1L;
				salida.P_OUT_UPDATE=null;
				
				return salida;
			}
			
			List<updateClaveUsuarioReplica> updateClaveUsuarioReplica = new ArrayList<>();
			updateClaveUsuarioReplica ev = new updateClaveUsuarioReplica();
			ev.CodigoUsuario = entrada.p_CodigoUsuario.toUpperCase();
			ev.Clave = OpenEJBEncryption.encrypt(entrada.p_ClaveNueva, key);
			ev.IdEmpresa = entrada.p_IdEmpresa;
			updateClaveUsuarioReplica.add(ev);
			
			sql.batch(con, SQL_UPD_CLAVE_USUARIO_REPLICA, updateClaveUsuarioReplica, updateClaveUsuarioReplica.class);
			
			salida.P_OUT_UPDATE = "ok+++";

			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return salida;

	}
	

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {

			try {
				
				EntradaCambiarClaveUsuarioReplica entrada = new EntradaCambiarClaveUsuarioReplica();
				entrada.p_CodigoUsuario = "TEST22";
				entrada.p_ClaveNueva = "testpassword";
				entrada.p_IdEmpresa = (long) 1;
				JsonParser parser = new JsonParser();
				JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				CambiarClaveUsuarioReplica demo = new CambiarClaveUsuarioReplica();
				System.out.println(demo.call(con, dataJsonIn).toString());
//				con.commit(); 
				con.rollback();

			} catch (Exception e) {
				con.rollback();
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println("== F ==");
		}
		
		System.out.println("== END ==");
	}
}
