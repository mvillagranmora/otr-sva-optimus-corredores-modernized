package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.interfaces.GenerarInterfazICU.EntradaGenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.SalidaGenerarInterfazICU;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazC08CarteraIntermediada implements JavaCall {
	// Interfaz C08 - Cartera Intermediada
	// Servicio No. 700034
	
	private final String SQL_SELECT_CARTERA_INTERMEDIADA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08CarteraIntermediada_SelectInformacionCarteras.sql");
	private final String SQL_SELECT_CARTERA_INTERMEDIADA_HIS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08CarteraIntermediadaHIS_SelectInformacionCarterasHIS.sql");
	private final String SQL_SELECT_SALDO_CUENTAS_CONTABLES= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectSaldoCuentaContable.sql");
	private BigDecimal totalCompraPactos = BigDecimal.ZERO;
	private BigDecimal totalVentaPactos = BigDecimal.ZERO;
	private BigDecimal totalSimultaneas = BigDecimal.ZERO;
	
	public static class GenerarInterfazC08CBI_Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
		public Boolean  p_Generar_Interfaz = true;
		public Boolean  p_EsTodos = false;
	}
	
	
 public static class GenerarInterfazC08CBI_Salida {
		
		public Long                 p_out_ErroresContador = 0L;
		public String 				p_out_NombreArchivoLog;
		public String 	 			p_out_RutaArchivoLog;
		public String				p_out_NombreArchivo;
		public String				p_out_RutaArchivo; 
		public String 				p_out_Comentario;
		public List<String> 		p_out_ArchivoSecundario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatoCorredor {
		public String 	Nombre;
		public String 	Rut;
	}
		
	public static class InformacionCarterasIntermediadas {
		
		public Long IdOperacion; 
		public String IdTipoOperacion; 
		public String Nemotecnico;
		public String IdTipoReajuste;
		public Timestamp FechaOperacion;
		public Timestamp FechaVencimiento;
		public BigDecimal IngMontoValorizadoCLP;
		public BigDecimal IngMontoValorizado;
		public Long PlazoEnDias;
		public BigDecimal Tasa;
		public BigDecimal TasaMercado;
		public BigDecimal CambioMercado;
		public String MonedaPago;
		public String NemoSVS;
		
		public BigDecimal ValorPresente;
		//public BigDecimal ValorPresentePesos;
		public Timestamp FechaInicio;
		public BigDecimal TasaOperacion;
		public BigDecimal Cantidad;
		public String TipoOperacion;
	}
	
	public static class SaldoCuentasContablesCBI{
		public String CUENTA;
		public BigDecimal SALDO;
	}
	
	
	public GenerarInterfazC08CBI_Salida logica(Connection con, GenerarInterfazC08CBI_Entrada entrada) throws Exception {
		GenerarInterfazC08CBI_Salida salida = new GenerarInterfazC08CBI_Salida();

				
        // Formatos de fecha
        SimpleDateFormat sdfyyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");// Formato de entrada        
        SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("yyMMdd"); 		// Nombre de los archivos YYDDMM     
        SimpleDateFormat sdddMMYYY = new SimpleDateFormat("dd/MM/yy");
        
        
        Date hoy = new Date();
		if (entrada.p_Fecha == null) {
			entrada.p_Fecha = sdfyyyy_MM_dd.format(hoy);
		}

		// Asignacion de formatos en Fechas
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		
		String fechayymmdd = sdfyyMMdd.format(pFecha);
		String fechaddmmyy = sdddMMYYY.format(pFecha);
	
		// Configuracion para salida de archivo      
		salida.p_out_NombreArchivo = "C08CBI" + fechayymmdd + ".txt";
		salida.p_out_NombreArchivoLog = "C08ICU" + fechayymmdd + ".log";		
		salida.p_out_RutaArchivo = entrada.p_Ruta + "/" + salida.p_out_NombreArchivo;
		salida.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + salida.p_out_NombreArchivoLog;
		
		SQL sql = new SQL();	
		
		// Busca la informacion de las carteras intermediadas
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Fecha", entrada.p_Fecha);
		
		//Convierte la fecha recibida por parametro en Date, obtiene la fecha actual del sistema y la convierte en Date
		Date FechaEntrada = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		Date FechaSistema = new SimpleDateFormat("yyyy-MM-dd").parse(ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		List<InformacionCarterasIntermediadas> carteras = null;
		try {
			//Valida si la fecha ingresada por parametro es menor a la fecha del sistema.  Si es menor, consulta a la historica.
			if(FechaEntrada.after(FechaSistema)){
				//Fecha mayor, se consulta al dia
			    carteras = sql.query(con, SQL_SELECT_CARTERA_INTERMEDIADA, params, InformacionCarterasIntermediadas.class);
			}else if(FechaEntrada.equals(FechaSistema)){
				//Fecha igual, se consulta al dia
				carteras = sql.query(con, SQL_SELECT_CARTERA_INTERMEDIADA, params, InformacionCarterasIntermediadas.class);		
			}else{
				//Fecha menor, se consulta Historica
				carteras = sql.query(con, SQL_SELECT_CARTERA_INTERMEDIADA_HIS, params, InformacionCarterasIntermediadas.class);				
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		//List<InformacionCarterasIntermediadas> carteras = sql.query(con, SQL_SELECT_CARTERA_INTERMEDIADA, params, InformacionCarterasIntermediadas.class);
		
		List<String> cuentas= new ArrayList<>();
		cuentas.add("210202000000");
		cuentas.add("110401000000");
		cuentas.add("110201020000");
		
		params = new HashMap<String,Object>();
		params.put("Cuentas",cuentas);
		
		List<SaldoCuentasContablesCBI> saldoCuentasContables = sql.query(con, SQL_SELECT_SALDO_CUENTAS_CONTABLES, params, SaldoCuentasContablesCBI.class);
		
		
		//Se genera archivo Informativo Seccion Cartera Intermediada
		
		imprimeInterfazCBI(carteras, salida.p_out_RutaArchivo,entrada.p_Generar_Interfaz, entrada);
		
		//imprimeInformeInterfazCBI(con,saldoCuentasContables, salida.P_OUT_RUTA_ARCHIVO_LOG, true, fechaddmmyy, entrada, salida);
		salida = imprimeInformeInterfazCBI(con,saldoCuentasContables, salida.p_out_RutaArchivoLog, fechaddmmyy, entrada, salida);
		
		StringBuilder lineaArchivoICU = new StringBuilder("");
		lineaArchivoICU.append("Cartera Intermediada");
		
		return salida;
	}

	
	
	public void imprimeInterfazCBI(List<InformacionCarterasIntermediadas> carteras,String rutaArchivo, Boolean imprimeInterfaz, GenerarInterfazC08CBI_Entrada entrada) throws IOException, ParseException {
		//GenerarInterfazC08CBI_Entrada entrada
		BufferedWriter bwArchivoInterfazCBI = null;
		
		if(imprimeInterfaz) {
			File archivoCBI = new File(rutaArchivo);
			bwArchivoInterfazCBI = new BufferedWriter(new FileWriter(archivoCBI));
		}
		
		try {
			for (InformacionCarterasIntermediadas cartera : carteras) {
				//Acumulamos saldos para informe
				if(cartera.IdTipoOperacion.equalsIgnoreCase("COMTER") || cartera.IdTipoOperacion.equalsIgnoreCase("COMPAC") ) {
					totalCompraPactos =  totalCompraPactos.add(cartera.IngMontoValorizado);
				}else if(cartera.IdTipoOperacion.equalsIgnoreCase("VENTER") || cartera.IdTipoOperacion.equalsIgnoreCase("VENPAC") ) {
					totalVentaPactos = totalVentaPactos.add(cartera.IngMontoValorizado);
				}else {
					totalSimultaneas = totalSimultaneas.add(cartera.IngMontoValorizado);
				}			    
				
				if (imprimeInterfaz) {
					String ValorPresenteCLP ;
					if (!cartera.IdTipoReajuste.equalsIgnoreCase("CLP")) {
							ValorPresenteCLP = transformaNumeroToString(cartera.IngMontoValorizado.divide(cartera.CambioMercado, 2, RoundingMode.HALF_UP) ,12,4,'0',false);
					}else {	ValorPresenteCLP = transformaNumeroToString(cartera.IngMontoValorizado,12,4,'0',false) ;									}
					//String valorPresente = transformaNumeroToString(cartera.IngMontoValorizado,12,4,'0',false);
					String tipoMoneda 		= obtieneTipoMoneda(cartera.IdTipoReajuste,cartera.MonedaPago); 
					String banda 			= obtieneBanda(obtenerDiferenciaDias(cartera.FechaVencimiento, entrada));
					String fechaVencimiento = transformaFecha(cartera.FechaVencimiento);
					String tasaMercado 		= transformaNumeroToString(cartera.TasaMercado,4,4,'0',false);
					String tasaOperacion 	= transformaNumeroToString(cartera.Tasa, 4,4,'0',false);
					String tipoOperacion 	= obtieneTipoOperacion(cartera.IdTipoOperacion);
					String valorPresenteCampo3 = "";
					String valorPresenteCampo7 = "";
					BigDecimal CalculoFormula;
					String valorPste = "";
					
 					
					 
			        String C08CBIVALORPTEPESOS  = ValorPresenteCLP;

  				            
			        
			        C08CBIVALORPTEPESOS =transformaNumeroToString(cartera.IngMontoValorizadoCLP,12,4,'0',false)  ;
			        
			        
			        
			       
					//Orden de impresion
					StringBuilder lineaInterfazCBI = new StringBuilder("");
					lineaInterfazCBI.append("0"); 																								//C08CBI-REGCERO
					lineaInterfazCBI.append(StringUtils.rightPad(cartera.Nemotecnico, 10,' '));													//C08CBI-NEMO	
					
					if(cartera.IdTipoOperacion.equals("VENPAC") || cartera.IdTipoOperacion.equals("COMPAC")) {
						lineaInterfazCBI.append( transformaNumeroToString(cartera.IngMontoValorizadoCLP,12,4,'0',false) );																			//C08CBI-VALOR-PTE-PESOS PACTOS
					}else {																				
						CalculoFormula =  valorPresenteSimultanea(cartera.IngMontoValorizado, cartera.Cantidad, cartera.Tasa, cartera.FechaInicio, entrada);
						lineaInterfazCBI.append(valorPresenteCampo3 = transformaNumeroToString(CalculoFormula,12,4,'0',false));					//C08CBI-VALOR-PTE-PESOS SIMULTANEAS				
						valorPresenteCampo7 = valorPresenteCampo3;
					}	
					lineaInterfazCBI.append(tipoMoneda); 																						//C08CBI-TIPO-MONEDA
					lineaInterfazCBI.append(banda);																								//C08CBI-BANDA
					lineaInterfazCBI.append(fechaVencimiento);																					//C08CBI-FECHA-VCTO
					if(cartera.IdTipoOperacion.equals("VENPAC") || cartera.IdTipoOperacion.equals("COMPAC")) {																										
						lineaInterfazCBI.append(valorPresenteCampo7 = transformaNumeroToString(cartera.IngMontoValorizado,12,4,'0',false)); 	//C08CBI-VALOR-PTE PACTOS
					}else {																									
						//lineaInterfazCBI.append(valorPresente = valorPresenteSimultanea(cartera.IngMontoValorizado, cartera.Cantidad, cartera.Tasa, cartera.FechaInicio, entrada));
						lineaInterfazCBI.append(valorPresenteCampo7);																			//C08CBI-VALOR-PTE SIMULTANEAS
					}
					lineaInterfazCBI.append(tasaMercado);																						//C08CBI-TASA-MERCADO
					lineaInterfazCBI.append(tasaOperacion);																						//C08CBI-TASA-OPERACION
					lineaInterfazCBI.append(tipoOperacion);																						//C08CBI-TIPO-OPERACION
					//lineaInterfazCBI.append(StringUtils.rightPad(cartera.NemoSVS, 10,' '));	
					
					bwArchivoInterfazCBI.write(lineaInterfazCBI.toString());
					bwArchivoInterfazCBI.write("\r\n");
				}
				
			}
			
			if(bwArchivoInterfazCBI !=null){
				bwArchivoInterfazCBI.close();
			}
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }finally {
			if(bwArchivoInterfazCBI !=null){
				bwArchivoInterfazCBI.close();
			}	
		}
		
	}
	
	
	public GenerarInterfazC08CBI_Salida imprimeInformeInterfazCBI(Connection con, List<SaldoCuentasContablesCBI> saldoCuentasContables,String rutaArchivo, String fecha,
			GenerarInterfazC08CBI_Entrada entrada, GenerarInterfazC08CBI_Salida out) throws Exception {
		
		List<String> archivoSecundario = new ArrayList<>();
		GenerarInterfazICU interfazICU = new GenerarInterfazICU();
		EntradaGenerarInterfazICU entradaICU = new EntradaGenerarInterfazICU();
		SalidaGenerarInterfazICU salidaICU = new SalidaGenerarInterfazICU();
		
		
		BigDecimal saldoCuentaContableCompraPactos 	= obtieneSaldoCuentaContable(saldoCuentasContables, "210202000000").abs();
		BigDecimal saldoCuentaContableVentaPactos 	= obtieneSaldoCuentaContable(saldoCuentasContables, "110401000000").abs();
		BigDecimal saldoCuentaContableSimultaneas 	= obtieneSaldoCuentaContable(saldoCuentasContables, "110201020000").abs();
		
		StringBuilder lineaInformeInterfazCBI = new StringBuilder("");
		
	
		lineaInformeInterfazCBI = new StringBuilder(""); // TOTAL - ABSOLUTO DEL SALDO
		lineaInformeInterfazCBI.append(StringUtils.rightPad("    Compras con Pacto", 28,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(   transformaNumeroToStringLog(totalCompraPactos), 27,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad("21.02.02.000.000", 18,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(saldoCuentaContableCompraPactos), 25,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(totalCompraPactos.subtract(saldoCuentaContableCompraPactos)), 25,' '));
		archivoSecundario.add(lineaInformeInterfazCBI.toString());
		
		lineaInformeInterfazCBI = new StringBuilder("");
		lineaInformeInterfazCBI.append(StringUtils.rightPad("    Ventas con Pacto", 28,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(totalVentaPactos), 27,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad("11.04.01.000.000", 18,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(saldoCuentaContableVentaPactos), 25,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(totalVentaPactos.subtract(saldoCuentaContableVentaPactos)), 25,' '));
		archivoSecundario.add(lineaInformeInterfazCBI.toString());
		
		lineaInformeInterfazCBI = new StringBuilder("");
		lineaInformeInterfazCBI.append(StringUtils.rightPad("    Simultaneas", 28,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(totalSimultaneas), 27,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad("11.02.01.020.000", 18,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(saldoCuentaContableSimultaneas), 25,' '));
		lineaInformeInterfazCBI.append(StringUtils.leftPad(transformaNumeroToStringLog(totalSimultaneas.subtract(saldoCuentaContableSimultaneas)), 25,' '));
		
		archivoSecundario.add(lineaInformeInterfazCBI.toString());
			
		if(entrada.p_Generar_Interfaz && !entrada.p_EsTodos) {
			entradaICU.p_ArchivoSecundario = archivoSecundario;
			entradaICU.p_Fecha = entrada.p_Fecha;
			entradaICU.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaICU.p_IdModulo = entrada.p_IdModulo;
			entradaICU.p_IdUsuario = entrada.p_IdUsuario;
			entradaICU.p_Llamado = "C08-CBI";
			entradaICU.p_Ruta = entrada.p_Ruta;
			salidaICU = interfazICU.logica(con, entradaICU);
			out.p_out_NombreArchivoLog = salidaICU.p_out_NombreArchivoLog;
			out.p_out_RutaArchivoLog = salidaICU.p_out_RutaArchivoLog;
		}
		out.p_out_ArchivoSecundario = archivoSecundario;
		out.p_out_Comentario = "Archivo generado exitosamente";
		return out;
		
	}
	
	public BigDecimal obtieneSaldoCuentaContable(List<SaldoCuentasContablesCBI> saldoCuentasContables, String cuenta) {
		BigDecimal saldoCta = BigDecimal.ZERO;
		
		for(SaldoCuentasContablesCBI saldo : saldoCuentasContables ) {
			if (saldo.CUENTA!=null && (saldo.CUENTA).equalsIgnoreCase(cuenta))
				saldoCta = saldo.SALDO;
		}
		
		return saldoCta;
	}
	
	public String transformaNumeroToString(BigDecimal monto, int maxLengthEntero, int round, char caracter, Boolean formatoMil ) {
		String nroFormat = "";
 
		boolean esNegativo = (monto.compareTo(BigDecimal.ZERO) < 0);
		
		BigDecimal numero = monto.setScale(round,RoundingMode.HALF_UP);
		String numeroSTR = numero.toPlainString().replace('-', ' ').trim();
		int posDecimal = numeroSTR.indexOf('.');
		String parteEntera = numeroSTR.substring(0,posDecimal);
		
		if (parteEntera.length() > maxLengthEntero) {
			parteEntera = parteEntera.substring(0,maxLengthEntero);
		}
		
		String parteDecimal = numeroSTR.substring(posDecimal+1,numeroSTR.length());
		String signo = " ";
		
		if (esNegativo) {
			signo = "-";
		}
		
		if (formatoMil) {
			DecimalFormat formatea = new DecimalFormat("###,###.##");
			Long num = Long.parseLong(parteEntera);
			parteEntera = formatea.format(num);
		}
		
		
		nroFormat = signo + StringUtils.leftPad(parteEntera, maxLengthEntero, caracter) + "," + parteDecimal;
 
		return nroFormat;
	}
	
	
	
	public String transformaNumeroToStringLog(BigDecimal monto) {
		
		boolean esNegativo = (monto.compareTo(BigDecimal.ZERO) < 0);
		String nroFormat = "";
		String strMonto = "";
		
		String signo = " ";
		
		if (esNegativo) {
			signo = "-";
		}
		
		DecimalFormat formatea = new DecimalFormat( "#,##0.00", new DecimalFormatSymbols(new Locale("pt", "CL")));
		strMonto = formatea.format(monto.abs());	
		
		nroFormat = signo + strMonto;
		return nroFormat;
	}

	public String obtieneTipoMoneda(String IdTipoReajuste, String MonedaPago) {
	  String tipoMoneda;
		if (IdTipoReajuste.equalsIgnoreCase("CLP")) {			
			tipoMoneda = "1";
		}else if (IdTipoReajuste.equalsIgnoreCase("UF") ) {
			tipoMoneda = "2";
		}else  {
			tipoMoneda = "3";
		}
	  return tipoMoneda;
	}
	
	public String obtieneBanda(Long plazo) {
		String banda = "31";
		
		if (plazo < 8)
			return " 1";
		
		if(plazo > 8 && plazo < 16)
			return " 5";
		
		if(plazo > 16 && plazo < 31)
			return "10";
		
		if(plazo > 31 && plazo < 61)
			return "15";
		
		if(plazo > 61 && plazo < 91)
			return "20";
		
		if(plazo > 91 && plazo < 181)
			return "25";
		
		if(plazo > 181 && plazo < 361)
			return "30";
		
		return banda;
	}
	
	public String transformaFecha(Timestamp fecha) {
		String fechaSTR="";
		SimpleDateFormat formatFechaSalida = new SimpleDateFormat("dd-MM-yyyy");
		Date fechaDate = new Date(fecha.getTime());
		fechaSTR = formatFechaSalida.format(fechaDate);
		return fechaSTR;		
	}
	
	
	public long obtenerDiferenciaDias(Timestamp fechaVencimiento, GenerarInterfazC08CBI_Entrada entrada) throws ParseException {
		
		String fechaIN = entrada.p_Fecha;
		Timestamp fechaSQL = fechaVencimiento;
		
		//Convierte la fecha de entrada en tipo Date
		Date fechaEntrada = new SimpleDateFormat("yyyy-MM-dd").parse(fechaIN);
		
		//Convierte la fecha de vencimiento obtenida de la consulta sql en tipo Date
		SimpleDateFormat pFecha = new SimpleDateFormat("yyyy-MM-dd");
		String FechaFinal = pFecha.format(fechaSQL);
		Date fechaVencimien = new SimpleDateFormat("yyyy-MM-dd").parse(FechaFinal);
		
		//Calcula la diferencia de dias
		long dif = fechaVencimien.getTime() - fechaEntrada.getTime();
		TimeUnit time = TimeUnit.DAYS; 
		long diferencia = time.convert(dif, TimeUnit.MILLISECONDS);
		
		
		return diferencia;
	}
	
	//Calculo ValorPresente para simultaneas
	public BigDecimal valorPresenteSimultanea(BigDecimal IngMontoValorizado, BigDecimal Cantidad, BigDecimal Tasa, Timestamp fechaInicio, GenerarInterfazC08CBI_Entrada entrada) throws ParseException {
		
		BigDecimal ResultadoFormula = null;
		BigDecimal precioInicial = IngMontoValorizado;
		int var1 = 1;
		String var2 = "3000";
		long tiempoTranscurrido;
	
		
		//Calculo de fechas
		String fechaEntrada = entrada.p_Fecha;
		Timestamp fechaSQL = fechaInicio;
				
		//Convierte la fecha de entrada en tipo Date
		Date fechaConsulta = new SimpleDateFormat("yyyy-MM-dd").parse(fechaEntrada);
				
		//Convierte la fecha de vencimiento obtenida de la consulta sql en tipo Date
		SimpleDateFormat pFecha = new SimpleDateFormat("yyyy-MM-dd");
		String FechaFinal = pFecha.format(fechaSQL);
		Date fechaIni = new SimpleDateFormat("yyyy-MM-dd").parse(FechaFinal);
		
		//Calcula la diferencia de dias Fecha Consulta  (-) Fecha Inicio
		long dif = fechaConsulta.getTime() - fechaIni.getTime();
		TimeUnit time = TimeUnit.DAYS; 
		tiempoTranscurrido = time.convert(dif, TimeUnit.MILLISECONDS);
		
		
		
		//Calculo Formula
		//Tiempo Transcurrido = fecha de consulta - fecha de incio
		//Precio inicial*(1+[(tasa)*(tiempo transcurrido)]/3000)
		//El resultado se redondea a 3 decimales y luego se multiplica por la cantidad y el resultado final va redondeado a 0 decimales.
		BigDecimal divisor = new BigDecimal(var2);
		MathContext mc = new MathContext(5);
		//calculo3 = calculo1.divide(divisor, mc);
		
		//(tasa)*(tiempo transcurrido)]/3000
		BigDecimal valorDiv = Tasa.multiply(BigDecimal.valueOf(tiempoTranscurrido)).divide(divisor, mc);
		
		//(1+[(tasa)*(tiempo transcurrido)]/3000)
		BigDecimal valorSum = valorDiv.add(BigDecimal.valueOf(var1));
		
		//precio inicial*(1+[(tasa)*(tiempo transcurrido)]/3000)
		BigDecimal valorMul = precioInicial.multiply(valorSum);
		
		//se redondea a 3 decimales
		BigDecimal valorRedon =	valorMul.setScale(3, RoundingMode.HALF_UP);
		
		//valor redondeado se multiplica por la cantidad
		BigDecimal valorMulCant = valorRedon.multiply(Cantidad);
		
		//Resultado
		ResultadoFormula = valorMulCant.setScale(0, RoundingMode.HALF_UP);
		
		
		return ResultadoFormula;
	}
	
	
	public String obtieneTipoOperacion(String tipo) {
		String tipoOper = " ";
		
		if(tipo.equalsIgnoreCase("COMTER")) {
			return tipoOper = "C ";
		}

		if(tipo.equalsIgnoreCase("VENTER")) {
			return tipoOper = "V ";
		}
		
		if(tipo.equalsIgnoreCase("COMPAC")) {
			return tipoOper = "C ";
		}
		
		if(tipo.equalsIgnoreCase("VENPAC")) {
			return tipoOper = "V ";
		}
		
		if(tipo.equalsIgnoreCase("COMSIM")) {
			return tipoOper = "V ";
		}
		
		if(tipo.equalsIgnoreCase("VENSIM")) {
			return tipoOper = "V ";
		}
		
		return tipoOper;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		GenerarInterfazC08CBI_Entrada in = gson.fromJson(jin.toString(), GenerarInterfazC08CBI_Entrada.class);
		GenerarInterfazC08CBI_Salida out = logica(conn, in);
		
		//out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection();
			GenerarInterfazC08CBI_Entrada entrada = new GenerarInterfazC08CBI_Entrada();
			entrada.p_IdEmpresa		= new Long(1);
			entrada.p_IdUsuario		= new Long(21);
			entrada.p_Fecha	        = "2022-05-31";
			entrada.p_Ruta			= "C:\\BCS\\logs";
			JsonParser parser 		= new JsonParser();
			JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			GenerarInterfazC08CarteraIntermediada demo = new GenerarInterfazC08CarteraIntermediada();
			System.out.println(demo.call(conn, dataJsonIn).toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");

	}
}


