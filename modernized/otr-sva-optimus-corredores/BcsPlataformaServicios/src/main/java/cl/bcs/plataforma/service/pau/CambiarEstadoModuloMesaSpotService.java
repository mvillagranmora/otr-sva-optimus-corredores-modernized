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
 * Migrated from legacy cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot (JavaCall).
 * Dependencies on pla.* utilities: PlataformaUtils, FechaDB, ConfiguracionSistema
 * Complete execute() after pla subpackage migration.
 */
@Service
public class CambiarEstadoModuloMesaSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CambiarEstadoModuloMesaSpotService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;
    private static final String PATH_SQL_SELECT_CODIGO_USUARIO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_SelectCodigoUsuario.sql";
    private static final String PATH_SQL_DATOS_MESA = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_SelectDatosMesa.sql";
    private static final String PATH_SQL_UPDATE_MODULO_SPOT = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_UpdateEstadoModuloSpot.sql";
    private static final String PATH_SQL_UPDATE_MESA_SPOT = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_UpdateEstadoMesaSpot.sql";
    private static final String PATH_SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_SelectParidadTipoCambio.sql";
    private static final String PATH_SQL_UPDATE_PARIDAD_TIPO_CAMBIO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_UpdateTipoCambio.sql";
    private static final String PATH_SQL_CREATE_TEMPORARY_TABLE_ABRIR_MODULO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_CreateTemporaryTableAbrirModulo.sql";
    private static final String PATH_SQL_CREATE_TEMPORARY_TABLE_DATOS_MESA = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_CreateTemporaryTableDatosMesa.sql";
    private static final String PATH_SQL_CREATE_TEMPORARY_TABLE_DATOS_TIPOCAMBIO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_CreateTemporaryTableDatosTipoCambio.sql";
    private static final String PATH_SQL_INSERT_TEMPORARY_TABLE_ABRIR_MODULO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_InsertTemporaryTableAbrirModulo.sql";
    private static final String PATH_SQL_INSERT_TEMPORARY_TABLE_DATOS_MESA = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_InsertTemporaryTableDatosMesa.sql";
    private static final String PATH_SQL_INSERT_TEMPORARY_TABLE_DATOS_TIPOCAMBIO = "cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_InsertTemporaryTableDatosTipoCambio.sql";

    public CambiarEstadoModuloMesaSpotService(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility (PlataformaUtils, FechaDB, ConfiguracionSistema) — complete after pla subpackage migration
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
