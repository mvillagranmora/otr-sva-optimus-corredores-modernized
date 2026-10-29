package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarDiccionario.EntradaDiccionario;
import cl.bcs.plataforma.pla.AgregarDiccionario.SalidaDiccionario;
import cl.bcs.plataforma.pla.AgregarDiccionario.ServicioSalidaError;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDiccionario implements JavaCall{	
	private final Log log = new Log("PLA.Diccionario");
	//QUERIES
	private final String SQL_ConsultarDiccionario_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultaDiccionario_Select.sql");
	private final String SQL_ModificarDiccionario_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarDiccionario_Update.sql");
	/*
	 * Clases IN/OUT
	 */
	public static class EntradaServicio {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public String					p_SimularOperacion;
		//
		public List<EntradaDiccionario>	p_Codigos;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntradaDiccionario {
		public String	CodDiccionario;
		public String	CodLenguaje;
		public String	Descripcion;
		public String	Abreviado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalida {
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
		public List<ServicioSalidaAdvertencia>	P_OUT_ADVERTENCIADETALLE;
		public List<ServicioSalidaError>		P_OUT_ERRORESDETALLE;
		public List<SalidaDiccionario>			p_Respuesta;

		public ServicioSalida() {
			P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			p_Respuesta = new ArrayList<>();		
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaDiccionario {
		public String	CodDiccionario;
		public String	Mensaje;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class ServicioSalidaAdvertencia {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalidaError {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;

		public ServicioSalidaError() {
		}

		public ServicioSalidaError(String codigo, String mensaje) {
			this.Codigo = codigo;
			this.Descripcion = mensaje;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){		
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
                JsonParser parser = new JsonParser();
                
                /*EntradaServicio pp=new EntradaServicio();
                pp.p_Codigos=new ArrayList<>();
                pp.p_Codigos.add(new EntradaDiccionario());
                System.out.println("->"+pp.toString());*/

                //JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":'PLA',\"p_SimularOperacion\":'N',\"p_Codigos\":[{\"CodDiccionario\":'UI_TEST_1',\"CodLenguaje\":'CL',\"Descripcion\":'TEST1_Mod',\"Abreviado\":''},{\"CodDiccionario\":'UI_TEST_2',\"CodLenguaje\":'CL',\"Descripcion\":'TEST2_Mod',\"Abreviado\":''}]} ").getAsJsonObject();
                JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":'PLA',\"p_SimularOperacion\":'N',\"p_Codigos\":[{\"CodDiccionario\":'UI_TEST_2',\"CodLenguaje\":'CL',\"Descripcion\":'TEST2_MOD_PLA',\"Abreviado\":''}]}").getAsJsonObject();
                ModificarDiccionario demo = new ModificarDiccionario();
                
                System.out.println(demo.call(con, dataJsonIn).toString());
                con.commit();
            } catch (Exception er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
	}

	/**********************
	 * Transporte
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();
		EntradaServicio in = gson.fromJson(jin.toString(), EntradaServicio.class);
		ServicioSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ServicioSalida logica(Connection con, EntradaServicio entrada) throws Exception {
		ServicioSalida salida = new ServicioSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		salida.p_Respuesta = new ArrayList<SalidaDiccionario>();
		SQL sql = new SQL();
		
		// Leer BD
		HashMap<String, Object> parms = new HashMap<String, Object>();
		List<String> ListaCodDiccionario = Utils.getColumn(entrada.p_Codigos, "CodDiccionario", false);

		parms.put("ListaCodDiccionario", ListaCodDiccionario);
		List<EntradaDiccionario> listaEvento = sql.query(con, SQL_ConsultarDiccionario_Select, parms,
				EntradaDiccionario.class);

		// Procesar
		boolean existe = false;
		for (EntradaDiccionario dicIn : entrada.p_Codigos) {
			for (EntradaDiccionario dicBD : listaEvento) {
				if (dicIn.CodDiccionario.equals(dicBD.CodDiccionario) && dicIn.CodLenguaje.equals(dicBD.CodLenguaje)) {
					System.out.println("Registro " + dicIn.CodDiccionario + " existe.");
					existe = true;
					break;
				}
			}
			if (existe) {
				SalidaDiccionario msg = new SalidaDiccionario();
				String error = "NUEVO_MODIFICA_DICCIONARIO";
				HashMap<String, String> dicVals = new HashMap<>();
				dicVals.put("CodDiccionario", dicIn.CodDiccionario + "");
				dicVals.put("CodLenguaje", dicIn.CodLenguaje + "");
				String MsgError = Diccionario.getMensaje(con, entrada.p_IdEmpresa, error, dicVals);

				msg.CodDiccionario = dicIn.CodDiccionario;
				msg.Mensaje = MsgError;
				salida.p_Respuesta.add(msg);
			}
			else {
				salida.p_Respuesta.clear();
			}
		}
		if (!existe){
			String error = "ERROR_MODIFICA_DICCIONARIO";
            String MsgError = Diccionario.getMensaje(con, entrada.p_IdEmpresa,error, null);
			salida.P_OUT_ERRORESCONTADOR++;
            ServicioSalidaError err = new ServicioSalidaError();
            err.Codigo = error;
            err.Descripcion = MsgError;
            err.RequiereAutorizacion = "N";
            salida.P_OUT_ERRORESDETALLE.add(err);
		}
		
		// Escribir BD
		if (existe)
			sql.batch(con, SQL_ModificarDiccionario_Update, entrada.p_Codigos, EntradaDiccionario.class);
		
		return salida;
	}	
}
