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
public class CrearIngresoCSCService {

    private static final Logger log = LoggerFactory.getLogger(CrearIngresoCSCService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInsertContrato;
    private final String sqlInsertContratoDetalle;
    private final String sqlSelectContrato;
    private final String sqlConsultarContratosCSC;
    private final String sqlConsultarMandatoPAC;
    private final String sqlConsultarPortafolio;
    private final String sqlInsertarMandatoPAC;
    private final String sqlInsertIndicadores;
    private final String sqlSelectFondoById;
    private final String sqlUpdate;
    private final String sqlUpdateOptimus;

    public CrearIngresoCSCService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlInsertContrato = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContrato.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertContratoDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContratoDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectContrato = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_SelectContrato.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultarContratosCSC = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_ConsultarContratosCSC.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultarMandatoPAC = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_ConsultarMandatoPAC.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultarPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_ConsultarPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertarMandatoPAC = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertarMandatoPAC.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertIndicadores = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteCSC_InsertIndicadores.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectFondoById = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectFondoById.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdate = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_Update.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateOptimus = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSC_UpdateOptimus.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearIngresoCSC");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdPortafolio", idPortafolio)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdCliente", idCliente);

        List<Map<String, Object>> contratosExistentes = jdbc.queryForList(sqlConsultarContratosCSC, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlConsultarPortafolio, ps);
        List<Map<String, Object>> mandatoPAC = jdbc.queryForList(sqlConsultarMandatoPAC, ps);
        List<Map<String, Object>> fondo = jdbc.queryForList(sqlSelectFondoById, ps);

        jdbc.update(sqlInsertContrato, ps);
        jdbc.update(sqlInsertContratoDetalle, ps);
        List<Map<String, Object>> contrato = jdbc.queryForList(sqlSelectContrato, ps);
        jdbc.update(sqlInsertarMandatoPAC, ps);
        jdbc.update(sqlInsertIndicadores, ps);
        jdbc.update(sqlUpdate, ps);
        jdbc.update(sqlUpdateOptimus, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        addArray(out, "P_OUT_CONTRATOS_CSC", contratosExistentes);
        addArray(out, "P_OUT_PORTAFOLIO", portafolio);
        addArray(out, "P_OUT_MANDATO_PAC", mandatoPAC);
        addArray(out, "P_OUT_FONDO", fondo);
        addArray(out, "P_OUT_CONTRATO", contrato);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
