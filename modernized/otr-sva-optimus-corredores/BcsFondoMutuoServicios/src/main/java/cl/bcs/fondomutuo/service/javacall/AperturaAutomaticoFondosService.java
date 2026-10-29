package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.AperturaAutomaticoFondos.
 *
 * Legacy: calls HANA stored procedure BSCL.FondoMutuo.BD.Servicios::AperturaAutomaticoFondos
 * with p_IdEmpresa and p_IdUsuario.
 *
 * Spring: delegates to the same HANA SP via SimpleJdbcCall.
 */
@Service
public class AperturaAutomaticoFondosService {

    private static final Logger log = LoggerFactory.getLogger(AperturaAutomaticoFondosService.class);

    private final DataSource dataSource;

    public AperturaAutomaticoFondosService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;

        log.info("AperturaAutomaticoFondos: idEmpresa={} idUsuario={}", idEmpresa, idUsuario);

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("BSCL")
            .withProcedureName("AperturaAutomaticoFondos");

        MapSqlParameterSource inParams = new MapSqlParameterSource()
            .addValue("p_IdEmpresa", idEmpresa)
            .addValue("p_IdUsuario", idUsuario);

        Map<String, Object> result = jdbcCall.execute(inParams);

        JsonObject out = new JsonObject();
        Object errCount = result.get("P_OUT_ERRORESCONTADOR");
        Object errMsg   = result.get("P_OUT_MENSAJE");
        out.addProperty("P_OUT_ERRORESCONTADOR", errCount != null ? errCount.toString() : "0");
        out.addProperty("P_OUT_MENSAJE", errMsg != null ? errMsg.toString() : "");
        return out;
    }
}
