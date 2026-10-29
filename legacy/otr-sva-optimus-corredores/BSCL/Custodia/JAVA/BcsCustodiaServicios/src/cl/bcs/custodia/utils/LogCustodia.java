package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.custodia.AnularMovimientosCertificados;
import cl.bcs.custodia.AnularMovimientosCertificados.AnularMovimientosCertificadosEntrada;
import cl.bcs.custodia.AnularMovimientosCertificados.Comprobante_Entrada;
import cl.bcs.custodia.AnularMovimientosEnTransito;
import cl.bcs.custodia.AnularMovimientosEnTransito.AnularMovimientosEnTransitoEntrada;
import cl.bcs.custodia.CertificarMovimientosEnTransito;
import cl.bcs.custodia.CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada;
import cl.bcs.custodia.IngresarGarantiaJava;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class LogCustodia {	
	private static HashMap<String,String> SQLS; //MODULO,QUERY PARA CARGAR
	private static FileLoader loader;
	
	static {
		loader = new FileLoader();
		SQLS = new HashMap<>();
		SQLS.put("GenerarMovimientoCustodiaDatosEntrada", loader.loadResourceToString("cl/bcs/custodia/sql/LogCustodia_INS_GenerarMovimientoCustodiaDatosEntrada.sql"));		
	};
	
	public static class ObjMovimiento {
		public Long					IdProcesoMovimientoCustodia;
		public Long					IdGenerarMovimientoCustodia; 
		public String				EstaCertificando; 
		public Long					FolioCustodiaBusqueda;
		public String				IdTipoComprobanteBusqueda; 
		public Long					FolioComprobanteBusqueda; 
		public Long					FolioOperacionRelacionada; 
		public Long					FolioCarteraGarantizada; 
		public Long					IdInstrumentoCarteraGarantizada; 
		public String				EsEspecifica; 
		public Long					IdContraparteGarantia; 
		public String				RazonSocialContraparteGarantia; 
		public String				CuentaContraparteGarantia; 
		public String				IdOrigenGarantia; 
		public Long					IdEspecieGarantia; 
		public Long					IdEmpresa; 
		public Long					IdPais; 
		public java.sql.Date		FechaOperacion; 
		public java.sql.Date		FechaVencimientoOperacion; 
		public Long					PlazoEnDiasOperacion; 
		public String				EsNominalIngreso; 
		public Long					IdClienteIngreso; 
		public Long					IdPortafolioIngreso; 
		public Long					IdCuentaCustodioPortafolioIngreso; 
		public String				EsNominalRetiro; 
		public Long					IdClienteRetiro; 
		public Long					IdPortafolioRetiro; 
		public Long					IdCuentaCustodioPortafolioRetiro; 
		public String				IdTipoProducto; 
		public String				IdTipoCanalIngreso; 
		public String				IdMonedaSistema; 
		public String				IdMonedaAdicional; 
		public java.sql.Date		FechaFactorMonedaAdicional; 
		public BigDecimal			ValorFactorMonedaAdicional; 
		public String				CodigoIFRS; 
		public String				IdConcepto; 
		public String				IdTipoOperacion; 
		public Long					IdGrupoOperacion; 
		public Long					IdOperacion; 
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante; 
		public String				FolioExternoReferencia; 
		public Long					IdMesa; 
		public String				IdBolsa; 
		public Long					IdSucursal; 
		public Long					IdAgente; 
		public String				IdCondicionLiquidacion; 
		public java.sql.Date		FechaLiquidacion; 
		public String				EsIntraday; 
		public String				UsaGarantiaEspecifica; 
		public String				EsVentaCorta; 
		public String				CobraPrimaAlInicio; 
		public String				IdTipoPrestamo; 
		public String				Glosa; 
		public Long					IdUsuario; 
		public String				IdModulo; 
		public String				TipoCarteraCorredor; 
		public String				TipoCarteraNormativa; 
		public java.sql.Date		FechaCurse; 
		public String				TipoFactorAjuste; 
		public BigDecimal			ValorFactorAjusteaFechaCurse; 
		public BigDecimal			CostoEnFactorAjuste; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				Nemotecnico; 
		public String				IdEmisor; 
		public String				NemoSVS; 
		public String				IdTipoReajuste; 
		public java.sql.Date		FechaEmisionInstrumento; 
		public java.sql.Date		FechaVencimientoInstrumento; 
		public String				EsFungible; 
		public String				IdMonedaInstrumento; 
		public Long					Tamano; 
		public String				IdBoveda; 
		public String				CuentaBoveda; 
		public String				IdBovedaEsIncluyente; 
		public String				EsPagaderoEn; 
		public java.sql.Date		FechaFactorMonedaInstrumento; 
		public BigDecimal			ValorFactorMonedaInstrumento; 
		public BigDecimal			Cantidad; 
		public BigDecimal			CorteMinimo; 
		public BigDecimal			ValorRescate;
		public BigDecimal			PrecioSucio;
		public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra; 
		public BigDecimal			MontoCompra; 
		public BigDecimal			MontoCompraMonedaInstrumento; 
		public BigDecimal			MontoCompraMonedaAdicional; 
		public BigDecimal			TasaAgente; 
		public BigDecimal			PrecioAPlazo; 
		public BigDecimal			MontoAPlazo; 
		public BigDecimal			MontoAPlazoMonedaInstrumento; 
		public BigDecimal			MontoAPlazoMonedaAdicional; 
		public String				IdClasificacionTipoInstrumentoRelacionado; 
		public String				IdTipoInstrumentoRelacionado; 
		public Long					IdInstrumentoRelacionado; 
		public String				NemotecnicoRelacionado; 
		public String				IdEmisorRelacionado; 
		public String				NemoSVSRelacionado; 
		public String				IdTipoReajusteRelacionado; 
		public java.sql.Date		FechaEmisionInstrumentoRelacionado; 
		public java.sql.Date		FechaVencimientoInstrumentoRelacionado; 
		public String				EsFungibleRelacionado; 
		public String				IdBovedaEsIncluyenteRelacionado; 
		public String				IdBovedaRelacionado; 
		public String				CuentaBovedaRelacionado; 
		public String				IdMonedaInstrumentoRelacionado; 
		public Long					TamanoRelacionado; 
		public String				EsPagaderoEnRelacionado; 
		public java.sql.Date		FechaFactorMonedaInstrumentoRelacionado; 
		public BigDecimal			ValorFactorMonedaInstrumentoRelacionado; 
		public BigDecimal			CantidadInstrumentoRelacionado; 
		public BigDecimal			CorteMinimoInstrumentoRelacionado; 
		public BigDecimal			ValorRescateInstrumentoRelacionado; 
		public BigDecimal			PrecioCompraInstrumentoRelacionado; 
		public BigDecimal			TasaCompraInstrumentoRelacionado; 
		public BigDecimal			MontoCompraInstrumentoRelacionado; 
		public BigDecimal			MontoCompraMonedaInstrumentoRelacionado; 
		public BigDecimal			MontoCompraMonedaAdicionalRelacionado; 
		public BigDecimal			PrecioAPlazoInstrumentoRelacionado; 
		public BigDecimal			MontoAPlazoInstrumentoRelacionado; 
		public BigDecimal			MontoAPlazoMonedaInstrumentoRelacionado; 
		public BigDecimal			MontoAPlazoMonedaAdicionalRelacionado; 
		public String				MueveGarantiaAutomatica;
		public java.sql.Date		FechaEfectiva; 
		public Long					IdPlaza; 
		public Long					IdFixing; 
		public java.sql.Date		FechaFixing; 
		public String				HoraFixing; 
		public Long					IdReferenciaMercado; 
		public String				IdMonedaPrincipal; 
		public String				IdMonedaSecundaria; 
		public BigDecimal			ValorTipoCambioCierre; 
		public BigDecimal			ValorParidadCierre; 
		public BigDecimal			ValorTipoCambioCosto; 
		public BigDecimal			ValorParidadCosto; 
		public Long					IdSpotAsociado; 
		public Long					IdModalidadCumplimiento; 
		public BigDecimal			MontoMonedaPrincipal; 
		public BigDecimal			MontoMonedaSecundaria; 
		public String				IdMonedaLiquidacion; 
		public BigDecimal			MontoLiquidacion; 
		public BigDecimal			MontoLiquidacionMonedaPrincipal; 
		public BigDecimal			MontoDiferido; 
		public BigDecimal			MontoUtilidadDiferida; 
		public BigDecimal			MontoPerdidaDiferida; 
		public BigDecimal			MontoMargen; 
		public BigDecimal			MontoValorRazonableActivo; 
		public BigDecimal			MontoValorRazonablePasivo; 
		public BigDecimal			MontoValorRazonableNeto; 
		public BigDecimal			TasaValorRazonableActivo; 
		public BigDecimal			TasaValorRazonablePasivo; 
		public BigDecimal			MontoDevengoTipoCambio; 
		public BigDecimal			MontoDevengoTipoCambioAnterior; 
		public BigDecimal			MontoDiferidoDiario; 
		public BigDecimal			MontoDiferidoAcumulado; 
		public BigDecimal			ValorTipoCambioSpot; 
		public BigDecimal			ValorTipoCambioCotizador; 
		public String				EsPuntos; 
		public BigDecimal			TasaPuntosMonedaPrincipal; 
		public BigDecimal			TasaPuntosMonedaSecundaria; 
		public BigDecimal			MontoMatriz; 
		public BigDecimal			TasaDescuentoAnticipo; 
		public BigDecimal			TipoCambioAnticipo; 
		public BigDecimal			MontoARecibirAnticipo; 
		public BigDecimal			TipoCambioObservado; 
		public Long					IdCarga; 
		public Long					IdSecuenciaCarteraMigracion; 
		public String				CodigoPeriodo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	
	public static Long logIngresoRetiroTitulos(Connection conn,IngresoRetiroTitulosEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
		
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		for (IngresoRetiroTitulosEntrada.ObjInstrumento ins : ent.P_INSTRUMENTOS){
			if(ins.Procesar!=null && ins.Procesar.equalsIgnoreCase("N")) continue;
			
			ObjMovimiento ObjEntrada = new ObjMovimiento();		
			ObjEntrada.IdProcesoMovimientoCustodia			= TicketAtencion;
			ObjEntrada.IdGenerarMovimientoCustodia 			= ins.IdGenerarMovimientoCustodia;
						
			ObjEntrada.IdClienteIngreso						= ins.IdCliente;
			ObjEntrada.IdPortafolioIngreso					= ins.IdPortafolio;
			ObjEntrada.IdCuentaCustodioPortafolioIngreso	= ins.IdCuentaCustodioPortafolio;
			ObjEntrada.IdBoveda								= ins.IdBoveda;
			ObjEntrada.CuentaBoveda							= ins.CuentaBoveda;
			
			//Transpasos
			ObjEntrada.IdClienteRetiro						= ins.IdClienteDestino;
			ObjEntrada.IdPortafolioRetiro					= ins.IdPortafolioDestino;
			ObjEntrada.IdCuentaCustodioPortafolioRetiro		= ins.IdCuentaCustodioPortafolioRelacionado;
			ObjEntrada.IdBovedaRelacionado					= ins.IdBovedaRelacionada;
			ObjEntrada.CuentaBovedaRelacionado				= ins.CuentaBovedaRelacionada;
			
			//Operacion
			ObjEntrada.FechaOperacion						= Fecha.toDate(ins.FechaOperacion);
			ObjEntrada.FechaVencimientoOperacion			= Fecha.toDate(ins.FechaVencimiento);
			ObjEntrada.IdTipoCanalIngreso					= ins.IdTipoCanalIngreso;
			ObjEntrada.IdConcepto							= ins.IdConcepto;	
			ObjEntrada.IdTipoOperacion						= ins.IdTipoOperacion;
			ObjEntrada.FolioExternoReferencia				= ins.FolioExternoReferencia;
			ObjEntrada.IdBolsa								= ins.IdBolsa;
			ObjEntrada.IdAgente								= ins.IdAgente;
			ObjEntrada.IdSucursal							= ins.IdSucursal;
			ObjEntrada.IdCondicionLiquidacion				= ins.IdCondicionLiquidacion;
			ObjEntrada.FechaLiquidacion						= Fecha.toDate(ins.FechaLiquidacion);
			ObjEntrada.Glosa								= ins.Glosa;
			ObjEntrada.FolioCustodiaBusqueda				= ins.FolioCartera;
			ObjEntrada.PlazoEnDiasOperacion					= ins.PlazoEnDias;
			
			//Instrumentos
			ObjEntrada.EsNominalIngreso						= ins.IndicadorNominal;
			ObjEntrada.IdClasificacionTipoInstrumento		= ins.IdClasificacionTipoInstrumento;
			ObjEntrada.IdTipoInstrumento					= ins.IdTipoInstrumento;
			ObjEntrada.IdInstrumento						= ins.IdInstrumento;
			ObjEntrada.Nemotecnico							= ins.Nemotecnico;
			ObjEntrada.NemoSVS								= ins.NemoSVS;
			ObjEntrada.IdEmisor								= ins.IdEmisor;
			ObjEntrada.IdTipoReajuste						= ins.IdTipoReajuste;
			ObjEntrada.FechaEmisionInstrumento				= Fecha.toDate(ins.FechaEmisionInstrumento);
			ObjEntrada.FechaVencimientoInstrumento			= Fecha.toDate(ins.FechaVencimientoInstrumento);
			ObjEntrada.EsFungible							= ins.Fungible;
			ObjEntrada.IdMonedaInstrumento					= ins.IdMonedaInstrumento;
			
			//Montos, precios y tasas
			ObjEntrada.Cantidad								= ins.Cantidad;
			ObjEntrada.CorteMinimo							= ins.CorteMinimo;
			ObjEntrada.ValorRescate							= ins.ValorRescate;
			ObjEntrada.PrecioCompra							= ins.Precio;
			ObjEntrada.TasaCompra							= ins.Tasa;
			ObjEntrada.MontoCompra							= ins.Monto;
			ObjEntrada.MontoCompraMonedaInstrumento			= ins.MontoEnMonedaInstrumento;
			ObjEntrada.TasaAgente							= ins.TasaAgente;
			movimientos.add(ObjEntrada);		
		};
		
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		
		return TicketAtencion;
	};
	
	public static Long logEjecutarVencimiento(Connection conn,RetiroCustodia.ObjDatosEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
		
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		
		return TicketAtencion;
	};
	
	public static Long logCertificarMovimientosEnTransito(Connection conn,CertificarMovimientosEnTransitoEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
		
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		for (CertificarMovimientosEnTransito.ObjMovimientos mov : ent.P_MOVIMIENTOSCERTIFICAR){			
			ObjMovimiento ObjEntrada = new ObjMovimiento();
			ObjEntrada.IdProcesoMovimientoCustodia	= TicketAtencion;
			ObjEntrada.IdGenerarMovimientoCustodia 	= mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.FolioCustodiaBusqueda		= mov.IdMovimientoCarteraEnTransito;
			ObjEntrada.IdTipoOperacion				= mov.IdTipoOperacion;
			movimientos.add(ObjEntrada);
		};
		
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		
		return TicketAtencion;
	};
	public static Long logAnularMovimientosEnTransito(Connection conn,AnularMovimientosEnTransitoEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
		
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		for (AnularMovimientosEnTransito.ObjMovimientos mov : ent.P_MOVIMIENTOSANULAR){			
			ObjMovimiento ObjEntrada = new ObjMovimiento();
			ObjEntrada.IdProcesoMovimientoCustodia	= TicketAtencion;
			ObjEntrada.IdGenerarMovimientoCustodia 	= mov.IdMovimientoCarteraTransito;
			ObjEntrada.FolioCustodiaBusqueda		= mov.IdMovimientoCarteraTransito;
			ObjEntrada.IdTipoOperacion				= mov.IdTipoOperacion;
			movimientos.add(ObjEntrada);
		};
		
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		
		return TicketAtencion;
	};
	
	public static Long logAnularMovimientosCertificados(Connection conn,AnularMovimientosCertificadosEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
		
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		for (Comprobante_Entrada mov : ent.P_MOVIMIENTOSANULAR){			
			ObjMovimiento ObjEntrada = new ObjMovimiento();
			ObjEntrada.IdProcesoMovimientoCustodia	= TicketAtencion;
			ObjEntrada.IdGenerarMovimientoCustodia 	= mov.IdMovimientoCarteraTransito;
			ObjEntrada.FolioCustodiaBusqueda		= mov.IdMovimientoCarteraTransito;
			movimientos.add(ObjEntrada);
		};
		
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		
		return TicketAtencion;
	};
	
	public static Long logIngresoGarantia(Connection conn,IngresarGarantiaEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
		
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		for (IngresarGarantiaJava.ObjGarantiaCustodia mov : ent.p_GarantiaCustodia){			
			ObjMovimiento ObjEntrada = new ObjMovimiento();
			ObjEntrada.IdProcesoMovimientoCustodia		= TicketAtencion;			
			ObjEntrada.IdInstrumento 					= mov.IdInstrumento; 
			ObjEntrada.IdMonedaInstrumento 				= mov.IdMoneda; 
			ObjEntrada.Cantidad 						= mov.Cantidad;
			ObjEntrada.ValorRescate 					= mov.ValorRescate;
			ObjEntrada.MontoCompraMonedaInstrumento 	= mov.MontoMonedaInstrumento;
			ObjEntrada.MontoCompra 						= mov.MontoMonedaSistema; 
			ObjEntrada.IdClasificacionTipoInstrumento 	= mov.IdClasificacionTipoInstrumento; 
			ObjEntrada.IdTipoReajuste 					= mov.IdTipoReajuste; 
			ObjEntrada.NemoSVS 							= mov.NemoSVS; 
			ObjEntrada.FechaEmisionInstrumento 			= Fecha.toDate( ( mov.FechaEmisionInstrumento!=null) ? mov.FechaEmisionInstrumento : "0001-01-01" ); 
			ObjEntrada.FechaVencimientoInstrumento 		= Fecha.toDate( (mov.FechaVencimientoInstrumento!=null) ? mov.FechaVencimientoInstrumento : "0001-01-01"); 
			ObjEntrada.IdEmisor 						= mov.IdEmisor; 
			ObjEntrada.PlazoEnDiasOperacion 			= mov.PlazoEnDias; 
			ObjEntrada.EsFungible 						= mov.EsFungible; 
			ObjEntrada.IdClienteIngreso 				= mov.IdCliente; 
			ObjEntrada.IdPortafolioIngreso 				= mov.IdPortafolio; 
			ObjEntrada.IdTipoInstrumento 				= mov.IdTipoInstrumento; 
			ObjEntrada.IdBolsa 							= mov.IdBolsa; 
			ObjEntrada.IdInstrumentoCarteraGarantizada 	= mov.IdInstrumentoGarantizado; 
			ObjEntrada.IdConcepto 						= mov.IdConcepto; 
			ObjEntrada.IdContraparteGarantia 			= mov.IdContraparteGarantia; 
			ObjEntrada.IdTipoOperacion 					= mov.IdTipoOperacion; 
			ObjEntrada.IdOperacion 						= mov.IdOperacion; 
			ObjEntrada.PrecioCompra 					= mov.Precio; 
			ObjEntrada.TasaCompra 						= mov.Tasa;
			ObjEntrada.FolioCarteraGarantizada			= mov.FolioCarteraGarantizada;			
			movimientos.add(ObjEntrada);
		};
		for (IngresarGarantiaJava.ObjGarantiaCuentaInversion mov : ent.p_GarantiaCuentaInversion){			
			ObjMovimiento ObjEntrada = new ObjMovimiento();
			ObjEntrada.IdProcesoMovimientoCustodia	= TicketAtencion;
			ObjEntrada.IdProcesoMovimientoCustodia		= TicketAtencion;	
			ObjEntrada.IdMonedaInstrumento 				= mov.IdMoneda; 
			ObjEntrada.MontoCompraMonedaInstrumento 	= mov.MontoMonedaInstrumento;
			ObjEntrada.MontoCompra 						= mov.MontoMonedaSistema;  
			ObjEntrada.IdClienteIngreso 				= mov.IdCliente; 
			ObjEntrada.IdPortafolioIngreso 				= mov.IdPortafolio; 
			ObjEntrada.IdTipoInstrumento 				= mov.IdTipoInstrumento; 
			ObjEntrada.IdBolsa 							= mov.IdBolsa; 
			ObjEntrada.IdInstrumentoCarteraGarantizada 	= mov.IdInstrumentoGarantizado; 
			ObjEntrada.IdConcepto 						= mov.IdConcepto; 
			ObjEntrada.IdContraparteGarantia 			= mov.IdContraparteGarantia; 
			ObjEntrada.IdTipoOperacion 					= mov.IdTipoOperacion; 
			ObjEntrada.IdOperacion 						= mov.IdOperacion; 	
			movimientos.add(ObjEntrada);
		};
		for (IngresarGarantiaJava.ObjGarantiaEspecie mov : ent.p_GarantiaEspecies){			
			ObjMovimiento ObjEntrada = new ObjMovimiento();
			ObjEntrada.IdProcesoMovimientoCustodia	= TicketAtencion;
			ObjEntrada.IdEspecieGarantia 				= mov.IdEspecie;
			ObjEntrada.IdMonedaInstrumento 				= mov.IdMoneda;
			ObjEntrada.MontoCompra 						= mov.MontoMonedaSistema;
			ObjEntrada.Glosa							= mov.Glosa;
			ObjEntrada.IdClienteIngreso 				= mov.IdCliente; 
			ObjEntrada.IdPortafolioIngreso 				= mov.IdPortafolio; 
			ObjEntrada.IdTipoInstrumento 				= mov.IdTipoInstrumento; 
			ObjEntrada.IdBolsa 							= mov.IdBolsa; 
			ObjEntrada.IdInstrumentoCarteraGarantizada 	= mov.IdInstrumentoGarantizado; 
			ObjEntrada.IdConcepto 						= mov.IdConcepto; 
			ObjEntrada.IdContraparteGarantia 			= mov.IdContraparteGarantia; 
			ObjEntrada.IdTipoOperacion 					= mov.IdTipoOperacion; 
			ObjEntrada.IdOperacion 						= mov.IdOperacion; 
			movimientos.add(ObjEntrada);
		};
		
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		
		return TicketAtencion;
	};
	public static Long logRetirarGarantia(Connection conn,RetirarGarantiaEntrada ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
				
		List<ObjMovimiento> movimientos = new ArrayList<ObjMovimiento>();
		if (!writeLog(conn,movimientos,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		return TicketAtencion;
	};
	public static Long logGenerico (Connection conn) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);			
		return TicketAtencion;
	};
	
	public static Long logCustodia (Connection conn, List<ObjMovimiento> ent) throws SQLException {
		Long TicketAtencion = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdProcesoMovimientoCustodia", 1L).get(0);
				
		if (!writeLog(conn,ent,"GenerarMovimientoCustodiaDatosEntrada")){
			TicketAtencion = 0L;
		}
		return TicketAtencion;
	};
	public static boolean writeLog(Connection conn,List<ObjMovimiento> parametros,String NombreLog) throws SQLException{
		boolean result = true;
		
		try {
			SQL sql = new SQL();
			sql.batch(conn, SQLS.get(NombreLog), parametros, ObjMovimiento.class);	
			
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}		
		return result;
	};
	
};
