package cl.bcs.fondomutuo.certificacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
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
import cl.bcs.fondomutuo.aportereinversion.AporteReinversionPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.manejador.TipoNoServido;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;

/**
 * F3 W3 T4-C, the WIRING (REAL Spring context, H2, profile test, as {@code FondoMutuoCableadoRealTest}): with the DEFAULT configuration the facade of Certificar is a bean, the JavaCall handler is
 * registered for the legacy FQN and answers 503 (the list is closed: ServicioNoServidoException G0, which the controller maps to 503), the ports with no production implementation are replacements that FAIL
 * CLOSED with ServicioNoServidoException, the allow-list has exactly 13 FQN, the validator of {@code fondomutuo.solicitud.listas-servidas} accepts the new list and still stops the start-up on an unknown
 * name, and the AR is a bean WITHOUT a handler, behind the adapter (decision of the user: no JavaCall handler of the AR, no Manejador destination REINVERSION_APORTE).
 *
 * <p>RED BEFORE CODE: every test first demands the production wiring class ({@code CertificarAporteRescateConfiguracion}), so each fails with "T4-C not implemented". What is NOT pinned here (it needs a
 * seeded database): a request that gets past the early exits of F-A / F-B with the list OPEN and the default gates (it answers 503 at G8, covered by {@code CerT5CompuertaTest} on the facade itself).
 */
@SpringBootTest
@ActiveProfiles("test")
class CerT4cCableadoTest {

    private static final String TOKEN = "test-secret";   // application-test.yml: services.internal-token (a test value)

    /** Ports of Certificar with no production implementation: the wiring must give them a replacement that raises ServicioNoServidoException (G0 style), never a null and never a silent success. */
    private static final String PAQUETE = ApiT4c.PAQUETE_CER;
    private static final List<String> PUERTOS_SIN_IMPLEMENTACION = List.of(PAQUETE + "ProcesarCustodiaFondoMutuoPort", PAQUETE + "CalculaCustodiaRescatePort", PAQUETE + "CrearCargoAbonoPort",
            PAQUETE + "FacturacionPort", PAQUETE + "NotificacionCertificacionPort", PAQUETE + "NotificacionOrdenesOwpPort");

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
        ApiT4c.cargar(ApiT4c.CONFIGURACION);
        ApiT4c.cargar(ApiT4c.HANDLER);
    }

    private static String cuerpo(String clase) {
        return "{\"class\":\"" + clase + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdUsuario\":97}}";
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the facade and the handler
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C wiring: with the default configuration the bean CertificarAporteRescatePort exists, ONE, and it is the facade of T5 (all the fronts A..J)")
    void laFachadaEsUnBean() {
        exigirCableado();
        assertThat(ctx.getBeansOfType(CertificarAporteRescatePort.class)).hasSize(1);
        assertThat(ctx.getBean(CertificarAporteRescatePort.class)).isInstanceOf(ApiT4c.cargar(ApiT4c.FACHADA));
    }

    @Test
    @DisplayName("T4-C wiring: JavaCallDispatcher.buscar(FQN) finds the handler; allow-list of exactly 13 FQN; the new class is not a read class")
    void elDispatcherLoEncuentra() {
        exigirCableado();
        String fqn = ApiT4c.fqnDelDispatcher();
        assertThat(fqn).isEqualTo("cl.bcs.fondomutuo.CertificarSolicitudAporteRescate");
        JavaCallHandler h = dispatcher.buscar(fqn).orElseThrow(() -> new AssertionError("no handler registered for " + fqn));
        assertThat(h).isInstanceOf(ApiT4c.cargar(ApiT4c.HANDLER));
        assertThat(h.fqn()).isEqualTo(fqn);
        assertThat(dispatcher.servidas()).contains(fqn).isSubsetOf(JavaCallDispatcher.LISTA_BLANCA);
        assertThat(JavaCallDispatcher.LISTA_BLANCA).hasSize(13).contains(fqn);
        assertThat(JavaCallDispatcher.CLASES_LECTURA).doesNotContain(fqn).hasSize(8);
    }

    @Test
    @DisplayName("T4-C wiring: list closed by default => the handler of the context raises ServicioNoServidoException G0 before anything is read, and the HTTP entry answers 503 for the class")
    void cerradaPorDefecto_503() throws Exception {
        exigirCableado();
        assertThat(ctx.getBean(Compuertas.class).listasServidas()).doesNotContain(ApiT4c.lista());
        JavaCallHandler h = dispatcher.buscar(ApiT4c.fqnDelDispatcher()).orElseThrow(() -> new AssertionError("no handler"));
        assertThatThrownBy(() -> h.call(null, null)).isInstanceOfSatisfying(ServicioNoServidoException.class, e -> assertThat(e.compuerta()).isEqualTo("G0"));
        assertThatThrownBy(() -> h.call(null, new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content(cuerpo(ApiT4c.FQN_LEGACY))).andExpect(status().isServiceUnavailable());
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content("{\"class\":\"" + ApiT4c.FQN_LEGACY + "\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // fail-closed replacements
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C wiring: the six ports with no production implementation are beans that FAIL CLOSED (every method raises ServicioNoServidoException, before reading its argument)")
    void losPuertosSinImplementacionFallanCerrado() throws Exception {
        exigirCableado();
        for (String nombre : PUERTOS_SIN_IMPLEMENTACION) {
            Class<?> puerto = ApiT4c.cargar(nombre);
            Map<String, ?> beans = ctx.getBeansOfType(puerto);
            assertThat(beans).as("beans of " + puerto.getSimpleName()).hasSize(1);
            Object bean = beans.values().iterator().next();
            Method[] metodos = puerto.getMethods();
            assertThat(metodos).as(puerto.getSimpleName() + " methods").isNotEmpty();
            for (Method m : metodos) {
                Object[] args = new Object[m.getParameterCount()];
                for (int i = 0; i < args.length; i++) {
                    Class<?> t = m.getParameterTypes()[i];
                    args[i] = t == JsonObject.class ? new JsonObject() : t == List.class ? List.of() : null;
                }
                try {
                    m.invoke(bean, args);
                    throw new AssertionError(puerto.getSimpleName() + "." + m.getName() + " answered instead of failing closed");
                } catch (InvocationTargetException e) {
                    assertThat(e.getCause()).as(puerto.getSimpleName() + "." + m.getName()).isInstanceOf(ServicioNoServidoException.class);
                }
            }
        }
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
    @DisplayName("T4-C validator: CERTIFICAR_SOLICITUD_APORTE_RESCATE is accepted alone and next to NORMAL / the UI lists / RESCATE_REINVERSION with the custody closed; an unknown name still stops the start-up")
    void elValidadorLaAcepta_yUnNombreDesconocidoSigueFallando() {
        ApiT4c.lista();   // red before code: the constant must exist
        String nueva = ApiT4c.lista();
        assertThatCode(() -> validar(Set.of(nueva), false)).doesNotThrowAnyException();
        assertThatCode(() -> validar(Set.of("NORMAL", nueva), false)).doesNotThrowAnyException();
        assertThatCode(() -> validar(Set.of("NORMAL", nueva, "CARGAR_VALOR_CUOTA", "CONSULTAR_FECHA_SOLICITUD"), false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> validar(Set.of(nueva, "TYPO_T4C"), false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown list: TYPO_T4C");
        assertThatThrownBy(() -> validar(Set.of(nueva + "_X"), false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown list");
        // like CARGAR_VALOR_CUOTA it is not "another list of the Manejador" for the rule RESCATE_REINVERSION + custody closed
        assertThatCode(() -> validar(Set.of("RESCATE_REINVERSION", nueva), false)).doesNotThrowAnyException();
        // no Manejador destination REINVERSION_APORTE (decision of the user, deferred): naming it still stops the start-up
        assertThatThrownBy(() -> validar(Set.of("REINVERSION_APORTE"), false)).isInstanceOf(IllegalStateException.class).hasMessageContaining("no handler");
    }

    @Test
    @DisplayName("T4-C start-up: the property naming the new list starts the context (handler served by configuration), an unknown name stops the start-up")
    void arranqueConLaListaAbierta_yConUnNombreDesconocido() {
        exigirCableado();
        String base = "--spring.profiles.active=test";
        try (ConfigurableApplicationContext abierto = arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + ApiT4c.lista())) {
            assertThat(abierto.getBean(Compuertas.class).listasServidas()).contains("NORMAL", ApiT4c.lista());
            assertThat(abierto.getBean(JavaCallDispatcher.class).buscar(ApiT4c.FQN_LEGACY)).isPresent();
        }
        assertThatThrownBy(() -> arrancar(base, "--fondomutuo.solicitud.listas-servidas=NORMAL," + ApiT4c.lista() + ",TYPO_T4C")).hasStackTraceContaining("unknown list: TYPO_T4C");
    }

    private static ConfigurableApplicationContext arrancar(String... args) {
        SpringApplication app = new SpringApplicationBuilder(BcsFondoMutuoApplication.class).web(WebApplicationType.SERVLET).properties("server.port=0").build();
        return app.run(args);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the AR: a bean, behind the adapter, with NO handler
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C wiring AR: the bean AporteReinversionPort exists and Certificar gets the JSON adapter as its CrearAporteReinversionPort")
    void elArEsUnBean_yCertificarRecibeElAdaptador() {
        exigirCableado();
        ApiT4c.cargar(ApiT4c.CONFIGURACION_AR);
        assertThat(ctx.getBeansOfType(AporteReinversionPort.class)).hasSize(1);
        Map<String, CrearAporteReinversionPort> json = ctx.getBeansOfType(CrearAporteReinversionPort.class);
        assertThat(json).hasSize(1);
        assertThat(json.values().iterator().next()).isInstanceOf(ApiT4c.cargar(ApiT4c.ADAPTADOR));
    }

    @Test
    @DisplayName("T4-C wiring AR: NO JavaCall handler of the AR (0 direct uses): no FQN of it in the allow-list, no handler of its package, 503 for its legacy class")
    void elArNoTieneHandler() throws Exception {
        exigirCableado();
        ApiT4c.cargar(ApiT4c.CONFIGURACION_AR);
        assertThat(JavaCallDispatcher.LISTA_BLANCA).noneMatch(f -> f.contains("AporteReinversion") || f.contains("ReinversionAporte"));
        assertThat(dispatcher.servidas()).noneMatch(f -> f.contains("AporteReinversion"));
        for (JavaCallHandler h : ctx.getBeansOfType(JavaCallHandler.class).values()) {
            assertThat(h.getClass().getPackageName()).as(h.getClass().getName()).isNotEqualTo("cl.bcs.fondomutuo.aportereinversion");
        }
        assertThat(dispatcher.buscar(ApiT4c.FQN_LEGACY_AR)).isEmpty();
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content(cuerpo(ApiT4c.FQN_LEGACY_AR))).andExpect(status().isServiceUnavailable());
    }
}
