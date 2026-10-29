package cl.bcs.ordenes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

public class CargaArchivoTransaccionBVL implements JavaCall{
	
	private final Log log =new Log("CargaArchivoTransaccionBVL");
	
	private final String SQL_INSERT_CARGA_TRANSACCION_BVL = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_InsertCargaArchivoTransaccionBVL.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectInstrumentos.sql");
	private final String SQL_SELECT_CORREDOR_CORRESPONSAL = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectCorredorCorresponsal.sql");
	private final String SQL_SELECT_LIBRO = loadResourceToString("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectLibroOrdenes.sql");
	
	public static class TransaccionBVL{
		public Long			IdInterno;
		public Integer		TipoLibro;
		public Integer		TipoRepo;
		public Integer		TipoBlock;
		public String		Segmento;
		public String		Categoria;
		public String		FolioTransaccion;
		public String		ClientOrderID;
		public String		OriginalClientOrderID;
		public Integer		TradeId;
		public String		AgenciaResponsable;
		public String		AgenciaEnrutador;
		public String		UsuarioEnrutador;
		public String		AgenciaSponsor;
		public String		IdEjecucion;
		public String		IdReferenciaEjecucion;
		public String		TipoEjecucion;
		public String		EstadoPropuesta;
		public String		MotivoRechazo;
		public String		FechaOperacion;
		public String		FechaHoraUltimoEvento;
		public String		FechaLiquidacion;
		public String		Nemotecnico;
		public String		ISIN;
		public Integer		TipoSecurityID;
		public String		BolsaOrigenInstrumento;
		public Integer		TipoOperacion;
		public String		TipoPropuesta;
		public BigDecimal	CantidadPropuesta;
		public BigDecimal	CantidadVigente;
		public BigDecimal	CantidadAcumulada;
		public BigDecimal	CantidadOperacion;
		public BigDecimal	CantidadVisible;
		public BigDecimal	Precio;
		public BigDecimal	PrecioOperacion;
		public BigDecimal	PrecioPromedio;
		public Integer		VigenciaPropuesta;
		public BigDecimal	PrecioPropuestaStop;
		public String		HoraExpiracion;
		public Integer		TipoPrecio;
		public BigDecimal	Rendimiento;
		public String		Texto;
		public String		MonedaInstrumento;
		public String		MonedaLiquidacion;
		public Integer		PlazoLiquidacion;
		public String		AgenciaCalce1;
		public String		AgenciaCalce2;
		public String		AgenciaCalce3;
		public String		AgenciaCalce4;
		public String		AgenciaCalce5;
		public BigDecimal	TasaInteres;
		public Integer		Plazo;
		public String		Garantia1;
		public String		Garantia2;
		public String		Garantia3;
		public String		Garantia4;
		public String		Garantia5;
		public String		Garantia6;
		public String		Garantia7;
		public String		Garantia8;
		public BigDecimal	InteresCorrido1;
		public BigDecimal	InteresCorrido2;
		public Integer		LiquidacionAnticipada;
		public BigDecimal	TasaLiquidacionAnticipada;
		public String		OperacionReferencia;
		public String		AgenciaContraparte;
		public String		Referencia;
		public String		IdPropuestaExchange;
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
	
	public static class InCargaArchivoTransaccionBVL{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoTransaccionBVL{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoTransaccionBVL in=gson.fromJson(jin.toString() ,InCargaArchivoTransaccionBVL.class);
		OutCargaArchivoTransaccionBVL out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoTransaccionBVL logica(Connection con, InCargaArchivoTransaccionBVL entrada) throws Exception {
		OutCargaArchivoTransaccionBVL out = new OutCargaArchivoTransaccionBVL();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_NombreArchivoLog="ord/CARGA_TRANSACCION_BVL_"+fecha+".log";
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
			
	        SimpleDateFormat formatoArchivo = new SimpleDateFormat("yyyyMMdd-HH:mm:ss,SSS");
	        SimpleDateFormat formatoArchivoFecha = new SimpleDateFormat("yyyyMMdd");
	        SimpleDateFormat SPformatoArchivo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss.SSS");
			List<TransaccionBVL> transacciones = new ArrayList<TransaccionBVL>();
			List<Transaccion_Entrada> transacciones_entrada = new ArrayList<Transaccion_Entrada>();
			List<String> nemotecnicos = new ArrayList<String>();
			List<String> corredoresCorresponsal = new ArrayList<String>();
			JsonArray registros= dataJsonIn.getAsJsonArray("p_contenido");
			Long i = 0L;
			DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
			for(JsonElement registro : registros){
				numeroRegistros++;
				TransaccionBVL transaccion = new TransaccionBVL();
				JsonObject elemento = registro.getAsJsonObject();
				String validacion = elemento.get("MENSAJEVALIDACION").getAsString();
				if("OK".equalsIgnoreCase(validacion)){
					try{
						transaccion.TipoLibro = elemento.get("BookType").getAsInt();
						transaccion.TipoRepo = elemento.get("RepoType").getAsInt();
						transaccion.TipoBlock = elemento.get("BlockType").getAsInt();
						transaccion.Segmento = elemento.get("Segment").getAsString();
						transaccion.Categoria = elemento.get("Category").getAsString();
						transaccion.FolioTransaccion = elemento.get("OrderID").getAsString();
						transaccion.ClientOrderID = elemento.get("ClientOrderID").getAsString();
						transaccion.OriginalClientOrderID = elemento.get("OriginalClientOrderID").getAsString();
						transaccion.TradeId = elemento.get("TradeID").getAsInt();
						transaccion.AgenciaResponsable = elemento.get("ExecutingFirm").getAsString();
						transaccion.AgenciaEnrutador = elemento.get("EnteringFirm").getAsString();
						transaccion.UsuarioEnrutador = elemento.get("EnteringTrader").getAsString();
						transaccion.AgenciaSponsor = elemento.get("SponsoringFirm").getAsString();
						transaccion.IdEjecucion = elemento.get("ExecID").getAsString();
						transaccion.IdReferenciaEjecucion = elemento.get("ExecReferenceID").getAsString();
						transaccion.TipoEjecucion = elemento.get("ExecType").getAsString();
						transaccion.EstadoPropuesta = elemento.get("OrderStatus").getAsString();
						transaccion.MotivoRechazo = elemento.get("OrderRejectReason").getAsString();
						transaccion.FechaOperacion = elemento.get("TradeDate").getAsString().isEmpty() ? null : formatoFecha.format(formatoArchivo.parse(elemento.get("TradeDate").getAsString()));
						transaccion.FechaHoraUltimoEvento = SPformatoArchivo.format(formatoArchivo.parse(elemento.get("TransactionTime").getAsString()));
						transaccion.FechaLiquidacion = elemento.get("SettlementDate").getAsString().isEmpty() ? null :  formatoFecha.format(formatoArchivoFecha.parse(elemento.get("SettlementDate").getAsString()));
						transaccion.Nemotecnico = elemento.get("Symbol").getAsString();
						transaccion.ISIN = elemento.get("SecurityID").getAsString();
						transaccion.TipoSecurityID = elemento.get("SecurityIDSource").getAsInt();
						transaccion.BolsaOrigenInstrumento = elemento.get("SecurityExchange").getAsString();
						transaccion.TipoOperacion = elemento.get("Side").getAsInt();
						transaccion.TipoPropuesta = elemento.get("OrderType").getAsString();
						transaccion.CantidadPropuesta = elemento.get("OrderQuantity").getAsBigDecimal();
						transaccion.CantidadVigente  = elemento.get("LeavesQty").getAsBigDecimal();
						transaccion.CantidadAcumulada  = elemento.get("CumulativeQty").getAsBigDecimal();
						transaccion.CantidadOperacion  = elemento.get("LastQty").getAsBigDecimal();
						transaccion.CantidadVisible  = elemento.get("MaxFloor").getAsString().isEmpty() ? BigDecimal.ZERO : elemento.get("MaxFloor").getAsBigDecimal();
						transaccion.Precio  = elemento.get("Price").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("Price").getAsString()).toString());
						transaccion.PrecioOperacion  = elemento.get("LastPrice").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("LastPrice").getAsString()).toString());
						transaccion.PrecioPromedio  = elemento.get("AveragePrice").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("AveragePrice").getAsString()).toString());
						transaccion.VigenciaPropuesta = elemento.get("TimeInForce").getAsInt();
						transaccion.PrecioPropuestaStop = elemento.get("StopPrice").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("StopPrice").getAsString()).toString());
						transaccion.HoraExpiracion = elemento.get("ExpireTime").getAsString().isEmpty() ? null : SPformatoArchivo.format(formatoArchivo.parse(elemento.get("ExpireTime").getAsString()));
						transaccion.TipoPrecio = elemento.get("PriceType").getAsInt();
						transaccion.Rendimiento = elemento.get("Yield").getAsString().isEmpty() ? BigDecimal.ZERO :  new BigDecimal(formatter.parse(elemento.get("Yield").getAsString()).toString());
						transaccion.Texto = elemento.get("Text").getAsString();
						transaccion.MonedaInstrumento = elemento.get("Currency").getAsString();
						transaccion.MonedaLiquidacion = elemento.get("SettlCurrency").getAsString();
						transaccion.PlazoLiquidacion = elemento.get("SettlType").getAsInt();
						transaccion.AgenciaCalce1 = elemento.get("ContraFirm1").getAsString();
						transaccion.AgenciaCalce2 = elemento.get("ContraFirm2").getAsString();
						transaccion.AgenciaCalce3 = elemento.get("ContraFirm3").getAsString();
						transaccion.AgenciaCalce4 = elemento.get("ContraFirm4").getAsString();
						transaccion.AgenciaCalce5 = elemento.get("ContraFirm5").getAsString();
						transaccion.TasaInteres = elemento.get("InterestRate").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("InterestRate").getAsString()).toString());
						transaccion.Plazo = elemento.get("Term").getAsInt();
						transaccion.Garantia1 = elemento.get("Collateral1").getAsString();
						transaccion.Garantia2 = elemento.get("Collateral2").getAsString();
						transaccion.Garantia3 = elemento.get("Collateral3").getAsString();
						transaccion.Garantia4 = elemento.get("Collateral4").getAsString();
						transaccion.Garantia5 = elemento.get("Collateral5").getAsString();
						transaccion.Garantia6 = elemento.get("Collateral6").getAsString();
						transaccion.Garantia7 = elemento.get("Collateral7").getAsString();
						transaccion.Garantia8 = elemento.get("Collateral8").getAsString();
						transaccion.InteresCorrido1 = elemento.get("AccInterest1").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("AccInterest1").getAsString()).toString());
						transaccion.InteresCorrido2 = elemento.get("AccInterest2").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("AccInterest2").getAsString()).toString());
						transaccion.LiquidacionAnticipada = elemento.get("EarlySettlement").getAsInt();
						transaccion.TasaLiquidacionAnticipada = elemento.get("EarlySettlementRate").getAsString().isEmpty() ? BigDecimal.ZERO : new BigDecimal(formatter.parse(elemento.get("EarlySettlementRate").getAsString()).toString());
						transaccion.OperacionReferencia = elemento.get("ClOrderLinkID").getAsString();
						transaccion.AgenciaContraparte = elemento.get("ContraBroker").getAsString();
						transaccion.Referencia = elemento.get("Reference").getAsString();
						transaccion.IdPropuestaExchange = elemento.get("SecondaryOrderID").getAsString();
						transaccion.IdInterno = i;
						if(!nemotecnicos.contains(transaccion.Nemotecnico)) nemotecnicos.add(transaccion.Nemotecnico);
						if(!corredoresCorresponsal.contains(transaccion.AgenciaResponsable)) corredoresCorresponsal.add(transaccion.AgenciaResponsable);
						transaccion.IdEstado = "PEN";
						transaccion.Resultado = "";
						i++;
						transacciones.add(transaccion);
					 }catch(Exception e){
						e.printStackTrace();
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
			params.put("Nemotecnico", nemotecnicos);
			List<Instrumentos> idInstrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumentos.class);
			
			params.clear();
			params.put("CodigoCorredor", corredoresCorresponsal);
			params.put("IdPais", informacionEmpresa.IdPais);
			List<CorredorCorresponsal> _corredoresCorresponsal = sql.query(con, SQL_SELECT_CORREDOR_CORRESPONSAL, params, CorredorCorresponsal.class);
			
			params.clear();
			params.put("IdEmpresa",entrada.p_IdEmpresa);
			List<Libro> libroOrdenes = sql.query(con, SQL_SELECT_LIBRO, params, Libro.class);
			
			if(libroOrdenes.size() > 0) idLibroDefecto = libroOrdenes.get(0).IdLibroOrdenes;
			
			for(TransaccionBVL transaccion : transacciones){
				try{
					java.util.Date Fecha = SPformatoArchivo.parse(transaccion.FechaHoraUltimoEvento);
					Transaccion_Entrada transaccion_entrada = new Transaccion_Entrada();
					transaccion_entrada.IdInterno = transaccion.IdInterno;
					transaccion_entrada.IdMoneda = transaccion.MonedaInstrumento;
					transaccion_entrada.IdMonedaLiquidacionCorresponsal = transaccion.MonedaLiquidacion;
					transaccion_entrada.FolioTransaccion = transaccion.FolioTransaccion;
					transaccion_entrada.FechaCreacion = formatoFecha.format(Fecha);
					transaccion_entrada.HoraCreacion = formatoHora.format(Fecha);
					transaccion_entrada.FechaEmision = formatoFecha.format(Fecha);
					transaccion_entrada.EsCuentaPropia = "N";
					transaccion_entrada.EsFungible = "N";
					transaccion_entrada.EsVentaCorta = "N";
					String _compra = "";
					String _venta = "";
					for(Instrumentos instrumento : idInstrumentos){
						if(instrumento.Nemotecnico.equals(transaccion.Nemotecnico)){
							_compra = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "COMACN" : "RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "COMIRF" : "";
							_venta = "RV".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "VENACN" : "RF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? "VENIRF" : "";
							transaccion_entrada.IdMoneda = instrumento.IdMoneda;
							transaccion_entrada.IdInstrumento = instrumento.IdInstrumento;
						}
					}
					if(transaccion_entrada.IdInstrumento == null) throw new Exception("Instrumento:" + transaccion.Nemotecnico + ",no existe");
					for(CorredorCorresponsal corredorCorresponsal : _corredoresCorresponsal){
						if(corredorCorresponsal.CodigoCorredor.equalsIgnoreCase(transaccion.AgenciaResponsable)){
							transaccion_entrada.IdCorredoraCorresponsal = corredorCorresponsal.IdCorredorCorresponsal;
						}
					}
					if(transaccion_entrada.IdCorredoraCorresponsal == null) throw new Exception("Corredor Corresponsal:" + transaccion.AgenciaResponsable + ",no existe");
					transaccion_entrada.CodigoOperadorContraparte = transaccion.AgenciaContraparte;
					transaccion_entrada.CodigoOperadorCorredor = transaccion.AgenciaResponsable;
					transaccion_entrada.Cantidad = transaccion.CantidadPropuesta;
					transaccion_entrada.Monto = transaccion.CantidadPropuesta.multiply(transaccion.Precio,MathContext.DECIMAL128);
					transaccion_entrada.Precio = transaccion.Precio;
					transaccion_entrada.IdTipoOperacion = transaccion.TipoOperacion == 1 ? _compra : transaccion.TipoOperacion == 2 ? _venta : "";
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
					transaccion_entrada.Tasa = transaccion.TasaInteres;
					transaccion_entrada.GastosCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.TipoCambioCorresponsal = BigDecimal.ZERO;
					transaccion_entrada.Intereses = BigDecimal.ZERO;
					transaccion_entrada.Reajustes = BigDecimal.ZERO;
					
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
						for(TransaccionBVL transaccion : transacciones){
							if(resultado.IdInterno.longValue() == transaccion.IdInterno.longValue()){
								numeroRegistrosCorrecto++;
								transaccion.IdEstado = "OK";
								transaccion.Resultado = "OK";
							}
						}
					}
					for(MensajeSalida mensaje : crearTransacciones_Salida.P_OUT_MENSAJES){
						for(TransaccionBVL transaccion : transacciones){
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
					for(TransaccionBVL transaccion : transacciones){
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
			
			for(TransaccionBVL transaccion : transacciones){
				if("PEN".equalsIgnoreCase(transaccion.IdEstado)) transaccion.IdEstado = "ERR";
			}
			
			sql.batch(con, SQL_INSERT_CARGA_TRANSACCION_BVL, transacciones, TransaccionBVL.class);
			
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
  				InCargaArchivoTransaccionBVL entrada= new InCargaArchivoTransaccionBVL();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_IdUsuario		= new Long(21);
  				entrada.p_IdModulo 		= "ORD";
  				entrada.p_Ruta			= "C:\\BCS\\logs";
  				entrada.p_Archivo		= "";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				CargaArchivoTransaccionBVL demo= new CargaArchivoTransaccionBVL();
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
