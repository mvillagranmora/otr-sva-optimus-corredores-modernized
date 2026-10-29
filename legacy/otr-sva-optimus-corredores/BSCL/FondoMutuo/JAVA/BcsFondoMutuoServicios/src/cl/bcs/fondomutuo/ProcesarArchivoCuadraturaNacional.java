package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ArchivoCuadraturaNacional;
import cl.bcs.fondomutuo.pojo.CuadraturaCustodia;
import cl.bcs.fondomutuo.pojo.SerieFondoCuadratura;
import cl.bcs.fondomutuo.pojo.ValorCuota;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarArchivoCuadraturaNacional implements JavaCall {
	
	private final Log log = new Log("FM.ProcesarArchivoCuadraturaNacional");
	
	private final String SQL_SELECT_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDatosCuadratura.sql");
	private final String SQL_SELECT_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectValorCuota.sql");
	private final String SQL_INSERT_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_InsertDatosCuadratura.sql");
	private final String SQL_DELETE_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_DeleteDatosCuadratura.sql");
	private final String SQL_SELECT_DATOS_FONDO_RUN = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectDatosFondoRun.sql");
	
	public static class EntradaProcesarArchivoCuadraturaNacional {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public List<ArchivoCuadraturaNacional> p_Cuadratura;
		
		public EntradaProcesarArchivoCuadraturaNacional(){
			this.p_Cuadratura = new ArrayList<ArchivoCuadraturaNacional>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarArchivoCuadraturaNacional{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaProcesarArchivoCuadraturaNacional(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DeleteCuadratura{
		public Long IdCuadraturaCustodia;		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaProcesarArchivoCuadraturaNacional jin = gson.fromJson(in.toString(), EntradaProcesarArchivoCuadraturaNacional.class);
		SalidaProcesarArchivoCuadraturaNacional out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarArchivoCuadraturaNacional logica(Connection con, EntradaProcesarArchivoCuadraturaNacional entrada) throws Exception {
		SalidaProcesarArchivoCuadraturaNacional out = new SalidaProcesarArchivoCuadraturaNacional();
		SQL sql=new SQL();
		log.write("INICIO");
		
		List<CuadraturaCustodia> insertCuadraturaCustodia = new ArrayList<CuadraturaCustodia>();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		
		List<CuadraturaCustodia> cuadraturasCustodia = sql.query(con, SQL_SELECT_DATOS_CUADRATURA, params, CuadraturaCustodia.class);
		
		params.clear();
		List<String> runs = new ArrayList<String>();
		List<String> serie = new ArrayList<String>();
		for(ArchivoCuadraturaNacional cuadratura : entrada.p_Cuadratura){
			cuadratura.RunFondo = cuadratura.RunFondo.trim().replace("-", "").replaceFirst("^0+(?!$)", "");
			cuadratura.Serie = cuadratura.Serie.trim().replaceAll("\\s+$","");
			if(!runs.contains(cuadratura.RunFondo)) runs.add(cuadratura.RunFondo.trim());
			if(!serie.contains(cuadratura.Serie)) serie.add(cuadratura.Serie.trim());
		}
		
		params.put("RunFondo", runs);
		params.put("SerieFondo", serie);
		List<SerieFondoCuadratura> fondos = sql.query(con, SQL_SELECT_DATOS_FONDO_RUN, params, SerieFondoCuadratura.class); 
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		List<ValorCuota> valoresCuota = sql.query(con, SQL_SELECT_VALOR_CUOTA, params, ValorCuota.class); 
		
		List<DeleteCuadratura> deletesCuadratura = new ArrayList<DeleteCuadratura>();
		//Barremos Cuadraturas nuevas y existentes
		{
			for(ArchivoCuadraturaNacional archivo : entrada.p_Cuadratura){
				CuadraturaCustodia cuadratura = null;
				String valorStr = String.valueOf(archivo.Saldo);
				String parteEntera = valorStr.substring(0,9);
				String parteDecimal = valorStr.substring(9,18);
				BigDecimal cuotas = new BigDecimal(parteEntera + "." + parteDecimal); 
				for(SerieFondoCuadratura serieFondo : fondos){
					if(serieFondo.RunFondo.equalsIgnoreCase(archivo.RunFondo) && serieFondo.Serie.equalsIgnoreCase(archivo.Serie)){
						archivo.IdSerieFondo = serieFondo.IdSerieFondo;
						break;
					}
				}
				if(archivo.IdSerieFondo == null){
					HashMap<String, String> mje = new HashMap<String, String>();
					mje.put("RunFondo", archivo.RunFondo);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_FONDO_ARCHIVO_NO_EXISTE",mje,con,entrada.p_IdEmpresa));
					continue;
				}
				switch(archivo.Cuenta){
					case "01":
						archivo.IdRegimen = "No APV";
						break;
					case "02":
						archivo.IdRegimen = "APV";
						break;
					case "03":
						archivo.IdRegimen = "57B";
						break;
					case "04":
						archivo.IdRegimen = "57T";
						break;	
				}
				for(CuadraturaCustodia cuadraturaCustodia : cuadraturasCustodia){
					if(archivo.IdSerieFondo.equalsIgnoreCase(cuadraturaCustodia.IdSerieFondo) && archivo.IdRegimen.equalsIgnoreCase(cuadraturaCustodia.IdRegimen)){
						cuadratura = cuadraturaCustodia;
						cuadratura.TotalCuotasAGF = cuotas;
						cuadratura.ISIN = "1";
						DeleteCuadratura delete = new DeleteCuadratura();
						delete.IdCuadraturaCustodia = cuadratura.IdCuadraturaCustodia;
						deletesCuadratura.add(delete);
						break;
					}
				}
				if(cuadratura == null){
					cuadratura = new CuadraturaCustodia();
					cuadratura.IdSerieFondo = archivo.IdSerieFondo;
					cuadratura.TotalCuotasAGF = cuotas;
					cuadratura.IdRegimen = archivo.IdRegimen;
					cuadratura.IdEmpresa = entrada.p_IdEmpresa;
					cuadratura.TotalCuotas = BigDecimal.ZERO;
					cuadratura.DiferenciaCuotas = BigDecimal.ZERO;
					cuadratura.ValorizacionCuotasAGF = BigDecimal.ZERO;
					cuadratura.ValorizacionCuotas = BigDecimal.ZERO;
					cuadratura.DiferenciaValorizacion = BigDecimal.ZERO;
					cuadratura.FechaCuadratura = entrada.p_FechaCuadratura;
					cuadratura.IdEstado = "PEN";
					cuadratura.ISIN = "1";
				}
				insertCuadraturaCustodia.add(cuadratura);
			}
		}
		
		//Valorizamos y sacamos diferencia
		{
			for(CuadraturaCustodia cuadratura : insertCuadraturaCustodia){
				cuadratura.ValorCuota = BigDecimal.ZERO;
				for(ValorCuota vc : valoresCuota){
					if(vc.IdSerieFondo.equalsIgnoreCase(cuadratura.IdSerieFondo)){
						cuadratura.ValorCuota = vc.ValorCuota;
						break;
					}
				}
				cuadratura.DiferenciaCuotas = cuadratura.TotalCuotas.subtract(cuadratura.TotalCuotasAGF, MathContext.DECIMAL128);
				cuadratura.ValorizacionCuotasAGF = cuadratura.ValorizacionCuotasAGF.multiply(cuadratura.ValorCuota, MathContext.DECIMAL128);
				cuadratura.ValorizacionCuotas = cuadratura.ValorizacionCuotas.multiply(cuadratura.ValorCuota, MathContext.DECIMAL128);
				cuadratura.DiferenciaValorizacion = cuadratura.ValorizacionCuotas.subtract(cuadratura.ValorizacionCuotasAGF, MathContext.DECIMAL128);
			}
			
		}
		
		log.write("Se borrara las siguientes cuadraturas", deletesCuadratura);
		sql.batch(con, SQL_DELETE_DATOS_CUADRATURA, deletesCuadratura, DeleteCuadratura.class);
		
		log.write("Se insertaran las siguientes cuadraturas", insertCuadraturaCustodia);
		
		if(insertCuadraturaCustodia.size() > 0){
			sql.batch(con, SQL_INSERT_DATOS_CUADRATURA, insertCuadraturaCustodia, CuadraturaCustodia.class);
		}
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CARGA_EXITOSA",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
		
		log.write("FIN");
		return out;
	}
	
	
}
