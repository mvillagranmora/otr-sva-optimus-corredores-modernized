package cl.bcs.fondomutuo.liquidacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

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

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.BcsFondoMutuoApplication;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.certificacion.CrearCargoAbonoPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.manejador.TipoNoServido;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;

/**
 * F3 W3 T6-B, the WIRING (REAL Spring context, H2, profile test, as {@code CerT4cCableadoTest}): with the DEFAULT configuration the port of Liquidacion is a bean (ONE, the facade, over the ONE bean of every port it
 * uses), the JavaCall handler is registered for the legacy FQN and answers 503 (the list is closed: ServicioNoServidoException G0, which the controller maps to 503), the three collaborators outside the module are the
 * beans that ALREADY exist (the fail-closed CrearCargoAbono of Certificar's wiring, the Tesoreria service in process, the Log port): this step adds NO second bean of them, the allow-list has exactly 14 FQN,
 * the validator of {@code fondomutuo.solicitud.listas-servidas} accepts the new list and still stops the start-up on an unknown name.
 *
 * <p>RED BEFORE CODE: every test first demands the production wiring class ({@code LiquidacionConfiguracion}), so each fails with "T6-B not implemented".
 */
@SpringBootTest
@ActiveProfiles("test")
class LiqCableadoTest {

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
        ApiT6b.cargar(ApiT6b.CONFIGURACION);
        ApiT6b.cargar(ApiT6b.HANDLER);
    }

    private static String cuerpo(String clase) {
        return "{\"class\":\"" + clase + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_Movimientos\":[],\"p_Filtros\":[]}}";
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the port, the adapter, the handler
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B wiring: the bean LiquidacionPort exists, ONE, and it is the facade over the ONE bean of the data adapter and of each port it uses (the sequences, CrearCargoAbono, the Log)")
    void elPuertoEsUnBean() throws Exception {
        exigirCableado();
        Map<String, ?> puertos = ctx.getBeansOfType(ApiT6b.cargar(ApiT6b.PUERTO));
        assertThat(puertos).hasSize(1);
        Object bean = puertos.values().iterator().next();
        assertThat(bean).isInstanceOf(ApiT6b.cargar(ApiT6b.FACHADA));
        Map<String, ?> datos = ctx.getBeansOfType(ApiT6b.cargar(ApiT6b.DATOS));
        assertThat(datos).hasSize(1);
        assertThat(datos.values().iterator().next()).isInstanceOf(ApiT6b.cargar(ApiT6b.DATOS_JDBC));
        int ports = 0;
        for (Field f : bean.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType() == ApiT6b.cargar(ApiT6b.DATOS)) {
                assertThat(f.get(bean)).as("the JDBC adapter of the context").isSameAs(datos.values().iterator().next());
                ports++;
            } else if (f.getType() == SecuenciaPort.class) {
                assertThat(f.get(bean)).as("the sequence port of the context").isSameAs(ctx.getBean(SecuenciaPort.class));
                ports++;
            } else if (f.getType() == CrearCargoAbonoPort.class) {
                assertThat(f.get(bean)).as("the CrearCargoAbono port of the context (Certificar's fail-closed one)").isSameAs(ctx.getBean(CrearCargoAbonoPort.class));
                ports++;
            } else if (f.getType() == LogPort.class) {
                assertThat(f.get(bean)).as("the Log port of the context").isSameAs(ctx.getBean(LogPort.class));
                ports++;
            }
        }
        assertThat(ports).as("the facade holds the data adapter, the sequences, CrearCargoAbono and the Log").isEqualTo(4);
    }

    @Test
    @DisplayName("T6-B wiring: JavaCallDispatcher.buscar(FQN) finds the handler; allow-list of exactly 14 FQN; the new class is not a read class")
    void elDispatcherLoEncuentra() {
        exigirCableado();
        String fqn = ApiT6b.fqnDelDispatcher();
        assertThat(fqn).isEqualTo("cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate");
        JavaCallHandler h = dispatcher.buscar(fqn).orElseThrow(() -> new AssertionError("no handler registered for " + fqn));
        assertThat(h).isInstanceOf(ApiT6b.cargar(ApiT6b.HANDLER));
        assertThat(h.fqn()).isEqualTo(fqn);
        assertThat(dispatcher.servidas()).contains(fqn).isSubsetOf(JavaCallDispatcher.LISTA_BLANCA);
        assertThat(JavaCallDispatcher.LISTA_BLANCA).hasSize(14).contains(fqn);
        assertThat(JavaCallDispatcher.CLASES_LECTURA).doesNotContain(fqn).hasSize(8);
    }

    @Test
    @DisplayName("T6-B wiring: list closed by default => the handler of the context raises ServicioNoServidoException G0 before anything is read, and the HTTP entry answers 503 for the class")
    void cerradaPorDefecto_503() throws Exception {
        exigirCableado();
        assertThat(ctx.getBean(Compuertas.class).listasServidas()).doesNotContain(ApiT6b.lista());
        JavaCallHandler h = dispatcher.buscar(ApiT6b.fqnDelDispatcher()).orElseThrow(() -> new AssertionError("no handler"));
        assertThatThrownBy(() -> h.call(null, null)).isInstanceOfSatisfying(ServicioNoServidoException.class, e -> assertThat(e.compuerta()).isEqualTo("G0"));
        assertThatThrownBy(() -> h.call(null, new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content(cuerpo(ApiT6b.FQN_LEGACY))).andExpect(status().isServiceUnavailable());
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content("{\"class\":\"" + ApiT6b.FQN_LEGACY + "\"}")).andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("T6-B wiring: the list of Certificar does not open Liquidacion and the list of Liquidacion does not open Certificar (each class has its own list)")
    void cadaClaseTieneSuLista() {
        exigirCableado();
        String base = "--spring.profiles.active=test";
        try (ConfigurableApplicationContext solo = arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + ApiT6b.lista())) {
            assertThat(solo.getBean(JavaCallDispatcher.class).buscar(ApiT6b.FQN_LEGACY)).isPresent();
            assertThat(solo.getBean(Compuertas.class).listasServidas()).contains(ApiT6b.lista()).doesNotContain("CERTIFICAR_SOLICITUD_APORTE_RESCATE");
            JavaCallHandler certificar = solo.getBean(JavaCallDispatcher.class).buscar("cl.bcs.fondomutuo.CertificarSolicitudAporteRescate").orElseThrow();
            assertThatThrownBy(() -> certificar.call(null, null)).isInstanceOf(ServicioNoServidoException.class);
        }
        try (ConfigurableApplicationContext solo = arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL,CERTIFICAR_SOLICITUD_APORTE_RESCATE")) {
            JavaCallHandler liquidacion = solo.getBean(JavaCallDispatcher.class).buscar(ApiT6b.FQN_LEGACY).orElseThrow();
            assertThatThrownBy(() -> liquidacion.call(null, null)).isInstanceOfSatisfying(ServicioNoServidoException.class, e -> assertThat(e.compuerta()).isEqualTo("G0"));
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the collaborators: the beans that already exist, no second one
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B wiring: CrearCargoAbono, the Log and Tesoreria in process are the beans that ALREADY exist (ONE each: this step adds none) and CrearCargoAbono still FAILS CLOSED with ServicioNoServidoException")
    void losColaboradoresSonLosQueYaExisten() throws Exception {
        exigirCableado();
        assertThat(ctx.getBeansOfType(CrearCargoAbonoPort.class)).hasSize(1);
        assertThat(ctx.getBeansOfType(LogPort.class)).hasSize(1);
        assertThat(ctx.getBeansOfType(CrearIngresoEgresoService.class)).hasSize(1);
        assertThat(ctx.getBeansOfType(SecuenciaPort.class)).hasSize(1);
        CrearCargoAbonoPort cargoAbono = ctx.getBean(CrearCargoAbonoPort.class);
        assertThatThrownBy(() -> cargoAbono.crear(new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
        assertThatThrownBy(() -> cargoAbono.crear(null)).isInstanceOf(ServicioNoServidoException.class);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the Manejador validator
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static Map<Lista, TipoSolicitudPort> destinos() {
        Map<Lista, TipoSolicitudPort> d = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) if (l != Lista.NORMAL) d.put(l, new TipoNoServido(l.name()));
        // as in production: RESCATE_REINVERSION has its destination (REINVERSION_APORTE stays unserved)
        d.put(Lista.RESCATE_REINVERSION, new cl.bcs.fondomutuo.rescatereinversion.RescateReinversionDestino(in -> {
            throw new AssertionError("not called at start-up");
        }));
        return d;
    }

    /** {@code ManejadorConfiguracion.validarListasServidas(Set, Map, boolean)} (package-private): a rejection comes out as the IllegalStateException it raises. */
    private static void validar(Set<String> listas, boolean custodia) throws Exception {
        Method m = ManejadorConfiguracion.class.getDeclaredMethod("validarListasServidas", Set.class, Map.class, boolean.class);
        m.setAccessible(true);
        try {
            m.invoke(null, listas, destinos(), custodia);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    @Test
    @DisplayName("T6-B validator: LIQUIDACION_SOLICITUD_APORTE_RESCATE is accepted alone and next to NORMAL / the UI lists / Certificar / RESCATE_REINVERSION with the custody closed; an unknown name still stops the start-up")
    void elValidadorLaAcepta_yUnNombreDesconocidoSigueFallando() {
        ApiT6b.lista();   // red before code: the constant must exist
        String nueva = ApiT6b.lista();
        assertThatCode(() -> validar(Set.of(nueva), false)).doesNotThrowAnyException();
        assertThatCode(() -> validar(Set.of("NORMAL", nueva), false)).doesNotThrowAnyException();
        assertThatCode(() -> validar(Set.of("NORMAL", nueva, "CARGAR_VALOR_CUOTA", "CERTIFICAR_SOLICITUD_APORTE_RESCATE", "CONSULTAR_FECHA_SOLICITUD"), false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> validar(Set.of(nueva, "TYPO_T6B"), false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown list: TYPO_T6B");
        assertThatThrownBy(() -> validar(Set.of(nueva + "_X"), false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown list");
        // like CARGAR_VALOR_CUOTA it is not "another list of the Manejador" for the rule RESCATE_REINVERSION + custody closed
        assertThatCode(() -> validar(Set.of("RESCATE_REINVERSION", nueva), false)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("T6-B start-up: the property naming the new list starts the context (handler served by configuration), an unknown name stops the start-up")
    void arranqueConLaListaAbierta_yConUnNombreDesconocido() {
        exigirCableado();
        String base = "--spring.profiles.active=test";
        try (ConfigurableApplicationContext abierto = arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + ApiT6b.lista())) {
            assertThat(abierto.getBean(Compuertas.class).listasServidas()).contains("NORMAL", ApiT6b.lista());
            assertThat(abierto.getBean(JavaCallDispatcher.class).buscar(ApiT6b.FQN_LEGACY)).isPresent();
            assertThat(abierto.getBeansOfType(ApiT6b.cargar(ApiT6b.PUERTO))).hasSize(1);
        }
        assertThatThrownBy(() -> arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + ApiT6b.lista() + ",TYPO_T6B")).hasStackTraceContaining("unknown list: TYPO_T6B");
    }

    private static ConfigurableApplicationContext arrancar(String... args) {
        SpringApplication app = new SpringApplicationBuilder(BcsFondoMutuoApplication.class).web(WebApplicationType.SERVLET).properties("server.port=0").build();
        return app.run(args);
    }
}
