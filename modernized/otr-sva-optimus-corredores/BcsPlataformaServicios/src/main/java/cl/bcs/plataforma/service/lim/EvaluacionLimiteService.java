package cl.bcs.plataforma.service.lim;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EvaluacionLimiteService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public EvaluacionLimiteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.EvaluacionLimite"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // This service delegates to SAP HANA stored procedure:
            //   CALL "BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite" (...)
            // Temporary table #EvaluacionLimite_P_TABLAOPERACION must be created,
            // populated with P_TABLAOPERACION entries, then the SP invoked, then dropped.
            // Full implementation requires StoredProcedure support or raw JDBC CallableStatement
            // with HANA-specific temp-table and output-table handling.

            jdbc.getJdbcTemplate().execute(
                    "CREATE LOCAL TEMPORARY TABLE #EvaluacionLimite_P_TABLAOPERACION (" +
                    "\"IdPortafolio\" BIGINT,\"IdCliente\" BIGINT,\"MontoUtilizado\" DECIMAL(26,8)," +
                    "\"IdMoneda\" NVARCHAR(3),\"TipoCambio\" DECIMAL(26,8),\"IdOperacion\" BIGINT," +
                    "\"IdTipoOperacion\" NVARCHAR(6),\"IdTipoInstrumento\" NVARCHAR(3)," +
                    "\"IdClasificacionTipoInstrumento\" NVARCHAR(3),\"IdTipoReajuste\" NVARCHAR(3)," +
                    "\"IdInstrumento\" BIGINT,\"IdEmisor\" NVARCHAR(40),\"Tasa\" DECIMAL(26,8)," +
                    "\"DiasPlazo\" INTEGER,\"FechaOperacion\" NVARCHAR(10),\"CodigoOperador\" NVARCHAR(40)," +
                    "\"IdAgente\" BIGINT,\"IdCanalIngreso\" NVARCHAR(3))");

            if (params.has("P_TABLAOPERACION") && params.get("P_TABLAOPERACION").isJsonArray()) {
                for (var el : params.getAsJsonArray("P_TABLAOPERACION")) {
                    JsonObject op = el.getAsJsonObject();
                    MapSqlParameterSource ps = new MapSqlParameterSource()
                            .addValue("IdPortafolio", op.has("IdPortafolio") && !op.get("IdPortafolio").isJsonNull() ? op.get("IdPortafolio").getAsLong() : null)
                            .addValue("IdCliente", op.has("IdCliente") && !op.get("IdCliente").isJsonNull() ? op.get("IdCliente").getAsLong() : null)
                            .addValue("MontoUtilizado", op.has("MontoUtilizado") && !op.get("MontoUtilizado").isJsonNull() ? op.get("MontoUtilizado").getAsBigDecimal() : BigDecimal.ZERO)
                            .addValue("IdMoneda", op.has("IdMoneda") && !op.get("IdMoneda").isJsonNull() ? op.get("IdMoneda").getAsString() : null)
                            .addValue("TipoCambio", op.has("TipoCambio") && !op.get("TipoCambio").isJsonNull() ? op.get("TipoCambio").getAsBigDecimal() : BigDecimal.ZERO)
                            .addValue("IdOperacion", op.has("IdOperacion") && !op.get("IdOperacion").isJsonNull() ? op.get("IdOperacion").getAsLong() : null)
                            .addValue("IdTipoOperacion", op.has("IdTipoOperacion") && !op.get("IdTipoOperacion").isJsonNull() ? op.get("IdTipoOperacion").getAsString() : null)
                            .addValue("IdTipoInstrumento", op.has("IdTipoInstrumento") && !op.get("IdTipoInstrumento").isJsonNull() ? op.get("IdTipoInstrumento").getAsString() : null)
                            .addValue("IdClasificacionTipoInstrumento", op.has("IdClasificacionTipoInstrumento") && !op.get("IdClasificacionTipoInstrumento").isJsonNull() ? op.get("IdClasificacionTipoInstrumento").getAsString() : null)
                            .addValue("IdTipoReajuste", op.has("IdTipoReajuste") && !op.get("IdTipoReajuste").isJsonNull() ? op.get("IdTipoReajuste").getAsString() : null)
                            .addValue("IdInstrumento", op.has("IdInstrumento") && !op.get("IdInstrumento").isJsonNull() ? op.get("IdInstrumento").getAsLong() : null)
                            .addValue("IdEmisor", op.has("IdEmisor") && !op.get("IdEmisor").isJsonNull() ? op.get("IdEmisor").getAsString() : null)
                            .addValue("Tasa", op.has("Tasa") && !op.get("Tasa").isJsonNull() ? op.get("Tasa").getAsBigDecimal() : BigDecimal.ZERO)
                            .addValue("DiasPlazo", op.has("DiasPlazo") && !op.get("DiasPlazo").isJsonNull() ? op.get("DiasPlazo").getAsInt() : null)
                            .addValue("FechaOperacion", op.has("FechaOperacion") && !op.get("FechaOperacion").isJsonNull() ? op.get("FechaOperacion").getAsString() : null)
                            .addValue("CodigoOperador", op.has("CodigoOperador") && !op.get("CodigoOperador").isJsonNull() ? op.get("CodigoOperador").getAsString() : null)
                            .addValue("IdAgente", op.has("IdAgente") && !op.get("IdAgente").isJsonNull() ? op.get("IdAgente").getAsLong() : null)
                            .addValue("IdCanalIngreso", op.has("IdCanalIngreso") && !op.get("IdCanalIngreso").isJsonNull() ? op.get("IdCanalIngreso").getAsString() : null);
                    jdbc.update(
                            "INSERT INTO #EvaluacionLimite_P_TABLAOPERACION " +
                            "(\"IdPortafolio\",\"IdCliente\",\"MontoUtilizado\",\"IdMoneda\",\"TipoCambio\",\"IdOperacion\"," +
                            "\"IdTipoOperacion\",\"IdTipoInstrumento\",\"IdClasificacionTipoInstrumento\",\"IdTipoReajuste\"," +
                            "\"IdInstrumento\",\"IdEmisor\",\"Tasa\",\"DiasPlazo\",\"FechaOperacion\",\"CodigoOperador\"," +
                            "\"IdAgente\",\"IdCanalIngreso\") VALUES " +
                            "(:IdPortafolio,:IdCliente,:MontoUtilizado,:IdMoneda,:TipoCambio,:IdOperacion," +
                            ":IdTipoOperacion,:IdTipoInstrumento,:IdClasificacionTipoInstrumento,:IdTipoReajuste," +
                            ":IdInstrumento,:IdEmisor,:Tasa,:DiasPlazo,:FechaOperacion,:CodigoOperador,:IdAgente,:IdCanalIngreso)",
                            ps);
                }
            }

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // Invoke SP: CALL "BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite" via CallableStatement
            result.add("P_OUT_REGISTROEVALUACIONLIMITE", new JsonArray());
            result.add("P_OUT_ESTADOOPERACION", new JsonArray());
            result.add("P_OUT_IDAUTHTABLE", new JsonArray());
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
            result.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_ERRORESDETALLE", new JsonArray());

            try { jdbc.getJdbcTemplate().execute("DROP TABLE #EvaluacionLimite_P_TABLAOPERACION"); } catch (Exception ignore) {}
        } catch (Exception e) {
            try { jdbc.getJdbcTemplate().execute("DROP TABLE #EvaluacionLimite_P_TABLAOPERACION"); } catch (Exception ignore) {}
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray errs = new JsonArray(); JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR"); err.addProperty("Descripcion", e.getMessage()); errs.add(err); result.add("P_OUT_ERRORESDETALLE", errs);
        }
        return result;
    }
}
