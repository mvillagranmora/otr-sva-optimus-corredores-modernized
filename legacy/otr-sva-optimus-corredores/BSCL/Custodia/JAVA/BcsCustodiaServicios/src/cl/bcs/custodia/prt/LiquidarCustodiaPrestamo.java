package cl.bcs.custodia.prt;

/**
 * Objetivo	: Liquidar la custodia para el vencimiento y Anticipo de PRESTAMOS
 * 			  Retira Cartera(Ej. PRESTAMO FINES GENERALES) y Subyacente (Ej. COPEC)	
 **/

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.custodia.CertificarMovimientosEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;

public class LiquidarCustodiaPrestamo implements JavaCall{
	private final Log log = new Log("CUS.LiquidarCustodiaPrestamo");
	
	private final String SQL_SELECT_SUBYACENTE 	= loadResourceToString("cl/bcs/custodia/sql/prt/LiquidarCustodiaPrestamoEmprestito_ConsultarSubyacente.sql");
	private final String SQL_SELECT_CARTERA_RELACIONADA = loadResourceToString("cl/bcs/custodia/sql/prt/LiquidarCustodiaRelacionadaPrestamo_ConsultarSubyacente.sql");
	private final String SQL_SELECT_INDICADOR_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/prt/LiquidarCustodiaPrestamoEmprestito_IndicadorCliente.sql");
	private final String SQL_SelectMesas = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_SEL_Mesas.sql");
	private final String SQL_SELECT_CONCEPTO = loadResourceToString("cl/bcs/custodia/sql/LiquidarCustodiaPrestamo_SelectConceptoAnticipoPrestamo.sql");
	
	
	// ----------------------------------------------
	// -- CLASES PARA ENTRADA
	// ----------------------------------------------
	public static class LiquidarCustodiaPrestamo_Entrada {	
		public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_SimularOperacion;    // S/N 
	    public String			p_OrigenLiquidacion;   // VENCIMIENTO - ANTICIPO
	    public List<PrestamoVencer_Entrada>	p_PrestamosVencer;
	    
	    public LiquidarCustodiaPrestamo_Entrada() {
	    	this.p_PrestamosVencer = new ArrayList<PrestamoVencer_Entrada>();
	    }

		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	public static class PrestamoVencer_Entrada {	
		public Long			 IdCartera;
		public Long			 FolioCartera;
		public String		 Nemotecnico;
		public String		 IdClasificacionTipoInstrumento;
		public String		 IdTipoInstrumento;
		public Long			 IdInstrumento;		
		public BigDecimal	 INGMontoATasaCompraActual;
		public BigDecimal	 INGMontoCompraActualMonedaInstrumento;
		public Long			 IdPortafolio;
		public String		 IdMonedaInstrumento;
		public String		 IdTipoProducto;		
		public String		 IdTipoOperacion;
		public String		 OperaContraCuentaInversion;
		public Long			 IdPersona;
		public Long			 IdCliente;		
		public BigDecimal	 PrecioAPlazo;
		public BigDecimal	 MontoAPlazo;
		public java.sql.Date FechaComprobante;
		public String 		 FechaVencimientoAnticipo;
		public String		 IdMonedaSistema;
		public String		 IdMonedaEmision;
		public String		 IdCondicionLiquidacion;
		public BigDecimal	 INGCantidad;
		public BigDecimal	 INGValorRescate;
		public BigDecimal	 ValorRescate;
		public BigDecimal	 INGTasaCompra;
		public BigDecimal	 PrecioCompra;
		public BigDecimal	 INGPrecioCompra;		
		public String		 AccionOperacion;
		public BigDecimal	 INGMontoAPlazo;
		public String		 CobraPrimaAlInicio;
		public BigDecimal	 INGMontoPrima;
		public BigDecimal	 Cantidad = BigDecimal.ZERO;     // Si viene de ANTICIPO corresponde a la cantidad anticipada
		public BigDecimal	 CantidadPorAsignar = BigDecimal.ZERO;     // Indica la cantidad del prestamo que falta por asignar
		public String		 IdTipoPrestamo;
		public String		 PrestamoAsignadoTotal;
		public String 		 FolioSistemaExterno = "";
		public Long		 IdAgente;
		public String 		 IdBolsa;		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjCustodiaRelacionada {	
		public Long			 IdSubyacente;
		public Long			 FolioSubyacente;
		public Long			 FolioCarteraRelacionada;
		public String		 IdEstado;
		public java.sql.Date FechaLiquidacion;
		
		public Long			 IdCartera;
		public Long			 FolioCartera;
		public Long			 IdCliente;		
		public Long			 IdPortafolio;
		
		public String		 IdBoveda;
		public String		 Nemotecnico;
		public String		 IdEmisor;
		public Long			 IdInstrumento;	
		public String 		 IdMonedaInstrumento;
		public String		 IdMonedaEmision;
		public String		 IdTipoReajuste;
		public String		 EsFungible;
		public String		 NemoSVS;
		public BigDecimal	 CorteMinimo;		
		
		public BigDecimal	 INGCantidad;
		public BigDecimal	 INGValorRescate;
		public BigDecimal	 CantidadSaldoCartera;		
		public BigDecimal	 ValorRescateSaldoCartera;
		
		public BigDecimal	 INGPrecioCompra;
		public BigDecimal	 INGTasaCompra;
		public BigDecimal	 INGMontoAPlazo;

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjIndicadorCliente {	
		public Long			 IdCliente;		
		public String		 ContratoCustodia;

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	// ----------------------------------------------
	// -- CLASES PARA SALIDA
	// ----------------------------------------------
	public static class LiquidarCustodiaPrestamo_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;		
		public List<Long> 				P_OUT_PRESTAMOS_OK;
		
		public LiquidarCustodiaPrestamo_Salida() {
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_PRESTAMOS_OK 	= new ArrayList<Long>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	
	// ----------------------------------------------
	// -- CLASES UTILITARIAS
	// ----------------------------------------------
	public static class Subyacente {
		public Long 		IdSubyacente;
		public String 		FechaOperacion;
	    public Long 		FolioSubyacente;
	    public Long 		FolioCarteraRelacionada; 
	    public String 		IdEstado;
	    public BigDecimal 	Cantidad;
	    public BigDecimal 	ValorRescate;	    
	    public Long 		IdInstrumento;
	    public BigDecimal 	PrecioCompraOperacionRelacionada;    
	    public BigDecimal 	TasaCompraOperacionRelacionada;	    
	    public Long 		IdCarteraOrigen; 
	    public String 		IdTipoOperacionOrigen;	    
	    public Long 		IdCliente;
	    public Long 		IdPortafolio; 
	    public String 		IdBoveda;	    
	    public String 		IdMonedaInstrumento;
	    public String 		IdEmisor;
	    public String 		IdTipoCanalIngreso;	 
	    public String 		Nemotecnico;
	    
	    public BigDecimal 	PrecioSubyacente;
	    public BigDecimal 	TasaSubyacente;
	    public BigDecimal 	MontoATasaSubyacenteActual;
	    public BigDecimal 	MontoATasaSubyacenteActualMonedaInstrumento;
	    public String 		IdMonedaEmision;
	    public String 		IdTipoProducto;
	    public String 		IdTipoReajuste;
	    public String 		EsFungible;
	    public java.sql.Date 	FechaVencimientoInstrumento;
	    public java.sql.Date 	FechaEmisionInstrumento;
	    public String 		NemoSVS;
	    public BigDecimal 	CorteMinimo;
	    public String ConCustodiaPrestatario;
	    public String IdTipoInstrumento;
	    
	    public BigDecimal   INGCantidad;
	    public BigDecimal   INGMontoCompraMonedaInstrumento;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class objMesas {
		public Long		IdMesa;
		public Long		IdPortafolio;
	}	
	
	public static class PrestamoVencer_Proceso {

		public Long			 IdCartera;
		public Long			 FolioCartera;
		public String		 Nemotecnico;
		public String		 IdClasificacionTipoInstrumento;
		public String		 IdTipoInstrumento;
		public Long			 IdInstrumento;		
		public BigDecimal	 INGMontoATasaCompraActual;
		public BigDecimal	 INGMontoCompraActualMonedaInstrumento;
		public Long			 IdPortafolio;
		public String		 IdMonedaInstrumento;
		public String		 IdMonedaEmision;
		public String		 IdTipoProducto;		
		public String		 IdTipoOperacion;
		public String		 OperaContraCuentaInversion;
		public Long			 IdPersona;
		public Long			 IdCliente;		
		public BigDecimal	 PrecioAPlazo;
		public BigDecimal	 MontoAPlazo;
		public java.sql.Date FechaComprobante;
		public String 		 FechaVencimientoAnticipo;
		public String		 IdMonedaSistema;
		public String		 IdCondicionLiquidacion;
		public BigDecimal	 INGCantidad;
		public BigDecimal	 INGValorRescate;
		public BigDecimal	 ValorRescate;
		public BigDecimal	 INGTasaCompra;
		public BigDecimal	 PrecioCompra;
		public BigDecimal	 INGPrecioCompra;		
		public String		 AccionOperacion;
		public BigDecimal	 INGMontoAPlazo;
		public String		 CobraPrimaAlInicio;
		public BigDecimal	 INGMontoPrima;
		public BigDecimal	 Cantidad = BigDecimal.ZERO;
		public BigDecimal	 CantidadPorAsignar = BigDecimal.ZERO;     // Indica la cantidad del prestamo que falta por asignar
		public String		 IdTipoPrestamo;
		public String		 PrestamoAsignadoTotal;
		public String		 ContratoCustodia;
		public String 		 FolioSistemaExterno = "";
		public Long 		 IdAgente;
		public String 		 IdBolsa;
		public String		 IdMonedaAdicional;
		public Long			 IdSucursal = 0L;
		public BigDecimal 	 MontoCompra;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Concepto {
		public Long		IdMovimientoCarteraEnTransito;
		public String	IdConcepto;
		public String 	ConCertificadoAutomatico;
	}

	
	// ----------------------------------------------
	// LOGICA
	// ----------------------------------------------
	public LiquidarCustodiaPrestamo_Salida logica (Connection con, LiquidarCustodiaPrestamo_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		LiquidarCustodiaPrestamo_Salida salida = new LiquidarCustodiaPrestamo_Salida();
		
		log.write("== INICIO EJECUCION LIQUIDAR CUSTODIA PRESTAMOS ==");
		log.write("PARAMETROS DE ENTRADA : " + entrada.toString());
		
		try {			
			String conceptoOperacion = entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENPRT" : entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION") ? "ANUPRE" : "ANTPRT";
			String tipoOperacion     = entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENPRT" : entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION") ? "ANUPRE" : "ANTPRT";
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

			List<PrestamoVencer_Proceso> ProcesoPrestamoVencer = new ArrayList<PrestamoVencer_Proceso>();
			List<Long> ListCliente  = new ArrayList<>();
			
			
			for (PrestamoVencer_Entrada entradaPreVen: entrada.p_PrestamosVencer) {
				PrestamoVencer_Proceso procesoPreVen = new PrestamoVencer_Proceso();
				Utils.copyPojo(entradaPreVen, procesoPreVen);
				ProcesoPrestamoVencer.add(procesoPreVen);				
				ListCliente.add(procesoPreVen.IdCliente);
			}
			
			List<ObjIndicadorCliente> objIndicadorCliente  = new ArrayList<>();
			HashMap<String,Object> param = new HashMap<>();							   
			param.put("IdCliente", ListCliente);				
			objIndicadorCliente = sql.query(con, SQL_SELECT_INDICADOR_CLIENTE, param, ObjIndicadorCliente.class);
			
			HashMap<String,Object> prestamoParams = new HashMap<>();
			List<Subyacente> carterasSubyacentes  = new ArrayList<>();
			List<Long> idsPrestamos = ProcesoPrestamoVencer.stream().map(p -> p.FolioCartera).collect(Collectors.toList());
			prestamoParams.put("FolioCarteras", idsPrestamos);				
			carterasSubyacentes = sql.query(con, SQL_SELECT_SUBYACENTE, prestamoParams, Subyacente.class);
			Map<Long, List<Subyacente>> hCarterasSub = carterasSubyacentes.stream().collect(Collectors.groupingBy(c -> c.FolioCarteraRelacionada));
			
			CalcularParidad calculoParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			
			for (PrestamoVencer_Proceso procesoPreVen: ProcesoPrestamoVencer) {
				procesoPreVen.ContratoCustodia = "N";
						
				for (ObjIndicadorCliente indicador: objIndicadorCliente) {
					if (procesoPreVen.IdCliente.compareTo(indicador.IdCliente) == 0 ) {
						procesoPreVen.ContratoCustodia = indicador.ContratoCustodia;
					}
				}
				
				List<Subyacente> carteraSubyacente  = new ArrayList<>();				   			
				carteraSubyacente = hCarterasSub.get(procesoPreVen.FolioCartera);
				
				CalculoEntrada calculoEntradaPlazo = new CalculoEntrada();
				calculoEntradaPlazo.Fecha 			= carteraSubyacente.get(0).FechaOperacion;
				calculoEntradaPlazo.IdInterno 		= procesoPreVen.FolioCartera;
				calculoEntradaPlazo.IdMonedaOrigen 	= procesoPreVen.IdMonedaInstrumento;
				calculoEntradaPlazo.IdMonedaDestino = monedaSistema;
				calculoEntradaPlazo.IdTrabajo 		= 1L;
				calculoEntradaPlazo.IdUso 			= "";
				calculoEntradaPlazo.MontoOrigen 	= procesoPreVen.INGMontoAPlazo;
				calculoEntradaPlazo.ValorCambioLocalModificado = BigDecimal.ZERO;
				
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha 			= carteraSubyacente.get(0).FechaOperacion;
				calculoEntrada.IdInterno 		= procesoPreVen.FolioCartera * -10000;
				calculoEntrada.IdMonedaOrigen 	= procesoPreVen.IdMonedaInstrumento;
				calculoEntrada.IdMonedaDestino 	= monedaSistema;
				calculoEntrada.IdTrabajo 		= 1L;
				calculoEntrada.IdUso 			= "";
				calculoEntrada.MontoOrigen 		= procesoPreVen.INGMontoCompraActualMonedaInstrumento;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaPlazo);
				
				if ("ANULACION".equalsIgnoreCase(entrada.p_OrigenLiquidacion)) continue;
				
				BigDecimal cantidadRestante = procesoPreVen.INGCantidad.subtract(procesoPreVen.Cantidad);
				
				if (cantidadRestante.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal montoRestante = BigDecimal.ZERO;
					if ("RV".equalsIgnoreCase(procesoPreVen.IdClasificacionTipoInstrumento)) {
						montoRestante = cantidadRestante.multiply(procesoPreVen.INGPrecioCompra);
					} else if ("RF".equalsIgnoreCase(procesoPreVen.IdClasificacionTipoInstrumento)){
						BigDecimal CantidadIncial =  carteraSubyacente.get(0).INGCantidad;
						BigDecimal MontoIncial =  carteraSubyacente.get(0).INGMontoCompraMonedaInstrumento;
						montoRestante = MontoIncial.multiply(cantidadRestante.divide(CantidadIncial, 30, RoundingMode.HALF_UP));
					}
					
					CalculoEntrada calculoEntradaRestante = new CalculoEntrada();
					calculoEntradaRestante.Fecha 			= carteraSubyacente.get(0).FechaOperacion;
					calculoEntradaRestante.IdInterno 		= procesoPreVen.FolioCartera * -20000;
					calculoEntradaRestante.IdMonedaOrigen 	= procesoPreVen.IdMonedaInstrumento;
					calculoEntradaRestante.IdMonedaDestino 	= monedaSistema;
					calculoEntradaRestante.IdTrabajo 		= 1L;
					calculoEntradaRestante.IdUso 			= "";
					calculoEntradaRestante.MontoOrigen 		= montoRestante;
					calculoEntradaRestante.ValorCambioLocalModificado = BigDecimal.ZERO;
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaRestante);
				}
			}
			
			CalcularParidadSalida calculoSalida = calculoParidad.logica(con,calcularParidadEntrada);
			HashMap<Long,Object> hCalculoSalida = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
			
			// -------------------------------------------------------------------
			// GENERA MOVIMIENTO CUSTODIA PARA PRESTAMOS
			log.write("GENERANDO MOVIMIENTO CUSTODIA PARA LIQUIDACION PRESTAMOS");			
			List<RetiroCustodia.ObjDatosEntrada> retiros = new ArrayList<>();
			List<IngresoCustodia.ObjDatosEntrada> saldosCartera = new ArrayList<>();
			
			for (PrestamoVencer_Proceso procesoPreVen: ProcesoPrestamoVencer) {
				// Busca cartera subyacente asociada al prestamo
				retiros.clear();
				List<Subyacente> carteraSubyacente  = new ArrayList<>();				   			
				carteraSubyacente = hCarterasSub.get(procesoPreVen.FolioCartera);
				
				BigDecimal EGRmontoAPlazoLocal = ((CalculoSalida)hCalculoSalida.get(procesoPreVen.FolioCartera)).MontoLocal;
				BigDecimal EGRMontoLocal = ((CalculoSalida)hCalculoSalida.get(procesoPreVen.FolioCartera * -10000)).MontoLocal;
				
				BigDecimal MontoRestanteLocal = ((CalculoSalida)hCalculoSalida.getOrDefault(procesoPreVen.FolioCartera * -20000, new CalculoSalida())).MontoLocal;
				BigDecimal MontoRestanteMonedaInstrumento = ((CalculoSalida)hCalculoSalida.getOrDefault(procesoPreVen.FolioCartera * -20000, new CalculoSalida())).MontoOrigen;

				for (Subyacente carSub: carteraSubyacente) {
					// VENCIMIENTO/ANTICIPO CARTERA SUBYACENTE
					RetiroCustodia.ObjDatosEntrada	RetiroSubyacente = new RetiroCustodia.ObjDatosEntrada();					
					RetiroSubyacente.IdGenerarMovimientoCustodia = carSub.IdSubyacente;	//ID Interno del proceso
					RetiroSubyacente.TablaAfectada 				= "SUBYACENTE"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					RetiroSubyacente.EsRetiroNegocio 			= "S";
					RetiroSubyacente.IdCustodia 				= carSub.IdSubyacente;
					RetiroSubyacente.FolioCustodia 				= carSub.FolioSubyacente;					
					RetiroSubyacente.IdCarteraOrigen			= carSub.IdCarteraOrigen;
					RetiroSubyacente.AccionOperacion			= entrada.p_OrigenLiquidacion;	//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					RetiroSubyacente.FechaComprobante			= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
					RetiroSubyacente.IdCliente					= carSub.IdCliente;
					RetiroSubyacente.IdPortafolio				= carSub.IdPortafolio;
					RetiroSubyacente.FechaOperacion				= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
					RetiroSubyacente.IdTipoOperacion			= tipoOperacion;
					RetiroSubyacente.IdOperacion				= 0L;
					RetiroSubyacente.IdConcepto 				= conceptoOperacion;
					RetiroSubyacente.IdCondicionLiquidacion 	= "T0";
					RetiroSubyacente.FechaLiquidacion 			= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
					RetiroSubyacente.IdTipoCanalIngreso			= carSub.IdTipoCanalIngreso;
					RetiroSubyacente.IdUsuario					= entrada.p_IdUsuario;
					RetiroSubyacente.IdModulo					= entrada.p_IdModulo;
					RetiroSubyacente.Glosa						= entrada.p_OrigenLiquidacion + " PRESTAMO " + carSub.Nemotecnico + ", Folio " + procesoPreVen.FolioCartera;					
					RetiroSubyacente.IdInstrumento				= carSub.IdInstrumento;
					RetiroSubyacente.IdMonedaInstrumento		= carSub.IdMonedaInstrumento;
					RetiroSubyacente.IdEmisor					= carSub.IdEmisor;
					RetiroSubyacente.IdBoveda					= carSub.IdBoveda;
					RetiroSubyacente.PrecioCompra				= carSub.PrecioSubyacente;
					RetiroSubyacente.TasaCompra					= carSub.TasaSubyacente;					
					RetiroSubyacente.Cantidad 					= "ANTICIPO".equals(entrada.p_OrigenLiquidacion) ? procesoPreVen.Cantidad : carSub.Cantidad;
					RetiroSubyacente.ValorRescate 				= carSub.ValorRescate;
					RetiroSubyacente.MontoCompra 					= EGRMontoLocal;
					RetiroSubyacente.MontoCompraMonedaInstrumento 	= procesoPreVen.INGMontoCompraActualMonedaInstrumento;
					RetiroSubyacente.IdMonedaEmision 				= carSub.IdMonedaEmision;
					RetiroSubyacente.IdTipoProducto					= carSub.IdTipoProducto;	
					RetiroSubyacente.IdTipoReajusteInstrumento		= carSub.IdTipoReajuste;	
					retiros.add(RetiroSubyacente);
					
					
					// VENCIMIENTO/ANTICIPO CARTERA PRESTATARIO (INSTRUMENTO)
					if (!entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION")) {
						HashMap<String,Object> params2 = new HashMap<>();
						params2.put("FolioCarteraRelacionada",procesoPreVen.FolioCartera);
						params2.put("IdPortafolio", procesoPreVen.IdPortafolio);
						
						List<objMesas> Mesas = new ArrayList<objMesas>();
						List<Long> idsPortafolio = null;
						idsPortafolio = entrada.p_PrestamosVencer.parallelStream().map(i -> i.IdPortafolio).distinct().collect(Collectors.toList());
						
						HashMap<String,Object> paramsMesas = new HashMap<>();
						paramsMesas.put("IdEmpresa", entrada.p_IdEmpresa);
						paramsMesas.put("IdPortafolio", idsPortafolio);
						paramsMesas.put("filtroIdPortafolio", 1);
						Mesas = new SQL().query(con, SQL_SelectMesas, paramsMesas, objMesas.class);
						
						List<ObjCustodiaRelacionada> carteraRelacionada  = new ArrayList<>();
						carteraRelacionada = sql.query(con, SQL_SELECT_CARTERA_RELACIONADA, params2, ObjCustodiaRelacionada.class);
						BigDecimal cantidadARetirar = BigDecimal.ZERO;
	
						if (procesoPreVen.PrestamoAsignadoTotal.equals("N") && !(procesoPreVen.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") || procesoPreVen.IdTipoPrestamo.equalsIgnoreCase("PRVCRF"))) {
							cantidadARetirar = "ANTICIPO".equalsIgnoreCase(entrada.p_OrigenLiquidacion) ? procesoPreVen.Cantidad.subtract(procesoPreVen.CantidadPorAsignar) : procesoPreVen.INGCantidad.subtract(procesoPreVen.CantidadPorAsignar);
						} else {
							cantidadARetirar = "ANTICIPO".equalsIgnoreCase(entrada.p_OrigenLiquidacion) ? procesoPreVen.Cantidad : procesoPreVen.INGCantidad;
						}					
						
						for (ObjCustodiaRelacionada carRel: carteraRelacionada) {
							if (cantidadARetirar.compareTo(BigDecimal.ZERO) == 0) continue;
							
							RetiroCustodia.ObjDatosEntrada	RetiroInstrumento = new RetiroCustodia.ObjDatosEntrada();				
							RetiroInstrumento.IdGenerarMovimientoCustodia 	= carRel.FolioCartera;			//ID Interno del proceso
							RetiroInstrumento.TablaAfectada 		  		= "CARTERA"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
							RetiroInstrumento.EsRetiroNegocio 		  		= "S";
							RetiroInstrumento.IdCustodia		 	  		= carRel.IdCartera;
							RetiroInstrumento.FolioCustodia 		  		= carRel.FolioCartera;
							RetiroInstrumento.AccionOperacion		  		= entrada.p_OrigenLiquidacion;	//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION					
							RetiroInstrumento.FechaComprobante 	  			= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
							RetiroInstrumento.FechaOperacion 		  		= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
							RetiroInstrumento.FechaLiquidacion		  		= carRel.FechaLiquidacion;
							RetiroInstrumento.IdCliente 			  		= procesoPreVen.IdCliente;
							RetiroInstrumento.IdPortafolio			  		= procesoPreVen.IdPortafolio;
							RetiroInstrumento.IdTipoOperacion 		  		= tipoOperacion;
							RetiroInstrumento.IdConcepto			  		= conceptoOperacion;
							RetiroInstrumento.IdCondicionLiquidacion		= "T0";
							RetiroInstrumento.IdTipoCanalIngreso 	  		= "SIS";
							RetiroInstrumento.IdUsuario 			  		= entrada.p_IdUsuario;
							RetiroInstrumento.IdModulo 			  			= entrada.p_IdModulo;
							RetiroInstrumento.Glosa 				  		= entrada.p_OrigenLiquidacion + " PRESTAMO " + procesoPreVen.FolioCartera.toString();
							RetiroInstrumento.IdInstrumento 		  		= carRel.IdInstrumento;
							RetiroInstrumento.Cantidad				  		= cantidadARetirar.compareTo(carRel.INGCantidad) > 0 ? carRel.INGCantidad : cantidadARetirar;
							RetiroInstrumento.ValorRescate			  		= procesoPreVen.INGValorRescate;
							RetiroInstrumento.TasaCompra			  		= procesoPreVen.INGTasaCompra;
							RetiroInstrumento.PrecioCompra 		  			= procesoPreVen.INGPrecioCompra;
							RetiroInstrumento.IdMonedaLiquidacion 	  		= procesoPreVen.IdMonedaInstrumento;											
							retiros.add(RetiroInstrumento);

							cantidadARetirar = cantidadARetirar.compareTo(carRel.INGCantidad) > 0 ? cantidadARetirar.subtract(carRel.INGCantidad) : BigDecimal.ZERO;
						}
						
						if (cantidadARetirar.compareTo(BigDecimal.ZERO) > 0) {
							IngresoCustodia.ObjDatosEntrada saldoCartera = new IngresoCustodia.ObjDatosEntrada();
							saldoCartera.IdGenerarMovimientoCustodia 	= procesoPreVen.FolioCartera;
							saldoCartera.TablaAfectada 					= "TRANSITO";
							saldoCartera.AccionOperacion				= "INGRESO";
							saldoCartera.EsRegistroNuevo				= "S";
							saldoCartera.IdCustodia 					= 0L;
							saldoCartera.FolioCarteraOrigen				= 0L;	
							saldoCartera.FolioCustodia                  = 0L;
							saldoCartera.IdCliente						= procesoPreVen.IdCliente;
							saldoCartera.IdPortafolio					= procesoPreVen.IdPortafolio;
							saldoCartera.FechaOperacion					= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
							saldoCartera.FechaComprobante 				= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
							saldoCartera.FechaLiquidacion				= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
							saldoCartera.FechaVencimientoOperacion		= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);						
							saldoCartera.IdTipoOperacion				= procesoPreVen.IdTipoOperacion.equalsIgnoreCase("ANTPRT") ? "INGTIT" : "RETTIT";
							saldoCartera.IdTipoProducto 				= "PRES";
							saldoCartera.IdConcepto						= conceptoOperacion;						
							saldoCartera.IdCondicionLiquidacion			= procesoPreVen.IdCondicionLiquidacion; 
							saldoCartera.IdUsuario						= entrada.p_IdUsuario;
							saldoCartera.Glosa							= entrada.p_OrigenLiquidacion + " PRESTAMO " + procesoPreVen.IdTipoPrestamo;
							saldoCartera.IdInstrumento					= carSub.IdInstrumento;
							saldoCartera.Nemotecnico					= carSub.Nemotecnico;
							saldoCartera.IdMonedaInstrumento			= carSub.IdMonedaInstrumento;
							saldoCartera.IdEmisor						= carSub.IdEmisor;
							saldoCartera.Cantidad						= cantidadARetirar;
							saldoCartera.EsNominal						= procesoPreVen.ContratoCustodia.equals("S") ? "N" : "S";
							saldoCartera.IdBoveda						= carSub.IdBoveda;
							saldoCartera.IdCuentaCustodioPortafolio		= 0L;
							saldoCartera.IdTipoCanalIngreso 	  		= "SIS";
							saldoCartera.EsVentaCorta					= (procesoPreVen.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") || procesoPreVen.IdTipoPrestamo.equalsIgnoreCase("PRVCRF")) ? "S" : "N";						
							if(procesoPreVen.IdTipoOperacion.equalsIgnoreCase("ANTPRT")) {
								saldoCartera.EsFungible						= "N";
								saldoCartera.IdCorredora					= 0L;
								saldoCartera.IdAgente						= procesoPreVen.IdAgente;
								saldoCartera.IdBolsa						= procesoPreVen.IdBolsa;
								saldoCartera.IdCustodia						= null;
								saldoCartera.IdMonedaAdicional				= procesoPreVen.IdMonedaInstrumento;
								saldoCartera.IdTipoReajusteInstrumento		= procesoPreVen.IdMonedaSistema;
								saldoCartera.MontoCompra					= procesoPreVen.INGMontoCompraActualMonedaInstrumento;
								saldoCartera.MontoCompraMonedaInstrumento	= procesoPreVen.INGMontoCompraActualMonedaInstrumento;
								saldoCartera.PrecioCompra					= procesoPreVen.INGPrecioCompra;
								saldoCartera.TasaCompra						= procesoPreVen.INGTasaCompra;
								
								if (procesoPreVen.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") || procesoPreVen.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
									saldoCartera.IdMesa = Mesas.size() > 0 ? Mesas.get(0).IdMesa : 0L;
								} else {
									saldoCartera.IdMesa = 0L;
								}
							}
							
							saldosCartera.add(saldoCartera);
						}	
					}
				} //end ciclo x c/subyacente
				
				// VENCIMIENTO/ANTICIPO CARTERA PRESTAMO(CABECERA)
				RetiroCustodia.ObjDatosEntrada	RetiroCartera = new RetiroCustodia.ObjDatosEntrada();				
				RetiroCartera.IdGenerarMovimientoCustodia 	= procesoPreVen.FolioCartera;	//ID Interno del proceso
				RetiroCartera.TablaAfectada 		  		= "CARTERA"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
				RetiroCartera.EsRetiroNegocio 		  		= "S";
				RetiroCartera.IdCustodia		 	  		= procesoPreVen.IdCartera;
				RetiroCartera.FolioCustodia 		  		= procesoPreVen.FolioCartera;
				RetiroCartera.AccionOperacion		  		= entrada.p_OrigenLiquidacion;	//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION					
				RetiroCartera.FechaComprobante 	  			= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
				RetiroCartera.IdCliente 			  		= procesoPreVen.IdCliente;
				RetiroCartera.IdPortafolio			  		= procesoPreVen.IdPortafolio;
				RetiroCartera.FechaOperacion 		  		= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
				RetiroCartera.IdTipoOperacion 		  		= tipoOperacion;
				RetiroCartera.IdOperacion 			  		= 0L;
				RetiroCartera.IdConcepto			  		= conceptoOperacion;
				RetiroCartera.IdCondicionLiquidacion		= "T0";
				RetiroCartera.FechaLiquidacion		  		= Fecha.toDate(procesoPreVen.FechaVencimientoAnticipo);
				RetiroCartera.IdTipoCanalIngreso 	  		= "SIS";
				RetiroCartera.IdUsuario 			  		= entrada.p_IdUsuario;
				RetiroCartera.IdModulo 			  			= entrada.p_IdModulo;
				RetiroCartera.Glosa 				  		= entrada.p_OrigenLiquidacion + " PRESTAMO " + procesoPreVen.Nemotecnico + ", Folio " + procesoPreVen.FolioCartera.toString();
				RetiroCartera.IdInstrumento 		  		= procesoPreVen.IdInstrumento;
				RetiroCartera.Cantidad				  		= "ANTICIPO".equalsIgnoreCase(entrada.p_OrigenLiquidacion) ? procesoPreVen.Cantidad : procesoPreVen.INGCantidad;
				RetiroCartera.ValorRescate			  		= procesoPreVen.INGValorRescate;
				RetiroCartera.PrecioAPlazo			  		= procesoPreVen.INGMontoPrima;
				RetiroCartera.TasaCompra			  		= procesoPreVen.INGTasaCompra;
				RetiroCartera.PrecioCompra 		  			= procesoPreVen.INGPrecioCompra;
				RetiroCartera.MontoCompra 					= EGRMontoLocal;
				RetiroCartera.MontoCompraMonedaInstrumento 	= procesoPreVen.INGMontoCompraActualMonedaInstrumento;
				RetiroCartera.MontoLiquidacion 	  			= procesoPreVen.INGMontoAPlazo;
				RetiroCartera.IdMonedaLiquidacion 	  		= procesoPreVen.IdMonedaInstrumento;
				RetiroCartera.FolioSistemaExterno			= procesoPreVen.FolioSistemaExterno;
				RetiroCartera.MontoAPlazo					= EGRmontoAPlazoLocal;
				RetiroCartera.MontoAPlazoMonedaInstrumento	= procesoPreVen.INGMontoAPlazo;
				RetiroCartera.IdMonedaEmision				= procesoPreVen.IdMonedaEmision;
				RetiroCartera.IdTipoProducto				= "PRES";
				RetiroCartera.IdTipoReajusteInstrumento     = carteraSubyacente.get(0).IdTipoReajuste;
				
				// AGREGAMOS MONTO LOCAL E INSTRUMENTO RESTANTE
				RetiroCartera.MontoRestanteLocal             	= MontoRestanteLocal;
				RetiroCartera.MontoRestanteMonedaInstrumento     = MontoRestanteMonedaInstrumento;

				RetiroCartera.IdInstrumentoPrestamo               	 = carteraSubyacente.get(0).IdInstrumento; 
				RetiroCartera.NemotecnicoPrestamo 				 	 = carteraSubyacente.get(0).Nemotecnico;
				RetiroCartera.IdTipoInstrumentoPrestamo 	     	 = carteraSubyacente.get(0).IdTipoInstrumento;
				RetiroCartera.TasaCompraInstrumentoPrestamo 	     = carteraSubyacente.get(0).TasaSubyacente;
				RetiroCartera.IdMonedaInstrumento 	     			 = carteraSubyacente.get(0).IdMonedaInstrumento;
				RetiroCartera.IdMonedaEmision			 			 = carteraSubyacente.get(0).IdMonedaEmision;
				RetiroCartera.FechaVencimientoInstrumento 			 = carteraSubyacente.get(0).FechaVencimientoInstrumento;
				retiros.add(RetiroCartera);

				if (retiros.size() > 0){
					RetiroCustodia retirosCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
					paramInSP.p_IdEmpresa				= entrada.p_IdEmpresa;
					paramInSP.p_IdModulo				= entrada.p_IdModulo;
					paramInSP.p_IdUsuario				= entrada.p_IdUsuario;
					paramInSP.p_ValorizacionAutomatica 	= "S";
					paramInSP.p_SimularOperacion		= entrada.p_SimularOperacion;
					paramInSP.p_TicketAtencion 			= 0L;
					paramInSP.p_DatosEntrada			= retiros;
					RetiroCustodia.RetiroCustodiaSalida resultRetiros = retirosCustodia.logica(con, paramInSP);
					
					if (MensajeSalida.getNumeroErrores(resultRetiros.P_OUT_MENSAJES) > 0) {
						salida.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
					} else {
						for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultRetiros.P_OUT_DATOSSALIDA) {
							String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO";
							HashMap<String,String> par = new HashMap<>();
							par.put("TipoCartera", datoSalida.TablaOrigen);
							par.put("Folio", datoSalida.FolioCustodia.toString());
							
							String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
							log.write(descripcion);
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion, par, con, entrada.p_IdEmpresa));
						}
						salida.P_OUT_PRESTAMOS_OK.add(procesoPreVen.FolioCartera);
					}
				}
			}  // FIN - for (PrestamoVencer_Proceso proPreVen: ProcesoPrestamoVencer)
			
			
			if ( !saldosCartera.isEmpty() ) {
				IngresoCustodia ingresoSaldoCartera = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada entradaIngresoSaldoCartera = new IngresoCustodia.IngresoCustodiaEntrada();
				entradaIngresoSaldoCartera.p_IdEmpresa 				= entrada.p_IdEmpresa;
				entradaIngresoSaldoCartera.p_IdModulo 				= entrada.p_IdModulo;
				entradaIngresoSaldoCartera.p_IdUsuario 				= entrada.p_IdUsuario;
				entradaIngresoSaldoCartera.p_SimularOperacion 		= "N";
				entradaIngresoSaldoCartera.p_TicketAtencion 		= 0L;
				entradaIngresoSaldoCartera.p_ValorizacionAutomatica = "S";
				entradaIngresoSaldoCartera.p_DatosEntrada 			= saldosCartera;
				IngresoCustodia.IngresoCustodiaSalida salidaIngresoSaldoCartera = ingresoSaldoCartera.logica(con,entradaIngresoSaldoCartera);
				
				// CELLINCECT-401 && CELLINCECT-711 CERTIFICACION DE MOVIMIENTOS EN TRANSITO AUTOMATICA POR ANTICIPO Y VENCIMIENTO DE PRESTAMOS:
				List<Concepto> conceptos = new ArrayList<>();
				List<Long> IdMovimientosEnTransito = new ArrayList<Long>();
				List<Long> IdMovTransito = new ArrayList<>();
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : salidaIngresoSaldoCartera.P_OUT_DATOSSALIDA){
					IdMovimientosEnTransito.add(result.IdCustodia);			
				}
								
				HashMap<String,Object> params = new HashMap<>();
				params.put("IdMovimientoCarteraEnTransito", IdMovimientosEnTransito);
				
				conceptos = new SQL().query(con, SQL_SELECT_CONCEPTO, params, Concepto.class);
				
				
				for(Concepto concepto : conceptos) {
					if(concepto.IdConcepto.equalsIgnoreCase("ANTPRT") || concepto.IdConcepto.equalsIgnoreCase("VENPRT") && concepto.ConCertificadoAutomatico.equalsIgnoreCase("S")) {
						IdMovTransito.add(concepto.IdMovimientoCarteraEnTransito);					
					}
				}
				
				if(!IdMovTransito.isEmpty()) {
					
					log.write("SE INICIA LA CERTIFICACION AUTOMATICA DEL MOVIMIENTO EN TRANSITO CREADO POR ANTICIPO/VENCIMIENTO DE PRESTAMO");
					
					List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransito = GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, IdMovTransito);

					CertificarMovimientosEnTransito certificaMovTransito = new CertificarMovimientosEnTransito();
					CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada entradaCerMov = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada();						
					
					entradaCerMov.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCerMov.p_IdUsuario = entrada.p_IdUsuario;
					entradaCerMov.p_IdModulo  = entrada.p_IdModulo;
					entradaCerMov.p_FechaOperacion = fechaSistema;
					entradaCerMov.P_MOVIMIENTOSCERTIFICAR = movimientosEnTransito.stream().map(movimientoEnTrasito->{
						CertificarMovimientosEnTransito.ObjMovimientos movimientoCertificar = new CertificarMovimientosEnTransito.ObjMovimientos();
						movimientoCertificar.Cantidad = movimientoEnTrasito.Cantidad;
						movimientoCertificar.IdClasificacionTipoInstrumento = movimientoEnTrasito.IdClasificacionTipoInstrumento;
						movimientoCertificar.IdMovimientoCarteraEnTransito = movimientoEnTrasito.IdMovimientoCarteraEnTransito;
						movimientoCertificar.IdOperacion = movimientoEnTrasito.IdOperacion;
						movimientoCertificar.IdTipoOperacion = movimientoEnTrasito.IdTipoOperacion;
						movimientoCertificar.MovimientoGarantia = "N";
						movimientoCertificar.ValorRescate = movimientoEnTrasito.ValorRescate;
						return movimientoCertificar;
					})
							.collect(Collectors.toList());
					
					CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida salidaCerMov = certificaMovTransito.logica(con, entradaCerMov);
					
					if (MensajeSalida.getNumeroErrores(salidaCerMov.P_OUT_MENSAJES) > 0 && !entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO")) {
						salidaCerMov.P_OUT_MENSAJES.forEach(mensaje-> {
							if (!mensaje.codigo.equalsIgnoreCase("CONCEPTO_NO_EXISTE")) {
								salida.P_OUT_MENSAJES.add(mensaje);
							}
						});
					}
					
					log.write("FIN A LA CERTIFICACION AUTOMATICA DEL MOVIMIENTO EN TRANSITO CREADO POR ANTICIPO/VENCIMIENTO DE PRESTAMO");
					
				}				
				// FIN CELLINCECT-401 && CELLINCECT-711
				
				if (MensajeSalida.getNumeroErrores(salidaIngresoSaldoCartera.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES.addAll(salidaIngresoSaldoCartera.P_OUT_MENSAJES);			
				} else {
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida datoSalida : salidaIngresoSaldoCartera.P_OUT_DATOSSALIDA) {
						String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CARTERA_CREADA_FOLIO")
								.add("FolioCartera", datoSalida.FolioCustodia)
								.add("TipoCartera", datoSalida.TablaOrigen).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion, null, con, entrada.p_IdEmpresa));				
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);		
		log.write("PARAMETROS DE SALIDA : " + salida.toString());				
		log.write("== TERMINO EJECUCION LIQUIDAR CUSTODIA PRESTAMOS ==");
		
		return salida;
	};
	
	
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		LiquidarCustodiaPrestamo_Entrada jin = gson.fromJson(in.toString(), LiquidarCustodiaPrestamo_Entrada.class);
		
		LiquidarCustodiaPrestamo_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	// ----------------------------------------------
	//  METODO PARA PRUEBAS LOCALES
	// ----------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "";			
		
			LiquidarCustodiaPrestamo_Entrada Objin = gson.fromJson(json, LiquidarCustodiaPrestamo_Entrada.class);
			LiquidarCustodiaPrestamo demo = new LiquidarCustodiaPrestamo();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				LiquidarCustodiaPrestamo_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}

