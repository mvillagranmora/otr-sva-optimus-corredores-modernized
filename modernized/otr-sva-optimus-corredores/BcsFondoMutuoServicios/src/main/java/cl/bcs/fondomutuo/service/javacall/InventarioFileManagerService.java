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
public class InventarioFileManagerService {

    private static final Logger log = LoggerFactory.getLogger(InventarioFileManagerService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectDetalle;
    private final String sqlSelectCabecera;

    public InventarioFileManagerService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectDetalle = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCabecera = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectCabecera.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("InventarioFileManager");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fecha = params.has("p_FechaCuadratura") ? params.get("p_FechaCuadratura").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaCuadratura", fecha);

        List<Map<String, Object>> cabeceras = jdbc.queryForList(sqlSelectCabecera, ps);
        List<Map<String, Object>> detalles = jdbc.queryForList(sqlSelectDetalle, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrCab = new JsonArray();
        for (Map<String, Object> row : cabeceras) arrCab.add(GSON.toJsonTree(row));
        out.add("P_OUT_CABECERAS", arrCab);
        JsonArray arrDet = new JsonArray();
        for (Map<String, Object> row : detalles) arrDet.add(GSON.toJsonTree(row));
        out.add("P_OUT_DETALLES", arrDet);
        return out;
    }
}
