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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RecaudacionTraspasoService {

    private static final Logger log = LoggerFactory.getLogger(RecaudacionTraspasoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectTraspaso;
    private final String sqlSelectTraspasoDestino;
    private final String sqlUpdateTraspasoDestino;
    private final String sqlUpdateTraspaso;
    private final String sqlInsertRecaudacion;
    private final String sqlInsertRecaudacionMovimiento;
    private final String sqlCuentaBancaria;

    public RecaudacionTraspasoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectTraspaso = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Recaudacion_SelectTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectTraspasoDestino = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Recaudacion_SelectTraspasoDestino.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateTraspasoDestino = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Recaudacion_UpdateTraspasoDestino.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateTraspaso = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Recaudacion_UpdateTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertRecaudacion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Recaudacion_InsertRecaudacion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertRecaudacionMovimiento = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Recaudacion_InsertRecaudacionMovimiento.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlCuentaBancaria = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuentaBancariaTraspaso.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("RecaudacionTraspaso");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray traspasos = params.has("p_Traspasos") ? params.getAsJsonArray("p_Traspasos") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            if (t.has("IdTraspaso")) ids.add(t.get("IdTraspaso").getAsLong());
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdTraspaso", ids.isEmpty() ? List.of(0L) : ids);

        List<Map<String, Object>> trasp = jdbc.queryForList(sqlSelectTraspaso, ps);
        List<Map<String, Object>> dest = jdbc.queryForList(sqlSelectTraspasoDestino, ps);
        List<Map<String, Object>> cuentas = jdbc.queryForList(sqlCuentaBancaria, ps);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdTraspaso", t.has("IdTraspaso") ? t.get("IdTraspaso").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa));
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(sqlUpdateTraspasoDestino, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateTraspaso, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertRecaudacion, batch.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertRecaudacionMovimiento, batch.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrT = new JsonArray();
        for (Map<String, Object> row : trasp) arrT.add(GSON.toJsonTree(row));
        out.add("P_OUT_TRASPASOS", arrT);
        JsonArray arrD = new JsonArray();
        for (Map<String, Object> row : dest) arrD.add(GSON.toJsonTree(row));
        out.add("P_OUT_DESTINO", arrD);
        JsonArray arrC = new JsonArray();
        for (Map<String, Object> row : cuentas) arrC.add(GSON.toJsonTree(row));
        out.add("P_OUT_CUENTAS_BANCARIAS", arrC);
        return out;
    }
}
