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
public class ConfirmarModificarFechaGestionOperacionesService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarModificarFechaGestionOperacionesService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlUpdateFechas;
    private final String sqlInsertCambios;
    private final String sqlSelectCambios;
    private final String sqlSelectFechas;

    public ConfirmarModificarFechaGestionOperacionesService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdateFechas = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarFechaGestionOperaciones_UpdateFechas.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertCambios = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/HistorialCambiosModificarFechas_InsertCambios.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCambios = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/HistorialCambiosModificarFechas_SelectCambios.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectFechas = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarFechaGestionOperaciones_SelectFechas.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ConfirmarModificarFechaGestionOperaciones");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;
        JsonArray solicitudes = params.has("p_Solicitudes") ? params.getAsJsonArray("p_Solicitudes") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        for (JsonElement e : solicitudes) {
            JsonObject s = e.getAsJsonObject();
            if (s.has("IdSolicitudAporteRescate")) ids.add(s.get("IdSolicitudAporteRescate").getAsLong());
        }

        List<Map<String, Object>> fechas = ids.isEmpty() ? List.of() :
            jdbc.queryForList(sqlSelectFechas, new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", ids).addValue("IdEmpresa", idEmpresa));

        List<SqlParameterSource> batchUpdate = new ArrayList<>();
        List<SqlParameterSource> batchInsert = new ArrayList<>();
        for (JsonElement e : solicitudes) {
            JsonObject s = e.getAsJsonObject();
            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", s.has("IdSolicitudAporteRescate") ? s.get("IdSolicitudAporteRescate").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdUsuario", idUsuario)
                .addValue("FechaAceptacion", s.has("FechaAceptacion") ? s.get("FechaAceptacion").getAsString() : null)
                .addValue("FechaLiquidacion", s.has("FechaLiquidacion") ? s.get("FechaLiquidacion").getAsString() : null);
            batchUpdate.add(ps);
            batchInsert.add(ps);
        }
        if (!batchUpdate.isEmpty()) {
            jdbc.batchUpdate(sqlUpdateFechas, batchUpdate.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertCambios, batchInsert.toArray(new SqlParameterSource[0]));
        }

        List<Map<String, Object>> cambios = ids.isEmpty() ? List.of() :
            jdbc.queryForList(sqlSelectCambios, new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", ids).addValue("IdEmpresa", idEmpresa));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrFechas = new JsonArray();
        for (Map<String, Object> row : fechas) arrFechas.add(GSON.toJsonTree(row));
        out.add("P_OUT_FECHAS", arrFechas);
        JsonArray arrCambios = new JsonArray();
        for (Map<String, Object> row : cambios) arrCambios.add(GSON.toJsonTree(row));
        out.add("P_OUT_CAMBIOS", arrCambios);
        return out;
    }
}
