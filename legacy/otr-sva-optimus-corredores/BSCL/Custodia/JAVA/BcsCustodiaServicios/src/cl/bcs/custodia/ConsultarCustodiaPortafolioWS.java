package cl.bcs.custodia;

import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

public class ConsultarCustodiaPortafolioWS implements JavaCall{
	
	private final Logger logger =  LogManager.getLogger(ConsultarCustodiaPortafolioWS.class.getName());
	
	public static final String[] TIPO_BUSQUEDAS = {"CUS","VENPAC","COMPAC","SIM","PRT","EMP","GTA","TRA","TRANOM","REP","SUB"};
	
	public static Date fechaInvalida = Fecha.toDate("0001-01-01");

	/** Tope defensivo de pasadas de expansion de macros en preparar(). */
	private static final int MAX_PASADAS_MACROS = 10;

	/**
	 * Carga un recurso .sql del classpath. Equivalente al metodo default loadResourceToString() de
	 * JavaCall, pero estatico, para poder inicializar las constantes de query como static final:
	 * PluginLoader.getObject() hace newInstance() en cada request, y loadResourceToString() no cachea,
	 * asi que con campos de instancia se releian los 45 .sql (~271 KB) en cada llamada al servicio.
	 * Usa el mismo classloader que cargo la clase (el del plugin), por lo que un redespliegue del jar
	 * crea un classloader nuevo y reinicializa estas constantes.
	 */
	private static String loadSql(final String path){
		final InputStream stream = ConsultarCustodiaPortafolioWS.class.getClassLoader().getResourceAsStream(path);
		if (stream == null) {
			throw new IllegalStateException("can't load resource:" + path);
		}
		try {
			return IOUtils.toString(stream);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private static final String DATOS_CTA_PROPIA_SELECT 				= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultarCtaPropia.sql");

	private static final String DATOS_CTA_PROPIA_MESA_RF		        = loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultarCtaPropiaMesaRF.sql");
	
	private static final String CONSULTA_CUSTODIA_SELECT_WS 				= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCustodia.sql");
	private static final String CONSULTA_CUSTODIA_HIS_SELECT_WS 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCustodiaHIS.sql");
	
	private static final String CONSULTA_CUSTODIA_PRESUNTO_SELECT_WS		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCustodiaPresunto.sql");
	private static final String CONSULTA_CUSTODIA_PRESUNTO_HIS_SELECT_WS	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCustodiaPresuntoHIS.sql");
	
	private static final String CONSULTA_VENTA_PACTO_SELECT 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaVentaPacto.sql");
	private static final String CONSULTA_VENTA_PACTO_HIS_SELECT 		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaVentaPactoHIS.sql");
	private static final String CONSULTA_COMPRA_PACTO_SELECT 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCompraPacto.sql");
	private static final String CONSULTA_COMPRA_PACTO_HIS_SELECT 		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCompraPactoHIS.sql");
	
	private static final String CONSULTA_COMPRA_VENTA_SIMULTANEA_SELECT= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCompraSimultanea.sql");
	private static final String CONSULTA_COMPRA_VENTA_SIMULTANEA_HIS_SELECT 	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaCompraSimultaneaHIS.sql");
	
	private static final String CONSULTA_SUBYACENTE					= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaSubyacente.sql");
	private static final String CONSULTA_SUBYACENTE_HIS				= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaSubyacenteHIS.sql");
	
	private static final String CONSULTA_PRESTAMOS_SELECT 				= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaPrestamos.sql");
	private static final String CONSULTA_PRESTAMOS_HIS_SELECT 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaPrestamosHIS.sql");
	
	private static final String CONSULTA_EMPRESTITOS_SELECT 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaEmprestitos.sql");
	private static final String CONSULTA_EMPRESTITOS_HIS_SELECT 		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaEmprestitosHIS.sql");
	
	private static final String CONSULTA_GARANTIA_SELECT_WS 				= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaGarantias.sql");
	private static final String CONSULTA_GARANTIA_HIS_SELECT_WS 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaGarantiasHIS.sql");
	
	private static final String CONSULTA_ING_TRANSITO_SELECT_WS		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaIngresoTransito.sql");
	private static final String CONSULTA_ING_TRANSITO_HIS_SELECT_WS		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaIngresoTransitoHis.sql");
	
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_SELECT_WS	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaIngresoTransitoNominal.sql");
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_HIS_SELECT	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaIngresoTransitoNominalHis.sql");
	
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_SELECT_WS	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaIngresoTransitoNominalPresunto.sql");
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_HIS_SELECT_WS	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultaIngresoTransitoNominalPresuntoHis.sql");
	
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_SELECT_WS	    = loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaIngresoTransitoNominalCtaPropia.sql");
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_HIS_SELECT_WS	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaIngresoTransitoNominalCtaPropiaHis.sql");
	
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_SELECT_WS	    = loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaIngresoTransitoNominalPresuntoCtaPropia.sql");
	private static final String CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_HIS_SELECT_WS	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaIngresoTransitoNominalPresuntoCtaPropiaHis.sql");
	
	private static final String CONSULTA_DIVIDENDOS_DINERO_WS			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultarDividendosDinero.sql");
	private static final String CONSULTA_DIVIDENDOS_DINERO_HIS_WS		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolioWS_ConsultarDividendosDineroHIS.sql");
	private static final String SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV = loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_SelectDividendosTBCEDVRV.sql");
	private static final String SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV_HIS = loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_SelectDividendosTBCEDVRVHIS.sql");
	
	private static final String CONSULTA_CLIENTES_SELECT 				= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaClientes.sql");
	private static final String CONSULTA_INSTRUMENTO_RIESGO_SELECT 	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaInstrumentoRiesgo.sql");	
	private static final String CONSULTA_INSTRUMENTO_SELECT 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaInstrumentos.sql");
	private static final String CONSULTA_PORTAFOLIO_SELECT 			= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaPortafolio.sql");
	private static final String CONSULTA_MATRIZ_AGRUPADOR_APV_SELECT 	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaMatrizAgrupadorAPV.sql");
	private static final String CONSULTA_PRODUCTO_AGRUPADOR_SELECT 	= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaProductoAgrupador.sql");
	private static final String CONSULTA_CUENTA_CUSTODIO_SELECT 		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultaCuentaCustodioPortafolioPrimera.sql");
	private static final String CONSULTA_PRODUCTOS_CUSTODIA	 		= loadSql("cl/bcs/custodia/sql/ConsultarCustodiaPortafolio_ConsultarProductos.sql");

	/**************************************************
	 * 					CLASES 
	 * ************************************************/
	public static class Entrada_ConsultaCustodiaPortafolio{
		
		public Long  	p_IdEmpresa;
		public Long  	p_IdUsuario;
		public String   p_IdModulo;
		
		// VARIABLES DEL PROCESO
		public Long 	p_IdCliente; 
		public Long 	p_IdPortafolio;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;
		public Long		p_IdInstrumento;
		public String 	p_Nemotecnico;
		public String 	p_IdBoveda;       //solo actua como filtro, no agrupa x boveda
		public String	p_AgrupaPorBoveda; 
		public String 	p_AgrupaPorRelacion;//S: usa relacion como un criterio mas de agrupacion y la muestra   N: no considera relacion en la agrupacion
		public String	p_SaldosNegativos; 
		public String 	p_SaldosNegativosPresuntos;
		public String   p_FechaConsulta;
		public String  	p_TipoFechaConsulta;  //Consultar por LIQ: Fecha Liquidacion  ADQ: Fecha Adquisicion
		public Long 	p_IdProductoAgrupador;
		public String	p_LlamadaDesdeAnalitica;
		public String	p_SolicitaDividendo;
		
		public Entrada_ConsultaCustodiaPortafolio(){
			this.p_IdCliente=0L;
			this.p_IdPortafolio=0L;
			this.p_IdInstrumento=0L;
			this.p_Nemotecnico="";
			this.p_IdBoveda="";
			this.p_AgrupaPorBoveda="N";
			this.p_AgrupaPorRelacion="N";
			this.p_FechaConsulta="0001-01-01";
			this.p_TipoFechaConsulta="LIQ";
			this.p_LlamadaDesdeAnalitica="N";
			this.p_SaldosNegativos = "N";
			this.p_SaldosNegativosPresuntos="N";
			this.p_SolicitaDividendo="N";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida_ConsultaCustodiaPortafolio{
		public List<Tabla_Salida> P_OUT_TBL;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		
		public Salida_ConsultaCustodiaPortafolio(){
			this.P_OUT_TBL=new ArrayList<Tabla_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
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
		//public String	  IdMonedaInstrumento;	// AV
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
		//public BigDecimal ValorMercado;
		//public BigDecimal MontoMercado;

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
		public BigDecimal DividendoEnDinero;
		
			//(PrecioMercadoCustodia//PrecioCompraCustodia)/ PrecioCompraCustodia
		public BigDecimal ValorMercadoCustodia; //moneda instrumento
		public BigDecimal MontoMercadoCustodia; //moneda sistema
		public BigDecimal DiferenciaMercadoCustodia;		
		public String     EsPagaderoEn;
		public String     FechaRespaldo;
		public BigDecimal InteresCorridoCustodia;		

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
//		public BigDecimal CantidadTranomEgr;
		public BigDecimal ValorPPPTransEgrNom;
		public BigDecimal MontoPPPTransEgrNom;
		public BigDecimal PrecioMercadoTransEgrNom;
		public BigDecimal TasaMercadoTransEgrNom;
		public BigDecimal ValorMercadoTransEgrNom;
		public BigDecimal MontoMercadoTransEgrNom;
		public BigDecimal InteresCorridoTransEgrNom;
		
		//INGRESOS COMPRAS NOMINALES
		public BigDecimal CantidadTransIngNom;
//		public BigDecimal CantidadTranomIng;
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
			this.MontoPrecioPromedio=BigDecimal.ZERO;this.CantidadPrecioPromedio=BigDecimal.ZERO;this.FechaFuente = fechaInvalida;
		}
		
		public String toString() {
			return Utils.printJson(this, true);
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
		}
	}
	
	public static class DatosCorredora{
		public Long IdPersonaCtaPropia;
		public Long IdClienteCtaPropia;
		public String DocumentoIdentidadCtaPropia;
		public String DocumentoIdentidadRutCavali;
		public Long IdPortafolioCtaPropia = 0L;
		public Long IdPortafolioCtaPropiaMesaRF = 0L;
		public Long IdClienteCtaPropiaMesaRF = 0L;
	}
	public static class PersonaDestino{
		public Long IdPersonaDestino;
	}
	
	public static class DividendoDinero{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public BigDecimal Disponible;
		public String IdMoneda;
		public String FechaEfectivaMovimiento;
		
		public DividendoDinero() {
			this.IdMoneda="";
			this.FechaEfectivaMovimiento="";
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
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
	
	public static class SalidaTablaNegocio{
		public String Concepto;
		public String TipoSalida;
		
		public SalidaTablaNegocio(String Concepto,String TipoSalida){
			this.Concepto=Concepto ;
			this.TipoSalida=TipoSalida ;
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
	
	public static class Cliente{
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String DocumentoIdentidadAdicional;
		public Long   IdCliente;
		public Long   IdEmpresa;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class InstrumentoClasificacionRiesgo{
		public String ValorClasificacionRiesgo;
		public Long IdInstrumento;
		public String IdEntidadClasificadoraRiesgo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class Instrumento{
		
		public Long IdInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public Long Tamano;
		public String IdEmisor;
		public String ISIN;
		public Date FechaVencimiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}

		
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
		public Long IdMatrizAgrupadorAPV;
		public String CodigoPortafolio;
		public String CodigoPropositoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class MatrizAgrupadorAPV{
		public long IdMatrizAgrupadorAPV;
		public long IdProductoAgrupador;
		
		public MatrizAgrupadorAPV(){
			this.IdMatrizAgrupadorAPV=0L;
			this.IdProductoAgrupador=0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProductoCustodia{
		public Long IdMatrizAgrupadorAPV;
		public String NombreProductoAgrupador;
		
		public ProductoCustodia(){
			this.IdMatrizAgrupadorAPV=0L;
			this.NombreProductoAgrupador="";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProductoAgrupador{
		public int 	   IdProductoAgrupador;
		public String NombreProductoAgrupador;
		
		public ProductoAgrupador(){
			this.IdProductoAgrupador=0;
			this.NombreProductoAgrupador="";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CuentaCustodio{
		public String Id;
		public Long IdPortafolio;
		public String IdBoveda;
		public Long IdCuentaCustodioPortafolio;
		public String Codigo;
		
		public CuentaCustodio() {
			this.IdCuentaCustodioPortafolio = 0L;
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
	
	/**
	 * Contenedor de una query ya expandida por PlataformaUtils.creaCondicionesQuery()
	 * junto con el mapa de parametros saneado (sin binds que el sql final no referencia).
	 */
	private static class QueryPreparada{
		final String sql;
		final HashMap<String,Object> params;
		QueryPreparada(String sql, HashMap<String,Object> params){
			this.sql = sql;
			this.params = params;
		}
	}

	/**
	 * Expande los macros <<CASE.ALIAS.p_X>> / <<COALESCE.ALIAS.p_X>> / <<FECHACONSULTA.ALIAS.p_X>>
	 * de sqlBase usando PlataformaUtils.creaCondicionesQuery(), y sanea el mapa de parametros
	 * resultante para dejar solo los binds que la query expandida realmente referencia.
	 * Evita repetir en cada bloque del bucle de busqueda el patron
	 * creaCondicionesQuery + replace + saneaParametros del servicio original.
	 *
	 * La expansion es iterativa: creaCondicionesQuery() ubica cada parametro con indexOf(k + ">>"),
	 * o sea encuentra solo la PRIMERA aparicion y deduce de ahi un unico alias de tabla. Cuando el
	 * mismo parametro aparece con dos alias distintos en el mismo .sql (caso de
	 * ConsultarCustodiaPortafolioWS_ConsultarDividendosDinero.sql, con CAR y EVC), una sola pasada
	 * dejaria el segundo macro sin reemplazar como texto literal y rompería la sentencia. Cada vuelta
	 * elimina los macros de un alias y deja que la siguiente encuentre el que sigue; el bucle corta
	 * cuando ya no hay nada que reemplazar. Para los .sql de un solo alias sale en la primera vuelta.
	 */
	private QueryPreparada preparar(String sqlBase, HashMap<String,Object> params){
		String q = sqlBase;
		HashMap<String,Object> aux = new HashMap<String,Object>(params);

		for(int pasada = 0; pasada < MAX_PASADAS_MACROS; pasada++){
			HashMap<String,String> condiciones = PlataformaUtils.creaCondicionesQuery(q, aux);
			if(condiciones.isEmpty()) break;

			String previo = q;
			for(Map.Entry<String,String> entry : condiciones.entrySet()){
				q = q.replace(entry.getKey(), entry.getValue());
			}
			// Sin cambios en la pasada no hay progreso posible: se corta para no ciclar.
			if(q.equals(previo)) break;
		}

		aux = PlataformaUtils.saneaParametros(q, aux);
		return new QueryPreparada(q, aux);
	}

	/**
	 * Valida los parametros de entrada que se inyectan como literal SQL en las queries
	 * a traves de los macros <<CASE.../<<FECHACONSULTA...>> (PlataformaUtils.creaCondicionesQuery
	 * concatena el valor del parametro directo en el texto de la sentencia, sin bind).
	 * Se rechaza aqui, antes de tocar la base de datos, cualquier valor que no calce con el
	 * formato de negocio esperado.
	 */
	private void validarEntrada(Entrada_ConsultaCustodiaPortafolio entrada) throws Exception {
		validarPatron("p_TipoFechaConsulta", entrada.p_TipoFechaConsulta, "^(LIQ|ADQ)$");
		validarPatron("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento, "^[A-Z]{0,4}$");
		validarPatron("p_IdTipoInstrumento", entrada.p_IdTipoInstrumento, "^[A-Z0-9]{0,10}$");
		validarPatron("p_Nemotecnico", entrada.p_Nemotecnico, "^[A-Za-z0-9._-]{0,50}$");
		validarPatron("p_IdBoveda", entrada.p_IdBoveda, "^[A-Z0-9]{0,10}$");
		validarPatron("p_AgrupaPorBoveda", entrada.p_AgrupaPorBoveda, "^[SN]?$");
		validarPatron("p_AgrupaPorRelacion", entrada.p_AgrupaPorRelacion, "^[SN]?$");
		validarPatron("p_SaldosNegativos", entrada.p_SaldosNegativos, "^[SN]?$");
		validarPatron("p_SaldosNegativosPresuntos", entrada.p_SaldosNegativosPresuntos, "^[SN]?$");
		validarPatron("p_SolicitaDividendo", entrada.p_SolicitaDividendo, "^[SN]?$");
		validarPatron("p_FechaConsulta", entrada.p_FechaConsulta, "^(\\d{4}-\\d{2}-\\d{2})?$");
	}

	private void validarPatron(String nombreParametro, String valor, String patron) throws Exception {
		if(valor != null && !valor.matches(patron)){
			throw new Exception("Parametro invalido " + nombreParametro + ": '" + valor + "'");
		}
	}

	public Salida_ConsultaCustodiaPortafolio logica (Connection con, Entrada_ConsultaCustodiaPortafolio entrada) throws Exception {
		logger.info("<<INICIO CONSULTA CUSTODIA PORTAFOLIO WS>>");
		// El macro <<FECHACONSULTA...>> de PlataformaUtils evalua v.equals("LIQ") sin control de nulo:
		// se normaliza aqui al mismo valor por defecto que fija el constructor de la entrada.
		if(entrada.p_TipoFechaConsulta == null || entrada.p_TipoFechaConsulta.isEmpty()) {
			entrada.p_TipoFechaConsulta = "LIQ";
		}
		validarEntrada(entrada);
		SQL sql=new SQL();
		Salida_ConsultaCustodiaPortafolio salida = new Salida_ConsultaCustodiaPortafolio();
		logger.debug("INIT BUSQUEDA PARAMETROS SISTEMA");
		String v_FechaConsulta= "2300-12-31";
		String v_IdBovedaDefecto;
		String v_IdMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String v_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
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
		
		
		logger.debug("END BUSQUEDA PARAMETROS SISTEMA");
		
		logger.debug("INIT AGREGAR SALIDA A LISTA"); 	
		
		List<ProductoCustodia> listaProducto = sql.query(con, CONSULTA_PRODUCTOS_CUSTODIA, new HashMap<>(),ProductoCustodia.class);
		HashMap<Long, Object> HashProductos = Utils.List2Hash(listaProducto, "IdMatrizAgrupadorAPV");	
		
				
		logger.debug("INIT AGREGAR SALIDA A LISTA"); 

		
		logger.debug("INIT BUSQUEDA DATOS CUENTA PROPIA");
		//Obtiene los datos de la corredora(para cuenta propia)
		HashMap<String,Object> InDatosCtaPropia = new HashMap<String,Object>();
		InDatosCtaPropia.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DatosCorredora> v_DatosCtaPropia = sql.query(con,DATOS_CTA_PROPIA_SELECT,InDatosCtaPropia,DatosCorredora.class);
		logger.debug("END BUSQUEDA DATOS CUENTA PROPIA");
		
		//Obtiene los datos de la corredora(para cuenta propia MESA RF)
		List<DatosCorredora> v_DatosCtaPropia_MESA_RF = sql.query(con,DATOS_CTA_PROPIA_MESA_RF,new HashMap<>(),DatosCorredora.class);
		logger.debug("END BUSQUEDA DATOS CUENTA PROPIA MESA RF");
		
		logger.debug("INIT BUSQUEDA DATOS CUENTA RELACIONADOS");
		//Obtiene los relacionados a la corredora
		HashMap<String,Object> InPersonaDestino = new HashMap<String,Object>();	
		//List<PersonaDestino> v_tblRelacionados = sql.query(con,CTA_PROPIA_RELACIONADOS_SELECT,InPersonaDestino,PersonaDestino.class);
		HashMap<Object,Object> hashRelacionados = new HashMap<>();//tils.List2Hash(v_tblRelacionados, "IdPersonaDestino");		
		logger.debug("END BUSQUEDA DATOS CUENTA RELACIONADOS");
		
		if(entrada.p_LlamadaDesdeAnalitica.equals("S") || entrada.p_AgrupaPorBoveda.equals("S")){
			v_IdBovedaDefecto="DCV";
		}else{
			v_IdBovedaDefecto="";
		}
		
		//Tipos de conceptos y salidas de las busquedas		
		List<String> TipoBusqueda = Arrays.asList(TIPO_BUSQUEDAS);
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
		List<ResultadoConsulta> v_tblTrasnIng= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTrasnEgr= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTrasnEgrVenDes= new ArrayList<ResultadoConsulta>();  //  Transitos de Egreso por Ventas Descubiertas
		List<ResultadoConsulta> v_tblTrasnEgrVenDesPresunto= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomIng= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomPresuntoIng = new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomEgr= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblTranomPresuntoEgr = new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblReporteCompra= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblReporteVenta= new ArrayList<ResultadoConsulta>();
		List<ResultadoConsulta> v_tblSubyacente = new ArrayList<ResultadoConsulta>();
		
		Long c = 0L;
		logger.debug("INIT BUSQUEDA DATOS MODULOS DEL SISTEMA");
		for(String Obj : TipoBusqueda){
			
			HashMap<String,Object> InBusqueda = new HashMap<String,Object>();
			InBusqueda.put("p_IdEmpresa", entrada.p_IdEmpresa);
			InBusqueda.put("p_AgrupaPorBoveda", entrada.p_AgrupaPorBoveda);
			InBusqueda.put("p_IdCliente",entrada.p_IdCliente);
			InBusqueda.put("v_FechaConsulta",v_FechaConsulta);
			InBusqueda.put("v_IdBovedaDefecto", v_IdBovedaDefecto);
			InBusqueda.put("p_IdPortafolio",entrada.p_IdPortafolio);
			// Filtros comunes restaurados desde ConsultarCustodiaPortafolio.java (paridad funcional).
			// Se agregan siempre al mapa base: preparar()/saneaParametros() descarta el bind si el
			// .sql final de cada bloque no lo referencia, por lo que es seguro unificarlos aqui.
			InBusqueda.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
			InBusqueda.put("p_IdTipoInstrumento", entrada.p_IdTipoInstrumento);
			InBusqueda.put("p_IdInstrumento", entrada.p_IdInstrumento);
			InBusqueda.put("p_Nemotecnico", entrada.p_Nemotecnico);
			InBusqueda.put("p_TipoFechaConsulta", entrada.p_TipoFechaConsulta);
			InBusqueda.put("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);




			if( !v_DatosCtaPropia_MESA_RF.isEmpty() && (Obj.equals("VENPAC") || Obj.equals("COMPAC") || Obj.equals("TRANOM")) ){
				InBusqueda.put("v_IdClienteCuentaPropia",v_DatosCtaPropia_MESA_RF.get(0).IdClienteCtaPropiaMesaRF);
				InBusqueda.put("v_IdPortafolioCuentaPropia",v_DatosCtaPropia_MESA_RF.get(0).IdPortafolioCtaPropiaMesaRF);
			}
			
			//BUSQUEDA CUSTODIA
			if(Obj.equals("CUS")){
				logger.debug("QUERY CUS");

				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpCus = preparar(CONSULTA_CUSTODIA_SELECT_WS, InBusqueda);
					v_tblCustodia = sql.query(con,qpCus.sql,qpCus.params,ResultadoConsulta.class);

					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						QueryPreparada qpCusPresunto = preparar(CONSULTA_CUSTODIA_PRESUNTO_SELECT_WS, InBusqueda);
						v_tblCustodiaPresunto = sql.query(con,qpCusPresunto.sql,qpCusPresunto.params,ResultadoConsulta.class);
					}


				}else{
					QueryPreparada qpCusHis = preparar(CONSULTA_CUSTODIA_HIS_SELECT_WS, InBusqueda);
					v_tblCustodia = sql.query(con,qpCusHis.sql,qpCusHis.params,ResultadoConsulta.class);
					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						QueryPreparada qpCusPresuntoHis = preparar(CONSULTA_CUSTODIA_PRESUNTO_HIS_SELECT_WS, InBusqueda);
						v_tblCustodiaPresunto = sql.query(con,qpCusPresuntoHis.sql,qpCusPresuntoHis.params,ResultadoConsulta.class);
					}
				}
				
				logger.debug("CUS: TERMINO");
				logger.debug(v_tblCustodia.size());
				v_tblCustodia = procesaTablaResultado(entrada,v_tblCustodia,v_DatosCtaPropia,hashRelacionados,Obj);
				v_tblCustodiaPresunto = procesaTablaResultado(entrada,v_tblCustodiaPresunto,v_DatosCtaPropia,hashRelacionados,Obj);				
				logger.debug(v_tblCustodia.size());
				c += v_tblCustodia.size();
				//Obtiene dividendo en dinero
				if("S".equalsIgnoreCase(entrada.p_SolicitaDividendo)) {
					List<DividendoDinero> dividendoDinero = new ArrayList<DividendoDinero>();
					List<DividendoDinero> dividendoDineroTBCEDVRV = new ArrayList<DividendoDinero>();
					
					{
						// Ambas ramas usan la version propia del WS, con los filtros como macro (sargables)
						// en vez del CASE del .sql compartido, que impedia a HANA podar el CTE y la vista.
						HashMap<String,Object> params = new HashMap<String,Object>();
						params.put("p_IdEmpresa", entrada.p_IdEmpresa);
						params.put("p_IdCliente",entrada.p_IdCliente);
						params.put("p_IdInstrumento", entrada.p_IdInstrumento);
						params.put("p_Nemotecnico", entrada.p_Nemotecnico);
						params.put("p_IdPortafolio", entrada.p_IdPortafolio);
						params.put("p_FechaConsulta", entrada.p_FechaConsulta);

						logger.info("INIT QUERY DIVIDENDOS EN DINERO");
						long t0Dividendos = System.currentTimeMillis();
						QueryPreparada qpDividendos;
						if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
							qpDividendos = preparar(CONSULTA_DIVIDENDOS_DINERO_WS, params);
						} else {
							qpDividendos = preparar(CONSULTA_DIVIDENDOS_DINERO_HIS_WS, params);
						}
						dividendoDinero = sql.queryParallel(con,qpDividendos.sql,qpDividendos.params,DividendoDinero.class);
						logger.info("END QUERY DIVIDENDOS EN DINERO - " + (System.currentTimeMillis() - t0Dividendos) + " ms - " + dividendoDinero.size() + " filas");
					}
					
					{
						String usaDividendosTBCEDVRV = "N";
						
						try	{
							usaDividendosTBCEDVRV= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.DividendosTBCEDVRV_TBCECCRF");
						} catch(Exception ex){
							logger.warn("PARAMETRO PLA.DividendosTBCEDVRV_TBCECCRF NO EXISTE");
						}
						
						if("S".equalsIgnoreCase(usaDividendosTBCEDVRV)) {
							String fechaGoLive = "0001-01-01";
							try	{
								fechaGoLive	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaGoLive");
							} catch(Exception ex){
								logger.warn("PARAMETRO PLA.FechaGoLive NO EXISTE");
							}
							HashMap<String,Object> params = new HashMap<String,Object>();
							params.put("p_IdCliente",entrada.p_IdCliente);
							params.put("p_IdInstrumento", entrada.p_IdInstrumento);
							params.put("p_Nemotecnico", entrada.p_Nemotecnico);
							params.put("p_IdPortafolio", entrada.p_IdPortafolio);
							params.put("p_FechaConsulta", entrada.p_FechaConsulta);
							params.put("FechaGoLive", fechaGoLive);
							
							logger.info("INIT QUERY DIVIDENDOS TBCEDVRV");
							long t0DividendosTBCEDVRV = System.currentTimeMillis();
							if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
								dividendoDineroTBCEDVRV = sql.queryParallel(con,SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV,params,DividendoDinero.class);
							} else {
								dividendoDineroTBCEDVRV = sql.queryParallel(con,SQL_SELECT_DIVIDENDOS_DINERO_TBCEDVRV_HIS,params,DividendoDinero.class);
							}
							logger.info("END QUERY DIVIDENDOS TBCEDVRV - " + (System.currentTimeMillis() - t0DividendosTBCEDVRV) + " ms - " + dividendoDineroTBCEDVRV.size() + " filas");
						}
					}
					
					
					Map<String, BigDecimal> dividendos = dividendoDinero.stream().collect(Collectors.toMap(dividendo->dividendo.IdCliente + "_" + dividendo.IdPortafolio + "_" + dividendo.IdInstrumento + "_" + dividendo.IdMoneda, dividendo->dividendo.Disponible));
					Map<String, BigDecimal> dividendosTBCEDVRV = dividendoDineroTBCEDVRV.stream().collect(Collectors.toMap(dividendo->dividendo.IdCliente + "_" + dividendo.IdPortafolio + "_" + dividendo.IdInstrumento, dividendo->dividendo.Disponible));
					{
						CalcularParidadEntrada in = new CalcularParidadEntrada();
						in.p_IdEmpresa=entrada.p_IdEmpresa;
						in.p_IdTrabajo=0L;

						for(ResultadoConsulta obj : v_tblCustodia) {
							// Se calcula para identificar si hay alguna moneda diferente a la principal y que requiere conversion para sumar el disponible en moneda principal.						
							List<DividendoDinero> dividendos_monedaAdicional = dividendoDinero.stream()
									.filter(x->x.IdCliente.equals(obj.IdCliente))
									.filter(x->x.IdPortafolio.equals(obj.IdPortafolio))
									.filter(x->x.IdInstrumento.equals(obj.IdInstrumento))																			
									.filter(x->!x.IdMoneda.equals(obj.IdMonedaPrincipal))
									.collect(Collectors.toList());							
							if(dividendos_monedaAdicional.size()>0) {
								CalculoEntrada calculo = new CalculoEntrada();
								calculo.IdMonedaOrigen = dividendos_monedaAdicional.get(0).IdMoneda;
								calculo.IdMonedaDestino = obj.IdMonedaPrincipal;
								calculo.Fecha= dividendos_monedaAdicional.get(0).FechaEfectivaMovimiento;
								calculo.IdInterno=0L;
								calculo.IdTrabajo = 0L;
								calculo.IdUso = "";
								calculo.MontoOrigen = dividendos_monedaAdicional.get(0).Disponible;
								calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
								in.p_CalculosEntrada.add(calculo);
							}	
						}							
						//se calcula paridad para la moneda en fecha efectiva del pago
						// CalcularParidad.logica() no valida entrada vacia: hace 2 lecturas de configuracion
						// mas 6 queries siempre, aunque no haya nada que convertir (portafolio integro en
						// moneda de sistema, el caso comun). Se salta la llamada dejando la salida vacia;
						// el consumo de mas abajo ya valida p_out_CalculosSalida.size() > 0.
						CalcularParidadSalida salidaCalculoParidad = new CalcularParidadSalida();
						if(!in.p_CalculosEntrada.isEmpty()){
							CalcularParidad calculoParidad= new CalcularParidad();
							salidaCalculoParidad = calculoParidad.logica(con, in);
						}

						for(ResultadoConsulta obj : v_tblCustodia) {
							BigDecimal dividendo = dividendos.get(obj.IdCliente + "_" + obj.IdPortafolio + "_" + obj.IdInstrumento+"_"+obj.IdMonedaPrincipal);
							BigDecimal dividendoTBCEDVRV = dividendosTBCEDVRV.get(obj.IdCliente + "_" + obj.IdPortafolio + "_" + obj.IdInstrumento);
							
							if(salidaCalculoParidad.p_out_CalculosSalida.size()>0) {
								dividendo=dividendo!=null?dividendo:BigDecimal.ZERO;
								dividendo=dividendo.add(salidaCalculoParidad.p_out_CalculosSalida.get(0).MontoDestino,MathContext.DECIMAL128);
							}
							
							if(dividendo != null) {
								obj.DividendoEnDinero = dividendo;
							}
							if(dividendoTBCEDVRV != null) {
								obj.DividendoEnDinero = obj.DividendoEnDinero.add(dividendoTBCEDVRV, MathContext.DECIMAL128);
							}
						}
					}
				}
			}
			
			
			//BUSQUEDA VENTA PACTO
			if(Obj.equals("VENPAC")){
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpVenPac = preparar(CONSULTA_VENTA_PACTO_SELECT, InBusqueda);
					v_tblVentaPacto = sql.query(con,qpVenPac.sql,qpVenPac.params,ResultadoConsulta.class);
				}else{
					QueryPreparada qpVenPacHis = preparar(CONSULTA_VENTA_PACTO_HIS_SELECT, InBusqueda);
					v_tblVentaPacto = sql.query(con,qpVenPacHis.sql,qpVenPacHis.params,ResultadoConsulta.class);
				}

				c += v_tblVentaPacto.size();
			}
			//BUSQUEDA COMPRA PACTO
			if(Obj.equals("COMPAC")){
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpComPac = preparar(CONSULTA_COMPRA_PACTO_SELECT, InBusqueda);
					v_tblCompraPacto = sql.query(con,qpComPac.sql,qpComPac.params,ResultadoConsulta.class);
				}else{
					QueryPreparada qpComPacHis = preparar(CONSULTA_COMPRA_PACTO_HIS_SELECT, InBusqueda);
					v_tblCompraPacto = sql.query(con,qpComPacHis.sql,qpComPacHis.params,ResultadoConsulta.class);

				}
				c += v_tblCompraPacto.size();
			}

			//BUSQUEDA COMPRA y VENTA SIMULTANEA
			if(Obj.equals("SIM")){
				List<ResultadoConsulta> res = new ArrayList<ResultadoConsulta>();
				//se unieron las consultas de compra y venta en una.
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpSim = preparar(CONSULTA_COMPRA_VENTA_SIMULTANEA_SELECT, InBusqueda);
					res = sql.query(con,qpSim.sql,qpSim.params,ResultadoConsulta.class);
				}else {
					QueryPreparada qpSimHis = preparar(CONSULTA_COMPRA_VENTA_SIMULTANEA_HIS_SELECT, InBusqueda);
					res = sql.query(con,qpSimHis.sql,qpSimHis.params,ResultadoConsulta.class);
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
			
			//BUSCA SUBYACENTES PARA PPPCUSTODIA
			if(Obj.equals("SUB")) {
				List<ResultadoConsulta> res = new ArrayList<ResultadoConsulta>();
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpSub = preparar(CONSULTA_SUBYACENTE, InBusqueda);
					res = sql.query(con,qpSub.sql,qpSub.params,ResultadoConsulta.class);
				}else {
					QueryPreparada qpSubHis = preparar(CONSULTA_SUBYACENTE_HIS, InBusqueda);
					res = sql.query(con,qpSubHis.sql,qpSubHis.params,ResultadoConsulta.class);
				}
				res = procesaTablaResultado(entrada,res,v_DatosCtaPropia,hashRelacionados,"SUB");
				v_tblSubyacente.addAll(res);
			}

			//BUSQUEDA PRESTAMOS
			if(Obj.equals("PRT")){
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpPrt = preparar(CONSULTA_PRESTAMOS_SELECT, InBusqueda);
					v_tblPrestamos = sql.query(con,qpPrt.sql,qpPrt.params,ResultadoConsulta.class);
				}else{
					QueryPreparada qpPrtHis = preparar(CONSULTA_PRESTAMOS_HIS_SELECT, InBusqueda);
					v_tblPrestamos = sql.query(con,qpPrtHis.sql,qpPrtHis.params,ResultadoConsulta.class);
				}

				logger.debug("PRT_VC_CON_CUS: TERMINO");
				logger.debug(v_tblPrestamos.size());
				v_tblPrestamos = procesaTablaResultado(entrada,v_tblPrestamos,v_DatosCtaPropia,hashRelacionados,Obj);
				logger.debug(v_tblPrestamos.size());
				c += v_tblPrestamos.size();
			}

			//BUSQUEDA EMPRESTITOS
			if(Obj.equals("EMP")){
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpEmp = preparar(CONSULTA_EMPRESTITOS_SELECT, InBusqueda);
					v_tblEmprestitos = sql.query(con,qpEmp.sql,qpEmp.params,ResultadoConsulta.class);
				}else{
					QueryPreparada qpEmpHis = preparar(CONSULTA_EMPRESTITOS_HIS_SELECT, InBusqueda);
					v_tblEmprestitos = sql.query(con,qpEmpHis.sql,qpEmpHis.params,ResultadoConsulta.class);
				}

				logger.debug("EMP: TERMINO" );
				logger.debug(v_tblEmprestitos.size());
				v_tblEmprestitos = procesaTablaResultado(entrada,v_tblEmprestitos,v_DatosCtaPropia,hashRelacionados,Obj);
				logger.debug(v_tblEmprestitos.size());
				c += v_tblEmprestitos.size();
			}


			//BUSQUEDA GARANTIA
			if(Obj.equals("GTA")){
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpGta = preparar(CONSULTA_GARANTIA_SELECT_WS, InBusqueda);
					v_tblGarantias = sql.query(con,qpGta.sql,qpGta.params,ResultadoConsulta.class);
				}else{
					QueryPreparada qpGtaHis = preparar(CONSULTA_GARANTIA_HIS_SELECT_WS, InBusqueda);
					v_tblGarantias = sql.query(con,qpGtaHis.sql,qpGtaHis.params,ResultadoConsulta.class);
				}

				logger.debug("GTA: TERMINO");
				logger.debug(v_tblGarantias.size());
				v_tblGarantias = procesaTablaResultado(entrada,v_tblGarantias,v_DatosCtaPropia,hashRelacionados,Obj);
				logger.debug(v_tblGarantias.size());
				c += v_tblGarantias.size();
			}
			
			
			//BUSQUEDA INGRESO/EGRESO TRANSITO con custodia
			if(Obj.equals("TRA")){
				List<ResultadoConsulta> res = null;
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					QueryPreparada qpTra = preparar(CONSULTA_ING_TRANSITO_SELECT_WS, InBusqueda);
					res = sql.query(con,qpTra.sql,qpTra.params,ResultadoConsulta.class);
					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						//v_tblTrasnEgrVenDesPresunto = sql.query(con,CONSULTA_VENTA_DESCUBIERTA_PRESUNTO_SELECT,InBusqueda,ResultadoConsulta.class);
					}
				}else {
					QueryPreparada qpTraHis = preparar(CONSULTA_ING_TRANSITO_HIS_SELECT_WS, InBusqueda);
					res = sql.query(con,qpTraHis.sql,qpTraHis.params,ResultadoConsulta.class);
					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						//v_tblTrasnEgrVenDesPresunto = sql.query(con,CONSULTA_VENTA_DESCUBIERTA_PRESUNTO_HIS_SELECT,InBusqueda,ResultadoConsulta.class);
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
			
			//BUSQUEDA INGRESOS POR VENTAS NOMINALES
			if(Obj.equals("TRANOM")){
				InBusqueda.remove("v_IdPortafolioCuentaPropia");
				List<ResultadoConsulta> res = new ArrayList<ResultadoConsulta>();
				List<ResultadoConsulta> resPresunto = new ArrayList<ResultadoConsulta>();
				if (Fecha.toDate(entrada.p_FechaConsulta).equals(Fecha.toDate(v_FechaSistema)) || Fecha.toDate(entrada.p_FechaConsulta).after(Fecha.toDate(v_FechaSistema))){
					if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
						QueryPreparada qpTranomCtaPropia = preparar(CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_SELECT_WS, InBusqueda);
						res = sql.query(con,qpTranomCtaPropia.sql,qpTranomCtaPropia.params,ResultadoConsulta.class);
					}else {
						QueryPreparada qpTranom = preparar(CONSULTA_ING_TRANSITO_NOMINAL_SELECT_WS, InBusqueda);
						res = sql.query(con,qpTranom.sql,qpTranom.params,ResultadoConsulta.class);
					}

					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
							QueryPreparada qpTranomPresuntoCtaPropia = preparar(CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_SELECT_WS, InBusqueda);
							resPresunto = sql.query(con,qpTranomPresuntoCtaPropia.sql,qpTranomPresuntoCtaPropia.params,ResultadoConsulta.class);
						}else {
							QueryPreparada qpTranomPresunto = preparar(CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_SELECT_WS, InBusqueda);
							resPresunto = sql.query(con,qpTranomPresunto.sql,qpTranomPresunto.params,ResultadoConsulta.class);
						}
					}

				}else{
					if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
						QueryPreparada qpTranomCtaPropiaHis = preparar(CONSULTA_ING_TRANSITO_NOMINAL_CTA_PROPIA_HIS_SELECT_WS, InBusqueda);
						res = sql.query(con,qpTranomCtaPropiaHis.sql,qpTranomCtaPropiaHis.params,ResultadoConsulta.class);
					}
					else {
						QueryPreparada qpTranomHis = preparar(CONSULTA_ING_TRANSITO_NOMINAL_HIS_SELECT, InBusqueda);
						res = sql.query(con,qpTranomHis.sql,qpTranomHis.params,ResultadoConsulta.class);
					}

					if("LIQ".equals(entrada.p_TipoFechaConsulta)) {
						if(entrada.p_IdCliente != null && entrada.p_IdCliente.compareTo(v_DatosCtaPropia.get(0).IdClienteCtaPropia)==0) {
							QueryPreparada qpTranomPresuntoCtaPropiaHis = preparar(CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_CTA_PROPIA_HIS_SELECT_WS, InBusqueda);
							resPresunto = sql.query(con,qpTranomPresuntoCtaPropiaHis.sql,qpTranomPresuntoCtaPropiaHis.params,ResultadoConsulta.class);
						}else {
							QueryPreparada qpTranomPresuntoHis = preparar(CONSULTA_ING_TRANSITO_NOMINAL_PRESUNTO_HIS_SELECT_WS, InBusqueda);
							resPresunto = sql.query(con,qpTranomPresuntoHis.sql,qpTranomPresuntoHis.params,ResultadoConsulta.class);
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
			
		}
		logger.debug("END BUSQUEDA DATOS MODULOS DEL SISTEMA");
		logger.debug("Total :"+c);
		
		//junta todas las carteras
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
		v_tblConsulta.addAll(v_tblReporteCompra);
		v_tblConsulta.addAll(v_tblReporteVenta);
		v_tblConsulta.addAll(v_tblCustodiaPresunto);
		v_tblConsulta.addAll(v_tblTrasnEgrVenDesPresunto);
		v_tblConsulta.addAll(v_tblTranomPresuntoIng);
		v_tblConsulta.addAll(v_tblTranomPresuntoEgr);
		logger.debug("Agrupadas :"+v_tblConsulta.size());
		
		//Hash Distinc de las consultas de carteras
		
		HashMap<Long,Boolean> hashIdClientes = new HashMap<Long,Boolean>();
		HashMap<Long,Boolean> hashIdPortafolios = new HashMap<Long,Boolean>();
		HashMap<Long,Boolean> hashIdInstrumentos = new HashMap<Long,Boolean>();
		logger.debug("INIT SEPARAR IDS");
		for(ResultadoConsulta resultaConsulta : v_tblConsulta){
			if (hashIdClientes.get(resultaConsulta.IdCliente) == null) hashIdClientes.put(resultaConsulta.IdCliente,true);
			if (hashIdPortafolios.get(resultaConsulta.IdPortafolio) == null) hashIdPortafolios.put(resultaConsulta.IdPortafolio,true);
			if (hashIdInstrumentos.get(resultaConsulta.IdInstrumento)  == null) hashIdInstrumentos.put(resultaConsulta.IdInstrumento,true);
		}
		List<Long> idClientes = new ArrayList<Long>(hashIdClientes.keySet()); 
		List<Long> idPortafolios = new ArrayList<Long>(hashIdPortafolios.keySet()); 
		List<Long> idInstrumentos = new ArrayList<Long>(hashIdInstrumentos.keySet());
		logger.debug("END SEPARAR IDS");
		
		//Hash y Lista Cliente
		logger.debug("INIT OBTENER CLIENTES");
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdCliente", idClientes);
		//TODO pasar a tabla temporal
		List<Cliente> listaClientes = sql.query(con,CONSULTA_CLIENTES_SELECT,params,Cliente.class);
		HashMap<Long,Object> hClientes = Utils.List2Hash(listaClientes, "IdCliente");
		params.clear();
		logger.debug("END OBTENER CLIENTES");
		
		//Hash y Lista Factor Riesgo
		logger.debug("INIT OBTENER INSTRUMENTOS RIESGO");
		params.put("IdInstrumento", idInstrumentos);
		List<InstrumentoClasificacionRiesgo> listaInsfactosRiesgo = sql.query(con,CONSULTA_INSTRUMENTO_RIESGO_SELECT,params,InstrumentoClasificacionRiesgo.class);
		HashMap<Long,Object> hlistaInsfactosRiesgo = Utils.List2Hash(listaInsfactosRiesgo, "IdInstrumento");
		params.clear();
		logger.debug("END OBTENER INSTRUMENTOS RIESGO");
		
		logger.debug("INIT GENERAR LISTA CLIENTE INSTRUMENTO"); 
		HashMap<String, ResultadoConsulta> TablaConsultas = new HashMap<String, ResultadoConsulta>();
		List<ClientePortafolioInstrumento> listaClienteInstrumento = new ArrayList<>();
		for(ResultadoConsulta obj : v_tblConsulta){
			String keyObj = obj.IdClasificacionTipoInstrumento+"_"+obj.IdTipoInstrumento+"_"+obj.IdInstrumento+"_"+obj.Nemotecnico+"_"+obj.IdEmisor+"_"+obj.EsFungible+"_"+
							obj.IdCliente+"_"+obj.IdPortafolio+"_"+obj.IdBoveda+"_"+obj.IdCuentaCustodioPortafolio+"_"+obj.NumeroCuentaBoveda+"_"+obj.Relacion + "_" + obj.RegimenTributario +
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
		//logger.debug(listaClienteInstrumento.size());
		//logger.debug(listaClienteInstrumento);
		params.clear();
		params.put("IdPortafolio", idPortafolios);
		List<CuentaCustodio> ListaCuentaCustodio = sql.query(con,CONSULTA_CUENTA_CUSTODIO_SELECT, params,CuentaCustodio.class);
		logger.debug("END OBTENER CUENTA CUSTODIOS"); 
		
		HashMap<String, Object> HashCuentaCustodio = Utils.List2Hash(ListaCuentaCustodio, "Id");
		
		//PREPARACION SALIDA
		//tipo es el tipo key que necesitan keyC keyP
		HashMap<String,ResultadoConsulta> v_tblCustodiaHash= TransformarAHash(v_tblCustodia,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblCustodiaPresuntoHash= TransformarAHash(v_tblCustodiaPresunto,"keyC",HashCuentaCustodio);
		//MSH
		//HashMap<String,ResultadoConsulta> v_tblVentaPactoHash= TransformarAHash(v_tblVentaPacto,"keyP");
		//HashMap<String,ResultadoConsulta> v_tblCompraPactoHash= TransformarAHash(v_tblCompraPacto,"keyP");
		HashMap<String,ResultadoConsulta> v_tblVentaPactoHash= TransformarAHash(v_tblVentaPacto,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblCompraPactoHash= TransformarAHash(v_tblCompraPacto,"keyC",HashCuentaCustodio);
		
		HashMap<String,ResultadoConsulta> v_tblSimulCHash= TransformarAHash(v_tblSimulC,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblSimulVHash= TransformarAHash(v_tblSimulV,"keyC",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblSubyacenteHash= TransformarAHash(v_tblSubyacente,"keyC",HashCuentaCustodio); 
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
		HashMap<String,ResultadoConsulta> v_tblReporteCompraHash= TransformarAHash(v_tblReporteCompra,"keyP",HashCuentaCustodio);
		HashMap<String,ResultadoConsulta> v_tblReporteVentaHash= TransformarAHash(v_tblReporteVenta,"keyP",HashCuentaCustodio);
		
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
		params.put("IdPortafolio", idPortafolios);
		List<Portafolio> ListaPortafolios = sql.query(con,CONSULTA_PORTAFOLIO_SELECT, params,Portafolio.class);
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
			
			ResultadoConsulta ObjtblSimulCTraNom = new ResultadoConsulta();
			ResultadoConsulta ObjtblSimulVTraNom = new ResultadoConsulta();
			ResultadoConsulta ObjtblSub = new ResultadoConsulta();
			CustodiaFondoMutuo custodiaFondoMutuo = new CustodiaFondoMutuo();
			
			boolean ObjValido=false;
			
			
			
			
			
			//valida que los objetos del Hash portafolio e instrumentos sean validos
			
			
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

				 // MSH
				 // ObjtblVentaPacto 	  = v_tblVentaPactoHash.get(key)  		!=null?v_tblVentaPactoHash.get(key):new ResultadoConsulta();
				 // ObjtblCompraPacto 	  = v_tblCompraPactoHash.get(key) 		!=null?v_tblCompraPactoHash.get(key):new ResultadoConsulta();
				 ObjtblVentaPacto 	  = v_tblVentaPactoHash.get(keycus)  		!=null?v_tblVentaPactoHash.get(keycus):new ResultadoConsulta();
				 ObjtblCompraPacto 	  = v_tblCompraPactoHash.get(keycus) 		!=null?v_tblCompraPactoHash.get(keycus):new ResultadoConsulta();
				 
				 ObjtblSimulC 		  = v_tblSimulCHash.get(keycus)   		!=null?v_tblSimulCHash.get(keycus):new ResultadoConsulta();
				 ObjtblSimulV 		  = v_tblSimulVHash.get(keycus)   		!=null?v_tblSimulVHash.get(keycus):new ResultadoConsulta();
				 ObjtblSub 		  	  = v_tblSubyacenteHash.get(keycus) != null? v_tblSubyacenteHash.get(keycus):new ResultadoConsulta();
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
				 ObjtblReporteCompra  = v_tblReporteCompraHash.get(key)		!=null?v_tblReporteCompraHash.get(key):new ResultadoConsulta();
				 ObjtblReporteVenta   = v_tblReporteVentaHash.get(key) 		!=null?v_tblReporteVentaHash.get(key):new ResultadoConsulta();				 
				 ObjtblSimulCTraNom   = v_tblSimulCTraNomHash.get(keycus) 	!=null?v_tblSimulCTraNomHash.get(keycus):new ResultadoConsulta();
				 ObjtblSimulVTraNom   = v_tblSimulVTraNomHash.get(keycus) 	!=null?v_tblSimulVTraNomHash.get(keycus):new ResultadoConsulta();
				 
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
				salidaPrevia.CodigoPortafolio = entrada.p_LlamadaDesdeAnalitica.equals("S")?ObjPort.CodigoPortafolio:ObjPort.CodigoPropositoPortafolio;
				salidaPrevia.CodigoPortafolioWS = ObjPort.CodigoPortafolio;
				salidaPrevia.IdBoveda=ins.IdBoveda;
				salidaPrevia.NumeroCuentaBoveda=ins.NumeroCuentaBoveda.equals("")?ObjCtaCus.Codigo:ins.NumeroCuentaBoveda;
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
			
				
				//sim compra	
				salidaPrevia.CantidadSimulCom =salidaPrevia.CantidadSimulCom.add(ObjtblSimulC.Cantidad);
				salidaPrevia.ValorPPPSimulCom=salidaPrevia.ValorPPPSimulCom.add(ObjtblSimulC.ValorCompra);
				salidaPrevia.MontoPPPSimulCom=salidaPrevia.MontoPPPSimulCom.add(ObjtblSimulC.MontoCompra);
				salidaPrevia.PrecioMercadoSimulCom=salidaPrevia.PrecioMercadoSimulCom.add(ObjtblSimulC.INGPrecioMercadoActual);
				salidaPrevia.TasaMercadoSimulCom=salidaPrevia.TasaMercadoSimulCom.add(ObjtblSimulC.INGTasaMercadoActual);//max
				salidaPrevia.ValorMercadoSimulCom=salidaPrevia.ValorMercadoSimulCom.add(ObjtblSimulC.ValorMercado);
				salidaPrevia.MontoMercadoSimulCom=salidaPrevia.MontoMercadoSimulCom.add(ObjtblSimulC.MontoMercado);
				salidaPrevia.InteresCorridoSimulCom=salidaPrevia.InteresCorridoSimulCom.add(ObjtblSimulC.InteresCorrido);
				
				salidaPrevia.DividendoEnDinero = ObjtblCustodia.DividendoEnDinero;
				
				
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
//				salidaPrevia.CantidadTranomIng =salidaPrevia.CantidadTranomIng.add(ObjtblTranomIng.Cantidad);
//				salidaPrevia.CantidadTransIng =salidaPrevia.CantidadTransIng.add(salidaPrevia.CantidadTranomIng);
				
				salidaPrevia.ValorPPPTransIng=salidaPrevia.ValorPPPTransIng.add(ObjtblTrasnIng.ValorCompra);
				salidaPrevia.MontoPPPTransIng=salidaPrevia.MontoPPPTransIng.add(ObjtblTrasnIng.MontoCompra);
				salidaPrevia.PrecioMercadoTransIng=salidaPrevia.PrecioMercadoTransIng.add(ObjtblTrasnIng.INGPrecioMercadoActual);
				salidaPrevia.TasaMercadoTransIng=salidaPrevia.TasaMercadoTransIng.add(ObjtblTrasnIng.INGTasaMercadoActual);//max
				salidaPrevia.ValorMercadoTransIng=salidaPrevia.ValorMercadoTransIng.add(ObjtblTrasnIng.ValorMercado);
				salidaPrevia.MontoMercadoTransIng=salidaPrevia.MontoMercadoTransIng.add(ObjtblTrasnIng.MontoMercado);
				salidaPrevia.InteresCorridoTransIng=salidaPrevia.InteresCorridoTransIng.add(ObjtblTrasnIng.InteresCorrido);
				
				//Egreso de transito
				salidaPrevia.CantidadTransEgr =salidaPrevia.CantidadTransEgr.add(ObjtblTrasnEgr.Cantidad);
//				salidaPrevia.CantidadTranomEgr =salidaPrevia.CantidadTranomEgr.add(ObjtblTranomEgr.Cantidad);
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
						( ins.NumeroCuentaBoveda.equals("")?ObjCtaCus.Codigo:ins.NumeroCuentaBoveda)+ins.Relacion+salidaPrevia.IdMonedaInstrReajuste+salidaPrevia.IdMonedaEmision+ObjPro.NombreProductoAgrupador+
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
					//paramsRegIn.NemoSVS = "";
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
		//HashMap<String, Object> hashSalidaPrecioTasa = Utils.List2Hash(salidaPrecioTasa.P_OUT_PRECIOSTASAS, "Nemotecnico");

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
		if(entrada.p_SaldosNegativos.equalsIgnoreCase("S")) {
			out_tbl = out_tbl.stream()
					.filter(p -> p.TotalPosicion.compareTo(BigDecimal.ZERO) < 0 ||
							p.TotalPosicionPresunta.compareTo(BigDecimal.ZERO) < 0 ||
							p.CantidadTranomIng.compareTo(BigDecimal.ZERO) > 0)
					.collect(Collectors.toList());
		}
		if(entrada.p_SaldosNegativosPresuntos.equalsIgnoreCase("S")) {
			out_tbl = out_tbl.stream()
					.filter(p -> p.CantidadTransIng.compareTo(BigDecimal.ZERO) > 0 ||
							p.CantidadTransEgr.compareTo(BigDecimal.ZERO) > 0)
					.collect(Collectors.toList());
		}
		// agrega el formato de decimales al Precio de Custodia y Precio de Mercado
		for(Tabla_Salida out : out_tbl) {
			String idMoneda = out.IdMonedaInstrReajuste == null ? "CLP" : out.IdMonedaInstrReajuste;
			out.PPPCustodia = MonedaFormato.formatear(con, out.PPPCustodia, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;	
			out.PPPCustodiaMonedaLocal = MonedaFormato.formatear(con, out.PPPCustodiaMonedaLocal, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;			
			out.PrecioMercadoCustodia = MonedaFormato.formatear(con, out.PrecioMercadoCustodia, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;
			out.PrecioMercadoCustodiaMonedaLocal = MonedaFormato.formatear(con, out.PrecioMercadoCustodiaMonedaLocal, MonedaFormato.PRECIO, idMoneda, out.IdTipoInstrumento,"").valor;
			if(out.IdTipoInstrumento != null && !out.IdTipoInstrumento.isEmpty()) {
				out.ValorMercadoCustodia = MonedaFormato.formatear(con, out.ValorMercadoCustodia, MonedaFormato.MONTO, idMoneda, out.IdTipoInstrumento,"").valor;
				out.MontoMercadoCustodia = MonedaFormato.formatear(con, out.MontoMercadoCustodia, MonedaFormato.MONTO, v_IdMonedaSistema, out.IdTipoInstrumento,"").valor;
			}else {
			out.ValorMercadoCustodia = MonedaFormato.formatear(con, out.ValorMercadoCustodia, MonedaFormato.MONTO, idMoneda,"" ,"").valor;
			out.MontoMercadoCustodia = MonedaFormato.formatear(con, out.MontoMercadoCustodia, MonedaFormato.MONTO, v_IdMonedaSistema,"" ,"").valor;
			}
		}
		logger.debug("END AGREGAR SALIDA A LISTA"); 

		logger.debug("INIT AGREGAR LISTA A SALIDA");
		salida.P_OUT_TBL.addAll(out_tbl);
		logger.debug("END AGREGAR LISTA A SALIDA");
		
		logger.info("TERMINO");
		return salida;
	}
	
	public BigDecimal CalculoDiferenciaMercado(BigDecimal ValorMercado,BigDecimal Cantidad, BigDecimal ValorCompra, BigDecimal CantidadTotal, BigDecimal Tamano){
		
/*		BigDecimal VM_CA   = ValorMercado.subtract(Cantidad);
		BigDecimal CAT_TA  = CantidadTotal.multiply(Tamano);
		BigDecimal COND    = CantidadTotal.compareTo(BigDecimal.ZERO)>0?(CAT_TA):BigDecimal.ONE;
		BigDecimal VC_CA   = ValorCompra.divide(COND,20,RoundingMode.HALF_UP);
		BigDecimal FX      = VM_CA.multiply(VC_CA);
*/
		
		BigDecimal CAT_TA	= CantidadTotal.multiply(Tamano);
		BigDecimal COND		= CantidadTotal.compareTo(BigDecimal.ZERO)>0?(CAT_TA):BigDecimal.ONE;
		BigDecimal VC_CA	= ValorCompra.divide(COND,20,RoundingMode.HALF_UP);
		BigDecimal VM_CA	= Cantidad.multiply(VC_CA); 
		
		BigDecimal FX       = ValorMercado.subtract(VM_CA);
		
		return FX;
	}
	
	public BigDecimal CalculoDiferenciaCustodiaMercado(BigDecimal ValorMercado,BigDecimal Cantidad, BigDecimal ValorCompra, BigDecimal CantidadTotal, BigDecimal Tamano){
			
		BigDecimal CAT_TA	= CantidadTotal.multiply(Tamano);
		BigDecimal COND		= CantidadTotal.compareTo(BigDecimal.ZERO)>0?(CAT_TA):BigDecimal.ONE;
		BigDecimal VC_CA	= ValorCompra.divide(COND,20,RoundingMode.HALF_UP);
		BigDecimal VM_CA	= CantidadTotal.multiply(VC_CA); 
		
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
	
	/*
	public String TipoMoneda(String TipoMoneda,ResultadoConsulta Obj){
		
		String monedaSalida="";
		switch(TipoMoneda){
		case "reajuste" : monedaSalida=Obj.IdMonedaInstrReajuste; break;
		case "emision" : monedaSalida=Obj.IdMonedaEmision; break;
		case "principal" : monedaSalida=Obj.IdMonedaPrincipal; break;
		}
		
		return monedaSalida;
	}*/
	
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
	
	
	public List<ResultadoConsulta> procesaTablaResultado (Entrada_ConsultaCustodiaPortafolio entrada ,List<ResultadoConsulta> tablaEntrada ,List<DatosCorredora> v_DatosCtaPropia,HashMap<Object, Object> hashRelacionados,String ObjBusqueda) throws Exception {
		
		List<ResultadoConsulta> TablaSalida = new ArrayList<>();
		try {
			 List<ResultadoConsulta> tablaTemporal = new ArrayList<>();
			 HashMap<String, ResultadoConsulta> TablaCustodia = new HashMap<String, ResultadoConsulta>();
			 			 
			 for(ResultadoConsulta res : tablaEntrada){
					
					ResultadoConsulta tmp = new ResultadoConsulta();
					//Utils.copyPojo(res, tmp);
					//ResultadoConsulta tmp2 = new ResultadoConsulta();
					tmp.copyValuesFrom(res);
					//Agrega Relacionado
					boolean relacionado = ( hashRelacionados.getOrDefault(res.IdPersona, null ) != null);
										
					if(entrada.p_IdProductoAgrupador!=null){
						tmp.Relacion="";
					}else if(res.IdCliente.equals(v_DatosCtaPropia.get(0).IdClienteCtaPropia)){
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
							+"_"+tmp.EsFungible+tmp.IdCliente+"_"+tmp.IdPortafolio+"_"+tmp.IdBoveda+"_"+tmp.IdCuentaCustodioPortafolio+"_"+tmp.NumeroCuentaBoveda
							+"_"+tmp.Relacion+tmp.IdMonedaInstrReajuste+"_"+tmp.IdMonedaEmision+"_"+tmp.IdMonedaPrincipal+"_"+tmp.RegimenTributario+"_"+tmp.AccionCustodia
							+"_"+tmp.IdMatrizAgrupador+"_"+tmp.EsADC;
				
					ResultadoConsulta ObjResultado = new ResultadoConsulta();
					ObjResultado = TablaCustodia.get(KeyTablaSalida);
					
					if(ObjResultado==null){
						ObjResultado = new ResultadoConsulta();
						//Utils.copyPojo(tmp, ObjResultado);
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
	
	/**************************************************
	 * 					CALL 
	 * ************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_ConsultaCustodiaPortafolio in=gson.fromJson(jin.toString() ,Entrada_ConsultaCustodiaPortafolio.class);
		Salida_ConsultaCustodiaPortafolio out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**************************************************
	 * 					MAIN 
	 * ************************************************/
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");

		try (Connection con =  PlataformaUtils.getPropertiesConnection("becbpro1","connections.properties")){
			Gson gson = Utils.getGsonBuilder();
						
			//String jsonPRESURAWS = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"CANALWEB\",\"p_IdModulo\":\"CUS\",\"p_IdCliente\":41253,\"p_IdPortafolio\":0,\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"2019-11-04\",\"p_TipoFechaConsulta\":\"LIQ\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"\"}";

		//	String jsonPRESURATODOSRV = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":245,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":2044,\"p_IdPortafolio\":538,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_AgrupaPorRelacion\":\"S\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"N\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2020-02-04\"}";
			
			String jsonPRESURATODOSRV = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"1646\",\"p_IdModulo\":\"CUS\",\"p_IdCliente\":33938,\"p_IdPortafolio\":2202,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":0,\"p_Nemotecnico\":\"\",\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"\",\"p_AgrupaPorRelacion\":\"N\",\"p_FechaConsulta\":\"2026-09-07\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"\",\"p_SolicitaDividendo\":\"S\"}";
		//	String jsonPRESURATODOSRV = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":23742,\"p_IdPortafolio\":null,\"p_IdClasificacionTipoInstrumento\":\"FM\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_AgrupaPorRelacion\":\"S\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"N\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2019-12-16\"}";
		
			
			Entrada_ConsultaCustodiaPortafolio entrada = gson.fromJson(jsonPRESURATODOSRV, Entrada_ConsultaCustodiaPortafolio.class);
			ConsultarCustodiaPortafolioWS demo = new ConsultarCustodiaPortafolioWS();
			long init = System.currentTimeMillis();
			Salida_ConsultaCustodiaPortafolio salida = demo.logica(con, entrada);
			/*
			for(Tabla_Salida out : salida.P_OUT_TBL) {
				System.out.println(String.format("INSTRUMENTO: %s, CANTIDAD TOTAL: %f, CANTIDAD DISPONIBLE %f", out.Nemotecnico, out.TotalPosicion, out.CantidadCustodia));
				//System.out.println(out);
			}
			*/
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));
			
			System.out.println(salida.P_OUT_TBL.toString());
			//con.commit();
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}

}

