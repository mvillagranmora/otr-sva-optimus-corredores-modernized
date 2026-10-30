package cl.bcs.canalagente.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * JSON form of the pieces of {@code ConsultarCartolaDeMovimiento} (module CuentaInversion) this service touches, with the names of the legacy POJOs. Only the fields
 * CanalAgente reads are declared: the rest of the answer is ignored when it is read (as Gson ignores unknown fields). The classes are public because the SQL executor reads
 * the fields by reflection from another package.
 */
public final class CartolaJson {

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

    /** Legacy {@code CartolaDeMovimiento}, the fields CanalAgente uses. */
    public static class Movimiento {
        public Long IdCargoAbonoMovimiento;
        public Date FechaSistema;
        public String NombreCliente;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public String IdConcepto;
        public String NombreConcepto;
        public Date FechaLiquidacionOperacion;
        public String NombreTipoMovimiento;
        public String NombreTipoProducto;
        public String NombreTipoOperacion;
        public String NombreTipoComprobante;
        public Long IdComprobante;
        public String IdEstado;
        public String NombreEstado;
        public BigDecimal Cargo;
        public BigDecimal Abono;
        public BigDecimal SaldoSimulado;
        public Long IdMatrizAgrupador;
        public String EsADC;
    }
}
