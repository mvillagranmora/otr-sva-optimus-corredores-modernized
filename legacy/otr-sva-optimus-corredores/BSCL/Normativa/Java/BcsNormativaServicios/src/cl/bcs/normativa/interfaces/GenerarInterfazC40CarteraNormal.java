package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.interfaces.GenerarInterfazICU.EntradaGenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.SalidaGenerarInterfazICU;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazC40CarteraNormal implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazC40CarteraNormal");
	private final String EXTENCION = ".TXT";
	private final String SQL_SELECT_MOVIMIENTOS 	= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40CarteraNormal_SelectMovimientos.sql"		);
	private final String SQL_SELECT_HIS_MOVIMIENTOS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazC40CarteraNormal_HIS_SelectMovimientos.sql"	);
	
	private final String SQL_SELECT_SALDO_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectSaldoCuentaContable.sql");
	private final String SELECT_IDCLIENTE_EMPRESA = "SELECT SN.\"IdCliente\"  FROM \"BSCL.Plataforma.BD::PLA.Empresa\" EMP  LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" SN ON EMP.\"IdSocioDeNegocio\" = SN.\"IdSocioDeNegocio\" WHERE EMP.\"IdEmpresa\" = :IdEmpresa";
	
	public static class EntradaGenerarInterfazC40CarteraNormal{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
		public Boolean  p_Interfaz = true;
		public Boolean  p_EsTodos = false;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGenerarInterfazC40CarteraNormal{
		public String 	p_out_NombreArchivoLog;
		public String 	p_out_RutaArchivoLog;
		public String	p_out_NombreArchivo;
		public String	p_out_RutaArchivo;
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
		public List<String> p_out_ArchivoSecundario;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimientos{
		public Long Folio;
		public String Nemotecnico;
		public BigDecimal ValorMercado;
		public String TipoMoneda;
		public String Banda;
		public Date FechaVencimiento;
		public BigDecimal ValorPresente;
		public BigDecimal TasaMercado;
		public BigDecimal TasaCompra;
		public BigDecimal PrecioPromedio;
		public BigDecimal Cantidad;
		public BigDecimal CantidadGarantia;
		public String TipoInstrumento;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentasContables{
		public String CUENTA;
		public BigDecimal SALDO;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IdClientes{
		public Long IdCliente;
	
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaGenerarInterfazC40CarteraNormal in=gson.fromJson(jin.toString() , EntradaGenerarInterfazC40CarteraNormal.class);
		SalidaGenerarInterfazC40CarteraNormal out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public  SalidaGenerarInterfazC40CarteraNormal logica(Connection con, EntradaGenerarInterfazC40CarteraNormal entrada) throws Exception {
		SalidaGenerarInterfazC40CarteraNormal out = new SalidaGenerarInterfazC40CarteraNormal();
		List<String> archivoSecundario = new ArrayList<>();
		GenerarInterfazICU interfazICU = new GenerarInterfazICU();
		EntradaGenerarInterfazICU entradaICU = new EntradaGenerarInterfazICU();
		SalidaGenerarInterfazICU salidaICU = new SalidaGenerarInterfazICU();
		
		SimpleDateFormat formatoFechaTitulo = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat formatoFechaArchivo = new SimpleDateFormat("yyMMdd");
		Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		String fechaddmmyy = formatoFechaTitulo.format(fecha);
		String fechayymmdd = formatoFechaArchivo.format(fecha);
		DecimalFormat formato = new DecimalFormat( "#,##0.00", new DecimalFormatSymbols(new Locale("pt", "CL"))); 
		BufferedWriter bwArchivoC40CBN = null;
		
		boolean Historico = false;
		Date fechaActual = Fecha.toDate( Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa));
		if (!fechaActual.toString().equalsIgnoreCase(entrada.p_Fecha)) // si no es igual la fecha de entrada entonces se utiliza para ver el historico
		{	Historico = true ;		}
		
		if(entrada.p_Interfaz || entrada.p_EsTodos) {
			out.p_out_NombreArchivo = "C40CBN" + fechayymmdd + EXTENCION;
			out.p_out_RutaArchivo = entrada.p_Ruta+"/" + out.p_out_NombreArchivo;
		
			File fileC40CBN = new File(out.p_out_RutaArchivo);
			bwArchivoC40CBN = new BufferedWriter(new FileWriter(fileC40CBN));
		}
		
		List<String> cuentas= new ArrayList<>();
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
		
		try{
			SQL sql=new SQL();
			//ARCHIVO C40CBN
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			Long Cliente = sql.query(con, SELECT_IDCLIENTE_EMPRESA, params, IdClientes.class).get(0).IdCliente; 
			
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FiltroFecha", entrada.p_Fecha);
			params.put("IdCliente", Cliente);
			List<Movimientos> listDatosC40CBN ;
			if (Historico) {
				listDatosC40CBN = sql.query(con, SQL_SELECT_HIS_MOVIMIENTOS, params, Movimientos.class);
				System.out.print("historica");
			}else {
				listDatosC40CBN = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, Movimientos.class);
				System.out.print("no historica");
			}
			
			
			
			
			HashMap<String,BigDecimal> saldoCuentas = new HashMap<String,BigDecimal>();
			params.clear();
			params.put("Cuentas",cuentas);
			
			List<CuentasContables> listCuentas = sql.query(con, SQL_SELECT_SALDO_CUENTAS_CONTABLES, params, CuentasContables.class);
			
			if(!listCuentas.isEmpty()) {
				for(CuentasContables cuenta:listCuentas) {
					saldoCuentas.put(cuenta.CUENTA, cuenta.SALDO);
				}
				listCuentas.clear();
			}
			//Saldos Totales		
			BigDecimal acumulaInstrumentoPRBC = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoPDBC = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoPRC = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoBR = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoDPF = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoDPR = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoDPRUSD = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoLH = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoMonetario = BigDecimal.ZERO;
			BigDecimal acumulaInstrumentoOtros = BigDecimal.ZERO;

			for(Movimientos movimiento: listDatosC40CBN) {
				ObtenerPlazo plazo = new ObtenerPlazo();
				InObtenerPlazo entradaPlazo = new InObtenerPlazo();
				OutObtenerPlazo salidaPlazo = new OutObtenerPlazo();
				entradaPlazo.p_IdPais = 44L;
				FechasIn f1 = new FechasIn();
				f1.FechaDesde = entrada.p_Fecha;
				f1.FechaHasta = movimiento.FechaVencimiento.toString();
				f1.IdInterno = 0L;
				f1.SoloDiasHabiles = "N";
				entradaPlazo.p_Fechas.add(f1);
				
				salidaPlazo = plazo.logica(con, entradaPlazo);		
				if(entrada.p_Interfaz || entrada.p_EsTodos) {
					StringBuilder c40cbn = new StringBuilder(); 				
					//***REGCERO***
					//c40cbn.append(movimiento.Folio);
					c40cbn.append("0");
					//***NEMO***	
					if(movimiento.Nemotecnico.length() < 10) {
						c40cbn.append(StringUtils.rightPad(movimiento.Nemotecnico, 10, ' '));
					}else {
						c40cbn.append(movimiento.Nemotecnico.substring(0, 10));
					}
					//***VALOR-MERC***
					c40cbn.append((movimiento.ValorMercado.signum() == -1)? "-" : " ");
					c40cbn.append(StringUtils.leftPad(movimiento.ValorMercado.setScale(4, RoundingMode.FLOOR).toString().replace(".", ",").replace("-", ""), 17, '0'));
					//***TIPO-MONED***
				 	c40cbn.append(TipoMoneda(movimiento.TipoMoneda));
				 	//***BANDA***
				 	c40cbn.append(Banda( Long.valueOf(movimiento.Banda) ));
				 	//***FECHA-VCTO***
				 	c40cbn.append(movimiento.FechaVencimiento);
				 	//***VALOR-PTE***
				 	c40cbn.append((movimiento.ValorPresente.signum() == -1)? "-" : " ");
				 	c40cbn.append( StringUtils.leftPad(movimiento.ValorPresente.setScale(4, RoundingMode.FLOOR).toString().replace(".", ",").replace("-", ""), 17, '0'));
				 	//***TASA-MERCADO***
				 	c40cbn.append((movimiento.TasaMercado.signum() == -1)? "-" : " ");
				 	//c40cbn.append("|");
				 	c40cbn.append(StringUtils.leftPad(movimiento.TasaMercado.setScale(4, RoundingMode.FLOOR).toString().replace(".", ",").replace("-", ""), 9, '0'));
				 	//***TASA-COMPRA***
				 	c40cbn.append((movimiento.TasaCompra.signum() == -1)? "-" : " ");
				 	c40cbn.append(StringUtils.leftPad(movimiento.TasaCompra.setScale(4, RoundingMode.FLOOR).toString().replace(".", ",").replace("-", ""), 9, '0'));

					bwArchivoC40CBN.write(c40cbn.toString());
					bwArchivoC40CBN.write("\r\n");
			 	}
				//Acumula TotalCartera por nemo
				if(movimiento.Nemotecnico.startsWith("PRBC")) {
					acumulaInstrumentoPRBC = acumulaInstrumentoPRBC.add(movimiento.ValorPresente);
				 } else if(movimiento.Nemotecnico.startsWith("PDBC")) {
					 acumulaInstrumentoPDBC = acumulaInstrumentoPDBC.add(movimiento.ValorPresente);
				 } else if(movimiento.Nemotecnico.startsWith("PRC") || movimiento.Nemotecnico.startsWith("CERO")) {
					 acumulaInstrumentoPRC = acumulaInstrumentoPRC.add(movimiento.ValorPresente);
				 } else if(movimiento.Nemotecnico.startsWith("BR") || movimiento.Nemotecnico.startsWith("BCP")|| movimiento.Nemotecnico.startsWith("BCU") || movimiento.Nemotecnico.startsWith("BCD") || movimiento.Nemotecnico.startsWith("BCX")) {
					 acumulaInstrumentoBR = acumulaInstrumentoBR.add(movimiento.ValorPresente);
				 } else if(movimiento.Nemotecnico.startsWith("DPF") || movimiento.Nemotecnico.startsWith("PAGARE NR")) {
					 acumulaInstrumentoDPF = acumulaInstrumentoDPF.add(movimiento.ValorPresente);
				 } else if(movimiento.Nemotecnico.startsWith("DPR") || movimiento.Nemotecnico.startsWith("PAGARE R")) {
					 if(movimiento.TipoMoneda.equals("USD")) {
						 acumulaInstrumentoDPRUSD = acumulaInstrumentoDPRUSD.add(movimiento.ValorPresente);
					 }else {
						 acumulaInstrumentoDPR = acumulaInstrumentoDPR.add(movimiento.ValorPresente);
					 } 
				 } else if(movimiento.Nemotecnico.startsWith("LH")) {
					 acumulaInstrumentoLH = acumulaInstrumentoLH.add(movimiento.ValorPresente);
				 } else if(movimiento.TipoInstrumento.equals("MON")) {
					 acumulaInstrumentoMonetario = acumulaInstrumentoMonetario.add(((movimiento.Cantidad.add(movimiento.CantidadGarantia)).multiply(movimiento.PrecioPromedio)));
				 } else {
					 acumulaInstrumentoOtros = acumulaInstrumentoOtros.add(movimiento.ValorPresente);
				 }
			}// Fin for
			
			//Archivo ICU
			if(saldoCuentas.containsKey("110301070000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total PRBC", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoPRBC)	, 27, " ") + "  11.03.01.070.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110301070000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPRBC.subtract(saldoCuentas.get("110301070000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total PRBC", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoPRBC)	, 27, " ") + "  11.03.01.070.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPRBC), 25, " "));
			}
			if(saldoCuentas.containsKey("110301160000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total PDBC", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoPDBC) , 27, " ") + "  11.03.01.160.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110301160000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPDBC.subtract(saldoCuentas.get("110301160000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total PDBC", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoPDBC)	, 27, " ") + "  11.03.01.160.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPDBC), 25, " "));
			}
			if(saldoCuentas.containsKey("110301200000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total PRC-CERO", 28, ' ') + StringUtils.leftPad(	formato.format(acumulaInstrumentoPRC) , 27, " ") + "  11.03.01.200.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110301200000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPRC.subtract(saldoCuentas.get("110301200000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total PRC-CERO", 28, ' ') + StringUtils.leftPad(	formato.format(acumulaInstrumentoPRC) , 27, " ") + "  11.03.01.200.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPRC), 25, " "));
			}
			if(saldoCuentas.containsKey("110301900000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total BR-BCP-BCU-BCD-BCX", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoPRBC) , 27, " ") + "  11.03.01.900.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110301900000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPRBC.subtract(saldoCuentas.get("110301900000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total BR-BCP-BCU-BCD-BCX", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoPRBC) , 27, " ") + "  11.03.01.900.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoPRBC), 25, " "));
			}
			if(saldoCuentas.containsKey("110302100000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total DPF", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoDPF) , 27, " ") + "  11.03.02.100.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110302100000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoDPF.subtract(saldoCuentas.get("110302100000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total DPF", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoDPF) , 27, " ") + "  11.03.02.100.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoDPF), 25, " "));
			}
			if(saldoCuentas.containsKey("110302200000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total DPR", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoDPR) , 27, " ") + "  11.03.02.200.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110302200000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoDPR.subtract(saldoCuentas.get("110302200000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total DPR", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoDPR) , 27, " ") + "  11.03.02.200.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoDPR), 25, " "));
			}
			if(saldoCuentas.containsKey("110302300000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total DPR US$", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoDPRUSD) , 27, " ") + "  11.03.02.300.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110302300000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoDPRUSD.subtract(saldoCuentas.get("110302300000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total DPR US$", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoDPRUSD) , 27, " ") + "  11.03.02.300.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoDPRUSD), 25, " "));
			}
			if(saldoCuentas.containsKey("110302400000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total L.H.", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoLH) , 27, " ") + "  11.03.02.400.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110302400000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoLH.subtract(saldoCuentas.get("110302400000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total L.H.", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoLH) , 27, " ") + "  11.03.02.400.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoLH), 25, " "));
			}
			if(saldoCuentas.containsKey("110203010000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total Monetario", 28, ' ') + StringUtils.leftPad(formato.format(acumulaInstrumentoMonetario), 27, " ") + "  11.02.03.010.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110203010000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoMonetario.subtract(saldoCuentas.get("110203010000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total Monetario", 28, ' ') + StringUtils.leftPad(formato.format(acumulaInstrumentoMonetario), 27, " ") + "  11.02.03.010.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoMonetario), 25, " "));
			}
			if(saldoCuentas.containsKey("110302900000")) {
				archivoSecundario.add(StringUtils.rightPad("    Total Otros Renta Fija", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoOtros) , 27, " ") + "  11.03.02.900.000" + StringUtils.leftPad(formato.format(saldoCuentas.get("110302900000")), 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoOtros.subtract(saldoCuentas.get("110302900000"))), 25, " "));
			}else {
				archivoSecundario.add(StringUtils.rightPad("    Total Otros Renta Fija", 28, ' ') + StringUtils.leftPad( formato.format(acumulaInstrumentoOtros) , 27, " ") + "  11.03.02.900.000" + StringUtils.leftPad("0,00", 25, " ") + StringUtils.leftPad(formato.format(acumulaInstrumentoOtros), 25, " "));
			}
			
			if(entrada.p_Interfaz && !entrada.p_EsTodos) {
				entradaICU.p_ArchivoSecundario = archivoSecundario;
				entradaICU.p_Fecha = entrada.p_Fecha;
				entradaICU.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaICU.p_IdModulo = entrada.p_IdModulo;
				entradaICU.p_IdUsuario = entrada.p_IdUsuario;
				entradaICU.p_Llamado = "C40-CBN";
				entradaICU.p_Ruta = entrada.p_Ruta;
				salidaICU = interfazICU.logica(con, entradaICU);
				out.p_out_NombreArchivoLog = salidaICU.p_out_NombreArchivoLog;
				out.p_out_RutaArchivoLog = salidaICU.p_out_RutaArchivoLog;
			}
			out.p_out_ArchivoSecundario = archivoSecundario;
			
		}catch(Exception e){
			log.write(e); 
			 out.p_out_Comentario = "Archivo no pudo ser Generado";
			 out.p_out_ErroresContador = 1L;
		}finally{ 
			 if(bwArchivoC40CBN !=null){
				 bwArchivoC40CBN.close();
			 }
			 out.p_out_Comentario = "Archivo generado exitosamente";
			 out.p_out_ErroresContador = 0L;
			 log.writeAsJson("SALIDA",out);
		}	
		return out;
	}
	
	public String Banda(Long diasDiferencia) {
		String banda = "";
		if(diasDiferencia < 31) {// DISPONIBLE HASTA UN MES
	 		banda = " 1";
	 	}else if(diasDiferencia > 30 && diasDiferencia < 91) {//MAS DE 1 MES HASTA 3 MESES
	 		banda = " 2";
	 	}else if(diasDiferencia > 90 && diasDiferencia < 181) {//MAS DE 3 MESES HASTA 6 MESES
	 		banda = " 3";
	 	}else if(diasDiferencia > 180 && diasDiferencia < 271) {//MAS DE 6 MESES HASTA 9 MESES
	 		banda = " 4";
	 	}else if(diasDiferencia > 270 && diasDiferencia < 366) {//MAS DE 9 MESES HASTA 1 ANIO
	 		banda = " 5";
	 	}else if(diasDiferencia > 365 && diasDiferencia < 731) {//MAS DE 1 ANIO HASTA 2 ANIOS
	 		banda = " 6";
	 	}else if(diasDiferencia > 730 && diasDiferencia < 1096) {//MAS DE 2 ANIOS HASTA 3 ANIOS
	 		banda = " 7";
	 	}else if(diasDiferencia > 1095 && diasDiferencia < 1461) {//MAS DE 3 ANIOS HASTA 4 ANIOS
	 		banda = " 8";
	 	}else if(diasDiferencia > 1460 && diasDiferencia < 1826) {//MAS DE 4 ANIOS HASTA 5 ANIOS
	 		banda = " 9";
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
	
	public String TipoMoneda(String TipoReajuste) {
		String tipoMoneda = "";
		if(TipoReajuste.equals("CLP")) {
			tipoMoneda = "1";
		}else if(TipoReajuste.equals("UF") || TipoReajuste.equals("IVP")) {
			tipoMoneda = "2";
		}else if(TipoReajuste.equals("USD")) {
			tipoMoneda = "3";
		}else {
			tipoMoneda = "0";
		}
		return tipoMoneda;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection() ) {
			
			EntradaGenerarInterfazC40CarteraNormal entrada= new EntradaGenerarInterfazC40CarteraNormal();
				entrada.p_IdEmpresa		= new Long(1);
				entrada.p_Fecha			= "2022-05-31";
				entrada.p_Ruta			= "C:\\BCS\\logs";
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				GenerarInterfazC40CarteraNormal demo= new GenerarInterfazC40CarteraNormal();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}