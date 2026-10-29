package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.apv.jobs.job.JsonLegacy;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.custodia.ConsultarCustodiaPortafolio} (the Colaboradores port carries its input/output as the legacy JSON). The nested data
 * classes are VERBATIM copies of the legacy source (field names ARE the contract, constructor defaults included); only {@code toString} uses
 * {@link JsonLegacy}. Generated from the oracle shims by the Step 2 scratch script; nothing is retyped.
 */
@SuppressWarnings({"unused", "rawtypes"})
public final class ConsultarCustodiaPortafolio {

    private ConsultarCustodiaPortafolio() {
    }

	// legacy cl/bcs/custodia/ConsultarCustodiaPortafolio.java:178-222 (verbatim)
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
		public String	p_LlamadaDesdePatrimonio = "N";
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
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/custodia/ConsultarCustodiaPortafolio.java:224-238 (verbatim)
	public static class Salida_ConsultaCustodiaPortafolio{
		public List<Tabla_Salida> P_OUT_TBL;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		
		public Salida_ConsultaCustodiaPortafolio(){
			this.P_OUT_TBL=new ArrayList<Tabla_Salida>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/custodia/ConsultarCustodiaPortafolio.java:240-746 (verbatim)
	public static class Tabla_Salida{
		public Long IdInterno;
		public Long IdEmpresa; 				// AV
		public String TipoFechaConsulta;  		// AV
		public String FechaCustodia;			// AV
		public String FechaConsulta;

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
		public String	  NombreEjecutivo;
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
		public String EsCustodiaExterna;
		
			//(PrecioMercadoCustodia//PrecioCompraCustodia)/ PrecioCompraCustodia
		public BigDecimal ValorMercadoCustodia; //moneda instrumento
		public BigDecimal MontoMercadoCustodia; //moneda sistema
		public BigDecimal ValorMercadoCustodiaReajuste; 
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
		public BigDecimal CantidadEmprestiInterno;
		public BigDecimal CantidadEmprestiExterno;
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
		public String CodigoRegimenTipoAhorro;
		public String EsNegativa;
		
		public BigDecimal PorcentajeCobroComision;

		public BigDecimal TIRCompraCRM;
		public BigDecimal TasaCompraCRM;
		
		public BigDecimal CuponesYSorteoDeLetras;
		public BigDecimal MontoCompraInicial;
		public BigDecimal InteresesCuponesYSorteoLetras;
		public BigDecimal MontoMercadoTotalCustodia;
		
		public Tabla_Salida(){
			
			this.Rentabilidad=BigDecimal.ZERO;this.CantidadCustodia=BigDecimal.ZERO;this.PPPCustodia=BigDecimal.ZERO;this.ValorPPPCustodia=BigDecimal.ZERO;		
			this.MontoPPPCustodia=BigDecimal.ZERO;this.PrecioMercadoCustodia=BigDecimal.ZERO;this.TasaMercadoCustodia=BigDecimal.ZERO;this.PrecioCompraCustodia=BigDecimal.ZERO;this.ValorMercadoCustodia=BigDecimal.ZERO;this.MontoMercadoCustodia=BigDecimal.ZERO; this.DiferenciaMercadoCustodia=BigDecimal.ZERO;this.INGPrecioPromedioCompra = BigDecimal.ZERO;this.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
			this.InteresCorridoCustodia=BigDecimal.ZERO;this.DividendoEnDinero=BigDecimal.ZERO;this.Tamano=1L;this.CantidadTotal=BigDecimal.ZERO;this.ValorCompra=BigDecimal.ZERO;this.MontoCompra=BigDecimal.ZERO;this.ValorTotalMercado=BigDecimal.ZERO;this.MontoTotalMercado=BigDecimal.ZERO;
			this.CantidadSimulCom=BigDecimal.ZERO;this.PPPSimulCom=BigDecimal.ZERO;this.ValorPPPSimulCom=BigDecimal.ZERO;this.PrecioMercadoSimulCom=BigDecimal.ZERO;this.ValorMercadoSimulCom=BigDecimal.ZERO;this.DiferenciaMercadoSimulCom=BigDecimal.ZERO;this.InteresCorridoSimulCom=BigDecimal.ZERO;this.MontoPPPSimulCom=BigDecimal.ZERO;this.TasaMercadoSimulCom=BigDecimal.ZERO;this.MontoMercadoSimulCom=BigDecimal.ZERO;
			this.CantidadSimulVen=BigDecimal.ZERO;this.PPPSimulVen=BigDecimal.ZERO;this.ValorPPPSimulVen=BigDecimal.ZERO;this.PrecioMercadoSimulVen=BigDecimal.ZERO;this.ValorMercadoSimulVen=BigDecimal.ZERO;this.DiferenciaMercadoSimulVen=BigDecimal.ZERO;this.InteresCorridoSimulVen=BigDecimal.ZERO;this.MontoPPPSimulVen=BigDecimal.ZERO;this.MontoMercadoSimulVen=BigDecimal.ZERO;this.TasaMercadoSimulVen=BigDecimal.ZERO;
			this.CantidadPrestamo=BigDecimal.ZERO;this.PPPPrestamo=BigDecimal.ZERO;this.ValorPPPPrestamo=BigDecimal.ZERO;this.PrecioMercadoPrestamo=BigDecimal.ZERO;this.ValorMercadoPrestamo=BigDecimal.ZERO;this.DiferenciaMercadoPrestamo=BigDecimal.ZERO;this.InteresCorridoPrestamo=BigDecimal.ZERO;this.MontoPPPPrestamo=BigDecimal.ZERO;this.MontoMercadoPrestamo=BigDecimal.ZERO;this.TasaMercadoPrestamo=BigDecimal.ZERO;
			this.CantidadEmpresti=BigDecimal.ZERO;this.CantidadEmprestiInterno=BigDecimal.ZERO;this.CantidadEmprestiExterno=BigDecimal.ZERO;this.PPPEmpresti=BigDecimal.ZERO;this.ValorPPPEmpresti=BigDecimal.ZERO;this.PrecioMercadoEmpresti=BigDecimal.ZERO;this.ValorMercadoEmpresti=BigDecimal.ZERO;this.DiferenciaMercadoEmpresti=BigDecimal.ZERO;this.InteresCorridoEmpresti=BigDecimal.ZERO;this.MontoPPPEmpresti=BigDecimal.ZERO;this.MontoMercadoEmpresti=BigDecimal.ZERO;this.TasaMercadoEmpresti=BigDecimal.ZERO;
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
			this.EsNegativa = "N";
			this.PorcentajeCobroComision = BigDecimal.ZERO;
			this.ValorMercadoCustodiaReajuste= BigDecimal.ZERO;
			this.TIRCompraCRM = BigDecimal.ZERO;
			this.TasaCompraCRM = BigDecimal.ZERO;
			this.MontoCompraInicial = BigDecimal.ZERO;
			this.InteresesCuponesYSorteoLetras = BigDecimal.ZERO;
			this.CuponesYSorteoDeLetras = BigDecimal.ZERO;
			this.MontoMercadoTotalCustodia = BigDecimal.ZERO;
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
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
			this.NombreEjecutivo = obj.NombreEjecutivo;
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
			this.CantidadEmprestiExterno = obj.CantidadEmprestiExterno;
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
			this.EsCustodiaExterna=obj.EsCustodiaExterna;
			this.CodigoRegimenTipoAhorro= obj.CodigoRegimenTipoAhorro;
			this.EsNegativa = obj.EsNegativa;
			this.PorcentajeCobroComision = obj.PorcentajeCobroComision;
			this.TIRCompraCRM = BigDecimal.ZERO;
			this.TasaCompraCRM = BigDecimal.ZERO;
			this.MontoCompraInicial = obj.MontoCompraInicial;
			this.InteresesCuponesYSorteoLetras = obj.InteresesCuponesYSorteoLetras;
			this.CuponesYSorteoDeLetras = obj.CuponesYSorteoDeLetras;
			this.MontoMercadoTotalCustodia = obj.MontoMercadoTotalCustodia;
		}
	}

	// legacy ConsultarCustodiaPortafolio.java: static of the outer class, used by the Tabla_Salida constructor
	public static java.sql.Date fechaInvalida = java.sql.Date.valueOf("0001-01-01");
}
