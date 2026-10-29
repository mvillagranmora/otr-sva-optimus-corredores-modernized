package cl.bcs.plataforma.acc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerTokenOptimusClientes implements JavaCall {
	
	private final Logger logger = LogManager.getLogger(ObtenerTokenOptimusClientes.class);
	private final String SQL_SELECT_USUARIO_COMPLETO = loadResourceToString("cl/bcs/plataforma/acc/sql/ObtenerTokenOptimusClientes_Select_UsuarioCompleto.sql");
	private final String SQL_UPDATE_DISABLE_TOKENS = loadResourceToString("cl/bcs/plataforma/acc/sql/ObtenerTokenOptimusClientes_Update_DisableTokens.sql");
	private final String SQL_INSERT_SESION = loadResourceToString("cl/bcs/plataforma/acc/sql/ObtenerTokenOptimusClientes_Insert_Sesion.sql");
	
	
	public static class EntradaObtenerTokenOptimusClientes{
		public Long p_IdEmpresa;
		public String p_CodigoUsuario;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaObtenerTokenOptimusClientes{
		public String P_OUT_TOKEN;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaObtenerTokenOptimusClientes() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UsuarioCompleto{
		public String Token;
		public Long IdUsuario;
		public Long IdEmpresa;
		public String CodigoUsuario;
		public String EsAutorizado;
		public String TieneBloqueoTemporal;
		public String TipoEntidad;
		public String DebeCambiarClave;
		public String DescripcionEstado;//estado usuario -- ACTIVO
		public String UsuarioClientes;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaObtenerTokenOptimusClientes entrada = gson.fromJson(jin.toString(), EntradaObtenerTokenOptimusClientes.class);
		SalidaObtenerTokenOptimusClientes salida = logica(conn, entrada);
		JsonObject dataout = new JsonParser().parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaObtenerTokenOptimusClientes logica(Connection conn, EntradaObtenerTokenOptimusClientes entrada) {
		logger.info("INIT");
		SalidaObtenerTokenOptimusClientes salida = new SalidaObtenerTokenOptimusClientes();
		if(entrada==null || entrada.p_IdEmpresa==null || entrada.p_CodigoUsuario==null) {
			salida.P_OUT_ERRORESCONTADOR =1L;
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "USUARIO_CLAVE_INCORRECTA", "USUARIO_CLAVE_INCORRECTA"));
		}else {
			try {		
				SQL sql = new SQL();
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdEmpresa", entrada.p_IdEmpresa);
				params.put("CodigoUsuario", entrada.p_CodigoUsuario);
			
				List<UsuarioCompleto> usuarios = sql.query(conn, SQL_SELECT_USUARIO_COMPLETO, params, UsuarioCompleto.class);
				//validar que el usuario este activo y no bloqueado.
				if(usuarioValido(usuarios)) {
					logger.debug("usuario valido, generar token");
					UsuarioCompleto usuario = usuarios.get(0);
					usuario.Token = usuario.Token.substring(0, usuario.Token.length()-2);
					//desabilitar todos los demas token del usuario
					sql.batch(conn, SQL_UPDATE_DISABLE_TOKENS, Arrays.asList(usuario), UsuarioCompleto.class);
					//Ingresar nuevo token valido
					sql.batch(conn, SQL_INSERT_SESION, Arrays.asList(usuario), UsuarioCompleto.class);
					//retornar nuevo token autologin
					salida.P_OUT_TOKEN = usuario.Token;
				}else {
					salida.P_OUT_ERRORESCONTADOR =1L;
					salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "USUARIO_CLAVE_INCORRECTA", "USUARIO_CLAVE_INCORRECTA"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		logger.info("END");
		return salida;
	}
	
	private boolean usuarioValido(List<UsuarioCompleto> usuariosCompleto) {
		if(usuariosCompleto.isEmpty()) {
			return false;
		}
		
		UsuarioCompleto usuario = usuariosCompleto.stream().findFirst().orElse(new UsuarioCompleto());
		if(usuario.CodigoUsuario==null) {
			return false;
		}
		if(usuario.EsAutorizado.equals("N") || usuario.TieneBloqueoTemporal.equals("S") || !usuario.DescripcionEstado.equals("ACTIVO") || usuario.UsuarioClientes.equals("N")) {
			return false;
		}
		if (!(usuario.TipoEntidad.equals("CLI") || usuario.TipoEntidad.equals("PER"))) {
			return false;
		}
		return true;
		
	}

	/*{
		  "IdEmpresa": 1,
		  "IdUsuario": "sistema",
		  "ClaveUsuario": "3d93e4d26910f1c6bbc81d7801db68e9bf35c5a7bb02bed479ff181d77dfc1e4",
		  "Cliente": {
		    "IdEmpresa": 1,
		    "CodigoUsuario": "FFSCHNEIDERR"
		  }
		}*/
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsa","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\": 1, \"p_CodigoUsuario\" : \"FFSCHNEIDERR\"}";

			EntradaObtenerTokenOptimusClientes entrada = gson.fromJson(json, EntradaObtenerTokenOptimusClientes.class);

			ObtenerTokenOptimusClientes servicio = new ObtenerTokenOptimusClientes();
			System.out.println("ENTRADA");
			System.out.println(entrada);
			
			SalidaObtenerTokenOptimusClientes salida = servicio.logica(con, entrada);
			System.out.println(salida);
			con.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
