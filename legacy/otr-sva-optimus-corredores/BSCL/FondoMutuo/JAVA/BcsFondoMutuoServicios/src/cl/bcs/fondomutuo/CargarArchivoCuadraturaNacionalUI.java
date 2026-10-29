package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ProcesarArchivoCuadraturaNacional.EntradaProcesarArchivoCuadraturaNacional;
import cl.bcs.fondomutuo.ProcesarArchivoCuadraturaNacional.SalidaProcesarArchivoCuadraturaNacional;
import cl.bcs.fondomutuo.pojo.ArchivoCuadraturaNacional;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CargarArchivoCuadraturaNacionalUI implements JavaCall{
	
	private final Log log = new Log("FM.CargarArchivoCuadraturaNacionalUI");
		
	public static class EntradaCargarArchivoCuadraturaNacionalUI {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public List<Archivo> p_Cuadratura;
		
		public EntradaCargarArchivoCuadraturaNacionalUI(){
			this.p_Cuadratura = new ArrayList<Archivo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCargarArchivoCuadraturaNacionalUI{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarArchivoCuadraturaNacionalUI(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Archivo{
		public String CAGF_RUTAGF;
		public String CAGF_CUENTA;
		public String CAGF_FONDO;
		public String CAGF_SALDO;
		public String CAGF_SERIE;
		public String CAGF_AHORRO_NETO;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCargarArchivoCuadraturaNacionalUI jin = gson.fromJson(in.toString(), EntradaCargarArchivoCuadraturaNacionalUI.class);
		SalidaCargarArchivoCuadraturaNacionalUI out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarArchivoCuadraturaNacionalUI logica(Connection con, EntradaCargarArchivoCuadraturaNacionalUI entrada) throws Exception {
		SalidaCargarArchivoCuadraturaNacionalUI out = new SalidaCargarArchivoCuadraturaNacionalUI();
		log.write("INICIO");
		
		List<ArchivoCuadraturaNacional> cuadraturas = new ArrayList<ArchivoCuadraturaNacional>();
		for(Archivo archivo : entrada.p_Cuadratura){
			ArchivoCuadraturaNacional c = new ArchivoCuadraturaNacional();
			c.RutAdministradora = archivo.CAGF_RUTAGF;
			c.Cuenta = archivo.CAGF_CUENTA;
			c.RunFondo = archivo.CAGF_FONDO;
			c.Saldo = archivo.CAGF_SALDO;
			c.Serie = archivo.CAGF_SERIE;
			c.AhorroNeto = archivo.CAGF_AHORRO_NETO;
			cuadraturas.add(c);
		}
		
		ProcesarArchivoCuadraturaNacional p = new ProcesarArchivoCuadraturaNacional();
		
		EntradaProcesarArchivoCuadraturaNacional e = new EntradaProcesarArchivoCuadraturaNacional();
		
		e.p_IdEmpresa = entrada.p_IdEmpresa;
		e.p_IdModulo = entrada.p_IdModulo;
		e.p_IdUsuario = entrada.p_IdUsuario;
		e.p_FechaCuadratura = entrada.p_FechaCuadratura;
		e.p_Cuadratura = cuadraturas;
		
		log.write("Se cuadraran los siguientes campos:", e.p_Cuadratura);
		SalidaProcesarArchivoCuadraturaNacional s = p.logica(con, e);
		
		out.P_OUT_ERRORESCONTADOR = s.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = s.P_OUT_MENSAJES;
		
		log.write("FIN");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaCuadratura\":\"2018-05-09\",\"p_Cuadratura\":[{\"CAGF_RUTAGF\":\"0760365211\",\"CAGF_CUENTA\":\"01\",\"CAGF_FONDO\":\"00080907\",\"CAGF_SALDO\":\"017416807856200000\",\"CAGF_SERIE\":\"A         \",\"CAGF_AHORRO_NETO\":\"000000000000000000\",\"MENSAJEVALIDACION\":\"OK\"},{\"CAGF_RUTAGF\":\"0760365211\",\"CAGF_CUENTA\":\"02\",\"CAGF_FONDO\":\"00080907\",\"CAGF_SALDO\":\"000049310688800000\",\"CAGF_SERIE\":\"A         \",\"CAGF_AHORRO_NETO\":\"000000000000000000\",\"MENSAJEVALIDACION\":\"OK\"}]}";
			EntradaCargarArchivoCuadraturaNacionalUI Objin = gson.fromJson(json, EntradaCargarArchivoCuadraturaNacionalUI.class);
			
			CargarArchivoCuadraturaNacionalUI demo=new CargarArchivoCuadraturaNacionalUI();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCargarArchivoCuadraturaNacionalUI ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
	
}
