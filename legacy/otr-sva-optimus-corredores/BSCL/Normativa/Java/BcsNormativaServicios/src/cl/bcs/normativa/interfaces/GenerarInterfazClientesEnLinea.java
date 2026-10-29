package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazClientesEnLinea implements JavaCall {
	
	private final Log log =new Log("GenerarInterfazClientesEnLinea");
	
	private final int LARGO_ARCHIVO = 183;
	private final String SQL_SELECT_INFORMACION_CLIENTES_EN_LINEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazClientesEnLinea_SelectInformacionClientesEnLinea.sql");
	private final String SQL_SELECT_DATE_CLIENTES_EN_LINEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazClienteEnLinea_SelectDate.sql");
	private final String SQL_SELECT_SECUENCIA_INTERFAZ = loadResourceToString("cl/bcs/normativa/interfaces/sql/SelectInterfazSecuenciaDiaria.sql");
	private final String SQL_INSERT_SECUENCIA_INTERFAZ = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertInterfazSecuenciaDiaria.sql");
	private final String SQL_UPDATE_SECUENCIA_INTERFAZ = loadResourceToString("cl/bcs/normativa/interfaces/sql/UpdateInterfazSecuenciaDiaria.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazClientesEnLinea{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazClientesEnLinea{
		
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String	p_out_Comentario;
		
	}
	
	public static class ConsultaInformacionClientesEnLinea{
		public String CodigoAI;
		public String IdentificadorUnicoPersona;
		public String TipoCodigoCliente;
		public String CodigoCliente;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String Nombres;
		public String RazonSocial;
		public String FechaInscripcionCliente;
		public String NumeroCorrelativoBenificiario;
	}
	
	public static class ConsultaSecuenciaInterfaz{
		public Long	IdInterfaz;
		public Date Fecha;
		public Long Secuencia;
	}
	
	public static class SecuenciaInterfaz{
		public Long	IdInterfaz;
		public String Fecha;
		public Long Secuencia;
		
		public SecuenciaInterfaz(Long IdInterfaz,String Fecha, Long Secuencia){
			this.IdInterfaz = IdInterfaz;
			this.Fecha = Fecha;
			this.Secuencia = Secuencia;
		}
	}
	
	public static class DateUTC{
		public String UTC;
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
		InGenerarInterfazClientesEnLinea in=gson.fromJson(jin.toString() ,InGenerarInterfazClientesEnLinea.class);
		OutGenerarInterfazClientesEnLinea out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazClientesEnLinea logica(Connection con, InGenerarInterfazClientesEnLinea entrada) throws Exception {
		OutGenerarInterfazClientesEnLinea out = new OutGenerarInterfazClientesEnLinea();
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new java.util.Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		BufferedWriter bwArchivo = null;
		BufferedWriter bwArchivoLog = null;
		try{
			SQL sql=new SQL();
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			HashMap<String,Object> paramsConsultaSecuencia = new HashMap<String,Object>();
			paramsConsultaSecuencia.put("IdInterfaz", 1L);
			paramsConsultaSecuencia.put("Fecha", FechaSistema);
			List<ConsultaSecuenciaInterfaz> secuencias = sql.query(con,SQL_SELECT_SECUENCIA_INTERFAZ,paramsConsultaSecuencia,ConsultaSecuenciaInterfaz.class);
			Long secuencia = 1L;
			List<SecuenciaInterfaz> batch = new ArrayList<SecuenciaInterfaz>();
			if(secuencias.size() > 0){
				secuencia = secuencias.get(0).Secuencia + 1;
				batch.add(new SecuenciaInterfaz(1L,FechaSistema,secuencia));
				sql.batch(con, SQL_UPDATE_SECUENCIA_INTERFAZ, batch, SecuenciaInterfaz.class);
			}else{
				batch.add(new SecuenciaInterfaz(1L,FechaSistema,secuencia));
				sql.batch(con, SQL_INSERT_SECUENCIA_INTERFAZ, batch, SecuenciaInterfaz.class);
			}
			out.p_out_NombreArchivo="nor/CLL_"+fecha+"."+String.format("%03d", secuencia.intValue());
			out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
			out.p_out_NombreArchivoLog="nor/CLL_"+fecha+".log";
			out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
			File archivo = new File(out.p_out_RutaArchivo);
			File archivoLog = new File(out.p_out_RutaArchivoLog);

			bwArchivo = new BufferedWriter(new FileWriter(archivo));
			
			bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
			
			List<DateUTC> fechaUTC = sql.query(con,SQL_SELECT_DATE_CLIENTES_EN_LINEA,new HashMap<String,Object>(),DateUTC.class);
			HashMap<String,Object> paramsConsultaCliente = new HashMap<String,Object>();
			String Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa);
			Fecha = fechaUTC.get(0).UTC + " " + Fecha.split(" ")[1];
			paramsConsultaCliente.put("Fecha", Fecha);
			paramsConsultaCliente.put("IdEmpresa", entrada.p_IdEmpresa);
			
			List<ConsultaInformacionClientesEnLinea> clientes = sql.query(con,SQL_SELECT_INFORMACION_CLIENTES_EN_LINEA,paramsConsultaCliente,ConsultaInformacionClientesEnLinea.class);
			
			for(ConsultaInformacionClientesEnLinea cliente : clientes){
				String ret = "";
				ret += String.format("%-10s", 	(cliente.CodigoAI.length() > 10)? cliente.CodigoAI.substring(0, 10): cliente.CodigoAI);
				ret += String.format("%-14s", 	(cliente.IdentificadorUnicoPersona.length() > 14)? cliente.IdentificadorUnicoPersona.substring(0, 14): cliente.IdentificadorUnicoPersona);
				ret += String.format("%-1s", 	(cliente.TipoCodigoCliente.length() > 1)? cliente.TipoCodigoCliente.substring(0, 1): cliente.TipoCodigoCliente);
				ret += String.format("%-8s", 	(cliente.CodigoCliente.length() > 8)? cliente.CodigoCliente.substring(0, 8): cliente.CodigoCliente);
				ret += String.format("%-30s", 	(cliente.ApellidoPaterno.length() > 30)? cliente.ApellidoPaterno.substring(0, 30): cliente.ApellidoPaterno);
				ret += String.format("%-30s", 	(cliente.ApellidoMaterno.length() > 30)? cliente.ApellidoMaterno.substring(0, 30): cliente.ApellidoMaterno);
				ret += String.format("%-30s", 	(cliente.Nombres.length() > 30)? cliente.Nombres.substring(0, 30): cliente.Nombres);
				ret += String.format("%-50s", 	(cliente.RazonSocial.length() > 50)? cliente.RazonSocial.substring(0, 30): cliente.RazonSocial);
				ret += String.format("%-8s", 	(cliente.FechaInscripcionCliente.length() > 8)? cliente.FechaInscripcionCliente.substring(0, 8): cliente.FechaInscripcionCliente);
				ret += String.format("%-2s", 	(cliente.NumeroCorrelativoBenificiario.length() > 2)? cliente.NumeroCorrelativoBenificiario.substring(0, 2): cliente.NumeroCorrelativoBenificiario);
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
			logEjecucion.IdInterfaz = 1L;
			logEjecucion.NumeroRegistros = new Long(numeroRegistros);
			logEjecucion.NumeroRegistrosCorrectos = new Long(numeroRegistrosCorrecto);
			logEjecucion.NumeroRegistrosIncorrectos = new Long(numeroRegistrosErroneos);
			List<LogEjecucion> logs = new ArrayList<LogEjecucion>();
			logs.add(logEjecucion);
			sql.batch(con, SQL_INSERT_LOG_EJECUCION, logs, LogEjecucion.class);
			
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new java.util.Date());
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
			con.commit();
		}catch(Exception e){
			con.rollback();
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
  				
  				InGenerarInterfazClientesEnLinea entrada= new InGenerarInterfazClientesEnLinea();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazClientesEnLinea demo= new GenerarInterfazClientesEnLinea();
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
