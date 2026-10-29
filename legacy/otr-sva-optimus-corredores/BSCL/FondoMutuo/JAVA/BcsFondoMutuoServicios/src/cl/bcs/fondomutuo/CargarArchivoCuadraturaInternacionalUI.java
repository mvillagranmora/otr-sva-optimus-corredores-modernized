package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ProcesarArchivoCuadraturaInternacional.EntradaProcesarArchivoCuadraturaInternacional;
import cl.bcs.fondomutuo.ProcesarArchivoCuadraturaInternacional.SalidaProcesarArchivoCuadraturaInternacional;
import cl.bcs.fondomutuo.pojo.ArchivoCuadraturaInternacional;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CargarArchivoCuadraturaInternacionalUI implements JavaCall{
	
	private final Log log = new Log("FM.CargarArchivoCuadraturaInternacionalUI");
	
	public static class EntradaCargarArchivoCuadraturaInternacionalUI {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public List<Archivo> p_Cuadratura;
		
		public EntradaCargarArchivoCuadraturaInternacionalUI(){
			this.p_Cuadratura = new ArrayList<Archivo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCargarArchivoCuadraturaInternacionalUI{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarArchivoCuadraturaInternacionalUI(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Archivo{
		public String CIAGF_ISIN;
		public String CIAGF_CANTIDAD;
		public String CIAGF_FECHA;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCargarArchivoCuadraturaInternacionalUI jin = gson.fromJson(in.toString(), EntradaCargarArchivoCuadraturaInternacionalUI.class);
		SalidaCargarArchivoCuadraturaInternacionalUI out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarArchivoCuadraturaInternacionalUI logica(Connection con, EntradaCargarArchivoCuadraturaInternacionalUI entrada) throws Exception {
		SalidaCargarArchivoCuadraturaInternacionalUI out = new SalidaCargarArchivoCuadraturaInternacionalUI();
		log.write("INICIO::CargarArchivoCuadraturaInternacionalUI");
		
		List<ArchivoCuadraturaInternacional> cuadraturas = new ArrayList<ArchivoCuadraturaInternacional>();
		for(Archivo archivo : entrada.p_Cuadratura){
			ArchivoCuadraturaInternacional c = new ArchivoCuadraturaInternacional();
			c.ISIN = archivo.CIAGF_ISIN;
			c.Fecha = archivo.CIAGF_FECHA;
			c.Cantidad = archivo.CIAGF_CANTIDAD;
			cuadraturas.add(c);
		}
		
		ProcesarArchivoCuadraturaInternacional p = new ProcesarArchivoCuadraturaInternacional();
		
		EntradaProcesarArchivoCuadraturaInternacional e = new EntradaProcesarArchivoCuadraturaInternacional();
		
		e.p_IdEmpresa = entrada.p_IdEmpresa;
		e.p_IdModulo = entrada.p_IdModulo;
		e.p_IdUsuario = entrada.p_IdUsuario;
		e.p_FechaCuadratura = entrada.p_FechaCuadratura;
		e.p_Cuadratura = cuadraturas;
		
		log.write("Se cuadraran los siguientes campos:", e.p_Cuadratura);
		SalidaProcesarArchivoCuadraturaInternacional s = p.logica(con, e);
		
		out.P_OUT_ERRORESCONTADOR = s.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = s.P_OUT_MENSAJES;
		
		log.write("FIN::CargarArchivoCuadraturaInternacionalUI");
		return out;
	}
	
}
