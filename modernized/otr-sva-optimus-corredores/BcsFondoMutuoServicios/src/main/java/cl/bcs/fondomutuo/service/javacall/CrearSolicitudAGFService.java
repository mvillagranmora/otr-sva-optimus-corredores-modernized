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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class CrearSolicitudAGFService {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudAGFService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitud;
    private final String sqlSelectBeneficiario;
    private final String sqlInsertBeneficiario;
    private final String sqlInsertSolicitud;

    public CrearSolicitudAGFService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitud = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_SelectSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectBeneficiario = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_SelectBeneficiario.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertBeneficiario = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_InsertBeneficiario.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertSolicitud = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearSolicitudAGF_InsertSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearSolicitudAGF");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa);

        List<Map<String, Object>> solicitud = jdbc.queryForList(sqlSelectSolicitud, ps);
        List<Map<String, Object>> beneficiario = jdbc.queryForList(sqlSelectBeneficiario, ps);

        jdbc.update(sqlInsertSolicitud, ps);
        jdbc.update(sqlInsertBeneficiario, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSol = new JsonArray();
        for (Map<String, Object> row : solicitud) arrSol.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUD", arrSol);
        JsonArray arrBen = new JsonArray();
        for (Map<String, Object> row : beneficiario) arrBen.add(GSON.toJsonTree(row));
        out.add("P_OUT_BENEFICIARIO", arrBen);
        return out;
    }
}
