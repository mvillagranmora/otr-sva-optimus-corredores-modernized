package cl.bcs.normativa.interfaces;

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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazInstrumentosFinancieros implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazInstrumentosFinancieros");
	
	private final int LARGO_ARCHIVO = 216;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "RIF";
	private final String SQL_SELECT_INTERFAZ_INSTRUMENTOS_FINANCIEROS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazInstrumentosFinancieros_SelectInterfazInstrumentosFinancieros.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazInstrumentosFinancieros{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_FechaHasta;
		public Long		p_IdInstrumento;
		public String	p_EsTodosInstrumentos;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazInstrumentosFinancieros{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaInstrumentosFinancieros{
		public String	TipoRegistro;
		public String	CodigoAI;
		public String	CodigoIF;
		public String	IdentificadorUnicoPersona;
		public String	RazonSocial;
		public String	Moneda;
		public String 	TipoInstrumento;
		public String	Direccion;
		public String	Ubigeo;
		public String	Pais;
		public String	FechaVencimiento;
		public String	PrecioDerivado;
		public String	IdentificadorUnico;
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
		InGenerarInterfazInstrumentosFinancieros in=gson.fromJson(jin.toString() ,InGenerarInterfazInstrumentosFinancieros.class);
		OutGenerarInterfazInstrumentosFinancieros out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazInstrumentosFinancieros logica(Connection con, InGenerarInterfazInstrumentosFinancieros entrada) throws Exception {
		OutGenerarInterfazInstrumentosFinancieros out = new OutGenerarInterfazInstrumentosFinancieros();
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
		out.p_out_NombreArchivo="nor/RIF_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/RIF_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaInstrumentos = new HashMap<String,Object>();
			paramsConsultaInstrumentos.put("FechaDesde", entrada.p_FechaDesde);
			paramsConsultaInstrumentos.put("EsTodosInstrumentos", entrada.p_EsTodosInstrumentos);
			paramsConsultaInstrumentos.put("IdInstrumento", entrada.p_IdInstrumento);
			List<ConsultaInstrumentosFinancieros> instrumentos = sql.query(con,SQL_SELECT_INTERFAZ_INSTRUMENTOS_FINANCIEROS,paramsConsultaInstrumentos,ConsultaInstrumentosFinancieros.class);
			
			String detalle = "";
			
			for(ConsultaInstrumentosFinancieros instrumento : instrumentos){
				String ret = "";
				
				ret += String.format("%-1s", 	(instrumento.TipoRegistro.length() > 1)? instrumento.TipoRegistro.substring(0, 1): instrumento.TipoRegistro);
				ret += String.format("%-10s", 	(instrumento.CodigoAI.length() > 10)? instrumento.CodigoAI.substring(0, 10): instrumento.CodigoAI);
				ret += String.format("%-20s", 	(instrumento.CodigoIF.length() > 10)? instrumento.CodigoIF.substring(0, 10): instrumento.CodigoIF);
				ret += String.format("%-14s", 	(instrumento.IdentificadorUnicoPersona.length() > 14)? instrumento.IdentificadorUnicoPersona.substring(0, 14): instrumento.IdentificadorUnicoPersona);
				ret += String.format("%-50s", 	(instrumento.RazonSocial.length() > 50)? instrumento.RazonSocial.substring(0, 50): instrumento.RazonSocial);
				ret += String.format("%-2s", 	(instrumento.Moneda.length() > 2)? instrumento.Moneda.substring(0, 2): instrumento.Moneda);
				ret += String.format("%-2s", 	(instrumento.TipoInstrumento.length() > 2)? instrumento.TipoInstrumento.substring(0, 2): instrumento.TipoInstrumento);
				ret += String.format("%-60s", 	(instrumento.Direccion.length() > 60)? instrumento.Direccion.substring(0, 60): instrumento.Direccion);
				ret += String.format("%-6s", 	(instrumento.Ubigeo.length() > 6)? instrumento.Ubigeo.substring(0, 6): instrumento.Ubigeo);
				ret += String.format("%-3s", 	(instrumento.Pais.length() > 3)? instrumento.Pais.substring(0, 3): instrumento.Pais);
				ret += String.format("%-8s", 	(instrumento.FechaVencimiento.length() > 8)? instrumento.FechaVencimiento.substring(0, 8): instrumento.FechaVencimiento);
				ret += String.format("%-20s", 	(instrumento.PrecioDerivado.length() > 20)? instrumento.PrecioDerivado.substring(0, 20): instrumento.PrecioDerivado);
				ret += String.format("%-20s", 	(instrumento.IdentificadorUnico.length() > 20)? instrumento.IdentificadorUnico.substring(0, 20): instrumento.IdentificadorUnico);
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
			
			String cabecera = "C" + String.format("%-10s",CodigoAI) + IdArchivo + fechaCabecera + String.format("%06d", numeroRegistros);
			
			bwArchivo.write(cabecera);
			bwArchivo.newLine();
			bwArchivo.write(detalle);
			LogEjecucion logEjecucion = new LogEjecucion();
			logEjecucion.IdInterfaz = 4L;
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
  				
  				InGenerarInterfazInstrumentosFinancieros entrada= new InGenerarInterfazInstrumentosFinancieros();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "1987-06-30";
  				entrada.p_FechaHasta	= "2017-06-30";
  				entrada.p_IdInstrumento	=	0L;
  				entrada.p_EsTodosInstrumentos = "S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazInstrumentosFinancieros demo= new GenerarInterfazInstrumentosFinancieros();
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
