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

public class CargaArchivoLEK implements JavaCall{
	
	private final Log log =new Log("CUS.CargaArchivoLEK");
	
	private final String SQL_INSERT_CARGA_ARCHIVO_LEK = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoLEK_InsertCargaArchivoLEK.sql");
	private final String SQL_SELECT_CARGA_OPTIMUS = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectCargaOptimus.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectInstrumentos.sql");
	private final String SQL_SELECT_CUENTAS_BOVEDA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_SelectCuentasBoveda.sql");
	private final String SQL_INSERT_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_InsertCuadraturaCustodia.sql");
	private final String SQL_UPDATE_CUADRATURA_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CargaArchivoCuadratura_UpdateCuadraturaCustodia.sql");
	
	
	public static class ArchivoLEK{
		public String FechaCarga;
		public String Boveda = "";
		public String Cuenta = "";
		public String ISIN = "";
		public String Nemonico = "";
		public BigDecimal Cantidad = BigDecimal.ZERO;
		public String IdEstado = "";
		public String Resultado = "";
		public Instrumento Instrumento;
		public CuentaBoveda CuentaBoveda;
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
		public String IdBoveda;
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
	
	public static class InCargaArchivoLEK{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_Archivo;
		public String 		p_Ruta;
	}
	
	public static class OutCargaArchivoLEK{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public Long 	p_out_ErroresContador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargaArchivoLEK in=gson.fromJson(jin.toString() ,InCargaArchivoLEK.class);
		OutCargaArchivoLEK out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCargaArchivoLEK logica(Connection con, InCargaArchivoLEK entrada) throws Exception {
		OutCargaArchivoLEK out = new OutCargaArchivoLEK();
		out.p_out_ErroresContador = 1L;
		long startTime = System.currentTimeMillis();
		SimpleDateFormat fm=new SimpleDateFormat("ddMMyyyyHHmm");
		String fecha = fm.format(new Date());
		int numeroRegistros = 0;
		int numeroRegistrosErroneos = 0;
		int numeroRegistrosCorrecto = 0;
		out.p_out_ErroresContador = 1L;
		out.p_out_NombreArchivoLog="cus/CARGA_ARCHIVO_LEK_"+fecha+".log";
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
			
			String archivoDecoded = new String(Base64.getDecoder().decode(entrada.p_Archivo));
			String[] registros = archivoDecoded.split(System.getProperty("line.separator"));
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	        List<ArchivoLEK> registrosLEK = new ArrayList<ArchivoLEK>();
	        
	        List<String> Isins = new ArrayList<String>();
			List<String> codigos = new ArrayList<String>();
			List<String> bovedas = new ArrayList<String>();
			
			for(String registro : registros){
				registro = registro.replace("\n", "").replace("\r", "");
				if(!registro.isEmpty()){
					numeroRegistros++;
					ArchivoLEK registroLEK= new ArchivoLEK();
					String[] elemento = registro.split(",");
					
					try{
						registroLEK.FechaCarga = fechaSistema;
						registroLEK.Boveda = elemento[0];
						registroLEK.Cuenta = elemento[1];
						registroLEK.ISIN = elemento[2];
						registroLEK.Nemonico = elemento[3];
						registroLEK.Cantidad = elemento[4].isEmpty() ? BigDecimal.ZERO : new BigDecimal(elemento[4]);
						registroLEK.IdEstado = "PEN";
						registroLEK.Resultado = "PEN";
						
						if(!Isins.contains(registroLEK.ISIN)) Isins.add(registroLEK.ISIN);
						if(!codigos.contains(registroLEK.Cuenta)) codigos.add(registroLEK.Cuenta);
						if(!bovedas.contains(registroLEK.Boveda)) codigos.add(registroLEK.Boveda);
						registrosLEK.add(registroLEK);
					}catch(Exception e){
						log.write("ERROR",e);
						numeroRegistrosErroneos++;
						registroLEK.IdEstado = "ERR";
						registroLEK.Resultado = "Formato incorrecto";
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
			
			for(ArchivoLEK registroLEK : registrosLEK){
				try{
					for(Instrumento instrumento : instrumentos){
						if(instrumento.ISIN.equals(registroLEK.ISIN)){
							registroLEK.Instrumento = instrumento;
							break;
						}
					}
					
					for(CuentaBoveda codigoBoveda : codigosBoveda){
						if(codigoBoveda.Codigo.equalsIgnoreCase(registroLEK.Cuenta) && registroLEK.Boveda.equalsIgnoreCase(codigoBoveda.IdBoveda)){
							registroLEK.CuentaBoveda = codigoBoveda;
							break;
						}
					}
					
					if(registroLEK.Instrumento == null) throw new Exception("Instrumento no existe " + registroLEK.ISIN);
					
					PosicionCuadratura posicionCuadratura = new PosicionCuadratura();
					for(CustodiaOptimus custodiaOptimus : custodiasOptimus){
						if(!custodiaOptimus.CuentaBoveda.isEmpty() 
								&& registroLEK.Cuenta.equalsIgnoreCase(custodiaOptimus.CuentaBoveda) 
								&& registroLEK.Instrumento.IdInstrumento.compareTo(custodiaOptimus.IdInstrumento) == 0
								&& registroLEK.Boveda.equalsIgnoreCase(custodiaOptimus.IdBoveda)
								){
							posicionCuadratura.IdCuadraturaCustodia = custodiaOptimus.IdCuadraturaCustodia;
							posicionCuadratura.IdInstrumento = custodiaOptimus.IdInstrumento;
							posicionCuadratura.PosicionBoveda = custodiaOptimus.PosicionBoveda.add(registroLEK.Cantidad,MathContext.DECIMAL128);
							posicionCuadratura.PosicionBloqueada = BigDecimal.ZERO;
							posicionCuadratura.PosicionTotal = custodiaOptimus.PosicionTotal.add(registroLEK.Cantidad,MathContext.DECIMAL128);
							posicionCuadratura.Diferencia = custodiaOptimus.CantidadTotal.subtract(posicionCuadratura.PosicionTotal,MathContext.DECIMAL128).negate();
							posicionesCuadratura.add(posicionCuadratura);
							break;
						}
					}
					
					if(posicionCuadratura.IdCuadraturaCustodia == null){
						CuadraturaCustodia cuadraturaCustodia = new CuadraturaCustodia();
						cuadraturaCustodia.FechaCuadraturaCustodia = fechaSistema;
						cuadraturaCustodia.IdClasificacionTipoInstrumento = registroLEK.Instrumento.IdClasificacionTipoInstrumento;
						cuadraturaCustodia.IdTipoInstrumento = registroLEK.Instrumento.IdTipoInstrumento;
						cuadraturaCustodia.Nemotecnico = registroLEK.Instrumento.Nemotecnico;
						cuadraturaCustodia.IdInstrumento = registroLEK.Instrumento.IdInstrumento;
						cuadraturaCustodia.CuentaBoveda = registroLEK.Cuenta;
						cuadraturaCustodia.IdBoveda = registroLEK.Boveda;
						cuadraturaCustodia.PosicionBoveda = registroLEK.Cantidad;
						cuadraturaCustodia.PosicionBloqueada = BigDecimal.ZERO;
						
						cuadraturaCustodia.PosicionTotal =registroLEK.Cantidad;
						cuadraturaCustodia.RutBeneficiario = registroLEK.CuentaBoveda != null ? registroLEK.CuentaBoveda.RutBeneficiario : "";
						cuadraturaCustodia.NombreBeneficiario = registroLEK.CuentaBoveda != null ? registroLEK.CuentaBoveda.NombreBeneficiario : "";
						cuadraturaCustodia.Diferencia = registroLEK.Cantidad;
						cuadraturaCustodia.IdUsuario = entrada.p_IdUsuario;
						cuadraturasCustodia.add(cuadraturaCustodia);
					}
					registroLEK.IdEstado = "OK";
					registroLEK.Resultado = "OK";
					numeroRegistrosCorrecto++;
				}catch(Exception e){
					registroLEK.IdEstado = "ERR";
					registroLEK.Resultado = "Error al validar registro";
					bwArchivoLog.write("ERROR" + e.getMessage());
					numeroRegistrosErroneos++;
					log.write("ERROR", e);
				}
			}
			
			sql.batch(con, SQL_INSERT_CARGA_ARCHIVO_LEK, registrosLEK, ArchivoLEK.class);
			
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
