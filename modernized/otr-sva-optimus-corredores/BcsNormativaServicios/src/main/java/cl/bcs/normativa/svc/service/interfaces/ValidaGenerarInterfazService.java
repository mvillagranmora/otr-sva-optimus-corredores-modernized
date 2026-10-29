package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ValidaGenerarInterfazService {

    private static final Logger log = LoggerFactory.getLogger(ValidaGenerarInterfazService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_CIERRE;
    private final String SQL_INSERT_LOG;

    public ValidaGenerarInterfazService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        // Note: stubs had SQL_QUERY/SQL_1 swapped — corrected here
        this.SQL_SELECT_CIERRE = loadSql("cl/bcs/normativa/interfaces/sql/GenerarInterfaz_SelectCierreModulos.sql");
        this.SQL_INSERT_LOG = loadSql("cl/bcs/normativa/interfaces/sql/InsertLogEjecucionInterfaz.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ValidaGenerarInterfazService.execute called");
        JsonObject out = new JsonObject();

        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
        String fechaEjecucion = params.has("p_FechaEjecucion") ? params.get("p_FechaEjecucion").getAsString() : null;

        try {
            List<String> modulos = Arrays.asList("ORD", "FAC");
            MapSqlParameterSource p = new MapSqlParameterSource();
            p.addValue("IdEmpresa", idEmpresa);
            p.addValue("IdModulo", modulos);
            p.addValue("FechaEjecucion", fechaEjecucion);

            List<Map<String, Object>> moduloList = jdbc.queryForList(SQL_SELECT_CIERRE, p);

            int contCierreAbierto = 0;
            StringBuilder modulosAbiertos = new StringBuilder();
            for (Map<String, Object> mod : moduloList) {
                String estado = mod.getOrDefault("IdEstado", "").toString();
                if ("ABT".equalsIgnoreCase(estado) || "UI_DESCUADRADO".equalsIgnoreCase(estado)) {
                    contCierreAbierto++;
                    modulosAbiertos.append(mod.getOrDefault("IdModulo", "")).append(" ");
                }
            }

            if (contCierreAbierto > 0) {
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.addProperty("P_OUT_MODULOS_ABIERTOS", modulosAbiertos.toString().trim());
                out.addProperty("P_OUT_MENSAJE", "Módulos abiertos detectados: " + modulosAbiertos);
            } else {
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.addProperty("P_OUT_MENSAJE", "Todos los módulos cerrados correctamente");
            }
        } catch (Exception e) {
            log.error("Error en ValidaGenerarInterfaz", e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            out.addProperty("P_OUT_MENSAJE", "Error: " + e.getMessage());
        }
        return out;
    }

    private static String loadSql(String path) {
        try (var is = ValidaGenerarInterfazService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
