package cl.bcs.facturacion.pojo;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;


public class ReporteMatrizComisionesDiarias {
	public Long IdComprobante;
	public Long FolioComprobante ;
	public String TipoComprobante;
	public String ReferenciaComprobante;
	public String Concepto;
	public String DocumentoIdentidad;
	public String Cliente;
	public String CodigoPortafolio;
	public String Ejecutivo;
	public String MedioDePagoRecibimos;
	public String MedioDePagoEntregamos;
	public Timestamp FechaComprobante;
	public Date FechaLiquidacion;
	public String Moneda;
	public BigDecimal Monto;
	public String MonedaLocal;
	public BigDecimal MontoLocal;
	public String MonedaLiquidacion;
	public BigDecimal MontoLiquidacion;
	public String MonedaCobro;
	public BigDecimal MontoCobro;
	public String MonedaLocalCobro;
	public BigDecimal MontoLocalCobro;
	public String MonedaLiquidacionCobro;
	public BigDecimal MontoLiquidacionCobro;
	public BigDecimal MontoFinal;
	public BigDecimal MontoLiquidacionFinal;
	public BigDecimal MontoPagado;
	public String TipoProducto;
	public String TipoOperacion;
	public String EsVentaCorta;
	public BigDecimal ComisionMinima;
	public BigDecimal ComisionCobrada;
	public BigDecimal Derecho;
	public BigDecimal Gasto;
	public BigDecimal Impuesto;
	public BigDecimal Retencion;
	public String NemotecnicoInstrumento;
	public BigDecimal Cantidad;

	public ReporteMatrizComisionesDiarias () {
		this.IdComprobante = 0L;
		this.FolioComprobante = 0L;
		this.TipoComprobante = "";
		this.ReferenciaComprobante = "";
		this.Concepto = "";
		this.DocumentoIdentidad = "";
		this.Cliente = "";
		this.CodigoPortafolio = "";
		this.Ejecutivo = "";
		this.MedioDePagoRecibimos = "";
		this.MedioDePagoEntregamos = "";
		this.FechaComprobante = Timestamp.valueOf("2000-01-01 08:00:00");
		this.FechaLiquidacion = Date.valueOf("2000-01-01");
		this.Moneda = "";
		this.Monto = BigDecimal.ZERO;
		this.MonedaLocal = "";
		this.MontoLocal = BigDecimal.ZERO;
		this.MonedaLiquidacion = "";
		this.MontoLiquidacion = BigDecimal.ZERO;
		this.MonedaCobro  = "";
		this.MontoCobro = BigDecimal.ZERO;
		this.MonedaLocalCobro  = "";
		this.MontoLocalCobro = BigDecimal.ZERO;
		this.MonedaLiquidacionCobro  = "";
		this.MontoLiquidacionCobro = BigDecimal.ZERO;
		this.MontoFinal = BigDecimal.ZERO;
		this.MontoLiquidacionFinal = BigDecimal.ZERO;
		this.MontoPagado = BigDecimal.ZERO;
		this.TipoProducto = "";
		this.TipoOperacion = "";
		this.EsVentaCorta = "";
		this.ComisionMinima = BigDecimal.ZERO;
		this.ComisionCobrada = BigDecimal.ZERO;
		this.Derecho = BigDecimal.ZERO;
		this.Gasto = BigDecimal.ZERO;
		this.Impuesto = BigDecimal.ZERO;
		this.Retencion = BigDecimal.ZERO;
		this.NemotecnicoInstrumento  = "";
		this.Cantidad = BigDecimal.ZERO;
	}
}



