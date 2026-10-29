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

public class CargaArchivoCavali implements JavaCall{
	
	private final Log log =new Log("CUS.CargaArchivoCavali");
	
	private final String SQL_INSERT_CARGA_ARCHIVO_CAVALI = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCavali_InsertCargaArchivoCavali.sql");
	private final String SQL_SELECT_CARGA_OPTIMUS = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectCargaOptimus.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectInstrumentos.sql");
	private final String SQL_SELECT_CUENTAS_BOVEDA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectCuentasBoveda.sql");
	private final String SQL_INSERT_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_InsertCuadraturaCustodia.sql");
	private final String SQL_UPDATE_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_UpdateCuadraturaCustodia.sql");
	
	private final int largo = 233;
	
	public static class ArchivoCavali{
		public String FechaCarga;
		public Long TipoIntermediario = 0L;
		public Long CodigoIntermediario = 0L;
		public String ISIN = "";
		public String DescripcionValor = "";
		public String CodigoTitular = "";
		public String NombreTitular = "";
		public Long SaldoContable = 0L;
		public Long SaldoDisponible = 0L;
		public Long Prenda = 0L;
		public Long Embargo = 0L;
		public Long BloqueoOtros= 0L;
		public Long CertificadoAcreditacion = 0L;
		public Long SaldoTransito = 0L;
		public Long SaldoMargenGarantia = 0L;
		public Long SaldoComprometidoVenta = 0L;
		public Long SaldoReportante = 0L;
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
	
	public static class InCargaArchivoCavali{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoCavali{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoCavali in=gson.fromJson(jin.toString() ,InCargaArchivoCavali.class);
		OutCargaArchivoCavali out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoCavali logica(Connection con, InCargaArchivoCavali entrada) throws Exception {
		OutCargaArchivoCavali out = new OutCargaArchivoCavali();
		
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivoLog="cus/CARGA_ARCHIVO_CAVALI_"+fecha+".log";
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
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	        List<ArchivoCavali> registrosCavali = new ArrayList<ArchivoCavali>();
			List<String> Isins = new ArrayList<String>();
			List<String> codigos = new ArrayList<String>();
			List<String> bovedas = new ArrayList<String>();
			bovedas.add("CAV");
			for(String registro : registros){
				registro = registro.replace("\n", "").replace("\r", "");
				if(!registro.isEmpty()){
					numeroRegistros++;
					ArchivoCavali registroCavali = new ArchivoCavali();
					try{
						if(registro.length() != largo){
							throw new Exception("Largo incorrecto se esperaba: " + largo + ",se encontro: " + registro.length());
						}
						registroCavali.FechaCarga = fechaSistema;
						registroCavali.TipoIntermediario = new Long(registro.substring(0, 2));
						registroCavali.CodigoIntermediario = new Long(registro.substring(2, 6));
						registroCavali.ISIN = registro.substring(6, 18);
						registroCavali.DescripcionValor = registro.substring(18, 53);
						registroCavali.CodigoTitular = registro.substring(53, 63);
						registroCavali.NombreTitular = registro.substring(63, 113);
						registroCavali.SaldoContable = new Long(registro.substring(113, 125));
						registroCavali.SaldoDisponible = new Long(registro.substring(125, 137));
						registroCavali.Prenda = new Long(registro.substring(137, 149));
						registroCavali.Embargo = new Long(registro.substring(149, 161));
						registroCavali.BloqueoOtros = new Long(registro.substring(161, 173));
						registroCavali.CertificadoAcreditacion = new Long(registro.substring(173, 185));
						registroCavali.SaldoTransito = new Long(registro.substring(185, 197));
						registroCavali.SaldoMargenGarantia = new Long(registro.substring(197, 209));
						registroCavali.SaldoComprometidoVenta = new Long(registro.substring(209, 221));
						registroCavali.SaldoReportante = new Long(registro.substring(221, 233));
						registroCavali.IdEstado = "PEN";
						registroCavali.Resultado = "PEN";
						if(!Isins.contains(registroCavali.ISIN)) Isins.add(registroCavali.ISIN);
						if(!codigos.contains(registroCavali.CodigoTitular)) codigos.add(registroCavali.CodigoTitular);
						registrosCavali.add(registroCavali);
					}catch(Exception e){
						log.write("ERROR",e);
						numeroRegistrosErroneos++;
						registroCavali.IdEstado = "ERR";
						registroCavali.Resultado = "Formato incorrecto";
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
			
			for(ArchivoCavali registroCavali : registrosCavali){
				try{
					for(Instrumento instrumento : instrumentos){
						if(instrumento.ISIN.equals(registroCavali.ISIN)){
							registroCavali.Instrumento = instrumento;
							break;
						}
					}
					
					for(CuentaBoveda codigoBoveda : codigosBoveda){
						if(codigoBoveda.Codigo.equalsIgnoreCase(registroCavali.CodigoTitular)){
							registroCavali.CuentaBoveda = codigoBoveda;
							break;
						}
					}
					
					if(registroCavali.Instrumento == null) throw new Exception("Instrumento no existe " + registroCavali.ISIN);
					
					PosicionCuadratura posicionCuadratura = new PosicionCuadratura();
					for(CustodiaOptimus custodiaOptimus : custodiasOptimus){
						if(!custodiaOptimus.CuentaBoveda.isEmpty() 
								&& new Long(registroCavali.CodigoTitular).compareTo(new Long(custodiaOptimus.CuentaBoveda)) == 0 
								&& registroCavali.Instrumento.IdInstrumento.compareTo(custodiaOptimus.IdInstrumento) == 0){
							posicionCuadratura.IdCuadraturaCustodia = custodiaOptimus.IdCuadraturaCustodia;
							posicionCuadratura.IdInstrumento = custodiaOptimus.IdInstrumento;
							posicionCuadratura.PosicionBoveda = custodiaOptimus.PosicionBoveda.add(new BigDecimal(registroCavali.SaldoDisponible),MathContext.DECIMAL128);
							posicionCuadratura.PosicionBloqueada = custodiaOptimus.PosicionBloqueada
									.add(new BigDecimal(registroCavali.Prenda),MathContext.DECIMAL128)
									.add(new BigDecimal(registroCavali.Embargo),MathContext.DECIMAL128)
									.add(new BigDecimal(registroCavali.BloqueoOtros),MathContext.DECIMAL128)
									.add(new BigDecimal(registroCavali.CertificadoAcreditacion),MathContext.DECIMAL128);
							posicionCuadratura.PosicionTotal = custodiaOptimus.PosicionTotal.add(new BigDecimal(registroCavali.SaldoContable),MathContext.DECIMAL128);
							posicionCuadratura.PosicionSimultanea = custodiaOptimus.PosicionSimultanea.add(new BigDecimal(registroCavali.SaldoReportante),MathContext.DECIMAL128);
							posicionCuadratura.PosicionReventa = custodiaOptimus.PosicionReventa.add(new BigDecimal(registroCavali.SaldoComprometidoVenta),MathContext.DECIMAL128);
							posicionCuadratura.Diferencia = custodiaOptimus.CantidadTotal.subtract(posicionCuadratura.PosicionTotal,MathContext.DECIMAL128).negate();
							posicionesCuadratura.add(posicionCuadratura);
							break;
						}
					}
					
					if(posicionCuadratura.IdCuadraturaCustodia == null){
						CuadraturaCustodia cuadraturaCustodia = new CuadraturaCustodia();
						cuadraturaCustodia.FechaCuadraturaCustodia = fechaSistema;
						cuadraturaCustodia.IdClasificacionTipoInstrumento = registroCavali.Instrumento.IdClasificacionTipoInstrumento;
						cuadraturaCustodia.IdTipoInstrumento = registroCavali.Instrumento.IdTipoInstrumento;
						cuadraturaCustodia.Nemotecnico = registroCavali.Instrumento.Nemotecnico;
						cuadraturaCustodia.IdInstrumento = registroCavali.Instrumento.IdInstrumento;
						cuadraturaCustodia.CuentaBoveda = registroCavali.CodigoTitular;
						cuadraturaCustodia.IdBoveda = "CAV";
						cuadraturaCustodia.PosicionBoveda = new BigDecimal(registroCavali.SaldoDisponible,MathContext.DECIMAL128);
						cuadraturaCustodia.PosicionBloqueada = new BigDecimal(registroCavali.Prenda,MathContext.DECIMAL128)
								.add(new BigDecimal(registroCavali.Embargo),MathContext.DECIMAL128)
								.add(new BigDecimal(registroCavali.BloqueoOtros),MathContext.DECIMAL128)
								.add(new BigDecimal(registroCavali.CertificadoAcreditacion),MathContext.DECIMAL128);
						
						cuadraturaCustodia.PosicionTotal = new BigDecimal(registroCavali.SaldoContable);
						cuadraturaCustodia.PosicionSimultanea = new BigDecimal(registroCavali.SaldoReportante);
						cuadraturaCustodia.PosicionReventa = new BigDecimal(registroCavali.SaldoComprometidoVenta);
						cuadraturaCustodia.RutBeneficiario = registroCavali.CuentaBoveda != null ? registroCavali.CuentaBoveda.RutBeneficiario : "";
						cuadraturaCustodia.NombreBeneficiario = registroCavali.CuentaBoveda != null ? registroCavali.CuentaBoveda.NombreBeneficiario : "";
						cuadraturaCustodia.Diferencia = new BigDecimal(registroCavali.SaldoContable);
						cuadraturaCustodia.IdUsuario = entrada.p_IdUsuario;
						cuadraturasCustodia.add(cuadraturaCustodia);
					}
					registroCavali.IdEstado = "OK";
					registroCavali.Resultado = "OK";
					numeroRegistrosCorrecto++;
				}catch(Exception e){
					registroCavali.IdEstado = "ERR";
					registroCavali.Resultado = "Error al validar registro";
					bwArchivoLog.write("ERROR" + e.getMessage());
					numeroRegistrosErroneos++;
					log.write("ERROR", e);
				}
			}
			
			sql.batch(con, SQL_INSERT_CARGA_ARCHIVO_CAVALI, registrosCavali, ArchivoCavali.class);
			
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
