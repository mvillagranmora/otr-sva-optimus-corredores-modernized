package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IndiceRentabilidadAtributtionAnalysis implements JavaCall{
	
	
	private static final Logger logger = LogManager.getLogger(SaldoCuentaInversion.class);
	private final String SQL_SELECT_DATOS_INDICES = loadResourceToString("cl/bcs/cuentaInversion/sql/InformeAtributtionAnalysis_Select_Indice.sql");
	
	private final String SQL_CREATE_TEMP_TABLE_ARBOLCLASIFICACION = "CREATE LOCAL TEMPORARY COLUMN TABLE #InArbolClasificacion(\"IdArbolClasificacionActivo\" BIGINT);";
	private final String SQL_INSERT_TEMP_TABLE_ARBOLCLASIFICACION = "INSERT INTO #InArbolClasificacion VALUES(:IdArbolClasificacionActivo);";
	private final String SQL_DROP_TEMP_TABLE_ARBOLCLASIFICACION = "DROP TABLE #InArbolClasificacion;";
	
	public static class EntradaClasificacionAtributtion{
		
		public String	p_FechaDesde;         
		public String	p_FechaHasta;
		public List<ClasificacionRentabilidad> p_clasificacion;
		
		public EntradaClasificacionAtributtion() {
			this.p_clasificacion = new ArrayList<ClasificacionRentabilidad>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public static class ClasificacionRentabilidad{
		
		public Long  IdArbolClasificacionActivo;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaIndiceRentabilidadAtributtionAnalysis{
		
		public String Indice;
		public BigDecimal PrecioFinal;
		public BigDecimal PrecioInicial;
		public BigDecimal rentabilidadIndice;
		public Long IdClasificacionActivo;
		public Long Padre;
		public String Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaIndice{
		
		public String Indice;
		public BigDecimal rentabilidadIndice;
		public String Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	//Salida desde AtributtionAnalysis 
	public static class InformeAtributtionAnalisys_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public HashMap<String,List<HashMap<String, Object>>> P_OUT_SALIDA = new HashMap<>();
		
		public InformeAtributtionAnalisys_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public InformeAtributtionAnalisys_Salida procesar(Connection con, EntradaClasificacionAtributtion in) throws Exception {
		
		InformeAtributtionAnalisys_Salida out = new InformeAtributtionAnalisys_Salida();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		
		try {
			
			
			EntradaClasificacionAtributtion  entrada = new EntradaClasificacionAtributtion();
			entrada.p_FechaDesde = in.p_FechaDesde;
			entrada.p_FechaHasta = in.p_FechaHasta;

			List<ClasificacionRentabilidad> ListaIdArbolClasificacion = new ArrayList<ClasificacionRentabilidad>(); 
			for(ClasificacionRentabilidad dato : in.p_clasificacion){
				
				ListaIdArbolClasificacion.add(dato);
			}
			
			params.put("FechaDesde", entrada.p_FechaDesde);
			params.put("FechaHasta", entrada.p_FechaHasta);
			
			System.out.println(ListaIdArbolClasificacion);
			//params.put("IdArbolClasificacionActivo", ListaIdArbolClasificacion);

			SQL.exec(con, SQL_CREATE_TEMP_TABLE_ARBOLCLASIFICACION);
			
			
			logger.debug("Inicio SQL_INSERT_TEMP_TABLE");
			sql.batch(con, SQL_INSERT_TEMP_TABLE_ARBOLCLASIFICACION, ListaIdArbolClasificacion, ClasificacionRentabilidad.class);
			
			List<SalidaIndiceRentabilidadAtributtionAnalysis> resultado = sql.query(con, SQL_SELECT_DATOS_INDICES, params, SalidaIndiceRentabilidadAtributtionAnalysis.class);
			List<SalidaIndice> salida = new ArrayList<>();
			
			int size = resultado.size();
			for(int i = 0; i < size; i++) {
				SalidaIndice ab = new SalidaIndice();
				if(resultado.get(i).Indice.equals("ND")) {

					resultado.get(i).rentabilidadIndice= new BigDecimal(0.000);
					ab.Indice = resultado.get(i).Indice;
					ab.rentabilidadIndice = resultado.get(i).rentabilidadIndice;
					ab.Descripcion = resultado.get(i).Descripcion ;
					salida.add(ab);
					
				}else{
					
					BigDecimal a  = BigDecimal.ONE;
					BigDecimal b  = BigDecimal.ONE;
					
					if(!((resultado.get(i).PrecioFinal) == null)) {
						 a  = resultado.get(i).PrecioFinal ;
					}
					if(!((resultado.get(i).PrecioInicial) == null)) {
						b  = resultado.get(i).PrecioInicial;
					}
					BigDecimal c  = (a.divide(b,2,RoundingMode.HALF_UP)).subtract(new BigDecimal(1));
					resultado.get(i).rentabilidadIndice = c;
					ab.Indice = resultado.get(i).Indice;
					ab.rentabilidadIndice = resultado.get(i).rentabilidadIndice.multiply(new BigDecimal(100L)); // se multiplica por 100 ya que es un porcentaje
					ab.Descripcion  = resultado.get(i).Descripcion ;
					salida.add(ab);
					
				}	
			}

			List<HashMap<String,Object>> resutadoRentabilidadIndice= Utils.List2ListHashGenericObject(salida);
			out.P_OUT_SALIDA.put("rentabilidadindice", resutadoRentabilidadIndice);
			
			SQL.exec(con, SQL_DROP_TEMP_TABLE_ARBOLCLASIFICACION);
			
			return out;
			
		}catch(Exception e){
		    logger.error("Error",e);
			throw e;
		}

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {	
			
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String json = "{\"p_FechaDesde\":\"2019-09-02\",\"p_FechaHasta\":\"2019-09-03\",\"p_clasificacion\":[{\"IdArbolClasificacionActivo\":1045},{\"IdArbolClasificacionActivo\":1043},{\"IdArbolClasificacionActivo\":1041}]}";
		IndiceRentabilidadAtributtionAnalysis.EntradaClasificacionAtributtion Objin_json = gson.fromJson(json, IndiceRentabilidadAtributtionAnalysis.EntradaClasificacionAtributtion.class);	
			
		IndiceRentabilidadAtributtionAnalysis demo = new IndiceRentabilidadAtributtionAnalysis();				
		System.out.println("ENTRADA");
		System.out.println(Objin_json.toString());
		System.out.println("SALIDA");	
		try {
			InformeAtributtionAnalisys_Salida ObjOut=demo.procesar(con, Objin_json);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");

	}



	
	
	
}