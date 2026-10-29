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
public class ModificarCSCService {

    private static final Logger log = LoggerFactory.getLogger(ModificarCSCService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInsertContrato;
    private final String sqlInsertContratoDetalle;
    private final String sqlSelectContratoById;
    private final String sqlSelectFondoById;
    private final String sqlConsultarPortafolio;
    private final String sqlConsultarMandatoPAC;
    private final String sqlInsertarMandatoPAC;

    public ModificarCSCService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlInsertContrato = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContrato.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertContratoDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContratoDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectContratoById = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectContratoById.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectFondoById = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectFondoById.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultarPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarCSC_ConsultarPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultarMandatoPAC = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarCSC_ConsultarMandatoPAC.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertarMandatoPAC = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarCSC_InsertarMandatoPAC.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ModificarCSC");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idContratoCSC = params.has("p_IdContratoCSC") ? params.get("p_IdContratoCSC").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdContratoCSC", idContratoCSC);

        List<Map<String, Object>> contrato = jdbc.queryForList(sqlSelectContratoById, ps);
        List<Map<String, Object>> fondo = jdbc.queryForList(sqlSelectFondoById, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlConsultarPortafolio, ps);
        List<Map<String, Object>> mandatoPAC = jdbc.queryForList(sqlConsultarMandatoPAC, ps);

        jdbc.update(sqlInsertContrato, ps);
        jdbc.update(sqlInsertContratoDetalle, ps);
        jdbc.update(sqlInsertarMandatoPAC, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrContrato = new JsonArray();
        for (Map<String, Object> row : contrato) arrContrato.add(GSON.toJsonTree(row));
        out.add("P_OUT_CONTRATO", arrContrato);
        JsonArray arrFondo = new JsonArray();
        for (Map<String, Object> row : fondo) arrFondo.add(GSON.toJsonTree(row));
        out.add("P_OUT_FONDO", arrFondo);
        JsonArray arrPort = new JsonArray();
        for (Map<String, Object> row : portafolio) arrPort.add(GSON.toJsonTree(row));
        out.add("P_OUT_PORTAFOLIO", arrPort);
        JsonArray arrMandato = new JsonArray();
        for (Map<String, Object> row : mandatoPAC) arrMandato.add(GSON.toJsonTree(row));
        out.add("P_OUT_MANDATO_PAC", arrMandato);
        return out;
    }
}
