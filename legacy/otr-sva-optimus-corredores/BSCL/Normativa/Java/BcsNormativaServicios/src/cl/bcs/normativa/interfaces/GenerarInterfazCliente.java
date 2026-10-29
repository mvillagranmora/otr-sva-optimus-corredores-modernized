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

public class GenerarInterfazCliente implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazCliente");
	
	private final int LARGO_ARCHIVO = 510;
	private final String CodigoAI = "S80080";
	private final String IdArchivo = "CLI";
	private final String SQL_SELECT_INTERFAZ_CLIENTE = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazCliente_SelectInterfazCliente.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGenerarInterfazCliente{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaDesde;
		public String	p_FechaHasta;
		public Long		p_IdCliente;
		public String	p_EsTodosClientes;
		public String	p_Ruta;
	}
	
	public static class OutGenerarInterfazCliente{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo; 
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	public static class ConsultarCliente{
		public String TipoRegistro;
		public String CodigoAI;
		public String IdentificadorUnicoPersona;
		public String TipoCodigoCliente;
		public String CodigoCliente;
		public String PrimeraRelacion;
		public String SegundaRelacion;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String Nombres;
		public String RazonSocial;
		public String FechaInscripcionCliente;
		public String Direccion;
		public String Ubigeo;
		public String Pais;
		public String Telefono;
		public String FlagIndicadorRepresentanteLegal;
		public String TipoPrimeraRelacion;
		public String NacionalidadCliente;
		public String DireccionElectronica;
		public String RepresentanteLegalIdentificadorUnicoPersona;
		public String RepresentanteLegalRazonSocial;
		public String RepresentanteLegalApellidoPaterno;
		public String RepresentanteLegalApellidoMaterno;
		public String RepresentanteLegalNombres;
		public String Ruc;
		public String FechaNacimiento;
		public String ActividadEconomica;
		public String FlagClientePersonaExpuestaPoliticamente;
		public String NumeroCorrelativoBeneficiario;
		public String FechaFinal;
		public String TipoCliente;
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
		InGenerarInterfazCliente in=gson.fromJson(jin.toString() ,InGenerarInterfazCliente.class);
		OutGenerarInterfazCliente out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	

	/**********************
	 * lógica del programa
	 **********************/
	
	public  OutGenerarInterfazCliente logica(Connection con, InGenerarInterfazCliente entrada) throws Exception {
		OutGenerarInterfazCliente out = new OutGenerarInterfazCliente();
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
		out.p_out_NombreArchivo="nor/CLI_"+fecha+".001";
		out.p_out_RutaArchivo=entrada.p_Ruta+"/"+out.p_out_NombreArchivo;
		out.p_out_NombreArchivoLog="nor/CLI_"+fecha+".log";
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
			paramsConsultaCliente.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsConsultaCliente.put("FechaDesde", entrada.p_FechaDesde);
			paramsConsultaCliente.put("EsTodosClientes", entrada.p_EsTodosClientes);
			paramsConsultaCliente.put("IdCliente", entrada.p_IdCliente);
			List<ConsultarCliente> clientes = sql.query(con,SQL_SELECT_INTERFAZ_CLIENTE,paramsConsultaCliente,ConsultarCliente.class);
			
			String detalle = "";
			
			for(ConsultarCliente cliente : clientes){
				String ret = "";
				
				ret += String.format("%-1s", 	(cliente.TipoRegistro.length() > 1)? cliente.TipoRegistro.substring(0, 1): cliente.TipoRegistro);
				ret += String.format("%-10s", 	(cliente.CodigoAI.length() > 10)? cliente.CodigoAI.substring(0, 10): cliente.CodigoAI);
				ret += String.format("%-14s", 	(cliente.IdentificadorUnicoPersona.length() > 14)? cliente.IdentificadorUnicoPersona.substring(0, 14): cliente.IdentificadorUnicoPersona);
				ret += String.format("%-1s", 	(cliente.TipoCodigoCliente.length() > 1)? cliente.TipoCodigoCliente.substring(0, 1): cliente.TipoCodigoCliente);
				ret += String.format("%-8s", 	(cliente.CodigoCliente.length() > 8)? cliente.CodigoCliente.substring(0, 8): cliente.CodigoCliente);
				ret += String.format("%-1s", 	(cliente.PrimeraRelacion.length() > 1)? cliente.PrimeraRelacion.substring(0, 1): cliente.PrimeraRelacion);
				ret += String.format("%-1s", 	(cliente.SegundaRelacion.length() > 1)? cliente.SegundaRelacion.substring(0, 1): cliente.SegundaRelacion);
				ret += String.format("%-30s", 	(cliente.ApellidoPaterno.length() > 30)? cliente.ApellidoPaterno.substring(0, 30): cliente.ApellidoPaterno);
				ret += String.format("%-30s", 	(cliente.ApellidoMaterno.length() > 30)? cliente.ApellidoMaterno.substring(0, 30): cliente.ApellidoMaterno);
				ret += String.format("%-30s", 	(cliente.Nombres.length() > 30)? cliente.Nombres.substring(0, 30): cliente.Nombres);
				ret += String.format("%-50s", 	(cliente.RazonSocial.length() > 50)? cliente.RazonSocial.substring(0, 30): cliente.RazonSocial);
				ret += String.format("%-8s", 	(cliente.FechaInscripcionCliente.length() > 8)? cliente.FechaInscripcionCliente.substring(0, 8): cliente.FechaInscripcionCliente);
				ret += String.format("%-60s", 	(cliente.Direccion.length() > 60)? cliente.Direccion.substring(0, 60): cliente.Direccion);
				ret += String.format("%-6s", 	(cliente.Ubigeo.length() > 6)? cliente.Ubigeo.substring(0, 6): cliente.Ubigeo);
				ret += String.format("%-3s", 	(cliente.Pais.length() > 3)? cliente.Pais.substring(0, 3): cliente.Pais);
				ret += String.format("%-10s", 	(cliente.Telefono.length() > 10)? cliente.Telefono.substring(0, 10): cliente.Telefono);
				ret += String.format("%-2s", 	(cliente.FlagIndicadorRepresentanteLegal.length() > 2)? cliente.FlagIndicadorRepresentanteLegal.substring(0, 2): cliente.FlagIndicadorRepresentanteLegal);
				ret += String.format("%-2s", 	(cliente.TipoPrimeraRelacion.length() > 2)? cliente.TipoPrimeraRelacion.substring(0, 2): cliente.TipoPrimeraRelacion);
				ret += String.format("%-3s", 	(cliente.NacionalidadCliente.length() > 3)? cliente.NacionalidadCliente.substring(0, 3): cliente.NacionalidadCliente);
				ret += String.format("%-50s", 	(cliente.DireccionElectronica.length() > 50)? cliente.DireccionElectronica.substring(0, 50): cliente.DireccionElectronica);
				ret += String.format("%-14s", 	(cliente.RepresentanteLegalIdentificadorUnicoPersona.length() > 14)? cliente.RepresentanteLegalIdentificadorUnicoPersona.substring(0, 14): cliente.RepresentanteLegalIdentificadorUnicoPersona);
				ret += String.format("%-50s", 	(cliente.RepresentanteLegalRazonSocial.length() > 50)? cliente.RepresentanteLegalRazonSocial.substring(0, 50): cliente.RepresentanteLegalRazonSocial);
				ret += String.format("%-30s", 	(cliente.RepresentanteLegalApellidoPaterno.length() > 30)? cliente.RepresentanteLegalApellidoPaterno.substring(0, 30): cliente.RepresentanteLegalApellidoPaterno);
				ret += String.format("%-30s", 	(cliente.RepresentanteLegalApellidoMaterno.length() > 30)? cliente.RepresentanteLegalApellidoMaterno.substring(0, 30): cliente.RepresentanteLegalApellidoMaterno);
				ret += String.format("%-30s", 	(cliente.RepresentanteLegalNombres.length() > 30)? cliente.RepresentanteLegalNombres.substring(0, 30): cliente.RepresentanteLegalNombres);
				ret += String.format("%-11s", 	(cliente.Ruc.length() > 11)? cliente.Ruc.substring(0, 11): cliente.Ruc);
				ret += String.format("%-8s", 	(cliente.FechaNacimiento.length() > 8)? cliente.FechaNacimiento.substring(0, 8): cliente.FechaNacimiento);
				ret += String.format("%-4s", 	(cliente.ActividadEconomica.length() > 4)? cliente.ActividadEconomica.substring(0, 4): cliente.ActividadEconomica);
				ret += String.format("%-1s", 	(cliente.FlagClientePersonaExpuestaPoliticamente.length() > 1)? cliente.FlagClientePersonaExpuestaPoliticamente.substring(0, 1): cliente.FlagClientePersonaExpuestaPoliticamente);
				ret += String.format("%-2s", 	(cliente.NumeroCorrelativoBeneficiario.length() > 2)? cliente.NumeroCorrelativoBeneficiario.substring(0, 2): cliente.NumeroCorrelativoBeneficiario);
				ret += String.format("%-8s", 	(cliente.FechaFinal.length() > 8)? cliente.FechaFinal.substring(0, 8): cliente.FechaFinal);
				ret += String.format("%-2s", 	(cliente.TipoCliente.length() > 2)? cliente.TipoCliente.substring(0, 2): cliente.TipoCliente);
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
  				
  				InGenerarInterfazCliente entrada= new InGenerarInterfazCliente();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "FAC";
  				entrada.p_FechaDesde	= "2017-01-30";
  				entrada.p_FechaHasta	= "2017-06-30";
  				entrada.p_IdCliente	=	0L;
  				entrada.p_EsTodosClientes = "S";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				GenerarInterfazCliente demo= new GenerarInterfazCliente();
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
