package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazMontosIntermediados implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazMontosIntermediados");
	
	private final int LARGO_ARCHIVO = 59;
	private final String SQL_SELECT_INTERFAZ_MONTOS_INTERMEDIADOS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazMontosIntermediados_SelectInterfazMontosIntermediados.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazMontosIntermediados{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Ruta;
		public String	p_Ejercicio;
	}
	
	public static class OutGenerarInterfazMontosIntermediados{
		
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaMontosIntermediados{
		public String 		TipoRegistro;
		public String		CodigoAI;
		public String		Ejercicio;
		public String		Segmento;
		public String		CodigoCuenta;
		public BigDecimal	Compra;
		public BigDecimal	Venta;
	}
	
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InGenerarInterfazMontosIntermediados in=gson.fromJson(jin.toString() ,InGenerarInterfazMontosIntermediados.class);
		OutGenerarInterfazMontosIntermediados out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazMontosIntermediados logica(Connection con, InGenerarInterfazMontosIntermediados entrada) throws Exception {
		OutGenerarInterfazMontosIntermediados out = new OutGenerarInterfazMontosIntermediados();
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivo="nor/MIM_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/MIM_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaMontosIntermediados = new HashMap<String,Object>();
			paramsConsultaMontosIntermediados.put("Ejercicio", entrada.p_Ejercicio);
			paramsConsultaMontosIntermediados.put("IdEmpresa", entrada.p_IdEmpresa);
			List<ConsultaMontosIntermediados> montos = sql.query(con,SQL_SELECT_INTERFAZ_MONTOS_INTERMEDIADOS,paramsConsultaMontosIntermediados,ConsultaMontosIntermediados.class);
			
			for(ConsultaMontosIntermediados monto : montos){
				String ret = "";
				ret += String.format("%-1s", 	(monto.TipoRegistro.length() > 1)? monto.TipoRegistro.substring(0, 1): monto.TipoRegistro);
				ret += String.format("%-10s", 	(monto.CodigoAI.length() > 10)? monto.CodigoAI.substring(0, 10): monto.CodigoAI);
				ret += String.format("%-6s", 	(monto.Ejercicio.length() > 6)? monto.Ejercicio.substring(0, 6): monto.Ejercicio);
				ret += String.format("%-2s", 	(monto.Segmento.length() > 2)? monto.Segmento.substring(0, 2): monto.Segmento);
				ret += String.format("%-4s", 	(monto.CodigoCuenta.length() > 4)? monto.CodigoCuenta.substring(0, 4): monto.CodigoCuenta);
				ret += String.format("%016d", 	monto.Compra.intValue());
			    ret += String.format("%02d", 	monto.Compra.toPlainString().split(".").length == 2 ? Integer.valueOf(monto.Compra.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%016d", 	monto.Venta.intValue());
			    ret += String.format("%02d", 	monto.Venta.toPlainString().split(".").length == 2 ? Integer.valueOf(monto.Venta.toPlainString().split(".")[1]) : 0);
			    
				numeroRegistros++;
				if(ret.length() != LARGO_ARCHIVO){
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error de tamaño del registro, se esperaba:" + LARGO_ARCHIVO + ",se obtuvo:" + ret.length() + ",registro:" + ret);
					bwArchivoLog.newLine();
				}else{
					numeroRegistrosCorrecto++;
					bwArchivo.write(ret);
					bwArchivo.newLine();
				}
			}
			
			LogEjecucion logEjecucion = new LogEjecucion();
			logEjecucion.IdInterfaz = 10L;
			logEjecucion.NumeroRegistros = new Long(numeroRegistros);
			logEjecucion.NumeroRegistrosCorrectos = new Long(numeroRegistrosCorrecto);
			logEjecucion.NumeroRegistrosIncorrectos = new Long(numeroRegistrosErroneos);
			List<LogEjecucion> logs = new ArrayList<LogEjecucion>();
			logs.add(logEjecucion);
			sql.batch(con, SQL_INSERT_LOG_EJECUCION, logs, LogEjecucion.class);
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			String fechaInicio = "Fecha Hora Inicio:" + fecha;
			String fechaTermino = "Fecha Hora Termino:" + fechaFin;
			String tiempoEjecucion = "Tiempo de ejecución:" + (endTime - startTime);
			String totalRegistros = "Total Registros:" + numeroRegistros;
			String registrosCorrectos = "Registros Correctos:" + numeroRegistrosCorrecto;
			String registrosErroneos = "Registros Erroneos:" + numeroRegistrosErroneos;

			bwArchivoLog.write(fechaInicio);
			bwArchivoLog.newLine();
			bwArchivoLog.write(fechaTermino);
			bwArchivoLog.newLine();
			bwArchivoLog.write(tiempoEjecucion);
			bwArchivoLog.newLine();
			bwArchivoLog.write(totalRegistros);
			bwArchivoLog.newLine();
			bwArchivoLog.write(registrosCorrectos);
			bwArchivoLog.newLine();
			bwArchivoLog.write(registrosErroneos);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Ruta Archivo Salida:" + out.p_out_RutaArchivo);
			out.p_out_ErroresContador = 0L;
			out.p_out_Comentario = totalRegistros + " / " + registrosCorrectos + " / " + registrosErroneos;
			
		}catch(Exception e){
			log.write(e);
		}finally{
			 log.writeAsJson("SALIDA",out);
			 if(bwArchivo !=null){
				 bwArchivo.close();
			 }
			 if(bwArchivoLog != null){
				 bwArchivoLog.close();
			 }
		}
		
		return out;
	}
	
	
	public static void main (String [] args) {
		
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
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
  				
  				InGenerarInterfazMontosIntermediados entrada= new InGenerarInterfazMontosIntermediados();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Ejercicio 	= "062017";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazMontosIntermediados demo= new GenerarInterfazMontosIntermediados();
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
