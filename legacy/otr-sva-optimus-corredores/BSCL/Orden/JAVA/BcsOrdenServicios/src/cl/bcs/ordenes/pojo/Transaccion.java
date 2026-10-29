package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class Transaccion {
	public Long IdInterno;

	public Long BaseTasa;
	public String Bloqueado;
	
	public BigDecimal Cantidad;
	public String CodigoOperadorContraparte;
	public String CodigoOperadorCorredor;
	
	public String EsAPlazo;
	public String EsBilateral;
	public String EsCuentaPropia;
	public String EsFueraRueda;
	public String EsFungible;
	public String EsVentaCorta;
	
	public java.sql.Date FechaEmision;
	public java.sql.Timestamp FechaIngresoOferta;
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Date FechaLiquidacion;
	public java.sql.Timestamp FechaModificacionOperacion;
	public java.sql.Timestamp FechaOperacion;
	public java.sql.Date FechaParidad;
	public java.sql.Date FechaSistemaCreacion;
	public java.sql.Date FechaTipoCambio;
	public java.sql.Date FechaVencimiento;
	public String Folio;
	public Long FolioCartera;
	
	public java.sql.Time HoraSistemaCreacion;
	
	public String IdBolsa;
	public String IdBoveda;
	public String IdCamara;
	public Long IdCarga;
	public String IdClasificacionTipoInstrumento;
	public Long IdComprobanteTesoreria;
	public String IdCondicionDeLiquidacion;
	public Long IdCorredoraContraparte;
	public Long IdCorredoraCorresponsal;
	public Long IdCorredoraLocal;
	public String IdEmisor;
	public Long IdEmpleado;
	public Long IdEmpresa;
	public String IdEstado;
	public Long IdInstrumento;
	public Long IdInstrumentoSubyacente;
	public Long IdLibroOrdenes;
	public String IdMoneda;
	public String IdMonedaEmision;
	public String IdMonedaLiquidacionCorresponsal;
	public String IdMonedaSubyacente;
	public Long IdProgramaProcesaAutomatico;
	public String IdSistemaBursatil;
	public String IdTipoInstrumento;
	public String IdTipoOperacion;
	public String IdTipoReajuste;
	public Long IdTransaccion;
	public Long IdTransaccionOd;
	public Long IdTransaccionPlazo;
	public Long IdUsuarioBloqueo;
	public Long IdUsuarioReserva;
	public String IndicadorDCV;
	public String IndicadorOrdenDirecta;
	
	public String MarcaNegociacion;
	public BigDecimal MejorPrecioCompra;
	public BigDecimal MejorPrecioVenta;
	public BigDecimal Monto;
	public BigDecimal MontoLiquidacionCorresponsal;
	public String MotivoBloqueo;
	public String MotivoReserva;
	
	public String Nemotecnico;
	public String NemotecnicoSubyacente;
	public String NemotecnicoSVS;
	
	public Long Plazo;
	public BigDecimal Precio;
	
	public String Reservado;
	
	public BigDecimal Tasa;
	public BigDecimal TipoCambioCorresponsal;
	public String TipoInteres;
	
	public BigDecimal ValorParidad;
	public BigDecimal ValorRescate;
	public BigDecimal ValorTipoCambio;	
	
	public BigDecimal Intereses;
	public BigDecimal Reajustes;
	public BigDecimal GastosCorresponsal;
	
	public String BloqueoLiquidacion;
	public BigDecimal MontoRetencion;
	

	

	public String IdGrupoReserva;  		// Identificador de reserva grupal por refacturacion
	public String AccionOrden;         // COMPRA / VENTA         

	public String FlujoOrigen = "INGRESOORDEN";
	
	public BigDecimal PrecioRaiz;
	public BigDecimal MontoRaiz;
	public String IdMonedaRaiz;
	public Long IdInstrumentoNegociacion;
	
	public String EsModificacionMarketData;
	public String EstadoModificacion;
	public Long IdTransaccionHistoricoAnulacion = 0L;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
