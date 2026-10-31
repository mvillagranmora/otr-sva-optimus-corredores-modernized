package cl.bcs.canalagente.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON form of the pieces of {@code ConsultarCartolaDeMovimiento} (module CuentaInversion) this service touches, with the names of the legacy POJOs. The
 * cartola POJO is declared whole (see {@link Movimiento}); the unknown fields of the answer are ignored when it is read (as Gson ignores unknown fields). The classes are public because the SQL executor reads
 * the fields by reflection from another package.
 */
public final class CartolaJson {

    /** Gson of the legacy {@code Utils.printJson(obj, false)}. */
    private static final Gson IMPRESION = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private CartolaJson() {
    }

    /** Legacy {@code EntradaConsultarCartolaDeMovimiento}, with the defaults of its constructor. */
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdCuentaInversion;
        public Long p_IdCliente;
        public Long p_IdPortafolio;
        public String p_IdMoneda;
        public String p_IdEstado;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_Validaciones = "S";
        public String p_SaldoPorFechaEfectiva = "S";
        public Long p_IdProductoAgrupador;
        public Long p_IdMatrizAgrupador;
        public String p_EsADC;
        public String p_IncluyeSaldo = "N";
        public String p_IncluyeAnulados = "S";
    }

    /** Legacy {@code SalidaConsultarCartolaDeMovimiento} (the errors and messages of the cartola are not read: D-CAN-04). */
    public static class Salida {
        public List<Movimiento> P_OUT_CARTOLADEMOVIMIENTO;
    }

    /**
     * Legacy {@code CartolaDeMovimiento} with ALL its fields, the types and the ORDER of the legacy POJO (89 fields; CanalAgente reads 21 of them, but the legacy POJO is
     * printed whole in the {@code Parameters:} text of a failed batch INSERT, with the nulls). {@link #toString()} is the legacy {@code Utils.printJson(this, false)}: the
     * Gson of {@code printJson} (serializeSpecialFloatingPointValues, date {@code yyyy-MM-dd HH:mm:ss.SSS}, serializeNulls, HTML escaping ON), which is NOT
     * {@link GsonPlataforma#GSON} (no custom adapters; same output for these types). Known deviation from the legacy: the list of fields is a copy of the legacy POJO, not the
     * POJO itself (it lives in module CuentaInversion, not ported here); if that POJO changes, this class must follow.
     */
    public static class Movimiento {
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
        public String IdEstado;
        public String NombreEstado;
        public BigDecimal Cargo;
        public BigDecimal Abono;
        public BigDecimal SaldoSimulado;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public String NombreUsuarioCert;
        public String IdEstadoSII;
        public Long IdRespuestaDTE;
        public BigDecimal MayorMenorValor;
        public BigDecimal MontoRetencionGananciaCap;

        @Override
        public String toString() {
            return IMPRESION.toJson(this);
        }
    }
}
