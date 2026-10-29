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
import cl.bcs.fondomutuo.pojo.ArchivoCuadraturaInternacional;
import cl.bcs.fondomutuo.pojo.CuadraturaCustodia;
import cl.bcs.fondomutuo.pojo.SerieFondoCuadratura;
import cl.bcs.fondomutuo.pojo.ValorCuota;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarArchivoCuadraturaInternacional implements JavaCall {
	
	private final Log log = new Log("FM.ProcesarArchivoCuadraturaInternacional");
	
	private final String SQL_SELECT_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDatosCuadratura.sql");
	private final String SQL_SELECT_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectValorCuota.sql");
	private final String SQL_INSERT_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_InsertDatosCuadratura.sql");
	private final String SQL_DELETE_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_DeleteDatosCuadratura.sql");
	private final String SQL_SELECT_DATOS_FONDO_ISIN = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectDatosFondoISIN.sql");
	
	public static class EntradaProcesarArchivoCuadraturaInternacional {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		public List<ArchivoCuadraturaInternacional> p_Cuadratura;
		
		public EntradaProcesarArchivoCuadraturaInternacional(){
			this.p_Cuadratura = new ArrayList<ArchivoCuadraturaInternacional>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarArchivoCuadraturaInternacional{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaProcesarArchivoCuadraturaInternacional(){
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
		EntradaProcesarArchivoCuadraturaInternacional jin = gson.fromJson(in.toString(), EntradaProcesarArchivoCuadraturaInternacional.class);
		SalidaProcesarArchivoCuadraturaInternacional out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarArchivoCuadraturaInternacional logica(Connection con, EntradaProcesarArchivoCuadraturaInternacional entrada) throws Exception {
		log.write("INICIO::ProcesarArchivoCuadraturaInternacional");
		SalidaProcesarArchivoCuadraturaInternacional out = new SalidaProcesarArchivoCuadraturaInternacional();
		SQL sql=new SQL();
		List<CuadraturaCustodia> insertCuadraturaCustodia = new ArrayList<CuadraturaCustodia>();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		
		List<CuadraturaCustodia> cuadraturasCustodia = sql.query(con, SQL_SELECT_DATOS_CUADRATURA, params, CuadraturaCustodia.class);
		
		params.clear();
		List<String> isins = new ArrayList<String>();
		
		for(ArchivoCuadraturaInternacional fondo : entrada.p_Cuadratura) {
			isins.add(fondo.ISIN);
		}
		
		params.put("ISIN", isins);
		List<SerieFondoCuadratura> fondos = sql.query(con, SQL_SELECT_DATOS_FONDO_ISIN, params, SerieFondoCuadratura.class);
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		List<ValorCuota> valoresCuota = sql.query(con, SQL_SELECT_VALOR_CUOTA, params, ValorCuota.class);
		
		List<DeleteCuadratura> deletesCuadratura = new ArrayList<DeleteCuadratura>();
		
		//Barremos Cuadraturas nuevas y existentes
		{
			for(ArchivoCuadraturaInternacional archivo : entrada.p_Cuadratura){
				CuadraturaCustodia cuadratura = null;
				String valorStr = String.valueOf(archivo.Cantidad);
				String parteEntera = valorStr.substring(0,10);
				String parteDecimal = valorStr.substring(10,13);
				BigDecimal cuotas = new BigDecimal(parteEntera + "." + parteDecimal); 
				for(SerieFondoCuadratura serieFondo : fondos){
					if(serieFondo.ISIN.equalsIgnoreCase(archivo.ISIN)){
						archivo.IdSerieFondo = serieFondo.IdSerieFondo;
						break;
					}
				}
				if(archivo.IdSerieFondo == null){
					HashMap<String, String> mje = new HashMap<String, String>();
					mje.put("RunFondo", archivo.ISIN);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_FONDO_ARCHIVO_NO_EXISTE",mje,con,entrada.p_IdEmpresa));
					continue;
				}
				
				for(CuadraturaCustodia cuadraturaCustodia : cuadraturasCustodia){
					if(archivo.IdSerieFondo.equalsIgnoreCase(cuadraturaCustodia.IdSerieFondo)){
						cuadratura = cuadraturaCustodia;
						cuadratura.TotalCuotasAGF = cuotas;
						cuadratura.ISIN = archivo.ISIN;
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
					cuadratura.IdRegimen = "";
					cuadratura.IdEmpresa = entrada.p_IdEmpresa;
					cuadratura.TotalCuotas = BigDecimal.ZERO;
					cuadratura.DiferenciaCuotas = BigDecimal.ZERO;
					cuadratura.ValorizacionCuotasAGF = BigDecimal.ZERO;
					cuadratura.ValorizacionCuotas = BigDecimal.ZERO;
					cuadratura.DiferenciaValorizacion = BigDecimal.ZERO;
					cuadratura.FechaCuadratura = entrada.p_FechaCuadratura;
					cuadratura.IdEstado = "PEN";
					cuadratura.ISIN = archivo.ISIN;
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
		
		
		log.write("FIN::ProcesarArchivoCuadraturaInternacional");
		
		return out;
	}
	
}
