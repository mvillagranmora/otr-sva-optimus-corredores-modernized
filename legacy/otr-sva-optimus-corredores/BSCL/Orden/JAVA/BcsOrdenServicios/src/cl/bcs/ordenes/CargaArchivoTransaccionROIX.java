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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoTransaccionROIX implements JavaCall{
	
	private final Log log =new Log("CargaArchivoTransaccionROIX");
	
	private final String SQL_INSERT_CARGA_TRANSACCION_ROIX = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionROIX_InsertCargaArchivoTransaccionROIX.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionROIX_SelectInstrumentos.sql");
	private final String SQL_SELECT_CORREDOR_CORRESPONSAL = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionROIX_SelectCorredorCorresponsal.sql");
	private final String SQL_SELECT_LIBRO = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionROIX_SelectLibroOrdenes.sql");
	
	public static class TransaccionROIX{
		public Long			IdInterno;
		public String 		NombreOrden;
		public Integer 		NumeroOperacion;
		public String  		Destino;
		public String  		Sucursal;
		public Integer		Secuencia;
		public String		HoraTransaccion;
		public String		TipoOperacion;
		public BigDecimal	Cantidad;
		public String		Instrumento;
		public BigDecimal	Precio;
		public String		Caja;
		public String		AliasCliente;
		public String 		Operador;
		public BigDecimal 	LvsQuan;
		public BigDecimal	ExQuan;
		public BigDecimal 	ExPrice;
		public BigDecimal	CxlQuan;
		public BigDecimal	StopPrice;
		public String		Firm;
		public String		ID;
		public String		MMID;
		public String		PutCall;
		public String		OpenClose;
		public String		ExpMonth;
		public BigDecimal	StrikePrice;
		public String		ExecutingBroker;
		public BigDecimal	Comision;
		public BigDecimal	AwayFlag;
		public BigDecimal	BigDecimal;
		public Integer		BoothID;
		public String		SubAccount;
		public String		Terminal;
		public Integer		ClOrdID;
		public String		CustExtra;
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
	
	public static class InCargaArchivoTransaccionROIX{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String		p_NombreArchivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoTransaccionROIX{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoTransaccionROIX in=gson.fromJson(jin.toString() ,InCargaArchivoTransaccionROIX.class);
		OutCargaArchivoTransaccionROIX out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoTransaccionROIX logica(Connection con, InCargaArchivoTransaccionROIX entrada) throws Exception {
		OutCargaArchivoTransaccionROIX out = new OutCargaArchivoTransaccionROIX();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_ARCHIVO_TRANSACCION_ROIX_"+fecha+".log";
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
			
			List<TransaccionROIX> transacciones = new ArrayList<TransaccionROIX>();
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			SimpleDateFormat formatoNombreArchivo = new SimpleDateFormat("yyyyMMdd");
			
			SimpleDateFormat formatoSistema = new SimpleDateFormat("yyyy-MM-dd");
			
			String fechaArchivo = null;
			
			try{
				fechaArchivo = formatoSistema.format(formatoNombreArchivo.parse(entrada.p_NombreArchivo.split("_")[1].split("\\.")[0]));
			}catch(Exception e){
				numeroRegistrosErroneos++;
				bwArchivoLog.write("Formato nombre archivo erroneo:" + entrada.p_NombreArchivo);
				bwArchivoLog.newLine();
			}
					
			
			if(entrada.p_NombreArchivo != null && fechaArchivo != null && fechaSistema.equals(formatoSistema.format(formatoNombreArchivo.parse(entrada.p_NombreArchivo.split("_")[1].split("\\.")[0])))){
				JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
				List<Transaccion_Entrada> transacciones_entrada = new ArrayList<Transaccion_Entrada>();
				List<String> nemotecnicos = new ArrayList<String>();
				List<String> corredoresCorresponsal = new ArrayList<String>();
				Long i = 0L;
				for(JsonElement registro : registros){
					numeroRegistros++;
					TransaccionROIX transaccion = new TransaccionROIX();
					JsonObject elemento = registro.getAsJsonObject();
					String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
					if("OK".equalsIgnoreCase(validacion)){
						try{ 
							transaccion.NombreOrden = elemento.get("OrderName").getAsString();
							transaccion.NumeroOperacion = elemento.get("OrderID").getAsInt();
							transaccion.Destino = elemento.get("Dest").getAsString();
							transaccion.Sucursal = elemento.get("Branch").getAsString();
							transaccion.Secuencia = elemento.get("Seq").getAsInt();
							transaccion.HoraTransaccion = elemento.get("Time").getAsString();
							transaccion.TipoOperacion = elemento.get("Instr").getAsString();
							transaccion.Cantidad = elemento.get("Quan").getAsBigDecimal();
							transaccion.Instrumento = elemento.get("Symbol").getAsString();
							transaccion.Precio = elemento.get("Price").getAsBigDecimal();
							transaccion.Caja = elemento.get("TIF").getAsString();
							transaccion.AliasCliente = elemento.get("Acct").getAsString();
							transaccion.Operador = elemento.get("Trader").getAsString();
							transaccion.LvsQuan = elemento.get("LvsQuan").getAsBigDecimal();
							transaccion.ExQuan = elemento.get("ExQuan").getAsBigDecimal();
							transaccion.ExPrice = elemento.get("ExPrice").getAsBigDecimal();
							transaccion.CxlQuan = elemento.get("CxlQuan").getAsBigDecimal();
							transaccion.StopPrice = elemento.get("StopPrice").getAsBigDecimal();
							transaccion.Firm = elemento.get("Firm").getAsString();
							transaccion.ID = elemento.get("ID").getAsString();
							transaccion.MMID = elemento.get("MMID").getAsString();
							transaccion.PutCall = elemento.get("PutCall").getAsString();
							transaccion.OpenClose = elemento.get("OpenClose").getAsString();					
							transaccion.ExpMonth = elemento.get("ExpMonth").getAsString();
							transaccion.StrikePrice = elemento.get("StrikePrice").getAsBigDecimal();
							transaccion.ExecutingBroker = elemento.get("ExecutingBroker").getAsString();
							transaccion.Comision = elemento.get("Commission").getAsBigDecimal();
							transaccion.AwayFlag = elemento.get("AwayFlag").getAsBigDecimal();
							transaccion.BoothID = elemento.get("BoothID").getAsInt();
							transaccion.SubAccount = elemento.get("SubAccount").getAsString();
							transaccion.Terminal = elemento.get("StationID").getAsString();
							transaccion.ClOrdID = elemento.get("ClOrdID").getAsInt();
							transaccion.CustExtra = elemento.get("CustExtra").getAsString();
							transaccion.IdInterno = i;
							transaccion.IdEstado = "PEN";
							transaccion.Resultado = "";
							i++;
							if(!nemotecnicos.contains(transaccion.Instrumento)) nemotecnicos.add(transaccion.Instrumento);
							if(!corredoresCorresponsal.contains(transaccion.ExecutingBroker)) corredoresCorresponsal.add(transaccion.ExecutingBroker);
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
				params.put("Nemotecnicos", nemotecnicos);
				List<Instrumentos> idInstrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
				
				params.clear();
				params.put("CodigoCorredor", corredoresCorresponsal);
				params.put("IdPais", informacionEmpresa.IdPais);
				List<CorredorCorresponsal> _corredoresCorresponsal = sql.query(con, SQL_SELECT_CORREDOR_CORRESPONSAL, params, CorredorCorresponsal.class);
				
				params.clear();
				params.put("IdEmpresa",entrada.p_IdEmpresa);
				List<Libro> libroOrdenes = sql.query(con, SQL_SELECT_LIBRO, params, Libro.class);
				
				if(libroOrdenes.size() > 0) idLibroDefecto = libroOrdenes.get(0).IdLibroOrdenes;
				
				for(TransaccionROIX transaccion : transacciones){
					try{
						Transaccion_Entrada transaccion_entrada = new Transaccion_Entrada();
						transaccion_entrada.IdInterno = transaccion.IdInterno;
						transaccion_entrada.FolioTransaccion = String.valueOf(transaccion.NumeroOperacion);
						transaccion_entrada.FechaCreacion = fechaSistema;
						transaccion_entrada.HoraCreacion = transaccion.HoraTransaccion;
						transaccion_entrada.FechaEmision = fechaSistema;
						transaccion_entrada.EsCuentaPropia = "N";
						transaccion_entrada.EsFungible = "N";
						transaccion_entrada.EsVentaCorta = "N";
						transaccion_entrada.CodigoOperadorCorredor = transaccion.Operador;
						transaccion_entrada.Cantidad = transaccion.Cantidad;
						transaccion_entrada.Monto = transaccion.Cantidad.multiply(transaccion.Precio,MathContext.DECIMAL128);
						transaccion_entrada.Precio = transaccion.Precio;
						transaccion_entrada.GastosCorresponsal = BigDecimal.ZERO;
						transaccion_entrada.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
						transaccion_entrada.TipoCambioCorresponsal = BigDecimal.ZERO;
						transaccion_entrada.Intereses = BigDecimal.ZERO;
						transaccion_entrada.Reajustes = BigDecimal.ZERO;
						String _compra = "";
						String _venta = "";
						for(Instrumentos instrumento : idInstrumentos){
							if(instrumento.Nemotecnico.equals(transaccion.Instrumento)){
								_compra = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "COMACN" : "RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "COMIRF" : "";
								_venta = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "VENACN" : "RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "VENIRF" : "";
								transaccion_entrada.IdMoneda = instrumento.IdMoneda;
								transaccion_entrada.IdMonedaLiquidacionCorresponsal = instrumento.IdMoneda;
								transaccion_entrada.IdInstrumento = instrumento.IdInstrumento;
							}
						}
						if(transaccion_entrada.IdInstrumento == null) throw new Exception("Instrumento:" + transaccion.Instrumento + ",no existe");
						
						transaccion_entrada.IdTipoOperacion = "BUY".equalsIgnoreCase(transaccion.TipoOperacion) ? _compra : "SL".equalsIgnoreCase(transaccion.TipoOperacion) ? _venta : "";
						for(CorredorCorresponsal corredorCorresponsal : _corredoresCorresponsal){
							if(corredorCorresponsal.CodigoCorredor.equalsIgnoreCase(transaccion.ExecutingBroker)){
								transaccion_entrada.IdCorredoraCorresponsal = corredorCorresponsal.IdCorredorCorresponsal;
							}
						}
						if(transaccion_entrada.IdCorredoraCorresponsal == null) throw new Exception("Corredor Corresponsal:" + transaccion.ExecutingBroker + ",no existe");
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
							for(TransaccionROIX transaccion : transacciones){
								if(resultado.IdInterno.longValue() == transaccion.IdInterno.longValue()){
									numeroRegistrosCorrecto++;
									transaccion.IdEstado = "OK";
									transaccion.Resultado = "OK";
								}
							}
						}
						for(MensajeSalida mensaje : crearTransacciones_Salida.P_OUT_MENSAJES){
							for(TransaccionROIX transaccion : transacciones){
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
						for(TransaccionROIX transaccion : transacciones){
							if("PEN".equalsIgnoreCase(transaccion.IdEstado)){
								transaccion.IdEstado = "ERR";
								transaccion.Resultado = "Error servicio transaccion";
								numeroRegistrosErroneos++;
							}
						}
						bwArchivoLog.write("Error servicio transaccion:" + e.getMessage());
						bwArchivoLog.newLine();
						con.rollback();
					}
				}
				
				for(TransaccionROIX transaccion : transacciones){
					if("PEN".equalsIgnoreCase(transaccion.IdEstado)) transaccion.IdEstado = "ERR";
				}
				
				sql.batch(con, SQL_INSERT_CARGA_TRANSACCION_ROIX, transacciones, TransaccionROIX.class);
			}else{
				numeroRegistrosErroneos++;
				bwArchivoLog.write("Fecha del archivo no coincide con fecha sistema");
				bwArchivoLog.newLine();
			}
			
			
			
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
			out.p_out_ErroresContador = new Long(numeroRegistrosErroneos);;
			
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
  				
  				InCargaArchivoTransaccionROIX entrada= new InCargaArchivoTransaccionROIX();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_NombreArchivo = "roxexec.csv";
  				entrada.p_Archivo		= "eyJwX2NvbnRlbmlkbyI6W3siT3JkZXJOYW1lIjoiUUNDLUNETi0xIiwiT3JkZXJJRCI6MTY4MDAwMSwiRGVzdCI6IkFFWEciLCJCcmFuY2giOiJDRE4iLCJTZXEiOjEsIlRpbWUiOiIxMDowOToyMSIsIkluc3RyIjoiQlVZIiwiUXVhbiI6MzAsIlN5bWJvbCI6IkNPUEVDIiwiUHJpY2UiOiI0LjM2IiwiVElGIjoiREFZIiwiQWNjdCI6IkNSRURJUkVUQUlMIiwiVHJhZGVyIjoiSkMiLCJMdnNRdWFuIjoiMCIsIkV4UXVhbiI6MzAsIkV4UHJpY2UiOiI0LjM2IiwiQ3hsUXVhbiI6IjAiLCJTdG9wUHJpY2UiOiIwIiwiRmlybSI6IkxFSyIsIklEIjoiIiwiTU1JRCI6IiIsIlB1dENhbGwiOiIiLCJPcGVuQ2xvc2UiOiIiLCJFeHBNb250aCI6IiIsIlN0cmlrZVByaWNlIjoiMCIsIkV4ZWN1dGluZ0Jyb2tlciI6IkVYWFgiLCJDb21taXNzaW9uIjoiMCIsIkF3YXlGbGFnIjoiMCIsIkJvb3RoSUQiOjAsIlN1YkFjY291bnQiOiIiLCJTdGF0aW9uSUQiOiJGUzUzIiwiQ2xPcmRJRCI6MCwiQ3VzdEV4dHJhIjoiUy1GUzUzIE9ULTEwMDkyMSIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifV19DQo=";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoTransaccionROIX demo= new CargaArchivoTransaccionROIX();
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
