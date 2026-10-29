package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.PrevisualizarDesasignacion (JavaCall).
 */
@Service
public class PrevisualizarDesasignacionService {

    private static final Logger log = LoggerFactory.getLogger(PrevisualizarDesasignacionService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_SELECT_ASIGNACIONES;
    private final String SQL_SELECT_ASIGNACIONES_BY_ID;

    public PrevisualizarDesasignacionService(NamedParameterJdbcTemplate jdbc, RequisitosBasicosUtil requisitosUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_SELECT_ASIGNACIONES       = loadSql("cl/bcs/ordenes/sql/PrevisualizarDesasignacion_ConsultarAsignacionesParaAnular.sql");
        this.SQL_SELECT_ASIGNACIONES_BY_ID = loadSql("cl/bcs/ordenes/sql/PrevisualizarDesasignacion_ConsultarAsignacionesParaAnularMasivaPorId.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("PrevisualizarDesasignacionService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;

            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            Long idAsignacionMasiva = params.has("p_IdAsignacionMasiva") && !params.get("p_IdAsignacionMasiva").isJsonNull()
                ? params.get("p_IdAsignacionMasiva").getAsLong() : null;

            List<Map<String, Object>> asignaciones;
            if (idAsignacionMasiva != null) {
                asignaciones = jdbc.queryForList(SQL_SELECT_ASIGNACIONES_BY_ID,
                    new MapSqlParameterSource("IdAsignacionMasiva", idAsignacionMasiva));
            } else {
                MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdLibroOrdenes", params.has("p_IdLibroOrdenes") ? params.get("p_IdLibroOrdenes").getAsLong() : null)
                    .addValue("IdClasificacionTipoInstrumento", params.has("p_IdClasificacionTipoInstrumento") ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : null)
                    .addValue("IdTipoInstrumento", params.has("p_IdTipoInstrumento") ? params.get("p_IdTipoInstrumento").getAsString() : null)
                    .addValue("IdAccionOrden", params.has("p_IdAccionOrden") ? params.get("p_IdAccionOrden").getAsString() : null)
                    .addValue("IdBolsa", params.has("p_IdBolsa") ? params.get("p_IdBolsa").getAsString() : null)
                    .addValue("FechaAsignacionDesde", params.has("p_FechaDesde") ? params.get("p_FechaDesde").getAsString() : null)
                    .addValue("FechaAsignacionHasta", params.has("p_FechaHasta") ? params.get("p_FechaHasta").getAsString() : null);
                asignaciones = jdbc.queryForList(SQL_SELECT_ASIGNACIONES, sqlParams);
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_ASIGNACIONES", JsonParser.parseString(GSON.toJson(asignaciones)));
            return out;
        } catch (Exception e) {
            log.error("PrevisualizarDesasignacionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = PrevisualizarDesasignacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
