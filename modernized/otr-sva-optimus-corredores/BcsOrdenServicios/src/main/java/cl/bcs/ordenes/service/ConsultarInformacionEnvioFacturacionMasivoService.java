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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarInformacionEnvioFacturacionMasivo (JavaCall).
 */
@Service
public class ConsultarInformacionEnvioFacturacionMasivoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarInformacionEnvioFacturacionMasivoService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT_ASIGNACIONES;
    private final String SQL_SELECT_ASIGNACIONES_BY_ID;

    public ConsultarInformacionEnvioFacturacionMasivoService(
            NamedParameterJdbcTemplate jdbc,
            RequisitosBasicosUtil requisitosUtil,
            ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.configUtil = configUtil;
        this.SQL_SELECT_ASIGNACIONES = loadSql("cl/bcs/ordenes/sql/ConsultarInformacionEnvioFacturacionMasivo_SelectAsignaciones.sql");
        this.SQL_SELECT_ASIGNACIONES_BY_ID = loadSql("cl/bcs/ordenes/sql/ConsultarInformacionEnvioFacturacionMasivo_SelectAsignacionesPorIdAsignacionMasiva.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarInformacionEnvioFacturacionMasivoService.execute()");
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
            String horaHasta = params.has("p_HoraHasta") && !params.get("p_HoraHasta").isJsonNull()
                ? params.get("p_HoraHasta").getAsString() : null;

            if (idAsignacionMasiva == null && horaHasta == null) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"codigo\":\"UI_FALTA_HORA_HASTA\",\"tipo\":\"ERROR\"}]"));
                return out;
            }

            List<Map<String, Object>> asignaciones;

            if (idAsignacionMasiva != null) {
                MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                    .addValue("p_IdAsignacionMasiva", idAsignacionMasiva);
                asignaciones = jdbc.queryForList(SQL_SELECT_ASIGNACIONES_BY_ID, sqlParams);
            } else {
                String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
                long offsetSec = configUtil.getParametroLong(idEmpresa, "PLA.SegundosZonaHoraria");
                String fechaDesde = toUtcTimestamp(fechaSistema + " 00:00:00", offsetSec);
                String fechaHasta = toUtcTimestamp(fechaSistema + " " + horaHasta, offsetSec);

                MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                    .addValue("v_FechaAsignacionDesde", fechaDesde)
                    .addValue("v_FechaAsignacionHasta", fechaHasta)
                    .addValue("p_IdLibroOrdenes", params.has("p_IdLibroOrdenes") ? params.get("p_IdLibroOrdenes").getAsLong() : null)
                    .addValue("p_IdClasificacionTipoInstrumento", params.has("p_IdClasificacionTipoInstrumento") ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : null)
                    .addValue("p_IdTipoInstrumento", params.has("p_IdTipoInstrumento") ? params.get("p_IdTipoInstrumento").getAsString() : null)
                    .addValue("p_IdAccionOrden", params.has("p_IdAccionOrden") ? params.get("p_IdAccionOrden").getAsString() : null)
                    .addValue("p_IdCondicionDeLiquidacion", params.has("p_IdCondicionDeLiquidacion") ? params.get("p_IdCondicionDeLiquidacion").getAsString() : null)
                    .addValue("p_IdBolsa", params.has("p_IdBolsa") ? params.get("p_IdBolsa").getAsString() : null)
                    .addValue("p_IdInstrumento", params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsLong() : null)
                    .addValue("p_IdAsignacion", params.has("p_IdAsignacion") ? params.get("p_IdAsignacion").getAsLong() : null)
                    .addValue("p_IdOrden", params.has("p_IdOrden") ? params.get("p_IdOrden").getAsLong() : null);

                asignaciones = jdbc.queryForList(SQL_SELECT_ASIGNACIONES, sqlParams);
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_ASIGNACIONES", JsonParser.parseString(GSON.toJson(asignaciones)));
            return out;
        } catch (Exception e) {
            log.error("ConsultarInformacionEnvioFacturacionMasivoService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String toUtcTimestamp(String localDatetime, long offsetSeconds) {
        try {
            LocalDateTime local = LocalDateTime.parse(localDatetime.replace(" ", "T"));
            LocalDateTime utc = local.minusSeconds(offsetSeconds);
            return utc.toString().replace("T", " ");
        } catch (Exception e) {
            return localDatetime;
        }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarInformacionEnvioFacturacionMasivoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
