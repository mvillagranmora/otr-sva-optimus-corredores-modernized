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
public class GenerarDataReporteTRXIFolioContratoService {

    private static final Logger log = LoggerFactory.getLogger(GenerarDataReporteTRXIFolioContratoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitudes;

    public GenerarDataReporteTRXIFolioContratoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitudes = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXIFolioContrato_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("GenerarDataReporteTRXIFolioContrato");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String folioContrato = params.has("p_FolioContrato") ? params.get("p_FolioContrato").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FolioContrato", folioContrato);

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelectSolicitudes, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : solicitudes) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        return out;
    }
}
