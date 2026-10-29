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
import cl.bcs.utils.Utils;

public class GenerarInterfazC40CarteraPacteada implements JavaCall  {
	// Interfaz C40 - CARTERA PACTEADA
	
	private final String SQL_SELECT_CARTERA_PACTEADA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40CarteraPacteada_SelectPacteadas.sql");
	private final String SQL_SELECT_CARTERA_SIMULTANEA = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40CarteraPacteada_SelectSimultaneas.sql");
	private final String SQL_SELECT_CARTERA_SIMULTANEA_His = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40CarteraPacteada_SelectSimultaneas_His.sql");
	private final String SQL_SELECT_CUENTA_CONTABLE_SALDO = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectSaldoCuentaContable.sql");	
	
	public static class GenerarInterfazC40CarteraPacteada_Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
		public boolean  interfaz = true;
		public boolean  p_EsTodos= false;
	}
	
	public static class GenerarInterfazC40CarteraPacteada_Salida {
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
        public String     PLAZO;
        public BigDecimal VALOR_PTE;
   }	
	
	public static class CuentaContable {
        public String     CUENTA;		
        public BigDecimal SALDO;
   }	
	
	public GenerarInterfazC40CarteraPacteada_Salida logica(Connection con, GenerarInterfazC40CarteraPacteada_Entrada entrada) throws Exception {
		GenerarInterfazC40CarteraPacteada_Salida salida = new GenerarInterfazC40CarteraPacteada_Salida();

		if (entrada.p_Fecha == null) {
			String fechaSistema = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
			Date fechaActual = Fecha.toDate(fechaSistema);
			entrada.p_Fecha = fechaActual.toString();
		}
		
		boolean Historico = false;
		Date fechaActual = Fecha.toDate( Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa));
		if (!fechaActual.toString().equalsIgnoreCase(entrada.p_Fecha)) // si no es igual la fecha de entrada entonces se utiliza para ver el historico
		{	Historico = true ;		}
		
        // Formatos de fecha
        SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("yyMMdd"); 	// Nombre de los archivos YYDDMM

		// Asignacion de formatos en Fechas
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		String fechayyMMdd = sdfyyMMdd.format(pFecha);
		
		BufferedWriter bwArchivoC40CBP = null;
		BufferedWriter bwArchivoC40ICU = null;
		//Boolean existFileICU = false;
		
		if (entrada.interfaz || entrada.p_EsTodos) {
			// Configuracion para salida de archivo
			salida.P_OUT_NOMBRE_ARCHIVO = "C40CBP" + fechayyMMdd + ".TXT";
			salida.P_OUT_NOMBRE_ARCHIVO_LOG = "C40ICU" + fechayyMMdd + ".LOG";
			salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
			salida.P_OUT_RUTA_ARCHIVO_LOG = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
			
			File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
			//File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
			
			// Buffer para el archivo C40CBP
			bwArchivoC40CBP = new BufferedWriter(new FileWriter(archivo));
			// Si existe el archivo C40ICU (existFileICU = true) el FlieWriter adiciona 
			// el detalle al archivo existente y omite la escritura del encabezado 
			//existFileICU = archivoLog.exists();
			//bwArchivoC40ICU = new BufferedWriter(new FileWriter(archivoLog, existFileICU));
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
			List<CarteraPacteada> carteraPacteada = sql.query(con, SQL_SELECT_CARTERA_PACTEADA, paramsP, CarteraPacteada.class);
			// Cartera de Pacto
			for (CarteraPacteada p : carteraPacteada) {
				// Concatenacion de strings para el archivo C40CBP Cartera pacteada 
				StringBuilder linea = new StringBuilder("");			
				linea.append("0");								// (01) C40CBP-REGISTRO - Codigo de registro
				linea.append(p.TIPO_COMPROM);					// (01) C40CBP-TIPO - Tipo de operacion C o V
				linea.append(p.FEC_OPERAC);						// (10) C40CBP-FECINIPAC - Fecha operacion 
				linea.append(p.FEC_VENC);						// (10) C40CBP-FECTERPAC - Fecha liquidacion
				linea.append(formatMonto(p.MONTO_CAPITAL));		// (-999999999999,9999.) C40CBP-CAPITAL - Monto Capital
				linea.append(formatTasa(p.TASA_COMPROM));		// (---9,9999.) C40CBP-TASA
				linea.append(calcularBase(p.TIPO_REAJ_COMP));	// (03) C40CBP-BASE
				linea.append(calcularMoneda(p.TIPO_REAJ_COMP));	// (03) C40CBP-MONEDA
				linea.append(calcularBanda(p.PLAZO));			// (02) C40CBP-BANDA 
				linea.append(formatMonto(p.VALOR_PTE));			// (-999999999999,9999.) C40CBP-VAL-PTE - Valor presente
				linea.append(String.format("%06d", i++));		// (06) C40CBP-SECUENCIA			
				linea.append("PACTO");							// (10) C40CBP-NEMO - Valor asignado por codigo (PACTO)
				
				// Calcula el monto total del valor presente para el Compromiso de Compra y Venta
				if (p.TIPO_COMPROM.equals("C")) {
					acumCarteraCompra = acumCarteraCompra.add(p.VALOR_PTE);
				} else {
					acumCarteraVenta = acumCarteraVenta.add(p.VALOR_PTE);				
				}
				
				if (entrada.interfaz || entrada.p_EsTodos) {
					bwArchivoC40CBP.write(linea.toString());
					bwArchivoC40CBP.write("\r\n");
				}
			}
			
			BigDecimal acumCarteraSimult = BigDecimal.ZERO;
			HashMap<String, Object> paramsS = new HashMap<String, Object>();
			paramsS.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsS.put("FiltroFecha", entrada.p_Fecha);
			List<CarteraSimultanea> carteraSimultanea;
			if(Historico) {
			 carteraSimultanea = sql.query(con, SQL_SELECT_CARTERA_SIMULTANEA_His, paramsS, CarteraSimultanea.class); 
			}else {
			 carteraSimultanea = sql.query(con, SQL_SELECT_CARTERA_SIMULTANEA, paramsS, CarteraSimultanea.class); 
			} 
			
			
			
			
			// Cartera Simultanea 
			for (CarteraSimultanea s :carteraSimultanea) {
				// Concatenacion de strings para el archivo C40CBP Cartera simultanea
				StringBuilder linea = new StringBuilder("");			
				linea.append("0");								// (01) C40CBP-REGISTRO - Codigo de registro
				linea.append("V");	//	s.TIPO_COMPROM)			// (01) C40CBP-TIPO - Tipo de operacion C o V
				linea.append(s.FEC_TRANS);						// (10) C40CBP-FECINIPAC - Fecha transaccion 
				linea.append(s.FEC_VENC);						// (10) C40CBP-FECTERPAC - Fecha liquidacion
				linea.append(formatMonto(s.MONTO_CAPITAL));		// (-999999999999,9999.) C40CBP-CAPITAL - Monto Capital
				linea.append(formatTasa(s.TASA));				// (---9,9999.) C40CBP-TASA
				linea.append(String.format("%3s", "30")); 		// (03) C40CBP-BASE
				linea.append(String.format("%-3s", "1"));		// (03) C40CBP-MONEDA
				linea.append(calcularBanda(Long.valueOf(s.PLAZO)));			// (02) C40CBP-BANDA
				linea.append(formatMonto(s.VALOR_PTE));			// (-999999999999,9999.) C40CBP-VAL-PTE - Valor presente
				linea.append(String.format("%06d", i++));		// (06) C40CBP-SECUENCIA			
				linea.append("SIMULTANEA");						// (10) C40CBP-NEMO - Valor asignado por codigo (PACTO)
					
				// Asigna el monto total del Capital al Compromiso de Simultanea
				acumCarteraSimult = acumCarteraSimult.add(s.MONTO_CAPITAL);
				
				if (entrada.interfaz) {
					bwArchivoC40CBP.write(linea.toString());
					bwArchivoC40CBP.write("\r\n");
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
			lineaVenta.append(String.format("%25s", formatoNumero(saldoCuentaVenta, "#,##0.00", true)));
			lineaVenta.append(String.format("%25s", formatoNumero(acumCarteraVenta.subtract(saldoCuentaVenta), "#,##0.00", true)));
			
			// Concatena la linea detalle de Compromiso de Compra		
			StringBuilder lineaCompra = new StringBuilder("");
			lineaCompra.append(String.format("%-25s",  String.format("%4s", "") + "Compromiso Compra"));
			lineaCompra.append(String.format("%30s", formatoNumero(acumCarteraCompra, "#,##0.00", true)));			
			lineaCompra.append(String.format("%18s", "11.04.02.000.000"));
			lineaCompra.append(String.format("%25s", formatoNumero(saldoCuentaCompra, "#,##0.00", true)));
			lineaCompra.append(String.format("%25s", formatoNumero(acumCarteraCompra.subtract(saldoCuentaCompra), "#,##0.00", true)));		// Diferencia = Total Cartera - Saldo Contable	
			
			// Concatena la linea detalle de Compromiso de Simultanea		
			StringBuilder lineaSimultanea = new StringBuilder("");
			lineaSimultanea.append(String.format("%-25s",  String.format("%4s", "") + "Compromiso Simultanea"));
			lineaSimultanea.append(String.format("%30s", formatoNumero(acumCarteraSimult, "#,##0.00", true)));			
			lineaSimultanea.append(String.format("%18s", "21.02.04.010.000"));
			lineaSimultanea.append(String.format("%25s", formatoNumero(saldoCuentaSimult, "#,##0.00", true)));			
			lineaSimultanea.append(String.format("%25s", formatoNumero(acumCarteraSimult.subtract(saldoCuentaSimult), "#,##0.00", true)));		// Diferencia = Total Cartera - Saldo Contable			
			
			// Genera salida tipo Lista en P_ARCHIVO_SECUNDARIO
			List<String> salidaC40ICU = new ArrayList<String>();
			salidaC40ICU.add(lineaVenta.toString());
			salidaC40ICU.add(lineaCompra.toString());
			salidaC40ICU.add(lineaSimultanea.toString());
			if(entrada.interfaz && !entrada.p_EsTodos) {
				GenerarInterfazICU interfazICU = new GenerarInterfazICU();
				EntradaGenerarInterfazICU entradaICU = new EntradaGenerarInterfazICU();
				SalidaGenerarInterfazICU  salidaICU  = new SalidaGenerarInterfazICU();
				entradaICU.p_ArchivoSecundario = salidaC40ICU;
				entradaICU.p_Fecha = entrada.p_Fecha;
				entradaICU.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaICU.p_IdModulo = entrada.p_IdModulo;
				entradaICU.p_IdUsuario = entrada.p_IdUsuario;
				entradaICU.p_Llamado = "C40-CBP";
				entradaICU.p_Ruta = entrada.p_Ruta;
				salidaICU = interfazICU.logica(con, entradaICU);
				salida.P_OUT_NOMBRE_ARCHIVO_LOG = salidaICU.p_out_NombreArchivoLog;
				salida.P_OUT_RUTA_ARCHIVO_LOG   = salidaICU.p_out_RutaArchivoLog;
			}			
			salida.P_ARCHIVO_SECUNDARIO = salidaC40ICU;
			
		} catch (Exception e) {
			System.out.print(e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_COMENTARIO = "Archivo no pudo ser Generado";
 
		} finally {
			// Cierra los archivos C40CBP y C40ICU
			if(bwArchivoC40CBP !=null){
				bwArchivoC40CBP.close();
			}
			salida.P_OUT_COMENTARIO = "Archivo generado exitosamente";
		}
		
		return salida;
	}

	// Formato -999999999999,9999 para el monto Capital y Valor Presente
	private String formatMonto(BigDecimal monto) {
		String capital = String.format("%18s", String.format("%17s", formatoNumero(monto, "##0.0000", false)).replace(" ", "0"));
		return capital;
	}
	
	// Formato ---9,9999 para la tasa
	private String formatTasa(BigDecimal tasa) {
		String t = String.format("%9s", String.format("%4s", formatoNumero(tasa, "##0.0000", false)));
		return t;
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
	
	private String calcularMoneda(String tipoReajComp) {
		String moneda = "3";
		
		if (tipoReajComp.equals("CLP")) {
			moneda = "1";
		} else if (tipoReajComp.equals("UF")) { 
			moneda = "2";
		} else if (tipoReajComp.equals("IVP")) { 
			moneda = "2";
	
		} else if (tipoReajComp.equals("USD")) { 
			moneda = "3";				
		}		
		return String.format("%-3s", moneda);
	}

	private String calcularBanda(Long dias) {
		String banda = null;

		if (dias < 31) {
			banda = " 1";
		} else if (dias > 30 && dias < 91) {
			banda = " 2";
		} else if (dias > 90 && dias < 181) {
			banda = " 3";			
		} else if (dias > 180 && dias < 271) {
			banda = " 4";
		} else if (dias > 270 && dias < 366) {
			banda = " 5";
		} else if (dias > 365 && dias < 731) {
			banda = " 6";			
		} else if (dias > 730 && dias < 1096) {
			banda = " 7";
		} else if (dias > 1095 && dias < 1461) {
			banda = " 8";
		} else if (dias > 1460 && dias < 1826) {
			banda = " 9";
		} else if (dias > 1825 && dias < 2556) {
			banda = "10";
		} else if (dias > 2555 && dias < 3651) {
			banda = "11";
		} else if (dias > 3650 && dias < 5476) {
			banda = "12";
		} else if (dias > 5475 && dias < 7301) {
			banda = "13";
		} else  {
			banda = "14";				
		}
		
		return String.format("%2s", banda);
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarInterfazC40CarteraPacteada_Entrada in = gson.fromJson(jin.toString(), GenerarInterfazC40CarteraPacteada_Entrada.class);
		GenerarInterfazC40CarteraPacteada_Salida out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                					 .serializeNulls().create();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2022-05-31\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\"}";
			GenerarInterfazC40CarteraPacteada_Entrada entrada = gson.fromJson(json, GenerarInterfazC40CarteraPacteada_Entrada.class);
			GenerarInterfazC40CarteraPacteada demo = new GenerarInterfazC40CarteraPacteada();
			GenerarInterfazC40CarteraPacteada_Salida ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");

	}

}





