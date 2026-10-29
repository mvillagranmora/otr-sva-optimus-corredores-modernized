package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.BuscarCantidadesPorRegimen.
 */
@Service
public class BuscarCantidadesPorRegimenService {

    private static final Logger log = LoggerFactory.getLogger(BuscarCantidadesPorRegimenService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlCarteras;
    private final String sqlSolicitudesTransito;

    public BuscarCantidadesPorRegimenService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlCarteras = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/BuscarCantidadesPorRegimen_SelectCarteras.sql").getInputStream(),
            StandardCharsets.UTF_8);
        this.sqlSolicitudesTransito = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/BuscarCantidadesPorRegimen_SelectSolicitudesTransito.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("BuscarCantidadesPorRegimen");
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;
        JsonArray seriesArray = params.has("p_IdSerieFondo") ? params.getAsJsonArray("p_IdSerieFondo") : new JsonArray();

        List<Long> clientes = List.of(idCliente);
        List<String> series = new ArrayList<>();
        for (JsonElement e : seriesArray) series.add(e.getAsString());

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdCliente", clientes)
            .addValue("IdSerieFondo", series.isEmpty() ? List.of("") : series);

        List<Map<String, Object>> carteras = jdbc.queryForList(sqlCarteras, ps);
        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSolicitudesTransito, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrCarteras = new JsonArray();
        for (Map<String, Object> row : carteras) arrCarteras.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERAS", arrCarteras);
        JsonArray arrSolicitudes = new JsonArray();
        for (Map<String, Object> row : solicitudes) arrSolicitudes.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES_TRANSITO", arrSolicitudes);
        return out;
    }
}
