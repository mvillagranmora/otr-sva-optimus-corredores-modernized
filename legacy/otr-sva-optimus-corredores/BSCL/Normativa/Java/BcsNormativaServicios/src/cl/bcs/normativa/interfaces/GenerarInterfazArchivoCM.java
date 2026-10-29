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

public class GenerarInterfazArchivoCM implements JavaCall{
	private final Log log =new Log("GenerarInterfazArchivoCM");
	private final int LARGO_ARCHIVO = 145;
	
	private final String SQL_SELECT_CIERRE_ORDENES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazArchivoCM_SelectCierreOrdenes.sql");
	private final String SQL_SELECT_ASIGNACIONES   = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazArchivoCM_SelectAsignaciones.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InGenerarInterfazArchivoCM{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazArchivoCM{
		
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class CM{
		public String 		FechaOperacion;
		public Long 		ModalidadOperacion;
		public Long			NumeroOperacion;
		public Long			CodigoParticipante;
		public Long			TipoDetalle;
		public Long			CodigoTitular;
		public BigDecimal	CantidadCavali;
		public BigDecimal	CantidadFisica;
		public Long			Giro;
		public Long			FlagTrabajador;
		public BigDecimal	ComisionContado;
		public BigDecimal	ComisionPlazo;
		public String		FlagPoliza;
		public String		FlagContado;
		public String		FlagPlazo;
		public Long			CodigoCustodioValores;
		public Long			CodigoCustodioFondo;
		public BigDecimal	InteresContado;
		public BigDecimal	InteresPlazo;
		public String		LiquidacionDTC;
		public Long			FlagDayTrade;
		public String		FechaVencimientoDocumento;
		public Long			IndicadorCartaFianza;
		public BigDecimal	MontoCartaFianza;
		public Long			CodigoBanco;
		
	}
	
	public static class Modulo{
		public Long 	IdEmpresa;
		public String 	CodigoModulo;
		public String	IdEstado;
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
		InGenerarInterfazArchivoCM in=gson.fromJson(jin.toString() ,InGenerarInterfazArchivoCM.class);
		OutGenerarInterfazArchivoCM out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazArchivoCM logica(Connection con, InGenerarInterfazArchivoCM entrada) throws Exception {
		OutGenerarInterfazArchivoCM out = new OutGenerarInterfazArchivoCM();
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		SimpleDateFormat fma=new SimpleDateFormat("yyyyMMdd");
		String fechaArchivo = fma.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivo="nor/CM"+fechaArchivo+".TXT";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/CM_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		
		try{
			SQL sql=new SQL();
				HashMap<String,Object> paramsConsultaCM = new HashMap<String,Object>();
				paramsConsultaCM.put("FechaDesde", entrada.p_FechaDesde);
				paramsConsultaCM.put("IdEmpresa", entrada.p_IdEmpresa);
				
				List<CM> asignaciones = sql.query(con,SQL_SELECT_ASIGNACIONES,paramsConsultaCM,CM.class);
				
				for(CM asignacion : asignaciones){
					String ret = "";
					ret += String.format("%-8s",(asignacion.FechaOperacion.length() > 8)? asignacion.FechaOperacion.substring(0, 8): asignacion.FechaOperacion);
					ret += String.format("%02d", asignacion.ModalidadOperacion);
					ret += String.format("%06d", asignacion.NumeroOperacion);
					ret += String.format("%04d", asignacion.CodigoParticipante);
					ret += String.format("%01d", asignacion.TipoDetalle);
					ret += String.format("%08d", asignacion.CodigoTitular);
					ret += String.format("%14d", asignacion.CantidadCavali.intValue());
					ret += String.format("%02d", asignacion.CantidadCavali.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.CantidadCavali.toPlainString().split(".")[1]) : 0);
					ret += String.format("%14d", asignacion.CantidadFisica.intValue());
					ret += String.format("%02d", asignacion.CantidadFisica.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.CantidadFisica.toPlainString().split(".")[1]) : 0);
					ret += String.format("%02d", asignacion.Giro);
					ret += String.format("%02d", asignacion.FlagTrabajador);
					ret += String.format("%03d", asignacion.ComisionContado.intValue());
					ret += String.format("%06d", asignacion.ComisionContado.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.ComisionContado.toPlainString().split(".")[1]) : 0);
					ret += String.format("%03d", asignacion.ComisionPlazo.intValue());
					ret += String.format("%06d", asignacion.ComisionPlazo.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.ComisionPlazo.toPlainString().split(".")[1]) : 0);
					ret += String.format("%-1s", asignacion.FlagPoliza);
					ret += String.format("%-1s", asignacion.FlagContado);
					ret += String.format("%-1s", asignacion.FlagPlazo);
					ret += String.format("%04d", asignacion.CodigoCustodioValores);
					ret += String.format("%04d", asignacion.CodigoCustodioFondo);
					ret += String.format("%08d", asignacion.InteresContado.intValue());
					ret += String.format("%02d", asignacion.InteresContado.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.InteresContado.toPlainString().split(".")[1]) : 0);
					ret += String.format("%08d", asignacion.InteresPlazo.intValue());
					ret += String.format("%02d", asignacion.InteresPlazo.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.InteresPlazo.toPlainString().split(".")[1]) : 0);
					ret += String.format("%-1s", asignacion.LiquidacionDTC);
					ret += String.format("%01d", asignacion.FlagDayTrade);
					ret += String.format("%-8s",(asignacion.FechaVencimientoDocumento.length() > 8)? asignacion.FechaVencimientoDocumento.substring(0, 8): asignacion.FechaVencimientoDocumento);
					ret += String.format("%01d", asignacion.IndicadorCartaFianza);
					ret += String.format("%014d", asignacion.MontoCartaFianza.intValue());
					ret += String.format("%02d", asignacion.MontoCartaFianza.toPlainString().split(".").length == 2 ? Integer.valueOf(asignacion.MontoCartaFianza.toPlainString().split(".")[1]) : 0);
					ret += String.format("%04d", asignacion.CodigoBanco);
					
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
			logEjecucion.IdInterfaz = 13L;
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
  				
  				InGenerarInterfazArchivoCM entrada= new InGenerarInterfazArchivoCM();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "NOR";
  				entrada.p_FechaDesde	= "2017-06-06";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazArchivoCM demo= new GenerarInterfazArchivoCM();
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
