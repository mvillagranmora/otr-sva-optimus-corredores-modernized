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

public class GenerarInterfazClientesFinales implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazClientesFinales");
	
	private final int LARGO_ARCHIVO = 52;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "CLF";
	private final String SQL_SELECT_INTERFAZ_CLIENTES_FINALES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazClientesFinales_SelectInterfazClientesFinales.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazClientesFinales{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_FechaHasta;
		public Long		p_IdCliente;
		public String	p_EsTodosCliente;
		public String 	p_Ruta;
	}
	
	public static class OutGenerarInterfazClientesFinales{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaClientesFinales{
		public String 		TipoRegistro;
		public String		FechaOrden;
		public Long			FolioOrden;
		public String		TipoCodigoCliente;
		public String		CodigoCliente;
		public BigDecimal	Cantidad;
		public String		FechaOperacion;
		public Long			FolioOperacion;
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
		InGenerarInterfazClientesFinales in=gson.fromJson(jin.toString() ,InGenerarInterfazClientesFinales.class);
		OutGenerarInterfazClientesFinales out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutGenerarInterfazClientesFinales logica(Connection con, InGenerarInterfazClientesFinales entrada) throws Exception {
		OutGenerarInterfazClientesFinales out = new OutGenerarInterfazClientesFinales();
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
		out.p_out_NombreArchivo="nor/CLF_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/CLF_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaClientes = new HashMap<String,Object>();
			paramsConsultaClientes.put("FechaDesde", entrada.p_FechaDesde);
			paramsConsultaClientes.put("EsTodosCliente", entrada.p_EsTodosCliente);
			paramsConsultaClientes.put("IdCliente", entrada.p_IdCliente);
			paramsConsultaClientes.put("IdEmpresa", entrada.p_IdEmpresa);
			List<ConsultaClientesFinales> clientes = sql.query(con,SQL_SELECT_INTERFAZ_CLIENTES_FINALES,paramsConsultaClientes,ConsultaClientesFinales.class);
			
			String detalle = "";
			
			for(ConsultaClientesFinales cliente : clientes){
				String ret = "";
				ret += String.format("%-1s", 	(cliente.TipoRegistro.length() > 1)? cliente.TipoRegistro.substring(0, 1): cliente.TipoRegistro);
				ret += String.format("%-8s", 	(cliente.FechaOrden.length() > 8)? cliente.FechaOrden.substring(0, 8): cliente.FechaOrden);
				ret += String.format("%06d", 	cliente.FolioOrden.intValue());
				ret += String.format("%-1s", 	(cliente.TipoCodigoCliente.length() > 1)? cliente.TipoCodigoCliente.substring(0, 1): cliente.TipoCodigoCliente);
			    ret += String.format("%-8s", 	(cliente.CodigoCliente.length() > 8)? cliente.CodigoCliente.substring(0, 8): cliente.CodigoCliente);
			    ret += String.format("%012d", 	cliente.Cantidad.intValue());
			    ret += String.format("%04d", 	cliente.Cantidad.toPlainString().split(".").length == 2 ? Integer.valueOf(cliente.Cantidad.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-8s", 	(cliente.FechaOperacion.length() > 8)? cliente.FechaOperacion.substring(0, 8): cliente.FechaOperacion);
			    ret += String.format("%04d", 	cliente.FolioOperacion.intValue());
			    
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
			logEjecucion.IdInterfaz = 7L;
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
  				
  				InGenerarInterfazClientesFinales entrada= new InGenerarInterfazClientesFinales();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "2016-06-30";
  				entrada.p_FechaHasta	= "2017-06-30";
  				entrada.p_IdCliente		=	0L;
  				entrada.p_EsTodosCliente	=	"S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazClientesFinales demo= new GenerarInterfazClientesFinales();
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
