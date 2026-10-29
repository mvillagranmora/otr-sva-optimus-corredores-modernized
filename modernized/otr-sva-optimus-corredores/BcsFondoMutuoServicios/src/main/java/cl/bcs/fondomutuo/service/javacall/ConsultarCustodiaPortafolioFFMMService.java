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
public class ConsultarCustodiaPortafolioFFMMService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaPortafolioFFMMService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlPersonaDestino;
    private final String sqlCustodia;
    private final String sqlGarantia;
    private final String sqlCustodiaHistorico;
    private final String sqlGarantiaHistorico;

    public ConsultarCustodiaPortafolioFFMMService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlPersonaDestino = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMM_SelectPersonaDestino.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlCustodia = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMM_SelectConsultarCustodia.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlGarantia = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMM_SelectConsultarGarantia.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlCustodiaHistorico = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMM_SelectConsultarCustodiaHistorico.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlGarantiaHistorico = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFFMM_SelectConsultarGarantiaHistorico.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarCustodiaPortafolioFFMM");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        String idInstrumento = params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsString() : "";
        String fechaConsulta = params.has("p_FechaConsulta") ? params.get("p_FechaConsulta").getAsString() : "0001-01-01";
        String idProductoAgrupador = params.has("p_IdProductoAgrupador") ? params.get("p_IdProductoAgrupador").getAsString() : "";
        String idEmisor = params.has("p_IdEmisor") ? params.get("p_IdEmisor").getAsString() : "";
        String idFamiliaFondo = params.has("p_IdFamiliaFondo") ? params.get("p_IdFamiliaFondo").getAsString() : "";
        String tipoFechaConsulta = params.has("p_TipoFechaConsulta") ? params.get("p_TipoFechaConsulta").getAsString() : "";

        boolean esHistorico = !"0001-01-01".equals(fechaConsulta);

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdCliente", idCliente)
            .addValue("IdPortafolio", idPortafolio)
            .addValue("IdInstrumento", idInstrumento)
            .addValue("FechaConsulta", fechaConsulta)
            .addValue("IdProductoAgrupador", idProductoAgrupador)
            .addValue("IdEmisor", idEmisor)
            .addValue("IdFamiliaFondo", idFamiliaFondo)
            .addValue("TipoFechaConsulta", tipoFechaConsulta);

        String sqlCustodiaUse = esHistorico ? sqlCustodiaHistorico : sqlCustodia;
        String sqlGarantiaUse = esHistorico ? sqlGarantiaHistorico : sqlGarantia;

        List<Map<String, Object>> custodias = jdbc.queryForList(sqlCustodiaUse, ps);
        List<Map<String, Object>> garantias = jdbc.queryForList(sqlGarantiaUse, ps);

        MapSqlParameterSource psPersona = new MapSqlParameterSource()
            .addValue("IdPersonaCuentaPropia", idEmpresa);
        List<Map<String, Object>> personas = jdbc.queryForList(sqlPersonaDestino, psPersona);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrCustodias = new JsonArray();
        for (Map<String, Object> row : custodias) arrCustodias.add(GSON.toJsonTree(row));
        out.add("P_OUT_CUSTODIAS", arrCustodias);
        JsonArray arrGarantias = new JsonArray();
        for (Map<String, Object> row : garantias) arrGarantias.add(GSON.toJsonTree(row));
        out.add("P_OUT_GARANTIAS", arrGarantias);
        JsonArray arrPersonas = new JsonArray();
        for (Map<String, Object> row : personas) arrPersonas.add(GSON.toJsonTree(row));
        out.add("P_OUT_PERSONAS_DESTINO", arrPersonas);
        return out;
    }
}
