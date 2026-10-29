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
public class CuadrarOmnibusDetalleService {

    private static final Logger log = LoggerFactory.getLogger(CuadrarOmnibusDetalleService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectValorCuota;
    private final String sqlSelectOmnibusDetalle;
    private final String sqlUpdateOmnibusDetalle;

    public CuadrarOmnibusDetalleService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectValorCuota = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadrarOmnibusDetalle_SelectValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectOmnibusDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadrarOmnibusDetalle_SelectOmnibusDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateOmnibusDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadrarOmnibusDetalle_UpdateOmnibusDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CuadrarOmnibusDetalle");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idOmnibus = params.has("p_IdOmnibus") ? params.get("p_IdOmnibus").getAsLong() : 0L;
        String fecha = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdOmnibus", idOmnibus)
            .addValue("Fecha", fecha);

        List<Map<String, Object>> valorCuota = jdbc.queryForList(sqlSelectValorCuota, ps);
        List<Map<String, Object>> omnibusDetalle = jdbc.queryForList(sqlSelectOmnibusDetalle, ps);

        jdbc.update(sqlUpdateOmnibusDetalle, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrVC = new JsonArray();
        for (Map<String, Object> row : valorCuota) arrVC.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALOR_CUOTA", arrVC);
        JsonArray arrOD = new JsonArray();
        for (Map<String, Object> row : omnibusDetalle) arrOD.add(GSON.toJsonTree(row));
        out.add("P_OUT_OMNIBUS_DETALLE", arrOD);
        return out;
    }
}
