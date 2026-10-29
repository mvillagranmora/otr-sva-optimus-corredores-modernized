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

@Service
public class ModificarTraspasoService {

    private static final Logger log = LoggerFactory.getLogger(ModificarTraspasoService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlUpdate;
    private final String sqlUpdateDetalleOrigen;
    private final String sqlUpdateDetalleDestino;
    private final String sqlInsertDetalleOrigen;
    private final String sqlInsertDetalleDestino;

    public ModificarTraspasoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdate = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_Update.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateDetalleOrigen = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_UpdateDetalleOrigen.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateDetalleDestino = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_UpdateDetalleDestino.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertDetalleOrigen = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleOrigen.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertDetalleDestino = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleDestino.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ModificarTraspaso");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray traspasos = params.has("p_Traspasos") ? params.getAsJsonArray("p_Traspasos") : new JsonArray();

        List<SqlParameterSource> batchTraspaso = new ArrayList<>();
        List<SqlParameterSource> batchOrigen = new ArrayList<>();
        List<SqlParameterSource> batchDestino = new ArrayList<>();

        for (JsonElement e : traspasos) {
            JsonObject t = e.getAsJsonObject();
            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("IdTraspaso", t.has("IdTraspaso") ? t.get("IdTraspaso").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa);
            batchTraspaso.add(ps);
            batchOrigen.add(ps);
            batchDestino.add(ps);
        }

        if (!batchTraspaso.isEmpty()) {
            jdbc.batchUpdate(sqlUpdate, batchTraspaso.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateDetalleOrigen, batchOrigen.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateDetalleDestino, batchDestino.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertDetalleOrigen, batchOrigen.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertDetalleDestino, batchDestino.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
