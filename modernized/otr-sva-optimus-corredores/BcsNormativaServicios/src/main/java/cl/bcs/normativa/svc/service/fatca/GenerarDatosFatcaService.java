package cl.bcs.normativa.svc.service.fatca;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Map;

@Service
public class GenerarDatosFatcaService {

    private static final Logger log = LoggerFactory.getLogger(GenerarDatosFatcaService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;

    private static final String SP_NAME = "BSCL.Normativa.BD.Servicios.FATCA::GenerarDatosFatca";

    public GenerarDatosFatcaService(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarDatosFatcaService.execute called");
        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;

        JsonObject out = new JsonObject();
        try {
            // TODO: StoredProcedure "BSCL.Normativa.BD.Servicios.FATCA::GenerarDatosFatca"
            // Legacy called StoredProcedure.call(con, { p_IdEmpresa, p_SimularOperacion: "N" })
            // Replace with SimpleJdbcCall when HANA SP catalog and schema are confirmed in this environment
            SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("BSCL")
                .withProcedureName(SP_NAME);
            MapSqlParameterSource in = new MapSqlParameterSource();
            in.addValue("p_IdEmpresa", idEmpresa);
            in.addValue("p_SimularOperacion", "N");
            Map<String, Object> result = call.execute(in);
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.addProperty("P_OUT_MENSAJE", "El proceso GenerarDatosFatca se ha ejecutado correctamente");
        } catch (Exception e) {
            log.error("Error en GenerarDatosFatca", e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            out.addProperty("P_OUT_MENSAJE", "Error al procesar GenerarDatosFatca: " + e.getMessage());
        }
        return out;
    }
}
