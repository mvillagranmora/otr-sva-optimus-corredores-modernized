package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.interfaces.GenerarInterfazC08CarteraPacteada.CarteraPacteada;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.EntradaGenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.SalidaGenerarInterfazICU;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazC08CarteraNormal implements JavaCall {
	private final Log log = new Log("GenerarInterfazC40");
	private final String SQL_CONSULTAR_CORREDOR 				= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08CarteraNormal_SelectCarteraNormal.sql"); 
	private final String SQL_SELECT_HIS_CONSULTAR_CORREDOR 		= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazc08CarteraNormal_HIS_SelectCarteraNormal.sql");
	
	private final String SQL_SELECT_SALDO_CUENTAS_CONTABLES 	= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectSaldoCuentaContable.sql"); 
	
	
	/*******************
	 * clases in/out
	 *******************/

	public static class InGenerarInterfazC08 {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		public String p_Ruta;
		public Boolean p_Interfaz = true; // Parametro para elegir
		public Boolean p_EsTodo = false;
	}

	public static class OutGenerarInterfazC08 {
		public String p_out_NombreArchivoLog;
		public String p_out_RutaArchivoLog;
		public String p_out_NombreArchivo;
		public String p_out_RutaArchivo;
		public Long p_out_ErroresContador;
		public String p_out_Comentario;
		public List<String> p_out_ArchivoSecundario;
	}

	public static class Cartera { 
		public String Nemotecnico;
		public String TipoInstrumento;
		public String TipoMoneda;
		public Long IdCartera;
		public Long FolioCartera;
		public BigDecimal ValorMer;
		public BigDecimal TasaCompra;
		public BigDecimal TasaMercadoActual;
		public BigDecimal ValorPresente;
		public BigDecimal CantidadGarantia;
		public BigDecimal Cantidad;
		public BigDecimal PrecioPromedio;
		public Date FechaVencimiento;
		public String MonedaPago;
		
	}

	public static class LogEjecucion {
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}

	public static class SaldoCuentasContables {
		public String CUENTA;
		public BigDecimal SALDO;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		InGenerarInterfazC08 in = gson.fromJson(jin.toString(), InGenerarInterfazC08.class);
		OutGenerarInterfazC08 out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	/**********************
	 * logica del programa
	 **********************/

	public OutGenerarInterfazC08 logica(Connection con, InGenerarInterfazC08 entrada) throws Exception {
		OutGenerarInterfazC08 out = new OutGenerarInterfazC08();
		// -----------------------------------------------------------------------------------------------------------------------------------
		// Formato de fechas
		SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("yyMMdd"); 		// Nombre de los archivos YYDDMM   
		
		String fechaSistema = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
		Date fechaActual = Fecha.toDate(fechaSistema);
		Boolean Historico = false;
        // valida si la fecha de entrada es nula
		if (entrada.p_Fecha == null || entrada.p_Fecha.equals("")) {	
			entrada.p_Fecha = fechaActual.toString();
		}
		
		if (!fechaActual.toString().equalsIgnoreCase(entrada.p_Fecha)) // si no es igual la fecha de entrada entonces se utiliza para ver el historico
		{	Historico = true ;		}		
		
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);

		Calendar dia = Calendar.getInstance();
		dia.setTime(pFecha);
		BufferedWriter bwArchivo = null;
		
		
		Date FechaYYMMDD = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);		
		String fechayymmdd = sdfyyMMdd.format(FechaYYMMDD);
		
		// -----------------------------------------------------------------------------------------------------------------------------------
		if ((entrada.p_Interfaz) || (entrada.p_EsTodo)) {
			out.p_out_NombreArchivo = "C08CBN" + fechayymmdd + ".txt";
			out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
			out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
			if ((entrada.p_Interfaz) || (entrada.p_EsTodo)) {
				File archivo = new File(out.p_out_RutaArchivo);
				bwArchivo = new BufferedWriter(new FileWriter(archivo));
			}
		}
		try {
			SQL sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FiltroFecha", entrada.p_Fecha);
			
			List<Cartera> cartera;			
			if (Historico) {	cartera = sql.query(con, SQL_SELECT_HIS_CONSULTAR_CORREDOR , 		params,	Cartera.class);  System.out.println("HIS");}	// Consulta Historica
			else {				cartera = sql.query(con, SQL_CONSULTAR_CORREDOR, 					params, Cartera.class);	 System.out.println("NOS");}
			
			List<String> linea2 = new ArrayList<>();
			BigDecimal acumulaInstrumentoPRBC 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoPDBC 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoPRC 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoBR 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoDPF 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoDPR 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoDPRUSD 	= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoLH 		= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoMonetario 	= BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoOtros 		= BigDecimal.ZERO;
			HashMap<String, BigDecimal> saldoCuentas = new HashMap<String, BigDecimal>();
			
			for (Cartera oact : cartera) {
				SimpleDateFormat FormatoInterfaz = new SimpleDateFormat("dd-MM-yyyy");
				SimpleDateFormat Formatocalculodias = new SimpleDateFormat("yyyy-MM-dd");	 
				
				String fechavf = FormatoInterfaz.format(oact.FechaVencimiento);			// fechavencimiento con formato para la interfaz
				String fechav2 = Formatocalculodias.format(oact.FechaVencimiento);		// fechavencimiento con formato para calculo de dias para la banda
				
				LocalDate FechaActual 		= LocalDate.parse(entrada.p_Fecha	, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				LocalDate FechaVencimiento  = LocalDate.parse(fechav2 	 		, DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
				
				String bandaDefinida = AsignaBanda( ChronoUnit.DAYS.between(FechaActual,FechaVencimiento) ); // calculo de la banda segun los dias corridos de la fecha de la interfaz a la fecha de vencimiento
				if ((entrada.p_Interfaz) || (entrada.p_EsTodo)) {
					StringBuilder linea = new StringBuilder();
					String nemotecnico = (oact.Nemotecnico.length() < 10)
							? StringUtils.rightPad(String.format("%10s", oact.Nemotecnico), 10, ' ')
							: oact.Nemotecnico.substring(0, 10);
					// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
					// A R C H I V O P R I N C I P A L
					// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
					linea.append("0");																																	// campo-01 - Reg 0 
					if (oact.Nemotecnico.length() < 10) {	linea.append(StringUtils.rightPad(oact.Nemotecnico, 10, ' '));	} 
					else {								linea.append(oact.Nemotecnico.substring(0, 10));					} 											// campo-02 - Nemotecnico
					System.out.println(oact.ValorPresente);
					linea.append((oact.ValorMer.signum() == -1) ? "-" : " "); 																							//
					linea.append(String.format("%17s", String.format("%17.4f", oact.ValorMer).replace(" ", "0")).toString().replace(".", ",")); 									// campo-03 - Valor  mercado
					linea.append(calcularMoneda(oact)); 																												// campo-04 - Tipo Moneda 1. Peso 2. Moneda Nacional Reajustable 3.  Moneda Extranjera
					linea.append(bandaDefinida); 																														// campo-05 - Plazo o banda entre fecha de entrada y fecha de vencimiento
					linea.append(fechavf);																																// campo-06 - Fecha de vencimiento de la  transaccion
					linea.append((oact.ValorPresente.signum() == -1) ? "-" : " "); 																						//			
					
					linea.append(String.format("%17s", String.format("%17.4f", oact.ValorPresente).replace(" ", "0")).toString().replace(".", ",")); 								// campo-07 - Valor  presente
					linea.append((oact.TasaMercadoActual.signum() == -1) ? "-" : " "); 																					//	
					linea.append(StringUtils.leftPad(oact.TasaMercadoActual.setScale(4, RoundingMode.FLOOR).toString().replace(".", ",").replace("-", "0"), 9,'0')); 	// campo-08 - Tasa mercado
					linea.append((oact.TasaCompra.signum() == -1) ? "-" : " "); 																						//	
					linea.append(StringUtils.leftPad(oact.TasaCompra.setScale(4, RoundingMode.FLOOR).toString().replace(".", ",").replace("-", "0"), 9, '0')); 			// campo-09 - Tasa compra
					
					bwArchivo.write(linea.toString());
					bwArchivo.write("\r\n");
				}
				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
				// A R C H I V O D E T A L L E
				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
				List<String> cuentas = new ArrayList<String>();
				cuentas.add("110301070000");
				cuentas.add("110301160000");
				cuentas.add("110301200000");
				cuentas.add("110301900000");
				cuentas.add("110302100000");
				cuentas.add("110302200000");
				cuentas.add("110302300000");
				cuentas.add("110302400000");
				cuentas.add("110203010000");
				cuentas.add("110302900000");
				cuentas.add("412620924000");
				HashMap<String, Object> paramsC = new HashMap<String, Object>();
				paramsC.put("Cuentas", cuentas);
				
				List<SaldoCuentasContables> cuentaSaldo;
 
				
			    cuentaSaldo = sql.query(con, SQL_SELECT_SALDO_CUENTAS_CONTABLES	, paramsC,SaldoCuentasContables.class);
 
				
				BigDecimal totalAcumula = BigDecimal.ZERO;
				// Saldos Totales
				if (oact.Nemotecnico.startsWith("PRBC")) {
					acumulaInstrumentoPRBC = acumulaInstrumentoPRBC.add(oact.ValorPresente);
					totalAcumula = acumulaInstrumentoPRBC;
				} else if (oact.Nemotecnico.startsWith("PDBC")) {
					acumulaInstrumentoPDBC = acumulaInstrumentoPDBC.add(oact.ValorPresente);
					totalAcumula = acumulaInstrumentoPDBC;
				} else if (oact.Nemotecnico.startsWith("PRC") || oact.Nemotecnico.startsWith("CERO")) {
					acumulaInstrumentoPRC = acumulaInstrumentoPRC.add(oact.ValorPresente);
					totalAcumula = acumulaInstrumentoPRC;
				} else if (oact.Nemotecnico.startsWith("BR") || oact.Nemotecnico.startsWith("BCP")
						|| oact.Nemotecnico.startsWith("BCU") || oact.Nemotecnico.startsWith("BCD")
						|| oact.Nemotecnico.startsWith("BCX")) {
					acumulaInstrumentoBR = acumulaInstrumentoBR.add(oact.ValorPresente);
					totalAcumula = acumulaInstrumentoPRC;
				} else if (oact.Nemotecnico.startsWith("DPF") || oact.Nemotecnico.startsWith("PAGARE NR")) {
					acumulaInstrumentoDPF = acumulaInstrumentoDPF.add(oact.ValorPresente);
					totalAcumula = acumulaInstrumentoDPF;
				} else if (oact.Nemotecnico.startsWith("DPR") || oact.Nemotecnico.startsWith("PAGARE R")) {
					if (oact.TipoMoneda.equals("USD")) {
						acumulaInstrumentoDPRUSD = acumulaInstrumentoDPRUSD.add(oact.ValorPresente);
						totalAcumula = acumulaInstrumentoDPRUSD;
					} else {
						acumulaInstrumentoDPR = acumulaInstrumentoDPR.add(oact.ValorPresente);
						totalAcumula = acumulaInstrumentoDPR;
					}
				} else if (oact.Nemotecnico.startsWith("LH")) {
					acumulaInstrumentoLH = acumulaInstrumentoLH.add(oact.ValorPresente);
					totalAcumula = acumulaInstrumentoLH;
				}  else if(oact.TipoInstrumento.equals("MON")) {
					acumulaInstrumentoMonetario = acumulaInstrumentoMonetario.add(((oact.Cantidad.add(oact.CantidadGarantia)).multiply(oact.PrecioPromedio)));
				 } else {
					 acumulaInstrumentoOtros = acumulaInstrumentoOtros.add(oact.ValorPresente);
				 }
				// Generar lineas del archivo detalle

				for (SaldoCuentasContables cSaldo : cuentaSaldo) {
					saldoCuentas.put(cSaldo.CUENTA, cSaldo.SALDO);
				}

				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

			} // F I N D E L F O R

			linea2 = GeneraDetalle(saldoCuentas, acumulaInstrumentoPRBC, acumulaInstrumentoPDBC, acumulaInstrumentoPRC,
					acumulaInstrumentoBR, acumulaInstrumentoDPF, acumulaInstrumentoDPR, acumulaInstrumentoDPRUSD, 
					acumulaInstrumentoLH, acumulaInstrumentoMonetario, acumulaInstrumentoOtros);

			GenerarInterfazICU GenerarInterfazICU = new GenerarInterfazICU();
			EntradaGenerarInterfazICU entradaInterfazICU = new EntradaGenerarInterfazICU();
			SalidaGenerarInterfazICU salidaInterfazICU = new SalidaGenerarInterfazICU();
			if (entrada.p_Interfaz && !entrada.p_EsTodo) {
				entradaInterfazICU.p_ArchivoSecundario = linea2;
				entradaInterfazICU.p_Fecha = entrada.p_Fecha;
				entradaInterfazICU.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaInterfazICU.p_IdModulo = entrada.p_IdModulo;
				entradaInterfazICU.p_IdUsuario = entrada.p_IdUsuario;
				entradaInterfazICU.p_Llamado = "C08-CBN";
				entradaInterfazICU.p_Ruta = entrada.p_Ruta;
				salidaInterfazICU = GenerarInterfazICU.logica(con, entradaInterfazICU); // Servicio que genera el
																						// archivo detalle
				out.p_out_NombreArchivoLog = salidaInterfazICU.p_out_NombreArchivoLog;
				out.p_out_RutaArchivoLog = salidaInterfazICU.p_out_RutaArchivoLog;
			}
			out.p_out_ArchivoSecundario = linea2;
			System.out.print("LINEA ..." + linea2);

		} catch (Exception e) {
			log.write(e);
			out.p_out_Comentario = "Archivo no pudo ser generado";
		} finally {
			out.p_out_Comentario = "Archivo generado exitosamente";
			log.writeAsJson("SALIDA", out);
			// Cierra los archivos
			if (bwArchivo != null) {
				bwArchivo.close();
			}
		}
		return out;
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public List<String> GeneraDetalle(HashMap<String, BigDecimal> saldoCuentas, BigDecimal acu1, BigDecimal acu2,
			BigDecimal acu3, BigDecimal acu4, BigDecimal acu5, BigDecimal acu6, BigDecimal acu7, BigDecimal acu8,
			BigDecimal acu9, BigDecimal acu10) {
		List<String> linea = new ArrayList<String>();
		// Saldos Totales
		DecimalFormat formato = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("pt", "CL")));
		if (saldoCuentas.containsKey("110301070000")) {// if(saldoCuentas.containsKey("110301070000")) {
			linea.add(StringUtils.rightPad("    Total PRBC", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu1), 27, " ") + "  11.03.01.070.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110301070000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu1.subtract(saldoCuentas.get("110301070000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total PRBC", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu1), 27, " ") + "  11.03.01.070.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu1), 25, " "));
		}
		if (saldoCuentas.containsKey("110301160000")) {
			linea.add(StringUtils.rightPad("    Total PDBC", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu2), 27, " ") + "  11.03.01.160.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110301160000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu2.subtract(saldoCuentas.get("110301160000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total PDBC", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu2), 27, " ") + "  11.03.01.160.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu2), 25, " "));
		}
		if (saldoCuentas.containsKey("110301200000")) {
			linea.add(StringUtils.rightPad("    Total PRC-CERO", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu3), 27, " ") + "  11.03.01.200.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110301200000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu3.subtract(saldoCuentas.get("110301200000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total PRC-CERO", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu3), 27, " ") + "  11.03.01.200.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu3), 25, " "));
		}
		if (saldoCuentas.containsKey("110301900000")) {
			linea.add(StringUtils.rightPad("    Total BR-BCP-BCU-BCD-BCX", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu4), 27, " ") + "  11.03.01.900.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110301900000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu4.subtract(saldoCuentas.get("110301900000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total BR-BCP-BCU-BCD-BCX", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu4), 27, " ") + "  11.03.01.900.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu4), 25, " "));
		}
		if (saldoCuentas.containsKey("110302100000")) {
			linea.add(StringUtils.rightPad("    Total DPF", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu5), 27, " ") + "  11.03.02.100.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110302100000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu5.subtract(saldoCuentas.get("110302100000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total DPF", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu5), 27, " ") + "  11.03.02.100.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu5), 25, " "));
		}
		if (saldoCuentas.containsKey("110302200000")) {
			linea.add(StringUtils.rightPad("    Total DPR", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu6), 27, " ") + "  11.03.02.200.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110302200000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu6.subtract(saldoCuentas.get("110302200000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total DPR", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu6), 27, " ") + "  11.03.02.200.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu6), 25, " "));
		}
		if (saldoCuentas.containsKey("110302300000")) {
			linea.add(StringUtils.rightPad("    Total DPR US$", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu7), 27, " ") + "  11.03.02.300.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110302300000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu7.subtract(saldoCuentas.get("110302300000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total DPR US$", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu7), 27, " ") + "  11.03.02.300.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu7), 25, " "));
		}
		if (saldoCuentas.containsKey("110302400000")) {
			linea.add(StringUtils.rightPad("    Total L.H.", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu8), 27, " ") + "  11.03.02.400.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110302400000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu8.subtract(saldoCuentas.get("110302400000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total L.H.", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu8), 27, " ") + "  11.03.02.400.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu8), 25, " "));
		}
		if (saldoCuentas.containsKey("110203010000")) {
			linea.add(StringUtils.rightPad("    Total Monetario", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu9), 27, " ") + "  11.02.03.010.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110203010000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu9.subtract(saldoCuentas.get("110203010000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total Monetario", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu9), 27, " ") + "  11.02.03.010.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu9), 25, " "));
		}
		if (saldoCuentas.containsKey("110302900000")) {
			linea.add(StringUtils.rightPad("    Total Otros Renta Fija", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu10), 27, " ") + "  11.03.02.900.000"
					+ StringUtils.leftPad(formato.format(saldoCuentas.get("110302900000").abs()), 25, " ")
					+ StringUtils.leftPad(formato.format(acu10.subtract(saldoCuentas.get("110302900000").abs())), 25, " "));
		} else {
			linea.add(StringUtils.rightPad("    Total Otros Renta Fija", 28, ' ')
					+ StringUtils.leftPad(formato.format(acu10), 27, " ") + "  11.03.02.900.000"
					+ StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acu10), 25, " "));
		}

		return linea;
	}
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private String calcularMoneda(Cartera tipoReajComp) {
		String moneda = "3";
		
		if (!tipoReajComp.MonedaPago.equalsIgnoreCase("CLP")) {
			moneda = "3";
		}else if (tipoReajComp.TipoMoneda .equals("CLP")) {
			moneda = "1";
		} else {
			moneda = "2";				
		}
		return moneda;
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public String AsignaBanda(Long diasDiferencia) {
		String banda = "";
		if (diasDiferencia < 8) {// DISPONIBLE HASTA UN MES
			banda = " 1";
		} else if (diasDiferencia >= 8 && diasDiferencia <= 15) { 
			banda = "05";
		} else if (diasDiferencia >= 16 && diasDiferencia <= 30) { 
			banda = "10";
		} else if (diasDiferencia >= 31  && diasDiferencia <= 60) { 
			banda = "15";
		} else if (diasDiferencia >= 61 && diasDiferencia <= 90) { 
			banda = "20";
		} else if (diasDiferencia >= 91 && diasDiferencia <= 180) { 
			banda = "25";
		} else if (diasDiferencia >= 181 && diasDiferencia < 360) { 
			banda = "30";
		} else { 
			banda = "31";
		}
		return banda;
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {

		// prueba de la clase
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			try {
				InGenerarInterfazC08 entrada = new InGenerarInterfazC08();
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_Fecha = "2022-05-31";
				entrada.p_Ruta = "C:\\BCS\\logs";
				entrada.p_Interfaz = true;
				JsonParser parser = new JsonParser();
				JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				GenerarInterfazC08CarteraNormal demo = new GenerarInterfazC08CarteraNormal();
				System.out.println(demo.call(con, dataJsonIn).toString());

			} catch (Exception e) {
				con.rollback();
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
