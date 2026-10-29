package cl.bcs.plataforma.service.com;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migrated from legacy cl.bcs.plataforma.com.ConsultarMatrizOperacional (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, ConfiguracionSistema
 * Complete execute() after pla subpackage migration.
 */
@Service
public class ConsultarMatrizOperacionalService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMatrizOperacionalService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_CONSULTAR_PLANTILLAS_MODELO_COMERCIAL = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarPlantillasModeloComercial.sql";
    private static final String PATH_SQL_CONSULTAR_PLANTILLAS_PORTAFOLIO = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarPlantillasPortafolio.sql";
    private static final String PATH_SQL_CONSULTAR_DATOS = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarDatos.sql";
    private static final String PATH_SQL_CONSULTAR_MERCADOS = "cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarMercados.sql";

    public ConsultarMatrizOperacionalService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.ConsultarMatrizOperacional"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, ConfiguracionSistema) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long p_IdUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String p_IdModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String p_IdTipoEntidad = params.has("p_IdTipoEntidad") ? params.get("p_IdTipoEntidad").getAsString() : null;
            String p_IdEntidad = params.has("p_IdEntidad") ? params.get("p_IdEntidad").getAsString() : null;
            String p_IdPlantillaCobros = params.has("p_IdPlantillaCobros") ? params.get("p_IdPlantillaCobros").getAsString() : null;
            String p_IdPlantillaProductos = params.has("p_IdPlantillaProductos") ? params.get("p_IdPlantillaProductos").getAsString() : null;

        } catch (Exception e) {
            log.error("Error en {}: {}", fqn(), e.getMessage(), e);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("P_OUT_MENSAJE", e.getMessage());
        }
        log.info("FIN::{}", fqn());
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("SQL not found: " + path);
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
