package cl.bcs.plataforma.service.val;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.val.Valorizar (JavaCall).
 * Massive valuation engine supporting PQ (price x quantity) and WS (external
 * valorizador BCS / Peru) valuation paths for CL and PE localizations.
 *
 * Full logic depends on:
 *   - pla.* utilities (ConfiguracionSistema, ConsultarEmpresa, ObtenerPlazo)
 *   - mer.CalcularParidad
 *   - ValorizarWsBcsRest / ValorizadorBonos / ValorizadorPagares
 *   - CuponesWs
 */
@Service
public class ValorizarService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ValorizarService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.val.Valorizar";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idPais    = params.has("p_IdPais")    ? params.get("p_IdPais").getAsLong()    : null;

            // Validate mandatory inputs
            if (idEmpresa == null) {
                result.addProperty("P_OUT_ERRORESCONTADOR", 1);
                result.addProperty("error", "no se ha ingresado la empresa.");
                return result;
            }

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // Full valuation logic depends on ConfiguracionSistema, CalcularParidad,
            // ValorizarWsBcsRest/ValorizadorBonos/ValorizadorPagares, ObtenerPlazo, CuponesWs.
            // PQ valuation (price x quantity arithmetic) is self-contained and implemented
            // in the legacy logica() method — reproduce here once pla migration is done.

            result.add("P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ", new JsonObject());
            result.add("P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS", new JsonObject());
            result.add("P_OUT_MENSAJES", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.addProperty("P_OUT_EXECUTIONTIME", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
