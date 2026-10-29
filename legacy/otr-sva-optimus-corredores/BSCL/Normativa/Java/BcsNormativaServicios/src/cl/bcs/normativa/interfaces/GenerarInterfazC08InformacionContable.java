package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazC08InformacionContable implements JavaCall {
	// Interfaz C08 - INFORMACION CONTABLE
	// Servicio No. 700034
	
	private final String SQL_CONSULTAR_CORREDOR = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08InformacionContable_SelectInformacionCorredor.sql");
	private final String SQL_SELECT_SALDOS_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC08InformacionContable_SelectInformacionContable.sql");	
	
	public static class GenerarInterfazC08_Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
	}
	
	public static class GenerarInterfazC08_Salida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR = 0L;
		
		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatoCorredor {
		public String 	Nombre;
		public String 	Rut;
	}
		
	public static class SaldosCuentas {
		public String CuentaContabilidad;
		public String NombreCuenta;
		public String TipoCuenta;
		public String Imprime;
		public String Codigo;
		public String Banda;
		public String SBIF;		
		public BigDecimal Saldo;
		public String getCuentaContabilidad() {
			return CuentaContabilidad;
		}
		
		public void setCuentaContabilidad(String cuentaContabilidad) {
			CuentaContabilidad = cuentaContabilidad;
		}
		public String getNombreCuenta() {
			return NombreCuenta;
		}
		public void setNombreCuenta(String nombreCuenta) {
			NombreCuenta = nombreCuenta;
		}
		public String getTipoCuenta() {
			return TipoCuenta;
		}
		public void setTipoCuenta(String tipoCuenta) {
			TipoCuenta = tipoCuenta;
		}
		public String getImprime() {
			return Imprime;
		}
		public void setImprime(String imprime) {
			Imprime = imprime;
		}
		public String getCodigo() {
			return Codigo;
		}
		public void setCodigo(String codigo) {
			Codigo = codigo;
		}
		public String getBanda() {
			return Banda;
		}
		public void setBanda(String banda) {
			Banda = banda;
		}
		public String getSBIF() {
			return SBIF;
		}
		public void setSBIF(String sBIF) {
			SBIF = sBIF;
		}
		public BigDecimal getSaldo() {
			return Saldo;
		}
		public void setSaldo(BigDecimal saldo) {
			Saldo = saldo;
		}
	}
	
	public GenerarInterfazC08_Salida logica(Connection con, GenerarInterfazC08_Entrada entrada) throws Exception {
		GenerarInterfazC08_Salida salida = new GenerarInterfazC08_Salida();

		// Declaracion de variables y definicion de formatos de string
        String strTipoCuenta = "2";	//	Tipo cuenta inicializada en 1 (Pasivo)
		String format14d = "%014d"; // 	formato 0000000000000 
        String format2d = "%02d";	// 	formato 00

        // Formatos de fecha
        SimpleDateFormat sdfyyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");// Formato de entrada
        SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 	// Salida de interfaz Fecha de Informacion Contable YYYYMMDD
        SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("yyMMdd"); 		// Nombre de los archivos YYDDMM
        SimpleDateFormat sdfddMMyyyy = new SimpleDateFormat("dd/MM/yyyy"); 	// Salida en interfaz Fecha de Listado Detalle DD/MM/YYYY
        SimpleDateFormat sdfyyyyMM = new SimpleDateFormat("yyyy-MM"); 		// Salida en interfaz Fecha de Listado Detalle YYYY-MM
        SimpleDateFormat sdfHHmmss = new SimpleDateFormat("HH:mm:ss"); 		// Salida en interfaz Hora de Listado Detalle HH:mm:ss        
        
        Date hoy = new Date();
		if (entrada.p_Fecha == null) {
			entrada.p_Fecha = sdfyyyy_MM_dd.format(hoy);
		}

		// Asignacion de formatos en Fechas
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);
		String fechayymmdd = sdfyyMMdd.format(pFecha);
		String fechaddmmyyyy  = sdfddMMyyyy.format(pFecha);
		String fechayyyymm  = sdfyyyyMM.format(pFecha);
		String horaHHmmss  = sdfHHmmss.format(hoy);
		
		// Calcula el valor de TMonto para la salida del archivo Informacion Contable
		String tmonto = calcularTMonto(pFecha);
		
		// Configuracion para salida de archivo      
		salida.P_OUT_NOMBRE_ARCHIVO = "C08CBC" + fechayymmdd + ".txt";
		salida.P_OUT_NOMBRE_ARCHIVO_LOG = "C08CBC" + fechayymmdd + "DET.log";		
		salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		salida.P_OUT_RUTA_ARCHIVO_LOG = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		
		File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
		File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
		
		BufferedWriter bwArchivoInfContable = new BufferedWriter(new FileWriter(archivo));
		BufferedWriter bwArchivoListDetalle = new BufferedWriter(new FileWriter(archivoLog));
		
		// Busca la informacion de la corredora 
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DatoCorredor> datoCorredor = sql.query(con, SQL_CONSULTAR_CORREDOR, params, DatoCorredor.class);
		
		// Busca la informacion de las cuentas contables
		List<SaldosCuentas> cuentas = sql.query(con, SQL_SELECT_SALDOS_CUENTAS_CONTABLES, new HashMap<String,Object>(), SaldosCuentas.class);
		Map<String, SaldosCuentas> cSaldos =  CalcularSaldoCuenta(cuentas);		
		
		// Cabecera de arhivo de interfaz C08CBC Listado Detalle
		bwArchivoListDetalle.write("==================== Pagina 1 ========================================================================");
		bwArchivoListDetalle.write("\r\n");
		bwArchivoListDetalle.write(String.format("%-17s", datoCorredor.get(0).Nombre) + " CORREDORES DE BOLSA GENERACION CONTABLE GESTION C40        FECHA  : " +  String.format("%10s", fechaddmmyyyy));		
		bwArchivoListDetalle.write("\r\n");
		bwArchivoListDetalle.write(" " + String.format("%-11s", datoCorredor.get(0).Rut) + "                          ===============================        HORA   : " +  String.format("%10s", horaHHmmss));		
		bwArchivoListDetalle.write("\r\n");
		bwArchivoListDetalle.write("CODIGO : C08CBC                          PERIODO : " + fechayyyymm + "                   PAGINA : " + String.format("%10s", "1"));
		bwArchivoListDetalle.write("\r\n");
		bwArchivoListDetalle.write("                                         ARCHIVO : " +  salida.P_OUT_NOMBRE_ARCHIVO);
		bwArchivoListDetalle.write("\r\n");
		bwArchivoListDetalle.write("TIPO   CUENTA       DESCRIPCION                                                          CAPITAL");
		bwArchivoListDetalle.write("\r\n");
		bwArchivoListDetalle.write("------------------------------------------------------------------------------------------------");
		bwArchivoListDetalle.write("\r\n");
		
		// Ordena el Hashmap con el treemap para el loop
		Map<String, SaldosCuentas> t = new TreeMap<String, SaldosCuentas>(cSaldos);
		
		try {

			for (Map.Entry<String, SaldosCuentas> c : t.entrySet()) {
				if (c.getValue().Imprime.equals("S")) {
					StringBuilder a = new StringBuilder("");
					StringBuilder b = new StringBuilder("");
					String cuenta = c.getValue().CuentaContabilidad;
					BigDecimal saldo = c.getValue().Saldo;
					
					// Valida el Saldo capital de acuerdo al tipo de cuenta
					if (c.getValue().TipoCuenta.equals("P")) {
						if (c.getValue().Saldo.compareTo(BigDecimal.ZERO) < 0) {
							saldo = c.getValue().Saldo.multiply(new BigDecimal(-1));
						}
					} else if (c.getValue().TipoCuenta.equals("A")) {
						if (c.getValue().Saldo.compareTo(BigDecimal.ZERO) < 0) {
							saldo = BigDecimal.ZERO;
						}
					} else {
						saldo = BigDecimal.ZERO;
					}
					
					// Crea linea de separacion por grupo de cuentas (Activos y Pasivos)
					if ( cuenta.substring(0,1).equals("2") ) {
						strTipoCuenta = "1";
						bwArchivoListDetalle.write("\r\n");
					}				
					
					// Concatenacion de strings para el archivo C08CBC Informacion Contable
					a.append("MDD");											// (03) Codigo de Aplicacion
					a.append(tmonto);											// (01) Dia multiplo de 4 / 4 de lo contrario 0
					a.append(c.getValue().SBIF);								// (04) Codigo de SBIF:  equivalente a WT-DESCRIPCIONES -> WT-SBIF
					a.append(c.getValue().Banda);	 							// (02) Codigo de Plazo: equivalente a WT-DESCRIPCIONES -> WT-BANDA
					a.append("1");												// (01) Moneda
					a.append(String.format(format14d, saldo.toBigInteger()));	// (14) Capital (Haber - Debe)
					a.append(String.format(format14d, 0)); 						// (14) Intereses
					a.append(String.format(format14d, 0));						// (14) IntProy
					a.append(" ");												// (01) Separacion
					a.append(String.format(format14d, 0));						// (14) ReajDev
					a.append(" ");												// (01) Separacion
					a.append(String.format(format14d, 0));						// (14) ReajProy
					a.append(String.format(format14d, 0));						// (14) Comision
					a.append(" ");												// (01) Volal
					a.append(strTipoCuenta); 									// (01) Flujo (Activo = 1; Pasivo = 2)
					a.append(c.getValue().Codigo);								// (02) Codigo de Oriflu: equivalente a WT-DESCRIPCIONES -> WT-CODIGO			
					a.append("0");												// (01) Proceso
					a.append("COR"); 											// (03) ApliVol
					a.append( String.format(format2d, 0)); 						// (02) FilaCal
					a.append(fechayyyymmdd); 									// (08) Fecha de Informacion Contable YYYYMMDD
					a.append(cuenta.substring(0, 7)); 							// (07) Cuenta Capital - Primeros 7 digitos 
					a.append(cuenta.substring(cuenta.length() - 5)); 			// (05) Cuenta Comision - utlimos 5 digitos
					a.append(" ");												// (01) Codigo constante (blanco)
				
					// Concatenacion de strings para el archivo C08CBC Listado Detalle
					b.append(String.format("%-6s", TipoCuentaContable(strTipoCuenta)));
					b.append(" ");
					b.append(cuenta);				
					b.append(" ");
					b.append(String.format("%-50s", c.getValue().NombreCuenta));				
					b.append(" ");
					b.append(String.format("%26s", String.format(Locale.ROOT, "%,d\r\n", saldo.toBigInteger())));				
					
					// Escribe en archivo
					bwArchivoInfContable.write(a.toString());
					bwArchivoListDetalle.write(b.toString());
					bwArchivoInfContable.write("\r\n");
					bwArchivoListDetalle.write("\r\n");

				}
			}
			salida.P_OUT_COMENTARIO = "Archivo generado exitosamente";
		} catch (Exception e) {
			salida.P_OUT_COMENTARIO = "Archivo no pudo ser Generado";
		}
		
		
		// Cierra los archivos
		if(bwArchivoInfContable !=null){
			bwArchivoInfContable.close();
		}
		if(bwArchivoListDetalle !=null){
			bwArchivoListDetalle.close();
		}		
		return salida;
	}

	// Calcula el Saldo por Cuenta
	private Map<String, SaldosCuentas> CalcularSaldoCuenta(List<SaldosCuentas> s) {
		
		 Map<String, SaldosCuentas> r = s.stream().collect(Collectors.toMap(SaldosCuentas::getCuentaContabilidad,
				 													  		item -> item));
		 BigDecimal disponible = BigDecimal.ZERO;
		 
		 // Si el saldo de la cuenta 110100000000 (01) < 0, acumula el valor para sumarlo a la cuenta 210500000000 (43)
		 // e inicilaiza la cuenta a 110100000000 = 0
		 if (r.get("110100000000").Saldo.compareTo(BigDecimal.ZERO) < 0) {
			 disponible = r.get("110100000000").Saldo;
			 r.get("110100000000").setSaldo(BigDecimal.ZERO);
		 }
		 
		 // Suma a 210500000000 (43) el monto disponible de 110100000000 (01) si este ultimo es negativo 
		 r.get("210500000000").setSaldo(disponible.add(r.get("210500000000").getSaldo()));

		 // Suma a 110404000000 (08) los saldos de 110406000000 (09) y 110410000000 (10)
		 r.get("110404000000").setSaldo(r.get("110404000000").getSaldo().add(r.get("110406000000").getSaldo()).add(r.get("110410000000").getSaldo())); 	

		 // Suma a 110701000000 (13) los saldos de 110702000000 (14) y 110790000000 (15)
		 r.get("110701000000").setSaldo(r.get("110701000000").getSaldo().add(r.get("110702000000").getSaldo()).add(r.get("110790000000").getSaldo()));		
		 
		 // Suma a 110701000000 (18) los saldos de 110801040000 (19), 110801050000 (20), 110801100000 (21), 110801110000 (22) y 110802010000 (23)
		 r.get("110801010000").setSaldo(r.get("110801010000").getSaldo().add(r.get("110801040000").getSaldo()).add(r.get("110801050000").getSaldo()).add(r.get("110801100000").getSaldo()).add(r.get("110801110000").getSaldo()).add(r.get("110802010000").getSaldo()));		
		 
		 // Suma a 210206000000 (37) los saldos de 210207000000 (38) y 210208000000 (39)
		 r.get("210206000000").setSaldo(r.get("210206000000").getSaldo().add(r.get("210207000000").getSaldo()).add(r.get("210208000000").getSaldo()));
		 
		 // Suma a 210601010000 (44) los saldos de 210601020000 (45), 210601030000 (46) y 210601050000 (47)
		 r.get("210601010000").setSaldo(r.get("210601010000").getSaldo().add(r.get("210601020000").getSaldo()).add(r.get("210601030000").getSaldo()).add(r.get("210601050000").getSaldo()));

		 // Suma a 210601010000 (48) los saldos de 210601020000 (49), 210601030000 (50) y 210601050000 (51)
		 r.get("210601040000").setSaldo(r.get("210601040000").getSaldo().add(r.get("210601060000").getSaldo()).add(r.get("210601900000").getSaldo()).add(r.get("210602010000").getSaldo()));		
		 
		 // Multiplica por -1 si el Saldo de la Cuenta Contable 110701000000 (13) es negativo y suma al saldo de la cuenta 210400000000 (42)
		// e inicilaiza la cuenta a 110701000000 (13) = 0
		 
		 if (r.get("110701000000").Saldo.compareTo(BigDecimal.ZERO) < 0) {
			 // 210400000000 (42) Cuenta Contable en Sebra
			 r.get("210400000000").setSaldo(r.get("110701000000").Saldo.multiply(new BigDecimal(-1)).add(r.get("210400000000").Saldo));			
			 r.get("110701000000").setSaldo(BigDecimal.ZERO);	// 110701000000 (13)
		 }
		
		 return r;
	}
	
	// Calcula el valor de TMonto de acuerdo al dia del mes
	// dia 4  = 1; dia 8  = 2; // dia 12 = 3;... asi hasta el 28 = 7, de lo contrario devuelve 0	
	private String calcularTMonto (Date pFecha) {
		Integer esMultiple = 0;
		
		// Captura el dia de la fecha ingresada para calcular el valor de TMonto
		Calendar dia = Calendar.getInstance();
		dia.setTime(pFecha);
		Calendar siguienteDia = (Calendar) dia.clone();
		siguienteDia.add(Calendar.DAY_OF_MONTH, 1);
		
		Integer diaTMonto = dia.get(Calendar.DAY_OF_MONTH);
			
		if (diaTMonto % 4 == 0) {
			esMultiple = diaTMonto / 4; 
		}
		
		if (siguienteDia.get(Calendar.DAY_OF_MONTH) == 1) {
			esMultiple = 8 ;
		}
		return esMultiple.toString();
	}
	
	// Para salida de P_OUT_LIST_DETALLE: A = ACTIVO; P = PASIVO 
	private String TipoCuentaContable (String tipo) {
		String tipoCuenta;
		
		if (tipo.equals("1")) {
			tipoCuenta = "ACTIVO";
		} else if (tipo.equals("2")) {
			tipoCuenta = "PASIVO";
		} else {
			tipoCuenta = "N/A";
		}
		return tipoCuenta;
	}	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		GenerarInterfazC08_Entrada in = gson.fromJson(jin.toString(), GenerarInterfazC08_Entrada.class);
		GenerarInterfazC08_Salida out = logica(conn, in);
		
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
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2022-05-31\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\"}";
			GenerarInterfazC08_Entrada entrada = gson.fromJson(json, GenerarInterfazC08_Entrada.class);
			GenerarInterfazC08InformacionContable demo = new GenerarInterfazC08InformacionContable();
			GenerarInterfazC08_Salida ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");

	}
}


