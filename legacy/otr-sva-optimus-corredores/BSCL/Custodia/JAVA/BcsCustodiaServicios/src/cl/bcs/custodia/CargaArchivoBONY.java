package cl.bcs.custodia;

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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoBONY implements JavaCall{
	
	private final Log log =new Log("CUS.CargaArchivoBONY");
	
	private final String SQL_INSERT_CARGA_ARCHIVO_BONY = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoBONY_InsertCargaArchivoBONY.sql");
	private final String SQL_SELECT_CARGA_OPTIMUS = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectCargaOptimus.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectInstrumentos.sql");
	private final String SQL_SELECT_CUENTAS_BOVEDA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectCuentasBoveda.sql");
	private final String SQL_INSERT_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_InsertCuadraturaCustodia.sql");
	private final String SQL_UPDATE_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_UpdateCuadraturaCustodia.sql");
	
	public static class ArchivoBONY{
		public String FechaCarga;
		public Long AccountNumber = 0L;
		public String AccountName = "";
		public String AccountGroup = "";
		public String ReportingCurrencyCode = "";
		public String ReportingCurrencyName = "";
		public String SecurityShortDescription = "";
		public String SecurityDescriptionOne = "";
		public String SecurityDescriptionTwo = "";
		public String SecurityDescriptionThree = "";
		public String SecurityDescriptionFour = "";
		public String SecurityDescriptionFive = "";
		public String LocationCode = "";
		public String LocationName = "";
		public String CountryIncorporationCode = "";
		public String CountryIncorporationName = "";
		public String LocalCurrencyCode = "";
		public String LocalCurrencyName = "";
		public String AssetCategoryCode = "";
		public String AssetCategoryName = "";
		public String AssetTypeCode = "";
		public String AssetTypeName = "";
		public String PriceDate = "0001-01-01";
		public String MaturityDate = "0001-01-01";
		public BigDecimal CouponRate = BigDecimal.ZERO;
		public BigDecimal ExchangeRate = BigDecimal.ZERO;
		public String AmortizedFace = "";
		public BigDecimal Units = BigDecimal.ZERO;
		public BigDecimal MarketValueReportingCurrency = BigDecimal.ZERO;
		public BigDecimal AccruedInterestReportingCurrency = BigDecimal.ZERO;
		public BigDecimal AccruedValueReportingCurrency = BigDecimal.ZERO;
		public BigDecimal LocalPrice = BigDecimal.ZERO;
		public BigDecimal MarketValueLocal = BigDecimal.ZERO;
		public BigDecimal AccruedInterestLocal = BigDecimal.ZERO;
		public BigDecimal AccruedMarketValueLocal = BigDecimal.ZERO;
		public String CashInvestmentVehicle = "";
		public String CusipCins = "";
		public String ISIN = "";
		public String TICKER = "";
		public String QUICK = "";
		public String SEDOL = "";
		public String VALOREN = "";
		public String SICOVAM = "";
		public String WPK = "";
		public String Basis = "";
		public String AsOfDate = "0001-01-01";
		public String MoodysRating = "";
		public String SPRating = "";
		public String ReportRunDate = "0001-01-01";
		public String ReportRunTime = "";
		public String PreviousFactorRecordDate = "";
		public String PreviousFactorPayableDate = "";
		public String PreviousFactor = "";
		public String CurrentFactorRecordDate = "";
		public String CurrentFactorPayableDate = "";
		public String CurrentFactor = "";
		public String AccountBaseCurrencyCode = "";
		public String AccountBaseCurrencyName = "";
		public String AccruedIncome = "";
		public String AccruedInterestCurrencyCode = "";
		public String AccruedInterestCurrencyName = "";
		public String IssueCurrencyCode = "";
		public String IssueCurrencyName = "";
		public String IssueDate = "";
		public String PendingDeliverShares = "";
		public String PendingReceiveShares = "";
		public String PriceCurrencyCode = "";
		public String PriceCurrencyName = "";
		public BigDecimal PrincipalAmount = BigDecimal.ZERO;
		public String PrincipalCurrencyCode = "";
		public String PrincipalCurrencyName = "";
		public String RegistrationCode = "";
		public String RegistrationName = "";
		public String SecurityStatusCode = "";
		public String SecurityStatusName = "";
		public BigDecimal StrikePrice = BigDecimal.ZERO;
		public BigDecimal TradedShares = BigDecimal.ZERO;
		public Long AccountGSP = 0L;
		public Long ExtendedAccount = 0L;
		public String IASAccountNumber = "";
		public String CID = "";
		public Instrumento Instrumento;
		public CuentaBoveda CuentaBoveda;
		public String IdEstado = "";
		public String Resultado = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumento{
		public String ISIN;
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaBoveda{
		public Long IdCuentaCustodioPortafolio;
		public String Codigo;
		public Long IdPortafolio;
		public String RutBeneficiario;
		public String NombreBeneficiario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CustodiaOptimus{
		public Long IdCuadraturaCustodia;
		public String FechaCuadraturaCustodia;
		public Long IdInstrumento;
		public String IdBoveda;
		public String CuentaBoveda;
		public BigDecimal CantidadCustodia;
		public BigDecimal CantidadSimultaneaCompra;
		public BigDecimal CantidadSimultaneaVenta;
		public BigDecimal CantidadPrestamo;
		public BigDecimal CantidadEmprestito;
		public BigDecimal CantidadGarantia;
		public BigDecimal CantidadTransitoIngreso;
		public BigDecimal CantidadTransitoEgreso;
		public BigDecimal CantidadTotal;
		public BigDecimal MontoATasaMercadoActual;
		public BigDecimal PosicionBoveda;
		public BigDecimal PosicionBovedaValorizadaParaAFP;
		public BigDecimal PosicionBloqueada;
		public BigDecimal PosicionBloqueadaValorizadaParaAFP;
		public BigDecimal PosicionReventa;
		public BigDecimal PosicionRecompra;
		public BigDecimal PosicionTotal;
		public BigDecimal PosicionSimultanea;
		public BigDecimal Diferencia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PosicionCuadratura{
		public Long IdCuadraturaCustodia;
		public Long IdInstrumento;
		public BigDecimal PosicionBoveda = BigDecimal.ZERO;
		public BigDecimal PosicionBovedaValorizadaParaAFP = BigDecimal.ZERO;
		public BigDecimal PosicionBloqueada = BigDecimal.ZERO;
		public BigDecimal PosicionBloqueadaValorizadaParaAFP = BigDecimal.ZERO;
		public BigDecimal PosicionReventa = BigDecimal.ZERO;
		public BigDecimal PosicionRecompra = BigDecimal.ZERO;
		public BigDecimal PosicionTotal = BigDecimal.ZERO;
		public BigDecimal PosicionSimultanea = BigDecimal.ZERO;
		public BigDecimal Diferencia = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCustodia{
		public String FechaCuadraturaCustodia;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdBoveda;
		public String CuentaBoveda;
		public BigDecimal PosicionBoveda = BigDecimal.ZERO;
		public BigDecimal PosicionBloqueada = BigDecimal.ZERO;
		public BigDecimal PosicionTotal = BigDecimal.ZERO;
		public BigDecimal PosicionReventa = BigDecimal.ZERO;
		public BigDecimal PosicionRecompra = BigDecimal.ZERO;
		public BigDecimal PosicionSimultanea = BigDecimal.ZERO;
		public String RutBeneficiario;
		public String NombreBeneficiario;
		public Long IdUsuario;
		public BigDecimal Diferencia = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCargaArchivoBONY{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoBONY{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoBONY in=gson.fromJson(jin.toString() ,InCargaArchivoBONY.class);
		OutCargaArchivoBONY out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoBONY logica(Connection con, InCargaArchivoBONY entrada) throws Exception {
		OutCargaArchivoBONY out = new OutCargaArchivoBONY();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivoLog="cus/CARGA_ARCHIVO_BONY_"+fecha+".log";
		out.p_out_RutaArchivoLog=entrada.p_Ruta+"/"+out.p_out_NombreArchivoLog;
		File archivoLog = new File(out.p_out_RutaArchivoLog);
		File folder = new File(entrada.p_Ruta + "/cus");
		if(!folder.exists()){
			folder.mkdirs();
		}
		BufferedWriter bwArchivoLog;
		bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		
		try{
			SQL sql=new SQL();
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo.getBytes("utf-8")));
			String[] registros = archivoDecoded.split(System.getProperty("line.separator"));
	        SimpleDateFormat formatoArchivo = new SimpleDateFormat("dd/MM/yyyy");
	        SimpleDateFormat SPformatoArchivo = new SimpleDateFormat("yyyy-MM-dd");
	        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	        List<ArchivoBONY> registrosBONY = new ArrayList<ArchivoBONY>();
	        
	        List<String> Isins = new ArrayList<String>();
			List<String> codigos = new ArrayList<String>();
			List<String> bovedas = new ArrayList<String>();
			bovedas.add("BNY");
			
			for(String registro : registros){
				registro = registro.replace("\n", "").replace("\r", "");
				if(!registro.isEmpty()){
					numeroRegistros++;
					ArchivoBONY registroBONY = new ArchivoBONY();
					String[] elemento = registro.split(",");
					try{
						registroBONY.FechaCarga = fechaSistema;
						registroBONY.AccountNumber = elemento[0].isEmpty() ? 0L : new Long(elemento[0]);
						registroBONY.AccountName = elemento[1];
						registroBONY.AccountGroup = elemento[2];
						registroBONY.ReportingCurrencyCode = elemento[3];
						registroBONY.ReportingCurrencyName = elemento[4];
						registroBONY.SecurityShortDescription = elemento[5];
						registroBONY.SecurityDescriptionOne = elemento[6];
						registroBONY.SecurityDescriptionTwo = elemento[7];
						registroBONY.SecurityDescriptionThree = elemento[8];
						registroBONY.SecurityDescriptionFour = elemento[9];
						registroBONY.SecurityDescriptionFive = elemento[10];
						registroBONY.LocationCode = elemento[11];
						registroBONY.LocationName = elemento[12];
						registroBONY.CountryIncorporationCode = elemento[13];
						registroBONY.CountryIncorporationName = elemento[14];
						registroBONY.LocalCurrencyCode = elemento[15];
						registroBONY.LocalCurrencyName = elemento[16];
						registroBONY.AssetCategoryCode = elemento[17];
						registroBONY.AssetCategoryName = elemento[18];
						registroBONY.AssetTypeCode = elemento[19];
						registroBONY.AssetTypeName = elemento[20];
						String priceDate = elemento[21];
						registroBONY.PriceDate = priceDate.isEmpty() ? "0001-01-01" : SPformatoArchivo.format(formatoArchivo.parse(priceDate.replace('-', '/')));
						String maturityDate = elemento[22];
						registroBONY.MaturityDate = maturityDate.isEmpty() ? "0001-01-01" : SPformatoArchivo.format(formatoArchivo.parse(maturityDate.replace('-', '/')));
						registroBONY.CouponRate = elemento[23].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[23]);
						registroBONY.ExchangeRate = elemento[24].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[24]);
						registroBONY.AmortizedFace = elemento[25];
						registroBONY.Units = elemento[26].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[26]);
						registroBONY.MarketValueReportingCurrency = elemento[27].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[27]);
						registroBONY.AccruedInterestReportingCurrency = elemento[28].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[28]);
						registroBONY.AccruedValueReportingCurrency = elemento[29].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[29]);
						registroBONY.LocalPrice = elemento[30].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[30]);
						registroBONY.MarketValueLocal = elemento[31].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[31]);
						registroBONY.AccruedInterestLocal = elemento[32].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[32]);
						registroBONY.AccruedMarketValueLocal = elemento[33].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[33]);
						registroBONY.CashInvestmentVehicle =  elemento[34];
						registroBONY.CusipCins = elemento[35];
						registroBONY.ISIN = elemento[36];
						registroBONY.TICKER = elemento[37];
						registroBONY.QUICK = elemento[38];
						registroBONY.SEDOL = elemento[39];
						registroBONY.VALOREN = elemento[40];
						registroBONY.SICOVAM = elemento[41];
						registroBONY.WPK = elemento[42];
						registroBONY.Basis = elemento[43];
						String asOfDate = elemento[44];
						registroBONY.AsOfDate = asOfDate.isEmpty() ? "0001-01-01" : SPformatoArchivo.format(formatoArchivo.parse(asOfDate.replace('-', '/')));
						registroBONY.MoodysRating = elemento[45];
						registroBONY.SPRating = elemento[46];
						String reportRunDate = elemento[47];
						registroBONY.ReportRunDate = reportRunDate.isEmpty() ? "0001-01-01" : SPformatoArchivo.format(formatoArchivo.parse(reportRunDate.replace('-', '/')));
						registroBONY.ReportRunTime = elemento[48];
						registroBONY.PreviousFactorRecordDate = elemento[49];
						registroBONY.PreviousFactorPayableDate = elemento[50];
						registroBONY.PreviousFactor = elemento[51];
						registroBONY.CurrentFactorRecordDate = elemento[52];
						registroBONY.CurrentFactorPayableDate = elemento[53];
						registroBONY.CurrentFactor = elemento[54];
						registroBONY.AccountBaseCurrencyCode = elemento[55];
						registroBONY.AccountBaseCurrencyName = elemento[56];
						registroBONY.AccruedIncome = elemento[57];
						registroBONY.AccruedInterestCurrencyCode = elemento[58];
						registroBONY.AccruedInterestCurrencyName = elemento[59];
						registroBONY.IssueCurrencyCode = elemento[60];
						registroBONY.IssueCurrencyName = elemento[61];
						registroBONY.IssueDate = elemento[62];
						registroBONY.PendingDeliverShares = elemento[63];
						registroBONY.PendingReceiveShares = elemento[64];
						registroBONY.PriceCurrencyCode = elemento[65];
						registroBONY.PriceCurrencyName = elemento[66];
						registroBONY.PrincipalAmount = elemento[67].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[67]);
						registroBONY.PrincipalCurrencyCode = elemento[68];
						registroBONY.PrincipalCurrencyName = elemento[69];
						registroBONY.RegistrationCode = elemento[70];
						registroBONY.RegistrationName = elemento[71];
						registroBONY.SecurityStatusCode = elemento[72];
						registroBONY.SecurityStatusName = elemento[73];
						registroBONY.StrikePrice = elemento[74].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[74]);
						registroBONY.TradedShares = elemento[75].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[75]);
						registroBONY.AccountGSP = elemento[76].isEmpty() ? 0L : new Long(elemento[76]);
						registroBONY.ExtendedAccount = elemento[77].isEmpty() ? 0L : new Long(elemento[77]);
						registroBONY.IASAccountNumber = elemento[78];
						registroBONY.CID = elemento[79];
						registroBONY.IdEstado = "PEN";
						registroBONY.Resultado = "PEN";
						if(!Isins.contains(registroBONY.ISIN)) Isins.add(registroBONY.ISIN);
						if(!codigos.contains(registroBONY.AccountGSP.toString())) codigos.add(registroBONY.AccountGSP.toString());
						registrosBONY.add(registroBONY);
					}catch(Exception e){
						log.write("ERROR",e);
						numeroRegistrosErroneos++;
						registroBONY.IdEstado = "ERR";
						registroBONY.Resultado = "Formato incorrecto";
						bwArchivoLog.write("Error al parsear el registro:" + registro);
						bwArchivoLog.newLine();
					}
				}
			}
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("Fecha", fechaSistema);
			params.put("IdBoveda", bovedas);
			List<CustodiaOptimus> custodiasOptimus = sql.query(con, SQL_SELECT_CARGA_OPTIMUS, params, CustodiaOptimus.class);
			
			params.clear();
			params.put("ISIN",Isins);
			List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumento.class);
			
			params.clear();
			params.put("IdBoveda",bovedas);
			params.put("Codigos",codigos);
			List<CuentaBoveda> codigosBoveda = sql.query(con, SQL_SELECT_CUENTAS_BOVEDA, params, CuentaBoveda.class);
			
			List<PosicionCuadratura> posicionesCuadratura = new ArrayList<PosicionCuadratura>();
			List<CuadraturaCustodia> cuadraturasCustodia = new ArrayList<CuadraturaCustodia>();
			
			for(ArchivoBONY registroBONY : registrosBONY){
				try{
					for(Instrumento instrumento : instrumentos){
						if(instrumento.ISIN.equals(registroBONY.ISIN)){
							registroBONY.Instrumento = instrumento;
							break;
						}
					}
					
					for(CuentaBoveda codigoBoveda : codigosBoveda){
						if(codigoBoveda.Codigo.equalsIgnoreCase(registroBONY.AccountGSP.toString())){
							registroBONY.CuentaBoveda = codigoBoveda;
							break;
						}
					}
					
					if(registroBONY.Instrumento == null) throw new Exception("Instrumento no existe " + registroBONY.ISIN);
					
					PosicionCuadratura posicionCuadratura = new PosicionCuadratura();
					for(CustodiaOptimus custodiaOptimus : custodiasOptimus){
						if(!custodiaOptimus.CuentaBoveda.isEmpty() 
								&& registroBONY.AccountGSP.compareTo(new Long(custodiaOptimus.CuentaBoveda)) == 0 
								&& registroBONY.Instrumento.IdInstrumento.compareTo(custodiaOptimus.IdInstrumento) == 0){
							posicionCuadratura.IdCuadraturaCustodia = custodiaOptimus.IdCuadraturaCustodia;
							posicionCuadratura.IdInstrumento = custodiaOptimus.IdInstrumento;
							posicionCuadratura.PosicionBoveda = custodiaOptimus.PosicionBoveda.add(registroBONY.Units,MathContext.DECIMAL128);
							posicionCuadratura.PosicionBloqueada = BigDecimal.ZERO;
							posicionCuadratura.PosicionTotal = custodiaOptimus.PosicionTotal.add(registroBONY.Units,MathContext.DECIMAL128);
							posicionCuadratura.Diferencia = custodiaOptimus.CantidadTotal.subtract(posicionCuadratura.PosicionTotal,MathContext.DECIMAL128).negate();
							posicionesCuadratura.add(posicionCuadratura);
							break;
						}
					}
					
					if(posicionCuadratura.IdCuadraturaCustodia == null){
						CuadraturaCustodia cuadraturaCustodia = new CuadraturaCustodia();
						cuadraturaCustodia.FechaCuadraturaCustodia = fechaSistema;
						cuadraturaCustodia.IdClasificacionTipoInstrumento = registroBONY.Instrumento.IdClasificacionTipoInstrumento;
						cuadraturaCustodia.IdTipoInstrumento = registroBONY.Instrumento.IdTipoInstrumento;
						cuadraturaCustodia.Nemotecnico = registroBONY.Instrumento.Nemotecnico;
						cuadraturaCustodia.IdInstrumento = registroBONY.Instrumento.IdInstrumento;
						cuadraturaCustodia.CuentaBoveda = registroBONY.AccountGSP.toString();
						cuadraturaCustodia.IdBoveda = "BNY";
						cuadraturaCustodia.PosicionBoveda = registroBONY.Units;
						cuadraturaCustodia.PosicionBloqueada = BigDecimal.ZERO;
						
						cuadraturaCustodia.PosicionTotal = registroBONY.Units;
						cuadraturaCustodia.RutBeneficiario = registroBONY.CuentaBoveda != null ? registroBONY.CuentaBoveda.RutBeneficiario : "";
						cuadraturaCustodia.NombreBeneficiario = registroBONY.CuentaBoveda != null ? registroBONY.CuentaBoveda.NombreBeneficiario : "";
						cuadraturaCustodia.Diferencia = registroBONY.Units;
						cuadraturaCustodia.IdUsuario = entrada.p_IdUsuario;
						cuadraturasCustodia.add(cuadraturaCustodia);
					}
					registroBONY.IdEstado = "OK";
					registroBONY.Resultado = "OK";
					numeroRegistrosCorrecto++;
				}catch(Exception e){
					registroBONY.IdEstado = "ERR";
					registroBONY.Resultado = "Error al validar registro";
					bwArchivoLog.write("ERROR" + e.getMessage());
					numeroRegistrosErroneos++;
					log.write("ERROR", e);
				}
			}
			
			sql.batch(con, SQL_INSERT_CARGA_ARCHIVO_BONY, registrosBONY, ArchivoBONY.class);
			
			if(posicionesCuadratura.size() > 0) sql.batch(con, SQL_UPDATE_CUADRATURA_CUSTODIA, posicionesCuadratura, PosicionCuadratura.class);
			
			if(cuadraturasCustodia.size() > 0) sql.batch(con, SQL_INSERT_CUADRATURA_CUSTODIA, cuadraturasCustodia, CuadraturaCustodia.class);
			
		}catch(Exception e){
			bwArchivoLog.write("ERROR" + e.getMessage());
			numeroRegistrosErroneos++;
			log.write("ERROR", e);	
		}finally{
			if(bwArchivoLog != null){
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
				
				bwArchivoLog.close();
			}
		}
		
		log.writeAsJson("SALIDA",out);
		return out;
		
	}
	
}
