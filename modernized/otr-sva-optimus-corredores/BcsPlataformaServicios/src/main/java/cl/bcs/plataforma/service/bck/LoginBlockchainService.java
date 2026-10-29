package cl.bcs.plataforma.service.bck;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Migrated from cl.bcs.plataforma.bck.LoginBlockchain (JavaCall).
 * Reads blockchain endpoint configuration from HANA, then performs an HTTP
 * login to obtain a token. Credentials and server URL are stored in the DB
 * (never hardcoded).
 */
@Service
public class LoginBlockchainService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public LoginBlockchainService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.bck.LoginBlockchain";
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String codigoParametro = params.has("p_CodigoParametro")
                    ? params.get("p_CodigoParametro").getAsString() : null;

            // Query blockchain configuration from HANA
            String sql = loadSql("cl/bcs/plataforma/bck/sql/LoginBlockchain_SelectConfiguracion.sql");
            List<Map<String, Object>> configuracion = jdbc.queryForList(sql,
                    new MapSqlParameterSource()
                            .addValue("p_IdEmpresa", 1)
                            .addValue("p_CodigoParametro", codigoParametro));

            if (configuracion.size() < 3) {
                result.addProperty("p_Out_ContadorError", 1);
                result.addProperty("p_Out_MensajeError", "Configuracion blockchain incompleta para " + codigoParametro);
                return result;
            }

            // Extract config values (URL, user, password) from DB result
            // Credentials are read from DB, never hardcoded
            String serverUrl = configuracion.get(0).get("Valor") != null
                    ? configuracion.get(0).get("Valor").toString() : "";
            String user = configuracion.get(1).get("Valor") != null
                    ? configuracion.get(1).get("Valor").toString() : "";
            String pass = configuracion.get(2).get("Valor") != null
                    ? configuracion.get(2).get("Valor").toString() : "";

            // Perform HTTP login to blockchain endpoint
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse token from response (simple extraction)
            String responseStr = response.toString();
            String token = "";
            if (responseStr.contains("token")) {
                token = responseStr; // full response; caller extracts token field
            }

            result.addProperty("p_Out_UserLogin", user);
            result.addProperty("p_Out_Token", token);
            result.addProperty("p_Out_BrokerageID", "");
            result.addProperty("p_Out_ContadorError", 0);
            result.addProperty("p_Out_MensajeError", "");
        } catch (Exception e) {
            result.addProperty("p_Out_ContadorError", 1);
            result.addProperty("p_Out_MensajeError", e.getMessage());
        }
        return result;
    }
}
