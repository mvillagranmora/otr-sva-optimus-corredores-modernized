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

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ValidarNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.EntradaValidarNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.MensajeSalida;
import cl.bcs.utils.ValidarNuevaClave.SalidaValidarNuevaClave;

public class CambiarClaveUsuario implements JavaCall {

	private final Log log = new Log("Exportar RolesPerfil Completos");
	private final String SQL_CLAVE_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ConsultaClaveUsuario.sql");
	private final String SQL_LOG_CLAVE_USUARIO = loadResourceToString(
			"cl/bcs/plataforma/acc/sql/ConsultaLogClaveUsuario.sql");
	private final String SQL_UPD_CLAVE_USUARIO = loadResourceToString(
			"cl/bcs/plataforma/acc/sql/UpdateLogClaveUsuario.sql");
	private final String SQL_INS_CLAVE_USUARIO = loadResourceToString(
			"cl/bcs/plataforma/acc/sql/AgregarClaveUsuarioLog.sql");
	private final String SQL_INS_LOG = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarLog.sql");
	private long contadorErrores = 0;

	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/

	public static class EntradaCambiarClaveUsuario {
		public String p_Clave;
		public String p_ClaveNueva;
		public Long p_IdUsuario;
		public Long p_IdUsuarioModificar;
		public Long p_IdEmpresa;
		public String p_IdModulo;
	}


	public static class SalidaClaveUsuario {
		public Long codigoUsuario;
		public String clave;
		public Long numClaveCoincide;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		public String P_OUT_UPDATE;

		public SalidaClaveUsuario() {
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

	public static class UpdateClaveUsuario {
		public String CodigoUsuario;
		public Long IdUsuario;
		public String Clave;
		public Long IdEmpresa;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public static void add(UpdateClaveUsuario ev) {
			// TODO Auto-generated method stub
		}
	}

	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaCambiarClaveUsuario in = gson.fromJson(jin.toString(), EntradaCambiarClaveUsuario.class);
		SalidaClaveUsuario out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	public SalidaClaveUsuario logica(Connection con, EntradaCambiarClaveUsuario entrada) throws Exception {
		contadorErrores = 0L;
		SalidaClaveUsuario salida = new SalidaClaveUsuario();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();

		// Consultar clave usuario
		HashMap<String, Object> consultaClaveUsuario = new HashMap<String, Object>();
		
		if(entrada.p_IdUsuarioModificar!=null) {
			consultaClaveUsuario.put("p_IdUsuario", entrada.p_IdUsuarioModificar);
		}else {
			consultaClaveUsuario.put("p_IdUsuario", entrada.p_IdUsuario);
		}
		
		//consultaClaveUsuario.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<ClaveUsuario> ResulClaveUsuario = sql.query(con, SQL_CLAVE_USUARIO, consultaClaveUsuario,ClaveUsuario.class);

		try {
			// Aplicar hash a Clave Nueva.
			HashFunction hf = Hashing.sha256();
			HashCode hc = hf.newHasher().putString(entrada.p_ClaveNueva, Charsets.UTF_8).hash();
			HashCode hc2 = hf.newHasher().putString(hc.toString(), Charsets.UTF_8).hash();
			String hcClaveNueva = hc2.toString().toUpperCase();
			//String hcClaveNueva = entrada.p_ClaveNueva;
			log.write(hcClaveNueva);
			// Aplicar hash a Clave Actual.
			HashFunction hf2 = Hashing.sha256();
			HashCode hca = hf2.newHasher().putString(entrada.p_Clave, Charsets.UTF_8).hash();
			HashCode hca2 = hf2.newHasher().putString(hca.toString(), Charsets.UTF_8).hash();
			String hcClaveActual = hca2.toString().toUpperCase();
			//String hcClaveActual = entrada.p_Clave;
			//log.write(hcClaveActual);
			// Valida que la clave antigua sea igual a la almacenada en la bd

			if (ResulClaveUsuario.size() > 0) {

				// validar que la clave tenga mayusculas , minusculas y
				// caracteres especiales
				EntradaValidarNuevaClave entradaValidacion = new EntradaValidarNuevaClave(entrada.p_ClaveNueva, entrada.p_IdEmpresa);
				SalidaValidarNuevaClave salidaValidacion = ValidarNuevaClave.logica(con, entradaValidacion);
				boolean ClaveBuena = salidaValidacion.ClaveValida;
				salida.P_OUT_ERRORESCONTADOR = salidaValidacion.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_ERRORESDETALLE = salidaValidacion.P_OUT_ERRORESDETALLE;
				salida.P_OUT_ERRORESCONTADOR = salidaValidacion.P_OUT_ERRORESCONTADOR;
				
				if (!ResulClaveUsuario.get(0).Clave.equals(hcClaveActual)) {// "La
																			// clave
																			// actual
																			// no
																			// coincide";
					String codigo = "CLAVE_NO_COINCIDE";
					String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo)
							.add("pantalla", "asdf").msg();
					salida.P_OUT_ERRORESDETALLE
							.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
					salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + 1L;
					salida.P_OUT_UPDATE=null;
				} else {
					if (ClaveBuena == false) {
						// "La Clave debe tener mas de 8 digitos y al menos una
						// letra mayuscula una minuscula y un caracter";
						//String codigo = "UI_CLAVES_NOVALIDA";
						//String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo)
							//	.add("pantalla", "asdf").msg();
						//salida.P_OUT_ERRORESDETALLE
							//	.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
						salida.P_OUT_UPDATE=null;
						log.write(" clave buena" + ClaveBuena);
					} else {

						// Consultar log usuario
						HashMap<String, Object> consultaLogClaveUsuario = new HashMap<String, Object>();
						consultaLogClaveUsuario.put("idUsuario", ResulClaveUsuario.get(0).IdUsuario);
						consultaLogClaveUsuario.put("p_ClaveNueva", hcClaveNueva);
						List<LogClaveUsuario> ResulLogClaveUsuario = sql.query(con, SQL_LOG_CLAVE_USUARIO,
								consultaLogClaveUsuario, LogClaveUsuario.class);

						if ((ResulClaveUsuario.get(0).Clave.equals(hcClaveNueva))// Validar
																					// Claves
																					// no
																					// pueden
																					// ser
																					// iguales
																					// a
																					// las
																					// 10
																					// anteriores
								|| (ResulLogClaveUsuario.get(0).NumClaveCoincide > 0)) {

							String codigo = "UI_CLAVES_IGUALES";
							String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg();
							salida.P_OUT_ERRORESDETALLE
									.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
							salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + 1L;
							salida.P_OUT_UPDATE=null;
						} else {
							// Actualizar la tabla
							List<UpdateClaveUsuario> updateClaveUsuario = new ArrayList<>();
							UpdateClaveUsuario ev = new UpdateClaveUsuario();
							ev.CodigoUsuario = ResulClaveUsuario.get(0).CodigoUsuario;
							ev.Clave = hcClaveNueva;
							ev.IdEmpresa = entrada.p_IdEmpresa;
							updateClaveUsuario.add(ev);
							sql.batch(con, SQL_UPD_CLAVE_USUARIO, updateClaveUsuario, UpdateClaveUsuario.class);

							// Insert claves en la tabla log
							List<UpdateClaveUsuario> insertClaveUsuario = new ArrayList<>();
							UpdateClaveUsuario ev2 = new UpdateClaveUsuario();
							ev2.IdUsuario = ResulClaveUsuario.get(0).IdUsuario;
							ev2.Clave = ResulClaveUsuario.get(0).Clave;
							insertClaveUsuario.add(ev2);
							sql.batch(con, SQL_INS_CLAVE_USUARIO, insertClaveUsuario, UpdateClaveUsuario.class);

							/*
							 * List<UpdateClaveUsuario> insertClaveUsuario = new
							 * ArrayList<>(); UpdateClaveUsuario ev2 = new
							 * UpdateClaveUsuario(); //ev2.CodigoUsuario =
							 * entrada.p_IdUsuario; //ev2.Clave =
							 * entrada.p_ClaveNueva; //
							 * insertClaveUsuario.add(ev2); sql.batch(con,
							 * SQL_INS_LOG,
							 * insertClaveUsuario,UpdateClaveUsuario.class);
							 */
							salida.P_OUT_UPDATE = "ok+++";

						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// salida.P_Out_errores = new Long(1);
		}

		return salida;

	}
	

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			try {

				EntradaCambiarClaveUsuario entrada = new EntradaCambiarClaveUsuario();
				entrada.p_IdUsuario = (long) 107;
				entrada.p_Clave = "963";
				entrada.p_ClaveNueva = "2";
				entrada.p_IdEmpresa = (long) 1;
				JsonParser parser = new JsonParser();
				JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				CambiarClaveUsuario demo = new CambiarClaveUsuario();
				System.out.println(demo.call(con, dataJsonIn).toString());
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
