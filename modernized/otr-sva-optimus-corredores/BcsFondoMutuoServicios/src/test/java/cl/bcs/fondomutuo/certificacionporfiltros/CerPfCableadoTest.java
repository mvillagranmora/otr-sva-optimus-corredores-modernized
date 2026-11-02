package cl.bcs.fondomutuo.certificacionporfiltros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.fondomutuo.BcsFondoMutuoApplication;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateConfiguracion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescatePort;
import cl.bcs.fondomutuo.kit.Compuertas;

/**
 * F3 W3 T6-A, the WIRING (REAL Spring context, H2, profile test, as {@code CerT4cCableadoTest}): with the DEFAULT configuration the in-process port of CertificarPorFiltros is a bean (ONE), built over the
 * ONE bean of {@code CertificarAporteRescatePort} and the JDBC adapter of the select (ONE bean of the read port); it is NOT served over HTTP: the dispatcher has no handler for the legacy FQN, the allow-list is
 * unchanged, the HTTP entry answers 503 for the class even when the list of Certificar is open, and no list of {@code fondomutuo.solicitud.listas-servidas} names it (an invented name still stops the
 * start-up). The decision of the user: a job of another module calls it in-process; nothing is exposed.
 *
 * <p>RED BEFORE CODE: every test first demands the production wiring class ({@code CertificarPorFiltrosConfiguracion}), so each fails with "T6-A not implemented".
 */
@SpringBootTest
@ActiveProfiles("test")
class CerPfCableadoTest {

    private static final String TOKEN = "test-secret";   // application-test.yml: services.internal-token (a test value)

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired JavaCallDispatcher dispatcher;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter.class),
                ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
    }

    /** The first line of every test: the wiring class of the step must exist (red before code). */
    private static void exigirCableado() {
        ApiT6a.cargar(ApiT6a.CONFIGURACION);
        ApiT6a.cargar(ApiT6a.PUERTO);
    }

    private static String cuerpo(String clase) {
        return "{\"class\":\"" + clase + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_Filtros\":[]}}";
    }

    @Test
    @DisplayName("T6-A wiring: with the default configuration the bean CertificarPorFiltrosPort exists, ONE, and it is the facade over the ONE bean of CertificarAporteRescatePort and the ONE bean of the read port")
    void elPuertoEsUnBean() throws Exception {
        exigirCableado();
        Map<String, ?> puertos = ctx.getBeansOfType(ApiT6a.cargar(ApiT6a.PUERTO));
        assertThat(puertos).hasSize(1);
        Object bean = puertos.values().iterator().next();
        assertThat(bean).isInstanceOf(ApiT6a.cargar(ApiT6a.FACHADA));
        Map<String, ?> datos = ctx.getBeansOfType(ApiT6a.cargar(ApiT6a.DATOS));
        assertThat(datos).hasSize(1);
        assertThat(datos.values().iterator().next()).isInstanceOf(ApiT6a.cargar(ApiT6a.DATOS_JDBC));
        assertThat(ctx.getBeansOfType(CertificarAporteRescatePort.class)).hasSize(1);
        boolean conCertificar = false;
        boolean conDatos = false;
        for (Field f : bean.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType() == CertificarAporteRescatePort.class) {
                assertThat(f.get(bean)).as("the facade of Certificar of the context, not another one").isSameAs(ctx.getBean(CertificarAporteRescatePort.class));
                conCertificar = true;
            }
            if (f.getType() == ApiT6a.cargar(ApiT6a.DATOS)) {
                assertThat(f.get(bean)).as("the JDBC adapter of the context").isSameAs(datos.values().iterator().next());
                conDatos = true;
            }
        }
        assertThat(conCertificar && conDatos).as("the facade holds the two ports it is built over").isTrue();
    }

    @Test
    @DisplayName("T6-A wiring: the wiring is a @Configuration with a @Bean for the port and a @Bean for the read adapter (nothing else is exposed)")
    void laConfiguracionDeclaraLosDosBeans() {
        Class<?> c = ApiT6a.cargar(ApiT6a.CONFIGURACION);
        assertThat(c.isAnnotationPresent(org.springframework.context.annotation.Configuration.class)).isTrue();
        long beans = java.util.Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(org.springframework.context.annotation.Bean.class)).count();
        assertThat(beans).isEqualTo(2);
        assertThat(java.util.Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(org.springframework.context.annotation.Bean.class)).map(m -> m.getReturnType().getName()))
                .containsExactlyInAnyOrder(ApiT6a.PUERTO, ApiT6a.DATOS);
    }

    @Test
    @DisplayName("T6-A wiring: NOT served over HTTP: no handler of the context for the legacy FQN, no FQN of CertificarPorFiltros served, the allow-list unchanged, 503 for the class even with the right token")
    void noSeSirvePorHttp() throws Exception {
        exigirCableado();
        assertThat(dispatcher.buscar(ApiT6a.FQN_LEGACY)).isEmpty();
        assertThat(dispatcher.servidas()).noneMatch(f -> f.contains("PorFiltros"));
        assertThat(ctx.getBeanDefinitionNames()).as("the old stub service and the old controller (cl.bcs.fondomutuo.service.javacall / adapter.in.web) stay outside the context")
                .noneMatch(n -> n.toLowerCase().contains("certificarporfiltrossolicitud")).noneMatch(n -> n.equals("internalJavaCallController"));
        assertThat(JavaCallDispatcher.LISTA_BLANCA).doesNotContain(ApiT6a.FQN_LEGACY).hasSize(13);
        for (JavaCallHandler h : ctx.getBeansOfType(JavaCallHandler.class).values()) {
            assertThat(h.getClass().getPackageName()).as(h.getClass().getName()).isNotEqualTo("cl.bcs.fondomutuo.certificacionporfiltros");
            assertThat(h.fqn()).as(h.getClass().getName()).doesNotContain("PorFiltros");
        }
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content(cuerpo(ApiT6a.FQN_LEGACY))).andExpect(status().isServiceUnavailable());
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content("{\"class\":\"" + ApiT6a.FQN_LEGACY + "\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("T6-A wiring: no list was added: the default Compuertas serve none of them, and a list named after the class stops the start-up as unknown (the lists of Certificar, when OPEN, do not serve it either)")
    void noHayLista_yUnNombreInventadoDetieneElArranque() {
        exigirCableado();
        assertThat(ctx.getBean(Compuertas.class).listasServidas()).noneMatch(l -> l.contains("FILTROS"));
        String base = "--spring.profiles.active=test";
        for (String inventada : new String[] { "CERTIFICAR_POR_FILTROS", "CERTIFICAR_POR_FILTROS_SOLICITUD_APORTE_RESCATE" }) {
            assertThatThrownBy(() -> arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + inventada)).hasStackTraceContaining("unknown list: " + inventada);
        }
        try (ConfigurableApplicationContext abierto = arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + CertificarAporteRescateConfiguracion.LISTA)) {
            assertThat(abierto.getBean(JavaCallDispatcher.class).buscar("cl.bcs.fondomutuo.CertificarSolicitudAporteRescate")).as("Certificar is served with its list open").isPresent();
            assertThat(abierto.getBean(JavaCallDispatcher.class).buscar(ApiT6a.FQN_LEGACY)).as("CertificarPorFiltros is NOT, whatever list is open").isEmpty();
            assertThat(abierto.getBeansOfType(ApiT6a.cargar(ApiT6a.PUERTO))).as("the in-process port is still a bean").hasSize(1);
        }
    }

    private static ConfigurableApplicationContext arrancar(String... args) {
        SpringApplication app = new SpringApplicationBuilder(BcsFondoMutuoApplication.class).web(WebApplicationType.SERVLET).properties("server.port=0").build();
        return app.run(args);
    }
}
