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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RevocacionContratoCSCService {

    private static final Logger log = LoggerFactory.getLogger(RevocacionContratoCSCService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectValidaciones;
    private final String sqlSelectMandatos;
    private final String sqlUpdateContrato;
    private final String sqlUpdateContratoWS;

    public RevocacionContratoCSCService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectValidaciones = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_SelectValidaciones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectMandatos = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_SelectMandatos.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateContrato = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_UpdateContratoCSC.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateContratoWS = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_UpdateContratoCSCWS.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("RevocacionContratoCSC");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray contratosInput = params.has("p_Contrato") ? params.getAsJsonArray("p_Contrato") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        for (JsonElement e : contratosInput) {
            JsonObject c = e.getAsJsonObject();
            if (c.has("IdContratoCSC")) ids.add(c.get("IdContratoCSC").getAsLong());
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdContratoCSC", ids.isEmpty() ? List.of(0L) : ids);

        List<Map<String, Object>> validaciones = jdbc.queryForList(sqlSelectValidaciones, ps);
        List<Map<String, Object>> mandatos = jdbc.queryForList(sqlSelectMandatos, ps);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : contratosInput) {
            JsonObject c = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdContratoCSC", c.has("IdContratoCSC") ? c.get("IdContratoCSC").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa));
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(sqlUpdateContrato, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateContratoWS, batch.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrVal = new JsonArray();
        for (Map<String, Object> row : validaciones) arrVal.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALIDACIONES", arrVal);
        JsonArray arrMand = new JsonArray();
        for (Map<String, Object> row : mandatos) arrMand.add(GSON.toJsonTree(row));
        out.add("P_OUT_MANDATOS", arrMand);
        return out;
    }
}
