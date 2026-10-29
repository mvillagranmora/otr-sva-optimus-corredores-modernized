package cl.bcs.cuentainversion.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Row of the movement query by portfolio/currency (P_OUT_MOVIMIENTOS of ConsultarMovimientoCuentaInversion). Public mutable fields
 * with the legacy names: the JSON contract of the platform and the column labels of the SQL map straight onto them.
 */
public class MovimientoCuentaInversion {
    public Long IdCargoAbonoMovimiento;
    public Date FechaSistema;
    public Timestamp FechaIngresoRegistro;
    public Long IdCuentaInversion;
    public String CodigoCuentaInversion;
    public String NombreCuentaInversion;
    public Long IdCliente;
    public String NombreCliente;
    public Long IdPortafolio;
    public String IdMoneda;
    public String CodigoMoneda;
    public String NombreMoneda;
    public String SimboloMoneda;
    public Integer DecimalesMoneda;
    public String IdTipoMovimiento;
    public String TipoMovimiento;
    public String NombreTipoMovimiento;
    public String IdConcepto;
    public String NombreConcepto;
    public String OrigenConcepto;
    public String Descripcion;
    public String IdCondicionDeLiquidacion;
    public String CodigoCondicionDeLiquidacion;
    public String NombreCondicionDeLiquidacion;
    public Integer PlazoCondicionDeLiquidacion;
    public Date FechaEfectivaMovimiento;
    public BigDecimal Monto;
    public BigDecimal SaldoPorFechaEfectiva;
    public BigDecimal SaldoPorFechaMovimiento;
    public String IdModulo;
    public String IdTipoProducto;
    public String CodigoTipoProducto;
    public String NombreTipoProducto;
    public String IdTipoOperacion;
    public String CodigoTipoOperacion;
    public String NombreTipoOperacion;
    public String AccionTipoOperacion;
    public Long IdOperacion;
    public Timestamp FechaOperacion;
    public Date FechaLiquidacionOperacion;
    public Date FechaVencimientoOperacion;
    public String IdMonedaOperacion;
    public BigDecimal MontoOperacion;
    public BigDecimal TipoCambioOperacion;
    public String IdTipoInstrumento;
    public String NombreTipoInstrumento;
    public String IdClasificacionTipoInstrumento;
    public String NombreClasificacionTipoInstrumento;
    public Long IdInstrumento;
    public BigDecimal Cantidad;
    public String NemotecnicoOriginal;
    public String EsCargoAbonoPorTraspaso;
    public String EsCargoAbonoPorTraspasoTraducido;
    public Long IdCargoAbonoRelacionadoTraspaso;
    public String NombreClienteRelacionado;
    public String CodigoPortafolioRelacionado;
    public String IdTipoComprobante;
    public String CodigoTipoComprobante;
    public String NombreTipoComprobante;
    public Long IdComprobante;
    public String FolioComprobante;
    public String EsVentaCorta;
    public Timestamp FechaComprobante;
    public Long IdComprobanteDetalle;
    public Long IdPersonaPagoCobro;
    public String EsCertificadoAutomatico;
    public Long IdUsuarioCertificador;
    public Timestamp FechaCertificacion;
    public Long IdContraparte;
    public String EsIndicadorBilateral;
    public Long IdEmpresa;
    public String CodigoEmpresa;
    public String NombreEmpresa;
    public String EsIngresoManual;
    public Long IdEvento;
    public Long IdUsuario;
    public String NombreUsuario;
    public String IdEstado = ""; // legacy default (rows added by the service have it empty)
    public String NombreEstado;
    public BigDecimal Cargo;
    public BigDecimal Abono;
    public BigDecimal SaldoSimulado;
    public Long IdMatrizAgrupador;
    public String EsADC;
    public String NombreUsuarioCert;
    public Long IdCarga;
    public String IdEstadoSII;
    public Long IdRespuestaDTE;
    public BigDecimal MayorMenorValor;
    public BigDecimal MontoRetencionGananciaCap;

}
