package cl.bcs.ordenes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
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

import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;
import cl.bcs.ordenes.CrearTransacciones.Resultado_Salida;
import cl.bcs.ordenes.CrearTransacciones.Transaccion_Entrada;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoTransaccionTOMS implements JavaCall{
	
	private final Log log =new Log("CargaArchivoTransaccionTOMS");
	
	private final String SQL_INSERT_CARGA_TRANSACCION_TOMS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionTOMS_InsertCargaArchivoTransaccionTOMS.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionTOMS_SelectInstrumentos.sql");
	private final String SQL_SELECT_CORREDOR_CORRESPONSAL = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionTOMS_SelectCorredorCorresponsal.sql");
	private final String SQL_SELECT_LIBRO = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionTOMS_SelectLibroOrdenes.sql");
	
	public static class TransaccionTOMS{
		public Long			IdInterno;
		public Integer 		NumeroOperacion;
	    public String  		CodigoPortafolio;
	    public String		FechaOperacion;
	    public String		FechaOperacionHora;
	    public String		ISIN;
	    public String		TipoOperacion;
	    public String		FechaLiquidacion;
	    public String		IdMoneda;
	    public BigDecimal	Cantidad;
	    public BigDecimal	TraderPrice;
	    public BigDecimal	Principal;
	    public BigDecimal	Interes;
	    public String		LongNote1;
	    public BigDecimal	MontoComision;
		public BigDecimal	Yield;
	    public String		TipoTransaccion;
		public BigDecimal	CommissionRate;
	    public String		BrokerId;
	    public String		TraderLogin;
	    public String		ReasonCode;
	    public String		Mercado;
		public String		Filler;
	    public String		MasterTicket;
	    public String		TransCpartyShortName;
	    public Integer		NumeroOperacionOrigen;
	    public String		Action;
	    public Integer		TipoCambio;
		public BigDecimal	TraderPprice;
	    public String		SalesName;
	    public String		ExchangeRate;
	    public String		ShortCover;
	    public String		IdEstado;
	    public String		Resultado;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumentos{
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMoneda;
		public String ISIN;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CorredorCorresponsal{
		public Long IdCorredorCorresponsal;
		public String CodigoCorredor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Libro{
		public Long IdLibroOrdenes;
		public Long IdEmpresa;
		public String NombreLibroOrdenes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaArchivoTransaccionTOMS{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoTransaccionTOMS{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoTransaccionTOMS in=gson.fromJson(jin.toString() ,InCargaArchivoTransaccionTOMS.class);
		OutCargaArchivoTransaccionTOMS out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoTransaccionTOMS logica(Connection con, InCargaArchivoTransaccionTOMS entrada) throws Exception {
		OutCargaArchivoTransaccionTOMS out = new OutCargaArchivoTransaccionTOMS();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_TRANSACCION_TOMS_"+fecha+".log";
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
			JsonParser parser     = new JsonParser();
	        JsonObject dataJsonIn  = parser.parse(archivoDecoded).getAsJsonObject();
			
			List<TransaccionTOMS> transacciones = new ArrayList<TransaccionTOMS>();
			List<Transaccion_Entrada> transacciones_entrada = new ArrayList<Transaccion_Entrada>();
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			List<String> isins = new ArrayList<String>();
			List<String> corredoresCorresponsal = new ArrayList<String>();
			Long i = 0L;
			for(JsonElement registro : registros){
				numeroRegistros++;
				TransaccionTOMS transaccion = new TransaccionTOMS();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
						transaccion.NumeroOperacion = elemento.get("CTTOMS_NUMEROOPERACION").getAsInt();
						transaccion.CodigoPortafolio = elemento.get("CTTOMS_CODIGOPORTAFOLIO").getAsString();
						transaccion.FechaOperacion = elemento.get("CTTOMS_FECHAOPERACION").getAsString();
						transaccion.FechaOperacionHora = elemento.get("CTTOMS_FECHAOPERACIONHORA").getAsString();
						transaccion.ISIN = elemento.get("CTTOMS_ISIN").getAsString();
						transaccion.TipoOperacion = elemento.get("CTTOMS_TIPOOPERACION").getAsString();
						transaccion.FechaLiquidacion = elemento.get("CTTOMS_FECHALIQUIDACION").getAsString();
						transaccion.IdMoneda = elemento.get("CTTOMS_IDMONEDA").getAsString();
						transaccion.Cantidad = elemento.get("CTTOMS_CANTIDAD").getAsBigDecimal();
						transaccion.TraderPrice = elemento.get("CTTOMS_TRADERPRICE").getAsBigDecimal();
						transaccion.Principal = elemento.get("CTTOMS_PRINCIPAL").getAsBigDecimal();
						transaccion.Interes = elemento.get("CTTOMS_INTERES").getAsBigDecimal();
						transaccion.LongNote1 = elemento.get("CTTOMS_LONGNOTE1").getAsString();
						transaccion.MontoComision = elemento.get("CTTOMS_MONTOCOMISION").getAsBigDecimal();
						transaccion.Yield = elemento.get("CTTOMS_YIELD").getAsBigDecimal();
						transaccion.TipoTransaccion = elemento.get("CTTOMS_TIPOTRANSACCION").getAsString();
						transaccion.CommissionRate = elemento.get("CTTOMS_COMMISSIONRATE").getAsBigDecimal();
						transaccion.BrokerId = elemento.get("CTTOMS_BROKERID").getAsString();
						transaccion.TraderLogin = elemento.get("CTTOMS_TRADERLOGIN").getAsString();
						transaccion.ReasonCode = elemento.get("CTTOMS_REASONCODE").getAsString();
						transaccion.Mercado = elemento.get("CTTOMS_MERCADO").getAsString();
						transaccion.Filler = elemento.get("CTTOMS_FILLER").getAsString();
						transaccion.MasterTicket = elemento.get("CTTOMS_MASTERTICKET").getAsString();
						transaccion.TransCpartyShortName = elemento.get("CTTOMS_TRANSCPARTYSHORTNAME").getAsString();
						transaccion.NumeroOperacionOrigen = elemento.get("CTTOMS_NUMEROOPERACIONORIGEN").getAsInt();
						transaccion.Action = elemento.get("CTTOMS_ACTION").getAsString();
						transaccion.TipoCambio  = elemento.get("CTTOMS_TIPOCAMBIO").getAsInt();
						transaccion.TraderPprice = elemento.get("CTTOMS_TRADERPPRICE").getAsBigDecimal();
						transaccion.SalesName = elemento.get("CTTOMS_SALESNAME").getAsString();
						transaccion.ExchangeRate  = elemento.get("CTTOMS_EXCHANGERATE").getAsString();
						transaccion.ShortCover = elemento.get("CTTOMS_SHORTCOVER").getAsString();
						transaccion.IdInterno = i;
						transaccion.IdEstado = "PEN";
						transaccion.Resultado = "";
						i++;
						if(!isins.contains(transaccion.ISIN)) isins.add(transaccion.ISIN);
						if(!corredoresCorresponsal.contains(transaccion.BrokerId)) corredoresCorresponsal.add(transaccion.BrokerId);
						transacciones.add(transaccion);
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
			
			Long idLibroDefecto = 0L;
			ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
			InformacionEmpresa informacionEmpresa = consultarEmpresa.logica(con, entrada.p_IdEmpresa);
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("ISIN", isins);
			List<Instrumentos> idInstrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
			
			params.clear();
			params.put("CodigoCorredor", corredoresCorresponsal);
			params.put("IdPais", informacionEmpresa.IdPais);
			List<CorredorCorresponsal> _corredoresCorresponsal = sql.query(con, SQL_SELECT_CORREDOR_CORRESPONSAL, params, CorredorCorresponsal.class);
			
			params.clear();
			params.put("IdEmpresa",entrada.p_IdEmpresa);
			List<Libro> libroOrdenes = sql.query(con, SQL_SELECT_LIBRO, params, Libro.class);
			
			if(libroOrdenes.size() > 0) idLibroDefecto = libroOrdenes.get(0).IdLibroOrdenes;
			
			for(TransaccionTOMS transaccion : transacciones){
				try{
					Transaccion_Entrada transaccion_entrada = new Transaccion_Entrada();
					transaccion_entrada.IdInterno = transaccion.IdInterno;
					transaccion_entrada.IdMoneda = transaccion.IdMoneda;
					transaccion_entrada.IdMonedaLiquidacionCorresponsal = transaccion.IdMoneda;
					transaccion_entrada.FolioTransaccion = String.valueOf(transaccion.NumeroOperacion);
					transaccion_entrada.FechaCreacion = transaccion.FechaOperacion.split("T")[0];
					transaccion_entrada.HoraCreacion = transaccion.FechaOperacionHora;
					transaccion_entrada.FechaEmision = transaccion.FechaOperacion.split("T")[0];
					transaccion_entrada.EsCuentaPropia = "N";
					transaccion_entrada.EsFungible = "N";
					transaccion_entrada.EsVentaCorta = "N";
					String _compra = "";
					String _venta = "";
					for(Instrumentos instrumento : idInstrumentos){
						if(instrumento.ISIN.equals(transaccion.ISIN)){
							_compra = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "COMACN" : "RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "COMIRF" : "";
							_venta = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "VENACN" : "RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "VENIRF" : "";
							transaccion_entrada.IdInstrumento = instrumento.IdInstrumento;
						}
					}
					if(transaccion_entrada.IdInstrumento == null) throw new Exception("Instrumento:" + transaccion.ISIN + ",no existe");
					for(CorredorCorresponsal corredorCorresponsal : _corredoresCorresponsal){
						if(corredorCorresponsal.CodigoCorredor.equalsIgnoreCase(transaccion.BrokerId)){
							transaccion_entrada.IdCorredoraCorresponsal = corredorCorresponsal.IdCorredorCorresponsal;
						}
					}
					if(transaccion_entrada.IdCorredoraCorresponsal == null) throw new Exception("Corredor Corresponsal:" + transaccion.BrokerId + ",no existe");
					transaccion_entrada.CodigoOperadorCorredor = transaccion.TraderLogin;
					transaccion_entrada.Cantidad = transaccion.Cantidad;
					transaccion_entrada.Monto = transaccion.Cantidad.multiply(transaccion.TraderPrice,MathContext.DECIMAL128);
					transaccion_entrada.Precio = transaccion.TraderPrice;
					transaccion_entrada.IdTipoOperacion = "B".equalsIgnoreCase(transaccion.TipoOperacion) ? _compra : "S".equalsIgnoreCase(transaccion.TipoOperacion) ? _venta : "";
					transaccion_entrada.FechaLiquidacionContado = transaccion.FechaLiquidacion;
					transaccion_entrada.IdCorredoraContraparte = 0L;
					transaccion_entrada.IdCondicionDeLiquidacionContado = "T1";
					transaccion_entrada.IdBolsa = "BVL";
					transaccion_entrada.IdLibroOrdenes = idLibroDefecto;
					transaccion_entrada.IdSistemaBursatil = "";
					transaccion_entrada.CodigoOperadorContraparte = "";
					transaccion_entrada.ValorRescate = BigDecimal.ZERO;
					transaccion_entrada.IdEmpleado = 0L;
					transaccion_entrada.Plazo = 0L;
					transaccion_entrada.MarcaNegociacion = "";
					transaccion_entrada.IndicadorDCV = "";
					transaccion_entrada.EsBilateral = "N";
					transaccion_entrada.EsFueraRueda = "N";
					transaccion_entrada.TipoCambioCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.MejorPrecioCompra = BigDecimal.ZERO;
					transaccion_entrada.MejorPrecioVenta = BigDecimal.ZERO;
					transaccion_entrada.GastosCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.TipoCambioCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.Tasa = transaccion.Interes;
					transaccion_entrada.Intereses = BigDecimal.ZERO;
					transaccion_entrada.Reajustes = BigDecimal.ZERO;
					transaccion_entrada.FlujoOrigen = "INGRESOORDEN";
					transacciones_entrada.add(transaccion_entrada);
				}catch(Exception e){
					numeroRegistrosErroneos++;
					transaccion.Resultado = e.getMessage();
					transaccion.IdEstado = "ERR";
					bwArchivoLog.write("Registro con Error:" + e.getMessage());
					bwArchivoLog.newLine();
				}
				
			}
			
			if(transacciones_entrada.size() > 0){
				CrearTransacciones_Entrada crearTransacciones_Entrada = new CrearTransacciones_Entrada();
				crearTransacciones_Entrada.p_IdEmpresa = entrada.p_IdEmpresa;
				crearTransacciones_Entrada.p_IdModulo = entrada.p_IdModulo;
				crearTransacciones_Entrada.p_IdUsuario = entrada.p_IdUsuario;
				crearTransacciones_Entrada.p_simularOperacion = "N";
				crearTransacciones_Entrada.p_UsaReglasNegocio = "S";
				crearTransacciones_Entrada.p_Transacciones = transacciones_entrada;
				
				CrearTransacciones creaTransacciones=new CrearTransacciones();
				try{
					CrearTransacciones_Salida crearTransacciones_Salida=creaTransacciones.logica(con, crearTransacciones_Entrada);
					for(Resultado_Salida resultado : crearTransacciones_Salida.P_OUT_RESULTADO){
						for(TransaccionTOMS transaccion : transacciones){
							if(resultado.IdInterno.longValue() == transaccion.IdInterno.longValue()){
								numeroRegistrosCorrecto++;
								transaccion.IdEstado = "OK";
								transaccion.Resultado = "OK";
							}
						}
					}
					for(MensajeSalida mensaje : crearTransacciones_Salida.P_OUT_MENSAJES){
						for(TransaccionTOMS transaccion : transacciones){
							if(transaccion.IdInterno.longValue() == mensaje.id.longValue() && "ERROR".equalsIgnoreCase(mensaje.tipo) && "PEN".equalsIgnoreCase(transaccion.IdEstado)){
								transaccion.IdEstado = "ERR";
								transaccion.Resultado = mensaje.descripcion;
								numeroRegistrosErroneos++;
								bwArchivoLog.write("Error a crear transaccion:" + mensaje.descripcion);
								bwArchivoLog.newLine();
							}
						}
					}
				}catch(Exception e){
					for(TransaccionTOMS transaccion : transacciones){
						if("PEN".equalsIgnoreCase(transaccion.IdEstado)){
							transaccion.IdEstado = "ERR";
							transaccion.Resultado = "Error servicio transaccion";
							numeroRegistrosErroneos++;
						}
					}
					e.printStackTrace();
					bwArchivoLog.write("Error servicio transaccion:" + e.getMessage());
					bwArchivoLog.newLine();
					con.rollback();
				}
			}
			
			for(TransaccionTOMS transaccion : transacciones){
				if("PEN".equalsIgnoreCase(transaccion.IdEstado)) transaccion.IdEstado = "ERR";
			}
			
			sql.batch(con, SQL_INSERT_CARGA_TRANSACCION_TOMS, transacciones, TransaccionTOMS.class);
			
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
  				
  				InCargaArchivoTransaccionTOMS entrada= new InCargaArchivoTransaccionTOMS();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siQ1RUT01TX05VTUVST09QRVJBQ0lPTiI6IjE3ODA3IiwiQ1RUT01TX0NPRElHT1BPUlRBRk9MSU8iOiJJTlRFUk1FRCIsIkNUVE9NU19GRUNIQU9QRVJBQ0lPTiI6IjIwMTctMTAtMTZUMDA6MDA6MDAiLCJDVFRPTVNfRkVDSEFPUEVSQUNJT05IT1JBIjoiMTY6MDg6MjEiLCJDVFRPTVNfSVNJTiI6IkNMMDAwMDAwNjE3MiIsIkNUVE9NU19USVBPT1BFUkFDSU9OIjoiQiIsIkNUVE9NU19GRUNIQUxJUVVJREFDSU9OIjoiMjAxNy0xMC0xMFQwMDowMDowMCIsIkNUVE9NU19JRE1PTkVEQSI6IkNMUCIsIkNUVE9NU19DQU5USURBRCI6NTAwMDAsIkNUVE9NU19UUkFERVJQUklDRSI6OTk4Ljk3LCJDVFRPTVNfUFJJTkNJUEFMIjo0OTk0OC41LCJDVFRPTVNfSU5URVJFUyI6IjAiLCJDVFRPTVNfTE9OR05PVEUxIjoiIiwiQ1RUT01TX01PTlRPQ09NSVNJT04iOjI1LCJDVFRPTVNfWUlFTEQiOjQ2MDYuNjksIkNUVE9NU19USVBPVFJBTlNBQ0NJT04iOiJTQiAgIiwiQ1RUT01TX0NPTU1JU1NJT05SQVRFIjoiMC4wNSIsIkNUVE9NU19CUk9LRVJJRCI6IkVYWFgiLCJDVFRPTVNfVFJBREVSTE9HSU4iOiJMRVNQSVJJVFUxICAiLCJDVFRPTVNfUkVBU09OQ09ERSI6IiIsIkNUVE9NU19NRVJDQURPIjoiOCIsIkNUVE9NU19GSUxMRVIiOiIgICAgICAgICAgL1JJTUFDICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiLCJDVFRPTVNfTUFTVEVSVElDS0VUIjoiMTc4MDUiLCJDVFRPTVNfVFJBTlNDUEFSVFlTSE9SVE5BTUUiOiJQRS1SSU1BQyAgICAgICAgICAgICIsIkNUVE9NU19OVU1FUk9PUEVSQUNJT05PUklHRU4iOjE3ODA3LCJDVFRPTVNfQUNUSU9OIjoiMTYiLCJDVFRPTVNfVElQT0NBTUJJTyI6IjEiLCJDVFRPTVNfVFJBREVSUFBSSUNFIjo5OTguOTcsIkNUVE9NU19TQUxFU05BTUUiOiJMSVpFVEhQRSIsIkNUVE9NU19FWENIQU5HRVJBVEUiOiJOIiwiQ1RUT01TX1NIT1JUQ09WRVIiOiIiLCJNRU5TQUpFVkFMSURBQ0lPTiI6Ik9LIn1dfQ0K";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoTransaccionTOMS demo= new CargaArchivoTransaccionTOMS();
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
