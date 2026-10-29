package cl.bcs.portalconfirmacion.service;

import cl.bcs.portalconfirmacion.port.in.ValidateNotificationUseCase;
import cl.bcs.portalconfirmacion.port.out.NotificationPort;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Objects;

/**
 * Business logic for notification portal confirmation.
 *
 * Implements the same routing logic as the legacy NotificationsValidate servlet:
 * <ol>
 *   <li>Token validation via HANA SP (port).</li>
 *   <li>URL retrieval per empresa from PLA.Configuracion (port).</li>
 *   <li>Route by rut/accion presence to HTML page, template display, or accept/reject.</li>
 * </ol>
 *
 * Bug fixes vs. legacy:
 * <ul>
 *   <li>NPE guard: template != null || !template.equals("-1") replaced with
 *       template != null &amp;&amp; !"-1".equals(template).</li>
 *   <li>mapAccionCode is now a testable pure-static method.</li>
 *   <li>fetchHtml uses URI.create().toURL() (URL(String) deprecated since Java 20).</li>
 * </ul>
 */
@Service
public class NotificationService implements ValidateNotificationUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationPort port;

    public NotificationService(NotificationPort port) {
        this.port = port;
    }

    // ----------------------------------------------------------
    // ValidateNotificationUseCase
    // ----------------------------------------------------------

    @Override
    public ValidateResponse processValidation(String token, String rut, String accion, String observacion) {
        Objects.requireNonNull(token, "token is required");

        long idEmpresa = port.validateToken(token);
        String serverUrl = port.getUrlHana(idEmpresa);

        if (serverUrl == null) {
            throw new IllegalArgumentException("Invalid token: no company URL found");
        }

        if (rut == null) {
            return handleNoRut(serverUrl, token);
        }

        return handleWithRut(serverUrl, token, rut, accion, observacion);
    }

    // ----------------------------------------------------------
    // Pure-static helpers (testable without Spring)
    // ----------------------------------------------------------

    /**
     * Maps the raw accion request parameter to the domain action code.
     *
     * @param accion "1" (accept) or "2" (reject).
     * @return "ACP", "RZD", or null for invalid input.
     */
    public static String mapAccionCode(String accion) {
        if (accion == null) return null;
        return switch (accion) {
            case "1" -> "ACP";
            case "2" -> "RZD";
            default -> null;
        };
    }

    // ----------------------------------------------------------
    // Response value object
    // ----------------------------------------------------------

    /**
     * Encapsulates the HTTP response body and its content-type.
     */
    public record ValidateResponse(String body, String contentType) {}

    // ----------------------------------------------------------
    // Private routing helpers
    // ----------------------------------------------------------

    private ValidateResponse handleNoRut(String serverUrl, String token) {
        String html = fetchHtml(serverUrl, "/BSCL/Plataforma/UI/EVT/Confirmar/ConfirmarNotificacionIngreso.html");
        if (html == null) html = "";
        String tokenInput = "<input type=\"hidden\" id=\"p_token\" value=\"" + token + "\" >";
        html = html.replace("${token}", tokenInput);
        return new ValidateResponse(html, MediaType.TEXT_HTML_VALUE);
    }

    private ValidateResponse handleWithRut(
            String serverUrl, String token, String rut, String accion, String observacion) {

        String template = port.validateTokenAndId(token, rut);

        // Legacy bug fix: was "!= null || !equals(-1)" — always true; correct intent is &&
        if (template == null || "-1".equals(template)) {
            String html = safeHtml(fetchHtml(serverUrl, "-1"));
            return new ValidateResponse(html, MediaType.TEXT_HTML_VALUE);
        }

        if (accion == null && observacion == null) {
            return buildTemplateDisplay(token, rut, template);
        }

        return handleAcceptOrReject(serverUrl, token, observacion, accion);
    }

    private ValidateResponse buildTemplateDisplay(String token, String rut, String template) {
        String tokenInput = "<input type=\"hidden\" ng-model=\"token_conf\" id=\"p_token\" value=\"" + token + "\" >";
        String rutInput   = "<input type=\"hidden\" ng-model=\"rut_conf\"   id=\"p_rut\"   value=\"" + rut + "\" >";
        String body = StringEscapeUtils.unescapeJava(template)
                .replace("&quot;", "\"")
                .replace("<div class=\"container\">", "")
                .replace("<a", "<p")
                .replace("/a>", "/p>")
                + " ||1||0||" + rutInput + "||" + tokenInput;
        return new ValidateResponse(body, MediaType.TEXT_HTML_VALUE);
    }

    private ValidateResponse handleAcceptOrReject(
            String serverUrl, String token, String observacion, String accion) {

        String accionCode = mapAccionCode(accion);
        if (accionCode == null) {
            throw new IllegalArgumentException("Invalid accion: " + accion + " (expected 1 or 2)");
        }

        int result = port.acceptOrReject(token, observacion, accionCode);
        if (result != -1) {
            return new ValidateResponse("1", MediaType.TEXT_PLAIN_VALUE);
        }
        String html = safeHtml(fetchHtml(serverUrl, "-1"));
        return new ValidateResponse(html, MediaType.TEXT_HTML_VALUE);
    }

    /**
     * Fetches HTML from an external BCS platform URL.
     * Replaces the {@code ::URL::} placeholder with the server base URL.
     */
    private String fetchHtml(String serverUrl, String path) {
        try {
            var url = URI.create(serverUrl + path).toURL();
            try (var reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                var sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line.replaceAll("::URL::", serverUrl)).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            LOG.error("Error fetching HTML from {}{}", serverUrl, path, e);
            return null;
        }
    }

    private String safeHtml(String html) {
        return html != null ? html : "";
    }
}
