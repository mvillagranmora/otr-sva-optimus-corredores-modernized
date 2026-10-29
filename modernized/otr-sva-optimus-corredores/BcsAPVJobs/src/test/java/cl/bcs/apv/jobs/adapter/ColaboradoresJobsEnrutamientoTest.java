package cl.bcs.apv.jobs.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall;

/**
 * Architecture review M-5: every collaborator name the jobs pass to {@code invocar(...)} has a route. The two the job module sends itself to the
 * platform router (ConsultarFlujoCaja, ConsultarFlujoCustodiaFondoMutuo) must carry the legacy FQN; every other literal must be a name the service
 * adapter knows (or the StoredProcedure, which stays in the database). The scan reads the module's own sources, so a new call site cannot go unrouted.
 */
class ColaboradoresJobsEnrutamientoTest {

    private static final Pattern LITERAL = Pattern.compile("colaboradores\\s*\\.\\s*invocar\\(\\s*\"([A-Za-z0-9_]+)\"");
    private static final Pattern CUALQUIERA = Pattern.compile("colaboradores\\s*\\.\\s*invocar\\(");

    private HttpServer servidor;
    private final List<JsonObject> recibidos = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> token = new AtomicReference<>();

    @BeforeEach
    void arranca() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            token.set(exchange.getRequestHeaders().getFirst("X-Internal-Token"));
            recibidos.add(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
            byte[] r = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, r.length);
            exchange.getResponseBody().write(r);
            exchange.close();
        });
        servidor.start();
    }

    @AfterEach
    void para() {
        servidor.stop(0);
    }

    private static String fuentes() throws IOException {
        Path raiz = Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize().resolve("src/main/java");
        try (Stream<Path> s = Files.walk(raiz)) {
            List<Path> java = s.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
            assertThat(java).as("the scan must see the job sources").isNotEmpty();
            StringBuilder sb = new StringBuilder();
            for (Path p : java) {
                sb.append(Files.readString(p, StandardCharsets.UTF_8)).append('\n');
            }
            return sb.toString();
        }
    }

    @Test
    void todoLiteralDeInvocarDelModuloTieneRutaOEsConocidoPorElServicio() throws IOException {
        String fuente = fuentes();
        Set<String> literales = new TreeSet<>();
        Matcher m = LITERAL.matcher(fuente);
        while (m.find()) {
            literales.add(m.group(1));
        }
        assertThat(literales).contains("ConsultarFlujoCaja", "ConsultarFlujoCustodiaFondoMutuo", "CrearOrdenes", "AgregarLog", "StoredProcedure");
        for (String nombre : literales) {
            boolean propio = ColaboradoresJobs.CLASES.containsKey(nombre);
            boolean delServicio = ColaboradoresJavaCall.nombresConocidos().contains(nombre);
            assertThat(propio || delServicio).as("collaborator '" + nombre + "' has a route").isTrue();
        }
        assertThat(ColaboradoresJobs.CLASES).containsOnlyKeys("ConsultarFlujoCaja", "ConsultarFlujoCustodiaFondoMutuo");
    }

    @Test
    void ningunaLlamadaUsaUnNombreQueNoSeaUnLiteral() throws IOException {
        String fuente = fuentes();
        int todas = 0;
        Matcher m = CUALQUIERA.matcher(fuente);
        while (m.find()) {
            todas++;
        }
        int literales = 0;
        m = LITERAL.matcher(fuente);
        while (m.find()) {
            literales++;
        }
        assertThat(todas).as("every invocar call site passes a literal name").isEqualTo(literales).isPositive();
    }

    @Test
    void losDosDelModuloVanAlRouterConSuClaseLegalYElToken() {
        ColaboradoresJobs c = new ColaboradoresJobs(null, "http://127.0.0.1:" + servidor.getAddress().getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5),
                "tok");
        List<String> nombres = new ArrayList<>(List.of("ConsultarFlujoCaja", "ConsultarFlujoCustodiaFondoMutuo"));
        for (String n : nombres) {
            JsonObject params = new JsonObject();
            params.addProperty("marca", n);
            assertThat(c.invocar(n, params).get("ok").getAsBoolean()).isTrue();
            assertThat(token.get()).isEqualTo("tok");
        }
        assertThat(recibidos).hasSize(2);
        assertThat(recibidos.get(0).get("class").getAsString()).isEqualTo("cl.bcs.plataforma.pla.ConsultarFlujoCaja");
        assertThat(recibidos.get(1).get("class").getAsString()).isEqualTo("cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo");
        assertThat(recibidos.get(1).getAsJsonObject("params").get("marca").getAsString()).isEqualTo("ConsultarFlujoCustodiaFondoMutuo");
    }
}
