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
 * Migrated from legacy cl.bcs.plataforma.pau.AgregarControlPauta (JavaCall).
 */
@Service
public class AgregarControlPautaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AgregarControlPautaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;
    private static final String PATH_SQL_AgregarControlPautaSP = "cl/bcs/plataforma/pau/sql/AgregarControlPautaSP.sql";
    private static final String PATH_SQL_EntradaAgregarControlPauta = "cl/bcs/plataforma/pau/sql/EntradaAgregarControlPauta.sql";

    public AgregarControlPautaService(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pau.AgregarControlPauta"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::{}", fqn());
        JsonObject result = new JsonObject();
        try {
            // 1. Parse input params
            Long p_IdTrabajo = params.has("p_IdTrabajo") ? params.get("p_IdTrabajo").getAsLong() : null;
            Object p_Registros = params.has("p_Registros") ? params.get("p_Registros").getAsString() : null;

            // 2. Execute JDBC operations
            // TODO: stored proc call — cl/bcs/plataforma/pau/sql/AgregarControlPautaSP.sql
            // SimpleJdbcCall call0 = new SimpleJdbcCall(dataSource)
            //     .withSchemaName("BSCL")
            //     .withProcedureName("<proc>");

            String sql1 = loadSql(PATH_SQL_EntradaAgregarControlPauta);
            MapSqlParameterSource p1 = new MapSqlParameterSource();
            p1.addValue("p_IdTrabajo", p_IdTrabajo);
            p1.addValue("p_Registros", p_Registros);
            jdbc.update(sql1, p1);

            // 3. Build response
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
