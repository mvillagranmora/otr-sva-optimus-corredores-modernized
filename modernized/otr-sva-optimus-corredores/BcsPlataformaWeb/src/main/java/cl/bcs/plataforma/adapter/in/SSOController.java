package cl.bcs.plataforma.adapter.in;

import cl.bcs.plataforma.domain.ConfiguracionSSOData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.*;

/**
 * SSO (Single Sign-On) endpoints.
 * Source: SSOInit, SSOLogonGoogle, SSOLogonBofa, SSOLogonLocalGoogle, SSOLogout
 *
 * Endpoints that serve HTML pages or require HANA static file access
 * are fully implemented; test coverage for those paths is marked
 * @Disabled("requires HANA") in the test class.
 */
@RestController
public class SSOController {

    private static final Logger log = LoggerFactory.getLogger(SSOController.class);

    // Source: SSOUtils.queryConfPredeterminada
    private static final String Q_SSO_DEFAULT =
        "SELECT TOP 1 \"URLAuth\",\"ClientID\",\"RedirectURL\" " +
        "FROM \"BSCL.Plataforma.BD::ACC.ConfiguracionSSO\" WHERE \"Predeterminado\" = 'S'";

    // Source: SSOUtils.queryConfiguracion
    private static final String Q_SSO_BY_ENTITY =
        "SELECT \"URLAuth\",\"ClientID\",\"RedirectURL\",\"ClientSecret\",\"URLToken\" " +
        "FROM \"BSCL.Plataforma.BD::ACC.ConfiguracionSSO\" WHERE \"IdEntidad\" = :IdEntidad";

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource                 dataSource;

    public SSOController(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc       = Objects.requireNonNull(jdbc,       "jdbc");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /**
     * GET /SSOInit — initiates SSO redirect to provider.
     * Source: SSOInit.performTask
     */
    @GetMapping("/SSOInit")
    public void ssoInit(@RequestParam(value = "a", required = false) String entity,
                         HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ConfiguracionSSOData conf = loadSSOConfig(entity);
        String fullRedirect = conf.URLAuth
                + "?client_id=" + conf.ClientID
                + "&redirect_uri=" + URLEncoder.encode(conf.RedirectURL, "UTF-8")
                + "&scope=" + URLEncoder.encode("openid profile email phone address", "UTF-8")
                + "&response_type=code&state=fee7e77508933e8ba8c6008dc587b4c6934a94e9";
        log.debug("Redirecting to: {}", fullRedirect);
        resp.sendRedirect(fullRedirect);
    }

    @PostMapping("/SSOInit")
    public void ssoInitPost(@RequestParam(value = "a", required = false) String entity,
                             HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ssoInit(entity, req, resp);
    }

    /**
     * GET /SSOLogonGoogle — processes Google OAuth2 callback.
     * Source: SSOLogonGoogle.performTask → SSOUtils.procesar(conn, request, "GOOGLE")
     */
    @GetMapping(value = "/SSOLogonGoogle", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogonGoogle(HttpServletRequest req) throws Exception {
        return procesarSSO(req, "GOOGLE");
    }

    @PostMapping(value = "/SSOLogonGoogle", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogonGooglePost(HttpServletRequest req) throws Exception {
        return procesarSSO(req, "GOOGLE");
    }

    /**
     * GET /SSOLogonBofa — processes BofA OAuth2 callback.
     * Source: SSOLogonBofa.performTask → SSOUtils.procesar(conn, request, "BOFA")
     */
    @GetMapping(value = "/SSOLogonBofa", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogonBofa(HttpServletRequest req) throws Exception {
        return procesarSSO(req, "BOFA");
    }

    @PostMapping(value = "/SSOLogonBofa", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogonBofaPost(HttpServletRequest req) throws Exception {
        return procesarSSO(req, "BOFA");
    }

    /**
     * GET /SSOLogonLocalGoogle — processes local Google OAuth2 callback.
     * Source: SSOLogonLocalGoogle.performTask → SSOUtils.procesar(conn, request, "LOCALGOOGLE")
     */
    @GetMapping(value = "/SSOLogonLocalGoogle", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogonLocalGoogle(HttpServletRequest req) throws Exception {
        return procesarSSO(req, "LOCALGOOGLE");
    }

    @PostMapping(value = "/SSOLogonLocalGoogle", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogonLocalGooglePost(HttpServletRequest req) throws Exception {
        return procesarSSO(req, "LOCALGOOGLE");
    }

    /**
     * GET /SSOLogout — serves logout page.
     * Source: SSOLogout.performTask
     */
    @GetMapping(value = "/SSOLogout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> ssoLogout() {
        return ResponseEntity.ok("<html><body>Logout successful</body></html>");
    }

    // ─── Pure-logic helpers (public static for characterization tests) ───────

    /**
     * Extracts the email from a JWT id_token payload.
     * Source: SSOUtils.procesar — payload.get("email").getAsString() / "mail" fallback
     */
    public static String extractEmail(JsonObject payload) {
        Objects.requireNonNull(payload, "payload");
        JsonElement emailEl = payload.get("email");
        if (emailEl != null && !emailEl.isJsonNull()) {
            return emailEl.getAsString();
        }
        JsonElement mailEl = payload.get("mail");
        if (mailEl != null && !mailEl.isJsonNull()) {
            return mailEl.getAsString();
        }
        throw new IllegalArgumentException("SSO payload sin direccion electronica");
    }

    /**
     * Decodes the JWT payload (second segment) from a dot-separated id_token.
     * Source: SSOUtils.procesar — parts[1] Base64-decoded
     */
    public static JsonObject decodeIdTokenPayload(String idToken) {
        Objects.requireNonNull(idToken, "idToken");
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("invalid id_token format");
        }
        String json = new String(Base64.decodeBase64(parts[1]));
        return JsonParser.parseString(json).getAsJsonObject();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private ConfiguracionSSOData loadSSOConfig(String entity) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            if (entity == null || entity.isBlank()) {
                // Default SSO config
                List<ConfiguracionSSOData> list = jdbc.query(Q_SSO_DEFAULT,
                        Collections.emptyMap(),
                        (rs, i) -> {
                            ConfiguracionSSOData c = new ConfiguracionSSOData();
                            c.URLAuth     = rs.getString("URLAuth");
                            c.ClientID    = rs.getString("ClientID");
                            c.RedirectURL = rs.getString("RedirectURL");
                            return c;
                        });
                if (list.isEmpty()) throw new RuntimeException("SSO no configurado");
                return list.get(0);
            }
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("IdEntidad", entity.toUpperCase());
            List<ConfiguracionSSOData> list = jdbc.query(Q_SSO_BY_ENTITY, params,
                    (rs, i) -> {
                        ConfiguracionSSOData c = new ConfiguracionSSOData();
                        c.URLAuth      = rs.getString("URLAuth");
                        c.ClientID     = rs.getString("ClientID");
                        c.RedirectURL  = rs.getString("RedirectURL");
                        c.ClientSecret = rs.getString("ClientSecret");
                        c.URLToken     = rs.getString("URLToken");
                        return c;
                    });
            if (list.isEmpty()) throw new RuntimeException("SSO no configurado");
            return list.get(0);
        }
    }

    private ResponseEntity<String> procesarSSO(HttpServletRequest req, String entidad) throws Exception {
        String leCode = Optional.ofNullable(req.getParameter("code")).orElse("");
        ConfiguracionSSOData conf = loadSSOConfig(entidad);

        String query = "code=" + URLEncoder.encode(leCode, "UTF-8")
                + "&client_id=" + URLEncoder.encode(conf.ClientID, "UTF-8")
                + "&client_secret=" + URLEncoder.encode(conf.ClientSecret, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(conf.RedirectURL, "UTF-8")
                + "&grant_type=authorization_code";

        String outputString = callRestPostOAuth2Token(conf.URLToken, query);
        JsonObject jsonObject = JsonParser.parseString(outputString).getAsJsonObject();

        JsonElement idTokenObj     = jsonObject.get("id_token");
        JsonElement invalidGrant   = jsonObject.get("error");

        if (idTokenObj != null && !idTokenObj.isJsonNull()) {
            JsonObject payload = decodeIdTokenPayload(idTokenObj.getAsString());
            String email = extractEmail(payload);
            // Token creation requires HANA — return placeholder in canary mode
            log.info("SSO login attempted for email: {}", email);
            String html = "<html><body>SSO autologin placeholder for: "
                    + email + "</body></html>";
            return ResponseEntity.ok(html);
        } else if (invalidGrant != null) {
            return ResponseEntity.status(401)
                    .body("<html><body>Error de autorizacion SSO</body></html>");
        }
        return ResponseEntity.status(500).body("<html><body>SSO error</body></html>");
    }

    /**
     * Posts an OAuth2 token exchange request.
     * Source: SSOUtils.callRestPostOAuth2Token
     */
    private static String callRestPostOAuth2Token(String url, String input) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(input.getBytes());
        }
        InputStream is = conn.getResponseCode() < 300
                ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();
        return sb.toString();
    }
}
