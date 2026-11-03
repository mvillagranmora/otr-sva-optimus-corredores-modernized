package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.plataforma.BcsPlataformaWebApplication;

/**
 * T-30 selection and wiring: {@code plataforma.db.dialect} absent or {@code hana} gives the HANA dialect, {@code postgres} the Postgres one, any other value
 * stops the start with a clear message; the application context starts in both modes with H2 only (no real database), with ONE DataSource and no JPA.
 */
class LoginPgSeleccionTest {

    static void produccionExiste() {
        ApiLoginPg.clase("SqlDialecto");
        ApiLoginPg.clase("HanaDialecto");
        ApiLoginPg.clase("PostgresDialecto");
    }

    private static void comprobar(org.springframework.context.ApplicationContext ctx, String esperado, String clase) {
        Class<?> tipo = ApiLoginPg.clase("SqlDialecto");
        assertThat(ctx.getBeansOfType(tipo)).as("exactly one SqlDialecto bean").hasSize(1);
        Object d = ctx.getBean(tipo);
        assertEquals(clase, d.getClass().getSimpleName());
        assertEquals(esperado, ApiLoginPg.f(d, "nombre"));
        assertThat(ctx.getBeansOfType(DataSource.class)).as("no second DataSource").hasSize(1);
        assertThat(ctx.getBeansOfType(ApiLoginPg.clase("SentenciasSql"))).as("one statement catalog").hasSize(1);
        for (String n : ctx.getBeanDefinitionNames()) {
            Object def = ctx.getType(n);
            String cn = def == null ? "" : def.toString();
            assertThat(cn).as("bean " + n + " must not be a JPA/Hibernate bean").doesNotContain("jakarta.persistence").doesNotContain("org.hibernate");
        }
    }

    @Nested
    @SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:lpgsel_defecto;MODE=LEGACY;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    class SinPropiedad {
        @BeforeAll
        static void existe() {
            produccionExiste();
        }

        @Autowired
        org.springframework.context.ApplicationContext ctx;

        @Test
        @DisplayName("T-30 property absent: the context starts and the dialect bean is HanaDialecto (default hana)")
        void defecto_es_hana() {
            assertThat(ctx.getEnvironment().containsProperty(ApiLoginPg.PROPIEDAD) && !"hana".equals(ctx.getEnvironment().getProperty(ApiLoginPg.PROPIEDAD))).isFalse();
            comprobar(ctx, "hana", "HanaDialecto");
        }
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=hana", "spring.datasource.url=jdbc:h2:mem:lpgsel_hana;MODE=LEGACY;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    class Hana {
        @BeforeAll
        static void existe() {
            produccionExiste();
        }

        @Autowired
        org.springframework.context.ApplicationContext ctx;

        @Test
        @DisplayName("T-30 plataforma.db.dialect=hana: HanaDialecto")
        void hana() {
            comprobar(ctx, "hana", "HanaDialecto");
        }
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=postgres",
            "spring.datasource.url=jdbc:h2:mem:lpgsel_pg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    class Postgres {
        @BeforeAll
        static void existe() {
            produccionExiste();
        }

        @Autowired
        org.springframework.context.ApplicationContext ctx;

        @Test
        @DisplayName("T-30 plataforma.db.dialect=postgres: PostgresDialecto, the context starts on H2 with one DataSource and no JPA")
        void postgres() {
            comprobar(ctx, "postgres", "PostgresDialecto");
        }
    }

    @Test
    @DisplayName("T-30 an invalid value stops the start with a message that names the property, the value and the valid values")
    void valor_invalido_falla_al_arrancar() {
        produccionExiste();
        ConfigurableApplicationContext ctx = null;
        List<String> mensajes = new ArrayList<>();
        try {
            ctx = new SpringApplicationBuilder(BcsPlataformaWebApplication.class).web(WebApplicationType.NONE).profiles("test")
                    .properties("plataforma.db.dialect=oracle", "spring.datasource.url=jdbc:h2:mem:lpgsel_malo;DB_CLOSE_DELAY=-1", "server.port=0").run();
        } catch (Throwable e) {
            for (Throwable t = e; t != null; t = t.getCause()) mensajes.add(String.valueOf(t.getMessage()));
        } finally {
            if (ctx != null) ctx.close();
        }
        if (mensajes.isEmpty()) fail("the context started with plataforma.db.dialect=oracle");
        String todo = String.join(" | ", mensajes);
        assertThat(todo).contains("plataforma.db.dialect").contains("oracle").contains("hana").contains("postgres");
    }

    @Test
    @DisplayName("T-30 no JPA: the module declares no data-jpa / hibernate / persistence dependency and the classpath has no EntityManager (JDBC only, T-30 decision 10.1)")
    void sin_jpa() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        assertThat(pom).doesNotContain("data-jpa").doesNotContain("hibernate").doesNotContain("persistence-api").doesNotContain("jakarta.persistence");
        assertThat(LoginPgSeleccionTest.class.getClassLoader().getResource("jakarta/persistence/EntityManager.class")).isNull();
    }

    @Test
    @DisplayName("T-30 the HANA default is declared where the application reads it: no application*.properties of the module sets plataforma.db.dialect to anything but hana")
    void defecto_declarado() throws Exception {
        for (String f : List.of("src/main/resources/application.properties", "src/main/resources/application-test.properties")) {
            for (String l : Files.readAllLines(Path.of(f))) {
                if (l.strip().startsWith("plataforma.db.dialect")) {
                    assertThat(l).as(f).containsPattern("plataforma\\.db\\.dialect\\s*=\\s*(\\$\\{[A-Z_]+:)?hana\\}?\\s*$");
                }
            }
        }
    }
}
