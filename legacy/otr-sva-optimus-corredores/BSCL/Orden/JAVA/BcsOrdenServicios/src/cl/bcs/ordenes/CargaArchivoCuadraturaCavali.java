package cl.bcs.ordenes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoCuadraturaCavali implements JavaCall{
	
	private final Log log = new Log("CargaArchivoCuadraturaCavali");
	
	private final String SQL_INSERT_CARGA_CUADRATURA_CAVALI = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoCuadraturaCavali_InsertCargaArchivoCuadraturaCavali.sql");
	
	public static class CuadraturaCavali{
		public Long			IdInterno;
		public String 		FechaOperacion;
		public String 		Modalidad;
		public String 		NumeroOperacion;
		public String 		TipoMoneda;
		public String 		TipoOperacion;
		public String 		TipoInstrumento;
		public String		EstadoOperacion;
		public String		CodigoValor;
		public String		CodigoValor2;
		public BigDecimal   PrecioOperacion;
		public BigDecimal	CantidadOperacion;
		public String    	AgenteComprador;
		public String		AgenteVendedor;
		public BigDecimal	MontoEfectivo;
		public BigDecimal	ValorNominal;
		public BigDecimal	TasaOperacion;
		public String		FechaLiqContado;
		public String		FechaLiqPlazo;
		public String		CantidadDiasOpe;
		public String		TipoAval;
		public String		CodigoAval;
		public String		TipoLiquidacion;
		public String		IndicadorFondos;
		public String		TipoCambio;
		public String		DescripcionNegocio;
		public String		HoraOperacion;
		public BigDecimal	Interes;
		public String		NumeroSubasta;
		public String		AgenteCompEspecial;
		public String		AgenteVendEspecial;
		public String		FlagOfertaPublica;
		public String		CorrelativoBVL;
		public String		IdEstado;
		public String		Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Instrumentos{
		public Long		IdInstrumento;
		public String	IdMoneda;
		public String	Nemotecnico;
		public String   IdClasificacionTipoInstrumento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaArchivoCuadraturaCavali{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoCuadraturaCavali{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoCuadraturaCavali in=gson.fromJson(jin.toString() ,InCargaArchivoCuadraturaCavali.class);
		OutCargaArchivoCuadraturaCavali out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoCuadraturaCavali logica(Connection con, InCargaArchivoCuadraturaCavali entrada) throws Exception {
		OutCargaArchivoCuadraturaCavali out = new OutCargaArchivoCuadraturaCavali();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_CUADRATURA_CAVALI_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		File folder = new File(entrada.p_Ruta + "/ord");
		if(!folder.exists()){
			folder.mkdirs();
		}
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo));
			
			SimpleDateFormat formatoArchivo = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat formatoSP = new SimpleDateFormat("yyyy-MM-dd");
			JsonParser parser     = new JsonParser();
	        JsonObject dataJsonIn  = parser.parse(archivoDecoded).getAsJsonObject();
			
			List<CuadraturaCavali> operaciones = new ArrayList<CuadraturaCavali>();
			
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			Long i = 0L;
			for(JsonElement registro : registros){
				numeroRegistros++;
				CuadraturaCavali operacion = new CuadraturaCavali();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
						Date fechaOperacion = formatoArchivo.parse(elemento.get("CCCAV_FECHAOPERACION").getAsString());
						Date fechaLiqContado = formatoArchivo.parse(elemento.get("CCCAV_FECHALIQCONTADO").getAsString());
						Date fechaLiqPlazo = formatoArchivo.parse(elemento.get("CCCAV_FECHALIQPLAZO").getAsString());
						operacion.FechaOperacion = formatoSP.format(fechaOperacion);
						operacion.Modalidad = elemento.get("CCCAV_MODALIDAD").getAsString();
						operacion.NumeroOperacion = elemento.get("CCCAV_NUMEROOPERACION").getAsString();
						operacion.TipoMoneda = elemento.get("CCCAV_TIPOMONEDA").getAsString();
						operacion.TipoOperacion = elemento.get("CCCAV_TIPOOPERACION").getAsString();
						operacion.TipoInstrumento = elemento.get("CCCAV_TIPOINSTRUMENTO").getAsString();
						operacion.EstadoOperacion = elemento.get("CCCAV_ESTADOOPERACION").getAsString();
						operacion.CodigoValor = elemento.get("CCCAV_CODIGOVALOR").getAsString();
						operacion.CodigoValor2 = elemento.get("CCCAV_CODIGOVALOR2").getAsString();
						operacion.PrecioOperacion = elemento.get("CCCAV_PRECIOOPERACION").getAsBigDecimal();
						operacion.CantidadOperacion = elemento.get("CCCAV_CANTIDADOPERACION").getAsBigDecimal();
						operacion.AgenteComprador = elemento.get("CCCAV_AGENTECOMPRADOR").getAsString();
						operacion.AgenteVendedor = elemento.get("CCCAV_AGENTEVENDEDOR").getAsString();
						operacion.MontoEfectivo = elemento.get("CCCAV_MONTOEFECTIVO").getAsBigDecimal();
						operacion.ValorNominal = elemento.get("CCCAV_VALORNOMINAL").getAsBigDecimal();
						operacion.TasaOperacion = elemento.get("CCCAV_TASAOPERACION").getAsBigDecimal();
						operacion.FechaLiqContado = formatoSP.format(fechaLiqContado);
						operacion.FechaLiqPlazo = formatoSP.format(fechaLiqPlazo);
						operacion.CantidadDiasOpe = elemento.get("CCCAV_CANTIDADDIASOPE").getAsString();
						operacion.TipoAval = elemento.get("CCCAV_TIPOAVAL").getAsString();
						operacion.CodigoAval = elemento.get("CCCAV_CODIGOAVAL").getAsString();
						operacion.TipoLiquidacion = elemento.get("CCCAV_TIPOLIQUIDACION").getAsString();
						operacion.IndicadorFondos = elemento.get("CCCAV_INDICADORFONDOS").getAsString();
						operacion.TipoCambio = elemento.get("CCCAV_TIPOCAMBIO").getAsString();
						operacion.DescripcionNegocio = elemento.get("CCCAV_DESCRIPCIONNEGOCIO").getAsString();
						operacion.HoraOperacion = elemento.get("CCCAV_HORAOPERACION").getAsString();
						operacion.Interes = elemento.get("CCCAV_INTERES").getAsBigDecimal();
						operacion.NumeroSubasta = elemento.get("CCCAV_NUMEROSUBASTA").getAsString();
						operacion.AgenteCompEspecial = elemento.get("CCCAV_AGENTECOMPESPECIAL").getAsString();
						operacion.AgenteVendEspecial = elemento.get("CCCAV_AGENTEVENDESPECIAL").getAsString();
						operacion.FlagOfertaPublica = elemento.get("CCCAV_FLAGOFERTAPUBLICA").getAsString();
						operacion.CorrelativoBVL = elemento.get("CCCAV_CORRELATIVOBVL").getAsString();
						
						operacion.IdInterno = i;
						operacion.IdEstado = "PEN";
						operacion.Resultado = "";
						operaciones.add(operacion);
					 }catch(Exception e){
						numeroRegistrosErroneos++;
						bwArchivoLog.write("Error al parsear el registro:" + elemento);
						bwArchivoLog.newLine();
					}
				}else{
					numeroRegistrosErroneos++;
					bwArchivoLog.write("Registro con Error:" + validacion + ",registro:" + elemento);
					bwArchivoLog.newLine();
				}
			}
			
			sql.batch(con, SQL_INSERT_CARGA_CUADRATURA_CAVALI, operaciones, CuadraturaCavali.class);
			//evaluar salida
			long endTime = System.currentTimeMillis();
			String fechaFin = fm.format(new Date());
			bwArchivoLog.write("Fecha Hora Inicio:" + fecha);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Fecha Hora Termino:" + fechaFin);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Tiempo de ejecución:" + (endTime - startTime));
			bwArchivoLog.newLine();
			bwArchivoLog.write("Total Registros:" + numeroRegistros);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Correctos:" + numeroRegistrosCorrecto);
			bwArchivoLog.newLine();
			bwArchivoLog.write("Registros Erroneos:" + numeroRegistrosErroneos);
			out.p_out_ErroresContador = new Long(numeroRegistrosErroneos);
			
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
  				
  				InCargaArchivoCuadraturaCavali entrada= new InCargaArchivoCuadraturaCavali();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ0lHS19DT0RQQVJUSUNJUEFOVEUiOiIwMDAxIiwiQ0lHS19GRUNIQU9QRVJBQ0lPTiI6IjIwMTYxMTAyIiwiQ0lHS19NT0RBTElEQUQiOiIwMSIsIkNJR0tfQ09ETU9ORURBIjoiMDEiLCJDSUdLX1RJUE9DT01QIjoiMDEiLCJDSUdLX1RJUE9QQVJUIjoiMDEiLCJDSUdLX1RJUE9PUEVSIjoiMDEiLCJDSUdLX05FTU9OSUNPIjoiQUxJQ09SQzEgICIsIkNJR0tfTlJPT1BFUiI6MTcsIkNJR0tfQ09EVElUVUxBUiI6MTE3MS4zOCwiQ0lHS19DQU5USURBRCI6NTksIkNJR0tfUFJFQ0lPIjo3ODUwMDAwLCJDSUdLX1BBUlRJQ0lQQU5URUFGRUNUQURPIjoiMDAwMSIsIkNJR0tfTU9OVE9FRkVDVElWTyI6NDYzMTUsIkNJR0tfSUdLIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0NVT1RBQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0lHVkNBVkFMSSI6IjAwMDAwMDAwMDAwMDAwIiwiQ0lHS19FTkNBUkdBRE9WQUxPUkVTIjoiMDAwMCIsIkNJR0tfTlJPRU5WSU8iOiIwMSIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifSx7IkNJR0tfQ09EUEFSVElDSVBBTlRFIjoiMDAwMSIsIkNJR0tfRkVDSEFPUEVSQUNJT04iOiIyMDE2MTEwMiIsIkNJR0tfTU9EQUxJREFEIjoiMDEiLCJDSUdLX0NPRE1PTkVEQSI6IjAxIiwiQ0lHS19USVBPQ09NUCI6IjAxIiwiQ0lHS19USVBPUEFSVCI6IjAxIiwiQ0lHS19USVBPT1BFUiI6IjAxIiwiQ0lHS19ORU1PTklDTyI6IkFMSUNPUkMxICAiLCJDSUdLX05ST09QRVIiOjIwLCJDSUdLX0NPRFRJVFVMQVIiOjExNzEuMzgsIkNJR0tfQ0FOVElEQUQiOjYsIkNJR0tfUFJFQ0lPIjo3ODUwMDAwLCJDSUdLX1BBUlRJQ0lQQU5URUFGRUNUQURPIjoiMDAwMSIsIkNJR0tfTU9OVE9FRkVDVElWTyI6NDcxMCwiQ0lHS19JR0siOiIwMDAwMDAwMDAwMDAwMCIsIkNJR0tfQ1VPVEFDQVZBTEkiOiIwMDAwMDAwMDAwMDAwMCIsIkNJR0tfSUdWQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0VOQ0FSR0FET1ZBTE9SRVMiOiIwMDAwIiwiQ0lHS19OUk9FTlZJTyI6IjAxIiwiTUVOU0FKRVZBTElEQUNJT04iOiJPSyJ9LHsiQ0lHS19DT0RQQVJUSUNJUEFOVEUiOiIwMDAxIiwiQ0lHS19GRUNIQU9QRVJBQ0lPTiI6IjIwMTYxMTAyIiwiQ0lHS19NT0RBTElEQUQiOiIwMSIsIkNJR0tfQ09ETU9ORURBIjoiMDEiLCJDSUdLX1RJUE9DT01QIjoiMDEiLCJDSUdLX1RJUE9QQVJUIjoiMDEiLCJDSUdLX1RJUE9PUEVSIjoiMDEiLCJDSUdLX05FTU9OSUNPIjoiQUxJQ09SQzEgICIsIkNJR0tfTlJPT1BFUiI6NTEsIkNJR0tfQ09EVElUVUxBUiI6MTE3MS4zOCwiQ0lHS19DQU5USURBRCI6MzUsIkNJR0tfUFJFQ0lPIjo3ODUwMDAwLCJDSUdLX1BBUlRJQ0lQQU5URUFGRUNUQURPIjoiMDAwMSIsIkNJR0tfTU9OVE9FRkVDVElWTyI6Mjc0NzUsIkNJR0tfSUdLIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0NVT1RBQ0FWQUxJIjoiMDAwMDAwMDAwMDAwMDAiLCJDSUdLX0lHVkNBVkFMSSI6IjAwMDAwMDAwMDAwMDAwIiwiQ0lHS19FTkNBUkdBRE9WQUxPUkVTIjoiMDAwMCIsIkNJR0tfTlJPRU5WSU8iOiIwMSIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifV19";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoCuadraturaCavali demo= new CargaArchivoCuadraturaCavali();
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
