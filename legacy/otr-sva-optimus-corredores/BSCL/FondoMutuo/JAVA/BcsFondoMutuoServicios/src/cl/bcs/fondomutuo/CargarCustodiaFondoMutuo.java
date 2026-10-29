package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.CarterasFFMM;
import cl.bcs.fondomutuo.pojo.CuadraturaCustodia;
import cl.bcs.fondomutuo.pojo.ValorCuota;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarCustodiaFondoMutuo implements JavaCall {
	
	private final Log log = new Log("FM.CargarCustodiaFondoMutuo");
	
	private final String SQL_SELECT_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDatosCuadratura.sql");
	private final String SQL_SELECT_DATOS_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/CargarCustodiaFondoMutuo_SelectDatosCarteras.sql");
	private final String SQL_SELECT_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectValorCuota.sql");
	private final String SQL_INSERT_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_InsertDatosCuadratura.sql");
	private final String SQL_DELETE_DATOS_CUADRATURA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesCuadratura_DeleteDatosCuadratura.sql");
	
	public static class EntradaCargarCustodiaFondoMutuo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCuadratura;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCargarCustodiaFondoMutuo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarCustodiaFondoMutuo(){
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
		EntradaCargarCustodiaFondoMutuo jin = gson.fromJson(in.toString(), EntradaCargarCustodiaFondoMutuo.class);
		SalidaCargarCustodiaFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargarCustodiaFondoMutuo logica(Connection con, EntradaCargarCustodiaFondoMutuo entrada) throws Exception {
		SalidaCargarCustodiaFondoMutuo out = new SalidaCargarCustodiaFondoMutuo();
		SQL sql=new SQL();
		log.write("INICIO");
		
		List<CuadraturaCustodia> insertCuadraturaCustodia = new ArrayList<CuadraturaCustodia>();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		
		List<CuadraturaCustodia> cuadraturasCustodia = sql.query(con, SQL_SELECT_DATOS_CUADRATURA, params, CuadraturaCustodia.class);
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		List<CarterasFFMM> carteras = sql.query(con, SQL_SELECT_DATOS_CARTERAS, params, CarterasFFMM.class); 
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCuadratura", entrada.p_FechaCuadratura);
		List<ValorCuota> valoresCuota = sql.query(con, SQL_SELECT_VALOR_CUOTA, params, ValorCuota.class); 
		
		List<DeleteCuadratura> deletesCuadratura = new ArrayList<DeleteCuadratura>();
		//Barremos Cuadraturas nuevas y existentes
		{
			for(CarterasFFMM cartera : carteras){
				CuadraturaCustodia cuadratura = null;
				for(CuadraturaCustodia cuadraturaCustodia : cuadraturasCustodia){
					if(cartera.IdSerieFondo.equalsIgnoreCase(cuadraturaCustodia.IdSerieFondo) && cartera.IdRegimen.equalsIgnoreCase(cuadraturaCustodia.IdRegimen)){
						cuadratura = cuadraturaCustodia;
						cuadratura.TotalCuotas = cartera.TotalCuotas;
						if(cartera.FondoExtranjero == "N") {
							cuadratura.ISIN = "1";}
						else {
							cuadratura.ISIN = cartera.ISIN;}
						DeleteCuadratura delete = new DeleteCuadratura();
						delete.IdCuadraturaCustodia = cuadratura.IdCuadraturaCustodia;
						deletesCuadratura.add(delete);
						break;
					}
				}
				if(cuadratura == null){
					cuadratura = new CuadraturaCustodia();
					cuadratura.IdSerieFondo = cartera.IdSerieFondo;
					cuadratura.TotalCuotas = cartera.TotalCuotas;
					cuadratura.IdRegimen = cartera.IdRegimen;
					cuadratura.IdEmpresa = entrada.p_IdEmpresa;
					cuadratura.TotalCuotasAGF = BigDecimal.ZERO;
					cuadratura.DiferenciaCuotas = BigDecimal.ZERO;
					cuadratura.ValorizacionCuotasAGF = BigDecimal.ZERO;
					cuadratura.ValorizacionCuotas = BigDecimal.ZERO;
					cuadratura.DiferenciaValorizacion = BigDecimal.ZERO;
					cuadratura.FechaCuadratura = entrada.p_FechaCuadratura;
					cuadratura.IdEstado = "PEN";
					if(cartera.FondoExtranjero == "N") {
						cuadratura.ISIN = "1";}
					else {
						cuadratura.ISIN = cartera.ISIN;}
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
				cuadratura.ValorizacionCuotasAGF = cuadratura.TotalCuotasAGF.multiply(cuadratura.ValorCuota, MathContext.DECIMAL128);
				cuadratura.ValorizacionCuotas = cuadratura.TotalCuotas.multiply(cuadratura.ValorCuota, MathContext.DECIMAL128);
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
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FechaCuadratura\":\"2019-11-13\"}";
			EntradaCargarCustodiaFondoMutuo Objin = gson.fromJson(json, EntradaCargarCustodiaFondoMutuo.class);
			
			CargarCustodiaFondoMutuo demo=new CargarCustodiaFondoMutuo();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCargarCustodiaFondoMutuo ObjOut= demo.logica(con, Objin);
 				con.commit();
 				//con.rollback();
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
