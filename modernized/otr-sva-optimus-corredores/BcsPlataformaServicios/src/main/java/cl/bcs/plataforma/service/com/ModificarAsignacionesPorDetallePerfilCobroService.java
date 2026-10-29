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
 * Migrated from legacy cl.bcs.plataforma.com.ModificarAsignacionesPorDetallePerfilCobro (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils
 * Complete execute() after pla subpackage migration.
 */
@Service
public class ModificarAsignacionesPorDetallePerfilCobroService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarAsignacionesPorDetallePerfilCobroService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_SELECT_DETALLE_PERFIL_COBROS = "cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Select.sql";
    private static final String PATH_SQL_UPDATE_COBRO_ASIGNACION = "cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Update_cobroAsignacion.sql";
    private static final String PATH_SQL_INSERT_COBRO_ASIGNACION = "cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Insert_cobroAsignacion.sql";
    private static final String PATH_SQL_SELCT_COBRO_ASIGNACION = "cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Select_CobroAsignacion.sql";
    private static final String PATH_SQL_SELCT_EXISTE = "cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Select_existe.sql";
    private static final String PATH_SQL_SELCT_PORTAFOLIO = "cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Portafolio.sql";
    private static final String PATH_SQL_SELCT_COBRO_DEFAULT = "cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Cobro_Default.sql";
    private static final String PATH_SQL_DELETE_ASIGNACION_COBRO = "cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Delete_AsignacionCobros.sql";

    public ModificarAsignacionesPorDetallePerfilCobroService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.ModificarAsignacionesPorDetallePerfilCobro"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
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
