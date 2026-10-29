package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

public class ReporteDetallado extends DataReporteGenerico{
	
	public Long			IdCliente;
	public String		CodigoPortafolio;
	public String 		CodigoPortafolioComision;
	public String		FechaComprobante;
	public Long 		NumeroComprobante;
	public String 		TipoComprobante;
	public String		IdClasificacionTipoInstrumento;
	public String 		Accion;
	public BigDecimal 	MontoLiquidacion;
	public BigDecimal	MontoIVA;
	public BigDecimal	MontoComision;
	public BigDecimal	MontoDerecho;
	public String 		AgruparSeccion;
	public Long 		FolioDte;
	public String 		FormatoCobro;

}
