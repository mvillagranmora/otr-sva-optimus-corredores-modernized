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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnulacionSolicitudAporteRescateFolioContratoService {

    private static final Logger log = LoggerFactory.getLogger(AnulacionSolicitudAporteRescateFolioContratoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlSelectTraspaso;
    private final String sqlUpdateTraspaso;

    public AnulacionSolicitudAporteRescateFolioContratoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescateFolioContrato_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectTraspaso = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultaTraspasoEgresoAPVAsociado.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateTraspaso = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarTraspasoEgresoAPVAsociado.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("AnulacionSolicitudAporteRescateFolioContrato");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String folioContrato = params.has("p_FolioContrato") ? params.get("p_FolioContrato").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FolioContrato", folioContrato);

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelect, ps);

        List<Long> ids = new ArrayList<>();
        for (Map<String, Object> s : solicitudes) {
            Object id = s.get("IdSolicitudAporteRescate");
            if (id != null) ids.add(((Number) id).longValue());
        }

        if (!ids.isEmpty()) {
            MapSqlParameterSource psTraspaso = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdSolicitudAporteRescate", ids);
            List<Map<String, Object>> traspasos = jdbc.queryForList(sqlSelectTraspaso, psTraspaso);
            if (!traspasos.isEmpty()) jdbc.update(sqlUpdateTraspaso, psTraspaso);
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : solicitudes) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        return out;
    }
}
