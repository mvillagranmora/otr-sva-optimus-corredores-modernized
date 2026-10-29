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
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazSIG implements JavaCall{

	static final String NombreArchivo ="AUDSIG";
	static final Logger logger = LogManager.getLogger(GenerarInterfazSIG.class.getName());
	
	/* QUERY */
	private final String SQL_SIG_SELECT_PORTAFOLIOS_CTA_PROPIA 	= "SELECT \"IdPortafolio\" FROM BSCL.\"BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia\" WHERE \"IdEmpresa\" = :IdEmpresa";
	private final String SQL_SIG_SELECT_MOV_PACTOS 				= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazSIG_SelectMovimientosPactos.sql");	
	private final String SQL_SIG_SELECT_MOV_INTERMEDIACION 		= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazSIG_SelectMovimientosIntermediacion.sql");
	private final String SQL_SIG_SELECT_COMPRA_DOLAR 			= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazSIG_SelectMovimientosCompraDolar.sql");
	private final String SQL_SIG_SELECT_RUT_COMPRA_DOLAR 		= loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazSIG_SelectRutCompraDolar.sql");
	private final String SQL_CONSULTAR_FERIADOS 				= "SELECT \"IdFeriado\",	\"Tipo\",	\"Fecha\",	\"Dia\",	\"DescripcionDia\",	\"AgnoCarga\",	\"IdPais\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdPais\" IN (:IdPais) 	AND \"IdEstado\" = 'ACT';";
	
	public static class EntradaGenerarInterfazSIG{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario; 
		public String 	p_IdModulo;
		public String	p_Fecha;
		public String	p_Ruta;
	}
	
	private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

	
	public static class SalidaGenerarInterfazSIG{
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public SalidaGenerarInterfazSIG() {
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosSIG{
		//String FechaInforme;
		public Long 		NumeroOperacion;
		public String 		LinDet;
		public String 		Producto;
		public String 		Mercado;
		public String 		FechaInicio;
		public String 		FechaVencimiento;
		public Long 		Plazo;
		public String 		RutCliente;
		public String 		RutEmisor;
		public String 		MonedaEmisor;
		public String 		MonedaPacto;
		public BigDecimal 	Tasatransferencia;
		public BigDecimal 	TasaTransaccion;
		public BigDecimal 	TasaOperacion;
		public String 		BASTAS;
		public String 		Sucursal;
		public BigDecimal 	MontoCompraVenta;
		public BigDecimal 	ValorPresente;
		public BigDecimal 	Interes;
		public BigDecimal 	Reajuste;
		public BigDecimal 	MontoTransaccion;
		public BigDecimal 	Comision;
		public BigDecimal 	UtilidaPerdidaDiferenciaPrecio;
		public BigDecimal 	PrecioCompraVenta;
		public BigDecimal 	UtilidadPerdidaOperacionCambio;
		public BigDecimal 	UtilidadPerdidaSimultanea;
		
		
		public Long					IdInstrumento; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public String				Nemotecnico; 
		public String				InsFechaVencimiento;
		public String				InsFechaEmision;
		public String				TieneCorteDeCupon;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				Emisor;
		public String				IdCondicionDeLiquidacion;
		public String				FechaDeLiquidacion;	
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate;		
		public BigDecimal			MontoCompra;						//en moneda del instrumento ( se usa para el calculo de interes)
		public BigDecimal			MontoCompraMonedaSistema;
		public BigDecimal			MontoMercadoMonedaSistema;
		public BigDecimal			MontoCompraSubyacente;
		public BigDecimal			MontoCompraGarantia;
		public String				IdTipoOperacion;
		//-----------------------------------------------------------
		//public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			PrecioMercado; 
		public BigDecimal			TasaMercado;		
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia;
		public BigDecimal			TasaTIF;
		public BigDecimal			TasaCorredora;
		public String				FechaOperacion;
		public String			    FechaVencimientoOperacion;
		public BigDecimal			TasaAnterior;
		public BigDecimal			TasaActual;
		public BigDecimal			MontoMoneda;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosSIG() {};
	}
	public static class DatoRut{
		public String  Rut;
	}
	public static class PortafoliosCtaPropia{
		public Long  IdPortafolio;
	}
	public static class ObjInstrumentoAValorizar {
		public Long					IdValorizacion;
		public String				FechaValorizacion;
		public String				TablaOrigen;
		public Long					IdCustodia;
		public Long					IdCliente; 
		public Long					IdPortafolio;
		public String				EsRegistroNuevo;
		public String				GenerarNuevoFolioCustodia;
		public String				OrigenProcesamiento;
		
		//-----------------------------------------------------------
		public String				FechaOperacion;
		public String				FechaVencimientoOperacion;
		public Long					PlazoEnDiasOperacion;				//Solo para operaciones a plazo	O iif
		public String				IdCondicionDeLiquidacion;
		public String				FechaDeLiquidacion;	
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				FechaVencimiento;
		public String				FechaEmision;
		public String				TieneCorteDeCupon;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public BigDecimal			Cantidad; 
		public BigDecimal			ValorRescate;		
		public BigDecimal			MontoCompra;						//en moneda del instrumento ( se usa para el calculo de interes)
		public BigDecimal			MontoCompraMonedaSistema;
		public BigDecimal			MontoMercadoMonedaSistema;
		public BigDecimal			MontoCompraSubyacente;
		public BigDecimal			MontoCompraGarantia;
		
		//-----------------------------------------------------------
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			PrecioMercado; 
		public BigDecimal			TasaMercado;		
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia;
		public BigDecimal			TasaTIF;
		public BigDecimal			TasaCorredora;

		// MSH
		public String				Emisor;
		public String				NemoSVS;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	};
	public static class DiaFeriado {
		public Long			IdFeriado; 
		public String		Tipo; 
		public java.sql.Date Fecha; 
		public Integer		Dia; 
		public String		DescripcionDia; 
		public Integer		AgnoCarga; 
		public Long			IdPais; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ObjResultadoValorizacion {
		public Long					IdProcesamiento;
		public Long					IdValorizacion;
		public String				TipoValorizacion; 
		public String				SubTipoValorizacion;
		public String				TipoValorizacionInstrumento;
		public String				IdUso; 
		public String				TipoCartera; 
		public String				IdTipoCriterioTasa;
		public java.sql.Date		FechaValorizacion;
		public String				DimensionTiempo;					// AN - AC - PR 
		public String				DimensionTasa;						// COMP - MERC - GTIA - SUBY - CORR - TIFF - P+CM - P+MC - P+GT - P+SY - P-CM - P-MC - P-GT - P-SY   		
		public java.sql.Date		FechaDimemsion;
		public String				ResultadoProceso;
		public String				MensajeProceso;
		public Long					TicketProcesamiento;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public java.sql.Date		FechaOperacion;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				IdClasificacionTipoInstrumento;		
		public Long					IdInstrumento; 
		public String				Nemotecnico;
		public String				Emisor;
		public BigDecimal			Cantidad;
		public BigDecimal			ValorRescate;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
		public String				MensajeErrorFuente;
		public java.sql.Date		FechaPrecioTasa;					//Fecha de la tasa mercado obtenida 
		public String				FuentePrecioTasa;					//Fuente de la tasa mercado obtenida		
		public BigDecimal			Precio;
		public BigDecimal			Tasa;
		public BigDecimal			Tasa360;
		public BigDecimal			PrecioAPlazo;		
		public BigDecimal			TasaAPlazo;	 						//NO ESTAAAAAA
		public String				CalculaIntereses;
		public String				CalculaInteresesAPlazo;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				IdMonedaSistema;
		public BigDecimal			MontoMonedaSistema;								
		public BigDecimal			MontoInteresMonedaSistema;				
		public BigDecimal			MontoInteresAcumuladoMonedaSistema;
		public BigDecimal			MontoReajusteMonedaSistema;				
		public BigDecimal			MontoReajusteAcumuladoMonedaSistema;
		public BigDecimal			MontoDevengadoAMonedaSistema;		
		public BigDecimal			MontoAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAPlazoMonedaSistema;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaSistema;
		//++++++++++++++++++++++++++
		public String				IdMonedaInstrumento;
		public BigDecimal			FactorMonedaInstrumento; 
		public java.sql.Date		FechaFactorMonedaInstrumento;
		public BigDecimal			MontoMonedaInstrumento;							//Total (mon + interes y reajuste)		
		public BigDecimal			MontoInteresMonedaInstrumento;					//Interes diario
		public BigDecimal			MontoInteresAcumuladoMonedaInstrumento;			//Interes desde el ingreso
		public BigDecimal			MontoReajusteMonedaInstrumento;
		public BigDecimal			MontoReajusteAcumuladoMonedaInstrumento;
		public BigDecimal			MontoDevengadoAMonedaInstrumento;				//diferencia entre ma y hoy		
		public BigDecimal			MontoAPlazoMonedaInstrumento;					//desde CalculoInteres
		public BigDecimal			MontoInteresAPlazoMonedaInstrumento;		
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaInstrumento;
		//++++++++++++++++++++++++++
		public String				IdMonedaAdicional;
		public BigDecimal			FactorMonedaAdicional; 
		public java.sql.Date		FechaFactorMonedaAdicional;
		public BigDecimal			MontoMonedaAdicional;				
		public BigDecimal			MontoInteresMonedaAdicional;
		public BigDecimal			MontoInteresAcumuladoMonedaAdicional;
		public BigDecimal			MontoReajusteMonedaAdicional;
		public BigDecimal			MontoReajusteAcumuladoMonedaAdicional;
		public BigDecimal			MontoDevengadoAMonedaAdicional;
		public BigDecimal			MontoAPlazoMonedaAdicional;
		public BigDecimal			MontoInteresAPlazoMonedaAdicinal;
		public BigDecimal			MontoInteresAcumuladoAPlazoMonedaAdicional;
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public String				TieneCorteDeCupon;
		public java.sql.Date		FechaCorteCupon;
		public java.sql.Date		FechaPagoCorteCupon;
		public BigDecimal			MontoInteresesPagoCorteCupon;
		public Long					NumeroCupon; 
		public String				IdMonedaPagoCupon;		 
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaSistema;				
		public BigDecimal			MontoCapitalCorteCuponMonedaSistema;		
		public BigDecimal			MontoInteresCorteCuponMonedaSistema; 		
		public BigDecimal			MontoReajusteCorteCuponMonedaSistema;		
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaInstrumento;			//Monto amortizacion + interes
		public BigDecimal			MontoCapitalCorteCuponMonedaInstrumento;	//Monto total sin interes
		public BigDecimal			MontoInteresCorteCuponMonedaInstrumento;	//Interes periodo corte
		public BigDecimal			MontoReajusteCorteCuponMonedaInstrumento;	//Bono reajustado segun la inflacion
		//++++++++++++++++++++++++++
		public BigDecimal			MontoCorteCuponMonedaAdicional; 
		public BigDecimal			MontoCapitalCorteCuponMonedaAdicional; 
		public BigDecimal			MontoInteresCorteCuponMonedaAdicional; 
		public BigDecimal			MontoReajusteCorteCuponMonedaAdicional;
		//++++++++++++++++++++++++++
		public BigDecimal			Duration;
		public BigDecimal			Convexidad;
		public BigDecimal			PlazoValorizador;
		public String				ValorTieneCall;
		public BigDecimal			TasaEmision;
		public String				ValorClasificacionRiesgo;

		public String				IdTipoReajuste;
		public BigDecimal			ValorTipoReajuste;		
		public String				NemoOriginal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};	
	
	public List<String> diasAEjecutarInterfaz(Connection conn, SQL sql, String fechaSistemaYYYYMMDD, Long idPaisEmpresa) throws Exception {

	    LocalDate fechaSistema = LocalDate.parse(fechaSistemaYYYYMMDD, YYYYMMDD);
	    HashMap<String, Object> parmsFeriado = new HashMap<String, Object>();
	    parmsFeriado.put("IdPais", idPaisEmpresa);
	    List<DiaFeriado> listaDiaFeriado = sql.query(conn, SQL_CONSULTAR_FERIADOS, parmsFeriado, DiaFeriado.class);
	    Set<LocalDate> feriados = new HashSet<LocalDate>();
	    for (DiaFeriado f : listaDiaFeriado) {
	        feriados.add(f.Fecha.toLocalDate());
	    }
	    return obtenerDiasAProcesar(fechaSistema, feriados);
	}
	
	private List<String> obtenerDiasAProcesar(LocalDate fechaSistema, Set<LocalDate> feriados) {
	    YearMonth ym = YearMonth.from(fechaSistema);
	    LocalDate primerDiaMes = ym.atDay(1);
	    LocalDate ultimoDiaMes = ym.atEndOfMonth();
	    List<LocalDate> dias = new ArrayList<LocalDate>();
	    List<LocalDate> haciaAtras = new ArrayList<LocalDate>();
	    LocalDate cursorAtras = fechaSistema.minusDays(1);
	    while (!cursorAtras.isBefore(primerDiaMes)) {
	        if (esHabil(cursorAtras, feriados)) {
	            break; 
	        }
	        haciaAtras.add(0, cursorAtras); 
	        cursorAtras = cursorAtras.minusDays(1);
	    }
	    dias.addAll(haciaAtras);
	    dias.add(fechaSistema);
	    boolean esUltimoDiaHabilDelMes = true;
	    LocalDate cursorCheck = fechaSistema.plusDays(1);
	    while (!cursorCheck.isAfter(ultimoDiaMes)) {
	        if (esHabil(cursorCheck, feriados)) {
	            esUltimoDiaHabilDelMes = false;
	            break;
	        }
	        cursorCheck = cursorCheck.plusDays(1);
	    }
	       if (esUltimoDiaHabilDelMes && !fechaSistema.equals(ultimoDiaMes)) {
	        LocalDate cursorAdelante = fechaSistema.plusDays(1);
	        while (!cursorAdelante.isAfter(ultimoDiaMes)) {
	            dias.add(cursorAdelante);
	            cursorAdelante = cursorAdelante.plusDays(1);
	        }
	    }
	       List<String> resultado = new ArrayList<String>();
	    for (LocalDate d : dias) {
	        resultado.add(d.format(YYYYMMDD));
	    }
	    return resultado;
	}
	public static void EscribirRegistro(List<DatosSIG> data,String Fechaaaaammdd,BufferedWriter Archivo ,BufferedWriter ArchivoLOG,List<DatoRut> lista) throws IOException {
		
		for(DatosSIG r: data) {
			StringBuilder r1 = new StringBuilder();
			
			r1.append(Fechaaaaammdd); 												//INTSIG-FEC-INF (Fecha Informe)
			r1.append(String.format("%010d", r.NumeroOperacion));					//INTSIG-NUM-OPE (Numero Operacion) 
			r1.append(String.format("%2s", r.LinDet));								//INTSIG-LIN-DET 
			r1.append(String.format("%4s", r.Producto));							//INTSIG-PRODUCTO
			r1.append(String.format("%2s", r.Mercado));								//INTSIG-MERCADO
			r1.append(String.format("%8s", r.FechaInicio));							//INTSIG-FEC-INI (Fechainicio)
			r1.append(String.format("%8s", r.FechaVencimiento));					//INTSIG-FEC-VTO (Fecha Vencimiento)
			r1.append(String.format("%05d", r.Plazo));								//INTSIG-PLAZO
			r1.append(String.format("%10s","0"+r.RutCliente.substring(0, r.RutCliente.length()-1)));							//INTSIG-RUT-CLI
			r1.append(String.format("%10s","0"+r.RutEmisor.substring(0, r.RutEmisor.length()-1)));							//INTSIG--RUT-EMI
			r1.append(String.format("%3s", r.MonedaEmisor));						//INTSIG-MON-EMI
			r1.append(String.format("%3s", r.MonedaPacto));							//INTSIG-MON-PAC
			r1.append(" 000.00000");	//TASA-TRANSFERENCIA getFormatAmount(r.Tasatransferencia,10, 5) IMPBESTOPT-987 punto 2
			//r1.append(getFormatAmount(r.TasaTransaccion,10,5));						//INTSIG-TAS-TRA
			r1.append(getFormatAmount(r.TasaOperacion,10,5));						//INTSIG-TAS-OPE
			r1.append(String.format("%3s", r.BASTAS));								//INTSIG-BAS-TAS
			r1.append(String.format("%3s", r.Sucursal));							//INTSIG-SUCURSAL
			r1.append(getFormatAmount(r.MontoCompraVenta,19,2)); 					//INTSIG-MTO-CPA-VTA	valor clp
			r1.append(getFormatAmount( r.ValorPresente,19,2)); 						//INTSIG-VAL-PRE campo 18
			r1.append(getFormatAmount(r.Interes,18,4)); 							//INTSIG-INTERES
			r1.append(getFormatAmount(r.Reajuste,18,4)); 							//INTSIG-REAJUSTE
			r1.append(getFormatAmount(r.MontoTransaccion,19,2)); 					//INTSIG-MTO-TRA
			r1.append(getFormatAmount(r.Comision,18,4)); 							//INTSIG-COMISION
			r1.append(getFormatAmount(r.UtilidaPerdidaDiferenciaPrecio,19,2)); 		//INTSIG-UTI-PER-DIF-PRE
			r1.append(getFormatAmount(r.PrecioCompraVenta,19,2)); 					//INTSIG-PRE-CPA-VTA
			if(r.Producto.equals("MO")&& EstaEnTabla(r,lista)){
				r1.append(getFormatAmount( BigDecimal.ZERO,19,2));
			}else {
				r1.append(getFormatAmount( r.UtilidadPerdidaOperacionCambio.setScale(0,RoundingMode.HALF_UP),19,2));
			}																		//INTSIG-UTI-PER-OPE-CAM
			r1.append(getFormatAmount(r.UtilidadPerdidaSimultanea,19,2)); 			//INTSIG-UTI-PER-SIM	
			Archivo.write(r1.toString());
				Archivo.write("\r\n");
		}
		
	}
	
	public static boolean EstaEnTabla(DatosSIG r, List<DatoRut> listaRut) {
		boolean status= false;
		if(listaRut.isEmpty()) {
			return status;
		}else {
			for (DatoRut rut : listaRut) {
				if(rut.Rut.equals(r.RutCliente)) {
					status = true;
					break;
				}
			}
			return status;
		}
		
	}
	
	public void EncabezadoLog(EntradaGenerarInterfazSIG in ,BufferedWriter Log, Connection conn) throws Exception {
		//Inicio Encabezado Log Interfaz
		Log.write("---------------------------  Inicio Proceso.. ---------------------------");
		Log.write("\r\n");
		Log.write("----------------------- Datos Ingresados ------------------------------");
		Log.write("\r\n");
		Log.write(in.p_Ruta);
		Log.write("\r\n");
		Log.write(in.p_Fecha);
		Log.write("\r\n");
		Log.write("\r\n");
		Log.write("----------------------- Fin Datos Ingresados --------------------------");
		Log.write("\r\n");
		Log.write("Ubicacion del Interfaz SIG :");
		Log.write("\r\n");
		Log.write(in.p_Ruta);
		Log.write("\r\n");
		Log.write("PERIODO (AAAA-MM-DD).......:");
		Log.write("\r\n");
		Log.write(in.p_Fecha);
		Log.write("\r\n");
		//Fin Encabezado Log Interfaz
	}
	public BigDecimal InteresAcumuladoPactos(List<DatosSIG> data) {
		BigDecimal monto = BigDecimal.ZERO;
		for( DatosSIG r: data) {
			if(r.Producto.equals("VRC ")) {
				monto = monto.add(r.Interes);
			}
		}
		return monto;
	}
	
	public void PiePaginaLog(EntradaGenerarInterfazSIG in  ,BufferedWriter Log, Connection conn) throws Exception{
		Log.write("--------------------------------------------------");
		Log.write("\r\n");
		Log.write("Hora Termino   : ");Log.write(HoraActual(in,conn));
		Log.write("\r\n");
		Log.write("Fecha          : ");Log.write(in.p_Fecha);
		Log.write("\r\n");
		Log.write("--------------------------------------------------");
		Log.write("\r\n");
		Log.write("----------------------------  Fin Proceso -----------------------------");
	}
	
	public static String getFormatAmount(BigDecimal amount, Integer longitud, Integer decimales) {
	    String format = "";
	    if (amount == null) {
	        amount = BigDecimal.ZERO;
	    }
	    if (decimales < 0) {
	        decimales = 0;
	    }
	    if (amount.compareTo(BigDecimal.ZERO) == 0) {
	        if (decimales == 0) {
	            format = String.format(" %0" + (longitud - 1) + "d", 0);
	        } else {
	            format = String.format(" %0" + (longitud - 2 - decimales) + "d", 0) + "." + String.format("%0" + decimales + "d", 0);
	        }
	    } else {
	        amount = amount.setScale(decimales, RoundingMode.HALF_UP);
	        String decimals = decimales > 0 ? amount.toString().substring(amount.toString().length() - decimales) : "";
	        if (amount.compareTo(BigDecimal.ZERO) > 0) {
	            format = String.format(" %0" + (longitud - 1 - decimales - (decimales > 0 ? 1 : 0)) + "d", amount.toBigInteger()) + (decimales > 0 ? "." + decimals : "");
	        } else {
	            format = String.format("-%0" + (longitud - 1 - decimales - (decimales > 0 ? 1 : 0)) + "d", amount.abs().toBigInteger()) + (decimales > 0 ? "." + decimals : "");
	        }
	    }
	    return format;
	}

	public String HoraActual(EntradaGenerarInterfazSIG in, Connection conn) throws StoredProcedureException {
		String fechaSistema = Utils.getFechaHoraSistema(conn, "BSCL", in.p_IdEmpresa);
		String phora = fechaSistema.substring(fechaSistema.length()-8,fechaSistema.length());
		return phora;
	}
	
	public OutObtenerDiaAnterior DiaAnterior(String fechayyyymmdd,Connection conn,Long idPaisEmpresa,boolean soloHabil) throws Exception {
		//-------------------------------------------------------------
		//  OBTENER DIA ANTERIOR
		//-------------------------------------------------------------
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior  inObtenerDiaAnterior = new InObtenerDiaAnterior();

		
		List<FechasIn> lstFechas = new ArrayList<FechasIn>();
		FechasIn inFechas = new FechasIn();
		inFechas.fechaInicial 	= fechayyyymmdd.substring(0, 4) + "-"+ fechayyyymmdd.substring(4, 6) +"-"+ fechayyyymmdd.substring(6, 8)  ;
		inFechas.numeroDias 	= 1L;
		lstFechas.add(inFechas);
		String habiles = "";
		if (soloHabil) {habiles = "S"; } else { habiles = "N"; }
		
		inObtenerDiaAnterior.idPais 		 = idPaisEmpresa;
		inObtenerDiaAnterior.diaHabil 		 = habiles;  //al sumar dias, considera solo dias habiles
		inObtenerDiaAnterior.soloCaeDiaHabil = habiles;  //el resultado debe ser un dia habil
		inObtenerDiaAnterior.fechas 		 = lstFechas;
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(conn, inObtenerDiaAnterior);
		return outObtenerDiaAnterior;
	}

	private boolean esHabil(LocalDate fecha, Set<LocalDate> feriados) {
	    DayOfWeek dow = fecha.getDayOfWeek();
	    boolean esFinDeSemana = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
	    return !esFinDeSemana && !feriados.contains(fecha);
	}
	
	// ------------------------------------------------------------------------------
	// LOGICA
	// ------------------------------------------------------------------------------
	public SalidaGenerarInterfazSIG Logica(Connection conn, EntradaGenerarInterfazSIG in) throws Exception {
		
		SalidaGenerarInterfazSIG salida = new SalidaGenerarInterfazSIG();
		List<String> listArchivos = new ArrayList<>();
	    // valida si la fecha de entrada es nula
		String fechaSistema = Utils.getFechaHoraSistema(conn, "BSCL", in.p_IdEmpresa);
		if (in.p_Fecha.equals("") || in.p_Fecha == null) {
			Date fechaActual = Fecha.toDate(fechaSistema);
			in.p_Fecha = fechaActual.toString();
			System.out.println(fechaSistema);
		}
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		// Asignacion de formato en Fechas
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(in.p_Fecha);
		SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
		String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);

		SimpleDateFormat formateador = new SimpleDateFormat("yyMMdd"); 
		String fechaArchivo = formateador.format(pFecha);
		
		// Configuracion para salida de archivo      
		salida.P_OUT_NOMBRE_ARCHIVO = NombreArchivo + fechaArchivo + ".txt";
		salida.P_OUT_NOMBRE_ARCHIVO_LOG = NombreArchivo + fechayyyymmdd + "DET.log";		
		salida.P_OUT_RUTA_ARCHIVO = in.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		salida.P_OUT_RUTA_ARCHIVO_LOG = in.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		
		listArchivos.add(in.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO);
		
		File archivo = new File(salida.P_OUT_RUTA_ARCHIVO);
		File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
		
		SQL sql = new SQL();

		List<String> listadoFechasProcesar = null;
		listadoFechasProcesar = diasAEjecutarInterfaz(conn, sql, fechayyyymmdd, idPaisEmpresa);

		
		try(BufferedWriter bwArchivoInterfazSIG = new BufferedWriter(new FileWriter(archivo))){
			try(BufferedWriter bwAchivoInterfazSIGLog = new BufferedWriter(new FileWriter(archivoLog))){	
				//Escribe encabezado de Log
				EncabezadoLog(in, bwAchivoInterfazSIGLog, conn);
				String DiaAnteriorHabil = DiaAnterior(fechayyyymmdd, conn, idPaisEmpresa , true).fechas.get(0).fechaCalculada ;
				
				HashMap<String, Object> paramsRut = new HashMap<String, Object>();
				paramsRut.put("IdEmpresa", in.p_IdEmpresa);

				List<PortafoliosCtaPropia> Portafolios = sql.query(conn, SQL_SIG_SELECT_PORTAFOLIOS_CTA_PROPIA , paramsRut , PortafoliosCtaPropia.class);
				List<DatoRut> dataRut = sql.query(conn, SQL_SIG_SELECT_RUT_COMPRA_DOLAR, paramsRut, DatoRut.class);
				
				HashMap<String, Object> params = new HashMap<String, Object>();
		
				for (String fechaProcesada : listadoFechasProcesar)
				{
					fechayyyymmdd = fechaProcesada; 
					params.clear();
					params.put("IdEmpresa", in.p_IdEmpresa);
					params.put("Fecha",fechayyyymmdd);
					params.put("FechaAnterior", DiaAnteriorHabil);
					
					List <DatosSIG> data;
					data = sql.query(conn, SQL_SIG_SELECT_MOV_PACTOS, params, DatosSIG.class);
					
					if(data.isEmpty()) {
						bwAchivoInterfazSIGLog.write("No Existen Movimientos de Pactos Vigentes ");
						bwAchivoInterfazSIGLog.write("\r\n");
					}else {
						bwAchivoInterfazSIGLog.write("PROCESANDO PACTOS VIGENTES");
						bwAchivoInterfazSIGLog.write("\r\n");
						bwAchivoInterfazSIGLog.write("-------------------");
						bwAchivoInterfazSIGLog.write("\r\n");

						List<DatosSIG> dataR = new ArrayList<>();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						
						
						// DiaAnteriorHabil 
						for (DatosSIG Dato : data) {								
							boolean EsDelDia = Dato.FechaOperacion.equals(in.p_Fecha);
							boolean EsCLP = Dato.MonedaEmisor.equals("999");
						    int cantidadDiasPacto 			= Utils.diferenciaEnDias(dateFormat.parse(in.p_Fecha)		, dateFormat.parse(Dato.FechaOperacion));
						    int cantidadDiasPactoInteres 	= Utils.diferenciaEnDias(dateFormat.parse(DiaAnteriorHabil)	, dateFormat.parse(Dato.FechaOperacion));
						    
						    BigDecimal diasBasePacto = Dato.MonedaEmisor.equals("999") ? BigDecimal.valueOf(cantidadDiasPacto).divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP)
						                                                               : BigDecimal.valueOf(cantidadDiasPacto).divide(BigDecimal.valueOf(360), 8, RoundingMode.HALF_UP);
						    BigDecimal diasBasePactoInteres = Dato.MonedaEmisor.equals("999") 	? BigDecimal.valueOf(cantidadDiasPactoInteres).divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP)
	                                															: BigDecimal.valueOf(cantidadDiasPactoInteres).divide(BigDecimal.valueOf(360), 8, RoundingMode.HALF_UP);
						    // Precision of the divisions are adjusted to the Sebra interface	
						    
						    if ( !EsCLP ) {
						    	Dato.PrecioCompraVenta =  Dato.TasaActual;
						    }else if (EsDelDia ) {
						    	Dato.PrecioCompraVenta=  Dato.TasaActual;
						    }else {
						    	Dato.PrecioCompraVenta = BigDecimal.ZERO;
						    }
						    
						    Dato.Reajuste = (EsCLP || EsDelDia ) 	? BigDecimal.ZERO 
					                 								: Dato.MontoMoneda.multiply(Dato.TasaActual.subtract(Dato.TasaAnterior)).setScale(0, RoundingMode.DOWN);	
						    BigDecimal MonedaValorizadaCLP = !EsCLP ? Dato.MontoMoneda.multiply(Dato.TasaActual) : Dato.MontoMoneda;
						    BigDecimal MonedaValorizadaCLPanterior = !EsCLP ? Dato.MontoMoneda.multiply(Dato.TasaAnterior) : Dato.MontoMoneda;

						    Dato.ValorPresente = MonedaValorizadaCLP.multiply(BigDecimal.ONE.add(Dato.TasaOperacion.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
						    		.multiply(diasBasePacto))).setScale(0, RoundingMode.HALF_UP);		
						    
						    BigDecimal VPInteres = MonedaValorizadaCLPanterior.multiply(BigDecimal.ONE.add(Dato.TasaOperacion.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
						    		.multiply(diasBasePactoInteres))).setScale(0, RoundingMode.HALF_UP);
						    Dato.Interes = EsDelDia 	?	BigDecimal.ZERO
							    						:	Dato.ValorPresente.subtract(VPInteres ).subtract(Dato.Reajuste);
						    dataR.add(Dato);	
						    bwAchivoInterfazSIGLog.write("");
						}
						
						
						EscribirRegistro(dataR, fechayyyymmdd, bwArchivoInterfazSIG, bwAchivoInterfazSIGLog, dataRut);
						bwAchivoInterfazSIGLog.write("INTERES ACUMULADO : "); bwAchivoInterfazSIGLog.write(getFormatAmount(InteresAcumuladoPactos(data), 18,4));

						
						bwAchivoInterfazSIGLog.write("\r\n");
						bwAchivoInterfazSIGLog.write("-------------------");
						bwAchivoInterfazSIGLog.write("\r\n");
					}
					bwAchivoInterfazSIGLog.write("TOTAL DE REGISTROS GRABADOS DE PACTOS Vigentes : ");bwAchivoInterfazSIGLog.write(String.format("%010d", data.size()));
					bwAchivoInterfazSIGLog.write("\r\n");
					
					params.clear();
					params.put("IdEmpresa", in.p_IdEmpresa);
					params.put("FechaIng",fechayyyymmdd);

					
					List<Long> longList = Portafolios.stream()
						    .map(portafolio -> portafolio.IdPortafolio)
						    .collect(Collectors.toList());
					
					params.put("CtaPropia",longList);
					
					List <DatosSIG> data2 = sql.query(conn, SQL_SIG_SELECT_MOV_INTERMEDIACION, params, DatosSIG.class);
					params.remove("CtaPropia");
					if(data2.isEmpty()) {
						bwAchivoInterfazSIGLog.write("No Existen Movimientos de Intermediacion RV/RF/IF ");
						bwAchivoInterfazSIGLog.write("\r\n");
					}else {
						bwAchivoInterfazSIGLog.write("PROCESANDO INTERMEDIACION DE Renta Variable, Renta Fija e Intermediacion Financiera");
						bwAchivoInterfazSIGLog.write("\r\n");
						EscribirRegistro(data2, fechayyyymmdd, bwArchivoInterfazSIG, bwAchivoInterfazSIGLog, dataRut);
					}
					
					bwAchivoInterfazSIGLog.write("TOTAL DE REGISTROS GRABADOS DE INTERMEDIACION DE RV/RF/IF : ");bwAchivoInterfazSIGLog.write(String.format("%010d", data2.size()));
					bwAchivoInterfazSIGLog.write("\r\n");
					
					List <DatosSIG> data3 = sql.query(conn, SQL_SIG_SELECT_COMPRA_DOLAR, params, DatosSIG.class);
					if(data3.isEmpty()) {
						bwAchivoInterfazSIGLog.write("No Existen Movimientos de Cambios Dolar ");
						bwAchivoInterfazSIGLog.write("\r\n");
					}else {
						bwAchivoInterfazSIGLog.write("PROCESANDO CAMBIOS DOLAR");
						bwAchivoInterfazSIGLog.write("\r\n");
						EscribirRegistro(data3, fechayyyymmdd, bwArchivoInterfazSIG, bwAchivoInterfazSIGLog, dataRut);
					}
					
					bwAchivoInterfazSIGLog.write("TOTAL DE REGISTROS GRABADOS DE CAMBIOS DOLAR: ");bwAchivoInterfazSIGLog.write(String.format("%010d", data3.size()));
					bwAchivoInterfazSIGLog.write("\r\n");	
				}// for (String fechaProcesada : listadoFechasProcesar)
				
			
				//Inicio pie de pagina Interfaz
				PiePaginaLog(in, bwAchivoInterfazSIGLog, conn);
				//Fin pie de pagina 
				
				salida.P_OUT_COMENTARIO =  Diccionario.getMensaje(conn, in.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
				salida.P_OUT_ARCHIVOS = listArchivos;
			}
			
		}
				
		return salida;
	}
	
	public static class ValorizarSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ValorizarSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};
	

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson= Utils.getGsonBuilder();
		EntradaGenerarInterfazSIG entrada = gson.fromJson(in.toString(), EntradaGenerarInterfazSIG.class);
		SalidaGenerarInterfazSIG out = Logica(conn, entrada);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
 
	
	// -----------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -----------------------------------------------------------
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			System.out.println("== INICIO ==");
			conn = PlataformaUtils.getDESAConnection() ;
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2018-01-11\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\",\"p_Banco\":\"223\",\"p_SBIF\":\"862\",\"p_TipoCartera\":\"2\",\"p_UsaNemoSVS\":\"N\",\"p_ExcluyeGarantia\":\"N\",\"p_ExcluyeSimultanea\":\"N\",\"p_UsaCustodia\":\"S\",\"p_TipoMercado\":\"\",\"p_FamiliaInstrumento\":\"49\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_Modulo\":\"NOR\",\"p_Fecha\":\"2026-07-31\",\"p_Ruta\":\"C:\\\\BCS\\\\logs\",\"p_Banco\":\"223\",\"p_SBIF\":\"862\",\"p_TipoCartera\":\"2\",\"p_UsaNemoSVS\":\"N\",\"p_ExcluyeGarantia\":\"N\",\"p_ExcluyeSimultanea\":\"N\",\"p_UsaCustodia\":\"S\",\"p_TipoMercado\":\"\",\"p_FamiliaInstrumento\":\"49\"}";			
			
			EntradaGenerarInterfazSIG in = gson.fromJson(json, EntradaGenerarInterfazSIG.class);
			GenerarInterfazSIG demo = new GenerarInterfazSIG();
			SalidaGenerarInterfazSIG out = demo.Logica(conn, in);
			System.out.println(out.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		
		}
		System.out.println("== FIN ==");		
}
}
