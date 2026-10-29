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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInterfazDetalleOperacionesPolizas implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazDetalleOperacionesPolizas");
	
	private final int LARGO_ARCHIVO = 53;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "DPO";
	private final String SQL_SELECT_INTERFAZ_DETALLE_OPERACIONES_POLIZAS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazDetalleOperacionesPolizas_SelectInterfazOperacionesPolizas.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazDetalleOperacionesPolizas{
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
	
	public static class OutGenerarInterfazDetalleOperacionesPolizas{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaDetalleOperacionesPolizas{
		public String		TipoRegistro;
		public String		FechaOperacionPoliza;
		public Long			NumeroPolizaEmitida;
		public String		FolioOperacion;
		public BigDecimal	ImportePagadoCliente;
		public String		FechaOrden;
		public Long			FolioOrden;
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
		InGenerarInterfazDetalleOperacionesPolizas in=gson.fromJson(jin.toString() ,InGenerarInterfazDetalleOperacionesPolizas.class);
		OutGenerarInterfazDetalleOperacionesPolizas out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutGenerarInterfazDetalleOperacionesPolizas logica(Connection con, InGenerarInterfazDetalleOperacionesPolizas entrada) throws Exception {
		OutGenerarInterfazDetalleOperacionesPolizas out = new OutGenerarInterfazDetalleOperacionesPolizas();
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
		out.p_out_NombreArchivo="nor/DPO_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/DPO_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaDetallePolizas = new HashMap<String,Object>();
			paramsConsultaDetallePolizas.put("FechaDesde",entrada.p_FechaDesde);
			paramsConsultaDetallePolizas.put("EsTodosCliente",entrada.p_EsTodosCliente);
			paramsConsultaDetallePolizas.put("IdCliente",entrada.p_IdCliente);
			paramsConsultaDetallePolizas.put("EsTodosInstrumentos",entrada.p_EsTodosInstrumentos);
			paramsConsultaDetallePolizas.put("IdInstrumento",entrada.p_IdInstrumento);
			paramsConsultaDetallePolizas.put("IdEmpresa",entrada.p_IdEmpresa);
			List<ConsultaDetalleOperacionesPolizas> polizas = sql.query(con,SQL_SELECT_INTERFAZ_DETALLE_OPERACIONES_POLIZAS,paramsConsultaDetallePolizas,ConsultaDetalleOperacionesPolizas.class);
			
			String detalle = "";
			
			for(ConsultaDetalleOperacionesPolizas poliza : polizas){
				String ret = "";
				ret += String.format("%-1s", 	(poliza.TipoRegistro.length() > 1)? poliza.TipoRegistro.substring(0, 1): poliza.TipoRegistro);
				ret += String.format("%-8s", 	(poliza.FechaOperacionPoliza.length() > 8)? poliza.FechaOperacionPoliza.substring(0, 8): poliza.FechaOperacionPoliza);
				ret += String.format("%015d", 	poliza.NumeroPolizaEmitida.intValue());
				ret += String.format("%4s", 	(poliza.FolioOperacion.length() > 4)? poliza.FolioOperacion.substring(0, 4): poliza.FolioOperacion);
				ret += String.format("%09d", 	poliza.ImportePagadoCliente.intValue());
			    ret += String.format("%02d", 	poliza.ImportePagadoCliente.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.ImportePagadoCliente.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-8s", 	(poliza.FechaOrden.length() > 8)? poliza.FechaOrden.substring(0, 8): poliza.FechaOrden);
			    ret += String.format("%06d", 	poliza.FolioOrden.intValue());
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
			logEjecucion.IdInterfaz = 9L;
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
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getQAPEConnection();
  			try{
  				
  				InGenerarInterfazDetalleOperacionesPolizas entrada= new InGenerarInterfazDetalleOperacionesPolizas();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "2018-03-09";
  				entrada.p_FechaHasta	= "2018-03-09";
  				entrada.p_IdCliente		=	0L;
  				entrada.p_EsTodosCliente	=	"S";
  				entrada.p_IdInstrumento	=	0L;
  				entrada.p_EsTodosInstrumentos = "S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazDetalleOperacionesPolizas demo= new GenerarInterfazDetalleOperacionesPolizas();
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
