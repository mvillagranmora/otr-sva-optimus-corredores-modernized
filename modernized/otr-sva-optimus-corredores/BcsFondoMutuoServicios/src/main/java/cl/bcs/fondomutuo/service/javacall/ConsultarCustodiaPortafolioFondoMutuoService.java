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
public class ConsultarCustodiaPortafolioFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaPortafolioFondoMutuoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSolicitudes;
    private final String sqlSolicitudesTotales;
    private final String sqlSeries;
    private final String sqlRegimen;

    public ConsultarCustodiaPortafolioFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSolicitudes = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuo_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSolicitudesTotales = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuo_SelectSolicitudesTotales.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSeries = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuo_SelectSeries.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlRegimen = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuo_SelectRegimen.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarCustodiaPortafolioFondoMutuo");

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L)
            .addValue("IdCliente", params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L)
            .addValue("IdPortafolio", params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L)
            .addValue("IdInstrumento", params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsLong() : 0L)
            .addValue("IdProductoAgrupador", params.has("p_IdProductoAgrupador") && !params.get("p_IdProductoAgrupador").isJsonNull() ? params.get("p_IdProductoAgrupador").getAsLong() : null)
            .addValue("IdEmisor", params.has("p_IdEmisor") ? params.get("p_IdEmisor").getAsString() : "")
            .addValue("IdFamiliaFondo", params.has("p_IdFamiliaFondo") ? params.get("p_IdFamiliaFondo").getAsString() : "")
            .addValue("IngresoAutomatico", "")
            .addValue("FiltraIngresoAutomatico", "N");

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSolicitudes, ps);
        List<Map<String, Object>> totales = jdbc.queryForList(sqlSolicitudesTotales, ps);
        List<Map<String, Object>> series = jdbc.queryForList(sqlSeries, ps);
        List<Map<String, Object>> regimen = jdbc.queryForList(sqlRegimen, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSol = new JsonArray();
        for (Map<String, Object> row : solicitudes) arrSol.add(GSON.toJsonTree(row));
        out.add("P_OUT_TBL", arrSol);
        JsonArray arrTot = new JsonArray();
        for (Map<String, Object> row : totales) arrTot.add(GSON.toJsonTree(row));
        out.add("P_OUT_TBL_TOTALES", arrTot);
        JsonArray arrSer = new JsonArray();
        for (Map<String, Object> row : series) arrSer.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arrSer);
        JsonArray arrReg = new JsonArray();
        for (Map<String, Object> row : regimen) arrReg.add(GSON.toJsonTree(row));
        out.add("P_OUT_REGIMEN", arrReg);
        return out;
    }
}
