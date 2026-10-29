package cl.bcs.apv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.apv.api.CasoUso;
import cl.bcs.apv.api.casouso.CasosUso;
import cl.bcs.apv.application.ApvCasosDeUso;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.familia_a.support.H2Hana;
import cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.apv.infrastructure.web.TokenInternoFilter;

/**
 * Production wiring that the characterization tests cannot see (they compose the services with {@code ApvCasosDeUso} and a single connection):
 * the REAL Spring Boot application with the real {@code DataSourceTransactionManager} over H2 MODE=LEGACY behind the HANA dialect bridge, the real
 * transaction proxies, the real token filter, the real controllers and the real {@code ColaboradoresJavaCall}.
 * <ul>
 *   <li>every use case of {@code bcs-apv-api} has EXACTLY one bean, and it is the very class that the in-process composition root builds;</li>
 *   <li>H2 (Tesoreria review): a participating transactional collaborator that throws and is swallowed by its caller does NOT make the caller's commit fail
 *       (the legacy platform model: commit when the entry point returns, rollback only when it throws): {@code globalRollbackOnParticipationFailure=false};</li>
 *   <li>a checked exception thrown through {@code Sneaky} rolls back, and crosses the CGLIB transaction proxy wrapped in {@code UndeclaredThrowableException}
 *       (finding): {@code Sneaky.causa} unwraps it at the borders;</li>
 *   <li>H3: the token filter is an allow-list: {@code /x;a=b/}, {@code //x} variants and every typed endpoint need the token; only the health probes are public;</li>
 *   <li>a collaborator name that is not registered is an {@code IllegalArgumentException} through the real bean.</li>
 * </ul>
 */
@Import(ApvCableadoRealTest.Cableado.class)
@SpringBootTest(classes = ApvApplication.class, properties = {
        "HANA_URL=jdbc:h2:mem:apv_cableado_hana;MODE=LEGACY;DB_CLOSE_DELAY=-1",
        "HANA_USER=sa",
        "HANA_PASSWORD=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "INTERNAL_TOKEN=token-de-prueba",
        "JAVACALL_URL=http://127.0.0.1:9",
        "spring.autoconfigure.exclude=" })
class ApvCableadoRealTest {

    private static final String URL_H2 = "jdbc:h2:mem:apv_cableado_real;MODE=LEGACY;DB_CLOSE_DELAY=-1";
    private static final String TOKEN = "token-de-prueba";

    /** The service the transactional scenarios run on: a table of the case schema is used as scratch (ids far from the seeds). */
    @TestConfiguration
    static class Cableado {

        @Bean
        DataSource dataSource() throws SQLException {
            try (Connection raw = DriverManager.getConnection(URL_H2, "sa", "")) {
                for (String script : List.of("schema-apv-a.sql", "seed/base-apv-a.sql")) {
                    Path p = cl.bcs.apv.familia_a.support.Caso.recurso("apv-a/" + script);
                    ScriptUtils.executeSqlScript(raw, new EncodedResource(new FileSystemResource(p), StandardCharsets.UTF_8));
                }
                raw.createStatement().execute("CREATE TABLE IF NOT EXISTS CABLEADO_PRUEBA (ID BIGINT PRIMARY KEY, TEXTO VARCHAR(50))");
            }
            // one NEW connection per transaction, like the real DataSourceTransactionManager asks for; the HANA dialect bridge on every statement
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

        @Bean
        Exterior exterior(Interior interior, NamedParameterJdbcTemplate jdbc) {
            return new Exterior(interior, jdbc);
        }

        @Bean
        Interior interior(NamedParameterJdbcTemplate jdbc) {
            return new Interior(jdbc);
        }
    }

    /** A write entry point (the shape of every use case): writes, calls a transactional collaborator that fails, and swallows the failure (legacy catch). */
    @Transactional(rollbackFor = Exception.class)
    public static class Exterior {
        private final Interior interior;
        private final NamedParameterJdbcTemplate jdbc;

        public Exterior(Interior interior, NamedParameterJdbcTemplate jdbc) {
            this.interior = interior;
            this.jdbc = jdbc;
        }

        public void escribeYTragaElFalloDelColaborador() {
            jdbc.update("INSERT INTO CABLEADO_PRUEBA VALUES (1, 'exterior')", Map.of());
            try {
                interior.escribeYFalla();
            } catch (IllegalStateException e) {
                // LEGACY model: the exception is swallowed, the partial work of the caller stays and is committed
            }
        }

        public void lanzaUnaExcepcionChequeadaSinEnvolver() {
            jdbc.update("INSERT INTO CABLEADO_PRUEBA VALUES (2, 'exterior-que-falla')", Map.of());
            throw Sneaky.lanzar(new SQLException("fallo de la base de datos simulado"));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public static class Interior {
        private final NamedParameterJdbcTemplate jdbc;

        public Interior(NamedParameterJdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        public void escribeYFalla() {
            jdbc.update("INSERT INTO CABLEADO_PRUEBA VALUES (3, 'interior')", Map.of());
            throw new IllegalStateException("el colaborador fallo");
        }
    }

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private ApplicationContext aplicacion;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TokenInternoFilter filtro;

    @Autowired
    private Exterior exterior;

    @Autowired
    private DataSourceTransactionManager gestor;

    @Autowired
    private Colaboradores colaboradores;

    private MockMvc mvc;
    private JdbcTemplate fresca;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
        fresca = new JdbcTemplate(dataSource);
        fresca.update("DELETE FROM CABLEADO_PRUEBA");
    }

    // ---------------------------------------------------------------- the graph

    @Test
    void cadaCasoDeUsoTieneExactamenteUnBeanYEsLaClaseQueArmaLaComposicionEnProceso() {
        Map<Class<?>, Object> enProceso = ApvCasosDeUso.crear(new NamedParameterJdbcTemplate(dataSource), (e, m, n) -> null,
                (e, c, v) -> c, n -> 1L, java.time.Instant::now, (r, c) -> { }, (s, e) -> new com.google.gson.JsonObject());
        List<String> problemas = new ArrayList<>();
        for (Class<? extends CasoUso<?, ?>> caso : CasosUso.TODOS) {
            Map<String, ?> beans = aplicacion.getBeansOfType(caso);
            if (beans.size() != 1) {
                problemas.add(caso.getSimpleName() + ": " + beans.size() + " beans");
                continue;
            }
            Class<?> claseDelBean = AopUtils.getTargetClass(beans.values().iterator().next());
            if (!claseDelBean.equals(enProceso.get(caso).getClass())) {
                problemas.add(caso.getSimpleName() + ": " + claseDelBean + " vs " + enProceso.get(caso).getClass());
            }
        }
        assertThat(problemas).isEmpty();
    }

    @Test
    void todaImplementacionDeUnCasoDeUsoConEscrituraEsTransaccionalConRollbackForException() {
        List<String> sinTransaccion = new ArrayList<>();
        for (Class<? extends CasoUso<?, ?>> caso : CasosUso.TODOS) {
            Object bean = aplicacion.getBean(caso);
            Class<?> clase = AopUtils.getTargetClass(bean);
            var tx = org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(clase, Transactional.class);
            boolean consulta = caso.getSimpleName().startsWith("Consultar") || caso.getSimpleName().startsWith("Buscar");
            if (!consulta && (tx == null || !java.util.Arrays.asList(tx.rollbackFor()).contains(Exception.class))) {
                sinTransaccion.add(caso.getSimpleName());
            }
        }
        // the families still being implemented may not have the annotation yet: the list is reported, and must be empty when the module is done
        assertThat(sinTransaccion).as("write use cases without @Transactional(rollbackFor = Exception.class)").isEmpty();
    }

    // ---------------------------------------------------------------- transaction model (legacy)

    @Test
    void elGestorNoMarcaRollbackOnlyEnLaParticipacionQueFalla() {
        assertThat(gestor.isGlobalRollbackOnParticipationFailure()).isFalse();
    }

    @Test
    void unColaboradorTransaccionalQueFallaYSeTragaDejaElTrabajoDelLlamadorCommiteado() {
        exterior.escribeYTragaElFalloDelColaborador();

        // read from a NEW connection: the entry point returned, so the transaction COMMITS everything done on its one connection, the failed
        // collaborator's partial write included (legacy model: one connection, no savepoint; UnexpectedRollbackException would be the Spring default)
        assertThat(fresca.queryForList("SELECT TEXTO FROM CABLEADO_PRUEBA ORDER BY ID", String.class)).containsExactly("exterior", "interior");
    }

    @Test
    void unaExcepcionChequeadaQueCruzaElProxyLlegaEnvueltaPeroSeDesenvuelveEnElBordeYHaceRollback() {
        // FINDING: the CGLIB transaction proxy wraps a checked exception the proxied method does not declare (Sneaky) in UndeclaredThrowableException
        assertThatThrownBy(() -> exterior.lanzaUnaExcepcionChequeadaSinEnvolver()).isExactlyInstanceOf(java.lang.reflect.UndeclaredThrowableException.class)
                .satisfies(e -> assertThat(Sneaky.causa(e)).isExactlyInstanceOf(SQLException.class).hasMessage("fallo de la base de datos simulado"));
        assertThat(fresca.queryForList("SELECT TEXTO FROM CABLEADO_PRUEBA", String.class)).isEmpty();
    }

    // ---------------------------------------------------------------- collaborators

    @Test
    void elColaboradorDeProduccionEsElBeanRealYRechazaUnNombreNoRegistrado() {
        assertThat(colaboradores).isInstanceOf(ColaboradoresJavaCall.class);
        assertThatThrownBy(() -> colaboradores.invocar("NoExisteEsteColaborador", new com.google.gson.JsonObject()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------------------------------------------------------- token filter (allow-list)

    private static RequestPostProcessor uri(String crudo) {
        return peticion -> {
            peticion.setRequestURI(crudo);
            return peticion;
        };
    }

    @Test
    void elFiltroDelTokenProtegeTodoSalvoLaSaludYRechazaLasVariantesDeLaRuta() throws Exception {
        String cuerpo = "{}";
        for (String ruta : List.of("/apv/contratos/validacion", "/apv;x=1/contratos/validacion", "//apv/contratos/validacion", "/apv/;a=b/contratos/validacion",
                "/x;a=b/", "//x", "/internal/java-call", "/actuator/env", "/")) {
            mvc.perform(post("/apv/contratos/validacion").with(uri(ruta)).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/apv/contratos/validacion").with(uri(ruta)).header("X-Internal-Token", "otro").contentType(MediaType.APPLICATION_JSON)
                    .content(cuerpo)).andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/apv/portafolios/custodia")).andExpect(status().isUnauthorized());
        // control: with the secret the typed endpoint is reached (a body that is not JSON is a 400, which proves the controller ran)
        mvc.perform(post("/apv/contratos/validacion").header("X-Internal-Token", TOKEN).contentType(MediaType.APPLICATION_JSON).content("no es json"))
                .andExpect(status().isBadRequest());
        // the health probes are public
        assertThat(mvc.perform(get("/actuator/health")).andReturn().getResponse().getStatus()).isNotEqualTo(401);
    }
}
