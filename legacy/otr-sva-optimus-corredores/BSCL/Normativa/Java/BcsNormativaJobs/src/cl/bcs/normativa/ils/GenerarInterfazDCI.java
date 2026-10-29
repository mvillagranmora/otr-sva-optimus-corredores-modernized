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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazDCI implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazDCI");
	
	private final int LARGO_ARCHIVO = 35;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "DCI";
	private final String SQL_SELECT_INTERFAZ_DCI =  loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazDCI_SelectInterfaz.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazDCI{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazDCI{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
	}

	

	public static class RegistroDCI{
		public String CodigoAI;
		public String FechaInformacion;
		public String CodigoCuenta;
		public String Signo;		
		public String Monto;		
		public String TipoInformacion;		
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
		InGenerarInterfazDCI in=gson.fromJson(jin.toString() ,InGenerarInterfazDCI.class);
		OutGenerarInterfazDCI out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazDCI logica(Connection con, InGenerarInterfazDCI entrada) throws Exception {
		OutGenerarInterfazDCI out = new OutGenerarInterfazDCI();
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
		out.p_out_NombreArchivo="DCI_"+fechaCabecera+".cod";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="DCI_"+fechaCabecera+".log";
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
			List<RegistroDCI> Registros = sql.query(con,SQL_SELECT_INTERFAZ_DCI,paramsConsultaCliente,RegistroDCI.class);
			
			String detalle = "";
			
			for(RegistroDCI Registro : Registros){
				String ret = "";

				ret += String.format("%-10s", 	(Registro.CodigoAI.length() > 10)? Registro.CodigoAI.substring(0, 10): Registro.CodigoAI);
				ret += String.format("%-8s", 	(Registro.FechaInformacion.length() > 8)? Registro.FechaInformacion.substring(0, 8): Registro.FechaInformacion);				
				ret += String.format("%-3s", 	(Registro.CodigoCuenta.length() > 3)? Registro.CodigoCuenta.substring(0, 3): Registro.CodigoCuenta);				
				ret += String.format("%-1s", 	(Registro.Signo.length() > 1)? Registro.Signo.substring(0, 1): Registro.Signo);				
				ret += String.format("%-12s", 	(Registro.Monto.length() > 12)? Registro.Monto.substring(0, 12): Registro.Monto);			
				ret += String.format("%-1s", 	(Registro.TipoInformacion.length() > 1)? Registro.TipoInformacion.substring(0, 1): Registro.TipoInformacion);				

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
  				
  				InGenerarInterfazDCI entrada= new InGenerarInterfazDCI();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ILS";
  				entrada.p_FechaDesde	= "2017-01-30";
  				entrada.p_Ruta			= "C:\\temp\\";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazDCI demo= new GenerarInterfazDCI();
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
