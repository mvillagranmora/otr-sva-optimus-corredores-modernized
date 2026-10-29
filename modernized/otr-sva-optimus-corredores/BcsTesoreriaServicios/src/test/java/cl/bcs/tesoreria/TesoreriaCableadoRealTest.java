package cl.bcs.tesoreria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import cl.bcs.tesoreria.familia_a.support.H2Hana;
import cl.bcs.tesoreria.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.tesoreria.infrastructure.adapter.NombresDeColaborador;
import cl.bcs.tesoreria.infrastructure.web.TokenInternoFilter;
import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

/**
 * Production wiring that the 944 characterization tests cannot see (they build the services with {@code new} and script the Colaboradores seam):
 * the REAL filter, controller, dispatcher, Spring transaction proxies ({@code DataSourceTransactionManager} over H2 MODE=LEGACY behind the
 * HANA dialect bridge) and the REAL {@code ColaboradoresJavaCall}.
 * <ul>
 *   <li>H1: every collaborator name the code passes to {@code invocar} resolves through the real bean;</li>
 *   <li>H2: GirarComprobanteIngreso with a failing CrearComprobante returns the legacy result and its earlier writes are COMMITTED (not a 500);</li>
 *   <li>H4: cloper.ActualizarTransaccion answers 503 (router falls back to TomEE), not 200 with ERROR_AL_CONSULTAR_CLOPER_WS;</li>
 *   <li>the token filter rejects {@code /internal;x=1/java-call} and {@code //internal/java-call} with 401.</li>
 * </ul>
 * The schema and the seeds are the ones of the recorded cases (TES-A-560 for Girar, TES-A-764 for ActualizarTransaccion), unmodified.
 */
@Import(TesoreriaCableadoRealTest.Cableado.class)
@SpringBootTest(classes = TesoreriaServiceApplication.class, properties = {
        "HANA_URL=jdbc:h2:mem:cableado_hana;MODE=LEGACY;DB_CLOSE_DELAY=-1",
        "HANA_USER=sa",
        "HANA_PASSWORD=",
        "INTERNAL_TOKEN=token-de-prueba",
        "spring.autoconfigure.exclude=" })
class TesoreriaCableadoRealTest {

    private static final String URL_H2 = "jdbc:h2:mem:cableado_real;MODE=LEGACY;DB_CLOSE_DELAY=-1";
    private static final String TOKEN = "token-de-prueba";

    /** The platform router: records what the real ColaboradoresJavaCall posts to it. */
    private static final List<JsonObject> ROUTER = new CopyOnWriteArrayList<>();
    private static final HttpServer SERVIDOR_ROUTER = arrancaRouter();

    private static HttpServer arrancaRouter() {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            s.createContext("/", exchange -> {
                ROUTER.add(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
                byte[] r = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, r.length);
                exchange.getResponseBody().write(r);
                exchange.close();
            });
            s.start();
            return s;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void router(DynamicPropertyRegistry registro) {
        registro.add("JAVACALL_URL", () -> "http://127.0.0.1:" + SERVIDOR_ROUTER.getAddress().getPort());
    }

    @AfterAll
    static void paraElRouter() {
        SERVIDOR_ROUTER.stop(0);
    }

    /**
     * H2 behind the HANA dialect bridge, one new connection per transaction (what the real DataSourceTransactionManager asks for), and a
     * ConfiguracionSistema that gives the CLOPER web service parameters and makes CrearComprobante fail (the parameter it reads first).
     */
    @TestConfiguration
    static class Cableado {

        @Bean
        DataSource dataSource() throws SQLException {
            try (Connection raw = DriverManager.getConnection(URL_H2, "sa", "")) {
                for (String script : List.of("schema-tesoreria-a.sql", "seed/base-tesoreria-a.sql", "cases/TES-A-560_gci-gira-un-comprobante-actualiza-la-cuenta-de-la-corredora-y-lo-deja-GIR/seed.sql",
                        "cases/TES-A-764_atr-calce-con-liquidacion-bilateral-B-marca-la-camara-y-EsBilateral-S/seed.sql")) {
                    Path p = cl.bcs.tesoreria.familia_a.support.Caso.recurso("tesoreria-a/" + script);
                    ScriptUtils.executeSqlScript(raw, new EncodedResource(new FileSystemResource(p), StandardCharsets.UTF_8));
                }
            }
            return new AbstractDataSource() {
                @Override
                public Connection getConnection() throws SQLException {
                    return H2Hana.wrap(DriverManager.getConnection(URL_H2, "sa", ""));
                }

                @Override
                public Connection getConnection(String usuario, String clave) throws SQLException {
                    return getConnection();
                }

                @Override
                public Logger getParentLogger() {
                    return Logger.getLogger("cableado");
                }
            };
        }

        /** The message dictionary table is not part of the case schema: the code itself is the text. */
        @Bean
        @Primary
        cl.bcs.tesoreria.port.out.DiccionarioMensajes diccionarioDelCableado() {
            return (idEmpresa, codigo, valores) -> codigo;
        }

        @Bean
        @Primary
        ConfiguracionSistema configuracionDelCableado() {
            Map<String, String> cloper = Map.of(
                    "PLA.CloperWsBSC.Password", "fake-password-0000",
                    "PLA.CloperWsBSC.Profile", "fake-profile",
                    "PLA.CloperWsBSC.Wsdl", "http://cloper.example.invalid/ws?wsdl");
            return (idEmpresa, modulo, nombre) -> {
                if ("TESORERIA".equals(modulo) && "USARCREARCOMPROBANTEJAVA".equals(nombre)) {
                    throw new IllegalStateException("CrearComprobante exploded (scripted)");
                }
                return "PLATAFORMA".equals(modulo) ? cloper.get(nombre) : null;
            };
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private JavaCallDispatcher dispatcher;

    @Autowired
    private ColaboradoresJavaCall colaboradores;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TokenInternoFilter filtro;

    private MockMvc mvc;

    @BeforeEach
    void montaElMvcConElFiltroReal() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
        ROUTER.clear();
    }

    private org.springframework.test.web.servlet.ResultActions javaCall(String clase, String params) throws Exception {
        return mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"class\":\"" + clase + "\",\"params\":" + params + "}"));
    }

    // ---------------------------------------------------------------- H4

    @Test
    void cloperNoDisponibleResponde503ParaQueElRouterCaigaATomEE() throws Exception {
        javaCall("cl.bcs.tesoreria.cloper.ActualizarTransaccion",
                "{\"p_IdEmpresa\":1,\"p_FechaTransaccion\":\"2025-04-01\",\"p_FolioTransaccion\":\"?\"}").andExpect(status().isServiceUnavailable());
    }

    @Test
    void elMismoEndpointSiResponde200CuandoNoHayTransaccionesQueConsultarEnCloper() throws Exception {
        javaCall("cl.bcs.tesoreria.cloper.ActualizarTransaccion",
                "{\"p_IdEmpresa\":1,\"p_FechaTransaccion\":\"2030-01-01\",\"p_FolioTransaccion\":\"?\"}").andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("NO_SE_ENCONTRARON_TRANSACCIONES_PARA_FECHA_INDICADA")));
    }

    // ---------------------------------------------------------------- H2

    @Test
    void girarConCrearComprobanteQueFallaDevuelveElResultadoLegacyYDejaCommitEnLoEscrito() throws Exception {
        JdbcTemplate fresca = new JdbcTemplate(dataSource);
        String sql = "SELECT \"IdTipoCuentaBancariaCorredora\" FROM \"BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago\" WHERE \"IdComprobanteFormaDePago\" = 21011";
        assertThat(fresca.queryForObject(sql, String.class)).as("before: the broker account is not written yet").isNull();

        String entrada = """
                {"p_IdEmpresa":1,"p_IdUsuario":97,"p_IdModulo":"TES","p_simularOperacion":"N","p_UsaReglasNegocio":"S","p_FechaMovimiento":"2025-04-01",
                 "p_IdCaja":1,"p_ComprobantesAGirar":[{"IdComprobante":2101}],"p_GeneraNomina":"N","p_GeneraTransferencia":"N","p_IdMedioDePago":"TRA",
                 "p_IdCuentaBancariaCorredora":301,"p_FormasDePago":[],"p_monto":10000,"p_IdModalidadDePago":"MODDEP","p_IdMoneda":"CLP","p_usaTalonarioCheque":"N"}
                """;
        // through the proxies: dispatcher -> GirarComprobanteIngresoService (tx) -> CrearComprobanteService (tx) -> throws -> swallowed by Girar
        javaCall("cl.bcs.tesoreria.GirarComprobanteIngreso", entrada).andExpect(status().isOk())
                .andExpect(content().json("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[]}"));

        // LEGACY DEFECT kept (TES-A-567/568/585/590/591): the failure is swallowed and the partial work stays committed. Read it from a NEW connection.
        assertThat(fresca.queryForObject(sql, String.class)).as("the update of the broker account made before the failure is committed").isEqualTo("CTC");
        assertThat(fresca.queryForObject(
                "SELECT \"NombreCuentaBancariaCorredora\" FROM \"BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago\" WHERE \"IdComprobanteFormaDePago\" = 21011",
                String.class)).isEqualTo("Cuenta corredora CLP");
    }

    // ---------------------------------------------------------------- H1

    @Test
    void todoNombreDeColaboradorQueElCodigoInvocaSeResuelveEnElBeanReal() {
        for (String nombre : NombresDeColaborador.literalesDeInvocar()) {
            if (nombre.equals("StoredProcedure") || nombre.equals("Cloper")) {
                continue;
            }
            String fqn = ColaboradoresJavaCall.clase(nombre);
            if (dispatcher.maneja(fqn)) {
                continue; // served in process by a bean of this module (the dispatcher test checks that each has exactly one bean)
            }
            int antes = ROUTER.size();
            colaboradores.invocar(nombre, new JsonObject());
            assertThat(ROUTER).as("remote call of " + nombre).hasSize(antes + 1);
            assertThat(ROUTER.get(antes).get("class").getAsString()).as(nombre).isEqualTo(fqn).contains(".");
        }
        assertThatThrownBy(() -> colaboradores.invocar("NoExisteEsteColaborador", new JsonObject())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> colaboradores.invocar("Cloper", new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
    }

    // ---------------------------------------------------------------- token filter

    private static RequestPostProcessor uri(String crudo) {
        return peticion -> {
            peticion.setRequestURI(crudo);
            return peticion;
        };
    }

    @Test
    void elFiltroDelTokenRechazaLasVariantesDeLaRutaInterna() throws Exception {
        String cuerpo = "{\"class\":\"cl.bcs.tesoreria.CuadraturaCierreDiario\",\"params\":{}}";
        for (String ruta : List.of("/internal;x=1/java-call", "//internal/java-call", "/internal/java-call", "/internal/;a=b/java-call")) {
            mvc.perform(post("/internal/java-call").with(uri(ruta)).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/internal/java-call").with(uri(ruta)).header("X-Internal-Token", "otro").contentType(MediaType.APPLICATION_JSON)
                    .content(cuerpo)).andExpect(status().isUnauthorized());
        }
        // control: with the secret the same endpoint is reached (an unknown class answers 503, which proves the controller ran)
        javaCall("cl.bcs.tesoreria.NoMigrada", "{}").andExpect(status().isServiceUnavailable());
    }
}
