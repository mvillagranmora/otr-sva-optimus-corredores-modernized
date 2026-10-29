package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.List;
import java.util.Map;

@Service
public class SimulaCalculoSpreadRescateService {

    private static final Logger log = LoggerFactory.getLogger(SimulaCalculoSpreadRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitudes;

    public SimulaCalculoSpreadRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitudes = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("SimulaCalculoSpreadRescate");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idSolicitudAporteRescate = params.has("p_IdSolicitudAporteRescate") ? params.get("p_IdSolicitudAporteRescate").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSolicitudAporteRescate", List.of(idSolicitudAporteRescate));

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelectSolicitudes, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : solicitudes) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        return out;
    }
}
