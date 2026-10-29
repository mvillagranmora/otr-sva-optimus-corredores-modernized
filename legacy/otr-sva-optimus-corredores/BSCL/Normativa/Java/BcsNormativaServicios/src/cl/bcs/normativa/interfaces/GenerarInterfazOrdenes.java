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

public class GenerarInterfazOrdenes implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazOrdenes");
	private final int LARGO_ARCHIVO = 412;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "ORD";
	private final String SQL_SELECT_INTERFAZ_ORDENES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazOrdenes_SelectInterfazOrdenes.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazOrdenes{
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
	
	public static class OutGenerarInterfazOrdenes{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultaOrdenes{
		public String		TipoRegistro;
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
		public String		FlagPrecio;
		public BigDecimal	Precio;
		public BigDecimal	MontoOrdenado;
		public Integer		VigenciaOrden;
		public BigDecimal 	CantidadValoresDisponiblesOrdenesVenta;
		public BigDecimal	DineroDisponibleOrdenesCompra;
		public String 		MonedaOrdenado;
		public String		FlagEstadoOrden;
		public Integer		NumeroOrdenModificada;
		public String		PaisMercadoIntegrado;
		public String 		MedioInicialRecepcionOrden;
		public String 		SustentoCambio;
		public BigDecimal	PrecioStop;
		public String		UbigeoOficina;
		public String		PrimerRepresentante;
		public String		SegundoRepresentante;
		public String		TercerRepresentante;
		public Integer		CorrelativoCorrecciones;
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
		InGenerarInterfazOrdenes in=gson.fromJson(jin.toString() ,InGenerarInterfazOrdenes.class);
		OutGenerarInterfazOrdenes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazOrdenes logica(Connection con, InGenerarInterfazOrdenes entrada) throws Exception {
		OutGenerarInterfazOrdenes out = new OutGenerarInterfazOrdenes();
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
		out.p_out_NombreArchivo="nor/ORD_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/ORD_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivo = new File(out.p_out_RutaArchivo);
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			HashMap<String,Object> paramsConsultaOrdenes = new HashMap<String,Object>();
			paramsConsultaOrdenes.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsConsultaOrdenes.put("FechaDesde", entrada.p_FechaDesde);
			paramsConsultaOrdenes.put("EsTodosCliente", entrada.p_EsTodosCliente);
			paramsConsultaOrdenes.put("IdCliente", entrada.p_IdCliente);
			paramsConsultaOrdenes.put("EsTodosInstrumentos", entrada.p_EsTodosInstrumentos);
			paramsConsultaOrdenes.put("IdInstrumento", entrada.p_IdInstrumento);
			List<ConsultaOrdenes> ordenes = sql.query(con,SQL_SELECT_INTERFAZ_ORDENES,paramsConsultaOrdenes,ConsultaOrdenes.class);
			
			String detalle = "";
			
			for(ConsultaOrdenes orden : ordenes){
				String ret = "";
				ret += String.format("%-1s", 	(orden.TipoRegistro.length() > 1)? orden.TipoRegistro.substring(0, 1): orden.TipoRegistro);
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
				ret += String.format("%04d", 	orden.CantidadOrden.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.CantidadOrden.toPlainString().split(".")[1]) : 0);
				ret += String.format("%-1s", 	(orden.FlagPrecio.length() > 1)? orden.FlagPrecio.substring(0, 1): orden.FlagPrecio);
				ret += String.format("%08d", 	orden.Precio.intValue());
			    ret += String.format("%08d", 	orden.Precio.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.Precio.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%012d", 	orden.MontoOrdenado.intValue());
			    ret += String.format("%04d", 	orden.MontoOrdenado.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.MontoOrdenado.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%02d",  	orden.VigenciaOrden.intValue());
			    ret += String.format("%012d", 	orden.CantidadValoresDisponiblesOrdenesVenta.intValue());
			    ret += String.format("%04d", 	orden.CantidadValoresDisponiblesOrdenesVenta.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.CantidadValoresDisponiblesOrdenesVenta.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%012d", 	orden.DineroDisponibleOrdenesCompra.intValue());
			    ret += String.format("%04d", 	orden.DineroDisponibleOrdenesCompra.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.DineroDisponibleOrdenesCompra.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-2s", 	(orden.MonedaOrdenado.length() > 2)? orden.MonedaOrdenado.substring(0, 2): orden.MonedaOrdenado);
			    ret += String.format("%-2s", 	(orden.FlagEstadoOrden.length() > 2)? orden.FlagEstadoOrden.substring(0, 2): orden.FlagEstadoOrden);
			    ret += String.format("%06d", 	orden.NumeroOrdenModificada.intValue());
			    ret += String.format("%-3s", 	(orden.PaisMercadoIntegrado.length() > 3)? orden.PaisMercadoIntegrado.substring(0, 3): orden.PaisMercadoIntegrado);
			    ret += String.format("%-1s", 	(orden.MedioInicialRecepcionOrden.length() > 1)? orden.MedioInicialRecepcionOrden.substring(0, 1): orden.MedioInicialRecepcionOrden);
			    ret += String.format("%-200s", 	(orden.SustentoCambio.length() > 200)? orden.SustentoCambio.substring(0, 200): orden.SustentoCambio);
			    ret += String.format("%08d", 	orden.PrecioStop.intValue());
			    ret += String.format("%08d", 	orden.PrecioStop.toPlainString().split(".").length == 2 ? Integer.valueOf(orden.PrecioStop.toPlainString().split(".")[1]) : 0);
			    ret += String.format("%-6s", 	(orden.UbigeoOficina.length() > 2)? orden.UbigeoOficina.substring(0, 2): orden.UbigeoOficina);
			    ret += String.format("%9s", 	(orden.PrimerRepresentante.length() > 9)? orden.PrimerRepresentante.substring(0, 9): orden.PrimerRepresentante);
			    ret += String.format("%9s", 	(orden.SegundoRepresentante.length() > 9)? orden.SegundoRepresentante.substring(0, 9): orden.SegundoRepresentante);
			    ret += String.format("%9s", 	(orden.TercerRepresentante.length() > 9)? orden.TercerRepresentante.substring(0, 9): orden.TercerRepresentante);
			    ret += String.format("%01d", 	orden.CorrelativoCorrecciones.intValue());
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
			logEjecucion.IdInterfaz = 5L;
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
  				
  				InGenerarInterfazOrdenes entrada= new InGenerarInterfazOrdenes();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "2017-01-30";
  				entrada.p_FechaHasta	= "2017-06-30";
  				entrada.p_IdCliente		=	0L;
  				entrada.p_EsTodosCliente	=	"S";
  				entrada.p_IdInstrumento	=	0L;
  				entrada.p_EsTodosInstrumentos = "S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazOrdenes demo= new GenerarInterfazOrdenes();
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
