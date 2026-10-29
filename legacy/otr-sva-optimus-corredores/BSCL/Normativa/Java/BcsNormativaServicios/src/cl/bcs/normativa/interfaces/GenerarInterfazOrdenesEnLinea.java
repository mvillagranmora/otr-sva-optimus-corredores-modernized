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

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazOrdenesEnLinea implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazOrdenesEnLinea");
	
	private final int LARGO_ARCHIVO = 127;
	private final String SQL_SELECT_INFORMACION_ORDENES_EN_LINEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazOrdenesEnLinea_SelectInformacionOrdenesEnLinea.sql");
	private final String SQL_SELECT_SECUENCIA_INTERFAZ = loadResourceToString("cl/bcs/normativa/interfaces/sql/SelectInterfazSecuenciaDiaria.sql");
	private final String SQL_INSERT_SECUENCIA_INTERFAZ = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertInterfazSecuenciaDiaria.sql");
	private final String SQL_UPDATE_SECUENCIA_INTERFAZ = loadResourceToString("cl/bcs/normativa/interfaces/sql/UpdateInterfazSecuenciaDiaria.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazOrdenesEnLinea{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazOrdenesEnLinea{
		
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaOrdenesEnLinea{
		public String 		CodigoAI;
		public String 		FechaOrden;
		public Long			FolioOrden;
		public String		HoraMinutoSegundoOrden;
		public String		Mercado;
		public String		TipoCodigoCliente;
		public String		CodigoCliente;
		public String		TipoOperacion;
		public String		ModalidadOperacion;
		public String		FlagInscrito;
		public String		CodigoValor;
		public BigDecimal	CantidadOrden;
		public BigDecimal	Precio;
		public BigDecimal	MontoOrdenado;
		public String		FlagPrecio;
		public String		MonedaOrdenado;
		public BigDecimal	PrecioStop;
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
	
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InGenerarInterfazOrdenesEnLinea in=gson.fromJson(jin.toString() ,InGenerarInterfazOrdenesEnLinea.class);
		OutGenerarInterfazOrdenesEnLinea out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazOrdenesEnLinea logica(Connection con, InGenerarInterfazOrdenesEnLinea entrada) throws Exception {
		OutGenerarInterfazOrdenesEnLinea out = new OutGenerarInterfazOrdenesEnLinea();
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		BufferedWriter bwArchivoLog = null;
		BufferedWriter bwArchivo = null;
		try{
			SQL sql=new SQL();
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			HashMap<String,Object> paramsConsultaSecuencia = new HashMap<String,Object>();
			paramsConsultaSecuencia.put("IdInterfaz", 2L);
			paramsConsultaSecuencia.put("Fecha", FechaSistema);
			List<ConsultaSecuenciaInterfaz> secuencias = sql.query(con,SQL_SELECT_SECUENCIA_INTERFAZ,paramsConsultaSecuencia,ConsultaSecuenciaInterfaz.class);
			Long secuencia = 1L;
			List<SecuenciaInterfaz> batch = new ArrayList<SecuenciaInterfaz>();
			if(secuencias.size() > 0){
				secuencia = secuencias.get(0).Secuencia + 1;
				batch.add(new SecuenciaInterfaz(2L,FechaSistema,secuencia));
				sql.batch(con, SQL_UPDATE_SECUENCIA_INTERFAZ, batch, SecuenciaInterfaz.class);
			}else{
				batch.add(new SecuenciaInterfaz(2L,FechaSistema,secuencia));
				sql.batch(con, SQL_INSERT_SECUENCIA_INTERFAZ, batch, SecuenciaInterfaz.class);
			}
			out.p_out_NombreArchivo="nor/ORL_"+fecha+"."+String.format("%03d", secuencia.intValue());
			out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
			out.p_out_NombreArchivoLog="nor/ORL_"+fecha+".log"; 
			out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
			File archivo = new File(out.p_out_RutaArchivo);
			File archivoLog = new File(out.p_out_RutaArchivoLog);
			bwArchivo = new BufferedWriter(new FileWriter(archivo));
			bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
			
			HashMap<String,Object> paramsConsultaOrdenes = new HashMap<String,Object>();
			
			String Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa);
			paramsConsultaOrdenes.put("Fecha", Fecha);
			paramsConsultaOrdenes.put("IdEmpresa", entrada.p_IdEmpresa);
			
			List<ConsultaOrdenesEnLinea> ordenes = sql.query(con,SQL_SELECT_INFORMACION_ORDENES_EN_LINEA,paramsConsultaOrdenes,ConsultaOrdenesEnLinea.class);
			
			for(ConsultaOrdenesEnLinea orden : ordenes){
				String ret = "";
				ret += String.format("%-10s", 	(orden.CodigoAI.length() > 10)? orden.CodigoAI.substring(0, 10): orden.CodigoAI);
				ret += String.format("%-8s", 	(orden.FechaOrden.length() > 8)? orden.FechaOrden.substring(0, 8): orden.FechaOrden);
				ret += String.format("%06d", 	orden.FolioOrden.intValue());
				ret += String.format("%-6s", 	(orden.HoraMinutoSegundoOrden.length() > 6)? orden.HoraMinutoSegundoOrden.substring(0, 6): orden.HoraMinutoSegundoOrden);
				ret += String.format("%-1s", 	(orden.Mercado.length() > 1)? orden.Mercado.substring(0, 1): orden.Mercado);
				ret += String.format("%-1s", 	(orden.TipoCodigoCliente.length() > 1)? orden.TipoCodigoCliente.substring(0, 1): orden.TipoCodigoCliente);
				ret += String.format("%-8s", 	(orden.CodigoCliente.length() > 8)? orden.CodigoCliente.substring(0, 8): orden.CodigoCliente);
				ret += String.format("%-1s", 	(orden.TipoOperacion.length() > 1)? orden.TipoOperacion.substring(0, 1): orden.TipoOperacion);
				ret += String.format("%-2s", 	(orden.ModalidadOperacion.length() > 2)? orden.ModalidadOperacion.substring(0, 2): orden.ModalidadOperacion);
				ret += String.format("%-1s", 	(orden.FlagInscrito.length() > 1)? orden.FlagInscrito.substring(0, 1): orden.FlagInscrito);
				ret += String.format("%-20s", 	(orden.CodigoValor.length() > 20)? orden.CodigoValor.substring(0, 20): orden.CodigoValor);
				ret += String.format("%012d", 	orden.CantidadOrden.intValue());
				ret += String.format("%08d", 	orden.Precio.intValue());
			    ret += String.format("%08d", 	orden.Precio.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.Precio.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%012d", 	orden.MontoOrdenado.intValue());
			    ret += String.format("%04d", 	orden.MontoOrdenado.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.MontoOrdenado.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-1s", 	(orden.FlagPrecio.length() > 1)? orden.FlagPrecio.substring(0, 1): orden.FlagPrecio);    
			    ret += String.format("%-2s", 	(orden.MonedaOrdenado.length() > 2)? orden.MonedaOrdenado.substring(0, 2): orden.MonedaOrdenado);
			    ret += String.format("%08d", 	orden.PrecioStop.intValue());
			    ret += String.format("%08d", 	orden.PrecioStop.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.PrecioStop.toPlainString().split(".")[1]) : 0);
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
			logEjecucion.IdInterfaz = 3L;
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
  				
  				InGenerarInterfazOrdenesEnLinea entrada= new InGenerarInterfazOrdenesEnLinea();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazOrdenesEnLinea demo= new GenerarInterfazOrdenesEnLinea();
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
