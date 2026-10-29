package cl.bcs.contabilidad;

import java.math.BigDecimal;
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

public class CargarDeterminanteComprobante implements JavaCall{
private final Log log = new Log("CON.CargarCabeceraComprobante");
	
	private final String SQL_INSERT_DETERMINANTE = loadResourceToString("cl/bcs/contabilidad/sql/CargarDeterminanteComprobante_InsertDeterminante.sql");
	private final String SQL_SELECT_COMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/CargarDeterminanteComprobante_SelectCombrobante.sql");
	
	public static class InCargarDeterminanteComprobante{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Determinante[] p_Determinantes;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutCargarDeterminanteComprobante{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public OutCargarDeterminanteComprobante(){
			this.P_OUT_MENSAJE = "";
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Comprobante{
		public Long IdTipoContabilidad;
		public String Nemo;
	}
	
	public static class Determinante{
		public String CDCT_NEMOTECNICO;
		public String CDCT_TIPODATO;
		public String CDCT_CONDICION;
		public String CDCT_VALOR;
		public String MENSAJEVALIDACION;
	}
	
	public static class InsertDeterminante{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Condicion;
		public String Conector;
		public String TipoCondicion;
		public String Estado;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargarDeterminanteComprobante in=gson.fromJson(jin.toString() ,InCargarDeterminanteComprobante.class);
		OutCargarDeterminanteComprobante out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutCargarDeterminanteComprobante logica(Connection con, InCargarDeterminanteComprobante entrada) throws Exception {
		OutCargarDeterminanteComprobante out = new OutCargarDeterminanteComprobante();
		out.P_OUT_ERRORESCONTADOR = 0L;
		log.write("Inicio");
		try{
			
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			List<String> nemos = new ArrayList<String>();
			List<InsertDeterminante> insertDeterminantes = new ArrayList<InsertDeterminante>();
			List<Comprobante> comprobantes = new ArrayList<Comprobante>();
			for(Determinante determinante : entrada.p_Determinantes){
				if("OK".equalsIgnoreCase(determinante.MENSAJEVALIDACION)){
					nemos.add(determinante.CDCT_NEMOTECNICO);
				}
			}
			if(nemos.size() > 0){
				params.put("nemos", nemos);
				params.put("IdEmpresa", entrada.p_IdEmpresa);
				comprobantes = sql.query(con, SQL_SELECT_COMPROBANTE, params, Comprobante.class);
			}
			for(Determinante determinante : entrada.p_Determinantes){
				if("OK".equalsIgnoreCase(determinante.MENSAJEVALIDACION)){
					InsertDeterminante insertDeterminante = new InsertDeterminante();
					for(Comprobante comprobante : comprobantes){
						if(determinante.CDCT_NEMOTECNICO.equalsIgnoreCase(comprobante.Nemo)){
							insertDeterminante.IdTipoContabilidad = comprobante.IdTipoContabilidad;
							break;
						}
					}
					if(insertDeterminante.IdTipoContabilidad == null){
						HashMap<String,String> tipoContabilidad = new HashMap<String,String>();
						tipoContabilidad.put("IdTipoContabilidad", determinante.CDCT_NEMOTECNICO);
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_CONTABILIDAD",tipoContabilidad,con,entrada.p_IdEmpresa));
						log.write(determinante.CDCT_NEMOTECNICO + " no existe");
						continue;
					}
					insertDeterminante.IdDefinicionAsiento = 0L;
					insertDeterminante.IdTipoDatoEvento = determinante.CDCT_TIPODATO.trim();
					insertDeterminante.ValorVarchar = determinante.CDCT_VALOR.trim();
					try{
						insertDeterminante.ValorDecimal = new BigDecimal(determinante.CDCT_VALOR);
					}
					catch(Exception e){
						insertDeterminante.ValorDecimal = BigDecimal.ZERO;
					}
					insertDeterminante.Condicion = determinante.CDCT_CONDICION;
					insertDeterminante.Conector = "AND";
					insertDeterminante.TipoCondicion = "C";
					insertDeterminante.Estado = "V";
					if("=".equals(insertDeterminante.Condicion) || ">".equals(insertDeterminante.Condicion) || "<".equals(insertDeterminante.Condicion) || "<>".equals(insertDeterminante.Condicion)){
						insertDeterminantes.add(insertDeterminante);
					}else{
						HashMap<String,String> condicion = new HashMap<String,String>();
						condicion.put("Condicion", insertDeterminante.Condicion);
						condicion.put("IdTipoContabilidad", determinante.CDCT_NEMOTECNICO);
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"CONDICION_NO_VALIDA_DETERMINANTE",condicion,con,entrada.p_IdEmpresa));
						log.write(insertDeterminante.Condicion + " no valido");
					}
					
					
				}else{
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_FORMATO_LINEA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJE = "UI_ERROR";
				}
			}
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJE = "UI_EXITO";
			sql.batch(con, SQL_INSERT_DETERMINANTE, insertDeterminantes, InsertDeterminante.class);
		} catch(Exception e){
			log.write("Error",e);
			throw e;
		}
		log.write("Termino");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "";
			InCargarDeterminanteComprobante Objin = gson.fromJson(json, InCargarDeterminanteComprobante.class);
			
			CargarDeterminanteComprobante demo=new CargarDeterminanteComprobante();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				OutCargarDeterminanteComprobante ObjOut= demo.logica(con, Objin);
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
