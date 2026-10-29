package cl.bcs.plataforma.service.com.pe;

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
 * Migrated from legacy cl.bcs.plataforma.com.pe.ModificarDefinicionCobro (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, RevisarRequisitosBasicos, Diccionario
 * Complete execute() after pla subpackage migration.
 */
@Service
public class ModificarDefinicionCobroService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarDefinicionCobroService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String PATH_SQL_UPDATE_DEFINICION_COBRO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_UpdateDefinicionCobro.sql";
    private static final String PATH_SQL_INSERT_DEFINICION_RANGO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroRango.sql";
    private static final String PATH_SQL_CONSULTAR_DEFINICION_COBRO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_ConsultarDefinicionCobro.sql";
    private static final String PATH_SQL_DELETE_DEFINICION_COBRO_RANGO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroRango.sql";
    private static final String PATH_SQL_DELETE_DEFINICION_COBRO_EMISOR = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroEmisor.sql";
    private static final String PATH_SQL_DELETE_DEFINICION_COBRO_INSTRUMENTO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroInstrumento.sql";
    private static final String PATH_SQL_DELETE_DEFINICION_COBRO_TIPO_CLIENTE = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroTipoCliente.sql";
    private static final String PATH_SQL_DELETE_DEFINICION_COBRO_PORTAFOLIO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroPortafolio.sql";
    private static final String PATH_SQL_DELETE_DEFINICION_COBRO_CORRESPONSAL = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_DeleteDefinicionCobroCorredoraCorresponsal.sql";
    private static final String PATH_SQL_INSERT_DEFINICION_COBRO_EMISOR = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroEmisor.sql";
    private static final String PATH_SQL_INSERT_DEFINICION_COBRO_INSTRUMENTO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroInstrumento.sql";
    private static final String PATH_SQL_INSERT_DEFINICION_COBRO_TIPO_CLIENTE = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroTipoCliente.sql";
    private static final String PATH_SQL_INSERT_DEFINICION_COBRO_PORTAFOLIO = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroPortafolio.sql";
    private static final String PATH_SQL_INSERT_DEFINICION_COBRO_CORRESPONSAL = "cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_InsertDefinicionCobroCorredoraCorresponsal.sql";

    public ModificarDefinicionCobroService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.com.pe.ModificarDefinicionCobro"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, RevisarRequisitosBasicos, Diccionario) — complete after pla subpackage migration
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            // 1. Parse input params
            Long p_IdEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long p_IdUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String p_IdModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;

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
