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

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ConsultarSolicitudesFFMM.
 */
@Service
public class ConsultarSolicitudesFFMMService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSolicitudesFFMMService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ConsultarSolicitudesFFMMService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarSolicitudesFFMM_SelectSolicitudes.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarSolicitudesFFMM");
        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L)
            .addValue("IdCliente", params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L)
            .addValue("TipoSolicitud", getS(params, "p_TipoSolicitud"))
            .addValue("Origen", getS(params, "p_Origen"))
            .addValue("TipoAhorro", getS(params, "p_TipoAhorro"))
            .addValue("TipoCuenta", getS(params, "p_TipoCuenta"))
            .addValue("IdSerieFondo", getS(params, "p_IdSerieFondo"))
            .addValue("Estado", getS(params, "p_Estado"))
            .addValue("FechaDesde", getS(params, "p_FechaDesde"))
            .addValue("FechaHasta", getS(params, "p_FechaHasta"))
            .addValue("Folio", getS(params, "p_Folio"));

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        return out;
    }

    private String getS(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }
}
