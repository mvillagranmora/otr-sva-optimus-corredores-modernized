package cl.bcs.contabilidad.domain.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * SAP Business One queries feeding the daily FI/BPC interface (CONT-A-02, CONT-A-10..23).
 *
 * <p>Legacy: the {@code CreaBroad*}, {@code CreaDescriptivasBroad}, {@code CreaCuentasBPC} and
 * {@code CreaMovimientoBPC} classes ran {@code ConsultaBroadCuenta.sql}, {@code ConsultaBroadMovimiento.sql},
 * {@code ConsultaBroadSaldo.sql}, {@code ConsultaDesEntidad.sql}, {@code ConsultaDesTipoCuenta.sql},
 * {@code ConsultaDesCentroCosto.sql}, {@code ConsultaCuentaBPC.sql} and {@code ConsultaMovimientoBPC.sql}
 * against the SAP B1 database. Only the movimiento, saldo and movimiento-BPC queries take
 * {@code :FechaEjecucion}; the rest take no parameter.
 *
 * <p>Rows are returned in query order. Field values are passed through untouched, including {@code null}
 * (the files print a null value as the text {@code null}, CONT-A-25).
 */
public interface InterfazSapSource {

    /** ConsultaBroadCuenta.sql (no parameters). */
    List<BroadCuenta> cuentasBroad();

    /** ConsultaBroadMovimiento.sql with {@code :FechaEjecucion}. */
    List<BroadMovimiento> movimientosBroad(LocalDate fechaEjecucion);

    /** ConsultaBroadSaldo.sql with {@code :FechaEjecucion}. */
    List<BroadSaldo> saldosBroad(LocalDate fechaEjecucion);

    /** ConsultaDesEntidad.sql (no parameters). */
    List<Entidad> entidades();

    /** ConsultaDesTipoCuenta.sql (no parameters). */
    List<TipoCuenta> tiposCuenta();

    /** ConsultaDesCentroCosto.sql (no parameters). */
    List<CentroCosto> centrosCosto();

    /** ConsultaCuentaBPC.sql (no parameters). */
    List<CuentaBpc> cuentasBpc();

    /** ConsultaMovimientoBPC.sql with {@code :FechaEjecucion}. */
    List<MovimientoBpc> movimientosBpc(LocalDate fechaEjecucion);

    /** Row of ConsultaBroadCuenta.sql (legacy {@code CreaBroadCuentas.BroadCuenta}). */
    record BroadCuenta(
            String codEntidad,
            String codCtaContable,
            String descripcion,
            String tipoCuenta,
            String cuentaGrupo,
            String codMonedaContable,
            String indCtaConfidencial,
            String indCtaAsociada,
            String indCtaMonetaria,
            String indRelevanteFlujoCaja,
            String indCtaAfiliada) {
    }

    /**
     * Row of ConsultaBroadMovimiento.sql (legacy {@code CreaBroadMovimiento.BroadMovimiento}).
     * {@code numRegistro} is read but never written: the file carries the contador instead (CONT-A-04).
     */
    record BroadMovimiento(
            String codInstitucion,
            String codAplicativo,
            String codEntidad,
            LocalDate fecDia,
            Integer hraTransaccion,
            String numOperacion,
            String numPosOperacion,
            String numRegistro,
            String codCtaContable,
            String codTipoCuenta,
            String codProductoContable,
            String codCentroCosto,
            LocalDate fecDocumento,
            String nroSerieDoc,
            String nroDoc,
            String codAfiliada,
            String tipCargoAbono,
            String codMonedaContable,
            BigDecimal montoTransaccionContable,
            String indAnulado,
            String indOpeAfiliada,
            String filler01,
            String filler02) {
    }

    /**
     * Row of ConsultaBroadSaldo.sql (legacy {@code CreaBroadSaldo.BroadSaldo}).
     * {@code numRegistro} is read but never written (CONT-A-04).
     */
    record BroadSaldo(
            String codInstitucion,
            String codAplicativo,
            String codEntidad,
            LocalDate fecDia,
            String numRegistro,
            String codCtaContable,
            String codTipoCuenta,
            String codProductoContable,
            String codCentroCosto,
            String codMonedaContable,
            BigDecimal montoSaldoContable,
            String filler01,
            String filler02) {
    }

    /** Row of ConsultaDesEntidad.sql. */
    record Entidad(String codEntidad, String descripcion) {
    }

    /** Row of ConsultaDesTipoCuenta.sql. */
    record TipoCuenta(String tipoCuenta, String descripcion) {
    }

    /** Row of ConsultaDesCentroCosto.sql. */
    record CentroCosto(String codccPro, String descripcion, String tipo) {
    }

    /** Row of ConsultaCuentaBPC.sql (legacy {@code CreaCuentasBPC.CuentaBPC}). */
    record CuentaBpc(
            String codigoSociedad,
            String cuentaOperativa,
            String descripcion,
            String tipoCuenta,
            String cuentaGrupo) {
    }

    /** Row of ConsultaMovimientoBPC.sql (legacy {@code CreaMovimientoBPC.MovimientoBPC}). */
    record MovimientoBpc(
            String codigoSociedad,
            String cuentaMayor,
            String varianteDeEjercicio,
            String ejercicioPeriodo,
            String nroDocumentoContable,
            String nroApunteContable,
            String segmento,
            String ledger,
            String socGlAsociada,
            String moneda,
            BigDecimal totalContabDebe,
            BigDecimal totalContaHaber,
            String claseDeRegistro,
            String indicadorAnulacion) {
    }
}
