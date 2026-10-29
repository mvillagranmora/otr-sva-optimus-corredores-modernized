package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class P40AgregarModificarFilialesService {

    private static final Logger log = LoggerFactory.getLogger(P40AgregarModificarFilialesService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPSERT;

    public P40AgregarModificarFilialesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPSERT = loadSql("cl/bcs/normativa/interfaces/sql/P40AgregarModificarFiliales_UpsertFiliales.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("P40AgregarModificarFilialesService.execute called");
        JsonObject out = new JsonObject();

        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            JsonArray filiales = params.has("p_FilialesP40") ? params.get("p_FilialesP40").getAsJsonArray() : new JsonArray();

            List<MapSqlParameterSource> batchParams = new ArrayList<>();
            filiales.forEach(el -> {
                JsonObject filial = el.getAsJsonObject();
                MapSqlParameterSource p = new MapSqlParameterSource();
                p.addValue("IdEmpresa", idEmpresa);
                filial.entrySet().forEach(entry -> {
                    try { p.addValue(entry.getKey(), entry.getValue().getAsString()); }
                    catch (Exception ex) { p.addValue(entry.getKey(), null); }
                });
                batchParams.add(p);
            });

            if (!batchParams.isEmpty()) {
                jdbc.batchUpdate(SQL_UPSERT, batchParams.toArray(new MapSqlParameterSource[0]));
            }
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.addProperty("P_OUT_MENSAJE", "Datos guardados exitosamente");
        } catch (Exception e) {
            log.error("Error en P40AgregarModificarFiliales", e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            out.addProperty("P_OUT_MENSAJE", "Error: " + e.getMessage());
        }
        return out;
    }

    private static String loadSql(String path) {
        try (var is = P40AgregarModificarFilialesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
