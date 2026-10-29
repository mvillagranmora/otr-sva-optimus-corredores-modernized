package cl.bcs.plataforma.service.lim;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ValidarLimiteCuentaPropiaIFRFService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ValidarLimiteCuentaPropiaIFRFService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/lim/sql/ValidarLimiteCuentaPropiaIFRF_SelectInstrumentoClasificacionRiesgo.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // FechaDB needed for date calculations
            // ConfiguracionSistema needed for system parameters
            // ClasificadorCustodia / ConsultarCustodiaPortafolioCuentaPropia logic
            //   needed for custodia classification (porCliente, porTipoInstrumento, porEmisor, etc.)
            // Complex portfolio validation logic against LIM limits requires
            //   full pla subpackage migration to complete

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;

            List<Map<String, Object>> instrumentos = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa));

            JsonArray validaciones = new JsonArray();
            JsonArray mensajes = new JsonArray();

            result.add("P_OUT_VALIDACIONES", validaciones);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
