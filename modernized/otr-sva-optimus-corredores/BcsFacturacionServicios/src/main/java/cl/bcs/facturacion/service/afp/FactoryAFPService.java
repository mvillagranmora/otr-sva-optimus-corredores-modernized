package cl.bcs.facturacion.service.afp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.facturacion.afp.FactoryAFP.
 * Factory that returns AFP-specific transaction data for Capital, Hábitat, and Provida.
 * Not a JavaCall — registered as @Component without controller wiring per project convention.
 */
@Component
public class FactoryAFPService {

    private static final Logger log = LoggerFactory.getLogger(FactoryAFPService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlCapital;
    private final String sqlHabitat;
    private final String sqlProvida;

    public FactoryAFPService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlCapital = loadSql("cl/bcs/facturacion/sql/FactoryAFP_ConsultarTransaccionAFP_Capital.sql");
        this.sqlHabitat = loadSql("cl/bcs/facturacion/sql/FactoryAFP_ConsultarTransaccionAFP_Habitat.sql");
        this.sqlProvida = loadSql("cl/bcs/facturacion/sql/FactoryAFP_ConsultarTransaccionAFP_Provida.sql");
    }

    public JsonObject consultarTransacciones(JsonObject params) {
        log.info("INICIO::FactoryAFP.consultarTransacciones");

        String idAFP = params.has("p_IdAFP") && !params.get("p_IdAFP").isJsonNull()
            ? params.get("p_IdAFP").getAsString() : "";

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",  params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull()
                ? params.get("p_IdEmpresa").getAsLong() : null)
            .addValue("FechaDesde", params.has("p_FechaDesde") && !params.get("p_FechaDesde").isJsonNull()
                ? params.get("p_FechaDesde").getAsString() : null)
            .addValue("FechaHasta", params.has("p_FechaHasta") && !params.get("p_FechaHasta").isJsonNull()
                ? params.get("p_FechaHasta").getAsString() : null);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            String sql = switch (idAFP.toUpperCase()) {
                case "CAPITAL"  -> sqlCapital;
                case "HABITAT"  -> sqlHabitat;
                case "PROVIDA"  -> sqlProvida;
                default -> {
                    log.warn("FactoryAFP: unknown AFP '{}', defaulting to Capital", idAFP);
                    yield sqlCapital;
                }
            };

            List<Map<String, Object>> transacciones = jdbc.queryForList(sql, p);
            JsonArray arr = new JsonArray();
            transacciones.forEach(r -> arr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_TRANSACCIONES_AFP", arr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in FactoryAFP", ex);
            long cnt = out.get("P_OUT_ERRORESCONTADOR").getAsLong();
            out.addProperty("P_OUT_ERRORESCONTADOR", cnt + 1);
        } catch (Exception ex) {
            log.error("Error in FactoryAFP", ex);
            long cnt = out.get("P_OUT_ERRORESCONTADOR").getAsLong();
            out.addProperty("P_OUT_ERRORESCONTADOR", cnt + 1);
        }

        return out;
    }

    private static String loadSql(String path) {
        try (InputStream is = FactoryAFPService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
