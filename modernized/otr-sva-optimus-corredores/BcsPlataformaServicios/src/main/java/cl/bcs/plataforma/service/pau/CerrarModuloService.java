package cl.bcs.plataforma.service.pau;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migrated from legacy cl.bcs.plataforma.pau.CerrarModulo (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, RevisarRequisitosBasicos, ConfiguracionSistema, AgregarLog
 * Complete execute() after pla subpackage migration.
 */
@Service
public class CerrarModuloService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CerrarModuloService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;
    private static final String PATH_SQL_CALL_SP_DEPURACION = "cl/bcs/plataforma/pau/sql/CerrarModulo_CallServicioDepuracion.sql";
    private static final String PATH_SQL_SELECT_ESTADO_MODULO_SISTEMA = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectSistemaEntregado.sql";
    private static final String PATH_SQL_SELECT_MODULO_ESTADO = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectModuloEstado.sql";
    private static final String PATH_SQL_SELECT_MESAS_ABIERTAS = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectMesasAbiertas.sql";
    private static final String PATH_SQL_SELECT_EJECUCION_PROCESOS = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectProcesosEjecutados.sql";
    private static final String PATH_SQL_SELECT_MODULOS_DEPENDIENTES = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectModulosDependientes.sql";
    private static final String PATH_SQL_UPDATE_CIERRE_MODULO = "cl/bcs/plataforma/pau/sql/CerrarModulo_UpdateCierreModulo.sql";
    private static final String PATH_SQL_SELECT_NOMBRE_MODULO = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectNombreModulo.sql";
    private static final String PATH_SQL_CALL_SP_ACTUALIZAR_TABLA_TBBASIPA = "cl/bcs/plataforma/pau/sql/CerrarModulo_CallServicioActualizarTBBASIPA.sql";
    private static final String PATH_SQL_INSERT_MENSAJE_WEBSOCKET = "cl/bcs/plataforma/pau/sql/CerrarModulo_InsertMensajeWebsocket.sql";
    private static final String PATH_SQL_SELECT_REQUISITO_OPERATIVO = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectRequisitoOperativo.sql";
    private static final String PATH_SQL_SELECT_EVENTO_AUTORIZACION = "cl/bcs/plataforma/pau/sql/CerrarModulo_SelectEventoAutorizacion.sql";

    public CerrarModuloService(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pau.CerrarModulo"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, RevisarRequisitosBasicos, ConfiguracionSistema, AgregarLog) — complete after pla subpackage migration
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
