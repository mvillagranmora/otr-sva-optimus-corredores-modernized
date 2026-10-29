package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.apv.jobs.job.JsonLegacy;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.ordenes.CrearOrdenes} (the Colaboradores port carries its input/output as the legacy JSON). The nested data
 * classes are VERBATIM copies of the legacy source (field names ARE the contract, constructor defaults included); only {@code toString} uses
 * {@link JsonLegacy}. Generated from the oracle shims by the Step 2 scratch script; nothing is retyped.
 */
@SuppressWarnings({"unused", "rawtypes"})
public final class CrearOrdenes {

    private CrearOrdenes() {
    }

	// legacy cl/bcs/ordenes/CrearOrdenes.java:172-194 (verbatim)
	public static class CrearOrdenesEntrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public BarraProgreso				p_BarraProgreso;
		public List<ObjGarantiasIn>			p_Garantias;
		public List<ObjCobrosModificados>	p_CobrosModificados;
		public List<Cobro_Fijo>				p_CobrosFijos;
		public List<ObjOrdenesEntrada>		p_Ordenes;
		
		public CrearOrdenesEntrada() {
			p_Garantias = new ArrayList<ObjGarantiasIn>();
			p_CobrosModificados = new ArrayList<ObjCobrosModificados>();
			p_Ordenes = new ArrayList<ObjOrdenesEntrada>();
			p_CobrosFijos = new ArrayList<Cobro_Fijo>();
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:196-215 (verbatim)
	public static class Cobro_Fijo {
		public Long IdInterno; 
		public Long IdInternoCobro;
		public Long IdPortafolio; 
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro; 
		public String FormaCobro; 
		public BigDecimal ValorTopeMinimoOrden; 
		public String IdMoneda; 
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor; 
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:217-237 (verbatim)
	public static class CrearOrdenesSalida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<ObjCOBROS>		P_OUT_COBROS;
		public List<ObjGARANTIAS>	P_OUT_GARANTIAS;
		public List<ObjRESULTADO>	P_OUT_RESULTADO;
		
		public CrearOrdenesSalida() {
			this.P_OUT_COBROS = new ArrayList<ObjCOBROS>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_GARANTIAS = new ArrayList<ObjGARANTIAS>();
			this.P_OUT_RESULTADO = new ArrayList<ObjRESULTADO>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:239-254 (verbatim)
	public static class ObjGarantiasIn {
		public Long			IdInstrumento;
		public Long			IdInstrumentoGarantizado;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		public BigDecimal	Precio;
		public String		IdBolsa;
		public String		IdMoneda;
		public String		IdTipoGarantia;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:256-264 (verbatim)
	public static class ObjCobrosModificados {
		public Long			IdInterno;
		public String		IdCobro;
		public BigDecimal	ValorCobroAplicado;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:266-359 (verbatim)
	public static class ObjOrdenesEntrada {
		public Long			IdTrabajo;
		public Long			IdInterno;
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
		public BigDecimal	PrecioLimite = BigDecimal.ZERO;;
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
		
		public BigDecimal	TasaVentaCorta = BigDecimal.ZERO;
		public BigDecimal	TasaSimultanea = BigDecimal.ZERO;
		public BigDecimal	TasaRFIF = BigDecimal.ZERO;
		
		public Long			PlazoIF = 0L;
		public Long			PlazoSimultanea = 0L;
		public Long			PlazoVentaCorta = 0L;
		
		public String		EsVentaCorta = "N";            /////////////// en duro ?
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
		public String		GeneraSpot = "N";
		
		public BigDecimal	MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo = BigDecimal.ZERO;
		public BigDecimal 	PorcentajeAllIn;
		public String 		FechaConfirmacionPlazo;
		public Long			IdOrdenReferencia;
		public String 		TipoLiquidacionFactura;
		public String		CodigoModoFacturacionPortafolio;
		public Date         FechaVencimiento;
		public String 		CodigoEjecutivoDigitador = "";
					
		public String		IdBloqueaOrden; //id para eliminar de la tabla temporal de bloqueo de ordenes
		
		public String		IngresoDesde = "";

		public String GlosaRefactura = "";
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
		
		public  ObjOrdenesEntrada(){
			 this.FechaConfirmacionPlazo = "0001-01-01";
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:394-454 (verbatim)
	public static class ObjRESULTADO {
		public Long			IdInterno;
		public Long			IdOrden;
		public String		DocumentoIdentidad;
		public String		CodigoPortafolio;
		public Timestamp	FechaOperacionOrden;
		public BigDecimal	Cantidad;
		public BigDecimal	MontoNeto;
		public BigDecimal	MontoLiquidacionOperacion;
		public BigDecimal	MontoLiquidacionOperacionPlazo;
		public BigDecimal	TotalCobros;
		public BigDecimal	MontoTotal;
		public BigDecimal	PrecioOrden;
		public String		Instrumento;
		public String		InstrumentoNegociacion;
		public String		InstrumentoSubyacente;
		public String		NombreTipoOperacion;
		public String		IdTipoOperacion;
		public String		IdMonedaOrden;
		public String		IdMonedaLiquidacionOrden;
		public String		EsVentaCorta;
		public String		AccionOrden;
		public String		IdCondicionDeLiquidacion;
		public String		NombreCondicionDeLiquidacion;
		public String		IdTipoPrecio;
		public String		NombreTipoPrecio;
		public String		IdTipoVigenciaOrden;
		public String		NombreTipoVigenciaOrden;
		public Date			FechaVigencia;
		public Long			Plazo;
		public String		NombreTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public String		EsOD;
		public String		NombreCompleto;
		public BigDecimal	TasaVentaCorta;
		public BigDecimal	PlazoVentaCorta;
		public Date			FechaVencimientoVentaCorta;
		public String		NombreEjecutivoPortafolio;
		public BigDecimal	TasaReporte;
		public BigDecimal	TasaPromedioReporte;
		public Integer		PlazoReporte;
		public Long			BaseTasa;
		public BigDecimal	TasaSimultanea;
		public Long			PlazoSimultanea;
		public Long			IdPreferenciaPagoPortafolio;
		public BigDecimal	PrecioPlazo;
		public BigDecimal	MontoNetoPlazo;
		public BigDecimal	MontoTotalPlazo;
		public BigDecimal	MontoLiquidacionPlazo;
		public BigDecimal	MontoInteresContado;
		public BigDecimal	MontoInteresPlazo;
		public String 		TipoCanalDistribucion;
		
		public String GlosaRefactura = "";
		public String FechaOperacionOriginal = "";
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:361-378 (verbatim)
	public static class ObjCOBROS {
		public Long			IdInterno;
		public Long			IdOrden;
		public String		CodigoCobroClasificacion;
		public String		IdCobro;
		public String		IdMonedaCobro;
		public BigDecimal	ValorCobroPerfil;
		public String		FormaCobro;
		public BigDecimal	MontoMinimo;
		public BigDecimal	ValorCobroAplicado;
		public BigDecimal	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}

	// legacy cl/bcs/ordenes/CrearOrdenes.java:380-392 (verbatim)
	public static class ObjGARANTIAS {
		public Long			FolioGarantia;
		public String		IdMonedaInstrumento;
		public String		Instrumento;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		public String		IdMoneda;
		public String		IdTipoGarantia;
		
		public String toString() {
			return JsonLegacy.texto(this);
		}
	}
}
