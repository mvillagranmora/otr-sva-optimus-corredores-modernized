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

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazAsignaciones implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazAsignaciones");
	
	private final int LARGO_ARCHIVO = 77;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "ASI";
	private final String SQL_SELECT_INTERFAZ_ASIGNACIONES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazAsignaciones_SelectInterfazAsignaciones.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazAsignaciones{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_FechaHasta;
		public Long		p_IdCliente;
		public String	p_EsTodosCliente;
		public Long		p_IdInstrumento;
		public String	p_EsTodosInstrumentos;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazAsignaciones{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaAsignaciones{
		public String 		TipoRegistro;
		public String 		CodigoAI;
		public String 		FechaOrden;
		public Long			FolioOrden;
		public String		FechaOperacion;
		public String		HoraMinutoSegundoOperacion;
		public Long			FolioOperacion;
		public BigDecimal	Precio;
		public BigDecimal	CantidadAsignada;
		public String		MonedaOperacion;
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
		InGenerarInterfazAsignaciones in=gson.fromJson(jin.toString() ,InGenerarInterfazAsignaciones.class);
		OutGenerarInterfazAsignaciones out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutGenerarInterfazAsignaciones logica(Connection con, InGenerarInterfazAsignaciones entrada) throws Exception {
		OutGenerarInterfazAsignaciones out = new OutGenerarInterfazAsignaciones();
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
		out.p_out_NombreArchivo="nor/ASI_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/ASI_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaAsignaciones = new HashMap<String,Object>();
			paramsConsultaAsignaciones.put("FechaDesde", entrada.p_FechaDesde);
			paramsConsultaAsignaciones.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsConsultaAsignaciones.put("EsTodosCliente", entrada.p_EsTodosCliente);
			paramsConsultaAsignaciones.put("IdCliente", entrada.p_IdCliente);
			paramsConsultaAsignaciones.put("EsTodosInstrumentos", entrada.p_EsTodosInstrumentos);
			paramsConsultaAsignaciones.put("IdInstrumento", entrada.p_IdInstrumento);
			List<ConsultaAsignaciones> asignaciones = sql.query(con,SQL_SELECT_INTERFAZ_ASIGNACIONES,paramsConsultaAsignaciones,ConsultaAsignaciones.class);
			
			String detalle = "";
			
			for(ConsultaAsignaciones asignacion : asignaciones){
				String ret = "";
				ret += String.format("%-1s", 	(asignacion.TipoRegistro.length() > 1)? asignacion.TipoRegistro.substring(0, 1): asignacion.TipoRegistro);
				ret += String.format("%-10s", 	(asignacion.CodigoAI.length() > 10)? asignacion.CodigoAI.substring(0, 10): asignacion.CodigoAI);
				ret += String.format("%-8s", 	(asignacion.FechaOrden.length() > 8)? asignacion.FechaOrden.substring(0, 8): asignacion.FechaOrden);
				ret += String.format("%06d", 	asignacion.FolioOrden.intValue());
				ret += String.format("%-8s", 	(asignacion.FechaOperacion.length() > 8)? asignacion.FechaOperacion.substring(0, 8): asignacion.FechaOperacion);
				ret += String.format("%-6s", 	(asignacion.HoraMinutoSegundoOperacion.length() > 6)? asignacion.HoraMinutoSegundoOperacion.substring(0, 6): asignacion.HoraMinutoSegundoOperacion);
				ret += String.format("%04d", 	asignacion.FolioOperacion.intValue());
				ret += String.format("%08d", 	asignacion.Precio.intValue());
			    ret += String.format("%08d", 	asignacion.Precio.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.Precio.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%012d", 	asignacion.CantidadAsignada.intValue());
			    ret += String.format("%04d", 	asignacion.CantidadAsignada.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.CantidadAsignada.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-2s", 	(asignacion.MonedaOperacion.length() > 2)? asignacion.MonedaOperacion.substring(0, 2): asignacion.MonedaOperacion);
			    
			    
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
			logEjecucion.IdInterfaz = 8L;
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
  				
  				InGenerarInterfazAsignaciones entrada= new InGenerarInterfazAsignaciones();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "2017-06-06";
  				entrada.p_FechaHasta	= "2017-06-30";
  				entrada.p_IdCliente		=	0L;
  				entrada.p_EsTodosCliente	=	"S";
  				entrada.p_IdInstrumento	=	0L;
  				entrada.p_EsTodosInstrumentos = "S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazAsignaciones demo= new GenerarInterfazAsignaciones();
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
