package cl.bcs.plataforma.service.aut;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CrearEventoAutorizacionService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearEventoAutorizacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.CrearEventoAutorizacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // Delegates to SAP HANA stored procedure:
            //   CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (...)
            // Full implementation requires StoredProcedure support via JDBC CallableStatement
            // with HANA-specific output table handling.

            result.add("P_OUT_IDEVENTO", new JsonArray());
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
            result.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_ERRORESDETALLE", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray errs = new JsonArray(); JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR"); err.addProperty("Descripcion", e.getMessage()); errs.add(err); result.add("P_OUT_ERRORESDETALLE", errs);
        }
        return result;
    }
}
