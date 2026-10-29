package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ModificarInstrucciones (JavaCall).
 */
@Service
public class ModificarInstruccionesService {

    private static final Logger log = LoggerFactory.getLogger(ModificarInstruccionesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_SELECT_INSTRUCCIONES;
    private final String SQL_UPDATE_INSTRUCCIONES;

    public ModificarInstruccionesService(NamedParameterJdbcTemplate jdbc, RequisitosBasicosUtil requisitosUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_SELECT_INSTRUCCIONES = loadSql("cl/bcs/ordenes/sql/ModificarInstrucciones_SelectInstrucciones.sql");
        this.SQL_UPDATE_INSTRUCCIONES = loadSql("cl/bcs/ordenes/sql/ModificarInstrucciones_UpdateInstrucciones.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ModificarInstruccionesService.execute()");
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

            JsonArray instruccionesIn = params.has("p_Instrucciones") ? params.get("p_Instrucciones").getAsJsonArray() : new JsonArray();
            if (instruccionesIn.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_ENCONTRARON_INSTRUCCIONES\"}]"));
                return out;
            }

            List<Long> idsInstrucciones = new ArrayList<>();
            for (var el : instruccionesIn) {
                JsonObject inst = el.getAsJsonObject();
                if (inst.has("IdInstruccion")) idsInstrucciones.add(inst.get("IdInstruccion").getAsLong());
            }

            // Fetch current instructions
            List<Map<String, Object>> instrucciones = jdbc.queryForList(SQL_SELECT_INSTRUCCIONES,
                new MapSqlParameterSource("idsInstrucciones", idsInstrucciones));

            if (instrucciones.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_ENCONTRARON_INSTRUCCIONES\"}]"));
                return out;
            }

            // Build map for quick lookup
            Timestamp ahora = Timestamp.from(Instant.now());
            String idCanalIngreso = params.has("p_IdTipoCanalIngreso") ? params.get("p_IdTipoCanalIngreso").getAsString() : "";

            // Update each instruction
            List<Map<String, Object>> refIds = new ArrayList<>();
            for (var el : instruccionesIn) {
                JsonObject instIn = el.getAsJsonObject();
                Long idInstruccion = instIn.has("IdInstruccion") ? instIn.get("IdInstruccion").getAsLong() : null;
                if (idInstruccion == null) continue;

                jdbc.update(SQL_UPDATE_INSTRUCCIONES,
                    new MapSqlParameterSource("IdInstruccion", idInstruccion)
                        .addValue("IdEmpresa", idEmpresa)
                        .addValue("IdUsuario", idUsuario)
                        .addValue("FechaHoraSistema", ahora)
                        .addValue("IdTipoCanalIngreso", idCanalIngreso)
                        .addValue("Estado", instIn.has("Estado") ? instIn.get("Estado").getAsString() : null)
                        .addValue("Glosa", instIn.has("Glosa") ? instIn.get("Glosa").getAsString() : null));

                refIds.add(Map.of("IdInterno", idInstruccion, "IdInstruccion", idInstruccion));
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_REFID", JsonParser.parseString(GSON.toJson(refIds)));
            return out;
        } catch (Exception e) {
            log.error("ModificarInstruccionesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = ModificarInstruccionesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
