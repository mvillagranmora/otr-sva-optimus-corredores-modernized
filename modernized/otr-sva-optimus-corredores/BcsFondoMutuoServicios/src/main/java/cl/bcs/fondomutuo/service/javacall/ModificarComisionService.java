package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModificarComisionService {

    private static final Logger log = LoggerFactory.getLogger(ModificarComisionService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlUpdate;

    public ModificarComisionService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarComision_UpdateComision.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ModificarComision");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray comisiones = params.has("p_Comisiones") ? params.getAsJsonArray("p_Comisiones") : new JsonArray();
        // Allow single comision
        if (comisiones.size() == 0 && params.has("p_Comision")) {
            comisiones = new JsonArray();
            comisiones.add(params.getAsJsonObject("p_Comision"));
        }

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : comisiones) {
            JsonObject c = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdComision", c.has("IdComision") ? c.get("IdComision").getAsLong() : null)
                .addValue("Descripcion", c.has("Descripcion") ? c.get("Descripcion").getAsString() : "")
                .addValue("FechaVigenciaDesde", c.has("FechaVigenciaDesde") ? c.get("FechaVigenciaDesde").getAsString() : null)
                .addValue("FechaVigenciaHasta", c.has("FechaVigenciaHasta") ? c.get("FechaVigenciaHasta").getAsString() : null)
                .addValue("FechaSistema", c.has("FechaSistema") ? c.get("FechaSistema").getAsString() : null)
                .addValue("PorcentajeLiberado", c.has("PorcentajeLiberado") && !c.get("PorcentajeLiberado").isJsonNull() ? c.get("PorcentajeLiberado").getAsBigDecimal() : BigDecimal.ZERO)
                .addValue("Estado", c.has("Estado") ? c.get("Estado").getAsString() : ""));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlUpdate, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
