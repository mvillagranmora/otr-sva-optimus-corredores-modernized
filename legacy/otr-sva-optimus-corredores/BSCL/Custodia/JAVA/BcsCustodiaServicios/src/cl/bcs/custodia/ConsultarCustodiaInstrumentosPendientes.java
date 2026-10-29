package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.Cliente;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.CuentaCustodio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.DatosCorredora;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Instrumento;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.InstrumentoClasificacionRiesgo;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.MatrizAgrupadorAPV;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.PersonaDestino;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Portafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.ProductoAgrupador;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.ProductoCustodia;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCustodiaInstrumentosPendientes implements JavaCall{
	
	private final Logger logger =  LogManager.getLogger(ConsultarCustodiaInstrumentosPendientes.class.getName());
	
	public static final String[] TIPO_BUSQUEDAS = {"CUS","VENPAC","COMPAC","SIM","PRT","EMP","GTA","TRA","TRANOM","REP","SUB"};
	
	private final String DATOS_CTA_PROPIA_SELECT 				= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarCtaPropia.sql");
	private final String CTA_PROPIA_RELACIONADOS_SELECT 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarRelacionados.sql");

	private final String CONSULTA_ING_TRANSITO_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarMovimientoTransitoING.sql");
	private final String CONSULTA_ING_TRANSITO_HIS_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarMovimientoTransitoINGHIS.sql");
	
	private final String CONSULTA_MOV_TRANSITO_INGEGR_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarMovimientoTransitoINGEGR.sql");
	
	private final String CONSULTA_CUSTODIA_SELECT 				= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCustodia.sql");
	private final String CONSULTA_CUSTODIA_HISTORICO	 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarCustodiaHistorico.sql");
	private final String CONSULTA_CUSTODIA_HIS_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCustodiaHIS.sql");
	
	private final String CONSULTA_CUSTODIA_PRESUNTO_SELECT		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCustodiaPresunto.sql");
	private final String CONSULTA_CUSTODIA_PRESUNTO_HIS_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCustodiaPresuntoHIS.sql");
	
	private final String CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_SELECT = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultarIngresoTransitoNominalCtaPropia.sql");
	private final String CONSULTA_ING_TRANSITO_NOMINAL_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominal.sql");
	
	private final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_SELECT = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominalPresuntoCtaPropia.sql");
	private final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominalPresunto.sql");
	
	private final String CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_HIS_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominalCtaPropiaHis.sql");
	private final String CONSULTA_ING_TRANSITO_NOMINAL_HIS_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominalHis.sql");
	private final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_HIS_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominalPresuntoCtaPropiaHis.sql");
	private final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_HIS_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaIngresoTransitoNominalPresuntoHis.sql");
	

	private final String CONSULTA_VENTA_DESCUBIERTA_PRESUNTO_SELECT = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaVentaDescubiertaPresunto.sql");
	private final String CONSULTA_VENTA_DESCUBIERTA_PRESUNTO_HIS_SELECT	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaVentaDescubiertaPresuntoHis.sql");
	
	private final String CONSULTA_GARANTIA_SELECT 				= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaGarantias.sql");
	private final String CONSULTA_GARANTIA_HIS_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaGarantiasHIS.sql");
	
	private final String CONSULTA_PRESTAMOS_SELECT 				= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaPrestamos.sql");
	private final String CONSULTA_PRESTAMOS_HIS_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaPrestamosHIS.sql");
	

	private final String CONSULTA_VENTA_PACTO_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaVentaPacto.sql");
	private final String CONSULTA_VENTA_PACTO_HIS_SELECT 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaVentaPactoHIS.sql");
	private final String CONSULTA_COMPRA_PACTO_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCompraPacto.sql");
	private final String CONSULTA_COMPRA_PACTO_HIS_SELECT 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCompraPactoHIS.sql");
	
	private final String CONSULTA_COMPRA_VENTA_SIMULTANEA_SELECT= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCompraSimultanea.sql");
	private final String CONSULTA_COMPRA_VENTA_SIMULTANEA_HIS_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaCompraSimultaneaHIS.sql");
	

	private final String CONSULTA_EMPRESTITOS_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaEmprestitos.sql");
	private final String CONSULTA_EMPRESTITOS_HIS_SELECT 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaEmprestitosHIS.sql");
	
	
	private final String CONSULTA_CLIENTES_SELECT 				= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaClientes.sql");

	private final String CONSULTA_INSTRUMENTO_RIESGO_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaInstrumentoRiesgo.sql");

	private final String CONSULTA_CUENTA_CUSTODIO_SELECT 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaCuentaCustodioPortafolioPrimera.sql");

	private final String CONSULTA_INSTRUMENTO_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaInstrumentosPendientes_ConsultaInstrumentos.sql");

	private final String CONSULTA_PORTAFOLIO_SELECT 			= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaPortafolio.sql");


	private final String CONSULTA_MATRIZ_AGRUPADOR_APV_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaMatrizAgrupadorAPV.sql");
	private final String CONSULTA_PRODUCTO_AGRUPADOR_SELECT 	= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaProductoAgrupador.sql");

	private final String CONSULTA_PRODUCTOS_CUSTODIA	 		= loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultarProductos.sql");
	
	
	public static class Entrada_ConsultarCustodiaInstrumentosPendientes{
		
		public Long  	p_IdEmpresa;
		public Long  	p_IdUsuario;
		public String   p_IdModulo;
		public Long 	p_IdCliente; 
		public Long 	p_IdPortafolio;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;
		public Long		p_IdInstrumento;
		public String	p_Nemotecnico;
		public String 	p_IdBoveda;       //solo actua como filtro, no agrupa x boveda
		public String	p_AgrupaPorBoveda; 
		public String 	p_AgrupaPorRelacion;
		public String   p_FechaConsulta;
		public String  	p_TipoFechaConsulta; 
		public Long 	p_IdProductoAgrupador;
	
		public Entrada_ConsultarCustodiaInstrumentosPendientes(){
			this.p_IdCliente=0L;
			this.p_IdPortafolio=0L;
			this.p_IdInstrumento=0L;
			this.p_Nemotecnico="";
			this.p_IdBoveda="";
			this.p_AgrupaPorBoveda="N";
			this.p_AgrupaPorRelacion="N";
			this.p_FechaConsulta="0001-01-01";
			this.p_TipoFechaConsulta="LIQ";
			this.p_IdProductoAgrupador= null;	
		}
		 	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida_ConsultarCustodiaInstrumentosPendientes{
		public List<Tabla_Salida> P_OUT_TBL;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		
		public Salida_ConsultarCustodiaInstrumentosPendientes(){
			this.P_OUT_TBL=new ArrayList<Tabla_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class Tabla_Salida{
		public Long IdInterno;
		public Long IdEmpresa; 				// AV
		public String TipoFechaConsulta;  		// AV
		public String FechaCustodia;			// AV

		public String     IdClasificacionTipoInstrumento;
		public String     NombreClasificacionTipoInstrumento;
		public String     IdTipoInstrumento;
		public String     NombreTipoInstrumento;
		public String 	  ISIN;
		public Long		  IdInstrumento;
		public String	  ClasificacionRiesgoInstrumento;
		
		public String	  Nemotecnico;
		public String	  IdEmisor;
		public String	  EsFungible;
		public Long    	  IdCliente;
		public Long		  IdPortafolio;
		public String	  DocumentoIdentidad;
		public String 	  DocumentoIdentidadRutCavali;
		public String	  NombreCliente;
		public String	  CodigoPortafolio;
		public String	  CodigoPortafolioWS;
		public String	  IdBoveda;
		public String 	  NumeroCuentaBoveda;
		public String	  Relacion;
		public String 	  DescripcionRelacion;
		public Long 	  Tamano;
		public BigDecimal CantidadTotal;  //salida previa
		public BigDecimal ValorCompra;
		public BigDecimal MontoCompra;
		
		public BigDecimal ValorTotalMercado;
		public BigDecimal MontoTotalMercado;
		public String     IdMonedaEmision;
		public String	  IdMonedaInstrReajuste;
		public String	  IdMonedaPrincipal;
				

		//CUSTODIA
		public BigDecimal Rentabilidad;
		public BigDecimal CantidadCustodia;
		public BigDecimal PPPCustodia;
		public BigDecimal PPPCustodiaMonedaLocal;
		public BigDecimal ValorPPPCustodia;		 //moneda instrumento
		public BigDecimal MontoPPPCustodia; 	 //moneda sistema
		public BigDecimal PrecioMercadoCustodia;
		public BigDecimal PrecioMercadoCustodiaMonedaLocal;
		public BigDecimal TasaMercadoCustodia;
		public BigDecimal PrecioCompraCustodia;
		
		public BigDecimal INGPrecioPromedioCompra; //CELZORRO-37
		public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
		public String	  IdMonedaSistema;
		
			//(PrecioMercadoCustodia//PrecioCompraCustodia)/ PrecioCompraCustodia
		public BigDecimal ValorMercadoCustodia; //moneda instrumento
		public BigDecimal MontoMercadoCustodia; //moneda sistema
		public BigDecimal DiferenciaMercadoCustodia;		
		public String     EsPagaderoEn;
		public String     FechaRespaldo;
		public BigDecimal InteresCorridoCustodia;	
		
		public BigDecimal DividendoEnDinero;

		 //SIMULTANEA COMPRA
		public BigDecimal CantidadSimulCom;
		public BigDecimal PPPSimulCom;
		public BigDecimal ValorPPPSimulCom;
		public BigDecimal PrecioMercadoSimulCom;
		public BigDecimal ValorMercadoSimulCom;
		public BigDecimal DiferenciaMercadoSimulCom;
		public BigDecimal InteresCorridoSimulCom;
		public BigDecimal MontoPPPSimulCom;
		public BigDecimal TasaMercadoSimulCom;
		public BigDecimal MontoMercadoSimulCom;

		 //SIMULTANEA VENTA 
		public BigDecimal CantidadSimulVen;
		public BigDecimal PPPSimulVen;
		public BigDecimal ValorPPPSimulVen;
		public BigDecimal PrecioMercadoSimulVen;
		public BigDecimal ValorMercadoSimulVen;
		public BigDecimal DiferenciaMercadoSimulVen;
		public BigDecimal InteresCorridoSimulVen;
		public BigDecimal MontoPPPSimulVen;
		public BigDecimal MontoMercadoSimulVen;
		public BigDecimal TasaMercadoSimulVen;

		 //PRESTAMO
		public BigDecimal CantidadPrestamo;
		public BigDecimal PPPPrestamo;
		public BigDecimal ValorPPPPrestamo;
		public BigDecimal PrecioMercadoPrestamo;
		public BigDecimal ValorMercadoPrestamo;
		public BigDecimal DiferenciaMercadoPrestamo;
		public BigDecimal InteresCorridoPrestamo;
		public BigDecimal MontoPPPPrestamo;
		public BigDecimal MontoMercadoPrestamo;
		public BigDecimal TasaMercadoPrestamo;

		 //EMPRESTITO
		public BigDecimal CantidadEmpresti;
		public BigDecimal PPPEmpresti;
		public BigDecimal ValorPPPEmpresti;
		public BigDecimal PrecioMercadoEmpresti;
		public BigDecimal ValorMercadoEmpresti;
		public BigDecimal DiferenciaMercadoEmpresti;
		public BigDecimal InteresCorridoEmpresti;
		public BigDecimal MontoPPPEmpresti;
		public BigDecimal MontoMercadoEmpresti;
		public BigDecimal TasaMercadoEmpresti;
		
		 //GARANTIA
		public BigDecimal CantidadGarantia;
		public BigDecimal PPPGarantia;
		public BigDecimal ValorPPPGarantia;
		public BigDecimal PrecioMercadoGarantia;
		public BigDecimal ValorMercadoGarantia;
		public BigDecimal DiferenciaMercadoGarantia;
		public BigDecimal InteresCorridoGarantia;
		public BigDecimal MontoPPPGarantia;
		public BigDecimal MontoMercadoGarantia;
		public BigDecimal TasaMercadoGarantia;

		 //Venta con PACTO
		public BigDecimal CantidadPactosV;
		public BigDecimal CantidadPactosNomV;
		public BigDecimal PPPPactosV;
		public BigDecimal ValorPPPPactosV;
		public BigDecimal PrecioMercadoPactosV;
		public BigDecimal ValorMercadoPactosV;
		public BigDecimal DiferenciaMercadoPactosV;
		public BigDecimal InteresCorridoPactosV;
		public BigDecimal MontoPPPPactosV;
		public BigDecimal MontoMercadoPactosV;
		public BigDecimal TasaMercadoPactosV;

		 //Compra con PACTO
		public BigDecimal CantidadPactosC;
		public BigDecimal CantidadPactosNomC;
		public BigDecimal PPPPactosC;
		public BigDecimal ValorPPPPactosC;
		public BigDecimal PrecioMercadoPactosC;
		public BigDecimal ValorMercadoPactosC;
		public BigDecimal DiferenciaMercadoPactosC;
		public BigDecimal InteresCorridoPactosC;
		public BigDecimal MontoPPPPactosC;
		public BigDecimal MontoMercadoPactosC;
		public BigDecimal TasaMercadoPactosC;

		 //Reporte de Venta
		public BigDecimal CantidadReporteVenta;
		public BigDecimal PPPReporteVenta;
		public BigDecimal ValorPPPReporteVenta;
		public BigDecimal PrecioMercadoReporteVenta;
		public BigDecimal ValorMercadoReporteVenta;
		public BigDecimal DiferenciaMercadoReporteVenta;
		public BigDecimal InteresCorridoReporteVenta;
		public BigDecimal MontoPPPReporteVenta;
		public BigDecimal MontoMercadoReporteVenta;
		public BigDecimal TasaMercadoReporteVenta;

		 //Reporte de Compra
		public BigDecimal CantidadReporteCompra;
		public BigDecimal PPPReporteCompra;
		public BigDecimal ValorPPPReporteCompra;
		public BigDecimal PrecioMercadoReporteCompra;
		public BigDecimal ValorMercadoReporteCompra;
		public BigDecimal DiferenciaMercadoReporteCompra;
		public BigDecimal InteresCorridoReporteCompra;
		public BigDecimal MontoPPPReporteCompra;
		public BigDecimal MontoMercadoReporteCompra;
		public BigDecimal TasaMercadoReporteCompra;

		 //INGRESO
		public BigDecimal CantidadTransIng;
		public BigDecimal CantidadTranomIng;
		public BigDecimal ValorPPPTransIng;
		public BigDecimal MontoPPPTransIng;
		public BigDecimal PrecioMercadoTransIng;
		public BigDecimal TasaMercadoTransIng;
		public BigDecimal ValorMercadoTransIng;
		public BigDecimal MontoMercadoTransIng;
		public BigDecimal InteresCorridoTransIng;		
		
		 //EGRESO
		public BigDecimal CantidadTransEgr;
		public BigDecimal CantidadTransVenDesEgr;  // Transito Egreso por Venta Descubierta  
		public BigDecimal CantidadTranomEgr;
		public BigDecimal ValorPPPTransEgr;
		public BigDecimal MontoPPPTransEgr;
		public BigDecimal PrecioMercadoTransEgr;
		public BigDecimal TasaMercadoTransEgr;
		public BigDecimal ValorMercadoTransEgr;
		public BigDecimal MontoMercadoTransEgr;
		public BigDecimal InteresCorridoTransEgr;
		
		//EGRESOS COMPRAS NOMINALES
		public BigDecimal CantidadTransEgrNom;
		public BigDecimal ValorPPPTransEgrNom;
		public BigDecimal MontoPPPTransEgrNom;
		public BigDecimal PrecioMercadoTransEgrNom;
		public BigDecimal TasaMercadoTransEgrNom;
		public BigDecimal ValorMercadoTransEgrNom;
		public BigDecimal MontoMercadoTransEgrNom;
		public BigDecimal InteresCorridoTransEgrNom;
		
		//INGRESOS COMPRAS NOMINALES
		public BigDecimal CantidadTransIngNom;
		public BigDecimal ValorPPPTransIngNom;
		public BigDecimal MontoPPPTransIngNom;
		public BigDecimal PrecioMercadoTransIngNom;
		public BigDecimal TasaMercadoTransIngNom;
		public BigDecimal ValorMercadoTransIngNom;
		public BigDecimal MontoMercadoTransIngNom;
		public BigDecimal InteresCorridoTransIngNom;		

		
		public BigDecimal TotalPosicion;
		public BigDecimal TotalPosicionPresunta;

		public BigDecimal CantidadSimCTraNom;
		public BigDecimal CantidadSimVTraNom;
		
		public String Producto;
		public Date	  FechaVencimiento;
		public String AfectaPresunto;
		public String RegimenTributario;
		public String IdRegimenTributario;
		public Long   IdMatrizAgrupador;
		public String EsADC;
		
		//VALORES PARA PRECIO PROMEDIO
		public BigDecimal MontoPrecioPromedio;
		public BigDecimal CantidadPrecioPromedio;
		
		public String RazonSocialInstrumento;
		public Date FechaFuente;
		
		public BigDecimal CantidadTransitoIng;
		public BigDecimal CantidadTransitoEgr;
		
		public Tabla_Salida(){
			
			this.Rentabilidad=BigDecimal.ZERO;this.CantidadCustodia=BigDecimal.ZERO;this.PPPCustodia=BigDecimal.ZERO;this.ValorPPPCustodia=BigDecimal.ZERO;		
			this.MontoPPPCustodia=BigDecimal.ZERO;this.PrecioMercadoCustodia=BigDecimal.ZERO;this.TasaMercadoCustodia=BigDecimal.ZERO;this.PrecioCompraCustodia=BigDecimal.ZERO;this.ValorMercadoCustodia=BigDecimal.ZERO;this.MontoMercadoCustodia=BigDecimal.ZERO; this.DiferenciaMercadoCustodia=BigDecimal.ZERO;this.INGPrecioPromedioCompra = BigDecimal.ZERO;this.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
			this.InteresCorridoCustodia=BigDecimal.ZERO;this.DividendoEnDinero=BigDecimal.ZERO;this.Tamano=1L;this.CantidadTotal=BigDecimal.ZERO;this.ValorCompra=BigDecimal.ZERO;this.MontoCompra=BigDecimal.ZERO;this.ValorTotalMercado=BigDecimal.ZERO;this.MontoTotalMercado=BigDecimal.ZERO;
			this.CantidadSimulCom=BigDecimal.ZERO;this.PPPSimulCom=BigDecimal.ZERO;this.ValorPPPSimulCom=BigDecimal.ZERO;this.PrecioMercadoSimulCom=BigDecimal.ZERO;this.ValorMercadoSimulCom=BigDecimal.ZERO;this.DiferenciaMercadoSimulCom=BigDecimal.ZERO;this.InteresCorridoSimulCom=BigDecimal.ZERO;this.MontoPPPSimulCom=BigDecimal.ZERO;this.TasaMercadoSimulCom=BigDecimal.ZERO;this.MontoMercadoSimulCom=BigDecimal.ZERO;
			this.CantidadSimulVen=BigDecimal.ZERO;this.PPPSimulVen=BigDecimal.ZERO;this.ValorPPPSimulVen=BigDecimal.ZERO;this.PrecioMercadoSimulVen=BigDecimal.ZERO;this.ValorMercadoSimulVen=BigDecimal.ZERO;this.DiferenciaMercadoSimulVen=BigDecimal.ZERO;this.InteresCorridoSimulVen=BigDecimal.ZERO;this.MontoPPPSimulVen=BigDecimal.ZERO;this.MontoMercadoSimulVen=BigDecimal.ZERO;this.TasaMercadoSimulVen=BigDecimal.ZERO;
			this.CantidadPrestamo=BigDecimal.ZERO;this.PPPPrestamo=BigDecimal.ZERO;this.ValorPPPPrestamo=BigDecimal.ZERO;this.PrecioMercadoPrestamo=BigDecimal.ZERO;this.ValorMercadoPrestamo=BigDecimal.ZERO;this.DiferenciaMercadoPrestamo=BigDecimal.ZERO;this.InteresCorridoPrestamo=BigDecimal.ZERO;this.MontoPPPPrestamo=BigDecimal.ZERO;this.MontoMercadoPrestamo=BigDecimal.ZERO;this.TasaMercadoPrestamo=BigDecimal.ZERO;
			this.CantidadEmpresti=BigDecimal.ZERO;this.PPPEmpresti=BigDecimal.ZERO;this.ValorPPPEmpresti=BigDecimal.ZERO;this.PrecioMercadoEmpresti=BigDecimal.ZERO;this.ValorMercadoEmpresti=BigDecimal.ZERO;this.DiferenciaMercadoEmpresti=BigDecimal.ZERO;this.InteresCorridoEmpresti=BigDecimal.ZERO;this.MontoPPPEmpresti=BigDecimal.ZERO;this.MontoMercadoEmpresti=BigDecimal.ZERO;this.TasaMercadoEmpresti=BigDecimal.ZERO;
			this.CantidadGarantia=BigDecimal.ZERO;this.PPPGarantia=BigDecimal.ZERO;this.ValorPPPGarantia=BigDecimal.ZERO;this.PrecioMercadoGarantia=BigDecimal.ZERO;this.ValorMercadoGarantia=BigDecimal.ZERO;this.DiferenciaMercadoGarantia=BigDecimal.ZERO;this.InteresCorridoGarantia=BigDecimal.ZERO;this.MontoPPPGarantia=BigDecimal.ZERO;this.MontoMercadoGarantia=BigDecimal.ZERO;this.TasaMercadoGarantia=BigDecimal.ZERO;
			this.CantidadPactosV=BigDecimal.ZERO;this.PPPPactosV=BigDecimal.ZERO;this.ValorPPPPactosV=BigDecimal.ZERO;this.PrecioMercadoPactosV=BigDecimal.ZERO;this.ValorMercadoPactosV=BigDecimal.ZERO;this.DiferenciaMercadoPactosV=BigDecimal.ZERO;this.InteresCorridoPactosV=BigDecimal.ZERO;this.MontoPPPPactosV=BigDecimal.ZERO;this.MontoMercadoPactosV=BigDecimal.ZERO;this.TasaMercadoPactosV=BigDecimal.ZERO;
			this.CantidadPactosC=BigDecimal.ZERO;this.PPPPactosC=BigDecimal.ZERO;this.ValorPPPPactosC=BigDecimal.ZERO;this.PrecioMercadoPactosC=BigDecimal.ZERO;
			this.ValorMercadoPactosC=BigDecimal.ZERO;this.DiferenciaMercadoPactosC=BigDecimal.ZERO;this.InteresCorridoPactosC=BigDecimal.ZERO;this.MontoPPPPactosC=BigDecimal.ZERO;this.MontoMercadoPactosC=BigDecimal.ZERO;this.TasaMercadoPactosC=BigDecimal.ZERO;
			this.CantidadReporteVenta=BigDecimal.ZERO;this.PPPReporteVenta=BigDecimal.ZERO;this.ValorPPPReporteVenta=BigDecimal.ZERO;this.PrecioMercadoReporteVenta=BigDecimal.ZERO;this.ValorMercadoReporteVenta=BigDecimal.ZERO;this.DiferenciaMercadoReporteVenta=BigDecimal.ZERO;this.InteresCorridoReporteVenta=BigDecimal.ZERO;this.MontoPPPReporteVenta=BigDecimal.ZERO;this.MontoMercadoReporteVenta=BigDecimal.ZERO;this.TasaMercadoReporteVenta=BigDecimal.ZERO;
			this.CantidadReporteCompra=BigDecimal.ZERO;this.PPPReporteCompra=BigDecimal.ZERO;this.ValorPPPReporteCompra=BigDecimal.ZERO;this.PrecioMercadoReporteCompra=BigDecimal.ZERO;this.ValorMercadoReporteCompra=BigDecimal.ZERO;this.DiferenciaMercadoReporteCompra=BigDecimal.ZERO;this.InteresCorridoReporteCompra=BigDecimal.ZERO;this.MontoPPPReporteCompra=BigDecimal.ZERO;this.MontoMercadoReporteCompra=BigDecimal.ZERO;this.TasaMercadoReporteCompra=BigDecimal.ZERO;
			this.CantidadTransIng=BigDecimal.ZERO;this.CantidadTranomIng=BigDecimal.ZERO;this.ValorPPPTransIng=BigDecimal.ZERO;this.MontoPPPTransIng=BigDecimal.ZERO;this.PrecioMercadoTransIng=BigDecimal.ZERO;this.TasaMercadoTransIng=BigDecimal.ZERO;this.ValorMercadoTransIng=BigDecimal.ZERO;this.MontoMercadoTransIng=BigDecimal.ZERO;this.InteresCorridoTransIng=BigDecimal.ZERO;
			this.CantidadTransVenDesEgr=BigDecimal.ZERO;this.CantidadTransEgr=BigDecimal.ZERO;this.CantidadTranomEgr=BigDecimal.ZERO;this.ValorPPPTransEgr=BigDecimal.ZERO;this.MontoPPPTransEgr=BigDecimal.ZERO;this.PrecioMercadoTransEgr=BigDecimal.ZERO;this.TasaMercadoTransEgr=BigDecimal.ZERO;this.ValorMercadoTransEgr=BigDecimal.ZERO;
			this.MontoMercadoTransEgr=BigDecimal.ZERO;this.InteresCorridoTransEgr=BigDecimal.ZERO;this.TotalPosicion=BigDecimal.ZERO;this.TotalPosicionPresunta=BigDecimal.ZERO;
			this.CantidadSimCTraNom=BigDecimal.ZERO;this.CantidadSimVTraNom=BigDecimal.ZERO;this.CantidadPactosNomC=BigDecimal.ZERO;this.CantidadPactosNomV=BigDecimal.ZERO;
			this.MontoPrecioPromedio=BigDecimal.ZERO;this.CantidadPrecioPromedio=BigDecimal.ZERO;this.CantidadTransitoIng=BigDecimal.ZERO;;this.CantidadTransitoEgr=BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}

		public void copyValuesFrom(Tabla_Salida obj) {
			this.IdEmpresa = obj.IdEmpresa;
			this.TipoFechaConsulta = obj.TipoFechaConsulta;
			this.FechaCustodia = obj.FechaCustodia;
			this.IdClasificacionTipoInstrumento = obj.IdClasificacionTipoInstrumento;
			this.NombreClasificacionTipoInstrumento = obj.NombreClasificacionTipoInstrumento;
			this.IdTipoInstrumento = obj.IdTipoInstrumento;
			this.NombreTipoInstrumento = obj.NombreTipoInstrumento;
			this.ISIN = obj.ISIN;
			this.IdInstrumento = obj.IdInstrumento;
			this.ClasificacionRiesgoInstrumento = obj.ClasificacionRiesgoInstrumento;
			this.Nemotecnico = obj.Nemotecnico;
			this.IdEmisor = obj.IdEmisor;
			this.EsFungible = obj.EsFungible;
			this.IdCliente = obj.IdCliente;
			this.IdPortafolio = obj.IdPortafolio;
			this.DocumentoIdentidad = obj.DocumentoIdentidad;
			this.DocumentoIdentidadRutCavali = obj.DocumentoIdentidadRutCavali;
			this.NombreCliente = obj.NombreCliente;
			this.CodigoPortafolio = obj.CodigoPortafolio;
			this.CodigoPortafolioWS = obj.CodigoPortafolioWS;
			this.IdBoveda = obj.IdBoveda;
			this.NumeroCuentaBoveda = obj.NumeroCuentaBoveda;
			this.Relacion = obj.Relacion;
			this.DescripcionRelacion = obj.DescripcionRelacion;
			this.Tamano = obj.Tamano;
			this.CantidadTotal = obj.CantidadTotal;
			this.ValorCompra = obj.ValorCompra;
			this.MontoCompra = obj.MontoCompra;
			this.ValorTotalMercado = obj.ValorTotalMercado;
			this.MontoTotalMercado = obj.MontoTotalMercado;
			this.IdMonedaEmision = obj.IdMonedaEmision;
			this.IdMonedaInstrReajuste = obj.IdMonedaInstrReajuste;
			this.IdMonedaPrincipal = obj.IdMonedaPrincipal;
			this.Rentabilidad = obj.Rentabilidad;
			this.CantidadCustodia = obj.CantidadCustodia;
			this.PPPCustodia = obj.PPPCustodia;
			this.ValorPPPCustodia = obj.ValorPPPCustodia;
			this.MontoPPPCustodia = obj.MontoPPPCustodia;
			this.PrecioMercadoCustodia = obj.PrecioMercadoCustodia;
			this.TasaMercadoCustodia = obj.TasaMercadoCustodia;
			this.PrecioCompraCustodia = obj.PrecioCompraCustodia;
			this.INGPrecioPromedioCompra = obj.INGPrecioPromedioCompra;
			this.INGMontoPrecioPromedioMonedaInstrumento = obj.INGMontoPrecioPromedioMonedaInstrumento;
			this.IdMonedaSistema = obj.IdMonedaSistema;
			this.ValorMercadoCustodia = obj.ValorMercadoCustodia;
			this.MontoMercadoCustodia = obj.MontoMercadoCustodia;
			this.DiferenciaMercadoCustodia = obj.DiferenciaMercadoCustodia;
			this.EsPagaderoEn = obj.EsPagaderoEn;
			this.FechaRespaldo = obj.FechaRespaldo;
			this.InteresCorridoCustodia = obj.InteresCorridoCustodia;
			this.CantidadSimulCom = obj.CantidadSimulCom;
			this.PPPSimulCom = obj.PPPSimulCom;
			this.ValorPPPSimulCom = obj.ValorPPPSimulCom;
			this.PrecioMercadoSimulCom = obj.PrecioMercadoSimulCom;
			this.ValorMercadoSimulCom = obj.ValorMercadoSimulCom;
			this.DiferenciaMercadoSimulCom = obj.DiferenciaMercadoSimulCom;
			this.InteresCorridoSimulCom = obj.InteresCorridoSimulCom;
			this.MontoPPPSimulCom = obj.MontoPPPSimulCom;
			this.TasaMercadoSimulCom = obj.TasaMercadoSimulCom;
			this.MontoMercadoSimulCom = obj.MontoMercadoSimulCom;
			this.CantidadSimulVen = obj.CantidadSimulVen;
			this.PPPSimulVen = obj.PPPSimulVen;
			this.ValorPPPSimulVen = obj.ValorPPPSimulVen;
			this.PrecioMercadoSimulVen = obj.PrecioMercadoSimulVen;
			this.ValorMercadoSimulVen = obj.ValorMercadoSimulVen;
			this.DiferenciaMercadoSimulVen = obj.DiferenciaMercadoSimulVen;
			this.InteresCorridoSimulVen = obj.InteresCorridoSimulVen;
			this.MontoPPPSimulVen = obj.MontoPPPSimulVen;
			this.MontoMercadoSimulVen = obj.MontoMercadoSimulVen;
			this.TasaMercadoSimulVen = obj.TasaMercadoSimulVen;
			this.CantidadPrestamo = obj.CantidadPrestamo;
			this.PPPPrestamo = obj.PPPPrestamo;
			this.ValorPPPPrestamo = obj.ValorPPPPrestamo;
			this.PrecioMercadoPrestamo = obj.PrecioMercadoPrestamo;
			this.ValorMercadoPrestamo = obj.ValorMercadoPrestamo;
			this.DiferenciaMercadoPrestamo = obj.DiferenciaMercadoPrestamo;
			this.InteresCorridoPrestamo = obj.InteresCorridoPrestamo;
			this.MontoPPPPrestamo = obj.MontoPPPPrestamo;
			this.MontoMercadoPrestamo = obj.MontoMercadoPrestamo;
			this.TasaMercadoPrestamo = obj.TasaMercadoPrestamo;
			this.CantidadEmpresti = obj.CantidadEmpresti;
			this.PPPEmpresti = obj.PPPEmpresti;
			this.ValorPPPEmpresti = obj.ValorPPPEmpresti;
			this.PrecioMercadoEmpresti = obj.PrecioMercadoEmpresti;
			this.ValorMercadoEmpresti = obj.ValorMercadoEmpresti;
			this.DiferenciaMercadoEmpresti = obj.DiferenciaMercadoEmpresti;
			this.InteresCorridoEmpresti = obj.InteresCorridoEmpresti;
			this.MontoPPPEmpresti = obj.MontoPPPEmpresti;
			this.MontoMercadoEmpresti = obj.MontoMercadoEmpresti;
			this.TasaMercadoEmpresti = obj.TasaMercadoEmpresti;
			this.CantidadGarantia = obj.CantidadGarantia;
			this.PPPGarantia = obj.PPPGarantia;
			this.ValorPPPGarantia = obj.ValorPPPGarantia;
			this.PrecioMercadoGarantia = obj.PrecioMercadoGarantia;
			this.ValorMercadoGarantia = obj.ValorMercadoGarantia;
			this.DiferenciaMercadoGarantia = obj.DiferenciaMercadoGarantia;
			this.InteresCorridoGarantia = obj.InteresCorridoGarantia;
			this.MontoPPPGarantia = obj.MontoPPPGarantia;
			this.MontoMercadoGarantia = obj.MontoMercadoGarantia;
			this.TasaMercadoGarantia = obj.TasaMercadoGarantia;
			this.CantidadPactosV = obj.CantidadPactosV;
			this.CantidadPactosNomV = obj.CantidadPactosNomV;
			this.PPPPactosV = obj.PPPPactosV;
			this.ValorPPPPactosV = obj.ValorPPPPactosV;
			this.PrecioMercadoPactosV = obj.PrecioMercadoPactosV;
			this.ValorMercadoPactosV = obj.ValorMercadoPactosV;
			this.DiferenciaMercadoPactosV = obj.DiferenciaMercadoPactosV;
			this.InteresCorridoPactosV = obj.InteresCorridoPactosV;
			this.MontoPPPPactosV = obj.MontoPPPPactosV;
			this.MontoMercadoPactosV = obj.MontoMercadoPactosV;
			this.TasaMercadoPactosV = obj.TasaMercadoPactosV;
			this.CantidadPactosC = obj.CantidadPactosC;
			this.CantidadPactosNomC = obj.CantidadPactosNomC;
			this.PPPPactosC = obj.PPPPactosC;
			this.ValorPPPPactosC = obj.ValorPPPPactosC;
			this.PrecioMercadoPactosC = obj.PrecioMercadoPactosC;
			this.ValorMercadoPactosC = obj.ValorMercadoPactosC;
			this.DiferenciaMercadoPactosC = obj.DiferenciaMercadoPactosC;
			this.InteresCorridoPactosC = obj.InteresCorridoPactosC;
			this.MontoPPPPactosC = obj.MontoPPPPactosC;
			this.MontoMercadoPactosC = obj.MontoMercadoPactosC;
			this.TasaMercadoPactosC = obj.TasaMercadoPactosC;
			this.CantidadReporteVenta = obj.CantidadReporteVenta;
			this.PPPReporteVenta = obj.PPPReporteVenta;
			this.ValorPPPReporteVenta = obj.ValorPPPReporteVenta;
			this.PrecioMercadoReporteVenta = obj.PrecioMercadoReporteVenta;
			this.ValorMercadoReporteVenta = obj.ValorMercadoReporteVenta;
			this.DiferenciaMercadoReporteVenta = obj.DiferenciaMercadoReporteVenta;
			this.InteresCorridoReporteVenta = obj.InteresCorridoReporteVenta;
			this.MontoPPPReporteVenta = obj.MontoPPPReporteVenta;
			this.MontoMercadoReporteVenta = obj.MontoMercadoReporteVenta;
			this.TasaMercadoReporteVenta = obj.TasaMercadoReporteVenta;
			this.CantidadReporteCompra = obj.CantidadReporteCompra;
			this.PPPReporteCompra = obj.PPPReporteCompra;
			this.ValorPPPReporteCompra = obj.ValorPPPReporteCompra;
			this.PrecioMercadoReporteCompra = obj.PrecioMercadoReporteCompra;
			this.ValorMercadoReporteCompra = obj.ValorMercadoReporteCompra;
			this.DiferenciaMercadoReporteCompra = obj.DiferenciaMercadoReporteCompra;
			this.InteresCorridoReporteCompra = obj.InteresCorridoReporteCompra;
			this.MontoPPPReporteCompra = obj.MontoPPPReporteCompra;
			this.MontoMercadoReporteCompra = obj.MontoMercadoReporteCompra;
			this.TasaMercadoReporteCompra = obj.TasaMercadoReporteCompra;
			this.CantidadTransIng = obj.CantidadTransIng;
			this.CantidadTranomIng = obj.CantidadTranomIng;
			this.ValorPPPTransIng = obj.ValorPPPTransIng;
			this.MontoPPPTransIng = obj.MontoPPPTransIng;
			this.PrecioMercadoTransIng = obj.PrecioMercadoTransIng;
			this.TasaMercadoTransIng = obj.TasaMercadoTransIng;
			this.ValorMercadoTransIng = obj.ValorMercadoTransIng;
			this.MontoMercadoTransIng = obj.MontoMercadoTransIng;
			this.InteresCorridoTransIng = obj.InteresCorridoTransIng;
			this.CantidadTransEgr = obj.CantidadTransEgr;
			this.CantidadTransVenDesEgr = obj.CantidadTransVenDesEgr;
			this.CantidadTranomEgr = obj.CantidadTranomEgr;
			this.ValorPPPTransEgr = obj.ValorPPPTransEgr;
			this.MontoPPPTransEgr = obj.MontoPPPTransEgr;
			this.PrecioMercadoTransEgr = obj.PrecioMercadoTransEgr;
			this.TasaMercadoTransEgr = obj.TasaMercadoTransEgr;
			this.ValorMercadoTransEgr = obj.ValorMercadoTransEgr;
			this.MontoMercadoTransEgr = obj.MontoMercadoTransEgr;
			this.InteresCorridoTransEgr = obj.InteresCorridoTransEgr;
			this.CantidadTransEgrNom = obj.CantidadTransEgrNom;
			this.CantidadTranomEgr = obj.CantidadTranomEgr;
			this.ValorPPPTransEgrNom = obj.ValorPPPTransEgrNom;
			this.MontoPPPTransEgrNom = obj.MontoPPPTransEgrNom;
			this.PrecioMercadoTransEgrNom = obj.PrecioMercadoTransEgrNom;
			this.TasaMercadoTransEgrNom = obj.TasaMercadoTransEgrNom;
			this.ValorMercadoTransEgrNom = obj.ValorMercadoTransEgrNom;
			this.MontoMercadoTransEgrNom = obj.MontoMercadoTransEgrNom;
			this.InteresCorridoTransEgrNom = obj.InteresCorridoTransEgrNom;
			this.CantidadTransIngNom = obj.CantidadTransIngNom;
			this.CantidadTranomIng = obj.CantidadTranomIng;
			this.ValorPPPTransIngNom = obj.ValorPPPTransIngNom;
			this.MontoPPPTransIngNom = obj.MontoPPPTransIngNom;
			this.PrecioMercadoTransIngNom = obj.PrecioMercadoTransIngNom;
			this.TasaMercadoTransIngNom = obj.TasaMercadoTransIngNom;
			this.ValorMercadoTransIngNom = obj.ValorMercadoTransIngNom;
			this.MontoMercadoTransIngNom = obj.MontoMercadoTransIngNom;
			this.InteresCorridoTransIngNom = obj.InteresCorridoTransIngNom;
			this.TotalPosicion = obj.TotalPosicion;
			this.TotalPosicionPresunta = obj.TotalPosicionPresunta;
			this.CantidadSimCTraNom = obj.CantidadSimCTraNom;
			this.CantidadSimVTraNom = obj.CantidadSimVTraNom;
			this.Producto = obj.Producto;
			this.FechaVencimiento = obj.FechaVencimiento;
			this.AfectaPresunto = obj.AfectaPresunto;
			this.RegimenTributario = obj.RegimenTributario;
			this.IdRegimenTributario = obj.IdRegimenTributario;
			this.IdMatrizAgrupador = obj.IdMatrizAgrupador;
			this.EsADC = obj.EsADC;
			this.CantidadPrecioPromedio = obj.CantidadPrecioPromedio;
			this.MontoPrecioPromedio = obj.MontoPrecioPromedio;
			this.RazonSocialInstrumento= obj.RazonSocialInstrumento;
			this.DividendoEnDinero = obj.DividendoEnDinero;
			this.CantidadTransitoIng = obj.CantidadTransitoIng;
			this.CantidadTransitoEgr= obj.CantidadTransitoEgr;
		}
	}
	
	
	public static class ResultadoConsulta{
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long   IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public String EsFungible;
		public Long   IdCliente;
		public Long   IdPersona;
		public Long   IdPortafolio;
		public String Producto;
		public String IdBoveda;
		public Long   IdCuentaCustodioPortafolio;
		public String NumeroCuentaBoveda;
		public String Relacion;
		public BigDecimal Cantidad;
		public BigDecimal CantidadNominal;
		public BigDecimal PrecioCompra;
		public BigDecimal ValorCompra;
		public BigDecimal MontoCompra;
		public BigDecimal INGPrecioMercadoActual;
		public BigDecimal INGTasaMercadoActual;
		public BigDecimal ValorMercado;
		public BigDecimal MontoMercado;
		public BigDecimal INGPrecioPromedioCompra;
		public BigDecimal INGMontoPrecioPromedioMonedaInstrumento;
		public String IdMonedaSistema;
		public BigDecimal ValorPPP;
		public BigDecimal MontoPPP;
		public String IdMonedaInstrReajuste;
		public String IdMonedaEmision;
		public String IdMonedaPrincipal;
		public BigDecimal InteresCorrido;
		public String	EsNominal;
		public String 	IdTipoOperacion;
		public String	AfectaPresunto;
		public String   RegimenTributario;
		public String   IdRegimenTributario;
		public String   EsVentaDescubierta;  // Indica si Mov en transito corresponde a venta descubierta
		public String	AccionCustodia;
		public String   EsCuentaPropia;
		public Long     IdMatrizAgrupador;
		public String   EsADC;
		public BigDecimal CantidadPrecioCompra;
		public BigDecimal MontoPrecioPromedio;
		public BigDecimal MontoCompraCarteraOrigen;
		public BigDecimal ValorFactorMonedaInstrumento;
		public String RazonSocialInstrumento;
		public BigDecimal DividendoEnDinero;
		public BigDecimal CantidadTransEgr;
		public BigDecimal CantidadTransIng;
		
		public ResultadoConsulta(){
			this.Cantidad= BigDecimal.ZERO;
			this.PrecioCompra= BigDecimal.ZERO;
			this.ValorCompra= BigDecimal.ZERO;
			this.MontoCompra= BigDecimal.ZERO;
			this.INGPrecioMercadoActual= BigDecimal.ZERO;
			this.INGTasaMercadoActual= BigDecimal.ZERO;
			this.ValorMercado= BigDecimal.ZERO;
			this.MontoMercado= BigDecimal.ZERO;
			this.INGPrecioPromedioCompra= BigDecimal.ZERO;
			this.INGMontoPrecioPromedioMonedaInstrumento= BigDecimal.ZERO;
			this.IdMonedaSistema= "";
			this.ValorPPP= BigDecimal.ZERO;
			this.MontoPPP= BigDecimal.ZERO;
			this.InteresCorrido= BigDecimal.ZERO;
			this.CantidadNominal= BigDecimal.ZERO;
			this.IdBoveda="";
			this.IdMonedaInstrReajuste="";
			this.IdMonedaEmision="";
			this.IdMonedaPrincipal="";
			this.EsNominal = "N";
			this.IdTipoOperacion ="";
			this.AfectaPresunto="N";
			this.RegimenTributario="";
			this.IdRegimenTributario="";
			this.AccionCustodia="";
			this.EsCuentaPropia="N";
			this.IdMatrizAgrupador=0L;
			this.EsADC="";
			this.CantidadPrecioCompra=BigDecimal.ZERO;
			this.MontoPrecioPromedio=BigDecimal.ZERO;
			this.MontoCompraCarteraOrigen=BigDecimal.ZERO;
			this.ValorFactorMonedaInstrumento=BigDecimal.ZERO;
			this.DividendoEnDinero=BigDecimal.ZERO;
		}
		
		public void copyValuesFrom(ResultadoConsulta obj) {
			this.IdClasificacionTipoInstrumento = obj.IdClasificacionTipoInstrumento;
			this.IdTipoInstrumento = obj.IdTipoInstrumento;
			this.IdInstrumento = obj.IdInstrumento;
			this.Nemotecnico = obj.Nemotecnico;
			this.IdEmisor = obj.IdEmisor;
			this.EsFungible = obj.EsFungible;
			this.IdCliente = obj.IdCliente;
			this.IdPersona = obj.IdPersona;
			this.IdPortafolio = obj.IdPortafolio;
			this.IdBoveda = obj.IdBoveda;
			this.IdCuentaCustodioPortafolio = obj.IdCuentaCustodioPortafolio;
			this.NumeroCuentaBoveda = obj.NumeroCuentaBoveda;
			this.Relacion = obj.Relacion;
			this.Cantidad = obj.Cantidad;
			this.CantidadNominal = obj.CantidadNominal;
			this.PrecioCompra = obj.PrecioCompra;
			this.ValorCompra = obj.ValorCompra;
			this.MontoCompra = obj.MontoCompra;
			this.INGPrecioMercadoActual = obj.INGPrecioMercadoActual;
			this.INGTasaMercadoActual = obj.INGTasaMercadoActual;
			this.ValorMercado = obj.ValorMercado;
			this.MontoMercado = obj.MontoMercado;
			this.INGPrecioPromedioCompra = obj.INGPrecioPromedioCompra;
			this.INGMontoPrecioPromedioMonedaInstrumento = obj.INGMontoPrecioPromedioMonedaInstrumento;
			this.IdMonedaSistema = obj.IdMonedaSistema;
			this.ValorPPP = obj.ValorPPP;
			this.MontoPPP = obj.MontoPPP;
			this.IdMonedaInstrReajuste = obj.IdMonedaInstrReajuste;
			this.IdMonedaEmision = obj.IdMonedaEmision;
			this.IdMonedaPrincipal = obj.IdMonedaPrincipal;
			this.InteresCorrido = obj.InteresCorrido;
			this.EsNominal = obj.EsNominal;
			this.IdTipoOperacion = obj.IdTipoOperacion;
			this.AfectaPresunto = obj.AfectaPresunto;
			this.RegimenTributario = obj.RegimenTributario;
			this.IdRegimenTributario = obj.IdRegimenTributario;
			this.EsVentaDescubierta = obj.EsVentaDescubierta;
			this.AccionCustodia = obj.AccionCustodia;
			this.EsCuentaPropia = obj.EsCuentaPropia;
			this.IdMatrizAgrupador = obj.IdMatrizAgrupador;
			this.EsADC = obj.EsADC;
			this.ValorFactorMonedaInstrumento = obj.ValorFactorMonedaInstrumento;
			this.RazonSocialInstrumento = obj.RazonSocialInstrumento;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientePortafolioInstrumento{

		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long   IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public String EsFungible;
		public Long   IdCliente;
		public Long   IdPortafolio;
		public String NombreCliente;      //NombreCompleto
		public String DocumentoIdentidad;
		public String DocumentoIdentidadRutCavali;  //DocumentoIdentidadAdicional
		public String IdBoveda;
		public Long   IdCuentaCustodioPortafolio;
		public String NumeroCuentaBoveda;
		public String Relacion;
		public String ClasificacionRiesgoInstrumento; //ValorClasificacionRiesgo
		public String RegimenTributario;
		public Long   IdMatrizAgrupador;
		public String EsADC;
		public String RazonSocialInstrumento;
		
		public ClientePortafolioInstrumento(){
			this.ClasificacionRiesgoInstrumento="";
			this.IdCuentaCustodioPortafolio=0L;
			this.DocumentoIdentidadRutCavali="";
			this.RegimenTributario="";
			this.IdMatrizAgrupador=0L;
			this.EsADC="";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	public static class CustodiaFondoMutuo{
		ResultadoConsulta Custodia;
		ResultadoConsulta CustodiaPresunto;
		ResultadoConsulta Garantia;
		ResultadoConsulta TransitoIngreso;
		ResultadoConsulta TransitoEgreso;
		ResultadoConsulta TransitoNominalIngreso;
		ResultadoConsulta TransitoNominalEgreso;
		ResultadoConsulta TransitoNominalIngresoPresunto;
		ResultadoConsulta TransitoNominalEgresoPresunto;
		
		public CustodiaFondoMutuo() {
			this.Custodia = new ResultadoConsulta();
			this.CustodiaPresunto = new ResultadoConsulta();
			this.Garantia = new ResultadoConsulta();
			this.TransitoIngreso = new ResultadoConsulta();
			this.TransitoEgreso = new ResultadoConsulta();
			this.TransitoNominalIngreso = new ResultadoConsulta();
			this.TransitoNominalEgreso = new ResultadoConsulta();
			this.TransitoNominalIngresoPresunto = new ResultadoConsulta();
			this.TransitoNominalEgresoPresunto = new ResultadoConsulta();
		}
		
		public String obtenerIdRegimenFondoMutuo() {
			if(esRegimenValido(this.Custodia.IdRegimenTributario)) {
				return this.Custodia.IdRegimenTributario;
			}
			if(esRegimenValido(this.CustodiaPresunto.IdRegimenTributario)) {
				return this.CustodiaPresunto.IdRegimenTributario;
			}
			if(esRegimenValido(this.Garantia.IdRegimenTributario)) {
				return this.Garantia.IdRegimenTributario;
			}
			if(esRegimenValido(this.TransitoIngreso.IdRegimenTributario)) {
				return this.TransitoIngreso.IdRegimenTributario;
			}
			if(esRegimenValido(this.TransitoEgreso.IdRegimenTributario)) {
				return this.TransitoEgreso.IdRegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalIngreso.IdRegimenTributario)) {
				return this.TransitoNominalIngreso.IdRegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalEgreso.IdRegimenTributario)) {
				return this.TransitoNominalEgreso.IdRegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalIngresoPresunto.IdRegimenTributario)) {
				return this.TransitoNominalIngresoPresunto.IdRegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalEgresoPresunto.IdRegimenTributario)) {
				return this.TransitoNominalEgresoPresunto.IdRegimenTributario;
			}
			
			return "";
		}
		
		public String obtenerRegimenFondoMutuo() {
			if(esRegimenValido(this.Custodia.RegimenTributario)) {
				return this.Custodia.RegimenTributario;
			}
			if(esRegimenValido(this.CustodiaPresunto.RegimenTributario)) {
				return this.CustodiaPresunto.RegimenTributario;
			}
			if(esRegimenValido(this.Garantia.RegimenTributario)) {
				return this.Garantia.RegimenTributario;
			}
			if(esRegimenValido(this.TransitoIngreso.RegimenTributario)) {
				return this.TransitoIngreso.RegimenTributario;
			}
			if(esRegimenValido(this.TransitoEgreso.RegimenTributario)) {
				return this.TransitoEgreso.RegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalIngreso.RegimenTributario)) {
				return this.TransitoNominalIngreso.RegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalEgreso.RegimenTributario)) {
				return this.TransitoNominalEgreso.RegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalIngresoPresunto.RegimenTributario)) {
				return this.TransitoNominalIngresoPresunto.RegimenTributario;
			}
			if(esRegimenValido(this.TransitoNominalEgresoPresunto.RegimenTributario)) {
				return this.TransitoNominalEgresoPresunto.RegimenTributario;
			}
			
			return "";
		}
		
		public Boolean esRegimenValido(String regimenFondoMutuo) {
			return regimenFondoMutuo != null && !regimenFondoMutuo.isEmpty();
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
		
	public Salida_ConsultarCustodiaInstrumentosPendientes logica (Connection con, Entrada_ConsultarCustodiaInstrumentosPendientes entrada) throws Exception {
		 
		logger.info("<<INICIO CONSULTA CUSTODIA INSTRUMENTOS PENDIENTES>>");
		SQL sql=new SQL();
		Salida_ConsultarCustodiaInstrumentosPendientes salida = new Salida_ConsultarCustodiaInstrumentosPendientes();
		logger.debug("INIT BUSQUEDA PARAMETROS SISTEMA");
		String v_FechaConsulta= "2300-12-31";
		String v_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String v_IdMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema"); 
		
		List<ProductoCustodia> listaProducto = sql.query(con, CONSULTA_PRODUCTOS_CUSTODIA, new HashMap<>(),ProductoCustodia.class);
		HashMap<Long, Object> HashProductos = Utils.List2Hash(listaProducto, "IdMatrizAgrupadorAPV");	
		
		
		//Si fecha viene vacia, asignar valor por defecto
		if(entrada.p_FechaConsulta.equals("")) {
			entrada.p_FechaConsulta = "0001-01-01";
		}
		
		//Si viene fecha por defecta Consulta Fecha sistema y asigna a la fecha consulta de la entrada
		//el valor de la fecha por defecto 

		if(entrada.p_FechaConsulta.equals("0001-01-01")){
			 v_FechaConsulta = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			 entrada.p_FechaConsulta = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		}else{
			v_FechaConsulta = entrada.p_FechaConsulta;
		}
				
		
		logger.debug("INIT BUSQUEDA DATOS CUENTA PROPIA");
		//Obtiene los datos de la corredora(para cuenta propia)
		HashMap<String,Object> InDatosCtaPropia = new HashMap<String,Object>();
		InDatosCtaPropia.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DatosCorredora> v_DatosCtaPropia = sql.query(con,DATOS_CTA_PROPIA_SELECT,InDatosCtaPropia,DatosCorredora.class);
		logger.debug("END BUSQUEDA DATOS CUENTA PROPIA");
		
		logger.debug("INIT BUSQUEDA DATOS CUENTA RELACIONADOS");
		//Obtiene los relacionados a la corredora
		HashMap<String,Object> InPersonaDestino = new HashMap<String,Object>();	
		InPersonaDestino.put("IdEmpresa", entrada.p_IdEmpresa);
		InPersonaDestino.put("IdPersonaCuentaPropia", v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
		List<PersonaDestino> v_tblRelacionados = sql.query(con,CTA_PROPIA_RELACIONADOS_SELECT,InPersonaDestino,PersonaDestino.class);
		HashMap<Object,Object> hashRelacionados = Utils.List2Hash(v_tblRelacionados, "IdPersonaDestino");		
		logger.debug("END BUSQUEDA DATOS CUENTA RELACIONADOS");
		
		
		//Tipos de conceptos y salidas de las busquedas		
		List<String> TipoBusqueda = Arrays.asList(TIPO_BUSQUEDAS);
		List<ResultadoConsulta> v_tblInst= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTrasnIng= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTrasnEgr= new ArrayList<ResultadoConsulta>();
		
		HashMap<String,Object> InBusquedas = new HashMap<String,Object>();
		InBusquedas.put("p_IdEmpresa", entrada.p_IdEmpresa);
		InBusquedas.put("p_IdCliente",entrada.p_IdCliente);
		InBusquedas.put("p_IdPortafolio",entrada.p_IdPortafolio);
		InBusquedas.put("p_IdClasificacionTipoInstrumento",entrada.p_IdClasificacionTipoInstrumento);
		InBusquedas.put("p_IdTipoInstrumento",entrada.p_IdTipoInstrumento);
		InBusquedas.put("p_IdInstrumento",entrada.p_IdInstrumento);
		InBusquedas.put("p_Nemotecnico",entrada.p_Nemotecnico);
		InBusquedas.put("v_FechaConsulta",v_FechaConsulta);
		InBusquedas.put("p_IdProductoAgrupador",entrada.p_IdProductoAgrupador);
		InBusquedas.put("v_IdClienteCuentaPropia",v_DatosCtaPropia.get(0).IdClienteCtaPropia);
		InBusquedas.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
		
		//busca los movimientos ingreso y egreso pendientes
		v_tblInst = sql.query(con,CONSULTA_MOV_TRANSITO_INGEGR_SELECT,InBusquedas,ResultadoConsulta.class);
		
		List<Long> InstrumentosPendientes = new ArrayList<Long>();
		List<Long> portafolios = new ArrayList<Long>();
	
		for(ResultadoConsulta v_tblTrasnIngEgr : v_tblInst){	
			if(!InstrumentosPendientes.contains(v_tblTrasnIngEgr.IdInstrumento)) InstrumentosPendientes.add(v_tblTrasnIngEgr.IdInstrumento);		
		}
		
		for(ResultadoConsulta v_tblTrasnIngEgr : v_tblInst){	
			if(!portafolios.contains(v_tblTrasnIngEgr.IdPortafolio)) portafolios.add(v_tblTrasnIngEgr.IdPortafolio);		
		}
	
		
		logger.debug("INIT AGREGAR SALIDA A LISTA"); 
		
		//consultar custodia 
		String v_IdBovedaDefecto="";
		//tablas de salidas de consultas
		List<ResultadoConsulta> v_tblCustodia= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblCustodiaPresunto = new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblVentaPacto= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblCompraPacto= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblSimulC= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblSimulV= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblSimulCTranNom= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblSimulVTranNom= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblPrestamos= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblEmprestitos= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblGarantias= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTrasnEgrVenDes= new ArrayList<ResultadoConsulta>();  //  Transitos de Egreso por Ventas Descubiertas
		List<ResultadoConsulta> v_tblTrasnEgrVenDesPresunto= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomIng= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomPresuntoIng = new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomEgr= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomPresuntoEgr = new ArrayList<ResultadoConsulta>();
		
		Long c = 0L;
		logger.debug("INIT BUSQUEDA DATOS MODULOS DEL SISTEMA");
		for(String Obj : TipoBusqueda){
			
			HashMap<String,Object> InBusqueda = new HashMap<String,Object>();
			InBusqueda.put("p_IdEmpresa", entrada.p_IdEmpresa);
			InBusqueda.put("p_AgrupaPorBoveda", entrada.p_AgrupaPorBoveda);
			InBusqueda.put("p_IdCliente",entrada.p_IdCliente);
			InBusqueda.put("p_IdPortafolio",portafolios);
			InBusqueda.put("p_IdClasificacionTipoInstrumento",entrada.p_IdClasificacionTipoInstrumento);
			InBusqueda.put("p_IdTipoInstrumento",entrada.p_IdTipoInstrumento);
			InBusqueda.put("p_IdInstrumento",InstrumentosPendientes);
			InBusqueda.put("p_Nemotecnico",entrada.p_Nemotecnico);
			InBusqueda.put("v_FechaConsulta",v_FechaConsulta);
			InBusqueda.put("p_IdProductoAgrupador",entrada.p_IdProductoAgrupador);
			InBusqueda.put("v_IdBovedaDefecto", v_IdBovedaDefecto);	
					
			
			if(!Obj.equals("TRA")){
				InBusqueda.put("p_TipoFechaConsulta",entrada.p_TipoFechaConsulta);
			}
			if(Obj.equals("VENPAC")||Obj.equals("COMPAC")|| Obj.equals("TRANOM")){
				InBusqueda.put("v_IdClienteCuentaPropia",v_DatosCtaPropia.get(0).IdClienteCtaPropia);
			}
			
			
			//Si existe parametro PLA.FechaGoLive evalua si la fecha ingresada es inferior al Go Live
			String consultarCustodiaHistorico    	= "0001-01-01";
			try
			{
				consultarCustodiaHistorico    	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaGoLive");
			}
			catch(Exception ex)
			{
				consultarCustodiaHistorico    	= "0001-01-01";
			}
			
			if(Obj.equals("CUS")){
				logger.debug("QUERY CUS");
				
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					v_tblCustodia =  sql.query(con,CONSULTA_CUSTODIA_SELECT,InBusqueda,ResultadoConsulta.class);	
					v_tblCustodia = procesaTablaResultado(entrada,v_tblCustodia,v_DatosCtaPropia,hashRelacionados,Obj);
					c += v_tblCustodia.size();
					
					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						InBusqueda.remove("p_TipoFechaConsulta");
						v_tblCustodiaPresunto =  sql.query(con,CONSULTA_CUSTODIA_PRESUNTO_SELECT,InBusqueda,ResultadoConsulta.class);
						v_tblCustodiaPresunto = procesaTablaResultado(entrada,v_tblCustodiaPresunto,v_DatosCtaPropia,hashRelacionados,Obj);
					}
				
				}else {
					
					if(!consultarCustodiaHistorico.equals("0001-01-01") && !consultarCustodiaHistorico.equals("") && (Fecha.toDate(consultarCustodiaHistorico).after(Fecha.toDate(entrada.p_FechaConsulta)))){
						//Consulta Custodia Historica previa a Go Live
						logger.debug("INIT OBTENER HISTORICO GO LIVE");
						HashMap<String,Object> params = new HashMap<String,Object>();
						params.put("p_IdEmpresa", entrada.p_IdEmpresa);
						params.put("p_CodPer", entrada.p_FechaConsulta.substring(0,7));
						params.put("p_IdCliente", entrada.p_IdCliente);
						params.put("p_IdPortafolio", portafolios);
						params.put("p_IdClienteCuentaPropia",v_DatosCtaPropia.get(0).IdClienteCtaPropia);
						params.put("p_IdInstrumento", InstrumentosPendientes);
						params.put("p_Nemotecnico", entrada.p_Nemotecnico);
						params.put("p_IdTipoInstrumento",entrada.p_IdTipoInstrumento);
						params.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);		
						params.put("p_IdProductoAgrupador",entrada.p_IdProductoAgrupador);
						List<Tabla_Salida> out_tbl = sql.query(con,CONSULTA_CUSTODIA_HISTORICO,params,Tabla_Salida.class);
						salida.P_OUT_TBL.addAll(out_tbl);
						params.clear();
						logger.debug("END OBTENER HISTORICO GO LIVE");					
						
					}
				
					else {
						v_tblCustodia = sql.query(con,CONSULTA_CUSTODIA_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
						logger.debug(v_tblCustodia.size());
						v_tblCustodia = procesaTablaResultado(entrada,v_tblCustodia,v_DatosCtaPropia,hashRelacionados,Obj);
						logger.debug(v_tblCustodia.size());
						c += v_tblCustodia.size();
						
					}
					
					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						InBusqueda.remove("p_TipoFechaConsulta");
						v_tblCustodiaPresunto = sql.query(con,CONSULTA_CUSTODIA_PRESUNTO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);		
						v_tblCustodiaPresunto = procesaTablaResultado(entrada,v_tblCustodiaPresunto,v_DatosCtaPropia,hashRelacionados,Obj);				
						
					}
				}
				
			}
			//BUSQUEDA INGRESOS POR VENTAS NOMINALES
				if(Obj.equals("TRANOM")){
					
					List<ResultadoConsulta> res = new ArrayList<ResultadoConsulta>();
					List<ResultadoConsulta> resPresunto = new ArrayList<ResultadoConsulta>();
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
							res = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_SELECT,InBusqueda,ResultadoConsulta.class);
						}else {
							res = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_SELECT,InBusqueda,ResultadoConsulta.class);
						}
						
						if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
							if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
								InBusqueda.remove("p_TipoFechaConsulta");
								InBusqueda.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
								resPresunto = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_SELECT,InBusqueda,ResultadoConsulta.class);
							}else {
								InBusqueda.remove("p_TipoFechaConsulta");
								InBusqueda.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
								resPresunto = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_SELECT,InBusqueda,ResultadoConsulta.class);
							}
						}
						
					}else{
						if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
							res = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
						}
						else {
							res = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
						}
						
						if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
							if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
								InBusqueda.remove("p_TipoFechaConsulta");
								InBusqueda.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
								resPresunto = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
							}else {
								InBusqueda.remove("p_TipoFechaConsulta");
								InBusqueda.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
								resPresunto = sql.query(con,CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
							}
						}
						
					}
	
					logger.debug("TRANOM: TERMINO");
					logger.debug(res.size());
					res = procesaTablaResultado(entrada,res,v_DatosCtaPropia,hashRelacionados,Obj);
					
					for(ResultadoConsulta tran : res) {
						
						if(tran.EsCuentaPropia.equals("N")) {
							if(tran.IdTipoOperacion.equals("VENPAC") || tran.IdTipoOperacion.equals("VENTER")) {
								tran.AccionCustodia="INGRESO";
							}else if (tran.IdTipoOperacion.equals("COMPAC") || tran.IdTipoOperacion.equals("COMTER")){
								tran.AccionCustodia="RETIRO";
							}
						}
						
						if(tran.AccionCustodia.equals("RETIRO")) {
							v_tblTranomIng.add(tran);
							//se agrega los transitos de simultaneas para nominales
							if (tran.EsNominal.equalsIgnoreCase("S") && tran.IdTipoOperacion.equalsIgnoreCase("VENSIM")){
								v_tblSimulCTranNom.add(tran);
							}
						}else if(tran.AccionCustodia.equals("INGRESO")) {
							v_tblTranomEgr.add(tran);
							//se agrega los transitos de simultaneas para nominales
							if (tran.EsNominal.equalsIgnoreCase("S") && tran.IdTipoOperacion.equalsIgnoreCase("COMSIM")){							
								v_tblSimulVTranNom.add(tran);
							}
						}
					}
					
					//resPresunto = procesaTablaResultado(entrada,resPresunto,v_DatosCtaPropia,hashRelacionados,Obj);
					for(ResultadoConsulta tran : resPresunto) {
						
						if(tran.EsCuentaPropia.equals("N")) {
							if(tran.IdTipoOperacion.equals("VENPAC") || tran.IdTipoOperacion.equals("VENTER")) {
								tran.AccionCustodia="INGRESO";
								
							}else if (tran.IdTipoOperacion.equals("COMPAC") || tran.IdTipoOperacion.equals("COMTER")){
								tran.AccionCustodia="RETIRO";
								
							}
						}
						
						if(tran.AccionCustodia.equals("RETIRO")) {
							v_tblTranomPresuntoIng.add(tran);
						}else if(tran.AccionCustodia.equals("INGRESO")) {
							v_tblTranomPresuntoEgr.add(tran);
						}
					}
	
					logger.debug(v_tblTranomIng.size());
					logger.debug(v_tblTranomEgr.size());
					c += v_tblTranomEgr.size();
					c += v_tblTranomIng.size();
				}
				
				
				//BUSQUEDA INGRESO/EGRESO TRANSITO con custodia
				if(Obj.equals("TRA")){
					List<ResultadoConsulta> res = null;
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						res = sql.query(con,CONSULTA_ING_TRANSITO_SELECT,InBusqueda,ResultadoConsulta.class);	
						if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
							InBusqueda.remove("p_TipoFechaConsulta");
							InBusqueda.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
							InBusqueda.put("v_IdClienteCuentaPropia",v_DatosCtaPropia.get(0).IdClienteCtaPropia);
							v_tblTrasnEgrVenDesPresunto = sql.query(con,CONSULTA_VENTA_DESCUBIERTA_PRESUNTO_SELECT,InBusqueda,ResultadoConsulta.class);
						}
					}else {
						res = sql.query(con,CONSULTA_ING_TRANSITO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
						if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
							InBusqueda.remove("p_TipoFechaConsulta");
							InBusqueda.put("v_IdPersonaCuentaPropia",v_DatosCtaPropia.get(0).IdPersonaCtaPropia);
							InBusqueda.put("v_IdClienteCuentaPropia",v_DatosCtaPropia.get(0).IdClienteCtaPropia);
							v_tblTrasnEgrVenDesPresunto = sql.query(con,CONSULTA_VENTA_DESCUBIERTA_PRESUNTO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
						}
					}
					 
					logger.debug("TRA: TERMINO");
					logger.debug(res.size());
					res = procesaTablaResultado(entrada,res,v_DatosCtaPropia,hashRelacionados,Obj);
					for(ResultadoConsulta tra : res) {
						if(tra.IdTipoOperacion.equals("INGTIT")) {
							v_tblTrasnIng.add(tra);
						}else {
							// Separa los movimientos que son por venta descubierta de los que no
							if (tra.EsVentaDescubierta.equalsIgnoreCase("S"))
								v_tblTrasnEgrVenDes.add(tra);
							else
								v_tblTrasnEgr.add(tra);
						}
					}
					logger.debug(v_tblTrasnEgr.size());
					logger.debug(v_tblTrasnIng.size());
					c += v_tblTrasnEgr.size();
					c += v_tblTrasnIng.size();
				}
				
				//BUSQUEDA GARANTIA
				if(Obj.equals("GTA")){
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						v_tblGarantias = sql.query(con,CONSULTA_GARANTIA_SELECT,InBusqueda,ResultadoConsulta.class);
					}else{
						v_tblGarantias = sql.query(con,CONSULTA_GARANTIA_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
					}							
					
					logger.debug("GTA: TERMINO");
					logger.debug(v_tblGarantias.size());
					v_tblGarantias = procesaTablaResultado(entrada,v_tblGarantias,v_DatosCtaPropia,hashRelacionados,Obj);
					logger.debug(v_tblGarantias.size());
					c += v_tblGarantias.size();
				}
				
				//BUSQUEDA PRESTAMOS 
				if(Obj.equals("PRT")){
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						v_tblPrestamos = sql.query(con,CONSULTA_PRESTAMOS_SELECT,InBusqueda,ResultadoConsulta.class);
					}else{
						v_tblPrestamos = sql.query(con,CONSULTA_PRESTAMOS_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
					}			
									
					logger.debug("PRT: TERMINO");
					logger.debug(v_tblPrestamos.size());
					v_tblPrestamos = procesaTablaResultado(entrada,v_tblPrestamos,v_DatosCtaPropia,hashRelacionados,Obj);
					logger.debug(v_tblPrestamos.size());
					c += v_tblPrestamos.size();
				}
				
				//BUSQUEDA EMPRESTITOS
				if(Obj.equals("EMP")){
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						v_tblEmprestitos = sql.query(con,CONSULTA_EMPRESTITOS_SELECT,InBusqueda,ResultadoConsulta.class);	
					}else{
						v_tblEmprestitos = sql.query(con,CONSULTA_EMPRESTITOS_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
					}				
					
					logger.debug("EMP: TERMINO" );
					logger.debug(v_tblEmprestitos.size());
					v_tblEmprestitos = procesaTablaResultado(entrada,v_tblEmprestitos,v_DatosCtaPropia,hashRelacionados,Obj);
					logger.debug(v_tblEmprestitos.size());
					c += v_tblEmprestitos.size();
				}
				
				//BUSQUEDA VENTA PACTO
				if(Obj.equals("VENPAC")){
					
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						v_tblVentaPacto = sql.query(con,CONSULTA_VENTA_PACTO_SELECT,InBusqueda,ResultadoConsulta.class);	
					}else{
						v_tblVentaPacto = sql.query(con,CONSULTA_VENTA_PACTO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
					}				
					
					logger.debug("VENPAC: TERMINO");
					logger.debug(v_tblVentaPacto.size());
					v_tblVentaPacto = procesaTablaResultado(entrada,v_tblVentaPacto,v_DatosCtaPropia,hashRelacionados,Obj);				
					logger.debug(v_tblVentaPacto.size());
					c += v_tblVentaPacto.size();
				}
				//BUSQUEDA COMPRA PACTO
				if(Obj.equals("COMPAC")){
					
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						v_tblCompraPacto = sql.query(con,CONSULTA_COMPRA_PACTO_SELECT,InBusqueda,ResultadoConsulta.class);
					}else{
						v_tblCompraPacto = sql.query(con,CONSULTA_COMPRA_PACTO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
					}
					
					logger.debug("COMPAC: TERMINO");
					logger.debug(v_tblCompraPacto.size());
					v_tblCompraPacto = procesaTablaResultado(entrada,v_tblCompraPacto,v_DatosCtaPropia,hashRelacionados,Obj);
					logger.debug(v_tblCompraPacto.size());
					c += v_tblCompraPacto.size();
				}
				
				//BUSQUEDA COMPRA y VENTA SIMULTANEA
				if(Obj.equals("SIM")){
					List<ResultadoConsulta> res = new ArrayList<ResultadoConsulta>();
					//se unieron las consultas de compra y venta en una.
					if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
						res = sql.query(con,CONSULTA_COMPRA_VENTA_SIMULTANEA_SELECT,InBusqueda,ResultadoConsulta.class);
					}else {
						res = sql.query(con,CONSULTA_COMPRA_VENTA_SIMULTANEA_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
					}
					logger.debug("SIM: TERMINO");
					logger.debug(res.size());
					res = procesaTablaResultado(entrada,res,v_DatosCtaPropia,hashRelacionados,Obj);
					for(ResultadoConsulta sim : res) {
						if(sim.IdTipoOperacion.equals("VENSIM")) {
							v_tblSimulC.add(sim);//Segun los cambios de la revision 42560 SVNQA Carga las ventas aqui
						}else {
							v_tblSimulV.add(sim);
						}
					}

					logger.debug(v_tblSimulC.size());
					logger.debug(v_tblSimulV.size());
					c += v_tblSimulC.size();
					c += v_tblSimulV.size();
				}
				
		}
		

		List<ResultadoConsulta> v_tblConsulta1 = new ArrayList<>();
		v_tblConsulta1.addAll(v_tblInst);
		
		HashMap<Long,Boolean> hashIdClientes = new HashMap<Long,Boolean>();
		HashMap<Long,Boolean> hashIdPortafolios = new HashMap<Long,Boolean>();
		HashMap<Long,Boolean> hashIdInstrumentos = new HashMap<Long,Boolean>();
		logger.debug("INIT SEPARAR IDS");
		for(ResultadoConsulta resultaConsulta : v_tblConsulta1){
			if (hashIdClientes.get(resultaConsulta.IdCliente) == null) hashIdClientes.put(resultaConsulta.IdCliente,true);
			if (hashIdPortafolios.get(resultaConsulta.IdPortafolio) == null) hashIdPortafolios.put(resultaConsulta.IdPortafolio,true);
			if (hashIdInstrumentos.get(resultaConsulta.IdInstrumento)  == null) hashIdInstrumentos.put(resultaConsulta.IdInstrumento,true);
		}
		List<Long> idClientes = new ArrayList<Long>(hashIdClientes.keySet()); 
		List<Long> idPortafolios = new ArrayList<Long>(hashIdPortafolios.keySet()); 
		List<Long> idInstrumentos = new ArrayList<Long>(hashIdInstrumentos.keySet());
		logger.debug("END SEPARAR IDS");
		
		HashMap<Long,Object> hClientes = new HashMap<>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdCliente", idClientes);
		List<Cliente> listaClientes = sql.query(con,CONSULTA_CLIENTES_SELECT,params,Cliente.class);
		hClientes = Utils.List2Hash(listaClientes, "IdCliente");
		params.clear();
		logger.debug("END OBTENER CLIENTES");
		
		logger.debug("INIT OBTENER INSTRUMENTOS RIESGO");
		params.put("IdInstrumento", idInstrumentos);
		List<InstrumentoClasificacionRiesgo> listaInsfactosRiesgo = sql.query(con,CONSULTA_INSTRUMENTO_RIESGO_SELECT,params,InstrumentoClasificacionRiesgo.class);
		HashMap<Long,Object> hlistaInsfactosRiesgo = Utils.List2Hash(listaInsfactosRiesgo, "IdInstrumento");
		params.clear();
		logger.debug("END OBTENER INSTRUMENTOS RIESGO");
		
		logger.debug("INIT GENERAR LISTA CLIENTE INSTRUMENTO"); 
		HashMap<String, ResultadoConsulta> TablaConsultas = new HashMap<String, ResultadoConsulta>();
		List<ClientePortafolioInstrumento> listaClienteInstrumento = new ArrayList<>();
		
		
		List<ResultadoConsulta> v_tblConsulta = new ArrayList<>();
		v_tblConsulta.addAll(v_tblCustodia);
		v_tblConsulta.addAll(v_tblVentaPacto);
		v_tblConsulta.addAll(v_tblCompraPacto);
		v_tblConsulta.addAll(v_tblSimulC);
		v_tblConsulta.addAll(v_tblSimulV);
		v_tblConsulta.addAll(v_tblPrestamos);
		v_tblConsulta.addAll(v_tblEmprestitos);
		v_tblConsulta.addAll(v_tblGarantias);
		v_tblConsulta.addAll(v_tblTrasnIng);
		v_tblConsulta.addAll(v_tblTrasnEgr);
		v_tblConsulta.addAll(v_tblTrasnEgrVenDes); // Transito de egresos por ventas descubiertas
		v_tblConsulta.addAll(v_tblTranomIng);
		v_tblConsulta.addAll(v_tblTranomEgr);
		v_tblConsulta.addAll(v_tblCustodiaPresunto);
		v_tblConsulta.addAll(v_tblTrasnEgrVenDesPresunto);
		v_tblConsulta.addAll(v_tblTranomPresuntoIng);
		v_tblConsulta.addAll(v_tblTranomPresuntoEgr);
		logger.debug("Agrupadas :"+v_tblConsulta.size());
		
		
		for(ResultadoConsulta obj : v_tblConsulta1){
			String keyObj = obj.IdClasificacionTipoInstrumento+"_"+obj.IdTipoInstrumento+"_"+obj.IdInstrumento+"_"+obj.Nemotecnico+"_"+obj.IdEmisor+"_"+obj.EsFungible+"_"+
							obj.IdCliente+"_"+obj.IdPortafolio+"_"+obj.IdBoveda+"_"+obj.IdCuentaCustodioPortafolio+obj.Relacion + "_" + obj.RegimenTributario +
							"_" + obj.IdMatrizAgrupador + "_" + obj.EsADC;
			
			
			if(TablaConsultas.get(keyObj)==null){
				
				Cliente cliente = (Cliente) hClientes.get(obj.IdCliente);
				InstrumentoClasificacionRiesgo instrumento = (InstrumentoClasificacionRiesgo) hlistaInsfactosRiesgo.get(obj.IdInstrumento);
				TablaConsultas.put(keyObj, obj);
				//copiar valores
				ClientePortafolioInstrumento tmpCliInst =new  ClientePortafolioInstrumento();
				tmpCliInst.IdClasificacionTipoInstrumento = obj.IdClasificacionTipoInstrumento;
				tmpCliInst.IdTipoInstrumento = obj.IdTipoInstrumento;
				tmpCliInst.IdInstrumento = obj.IdInstrumento;
				tmpCliInst.Nemotecnico = obj.Nemotecnico;
				tmpCliInst.IdEmisor = obj.IdEmisor;
				tmpCliInst.EsFungible = obj.EsFungible;
				tmpCliInst.IdCliente = obj.IdCliente;
				tmpCliInst.IdPortafolio = obj.IdPortafolio;
				tmpCliInst.IdBoveda = obj.IdBoveda;
				tmpCliInst.IdCuentaCustodioPortafolio = obj.IdCuentaCustodioPortafolio;
				tmpCliInst.NumeroCuentaBoveda = obj.NumeroCuentaBoveda;
				tmpCliInst.Relacion = obj.Relacion;
				tmpCliInst.RegimenTributario = obj.RegimenTributario;
				tmpCliInst.IdMatrizAgrupador = obj.IdMatrizAgrupador;
				tmpCliInst.EsADC = obj.EsADC;
				tmpCliInst.RazonSocialInstrumento = obj.RazonSocialInstrumento;
				if(cliente != null){
					tmpCliInst.NombreCliente = cliente.NombreCompleto;
					tmpCliInst.DocumentoIdentidad= cliente.DocumentoIdentidad;
					tmpCliInst.DocumentoIdentidadRutCavali= cliente.DocumentoIdentidadAdicional!=null?cliente.DocumentoIdentidadAdicional:"";
					if(instrumento != null){
						tmpCliInst.ClasificacionRiesgoInstrumento=instrumento!=null? instrumento.ValorClasificacionRiesgo:"";
					}
					listaClienteInstrumento.add(tmpCliInst);
				}
				
			}
		}
		logger.debug("END GENERAR LISTA CLIENTE INSTRUMENTO"); 
		// FIN ---INSTRUMENTOS QUE EL PORTAFOLIO TIENE CUSTODIA
		params.clear();
		List<CuentaCustodio> ListaCuentaCustodio = new ArrayList<>();
		
		params.put("IdPortafolio", idPortafolios);
		ListaCuentaCustodio = sql.query(con,CONSULTA_CUENTA_CUSTODIO_SELECT, params,CuentaCustodio.class);
		logger.debug("END OBTENER CUENTA CUSTODIOS"); 
		
		HashMap<String, Object> HashCuentaCustodio = Utils.List2Hash(ListaCuentaCustodio, "Id");
		
		//PREPARACION SALIDA
		//tipo es el tipo key que necesitan keyC keyP
		HashMap<String,ResultadoConsulta> v_tblCustodiaHash= TransformarAHash(v_tblCustodia,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblCustodiaPresuntoHash= TransformarAHash(v_tblCustodiaPresunto,"keyC",HashCuentaCustodio);
		//MSH
		HashMap<String,ResultadoConsulta> v_tblVentaPactoHash= TransformarAHash(v_tblVentaPacto,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblCompraPactoHash= TransformarAHash(v_tblCompraPacto,"keyC",HashCuentaCustodio);
		
		HashMap<String,ResultadoConsulta> v_tblSimulCHash= TransformarAHash(v_tblSimulC,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblSimulVHash= TransformarAHash(v_tblSimulV,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblPrestamosHash= TransformarAHash(v_tblPrestamos,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblEmprestitosHash= TransformarAHash(v_tblEmprestitos,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblGarantiasHash= TransformarAHash(v_tblGarantias,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblTrasnIngHash= TransformarAHash(v_tblTrasnIng,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblTrasnEgrHash= TransformarAHash(v_tblTrasnEgr,"keyP",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblTrasnEgrVenDesHash= TransformarAHash(v_tblTrasnEgrVenDes,"keyP",HashCuentaCustodio);  //Transito Egresos por Venta Descubierta
		HashMap<String,ResultadoConsulta> v_tblTrasnEgrVenDesPresuntoHash= TransformarAHash(v_tblTrasnEgrVenDesPresunto,"keyP",HashCuentaCustodio);  //Transito Egresos por Venta Descubierta Presunto
		HashMap<String,ResultadoConsulta> v_tblTranomIngHash= TransformarAHash(v_tblTranomIng,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblTranomEgrHash= TransformarAHash(v_tblTranomEgr,"keyP",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblTranomPresuntoIngHash= TransformarAHash(v_tblTranomPresuntoIng,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblTranomPresuntoEgrHash= TransformarAHash(v_tblTranomPresuntoEgr,"keyP",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblInstHash = TransformarAHash(v_tblInst,"keyC",HashCuentaCustodio);
		
		HashMap<String,ResultadoConsulta> v_tblSimulCTraNomHash= TransformarAHash(v_tblSimulCTranNom,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblSimulVTraNomHash= TransformarAHash(v_tblSimulVTranNom,"keyC",HashCuentaCustodio);
		
		
		
		//Hash y Lista Instrumentos
				params.clear();
				logger.debug("INIT OBTENER INSTRUMENTOS"); 
				params.put("IdInstrumento", idInstrumentos);
				List<Instrumento> ListaInstrumentos = sql.query(con,CONSULTA_INSTRUMENTO_SELECT, params,Instrumento.class);
				HashMap<Long, Object> HashInstrumento = Utils.List2Hash(ListaInstrumentos, "IdInstrumento");
				logger.debug("END OBTENER INSTRUMENTOS");  
				
				//Hash y Lista Portafolio
				logger.debug("INIT OBTENER PORTAFOLIOS"); 
				params.clear();
				List<Portafolio> ListaPortafolios = new ArrayList<>();
				params.put("IdPortafolio", idPortafolios);
				ListaPortafolios = sql.query(con,CONSULTA_PORTAFOLIO_SELECT, params,Portafolio.class);
		
				HashMap<Long, Object> HashPortafolios = Utils.List2Hash(ListaPortafolios, "IdPortafolio");
				logger.debug("END OBTENER PORTAFOLIOS"); 
				
				//Hash y Lista MatrizAgrupador
				List<MatrizAgrupadorAPV> ListaMatrizAgrupadorAPV = sql.query(con,CONSULTA_MATRIZ_AGRUPADOR_APV_SELECT, new HashMap<>(),MatrizAgrupadorAPV.class);
				
				HashMap<Long, Object> HashMatrizAgrupadorAPV = Utils.List2Hash(ListaMatrizAgrupadorAPV, "IdMatrizAgrupadorAPV");
				
				//Hash y Lista ProdutoAgrupador
				List<ProductoAgrupador> ListaProdutoAgrupador = sql.query(con,CONSULTA_PRODUCTO_AGRUPADOR_SELECT, new HashMap<>(),ProductoAgrupador.class);
				
				HashMap<Long, Object> HashProdutoAgrupador = Utils.List2Hash(ListaProdutoAgrupador, "IdProductoAgrupador");
				
				
				//Hash y Lista cta custodia
				logger.debug("INIT OBTENER CUENTA CUSTODIOS"); 
				
				
				List<Tabla_Salida> v_SalidaPrevia= new ArrayList <>();
				HashMap<String, Tabla_Salida> HashSalidaPrevia = new HashMap<String, Tabla_Salida>();
				
				List<Tabla_Salida> listaSalida = new ArrayList<Tabla_Salida>();
				
				logger.debug("INIT CONSTRUIR PRE SALIDA"); 
				for (ClientePortafolioInstrumento ins: listaClienteInstrumento){
					//Objetos
					//ResultadoConsulta resCon= new ResultadoConsulta();
					Tabla_Salida salidaPrevia = new Tabla_Salida ();
					Instrumento ObjInst = new Instrumento();
					Portafolio ObjPort = new Portafolio();
					ProductoAgrupador ObjPro = new ProductoAgrupador();
					MatrizAgrupadorAPV ObjMat= new MatrizAgrupadorAPV();
					CuentaCustodio ObjCtaCus= new CuentaCustodio();
					ResultadoConsulta ObjtblCustodia = new ResultadoConsulta();
					ResultadoConsulta ObjtblCustodiaPresunto = new ResultadoConsulta();
					ResultadoConsulta ObjtblVentaPacto = new ResultadoConsulta();
					ResultadoConsulta ObjtblCompraPacto = new ResultadoConsulta();
					ResultadoConsulta ObjtblSimulC = new ResultadoConsulta();
					ResultadoConsulta ObjtblSimulV = new ResultadoConsulta();
					ResultadoConsulta ObjtblPrestamos = new ResultadoConsulta();
					ResultadoConsulta ObjtblEmprestitos = new ResultadoConsulta();
					ResultadoConsulta ObjtblGarantias = new ResultadoConsulta();
					ResultadoConsulta ObjtblTrasnIng = new ResultadoConsulta();
					ResultadoConsulta ObjtblTrasnEgr = new ResultadoConsulta();
					ResultadoConsulta ObjtblTrasnEgrVenDes = new ResultadoConsulta();
					ResultadoConsulta ObjtblTrasnEgrVenDesPresunto = new ResultadoConsulta();
					ResultadoConsulta ObjtblTranomIng = new ResultadoConsulta();
					ResultadoConsulta ObjtblTranomEgr = new ResultadoConsulta();
					ResultadoConsulta ObjtblTranomPresuntoIng = new ResultadoConsulta();
					ResultadoConsulta ObjtblTranomPresuntoEgr = new ResultadoConsulta();
					ResultadoConsulta ObjtblReporteCompra = new ResultadoConsulta();
					ResultadoConsulta ObjtblReporteVenta = new ResultadoConsulta();	
					ResultadoConsulta ObjtblIngEgr = new ResultadoConsulta();	
					
					ResultadoConsulta ObjtblSimulCTraNom = new ResultadoConsulta();
					ResultadoConsulta ObjtblSimulVTraNom = new ResultadoConsulta();
					ResultadoConsulta ObjtblSub = new ResultadoConsulta();
					CustodiaFondoMutuo custodiaFondoMutuo = new CustodiaFondoMutuo();
					
					boolean ObjValido=false;
					
					if(HashInstrumento.get(ins.IdInstrumento)!=null && HashPortafolios.get(ins.IdPortafolio)!=null){
						ObjCtaCus= HashCuentaCustodio.get(ins.IdPortafolio+ins.IdBoveda)!=null?(CuentaCustodio) HashCuentaCustodio.get(ins.IdPortafolio+ins.IdBoveda):new CuentaCustodio();
						
						String keycus =ins.IdClasificacionTipoInstrumento+ins.IdTipoInstrumento+ins.IdInstrumento+ins.Nemotecnico+ins.EsFungible+ins.IdCliente+ins.IdPortafolio+
							    ins.IdBoveda+ins.Relacion+ObjCtaCus.IdCuentaCustodioPortafolio+ins.RegimenTributario+ins.IdMatrizAgrupador+ins.EsADC;
					
						String key=ins.IdClasificacionTipoInstrumento+ins.IdTipoInstrumento+ins.IdInstrumento+ins.EsFungible+ins.IdCliente+ins.IdPortafolio+
								ins.IdBoveda+ins.Relacion+ObjCtaCus.IdCuentaCustodioPortafolio+ins.RegimenTributario+ins.IdMatrizAgrupador+ins.EsADC;
						
						ObjInst= (Instrumento) HashInstrumento.get(ins.IdInstrumento);
						ObjPort= (Portafolio) HashPortafolios.get(ins.IdPortafolio);
							
						
						 ObjtblCustodia		  = v_tblCustodiaHash.get(keycus) !=null?v_tblCustodiaHash.get(keycus):new ResultadoConsulta();
						 ObjtblCustodiaPresunto = v_tblCustodiaPresuntoHash.get(keycus) !=null?v_tblCustodiaPresuntoHash.get(keycus):new ResultadoConsulta();

						
						 ObjtblVentaPacto 	  = v_tblVentaPactoHash.get(keycus)  		!=null?v_tblVentaPactoHash.get(keycus):new ResultadoConsulta();
						 ObjtblCompraPacto 	  = v_tblCompraPactoHash.get(keycus) 		!=null?v_tblCompraPactoHash.get(keycus):new ResultadoConsulta();
						 
						 ObjtblSimulC 		  = v_tblSimulCHash.get(keycus)   		!=null?v_tblSimulCHash.get(keycus):new ResultadoConsulta();
						 ObjtblSimulV 		  = v_tblSimulVHash.get(keycus)   		!=null?v_tblSimulVHash.get(keycus):new ResultadoConsulta();
						 ObjtblPrestamos 	  = v_tblPrestamosHash.get(keycus)		!=null?v_tblPrestamosHash.get(keycus):new ResultadoConsulta();
						 ObjtblEmprestitos 	  = v_tblEmprestitosHash.get(keycus)	!=null?v_tblEmprestitosHash.get(keycus):new ResultadoConsulta();
						 ObjtblGarantias	  = v_tblGarantiasHash.get(keycus) 		!=null?v_tblGarantiasHash.get(keycus):new ResultadoConsulta();
						 ObjtblTrasnIng 	  = v_tblTrasnIngHash.get(keycus)  		!=null?v_tblTrasnIngHash.get(keycus):new ResultadoConsulta();
						 ObjtblTrasnEgr 	  = v_tblTrasnEgrHash.get(key)	   		!=null?v_tblTrasnEgrHash.get(key):new ResultadoConsulta();
						 ObjtblTrasnEgrVenDes = v_tblTrasnEgrVenDesHash.get(key)	!=null?v_tblTrasnEgrVenDesHash.get(key):new ResultadoConsulta(); // Transitos egresos por ventas decubiertas
						 ObjtblTrasnEgrVenDesPresunto = v_tblTrasnEgrVenDesPresuntoHash.get(key)!=null?v_tblTrasnEgrVenDesPresuntoHash.get(key):new ResultadoConsulta(); // Transitos egresos por ventas decubiertas Presunto
						 ObjtblTranomIng 	  = v_tblTranomIngHash.get(keycus)  	!=null?v_tblTranomIngHash.get(keycus):new ResultadoConsulta();
						 ObjtblTranomEgr 	  = v_tblTranomEgrHash.get(key)	   		!=null?v_tblTranomEgrHash.get(key):new ResultadoConsulta();
						 ObjtblTranomPresuntoIng = v_tblTranomPresuntoIngHash.get(keycus) !=null? v_tblTranomPresuntoIngHash.get(keycus):new ResultadoConsulta();
						 ObjtblTranomPresuntoEgr = v_tblTranomPresuntoEgrHash.get(key) !=null? v_tblTranomPresuntoEgrHash.get(key):new ResultadoConsulta();	 
						 ObjtblSimulCTraNom   = v_tblSimulCTraNomHash.get(keycus) 	!=null?v_tblSimulCTraNomHash.get(keycus):new ResultadoConsulta();
						 ObjtblSimulVTraNom   = v_tblSimulVTraNomHash.get(keycus) 	!=null?v_tblSimulVTraNomHash.get(keycus):new ResultadoConsulta();
						 ObjtblIngEgr         = v_tblInstHash.get(keycus)           !=null?v_tblInstHash.get(keycus):new ResultadoConsulta();
						 
						 ObjMat= HashMatrizAgrupadorAPV.get(ObjPort.IdMatrizAgrupadorAPV)!=null? (MatrizAgrupadorAPV) HashMatrizAgrupadorAPV.get(ObjPort.IdMatrizAgrupadorAPV):new MatrizAgrupadorAPV();
						 ObjPro= HashProdutoAgrupador.get(ObjMat.IdProductoAgrupador)!=null?(ProductoAgrupador) HashProdutoAgrupador.get(ObjMat.IdProductoAgrupador):new ProductoAgrupador();
						 
						 if("FM".equals(ObjInst.IdClasificacionTipoInstrumento)) {
							 custodiaFondoMutuo.Custodia = ObjtblCustodia;
							 custodiaFondoMutuo.CustodiaPresunto = ObjtblCustodiaPresunto;
							 custodiaFondoMutuo.Garantia = ObjtblGarantias;
							 custodiaFondoMutuo.TransitoIngreso = ObjtblTrasnIng;
							 custodiaFondoMutuo.TransitoEgreso = ObjtblTrasnEgr;
							 custodiaFondoMutuo.TransitoNominalIngreso = ObjtblTranomIng;
							 custodiaFondoMutuo.TransitoNominalEgreso = ObjtblTranomEgr;
							 custodiaFondoMutuo.TransitoNominalIngresoPresunto = ObjtblTranomPresuntoIng;
							 custodiaFondoMutuo.TransitoNominalEgresoPresunto = ObjtblTranomPresuntoEgr;
						 }
						 
						 ObjValido = true;
					}
						 if(ObjValido){
								//logger.debug("cartera valida");
								salidaPrevia.IdEmpresa=entrada.p_IdEmpresa;
								salidaPrevia.TipoFechaConsulta= entrada.p_TipoFechaConsulta;
								salidaPrevia.FechaCustodia= v_FechaConsulta;
								salidaPrevia.IdClasificacionTipoInstrumento=ObjInst.IdClasificacionTipoInstrumento;
								salidaPrevia.IdTipoInstrumento=ins.IdTipoInstrumento;
								salidaPrevia.NombreTipoInstrumento=ObjInst.NombreTipoInstrumento;
								salidaPrevia.ISIN=ObjInst.ISIN;
								salidaPrevia.IdInstrumento= ins.IdInstrumento;
								salidaPrevia.ClasificacionRiesgoInstrumento=ins.ClasificacionRiesgoInstrumento;
								salidaPrevia.Nemotecnico=ins.Nemotecnico;
								salidaPrevia.IdEmisor=ins.IdEmisor.equals("")?ObjInst.IdEmisor:ins.IdEmisor;
								salidaPrevia.EsFungible=ins.EsFungible;
								salidaPrevia.IdCliente=ins.IdCliente;
								salidaPrevia.IdPortafolio=ins.IdPortafolio;
								salidaPrevia.DocumentoIdentidad=ins.DocumentoIdentidad;
								salidaPrevia.DocumentoIdentidadRutCavali=ins.DocumentoIdentidadRutCavali;
								salidaPrevia.NombreCliente=ins.NombreCliente;
								salidaPrevia.CodigoPortafolio = ObjPort.CodigoPropositoPortafolio;
								salidaPrevia.IdBoveda=ins.IdBoveda;
								salidaPrevia.NumeroCuentaBoveda="".equals(ins.NumeroCuentaBoveda)?ObjCtaCus.Codigo:ins.NumeroCuentaBoveda;
								salidaPrevia.Relacion=ins.Relacion;
								salidaPrevia.DescripcionRelacion=ins.Relacion;
								
								
								if(ObjInst.Tamano.compareTo(salidaPrevia.Tamano)==1){
									salidaPrevia.Tamano=ObjInst.Tamano;
								}
								
								salidaPrevia.CantidadTotal = salidaPrevia.CantidadTotal.add(ObjtblCustodia.Cantidad).add(ObjtblVentaPacto.Cantidad).add(ObjtblCompraPacto.Cantidad).add(ObjtblSimulC.Cantidad)
										.add(ObjtblSimulV.Cantidad).add(ObjtblGarantias.Cantidad).add(ObjtblTrasnIng.Cantidad)
										.add(ObjtblTrasnEgr.Cantidad).add(ObjtblTrasnEgrVenDes.Cantidad).add(ObjtblReporteCompra.Cantidad).add(ObjtblReporteVenta.Cantidad);
								
								salidaPrevia.ValorCompra = salidaPrevia.ValorCompra.add(ObjtblCustodia.ValorCompra).add(ObjtblVentaPacto.ValorCompra).add(ObjtblCompraPacto.ValorCompra).add(ObjtblSimulC.ValorCompra)
										.add(ObjtblSimulV.ValorCompra).add(ObjtblPrestamos.ValorCompra).add(ObjtblEmprestitos.ValorCompra).add(ObjtblGarantias.ValorCompra).add(ObjtblTrasnIng.ValorCompra)
										.add(ObjtblTrasnEgr.ValorCompra).add(ObjtblReporteCompra.ValorCompra).add(ObjtblReporteVenta.ValorCompra);
								
								salidaPrevia.MontoCompra = salidaPrevia.MontoCompra.add(ObjtblCustodia.MontoCompra).add(ObjtblVentaPacto.MontoCompra).add(ObjtblCompraPacto.MontoCompra).add(ObjtblSimulC.MontoCompra)
										.add(ObjtblSimulV.MontoCompra).add(ObjtblPrestamos.MontoCompra).add(ObjtblEmprestitos.MontoCompra).add(ObjtblGarantias.MontoCompra).add(ObjtblTrasnIng.MontoCompra)
										.add(ObjtblTrasnEgr.MontoCompra).add(ObjtblReporteCompra.MontoCompra).add(ObjtblReporteVenta.MontoCompra);
								
								salidaPrevia.ValorTotalMercado = salidaPrevia.ValorTotalMercado.add(ObjtblCustodia.ValorMercado).add(ObjtblVentaPacto.ValorMercado).add(ObjtblCompraPacto.ValorMercado).add(ObjtblSimulC.ValorMercado)
										.add(ObjtblSimulV.ValorMercado).add(ObjtblPrestamos.ValorMercado).add(ObjtblEmprestitos.ValorMercado).add(ObjtblGarantias.ValorMercado).add(ObjtblTrasnIng.ValorMercado)
										.add(ObjtblTrasnEgr.ValorMercado).add(ObjtblReporteCompra.ValorMercado).add(ObjtblReporteVenta.ValorMercado);
								
								salidaPrevia.MontoTotalMercado = salidaPrevia.MontoTotalMercado.add(ObjtblCustodia.MontoMercado).add(ObjtblVentaPacto.MontoMercado).add(ObjtblCompraPacto.MontoMercado).add(ObjtblSimulC.MontoMercado)
										.add(ObjtblSimulV.MontoMercado).add(ObjtblPrestamos.MontoMercado).add(ObjtblEmprestitos.MontoMercado).add(ObjtblGarantias.MontoMercado).add(ObjtblTrasnIng.MontoMercado)
										.add(ObjtblTrasnEgr.MontoMercado).add(ObjtblReporteCompra.MontoMercado).add(ObjtblReporteVenta.MontoMercado);				
								
								//custodia
								salidaPrevia.CantidadCustodia =salidaPrevia.CantidadCustodia.add(ObjtblCustodia.Cantidad)
										.subtract(ObjtblCompraPacto.EsNominal.equals("S")?ObjtblCompraPacto.CantidadNominal:BigDecimal.ZERO,MathContext.DECIMAL128)
										.add(ObjtblTranomEgr.Cantidad,MathContext.DECIMAL128)
										.subtract(ObjtblTranomIng.Cantidad, MathContext.DECIMAL128)
										.subtract(ObjtblTrasnEgrVenDes.Cantidad.abs(MathContext.DECIMAL128),MathContext.DECIMAL128);
								salidaPrevia.ValorPPPCustodia=salidaPrevia.ValorPPPCustodia.add(ObjtblCustodia.ValorCompra)
										.subtract(ObjtblCompraPacto.EsNominal.equals("S")?ObjtblCompraPacto.ValorCompra:BigDecimal.ZERO,MathContext.DECIMAL128)
										.add(ObjtblTranomEgr.ValorCompra,MathContext.DECIMAL128)
										.subtract(ObjtblTranomIng.ValorCompra, MathContext.DECIMAL128);
								salidaPrevia.MontoPPPCustodia=salidaPrevia.MontoPPPCustodia.add(ObjtblCustodia.MontoCompra)
										.subtract(ObjtblCompraPacto.EsNominal.equals("S")?ObjtblCompraPacto.MontoCompra:BigDecimal.ZERO,MathContext.DECIMAL128)
										.add(ObjtblTranomEgr.MontoCompra,MathContext.DECIMAL128)
										.subtract(ObjtblTranomIng.MontoCompra, MathContext.DECIMAL128);
								salidaPrevia.PrecioMercadoCustodia=salidaPrevia.PrecioMercadoCustodia.add(ObjtblCustodia.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoCustodia=salidaPrevia.TasaMercadoCustodia.add(ObjtblCustodia.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoCustodia=salidaPrevia.ValorMercadoCustodia.add(ObjtblCustodia.ValorMercado)
										.subtract(ObjtblCompraPacto.EsNominal.equals("S")?ObjtblCompraPacto.ValorMercado:BigDecimal.ZERO,MathContext.DECIMAL128)
										.add(ObjtblTranomEgr.ValorMercado,MathContext.DECIMAL128)
										.subtract(ObjtblTranomIng.ValorMercado, MathContext.DECIMAL128);
								salidaPrevia.MontoMercadoCustodia=salidaPrevia.MontoMercadoCustodia.add(ObjtblCustodia.MontoMercado)
										.subtract(ObjtblCompraPacto.EsNominal.equals("S")?ObjtblCompraPacto.MontoMercado:BigDecimal.ZERO,MathContext.DECIMAL128)
										.add(ObjtblTranomEgr.MontoMercado,MathContext.DECIMAL128)
										.subtract(ObjtblTranomIng.MontoMercado, MathContext.DECIMAL128);
								salidaPrevia.InteresCorridoCustodia=salidaPrevia.InteresCorridoCustodia.add(ObjtblCustodia.InteresCorrido);	
								
								salidaPrevia.INGPrecioPromedioCompra = salidaPrevia.INGPrecioPromedioCompra.add(ObjtblCustodia.INGPrecioPromedioCompra);  //CELZORRO-37
								salidaPrevia.INGMontoPrecioPromedioMonedaInstrumento = salidaPrevia.INGMontoPrecioPromedioMonedaInstrumento.add(ObjtblCustodia.INGMontoPrecioPromedioMonedaInstrumento);
								salidaPrevia.IdMonedaSistema = ObjtblCustodia.IdMonedaSistema;
								salidaPrevia.DividendoEnDinero = ObjtblCustodia.DividendoEnDinero;
								
								salidaPrevia.CantidadTransitoIng = salidaPrevia.CantidadTransitoIng.add(ObjtblIngEgr.CantidadTransIng);
								salidaPrevia.CantidadTransitoEgr  = salidaPrevia.CantidadTransitoEgr.add(ObjtblIngEgr.CantidadTransEgr);
								
								//sim compra	
								salidaPrevia.CantidadSimulCom =salidaPrevia.CantidadSimulCom.add(ObjtblSimulC.Cantidad);
								salidaPrevia.ValorPPPSimulCom=salidaPrevia.ValorPPPSimulCom.add(ObjtblSimulC.ValorCompra);
								salidaPrevia.MontoPPPSimulCom=salidaPrevia.MontoPPPSimulCom.add(ObjtblSimulC.MontoCompra);
								salidaPrevia.PrecioMercadoSimulCom=salidaPrevia.PrecioMercadoSimulCom.add(ObjtblSimulC.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoSimulCom=salidaPrevia.TasaMercadoSimulCom.add(ObjtblSimulC.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoSimulCom=salidaPrevia.ValorMercadoSimulCom.add(ObjtblSimulC.ValorMercado);
								salidaPrevia.MontoMercadoSimulCom=salidaPrevia.MontoMercadoSimulCom.add(ObjtblSimulC.MontoMercado);
								salidaPrevia.InteresCorridoSimulCom=salidaPrevia.InteresCorridoSimulCom.add(ObjtblSimulC.InteresCorrido);
								
								
								//sim venta
								salidaPrevia.CantidadSimulVen =salidaPrevia.CantidadSimulVen.add(ObjtblSimulV.Cantidad);
								salidaPrevia.ValorPPPSimulVen=salidaPrevia.ValorPPPSimulVen.add(ObjtblSimulV.ValorCompra);
								salidaPrevia.MontoPPPSimulVen=salidaPrevia.MontoPPPSimulVen.add(ObjtblSimulV.MontoCompra);
								salidaPrevia.PrecioMercadoSimulVen=salidaPrevia.PrecioMercadoSimulVen.add(ObjtblSimulV.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoSimulVen=salidaPrevia.TasaMercadoSimulVen.add(ObjtblSimulV.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoSimulVen=salidaPrevia.ValorMercadoSimulVen.add(ObjtblSimulV.ValorMercado);
								salidaPrevia.MontoMercadoSimulVen=salidaPrevia.MontoMercadoSimulVen.add(ObjtblSimulV.MontoMercado);
								salidaPrevia.InteresCorridoSimulVen=salidaPrevia.InteresCorridoSimulVen.add(ObjtblSimulV.InteresCorrido);
								
								//prestamos
								salidaPrevia.CantidadPrestamo =salidaPrevia.CantidadPrestamo.add(ObjtblPrestamos.Cantidad);
								salidaPrevia.ValorPPPPrestamo=salidaPrevia.ValorPPPPrestamo.add(ObjtblPrestamos.ValorCompra);
								salidaPrevia.MontoPPPPrestamo=salidaPrevia.MontoPPPPrestamo.add(ObjtblPrestamos.MontoCompra);
								salidaPrevia.PrecioMercadoPrestamo=salidaPrevia.PrecioMercadoPrestamo.add(ObjtblPrestamos.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoPrestamo=salidaPrevia.TasaMercadoPrestamo.add(ObjtblPrestamos.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoPrestamo=salidaPrevia.ValorMercadoPrestamo.add(ObjtblPrestamos.ValorMercado);
								salidaPrevia.MontoMercadoPrestamo=salidaPrevia.MontoMercadoPrestamo.add(ObjtblPrestamos.MontoMercado);
								salidaPrevia.InteresCorridoPrestamo=salidaPrevia.InteresCorridoPrestamo.add(ObjtblPrestamos.InteresCorrido);
							
								
								//emprestitos				
								salidaPrevia.CantidadEmpresti =salidaPrevia.CantidadEmpresti.add(ObjtblEmprestitos.Cantidad);
								salidaPrevia.ValorPPPEmpresti=salidaPrevia.ValorPPPEmpresti.add(ObjtblEmprestitos.ValorCompra);
								salidaPrevia.MontoPPPEmpresti=salidaPrevia.MontoPPPEmpresti.add(ObjtblEmprestitos.MontoCompra);
								salidaPrevia.PrecioMercadoEmpresti=salidaPrevia.PrecioMercadoEmpresti.add(ObjtblEmprestitos.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoEmpresti=salidaPrevia.TasaMercadoEmpresti.add(ObjtblEmprestitos.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoEmpresti=salidaPrevia.ValorMercadoEmpresti.add(ObjtblEmprestitos.ValorMercado);
								salidaPrevia.MontoMercadoEmpresti=salidaPrevia.MontoMercadoEmpresti.add(ObjtblEmprestitos.MontoMercado);
								salidaPrevia.InteresCorridoEmpresti=salidaPrevia.InteresCorridoEmpresti.add(ObjtblEmprestitos.InteresCorrido);
								
								//garantia
								salidaPrevia.CantidadGarantia =salidaPrevia.CantidadGarantia.add(ObjtblGarantias.Cantidad);
								salidaPrevia.ValorPPPGarantia=salidaPrevia.ValorPPPGarantia.add(ObjtblGarantias.ValorCompra);
								salidaPrevia.MontoPPPGarantia=salidaPrevia.MontoPPPGarantia.add(ObjtblGarantias.MontoCompra);
								salidaPrevia.PrecioMercadoGarantia=salidaPrevia.PrecioMercadoGarantia.add(ObjtblGarantias.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoGarantia=salidaPrevia.TasaMercadoGarantia.add(ObjtblGarantias.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoGarantia=salidaPrevia.ValorMercadoGarantia.add(ObjtblGarantias.ValorMercado);
								salidaPrevia.MontoMercadoGarantia=salidaPrevia.MontoMercadoGarantia.add(ObjtblGarantias.MontoMercado);
								salidaPrevia.InteresCorridoGarantia=salidaPrevia.InteresCorridoGarantia.add(ObjtblGarantias.InteresCorrido);
								
								//venta pacto
								salidaPrevia.CantidadPactosV =salidaPrevia.CantidadPactosV.add(ObjtblVentaPacto.Cantidad);
								salidaPrevia.ValorPPPPactosV=salidaPrevia.ValorPPPPactosV.add(ObjtblVentaPacto.ValorCompra);
								salidaPrevia.MontoPPPPactosV=salidaPrevia.MontoPPPPactosV.add(ObjtblVentaPacto.MontoCompra);
								salidaPrevia.PrecioMercadoPactosV=salidaPrevia.PrecioMercadoPactosV.add(ObjtblVentaPacto.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoPactosV=salidaPrevia.TasaMercadoPactosV.add(ObjtblVentaPacto.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoPactosV=salidaPrevia.ValorMercadoPactosV.add(ObjtblVentaPacto.ValorMercado);
								salidaPrevia.MontoMercadoPactosV=salidaPrevia.MontoMercadoPactosV.add(ObjtblVentaPacto.MontoMercado);
								salidaPrevia.InteresCorridoPactosV=salidaPrevia.InteresCorridoPactosV.add(ObjtblVentaPacto.InteresCorrido);
								
								//compra pacto
								salidaPrevia.CantidadPactosC =salidaPrevia.CantidadPactosC.add(ObjtblCompraPacto.Cantidad);
								salidaPrevia.ValorPPPPactosC=salidaPrevia.ValorPPPPactosC.add(ObjtblCompraPacto.ValorCompra);
								salidaPrevia.MontoPPPPactosC=salidaPrevia.MontoPPPPactosC.add(ObjtblCompraPacto.MontoCompra);
								salidaPrevia.PrecioMercadoPactosC=salidaPrevia.PrecioMercadoPactosC.add(ObjtblCompraPacto.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoPactosC=salidaPrevia.TasaMercadoPactosC.add(ObjtblCompraPacto.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoPactosC=salidaPrevia.ValorMercadoPactosC.add(ObjtblCompraPacto.ValorMercado);
								salidaPrevia.MontoMercadoPactosC=salidaPrevia.MontoMercadoPactosC.add(ObjtblCompraPacto.MontoMercado);
								salidaPrevia.InteresCorridoPactosC=salidaPrevia.InteresCorridoPactosC.add(ObjtblCompraPacto.InteresCorrido);
								
								//Ingreso de transitos
								salidaPrevia.CantidadTransIng =salidaPrevia.CantidadTransIng.add(ObjtblTrasnIng.Cantidad);
								
								salidaPrevia.ValorPPPTransIng=salidaPrevia.ValorPPPTransIng.add(ObjtblTrasnIng.ValorCompra);
								salidaPrevia.MontoPPPTransIng=salidaPrevia.MontoPPPTransIng.add(ObjtblTrasnIng.MontoCompra);
								salidaPrevia.PrecioMercadoTransIng=salidaPrevia.PrecioMercadoTransIng.add(ObjtblTrasnIng.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoTransIng=salidaPrevia.TasaMercadoTransIng.add(ObjtblTrasnIng.INGTasaMercadoActual);//max
								salidaPrevia.ValorMercadoTransIng=salidaPrevia.ValorMercadoTransIng.add(ObjtblTrasnIng.ValorMercado);
								salidaPrevia.MontoMercadoTransIng=salidaPrevia.MontoMercadoTransIng.add(ObjtblTrasnIng.MontoMercado);
								salidaPrevia.InteresCorridoTransIng=salidaPrevia.InteresCorridoTransIng.add(ObjtblTrasnIng.InteresCorrido);
								
								//Egreso de transito
								salidaPrevia.CantidadTransEgr =salidaPrevia.CantidadTransEgr.add(ObjtblTrasnEgr.Cantidad);
								//Si es negativo se multiplica por -1 (pidieron mostrarlo positivo)
								salidaPrevia.CantidadTransVenDesEgr =salidaPrevia.CantidadTransVenDesEgr.add(ObjtblTrasnEgrVenDes.Cantidad.compareTo(BigDecimal.ZERO) > 0 ? ObjtblTrasnEgrVenDes.Cantidad : ObjtblTrasnEgrVenDes.Cantidad.multiply(new BigDecimal(-1)));
								
								salidaPrevia.ValorPPPTransEgr=salidaPrevia.ValorPPPTransEgr.add(ObjtblTrasnEgr.ValorCompra);
								salidaPrevia.MontoPPPTransEgr=salidaPrevia.MontoPPPTransEgr.add(ObjtblTrasnEgr.MontoCompra);
								salidaPrevia.PrecioMercadoTransEgr=salidaPrevia.PrecioMercadoTransEgr.add(ObjtblTrasnEgr.INGPrecioMercadoActual);
								salidaPrevia.TasaMercadoTransEgr=salidaPrevia.TasaMercadoTransEgr.add(ObjtblTrasnEgr.INGTasaMercadoActual);  //max
								salidaPrevia.ValorMercadoTransEgr=salidaPrevia.ValorMercadoTransEgr.add(ObjtblTrasnEgr.ValorMercado);
								salidaPrevia.MontoMercadoTransEgr=salidaPrevia.MontoMercadoTransEgr.add(ObjtblTrasnEgr.MontoMercado);
								salidaPrevia.InteresCorridoTransEgr=salidaPrevia.InteresCorridoTransEgr.add(ObjtblTrasnEgr.InteresCorrido);
								
								//Egresos por compras Nominales
								salidaPrevia.CantidadTranomEgr =salidaPrevia.CantidadTranomEgr.add(ObjtblTranomEgr.Cantidad);
								
								//Ingresos por Ventas Nominales
								salidaPrevia.CantidadTranomIng =salidaPrevia.CantidadTranomIng.add(ObjtblTranomIng.Cantidad);
							
								
								//Pactos Nominales
								salidaPrevia.CantidadPactosNomC= salidaPrevia.CantidadPactosNomC.add(ObjtblCompraPacto.EsNominal.equals("S")?ObjtblCompraPacto.CantidadNominal:BigDecimal.ZERO);
								salidaPrevia.CantidadPactosNomV= salidaPrevia.CantidadPactosNomV.add(ObjtblVentaPacto.EsNominal.equals("S")?ObjtblVentaPacto.CantidadNominal:BigDecimal.ZERO);
								
								
								//Presunto
								salidaPrevia.TotalPosicionPresunta = "ADQ".equals(entrada.p_TipoFechaConsulta) ? BigDecimal.ZERO : salidaPrevia.TotalPosicionPresunta
																																				.add(ObjtblCustodiaPresunto.Cantidad,MathContext.DECIMAL128)
																																				.add(ObjtblTranomPresuntoEgr.Cantidad,MathContext.DECIMAL128)
																																				.subtract(ObjtblTranomPresuntoIng.Cantidad,MathContext.DECIMAL128)
																																				.subtract(ObjtblTrasnEgrVenDesPresunto.Cantidad,MathContext.DECIMAL128);
						
								//Simultanea nominal transito
								salidaPrevia.CantidadSimCTraNom =salidaPrevia.CantidadSimCTraNom.add(ObjtblSimulCTraNom.Cantidad);
								salidaPrevia.CantidadSimVTraNom =salidaPrevia.CantidadSimVTraNom.add(ObjtblSimulVTraNom.Cantidad);				
								
								salidaPrevia.IdMonedaInstrReajuste =ValidaMoneda(v_IdMonedaSistema,"reajuste",ObjtblCustodia,ObjtblVentaPacto,ObjtblCompraPacto,ObjtblSimulC,ObjtblSimulV,ObjtblPrestamos,ObjtblEmprestitos,ObjtblGarantias,ObjtblTrasnIng,ObjtblTrasnEgr,ObjtblReporteCompra,ObjtblReporteVenta);
								salidaPrevia.IdMonedaEmision =ValidaMoneda(v_IdMonedaSistema,"emision",ObjtblCustodia,ObjtblVentaPacto,ObjtblCompraPacto,ObjtblSimulC,ObjtblSimulV,ObjtblPrestamos,ObjtblEmprestitos,ObjtblGarantias,ObjtblTrasnIng,ObjtblTrasnEgr,ObjtblReporteCompra,ObjtblReporteVenta);
								salidaPrevia.IdMonedaPrincipal=ValidaMoneda(v_IdMonedaSistema,"principal",ObjtblCustodia,ObjtblVentaPacto,ObjtblCompraPacto,ObjtblSimulC,ObjtblSimulV,ObjtblPrestamos,ObjtblEmprestitos,ObjtblGarantias,ObjtblTrasnIng,ObjtblTrasnEgr,ObjtblReporteCompra,ObjtblReporteVenta);
								
								salidaPrevia.FechaRespaldo="";
								salidaPrevia.Producto=ObjPro.NombreProductoAgrupador;				
								salidaPrevia.FechaVencimiento=ObjInst.FechaVencimiento;
								salidaPrevia.RegimenTributario = "FM".equals(ObjInst.IdClasificacionTipoInstrumento) ? custodiaFondoMutuo.obtenerRegimenFondoMutuo() : ObjtblCustodia.RegimenTributario;
								salidaPrevia.IdRegimenTributario = "FM".equals(ObjInst.IdClasificacionTipoInstrumento) ? custodiaFondoMutuo.obtenerIdRegimenFondoMutuo() :  ObjtblCustodia.IdRegimenTributario;
								salidaPrevia.IdMatrizAgrupador = ins.IdMatrizAgrupador;
								salidaPrevia.EsADC = ins.EsADC;
								salidaPrevia.RazonSocialInstrumento = ins.RazonSocialInstrumento;
								
								if("RV".equals(salidaPrevia.IdClasificacionTipoInstrumento) || "FM".equals(salidaPrevia.IdClasificacionTipoInstrumento) || "RF".equals(salidaPrevia.IdClasificacionTipoInstrumento) || "IF".equals(salidaPrevia.IdClasificacionTipoInstrumento)) {
									salidaPrevia.CantidadPrecioPromedio = salidaPrevia.CantidadPrecioPromedio.add(ObjtblCustodia.CantidadPrecioCompra, MathContext.DECIMAL128);
									salidaPrevia.CantidadPrecioPromedio = salidaPrevia.CantidadPrecioPromedio.add(ObjtblSub.CantidadPrecioCompra, MathContext.DECIMAL128);
									salidaPrevia.CantidadPrecioPromedio = salidaPrevia.CantidadPrecioPromedio.add(ObjtblGarantias.CantidadPrecioCompra, MathContext.DECIMAL128);
									salidaPrevia.MontoPrecioPromedio = salidaPrevia.MontoPrecioPromedio.add(ObjtblCustodia.MontoPrecioPromedio, MathContext.DECIMAL128);
									salidaPrevia.MontoPrecioPromedio = salidaPrevia.MontoPrecioPromedio.add(ObjtblSub.MontoPrecioPromedio, MathContext.DECIMAL128);
									salidaPrevia.MontoPrecioPromedio = salidaPrevia.MontoPrecioPromedio.add(ObjtblGarantias.MontoPrecioPromedio, MathContext.DECIMAL128);
								}
								
								//key para tabla preparacion salida
								
								
								//Reportes
								
								salidaPrevia.CantidadReporteCompra =salidaPrevia.CantidadReporteCompra.add(ObjtblReporteCompra.Cantidad);
								salidaPrevia.CantidadReporteVenta =salidaPrevia.CantidadReporteVenta.add(ObjtblReporteVenta.Cantidad);
								
								listaSalida.add(salidaPrevia);
										
								String keytablaSalida = ins.IdClasificacionTipoInstrumento+ObjInst.NombreClasificacionTipoInstrumento+ins.IdTipoInstrumento+ObjInst.NombreTipoInstrumento+ObjInst.ISIN+
										ins.IdTipoInstrumento+ins.ClasificacionRiesgoInstrumento+ins.Nemotecnico+(ins.IdEmisor==null?ObjInst.IdEmisor:ins.IdEmisor)+ins.EsFungible+ins.IdCliente+ins.IdPortafolio+
										ins.DocumentoIdentidad+ins.DocumentoIdentidadRutCavali+ins.NombreCliente+ObjPort.CodigoPropositoPortafolio+ins.IdBoveda+ObjCtaCus.IdBoveda+ObjCtaCus.IdCuentaCustodioPortafolio+
										ins.Relacion+salidaPrevia.IdMonedaInstrReajuste+salidaPrevia.IdMonedaEmision+ObjPro.NombreProductoAgrupador+
										salidaPrevia.IdMonedaPrincipal+salidaPrevia.FechaVencimiento+salidaPrevia.RegimenTributario+salidaPrevia.IdRegimenTributario+salidaPrevia.IdMatrizAgrupador+salidaPrevia.EsADC;
								
								HashSalidaPrevia.put(keytablaSalida, salidaPrevia);
							}
							
						}
						logger.debug("END CONSTRUIR PRE SALIDA"); 
						
						//logger.debug(listaSalida);
						logger.debug("INIT RECORRER PRE SALIDA"); 
						for(Map.Entry<String, Tabla_Salida> obj : HashSalidaPrevia.entrySet()){
							v_SalidaPrevia.add(obj.getValue());
						}//FIN PREPARACION TABLA SALIDA
						logger.debug("END RECORRER PRE SALIDA"); 
						EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
						EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
						paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
						paramsPrecioTasa.p_IdModulo = entrada.p_IdModulo;
						paramsPrecioTasa.p_IdPais = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
						paramsPrecioTasa.p_IdUsuario = entrada.p_IdUsuario;
						//Llenar entrada
						
						List<Tabla_Salida> instrumentosRVFM = v_SalidaPrevia.parallelStream()
								.filter(dato -> dato.IdClasificacionTipoInstrumento.equals("RV") || dato.IdClasificacionTipoInstrumento.equals("FM")).collect(Collectors.toList());
						List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas",new Long(instrumentosRVFM.size()));
						AtomicInteger i = new AtomicInteger(0);
						paramsPrecioTasa.p_DatosInstrumentos = v_SalidaPrevia.parallelStream()
								.filter(dato -> dato.IdClasificacionTipoInstrumento.equals("RV") || dato.IdClasificacionTipoInstrumento.equals("FM"))
								.map(dato->{
									EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
									paramsRegIn.FechaConsulta=Fecha.toDate(entrada.p_FechaConsulta);
									paramsRegIn.IdClasificacionTipoInstrumento = dato.IdClasificacionTipoInstrumento;
									paramsRegIn.IdInstrumento = dato.IdInstrumento;
									paramsRegIn.IdMoneda = dato.IdMonedaEmision;
									paramsRegIn.IdRegistro = secuencias.get(i.getAndIncrement());
									paramsRegIn.IdTipoCriterioTasa = "0";
									paramsRegIn.IdTipoInstrumento = dato.IdTipoInstrumento;
									paramsRegIn.IdUso = "CARTERA";
									paramsRegIn.Nemotecnico = dato.Nemotecnico;
									paramsRegIn.PlazoResidual = 0L;
									paramsRegIn.TipoCartera = "0";
									return paramsRegIn;
								})
								.collect(Collectors.toList());
						HashMap<String, Object> hashSalidaPrecioTasa = new HashMap<String,Object>();
						EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
						for(EntregarPreciosTasas_Salida_PreciosTasas dato : salidaPrecioTasa.P_OUT_PRECIOSTASAS){
							hashSalidaPrecioTasa.putIfAbsent(dato.Nemotecnico, dato);
						}
						
						//TABLA DE SALIDA
						logger.debug("INIT CONSRUIR SALIDA"); 
						
						CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada(); ///aqui
						calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						calcularParidadEntrada.p_IdTrabajo = 2L;
						String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
						EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
						entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
						
						CalcularParidad calcularParidad = new CalcularParidad();

						Long idInterno = 1L;///aqui

						HashMap<String, Tabla_Salida> HashP_out_tbl = new HashMap<String, Tabla_Salida>();
						for(Tabla_Salida pre :v_SalidaPrevia){
							
							Tabla_Salida ObjTabla= new Tabla_Salida();
							
							ObjTabla.IdInterno = idInterno;
							
							String keyOut = pre.IdClasificacionTipoInstrumento+pre.IdTipoInstrumento+pre.Nemotecnico+pre.IdEmisor+pre.EsFungible+
									pre.IdCliente+pre.IdPortafolio+ pre.DocumentoIdentidad+ pre.NombreCliente+ pre.IdBoveda+pre.Relacion+pre.RegimenTributario+pre.IdMatrizAgrupador+pre.EsADC;
									
							//Utils.copyPojo(pre, ObjTabla);
							ObjTabla.copyValuesFrom(pre);
							//TODO
							if(ObjTabla.CantidadPrecioPromedio.compareTo(BigDecimal.ZERO) != 0) {
								ObjTabla.PPPCustodia = ObjTabla.MontoPrecioPromedio.divide(ObjTabla.CantidadPrecioPromedio, MathContext.DECIMAL128);
							}
							if(!pre.IdClasificacionTipoInstrumento.equals("RV") && !pre.IdClasificacionTipoInstrumento.equals("FM")){
								if("RF".equals(pre.IdClasificacionTipoInstrumento) || "IF".equals(pre.IdClasificacionTipoInstrumento)) {
									ObjTabla.Rentabilidad = ObjTabla.TasaMercadoCustodia.compareTo(BigDecimal.ZERO) != 0 && ObjTabla.PPPCustodia.compareTo(BigDecimal.ZERO) != 0 ? ObjTabla.TasaMercadoCustodia.subtract(ObjTabla.PPPCustodia, MathContext.DECIMAL128).divide(ObjTabla.PPPCustodia,MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128) : new BigDecimal(9999);
								}
								ObjTabla.PPPPrestamo=BigDecimal.ZERO;
								ObjTabla.PPPSimulCom=BigDecimal.ZERO;
								ObjTabla.PPPSimulVen=BigDecimal.ZERO;
								ObjTabla.PPPEmpresti=BigDecimal.ZERO;
								ObjTabla.PPPGarantia=BigDecimal.ZERO;
								ObjTabla.PPPPactosV=BigDecimal.ZERO;
								ObjTabla.PPPPactosC=BigDecimal.ZERO;
								ObjTabla.PPPReporteVenta=BigDecimal.ZERO;
								ObjTabla.PPPReporteCompra=BigDecimal.ZERO;				
								ObjTabla.ValorPPPCustodia=BigDecimal.ZERO;
								ObjTabla.MontoPPPCustodia=BigDecimal.ZERO;
								ObjTabla.ValorPPPSimulCom=BigDecimal.ZERO;
								ObjTabla.ValorPPPSimulVen=BigDecimal.ZERO;
								ObjTabla.ValorPPPPrestamo=BigDecimal.ZERO;
								ObjTabla.ValorPPPPrestamo=BigDecimal.ZERO;
								ObjTabla.ValorPPPEmpresti=BigDecimal.ZERO;
								ObjTabla.ValorPPPGarantia=BigDecimal.ZERO;
								ObjTabla.ValorPPPPactosV=BigDecimal.ZERO;
								ObjTabla.ValorPPPPactosC=BigDecimal.ZERO;				
								ObjTabla.ValorPPPReporteCompra=BigDecimal.ZERO;
								ObjTabla.ValorPPPReporteVenta=BigDecimal.ZERO;
								ObjTabla.PrecioMercadoCustodiaMonedaLocal=BigDecimal.ZERO;
								ObjTabla.PPPCustodiaMonedaLocal=BigDecimal.ZERO;
								ObjTabla.FechaFuente = Fecha.toDate("0001-01-01");
							}else{		
								
								 /** Se Usa el Precio Actual Mercado Solo para RV **/
								EntregarPreciosTasas_Salida_PreciosTasas hashReg = (EntregarPreciosTasas_Salida_PreciosTasas)hashSalidaPrecioTasa.get(pre.Nemotecnico);
								if(hashReg != null && hashReg.Precio != null) {
									ObjTabla.ValorMercadoCustodia = pre.CantidadTotal.multiply(hashReg.Precio, MathContext.DECIMAL128);
									ObjTabla.PrecioMercadoCustodia = hashReg.Precio;
									ObjTabla.FechaFuente = Fecha.toDate(Fecha.toStringDatetime(hashReg.FechaFuente));
									if(idMonedaSistema.equals(ObjTabla.IdMonedaInstrReajuste)) {
										ObjTabla.MontoMercadoCustodia = ObjTabla.ValorMercadoCustodia;
										ObjTabla.PrecioMercadoCustodiaMonedaLocal = ObjTabla.PrecioMercadoCustodia;
										ObjTabla.PPPCustodiaMonedaLocal = ObjTabla.PPPCustodia; 
									}else {
										CalculoEntrada calculoEntradaMonto = new CalculoEntrada();
										calculoEntradaMonto.IdTrabajo = 2L;
										calculoEntradaMonto.IdInterno = ObjTabla.IdInterno * 2;
										calculoEntradaMonto.Fecha = entrada.p_FechaConsulta;
										calculoEntradaMonto.IdMonedaOrigen = ObjTabla.IdMonedaInstrReajuste;
										calculoEntradaMonto.IdMonedaDestino = idMonedaSistema;
										calculoEntradaMonto.ValorCambioLocalModificado = BigDecimal.ZERO;
										calculoEntradaMonto.MontoOrigen =  ObjTabla.ValorMercadoCustodia;
										calculoEntradaMonto.IdUso = "";
										calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonto);
										
										CalculoEntrada calculoEntradaPrecio = new CalculoEntrada();
										calculoEntradaPrecio.IdTrabajo = 2L;
										calculoEntradaPrecio.IdInterno = ObjTabla.IdInterno *  -2;
										calculoEntradaPrecio.Fecha = entrada.p_FechaConsulta;
										calculoEntradaPrecio.IdMonedaOrigen = ObjTabla.IdMonedaInstrReajuste;
										calculoEntradaPrecio.IdMonedaDestino = idMonedaSistema;
										calculoEntradaPrecio.ValorCambioLocalModificado = BigDecimal.ZERO;
										calculoEntradaPrecio.MontoOrigen =  ObjTabla.PrecioMercadoCustodia;
										calculoEntradaPrecio.IdUso = "";
										calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaPrecio);
										
										CalculoEntrada calculoEntradaPrecioPPP = new CalculoEntrada();
										calculoEntradaPrecioPPP.IdTrabajo = 2L;
										calculoEntradaPrecioPPP.IdInterno = (ObjTabla.IdInterno * 2)  + 1;
										calculoEntradaPrecioPPP.Fecha = entrada.p_FechaConsulta;
										calculoEntradaPrecioPPP.IdMonedaOrigen = ObjTabla.IdMonedaInstrReajuste;
										calculoEntradaPrecioPPP.IdMonedaDestino = idMonedaSistema;
										calculoEntradaPrecioPPP.ValorCambioLocalModificado = BigDecimal.ZERO;
										calculoEntradaPrecioPPP.MontoOrigen =  ObjTabla.PPPCustodia;
										calculoEntradaPrecioPPP.IdUso = "";
										calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaPrecioPPP);
									}
									
								}
								
								
								if(ObjTabla.CantidadPrecioPromedio.compareTo(BigDecimal.ZERO) != 0) {					
									ObjTabla.ValorPPPCustodia = ObjTabla.MontoPrecioPromedio;
								}
								else
								{
									ObjTabla.ValorPPPCustodia=pre.ValorCompra;
									ObjTabla.MontoPPPCustodia=pre.MontoCompra;
								}				
								
							
								
								
								if(ObjTabla.CantidadPrecioPromedio.compareTo(BigDecimal.ZERO) != 0) {
									ObjTabla.Rentabilidad = ObjTabla.PrecioMercadoCustodia.compareTo(BigDecimal.ZERO) != 0 && ObjTabla.PPPCustodia.compareTo(BigDecimal.ZERO) != 0 ? ObjTabla.PrecioMercadoCustodia.subtract(ObjTabla.PPPCustodia, MathContext.DECIMAL128).divide(ObjTabla.PPPCustodia, MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128) : new BigDecimal(9999);
								}
								
								ObjTabla.PPPSimulCom=pre.ValorPPPSimulCom.divide((pre.CantidadSimulCom.compareTo(BigDecimal.ZERO)>0?pre.CantidadSimulCom:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								ObjTabla.PPPSimulVen=pre.ValorPPPSimulVen.divide((pre.CantidadSimulVen.compareTo(BigDecimal.ZERO)>0?pre.CantidadSimulVen:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								ObjTabla.PPPPrestamo=pre.ValorPPPPrestamo.divide((pre.CantidadPrestamo.compareTo(BigDecimal.ZERO)>0?pre.CantidadPrestamo:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								ObjTabla.PPPEmpresti=pre.ValorPPPEmpresti.divide((pre.CantidadEmpresti.compareTo(BigDecimal.ZERO)>0?pre.CantidadEmpresti:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								ObjTabla.PPPGarantia=pre.ValorPPPGarantia.divide((pre.CantidadGarantia.compareTo(BigDecimal.ZERO)>0?pre.CantidadGarantia:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								ObjTabla.PPPPactosV=pre.ValorPPPPactosV.divide((pre.CantidadPactosV.compareTo(BigDecimal.ZERO)>0?pre.CantidadPactosV:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								
								ObjTabla.PPPPactosV=pre.ValorPPPPactosC.divide((pre.CantidadPactosC.compareTo(BigDecimal.ZERO)>0?pre.CantidadPactosC:BigDecimal.ONE),20,RoundingMode.HALF_UP);				
								ObjTabla.PPPReporteVenta=pre.ValorPPPReporteVenta.divide((pre.CantidadReporteVenta.compareTo(BigDecimal.ZERO)>0?pre.CantidadReporteVenta:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								ObjTabla.PPPReporteCompra=pre.ValorPPPReporteCompra.divide((pre.CantidadReporteCompra.compareTo(BigDecimal.ZERO)>0?pre.CantidadReporteCompra:BigDecimal.ONE),20,RoundingMode.HALF_UP);
								
							}
							//cus
							ObjTabla.PrecioCompraCustodia=BigDecimal.ZERO;
							
							if(pre.IdClasificacionTipoInstrumento.equals("IF") || pre.IdClasificacionTipoInstrumento.equals("RF") ){
								ObjTabla.DiferenciaMercadoCustodia=pre.ValorTotalMercado.subtract(pre.ValorCompra);
							}else{
								ObjTabla.DiferenciaMercadoCustodia=CalculoDiferenciaCustodiaMercado(pre.PrecioMercadoCustodia.multiply(pre.CantidadCustodia),pre.CantidadCustodia,pre.ValorCompra,pre.CantidadCustodia,new BigDecimal(pre.Tamano));
							}
							//comsim
							ObjTabla.DiferenciaMercadoSimulCom=CalculoDiferenciaMercado(pre.ValorMercadoSimulCom,pre.CantidadSimulCom,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//vensim
							ObjTabla.DiferenciaMercadoSimulVen=CalculoDiferenciaMercado(pre.ValorMercadoSimulVen,pre.CantidadSimulVen,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//prt
							ObjTabla.DiferenciaMercadoPrestamo=CalculoDiferenciaMercado(pre.ValorMercadoPrestamo,pre.CantidadPrestamo,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//emp
							ObjTabla.DiferenciaMercadoEmpresti=CalculoDiferenciaMercado(pre.ValorMercadoEmpresti,pre.CantidadEmpresti,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//gar
							ObjTabla.DiferenciaMercadoGarantia=CalculoDiferenciaMercado(pre.ValorMercadoGarantia,pre.CantidadGarantia,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//venpac
							ObjTabla.DiferenciaMercadoPactosV=CalculoDiferenciaMercado(pre.ValorMercadoPactosV,pre.CantidadPactosV,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//compac
							ObjTabla.DiferenciaMercadoPactosC=CalculoDiferenciaMercado(pre.ValorMercadoPactosC,pre.CantidadPactosC,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//venrep
							ObjTabla.DiferenciaMercadoReporteVenta=CalculoDiferenciaMercado(pre.ValorMercadoReporteVenta,pre.CantidadReporteVenta,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));
							//comrep
							ObjTabla.DiferenciaMercadoReporteCompra=CalculoDiferenciaMercado(pre.ValorMercadoReporteCompra,pre.CantidadReporteCompra,pre.ValorCompra,pre.CantidadTotal,new BigDecimal(pre.Tamano));

							ObjTabla.TotalPosicion=(pre.CantidadCustodia.add(pre.CantidadGarantia).add(pre.CantidadPactosV).subtract(pre.CantidadPactosC).add(pre.CantidadSimulCom.subtract(pre.CantidadSimulVen)).add(pre.CantidadReporteCompra.subtract(pre.CantidadReporteVenta)).add(pre.CantidadEmpresti.subtract(pre.CantidadPrestamo)));

							ObjTabla.TotalPosicionPresunta = pre.TotalPosicionPresunta;
							ObjTabla.CantidadCustodia=ObjTabla.CantidadCustodia.add(pre.CantidadTransIng).add(pre.CantidadSimVTraNom).subtract(pre.CantidadTransIng);			
						    ObjTabla.CantidadSimulVen = ObjTabla.CantidadSimulVen.add(pre.CantidadSimVTraNom);
						    ObjTabla.CantidadSimulCom = ObjTabla.CantidadSimulCom.add(pre.CantidadSimCTraNom);
						    
						    ObjTabla.CantidadPactosC = ObjTabla.CantidadPactosC.add(ObjTabla.CantidadPactosNomC);
						    ObjTabla.CantidadPactosV = ObjTabla.CantidadPactosV.add(ObjTabla.CantidadPactosNomV);
						   
						    ProductoCustodia producto = (ProductoCustodia) HashProductos.get(ObjTabla.IdMatrizAgrupador);
						    ObjTabla.Producto = producto != null ? producto.NombreProductoAgrupador : "Otros (No APV)";
						   
						    idInterno++;
							HashP_out_tbl.put(keyOut, ObjTabla);
							
						}
						
						CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
						Map<Long,CalculoSalida> hashCalcularParidadSalida = calcularParidadSalida.p_out_CalculosSalida.parallelStream().collect(Collectors.toMap(calcular->calcular.IdInterno, calcular->calcular));
						
						logger.debug("END CONSRUIR SALIDA"); 
						
						List<Tabla_Salida> out_tbl= new ArrayList<>();
						logger.debug("INIT AGREGAR SALIDA A LISTA"); 
						for(Map.Entry<String, Tabla_Salida> obj : HashP_out_tbl.entrySet()){
							Tabla_Salida ObjTabla = obj.getValue();
							CalculoSalida calculoSalidaMonto = hashCalcularParidadSalida.get(ObjTabla.IdInterno * 2);
							CalculoSalida calculoSalidaPrecio = hashCalcularParidadSalida.get(ObjTabla.IdInterno * -2);
							CalculoSalida calculoSalidaPPPCustodia = hashCalcularParidadSalida.get((ObjTabla.IdInterno  * 2) + 1);
							if(calculoSalidaMonto != null) {
								ObjTabla.MontoMercadoCustodia = calculoSalidaMonto.MontoDestino;
							}
							if(calculoSalidaPrecio != null) {
								ObjTabla.PrecioMercadoCustodiaMonedaLocal = calculoSalidaPrecio.MontoDestino;
							}
							if(calculoSalidaPPPCustodia != null) {
								ObjTabla.PPPCustodiaMonedaLocal = calculoSalidaPPPCustodia.MontoDestino;
							}
							out_tbl.add(obj.getValue());
						}//FIN TABLA SALIDA
						
						// agrega el formato de decimales al Precio de Custodia y Precio de Mercado
						for(Tabla_Salida out : out_tbl) {
							String idMoneda = out.IdMonedaInstrReajuste == null ? "CLP" : out.IdMonedaInstrReajuste;
							out.PPPCustodia = MonedaFormato.formatear(con, out.PPPCustodia, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;	
							out.PPPCustodiaMonedaLocal = MonedaFormato.formatear(con, out.PPPCustodiaMonedaLocal, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;			
							out.PrecioMercadoCustodia = MonedaFormato.formatear(con, out.PrecioMercadoCustodia, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;
							out.PrecioMercadoCustodiaMonedaLocal = MonedaFormato.formatear(con, out.PrecioMercadoCustodiaMonedaLocal, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;
						}
						logger.debug("END AGREGAR SALIDA A LISTA"); 

						logger.debug("INIT AGREGAR LISTA A SALIDA");
						salida.P_OUT_TBL.addAll(out_tbl);
						logger.debug("END AGREGAR LISTA A SALIDA");
						
						logger.info("TERMINO");
						return salida;
				
					} 
	
	public List<ResultadoConsulta> procesaTablaResultado (Entrada_ConsultarCustodiaInstrumentosPendientes entrada ,List<ResultadoConsulta> tablaEntrada ,List<DatosCorredora> v_DatosCtaPropia,HashMap<Object, Object> hashRelacionados,String ObjBusqueda) throws Exception {
		
		List<ResultadoConsulta> TablaSalida = new ArrayList<>();
		try {
			 List<ResultadoConsulta> tablaTemporal = new ArrayList<>();
			 HashMap<String, ResultadoConsulta> TablaCustodia = new HashMap<String, ResultadoConsulta>();
			 			 
			 for(ResultadoConsulta res : tablaEntrada){
					
				ResultadoConsulta tmp = new ResultadoConsulta();
				tmp.copyValuesFrom(res);
				//Agrega Relacionado
				boolean relacionado = ( hashRelacionados.getOrDefault(res.IdPersona, null ) != null);
									
				if(res.IdCliente.equals(v_DatosCtaPropia.get(0).IdClienteCtaPropia)){
					tmp.Relacion="CPRO";
				}else if(relacionado){
					tmp.Relacion="REL";
				}else{
					tmp.Relacion="TER";
				}
				
				tablaTemporal.add(tmp);
				
				if(ObjBusqueda.equals("REP")){
					tmp.IdMonedaEmision="";
				}
				
				//Calculos y agrupacion tabla 
				String KeyTablaSalida= tmp.IdClasificacionTipoInstrumento+"_"+tmp.IdTipoInstrumento+"_"+tmp.IdInstrumento+"_"+tmp.Nemotecnico+"_"+tmp.IdEmisor
						+"_"+tmp.EsFungible+tmp.IdCliente+"_"+tmp.IdPortafolio+"_"+tmp.IdBoveda+"_"+tmp.IdCuentaCustodioPortafolio
						+"_"+tmp.Relacion+tmp.IdMonedaInstrReajuste+"_"+tmp.IdMonedaEmision+"_"+tmp.IdMonedaPrincipal+"_"+tmp.RegimenTributario+"_"+tmp.AccionCustodia
						+"_"+tmp.IdMatrizAgrupador+"_"+tmp.EsADC;
			
				ResultadoConsulta ObjResultado = new ResultadoConsulta();
				ObjResultado = TablaCustodia.get(KeyTablaSalida);
				
				if(ObjResultado==null){
					ObjResultado = new ResultadoConsulta();
					ObjResultado.copyValuesFrom(tmp);
					calcularCantidadMontoPrecioPromedio(ObjResultado,tmp,ObjBusqueda);
				}else{
					calcularCantidadMontoPrecioPromedio(ObjResultado,tmp,ObjBusqueda);
					if("CUS".equals(ObjBusqueda) || "VENPAC".equals(ObjBusqueda) || "COMPAC".equals(ObjBusqueda) || "SIM".equals(ObjBusqueda)|| "PRT".equals(ObjBusqueda)|| "EMP".equals(ObjBusqueda)|| "GTA".equals(ObjBusqueda)
						|| ObjBusqueda.equals("REP")){
						ObjResultado.Cantidad=ObjResultado.Cantidad.add(tmp.Cantidad);
						ObjResultado.CantidadNominal=ObjResultado.CantidadNominal.add(tmp.CantidadNominal);
						ObjResultado.PrecioCompra=BigDecimal.ZERO;
						ObjResultado.ValorCompra=ObjResultado.ValorCompra.add(tmp.ValorCompra);
						ObjResultado.MontoCompra=ObjResultado.MontoCompra.add(tmp.MontoCompra);					
						ObjResultado.INGPrecioMercadoActual=(ObjResultado.INGPrecioMercadoActual.compareTo(tmp.INGPrecioMercadoActual)>=0)?ObjResultado.INGPrecioMercadoActual:tmp.INGPrecioMercadoActual;
						ObjResultado.ValorMercado= ObjResultado.ValorMercado.add(tmp.ValorMercado);
						ObjResultado.MontoMercado= ObjResultado.MontoMercado.add(tmp.MontoMercado);
						ObjResultado.INGPrecioPromedioCompra=ObjResultado.Cantidad.compareTo(BigDecimal.ZERO)==0?BigDecimal.ZERO: (ObjResultado.ValorCompra.divide(ObjResultado.Cantidad,20,RoundingMode.HALF_UP));
						ObjResultado.INGMontoPrecioPromedioMonedaInstrumento= BigDecimal.ZERO;
						ObjResultado.IdMonedaSistema= "";
						ObjResultado.ValorPPP=ObjResultado.ValorCompra;
						ObjResultado.MontoPPP=ObjResultado.Cantidad.compareTo(BigDecimal.ZERO)==0?BigDecimal.ZERO:ObjResultado.MontoCompra.divide(ObjResultado.Cantidad,20,RoundingMode.HALF_UP).multiply(ObjResultado.Cantidad);
						ObjResultado.InteresCorrido=ObjResultado.InteresCorrido.add(tmp.InteresCorrido);
					}
					else if(ObjBusqueda.equals("TRA") || ObjBusqueda.equals("TRANOM")){
						ObjResultado.Cantidad=ObjResultado.Cantidad.add(tmp.Cantidad);
						ObjResultado.PrecioCompra=BigDecimal.ZERO;
						ObjResultado.ValorCompra=BigDecimal.ZERO;
						ObjResultado.MontoCompra=BigDecimal.ZERO;				
						ObjResultado.INGPrecioMercadoActual=BigDecimal.ZERO;
						ObjResultado.ValorMercado= BigDecimal.ZERO;
						ObjResultado.MontoMercado= BigDecimal.ZERO;
						ObjResultado.INGPrecioPromedioCompra= BigDecimal.ZERO;
						ObjResultado.INGMontoPrecioPromedioMonedaInstrumento= BigDecimal.ZERO;
						ObjResultado.IdMonedaSistema= "";
						ObjResultado.ValorPPP=BigDecimal.ZERO;
						ObjResultado.MontoPPP=BigDecimal.ZERO;
						ObjResultado.InteresCorrido=BigDecimal.ZERO;
					}
					
				}
				ObjResultado.PrecioCompra = ObjResultado.PrecioCompra;
				TablaCustodia.put(KeyTablaSalida, ObjResultado);
			 }
			 
			 // Cambia HashMap a List
			 TablaSalida= new ArrayList<>();
			 for(Map.Entry<String, ResultadoConsulta> obj : TablaCustodia.entrySet()){
				 TablaSalida.add(obj.getValue());
			 }
			 
		 }catch (Exception e){
			 logger.debug("Error al construir Tabla ");
			 logger.debug(e);
		 }
		
		return TablaSalida;
	}
	
	
	private void calcularCantidadMontoPrecioPromedio(ResultadoConsulta ObjResultado,ResultadoConsulta tmp, String ObjBusqueda) {
		if(("CUS".equals(ObjBusqueda) && !"COMSIM".equals(tmp.IdTipoOperacion)) ||  ("SUB".equals(ObjBusqueda)) || "GTA".equals(ObjBusqueda)) {
			if("RV".equals(ObjResultado.IdClasificacionTipoInstrumento) || "FM".equals(ObjResultado.IdClasificacionTipoInstrumento)) {
				ObjResultado.CantidadPrecioCompra = ObjResultado.CantidadPrecioCompra.add(tmp.Cantidad,  MathContext.DECIMAL128);
				ObjResultado.MontoPrecioPromedio= ObjResultado.MontoPrecioPromedio.add(tmp.Cantidad.multiply(tmp.PrecioCompra, MathContext.DECIMAL128),MathContext.DECIMAL128);
			}
			if("RF".equals(ObjResultado.IdClasificacionTipoInstrumento) || "IF".equals(ObjResultado.IdClasificacionTipoInstrumento)) {
				ObjResultado.CantidadPrecioCompra = ObjResultado.CantidadPrecioCompra.add(tmp.Cantidad.multiply(tmp.ValorFactorMonedaInstrumento, MathContext.DECIMAL128),  MathContext.DECIMAL128);
				if("GTA".equals(ObjBusqueda)) {
					ObjResultado.MontoPrecioPromedio = ObjResultado.MontoPrecioPromedio.add(tmp.MontoCompraCarteraOrigen, MathContext.DECIMAL128);
				}else {
					ObjResultado.MontoPrecioPromedio = ObjResultado.MontoPrecioPromedio.add(tmp.MontoCompra, MathContext.DECIMAL128);
				}
			}
		}
	}
	
	public HashMap<String,ResultadoConsulta> TransformarAHash(List<ResultadoConsulta> ListaEntrada,String tipo,HashMap<String, Object> HashCuentaCustodio){
		
		HashMap<String, ResultadoConsulta> HashRespuesta = new HashMap<String, ResultadoConsulta>();
		
		for(ResultadoConsulta res :ListaEntrada){
			String keyHash="";
			CuentaCustodio ObjCtaCus= HashCuentaCustodio.get(res.IdPortafolio+res.IdBoveda)!=null?(CuentaCustodio) HashCuentaCustodio.get(res.IdPortafolio+res.IdBoveda):new CuentaCustodio();
			String keyC =res.IdClasificacionTipoInstrumento+res.IdTipoInstrumento+res.IdInstrumento+res.Nemotecnico+res.EsFungible+res.IdCliente+res.IdPortafolio+
					res.IdBoveda+res.Relacion+ObjCtaCus.IdCuentaCustodioPortafolio+res.RegimenTributario+res.IdMatrizAgrupador+res.EsADC;
			
			String keyP =res.IdClasificacionTipoInstrumento+res.IdTipoInstrumento+res.IdInstrumento+res.EsFungible+res.IdCliente+res.IdPortafolio+
					res.IdBoveda+res.Relacion+ObjCtaCus.IdCuentaCustodioPortafolio+res.RegimenTributario+res.IdMatrizAgrupador+res.EsADC;
			
			if(tipo.equals("keyC")){
				keyHash=keyC;
			}else{
				keyHash=keyP;
			}
			
			if( HashRespuesta.get(keyHash)==null){
				HashRespuesta.put(keyHash, res);
			}
		}
		
		return HashRespuesta;
	}
	
	public BigDecimal CalculoDiferenciaMercado(BigDecimal ValorMercado,BigDecimal Cantidad, BigDecimal ValorCompra, BigDecimal CantidadTotal, BigDecimal Tamano){
		
				
				BigDecimal CAT_TA	= CantidadTotal.multiply(Tamano);
				BigDecimal COND		= CantidadTotal.compareTo(BigDecimal.ZERO)>0?(CAT_TA):BigDecimal.ONE;
				BigDecimal VC_CA	= ValorCompra.divide(COND,20,RoundingMode.HALF_UP);
				BigDecimal VM_CA	= Cantidad.multiply(VC_CA); 
				
				BigDecimal FX       = ValorMercado.subtract(VM_CA);
				
				return FX;
			}
	
public String ValidaMoneda (String v_IdMonedaSistema,String TipoMoneda,ResultadoConsulta ObjtblCustodia,ResultadoConsulta ObjtblVentaPacto,ResultadoConsulta ObjtblCompraPacto,ResultadoConsulta ObjtblSimulC,ResultadoConsulta ObjtblSimulV,ResultadoConsulta ObjtblPrestamos,ResultadoConsulta ObjtblEmprestitos,ResultadoConsulta ObjtblGarantias,ResultadoConsulta ObjtblTrasnIng,ResultadoConsulta ObjtblTrasnEgr,ResultadoConsulta ObjtblReporteCompra,ResultadoConsulta ObjtblReporteVenta){
		
		String IdMonedaSalida=v_IdMonedaSistema;
		
		List<ResultadoConsulta> ListaObj = new ArrayList<ResultadoConsulta>();
		ListaObj.add(ObjtblCustodia);
		ListaObj.add(ObjtblVentaPacto);
		ListaObj.add(ObjtblCompraPacto);
		ListaObj.add(ObjtblSimulC);
		ListaObj.add(ObjtblSimulV);
		ListaObj.add(ObjtblPrestamos);
		ListaObj.add(ObjtblEmprestitos);
		ListaObj.add(ObjtblGarantias);
		ListaObj.add(ObjtblTrasnIng);
		ListaObj.add(ObjtblTrasnEgr);
		ListaObj.add(ObjtblReporteCompra);
		ListaObj.add(ObjtblReporteVenta);
		
		for (ResultadoConsulta Obj : ListaObj) {
			
			if(Obj.IdCliente==null && Obj.Nemotecnico==null){
				continue;
			}else {
				if(Obj.IdMonedaEmision.equals("") && Obj.IdMonedaInstrReajuste.equals("") && Obj.IdMonedaPrincipal.equals("")) {
					switch(TipoMoneda){
					case "reajuste"  : IdMonedaSalida=v_IdMonedaSistema; break;
					case "emision"   : IdMonedaSalida=v_IdMonedaSistema; break;
					case "principal" : IdMonedaSalida=v_IdMonedaSistema; break;
					}
					
				}else {
					switch(TipoMoneda){
					case "reajuste"  : IdMonedaSalida=Obj.IdMonedaInstrReajuste; break;
					case "emision"   : IdMonedaSalida=Obj.IdMonedaEmision; break;
					case "principal" : IdMonedaSalida=Obj.IdMonedaPrincipal; break;
					}
				}
			}
		}		
		
		return IdMonedaSalida;
	}
	

public BigDecimal CalculoDiferenciaCustodiaMercado(BigDecimal ValorMercado,BigDecimal Cantidad, BigDecimal ValorCompra, BigDecimal CantidadTotal, BigDecimal Tamano){
		
	BigDecimal CAT_TA	= CantidadTotal.multiply(Tamano);
	BigDecimal COND		= CantidadTotal.compareTo(BigDecimal.ZERO)>0?(CAT_TA):BigDecimal.ONE;
	BigDecimal VC_CA	= ValorCompra.divide(COND,20,RoundingMode.HALF_UP);
	BigDecimal VM_CA	= CantidadTotal.multiply(VC_CA); 
	
	BigDecimal FX       = ValorMercado.subtract(VM_CA);
	
	return FX;
}

	/**************************************************
	 * 					CALL 
	 * ************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_ConsultarCustodiaInstrumentosPendientes in=gson.fromJson(jin.toString() ,Entrada_ConsultarCustodiaInstrumentosPendientes.class);
		Salida_ConsultarCustodiaInstrumentosPendientes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**************************************************
	 * 					MAIN 
	 * ************************************************/
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");

		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":0,\"p_IdPortafolio\":null,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_Nemotecnico\":\"COPEC\",\"p_IdProductoAgrupador\":0,\"p_AgrupaPorRelacion\":\"S\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_FechaConsulta\":\"2024-12-18\"}";			
			
			Entrada_ConsultarCustodiaInstrumentosPendientes entrada = gson.fromJson(json, Entrada_ConsultarCustodiaInstrumentosPendientes.class);
			ConsultarCustodiaInstrumentosPendientes demo = new ConsultarCustodiaInstrumentosPendientes();
			long init = System.currentTimeMillis();
			
			Salida_ConsultarCustodiaInstrumentosPendientes salida = demo.logica(con, entrada);
			
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));
			
			System.out.println(salida.P_OUT_TBL);
			//con.commit();
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}

}