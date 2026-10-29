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
public class CrearAporteSAAPVService {

    private static final Logger log = LoggerFactory.getLogger(CrearAporteSAAPVService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectDatosAPV;
    private final String sqlSelectIdPortafolio;
    private final String sqlSelectPrimerIdPortafolio;
    private final String sqlSelectDatosPersona;
    private final String sqlUpdateContratoSAAPV;
    private final String sqlSelectConsultaBanco;
    private final String sqlSelectContratoSAAPV;
    private final String sqlSelectContratoPortafolioSAAPV;

    public CrearAporteSAAPVService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectDatosAPV = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectDatosAPV.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectIdPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectIdPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPrimerIdPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectPrimerIdPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectDatosPersona = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectDatosPersona.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateContratoSAAPV = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_UpdateContratoSAAPV.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConsultaBanco = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectConsultaBanco.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectContratoSAAPV = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectContratoSAAPV.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectContratoPortafolioSAAPV = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectContratoPortafolioSAAPV.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearAporteSAAPV");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdPortafolio", idPortafolio)
            .addValue("IdCliente", idCliente)
            .addValue("IdSerieFondo", idSerieFondo);

        List<Map<String, Object>> datosAPV = jdbc.queryForList(sqlSelectDatosAPV, ps);
        List<Map<String, Object>> idPortafolioRows = jdbc.queryForList(sqlSelectIdPortafolio, ps);
        List<Map<String, Object>> primerIdPortafolio = jdbc.queryForList(sqlSelectPrimerIdPortafolio, ps);
        List<Map<String, Object>> datosPersona = jdbc.queryForList(sqlSelectDatosPersona, ps);
        List<Map<String, Object>> banco = jdbc.queryForList(sqlSelectConsultaBanco, ps);
        List<Map<String, Object>> contratoSAAPV = jdbc.queryForList(sqlSelectContratoSAAPV, ps);
        List<Map<String, Object>> contratoPortafolio = jdbc.queryForList(sqlSelectContratoPortafolioSAAPV, ps);

        jdbc.update(sqlUpdateContratoSAAPV, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        addArray(out, "P_OUT_DATOS_APV", datosAPV);
        addArray(out, "P_OUT_ID_PORTAFOLIO", idPortafolioRows);
        addArray(out, "P_OUT_PRIMER_ID_PORTAFOLIO", primerIdPortafolio);
        addArray(out, "P_OUT_DATOS_PERSONA", datosPersona);
        addArray(out, "P_OUT_BANCO", banco);
        addArray(out, "P_OUT_CONTRATO_SAAPV", contratoSAAPV);
        addArray(out, "P_OUT_CONTRATO_PORTAFOLIO", contratoPortafolio);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
