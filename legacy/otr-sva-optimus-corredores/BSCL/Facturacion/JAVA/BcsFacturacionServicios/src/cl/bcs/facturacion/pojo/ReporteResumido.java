package cl.bcs.facturacion.pojo;

import java.math.BigDecimal;

public class ReporteResumido extends DataReporteGenerico {
	
	public String 		PeriodoComprobante;
	public BigDecimal	MontoAcciones;
	public BigDecimal	ComisionAcciones;
	public BigDecimal	MontoIIF;
	public BigDecimal	ComisionIIF;
	public BigDecimal	MontoRF;
	public BigDecimal	ComisionRF;	
	public BigDecimal	MontoMonetario;
	public BigDecimal	ComisionMonetario;	
	public String		TipoComprobante;
	public String 		AgruparDatosCliente;
	public Integer      OrdenFila;
}
