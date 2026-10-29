package cl.bcs.plataforma.acc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ValidarNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.EntradaValidarNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.EstadoCriteriosNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.MensajeSalida;
import cl.bcs.utils.ValidarNuevaClave.SalidaValidarNuevaClave;

public class ValidarClaveUsuario implements JavaCall {

	private final Log log = new Log("Exportar RolesPerfil Completos");
	private final String SQL_CLAVE_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ConsultaClaveUsuario.sql");

	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/

	public static class EntradaValidarClaveUsuario {
		public String p_Clave;
		public String p_ClaveNueva;
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
	}

	public static class SalidaValidarClaveUsuario {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		public List<EstadoCriteriosNuevaClave> P_OUT_ESTADOCRITERIOSNUEVACLAVE;
		public boolean P_OUT_VALIDACION_OK;
		public boolean P_OUT_CLAVEACTUAL_OK;

		public SalidaValidarClaveUsuario() {
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_VALIDACION_OK = false;
			P_OUT_CLAVEACTUAL_OK = false;
		}
	}

	public static class ClaveUsuario {
		public String CodigoUsuario;
		public String Clave;
		public Long IdUsuario;
	}

	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaValidarClaveUsuario in = gson.fromJson(jin.toString(), EntradaValidarClaveUsuario.class);
		SalidaValidarClaveUsuario out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	public SalidaValidarClaveUsuario logica(Connection con, EntradaValidarClaveUsuario entrada) throws Exception {
		SalidaValidarClaveUsuario salida = new SalidaValidarClaveUsuario();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();

		// Consultar clave usuario
		HashMap<String, Object> consultaClaveUsuario = new HashMap<String, Object>();
		
		consultaClaveUsuario.put("p_IdUsuario", entrada.p_IdUsuario);
		
		List<ClaveUsuario> resultClaveUsuario = sql.query(con, SQL_CLAVE_USUARIO, consultaClaveUsuario, ClaveUsuario.class);

		try {
			// Aplicar hash a Clave Actual.
			HashFunction hf2 = Hashing.sha256();
			HashCode hca = hf2.newHasher().putString(entrada.p_Clave, Charsets.UTF_8).hash();
			HashCode hca2 = hf2.newHasher().putString(hca.toString(), Charsets.UTF_8).hash();
			String hcClaveActual = hca2.toString().toUpperCase();
			
			// Valida que la clave antigua sea igual a la almacenada en la bd
			if (resultClaveUsuario.size() > 0) {
				boolean coincideConClaveAnterior = resultClaveUsuario.get(0).Clave.equals(hcClaveActual);
				
				if (!coincideConClaveAnterior) {
					String codigo = "CLAVE_NO_COINCIDE";
					String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo)
							.add("pantalla", "asdf").msg();
					MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion);
					salida.P_OUT_ERRORESDETALLE.add(mensajeSalida);
					salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + 1L;
				} else {
					salida.P_OUT_CLAVEACTUAL_OK = true;
				}
			}
			
			// Valida criterios en nueva clave
			if (!entrada.p_ClaveNueva.equals("")) {
				// Validar que la clave cumpla con criterios almacenados en la configuracion (p.ej. que tenga mayusculas,
				// minusculas y caracteres especiales)
				EntradaValidarNuevaClave entradaValidacion = new EntradaValidarNuevaClave(entrada.p_ClaveNueva, entrada.p_IdEmpresa);
				SalidaValidarNuevaClave salidaValidacion = ValidarNuevaClave.logica(con, entradaValidacion);
							
				salida.P_OUT_ERRORESCONTADOR += salidaValidacion.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_ERRORESDETALLE.addAll(salidaValidacion.P_OUT_ERRORESDETALLE);
				salida.P_OUT_ESTADOCRITERIOSNUEVACLAVE = salidaValidacion.P_OUT_ESTADOCRITERIOSNUEVACLAVE;
				
				// Si se llega a esta etapa sin errores (validacion clave anterior = actual y criterios validados),
				// el flujo esta completamente validado
				if (salida.P_OUT_ERRORESCONTADOR == 0) {
					salida.P_OUT_VALIDACION_OK = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return salida;

	}

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":189,\"p_IdModulo\":\"PLA\",\"p_Clave\":\"\",\"p_ClaveNueva\":\"12**A\"}";
			EntradaValidarClaveUsuario Objin = gson.fromJson(json,
					EntradaValidarClaveUsuario.class);
			ValidarClaveUsuario demo = new ValidarClaveUsuario();
			System.out.println("ENTRADA:" + Objin.toString());

			try {
				SalidaValidarClaveUsuario ObjOut = demo.logica(con, Objin);

				JsonParser parser = new JsonParser();
				JsonObject dataout = parser.parse(gson.toJson(ObjOut)).getAsJsonObject();

				System.out.println(ObjOut.P_OUT_CLAVEACTUAL_OK);
				System.out.println(ObjOut.P_OUT_VALIDACION_OK);
				System.out.println(ObjOut.P_OUT_ERRORESDETALLE);
				System.out.println(ObjOut.P_OUT_ESTADOCRITERIOSNUEVACLAVE);
				System.out.println(dataout);
				
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				// con.commit();
			}

			con.close();
			
			
		} catch (Exception e) {
			System.out.println("== F ==");
		}
		System.out.println("== END ==");
	}
}
