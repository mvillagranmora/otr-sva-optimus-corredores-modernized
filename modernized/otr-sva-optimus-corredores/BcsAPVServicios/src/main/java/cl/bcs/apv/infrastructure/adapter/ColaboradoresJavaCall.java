package cl.bcs.apv.infrastructure.adapter;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.domain.port.out.Colaboradores;

/**
 * Production {@link Colaboradores}: invokes a legacy JavaCall class that is NOT an APV use case by its simple name, with the JSON contract of its
 * Entrada/Salida.
 * <ul>
 *   <li>{@code StoredProcedure} runs a HANA procedure through {@link ProcedimientoAlmacenado};</li>
 *   <li>every other name goes to the platform JavaCall router ({@code POST {services.javacall.url}/internal/java-call}, body with the legacy
 *       class and its params), which routes it to the service that owns it or falls back to TomEE.</li>
 * </ul>
 * The other APV classes are NEVER routed by name: they are typed use cases injected into the services. The router URL and the shared token come
 * from the environment ({@code JAVACALL_URL}, {@code INTERNAL_TOKEN}); there is no default URL and no credential in the code; the calls have connect and
 * read timeouts so that a hung collaborator does not hold a thread inside an open database transaction.
 */
@Component
public class ColaboradoresJavaCall implements Colaboradores {

    static final String CABECERA = "X-Internal-Token";
    static final String PROCEDIMIENTO = "StoredProcedure";

    /**
     * EVERY simple name the three families of this module pass to {@link #invocar} -> legacy fully qualified JavaCall class (the key of the router).
     * A name that is not here is a wiring bug and raises {@link IllegalArgumentException}: it is never sent bare. {@code ColaboradoresJavaCallEnrutamientoTest}
     * scans every invocar call literal of {@code src/main} and checks it is here.
     */
    private static final Map<String, String> CLASES = Map.ofEntries(
            // Plataforma
            Map.entry("AgregarLog", "cl.bcs.plataforma.pla.AgregarLog"),
            Map.entry("RevisarRequisitosBasicos", "cl.bcs.plataforma.pla.RevisarRequisitosBasicos"),
            Map.entry("ConsultarEmpresa", "cl.bcs.plataforma.pla.utils.ConsultarEmpresa"),
            Map.entry("CrearEventoAutorizacionMasivo", "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo"),
            Map.entry("ConsultarInstrumentoPrecioBolsa", "cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa"),
            Map.entry("EntregarPreciosTasas", "cl.bcs.plataforma.tas.EntregarPreciosTasas"),
            Map.entry("CalcularParidad", "cl.bcs.plataforma.mer.CalcularParidad"),
            // Tesoreria
            Map.entry("CrearIngresoEgreso", "cl.bcs.tesoreria.CrearIngresoEgreso"),
            Map.entry("LiquidarComprobante", "cl.bcs.tesoreria.LiquidarComprobante"),
            Map.entry("CrearComprobante", "cl.bcs.tesoreria.CrearComprobante"),
            Map.entry("AnulacionMandatoPAC", "cl.bcs.tesoreria.AnulacionMandatoPAC"),
            Map.entry("RevocacionMandatoPAC", "cl.bcs.tesoreria.RevocacionMandatoPAC"),
            Map.entry("IngresarMandatoPAC", "cl.bcs.tesoreria.IngresarMandatoPAC"),
            // Cuenta de inversion, fondos mutuos, custodia, ordenes, portafolio
            Map.entry("CrearCargoAbono", "cl.bcs.cuentaInversion.CrearCargoAbono"),
            Map.entry("ConsultarSaldoCuentaInversion", "cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion"),
            Map.entry("CrearSolicitudAporteRescateNormal", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal"),
            Map.entry("CalcularMontoNumeroCuota", "cl.bcs.fondomutuo.CalcularMontoNumeroCuota"),
            Map.entry("ConsultarCustodiaPortafolioFondoMutuo", "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo"),
            Map.entry("ConsultarCustodiaPortafolio", "cl.bcs.custodia.ConsultarCustodiaPortafolio"),
            Map.entry("CalcularMontoYCobrosOrden", "cl.bcs.ordenes.CalcularMontoYCobrosOrden"),
            Map.entry("CrearOrdenes", "cl.bcs.ordenes.CrearOrdenes"),
            Map.entry("ModificarPortafolio", "cl.bcs.portafolio.ModificarPortafolio"),
            // Socio de negocio
            Map.entry("CrearCliente", "cl.bcs.socionegocio.cli.CrearCliente"),
            Map.entry("CrearPersona", "cl.bcs.socionegocio.per.CrearPersona"),
            Map.entry("CrearTelefono", "cl.bcs.socionegocio.per.CrearTelefono"),
            Map.entry("CrearDireccionElectronica", "cl.bcs.socionegocio.per.CrearDireccionElectronica"),
            Map.entry("CrearPersonaCuentaBancaria", "cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria"));

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

    /** The simple names this adapter resolves ({@code StoredProcedure} included). */
    public static Set<String> nombresConocidos() {
        Set<String> nombres = new TreeSet<>(CLASES.keySet());
        nombres.add(PROCEDIMIENTO);
        return nombres;
    }

    private final ProcedimientoAlmacenado procedimientos;
    private final RestClient router;
    private final String tokenInterno;

    public ColaboradoresJavaCall(ProcedimientoAlmacenado procedimientos,
            @Value("${services.javacall.url}") String urlRouter,
            @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
            @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
            @Value("${services.internal-token:}") String tokenInterno) {
        this.procedimientos = procedimientos;
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
        String clase = clase(servicio);
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
