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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ModificarComisionSolicitudPorPerfeccionamientoService {

    private static final Logger log = LoggerFactory.getLogger(ModificarComisionSolicitudPorPerfeccionamientoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlActualizar;
    private final String sqlSerieFondo;

    public ModificarComisionSolicitudPorPerfeccionamientoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarComisionSolicitudPorPerfeccionamiento_SelectSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlActualizar = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarComisionSolicitudPorPerfeccionamiento_ActualizarSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ModificarComisionSolicitudPorPerfeccionamiento");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray solicitudes = params.has("p_Solicitudes") ? params.getAsJsonArray("p_Solicitudes") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        List<String> series = new ArrayList<>();
        for (JsonElement e : solicitudes) {
            JsonObject s = e.getAsJsonObject();
            if (s.has("IdSolicitudAporteRescate")) ids.add(s.get("IdSolicitudAporteRescate").getAsLong());
            if (s.has("IdSerieFondo")) {
                String sf = s.get("IdSerieFondo").getAsString();
                if (!series.contains(sf)) series.add(sf);
            }
        }

        List<Map<String, Object>> solicitudesExistentes = List.of();
        if (!ids.isEmpty()) {
            solicitudesExistentes = jdbc.queryForList(sqlSelect,
                new MapSqlParameterSource().addValue("IdSolicitudAporteRescate", ids).addValue("IdEmpresa", idEmpresa));
        }

        List<Map<String, Object>> seriesFondo = List.of();
        if (!series.isEmpty()) {
            seriesFondo = jdbc.queryForList(sqlSerieFondo,
                new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdSerieFondo", series));
        }

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : solicitudes) {
            JsonObject s = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", s.has("IdSolicitudAporteRescate") ? s.get("IdSolicitudAporteRescate").getAsLong() : null)
                .addValue("MontoComision", s.has("MontoComision") && !s.get("MontoComision").isJsonNull() ? s.get("MontoComision").getAsBigDecimal() : BigDecimal.ZERO));
        }

        if (!batch.isEmpty()) jdbc.batchUpdate(sqlActualizar, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSol = new JsonArray();
        for (Map<String, Object> row : solicitudesExistentes) arrSol.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arrSol);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : seriesFondo) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arrSeries);
        return out;
    }
}
