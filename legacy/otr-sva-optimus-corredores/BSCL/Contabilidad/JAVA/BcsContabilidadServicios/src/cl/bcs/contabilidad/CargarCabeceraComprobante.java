package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarCabeceraComprobante implements JavaCall{
	private final Log log = new Log("CON.CargarCabeceraComprobante");
	
	private final String SQL_INSERT_COMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/CargarCabeceraComprobante_InsertCombrobante.sql");
	private final String SQL_SELECT_ID_COMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/CargarCabeceraComprobante_SelectIdCombrobante.sql");
	private final String SQL_INSERT_CENTRALIZACIONES = loadResourceToString("cl/bcs/contabilidad/sql/CargarCabeceraComprobante_InsertCentralizaciones.sql");
	private final String SQL_INSERT_GLOSA = loadResourceToString("cl/bcs/contabilidad/sql/CargarCabeceraComprobante_InsertGlosa.sql");
	
	public static class InCargarCabeceraComprobante{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Comprobantes[] p_Comprobantes;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutCargarCabeceraComprobante{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public OutCargarCabeceraComprobante(){
			this.P_OUT_MENSAJE = "";
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Comprobantes{
		public String CCCT_AGRUPA;
		public Long CCCT_EJERCICIO;
		public String CCCT_FORMATO;
		public String CCCT_NEMOTECNICO;
		public String CCCT_NOMBRETIPOCONTABILIDAD;
		public String CCCT_REVERSA;
		public String CCCT_TIPOCOMPROBANTE;
		public String CCCT_TIPOGENERACION;
		public Long CCCT_CENTRALIZACION;
		public String CCCT_GLOSA;
		public String MENSAJEVALIDACION;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Id{
		public Long Id;
	}
	
	public static class InsertComprobantes{
		public Long IdTipoContabilidad;
		public String Nombre;
		public String Nemo;
		public String Tipo;
		public String Reversa;
		public String Agrupa;
		public String Condicionado;
		public String TipoGeneracion;
		public Long IdEmpresa;
		public Long Ejercicio;
		public String Formato;
		public String Estado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Glosa{
		public Long IdTipoContabilidad;
		public Long IdDefinicionAsiento;
		public String Origen;
		public Long Orden;
		public String GlosaCampo;
		public String NombreCampoMovimiento;
		public String IdTipoDatoEvento;
		public String Estado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Centralizacion{
		public Long IdTipoCentralizacion; 
		public Long IdTipoContabilidad; 
		public String Estado;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargarCabeceraComprobante in=gson.fromJson(jin.toString() ,InCargarCabeceraComprobante.class);
		OutCargarCabeceraComprobante out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutCargarCabeceraComprobante logica(Connection con, InCargarCabeceraComprobante entrada) throws Exception {
		OutCargarCabeceraComprobante out = new OutCargarCabeceraComprobante();
		out.P_OUT_ERRORESCONTADOR = 0L;
		log.write("Inicio");
		try{
			
			SQL sql=new SQL();
			List<InsertComprobantes> insertComprobantes = new ArrayList<InsertComprobantes>();
			List<Glosa> insertGlosa = new ArrayList<Glosa>();
			List<Centralizacion> insertCentralizaciones = new ArrayList<Centralizacion>();
			for(Comprobantes comprobante : entrada.p_Comprobantes){
				if("OK".equalsIgnoreCase(comprobante.MENSAJEVALIDACION)){
					InsertComprobantes insertComprobante = new InsertComprobantes();
					List<Id> id = sql.query(con, SQL_SELECT_ID_COMPROBANTE, new HashMap<String,Object>(), Id.class);
					insertComprobante.IdTipoContabilidad = id.get(0).Id;
					insertComprobante.Nombre = comprobante.CCCT_NOMBRETIPOCONTABILIDAD;
					insertComprobante.Nemo = comprobante.CCCT_NEMOTECNICO;
					insertComprobante.Tipo = comprobante.CCCT_TIPOCOMPROBANTE;
					insertComprobante.Reversa = comprobante.CCCT_REVERSA;
					insertComprobante.Agrupa = comprobante.CCCT_AGRUPA;
					insertComprobante.Condicionado = "S";
					insertComprobante.TipoGeneracion = comprobante.CCCT_TIPOGENERACION;
					insertComprobante.IdEmpresa = entrada.p_IdEmpresa;
					insertComprobante.Ejercicio = comprobante.CCCT_EJERCICIO;
					insertComprobante.Formato = comprobante.CCCT_FORMATO;
					insertComprobante.Estado = "V";
					insertComprobantes.add(insertComprobante);
					
					if(comprobante.CCCT_CENTRALIZACION != null && comprobante.CCCT_CENTRALIZACION.longValue() > 0){
						Centralizacion centralizacion = new Centralizacion();
						centralizacion.IdTipoContabilidad = id.get(0).Id;
						centralizacion.IdTipoCentralizacion = comprobante.CCCT_CENTRALIZACION;
						centralizacion.Estado = "V";
						insertCentralizaciones.add(centralizacion);
					}
					
					if(comprobante.CCCT_GLOSA != null && !comprobante.CCCT_GLOSA.isEmpty()){
						String[] glosas = comprobante.CCCT_GLOSA.split("\\|");
						int i = 1;
						for(String glosa : glosas){
							Glosa inGlosa = new Glosa();
							inGlosa.IdTipoContabilidad = id.get(0).Id;
							inGlosa.IdDefinicionAsiento = 0L;
							inGlosa.Origen = "DAT";
							inGlosa.Orden = new Long(i);
							inGlosa.GlosaCampo = "";
							inGlosa.NombreCampoMovimiento = "";
							inGlosa.IdTipoDatoEvento = glosa;
							inGlosa.Estado = "V";
							i++;
							insertGlosa.add(inGlosa);
						}
					}
					
				}else{
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_FORMATO_LINEA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJE = "UI_ERROR";
				}
			}
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJE = "UI_EXITO";
			if(insertComprobantes.size() > 0){
				sql.batch(con, SQL_INSERT_COMPROBANTE, insertComprobantes, InsertComprobantes.class);
				if(insertCentralizaciones.size() > 0){
					sql.batch(con, SQL_INSERT_CENTRALIZACIONES, insertCentralizaciones, Centralizacion.class);
				}
				if(insertGlosa.size() > 0){
					sql.batch(con, SQL_INSERT_GLOSA, insertGlosa, Glosa.class);
				}
			}
			
		} catch(Exception e){
			log.write("Error",e);
			throw e;
		}
		log.write("Termino");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "";
			InCargarCabeceraComprobante Objin = gson.fromJson(json, InCargarCabeceraComprobante.class);
			
			CargarCabeceraComprobante demo=new CargarCabeceraComprobante();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				OutCargarCabeceraComprobante ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}
