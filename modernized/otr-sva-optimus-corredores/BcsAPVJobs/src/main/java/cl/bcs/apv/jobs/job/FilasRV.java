package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Row and payload classes of the legacy {@code IngresarOrdenRVBonificacionFiscal} (IngresarOrdenRVBonificacionFiscal.java:75-206): {@code Orden} is filled from the
 * SQL by column label, {@code Instrumento} is the row of the temporary table and of the price procedure input, {@code ResultadoInstrumento} the row it answers.
 * VERBATIM copies (field names ARE the contract); only {@code toString} uses {@link JsonLegacy}.
 */
public final class FilasRV {

    private FilasRV() {
    }

	public static class Instrumento{
		public Long IdEmpresa;
		public Long IdInstrumento;
		public String IdBolsa;
		public String Fecha;
		public String IdCondicionDeLiquidacion;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	public static class ResultadoInstrumento{
		public Long IdInstrumento; 				
		public String IdBolsa; 					
		public String FechaConsulta;				
		public Date Fecha;						
		public BigDecimal VolumenTransado; 		
		public BigDecimal MontoTransado; 		
		public BigDecimal PrecioBase;			
		public BigDecimal PrecioCierre;			
		public BigDecimal PuntaCompra;			
		public BigDecimal PuntaVenta;			
		public BigDecimal Variacion;			
		public String IdMoneda;					
		public BigDecimal PrecioApertura;		
		public BigDecimal PrecioMayor;			
		public BigDecimal PrecioMenor;			
		public BigDecimal PrecioMedio;
		public BigDecimal PrecioMercado;		
		public BigDecimal TasaMercado;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
		
	}

	public static class Orden {
		public Long			IdInterno;
		public Long			IdCliente;
		public String		FechaRecepcion;
		public String		FechaVigencia;
		public String		FechaEmision;
		public String		FechaOperacion;
		public String		IdTipoVigencia;
		public String		Glosa;
		public BigDecimal	Cantidad;
		public BigDecimal	MontoNeto;
		public BigDecimal	ValorRescate;
		public String		IdBoveda;
		public String		IdFormaCustodia;
		public Long			IdPorcentajeVisibilidad;
		public BigDecimal	PrecioLimite;
		public String		EsFacturaInmediata;
		public String		IdTipoPrecio;
		public Long			IdPortafolio;
		public String		IdTipoLibroOrden;
		public Long			IdLibroOrdenes;
		public String		IdTipoCanalIngreso;
		public String		IdTipoReajuste;
		public String		IdEmisor;
		public String		IdBolsa;
		public String		EspecificaBolsa;
		public String		IdMonedaLiquidacionOrden;
		public String 		IdMonedaOrden; // solo para IF
		public BigDecimal	TipoCambio;
		public Long			IdInstrumento;
		public Long			IdInstrumentoSubyacente;
		public String		IdCondicionDeLiquidacionPlazo;
		public String		IdTipoInstrumento;
		
		public BigDecimal	TasaVentaCorta = BigDecimal.ZERO;
		public BigDecimal	TasaSimultanea = BigDecimal.ZERO;
		public BigDecimal	TasaRFIF = BigDecimal.ZERO;
		
		public Long			PlazoIF = 0L;
		public Long			PlazoSimultanea = 0L;
		public Long			PlazoVentaCorta = 0L;
		
		public String		EsVentaCorta = "N";
		public String		EsOD = "N";
		public String		EsFungible = "N";
		public Long			IdCorredorCorresponsal;
		public Long			IdPersonaAutorizadoDarOrdenes;
		public String		IdTipoCanalDistribucion;
		public BigDecimal	TasaReporte = BigDecimal.ZERO;
		public Long			PlazoReporte = 0L;
		public Long			BaseTasa = 0L; 
		public String		TipoInteres; //C o S (Compuesto Simple) 
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoOperacion;
		public Long 		IdCuentaCustodioPortafolio;
		public String		EsPorMonto = "N";
		public String		EsOrdenEspecial = "N";
		public String		PagoCobroCuentaInversion;
		public Long			IdPreferenciaPagoPortafolio;
		public String		IdMecanismoCaptacion = "";
		public BigDecimal	TasaPromedioReporte = BigDecimal.ZERO;
		public BigDecimal	TasaReportanteReferencia = BigDecimal.ZERO;
		public String 		NemotecnicoSVS = "";
		
		public String 		IdSistemaBursatil;
		public String 		InstrumentoAcreditado = "N";
		
		public String		FormadorMercado = "N";
		public String		EsClienteFinal;
		
		public BigDecimal	MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo = BigDecimal.ZERO;
		public BigDecimal 	PorcentajeAllIn;
		
		public String		AccionOrden;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}			
	}
}
