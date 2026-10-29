package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
/*
 * Permite leer los valores de parametros del sistema, quienes son solicitados a partir del par modulo-parametro y cuya consulta resulta en una lista con tres valores modulo, nombre parametro y valor
 * originalmente se creo este servicio para llenar el inicio de sesion de Optimus Negocio (Fiori)   
 */

public class ConsultarParametros implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearOrdenes.class);

	//clases de Entrada
	public static class EntradaConsultarParametro{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ParametroEntrada> p_ListaParametros = new ArrayList<>(); 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	public static class ParametroEntrada{
		public String moduloParametro;
		public String nombreParametro;

		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	//clases de Salida
	public static class SalidaConsultarParametro{
		public List<MensajeSalida>			P_OUT_MENSAJES 			= new ArrayList<>();
		public Long							P_OUT_ERRORESCONTADOR	= 0L;
		public List<ParametroSalida> P_OUT_LISTA_PARAMETROS			= new ArrayList<>();
		
		public String toString() {
			return Utils.printJson(this, false);
		};

	}
	
	public static class ParametroSalida{
		public String moduloParametro;
		public String nombreParametro;
		public String valorParametro;

		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarParametro in = gson.fromJson(jin.toString(), EntradaConsultarParametro.class);
		SalidaConsultarParametro out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarParametro logica(Connection con, EntradaConsultarParametro entrada) 
	{
		SalidaConsultarParametro salida = new SalidaConsultarParametro();
		
		try {
			for(ParametroEntrada objParam : entrada.p_ListaParametros) {
				
				ParametroSalida objParamSalida = new ParametroSalida();
				
				objParamSalida.moduloParametro = objParam.moduloParametro;
				objParamSalida.nombreParametro = objParam.nombreParametro;
				objParamSalida.valorParametro = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,objParam.moduloParametro, objParam.nombreParametro);
				
				salida.P_OUT_LISTA_PARAMETROS.add(objParamSalida); 
			
			}
		}
		catch(Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new  MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR", e.getMessage() ) );
			logger.error(e);
			//e.printStackTrace();
			//throw e;
		}

		
		return salida;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			SalidaConsultarParametro salida = new SalidaConsultarParametro();
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"PLA\",\"p_ListaParametros\": [{\"moduloParametro\" : \"PLATAFORMA\",\"nombreParametro\":\"PLA.FGL\"}]}";
				
				EntradaConsultarParametro Objin = gson.fromJson(json, EntradaConsultarParametro.class);

				ConsultarParametros  kk = new ConsultarParametros();
				salida = kk.logica(con, Objin);
				System.out.println(salida);
				con.commit();
				con.close();
				
		    	}catch(Exception er) {
					System.out.println(salida);
					//er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			//e.printStackTrace();			
		}	
		System.out.println("== END MAIN ==");
	};

}
