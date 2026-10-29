package cl.bcs.tesoreria.infrastructure.adapter;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Production {@link Colaboradores}: invokes a legacy JavaCall class by its simple name with the JSON contract of its Entrada/Salida.
 * <ul>
 *   <li>a class that this module migrated runs in process through {@link JavaCallDispatcher} (no network hop);</li>
 *   <li>any other class goes to the platform JavaCall router ({@code POST {services.javacall.url}/internal/java-call},
 *       body {@code {"class": ..., "params": ...}}), which routes it to the service that owns it or falls back to TomEE;</li>
 *   <li>{@code StoredProcedure} runs a HANA procedure through {@link ProcedimientoAlmacenado};</li>
 *   <li>{@code Cloper} (the external CLOPER web service) has no migrated client yet and raises {@link UnsupportedOperationException},
 *       which the internal endpoint maps to 503 so the router falls back to TomEE.</li>
 * </ul>
 * The router URL and the shared token come from the environment ({@code JAVACALL_URL}, {@code INTERNAL_TOKEN}); there are no credentials in the code.
 *
 * <p>Collaborators whose legacy entry point is not JSON (a scalar parameter or a list result: {@code ObtenerPortafoliosEjecutivo},
 * {@code ObtenerPortafoliosEjecutivo.restringeCartera}) exchange their value as {@code {"valor": <scalar or array>}}.
 */
@Component
public class ColaboradoresJavaCall implements Colaboradores {

    static final String CABECERA = "X-Internal-Token";
    static final String PROCEDIMIENTO = "StoredProcedure";
    static final String CLOPER = "Cloper";

    /**
     * EVERY simple name the services pass to {@link #invocar} -> legacy fully qualified JavaCall class (the key of the router and of the dispatcher).
     * A class of this module ({@code cl.bcs.tesoreria.*}) runs in process when {@link JavaCallDispatcher#maneja} says so; any other goes to the router,
     * which maps it by package prefix. A name that is not here is a wiring bug and raises {@link IllegalArgumentException}: it is never sent bare.
     */
    private static final Map<String, String> CLASES = Map.ofEntries(
            // this module (in process)
            Map.entry("CrearIngresoEgreso", "cl.bcs.tesoreria.CrearIngresoEgreso"),
            Map.entry("LiquidarComprobante", "cl.bcs.tesoreria.LiquidarComprobante"),
            Map.entry("CrearComprobante", "cl.bcs.tesoreria.CrearComprobante"),
            Map.entry("ActualizarSaldosCuentaBanco", "cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco"),
            Map.entry("AnularNominasTransferencias", "cl.bcs.tesoreria.nor.AnularNominasTransferencias"),
            // Plataforma
            Map.entry("RevisarRequisitosBasicos", "cl.bcs.plataforma.pla.RevisarRequisitosBasicos"),
            Map.entry("AgregarLog", "cl.bcs.plataforma.pla.AgregarLog"),
            Map.entry("ConsultarFlujoCaja", "cl.bcs.plataforma.pla.ConsultarFlujoCaja"),
            Map.entry("SaldoFlujoCaja", "cl.bcs.plataforma.pla.SaldoFlujoCaja"),
            Map.entry("AgregarPrograma", "cl.bcs.plataforma.bat.AgregarPrograma"),
            Map.entry("CalcularParidad", "cl.bcs.plataforma.mer.CalcularParidad"),
            Map.entry("ConsultarInstrumentoPrecioBolsa", "cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa"),
            Map.entry("CrearEventoAutorizacionMasivo", "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo"),
            Map.entry("ModificarEstadoEventoAutorizacionMasivo", "cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo"),
            // other business modules
            Map.entry("CrearCargoAbono", "cl.bcs.cuentaInversion.CrearCargoAbono"),
            Map.entry("AnularCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono"),
            Map.entry("AnulacionContratoCSC", "cl.bcs.fondomutuo.AnulacionContratoCSC"),
            Map.entry("RevocacionContratoCSC", "cl.bcs.fondomutuo.RevocacionContratoCSC"),
            Map.entry("CalcularMontoNumeroCuota", "cl.bcs.fondomutuo.CalcularMontoNumeroCuota"),
            Map.entry("CrearSolicitudAporteRescateNormal", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal"),
            Map.entry("CambiarEstadoContratosSAAPV", "cl.bcs.APV.CambiarEstadoContratosSAAPV"),
            Map.entry("CalcularMontoYCobrosOrden", "cl.bcs.ordenes.CalcularMontoYCobrosOrden"),
            Map.entry("CrearOrdenes", "cl.bcs.ordenes.CrearOrdenes"),
            Map.entry("ObtenerPortafoliosEjecutivo", "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo"),
            Map.entry("ObtenerPortafoliosEjecutivo.restringeCartera", "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.restringeCartera"));

    /**
     * The legacy fully qualified class of a collaborator simple name.
     *
     * @throws IllegalArgumentException when the name is not registered (never sent to the router with a bare name)
     */
    public static String clase(String servicio) {
        String clase = servicio == null ? null : CLASES.get(servicio);
        if (clase == null) {
            throw new IllegalArgumentException("collaborator not registered in ColaboradoresJavaCall: " + servicio);
        }
        return clase;
    }

    /** The simple names this adapter resolves (the two special ones, StoredProcedure and Cloper, included). */
    public static java.util.Set<String> nombresConocidos() {
        java.util.Set<String> nombres = new java.util.TreeSet<>(CLASES.keySet());
        nombres.add(PROCEDIMIENTO);
        nombres.add(CLOPER);
        return nombres;
    }

    private final JavaCallDispatcher local;
    private final ProcedimientoAlmacenado procedimientos;
    private final RestClient router;
    private final String tokenInterno;

    public ColaboradoresJavaCall(JavaCallDispatcher local, ProcedimientoAlmacenado procedimientos,
            @Value("${services.javacall.url}") String urlRouter,
            @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
            @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
            @Value("${services.internal-token:}") String tokenInterno) {
        this.local = local;
        this.procedimientos = procedimientos;
        // bounded waits: a hung collaborator must not hold a thread inside an open database transaction
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(tiempoConexion).build());
        fabrica.setReadTimeout(tiempoLectura);
        this.router = RestClient.builder().baseUrl(urlRouter).requestFactory(fabrica).build();
        this.tokenInterno = tokenInterno == null ? "" : tokenInterno;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        if (PROCEDIMIENTO.equals(servicio)) {
            return procedimientos.ejecutar(entrada);
        }
        if (CLOPER.equals(servicio)) {
            throw new UnsupportedOperationException("CLOPER web service client is not migrated yet");
        }
        String clase = clase(servicio);
        if (local.maneja(clase)) {
            try {
                return local.despachar(clase, entrada);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("collaborator " + servicio + " failed", e);
            }
        }
        JsonObject peticion = new JsonObject();
        peticion.addProperty("class", clase);
        peticion.add("params", entrada);
        RestClient.RequestBodySpec solicitud = router.post().uri("/internal/java-call").contentType(MediaType.APPLICATION_JSON);
        if (!tokenInterno.isBlank()) {
            solicitud = solicitud.header(CABECERA, tokenInterno);
        }
        String respuesta = solicitud.body(peticion.toString()).retrieve().body(String.class);
        JsonElement salida = respuesta == null ? null : JsonParser.parseString(respuesta);
        return salida == null || salida.isJsonNull() ? null : salida.getAsJsonObject();
    }
}
