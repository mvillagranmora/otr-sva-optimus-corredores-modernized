package cl.bcs.cuentainversion.transaccional.support;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra;

/**
 * Executable REFERENCE of legacy ActualizarSaldoCargoAbono.logica (ActualizarSaldoCargoAbono.java:147-254): the same statements, in the same
 * order, with the production SQL resources. It exists for two reasons (see HarnessSelfCheckTest):
 * <ol>
 *   <li>it proves the harness can go GREEN: oracle goldens, H2 fixtures, table dumps and comparison agree when the behaviour is the legacy one;</li>
 *   <li>it is the shape the modern {@code ActualizarSaldoCargoAbonoService} must have (its current implementation does not bind
 *       IdTrabajo, inserts the step table with one statement and updates by wrong parameter names).</li>
 * </ol>
 * The expected values are NOT computed here: they come from the goldens recorded by the legacy oracle.
 */
public final class ActualizarSaldoReferencia {

    private static final String DIR = "cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_";

    private ActualizarSaldoReferencia() {
    }

    /** @param mutador applied to the SQL text of the statement identified by its file suffix (null = production SQL); used to prove the tests can fail. */
    public static JsonObject ejecutar(NamedParameterJdbcTemplate jdbc, JsonObject entrada, java.util.function.BiFunction<String, String, String> mutador) {
        UnaryOperator<String> id = s -> s;
        java.util.function.Function<String, String> sql = nombre -> {
            String s = CtaJavaCallInfra.loadSql(DIR + nombre + ".sql");
            return mutador == null ? s : mutador.apply(nombre, s);
        };
        String todos = entrada.has("p_ActualizarTodosLosSaldos") ? entrada.get("p_ActualizarTodosLosSaldos").getAsString() : "N";
        LinkedHashSet<Long> cuentas = new LinkedHashSet<>();
        for (JsonElement e : entrada.getAsJsonArray("p_CuentaInversion")) {
            cuentas.add(e.getAsJsonObject().get("IdCuentaInversion").getAsLong());
        }
        String fecha = entrada.get("p_FechaMovimiento").getAsString();

        jdbc.getJdbcTemplate().execute(sql.apply("Create_Temp_Table_Cuenta_Inversion"));
        if ("N".equalsIgnoreCase(todos) && !cuentas.isEmpty()) {
            for (Long c : cuentas) {
                jdbc.update(sql.apply("Insert_Temp_Table_Cuenta_Inversion"), new MapSqlParameterSource("IdCuentaInversion", c));
            }
        }
        if ("S".equalsIgnoreCase(todos) && cuentas.isEmpty()) {
            jdbc.update(sql.apply("Insert_Temp_Table_Cuenta_Inversion_Pauta"), new MapSqlParameterSource());
        }
        List<Map<String, Object>> paso = jdbc.queryForList(sql.apply("Select_Cargo_Abono_Movimiento_Paso"), new MapSqlParameterSource());
        if (!paso.isEmpty()) {
            long idTrabajo = 1L;
            for (Map<String, Object> r : paso) {
                MapSqlParameterSource p = new MapSqlParameterSource(r);
                p.addValue("IdTrabajo", idTrabajo);
                jdbc.update(sql.apply("Insert_Cargo_Abono_Movimiento_Paso"), p);
            }
            MapSqlParameterSource pf = new MapSqlParameterSource("p_FechaMovimiento", fecha);
            List<Map<String, Object>> sm = jdbc.queryForList(sql.apply("Select_Saldo_Fecha_Movimiento"), pf);
            for (Map<String, Object> r : sm) {
                jdbc.update(sql.apply("Update_Saldo_Fecha_Movimiento"), new MapSqlParameterSource()
                        .addValue("IdCargoAbonoMovimiento", r.get("IdCargoAbonoMovimiento")).addValue("Saldo", r.get("SaldoMovimiento")));
            }
            List<Map<String, Object>> se = jdbc.queryForList(sql.apply("Select_Saldo_Fecha_Efectiva"), pf);
            for (Map<String, Object> r : se) {
                jdbc.update(sql.apply("Update_Saldo_Fecha_Efectiva"), new MapSqlParameterSource()
                        .addValue("IdCargoAbonoMovimiento", r.get("IdCargoAbonoMovimiento")).addValue("Saldo", r.get("SaldoEfectivo")));
            }
            jdbc.getJdbcTemplate().update(sql.apply("Delete_Cargo_Abono_Movimiento_Paso").replace(":IdTrabajo", idTrabajo + ""));
        }
        jdbc.getJdbcTemplate().execute(sql.apply("Drop_Temp_Table_Cuenta_Inversion"));

        JsonObject out = new JsonObject();
        out.add("P_OUT_MENSAJES", new com.google.gson.JsonArray());
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    @SuppressWarnings("unused")
    private static List<Long> vacio() {
        return new ArrayList<>();
    }
}
