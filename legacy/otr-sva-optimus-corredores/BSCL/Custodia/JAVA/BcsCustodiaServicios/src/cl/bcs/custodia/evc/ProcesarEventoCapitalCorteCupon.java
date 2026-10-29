package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos.ProcesarEventoCapitalCambioDeNemosEntrada2;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos.ProcesarEventoCapitalCambioDeNemosSalida2;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos.InstrumentoNemo;
import cl.bcs.custodia.utils.DeterminaNuevoNemo;
import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;

public class ProcesarEventoCapitalCorteCupon  implements JavaCall{

	private final Log log = new Log("CUS.ProcesarEventoCapitalCorteCupon");

	private final String SQL_SELECT_CABECERA_FECHA_PAGO_ACTIVA 			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectCabeceraEventoCapital.sql");
	private final String SQL_SELECT_CARTERA_EVENTO_CAPITAL 				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectCarteraEventoCapital.sql");
	private final String SQL_SELECT_CONTRAPARTE_EVENTO_CAPITAL 			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectContraparteEventoCapital.sql");
	private final String SQL_SELECT_CONTRAPARTE_BOVEDAS_EVENTO_CAPITAL 	= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectContraparteBovedasEventoCapital.sql");
	private final String SQL_SELECT_FORMA_PAGO_EVENTO_CAPITAL 			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectFormaPagoEventoCapital.sql");
	private final String SQL_INSERT_CABECERA_EVENTO_CAPITAL 			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_InsertCabeceraEventoCapital.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE 				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_InsertEventoCapitalCliente.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_FORMA_PAGO 			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_InsertEventoCapitalFormaPago.sql");
	private final String SQL_SELECT_GARANTIA_EVENTO_CAPITAL 		    = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectGarantiaEventoCapital.sql");
	private final String SQL_SELECT_CONTRAPARTE_GARANTIA_CC 		    = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectContraparteGarantiaEventoCapital.sql");
	private final String SQL_SELECT_CTA_PROPIA_PARA_CC = "SELECT \"IdPortafolio\" FROM BSCL.\"BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia\"";	
	private final String SQL_CONSULTA_OPERACIONES_MESA = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectOperacionesATermino.sql");
	private final String SQL_SELECT_DATOS_CLIENTE_ORIGEN = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectDatosClienteDerecho.sql");
	private final String SQL_SELECT_OPERACIONES_NOMINAL_CC = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectNominalEventoCapital.sql");
	private final String SQL_SELECT_CONTRAPARTE_NOMINAL_CC = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCorteCupon_SelectContraparteNominalEventoCapital.sql");
	
	// ----------------------------------------------------
	// CLASES DE ENTRADA
	// ----------------------------------------------------
	public static class EntradaProcesarEventoCapitalCorteCupon{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaCorteCuponDesde;
		public String 		p_FechaCorteCuponHasta;
		public String       p_Confirmado;

		public List<Long>	p_IdInstrumento;
		
		public EntradaProcesarEventoCapitalCorteCupon(){
			this.p_FechaCorteCuponDesde = "0001-01-01";
			this.p_FechaCorteCuponHasta = "0001-01-01";

			this.p_IdInstrumento 		= new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	// ----------------------------------------------------
	// CLASES DE SALIDA
	// ----------------------------------------------------

	
	public static class SalidaProcesarEventoCapitalCorteCupon{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <DetalleCalculo> P_OUT_RESUMEN_CALCULO;
		
		
		public SalidaProcesarEventoCapitalCorteCupon(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	
	
	// ----------------------------------------------------
	// CLASES COMPLEMANTARIAS
	// ----------------------------------------------------

	
	public static class EventoCapital{
		public Long 			IdEventoCapital;
		public Long 			IdEmpresa;
		public String 			IdTipoEventoCapital;
		public Long 			Secuencia;
		public String 			Descripcion;
		public String 			EsInscrito;
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdTipoInstrumento;
		public Long 			IdInstrumento;
		public String 			Nemo;
		public BigDecimal 		Factor;
		public BigDecimal 		Valor;
		public String 			IdMoneda;
		public Long 			IdInstrumentoNuevo;
		public String 			NemoNuevo;
		public BigDecimal 		FactorNuevo;
		public Date 			FechaActualizacion;
		public Date 			FechaLimite;
		public Date 			FechaPago;
		public Long 			NumeroDividendo;
		public String 			IdTipoDividendo;
		public BigDecimal 		PrecioAnterior;
		public Timestamp 		FechaHoraIngresoRegistro;
		public Timestamp 		FechaHoraUltimaActualizacion;
		public String 			TipoIngreso;
		public Long 			IdUsuario;
		public String 			IdEstado;
		public String 			Origen;
		public String 			IdEmisor;		
		public String 			TipoAccion;    // CAMBIO_NEMO | CORTE_CUPON
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientePortafolioCtaPropia{
		public Long 			IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Garantia {
		public Long 			FolioGarantia;
		public String 			RazonSocial; 
		public String 			DocumentoIdentidad; 
		public Long 			IdPortafolio; 
		public String 			CodigoPortafolio; 
		public String 			Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultaPrecioReajuste {
		public String 			Llave;
		public String 			IdTipoReajuste;
		public Date				FechaPago;
		public Date				FechaMoneda;
		public BigDecimal		TipoCambioMercado;
		public String			EsProyeccion;
		public String			EsFechaExacta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente
	{
		public Long 			IdEventoCapitalCliente;
		public String			EsCarteraNegativa;
		public Long 			IdEventoCapital;
		public String 			IdTipoEventoCapital;
		public Long 			Secuencia;
		public Long 			INGNumeroCupon;
		public Long 			IdSocioNegocio;
		public String 			TipoSocioNegocio;
		public Long 			IdPortafolio;
		public String 			IdTipoDocumentoIdentidad;
		public String 			DocumentoIdentidad;
		public String 			NombreCliente;
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdTipoInstrumento;
		public Long 			IdInstrumento;
		public String 			Nemo;
		public Long 			IdInstrumentoNuevo;
		public String 			NemoNuevo;
		public Date 			FechaCierre;
		public Date 			FechaPago;
		public BigDecimal 		CantidadDerecho;
		public String 			TipoCartera;
		public BigDecimal 		CantidadNueva;
		public BigDecimal 		MontoCapital;
		public BigDecimal 		MontoInteres;
		public BigDecimal 		Total;
		public String 			IdMoneda;
		public Long 			SecuenciaCupon;
		public Long 			IdEmpleadoEjecutivoPortafolio;
		public String 			CodigoUsuarioAgente;
		public String 			NombreAgente;
		public String 			CodigoSucursal;
		public Timestamp 		FechaHoraIngresoRegistro;
		public Timestamp 		FechaHoraUltimaActualizacion;
		public Long 			IdUsuario;
		public String 			IdEstado;
		public String 			IdBoveda;
		public String 			IdBolsa;
		public String 			IdConceptoGarantia;
		public String 			IdTipoOperacionGarantia;
		public BigDecimal 		MontoReajuste;
		public String 			Origen;
		public String 			IdTipoReajuste;
		public BigDecimal 		INGMontoInteresCorteCupon;
		public String 			CodigoCorredorContraparte = "";
		public String 			TipoOperacion = "";
		public String 	 IdTipoOperacion;
		public BigDecimal		MontoRetencion;
		public Date				FechaLiquidacion;
		public BigDecimal		INGValorFactorMonedaInstrumentoProximo;
		public Date				INGFechaCorteCupon;
		public Date				INGFechaFactorMonedaAdicionalProximo;
		public BigDecimal		INGValorFactorMonedaInstrumentoActual;
		public String 			UsaProximoFactorValorMoneda;
		public String			INGIdTipoOperacion;
		public Long				IdCartera;
		public String			QueryOrigen;
		
		public EventoCapitalCliente(EventoCapitalCliente copia) {
		    this.IdEventoCapitalCliente = copia.IdEventoCapitalCliente;
		    this.EsCarteraNegativa = copia.EsCarteraNegativa;
		    this.IdEventoCapital = copia.IdEventoCapital;
		    this.IdTipoEventoCapital = copia.IdTipoEventoCapital;
		    this.Secuencia = copia.Secuencia;
		    this.INGNumeroCupon = copia.INGNumeroCupon;
		    this.IdSocioNegocio = copia.IdSocioNegocio;
		    this.TipoSocioNegocio = copia.TipoSocioNegocio;
		    this.IdPortafolio = copia.IdPortafolio;
		    this.IdTipoDocumentoIdentidad = copia.IdTipoDocumentoIdentidad;
		    this.DocumentoIdentidad = copia.DocumentoIdentidad;
		    this.NombreCliente = copia.NombreCliente;
		    this.IdClasificacionTipoInstrumento = copia.IdClasificacionTipoInstrumento;
		    this.IdTipoInstrumento = copia.IdTipoInstrumento;
		    this.IdInstrumento = copia.IdInstrumento;
		    this.Nemo = copia.Nemo;
		    this.IdInstrumentoNuevo = copia.IdInstrumentoNuevo;
		    this.NemoNuevo = copia.NemoNuevo;
		    this.FechaCierre = copia.FechaCierre;
		    this.FechaPago = copia.FechaPago;
		    this.CantidadDerecho = copia.CantidadDerecho;
		    this.TipoCartera = copia.TipoCartera;
		    this.CantidadNueva = copia.CantidadNueva;
		    this.MontoCapital = copia.MontoCapital;
		    this.MontoInteres = copia.MontoInteres;
		    this.Total = copia.Total;
		    this.IdMoneda = copia.IdMoneda;
		    this.SecuenciaCupon = copia.SecuenciaCupon;
		    this.IdEmpleadoEjecutivoPortafolio = copia.IdEmpleadoEjecutivoPortafolio;
		    this.CodigoUsuarioAgente = copia.CodigoUsuarioAgente;
		    this.NombreAgente = copia.NombreAgente;
		    this.CodigoSucursal = copia.CodigoSucursal;
		    this.FechaHoraIngresoRegistro = copia.FechaHoraIngresoRegistro;
		    this.FechaHoraUltimaActualizacion = copia.FechaHoraUltimaActualizacion;
		    this.IdUsuario = copia.IdUsuario;
		    this.IdEstado = copia.IdEstado;
		    this.IdBoveda = copia.IdBoveda;
		    this.IdBolsa = copia.IdBolsa;
		    this.IdConceptoGarantia = copia.IdConceptoGarantia;
		    this.IdTipoOperacionGarantia = copia.IdTipoOperacionGarantia;
		    this.MontoReajuste = copia.MontoReajuste;
		    this.Origen = copia.Origen;
		    this.IdTipoReajuste = copia.IdTipoReajuste;
		    this.INGMontoInteresCorteCupon = copia.INGMontoInteresCorteCupon;
		    this.CodigoCorredorContraparte = copia.CodigoCorredorContraparte = "";
		    this.TipoOperacion = copia.TipoOperacion = "";
		    this.IdTipoOperacion = copia.IdTipoOperacion;
		    this.MontoRetencion = copia.MontoRetencion;
		    this.FechaLiquidacion = copia.FechaLiquidacion;
		    this.INGValorFactorMonedaInstrumentoProximo = copia.INGValorFactorMonedaInstrumentoProximo;
		    this.INGFechaCorteCupon = copia.INGFechaCorteCupon;
		    this.INGFechaFactorMonedaAdicionalProximo = copia.INGFechaFactorMonedaAdicionalProximo;
		    this.INGValorFactorMonedaInstrumentoActual = copia.INGValorFactorMonedaInstrumentoActual;
		    this.UsaProximoFactorValorMoneda = copia.UsaProximoFactorValorMoneda;
		    this.INGIdTipoOperacion = copia.INGIdTipoOperacion;
		    this.IdCartera = copia.IdCartera;
		    this.QueryOrigen = copia.QueryOrigen;
		}
		
		public EventoCapitalCliente() {
			this.MontoRetencion = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class EventoCapitalClienteFormaDePago{
		public Long 		IdEventoCapitalClienteFormaDePago;
		public Long 		IdEventoCapitalCliente;
		public Long 		IdPortafolio;
		public String 		IdTipoMovimiento;
		public String 		IdModalidadPago;
		public String 		IdMedioDePago;
		public String 		NombreMedioDePago;
		public Long 		IdBanco;
		public String 		NombreBanco;
		public String 		IdTipoCuentaBancaria;
		public String 		NombreTipoCuentaBancaria;
		public Long 		IdCuentaBancaria;
		public String 		NombreCuentaBancaria;
		public String 		NumeroCuentaBancaria;
		public String 		IdTipoTarjetaBancaria;
		public String 		NumeroTarjetaBancaria;
		public String 		NumeroMedioDePago;
		public String 		Sucursal;
		public Date 		FechaVctoDocumento;
		public BigDecimal 	Monto;
		public Timestamp 	FechaHoraIngresoRegistro;
		public Long 		IdUsuarioIngreso;
		public String 		IdEstado;

		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionesNominalCC extends BcsPojo{
		public Long         IdEventoCapital;
		public String       IdTipoEventoCapital;
		public String 	 	IdTipoOperacion;
		public String       IdMonedaEmisionInstrumento;
		public String       EsCarteraNegativa;
		public Long			IdPortafolio;
		public Long 		IdPortafolioRelacionado;
		public Long 	    IdEmpleadoEjecutivoPortafolio;
		public Long			IdCliente;
		public Long 		IdClienteRelacionado;
		public String 		IdEstado;
		public BigDecimal 	Cantidad;
		public BigDecimal   CantidadNueva;
		public String 	    CodigoUsuarioAgente;
		public String 		CodigoSucursal;
		public BigDecimal   PrecioCompra;
		public Long 		IdInstrumento;
		public String       QueryOrigen;
	    public String       NombreCliente;
	    public String       DocumentoIdentidad;
	    public String       OrigenCustodia;
	    public String       IdTipoDocumentoIdentidad;
	    public String       IdTipoInstrumento;
	    public Long         IdInstrumentoNuevo;
	    public String       NemoNuevo;
	    public String       IdConceptoGarantia;
		public String		IdTipoOperacionGarantia;
	    public String       IdClasificacionTipoInstrumento;
	    public String       IdMonedaInstrumento;
	    public String       IdMonedaAdicional;
	    public String       Nemotecnico;
	    public String       TipoCartera;
	    public String       NombreAgente;
	    public Long         IdSocioDeNegocio;
	    public String       IdBoveda;
	    public String       IdBolsa;
	    public Date         FechaLiquidacion;
	    public Date         FechaOperacion;
	    public Timestamp 	FechaHoraIngresoRegistro;
	    public Long		    IdCartera;
	    public String       TipoSocioNegocio;
	    public String       Emisor;
        public Date         FechaEmision;
        public Date         FechaVencimiento;
        public String       IdTipoReajusteInstrumento;
        public String       IdCondicionDeLiquidacion;
		
		
		
	}
	
	public static class OperacionesATerminoMesa{
		public String 	 	IdTipoOperacion;
		public Long			IdPortafolio;
		public Long 		IdPortafolioRelacionado;
		public Long			IdCliente;
		public Long 		IdClienteRelacionado;
		public String 		IdEstado;
		public BigDecimal 	Cantidad;
		public Long 		IdInstrumento;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DevolucionDerecho{
		public Long			IdPortafolio;
		public Long			IdCliente;
		public BigDecimal 	Cantidad;
		public String		EsResta;
		public Boolean		ExisteCartera;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleCalculo extends BcsPojo {

	    public String IdMonedaOrigen;
	    public String FechaPago;
	    public String FechaValor;
	    public BigDecimal TipoCambio;
	    public boolean RequiereConfirmacion ;
	    
	}

	public static class DatosClienteDerechoOrigen{
		public Long			IdPortafolio;
		public Long			IdCliente;
		public String 		DocumentoIdentidad;
		public String 		NombreAgente;
		public String 		NombreCliente;
		public Long 		IdSocioNegocio;
		public Long 		IdEmpleadoEjecutivoPortafolio;
		public String 		CodigoUsuarioAgente;
		public String 		CodigoSucursal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FechaMonedaCorteCupon{
		public String IdTipoReajuste;
		public String FechaPago;
	}
	
	public static class ParametroMensajeProyeccionCC{
		public String Nemo;
		public Date FechaLimite;
	}
	
	// ---------------------------------------------
	// LOGICA
	// ---------------------------------------------
	
	public SalidaProcesarEventoCapitalCorteCupon logica(Connection con, EntradaProcesarEventoCapitalCorteCupon entrada) throws Exception{
	log.write("INICIO:: ProcesarEventoCapitalCorteCupon");
		SalidaProcesarEventoCapitalCorteCupon out = new SalidaProcesarEventoCapitalCorteCupon();
		SQL sql = new SQL();
		
		String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String pNombreEmisor		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "CUS.NombreEmisor");
		String agrupaCCCtaPropia		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "AgrupaCorteCuponCtaPropia");

		String agruparEventoCapitalCliente    	= "S";
		try
		{
			agruparEventoCapitalCliente    	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "AgruparEventoCapitalCliente");
		}
		catch(Exception ex)
		{
			agruparEventoCapitalCliente    	= "S";
		}
		
		log.write("PARAMETROS DE ENTRADA > " + entrada);
		
		// --------------------------------------------------------------------------------
		// OBTIENE LAS CARTERAS QUE GENERAN EVENTO CORTE CUPON Y LAS QUE GENERAN CAMBIO DE NEMO
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		HashMap<String,Object> paramsCliente = new HashMap<String,Object>();
		
		params.put("FechaCorteCuponDesde", entrada.p_FechaCorteCuponDesde);
		paramsCliente.put("FechaCorteCuponDesde", entrada.p_FechaCorteCuponDesde);
		params.put("FechaCorteCuponHasta", entrada.p_FechaCorteCuponHasta);
		paramsCliente.put("FechaCorteCuponHasta", entrada.p_FechaCorteCuponHasta);
		params.put("IdInstrumento", entrada.p_IdInstrumento);	
		paramsCliente.put("IdInstrumento", entrada.p_IdInstrumento);
		paramsCliente.put("AgruparEventoCapitalCliente", agruparEventoCapitalCliente);
		
		
		
		List<EventoCapital> carterasFechaPagoActiva  = sql.query(con, SQL_SELECT_CABECERA_FECHA_PAGO_ACTIVA, params, EventoCapital.class);
		
		List<EventoCapital> eventoCapital = new ArrayList<EventoCapital>();  
		List<EventoCapital> cambioNemo    = new ArrayList<EventoCapital>();
		
	
		Map<Long, EventoCapital> mapPorInstrumento = new HashMap<>();

		for (EventoCapital ecc : carterasFechaPagoActiva) {
		    if ("CORTE_CUPON".equalsIgnoreCase(ecc.TipoAccion)) {
		        EventoCapital exist = mapPorInstrumento.get(ecc.IdInstrumento);

		        if (exist == null || (exist.Secuencia == 0 && ecc.Secuencia != 0)) {
		            mapPorInstrumento.put(ecc.IdInstrumento, ecc);
		        }
		    }

		    if ("CAMBIO_NEMO".equalsIgnoreCase(ecc.TipoAccion)) {
		        ecc.NemoNuevo = DeterminaNuevoNemo.NuevoNemo(ecc.Nemo, fechaSistema);
		        cambioNemo.add(ecc);
		    }
		}
		
		eventoCapital.clear();
		eventoCapital.addAll(mapPorInstrumento.values());
		
		//Si los 2 procesos no tienen datos para procesar sale 
		if (eventoCapital.size() == 0  && cambioNemo.size() == 0)
		{
			out.P_OUT_ERRORESCONTADOR += 1;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_FECHA_SIN_CORTES_DE_CUPON",new HashMap<String,String>(),con,entrada.p_IdEmpresa));  
			return out;
		}	
		
		
		// --------------------------------------------------------------------------------
		// PROCESAR EVENTO CAPITAL CORTE CUPON
		
		if (eventoCapital.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_FECHA_SIN_CORTES_DE_CUPON",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		
		if (eventoCapital.size() > 0)
		{
			log.write("PROCESANDO EVENTO CAPITAL CORTE CUPON > " + eventoCapital);
			MultiKeyMap mapEventoCapital = new MultiKeyMap();
			
			// --------------------------------------------------------------------------------
			// BUSCA LOS CLIENTES CON DERECHO A PAGO CUPON

			List<EventoCapitalCliente> eventoCapitalCliente = sql.query(con, SQL_SELECT_CARTERA_EVENTO_CAPITAL, paramsCliente, EventoCapitalCliente.class);
		
			//parametro en 'S' agrupa venpac/compac cta propia
			//parametro en 'N' no agrupa venpac/compac cta propia -> continua flujo normal
			if("S".equalsIgnoreCase(agrupaCCCtaPropia)) {
				eventoCapitalCliente = agruparCortesCuponPactadosCtaPropia(con, sql, eventoCapitalCliente);
			}
			
	
			List<Long> ecPorInstrumentos = new ArrayList<>();
			Date fechaPago = null;

			for (EventoCapital ecc : eventoCapital) {
			    if (ecc.IdInstrumento != null) {
			        ecPorInstrumentos.add(ecc.IdInstrumento);
			        if (fechaPago == null) {
			            fechaPago = ecc.FechaPago;
			        }
			    }
			}

			HashMap<String,Object> paramsNominal = new HashMap<String,Object>();
			paramsNominal.put("FechaCorteCuponDesde", entrada.p_FechaCorteCuponDesde);
	    	paramsNominal.put("FechaCorteCuponHasta", entrada.p_FechaCorteCuponHasta);
			paramsNominal.put("IdInstrumento", ecPorInstrumentos);
			paramsNominal.put("FechaPago", fechaPago);
     		List<OperacionesNominalCC> operacionesNominales = sql.query(con, SQL_SELECT_OPERACIONES_NOMINAL_CC, paramsNominal, OperacionesNominalCC.class);
 
     		List<EventoCapitalCliente> nuevasNominales = valorizarNominal(con, entrada, fechaSistema, eventoCapitalCliente,
					operacionesNominales);   
     	    eventoCapitalCliente.addAll(nuevasNominales);
     	    
     	    List<EventoCapitalCliente> garantias = sql.query(con,SQL_SELECT_GARANTIA_EVENTO_CAPITAL ,paramsCliente, EventoCapitalCliente.class);
     	  
     	    eventoCapitalCliente.addAll(garantias);
     	    
     	    List<EventoCapitalCliente> contGtias = sql.query(con,SQL_SELECT_CONTRAPARTE_GARANTIA_CC ,paramsCliente, EventoCapitalCliente.class);
     	    eventoCapitalCliente.addAll(contGtias);
     	 
     	  
			eventoCapitalCliente.addAll(sql.query(con, SQL_SELECT_CONTRAPARTE_BOVEDAS_EVENTO_CAPITAL, params, EventoCapitalCliente.class));
			params.put("p_NombreEmisor", pNombreEmisor);
		    
			eventoCapitalCliente.addAll(sql.query(con, SQL_SELECT_CONTRAPARTE_EVENTO_CAPITAL, params, EventoCapitalCliente.class));
	    	
			List<OperacionesNominalCC> contraparteNominales = sql.query(con,SQL_SELECT_CONTRAPARTE_NOMINAL_CC, paramsNominal, OperacionesNominalCC.class);
			List<EventoCapitalCliente> cntNominales = valorizarNominal(con, entrada, fechaSistema, eventoCapitalCliente,
					contraparteNominales);   
			eventoCapitalCliente.addAll(cntNominales);
	 
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			String idPaisEmpresa = empresa.IdPais.toString();
			
			List<String> listaFechaLiquidacion = new ArrayList<String>();
			List<String> listaFechaPagoReajuste = new ArrayList<String>();
			for (EventoCapitalCliente ec: eventoCapitalCliente) {
				if (!listaFechaLiquidacion.contains(ec.FechaLiquidacion.toString())) listaFechaLiquidacion.add(ec.FechaLiquidacion.toString());
				String fechaPagoYReajuste = ec.FechaPago.toString()+";"+ec.IdTipoReajuste;
				if (!listaFechaPagoReajuste.contains(fechaPagoYReajuste)) listaFechaPagoReajuste.add(fechaPagoYReajuste);
			}
			
			HashMap<String, String> diasDespuesFechaLiquidacion = new HashMap<String, String>();

			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = listaFechaLiquidacion
					.stream()
					.map(fechaLiq->{
						ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
						fecha.fechaInicial = fechaLiq;
						fecha.numeroDias = 0L;
						fecha.pais = Long.parseLong(idPaisEmpresa);
						return fecha;
					})
					.collect(Collectors.toList());
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
			
			for (FechasOut fechaSalida : outDiaSiguiente.p_out_fechas) {
				diasDespuesFechaLiquidacion.put(fechaSalida.fechaInicial, fechaSalida.fechaCalculada);
			}

            List<FechaMonedaCorteCupon> entradaFechaMonedaCC = new ArrayList<FechaMonedaCorteCupon>();
            for (String fpr: listaFechaPagoReajuste) {
            	FechaMonedaCorteCupon fmcc = new FechaMonedaCorteCupon();
            	fmcc.IdTipoReajuste = fpr.split(";")[1];
            	fmcc.FechaPago = fpr.split(";")[0];
            	entradaFechaMonedaCC.add(fmcc);
            }
            
            List<CalculoEntrada> calculosEntrada = new ArrayList<>();
			
			for (FechaMonedaCorteCupon fmcc: entradaFechaMonedaCC) {				
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.Fecha = fmcc.FechaPago;
				calculoEntrada.IdInterno = 0L;
				calculoEntrada.IdMonedaOrigen = fmcc.IdTipoReajuste;
				calculoEntrada.IdMonedaDestino = "CLP";
				calculoEntrada.IdTrabajo = 0L;
				calculoEntrada.IdUso = "";
				calculoEntrada.MontoOrigen = BigDecimal.ONE;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					
				calculosEntrada.add(calculoEntrada);
			}
			
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 0L;
			calcularParidadEntrada.p_CalculosEntrada = calculosEntrada;
			
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
			
			List<DetalleCalculo> resumen = new ArrayList<>();
			HashMap<String, SalidaConsultaPrecioReajuste> mapPreciosReajuste = new HashMap<String, SalidaConsultaPrecioReajuste>();
			for (CalculoSalida cs: calcularParidadSalida.p_out_CalculosSalida) {
				SalidaConsultaPrecioReajuste datoReajuste = new SalidaConsultaPrecioReajuste();
				datoReajuste.Llave = cs.Fecha.toString()+";"+cs.IdMonedaOrigen;
				datoReajuste.FechaPago = cs.Fecha;
				datoReajuste.FechaMoneda = cs.FechaParametroFinanciero;
				datoReajuste.IdTipoReajuste = cs.IdMonedaOrigen;
				datoReajuste.TipoCambioMercado = cs.FactorOrigenDestino;
				datoReajuste.EsFechaExacta = (cs.Fecha.toString().equalsIgnoreCase(cs.FechaParametroFinanciero.toString())) ? "S" : "N";
				datoReajuste.EsProyeccion = (!cs.Fecha.toString().equalsIgnoreCase(cs.FechaParametroFinanciero.toString()) && fechaSistema.compareToIgnoreCase(cs.Fecha.toString()) < 0) ? "S" : "N";
				mapPreciosReajuste.put(datoReajuste.Llave, datoReajuste);
		
				LocalDate fechaCorte = LocalDate.parse(entrada.p_FechaCorteCuponDesde);
				boolean existeEventoValido = true;

			    for (EventoCapitalCliente ec : eventoCapitalCliente) {

			        if ("NOMINAL".equalsIgnoreCase(ec.QueryOrigen)) {
			            if (!ec.FechaPago.toLocalDate().equals(fechaCorte)) {
			            	existeEventoValido = false;
			                continue;
			            }
			        }

			    }

			    if (!existeEventoValido) {
			        continue;
			    }
				   DetalleCalculo det = new DetalleCalculo();
				   det.RequiereConfirmacion = false;
				   
				    det.IdMonedaOrigen = cs.IdMonedaOrigen;
				    det.FechaPago = cs.Fecha.toString();
				    det.FechaValor = cs.FechaParametroFinanciero.toString();
				    det.TipoCambio = cs.FactorOrigenDestino;
				    if(!cs.Fecha.equals(cs.FechaParametroFinanciero)) {
				        det.RequiereConfirmacion = true;
				    }

				    resumen.add(det);
				} 	      
			out.P_OUT_RESUMEN_CALCULO = resumen;	
			if ("N".equals(entrada.p_Confirmado)){
			        log.write("SOLO RESUMEN - ESPERANDO CONFIRMACION");
			        return out;
			    }


			
			List<EventoCapitalCliente> eccTemp = new ArrayList<EventoCapitalCliente>();
			LocalDate fechaCorte = LocalDate.parse(entrada.p_FechaCorteCuponDesde);
			for (EventoCapitalCliente ec: eventoCapitalCliente) {
				if ("NOMINAL".equalsIgnoreCase(ec.QueryOrigen)) {
				    if (!ec.FechaPago.toLocalDate().equals(fechaCorte)){
				        continue; 
				    }
				}
				String siguienteDiaHabil = diasDespuesFechaLiquidacion.get(ec.FechaLiquidacion.toString());
				int diaSiguienteHabil = siguienteDiaHabil != null? Integer.parseInt(siguienteDiaHabil.replace("-","")): null;
				int diaCorteCupon = Integer.parseInt(ec.INGFechaCorteCupon.toString().replace("-",""));				
				if (ec.IdTipoOperacion == null ||  (!ec.IdTipoOperacion.equals("COMRF") && !ec.INGIdTipoOperacion.equals("INGTIT")) || siguienteDiaHabil == null || diaSiguienteHabil <= diaCorteCupon) {
					eccTemp.add(ec);
				}
			}
			eventoCapitalCliente.clear();
			eventoCapitalCliente.addAll(eccTemp);	
			
			
			HashMap<String, EventoCapitalCliente> hashFiltroDuplicados = new HashMap<String, EventoCapitalCliente>();
			for (EventoCapitalCliente ec: eventoCapitalCliente) {
				String llave = ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + ec.QueryOrigen;
				String llaveCartera = ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + "CARTERA";
				String llaveCarteraHistorica = ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + "CARTERA_HISTORICA";
				String llaveNominal = ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + "NOMINAL";
				String llaveNominalHistorica = ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + "NOMINAL_HISTORICA";
				String llaveGarantia= ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + "GARANTIA";
				String llaveGarantiaHistorica = ec.IdCartera.toString() + ec.IdTipoOperacion + ec.CantidadDerecho.toString() + "GARANTIA_HISTORICA";
				if (ec.QueryOrigen.equals("CARTERA_HISTORICA") && hashFiltroDuplicados.get(llaveCartera) != null) {
					EventoCapitalCliente ecCartera = hashFiltroDuplicados.get(llaveCartera);
					ec.UsaProximoFactorValorMoneda = ecCartera.UsaProximoFactorValorMoneda;
					hashFiltroDuplicados.remove(llaveCartera);
					hashFiltroDuplicados.put(llave, ec);
				}else if (ec.QueryOrigen.equals("NOMINAL_HISTORICA") && hashFiltroDuplicados.get(llaveNominal) != null) {
					hashFiltroDuplicados.remove(llaveNominal);
					hashFiltroDuplicados.put(llave, ec);
				}else if (ec.QueryOrigen.equals("GARANTIA_HISTORICA") && hashFiltroDuplicados.get(llaveGarantia) != null) {
					hashFiltroDuplicados.remove(llaveGarantia);
					hashFiltroDuplicados.put(llave, ec);
				}else if ((!ec.QueryOrigen.equals("CARTERA") || hashFiltroDuplicados.get(llaveCarteraHistorica) == null) &&
							((!ec.QueryOrigen.equals("GARANTIA") || hashFiltroDuplicados.get(llaveGarantiaHistorica) == null))) {					
					hashFiltroDuplicados.put(llave, ec);
				}
			}
			eventoCapitalCliente.clear();
			eventoCapitalCliente.addAll(hashFiltroDuplicados.values());

		
			HashMap<String, ParametroMensajeProyeccionCC> mapMensajesProyeccion = new HashMap<String, ParametroMensajeProyeccionCC>();
			
			for (EventoCapitalCliente ec: eventoCapitalCliente) {
				String llave = ec.FechaPago.toString()+";"+ec.IdTipoReajuste;
				SalidaConsultaPrecioReajuste datosReajuste = mapPreciosReajuste.get(llave);
				BigDecimal precio = datosReajuste.TipoCambioMercado;
				String esFechaExacta = datosReajuste.EsFechaExacta;
				String esProyeccion = datosReajuste.EsProyeccion;
				
				if (esProyeccion.equalsIgnoreCase("S") && mapMensajesProyeccion.get(ec.Nemo) == null) {
					ParametroMensajeProyeccionCC paramMensaje = new ParametroMensajeProyeccionCC();
					paramMensaje.Nemo = ec.Nemo;
					paramMensaje.FechaLimite = ec.INGFechaCorteCupon;
					mapMensajesProyeccion.put(ec.Nemo, paramMensaje);
				}
				
				if (precio != null && (esFechaExacta.equalsIgnoreCase("S") || esProyeccion.equalsIgnoreCase("S"))) {
					if (ec.UsaProximoFactorValorMoneda.equalsIgnoreCase("S") && !precio.equals(ec.INGValorFactorMonedaInstrumentoProximo) && !ec.INGValorFactorMonedaInstrumentoProximo.equals(new BigDecimal(0))) {
						ec.MontoCapital = (ec.MontoCapital.divide(ec.INGValorFactorMonedaInstrumentoProximo,50, RoundingMode.HALF_UP)).multiply(precio).setScale(0, RoundingMode.HALF_UP);
						ec.MontoInteres = (ec.MontoInteres.divide(ec.INGValorFactorMonedaInstrumentoProximo,50, RoundingMode.HALF_UP)).multiply(precio).setScale(0, RoundingMode.HALF_UP);
						ec.Total = (ec.Total.divide(ec.INGValorFactorMonedaInstrumentoProximo,50, RoundingMode.HALF_UP)).multiply(precio).setScale(0, RoundingMode.HALF_UP);
					}
					else if (ec.UsaProximoFactorValorMoneda.equalsIgnoreCase("N") && !precio.equals(ec.INGValorFactorMonedaInstrumentoActual) && !ec.INGValorFactorMonedaInstrumentoActual.equals(new BigDecimal(0))){
						ec.MontoCapital = (ec.MontoCapital.divide(ec.INGValorFactorMonedaInstrumentoActual,50, RoundingMode.HALF_UP)).multiply(precio).setScale(0, RoundingMode.HALF_UP);
						ec.MontoInteres = (ec.MontoInteres.divide(ec.INGValorFactorMonedaInstrumentoActual,50, RoundingMode.HALF_UP)).multiply(precio).setScale(0, RoundingMode.HALF_UP);
						ec.Total = (ec.Total.divide(ec.INGValorFactorMonedaInstrumentoActual,50, RoundingMode.HALF_UP)).multiply(precio).setScale(0, RoundingMode.HALF_UP);
					}
				}
				ec.MontoCapital = ec.MontoCapital.setScale(0, RoundingMode.HALF_UP);
				ec.MontoInteres = ec.MontoInteres.setScale(0, RoundingMode.HALF_UP);
				ec.Total = ec.Total.setScale(0, RoundingMode.HALF_UP);
			}
			
			// --------------------------------------------------------------------------------
			// CALCULAR FECHA SIGUIENTE A LA FECHA DE PAGO CUPON 
			/************
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			
			List<Date> fechas = new ArrayList<Date>();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			for(EventoCapital e : eventoCapital){
				if(!fechas.contains(e.INGFechaPagoCorteCupon)){
					InObtenerDiaSiguiente.FechasIn fecha = new InObtenerDiaSiguiente.FechasIn();
					fecha.fechaInicial = Fecha.stringToSqlDate(e.INGFechaPagoCorteCupon);
					fecha.numeroDias = 0L; ////////////// ????????????????? FALTA REVISAR ???????????????
					inObtenerDiaSiguiente.p_fechas.add(fecha);
					fechas.add(e.INGFechaPagoCorteCupon);
				}
			}
			
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
			
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
			
			HashMap<String, Object> fechasCalculadas = Utils.List2Hash(outObtenerDiaSiguiente.p_out_fechas, "fechaInicial");
			 ************/
			
			// --------------------------------------------------------------------------------
			// CONSULTA VENTAS Y COMPRAS A TERMINO CON F.LIQ. POSTERIOR A LA DE CC Y REALIZA DEVOLUCION A CARTERA DE ORIGEN POR DERECHO A CC
						
			HashMap<String,Object> paramsMesa = new HashMap<String,Object>();
			List<EventoCapitalCliente> listaEvDevoluciones = new ArrayList<EventoCapitalCliente>(); 
						
			paramsMesa.put("FechaCorteCuponDesde", entrada.p_FechaCorteCuponDesde);
			paramsMesa.put("FechaCorteCuponHasta", entrada.p_FechaCorteCuponHasta);
			paramsMesa.put("IdInstrumento", entrada.p_IdInstrumento);
						
			List<OperacionesATerminoMesa> operacionesATermino = sql.query(con, SQL_CONSULTA_OPERACIONES_MESA, paramsMesa, OperacionesATerminoMesa.class);
			
			// SI EL "IdInstrumento" ES [] LIMPIAMOS LAS OPERACIONES A TERMINO QUE NO TENGAN RELACION CON LAS CARTERAS DE eventoCapitalCliente:
			if(entrada.p_IdInstrumento.isEmpty()) {
				Iterator<OperacionesATerminoMesa> iterator = operacionesATermino.iterator();
				while (iterator.hasNext()) {
					OperacionesATerminoMesa op = iterator.next();
					boolean existeRelacion = eventoCapitalCliente.stream()
							.anyMatch(e -> e.IdInstrumento.equals(op.IdInstrumento));
					if (!existeRelacion) {
						iterator.remove();
					}
				}
			}			
			
			List<DevolucionDerecho> listaDevolucion = new ArrayList<DevolucionDerecho>();
						
			if(!operacionesATermino.isEmpty()) {
				
				// SE IDENTIFICAN LAS CARTERAS EXISTENTES, CANTIDADES A RESTAR Y DEVOLVER:
				for(OperacionesATerminoMesa op : operacionesATermino) {					
					// LE RESTA:
					DevolucionDerecho devolucionResta = new DevolucionDerecho();
					devolucionResta.IdCliente = op.IdCliente;
					devolucionResta.IdPortafolio = op.IdPortafolio;
					devolucionResta.Cantidad = op.Cantidad;
					devolucionResta.EsResta = "S";
					devolucionResta.ExisteCartera = eventoCapitalCliente.stream().anyMatch(e -> e.IdPortafolio.equals(op.IdPortafolio));
					listaDevolucion.add(devolucionResta);
								
					// LE SUMA:
					DevolucionDerecho devolucionSuma = new DevolucionDerecho();
					devolucionSuma.IdCliente = op.IdClienteRelacionado;
					devolucionSuma.IdPortafolio = op.IdPortafolioRelacionado;
					devolucionSuma.Cantidad = op.Cantidad;
					devolucionSuma.EsResta = "N";
					devolucionSuma.ExisteCartera = eventoCapitalCliente.stream().anyMatch(e -> e.IdPortafolio.equals(op.IdPortafolioRelacionado));
					listaDevolucion.add(devolucionSuma);
				}
				
				// REALIZA DEVOLUCIONES A LAS RESPECTIVAS CARTERAS Y CREA LAS QUE NO EXISTAN:
				for(DevolucionDerecho devolucion : listaDevolucion) {
								
					if(devolucion.ExisteCartera && devolucion.EsResta.equalsIgnoreCase("S")) {						
						EventoCapitalCliente evCaCli = eventoCapitalCliente.stream()
								.filter(e -> e.IdPortafolio.equals(devolucion.IdPortafolio))
								.findFirst()
								.map(EventoCapitalCliente::new)
								.orElse(null);
						
						evCaCli.CantidadDerecho = devolucion.Cantidad.negate();
						listaEvDevoluciones.add(evCaCli);
					} 
					if(devolucion.ExisteCartera && devolucion.EsResta.equalsIgnoreCase("N")) {						
						EventoCapitalCliente evCaCli = eventoCapitalCliente.stream()
								.filter(e -> e.IdPortafolio.equals(devolucion.IdPortafolio))
								.findFirst()
								.map(EventoCapitalCliente::new)
								.orElse(null);
						
						evCaCli.CantidadDerecho = devolucion.Cantidad;
						listaEvDevoluciones.add(evCaCli);
					}
					if(!devolucion.ExisteCartera && devolucion.EsResta.equalsIgnoreCase("N")) {
						
						HashMap<String,Object> paramsCarteraOrigen = new HashMap<String,Object>();
						
						paramsCarteraOrigen.put("IdPortafolio", devolucion.IdPortafolio);
						paramsCarteraOrigen.put("IdCliente", devolucion.IdCliente);
						
						List<DatosClienteDerechoOrigen> datosClienteDO = sql.query(con, SQL_SELECT_DATOS_CLIENTE_ORIGEN, paramsCarteraOrigen, DatosClienteDerechoOrigen.class);
						DatosClienteDerechoOrigen datosNuevos = new DatosClienteDerechoOrigen();
						
						datosNuevos = datosClienteDO.get(0);
						datosNuevos.IdPortafolio = devolucion.IdPortafolio;
						
						for(OperacionesATerminoMesa op : operacionesATermino) {
							if(op.IdPortafolioRelacionado.equals(devolucion.IdPortafolio)) {
								Long idPortafolioRelacionado = op.IdPortafolio;
								
								EventoCapitalCliente evCaCli = eventoCapitalCliente.stream()
										.filter(e -> e.IdPortafolio.equals(idPortafolioRelacionado))
										.findFirst().map(EventoCapitalCliente::new)
										.orElse(null);
								
								evCaCli.IdPortafolio = datosNuevos.IdPortafolio;
								evCaCli.DocumentoIdentidad = datosNuevos.DocumentoIdentidad;
								evCaCli.NombreAgente = datosNuevos.NombreAgente;
								evCaCli.NombreCliente = datosNuevos.NombreCliente;
								evCaCli.IdSocioNegocio = datosNuevos.IdSocioNegocio;
								evCaCli.IdEmpleadoEjecutivoPortafolio = datosNuevos.IdEmpleadoEjecutivoPortafolio;
								evCaCli.CodigoUsuarioAgente = datosNuevos.CodigoUsuarioAgente;
								evCaCli.CodigoSucursal = datosNuevos.CodigoSucursal;
								evCaCli.CantidadDerecho = devolucion.Cantidad;
								evCaCli.IdTipoOperacion = "INGTIT";							
								
								listaEvDevoluciones.add(evCaCli);
							}
						}
					}	
				}	
			}
			
			eventoCapitalCliente.addAll(listaEvDevoluciones); // A�ADE LAS DEVOLUCIONES A LA LISTA DE CLIENTES CON DERECHO
			
			// --------------------------------------------------------------------------------
			// CALCULA LAS SECUENCIAS DE LAS CABECERAS DE LOS NUEVOS EVENTOS CAPITAL 
			
			Map<Long, Long> secuenciaPorInstrumento = new HashMap<>();
			for (EventoCapitalCliente ecc : eventoCapitalCliente) {
			    if (ecc.Secuencia != null && ecc.Secuencia != 0) {
			        secuenciaPorInstrumento.put(ecc.IdInstrumento, ecc.Secuencia);
			    }
			}

			Map<Long, EventoCapitalCliente> nominalPorInstrumento = new HashMap<>();
			for (EventoCapitalCliente ecc : eventoCapitalCliente) {
			    if (ecc.Secuencia != null && ecc.Secuencia != 0 && ("NOMINAL".equalsIgnoreCase(ecc.QueryOrigen))) {
			    	nominalPorInstrumento.put(ecc.IdInstrumento, ecc);
			    }
			}
			
			List<Long> secuenciasEC = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapital", new Long(eventoCapital.size()));
			int indexEC = 0;
			List<EventoCapital> ecPaso = new ArrayList<EventoCapital>();
			
			for(EventoCapital ec : eventoCapital)
			{
				///OutObtenerDiaSiguiente.FechasOut fechaCalculada = (OutObtenerDiaSiguiente.FechasOut) fechasCalculadas.get(Fecha.stringToSqlDate(ec.INGFechaPagoCorteCupon)); 
				///ec.FechaPago = fechaCalculada.fechaCalculada; // ???
				ec.IdEmpresa = entrada.p_IdEmpresa;
				ec.IdUsuario = entrada.p_IdUsuario;
				ec.IdEventoCapital = secuenciasEC.get(indexEC);
				ec.IdEstado = "PRO";
				indexEC++;
				EventoCapitalCliente nominal = nominalPorInstrumento.get(ec.IdInstrumento); 
				if (ec.Secuencia == 0 && nominal == null) {
			        continue;
			    }

				if (nominal != null && (!nominal.FechaPago.equals(ec.FechaPago))) {	      
					continue;
		        }
				
				if (nominal != null) {
				        ec.Secuencia = nominal.Secuencia;
				        ec.FechaPago = nominal.FechaPago;
				}
				mapEventoCapital.put(ec.Secuencia, ec.IdTipoInstrumento, ec.IdInstrumento, ec.IdMoneda,  ec.FechaPago, ec); // Crea un mapa con llaves multiples de EC cabecera
				ecPaso.add(ec);
			}
			eventoCapital.clear();
			eventoCapital.addAll(ecPaso);	
			if (eventoCapital.size() == 0  && cambioNemo.size() == 0)
			{
				out.P_OUT_ERRORESCONTADOR += 1;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_FECHA_SIN_CORTES_DE_CUPON",new HashMap<String,String>(),con,entrada.p_IdEmpresa));  
				return out;
			}
			
			// --------------------------------------------------------------------------------
			// OBTIENE LAS SECUENCIAS DE LOS NUEVOS ECC (Clientes con derecho)
			List<Long> secuenciasECC = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(eventoCapitalCliente.size()));
			int indexECC = 0;
			
			List<Long> portafolios = new ArrayList<Long>();
			System.out.println(eventoCapitalCliente);
			
			for(EventoCapitalCliente ecc : eventoCapitalCliente)
			{
				if(ecc.Secuencia==null || ecc.IdTipoInstrumento==null || ecc.IdInstrumento==null || ecc.IdMoneda==null || ecc.FechaPago==null)
				{
				out.P_OUT_ERRORESCONTADOR += 1;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EN_DEFINICION_DE_CARTERA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));  
				return out;
				}
				else
				{
				EventoCapital infoEventoCapital = (EventoCapital) mapEventoCapital.get(ecc.Secuencia, ecc.IdTipoInstrumento, ecc.IdInstrumento, ecc.IdMoneda,  ecc.FechaPago);
			
					if(infoEventoCapital!= null ) {
						ecc.IdEventoCapital = infoEventoCapital.IdEventoCapital;
						ecc.IdTipoEventoCapital = infoEventoCapital.IdTipoEventoCapital;
						ecc.FechaPago = infoEventoCapital.FechaPago;
						ecc.IdEstado = "PRO";
						ecc.IdUsuario = entrada.p_IdUsuario;
						ecc.IdEventoCapitalCliente = secuenciasECC.get(indexECC);
						if(!portafolios.contains(ecc.IdPortafolio)) portafolios.add(ecc.IdPortafolio);
					}
				indexECC++;
				}
			}
			

			
			
			// --------------------------------------------------------------------------------
			// OBTIENE LAS PREFERENCIA DE PAGO(FORMA DE PAGO) PARA CORTE CUPON 
			
			
			List<Long> secuenciasEFP = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago", new Long(eventoCapitalCliente.size()));		
			int indexEFP = 0;
			
			params.clear();
			params.put("IdPortafolio", portafolios);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<EventoCapitalClienteFormaDePago> formaspago = sql.query(con, SQL_SELECT_FORMA_PAGO_EVENTO_CAPITAL, params, EventoCapitalClienteFormaDePago.class);
			HashMap mapFormaspago = Utils.List2Hash(formaspago, "IdPortafolio");
			
			
			List<EventoCapitalClienteFormaDePago> insertFormasPago = new ArrayList<EventoCapitalClienteFormaDePago>();
			
			for(EventoCapitalCliente ecc : eventoCapitalCliente)
			{	
				if(ecc.IdEventoCapital != null && ecc.IdEventoCapitalCliente != null) {
					EventoCapitalClienteFormaDePago efp = new EventoCapitalClienteFormaDePago();
					EventoCapitalClienteFormaDePago infoFormaDePago = (EventoCapitalClienteFormaDePago) mapFormaspago.get(ecc.IdPortafolio);
					if (infoFormaDePago == null) continue;
					efp = Utils.copyPojo(infoFormaDePago, efp);
					
					if(efp.IdTipoMovimiento != null)
					{
						efp.IdUsuarioIngreso = entrada.p_IdUsuario;
						efp.IdEventoCapitalCliente =  ecc.IdEventoCapitalCliente;
						efp.IdEventoCapitalClienteFormaDePago = secuenciasEFP.get(indexEFP);
						efp.Sucursal = ecc.CodigoSucursal;
						efp.FechaVctoDocumento = ecc.FechaPago;
						efp.Monto = ecc.Total;
						insertFormasPago.add(efp);
						indexEFP++;
						
					}
				}
				
			}
			
			
			// --------------------------------------------------------------------------------
			// COMBINA Y ELIMINA REGISTROS DE CLIENTES QUE PRESENTEN GARANTIAS Y CUSTODIAS POR SEPARADOS DEL CORTE CUPON 
			
			HashMap<String, EventoCapitalCliente> hashDocumentoIdentidad = new HashMap<String, EventoCapitalCliente>();
			List<EventoCapitalCliente> eventoCapitalClienteBorrar =  new ArrayList<>();
			for(EventoCapitalCliente clienteBase : eventoCapitalCliente) {
				if(hashDocumentoIdentidad.containsKey(clienteBase.DocumentoIdentidad + clienteBase.IdPortafolio + clienteBase.IdInstrumento + clienteBase.TipoSocioNegocio + clienteBase.TipoCartera) == true ) {	
					EventoCapitalCliente evCapiCli = (EventoCapitalCliente) hashDocumentoIdentidad.get(clienteBase.DocumentoIdentidad + clienteBase.IdPortafolio + clienteBase.IdInstrumento + clienteBase.TipoSocioNegocio + clienteBase.TipoCartera);
					

						evCapiCli.CantidadDerecho = evCapiCli.CantidadDerecho.add(clienteBase.CantidadDerecho);
						evCapiCli.Total			= evCapiCli.Total.add(clienteBase.Total);
						evCapiCli.MontoInteres 	= evCapiCli.MontoInteres.add(clienteBase.MontoInteres);
						evCapiCli.MontoCapital 	= evCapiCli.MontoCapital.add(clienteBase.MontoCapital);
						
						eventoCapitalClienteBorrar.add(clienteBase);

				}else {
					hashDocumentoIdentidad.put(clienteBase.DocumentoIdentidad + clienteBase.IdPortafolio + clienteBase.IdInstrumento + clienteBase.TipoSocioNegocio + clienteBase.TipoCartera , clienteBase);
				}
			}
			eventoCapitalCliente.clear();
			for (Map.Entry<String, EventoCapitalCliente> registros : hashDocumentoIdentidad.entrySet()) {
				EventoCapitalCliente ecc = registros.getValue();
				if (ecc.IdEventoCapitalCliente == null || ecc.IdEventoCapital==null ) {
			        continue; 
			    }
				
				eventoCapitalCliente.add(ecc);
			}
		
			// --------------------------------------------------------------------------------
			// CREA EC CC,LOS CLIENTES CON DERECHO (ecc) Y FORMA DE PAGO 
			
			sql.batch(con, SQL_INSERT_CABECERA_EVENTO_CAPITAL, eventoCapital, EventoCapital.class);
			sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, eventoCapitalCliente, EventoCapitalCliente.class);
			sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_FORMA_PAGO, insertFormasPago, EventoCapitalClienteFormaDePago.class);
			
			BigDecimal capitalSum=new BigDecimal(0);
			BigDecimal interesSum=new BigDecimal(0);
			for(EventoCapitalCliente ecc : eventoCapitalCliente)
			{
				capitalSum=capitalSum.add(ecc.MontoCapital);
				interesSum=interesSum.add(ecc.MontoInteres);
				
			}
			if(capitalSum.compareTo(BigDecimal.ZERO) == 0 && interesSum.compareTo(BigDecimal.ZERO) == 0)
			{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CORTE_CUPON_PROCESAR_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}else{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CORTE_CUPON_PROCESAR_EXITO_VALIDAR_CAPITAL_INTERES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));	
			}
			
			// SE AGREGAN MENSAJES PARA LOS CORTES DE CUPON PROYECTADOS
			for (EventoCapital ec: eventoCapital) {
				ParametroMensajeProyeccionCC parametrosMsjProyeccion = mapMensajesProyeccion.get(ec.Nemo);
				if (parametrosMsjProyeccion != null) {
					HashMap<String,String> mapParamMensaje = new HashMap<String, String>();
					mapParamMensaje.put("Nemo",parametrosMsjProyeccion.Nemo);
					mapParamMensaje.put("FechaLimite",parametrosMsjProyeccion.FechaLimite.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(ec.IdEventoCapital, MensajeSalida.MENSAJE_OK, "UI_PROYECCION_CORTE_CUPON", mapParamMensaje, con, entrada.p_IdEmpresa));
				}
			}
		} //FIN PROCESAR EVENTO CAPITAL CORTE CUPON
				
				
				
		
		
		
		// --------------------------------------------------------------------------------
		// PROCESAR CAMBIO DE NEMO
		

		if (cambioNemo.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_FECHA_SIN_CAMBIO_DE_NEMO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		if (cambioNemo.size() > 0)
		{	
			log.write("PROCESANDO CAMBIO DE NEMOS > " + cambioNemo);
			ProcesarEventoCapitalCambioDeNemos servicioCambioDeNemo = new ProcesarEventoCapitalCambioDeNemos();
			ProcesarEventoCapitalCambioDeNemosEntrada2 entradaCambioDeNemo = new ProcesarEventoCapitalCambioDeNemosEntrada2();
			
			
			entradaCambioDeNemo.p_IdEmpresa = entrada.p_IdEmpresa; 
			entradaCambioDeNemo.p_IdUsuario = entrada.p_IdUsuario;
			entradaCambioDeNemo.p_IdModulo = entrada.p_IdModulo;
			entradaCambioDeNemo.p_Origen = "AUT";    // MAN / AUT
			
			for (EventoCapital camNem: cambioNemo)
			{
				InstrumentoNemo entradaInstrumentoNemo = new InstrumentoNemo();
				entradaInstrumentoNemo.Nemotecnico = camNem.Nemo;
				entradaInstrumentoNemo.NemotecnicoNuevo = camNem.NemoNuevo;
				
				entradaCambioDeNemo.p_Instrumento.add(entradaInstrumentoNemo);
			}
			
			ProcesarEventoCapitalCambioDeNemosSalida2 salidaCambioDeNemo = servicioCambioDeNemo.logicaCambioNemo(con, entradaCambioDeNemo);
						
			if (MensajeSalida.getNumeroErrores(salidaCambioDeNemo.P_OUT_MENSAJES) > 0)
			{
				out.P_OUT_MENSAJES.addAll(salidaCambioDeNemo.P_OUT_MENSAJES);
				return out;
			}
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CAMBIO_NEMO_PROCESAR_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			
		}
		
		
		
		
		log.write("PARAMETROS DE SALIDA > " + out);
		log.write("FIN:: ProcesarEventoCapitalCorteCupon");
		return out;
	}



	private List<EventoCapitalCliente> valorizarNominal(Connection con, EntradaProcesarEventoCapitalCorteCupon entrada,
			String fechaSistema, List<EventoCapitalCliente> eventoCapitalCliente,
			List<OperacionesNominalCC> operacionesNominales) throws Exception {
		log.write("INICIO:: valorizarNominal");
		long inicioMetodo = System.currentTimeMillis();
		Map<Long, List<OperacionesNominalCC>> operacionesPorInstrumento = new HashMap<>();
		for (OperacionesNominalCC op : operacionesNominales) {
		    operacionesPorInstrumento
		        .computeIfAbsent(op.IdInstrumento, k -> new ArrayList<>())
		        .add(op);
		}
		
		ValorizarInstrumentoEntrada entradaValorizar = new ValorizarInstrumentoEntrada();
		entradaValorizar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValorizar.p_IdUsuario = entrada.p_IdUsuario;
		entradaValorizar.p_IdModulo = entrada.p_IdModulo;
		entradaValorizar.p_IdPais = -1L;
		entradaValorizar.p_OrigenProcesamiento = "CUSTODIA_UI";
		
		List<ObjEntradaInstrumentos> listaInstrumentos = new ArrayList<>();

		for (OperacionesNominalCC op : operacionesNominales) {
		    ObjEntradaInstrumentos instr = new ObjEntradaInstrumentos(); {
		
		    instr.IdInstrumento = op.IdInstrumento;
		    instr.IdTipoInstrumento = op.IdTipoInstrumento;
		    instr.IdClasificacionTipoInstrumento = op.IdClasificacionTipoInstrumento;
		    instr.Nemotecnico = op.Nemotecnico;
		    instr.IdPortafolio=op.IdPortafolio;
		    instr.IdMonedaInstrumento = op.IdMonedaInstrumento;
		    instr.Cantidad = op.Cantidad;
		    instr.TipoCartera = op.TipoCartera;
		    instr.FechaValorizacion=fechaSistema;
		    instr.FechaOperacion= new SimpleDateFormat("yyyy-MM-dd").format(op.FechaOperacion);
		    instr.IdValorizacion=  1L;
		    instr.PrecioCompra =op.PrecioCompra;
		    instr.IdMonedaEmisionInstrumento=op.IdMonedaEmisionInstrumento;
		    instr.Emisor=op.Emisor;
		    instr.FechaEmision = new SimpleDateFormat("yyyy-MM-dd").format(op.FechaEmision);
		    instr.FechaVencimiento= new SimpleDateFormat("yyyy-MM-dd").format(op.FechaVencimiento);
		    instr.IdTipoReajusteInstrumento=op.IdTipoReajusteInstrumento;
		    instr.IdCondicionDeLiquidacion= op.IdCondicionDeLiquidacion;
		    instr.FechaDeLiquidacion=new SimpleDateFormat("yyyy-MM-dd").format(op.FechaLiquidacion);
		    
		    listaInstrumentos.add(instr);
		}
		    }
		entradaValorizar.p_Instrumentos = listaInstrumentos;
		
  
		ValorizarInstrumento logicaValorizar = new ValorizarInstrumento();
		ValorizarInstrumentoSalida salidaValorizar = logicaValorizar.logica(con, entradaValorizar);	
   		  
		List<EventoCapitalCliente> nuevasNominales = new ArrayList<>();

   
   	Map<String, EventoCapitalCliente> ecPorInstrumentoCliente = new HashMap<>();
   	for (EventoCapitalCliente ec : eventoCapitalCliente) {
		String clave = ec.IdInstrumento + "-" + ec.IdPortafolio;
		ecPorInstrumentoCliente.put(clave, ec);
   	}


   	for (OperacionesNominalCC op : operacionesNominales) {
   		String clave = op.IdInstrumento +"-" + op.IdPortafolio;

		Long cartera = op.IdInstrumento + op.IdPortafolio+op.IdCliente;

   	   EventoCapitalCliente nuevo = ecPorInstrumentoCliente.get(clave);

   	 
		    nuevo = new EventoCapitalCliente();
		    nuevo.IdInstrumento = op.IdInstrumento;
		    nuevo.IdTipoOperacion=op.IdTipoOperacion;
		    nuevo.EsCarteraNegativa=op.EsCarteraNegativa;
		    nuevo.TipoSocioNegocio = op.TipoSocioNegocio;
		    nuevo.IdSocioNegocio=op.IdSocioDeNegocio;
		    nuevo.IdConceptoGarantia=op.IdConceptoGarantia;
		    nuevo.IdTipoOperacionGarantia=op.IdTipoOperacionGarantia;
		    nuevo.IdInstrumentoNuevo=op.IdInstrumentoNuevo;
		    nuevo.NemoNuevo=op.NemoNuevo;
		    nuevo.FechaHoraIngresoRegistro=op.FechaHoraIngresoRegistro;
		    nuevo.IdPortafolio = op.IdPortafolio;
		    nuevo.IdTipoDocumentoIdentidad = op.IdTipoDocumentoIdentidad;
		    nuevo.IdTipoEventoCapital = op.IdTipoEventoCapital;
		    nuevo.TipoCartera = op.TipoCartera;
		    nuevo.DocumentoIdentidad = op.DocumentoIdentidad;
		    nuevo.FechaHoraUltimaActualizacion=op.FechaHoraIngresoRegistro;
		    nuevo.NombreCliente = op.NombreCliente;
		    nuevo.IdTipoDocumentoIdentidad = op.IdTipoDocumentoIdentidad;
		    nuevo.IdTipoInstrumento = op.IdTipoInstrumento;
		    nuevo.IdEmpleadoEjecutivoPortafolio=op.IdEmpleadoEjecutivoPortafolio;
		    nuevo.NombreAgente=op.NombreAgente;
		    nuevo.CodigoSucursal=op.CodigoSucursal;
		    nuevo.Nemo = op.Nemotecnico;
		    nuevo.IdMoneda = op.IdMonedaInstrumento;
		    nuevo.CodigoUsuarioAgente= op.CodigoUsuarioAgente;
		    nuevo.IdCartera = cartera;
		    nuevo.IdClasificacionTipoInstrumento=op.IdClasificacionTipoInstrumento;
		    nuevo.FechaLiquidacion = op.FechaLiquidacion;
		    nuevo.CantidadDerecho = op.Cantidad;
		    nuevo.CantidadNueva=op.CantidadNueva;
		    nuevo.IdTipoOperacion = op.IdTipoOperacion;
		    nuevo.IdBoveda= op.IdBoveda;
		    nuevo.IdBolsa= op.IdBolsa;
		    nuevo.Origen=op.OrigenCustodia;
		    nuevo.INGIdTipoOperacion = op.IdTipoOperacion;
		    nuevo.IdEstado = op.IdEstado;
		    nuevo.QueryOrigen = op.QueryOrigen != null ? op.QueryOrigen : "NOMINAL";
		    
		   for (ObjResultadoValorizacion resultVal : salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS){
			  if (resultVal.IdInstrumento.equals(nuevo.IdInstrumento)&& resultVal.Cantidad.compareTo(nuevo.CantidadDerecho) ==0 ) {
		            nuevo.FechaCierre = resultVal.FechaCorteCupon;
		            nuevo.FechaPago = resultVal.FechaPagoCorteCupon;
		            nuevo.MontoCapital= resultVal.MontoCapitalCorteCuponMonedaInstrumento;
		            nuevo.MontoInteres= resultVal.MontoInteresCorteCuponMonedaInstrumento;
		            nuevo.Total= resultVal.MontoCorteCuponMonedaInstrumento;
		            nuevo.INGFechaCorteCupon = resultVal.FechaCorteCupon;
		            nuevo.Secuencia= resultVal.NumeroCupon;
		            nuevo.SecuenciaCupon= resultVal.NumeroCupon;
		            nuevo.INGMontoInteresCorteCupon= resultVal.MontoInteresCorteCuponMonedaInstrumento;
		            nuevo.UsaProximoFactorValorMoneda = "N";
		            nuevo.INGValorFactorMonedaInstrumentoActual= resultVal.FactorMonedaInstrumento;
		            nuevo.INGValorFactorMonedaInstrumentoProximo=resultVal.FactorMonedaInstrumento;
		            nuevo.INGFechaFactorMonedaAdicionalProximo=resultVal.FechaFactorMonedaAdicional;
		            nuevo.MontoReajuste=resultVal.MontoReajusteMonedaInstrumento;
		            nuevo.IdTipoReajuste=resultVal.IdTipoReajuste;
		       
		            
		          
		          
		            break;
		

		   }
		   }    
		       

		nuevasNominales.add(nuevo);
		long finMetodo=System.currentTimeMillis();
		log.write("FIN:: valorizarNominal"+(finMetodo-inicioMetodo));
   	}
		return nuevasNominales;
		
	}



	private List<EventoCapitalCliente> agruparCortesCuponPactadosCtaPropia(Connection con, SQL sql,
			List<EventoCapitalCliente> eventoCapitalCliente) throws SQLException {
		// --------------------------------------------------------------------------------
		// BUSCA LOS CLIENTE/PORTAFOLIO CUENTA PROPIA

		List<ClientePortafolioCtaPropia> ClientePortafolioCtaPropia = sql.query(con, SQL_SELECT_CTA_PROPIA_PARA_CC, new HashMap<String,Object>() , ClientePortafolioCtaPropia.class);
		
		
		List<EventoCapitalCliente> eventoCapitalClienteCCAgrupados = new ArrayList<>();
		
		String[] values = new String [ClientePortafolioCtaPropia.size()];
		 
		for(int i =0 ; i<ClientePortafolioCtaPropia.size(); i++) {
			values[i]=ClientePortafolioCtaPropia.get(i).IdPortafolio+"";
		}
		
		HashMap<String,Object> OperacionesCtaPropia = new HashMap<String,Object>();
		
		
		eventoCapitalCliente.forEach(element ->{
			boolean found = Arrays.asList(values).contains(element.IdPortafolio+"");
			if (found) {
				if("VENPAC".equalsIgnoreCase(element.IdTipoOperacion) 
						|| "COMPAC".equalsIgnoreCase(element.IdTipoOperacion) ) {
					EventoCapitalCliente value = element;
					eventoCapitalClienteCCAgrupados.add(value);
				}
			}
			
		});
		
		//agrupamos por instrumento y operacion
		eventoCapitalClienteCCAgrupados.forEach(element->{
			String key = element.IdPortafolio+"_"+element.IdTipoOperacion+"_"+element.IdInstrumento;
			EventoCapitalCliente OperacionesCtaPropiahashObject = (EventoCapitalCliente) OperacionesCtaPropia.get(key);
			if(OperacionesCtaPropiahashObject == null) {
				//si es nulo no ha sido agregado
				EventoCapitalCliente value = element;
				OperacionesCtaPropia.put(key, value);
			}else {
				//si es distinto de null es por que se agrego y sumamos campos
				sumaMontosCantidadesTotal(element, OperacionesCtaPropiahashObject, OperacionesCtaPropia);
			}
			
		});
		List<EventoCapitalCliente> eventoCapitalClienteAux = eventoCapitalCliente;
		eventoCapitalCliente = new ArrayList<>();
		
		for(int i =0 ; i<eventoCapitalClienteAux.size(); i++) {
			EventoCapitalCliente element = eventoCapitalClienteAux.get(i);
			
			//si es venta/compra pacto y no es de cta propia se agrega
			if("VENPAC".equalsIgnoreCase(element.IdTipoOperacion) 
					|| "COMPAC".equalsIgnoreCase(element.IdTipoOperacion) ) {
				boolean found = Arrays.asList(values).contains(element.IdPortafolio+"");
				if (!found) {
					eventoCapitalCliente.add(element);
				}
			}else {
				//si no es cta propia se agrega
				eventoCapitalCliente.add(element);
			}
		}
		//agregamos los agrupados.
		    for (Object valor : OperacionesCtaPropia.values()) {
		    	eventoCapitalCliente.add((EventoCapitalCliente)valor);
		    }
		return eventoCapitalCliente;
	}



	private void sumaMontosCantidadesTotal(EventoCapitalCliente element,
			EventoCapitalCliente OperacionesCtaPropiahashObject, HashMap<String, Object> operacionesCtaPropia) {
		String key = element.IdPortafolio+"_"+element.TipoOperacion+"_"+element.IdInstrumento;
		BigDecimal interesAux,MontoCapitalAux,MontoReajusteAux,MontoRetencionAux,CantidadDerechoAux, CantidadNuevaAux,TotalAux;
		
		interesAux= OperacionesCtaPropiahashObject.MontoInteres.add(element.MontoInteres);
		MontoCapitalAux = OperacionesCtaPropiahashObject.MontoCapital.add(element.MontoCapital);
		MontoReajusteAux = OperacionesCtaPropiahashObject.MontoReajuste.add(element.MontoReajuste);
		MontoRetencionAux= OperacionesCtaPropiahashObject.MontoRetencion.add(element.MontoRetencion);
		CantidadDerechoAux = OperacionesCtaPropiahashObject.CantidadDerecho.add(element.CantidadDerecho);
		CantidadNuevaAux = OperacionesCtaPropiahashObject.CantidadNueva.add(element.CantidadNueva);
		TotalAux = OperacionesCtaPropiahashObject.Total.add(element.Total);
		
		OperacionesCtaPropiahashObject.MontoInteres = interesAux;
		OperacionesCtaPropiahashObject.MontoCapital = MontoCapitalAux;
		OperacionesCtaPropiahashObject.MontoReajuste= MontoReajusteAux;
		OperacionesCtaPropiahashObject.MontoRetencion = MontoRetencionAux;
		OperacionesCtaPropiahashObject.CantidadDerecho =CantidadDerechoAux;
		OperacionesCtaPropiahashObject.CantidadNueva =CantidadNuevaAux;
		OperacionesCtaPropiahashObject.Total = TotalAux;
		
		operacionesCtaPropia.replace(key, OperacionesCtaPropiahashObject);
		System.out.println(operacionesCtaPropia);
		
	}



	// -----------------------------------------
	// CALL
	// -----------------------------------------
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaProcesarEventoCapitalCorteCupon jin = gson.fromJson(in.toString(), EntradaProcesarEventoCapitalCorteCupon.class);
		SalidaProcesarEventoCapitalCorteCupon out = logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	// -----------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -----------------------------------------
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCorteCuponDesde\":\"2026-02-16\",\"p_FechaCorteCuponHasta\":\"2026-02-16\"}";
               	EntradaProcesarEventoCapitalCorteCupon Objin = gson.fromJson(json, EntradaProcesarEventoCapitalCorteCupon.class);
								
				ProcesarEventoCapitalCorteCupon  kk = new ProcesarEventoCapitalCorteCupon();
				System.out.println(kk.logica(con, Objin));
				//con.rollback();
				 	con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
}
