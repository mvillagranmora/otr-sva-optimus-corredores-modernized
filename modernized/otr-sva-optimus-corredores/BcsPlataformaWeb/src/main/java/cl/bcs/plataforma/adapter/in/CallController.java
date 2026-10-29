package cl.bcs.plataforma.adapter.in;

import cl.bcs.plataforma.domain.SessionContext;
import cl.bcs.plataforma.service.JavaCallRouterService;
import cl.bcs.plataforma.service.ProcedureDispatcherService;
import cl.bcs.plataforma.service.QueryDispatcherService;
import cl.bcs.plataforma.service.SessionValidatorService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Dispatcher for the /call endpoint — ports legacy CallServlet.
 *
 * Protocol:
 *   GET or POST /call?t={IdServicio}&r={resultKey}&ch={canal}
 *   Header: X-SESSION-TOKEN: {token}
 *   Body:   JSON object with service-specific IN parameters
 *
 * Dispatch types (PLA.Servicio.TipoServicio):
 *   P — HANA Stored Procedure → ProcedureDispatcherService
 *   Q — OData view query       → QueryDispatcherService
 *   J — JavaCall plugin proxy  → JavaCallRouterService
 *
 * Source: CallServlet.java + ProcesaLlamadaServletCall.java
 */
@RestController
@RequestMapping("/call")
public class CallController {

    private static final Logger log = LoggerFactory.getLogger(CallController.class);

    private final SessionValidatorService  sessionValidator;
    private final ProcedureDispatcherService procedureDispatcher;
    private final QueryDispatcherService   queryDispatcher;
    private final JavaCallRouterService    javaCallRouter;

    public CallController(
            SessionValidatorService sessionValidator,
            ProcedureDispatcherService procedureDispatcher,
            QueryDispatcherService queryDispatcher,
            JavaCallRouterService javaCallRouter) {
        this.sessionValidator    = sessionValidator;
        this.procedureDispatcher = procedureDispatcher;
        this.queryDispatcher     = queryDispatcher;
        this.javaCallRouter      = javaCallRouter;
    }

    // STUB — PLA.Servicio registry not yet exported (Phase 5 deployment gate).
    // Re-enable by replacing the throw with `return dispatch(request, rawBody)` once the registry
    // is exported to the database and HANA is reachable from this service.
    @PostMapping
    public ResponseEntity<String> callPost(HttpServletRequest request,
                                           @RequestBody(required = false) String rawBody) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "PLA.Servicio registry not yet exported — Phase 5 gate");
    }

    @GetMapping
    public ResponseEntity<String> callGet(HttpServletRequest request) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "PLA.Servicio registry not yet exported — Phase 5 gate");
    }

    // ─── Core dispatcher ──────────────────────────────────────────────────────

    private ResponseEntity<String> dispatch(HttpServletRequest req, String rawBody)
            throws Exception {
        // 1. Parse request params
        String tParam = req.getParameter("t");
        if (!StringUtils.hasText(tParam)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parámetro 't' (IdServicio) requerido");
        }
        long   idServicio = Long.parseLong(tParam);
        String resultKey  = req.getParameter("r");
        String canal      = coalesce(req.getParameter("ch"), "WEB");
        String token      = req.getHeader("X-SESSION-TOKEN");

        // OData / pagination params
        String select      = req.getParameter("$select");
        String filter      = req.getParameter("$filter");
        String orderby     = req.getParameter("$orderby");
        long   top         = parseLong(req.getParameter("$top"), 0L);
        long   skip        = parseLong(req.getParameter("$skip"), 0L);
        int    skipInt     = req.getParameter("skip")  != null ? (int) parseLong(req.getParameter("skip"), 0L)  : (int) skip;
        int    take        = req.getParameter("take")  != null ? (int) parseLong(req.getParameter("take"), 0L)  : (int) top;
        boolean inlineCount = "allpages".equalsIgnoreCase(req.getParameter("$inlinecount"));

        // 2. Validate session + resolve service metadata
        SessionContext ctx = sessionValidator.validate(token, idServicio, canal);
        log.debug("call t={} tipo={} ruta={} usuario={}", idServicio, ctx.tipoServicio(),
            ctx.rutaServicio(), ctx.idUsuario());

        // 3. Parse body once
        JsonObject params = parseBody(rawBody);

        // 4. Dispatch by service type
        String responseBody = switch (ctx.tipoServicio()) {
            case "P" -> dispatchProcedure(ctx, params, resultKey);
            case "Q" -> dispatchQuery(ctx, select, filter, orderby, top, skip, inlineCount);
            case "J" -> javaCallRouter.route(idServicio, ctx.rutaServicio(), params, ctx.idEmpresa(), ctx.idUsuario(), token, skipInt, take);
            default  -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Tipo de servicio '" + ctx.tipoServicio() + "' no soportado");
        };

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseBody);
    }

    // ─── Type-P: Stored Procedure ─────────────────────────────────────────────

    private String dispatchProcedure(SessionContext ctx, JsonObject params,
                                     String resultKey) throws Exception {
        JsonObject result = procedureDispatcher.dispatch(
            ctx.rutaServicio(), params, ctx.idEmpresa(), ctx.idUsuario());

        // Check error counter (source: CallServlet.p_out_ErroresContador check)
        String errCount = result.has("p_out_ErroresContador")
            ? result.get("p_out_ErroresContador").getAsString() : "0";
        if (!"0".equals(errCount) && !errCount.isBlank()) {
            // Return error envelope matching legacy format
            JsonObject err = new JsonObject();
            err.addProperty("ErroresContador", errCount);
            if (result.has("p_out_Mensaje")) {
                err.addProperty("Mensaje", result.get("p_out_Mensaje").getAsString());
            }
            return err.toString();
        }

        // If resultKey requested, extract that subtree
        if (StringUtils.hasText(resultKey) && result.has(resultKey)) {
            return result.get(resultKey).toString();
        }
        return result.toString();
    }

    // ─── Type-Q: OData View Query ─────────────────────────────────────────────

    private String dispatchQuery(SessionContext ctx,
                                 String select, String filter, String orderby,
                                 long top, long skip, boolean inlineCount) throws Exception {
        QueryDispatcherService.QueryResult qr = queryDispatcher.dispatch(
            ctx.rutaServicio(), select, filter, orderby, top, skip, inlineCount);

        if (inlineCount) {
            JsonObject envelope = new JsonObject();
            envelope.addProperty("odata.count", qr.totalCount());
            envelope.add("value", qr.rows());
            return envelope.toString();
        }
        return qr.rows().toString();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static JsonObject parseBody(String raw) {
        if (!StringUtils.hasText(raw)) return new JsonObject();
        try {
            return JsonParser.parseString(raw).getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private static long parseLong(String s, long def) {
        if (!StringUtils.hasText(s)) return def;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return def; }
    }

    private static String coalesce(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
