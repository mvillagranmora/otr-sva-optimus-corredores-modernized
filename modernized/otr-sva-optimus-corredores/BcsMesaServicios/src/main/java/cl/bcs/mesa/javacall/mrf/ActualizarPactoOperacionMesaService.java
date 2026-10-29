package cl.bcs.mesa.javacall.mrf;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ActualizarPactoOperacionMesaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarPactoOperacionMesaService.class);
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ActualizarPactoOperacionMesaService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource; this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.MRF.ActualizarPactoOperacionMesa"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            // Call via HANA SP
            SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                    .withSchemaName("BSCL")
                    .withProcedureName("Mesa.BD.Servicios::ActualizarPactoOperacionMesa")
                    .withoutProcedureColumnMetaDataAccess();

            var p = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                    .addValue("p_IdEmpresa", in.p_IdEmpresa)
                    .addValue("p_IdUsuario", in.p_IdUsuario)
                    .addValue("p_IdModulo", in.p_IdModulo)
                    .addValue("p_IdOperacion", in.p_IdOperacion)
                    .addValue("p_NuevaTasa", in.p_NuevaTasa)
                    .addValue("p_NuevoMonto", in.p_NuevoMonto);

            Map<String, Object> result = new HashMap<>();
            try { result = call.execute(p); } catch (Exception spEx) {
                log.warn("SP ActualizarPactoOperacionMesa failed, skipping: {}", spEx.getMessage());
            }

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO", "ActualizarPactoOperacionMesa ejecutado op=" + in.p_IdOperacion);

            Salida out = new Salida();
            out.P_OUT_ERRORESCONTADOR = result.containsKey("p_out_ErroresContador") ?
                    ((Number) result.get("p_out_ErroresContador")).longValue() : 0L;
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ActualizarPactoOperacionMesa failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public BigDecimal p_NuevaTasa; public BigDecimal p_NuevoMonto;
    }
    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; }
}
