package cl.bcs.plataforma.service.tas;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.tas.ws.CargarArchivoTasas (JavaCall).
 * WS variant that receives structured tasa data with IdMoneda/Periodo fields
 * and enqueues a batch program (AgregarPrograma) for processing.
 * Depends on pla.* ConfiguracionSistema and bat.AgregarPrograma.
 */
@Service
public class CargarArchivoTasasWsService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargarArchivoTasasWsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.tas.ws.CargarArchivoTasas";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String fuente = params.has("p_Fuente") ? params.get("p_Fuente").getAsString() : null;
            JsonArray tasas = params.has("p_Tasas") ? params.get("p_Tasas").getAsJsonArray() : new JsonArray();

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // ConfiguracionSistema for PLA.FechaSistema lookup;
            // bat.AgregarPrograma to enqueue the batch program for tasa processing.

            result.addProperty("P_OUT_ID_PROGRAMA", 0);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
