package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.plataforma.config.GlobalExceptionHandler;

/**
 * T-32 / T-35 the 503 of an object that is NO_DISPONIBLE (or absent): in Postgres mode GET /bootstrap/login answers 503 BEFORE any SQL reaches the
 * connection (zero statements counted), never falls back to HANA, and the body names the HANA object; in HANA mode the same marked object is untouched
 * (the page answers 200 and reads). Convention found in the module (Q-LOGINPG-2): {@code GlobalExceptionHandler} maps UnsupportedOperationException to 503
 * (NOT_IMPLEMENTED) and passes a ResponseStatusException through; BootstrapController today catches every Exception and answers 500, so the production change
 * must let the 503 exception through (pinned here by the end-to-end test).
 */
class LoginPgNoDisponibleTest {

    static void produccionExiste() {
        ApiLoginPg.clase("ObjetoNoDisponibleException");
        ApiLoginPg.clase("CatalogoEquivalencias");
    }

    private static final String SQL_INIT = "classpath:login/schema-login.sql,classpath:login/schema-bootstrap.sql,classpath:login/base-login.sql";

    @TestConfiguration
    static class MarcaDiccionario {
        @Bean
        static org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor marca() {
            return new LoginPgContexto.CatalogoMarcado("BSCL.Plataforma.BD::PLA.Diccionario");
        }

        @Bean
        static org.springframework.beans.factory.config.BeanPostProcessor grabador() {
            return new LoginPgContexto.Grabador();
        }
    }

    @TestConfiguration
    static class MarcaImagenVista {
        @Bean
        static org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor marca() {
            return new LoginPgContexto.CatalogoMarcado("BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen");
        }

        @Bean
        static org.springframework.beans.factory.config.BeanPostProcessor grabador() {
            return new LoginPgContexto.Grabador();
        }
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=postgres",
            "spring.datasource.url=jdbc:h2:mem:lpg503a;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    @Import(MarcaDiccionario.class)
    class PostgresDiccionarioNoDisponible {
        @BeforeAll
        static void existe() {
            produccionExiste();
        }

        @Autowired
        WebApplicationContext wac;

        @BeforeEach
        void limpiar() {
            LoginPgContexto.limpiar();
        }

        @Test
        @DisplayName("T-32 postgres: PLA.Diccionario is NO_DISPONIBLE -> GET /bootstrap/login answers 503 with ZERO SQL on the connection (the first statement of the page is another table)")
        void responde_503_sin_leer_nada() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            String cuerpo = mvc.perform(get("/bootstrap/login")).andExpect(status().isServiceUnavailable()).andReturn().getResponse()
                    .getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).contains("PLA.Diccionario");
            assertThat(LoginPgContexto.SQL).as("SQL texts that reached the connection").isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=postgres",
            "spring.datasource.url=jdbc:h2:mem:lpg503b;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    @Import(MarcaImagenVista.class)
    class PostgresImagenNoDisponible {
        @BeforeAll
        static void existe() {
            produccionExiste();
        }

        @Autowired
        WebApplicationContext wac;

        @BeforeEach
        void limpiar() {
            LoginPgContexto.limpiar();
        }

        @Test
        @DisplayName("T-32 postgres: ConsultarImagen (the LAST object the page reads) is NO_DISPONIBLE -> 503 and still zero SQL: every object is checked before the first read")
        void el_ultimo_objeto_tambien_corta_antes_de_leer() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            mvc.perform(get("/bootstrap/login")).andExpect(status().isServiceUnavailable());
            assertThat(LoginPgContexto.SQL).isEmpty();
        }

        @Test
        @DisplayName("T-32 postgres: the 503 never falls back to HANA: no statement with a HANA name was sent, even after repeated requests")
        void nunca_cae_a_hana() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            for (int i = 0; i < 3; i++) mvc.perform(get("/bootstrap/login")).andExpect(status().isServiceUnavailable());
            assertThat(LoginPgContexto.SQL.stream().filter(s -> s.contains("BSCL") || s.contains("::")).toList()).isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:lpg503h;MODE=LEGACY;DB_CLOSE_DELAY=-1", "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=" + SQL_INIT })
    @ActiveProfiles("test")
    @Import(MarcaDiccionario.class)
    class HanaNoSeToca {
        @BeforeAll
        static void existe() {
            produccionExiste();
        }

        @Autowired
        WebApplicationContext wac;

        @BeforeEach
        void limpiar() {
            LoginPgContexto.limpiar();
        }

        @Test
        @DisplayName("T-32 hana (default): the same object marked NO_DISPONIBLE in the catalog is untouched: the page answers 200, reads HANA-named tables and carries its dictionary")
        void en_hana_el_objeto_marcado_no_afecta() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            String cuerpo = mvc.perform(get("/bootstrap/login")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).contains("BCS_DICCIONARIO");
            assertThat(LoginPgContexto.SQL).isNotEmpty();
            assertThat(LoginPgContexto.SQL.stream().anyMatch(s -> s.contains("BSCL.Plataforma.BD::PLA.Diccionario"))).isTrue();
        }
    }

    @Test
    @DisplayName("T-32 the exception of an unavailable object is mapped to 503 by the module's own GlobalExceptionHandler, unchanged (UnsupportedOperationException or ResponseStatusException 503)")
    void el_handler_existente_la_convierte_en_503() throws Exception {
        Class<?> c = ApiLoginPg.clase("ObjetoNoDisponibleException");
        Object e = null;
        for (var k : c.getConstructors()) {
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) e = k.newInstance("BSCL.Plataforma.BD::PLA.Diccionario");
        }
        assertThat(e).as("ObjetoNoDisponibleException(String hanaName) constructor (A-4)").isNotNull();
        final Throwable lanzada = (Throwable) e;
        assertThat(lanzada instanceof UnsupportedOperationException || (lanzada instanceof org.springframework.web.server.ResponseStatusException r
                && r.getStatusCode().value() == 503)).as("must be mapped by the existing handler").isTrue();

        @org.springframework.web.bind.annotation.RestController
        class Lanza {
            @org.springframework.web.bind.annotation.GetMapping("/x")
            String x() throws Throwable {
                throw lanzada;
            }
        }
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new Lanza()).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/x")).andExpect(status().isServiceUnavailable());
    }
}
