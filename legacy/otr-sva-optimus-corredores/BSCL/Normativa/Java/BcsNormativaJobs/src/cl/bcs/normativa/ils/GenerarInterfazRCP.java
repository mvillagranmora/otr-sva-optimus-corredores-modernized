package cl.bcs.normativa.ils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazRCP implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazRCP");
	
	private final int LARGO_ARCHIVO = 78;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "RCP";
	private final String SQL_SELECT_INTERFAZ_RCP =  loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazRCP_SelectInterfaz.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazRCP{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazRCP{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
	}

	

	public static class RegistroRCP{
		public String CodigoAI;
		public String FechaInformacion;
		public String FlagInscrito;
		public String CodigoIF;		
		public String CantidadNominal;		
		public String Precio;
		public String FactorPonderacion;		
	}
	
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosInCorrectos;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InGenerarInterfazRCP in=gson.fromJson(jin.toString() ,InGenerarInterfazRCP.class);
		OutGenerarInterfazRCP out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazRCP logica(Connection con, InGenerarInterfazRCP entrada) throws Exception {
		OutGenerarInterfazRCP out = new OutGenerarInterfazRCP();
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		
		String fechaSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		SimpleDateFormat formatFechaDB=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date fechaDB = formatFechaDB.parse(fechaSistema);
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		SimpleDateFormat fmc = new SimpleDateFormat("ddMMyyyy");
		String fecha = fm.format(fechaDB);
		String fechaCabecera = fmc.format(fechaDB);
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivo="RCP_"+fechaCabecera+".cod";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="RCP_"+fechaCabecera+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaCliente = new HashMap<String,Object>();
			//paramsConsultaCliente.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramsConsultaCliente.put("p_FechaDesde", fechaCabecera);
			List<RegistroRCP> Registros = sql.query(con,SQL_SELECT_INTERFAZ_RCP,paramsConsultaCliente,RegistroRCP.class);
			
			String detalle = "";
			
			for(RegistroRCP Registro : Registros){
				String ret = "";

				ret += String.format("%-10s", 	(Registro.CodigoAI.length() > 10)? Registro.CodigoAI.substring(0, 10): Registro.CodigoAI);
				ret += String.format("%-8s", 	(Registro.FechaInformacion.length() > 8)? Registro.FechaInformacion.substring(0, 8): Registro.FechaInformacion);				
				ret += String.format("%-1s", 	(Registro.FlagInscrito.length() > 1)? Registro.FlagInscrito.substring(0, 1): Registro.FlagInscrito);				
				ret += String.format("%-20s", 	(Registro.CodigoIF.length() > 20)? Registro.CodigoIF.substring(0, 20): Registro.CodigoIF);				
				ret += String.format("%-16s", 	(Registro.CantidadNominal.length() > 16)? Registro.CantidadNominal.substring(0, 16): Registro.CantidadNominal);				
				ret += String.format("%-16s", 	(Registro.Precio.length() > 16)? Registro.Precio.substring(0, 16): Registro.Precio);				
				ret += String.format("%-7s", 	(Registro.FactorPonderacion.length() > 7)? Registro.FactorPonderacion.substring(0, 7): Registro.FactorPonderacion);				

				numeroRegistros++;
				if(ret.length() != LARGO_ARCHIVO){
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error de tamaño del registro, se esperaba:" + LARGO_ARCHIVO + ",se obtuvo:" + ret.length() + ",registro:" + ret);
					bwArchivoLog.newLine();
				}else{
					numeroRegistrosCorrecto++;
					detalle += ret + System.getProperty("line.separator");
				}
			}

			// Archivo sin cabecera...			
			// String cabecera = "C" + String.format("%-10s",CodigoAI) + IdArchivo + fechaCabecera + String.format("%06d", numeroRegistros);
			// bwArchivo.write(cabecera);
			// bwArchivo.newLine();
			bwArchivo.write(detalle);
			
			/*
			LogEjecucion logEjecucion = new LogEjecucion();
			logEjecucion.IdInterfaz = 3L;
			logEjecucion.NumeroRegistros = new Long(numeroRegistros);
			logEjecucion.NumeroRegistrosCorrectos = new Long(numeroRegistrosCorrecto);
			logEjecucion.NumeroRegistrosInCorrectos = new Long(numeroRegistrosErroneos);
			List<LogEjecucion> logs = new ArrayList<LogEjecucion>();
			logs.add(logEjecucion);
			sql.batch(con, SQL_INSERT_LOG_EJECUCION, logs, LogEjecucion.class);
			*/
			
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			bwArchivoLog.write("Fecha Hora Inicio:" + fecha);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Fecha Hora Termino:" + fechaFin);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Tiempo de ejecución:" + (endTime - startTime));
			bwArchivoLog.newLine();
			bwArchivoLog.write("Total Registros:" + numeroRegistros);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Correctos:" + numeroRegistrosCorrecto);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Erroneos:" + numeroRegistrosErroneos);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Ruta Archivo Salida:" + out.p_out_RutaArchivo);
			out.p_out_ErroresContador = 0L;
			
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
		
        //prueba de la clase
        try (Connection con = PlataformaUtils.getDESAPEConnection()) {
  			try{
  				
  				InGenerarInterfazRCP entrada= new InGenerarInterfazRCP();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ILS";
  				entrada.p_FechaDesde	= "2017-01-30";
  				entrada.p_Ruta			= "C:\\temp\\";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazRCP demo= new GenerarInterfazRCP();
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
