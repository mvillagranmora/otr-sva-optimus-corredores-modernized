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

public class GenerarInterfazPolizas implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazPolizas");
	
	private final int LARGO_ARCHIVO = 231;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "POL";
	private final String SQL_SELECT_INTERFAZ_POLIZAS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazPolizas_SelectInterfazPolizas.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazPolizas{
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
	
	public static class OutGenerarInterfazPolizas{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaPolizas{
		public String 		TipoRegistro;
		public String 		CodigoAI;
		public Long			NumeroPoliza;
		public String		FechaOperacionPoliza;
		public String 		FechaLiquidacion;
		public String		ModalidadOperacion;
		public Integer 		FlagContadorPlazo;
		public String 		Moneda;
		public String 		Mercado;
		public BigDecimal 	Cantidad;
		public BigDecimal	Precio;
		public BigDecimal 	MontoBruto;
		public BigDecimal 	ComisionAgente;
		public BigDecimal 	ComisionBolsa;
		public BigDecimal 	FondoGarantia;
		public BigDecimal 	ComisionCavali;
		public BigDecimal 	FondoLiquidacion;
		public BigDecimal 	Smv;
		public BigDecimal 	Igv;
		public BigDecimal	Intereses;
		public Integer		EstadoPoliza;
		public String		FechaVencimiento;
		public BigDecimal	BrokerExtranjero;
		public BigDecimal	MontoNeto;
		public String		TipoCodigoCliente;
		public String 		CodigoCliente;
		public String		IdentificadorCliente;
		public String		SignoMontoNeto;
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
		InGenerarInterfazPolizas in=gson.fromJson(jin.toString() ,InGenerarInterfazPolizas.class);
		OutGenerarInterfazPolizas out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazPolizas logica(Connection con, InGenerarInterfazPolizas entrada) throws Exception {
		OutGenerarInterfazPolizas out = new OutGenerarInterfazPolizas();
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
		out.p_out_NombreArchivo="nor/POL_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/POL_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaPolizas = new HashMap<String,Object>();
			paramsConsultaPolizas.put("FechaDesde", entrada.p_FechaDesde);
			paramsConsultaPolizas.put("EsTodosCliente", entrada.p_EsTodosCliente);
			paramsConsultaPolizas.put("IdCliente", entrada.p_IdCliente);
			//paramsConsultaPolizas.put("EsTodosInstrumentos",entrada.p_EsTodosInstrumentos);
			//paramsConsultaPolizas.put("IdInstrumento",entrada.p_IdInstrumento);
			paramsConsultaPolizas.put("IdEmpresa", entrada.p_IdEmpresa);
			List<ConsultaPolizas> polizas = sql.query(con,SQL_SELECT_INTERFAZ_POLIZAS,paramsConsultaPolizas,ConsultaPolizas.class);
			
			String detalle = "";
			
			for(ConsultaPolizas poliza : polizas){
				String ret = "";
				ret += String.format("%-1s", 	(poliza.TipoRegistro.length() > 1)? poliza.TipoRegistro.substring(0, 1): poliza.TipoRegistro);
				ret += String.format("%-10s", 	(poliza.CodigoAI.length() > 10)? poliza.CodigoAI.substring(0, 10): poliza.CodigoAI);
				ret += String.format("%015d", 	poliza.NumeroPoliza.intValue());
				ret += String.format("%-8s", 	(poliza.FechaOperacionPoliza.length() > 8)? poliza.FechaOperacionPoliza.substring(0, 8): poliza.FechaOperacionPoliza);
				ret += String.format("%-8s", 	(poliza.FechaLiquidacion.length() > 8)? poliza.FechaLiquidacion.substring(0, 8): poliza.FechaLiquidacion);
				ret += String.format("%-2s", 	(poliza.ModalidadOperacion.length() > 2)? poliza.ModalidadOperacion.substring(0, 2): poliza.ModalidadOperacion);
				ret += String.format("%01d", 	poliza.FlagContadorPlazo.intValue());
				ret += String.format("%-2s", 	(poliza.Moneda.length() > 2)? poliza.Moneda.substring(0, 2): poliza.Moneda);
				ret += String.format("%-1s", 	(poliza.Mercado.length() > 1)? poliza.Mercado.substring(0, 1): poliza.Mercado);
				ret += String.format("%012d", 	poliza.Cantidad.intValue());
				ret += String.format("%04d", 	poliza.Cantidad.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.Cantidad.toPlainString().split(".")[1]) : 0);
				ret += String.format("%08d", 	poliza.Precio.intValue());
			    ret += String.format("%08d", 	poliza.Precio.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.Precio.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%012d", 	poliza.MontoBruto.intValue());
			    ret += String.format("%04d", 	poliza.MontoBruto.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.MontoBruto.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.ComisionAgente.intValue());
			    ret += String.format("%02d", 	poliza.ComisionAgente.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.ComisionAgente.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.ComisionBolsa.intValue());
			    ret += String.format("%02d", 	poliza.ComisionBolsa.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.ComisionBolsa.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.FondoGarantia.intValue());
			    ret += String.format("%02d", 	poliza.FondoGarantia.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.FondoGarantia.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.ComisionCavali.intValue());
			    ret += String.format("%02d", 	poliza.ComisionCavali.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.ComisionCavali.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.FondoLiquidacion.intValue());
			    ret += String.format("%02d", 	poliza.FondoLiquidacion.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.FondoLiquidacion.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.Smv.intValue());
			    ret += String.format("%02d", 	poliza.Smv.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.Smv.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.Igv.intValue());
			    ret += String.format("%02d", 	poliza.Igv.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.Igv.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%09d", 	poliza.Intereses.intValue());
			    ret += String.format("%02d", 	poliza.Intereses.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.Intereses.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%01d", 	poliza.EstadoPoliza.intValue());
			    ret += String.format("%-8s", 	(poliza.FechaVencimiento.length() > 8)? poliza.FechaVencimiento.substring(0, 8): poliza.FechaVencimiento);
			    ret += String.format("%09d", 	poliza.BrokerExtranjero.intValue());
			    ret += String.format("%02d", 	poliza.BrokerExtranjero.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.BrokerExtranjero.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%012d", 	poliza.MontoNeto.intValue());
			    ret += String.format("%04d", 	poliza.MontoNeto.toPlainString().split(".").length == 2 ? Integer.valueOf(poliza.MontoNeto.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-1s", 	(poliza.TipoCodigoCliente.length() > 1)? poliza.TipoCodigoCliente.substring(0, 1): poliza.TipoCodigoCliente);
			    ret += String.format("%-8s", 	(poliza.CodigoCliente.length() > 8)? poliza.CodigoCliente.substring(0, 8): poliza.CodigoCliente);
			    ret += String.format("%-1s", 	(poliza.IdentificadorCliente.length() > 1)? poliza.IdentificadorCliente.substring(0, 1): poliza.IdentificadorCliente);
			    ret += String.format("%-1s", 	(poliza.SignoMontoNeto.length() > 1)? poliza.SignoMontoNeto.substring(0, 1): poliza.SignoMontoNeto);
			    
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
			logEjecucion.IdInterfaz = 6L;
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
  				
  				InGenerarInterfazPolizas entrada= new InGenerarInterfazPolizas();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "2017-05-15";
  				entrada.p_FechaHasta	= "2017-06-30";
  				entrada.p_IdCliente		=	0L;
  				entrada.p_EsTodosCliente	=	"S";
  				entrada.p_IdInstrumento	=	0L;
  				entrada.p_EsTodosInstrumentos = "S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazPolizas demo= new GenerarInterfazPolizas();
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
