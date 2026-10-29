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
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarInstrumentosFondosMutuosService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarInstrumentosFondosMutuosService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlFondoMutuo;
    private final String sqlFondoMutuoReinversion;
    private final String sqlRegimen;

    public ConsultarInstrumentosFondosMutuosService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlFondoMutuo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarInstrumentosFondosMutuos_SelectFondoMutuo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlFondoMutuoReinversion = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarInstrumentosFondosMutuos_SelectFondoMutuoReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlRegimen = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarInstrumentosFondosMutuos_SelectRegimen.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarInstrumentosFondosMutuos");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonObject entrada = params.has("p_EntradaFondosMutuos") ? params.getAsJsonObject("p_EntradaFondosMutuos") : new JsonObject();

        String idSerieFondo = entrada.has("IdSerieFondo") ? entrada.get("IdSerieFondo").getAsString() : "";
        String idEmisor = entrada.has("IdEmisor") ? entrada.get("IdEmisor").getAsString() : "";
        String idFamiliaFondo = entrada.has("IdFamiliaFondo") ? entrada.get("IdFamiliaFondo").getAsString() : "";

        List<String> regimenes = new ArrayList<>();
        if (entrada.has("IdRegimenFondoMutuo") && entrada.get("IdRegimenFondoMutuo").isJsonArray()) {
            for (JsonElement e : entrada.getAsJsonArray("IdRegimenFondoMutuo")) {
                regimenes.add(e.getAsString());
            }
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdEmisor", idEmisor)
            .addValue("IdFamiliaFondo", idFamiliaFondo)
            .addValue("FiltroRegimen", regimenes.isEmpty() ? 0L : 1L)
            .addValue("IdRegimenFondoMutuo", regimenes);

        List<Map<String, Object>> fondos = jdbc.queryForList(sqlFondoMutuo, ps);
        List<Map<String, Object>> fondosReinversion = jdbc.queryForList(sqlFondoMutuoReinversion, ps);
        List<Map<String, Object>> regimen = jdbc.queryForList(sqlRegimen, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrFondos = new JsonArray();
        for (Map<String, Object> row : fondos) arrFondos.add(GSON.toJsonTree(row));
        out.add("P_OUT_FONDOS_MUTUOS", arrFondos);
        JsonArray arrReinv = new JsonArray();
        for (Map<String, Object> row : fondosReinversion) arrReinv.add(GSON.toJsonTree(row));
        out.add("P_OUT_FONDOS_MUTUOS_REINVERSION", arrReinv);
        JsonArray arrReg = new JsonArray();
        for (Map<String, Object> row : regimen) arrReg.add(GSON.toJsonTree(row));
        out.add("P_OUT_REGIMEN", arrReg);
        return out;
    }
}
