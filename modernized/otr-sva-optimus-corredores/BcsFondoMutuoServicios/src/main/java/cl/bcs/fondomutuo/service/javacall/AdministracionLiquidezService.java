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
public class AdministracionLiquidezService {

    private static final Logger log = LoggerFactory.getLogger(AdministracionLiquidezService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectPortafolios;
    private final String sqlInsertEjecucion;
    private final String sqlInsertLog;
    private final String sqlObtenerFechaActual;

    public AdministracionLiquidezService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectPortafolios = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AdministracionLiquidez_SelectPortafolios.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertEjecucion = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AdministracionLiquidez_InsertEjecucion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertLog = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AdministracionLiquidez_InsertLog.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlObtenerFechaActual = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AdministracionLiquidez_ObtenerFechaActual.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("AdministracionLiquidez");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo);

        List<Map<String, Object>> portafolios = jdbc.queryForList(sqlSelectPortafolios, ps);
        List<Map<String, Object>> fechaActual = jdbc.queryForList(sqlObtenerFechaActual, ps);

        jdbc.update(sqlInsertEjecucion, ps);
        jdbc.update(sqlInsertLog, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrPortafolios = new JsonArray();
        for (Map<String, Object> row : portafolios) arrPortafolios.add(GSON.toJsonTree(row));
        out.add("P_OUT_PORTAFOLIOS", arrPortafolios);
        if (!fechaActual.isEmpty()) out.add("P_OUT_FECHA_ACTUAL", GSON.toJsonTree(fechaActual.get(0)));
        return out;
    }
}
