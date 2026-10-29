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

public class CargaPagoOperacionesInterfaz implements JavaCall{
	
	private final Log log =new Log("CargaPagoOperacionesInterfaz");
	
	private final int	LINE_SIZE = 258;
	private final String SQL_INSERT_PAGO_OPERACIONES = loadResourceToString("cl/bcs/normativa/interfaces/sql/CargaPagoOperacionesInterfaz_InsertPagoOperaciones.sql");
	private final String SQL_INSERT_LOG_EJECUCION = loadResourceToString("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
	
	public static class CamposPagoOperacionesInterfaz{
		public String 	FechaOperacion;
		public Integer	Modalidad;
		public Integer	NumeroOperacion;
		public Integer	TipoMoneda;
		public Integer	TipoOperacion;
		public Integer TipoInstrumento;
		public Integer	EstadoOperacion;
		public String	CodigoValor;
		public String	Nemotecnico;
		public Integer	PrecioEntero;
		public Integer	PrecioDecimal;
		public Integer	CantidadEntero;
		public Integer CantidadDecimal;
		public Integer	AgenteComprador;
		public Integer	AgenteVendedor;
		public Integer MontoEfectivoEntero;
		public Integer	MontoEfectivoDecimal;
		public Integer	ValorNominalEntero;
		public Integer	ValorNominalDecimal;
		public Integer	TasaOperacionEntero;
		public Integer	TasaOperacionDecimal;
		public String	FechaLiquidacionContado;
		public String	FechaLiquidacionPlazo;
		public Integer	CantidadDiasOperacion;
		public Integer	TipoAval;
		public Integer	CodigoAval;
		public Integer	TipoLiquidacion;
		public Integer	IndicadorHabilitacionFondos;
		public Integer	TipoCambioEntero;
		public Integer TipoCambioDecimal;
		public String	DescripcionValorNegociado;
		public String	HoraOperacion;
		public Integer	InteresesCorridosContadoPlazoEntero;
		public Integer	InteresesCorridosContadoPlazoDecimal;
		public Integer	NumeroSubasta;
		public Integer	AgenteCompradorEspecialista;
		public Integer	AgenteVendedorEspecialista;
		public Integer	FlagOfertaPublica;
	}
	
	public static class PagoOperacion{
		public String		FechaOperacion;
		public Integer		Modalidad;
		public Integer		NumeroOperacion;
		public Integer		TipoMoneda;
		public Integer		TipoOperacion;
		public Integer		TipoInstrumento;
		public Integer		EstadoOperacion;
		public String		CodigoValor;
		public String		Nemotecnico;
		public BigDecimal	Precio;
		public BigDecimal	Cantidad;
		public Integer		AgenteComprador;
		public Integer		AgenteVendedor;
		public BigDecimal	MontoEfectivo;
		public BigDecimal	ValorNominal;
		public BigDecimal	TasaOperacion;
		public String		FechaLiquidacionContado;
		public String		FechaLiquidacionPlazo;
		public Integer		CantidadDiasOperacion;
		public Integer		TipoAval;
		public Integer		CodigoAval;
		public Integer		TipoLiquidacion;
		public Integer		IndicadorHabilitacionFondos;
		public BigDecimal	TipoCambio;
		public String		DescripcionValorNegociado;
		public String		HoraOperacion;
		public BigDecimal	InteresesCorridosContadoPlazo;
		public Integer		NumeroSubasta;
		public Integer		AgenteCompradorEspecialista;
		public Integer		AgenteVendedorEspecialista;
		public Integer		FlagOfertaPublica;
		
		public PagoOperacion(String FechaOperacion,Integer Modalidad,Integer NumeroOperacion,Integer TipoMoneda,Integer TipoOperacion,Integer TipoInstrumento,
				Integer EstadoOperacion,String CodigoValor,String Nemotecnico,BigDecimal Precio,BigDecimal Cantidad,Integer AgenteComprador,Integer AgenteVendedor,
				BigDecimal MontoEfectivo,BigDecimal ValorNominal,BigDecimal TasaOperacion,String FechaLiquidacionContado,String FechaLiquidacionPlazo,Integer CantidadDiasOperacion,
				Integer TipoAval,Integer CodigoAval,Integer TipoLiquidacion,Integer IndicadorHabilitacionFondos,BigDecimal TipoCambio,String DescripcionValorNegociado,
				String HoraOperacion,BigDecimal InteresesCorridosContadoPlazo,Integer NumeroSubasta,Integer AgenteCompradorEspecialista,Integer AgenteVendedorEspecialista,
				Integer FlagOfertaPublica){
			
			this.FechaOperacion = FechaOperacion;
			this.Modalidad = Modalidad;
			this.NumeroOperacion = NumeroOperacion;
			this.TipoMoneda = TipoMoneda;
			this.TipoOperacion = TipoOperacion;
			this.TipoInstrumento = TipoInstrumento;
			this.EstadoOperacion = EstadoOperacion;
			this.CodigoValor = CodigoValor;
			this.Nemotecnico = Nemotecnico;
			this.Precio = Precio;
			this.Cantidad = Cantidad;
			this.AgenteComprador = AgenteComprador;
			this.AgenteVendedor = AgenteVendedor;
			this.MontoEfectivo = MontoEfectivo;
			this.ValorNominal = ValorNominal;
			this.TasaOperacion = TasaOperacion;
			this.FechaLiquidacionContado = FechaLiquidacionContado;
			this.FechaLiquidacionPlazo = FechaLiquidacionPlazo;
			this.CantidadDiasOperacion = CantidadDiasOperacion;
			this.TipoAval = TipoAval;
			this.CodigoAval = CodigoAval;
			this.TipoLiquidacion = TipoLiquidacion;
			this.IndicadorHabilitacionFondos = IndicadorHabilitacionFondos;
			this.TipoCambio = TipoCambio;
			this.DescripcionValorNegociado = DescripcionValorNegociado;
			this.HoraOperacion = HoraOperacion;
			this.InteresesCorridosContadoPlazo = InteresesCorridosContadoPlazo;
			this.NumeroSubasta = NumeroSubasta;
			this.AgenteCompradorEspecialista = AgenteCompradorEspecialista;
			this.AgenteVendedorEspecialista = AgenteVendedorEspecialista;
			this.FlagOfertaPublica = FlagOfertaPublica;
			
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
	
	public static class InCargaPagoOperacionesInterfaz{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String		p_Archivo;
		public String		p_Ruta;
	}
	
	public static class OutCargaPagoOperacionesInterfaz{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaPagoOperacionesInterfaz in=gson.fromJson(jin.toString() ,InCargaPagoOperacionesInterfaz.class);
		OutCargaPagoOperacionesInterfaz out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutCargaPagoOperacionesInterfaz logica(Connection con, InCargaPagoOperacionesInterfaz entrada) throws Exception {
		OutCargaPagoOperacionesInterfaz out = new OutCargaPagoOperacionesInterfaz();
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivoLog="nor/POP_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			
			SQL sql=new SQL();
			
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo));
			
			String[] registros = archivoDecoded.split(System.getProperty("line.separator"));
			
			List<PagoOperacion> pagosOperaciones = new ArrayList<PagoOperacion>();
			
			for(String registro : registros){
				numeroRegistros++;
				if(registro.length() != LINE_SIZE){
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error de tamaño del registro, se esperaba:" + LINE_SIZE + ",se obtuvo:" + registro.length() + ",registro:" + registro);
					bwArchivoLog.newLine();
				}else{
					try{
						CamposPagoOperacionesInterfaz campo = new CamposPagoOperacionesInterfaz();
						campo.FechaOperacion = registro.substring(0, 8);
						campo.Modalidad	= Integer.parseInt(registro.substring(8,10));
						campo.NumeroOperacion = Integer.parseInt(registro.substring(10,16));
						campo.TipoMoneda = Integer.parseInt(registro.substring(16,18));
						campo.TipoOperacion = Integer.parseInt(registro.substring(18, 20));
						campo.TipoInstrumento = Integer.parseInt(registro.substring(20,22));
						campo.EstadoOperacion = Integer.parseInt(registro.substring(22,24));
						campo.CodigoValor = registro.substring(24,36);
						campo.Nemotecnico = registro.substring(36,46);
						campo.PrecioEntero = Integer.parseInt(registro.substring(46,54));
						campo.PrecioDecimal = Integer.parseInt(registro.substring(54,62));
						campo.CantidadEntero = Integer.parseInt(registro.substring(62,76));
						campo.CantidadDecimal = Integer.parseInt(registro.substring(76,80));
						campo.AgenteComprador = Integer.parseInt(registro.substring(80,84));
						campo.AgenteVendedor = Integer.parseInt(registro.substring(84,88));
						campo.MontoEfectivoEntero = Integer.parseInt(registro.substring(88,102));
						campo.MontoEfectivoDecimal = Integer.parseInt(registro.substring(102,104));
						campo.ValorNominalEntero = Integer.parseInt(registro.substring(104,112));
						campo.ValorNominalDecimal = Integer.parseInt(registro.substring(112,120));
						campo.TasaOperacionEntero = Integer.parseInt(registro.substring(120,124));
						campo.TasaOperacionDecimal = Integer.parseInt(registro.substring(124,128));
						campo.FechaLiquidacionContado = registro.substring(128,136);
						campo.FechaLiquidacionPlazo = registro.substring(136,144);
						campo.CantidadDiasOperacion = Integer.parseInt(registro.substring(144,148));
						campo.TipoAval = Integer.parseInt(registro.substring(148,150));
						campo.CodigoAval = Integer.parseInt(registro.substring(150,154));
						campo.TipoLiquidacion = Integer.parseInt(registro.substring(154,156));
						campo.IndicadorHabilitacionFondos = Integer.parseInt(registro.substring(156,158));
						campo.TipoCambioEntero = Integer.parseInt(registro.substring(158,164));
						campo.TipoCambioDecimal = Integer.parseInt(registro.substring(164,170));
						campo.DescripcionValorNegociado = registro.substring(170,230);
						campo.HoraOperacion = registro.substring(230,236);
						campo.InteresesCorridosContadoPlazoEntero = Integer.parseInt(registro.substring(236,246));
						campo.InteresesCorridosContadoPlazoDecimal = Integer.parseInt(registro.substring(246,250));
						campo.NumeroSubasta = Integer.parseInt(registro.substring(250,252));
						campo.AgenteCompradorEspecialista = Integer.parseInt(registro.substring(252,254));
						campo.AgenteVendedorEspecialista = Integer.parseInt(registro.substring(254,256));
						campo.FlagOfertaPublica = Integer.parseInt(registro.substring(256,258));
						pagosOperaciones.add(new PagoOperacion(campo.FechaOperacion,campo.Modalidad,campo.NumeroOperacion,campo.TipoMoneda,campo.TipoOperacion,campo.TipoInstrumento,
															   campo.EstadoOperacion,campo.CodigoValor,campo.Nemotecnico,new BigDecimal(campo.PrecioEntero + "." + campo.PrecioDecimal),
															   new BigDecimal(campo.CantidadEntero+"."+campo.CantidadDecimal),campo.AgenteComprador,campo.AgenteVendedor,new BigDecimal(campo.MontoEfectivoEntero + "." + campo.MontoEfectivoDecimal),
															   new BigDecimal(campo.ValorNominalEntero + "." + campo.ValorNominalDecimal),new BigDecimal(campo.TasaOperacionEntero + "." + campo.TasaOperacionDecimal),
															   campo.FechaLiquidacionContado,campo.FechaLiquidacionPlazo,campo.CantidadDiasOperacion,campo.TipoAval,campo.CodigoAval,campo.TipoLiquidacion,campo.IndicadorHabilitacionFondos,
															   new BigDecimal(campo.TipoCambioEntero + "." + campo.TipoCambioDecimal),campo.DescripcionValorNegociado,campo.HoraOperacion,new BigDecimal(campo.InteresesCorridosContadoPlazoEntero + "." + campo.InteresesCorridosContadoPlazoDecimal),
															   campo.NumeroSubasta,campo.AgenteCompradorEspecialista,campo.AgenteVendedorEspecialista,campo.FlagOfertaPublica));
						numeroRegistrosCorrecto++;
					}catch(Exception e){
						numeroRegistrosErroneos++;
						bwArchivoLog.write("Error al parsear el registro:" + registro);
						bwArchivoLog.newLine();
					}
				}
			}
			
			sql.batch(con,SQL_INSERT_PAGO_OPERACIONES,pagosOperaciones,PagoOperacion.class);
			
			LogEjecucion logEjecucion = new LogEjecucion();
			logEjecucion.IdInterfaz = 11L;
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
  				
  				InCargaPagoOperacionesInterfaz entrada= new InCargaPagoOperacionesInterfaz();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "NOR";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "MjAxNzA2MjEwMTAwMDAwMTAxMDEwMjAwUEVQNzM2NTgxMDA1R1JBTU9OQzEgIDAwMDAwMDA0ODUwMDAwMDAwMDAwMDAwMDAwMjA0MDAwMDAwMDkxMDAwMTAwMDAwMDAwMDA5ODk0MDAwMDAwMDAwMDAwMDAwMDAxMDAwMDAwMDAyMDE2MDQyNjAwMDAwMDAwMDAwMDAwMDAwMDAyMDMwMDAwMDMyNDkwMDBHUkHRQSBZIE1PTlRFUk8gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAwODM1NDUwMDAwMDAwMDAwMDAwMDAwMDAwMTAwDQoyMDE3MDYyMjAxMDAwMDAxMDEwMTAyMDBQRVA3MzY1ODEwMDVHUkFNT05DMSAgMDAwMDAwMDQ4NTAwMDAwMDAwMDAwMDAwMDAyMDQwMDAwMDAwOTEwMDAxMDAwMDAwMDAwMDk4OTQwMDAwMDAwMDAwMDAwMDAwMDEwMDAwMDAwMDIwMTYwNDI2MDAwMDAwMDAwMDAwMDAwMDAwMDIwMzAwMDAwMzI0OTAwMEdSQdFBIFkgTU9OVEVSTyAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDA4MzU0NTAwMDAwMDAwMDAwMDAwMDAwMDAxMDA=";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaPagoOperacionesInterfaz demo= new CargaPagoOperacionesInterfaz();
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
