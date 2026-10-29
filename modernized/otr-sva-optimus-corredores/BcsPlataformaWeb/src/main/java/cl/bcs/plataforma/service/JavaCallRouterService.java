package cl.bcs.plataforma.service;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.ConnectException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dispatches J-type services — routes JavaCall plugin invocations to the
 * corresponding Spring Boot service via POST /internal/java-call.
 *
 * RutaServicio format (from PLA.Servicio):
 *   "PLUGIN_XXX.jar@cl.bcs.pkg.ClassName"
 *   "PLUGIN_XXX@cl.bcs.pkg.ClassName"
 *   "cl.bcs.pkg.ClassName"  (no plugin prefix)
 *
 * Opt-in: a class reaches a new service only if it is listed in plataforma.javacall.clases-habilitadas
 * (default empty = everything goes to TomEE). Then routing is by Java package prefix. Two fallback levels:
 *   1. Paquete no mapeado → TomEE directo (ej. BcsCustodia, BcsSocioNegocio no migrados).
 *   2. Microservicio no disponible (HTTP 503 o conexión rechazada) → TomEE directo (degradación canary).
 *      Cualquier otro error (500, 4xx, timeout de lectura) se propaga: pudo ocurrir tras un commit (ARQ-H6).
 *
 * The session identity (IdEmpresa/IdUsuario) is imposed on the params before routing (ARQ-H5).
 */
@Service
public class JavaCallRouterService {

    private static final Logger log = LoggerFactory.getLogger(JavaCallRouterService.class);

    private static final Pattern RUTA_PATTERN = Pattern.compile(
        "^(?:PLUGIN_[^@]+@)?(.+)$", Pattern.CASE_INSENSITIVE);

    static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final Map<String, String> packageUrlMap;
    private final String internalToken;
    private final String tomeeUrl;
    private final RestClient restClient;
    /** Opt-in allow-list: exact FQNs and prefixes (stored with the trailing dot, e.g. "cl.bcs.tesoreria."). */
    private final java.util.Set<String> clasesExactas;
    private final java.util.List<String> clasesPrefijo;

    public JavaCallRouterService(
            @Value("${plataforma.javacall.url.bcs-plataforma-servicios:http://bcs-plataforma-servicios:8114}")   String urlPla,
            @Value("${plataforma.javacall.url.bcs-socio-negocio-servicios:http://bcs-socio-negocio-servicios:8098}") String urlSoc,
            @Value("${plataforma.javacall.url.bcs-orden-servicios:http://bcs-orden-servicios:8086}")             String urlOrd,
            @Value("${plataforma.javacall.url.bcs-normativa-servicios:http://bcs-normativa-servicios:8113}")     String urlNor,
            @Value("${plataforma.javacall.url.bcs-mesa-servicios:http://bcs-mesa-servicios:8085}")               String urlMes,
            @Value("${plataforma.javacall.url.bcs-custodia-servicios:http://bcs-custodia-servicios:8097}")       String urlCus,
            @Value("${plataforma.javacall.url.bcs-fondo-mutuo-servicios:http://bcs-fondo-mutuo-servicios:8111}") String urlFmm,
            @Value("${plataforma.javacall.url.bcs-apv-servicios:http://bcs-apv-servicios:8083}")                 String urlApv,
            @Value("${plataforma.javacall.url.bcs-tesoreria-servicios:http://bcs-tesoreria-servicios:8073}")     String urlTes,
            @Value("${plataforma.javacall.url.bcs-facturacion-servicios:http://bcs-facturacion-servicios:8099}") String urlFac,
            @Value("${plataforma.javacall.url.bcs-cuenta-inversion-servicios:http://bcs-cuenta-inversion-servicios:8080}") String urlCta,
            @Value("${plataforma.javacall.url.bcs-contabilidad-servicios:http://bcs-contabilidad-servicios:8081}") String urlCon,
            @Value("${plataforma.javacall.url.bcs-portafolio-servicios:http://bcs-portafolio-servicios:8076}")   String urlPor,
            @Value("${plataforma.javacall.url.bcs-canal-agente-servicios:http://bcs-canal-agente-servicios:8102}") String urlCag,
            @Value("${TOMEE_URL:http://tomee-legacy:8080}")                                                       String tomeeUrl,
            @Value("${plataforma.javacall.internal-token:}")                                                      String internalToken,
            RestClient.Builder restClientBuilder,
            @Value("${plataforma.javacall.clases-habilitadas:}")                                                  String clasesHabilitadas) {

        this.restClient = restClientBuilder.build();
        this.tomeeUrl   = tomeeUrl;
        this.internalToken = internalToken != null ? internalToken : "";
        this.clasesExactas = new java.util.HashSet<>();
        this.clasesPrefijo = new java.util.ArrayList<>();
        if (clasesHabilitadas != null) {
            for (String e : clasesHabilitadas.split(",")) {
                String t = e.trim();
                if (t.isEmpty()) continue;
                if (t.endsWith(".*")) this.clasesPrefijo.add(t.substring(0, t.length() - 1)); // keeps the trailing dot
                else this.clasesExactas.add(t);
            }
        }

        this.packageUrlMap = Map.ofEntries(
            Map.entry("cl.bcs.plataforma",        urlPla),
            Map.entry("cl.bcs.socionegocio",       urlSoc),
            Map.entry("cl.bcs.SocioNegocio",       urlSoc),
            Map.entry("cl.bcs.ordenes",            urlOrd),
            Map.entry("cl.bcs.normativa",          urlNor),
            Map.entry("cl.bcs.mesa",               urlMes),
            Map.entry("cl.bcs.Mesa",               urlMes),
            Map.entry("cl.bcs.custodia",           urlCus),
            Map.entry("cl.bcs.Custodia",           urlCus),
            Map.entry("cl.bcs.fondomutuo",         urlFmm),
            Map.entry("cl.bcs.fondoMutuo",         urlFmm),
            Map.entry("cl.bcs.FondoMutuo",         urlFmm),
            Map.entry("cl.bcs.APV",                urlApv),
            Map.entry("cl.bcs.apv",                urlApv),
            Map.entry("cl.bcs.tesoreria",          urlTes),
            Map.entry("cl.bcs.Tesoreria",          urlTes),
            Map.entry("cl.bcs.facturacion",        urlFac),
            Map.entry("cl.bcs.Facturacion",        urlFac),
            Map.entry("cl.bcs.cuentaInversion",    urlCta),
            Map.entry("cl.bcs.contabilidad",       urlCon),
            Map.entry("cl.bcs.portafolio",         urlPor),
            Map.entry("cl.bcs.Portafolio",         urlPor),
            Map.entry("cl.bcs.canalagente",        urlCag),
            Map.entry("cl.bcs.CanalAgente",        urlCag)
        );
    }

    /**
     * Routes the J-type call to the owning Spring Boot service.
     * Falls back to TomEE if:
     *   - the package prefix is not mapped (Fase D services not yet migrated), or
     *   - the target microservice returns an error (canary degradation).
     */
    public String route(long idServicio, String rutaServicio, JsonObject params,
                        long idEmpresaSesion, long idUsuarioSesion,
                        String token, int skip, int take) {
        String fqn        = extractFqn(rutaServicio);
        boolean habilitada = claseHabilitada(fqn);
        String serviceUrl = habilitada ? resolveUrl(fqn) : null;

        // ARQ-H5: the session, not the caller, decides who the call runs as.
        JsonObject efectivos = params != null ? params : new JsonObject();
        imponerIdentidadDeSesion(efectivos, idEmpresaSesion, idUsuarioSesion);

        // Nivel 1: paquete no mapeado → TomEE directo
        if (!habilitada) {
            log.debug("JavaCall idServicio={} class='{}' — clase no habilitada (plataforma.javacall.clases-habilitadas), TomEE directo",
                idServicio, fqn);
            return callTomee(idServicio, efectivos, token, skip, take);
        }
        if (serviceUrl == null) {
            log.warn("JavaCall idServicio={} class='{}' — paquete sin mapear, proxy a TomEE",
                idServicio, fqn);
            return callTomee(idServicio, efectivos, token, skip, take);
        }

        JsonObject body = new JsonObject();
        body.addProperty("class", fqn);
        if (skip > 0) body.addProperty("skip", skip);
        if (take > 0) body.addProperty("take", take);
        body.add("params", efectivos);

        log.debug("JavaCall idServicio={} class={} → {}", idServicio, fqn, serviceUrl);

        // Nivel 2: microservicio no disponible (503) o conexión rechazada → TomEE fallback.
        // ARQ-H6: any other failure (500, 4xx, read timeout) may come AFTER a commit, so it is
        // propagated and never re-executed in TomEE.
        try {
            RestClient.RequestBodySpec req = restClient.post()
                .uri(serviceUrl + "/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-SESSION-TOKEN", token != null ? token : "");
            if (!internalToken.isBlank()) {
                req = req.header(INTERNAL_TOKEN_HEADER, internalToken);
            }
            return req.body(body.toString())
                .retrieve()
                .body(String.class);
        } catch (RestClientException e) {
            if (!permiteFallbackATomee(e)) {
                throw e;
            }
            log.warn("JavaCall idServicio={} class='{}' — microservicio no disponible ({}), proxy a TomEE: {}",
                idServicio, fqn, serviceUrl, e.getMessage());
            return callTomee(idServicio, efectivos, token, skip, take);
        }
    }

    /**
     * ARQ-H5, legacy {@code CallServlet.java:680-683}: when the request carries one of these keys, its value is
     * replaced by the session's. A key that is absent stays absent.
     */
    static void imponerIdentidadDeSesion(JsonObject params, long idEmpresa, long idUsuario) {
        if (params.get("IdEmpresa") != null)   params.addProperty("IdEmpresa", idEmpresa);
        if (params.get("p_IdEmpresa") != null) params.addProperty("p_IdEmpresa", idEmpresa);
        if (params.get("IdUsuario") != null)   params.addProperty("IdUsuario", idUsuario);
        if (params.get("p_IdUsuario") != null) params.addProperty("p_IdUsuario", idUsuario);
    }

    /**
     * ARQ-H6: the TomEE fallback is allowed only when the service did not run the call: HTTP 503 (the services
     * answer 503 for a class they have not migrated) or a refused connection.
     */
    static boolean permiteFallbackATomee(RestClientException e) {
        if (e instanceof RestClientResponseException r) {
            return r.getStatusCode().value() == 503;
        }
        if (e instanceof ResourceAccessException) {
            for (Throwable c = e.getCause(); c != null; c = c.getCause()) {
                if (c instanceof ConnectException) return true;
            }
        }
        return false;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Proxies the original /call?t={idServicio} request to TomEE legacy.
     * TomEE recibe el mismo protocolo que el frontend — valida sesión internamente.
     */
    private String callTomee(long idServicio, JsonObject params, String token, int skip, int take) {
        String uri = tomeeUrl + "/call?t=" + idServicio
            + (skip > 0 ? "&skip=" + skip : "")
            + (take > 0 ? "&take=" + take : "");

        log.info("JavaCall idServicio={} → TomEE legacy ({})", idServicio, uri);

        return restClient.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-SESSION-TOKEN", token != null ? token : "")
            .body(params != null ? params.toString() : "{}")
            .retrieve()
            .body(String.class);
    }

    /** Opt-in: only a class listed exactly, or under a listed "prefix.*", may reach a new service (case-sensitive). */
    boolean claseHabilitada(String fqn) {
        if (fqn == null || fqn.isEmpty()) return false;
        if (clasesExactas.contains(fqn)) return true;
        for (String p : clasesPrefijo) {
            if (fqn.startsWith(p)) return true;
        }
        return false;
    }

    static String extractFqn(String rutaServicio) {
        if (rutaServicio == null) return "";
        Matcher m = RUTA_PATTERN.matcher(rutaServicio.trim());
        return m.matches() ? m.group(1) : rutaServicio;
    }

    String resolveUrl(String fqn) {
        String pkg = fqn;
        while (pkg.contains(".")) {
            if (packageUrlMap.containsKey(pkg)) return packageUrlMap.get(pkg);
            int lastDot = pkg.lastIndexOf('.');
            pkg = pkg.substring(0, lastDot);
        }
        return null;
    }
}
