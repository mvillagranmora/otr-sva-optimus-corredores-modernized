package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarInformacionAsignacionPropCrono (JavaCall).
 */
@Service
public class ConsultarInformacionAsignacionPropCronoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarInformacionAsignacionPropCronoService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_TRANSACCIONES;

    public ConsultarInformacionAsignacionPropCronoService(
            NamedParameterJdbcTemplate jdbc,
            RequisitosBasicosUtil requisitosUtil,
            ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.configUtil = configUtil;
        this.SQL_SELECT_ORDENES = loadSql("cl/bcs/ordenes/sql/ConsultarInformacionAsignacionPropCrono_SelectOrdenes.sql");
        this.SQL_SELECT_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/ConsultarInformacionAsignacionPropCrono_SelectTransacciones.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarInformacionAsignacionPropCronoService.execute()");
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

            String horaHasta = params.has("p_HoraHasta") && !params.get("p_HoraHasta").isJsonNull()
                ? params.get("p_HoraHasta").getAsString() : null;

            if (horaHasta == null) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"codigo\":\"UI_FALTA_HORA_HASTA\",\"tipo\":\"ERROR\"}]"));
                return out;
            }

            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
            long offsetSec = configUtil.getParametroLong(idEmpresa, "PLA.SegundosZonaHoraria");
            String fechaHasta = toUtcTimestamp(fechaSistema + " " + horaHasta, offsetSec);

            MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("v_FechaOperacionHasta", fechaHasta)
                .addValue("p_IdLibroOrdenes", params.has("p_IdLibroOrdenes") ? params.get("p_IdLibroOrdenes").getAsLong() : null)
                .addValue("p_IdClasificacionTipoInstrumento", params.has("p_IdClasificacionTipoInstrumento") ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : null)
                .addValue("p_IdTipoInstrumento", params.has("p_IdTipoInstrumento") ? params.get("p_IdTipoInstrumento").getAsString() : null)
                .addValue("p_IdAccionOrden", params.has("p_IdAccionOrden") ? params.get("p_IdAccionOrden").getAsString() : null)
                .addValue("p_IdCondicionDeLiquidacion", params.has("p_IdCondicionDeLiquidacion") ? params.get("p_IdCondicionDeLiquidacion").getAsString() : null)
                .addValue("p_IdBolsa", params.has("p_IdBolsa") ? params.get("p_IdBolsa").getAsString() : null)
                .addValue("p_IdEmpresa", idEmpresa)
                .addValue("p_IdTipoCanalIngreso", params.has("p_CanalIngreso") ? params.get("p_CanalIngreso").getAsString() : null)
                .addValue("p_IdInstrumento", params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsLong() : null);

            List<Map<String, Object>> ordenes = jdbc.queryForList(SQL_SELECT_ORDENES, sqlParams);
            List<Map<String, Object>> transacciones = jdbc.queryForList(SQL_SELECT_TRANSACCIONES, sqlParams);

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_ORDENES", JsonParser.parseString(GSON.toJson(ordenes)));
            out.add("P_OUT_TRANSACCIONES", JsonParser.parseString(GSON.toJson(transacciones)));
            return out;
        } catch (Exception e) {
            log.error("ConsultarInformacionAsignacionPropCronoService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String toUtcTimestamp(String localDatetime, long offsetSeconds) {
        try {
            LocalDateTime local = LocalDateTime.parse(localDatetime.replace(" ", "T"));
            return local.minusSeconds(offsetSeconds).toString().replace("T", " ");
        } catch (Exception e) {
            return localDatetime;
        }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarInformacionAsignacionPropCronoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
