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
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ConsultarSolicitudAporteRescateUI.
 */
@Service
public class ConsultarSolicitudAporteRescateUIService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSolicitudAporteRescateUIService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ConsultarSolicitudAporteRescateUIService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarSolicitudAporteRescateUI");
        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L)
            .addValue("FechaAceptacionDesde", getS(params, "p_FechaAceptacionDesde"))
            .addValue("FechaAceptacionHasta", getS(params, "p_FechaAceptacionHasta"))
            .addValue("IdTipoMovimientoFondoMutuo", getS(params, "p_IdTipoMovimientoFondoMutuo"))
            .addValue("IdEmisor", getS(params, "p_IdEmisor"))
            .addValue("IdSerieFondo", getS(params, "p_IdSerieFondo"))
            .addValue("IdCliente", params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L)
            .addValue("IdProductoAgrupador", params.has("p_IdProductoAgrupador") ? params.get("p_IdProductoAgrupador").getAsLong() : 0L)
            .addValue("IdPortafolio", params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L)
            .addValue("TipoSolicitud", getS(params, "p_TipoSolicitud"))
            .addValue("IdMoneda", getS(params, "p_IdMoneda"))
            .addValue("IdSolicitudAporteRescate", params.has("p_IdSolicitudAporteRescate") ? params.get("p_IdSolicitudAporteRescate").getAsLong() : 0L);

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
