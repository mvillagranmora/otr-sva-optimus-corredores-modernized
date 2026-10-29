package cl.bcs.tesoreria.jobs.wiring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base of the step 5 tests with the real application context ({@code tesoreria.jobs.colaboradores-en-proceso=true}) on H2: a fake platform
 * router (every request it receives is recorded: a request here means a collaborator went over HTTP), and the H2 objects the APV use case
 * {@code CambiarEstadoContratos} runs on (verbatim DDL of the APV family A schema, case APV-A-311 of the APV goldens, copied byte for byte).
 */
@SpringBootTest(properties = {"tesoreria.jobs.sesion-hana=false", "tesoreria.jobs.colaboradores-en-proceso=true"})
@ActiveProfiles("test")
abstract class EnProcesoBase {

    static final Gson GSON = new GsonBuilder().serializeNulls().create();
    static final String CASO = "/tesoreria-jobs/enproceso/apv-A-311/";
    static final List<String> TABLAS = List.of("BSCL.APV.BD::SCS.ContratoSAAPV", "BSCL.APV.BD::TRA.Traspaso",
            "BSCL.SocioNegocio.BD::CLI.Cliente", "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad", "BSCL.SocioNegocio.BD::PER.Persona",
            "BSCL.Tesoreria.BD::PAC.MandatoPAC", "BSCL.Plataforma.BD::PLA.Configuracion",
            "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion");

    /** Requests received by the fake router: {@code {"class", "params"}}. */
    static final List<JsonObject> PETICIONES = new CopyOnWriteArrayList<>();
    private static final HttpServer ROUTER = arrancarRouter();

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    DataSource dataSource;

    private static HttpServer arrancarRouter() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext("/internal/java-call", ex -> {
                String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                PETICIONES.add(JsonParser.parseString(cuerpo).getAsJsonObject());
                byte[] respuesta = "{\"P_OUT_MENSAJES\":[],\"P_OUT_ERRORESCONTADOR\":0}".getBytes(StandardCharsets.UTF_8);
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
        registro.add("services.javacall.url", () -> "http://127.0.0.1:" + ROUTER.getAddress().getPort());
    }

    static String recurso(String ruta) {
        try (InputStream in = EnProcesoBase.class.getResourceAsStream(ruta)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static JsonObject caso() {
        return JsonParser.parseString(recurso(CASO + "case.json")).getAsJsonObject();
    }

    static JsonObject esperado() {
        return JsonParser.parseString(recurso(CASO + "expected.json")).getAsJsonObject();
    }

    /** Fresh objects and the seed of the golden case before every test; the router log is cleared. */
    @BeforeEach
    void prepararBase() {
        PETICIONES.clear();
        for (String tabla : TABLAS) {
            jdbc.execute("DROP TABLE IF EXISTS \"" + tabla + "\"");
        }
        ResourceDatabasePopulator esquema = new ResourceDatabasePopulator(new ClassPathResource("/tesoreria-jobs/enproceso/schema-apv-min.sql"));
        esquema.execute(dataSource);
        jdbc.execute("CREATE TABLE \"BSCL.Plataforma.BD::PLA.Configuracion\" (\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(200), \"Valor\" VARCHAR(500))");
        jdbc.execute("CREATE TABLE \"BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion\" "
                + "(\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(200), \"Valor\" VARCHAR(500))");
        // the configuration of the golden case (seed/config.json of the APV goldens): only what the VIG path reads
        jdbc.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.FechaSistema', '2025-04-01')");
        jdbc.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'APV.AutorizaGestionContrato', 'N')");
        new ResourceDatabasePopulator(new ByteArrayResource(recurso(CASO + "seed.sql").getBytes(StandardCharsets.UTF_8))).execute(dataSource);
    }

    /** The rows of a table of the golden case ({@code tablas} of case.json: name, order, columns) as the golden holds them. */
    JsonArray volcar(String nombre) {
        for (JsonElement t : caso().getAsJsonArray("tablas")) {
            JsonObject tabla = t.getAsJsonObject();
            if (tabla.get("nombre").getAsString().equals(nombre)) {
                StringBuilder columnas = new StringBuilder();
                for (JsonElement c : tabla.getAsJsonArray("columnas")) {
                    columnas.append(columnas.isEmpty() ? "" : ", ").append('"').append(c.getAsString()).append('"');
                }
                List<Map<String, Object>> filas = jdbc.queryForList("SELECT " + columnas + " FROM \"" + nombre + "\" ORDER BY "
                        + tabla.get("orden").getAsString());
                return GSON.toJsonTree(filas).getAsJsonArray();
            }
        }
        throw new AssertionError("table not in the case: " + nombre);
    }
}
