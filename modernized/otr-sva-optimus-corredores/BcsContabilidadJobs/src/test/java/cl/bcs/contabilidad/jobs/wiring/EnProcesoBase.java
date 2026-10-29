package cl.bcs.contabilidad.jobs.wiring;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base of the step 5 tests with the real application context ({@code contabilidad.jobs.colaboradores-en-proceso=true}, which is also what
 * application.yml says) on H2. A fake TomEE ({@code plataforma.base-url}) records every request it receives: a request here means a collaborator
 * went over HTTP.
 */
@SpringBootTest(properties = {"contabilidad.jobs.sesion-hana=false", "contabilidad.jobs.colaboradores-en-proceso=true"})
@ActiveProfiles("test")
abstract class EnProcesoBase {

    /** Paths received by the fake TomEE. */
    static final List<String> PETICIONES = new CopyOnWriteArrayList<>();
    private static final HttpServer TOMEE = arrancarTomee();

    @Autowired
    JdbcTemplate jdbc;

    private static HttpServer arrancarTomee() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext("/", ex -> {
                PETICIONES.add(ex.getRequestURI().getPath());
                ex.getRequestBody().readAllBytes();
                byte[] respuesta = "[]".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, respuesta.length);
                ex.getResponseBody().write(respuesta);
                ex.close();
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("plataforma.base-url", () -> "http://127.0.0.1:" + TOMEE.getAddress().getPort());
    }

    @BeforeEach
    void limpiarPeticiones() {
        PETICIONES.clear();
    }

    /** Fresh, empty copies of the objects the Tesoreria queries of these tests read ({@code columnas}: names, all VARCHAR unless typed). */
    void crearTabla(String nombre, String columnas) {
        jdbc.execute("DROP TABLE IF EXISTS \"" + nombre + "\"");
        jdbc.execute("CREATE TABLE \"" + nombre + "\" (" + columnas + ")");
    }
}
