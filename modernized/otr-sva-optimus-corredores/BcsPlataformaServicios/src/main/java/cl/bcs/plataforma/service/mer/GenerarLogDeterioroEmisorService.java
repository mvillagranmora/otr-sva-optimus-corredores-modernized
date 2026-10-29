package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenerarLogDeterioroEmisorService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public GenerarLogDeterioroEmisorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.GenerarLogDeterioroEmisor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (ConfiguracionSistema, AgregarLog) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        result.add("P_OUT_MENSAJES", new JsonArray());
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
