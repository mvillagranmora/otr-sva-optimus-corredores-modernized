package cl.bcs.plataforma.acc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.acc.CambiarClaveUsuario.EntradaCambiarClaveUsuario;
import cl.bcs.plataforma.acc.CambiarClaveUsuario.SalidaClaveUsuario;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ValidarNuevaClave.MensajeSalida;

public class CambiarClaveUsuarioWS implements JavaCall {

	
	private final String SQL_GET_ID_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/CambiarClaveUsuarioWS_SelectIdUsuario.sql");
	

	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/

	public static class EntradaCambiarClaveUsuarioWS {
		public String p_Clave;
		public String p_ClaveNueva;
		public Long p_IdUsuario;
		public String p_CodigoUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
	}


	public static class SalidaClaveUsuarioWS {
		public Long codigoUsuario;
		public String clave;
		public Long numClaveCoincide;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		public List<MensajeSalida> P_OUT_UPDATE;

		public SalidaClaveUsuarioWS() {
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			P_OUT_UPDATE = new ArrayList<>();
		}
	}

	public static class ObtieneUsuario {
		public String CodigoUsuario;
		public Long IdEmpresa;
	}

	public static class DatosUsuario {
		public Long IdUsuario;
	}

	

	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaCambiarClaveUsuarioWS in = gson.fromJson(jin.toString(), EntradaCambiarClaveUsuarioWS.class);
		SalidaClaveUsuarioWS out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	public SalidaClaveUsuarioWS logica(Connection con, EntradaCambiarClaveUsuarioWS entrada) throws Exception {
		Long IdUsuario = 0L;
		SalidaClaveUsuarioWS out = new SalidaClaveUsuarioWS();
		try {
			ObtieneUsuario obtieneUsuario = new ObtieneUsuario();
			obtieneUsuario.CodigoUsuario = entrada.p_CodigoUsuario;
			obtieneUsuario.IdEmpresa = entrada.p_IdEmpresa;
			IdUsuario = ObtieneIdUsuario(con,obtieneUsuario);
			if(IdUsuario > 0) {
				out = cambiarClaveUsuario(con,entrada,IdUsuario);
			}
			else {
				out.P_OUT_ERRORESCONTADOR =  1L;
				out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_ENCUENTRA_USUARIO", "USUARIO NO ENCONTRADO"));
				out.P_OUT_UPDATE = null;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return out;

	}
	
	private SalidaClaveUsuarioWS cambiarClaveUsuario(Connection con, EntradaCambiarClaveUsuarioWS entrada, Long idUsuarioModificar) throws Exception {
		SalidaClaveUsuarioWS out = new SalidaClaveUsuarioWS();
		Long idEmpresa = 0L;
		if(entrada.p_IdEmpresa != 0) {
			idEmpresa= 1L;
		}
		else
		{
			idEmpresa = entrada.p_IdEmpresa;
		}
		EntradaCambiarClaveUsuario cambiarClaveUsuarioEntrada = new EntradaCambiarClaveUsuario();
		cambiarClaveUsuarioEntrada.p_IdEmpresa = idEmpresa;
		cambiarClaveUsuarioEntrada.p_Clave = entrada.p_Clave;
		cambiarClaveUsuarioEntrada.p_IdUsuario = entrada.p_IdUsuario;
		cambiarClaveUsuarioEntrada.p_IdModulo = entrada.p_IdModulo;
		cambiarClaveUsuarioEntrada.p_IdUsuarioModificar = idUsuarioModificar;
		cambiarClaveUsuarioEntrada.p_ClaveNueva = entrada.p_ClaveNueva;
		
				
		
		CambiarClaveUsuario cambiarClaveUsuario = new CambiarClaveUsuario();
		SalidaClaveUsuario cambiarClaveUsuarioSalida = cambiarClaveUsuario.logica(con, cambiarClaveUsuarioEntrada);
		if(cambiarClaveUsuarioSalida.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_ERRORESCONTADOR =  cambiarClaveUsuarioSalida.P_OUT_ERRORESCONTADOR;
			out.P_OUT_ERRORESDETALLE = cambiarClaveUsuarioSalida.P_OUT_ERRORESDETALLE;
			out.P_OUT_UPDATE = null;
		}
		else {
			out.P_OUT_UPDATE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "CLAVE_MODIFICADA", "CLAVE MODIFICADA SATISFACTORIAMENTE"));
			out.P_OUT_ERRORESCONTADOR =  cambiarClaveUsuarioSalida.P_OUT_ERRORESCONTADOR;
			out.P_OUT_ERRORESDETALLE = null;
		}
		
		
		return out;
	}
	
	
	public Long ObtieneIdUsuario(Connection con,  ObtieneUsuario entrada) throws Exception {
		SQL sql = new SQL();
		try {
			HashMap<String, Object> consultaIdUsuario = new HashMap<String, Object>();
			consultaIdUsuario.put("p_CodigoUsuario", entrada.CodigoUsuario);
			consultaIdUsuario.put("p_IdEmpresa", entrada.IdEmpresa);

			List<DatosUsuario> datosUsuario= sql.query(con, SQL_GET_ID_USUARIO, consultaIdUsuario, DatosUsuario.class);
			if(datosUsuario.size() > 0){
				return datosUsuario.get(0).IdUsuario;
			}
			else
			{
				return 0L;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
		
		
	}

	

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_Clave\":\"Mateo%0909\",\"p_ClaveNueva\":\"Mao%0909\",\"p_CodigoUsuario\": \"JBRUNA\"}";

			EntradaCambiarClaveUsuarioWS Objin = gson.fromJson(json,
					EntradaCambiarClaveUsuarioWS.class);
			CambiarClaveUsuarioWS demo = new CambiarClaveUsuarioWS();
			System.out.println("ENTRADA:" + Objin.toString());

			try {
				SalidaClaveUsuarioWS ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_ERRORESCONTADOR);
				System.out.println(ObjOut.P_OUT_ERRORESDETALLE);
				System.out.println(ObjOut.P_OUT_UPDATE);
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				// con.commit();
			}

			con.close();
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("== END ==");
	}
}
