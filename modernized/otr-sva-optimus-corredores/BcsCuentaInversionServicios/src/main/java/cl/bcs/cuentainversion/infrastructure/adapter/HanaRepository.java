package cl.bcs.cuentainversion.infrastructure.adapter;

import cl.bcs.cuentainversion.domain.*;
import cl.bcs.cuentainversion.port.out.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Phase 3: real HANA SQL wiring.
 * All SQL targets SAP HANA (ngdbc driver). H2 is incompatible due to HANA temp
 * tables (#prefix), namespace-qualified identifiers (::), and TO_DATE/TO_TIMESTAMP
 * built-ins. Integration tests are @Disabled until a HANA test instance is available.
 */
@Repository
public class HanaRepository implements HanaCargoAbonoPort, HanaSaldoPort, HanaCobroComisionPort {

    // ── HANA table / view identifiers ──────────────────────────────────────────
    private static final String TBL_CAM =
            "\"BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento\"";
    private static final String TBL_CAM_PASO =
            "\"BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso\"";
    private static final String TBL_CI =
            "\"BSCL.CuentaInversion.BD::CTA.CuentaInversion\"";
    private static final String TBL_COBRO_CAJA =
            "\"BSCL.CuentaInversion.BD::CTA.PatrimonioCaja\"";
    private static final String TBL_COBRO_CUS =
            "\"BSCL.Custodia.BD::CUS.PatrimonioCustodia\"";
    private static final String TBL_CAM_HIST =
            "\"BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimientoHistorico\"";
    private static final String TBL_CONCEPTO =
            "\"BSCL.CuentaInversion.BD::CTA.Concepto\"";
    private static final String TBL_CAG =
            "\"BSCL.CuentaInversion.BD::CTA.CargoAbonoAGenerar\"";
    private static final String SP_CREAR_CARGO_ABONO =
            "\"BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono\"";

    // ── ActualizarSaldoCargoAbono SQL ─────────────────────────────────────────
    private static final String SQL_SELECT_CAM_PARA_ACTUALIZAR =
            "SELECT B.\"IdCuentaInversion\",B.\"IdCargoAbonoMovimiento\",B.\"IdTipoMovimiento\"," +
            "(CASE WHEN B.\"IdTipoMovimiento\"='MOVCAR' THEN -1*B.\"Monto\" ELSE B.\"Monto\" END) AS \"Monto\"," +
            "B.\"Saldo\",B.\"SaldoPorFechaMovimiento\",B.\"FechaSistema\"," +
            "RANK() OVER (PARTITION BY B.\"IdCuentaInversion\"" +
            "             ORDER BY B.\"FechaSistema\",B.\"IdCargoAbonoMovimiento\") AS \"RankFechaMovimiento\"," +
            "B.\"FechaEfectivaMovimiento\"," +
            "RANK() OVER (PARTITION BY B.\"IdCuentaInversion\"" +
            "             ORDER BY B.\"FechaEfectivaMovimiento\",B.\"FechaCertificacion\"," +
            "                      B.\"IdCargoAbonoMovimiento\") AS \"RankFechaEfectiva\"," +
            "B.\"FechaCertificacion\"" +
            " FROM " + TBL_CI + " AS A" +
            " INNER JOIN " + TBL_CAM + " AS B ON B.\"IdCuentaInversion\"=A.\"IdCuentaInversion\"" +
            " WHERE B.\"IdEstado\"='CRT'" +
            "   AND (:todos=1 OR A.\"IdCuentaInversion\" IN (:ids))";

    private static final String SQL_INSERT_CAM_PASO =
            "INSERT INTO " + TBL_CAM_PASO +
            " (\"IdTrabajo\",\"IdCuentaInversion\",\"IdCargoAbonoMovimiento\",\"IdTipoMovimiento\",\"Monto\"," +
            "  \"Saldo\",\"SaldoPorFechaMovimiento\",\"FechaSistema\",\"RankFechaMovimiento\"," +
            "  \"FechaEfectivaMovimiento\",\"RankFechaEfectiva\",\"FechaCertificacion\")" +
            " VALUES(:idTrabajo,:idCuentaInversion,:idCargoAbonoMovimiento,:idTipoMovimiento,:monto," +
            "        :saldo,:saldoPorFechaMovimiento,:fechaSistema,:rankFechaMovimiento," +
            "        :fechaEfectivaMovimiento,:rankFechaEfectiva,:fechaCertificacion)";

    private static final String SQL_SELECT_SALDO_FM =
            "WITH MovimientoAnterior AS (" +
            "  SELECT MOV.\"IdCuentaInversion\",MOV.\"SaldoPorFechaMovimiento\" AS \"SaldoMovimientoAnterior\"," +
            "         FMAX.\"RankMovimientoAnterior\"" +
            "  FROM " + TBL_CAM_PASO + " AS MOV," +
            "       (SELECT \"IdCuentaInversion\",MAX(\"RankFechaMovimiento\") AS \"RankMovimientoAnterior\"" +
            "        FROM " + TBL_CAM_PASO +
            "        WHERE \"FechaSistema\" < :p_FechaMovimiento" +
            "        GROUP BY \"IdCuentaInversion\") FMAX" +
            "  WHERE FMAX.\"IdCuentaInversion\"=MOV.\"IdCuentaInversion\"" +
            "    AND FMAX.\"RankMovimientoAnterior\"=MOV.\"RankFechaMovimiento\"" +
            ")" +
            " SELECT CBM.\"IdCargoAbonoMovimiento\",CBM.\"IdCuentaInversion\",CBM.\"RankFechaMovimiento\"," +
            "        MOVANT.\"RankMovimientoAnterior\",MOVANT.\"SaldoMovimientoAnterior\",CBM.\"Monto\"," +
            "        COALESCE(MOVANT.\"SaldoMovimientoAnterior\",0)+" +
            "        COALESCE(SUM(CBM.\"Monto\") OVER (PARTITION BY CBM.\"IdCuentaInversion\"" +
            "                 ORDER BY CBM.\"FechaSistema\",CBM.\"IdCargoAbonoMovimiento\"" +
            "                 ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW),0) AS \"SaldoMovimiento\"" +
            " FROM " + TBL_CAM_PASO + " AS CBM" +
            " LEFT JOIN MovimientoAnterior AS MOVANT ON MOVANT.\"IdCuentaInversion\"=CBM.\"IdCuentaInversion\"" +
            " WHERE CBM.\"FechaSistema\" >= :p_FechaMovimiento" +
            "   AND CBM.\"IdTrabajo\" = :idTrabajo";

    private static final String SQL_SELECT_SALDO_FE =
            "WITH MovimientoAnterior AS (" +
            "  SELECT MOV.\"IdCuentaInversion\",MOV.\"Saldo\" AS \"SaldoEfectivoAnterior\"," +
            "         FMAX.\"RankEfectivoAnterior\"" +
            "  FROM " + TBL_CAM_PASO + " AS MOV," +
            "       (SELECT \"IdCuentaInversion\",MAX(\"RankFechaEfectiva\") AS \"RankEfectivoAnterior\"" +
            "        FROM " + TBL_CAM_PASO +
            "        WHERE \"FechaEfectivaMovimiento\" < :p_FechaMovimiento" +
            "        GROUP BY \"IdCuentaInversion\") FMAX" +
            "  WHERE FMAX.\"IdCuentaInversion\"=MOV.\"IdCuentaInversion\"" +
            "    AND FMAX.\"RankEfectivoAnterior\"=MOV.\"RankFechaEfectiva\"" +
            ")" +
            " SELECT CBM.\"IdCargoAbonoMovimiento\",CBM.\"IdCuentaInversion\",CBM.\"RankFechaEfectiva\"," +
            "        MOVANT.\"RankEfectivoAnterior\",MOVANT.\"SaldoEfectivoAnterior\",CBM.\"Monto\"," +
            "        COALESCE(MOVANT.\"SaldoEfectivoAnterior\",0)+" +
            "        COALESCE(SUM(CBM.\"Monto\") OVER (PARTITION BY CBM.\"IdCuentaInversion\"" +
            "                 ORDER BY CBM.\"FechaEfectivaMovimiento\",CBM.\"FechaCertificacion\"," +
            "                          CBM.\"IdCargoAbonoMovimiento\"" +
            "                 ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW),0) AS \"SaldoEfectivo\"" +
            " FROM " + TBL_CAM_PASO + " AS CBM" +
            " LEFT JOIN MovimientoAnterior AS MOVANT ON MOVANT.\"IdCuentaInversion\"=CBM.\"IdCuentaInversion\"" +
            " WHERE CBM.\"FechaEfectivaMovimiento\" >= :p_FechaMovimiento" +
            "   AND CBM.\"IdTrabajo\" = :idTrabajo";

    private static final String SQL_UPDATE_SALDO_FM =
            "UPDATE CBM SET CBM.\"SaldoPorFechaMovimiento\"=:saldo" +
            " FROM " + TBL_CAM + " AS CBM" +
            " WHERE CBM.\"IdCargoAbonoMovimiento\"=:idCargoAbonoMovimiento";

    private static final String SQL_UPDATE_SALDO_FE =
            "UPDATE CBM SET CBM.\"Saldo\"=:saldo" +
            " FROM " + TBL_CAM + " AS CBM" +
            " WHERE CBM.\"IdCargoAbonoMovimiento\"=:idCargoAbonoMovimiento";

    // ── SaldoPort SQL ──────────────────────────────────────────────────────────
    private static final String SQL_SALDO_INICIAL_FINAL =
            "SELECT" +
            " MAX(CASE WHEN \"FechaEfectivaMovimiento\"=:fechaDesde AND \"IdEstado\"='CRT'" +
            "          THEN \"Saldo\" END) AS \"SaldoEfectivoInicial\"," +
            " MAX(CASE WHEN \"FechaEfectivaMovimiento\"<=:fechaHasta AND \"IdEstado\"='CRT'" +
            "          THEN \"Saldo\" END) AS \"SaldoEfectivoFinal\"," +
            " MAX(CASE WHEN \"FechaSistema\"=:fechaDesde AND \"IdEstado\"='CRT'" +
            "          THEN \"SaldoPorFechaMovimiento\" END) AS \"SaldoMovimientoInicial\"," +
            " MAX(CASE WHEN \"FechaSistema\"<=:fechaHasta AND \"IdEstado\"='CRT'" +
            "          THEN \"SaldoPorFechaMovimiento\" END) AS \"SaldoMovimientoFinal\"," +
            " SUM(CASE WHEN \"IdEstado\"='PEN' AND \"IdTipoMovimiento\"='MOVCAR' THEN \"Monto\" ELSE 0 END)" +
            "     AS \"MontoCargoPendiente\"," +
            " SUM(CASE WHEN \"IdEstado\"='PEN' AND \"IdTipoMovimiento\"='MOVABN' THEN \"Monto\" ELSE 0 END)" +
            "     AS \"MontoAbonoPendiente\"" +
            " FROM " + TBL_CAM +
            " WHERE \"IdCuentaInversion\" IN (:ids)" +
            "   AND \"IdEstado\" IN ('CRT','PEN')";

    private static final String SQL_SALDO_POR_FECHA_EFECTIVA =
            "SELECT B.\"IdCuentaInversion\",B.\"FechaEfectivaMovimiento\" AS \"FechaEfectiva\"," +
            "       B.\"Saldo\"," +
            "       SUM(CASE WHEN B.\"IdEstado\"='PEN' AND B.\"IdTipoMovimiento\"='MOVCAR'" +
            "                THEN B.\"Monto\" ELSE 0 END) AS \"MontoCargoPendiente\"," +
            "       SUM(CASE WHEN B.\"IdEstado\"='PEN' AND B.\"IdTipoMovimiento\"='MOVABN'" +
            "                THEN B.\"Monto\" ELSE 0 END) AS \"MontoAbonoPendiente\"" +
            " FROM " + TBL_CAM + " AS B" +
            " WHERE B.\"IdCuentaInversion\" IN (:ids)" +
            "   AND B.\"IdEstado\" IN ('CRT','PEN')" +
            "   AND B.\"FechaEfectivaMovimiento\" BETWEEN :fechaDesde AND :fechaHasta" +
            " GROUP BY B.\"IdCuentaInversion\",B.\"FechaEfectivaMovimiento\",B.\"Saldo\"" +
            " ORDER BY B.\"IdCuentaInversion\",B.\"FechaEfectivaMovimiento\"";

    private static final String SQL_MOVIMIENTOS =
            "SELECT M.\"IdCargoAbonoMovimiento\",M.\"IdPortafolio\",M.\"IdMoneda\",M.\"IdEstado\"," +
            "       M.\"IdTipoMovimiento\",M.\"IdConcepto\",M.\"Descripcion\"," +
            "       M.\"FechaEfectivaMovimiento\" AS \"FechaEfectiva\",M.\"FechaSistema\"," +
            "       CASE WHEN M.\"IdTipoMovimiento\"='MOVCAR' THEN -1*M.\"Monto\" ELSE M.\"Monto\" END AS \"Monto\"," +
            "       M.\"Saldo\",M.\"IdModulo\",M.\"IdCarga\",M.\"Nemo\" AS \"NemotecnicoOriginal\"," +
            "       C.\"Nombre\" AS \"NombreConcepto\"" +
            " FROM " + TBL_CAM + " AS M" +
            " LEFT JOIN " + TBL_CONCEPTO + " C ON C.\"IdConcepto\"=M.\"IdConcepto\"" +
            " WHERE M.\"IdCuentaInversion\"=:idCuentaInversion" +
            "   AND ((M.\"FechaEfectivaMovimiento\" BETWEEN :fechaDesde AND :fechaHasta)" +
            "        OR (M.\"FechaSistema\" BETWEEN :fechaDesde AND :fechaHasta))" +
            "   AND (:incluyeAnulados=1 OR M.\"IdEstado\"<>'ANU')" +
            " ORDER BY M.\"FechaSistema\",M.\"IdCargoAbonoMovimiento\"";

    private static final String SQL_MOVIMIENTOS_HISTORICOS =
            "SELECT M.\"IdCargoAbonoMovimiento\",M.\"IdPortafolio\",M.\"IdMoneda\",M.\"IdEstado\"," +
            "       M.\"IdTipoMovimiento\",M.\"IdConcepto\",M.\"Descripcion\"," +
            "       M.\"FechaEfectivaMovimiento\" AS \"FechaEfectiva\",M.\"FechaSistema\"," +
            "       CASE WHEN M.\"IdTipoMovimiento\"='MOVCAR' THEN -1*M.\"Monto\" ELSE M.\"Monto\" END AS \"Monto\"," +
            "       M.\"Saldo\",M.\"IdModulo\",M.\"IdCarga\",M.\"Nemo\" AS \"NemotecnicoOriginal\"," +
            "       '' AS \"NombreConcepto\"" +
            " FROM " + TBL_CAM_HIST + " AS M" +
            " WHERE M.\"IdCuentaInversion\"=:idCuentaInversion" +
            "   AND ((M.\"FechaEfectivaMovimiento\" BETWEEN :fechaDesde AND :fechaHasta)" +
            "        OR (M.\"FechaSistema\" BETWEEN :fechaDesde AND :fechaHasta))" +
            "   AND (:incluyeAnulados=1 OR M.\"IdEstado\"<>'ANU')" +
            " ORDER BY M.\"FechaSistema\",M.\"IdCargoAbonoMovimiento\"";

    private static final String SQL_SALDO_ANTERIOR =
            "SELECT TOP 1 \"Saldo\"" +
            " FROM " + TBL_CAM +
            " WHERE \"IdCuentaInversion\"=:idCuentaInversion" +
            "   AND \"FechaEfectivaMovimiento\" < :fecha" +
            "   AND \"IdEstado\"='CRT'" +
            " ORDER BY \"FechaEfectivaMovimiento\" DESC,\"IdCargoAbonoMovimiento\" DESC";

    // ── CobroComision SQL ──────────────────────────────────────────────────────
    private static final String SQL_COBRO_VIGENTES =
            "SELECT \"IdPortafolio\",\"IdEstadoComision\",\"MontoComision\",\"IdCliente\"" +
            " FROM " + TBL_COBRO_CAJA +
            " WHERE \"IdEmpresa\"=:idEmpresa AND \"IdMoneda\"=:idMoneda" +
            "   AND \"IdEstadoComision\"='VIG'" +
            " UNION ALL" +
            " SELECT PC.\"IdPortafolio\",PC.\"IdEstadoComision\",PC.\"MontoComision\",PC.\"IdCliente\"" +
            " FROM " + TBL_COBRO_CUS + " PC" +
            " WHERE PC.\"IdEstadoComision\"='VIG'";

    private static final String SQL_MARCAR_PROCESADO =
            "UPDATE " + TBL_COBRO_CAJA +
            " SET \"IdEstadoComision\"='PRO'" +
            " WHERE \"IdEmpresa\"=:idEmpresa AND \"IdPortafolio\"=:idRegistro";

    // ── INSERT CargoAbonoAGenerar SQL ─────────────────────────────────────────
    private static final String SQL_INSERT_CAG =
            "INSERT INTO " + TBL_CAG +
            " (\"NumeroCargoAbono\",\"IdPortafolio\",\"IdMoneda\",\"IdTipoMovimiento\",\"IdConcepto\"," +
            "  \"Descripcion\",\"IdCondicionDeLiquidacion\",\"FechaCondicionDeLiquidacion\",\"Monto\"," +
            "  \"IdEjecutivo\",\"IdSucursal\",\"IdTipoProducto\",\"IdTipoOperacion\",\"IdOperacion\"," +
            "  \"FechaOperacion\",\"FechaLiquidacionOperacion\",\"FechaVencimientoOperacion\"," +
            "  \"IdMonedaOperacion\",\"MontoOperacion\",\"TipoCambioOperacion\",\"IdContraparte\"," +
            "  \"IdTipoComprobante\",\"IdComprobante\",\"FolioComprobante\",\"IdComprobanteDetalle\"," +
            "  \"IdComprobanteFormaDePago\",\"IdTipoMovimientoAsociado\",\"IdMovimientoAsociado\"," +
            "  \"EsCargoAbonoPorTraspaso\",\"EsIndicadorBilateral\",\"GeneroContraparteEnTesoreria\"," +
            "  \"IdPersonaPagoCobro\",\"EsIngresoManual\")" +
            " VALUES(:numeroCargoAbono,:idPortafolio,:idMoneda,:idTipoMovimiento,:idConcepto," +
            "        :descripcion,:idCondicionDeLiquidacion,:fechaCondicionDeLiquidacion,:monto," +
            "        :idEjecutivo,:idSucursal,:idTipoProducto,:idTipoOperacion,:idOperacion," +
            "        :fechaOperacion,:fechaLiquidacionOperacion,:fechaVencimientoOperacion," +
            "        :idMonedaOperacion,:montoOperacion,:tipoCambioOperacion,:idContraparte," +
            "        :idTipoComprobante,:idComprobante,:folioComprobante,:idComprobanteDetalle," +
            "        :idComprobanteFormaDePago,:idTipoMovimientoAsociado,:idMovimientoAsociado," +
            "        :esCargoAbonoPorTraspaso,:esIndicadorBilateral,:generoContraparteEnTesoreria," +
            "        :idPersonaPagoCobro,:esIngresoManual)";

    // ── Fields ────────────────────────────────────────────────────────────────
    private final NamedParameterJdbcTemplate jdbc;
    private final SimpleJdbcCall spCrearCargoAbono;

    public HanaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        // SP call wired; executes against HANA only (not H2-compatible)
        this.spCrearCargoAbono = new SimpleJdbcCall(jdbc.getJdbcTemplate())
                .withProcedureName(SP_CREAR_CARGO_ABONO);
    }

    // ── HanaCargoAbonoPort ────────────────────────────────────────────────────

    @Override
    public CrearCargoAbonoSalida callCrearCargoAbonoHanaProc(Long idEmpresa, Long idUsuario,
            String idModulo, String simularOperacion, LocalDate fechaSistema,
            String esIngresoManual, List<CargoAbono> cargos) {
        var params = new MapSqlParameterSource()
                .addValue("p_IdEmpresa", idEmpresa)
                .addValue("p_IdUsuario", idUsuario)
                .addValue("p_IdModulo", idModulo)
                .addValue("p_simularOperacion", simularOperacion)
                .addValue("p_UsaReglasNegocio", "S")
                .addValue("p_FechaSistema", fechaSistema)
                .addValue("p_EsIngresoManual", esIngresoManual);
        Map<String, Object> result = spCrearCargoAbono.execute(params);
        long errores = result.get("p_out_ErroresContador") instanceof Number n ? n.longValue() : 0L;
        long advertencias = result.get("p_out_AdvertenciasContador") instanceof Number n ? n.longValue() : 0L;
        var errList = errores > 0
                ? List.of(new MensajeSalida(MensajeSalida.ERROR, "SP", "Errores SP: " + errores))
                : List.<MensajeSalida>of();
        var warnList = advertencias > 0
                ? List.of(new MensajeSalida(MensajeSalida.ADVERTENCIA, "SP", "Advertencias SP: " + advertencias))
                : List.<MensajeSalida>of();
        return new CrearCargoAbonoSalida(List.of(), List.of(), warnList, errList);
    }

    @Override
    public void insertarCargosAGenerar(List<CargoAbono> cargos) {
        var batchParams = cargos.stream()
                .map(c -> new MapSqlParameterSource()
                        .addValue("numeroCargoAbono", c.numeroCargoAbono())
                        .addValue("idPortafolio", c.idPortafolio())
                        .addValue("idMoneda", c.idMoneda())
                        .addValue("idTipoMovimiento", c.idTipoMovimiento())
                        .addValue("idConcepto", c.idConcepto())
                        .addValue("descripcion", c.descripcion())
                        .addValue("idCondicionDeLiquidacion", c.idCondicionDeLiquidacion())
                        .addValue("fechaCondicionDeLiquidacion", c.fechaCondicionDeLiquidacion())
                        .addValue("monto", c.monto())
                        .addValue("idEjecutivo", c.idEjecutivo())
                        .addValue("idSucursal", c.idSucursal())
                        .addValue("idTipoProducto", c.idTipoProducto())
                        .addValue("idTipoOperacion", c.idTipoOperacion())
                        .addValue("idOperacion", c.idOperacion())
                        .addValue("fechaOperacion", c.fechaOperacion())
                        .addValue("fechaLiquidacionOperacion", c.fechaLiquidacionOperacion())
                        .addValue("fechaVencimientoOperacion", c.fechaVencimientoOperacion())
                        .addValue("idMonedaOperacion", c.idMonedaOperacion())
                        .addValue("montoOperacion", c.montoOperacion())
                        .addValue("tipoCambioOperacion", c.tipoCambioOperacion())
                        .addValue("idContraparte", c.idContraparte())
                        .addValue("idTipoComprobante", c.idTipoComprobante())
                        .addValue("idComprobante", c.idComprobante())
                        .addValue("folioComprobante", c.folioComprobante())
                        .addValue("idComprobanteDetalle", c.idComprobanteDetalle())
                        .addValue("idComprobanteFormaDePago", c.idComprobanteFormaDePago())
                        .addValue("idTipoMovimientoAsociado", c.idTipoMovimientoAsociado())
                        .addValue("idMovimientoAsociado", c.idMovimientoAsociado())
                        .addValue("esCargoAbonoPorTraspaso", c.esCargoAbonoPorTraspaso())
                        .addValue("esIndicadorBilateral", c.esIndicadorBilateral())
                        .addValue("generoContraparteEnTesoreria", c.generoContraparteEnTesoreria())
                        .addValue("idPersonaPagoCobro", c.idPersonaPagoCobro())
                        .addValue("esIngresoManual", c.certificadoAutomatico()))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_INSERT_CAG, batchParams);
    }

    @Override
    public List<CargoAbonoMovimiento> consultarMovimientosParaActualizar(Long idTrabajo,
            List<Long> idsCuentaInversion) {
        boolean todos = idsCuentaInversion == null || idsCuentaInversion.isEmpty();
        var params = new MapSqlParameterSource()
                .addValue("todos", todos ? 1 : 0)
                .addValue("ids", todos ? List.of(-1L) : idsCuentaInversion);
        return jdbc.query(SQL_SELECT_CAM_PARA_ACTUALIZAR, params, (rs, row) ->
                new CargoAbonoMovimiento(
                        rs.getLong("IdCargoAbonoMovimiento"),         // 1  idCargoAbonoMovimiento
                        rs.getLong("IdCuentaInversion"),              // 2  idCuentaInversion
                        null,                                          // 3  idPortafolio
                        null,                                          // 4  idMoneda
                        null,                                          // 5  idEstado
                        rs.getString("IdTipoMovimiento"),             // 6  idTipoMovimiento
                        null,                                          // 7  idConcepto
                        null,                                          // 8  descripcion
                        null,                                          // 9  idCondicionDeLiquidacion
                        null,                                          // 10 fechaCondicionDeLiquidacion
                        rs.getBigDecimal("Monto"),                    // 11 monto
                        rs.getBigDecimal("Saldo"),                    // 12 saldo
                        rs.getBigDecimal("SaldoPorFechaMovimiento"),  // 13 saldoPorFechaMovimiento
                        null,                                          // 14 saldoLibroInventario
                        null,                                          // 15 esCertificadoAutomatico
                        rs.getTimestamp("FechaCertificacion") != null
                                ? rs.getTimestamp("FechaCertificacion").toLocalDateTime() : null, // 16
                        null,                                          // 17 idEjecutivo
                        null,                                          // 18 idSucursal
                        null,                                          // 19 idTipoComprobante
                        null,                                          // 20 idComprobante
                        null,                                          // 21 folioComprobante
                        null,                                          // 22 idComprobanteDetalle
                        null,                                          // 23 idComprobanteFormaDePago
                        null,                                          // 24 idTipoMovimientoAsociado
                        null,                                          // 25 idMovimientoAsociado
                        null,                                          // 26 fechaComprobante
                        null,                                          // 27 idTipoProducto
                        null,                                          // 28 idTipoOperacion
                        null,                                          // 29 idOperacion
                        null,                                          // 30 fechaOperacion
                        null,                                          // 31 fechaLiquidacionOperacion
                        null,                                          // 32 fechaVencimientoOperacion
                        null,                                          // 33 idMonedaOperacion
                        null,                                          // 34 montoOperacion
                        null,                                          // 35 tipoCambioOperacion
                        null,                                          // 36 idInstrumento
                        null,                                          // 37 idTipoInstrumento
                        null,                                          // 38 nemo
                        null,                                          // 39 cantidad
                        null,                                          // 40 esCargoAbonoPorTraspaso
                        null,                                          // 41 esIndicadorBilateral
                        null,                                          // 42 generoContraparteEnTesoreria
                        null,                                          // 43 idContraparte
                        null,                                          // 44 idPersonaPagoCobro
                        null,                                          // 45 idUsuario
                        null,                                          // 46 idEmpresa
                        null,                                          // 47 idModulo
                        null,                                          // 48 esIngresoManual
                        rs.getDate("FechaSistema") != null
                                ? rs.getDate("FechaSistema").toLocalDate() : null)); // 49
    }

    @Override
    public void actualizarSaldos(Long idTrabajo, List<CargoAbonoMovimiento> movimientos) {
        var fmParams = movimientos.stream()
                .map(m -> new MapSqlParameterSource()
                        .addValue("saldo", m.saldoPorFechaMovimiento())
                        .addValue("idCargoAbonoMovimiento", m.idCargoAbonoMovimiento()))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_UPDATE_SALDO_FM, fmParams);

        var feParams = movimientos.stream()
                .map(m -> new MapSqlParameterSource()
                        .addValue("saldo", m.saldo())
                        .addValue("idCargoAbonoMovimiento", m.idCargoAbonoMovimiento()))
                .toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_UPDATE_SALDO_FE, feParams);
    }

    // ── HanaSaldoPort ─────────────────────────────────────────────────────────

    @Override
    public SaldoInicialFinal consultarSaldoInicialFinal(SaldoCuentaInversionEntrada entrada) {
        var params = new MapSqlParameterSource()
                .addValue("ids", entrada.idsCuentaInversion())
                .addValue("fechaDesde", entrada.fechaDesde())
                .addValue("fechaHasta", entrada.fechaHasta());
        return jdbc.queryForObject(SQL_SALDO_INICIAL_FINAL, params, (rs, row) ->
                new SaldoInicialFinal(
                        rs.getBigDecimal("SaldoEfectivoInicial"),
                        rs.getBigDecimal("SaldoEfectivoFinal"),
                        rs.getBigDecimal("SaldoMovimientoInicial"),
                        rs.getBigDecimal("SaldoMovimientoFinal"),
                        rs.getBigDecimal("MontoCargoPendiente"),
                        rs.getBigDecimal("MontoAbonoPendiente")));
    }

    @Override
    public List<SaldoPorFechaEfectiva> consultarSaldoPorFechaEfectiva(SaldoCuentaInversionEntrada entrada) {
        var params = new MapSqlParameterSource()
                .addValue("ids", entrada.idsCuentaInversion())
                .addValue("fechaDesde", entrada.fechaDesde())
                .addValue("fechaHasta", entrada.fechaHasta());
        return jdbc.query(SQL_SALDO_POR_FECHA_EFECTIVA, params, (rs, row) ->
                new SaldoPorFechaEfectiva(
                        rs.getLong("IdCuentaInversion"),
                        rs.getDate("FechaEfectiva").toLocalDate(),
                        rs.getBigDecimal("Saldo"),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        rs.getBigDecimal("MontoCargoPendiente"),
                        rs.getBigDecimal("MontoAbonoPendiente")));
    }

    @Override
    public List<Movimiento> consultarMovimientos(Long idCuentaInversion, LocalDate fechaDesde,
            LocalDate fechaHasta, boolean incluyeAnulados) {
        var params = new MapSqlParameterSource()
                .addValue("idCuentaInversion", idCuentaInversion)
                .addValue("fechaDesde", fechaDesde)
                .addValue("fechaHasta", fechaHasta)
                .addValue("incluyeAnulados", incluyeAnulados ? 1 : 0);
        return jdbc.query(SQL_MOVIMIENTOS, params, this::mapMovimiento);
    }

    @Override
    public List<Movimiento> consultarMovimientosHistoricos(Long idCuentaInversion, LocalDate fechaDesde,
            LocalDate fechaHasta, boolean incluyeAnulados) {
        var params = new MapSqlParameterSource()
                .addValue("idCuentaInversion", idCuentaInversion)
                .addValue("fechaDesde", fechaDesde)
                .addValue("fechaHasta", fechaHasta)
                .addValue("incluyeAnulados", incluyeAnulados ? 1 : 0);
        return jdbc.query(SQL_MOVIMIENTOS_HISTORICOS, params, this::mapMovimiento);
    }

    @Override
    public BigDecimal consultarSaldoPorFechaAnterior(Long idCuentaInversion, LocalDate fecha) {
        var params = new MapSqlParameterSource()
                .addValue("idCuentaInversion", idCuentaInversion)
                .addValue("fecha", fecha);
        List<BigDecimal> result = jdbc.query(SQL_SALDO_ANTERIOR, params,
                (rs, row) -> rs.getBigDecimal("Saldo"));
        return result.isEmpty() ? BigDecimal.ZERO : result.getFirst();
    }

    // ── Movimientos por consulta (portafolio / moneda) ────────────────────────

    private static final String SQL_CONSULTA_INSERT = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarMovimientoCuentaInversion_InsertConsulta.sql");
    private static final String SQL_CONSULTA_DELETE = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarMovimientoCuentaInversion_DeleteConsulta.sql");
    private static final String SQL_CONSULTA_SELECT = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarMovimientoCuentaInversion_SelectConsulta.sql");
    private static final String SQL_CONSULTA_SELECT_HIST = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarMovimientoCuentaInversion_SelectConsultaHistorico.sql");

    @Override
    public List<MovimientoCuentaInversion> consultarMovimientosPorConsulta(List<ConsultaMovimiento> consultas) {
        return porConsulta(consultas, SQL_CONSULTA_SELECT);
    }

    @Override
    public List<MovimientoCuentaInversion> consultarMovimientosHistoricosPorConsulta(List<ConsultaMovimiento> consultas) {
        return porConsulta(consultas, SQL_CONSULTA_SELECT_HIST);
    }

    /** The process table is a work table: load the queries, select, empty it (the delete is skipped if the select fails). */
    private List<MovimientoCuentaInversion> porConsulta(List<ConsultaMovimiento> consultas, String select) {
        jdbc.batchUpdate(SQL_CONSULTA_INSERT, consultas.stream()
                .map(c -> (SqlParameterSource) new MapSqlParameterSource()
                        .addValue("IdPortafolio", c.IdPortafolio)
                        .addValue("IdMoneda", c.IdMoneda)
                        .addValue("IdEstado", c.IdEstado)
                        .addValue("SaldoInicial", CtaJavaCallInfra.escala16(c.SaldoInicial))
                        .addValue("FechaDesde", c.FechaDesde)
                        .addValue("FechaHasta", c.FechaHasta)
                        .addValue("IdMatrizAgrupador", c.IdMatrizAgrupador)
                        .addValue("EsADC", c.EsADC)
                        .addValue("SaldoPorFechaEfectiva", c.SaldoPorFechaEfectiva))
                .toArray(SqlParameterSource[]::new));
        List<MovimientoCuentaInversion> filas = jdbc.query(select, new MapSqlParameterSource(), (rs, n) -> mapearPorEtiqueta(rs));
        jdbc.getJdbcTemplate().execute(SQL_CONSULTA_DELETE);
        return filas;
    }

    /** Maps each column onto the public field with the same name, converting numbers and dates to the field type. */
    private static MovimientoCuentaInversion mapearPorEtiqueta(ResultSet rs) throws SQLException {
        MovimientoCuentaInversion fila = new MovimientoCuentaInversion();
        ResultSetMetaData md = rs.getMetaData();
        for (int c = 1; c <= md.getColumnCount(); c++) {
            try {
                var campo = MovimientoCuentaInversion.class.getField(md.getColumnLabel(c));
                campo.set(fila, convertir(rs.getObject(c), campo.getType()));
            } catch (NoSuchFieldException ignorada) {
                // columns without a field (DocumentoIdentidad, CodigoPortafolio of the historic query) are not part of the contract
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return fila;
    }

    private static Object convertir(Object v, Class<?> tipo) {
        if (v instanceof Number n) {
            if (tipo == Long.class) return n.longValue();
            if (tipo == Integer.class) return n.intValue();
            if (tipo == BigDecimal.class) return new BigDecimal(n.toString());
            if (tipo == String.class) return n.toString();
        }
        if (v instanceof java.sql.Timestamp ts && tipo == java.sql.Date.class) return new java.sql.Date(ts.getTime());
        if (v instanceof java.sql.Date d && tipo == java.sql.Timestamp.class) return new java.sql.Timestamp(d.getTime());
        return v;
    }

    // ── HanaCobroComisionPort ─────────────────────────────────────────────────

    @Override
    public List<Map<String, Object>> consultarRegistrosVigentes(Long idEmpresa, String idMoneda) {
        var params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("idMoneda", idMoneda);
        return jdbc.queryForList(SQL_COBRO_VIGENTES, params);
    }

    @Override
    public void marcarComoProcesado(Long idEmpresa, Long idRegistro) {
        jdbc.update(SQL_MARCAR_PROCESADO,
                new MapSqlParameterSource()
                        .addValue("idEmpresa", idEmpresa)
                        .addValue("idRegistro", idRegistro));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Movimiento mapMovimiento(ResultSet rs, int row) throws SQLException {
        return new Movimiento(
                rs.getLong("IdCargoAbonoMovimiento"),
                rs.getLong("IdPortafolio"),
                rs.getString("IdMoneda"),
                rs.getString("IdEstado"),
                rs.getString("IdTipoMovimiento"),
                rs.getString("IdConcepto"),
                rs.getString("Descripcion"),
                rs.getDate("FechaEfectiva") != null ? rs.getDate("FechaEfectiva").toLocalDate() : null,
                rs.getDate("FechaSistema") != null ? rs.getDate("FechaSistema").toLocalDate() : null,
                rs.getBigDecimal("Monto"),
                rs.getBigDecimal("Saldo"),
                rs.getString("IdModulo"),
                rs.getLong("IdCarga"),
                rs.getString("NemotecnicoOriginal"),
                rs.getString("NombreConcepto"));
    }
}
