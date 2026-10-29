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
public class CrearComisionService {

    private static final Logger log = LoggerFactory.getLogger(CrearComisionService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInsertComision;
    private final String sqlInsertTramo;

    public CrearComisionService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlInsertComision = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CrearComision_InsertComision.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertTramo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CrearTramoComision_InsertTramo.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearComision");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonObject comision = params.has("p_Comision") ? params.getAsJsonObject("p_Comision") : new JsonObject();

        MapSqlParameterSource psComision = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("Nombre", comision.has("Nombre") ? comision.get("Nombre").getAsString() : "")
            .addValue("IdTipoComision", comision.has("IdTipoComision") ? comision.get("IdTipoComision").getAsString() : "")
            .addValue("IdMoneda", comision.has("IdMoneda") ? comision.get("IdMoneda").getAsString() : "");

        jdbc.update(sqlInsertComision, psComision);

        JsonArray tramos = comision.has("TramoComision") ? comision.getAsJsonArray("TramoComision") : new JsonArray();
        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : tramos) {
            JsonObject t = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdComision", comision.has("IdComision") ? comision.get("IdComision").getAsLong() : null)
                .addValue("MontoDesde", t.has("MontoDesde") && !t.get("MontoDesde").isJsonNull() ? t.get("MontoDesde").getAsBigDecimal() : BigDecimal.ZERO)
                .addValue("MontoHasta", t.has("MontoHasta") && !t.get("MontoHasta").isJsonNull() ? t.get("MontoHasta").getAsBigDecimal() : BigDecimal.ZERO)
                .addValue("Porcentaje", t.has("Porcentaje") && !t.get("Porcentaje").isJsonNull() ? t.get("Porcentaje").getAsBigDecimal() : BigDecimal.ZERO)
                .addValue("IdEmpresa", idEmpresa));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlInsertTramo, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
