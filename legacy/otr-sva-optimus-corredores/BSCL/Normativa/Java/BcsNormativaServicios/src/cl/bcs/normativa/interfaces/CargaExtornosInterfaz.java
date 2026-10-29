package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class CargaExtornosInterfaz implements JavaCall{
	
	private final Log log =new Log("CargaExtornosInterfaz");
	
	private final int LINE_SIZE = 145;
	private final String SQL_INSERT_EXTORNOS = loadResourceToString("cl/bcs/normativa/interfaces/sql/CargaExtornosInterfaz_InsertExtornos.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	public static class CamposExtornos{
		public String 	FechaOperacion;
		public Integer	ModalidadOperacion;
		public Integer	NumeroOperacion;
		public Integer	CodigoAgente;
		public Integer	TipoDetalle;
		public Integer	CodigoComision;
		public Integer	CantidadCavaEntero;
		public Integer	CantidadCavaDecimal;
		public Integer 	CantidadFisiEntero;
		public Integer	CantidadFisiDecimal;
		public Integer	GiroCheq;
		public Integer	FlagTrabajador;
		public Integer	ComisionContadoEntero;
		public Integer	ComisionContadoDecimal;
		public Integer	ComisionPlazoEntero;
		public Integer	ComisionPlazoDecimal;
		public String	FlagPoliza;
		public String	FlagContado;
		public String	FlagPlazo;
		public Integer 	CodigoCustodioValores;
		public Integer	CodigoCustodioFondos;
		public Integer	InteresContadoEntero;
		public Integer	InteresContadoDecimal;
		public Integer	InteresPlazoEntero;
		public Integer	InteresPlazoDecimal;
		public String	LiqDtc;
		public Integer	FlagDayTrade;
		public String	FechaVencimientoDocumento;
		public Integer	IndicadorCartaFianza;
		public Integer	MontoCartaFianzaEntero;
		public Integer	MontoCartaFianzaDecimal;
		public Integer	CodigoBanco;
	}
	
	public static class Extorno{
		public String		FechaOperacion;
		public Integer		ModalidadOperacion;
		public Integer		NumeroOperacion;
		public Integer		CodigoAgente;
		public Integer		TipoDetalle;
		public Integer		CodigoComision;
		public BigDecimal	CantidadCava;
		public BigDecimal	CantidadFisi;
		public Integer		GiroCheq;
		public Integer		FlagTrabajador;
		public BigDecimal	ComisionContado;
		public BigDecimal	ComisionPlazo;
		public String		FlagPoliza;
		public String		FlagContado;
		public String		FlagPlazo;
		public Integer 		CodigoCustodioValores;
		public Integer		CodigoCustodioFondos;
		public BigDecimal	InteresContado;
		public BigDecimal	InteresPlazo;
		public String		LiqDtc;
		public Integer		FlagDayTrade;
		public String		FechaVencimientoDocumento;
		public Integer		IndicadorCartaFianza;
		public BigDecimal	MontoCartaFianza;
		public Integer		CodigoBanco;
		
		public Extorno(String FechaOperacion,Integer ModalidadOperacion,Integer NumeroOperacion,Integer CodigoAgente,Integer TipoDetalle,Integer CodigoComision,
						BigDecimal CantidadCava,BigDecimal CantidadFisi,Integer GiroCheq,Integer FlagTrabajador,BigDecimal ComisionContado,BigDecimal ComisionPlazo,
						String FlagPoliza,String FlagContado,String FlagPlazo,Integer CodigoCustodioValores,Integer CodigoCustodioFondos,BigDecimal InteresContado,
						BigDecimal InteresPlazo,String LiqDtc,Integer FlagDayTrade,String FechaVencimientoDocumento,Integer IndicadorCartaFianza,BigDecimal MontoCartaFianza,
						Integer CodigoBanco){
			this.FechaOperacion = FechaOperacion;
			this.ModalidadOperacion = ModalidadOperacion;
			this.NumeroOperacion = NumeroOperacion;
			this.CodigoAgente = CodigoAgente;
			this.TipoDetalle = TipoDetalle;
			this.CodigoComision = CodigoComision;
			this.CantidadCava = CantidadCava;
			this.CantidadFisi = CantidadFisi;
			this.GiroCheq = GiroCheq;
			this.FlagTrabajador = FlagTrabajador;
			this.ComisionContado = ComisionContado;
			this.ComisionPlazo = ComisionPlazo;
			this.FlagPoliza = FlagPoliza;
			this.FlagContado = FlagContado;
			this.FlagPlazo = FlagPlazo;
			this.CodigoCustodioValores = CodigoCustodioValores;
			this.CodigoCustodioFondos = CodigoCustodioFondos;
			this.InteresContado = InteresContado;
			this.InteresPlazo = InteresPlazo;
			this.LiqDtc = LiqDtc;
			this.FlagDayTrade = FlagDayTrade;
			this.FechaVencimientoDocumento = FechaVencimientoDocumento;
			this.IndicadorCartaFianza = IndicadorCartaFianza;
			this.MontoCartaFianza = MontoCartaFianza;
			this.CodigoBanco = CodigoBanco;
		}
	}
	
	public static class LogEjecucion{
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}
	
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaExtornosInterfaz{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaExtornosInterfaz{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
		public String	p_out_Comentario;
	}
	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaExtornosInterfaz in=gson.fromJson(jin.toString() ,InCargaExtornosInterfaz.class);
		OutCargaExtornosInterfaz out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutCargaExtornosInterfaz logica(Connection con, InCargaExtornosInterfaz entrada) throws Exception {
		OutCargaExtornosInterfaz out = new OutCargaExtornosInterfaz();
		out.p_out_ErroresContador = 1L;
		
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivoLog="nor/EXT_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo));
			
			String[] registros = archivoDecoded.split(System.getProperty("line.separator"));
			
			List<Extorno> extornos = new ArrayList<Extorno>();
			
			for(String registro : registros){
				numeroRegistros++;
				if(registro.length() != LINE_SIZE){
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error de tamaño del registro, se esperaba:" + LINE_SIZE + ",se obtuvo:" + registro.length() + ",registro:" + registro);
					bwArchivoLog.newLine();
				}else{
					try{
						
						CamposExtornos campo = new CamposExtornos();
						campo.FechaOperacion = registro.substring(0, 8);
						campo.ModalidadOperacion = Integer.parseInt(registro.substring(8,10));
						campo.NumeroOperacion = Integer.parseInt(registro.substring(10,16));
						campo.CodigoAgente = Integer.parseInt(registro.substring(16,20));
						campo.TipoDetalle = Integer.parseInt(registro.substring(20,21));
						campo.CodigoComision = Integer.parseInt(registro.substring(21,29));
						campo.CantidadCavaEntero = Integer.parseInt(registro.substring(29,43));
						campo.CantidadCavaDecimal = Integer.parseInt(registro.substring(43,45));
						campo.CantidadFisiEntero = Integer.parseInt(registro.substring(45,59));
						campo.CantidadFisiDecimal = Integer.parseInt(registro.substring(59,61));
						campo.GiroCheq = Integer.parseInt(registro.substring(61,63));
						campo.FlagTrabajador = Integer.parseInt(registro.substring(63,65));
						campo.ComisionContadoEntero = Integer.parseInt(registro.substring(65,68));
						campo.ComisionContadoDecimal = Integer.parseInt(registro.substring(68,74));
						campo.ComisionPlazoEntero = Integer.parseInt(registro.substring(74,77));
						campo.ComisionPlazoDecimal = Integer.parseInt(registro.substring(77,83));
						campo.FlagPoliza = registro.substring(83,84);
						campo.FlagContado = registro.substring(84,85);
						campo.FlagPlazo = registro.substring(85,86);
						campo.CodigoCustodioValores = Integer.parseInt(registro.substring(86,90));
						campo.CodigoCustodioFondos = Integer.parseInt(registro.substring(90,94));
						campo.InteresContadoEntero = Integer.parseInt(registro.substring(94,102));
						campo.InteresContadoDecimal = Integer.parseInt(registro.substring(102,104));
						campo.InteresPlazoEntero = Integer.parseInt(registro.substring(104,112));
						campo.InteresPlazoDecimal = Integer.parseInt(registro.substring(112,114));
						campo.LiqDtc = registro.substring(114,115);
						campo.FlagDayTrade = Integer.parseInt(registro.substring(115,116));
						campo.FechaVencimientoDocumento = registro.substring(116,124);
						campo.IndicadorCartaFianza = Integer.parseInt(registro.substring(124,125));
						campo.MontoCartaFianzaEntero = Integer.parseInt(registro.substring(125,139));
						campo.MontoCartaFianzaDecimal = Integer.parseInt(registro.substring(139,141));
						campo.CodigoBanco = Integer.parseInt(registro.substring(141,145));
						extornos.add(new Extorno(campo.FechaOperacion,campo.ModalidadOperacion,campo.NumeroOperacion,campo.CodigoAgente,campo.TipoDetalle,campo.CodigoComision,
												new BigDecimal(campo.CantidadCavaEntero + "." + campo.CantidadCavaDecimal), new BigDecimal(campo.CantidadFisiEntero + "." + campo.CantidadFisiDecimal),
												campo.GiroCheq,campo.FlagTrabajador,new BigDecimal(campo.ComisionContadoEntero + "." + campo.ComisionContadoDecimal),new BigDecimal(campo.ComisionPlazoEntero + "." + campo.ComisionPlazoDecimal),
												campo.FlagPoliza,campo.FlagContado,campo.FlagPlazo,campo.CodigoCustodioValores,campo.CodigoCustodioFondos,new BigDecimal(campo.InteresContadoEntero + "." + campo.InteresContadoDecimal),
												new BigDecimal(campo.InteresPlazoEntero + "." + campo.InteresPlazoDecimal),campo.LiqDtc,campo.FlagDayTrade,campo.FechaVencimientoDocumento,campo.IndicadorCartaFianza,new BigDecimal(campo.MontoCartaFianzaEntero + "." + campo.MontoCartaFianzaDecimal),
												campo.CodigoBanco));
						numeroRegistrosCorrecto++;
					}catch(Exception e){
						numeroRegistrosErroneos++;
						bwArchivoLog.write("Error al parsear el registro:" + registro);
						bwArchivoLog.newLine();
					}
				}
			}
			
			LogEjecucion logEjecucion = new LogEjecucion();
			logEjecucion.IdInterfaz = 12L;
			logEjecucion.NumeroRegistros = new Long(numeroRegistros);
			logEjecucion.NumeroRegistrosCorrectos = new Long(numeroRegistrosCorrecto);
			logEjecucion.NumeroRegistrosIncorrectos = new Long(numeroRegistrosErroneos);
			List<LogEjecucion> logs = new ArrayList<LogEjecucion>();
			logs.add(logEjecucion);
			sql.batch(con, SQL_INSERT_EXTORNOS, extornos, Extorno.class);
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
			out.p_out_ErroresContador = 0L;
			out.p_out_Comentario = totalRegistros + " / " + registrosCorrectos + " / " + registrosErroneos;

			
		}catch(Exception e){
			log.write(e);
		}finally{
			log.writeAsJson("SALIDA",out);
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
  				
  				InCargaExtornosInterfaz entrada= new InCargaExtornosInterfaz();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "NOR";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "MjAxNjA0MjEwMTAwMDAyMTAwMDEyMDA5MzIzNzYwMDAwMDAwMDAwMDA1MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA2MDAwMDAwMDAwMDAwMDBOU04wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwTjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMA0KMjAxNjA0MjIwMTAwMDAyMTAwMDEyMDA5MzIzNzYwMDAwMDAwMDAwMDA1MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA2MDAwMDAwMDAwMDAwMDBOU04wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwTjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMA==";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaExtornosInterfaz demo= new CargaExtornosInterfaz();
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
