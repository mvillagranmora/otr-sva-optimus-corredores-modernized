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
 * Migrated from legacy cl.bcs.ordenes.CrearInstrucciones (JavaCall).
 */
@Service
public class CrearInstruccionesService {

    private static final Logger log = LoggerFactory.getLogger(CrearInstruccionesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_INSERT_INSTRUCCIONES;
    private final String SQL_SELECT_TIPO_OPERACION;
    private final String SQL_SELECT_MOTIVO;

    public CrearInstruccionesService(NamedParameterJdbcTemplate jdbc, RequisitosBasicosUtil requisitosUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_INSERT_INSTRUCCIONES = loadSql("cl/bcs/ordenes/sql/CrearInstrucciones_InsertInstrucciones.sql");
        this.SQL_SELECT_TIPO_OPERACION = loadSql("cl/bcs/ordenes/sql/CrearInstrucciones_SelectTipoOperacion.sql");
        this.SQL_SELECT_MOTIVO = loadSql("cl/bcs/ordenes/sql/CrearInstrucciones_SelectMotivoInstruccion.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CrearInstruccionesService.execute()");
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
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_INSTRUCCIONES\"}]"));
                return out;
            }

            Timestamp ahora = Timestamp.from(Instant.now());
            String idCanalIngreso = params.has("p_IdTipoCanalIngreso") ? params.get("p_IdTipoCanalIngreso").getAsString() : "";

            // Validate tipo operacion and motivo via SQL
            List<Map<String, Object>> tiposOp = jdbc.queryForList(SQL_SELECT_TIPO_OPERACION,
                new MapSqlParameterSource("IdEmpresa", idEmpresa));
            List<Map<String, Object>> motivos = jdbc.queryForList(SQL_SELECT_MOTIVO,
                new MapSqlParameterSource("IdEmpresa", idEmpresa));

            List<Map<String, Object>> refIds = new ArrayList<>();
            List<Map<String, Object>> mensajes = new ArrayList<>();

            for (var el : instruccionesIn) {
                JsonObject instIn = el.getAsJsonObject();

                String idTipoOperacion = instIn.has("IdTipoOperacion") ? instIn.get("IdTipoOperacion").getAsString() : null;
                String idMotivoInstruccion = instIn.has("IdMotivoInstruccion") ? instIn.get("IdMotivoInstruccion").getAsString() : null;

                // Validate tipo operacion
                if (idTipoOperacion != null) {
                    boolean tipoValido = tiposOp.stream().anyMatch(t ->
                        idTipoOperacion.equalsIgnoreCase(String.valueOf(t.get("IdTipoOperacion"))));
                    if (!tipoValido) {
                        mensajes.add(Map.of("tipo", "ERROR", "codigo", "TIPO_OPERACION_NO_VALIDO", "IdInterno", 0L));
                        continue;
                    }
                }

                jdbc.update(SQL_INSERT_INSTRUCCIONES,
                    new MapSqlParameterSource()
                        .addValue("IdEmpresa", idEmpresa)
                        .addValue("IdUsuario", idUsuario)
                        .addValue("FechaHoraSistema", ahora)
                        .addValue("IdTipoCanalIngreso", idCanalIngreso)
                        .addValue("IdTipoOperacion", idTipoOperacion)
                        .addValue("IdMotivoInstruccion", idMotivoInstruccion)
                        .addValue("IdOrden", instIn.has("IdOrden") ? instIn.get("IdOrden").getAsLong() : null)
                        .addValue("IdAsignacion", instIn.has("IdAsignacion") ? instIn.get("IdAsignacion").getAsLong() : null)
                        .addValue("Glosa", instIn.has("Glosa") ? instIn.get("Glosa").getAsString() : null)
                        .addValue("IdInterno", instIn.has("IdInterno") ? instIn.get("IdInterno").getAsLong() : null));

                Long idInterno = instIn.has("IdInterno") ? instIn.get("IdInterno").getAsLong() : 0L;
                refIds.add(Map.of("IdInterno", idInterno, "Creado", true));
            }

            long errores = mensajes.stream().filter(m -> "ERROR".equals(m.get("tipo"))).count();
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(mensajes)));
            out.add("P_OUT_REFID", JsonParser.parseString(GSON.toJson(refIds)));
            return out;
        } catch (Exception e) {
            log.error("CrearInstruccionesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = CrearInstruccionesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
