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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazC40INT implements JavaCall {
	// Interfaz C40 - INT
	public static final String CODIGO_MODULO = "COR";	
	private final String SQL_SELECT_INFORMACION = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40INT_SelectInformacionCarteras.sql");

	public static class GenerarInterfazC40INTEntrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
	}
	
	public static class GenerarInterfazC40INTSalida {
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public GenerarInterfazC40INTSalida() {
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatoCorredor {
		public String 	Nombre;
		public String 	Rut;
	}
	public static class InformacionC40 {
		public String 		Banda;
		public Long 		IdOperacion; 
		public String 		IdTipoOperacion; 
		public String 		Nemotecnico;
		public String 		IdTipoReajuste;
		public Date 	FechaOperacion;
		public Date 	FechaVencimiento;
		public BigDecimal 	IngMontoValorizado;
		public Long 		PlazoEnDias;
		public BigDecimal 	Tasa;
		public BigDecimal 	TasaMercado;		
		public BigDecimal 	Capital;
		public String 		Cuenta;
		public String 		CuentaCom;
		public BigDecimal 	Interes;
		public BigDecimal 	IntProy;
		public String 		Moneda;
		public BigDecimal   Readev;
		public String       Partida;
	}
	
	public GenerarInterfazC40INTSalida logica(Connection con, GenerarInterfazC40INTEntrada entrada) throws Exception {
		GenerarInterfazC40INTSalida salida = new GenerarInterfazC40INTSalida();

        // Formatos de fecha
        SimpleDateFormat sdfyyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");// Formato de entrada
        SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 	// Salida de interfaz Fecha de Informacion Contable YYYYMMDD
        SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("yyMMdd"); 		// Nombre de los archivos YYDDMM
        SimpleDateFormat sdfddMMyyyy = new SimpleDateFormat("dd/MM/yyyy"); 	// Salida en interfaz Fecha de Listado Detalle DD/MM/YYYY
        SimpleDateFormat sdfyyyyMM = new SimpleDateFormat("yyyy-MM"); 		// Salida en interfaz Fecha de Listado Detalle YYYY-MM
        SimpleDateFormat sdfHHmmss = new SimpleDateFormat("HH:mm:ss"); 		// Salida en interfaz Hora de Listado Detalle HH:mm:ss        
        
       
		if (entrada.p_Fecha == null ||  entrada.p_Fecha.equals("")  ) {
			String fechaSistema = Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa);
			Date fechaActual = Fecha.toDate(fechaSistema);
			entrada.p_Fecha = fechaActual.toString();
		}

		// Asignacion de formatos en Fechas
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);

		String fechayymmdd = sdfyyMMdd.format(pFecha);

		// Configuracion para salida de archivo      
		salida.P_OUT_NOMBRE_ARCHIVO = "C40COR" + fechayymmdd + ".Txt"; 
		salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		
		File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
	
		BufferedWriter bwArchivoInfContable = new BufferedWriter(new FileWriter(archivo));
		
		// Busca la informacion de la corredora 
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);

		// Ordena el Hashmap con el treemap para el loop
		try {			
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FiltroFecha", entrada.p_Fecha);
			List<InformacionC40> listDatosC40CBN = sql.query(con, SQL_SELECT_INFORMACION, params, InformacionC40.class);
			
			int partida = 0;//3115
			String cuenta = null;
			String libro = "01";
			for(InformacionC40 movimiento: listDatosC40CBN) {				
				// ObtenerPlazo para calcular banda  
				ObtenerPlazo plazo = new ObtenerPlazo();
				InObtenerPlazo entradaPlazo = new InObtenerPlazo();
				OutObtenerPlazo salidaPlazo = new OutObtenerPlazo();
				entradaPlazo.p_IdPais = 44L;
				FechasIn f1 = new FechasIn();
				f1.FechaDesde = entrada.p_Fecha;
				f1.FechaHasta = sdfyyyy_MM_dd.format(movimiento.FechaVencimiento);
				f1.IdInterno = 0L;
				f1.SoloDiasHabiles = "S";
				entradaPlazo.p_Fechas.add(f1);
				salidaPlazo = plazo.logica(con, entradaPlazo);	

				if (movimiento.IdTipoOperacion.equalsIgnoreCase("COMPAC")) {
					cuenta = "210202000000";
					partida = 3115;
				}else if (movimiento.IdTipoOperacion.equalsIgnoreCase("VENPAC")) {
					cuenta = "210201000000"; 
					partida =  1695;
				}
					StringBuilder a = new StringBuilder("");
				
					// Concatenacion de strings para el archivo C08CBC Informacion Contable
					a.append(CODIGO_MODULO);													//(03) Codigo de Aplicacion    VALOR FIJO
					a.append("1");																//(01) Tasa  VALOR FIJO
					a.append(String.format("%.4s", movimiento.Partida)); 						//(04) Partida  LOGICA
					a.append(String.format("%.2s",Banda(salidaPlazo.p_out_Fechas.get(0).Plazo)));//(02) Banda        //banda+++ llama funcion que calcula la banda           
					a.append(String.format("%.3s", movimiento.Moneda)); 						//(03) Moneda                  
					a.append(String.format("%014d", movimiento.Capital.toBigInteger()));        //(14) Capital                 				
					a.append(String.format("%014d", movimiento.Interes.toBigInteger()));		//(14) Intereses   
					a.append(String.format("%014d",movimiento.IntProy.toBigInteger()));			//(14) Intproy				   
					if (movimiento.Readev.compareTo(BigDecimal.ZERO) >= 0) {
						a.append(" "); 															//(01) Signo1    
					}else {
						a.append("-"); 															//(01) Signo1    
					}
					a.append(String.format("%014d",movimiento.Readev.toBigInteger()));			//(14) Readev				  
					a.append(" "); //(01) Signo2                  
					a.append("00000000000000");				 									//(14) Readpro valor fijo      VALOR FIJO
					a.append("00000000000000");													//(14) Comision valor fijo     VALOR FIJO
					a.append("0");																//(01) Tcomput valor fijo      VALOR FIJO					
					a.append(String.format("%-12s",movimiento.Cuenta));							//(12) Cuentas  buscar segun instrumentos     ?				
					a.append(movimiento.CuentaCom);												//(02) Cuenta Com              VALOR FIJO
					if (movimiento.FechaOperacion == null ) {
						a.append("00010101");					//(08) FechaOpe				   BASE DE DATOS
					}else {
						a.append(sdfyyyyMMdd.format(movimiento.FechaOperacion));
					}
					a.append(libro);															//(02) Libro                   VALOR FIJO
					a.append("    "); 															//(04) Filler                  VALOR FIJO
					// Escribe en archivo
					bwArchivoInfContable.write(a.toString());
					bwArchivoInfContable.write("\r\n");

			}
			salida.P_OUT_COMENTARIO = "Archivo generado exitosamente";
		} catch (Exception e) {
			salida.P_OUT_COMENTARIO = "Archivo no pudo ser Generado";
		}

		// Cierra el archivo
		if(bwArchivoInfContable !=null){
			bwArchivoInfContable.close();
		}
			
		return salida;
	}
	public String Banda(Long diasDiferencia) {
		String banda = "";
		if(diasDiferencia < 31) {// DISPONIBLE HASTA UN MES
	 		banda = "01";
	 	}else if(diasDiferencia > 30 && diasDiferencia < 91) {//MAS DE 1 MES HASTA 3 MESES
	 		banda = "02";
	 	}else if(diasDiferencia > 90 && diasDiferencia < 181) {//MAS DE 3 MESES HASTA 6 MESES
	 		banda = "03";
	 	}else if(diasDiferencia > 180 && diasDiferencia < 271) {//MAS DE 6 MESES HASTA 9 MESES
	 		banda = "04";
	 	}else if(diasDiferencia > 270 && diasDiferencia < 366) {//MAS DE 9 MESES HASTA 1 ANIO
	 		banda = "05";
	 	}else if(diasDiferencia > 365 && diasDiferencia < 731) {//MAS DE 1 ANIO HASTA 2 ANIOS
	 		banda = "06";
	 	}else if(diasDiferencia > 730 && diasDiferencia < 1096) {//MAS DE 2 ANIOS HASTA 3 ANIOS
	 		banda = "07";
	 	}else if(diasDiferencia > 1095 && diasDiferencia < 1461) {//MAS DE 3 ANIOS HASTA 4 ANIOS
	 		banda = "08";
	 	}else if(diasDiferencia > 1460 && diasDiferencia < 1826) {//MAS DE 4 ANIOS HASTA 5 ANIOS
	 		banda = "09";
	 	}else if(diasDiferencia > 1825 && diasDiferencia < 2556) {//MAS DE 5 ANIOS HASTA 7 ANIOS
	 		banda = "10";
	 	}else if(diasDiferencia > 2555 && diasDiferencia < 3651) {//MAS DE 7 ANIOS HASTA 10 ANIOS
	 		banda = "11";
	 	}else if(diasDiferencia > 3650 && diasDiferencia < 5476) {//MAS DE 10 ANIOS HASTA 15 ANIOS
	 		banda = "12";
	 	}else if(diasDiferencia > 5475 && diasDiferencia < 7301) {//MAS DE 15 ANIOS HASTA 20 ANIOS
	 		banda = "13";
	 	}else {//MAS DE 20 ANIOS
	 		banda = "14";
	 	}
		return banda;
	}

	private String formatMonto(BigDecimal monto) {
			String capital = String.format("%14s", String.format("%14s", formatoNumero(monto, "##0.0000", false)).replace(" ", "0"));
			return capital;
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
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		GenerarInterfazC40INTEntrada in = gson.fromJson(jin.toString(), GenerarInterfazC40INTEntrada.class);
		GenerarInterfazC40INTSalida out = logica(conn, in);
		
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
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2019-11-29\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\"}";
			GenerarInterfazC40INTEntrada entrada = gson.fromJson(json, GenerarInterfazC40INTEntrada.class);
			GenerarInterfazC40INT demo = new GenerarInterfazC40INT();
			GenerarInterfazC40INTSalida ObjOut = demo.logica(conn, entrada);
			System.out.println(ObjOut.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== FIN ==");

	}
}


