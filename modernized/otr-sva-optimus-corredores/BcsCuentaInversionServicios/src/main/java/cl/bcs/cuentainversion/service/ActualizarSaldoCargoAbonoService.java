package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * Recalculates the running balances of the CERTIFIED movements of the selected accounts: {@code Saldo} (by effective date) and
 * {@code SaldoPorFechaMovimiento} (by system/movement date), starting from the last stored balance before the given date
 * (README CTA-A-01..11; legacy ActualizarSaldoCargoAbono).
 *
 * <p>The movements go through a step table keyed by a random job id so concurrent runs do not mix; the balances themselves are
 * computed by the window functions of the SQL resources.
 */
@Service
public class ActualizarSaldoCargoAbonoService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarSaldoCargoAbonoService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/ActualizarSaldoCargoAbono_";
    private static final String SQL_CREATE_TEMP = CtaJavaCallInfra.loadSql(SQL + "Create_Temp_Table_Cuenta_Inversion.sql");
    private static final String SQL_INSERT_TEMP = CtaJavaCallInfra.loadSql(SQL + "Insert_Temp_Table_Cuenta_Inversion.sql");
    private static final String SQL_INSERT_TEMP_PAUTA = CtaJavaCallInfra.loadSql(SQL + "Insert_Temp_Table_Cuenta_Inversion_Pauta.sql");
    private static final String SQL_DROP_TEMP = CtaJavaCallInfra.loadSql(SQL + "Drop_Temp_Table_Cuenta_Inversion.sql");
    private static final String SQL_INSERT_PASO = CtaJavaCallInfra.loadSql(SQL + "Insert_Cargo_Abono_Movimiento_Paso.sql");
    private static final String SQL_SELECT_PASO = CtaJavaCallInfra.loadSql(SQL + "Select_Cargo_Abono_Movimiento_Paso.sql");
    private static final String SQL_DELETE_PASO = CtaJavaCallInfra.loadSql(SQL + "Delete_Cargo_Abono_Movimiento_Paso.sql");
    private static final String SQL_SELECT_SALDO_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "Select_Saldo_Fecha_Movimiento.sql");
    private static final String SQL_SELECT_SALDO_EFECTIVO = CtaJavaCallInfra.loadSql(SQL + "Select_Saldo_Fecha_Efectiva.sql");
    private static final String SQL_UPDATE_SALDO_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "Update_Saldo_Fecha_Movimiento.sql");
    private static final String SQL_UPDATE_SALDO_EFECTIVO = CtaJavaCallInfra.loadSql(SQL + "Update_Saldo_Fecha_Efectiva.sql");

    /** Input. {@code p_ActualizarTodosLosSaldos = S} with an empty list means every active account. */
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaMovimiento;
        public String p_ActualizarTodosLosSaldos = "N";
        public List<CuentaInversion> p_CuentaInversion = new ArrayList<>();
    }

    public static class CuentaInversion {
        public Long IdCuentaInversion;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    private final NamedParameterJdbcTemplate jdbc;

    public ActualizarSaldoCargoAbonoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Typed entry point used by the services that recalculate balances after changing movements (Certificar, Anular). */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Salida actualizar(Entrada entrada) {
        log.info("INICIO::ActualizarSaldoCargoAbono");
        jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);

        // 'N' + accounts: those accounts; 'S' + no accounts: every active one; any other combination selects nothing
        // LEGACY DEFECT kept (see README CTA-A-07): 'S' with a non-empty list does nothing; a null list throws NullPointerException (CTA-A-10)
        if ("N".equalsIgnoreCase(entrada.p_ActualizarTodosLosSaldos) && !entrada.p_CuentaInversion.isEmpty()) {
            List<SqlParameterSource> cuentas = entrada.p_CuentaInversion.stream()
                    .map(c -> c.IdCuentaInversion)
                    .distinct()
                    .map(id -> (SqlParameterSource) new MapSqlParameterSource("IdCuentaInversion", id))
                    .toList();
            jdbc.batchUpdate(SQL_INSERT_TEMP, cuentas.toArray(SqlParameterSource[]::new));
        }
        if ("S".equalsIgnoreCase(entrada.p_ActualizarTodosLosSaldos) && entrada.p_CuentaInversion.isEmpty()) {
            jdbc.getJdbcTemplate().execute(SQL_INSERT_TEMP_PAUTA);
        }

        List<Map<String, Object>> movimientos = jdbc.queryForList(SQL_SELECT_PASO, new MapSqlParameterSource());
        if (!movimientos.isEmpty()) {
            long idTrabajo = ThreadLocalRandom.current().nextLong(10_000_000_000L);
            jdbc.batchUpdate(SQL_INSERT_PASO, movimientos.stream()
                    .map(m -> (SqlParameterSource) pasoDe(m, idTrabajo))
                    .toArray(SqlParameterSource[]::new));

            MapSqlParameterSource fecha = new MapSqlParameterSource("p_FechaMovimiento", entrada.p_FechaMovimiento);
            actualizarSaldos(SQL_SELECT_SALDO_MOVIMIENTO, "SaldoMovimiento", SQL_UPDATE_SALDO_MOVIMIENTO, fecha);
            actualizarSaldos(SQL_SELECT_SALDO_EFECTIVO, "SaldoEfectivo", SQL_UPDATE_SALDO_EFECTIVO, fecha);

            jdbc.update(SQL_DELETE_PASO, new MapSqlParameterSource("IdTrabajo", idTrabajo));
        }
        jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP);
        log.info("FIN::ActualizarSaldoCargoAbono");
        return new Salida();
    }

    /** JavaCall entry used by callers that exchange JSON; runs the recalculation. */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public JsonObject execute(JsonObject params) {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        return Utils.getGsonBuilder().toJsonTree(actualizar(entrada)).getAsJsonObject();
    }

    /**
     * The legacy JavaCall entry point {@code call(...)} parses the input and returns {@code null} WITHOUT running the logic,
     * so the endpoint registered for this class never updates anything.
     * LEGACY DEFECT kept (see README CTA-A-08); callers that need the recalculation use {@link #execute(JsonObject)}.
     */
    public JsonObject call(JsonObject params) {
        Utils.getGsonBuilder().fromJson(params, Entrada.class);
        return null;
    }

    private void actualizarSaldos(String select, String columnaSaldo, String update, MapSqlParameterSource fecha) {
        List<Map<String, Object>> saldos = jdbc.queryForList(select, fecha);
        jdbc.batchUpdate(update, saldos.stream()
                .map(s -> (SqlParameterSource) new MapSqlParameterSource()
                        .addValue("IdCargoAbonoMovimiento", s.get("IdCargoAbonoMovimiento"))
                        .addValue("Saldo", CtaJavaCallInfra.escala16((BigDecimal) s.get(columnaSaldo))))
                .toArray(SqlParameterSource[]::new));
    }

    private static MapSqlParameterSource pasoDe(Map<String, Object> m, long idTrabajo) {
        MapSqlParameterSource p = new MapSqlParameterSource("IdTrabajo", idTrabajo);
        m.forEach((columna, valor) -> p.addValue(columna, valor instanceof BigDecimal bd ? CtaJavaCallInfra.escala16(bd) : valor));
        return p;
    }

    // B-CTA-04: accumulation formulas of both series; the SQL window functions apply the same sum (kept for ServiceFormulasTest)
    BigDecimal acumularSaldoEfectivo(BigDecimal saldoAnterior, BigDecimal monto) {
        return saldoAnterior.add(monto, MathContext.DECIMAL128);
    }

    BigDecimal acumularSaldoMovimiento(BigDecimal saldoAnterior, BigDecimal monto) {
        return saldoAnterior.add(monto, MathContext.DECIMAL128);
    }
}
