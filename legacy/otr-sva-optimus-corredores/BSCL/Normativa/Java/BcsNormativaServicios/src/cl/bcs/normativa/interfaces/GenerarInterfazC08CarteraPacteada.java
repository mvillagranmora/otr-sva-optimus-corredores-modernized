package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.interfaces.GenerarInterfazICU.EntradaGenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.SalidaGenerarInterfazICU;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class GenerarInterfazC08CarteraPacteada implements JavaCall  {
	// Interfaz C08 - CARTERA PACTEADA
	
	private final String SQL_SELECT_CARTERA_PACTEADA      = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08CarteraPacteada_SelectInterfazPactos.sql");
	private final String SQL_SELECT_CARTERA_SIMULTANEA    = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08CarteraPacteada_SelectInterfazSimultaneas.sql");
 
	private final String SQL_SELECT_CUENTA_CONTABLE_SALDO = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectSaldoCuentaContable.sql");	
	
	public static class GenerarInterfazC08CarteraPacteada_Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
		public boolean  interfaz = true;
		public boolean  p_EsTodos= false;
	}
	
	public static class GenerarInterfazC08CarteraPacteada_Salida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR = 0L; 
		
		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO;
		public String 				P_OUT_COMENTARIO;
		
		public List<String> 		P_ARCHIVO_SECUNDARIO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CarteraPacteada {
        public String     TIPO_COMPROM;
        public String     FEC_OPERAC;
        public String     FEC_LIQUID;
        public String     FEC_VENC;
        public BigDecimal MONTO_CAPITAL;
        public BigDecimal TASA_COMPROM;
        public String     TIPO_REAJ_COMP;
        public Long       PLAZO;
        public BigDecimal VALOR_PTE;
        public String 	  MONEDA_PAGO;
   }
	
	public static class CarteraSimultanea {
        public String     TIPO_COMPROM;		
        public String     TIPO_OPERAC;
        public String     FEC_TRANS;
        public String     FEC_VENC;
        public String     FEC_LIQUID;
        public BigDecimal MONTO_CAPITAL;
        public BigDecimal TASA;
        public String     TIPO_REAJ_COMP;
        public Long       PLAZO;
        public BigDecimal VALOR_PTE;
   }	
	
	public static class CuentaContable {
        public String     CUENTA;		
        public BigDecimal SALDO;
   }	
	
	public GenerarInterfazC08CarteraPacteada_Salida logica(Connection con, GenerarInterfazC08CarteraPacteada_Entrada entrada) throws Exception {
		GenerarInterfazC08CarteraPacteada_Salida salida = new GenerarInterfazC08CarteraPacteada_Salida();
		
		// Asignacion de formatos en Fechas
	    LocalDate 	FechaActual 	= LocalDate.parse(entrada.p_Fecha	, DateTimeFormatter.ofPattern("yyyy-MM-dd")); // fecha en localdate para el calculo de dias para las bandas
		Date 		pFecha 			= new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha)	;
		Date 		fechaActual 	= Fecha.toDate(Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa));
		String 		fechayyMMdd 	= new SimpleDateFormat("yyMMdd")	.format(pFecha)			;
		
		BufferedWriter bwArchivoC08CBP = null;
		BufferedWriter bwArchivoC08ICU = null;
	
	    // valida si la fecha de entrada es nula
		if (entrada.p_Fecha == null || entrada.p_Fecha.equals("")) {	
			entrada.p_Fecha = fechaActual.toString();
		}
		
		Boolean Historico = false;
		if (!fechaActual.toString().equalsIgnoreCase(entrada.p_Fecha)) // si no es igual la fecha de entrada entonces se utiliza para ver el historico
		{	Historico = true ;		}	
		
		if (entrada.interfaz || entrada.p_EsTodos) {
			// Configuracion para salida de archivo      
			salida.P_OUT_NOMBRE_ARCHIVO = "C08CBP" + fechayyMMdd + ".TXT";
			salida.P_OUT_NOMBRE_ARCHIVO_LOG = "C08ICU" + fechayyMMdd + ".LOG";
			salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
			salida.P_OUT_RUTA_ARCHIVO_LOG = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
			
			File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
			
			
			// Buffer para el archivo C08CBP
			bwArchivoC08CBP = new BufferedWriter(new FileWriter(archivo));
		}
		
		try {
			// Busca la informacion de la corredora
			int i = 1;
			SQL sql = new SQL();		
			BigDecimal acumCarteraCompra = BigDecimal.ZERO;
			BigDecimal acumCarteraVenta = BigDecimal.ZERO;
			HashMap<String, Object> paramsP = new HashMap<String, Object>();
			paramsP.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsP.put("FiltroFecha", entrada.p_Fecha);
			
			List<CarteraPacteada> carteraPacteada;
 
			carteraPacteada = sql.query(con, SQL_SELECT_CARTERA_PACTEADA	, paramsP, CarteraPacteada.class);				
 
			
			DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			// Cartera de Pacto
			for (CarteraPacteada p : carteraPacteada) {
				
				LocalDate fechavencimiento = LocalDate.parse(p.FEC_VENC, sourceFormatter);
				String bandaDefinida = AsignaBanda( ChronoUnit.DAYS.between(FechaActual,fechavencimiento) );
				// Concatenacion de strings para el archivo C08CBP Cartera pacteada 
				StringBuilder linea = new StringBuilder("");			
				linea.append("0");								// (01) C08CBP-REGISTRO 		- Codigo de registro
				linea.append(p.TIPO_COMPROM);					// (01) C08CBP-TIPO 			- Tipo de operacion C o V
				linea.append(p.FEC_OPERAC);						// (10) C08CBP-FECINIPAC 		- Fecha operacion 
				linea.append(p.FEC_VENC);						// (10) C08CBP-FECTERPAC 		- Fecha liquidacion
				linea.append(formatMonto(p.MONTO_CAPITAL));		// (-999999999999,9999.) C08CBP-CAPITAL - Monto Capital
				linea.append(formatTasa(p.TASA_COMPROM));		// (---9,9999.) C08CBP-TASA
				linea.append(calcularBase(p.TIPO_REAJ_COMP));	// (03) C08CBP-BASE
				linea.append(calcularMoneda(p));	// (03) C08CBP-MONEDA
				linea.append(bandaDefinida);					// (02) C08CBP-BANDA 
				linea.append(formatMonto(p.VALOR_PTE));			// (-999999999999,9999.) C08CBP-VAL-PTE - Valor presente
				linea.append(String.format("%06d", i++));		// (06) C08CBP-SECUENCIA			
				linea.append("PACTO");							// (10) C08CBP-NEMO 			- Valor asignado por codigo (PACTO)
				
				// Calcula el monto total del valor presente para el Compromiso de Compra y Venta
				if (p.TIPO_COMPROM.equals("C")) {
					acumCarteraCompra = acumCarteraCompra.add(p.VALOR_PTE);
				} else {
					acumCarteraVenta = acumCarteraVenta.add(p.VALOR_PTE);				
				}
				
				if (entrada.interfaz || entrada.p_EsTodos) {
					bwArchivoC08CBP.write(linea.toString());
					bwArchivoC08CBP.write("\r\n");
				}
			}
			
			BigDecimal acumCarteraSimult = BigDecimal.ZERO;
			HashMap<String, Object> paramsS = new HashMap<String, Object>();
			paramsS.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsS.put("FiltroFecha", entrada.p_Fecha);
			List<CarteraSimultanea> carteraSimultanea;
			
			carteraSimultanea = sql.query(con, SQL_SELECT_CARTERA_SIMULTANEA, paramsS		, CarteraSimultanea.class);  
			
			// Cartera Simultanea  
			for (CarteraSimultanea s :carteraSimultanea) {
				LocalDate fechavencimiento = LocalDate.parse(s.FEC_VENC, sourceFormatter);
				String bandaDefinida = AsignaBanda( ChronoUnit.DAYS.between(FechaActual,fechavencimiento) );
				// Concatenacion de strings para el archivo C08CBP Cartera simultanea
				StringBuilder linea = new StringBuilder("");			
				linea.append("0");								// (01) C08CBP-REGISTRO - Codigo de registro
				linea.append(s.TIPO_COMPROM);					// (01) C08CBP-TIPO - Tipo de operacion C o V
				linea.append(s.FEC_TRANS);						// (10) C08CBP-FECINIPAC - Fecha transaccion 
				linea.append(s.FEC_VENC);						// (10) C08CBP-FECTERPAC - Fecha liquidacion
				linea.append(formatMonto(s.MONTO_CAPITAL));		// (-999999999999,9999.) C08CBP-CAPITAL - Monto Capital
				linea.append(formatTasa(s.TASA));				// (---9,9999.) C08CBP-TASA
				linea.append(String.format("%3s", "30")); 		// (03) C08CBP-BASE
				linea.append(String.format("%-3s", "1"));		// (03) C08CBP-MONEDA
				linea.append(bandaDefinida);					// (02) C08CBP-BANDA
				linea.append(formatMonto(s.VALOR_PTE));			// (-999999999999,9999.) C08CBP-VAL-PTE - Valor presente
				linea.append(String.format("%06d", i++));		// (06) C08CBP-SECUENCIA			
				linea.append("SIMULTANEA");						// (10) C08CBP-NEMO - Valor asignado por codigo (PACTO)
					
				// Asigna el monto total del Capital al Compromiso de Simultanea
				acumCarteraSimult = acumCarteraSimult.add(s.MONTO_CAPITAL);
				
				if (entrada.interfaz) {
					bwArchivoC08CBP.write(linea.toString());
					bwArchivoC08CBP.write("\r\n");
				}
			}
			
			// Obtiene el saldo capital para las cuentas 210201000000, 110402000000 y 210204010000
			List<String> cuentas = new ArrayList<String>();
			cuentas.add("210201000000");
			cuentas.add("110402000000");
			cuentas.add("210204010000");
			HashMap<String, Object> paramsC = new HashMap<String, Object>();
			paramsC.put("Cuentas", cuentas);
			List<CuentaContable> cuentaSaldo = sql.query(con, SQL_SELECT_CUENTA_CONTABLE_SALDO, paramsC, CuentaContable.class);
			
			BigDecimal saldoCuentaVenta = BigDecimal.ZERO;
			BigDecimal saldoCuentaCompra = BigDecimal.ZERO;
			BigDecimal saldoCuentaSimult = BigDecimal.ZERO;
			
			for (CuentaContable cuenta: cuentaSaldo) {
				if (cuenta.CUENTA.equals("210201000000")) {
					saldoCuentaVenta = cuenta.SALDO;
					// Como es una cuenta de Pasivo si es Negativo se cambia a Positivo
					if (saldoCuentaVenta.compareTo(BigDecimal.ZERO) < 0) {
						saldoCuentaVenta = saldoCuentaVenta.multiply(new BigDecimal(-1));
					}
				} else if (cuenta.CUENTA.equals("110402000000")) {
					saldoCuentaCompra = cuenta.SALDO;
					
				} if (cuenta.CUENTA.equals("210204010000")) {
					saldoCuentaSimult = cuenta.SALDO;
					// Como es una cuenta de Pasivo si es Negativo se cambia a Positivo
					if (saldoCuentaSimult.compareTo(BigDecimal.ZERO) < 0) {
						saldoCuentaSimult = saldoCuentaVenta.multiply(new BigDecimal(-1));
					}				
				}
			}
			
			// Concatena la linea detalle de Compromiso de Venta
			StringBuilder lineaVenta = new StringBuilder("");
			lineaVenta.append(String.format("%-25s",  String.format("%4s", "") + "Compromiso Venta"));
			lineaVenta.append(String.format("%30s", formatoNumero(acumCarteraVenta, "#,##0.00", true)));
			lineaVenta.append(String.format("%18s", "21.02.01.000.000"));
			lineaVenta.append(String.format("%25s", formatoNumero(saldoCuentaVenta.abs(), "#,##0.00", true)));
			lineaVenta.append(String.format("%25s", formatoNumero(acumCarteraVenta.subtract(saldoCuentaVenta.abs()), "#,##0.00", true)));
			
			// Concatena la linea detalle de Compromiso de Compra		
			StringBuilder lineaCompra = new StringBuilder("");
			lineaCompra.append(String.format("%-25s",  String.format("%4s", "") + "Compromiso Compra"));
			lineaCompra.append(String.format("%30s", formatoNumero(acumCarteraCompra, "#,##0.00", true)));			
			lineaCompra.append(String.format("%18s", "11.04.02.000.000"));
			lineaCompra.append(String.format("%25s", formatoNumero(saldoCuentaCompra.abs(), "#,##0.00", true)));
			lineaCompra.append(String.format("%25s", formatoNumero(acumCarteraCompra.subtract(saldoCuentaCompra.abs()), "#,##0.00", true)));		// Diferencia = Total Cartera - Saldo Contable	
			
			// Concatena la linea detalle de Compromiso de Simultanea		
			StringBuilder lineaSimultanea = new StringBuilder("");
			lineaSimultanea.append(String.format("%-25s",  String.format("%4s", "") + "Compromiso Simultanea"));
			lineaSimultanea.append(String.format("%30s", formatoNumero(acumCarteraSimult, "#,##0.00", true)));			
			lineaSimultanea.append(String.format("%18s", "21.02.04.010.000"));
			lineaSimultanea.append(String.format("%25s", formatoNumero(saldoCuentaSimult.abs(), "#,##0.00", true)));			
			lineaSimultanea.append(String.format("%25s", formatoNumero(acumCarteraSimult.subtract(saldoCuentaSimult.abs()), "#,##0.00", true)));		// Diferencia = Total Cartera - Saldo Contable			
			
			// Genera salida tipo Lista en P_ARCHIVO_SECUNDARIO
			List<String> salidaC08ICU = new ArrayList<String>();
			salidaC08ICU.add(lineaVenta.toString());
			salidaC08ICU.add(lineaCompra.toString());
			salidaC08ICU.add(lineaSimultanea.toString());
			if(entrada.interfaz && !entrada.p_EsTodos) {
				GenerarInterfazICU interfazICU = new GenerarInterfazICU();
				EntradaGenerarInterfazICU entradaICU = new EntradaGenerarInterfazICU();
				SalidaGenerarInterfazICU  salidaICU  = new SalidaGenerarInterfazICU();
				entradaICU.p_ArchivoSecundario = salidaC08ICU;
				entradaICU.p_Fecha = entrada.p_Fecha;
				entradaICU.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaICU.p_IdModulo = entrada.p_IdModulo;
				entradaICU.p_IdUsuario = entrada.p_IdUsuario;
				entradaICU.p_Llamado = "C08-CBP";
				entradaICU.p_Ruta = entrada.p_Ruta;
				salidaICU = interfazICU.logica(con, entradaICU);
				salida.P_OUT_NOMBRE_ARCHIVO_LOG = salidaICU.p_out_NombreArchivoLog;
				salida.P_OUT_RUTA_ARCHIVO_LOG   = salidaICU.p_out_RutaArchivoLog;
			}			
			salida.P_ARCHIVO_SECUNDARIO = salidaC08ICU;
	
		} catch (Exception e) {
			System.out.println(e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = "Archivo no se pudo generar";
 
		} finally {
			// Cierra los archivos C08CBP y C08ICU
			salida.P_OUT_COMENTARIO = "Archivo generado exitosamente";
			if(bwArchivoC08CBP !=null){
				bwArchivoC08CBP.close();
			}
			if(bwArchivoC08ICU !=null){
				bwArchivoC08ICU.close();
			}
		}
		
		return salida;
	}

	// Formato -999999999999,9999 para el monto Capital y Valor Presente
	private String formatMonto(BigDecimal monto) {		
		String capital = String.format("%18s", String.format("%17.4f",monto).replace(" ", "0"));
		capital = capital.replace(".",",");
		return capital;
	}
	
	// Formato ---9,9999 para la tasa
	private String formatTasa(BigDecimal tasa) {
		String t = String.format("%9s", String.format("%4.4f", tasa));
		t = t.replace(".",",");
		return t;
	}
	
	private String calcularBase(String tipoReajComp) {
		String base = tipoReajComp;
		
		if (tipoReajComp.equals("CLP")) {
			base = " 30";
		} else if (tipoReajComp.equals("UF")) { 
			base = "360";
		} else if (tipoReajComp.equals("IVP")) { 
			base = "360";
		} else if (tipoReajComp.equals("USD")) { 
			base = " 30";
		}
		return String.format("%3s", base);
	}
	// Fija la coma como separador decimal y el punto como separador de grupo si aplica
		private String formatoNumero(BigDecimal monto, String formato, Boolean separadorDeGrupo) {
			DecimalFormatSymbols fijarSimbolos = new DecimalFormatSymbols(Locale.getDefault()); 
			fijarSimbolos.setDecimalSeparator(','); 
			if (separadorDeGrupo) {
				fijarSimbolos.setGroupingSeparator('.');
			} 
			DecimalFormat df = new DecimalFormat(formato, fijarSimbolos);
			
			return df.format(monto);
		}
		
	
	private String calcularMoneda(CarteraPacteada tipoReajComp) {
		String moneda = "3";
		
		if (!tipoReajComp.MONEDA_PAGO.equalsIgnoreCase("CLP")) {
			moneda = "3";
		}else if (tipoReajComp.TIPO_REAJ_COMP.equals("CLP")) {
			moneda = "1";
		} else {
			moneda = "2";				
		}		
		return String.format("%-3s", moneda);
	}

	public String AsignaBanda(Long diasDiferencia) {
		String banda = "";
		if (diasDiferencia < 8) {// DISPONIBLE HASTA UN MES
			banda = " 1";
		} else if (diasDiferencia >= 8 && diasDiferencia <= 15) {// MAS DE 1 MES HASTA 3 MESES
			banda = "05";
		} else if (diasDiferencia >= 16 && diasDiferencia <= 30) {// MAS DE 3 MESES HASTA 6 MESES
			banda = "10";
		} else if (diasDiferencia >= 31  && diasDiferencia <= 60) {// MAS DE 6 MESES HASTA 9 MESES
			banda = "15";
		} else if (diasDiferencia >= 61 && diasDiferencia <= 90) {// MAS DE 9 MESES HASTA 1 ANIO
			banda = "20";
		} else if (diasDiferencia >= 91 && diasDiferencia <= 180) {// MAS DE 1 ANIO HASTA 2 ANIOS
			banda = "25";
		} else if (diasDiferencia >= 181 && diasDiferencia < 360) {// MAS DE 2 ANIOS HASTA 3 ANIOS
			banda = "30";
		} else {// MAS DE 20 ANIOS
			banda = "31";
		}
		return banda;
	}
	
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls().create();
		GenerarInterfazC08CarteraPacteada_Entrada in = gson.fromJson(jin.toString(), GenerarInterfazC08CarteraPacteada_Entrada.class);
		GenerarInterfazC08CarteraPacteada_Salida out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection()  ;
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2022-05-31\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\"}";
			GenerarInterfazC08CarteraPacteada_Entrada entrada = gson.fromJson(json, GenerarInterfazC08CarteraPacteada_Entrada.class);
			GenerarInterfazC08CarteraPacteada demo = new GenerarInterfazC08CarteraPacteada();
			GenerarInterfazC08CarteraPacteada_Salida ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");

	}

}





