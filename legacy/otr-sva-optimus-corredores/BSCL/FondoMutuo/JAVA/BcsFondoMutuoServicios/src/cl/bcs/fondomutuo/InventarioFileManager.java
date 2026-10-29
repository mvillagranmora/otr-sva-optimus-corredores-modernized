package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.ftp.FileManager.Detalle;
import cl.bcs.fondomutuo.ProcesarArchivoCuadraturaNacional.EntradaProcesarArchivoCuadraturaNacional;
import cl.bcs.fondomutuo.ProcesarArchivoCuadraturaNacional.SalidaProcesarArchivoCuadraturaNacional;
import cl.bcs.fondomutuo.pojo.ArchivoCuadraturaNacional;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InventarioFileManager implements JavaCall {
	
	private final Log log = new Log("FM.InventarioFileManager");
	private static final String CODIGO_TIPO = "INVNA";
	
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDetalle.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectCabecera.sql");
	
	public static class EntradaInventarioFileManager{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_FechaCuadratura;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaInventarioFileManager{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaInventarioFileManager(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaInventarioFileManager jin = gson.fromJson(in.toString(), EntradaInventarioFileManager.class);
		SalidaInventarioFileManager out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaInventarioFileManager logica(Connection con, EntradaInventarioFileManager entrada) throws Exception {
		SalidaInventarioFileManager out = new SalidaInventarioFileManager();
		SQL sql=new SQL();
		log.write("INICIO");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdCodigoTipo", CODIGO_TIPO);
		List<Cabecera> cb = sql.query(con, SQL_SELECT_CABECERA, params, Cabecera.class);
		
		for(Cabecera cabecera : cb){
			params.clear();
			params.put("IdCabecera", cabecera.IdCabecera);
			List<Detalle> dt = sql.query(con, SQL_SELECT_DETALLE, params, Detalle.class);
			List<ArchivoCuadraturaNacional> cuadraturas = new ArrayList<ArchivoCuadraturaNacional>();
			ProcesarArchivoCuadraturaNacional p = new ProcesarArchivoCuadraturaNacional();
			EntradaProcesarArchivoCuadraturaNacional e = new EntradaProcesarArchivoCuadraturaNacional();
			for(Detalle detalle : dt){
				String linea = detalle.Linea;
				ArchivoCuadraturaNacional c = new ArchivoCuadraturaNacional();
				c.RutAdministradora = linea.substring(0, 10);
				c.Cuenta = linea.substring(10,12);
				c.RunFondo = linea.substring(12, 20);
				c.Saldo = linea.substring(20,38);
				c.Serie = linea.substring(38,48);
				c.AhorroNeto = linea.substring(48,66);
				cuadraturas.add(c);
			}
			e.p_IdEmpresa = entrada.p_IdEmpresa;
			e.p_IdModulo = entrada.p_IdModulo;
			e.p_IdUsuario = entrada.p_IdUsuario;
			e.p_FechaCuadratura = entrada.p_FechaCuadratura;
			e.p_Cuadratura = cuadraturas;
			
			SalidaProcesarArchivoCuadraturaNacional s = p.logica(con, e);
			
			out.P_OUT_ERRORESCONTADOR += s.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(s.P_OUT_MENSAJES);
			
		}
		
		log.write("FIN");
		return out;
	}
	
	
}
