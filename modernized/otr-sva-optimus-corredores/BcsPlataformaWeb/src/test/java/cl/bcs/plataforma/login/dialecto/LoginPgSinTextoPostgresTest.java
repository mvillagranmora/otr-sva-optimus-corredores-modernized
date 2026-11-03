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
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.plataforma.config.GlobalExceptionHandler;

/**
 * A3-3 the 503 of a statement that has no text in the active dialect (it REPLACES the NO_DISPONIBLE catalog mark: no text = 503, nothing is sent). In
 * Postgres mode GET /bootstrap/login answers 503 BEFORE any SQL reaches the connection (zero statements counted), never falls back to HANA text, and the body
 * names the statement id. The statement without text is produced by a test double of SentenciasSql registered as {@code @Primary} (it overrides the public
 * non-final {@code texto} and {@code exigirDisponibles}). In HANA mode the same double is inert: the page answers 200. Convention of the module:
 * {@code GlobalExceptionHandler} maps NoDisponibleException to 503 (NOT_IMPLEMENTED). Rewrite of LoginPgNoDisponibleTest (see TRANSFORMATION_NOTES).
 */
class LoginPgSinTextoPostgresTest {

    static void produccionExiste() {
        ApiLoginPg3.clase("SentenciasSql");
        ApiLoginPg3.clase("HanaDialecto");
        ApiLoginPg3.clase("PostgresDialecto");
        if (ApiLoginPg3.existe("CatalogoEquivalencias")) throw ApiLoginPg3.catalogoEnProduccion("class CatalogoEquivalencias is still on the classpath of main");
    }

    private static final String SQL_INIT = "classpath:login/schema-login.sql,classpath:login/schema-bootstrap.sql,classpath:login/base-login.sql";

    /** the FIRST statement of the page without Postgres text */
    @TestConfiguration
    static class SinDiccionario {
        @Bean
        @Primary
        static SentenciasSql sentenciasSinDiccionario(SqlDialecto dialecto) {
            return new LoginPgContexto.SinTextoPostgres(dialecto, "BootstrapServicio.SQL_DICCIONARIO");
        }

        @Bean
        static org.springframework.beans.factory.config.BeanPostProcessor grabador() {
            return new LoginPgContexto.Grabador();
        }
    }

    /** the LAST statement of the page without Postgres text */
    @TestConfiguration
    static class SinImagen {
        @Bean
        @Primary
        static SentenciasSql sentenciasSinImagen(SqlDialecto dialecto) {
            return new LoginPgContexto.SinTextoPostgres(dialecto, "BootstrapServicio.SQL_IMAGEN");
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
    @Import(SinDiccionario.class)
    class PostgresSinTextoDelDiccionario {
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
        @DisplayName("A3-3 postgres: BootstrapServicio.SQL_DICCIONARIO has no Postgres text (test double) -> GET /bootstrap/login answers 503 with ZERO SQL on the connection; the body names the statement id and carries no HANA text")
        void responde_503_sin_leer_nada() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            String cuerpo = mvc.perform(get("/bootstrap/login")).andExpect(status().isServiceUnavailable()).andReturn().getResponse()
                    .getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).contains("BootstrapServicio.SQL_DICCIONARIO").doesNotContain("BSCL").doesNotContain("::");
            assertThat(LoginPgContexto.SQL).as("SQL texts that reached the connection").isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=postgres",
            "spring.datasource.url=jdbc:h2:mem:lpg503b;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    @Import(SinImagen.class)
    class PostgresSinTextoDeLaImagen {
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
        @DisplayName("A3-3 postgres: BootstrapServicio.SQL_IMAGEN (the LAST statement the page reads) has no Postgres text -> 503 and still zero SQL: every statement is checked before the first read")
        void la_ultima_sentencia_tambien_corta_antes_de_leer() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            String cuerpo = mvc.perform(get("/bootstrap/login")).andExpect(status().isServiceUnavailable()).andReturn().getResponse()
                    .getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).contains("BootstrapServicio.SQL_IMAGEN");
            assertThat(LoginPgContexto.SQL).isEmpty();
        }

        @Test
        @DisplayName("A3-3 postgres: the 503 never falls back to HANA: no statement with a HANA name was sent, even after repeated requests")
        void nunca_cae_a_hana() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            for (int i = 0; i < 3; i++) mvc.perform(get("/bootstrap/login")).andExpect(status().isServiceUnavailable());
            assertThat(LoginPgContexto.SQL.stream().filter(s -> s.contains("BSCL") || s.contains("::")).toList()).isEmpty();
            assertThat(LoginPgContexto.SQL).isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:lpg503h;MODE=LEGACY;DB_CLOSE_DELAY=-1", "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=" + SQL_INIT })
    @ActiveProfiles("test")
    @Import(SinDiccionario.class)
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
        @DisplayName("A3-3 hana (default): the same double is inert (it hides the text only in Postgres): the page answers 200, reads HANA-named tables and carries its dictionary")
        void en_hana_el_doble_no_afecta() throws Exception {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(wac).build();
            String cuerpo = mvc.perform(get("/bootstrap/login")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).contains("BCS_DICCIONARIO");
            assertThat(LoginPgContexto.SQL).isNotEmpty();
            assertThat(LoginPgContexto.SQL.stream().anyMatch(s -> s.contains("BSCL.Plataforma.BD::PLA.Diccionario"))).isTrue();
        }
    }

    @Test
    @DisplayName("A3-3 the exception of a statement without text is mapped to 503 by the module's own GlobalExceptionHandler, (NoDisponibleException naming the id)")
    void el_handler_existente_la_convierte_en_503() throws Exception {
        ApiLoginPg3.clase("SentenciasSql");
        SentenciasSql doble = new LoginPgContexto.SinTextoPostgres(ApiLoginPg3.postgres(), "CrearSesionServicio.QUERY_RELOJ_UTC");

        @org.springframework.web.bind.annotation.RestController
        class Lanza {
            @org.springframework.web.bind.annotation.GetMapping("/x")
            String x() {
                return doble.texto("CrearSesionServicio.QUERY_RELOJ_UTC");
            }
        }
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new Lanza()).setControllerAdvice(new GlobalExceptionHandler()).build();
        String cuerpo = mvc.perform(get("/x")).andExpect(status().isServiceUnavailable()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(cuerpo).contains("CrearSesionServicio.QUERY_RELOJ_UTC");
    }
}
