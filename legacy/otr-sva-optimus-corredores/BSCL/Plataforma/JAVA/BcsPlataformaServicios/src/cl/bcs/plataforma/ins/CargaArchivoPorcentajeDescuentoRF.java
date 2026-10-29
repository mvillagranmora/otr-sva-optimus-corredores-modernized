package cl.bcs.plataforma.ins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV.CargaValorRiesgoRV;
import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV.InCargaValorRiesgoRV;
import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV.OutCargaValorRiesgoRV;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoPorcentajeDescuentoRF implements JavaCall{
	
	private final Log log = new Log("CargaArchivoPorcentajeDescuentoRF");
	
	private final String SQL_UPDATE_CARGA_PORCENTAJE_DESCUENTO_RF = loadResourceToString("cl/bcs/plataforma/ins/sql/CargaArchivoPorcentajeDescuentoRF_UpdatePorcentajeDescuento.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/ins/sql/ConsultarMaestroInstrumentosPorClasificacion.sql");

	
	public static class CargaPorcentajeDescuentoRF{
		public String 		Nemotecnico;
		public BigDecimal	PorcentajeDescuento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaPorcentajeDescuentoRF{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public List<CargaPorcentajeDescuentoRF> p_ListaCarga;
	}
	
	public static class OutCargaPorcentajeDescuentoRF{
		public String 				p_out_NombreArchivoLog;
		public String 				p_out_RutaArchivoLog;
		public Long 				p_out_ErroresContador;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public OutCargaPorcentajeDescuentoRF(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}	
	}
	
	public static class Instrumentos{
		public Long		IdInstrumento;
		public String	IdMoneda;
		public String	Nemotecnico;
		public String   IdClasificacionTipoInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaPorcentajeDescuentoRF in=gson.fromJson(jin.toString() ,InCargaPorcentajeDescuentoRF.class);
		OutCargaPorcentajeDescuentoRF out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaPorcentajeDescuentoRF logica(Connection con, InCargaPorcentajeDescuentoRF entrada) throws Exception {
		OutCargaPorcentajeDescuentoRF out = new OutCargaPorcentajeDescuentoRF();
		
		out.p_out_ErroresContador = 0L;
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		
		try{
			SQL sql=new SQL();
									
			//List<String> nemotecnicos = new ArrayList<String>();
			
			// ---------------------------------------------------------------------------------------------
			// BUSCA INFORMACION RELACIONADA AL REGISTRO						
			HashMap<String,Object> params = new HashMap<>();
			
			// ---------------------------------------------------------------------------------------------
			// RECORRE REGISTROS DEL ARCHIVO Y METE DATA EN LISTA ("operaciones") PARA SU PROCESAMIENTO
			// ---------------------------------------------------------------------------------------------
			
			for (CargaPorcentajeDescuentoRF data : entrada.p_ListaCarga){
				
//				params.put("Nemotecnico", data.Nemotecnico);
//				params.put("CodigoClasificacion", "RV");
//				List<Instrumentos> Instrumento = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
//				
//				
//				
//				CargaValorRiesgoRV dataVal = new CargaValorRiesgoRV();	
//				
//				for (Instrumentos ins : Instrumentos){
//					if (ins.Nemotecnico.equalsIgnoreCase(data.Nemotecnico)){
//						operacionesUpdate = new ArrayList<CargaValorRiesgoRV>();
//						dataVal.Nemotecnico = data.Nemotecnico;
//						dataVal.Entidad = data.Entidad;
//						dataVal.Valor = data.Valor;
//						
//						operacionesUpdate.add(dataVal);
//						
//						sql.batch(con, SQL_UPDATE_CARGA_VALOR_RIESGO_RV, operacionesUpdate, CargaValorRiesgoRV.class);
//						
//						msg.id = ContadorMSG;
//						msg.tipo = "OK";
//						msg.codigo = "NEMOTECNICO_OK";
//						msg.descripcion = "El Valor de Riesgo de la Entidad Clasificadora: " + dataVal.Entidad + " del Nemotecnico " + dataVal.Nemotecnico + " fue modificado a: " + dataVal.Valor + ".";
//						salida.P_OUT_MENSAJES.add(msg);
//						
//						//evaluar salida
//						long endTime = System.currentTimeMillis();
//						String fechaFin = fm.format(new Date());
//						bwArchivoLog.write("Fecha Hora Inicio:" + fecha);
//						bwArchivoLog.newLine();
//						bwArchivoLog.write("Fecha Hora Termino:" + fechaFin);
//						bwArchivoLog.newLine();
//						bwArchivoLog.write("Tiempo de ejecución:" + (endTime - startTime));
//						bwArchivoLog.newLine();
//						bwArchivoLog.write("Total Registros:" + numeroRegistros);
//						bwArchivoLog.newLine();
//						bwArchivoLog.write("Registros Correctos:" + numeroRegistrosCorrecto);
//						bwArchivoLog.newLine();
//						bwArchivoLog.write("Registros Erroneos:" + numeroRegistrosErroneos);
//						out.p_out_ErroresContador = new Long(numeroRegistrosErroneos);
//					}
//				}
//				if (dataVal.Nemotecnico == null){
//					msg.id = ContadorMSG;
//					msg.tipo = "ERROR";
//					msg.codigo = "NEMOTECNICO_ERROR";
//					msg.descripcion = "Nemotecnico " + dataVal.Nemotecnico + " no existe.";
//					salida.P_OUT_MENSAJES.add(msg);
//					
//					ContadorMSG++;
//				} 					
				
				CargaPorcentajeDescuentoRF dataVal = new CargaPorcentajeDescuentoRF();	
				
				dataVal.Nemotecnico 		= data.Nemotecnico;
				dataVal.PorcentajeDescuento	= data.PorcentajeDescuento;
				
//				params = new HashMap<>();
//				 
//				params.put("Nemotecnico", data.Nemotecnico);
//				params.put("Entidad", data.Entidad);
//				params.put("Valor", data.Valor);
				List<CargaPorcentajeDescuentoRF> paramUpdateDet = new ArrayList<>();
				paramUpdateDet.add(dataVal);
				
				sql.batch(con, SQL_UPDATE_CARGA_PORCENTAJE_DESCUENTO_RF, paramUpdateDet, CargaPorcentajeDescuentoRF.class);
					
			}			
			
			
		}catch(Exception e){
			log.write(e);
		}finally{
			
		}
		
		return out;
		
	}
	
	public static void main (String [] args) {
		
		//Settings by file
        /*String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";*/
		
		String dbURL  = "jdbc:sap://161.238.34.163:30015/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "desarrollo";
        String pass   = "Bolsa123";
        
        
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
       
        
        //prueba de la clase
  		try {
  			Connection con = ds.getConnection();
  			try{
  				
  				InCargaValorRiesgoRV entrada= new InCargaValorRiesgoRV();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoPresenciaBursatil demo= new CargaArchivoPresenciaBursatil();
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
