package cl.bcs.plataforma.service;

import cl.bcs.plataforma.internal.ApvRestEstrategia;
import cl.bcs.plataforma.internal.ClienteInterno;
import cl.bcs.plataforma.internal.Destino;
import cl.bcs.plataforma.internal.EstrategiaDueno;
import cl.bcs.plataforma.internal.JavaCallDirectaEstrategia;
import cl.bcs.plataforma.internal.RegistroServicios;
import cl.bcs.plataforma.internal.Respuesta;
import cl.bcs.plataforma.internal.SolicitudJavaCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Router of {@code POST /internal/java-call} (step 5b). Same rules as {@link JavaCallRouterService} for {@code /call}:
 * opt-in allow-list ({@code plataforma.javacall.clases-habilitadas}, default empty = everything to TomEE), fallback to TomEE only
 * on HTTP 503 or a refused connection, any other error propagated (it may come after a commit, ARQ-H6), no idempotency header.
 * Differences, by design: the caller is a trusted job, so there is NO session and NO identity override of the params.
 * The owner strategy is chosen by {@code plataforma.javacall.destinos}.
 */
@Service
public class InternalJavaCallRouter {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallRouter.class);

    private final JavaCallRouterService base;
    private final ClienteInterno http;
    private final List<Destino> destinos;
    private final String tomeeUrl;
    private final RegistroServicios registro;

    public InternalJavaCallRouter(JavaCallRouterService base,
            @Qualifier("internalJavaCallClient") ClienteInterno http,
            RegistroServicios registro,
            @Value("${plataforma.javacall.destinos:}") String destinos,
            @Value("${TOMEE_URL:http://tomee-legacy:8080}") String tomeeUrl) {
        this.base = base;
        this.http = http;
        this.registro = registro;
        this.destinos = Destino.parsear(destinos);
        this.tomeeUrl = tomeeUrl;
    }

    public Respuesta enrutar(SolicitudJavaCall s, String sessionToken) {
        String clase = JavaCallRouterService.extractFqn(s.clase());
        if (ApvRestEstrategia.soloEnProceso(clase)) {
            throw new SolicitudJavaCall.Invalida(422, "operation is in-process only and not routable over HTTP");
        }
        Destino d = base.claseHabilitada(clase) ? Destino.buscar(destinos, clase) : null;
        if (d == null) {
            log.debug("internal java-call class='{}': not enabled or no destination, TomEE", clase);
            return tomee(s, clase, sessionToken);
        }
        try {
            Respuesta r = estrategia(d).invocar(http, d.url(), s);
            if (!r.noDisponible()) return r;
            log.warn("internal java-call class='{}': owner answered 503, TomEE", clase);
        } catch (EstrategiaDueno.NoRuteableAun e) {
            log.warn("internal java-call class='{}': not routable yet ({}), TomEE", clase, e.getMessage());
        } catch (ResourceAccessException e) {
            if (!JavaCallRouterService.permiteFallbackATomee(e)) throw e;
            log.warn("internal java-call class='{}': owner unreachable, TomEE", clase);
        }
        return tomee(s, clase, sessionToken);
    }

    private static EstrategiaDueno estrategia(Destino d) {
        return d.estrategia() == Destino.Estrategia.APV_REST ? new ApvRestEstrategia() : new JavaCallDirectaEstrategia();
    }

    /** TomEE serves JavaCall by {@code /call?t={IdServicio}} (same URL as {@code /call}); params untouched, no internal token. */
    private Respuesta tomee(SolicitudJavaCall s, String clase, String sessionToken) {
        Long id = s.idServicio() != null ? s.idServicio() : registro.idDe(clase).orElse(null);
        if (id == null) {
            throw new SolicitudJavaCall.Invalida(422, "TomEE route unknown: no IdServicio for the class "
                    + "(send idServicio or configure plataforma.javacall.registro-servicios)");
        }
        String uri = tomeeUrl + "/call?t=" + id + (s.skip() > 0 ? "&skip=" + s.skip() : "") + (s.take() > 0 ? "&take=" + s.take() : "");
        return http.post(uri, s.params().toString(), false, sessionToken);
    }
}
