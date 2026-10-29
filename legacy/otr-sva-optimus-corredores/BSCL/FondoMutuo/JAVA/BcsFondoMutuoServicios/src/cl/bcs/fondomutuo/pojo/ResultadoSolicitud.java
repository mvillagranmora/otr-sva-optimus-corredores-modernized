package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class ResultadoSolicitud {
	public Long IdInterno;
	public Long IdSolicitudAporteRescate;
	public Long IdPortafolio;
	public String TipoSolicitud;
	public String IdSerieFondo;
	public String NombreSerieFondo;
	public String CodigoSerie;
	public String IdTipoMovimientoFondoMutuo;
	public String IdSerieFondoDestino;
	public String IdEmisorDestino;
	public String IndicadorMLT;
	public String Overnight;
	public String CalculoPorMontoCuota;
	public String FechaAceptacion;
	public String FechaOriginalInversion;
	public String FechaIngresoRegistro;
	public String EsPorMontoLiquido;
	public BigDecimal Monto;
	public BigDecimal NumeroCuotas;
	public BigDecimal ValorCuota;
	public BigDecimal CostoUF;
	public BigDecimal CostoMonedaLocal;
	public BigDecimal ValorUFFechaInversion;
	public BigDecimal ParidadFechaInversion;
	public String IdMoneda;
	public BigDecimal MontoComision;
	public BigDecimal MontoPago;
	public BigDecimal MontoComisionLocal;
	public BigDecimal MontoComisionMonedaSistema;
	public BigDecimal MontoPagoLocal;
	public String IdFondo;
	public String Nemotecnico;
	public String NombreFondo;
	public String NombreCorto;
	public String IdEmisorOrigen;
	public String DocumentoIdentidad;
	public String CodigoPortafolio;
	public String NombreCliente;
	public String FechaLiquidacion;
	public String FechaCertificacion;
	public BigDecimal MontoTransferencia;
	public BigDecimal NumeroCuotaTransferencia;
	public String FechaValorCuota;
	public BigDecimal MontoLiquido;
	public BigDecimal MontoLiquidoLocal;
	public BigDecimal Porcentaje;
	public Long FolioContrato;
	public BigDecimal ParidadFechaCertificacion;
	public String TipoCarteraNormativa;
	public String IdMonedaSistema;
	public BigDecimal MontoMonedaSistema;
	public BigDecimal MontoAporte;
	public String IdEmisorAporte;
	public BigDecimal NumeroCuotasAporte;
	public String IdSerieFondoOrigen;
	public Long IdAgrupador;
	public String Switch;
	public String IdTipoCanalIngreso;
	public BigDecimal ValorPivoteLocalConversionLocal;
	public BigDecimal ValorPivoteSistemaConversionSistema;
	public Long FolioCartera;
	public String toString() {
		return Utils.printJson(this, false);
	}
}