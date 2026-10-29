package cl.bcs.custodia.prt;

/**
 * Objetivo	: Liquidar la custodia para el vencimiento y anticipo de EMPRESTITOS
 * 			  Retira Cartera(Ej. PRESTAMO FINES GENERALES) y Subyacente (Ej. COPEC)
 * 				y devuelve los titulos al prestamista
 * 
 *  		En el caso de ser un anticipo de un prestamista hecho desde la UI, siempre sera un solo registro para procesar.
 * 				
 **/
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
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

public class LiquidarCustodiaEmprestito implements JavaCall{
	private final Log log = new Log("CUS.LiquidarCustodiaEmprestito");
	
	private final String SQL_SELECT_SUBYACENTE 	= loadResourceToString("cl/bcs/custodia/sql/prt/LiquidarCustodiaPrestamoEmprestito_ConsultarSubyacente.sql");  //ojo, comparte este archivo con la liquidacion de custodia de prestamos

	// ----------------------------------------------
	// -- CLASES PARA ENTRADA
	// ----------------------------------------------
	
	public static class LiquidarCustodiaEmprestito_Entrada{	
		public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public Long 			p_IdPais;
	    public String			p_SimularOperacion;    // S/N 
	    public String			p_OrigenLiquidacion;   // VENCIMIENTO - ANTICIPO
	    public String 			p_RebajaPrestamo;    //Si viene en S, significa q es un anticipo manual del UN prestamista, por ende tengo q rebajar la cantidad que estoy anticipando tambien del prestamo.
	    public List<EmprestitoALiquidar>	p_EmprestitosVencer;
	    public String 			p_FechaVencimientoAnticipo;
	    
	    public LiquidarCustodiaEmprestito_Entrada() {
	    	this.p_RebajaPrestamo = "N";  //por defecto N
	    	this.p_EmprestitosVencer = new ArrayList<EmprestitoALiquidar>();
	    }

		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	public static class DatosPrestamo{
		public Long 	IdCartera;
		public Long 	FolioPrestamo;
		public BigDecimal Tasa;
		public Long 	IdSubyacente;
		public Long 	IdInstrumentoSubyacente;
		public String 	IdMonedaSubyacente;
		public String 	IdEmisorSubyacente;
		public String 	IdMonedaEmisionSubyacente;
		public java.sql.Date 	FechaVencimientoInstrumento;
		public String 	EsFungibleSubyacente;
		public String 	IdTipoReajusteSubyacente;
		public Long 	IdInstrumentoPrestamo;
		public String 	IdBolsa;
		public String 	IdTipoProducto;
	}
	
	
	public static class EmprestitoALiquidar{ 
		public Long IdCartera;
		public Long FolioEmprestito;
		public String FechaVencimientoEmprestito;
		public String FechaEmprestito;
		public String FechaVencimientoSubyacente;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioPrestamo;
		public Long IdClientePrestamo;
		public Long IdPortafolioPrestamo;
		public String FechaPrestamo;
		public Long IdInstrumentoEmprestito;
		public String IdTipoInstrumentoEmprestito;
		public String IdEmisorEmprestito;
		public String NemoSubyacente;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public String IdTipoReajusteSubyacente;
		public String IdEmisorSubyacente;
		public String IdMonedaSubyacente;
		public String EsFungible;
		public BigDecimal INGCantidad;
		public BigDecimal INGTasaEmprestito;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoInstrumento;
		public BigDecimal INGMontoTotalEmprestito;
		public BigDecimal INGMontoPremio;
		public BigDecimal PrecioInstrumento;
		public String ConCustodiaPrestamista;
		public String ConCustodiaPrestatario;
		public String OperaContraCuentaInversionPrestamista;
		public String OperaContraCuentaInversionPrestatario;
		public String IdTipoPrestamo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	
	// ----------------------------------------------
	// -- CLASES PARA SALIDA
	// ----------------------------------------------

	public static class LiquidarCustodiaEmprestito_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<Long> 				P_OUT_EMPRESTITOS_OK;
		
		
		
		public LiquidarCustodiaEmprestito_Salida() {
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_EMPRESTITOS_OK 	= new ArrayList<Long>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// ----------------------------------------------
	// -- CLASES UTILITARIAS
	// ----------------------------------------------
	public static class Subyacente{
		public Long 		IdSubyacente;
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
	    public String 		OperaContraCuentaInversion;
	    public String 		IdTipoInstrumento;
	    public BigDecimal   INGCantidad;
	    public BigDecimal   INGMontoCompraMonedaInstrumento;
	    public String   	FechaOperacion;
	    
	    public BigDecimal 	PrecioSubyacente;
	    public BigDecimal 	TasaSubyacente;
	    public BigDecimal 	MontoATasaSubyacenteActual;
	    public BigDecimal 	MontoATasaSubyacenteActualMonedaInstrumento;
	    public String 		IdMonedaEmision;
	    public String 		IdTipoReajuste;
	    public String 		EsFungible;
	    public String 		IdTipoProducto;
	    public java.sql.Date 	FechaVencimientoInstrumento;
	    public java.sql.Date 	FechaEmisionInstrumento;
	    public String 		NemoSVS;
	    public BigDecimal 	CorteMinimo;
	    public Long IdCarteraRetirada;
	    
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// ----------------------------------------------
	// LOGICA
	// ----------------------------------------------

	public static Long CuentaDias (String d1, String d2, String tipo){
		Long CantDias = 0L;
		LocalDate ld = LocalDate.parse(d1, Fecha.YYYYMMDD);
        LocalDate ld2 = LocalDate.parse(d2, Fecha.YYYYMMDD);
       
        int diasDiferenciaCompleto = ld2.getDayOfYear() - ld.getDayOfYear();
        int diasDiferenciaLaboral = 0;
       
        for(int asd = 0; asd< diasDiferenciaCompleto; asd++) {
            ld = ld.plusDays(asd);
            if(ld.getDayOfWeek() != DayOfWeek.SATURDAY && ld.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasDiferenciaLaboral++;
            }
        }
		
        if (tipo.equalsIgnoreCase("L"))
        	CantDias = new Long(diasDiferenciaLaboral);
        else
        	CantDias = new Long(diasDiferenciaCompleto);
		return CantDias;
	}
	
	public static BigDecimal CalculoInteres (BigDecimal Monto, BigDecimal Plazo, BigDecimal Tasa, BigDecimal Base){
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal tasaDivCien = BigDecimal.ZERO;
		BigDecimal plazoDivBase = BigDecimal.ZERO;
		
		//return Number(monto * ( 1 + (tasa/100) * (plazo /base) ));
		
		tasaDivCien = Tasa.divide(new BigDecimal(100));
		plazoDivBase = Plazo.divide(Base);
		
		total = Monto.multiply( BigDecimal.ONE.add(tasaDivCien.multiply(plazoDivBase)) );
		total = total.subtract(Monto);
		
		return total;
	}
	
	
	
	public BigDecimal FlujoCustodia(Long IdCliente, Long IdPortafolio, EmprestitoALiquidar emprestito, 
			LiquidarCustodiaEmprestito_Entrada entrada, Connection conn) throws Exception {
		
		List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
		ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
		datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= emprestito.IdClasificacionTipoInstrumentoSubyacente;
		datosFlujoFiltrosBusqueda.IdTipoInstrumento					= emprestito.IdTipoInstrumentoSubyacente;
		datosFlujoFiltrosBusqueda.IdInstrumento						= emprestito.IdInstrumentoSubyacente;
		datosFlujoFiltrosBusqueda.IdEmisor							= null;
		datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
		datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
		datosFlujoFiltrosBusqueda.EsFungible						= null;
		datosFlujoFiltrosBusqueda.IdBoveda							= null;
		dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
		
		List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
		ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
		datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
		datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
		datosFlujoEsAgrupadoPor.Instrumento						= "S";
		datosFlujoEsAgrupadoPor.Emisor							= "N";
		datosFlujoEsAgrupadoPor.Nemotecnico						= "N";
		datosFlujoEsAgrupadoPor.NemoSVS							= "N";
		datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "N";
		datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "N";
		datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "N";
		datosFlujoEsAgrupadoPor.TipoReajuste					= "N";
		datosFlujoEsAgrupadoPor.EsFungible						= "N";
		datosFlujoEsAgrupadoPor.CodigoIFRS						= "N";
		datosFlujoEsAgrupadoPor.Boveda							= "N";
		datosFlujoEsAgrupadoPor.Bolsa							= "N";
		datosFlujoEsAgrupadoPor.TipoOperacion					= "N";
		datosFlujoEsAgrupadoPor.Mesa							= "N";
		datosFlujoEsAgrupadoPor.FolioRegistro					= "N";
		datosFlujoEsAgrupadoPor.ItemOrigen						= "N";
		dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
		
		List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
		ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
		datosFlujoUso.UsoTipoOperacion				= "INGGPR";
		datosFlujoUso.IdCliente						= IdCliente;
		datosFlujoUso.IdPortafolio					= IdPortafolio;
		datosFlujoUso.BusquedaEspecifica			= "SOLO_GARANTIZABLE";
		datosFlujoUso.IdTipoInstrumentoEspecifico	= null;
		dato_p_uso.add(datosFlujoUso);
		
		ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
		ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
		paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;
		paramInSP.p_IdUsuario			= entrada.p_IdUsuario;
		paramInSP.p_IdModulo			= entrada.p_IdModulo;
		paramInSP.p_FechaSaldo			= entrada.p_FechaVencimientoAnticipo;
		paramInSP.p_IdPais				= entrada.p_IdPais;
		paramInSP.p_DiasProyeccion		= 0;
		paramInSP.p_DiaHabil			= "S";
		paramInSP.p_ValorizarFlujo		= "N";
		paramInSP.p_Uso					= dato_p_uso;
		paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
		paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;
		
		
		ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(conn, paramInSP);
		if (salidaFlujo.p_out_FlujoTotalizado.size()>0 ){
			return salidaFlujo.p_out_FlujoTotalizado.get(0).Cantidad;

		} else {
			return new BigDecimal(0);
		}
	
	}
	
	public LiquidarCustodiaEmprestito_Salida logica (Connection con, LiquidarCustodiaEmprestito_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		LiquidarCustodiaEmprestito_Salida salida = new LiquidarCustodiaEmprestito_Salida();
		
		log.write("== INICIO EJECUCION LIQUIDAR CUSTODIA EMPRESTITOS ==");
		log.write("PARAMETROS DE ENTRADA : " + entrada.toString());
		
		try {
			
			String conceptoOperacion = entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENEMP" : entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION") ? "ANUEMP" : "ANTEMP";
			String tipoOperacion     = entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENEMP" : entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION") ? "ANUEMP" : "ANTEMP";
			
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodiaTransitoConCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
			List<RetiroCustodia.ObjDatosEntrada> lstRetirosCustodiaPrestamos = new ArrayList<>();

			
			// -------------------------------------------------------------------
			// GENERA MOVIMIENTO CUSTODIA PARA EMPRESTITOS
			log.write("GENERANDO MOVIMIENTO CUSTODIA PARA LIQUIDACION DE EMPRESTITOS");
			
			List<RetiroCustodia.ObjDatosEntrada> lstRetiros = new ArrayList<>();
			
			HashMap<String,Object> prestamoParams = new HashMap<>();
			List<Subyacente> carterasSubyacentes  = new ArrayList<>();
			List<Long> idsPrestamos = entrada.p_EmprestitosVencer.stream().map(p -> p.FolioEmprestito).collect(Collectors.toList());
			prestamoParams.put("FolioCarteras", idsPrestamos);				
			carterasSubyacentes = sql.query(con, SQL_SELECT_SUBYACENTE, prestamoParams, Subyacente.class);
			Map<Long, List<Subyacente>> hCarterasSub = carterasSubyacentes.stream().collect(Collectors.groupingBy(c -> c.FolioCarteraRelacionada));
			
			CalcularParidad calculoParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			for (EmprestitoALiquidar emprestito: entrada.p_EmprestitosVencer) {
				List<Subyacente> carteraSubyacente = hCarterasSub.get(emprestito.FolioEmprestito);
				if (carteraSubyacente == null) continue;
				BigDecimal INGCantidad = carteraSubyacente.get(0).INGCantidad;
				
				BigDecimal EGRMontoMonedaInstrumento = BigDecimal.ZERO;
				BigDecimal EGRMontoMonedaAPlazoInstrumento = BigDecimal.ZERO;
				if ("RV".equalsIgnoreCase(emprestito.IdClasificacionTipoInstrumentoSubyacente)) {
					EGRMontoMonedaInstrumento = emprestito.INGCantidad.multiply(emprestito.PrecioInstrumento);
					EGRMontoMonedaAPlazoInstrumento = EGRMontoMonedaInstrumento.add(emprestito.INGMontoPremio);
				} else if ("RF".equalsIgnoreCase(emprestito.IdClasificacionTipoInstrumentoSubyacente)){
					BigDecimal CantidadInicial = carteraSubyacente.get(0).INGCantidad;
					BigDecimal MontoInstrumentoInicial = carteraSubyacente.get(0).INGMontoCompraMonedaInstrumento;
					EGRMontoMonedaInstrumento = MontoInstrumentoInicial.multiply(emprestito.INGCantidad.divide(CantidadInicial, 30, RoundingMode.HALF_UP));
					EGRMontoMonedaAPlazoInstrumento = EGRMontoMonedaInstrumento.add(emprestito.INGMontoPremio);
				}
				String Fecha = emprestito.FechaEmprestito;
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha 				= Fecha;
				calculoEntrada.IdInterno 			= emprestito.FolioEmprestito;
				calculoEntrada.IdMonedaOrigen 		= carteraSubyacente.get(0).IdMonedaInstrumento;
				calculoEntrada.IdMonedaDestino 		= monedaSistema;
				calculoEntrada.IdTrabajo 			= 1L;
				calculoEntrada.IdUso 				= "";
				calculoEntrada.MontoOrigen 			= EGRMontoMonedaInstrumento;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				
				CalculoEntrada calculoEntradaMontoPlazo = new CalculoEntrada();
				calculoEntradaMontoPlazo.Fecha 				= Fecha;
				calculoEntradaMontoPlazo.IdInterno 			= emprestito.FolioEmprestito * 10000;
				calculoEntradaMontoPlazo.IdMonedaOrigen  	= carteraSubyacente.get(0).IdMonedaInstrumento;
				calculoEntradaMontoPlazo.IdMonedaDestino 	= monedaSistema;
				calculoEntradaMontoPlazo.IdTrabajo 			= 2L;
				calculoEntradaMontoPlazo.IdUso 				= "";
				calculoEntradaMontoPlazo.MontoOrigen 		= EGRMontoMonedaAPlazoInstrumento;
				calculoEntradaMontoPlazo.ValorCambioLocalModificado = BigDecimal.ZERO;
				
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMontoPlazo);
				
				if ("ANULACION".equalsIgnoreCase(entrada.p_OrigenLiquidacion)) continue;
				
				BigDecimal cantidadRestante = INGCantidad.subtract(emprestito.INGCantidad);
				if (cantidadRestante.compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal montoRestante = BigDecimal.ZERO;
					if ("RV".equalsIgnoreCase(emprestito.IdClasificacionTipoInstrumentoSubyacente)) {
						montoRestante = cantidadRestante.multiply(emprestito.PrecioInstrumento);
					} else if ("RF".equalsIgnoreCase(emprestito.IdClasificacionTipoInstrumentoSubyacente)){
						BigDecimal CantidadInicial = carteraSubyacente.get(0).INGCantidad;
						BigDecimal MontoInstrumentoInicial = carteraSubyacente.get(0).INGMontoCompraMonedaInstrumento;
						montoRestante = MontoInstrumentoInicial.multiply(cantidadRestante.divide(CantidadInicial, 30, RoundingMode.HALF_UP));
					}
					
					CalculoEntrada calculoEntradaMontoRestante = new CalculoEntrada();
					calculoEntradaMontoRestante.Fecha 				= Fecha;
					calculoEntradaMontoRestante.IdInterno 			= emprestito.FolioEmprestito * 20000;
					calculoEntradaMontoRestante.IdMonedaOrigen 		= carteraSubyacente.get(0).IdMonedaInstrumento;
					calculoEntradaMontoRestante.IdMonedaDestino 	= monedaSistema;
					calculoEntradaMontoRestante.IdTrabajo 			= 2L;
					calculoEntradaMontoRestante.IdUso				= "";
					calculoEntradaMontoRestante.MontoOrigen 		= montoRestante;
					calculoEntradaMontoRestante.ValorCambioLocalModificado = BigDecimal.ZERO;
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMontoRestante);
				}
				
			}
			
			CalcularParidadSalida calculoSalida = calculoParidad.logica(con,calcularParidadEntrada);
			HashMap<Long,Object> hCalculoSalida = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");

			for (EmprestitoALiquidar emprestito: entrada.p_EmprestitosVencer){
				// Busca cartera subyacente del emprestito
				lstRetiros.clear();
				
				List<Subyacente> carteraSubyacente  = new ArrayList<>();
				
				carteraSubyacente = hCarterasSub.get(emprestito.FolioEmprestito);
				if (carteraSubyacente == null) continue;
				
				BigDecimal EGRMontoLocal = ((CalculoSalida)hCalculoSalida.get(emprestito.FolioEmprestito)).MontoLocal;
				BigDecimal EGRMontoAPlazoLocal = ((CalculoSalida)hCalculoSalida.get(emprestito.FolioEmprestito * 10000)).MontoLocal;
				
				BigDecimal EGRMontoMonedaInstrumento = ((CalculoSalida)hCalculoSalida.get(emprestito.FolioEmprestito)).MontoOrigen;
				BigDecimal EGRMontoAPlazoMonedaInstrumento = ((CalculoSalida)hCalculoSalida.get(emprestito.FolioEmprestito * 10000)).MontoOrigen;
				
				BigDecimal MontoRestanteLocal = ((CalculoSalida)hCalculoSalida.getOrDefault(emprestito.FolioEmprestito * 20000, new CalculoSalida())).MontoLocal;
				BigDecimal MontoRestanteMonedaInstrumento = ((CalculoSalida)hCalculoSalida.getOrDefault(emprestito.FolioEmprestito * 20000, new CalculoSalida())).MontoOrigen;
					
				for (Subyacente carSub: carteraSubyacente){
					//-----------------------------------------------------------------
					//-- MOVIMIENTO VENCIMIENTO/ANTICIPO SUBYACENTE DEL EMPRESTITO
					//-----------------------------------------------------------------
					RetiroCustodia.ObjDatosEntrada RetiroCustodiaSubyacente = new RetiroCustodia.ObjDatosEntrada();
					RetiroCustodiaSubyacente.IdGenerarMovimientoCustodia = carSub.IdSubyacente;	//ID Interno del proceso
					RetiroCustodiaSubyacente.TablaAfectada 					= "SUBYACENTE"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					RetiroCustodiaSubyacente.EsRetiroNegocio 				= "S";
					RetiroCustodiaSubyacente.IdCustodia 					= carSub.IdSubyacente;
					RetiroCustodiaSubyacente.FolioCustodia 					= carSub.FolioSubyacente;					
					RetiroCustodiaSubyacente.IdCarteraOrigen				= carSub.IdCarteraOrigen;
					RetiroCustodiaSubyacente.AccionOperacion				= entrada.p_OrigenLiquidacion;	//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					RetiroCustodiaSubyacente.FechaComprobante				= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
					RetiroCustodiaSubyacente.IdCliente						= carSub.IdCliente;
					RetiroCustodiaSubyacente.IdPortafolio					= carSub.IdPortafolio;
					RetiroCustodiaSubyacente.FechaOperacion					= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
					RetiroCustodiaSubyacente.IdTipoOperacion				= tipoOperacion;
					RetiroCustodiaSubyacente.IdOperacion					= 0L;
					RetiroCustodiaSubyacente.IdConcepto 					= conceptoOperacion;
					RetiroCustodiaSubyacente.IdCondicionLiquidacion 		= "T0";
					RetiroCustodiaSubyacente.FechaLiquidacion 				= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
					RetiroCustodiaSubyacente.IdTipoCanalIngreso				= carSub.IdTipoCanalIngreso;
					RetiroCustodiaSubyacente.IdUsuario						= entrada.p_IdUsuario;
					RetiroCustodiaSubyacente.IdModulo						= entrada.p_IdModulo;
					RetiroCustodiaSubyacente.Glosa							= entrada.p_OrigenLiquidacion + " PRESTAMISTA " + carSub.Nemotecnico + ", Folio: " + emprestito.FolioEmprestito.toString();					
					RetiroCustodiaSubyacente.IdInstrumento					= carSub.IdInstrumento;
					RetiroCustodiaSubyacente.IdMonedaInstrumento			= carSub.IdMonedaInstrumento;
					RetiroCustodiaSubyacente.IdEmisor						= carSub.IdEmisor;
					RetiroCustodiaSubyacente.IdBoveda						= carSub.IdBoveda;
					RetiroCustodiaSubyacente.IdCarteraOrigen				= carSub.IdCarteraOrigen;
					RetiroCustodiaSubyacente.PrecioCompra					= carSub.PrecioSubyacente;
					RetiroCustodiaSubyacente.TasaCompra						= carSub.TasaSubyacente;	
					RetiroCustodiaSubyacente.Cantidad 						= emprestito.INGCantidad;
					RetiroCustodiaSubyacente.ValorRescate 					= emprestito.INGValorRescate;
					RetiroCustodiaSubyacente.MontoCompraMonedaInstrumento 	= EGRMontoMonedaInstrumento;
					RetiroCustodiaSubyacente.MontoCompra 					= EGRMontoLocal;
					RetiroCustodiaSubyacente.IdMonedaEmision 				= carSub.IdMonedaEmision;
					RetiroCustodiaSubyacente.FechaVencimientoInstrumento 	= carSub.FechaVencimientoInstrumento;
					RetiroCustodiaSubyacente.EsFungible 					= carSub.EsFungible;
					RetiroCustodiaSubyacente.IdTipoReajusteInstrumento 		= carSub.IdTipoReajuste;
					RetiroCustodiaSubyacente.IdTipoProducto 				= carSub.IdTipoProducto;
					lstRetiros.add(RetiroCustodiaSubyacente);
				} //end ciclo x c/subyacente
				
				//---------------------------------------------------------	
				// MOVIMIENTO VENCIMIENTO/ANTICIPO CABECERA EMPRESTITO
				//---------------------------------------------------------
				
				RetiroCustodia.ObjDatosEntrada	RetiroCabeceraEmprestito = new RetiroCustodia.ObjDatosEntrada();
				RetiroCabeceraEmprestito.IdGenerarMovimientoCustodia = emprestito.FolioEmprestito;	//ID Interno del proceso
				RetiroCabeceraEmprestito.TablaAfectada 		  		= "CARTERA"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
				RetiroCabeceraEmprestito.EsRetiroNegocio 		  	= "S";
				RetiroCabeceraEmprestito.IdCustodia		 	  		= emprestito.IdCartera;
				RetiroCabeceraEmprestito.FolioCustodia 		  		= emprestito.FolioEmprestito;
				RetiroCabeceraEmprestito.AccionOperacion		  	= entrada.p_OrigenLiquidacion;	//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION					
				RetiroCabeceraEmprestito.FechaComprobante 	  		= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
				RetiroCabeceraEmprestito.IdCliente					= emprestito.IdCliente;
				RetiroCabeceraEmprestito.IdPortafolio			  	= emprestito.IdPortafolio;
				RetiroCabeceraEmprestito.FechaOperacion 		  	= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);					
				RetiroCabeceraEmprestito.IdTipoOperacion 		  	= tipoOperacion;
				RetiroCabeceraEmprestito.IdOperacion 			 	= 0L;
				RetiroCabeceraEmprestito.IdConcepto			  		= conceptoOperacion;
				RetiroCabeceraEmprestito.IdCondicionLiquidacion 	= "T0";
				RetiroCabeceraEmprestito.FechaLiquidacion		 	= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
				RetiroCabeceraEmprestito.IdTipoCanalIngreso 	  	= "SIS";
				RetiroCabeceraEmprestito.IdUsuario 			  		= entrada.p_IdUsuario;
				RetiroCabeceraEmprestito.IdModulo 			  		= entrada.p_IdModulo;
				RetiroCabeceraEmprestito.IdTipoReajusteInstrumento  = emprestito.IdTipoReajusteSubyacente;
				RetiroCabeceraEmprestito.Glosa 				  		= entrada.p_OrigenLiquidacion + " PRESTAMISTA ";
				RetiroCabeceraEmprestito.IdInstrumento 		  		= emprestito.IdInstrumentoEmprestito;
				RetiroCabeceraEmprestito.Cantidad				  	= emprestito.INGCantidad;	 
				RetiroCabeceraEmprestito.ValorRescate			  	= emprestito.INGValorRescate;
				RetiroCabeceraEmprestito.PrecioAPlazo			  		= emprestito.INGMontoPremio;
				RetiroCabeceraEmprestito.MontoAPlazo 			  		= EGRMontoAPlazoLocal;
				RetiroCabeceraEmprestito.MontoAPlazoMonedaInstrumento 	= EGRMontoAPlazoMonedaInstrumento;
				RetiroCabeceraEmprestito.TasaCompra			  			= emprestito.INGTasaEmprestito;
				RetiroCabeceraEmprestito.MontoCompra 					= EGRMontoLocal;
				RetiroCabeceraEmprestito.MontoCompraMonedaInstrumento 	= EGRMontoMonedaInstrumento;
				RetiroCabeceraEmprestito.MontoLiquidacion 	  			= EGRMontoAPlazoMonedaInstrumento;
				RetiroCabeceraEmprestito.IdMonedaLiquidacion 	  		= emprestito.IdMonedaSubyacente;
				RetiroCabeceraEmprestito.PrecioCompra					= emprestito.PrecioInstrumento;
				RetiroCabeceraEmprestito.IdTipoProducto					= "PRES";
				
				// AGREGAMOS MONTO LOCAL E INSTRUMENTO RESTANTE
				RetiroCabeceraEmprestito.MontoRestanteLocal             	= MontoRestanteLocal;
				RetiroCabeceraEmprestito.MontoRestanteMonedaInstrumento     = MontoRestanteMonedaInstrumento;
				
				RetiroCabeceraEmprestito.IdInstrumentoPrestamo              = emprestito.IdInstrumentoSubyacente; 
				RetiroCabeceraEmprestito.NemotecnicoPrestamo 				= emprestito.NemoSubyacente;
				RetiroCabeceraEmprestito.IdTipoInstrumentoPrestamo 	     	= emprestito.IdTipoInstrumentoSubyacente;
				RetiroCabeceraEmprestito.TasaCompraInstrumentoPrestamo 	    = carteraSubyacente.get(0).TasaSubyacente;
				RetiroCabeceraEmprestito.IdMonedaInstrumento 	     		= carteraSubyacente.get(0).IdMonedaInstrumento;
				RetiroCabeceraEmprestito.IdMonedaEmision			 		= carteraSubyacente.get(0).IdMonedaEmision;
				RetiroCabeceraEmprestito.FechaVencimientoInstrumento 		= carteraSubyacente.get(0).FechaVencimientoInstrumento;
				lstRetiros.add(RetiroCabeceraEmprestito);

				RetiroCustodia retirosCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa				= entrada.p_IdEmpresa;
				paramInSP.p_IdModulo				= entrada.p_IdModulo;
				paramInSP.p_IdUsuario				= entrada.p_IdUsuario;
				paramInSP.p_ValorizacionAutomatica 	= "S";
				paramInSP.p_SimularOperacion		= entrada.p_SimularOperacion;
				paramInSP.p_TicketAtencion 			= 0L;
				paramInSP.p_DatosEntrada			= lstRetiros;		
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retirosCustodia.logica(con, paramInSP);
				
				
				if (MensajeSalida.getNumeroErrores(resultRetiros.P_OUT_MENSAJES) > 0) {
                    salida.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
                } else {
                	for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultRetiros.P_OUT_DATOSSALIDA) {
						HashMap<String,String> par = new HashMap<>();
						par.put("TipoCartera", datoSalida.TablaOrigen);
						par.put("FolioCartera", datoSalida.FolioCustodia.toString());
						
						String codigo = "UI_TIPO_CARTERA_RETIRADA_FOLIO"; //::TipoCartera:: retirada, Folio : ::FolioCartera::
						String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();						
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion, par, con, entrada.p_IdEmpresa));
						salida.P_OUT_EMPRESTITOS_OK.add(emprestito.FolioEmprestito);
    				} 
                }
				
	
				if("S".equals(emprestito.ConCustodiaPrestamista)) {
					for (Subyacente carSub: carteraSubyacente){
						if(carSub.IdCarteraRetirada == 0L ) {
							IngresoCustodia.ObjDatosEntrada ObjEntradaTransito = new IngresoCustodia.ObjDatosEntrada();
							ObjEntradaTransito.IdGenerarMovimientoCustodia = carSub.FolioSubyacente;
							ObjEntradaTransito.IdCliente = carSub.IdCliente;
							ObjEntradaTransito.IdPortafolio = carSub.IdPortafolio;
							ObjEntradaTransito.IdTipoProducto = "CUST";
							ObjEntradaTransito.IdConcepto = conceptoOperacion;
							ObjEntradaTransito.IdTipoOperacion = "INGTIT";
							ObjEntradaTransito.FechaOperacion = Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
							ObjEntradaTransito.FechaVencimientoOperacion = Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
							ObjEntradaTransito.IdCondicionLiquidacion = "T0";
							ObjEntradaTransito.FechaLiquidacion = java.sql.Date.valueOf(entrada.p_FechaVencimientoAnticipo);
							ObjEntradaTransito.IdInstrumento = carSub.IdInstrumento;
							ObjEntradaTransito.Cantidad = emprestito.INGCantidad;
							ObjEntradaTransito.CorteMinimo = carSub.CorteMinimo;
							ObjEntradaTransito.PrecioCompra = emprestito.PrecioInstrumento;
							ObjEntradaTransito.TasaCompra = carteraSubyacente.get(0).TasaSubyacente;
							ObjEntradaTransito.MontoCompra =  emprestito.INGMontoTotalEmprestito;
							ObjEntradaTransito.MontoCompraMonedaInstrumento = emprestito.INGMontoTotalEmprestito;
							ObjEntradaTransito.PrecioAPlazo = emprestito.PrecioInstrumento;
							ObjEntradaTransito.MontoAPlazo = emprestito.INGMontoTotalEmprestito;
							ObjEntradaTransito.Nemotecnico = carSub.Nemotecnico;
							ObjEntradaTransito.EsFungible = carSub.EsFungible;
							ObjEntradaTransito.EsNominal = "N";
							ObjEntradaTransito.IdMonedaInstrumento = carSub.IdMonedaInstrumento;
							ObjEntradaTransito.IdMonedaEmision = carSub.IdMonedaEmision;
							ObjEntradaTransito.IdTipoReajusteInstrumento = carSub.IdTipoReajuste;
							ObjEntradaTransito.FechaVencimientoInstrumento = carSub.FechaVencimientoInstrumento;
							ObjEntradaTransito.IdBoveda = carSub.IdBoveda;
							ObjEntradaTransito.IdEmisor = carSub.IdEmisor;
							ObjEntradaTransito.TablaAfectada = "TRANSITO";
							ObjEntradaTransito.AccionOperacion = "INGRESO";
							ObjEntradaTransito.FolioCarteraOrigen = 0L;
							ObjEntradaTransito.EsRegistroNuevo = "S";
							ObjEntradaTransito.EsVentaCorta = "N";
							ObjEntradaTransito.Glosa = "EMPRESTITO " + carSub.Nemotecnico;							
							entradaIngresoCustodiaTransitoConCustodia.p_DatosEntrada.add(ObjEntradaTransito);
						}
					}
				}
				
				if("S".equals(entrada.p_RebajaPrestamo) && "S".equals(emprestito.ConCustodiaPrestatario) && !("PRTVTC".equals(emprestito.IdTipoPrestamo) || "PRVCRF".equals(emprestito.IdTipoPrestamo))) {
					for (Subyacente carSub: carteraSubyacente){
						RetiroCustodia.ObjDatosEntrada	RetiroSubyacenteCart = new RetiroCustodia.ObjDatosEntrada();
						RetiroSubyacenteCart.IdGenerarMovimientoCustodia = -1*carSub.IdSubyacente;	//ID Interno del proceso
						RetiroSubyacenteCart.TablaAfectada 				= "CARTERA"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
						RetiroSubyacenteCart.EsRetiroNegocio 			= "S";
						RetiroSubyacenteCart.AccionOperacion			= "RETIRO";	//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION						
						RetiroSubyacenteCart.FechaComprobante			= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
						RetiroSubyacenteCart.IdCliente					= emprestito.IdClientePrestamo;
						RetiroSubyacenteCart.IdPortafolio				= emprestito.IdPortafolioPrestamo;
						RetiroSubyacenteCart.FechaOperacion				= Fecha.toDate(entrada.p_FechaVencimientoAnticipo);
						RetiroSubyacenteCart.IdTipoOperacion			= tipoOperacion;
						RetiroSubyacenteCart.IdOperacion				= 0L;
						RetiroSubyacenteCart.IdConcepto 				= conceptoOperacion;
						RetiroSubyacenteCart.IdCondicionLiquidacion 	= "T0";
						RetiroSubyacenteCart.IdTipoCanalIngreso			= carSub.IdTipoCanalIngreso;
						RetiroSubyacenteCart.IdUsuario					= entrada.p_IdUsuario;
						RetiroSubyacenteCart.IdModulo					= entrada.p_IdModulo;
						RetiroSubyacenteCart.Glosa						= entrada.p_OrigenLiquidacion + " PRESTAMO " + carSub.Nemotecnico + ", Folio " + emprestito.FolioPrestamo;					
						RetiroSubyacenteCart.IdInstrumento				= carSub.IdInstrumento;
						RetiroSubyacenteCart.IdMonedaInstrumento		= carSub.IdMonedaInstrumento;
						RetiroSubyacenteCart.IdEmisor					= carSub.IdEmisor;
						RetiroSubyacenteCart.IdBoveda					= carSub.IdBoveda;
						RetiroSubyacenteCart.IdCarteraOrigen			= carSub.IdCarteraOrigen;
						RetiroSubyacenteCart.PrecioCompra				= carSub.PrecioSubyacente;
						RetiroSubyacenteCart.TasaCompra					= carSub.TasaSubyacente;	
						RetiroSubyacenteCart.Cantidad 					= "ANTICIPO".equalsIgnoreCase(entrada.p_OrigenLiquidacion) ? emprestito.INGCantidad : carSub.Cantidad;
						RetiroSubyacenteCart.ValorRescate 				= carSub.ValorRescate;
						RetiroSubyacenteCart.MontoCompraMonedaInstrumento = carSub.MontoATasaSubyacenteActual;
						RetiroSubyacenteCart.IdMonedaEmision 			= carSub.IdMonedaEmision;
						lstRetirosCustodiaPrestamos.add(RetiroSubyacenteCart);
					}
				}
			}  // FIN - for (PrestamoVencer_Proceso proPreVen: ProcesoPrestamoVencer)
			
			
			//---------------------------------------------------------	
			//if("S".equals(emprestito.ConCustodiaPrestamista))
			//---------------------------------------------------------	
			if(entradaIngresoCustodiaTransitoConCustodia.p_DatosEntrada.size() > 0) {
				entradaIngresoCustodiaTransitoConCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaIngresoCustodiaTransitoConCustodia.p_IdModulo = entrada.p_IdModulo;
				entradaIngresoCustodiaTransitoConCustodia.p_IdUsuario = entrada.p_IdUsuario;
				entradaIngresoCustodiaTransitoConCustodia.p_SimularOperacion = "N";
				entradaIngresoCustodiaTransitoConCustodia.p_TicketAtencion = 0L;
				entradaIngresoCustodiaTransitoConCustodia.p_ValorizacionAutomatica = "S";
				
				IngresoCustodia ingresoCustodiaTransitoConCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaSalida salidaMovimientoTransitoConCustodia = ingresoCustodiaTransitoConCustodia.logica(con, entradaIngresoCustodiaTransitoConCustodia);				
				salida.P_OUT_MENSAJES.addAll(salidaMovimientoTransitoConCustodia.P_OUT_MENSAJES);
				
				List<Long> transitosPorCertificar = salidaMovimientoTransitoConCustodia.P_OUT_DATOSSALIDA
						.stream()
						.filter(cartera->"N".equalsIgnoreCase(cartera.RegistroConProblemas))
						.map(cartera->cartera.IdCustodia)
						.collect(Collectors.toList());
				
				List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransito = GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, transitosPorCertificar);
				List<IngresoCustodia.ObjDatosEntrada> lstIngresosCustodiaPrestamista = new ArrayList<>();
				
				for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movTransito : movimientosEnTransito) {
					IngresoCustodia.ObjDatosEntrada	obj = new IngresoCustodia.ObjDatosEntrada();
					obj.IdGenerarMovimientoCustodia			= movTransito.IdMovimientoCarteraEnTransito;
					obj.TablaAfectada 						= "CARTERA";
					obj.EsRegistroNuevo						= "S";						
					obj.IdMovimientoCarteraEnTransitoOrigen	= movTransito.IdMovimientoCarteraEnTransito;
					obj.AccionOperacion 					= "INGRESO";		
					obj.IdCliente 							= movTransito.IdCliente;
					obj.IdPortafolio 						= movTransito.IdPortafolio; 
					obj.IdCuentaCustodioPortafolio 			= movTransito.IdCuentaCustodioPortafolio;
					obj.FechaOperacion 						= movTransito.FechaOperacion;
					obj.FechaVencimientoOperacion 			= movTransito.FechaVencimientoOperacion;
					obj.PlazoEnDiasOperacion 				= movTransito.PlazoEnDiasOperacion;
					obj.IdTipoOperacion						= movTransito.IdTipoOperacion;		
					obj.IdConcepto							= movTransito.IdConcepto;
					obj.IdCondicionLiquidacion 				= movTransito.IdCondicionLiquidacion; 
					obj.FechaLiquidacion 					= movTransito.FechaLiquidacion;
					obj.IdTipoCanalIngreso  				= movTransito.IdTipoCanalIngreso; 		
					obj.CodigoIFRS 							= movTransito.CodigoIFRS;		 		 
					obj.IdTipoComprobante 					= movTransito.IdTipoComprobante; 
					obj.FolioComprobante 					= movTransito.FolioComprobante; 
					obj.IdBolsa								= movTransito.IdBolsa;
					obj.EsIntraday 							= movTransito.EsIntraday; 
					obj.TipoCarteraCorredor 				= movTransito.TipoCarteraCorredor;
					obj.IdUsuario 							= movTransito.IdUsuario;
					obj.IdSucursal 							= movTransito.IdSucursal; 
					obj.IdAgente 							= movTransito.IdAgente;
					obj.Glosa 								= movTransito.Glosa;
					obj.IdInstrumento 						= movTransito.IdInstrumento;
					obj.IdMonedaInstrumento					= movTransito.IdMonedaInstrumento;
					obj.IdTipoReajusteInstrumento 			= movTransito.IdTipoReajuste;
					obj.IdEmisor 							= movTransito.IdEmisor;
					obj.EsFungible 							= movTransito.EsFungible;
					obj.IdBoveda 							= movTransito.IdBoveda;
					obj.CuentaBoveda 						= movTransito.CuentaBoveda;
					obj.EsNominal 							= movTransito.EsNominal;
					obj.IdMonedaEmision 					= movTransito.IdMonedaEmision;
					obj.Cantidad 							= movTransito.Cantidad; 
					obj.CorteMinimo 						= movTransito.CorteMinimo; 
					obj.ValorRescate 						= movTransito.ValorRescate; 
					obj.PrecioCompra 						= movTransito.PrecioCompra; 
					obj.TasaCompra							= movTransito.TasaCompra;
					obj.MontoCompra							= movTransito.MontoCompra;
					obj.MontoCompraMonedaInstrumento 		= movTransito.MontoCompraMonedaInstrumento;
					obj.TipoCarteraNormativa 				= movTransito.TipoCarteraNormativa;
					obj.FechaCurse 							= movTransito.FechaCurse;
					obj.TipoFactorAjuste 					= movTransito.TipoFactorAjuste;
					obj.ValorFactorAjusteaFechaCurse 		= movTransito.ValorFactorAjusteaFechaCurse;
					obj.CostoEnFactorAjuste 				= movTransito.CostoEnFactorAjuste;
					obj.IdOperacion							= movTransito.IdOperacion;
					obj.IdRegimenFondoMutuo					= movTransito.IdRegimenFondoMutuo;
					obj.FechaOrigenCompra					= movTransito.FechaOrigenCompra;
					obj.IdTipoProducto                      = movTransito.IdTipoProducto;
					obj.Nemotecnico							= movTransito.Nemotecnico;
					lstIngresosCustodiaPrestamista.add(obj);
				}
				
				if(lstIngresosCustodiaPrestamista.size() > 0) {
					IngresoCustodia.IngresoCustodiaEntrada entradaIngresoConCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
					entradaIngresoConCustodia.p_IdEmpresa 				= entrada.p_IdEmpresa;
					entradaIngresoConCustodia.p_IdModulo 				= entrada.p_IdModulo;
					entradaIngresoConCustodia.p_IdUsuario 				= entrada.p_IdUsuario;
					entradaIngresoConCustodia.p_SimularOperacion 		= "N";
					entradaIngresoConCustodia.p_TicketAtencion 			= 0L;
					entradaIngresoConCustodia.p_ValorizacionAutomatica 	= "S";
					entradaIngresoConCustodia.p_DatosEntrada			= lstIngresosCustodiaPrestamista;
					
					IngresoCustodia ingresoCustodiaConCustodia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaSalida salidaMovimientoConCustodia = ingresoCustodiaConCustodia.logica(con, entradaIngresoConCustodia);
					salida.P_OUT_MENSAJES.addAll(salidaMovimientoConCustodia.P_OUT_MENSAJES);
					
					List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> transitosOKk = salidaMovimientoTransitoConCustodia.P_OUT_DATOSSALIDA
							.stream()
							.filter(result->"N".equalsIgnoreCase(result.RegistroConProblemas))
							.map(result->{
									GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar movTraAct = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
									movTraAct.IdMovimientoCarteraEnTransito = result.IdCustodia;
									movTraAct.EsCertificacion = "S";
									movTraAct.EsAnulacion = "N";
									movTraAct.IdUsuario = entrada.p_IdUsuario;
									return movTraAct;
								}
							)
							.collect(Collectors.toList());
					
					if (transitosOKk.size() > 0) {
						GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, transitosOKk);
					}
				}
			}
			
			//---------------------------------------------------------	
			// if("S".equals(entrada.p_RebajaPrestamo) && "S".equals(emprestito.ConCustodiaPrestatario))
			//---------------------------------------------------------	
			if(lstRetirosCustodiaPrestamos.size() > 0) {
				RetiroCustodia retirosCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa				= entrada.p_IdEmpresa;
				paramInSP.p_IdModulo				= entrada.p_IdModulo;
				paramInSP.p_IdUsuario				= entrada.p_IdUsuario;
				paramInSP.p_ValorizacionAutomatica 	= "S";
				paramInSP.p_SimularOperacion		= entrada.p_SimularOperacion;
				paramInSP.p_TicketAtencion 			= 0L;
				paramInSP.p_DatosEntrada			= lstRetirosCustodiaPrestamos;	
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retirosCustodia.logica(con, paramInSP);				
				salida.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
			}
			
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		log.write("PARAMETROS DE SALIDA : " + salida.toString());		
		log.write("== TERMINO EJECUCION LIQUIDAR CUSTODIA EMPRESTITOS ==");
		
		return salida;
	};
	
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		LiquidarCustodiaEmprestito_Entrada jin = gson.fromJson(in.toString(), LiquidarCustodiaEmprestito_Entrada.class);
		
		LiquidarCustodiaEmprestito_Salida out = logica(conn, jin);
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
		
		try (Connection con = PlataformaUtils.getQAConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "";			
		
			LiquidarCustodiaEmprestito_Entrada Objin = gson.fromJson(json, LiquidarCustodiaEmprestito_Entrada.class);
			LiquidarCustodiaEmprestito demo = new LiquidarCustodiaEmprestito();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				LiquidarCustodiaEmprestito_Salida ObjOut = demo.logica(con, Objin);
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
