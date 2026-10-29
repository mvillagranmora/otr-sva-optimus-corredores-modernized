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

public class DeshabilitaTokenUsuarios implements JavaCall{	
	private final Log log = new Log("PLA.HoraSistema");
	//QUERIES
	private final String SQL_ModificarTokenSesion_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarTokenSesion_Update.sql");
	/*
	 * Clases IN/OUT
	 */
	public static class EntradaServicio {
		public Long							IdEmpresa;
	}
	

	public static class ServicioSalida {
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
		public List<ServicioSalidaAdvertencia>	P_OUT_ADVERTENCIADETALLE;
		public List<ServicioSalidaError>		P_OUT_ERRORESDETALLE;

		public ServicioSalida() {
			P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
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
                
                /*ESTRUCTURA JSON
                EntradaServicio pp=new EntradaServicio();
                pp.p_FechaSistema=new ArrayList<>();
                pp.p_FechaSistema.add(new EntradaFechaSistema());
                System.out.println("->"+pp.toString());
                */

                JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":'PLA',\"p_SimularOperacion\":'N',\"p_FechaSistema\":[{\"FechaSistema\":'2017-05-22',\"IdEmpresa\":1}]}").getAsJsonObject();
                AjustarFechaSistema demo = new AjustarFechaSistema();
                
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
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		SQL sql = new SQL();
		List<EntradaServicio> entradaEmpresa = new ArrayList<EntradaServicio>();
		entradaEmpresa.add(entrada);
		sql.batch(con, SQL_ModificarTokenSesion_Update, entradaEmpresa, EntradaServicio.class);
		//AVISO MASIVO A USUARIOS CONECTADOS
		//AgregarMensajeWebsocket.agregar(con, entrada.p_IdEmpresa, "AVISO", "Se ha cambiado la fecha sistema a: " + entrada.p_FechaSistema.get(0).FechaSistema);
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		return salida;
	}	
}
