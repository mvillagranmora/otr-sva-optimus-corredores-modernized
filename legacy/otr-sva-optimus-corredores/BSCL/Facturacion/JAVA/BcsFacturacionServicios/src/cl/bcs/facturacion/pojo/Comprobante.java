package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class Comprobante {
	public BigDecimal 			Cantidad;
	public String 				CodigoBancoCustodio;
	public String 				CodigoBancoLiquidador;
	public String 				CodigoBroker;
	public String 				CodigoIFRS;
	public BigDecimal 			DiasPlazo;
	public String 				DireccionFacturacion;
	public String 				DocumentoIdentidad;
	
	public String				EsAPlazo;
	public String 				EsClienteFinal;
	public String 				EsExento;
	public String 				EsVentaCorta;
	
	public java.sql.Timestamp 	FechaComprobante;
	public java.sql.Date 		FechaComprobanteReferencia;
	public java.sql.Timestamp	FechaEmision;
	public java.sql.Timestamp 	FechaEnvio;
	public java.sql.Timestamp 	FechaIngresoRegistro;
	public java.sql.Date 		FechaLiquidacion;
	public java.sql.Timestamp 	FechaRespuestaEnvio;
	public java.sql.Timestamp 	FechaSistema;
	public java.sql.Timestamp 	FechaVencimiento;
	public Long 				FolioComprobante;
	public Long 				FolioComprobanteFacturacionMigracion;
	public Long 				FolioComprobanteReferencia;
	public String 				Glosa;
	
	public String 				IdBolsa;
	public String 				IdBoveda;
	public Long 				IdCarga;
	public String 				IdClasificacionTipoInstrumento;
	public Long 				IdCliente;
	public Long 				IdComprobante;
	public Long 				IdComprobanteReferencia;
	public String 				IdConcepto;
	public String 				IdCondicionLiquidacion;
	public Long   				IdCuentaCustodioPortafolio;
	public String 				IdEmisorSubyacente;
	public Long 				IdEmpleadoEjecutivoPortafolio;
	public Long 				IdEmpresa;
	public String 				IdEstadoComprobante;
	public String 				IdEstadoDTE;
	public String 				IdEstadoSII;
	public String 				IdFormaCustodia;
	public Long 				IdInstrumento;
	public Long 				IdInstrumentoSubyacente;
	public String 				IdMoneda;
	public String 				IdMonedaComprobante;
	public String				IdMonedaLiquidacion;
	public String				IdMonedaSpot;
	public String				IdMonedaNegociacion;
	public Long 				IdPersonaBancoCustodio;
	public Long 				IdPersonaBancoLiquidador;
	public Long 				IdPersonaBroker;
	public Long 				IdPortafolio;
	public Long 				IdRespuestaDTE;
	public Long 				IdSucursalEmpleadoEjecutivoPortafolio;
	public String 				IdTipoCliente;
	public String 				IdTipoComprobante;
	public String 				IdTipoComprobanteReferencia;
	public String 				IdTipoInstrumento;
	public String				IdTipoLiquidacionComisionFactura;
	public String 				IdTipoOperacion;
	public String 				IdTipoProducto;
	public Long 				IdUsuario;
	public BigDecimal 			Intereses;
	
	public String 				LiquidaBancoCustodio;
	
	public String 				MercadoSMV; 
	public String 				ModalidadSMV;
	public String 				ModPrefFact;
	public String 				IdEstadoModPrefFact;
	public BigDecimal 			Monto;
	public BigDecimal 			MontoRaiz;
	public BigDecimal 			MontoCobro;
	public BigDecimal 			MontoComprobante;
	public BigDecimal 			MontoComprobanteCobro;
	public BigDecimal 			MontoComprobanteFinal;
	public BigDecimal 			MontoFinal;
	public BigDecimal 			MontoLiquidacion;
	public BigDecimal 			MontoLiquidacionCobro;
	public BigDecimal 			MontoLiquidacionFinal;
	public BigDecimal 			MontoPagado;
	public String				NemotecnicoInstrumento;
	public String				NemotecnicoSubyacente;
	public String 				NombreEmpleadoEjecutivoPortafolio;
	public String 				NombreLargoCliente;
	public String 				NombreSucursalEmpleadoEjecutivoPortafolio;
	public String 				NombreTipoCliente;
	
	public BigDecimal 			Precio;
	public BigDecimal 			PrecioRaiz;
	
	public BigDecimal 			ReajustesVAC;
	
	public String 				RespuestaDTEEnvioSII;
	public String 				RespuestaLocationDTE;
	public String 				RespuestaPdfDTE;
	public String 				RespuestaXmlDTE;
	
	public BigDecimal 			Tasa;
	public String 				TipoDocumentoIdentidad;
	
	public BigDecimal 			ValorTipoCambio;
	public BigDecimal 			ValorParidad;
	public String 				EsVentaDescubierta;
	public String               CodigoPortafolio;
	public String               IdTipoCanalIngreso;
	public String				Contrapartida;
	public BigDecimal 			TasaTIR;

	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
