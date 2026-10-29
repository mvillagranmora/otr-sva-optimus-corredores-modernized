package cl.bcs.plataforma.service.cua;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF (JavaCall).
 * Runs the RF/IF custody quadrature process:
 *   1. Clears temporary saldo/movimiento tables
 *   2. Loads initial saldos via SQL
 *   3. Loads movements (mesa pacto, manuales, facturacion)
 *   4. Computes final calculated saldo
 */
@Service
public class ProcesarCuadraturaCustodiaRFIFService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ProcesarCuadraturaCustodiaRFIFService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF";
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String fechaInicio = params.has("p_FechaInicio") ? params.get("p_FechaInicio").getAsString() : null;
            String fechaFin = params.has("p_FechaFin") ? params.get("p_FechaFin").getAsString() : null;

            // 1. Clear temporary tables
            jdbc.update("DELETE FROM \"BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF\"",
                    new MapSqlParameterSource());
            jdbc.update("DELETE FROM \"BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF\"",
                    new MapSqlParameterSource());

            MapSqlParameterSource baseParams = new MapSqlParameterSource()
                    .addValue("p_FechaInicio", fechaInicio)
                    .addValue("p_FechaFin", fechaFin);

            // 2. Load initial saldos
            String sqlCargaInicial = loadSql(
                    "cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaInicialCuadraturaRFIF.sql");
            jdbc.update(sqlCargaInicial, baseParams);

            // 3. Load movements
            String sqlMesaPacto = loadSql(
                    "cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosMesaPacto.sql");
            jdbc.update(sqlMesaPacto, baseParams);

            String sqlManuales = loadSql(
                    "cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosManuales.sql");
            jdbc.update(sqlManuales, baseParams);

            String sqlFacturacion = loadSql(
                    "cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosFacturacion.sql");
            jdbc.update(sqlFacturacion, baseParams);

            // 4. Compute final saldo
            String sqlSaldoFinal = loadSql(
                    "cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaSaldoFinalCuadraturaRFIF.sql");
            jdbc.update(sqlSaldoFinal, baseParams);

            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
