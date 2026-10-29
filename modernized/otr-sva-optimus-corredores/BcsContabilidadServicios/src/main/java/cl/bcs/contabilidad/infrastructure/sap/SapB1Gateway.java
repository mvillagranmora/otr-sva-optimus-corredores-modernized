package cl.bcs.contabilidad.infrastructure.sap;

import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.sap.BusinessPartner;
import cl.bcs.contabilidad.domain.model.sap.JournalVoucher;
import cl.bcs.contabilidad.domain.model.sap.SapException;
import cl.bcs.contabilidad.domain.model.sap.SapRespuesta;
import cl.bcs.contabilidad.domain.model.sap.SapSesion;
import cl.bcs.contabilidad.domain.port.out.SapGateway;
import cl.bcs.contabilidad.domain.service.AsientoSapMapper;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * SAP Business One Service Layer client, legacy {@code jobs/CallerWSJournalVoucher} (CONT-B-60..65).
 *
 * <p>Kept from legacy: endpoints, JSON bodies, accepted status per endpoint, up to {@code maximoIntentos} attempts
 * (login waits {@code esperaReintentoLogin} between attempts, the other calls retry at once), and the exhaustion
 * texts. Deliberate deviations: every login cookie (B1SESSION and routing cookies) is sent as {@code name=value}
 * (legacy sent only the last Set-Cookie); TLS uses the JVM trust store and hostname verification (legacy installed a
 * trust-all manager); the password is never printed (legacy printed the login JSON); the card code is
 * percent-encoded in the URL; POST bodies declare {@code application/json}. Transport failures raise
 * {@link UncheckedIOException} without retry, as legacy propagated IOExceptions.
 */
public class SapB1Gateway implements SapGateway {

    private static final Logger log = LoggerFactory.getLogger(SapB1Gateway.class);

    private static final String BASE = "/b1s/v1";
    private static final Set<Integer> ACEPTADOS_CONSULTA_SOCIO = Set.of(200, 400, 403, 404);
    private static final Set<Integer> ACEPTADOS_CREAR_SOCIO = Set.of(200, 201, 400, 403);
    private static final Set<Integer> ACEPTADOS_ASIENTO = Set.of(200, 204, 400);
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(30);
    private static final Duration TIMEOUT_PETICION = Duration.ofMinutes(5);

    private final SapB1Config config;
    private final HttpClient http;

    public SapB1Gateway(SapB1Config config) {
        this.config = Objects.requireNonNull(config, "config");
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT_CONEXION)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /** Login body (legacy pojo/LoginSAP). Never logged. */
    private record Login(@SerializedName("UserName") String userName, @SerializedName("Password") String password,
                         @SerializedName("CompanyDB") String companyDb) {
        @Override
        public String toString() {
            return "Login[userName=" + userName + ", password=<redacted>, companyDb=" + companyDb + "]";
        }
    }

    @Override
    public String postJournalEntries(List<ResultadoCentralizacion> entries) {
        throw new UnsupportedOperationException(
                "Direct posting of centralization entries is not a legacy flow; SAP posting goes through EnvioContabilidad "
                        + "staging and InterfazContable");
    }

    @Override
    public SapSesion login() {
        List<String> faltantes = config.faltantes();
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException("SAP B1 Service Layer not configured, missing: " + faltantes);
        }
        String cuerpo = AsientoSapMapper.toSapJson(new Login(config.usuario(), config.clave(), config.companyDb()));
        int status = 0;
        for (int intento = 1; intento <= config.maximoIntentos(); intento++) {
            HttpResponse<String> r = enviar(post("/Login", cuerpo).build());
            status = r.statusCode();
            if (status == 200) {
                List<String> cookies = r.headers().allValues("Set-Cookie").stream()
                        .map(c -> c.split(";", 2)[0].strip()).filter(c -> !c.isEmpty()).toList();
                SapSesion sesion = new SapSesion(cookies);
                if (!sesion.tieneSesionB1()) log.warn("SAP B1 login answered 200 without a {} cookie", SapSesion.COOKIE_SESION);
                return sesion;
            }
            log.warn("SAP B1 login failed with HTTP {} (attempt {}/{})", status, intento, config.maximoIntentos());
            if (intento < config.maximoIntentos()) esperar(config.esperaReintentoLogin());
        }
        throw new SapException(new SapRespuesta(status, "Error de conexión"));
    }

    @Override
    public SapRespuesta consultarSocio(SapSesion sesion, String cardCode) {
        String ruta = "/BusinessPartners('" + codificar(cardCode) + "')";
        return conReintentos(() -> request(ruta).GET(), sesion, ACEPTADOS_CONSULTA_SOCIO,
                "Error al solicitar informacion de socio, maximo de intentos superado");
    }

    @Override
    public SapRespuesta crearSocio(SapSesion sesion, BusinessPartner socio) {
        String cuerpo = AsientoSapMapper.toSapJson(socio);
        return conReintentos(() -> post("/BusinessPartners", cuerpo), sesion, ACEPTADOS_CREAR_SOCIO,
                "Error al crear socio, maximo de intentos superado");
    }

    @Override
    public SapRespuesta enviarAsiento(SapSesion sesion, JournalVoucher voucher) {
        String cuerpo = AsientoSapMapper.toSapJson(voucher);
        return conReintentos(() -> post("/JournalVouchersService_Add", cuerpo), sesion, ACEPTADOS_ASIENTO,
                "Error al enviar asiento, maximo de intentos superado");
    }

    private SapRespuesta conReintentos(Supplier<HttpRequest.Builder> peticion, SapSesion sesion, Set<Integer> aceptados,
                                       String textoAgotado) {
        int status = 0;
        for (int intento = 1; intento <= config.maximoIntentos(); intento++) {
            HttpRequest.Builder b = peticion.get();
            if (!sesion.cookies().isEmpty()) b.header("Cookie", sesion.cabeceraCookie());
            HttpRequest req = b.build();
            HttpResponse<String> r = enviar(req);
            status = r.statusCode();
            if (aceptados.contains(status)) {
                return new SapRespuesta(status, r.body() == null ? "" : r.body());
            }
            log.warn("SAP B1 {} {} answered HTTP {} (attempt {}/{})", req.method(), req.uri().getPath(), status, intento,
                    config.maximoIntentos());
        }
        throw new SapException(new SapRespuesta(status, textoAgotado));
    }

    private HttpRequest.Builder request(String ruta) {
        return HttpRequest.newBuilder(URI.create(config.serviceLayerUrl() + BASE + ruta)).timeout(TIMEOUT_PETICION);
    }

    private HttpRequest.Builder post(String ruta, String cuerpoJson) {
        return request(ruta).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson, StandardCharsets.UTF_8));
    }

    private HttpResponse<String> enviar(HttpRequest req) {
        try {
            return http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("SAP B1 Service Layer unreachable: " + req.method() + " " + req.uri().getPath(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling SAP B1 Service Layer", e);
        }
    }

    private static void esperar(Duration espera) {
        if (espera.isZero()) return;
        try {
            Thread.sleep(espera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry the SAP B1 login", e);
        }
    }

    /** RFC 3986 percent-encoding of everything but unreserved characters. */
    static String codificar(String valor) {
        StringBuilder sb = new StringBuilder();
        for (byte b : String.valueOf(valor).getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || "-._~".indexOf(c) >= 0) {
                sb.append(c);
            } else {
                sb.append('%').append(String.format("%02X", b & 0xFF));
            }
        }
        return sb.toString();
    }
}
