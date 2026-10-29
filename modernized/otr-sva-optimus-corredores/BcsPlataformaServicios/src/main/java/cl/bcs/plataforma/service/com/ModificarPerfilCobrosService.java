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
 * Migrated from legacy cl.bcs.plataforma.com.ModificarPerfilCobros (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, ConfiguracionSistema, AgregarLog
 * Complete execute() after pla subpackage migration.
 */
@Service
public class ModificarPerfilCobrosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarPerfilCobrosService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_COUNT_EXISTE = "cl/bcs/plataforma/com/sql/ModificarPerfilCobros_Count_Existe.sql";
    private static final String PATH_SQL_SELECT_VALORES_ANTIGUOS = "cl/bcs/plataforma/com/sql/ModificarPerfilCobros_Select_ValoresAntiguos.sql";
    private static final String PATH_SQL_SELECT_TIPOS_DATO = "cl/bcs/plataforma/com/sql/ModificarPerfilCobros_Select_TiposDato.sql";
    private static final String PATH_SQL_SELECT_GLOSA = "cl/bcs/plataforma/com/sql/ModificarPerfilCobros_Select_Glosa.sql";
    private static final String PATH_SQL_SELECT_REQUIERE_AUTORIZACION = "cl/bcs/plataforma/com/sql/ModificarPerfilCobros_Select_RequiereAutorizacion.sql";

    public ModificarPerfilCobrosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.ModificarPerfilCobros"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, ConfiguracionSistema, AgregarLog) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long p_IdUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String p_IdModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String p_UsaReglasNegocio = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : null;
            String p_IdPerfilCobros = params.has("p_IdPerfilCobros") ? params.get("p_IdPerfilCobros").getAsString() : null;
            Object p_RegistroCambio = params.has("p_RegistroCambio") ? params.get("p_RegistroCambio").getAsString() : null;

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
