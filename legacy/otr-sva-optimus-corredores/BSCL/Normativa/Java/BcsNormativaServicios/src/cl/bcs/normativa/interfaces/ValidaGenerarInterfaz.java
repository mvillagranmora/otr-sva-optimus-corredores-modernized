package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidaGenerarInterfaz implements JavaCall {	
	private final Log log =new Log("ValidaGenerarInterfaces");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	private final String SQL_SELECT_CIERRE_ORDENES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectCierreModulos.sql");
	/*******************
	 * clases in/out
	 *******************/
	
	
	public static class InValidaGenerarInterfaz{
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaEjecucion;
	}
	
	public static class OutValidaGenerarInterfaz{
		public Long			  			P_OUT_ERRORESCONTADOR;
		public List<MensajesErrores>	P_OUT_MENSAJES;
		
		public OutValidaGenerarInterfaz()
		{
			P_OUT_MENSAJES = new ArrayList<MensajesErrores>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class MensajesErrores
	{
		public Long		Id;
		public String	tipo;
		public String	codigo;
		public String	descripcion;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}
	
	public static class Modulo{
		public Long 	IdEmpresa;
		public String 	CodigoModulo;
		public String 	Nombre;
		public String	IdEstado;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InValidaGenerarInterfaz in = gson.fromJson(jin.toString() ,InValidaGenerarInterfaz.class);
		OutValidaGenerarInterfaz out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutValidaGenerarInterfaz logica(Connection con, InValidaGenerarInterfaz entrada) throws Exception {
		OutValidaGenerarInterfaz out = new OutValidaGenerarInterfaz();
		out.P_OUT_ERRORESCONTADOR = 0L;	
		try{
			SQL sql=new SQL();
			List<String> listModulos = new ArrayList<>();
			listModulos.add("ORD");
			listModulos.add("FAC");
			//listModulos.add("CUS");
			//listModulos.add("CTA");
			//listModulos.add("TES");
			
			HashMap<String,Object> paramsConsultaCierre = new HashMap<String,Object>();
			paramsConsultaCierre.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsConsultaCierre.put("IdModulo", listModulos);
			paramsConsultaCierre.put("FechaEjecucion", entrada.p_FechaEjecucion);
			List<Modulo> modulo = sql.query(con,SQL_SELECT_CIERRE_ORDENES,paramsConsultaCierre,Modulo.class);
			List<String> moduloAbierto = new ArrayList<>();
			int contCierreModulo = 0;
			for(Modulo mod : modulo) {
				if("ABT".equalsIgnoreCase(mod.IdEstado) || "UI_DESCUADRADO".equalsIgnoreCase(mod.IdEstado)){
					moduloAbierto.add(mod.Nombre);
				}else {
					contCierreModulo ++;
				}	
			}
			if(modulo.size() != contCierreModulo) {
				MensajesErrores mensajesErrores = new MensajesErrores();
				mensajesErrores.Id = 1L;
				mensajesErrores.tipo = "ERROR";
				mensajesErrores.codigo = "UI_CERRAR_MODULO";
				mensajesErrores.descripcion = "Falta Cerrar los modulos " + moduloAbierto + " ";

				//bwArchivoLog.write("Ruta Archivo Salida:" + Mensaje);
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(mensajesErrores);
			}else {
				out.P_OUT_ERRORESCONTADOR = 0L;
				out.P_OUT_MENSAJES.add(new MensajesErrores());
			}
		}catch(Exception e){
			log.write(e);
		}finally{
			 log.writeAsJson("SALIDA",out);
		}
		
		return out;
	}
	
	
	public static void main (String [] args) {
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAPEConnection();
  			try{
  				
  				InValidaGenerarInterfaz entrada = new InValidaGenerarInterfaz();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "NOR";
  				entrada.p_FechaEjecucion= "2017-07-26";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				ValidaGenerarInterfaz demo = new ValidaGenerarInterfaz();
  				System.out.println(demo.call(con, dataJsonIn).toString());
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
        System.out.println("== END ==");
	}
	
}
