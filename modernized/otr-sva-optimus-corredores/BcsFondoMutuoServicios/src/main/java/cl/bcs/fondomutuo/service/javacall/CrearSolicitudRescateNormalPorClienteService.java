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
public class CrearSolicitudRescateNormalPorClienteService {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudRescateNormalPorClienteService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSerieFondo;
    private final String sqlSelectPreferenciaPago;
    private final String sqlSelectPortafolio;

    public CrearSolicitudRescateNormalPorClienteService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSerieFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPreferenciaPago = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPreferenciaPago.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearSolicitudRescateNormalPorCliente_SelectPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CrearSolicitudRescateNormalPorCliente");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdPortafolio", idPortafolio);

        List<Map<String, Object>> series = jdbc.queryForList(sqlSelectSerieFondo, ps);
        List<Map<String, Object>> preferenciaPago = jdbc.queryForList(sqlSelectPreferenciaPago, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlSelectPortafolio, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : series) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIE_FONDO", arrSeries);
        JsonArray arrPref = new JsonArray();
        for (Map<String, Object> row : preferenciaPago) arrPref.add(GSON.toJsonTree(row));
        out.add("P_OUT_PREFERENCIA_PAGO", arrPref);
        JsonArray arrPort = new JsonArray();
        for (Map<String, Object> row : portafolio) arrPort.add(GSON.toJsonTree(row));
        out.add("P_OUT_PORTAFOLIO", arrPort);
        return out;
    }
}
