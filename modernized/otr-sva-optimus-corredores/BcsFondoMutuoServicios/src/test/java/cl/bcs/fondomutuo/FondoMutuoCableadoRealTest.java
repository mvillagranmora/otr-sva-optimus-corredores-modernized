package cl.bcs.fondomutuo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.kit.Compuertas;

/**
 * FM-KIT-120..134: REAL Spring context (H2, profile test) with the real filters. D-4/DEV-F1: none of the ~130 stubs is a bean; the allow-list is the 2 FQN of the plan
 * and, in this step (kit), they have no handler yet, so EVERY class answers 503 (never the 200 with invented data of the old skeleton); token, 400 and the removed
 * old endpoints; the transaction manager of C1/C1b.
 */
@SpringBootTest
@ActiveProfiles("test")
class FondoMutuoCableadoRealTest {

    static final String TOKEN = "test-secret";

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired JavaCallDispatcher dispatcher;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter.class), ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
    }

    private static String cuerpo(String clase) {
        return "{\"class\":\"" + clase + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdUsuario\":97}}";
    }

    private static List<Class<?>> stubs(String paquete) throws Exception {
        ClassPathScanningCandidateComponentProvider p = new ClassPathScanningCandidateComponentProvider(false);
        p.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        List<Class<?>> r = new ArrayList<>();
        for (var bd : p.findCandidateComponents(paquete)) r.add(Class.forName(bd.getBeanClassName()));
        return r;
    }

    @Test
    void fmKit120_ningunStubEstaEnElContexto_D4_DEVF1() throws Exception {
        List<Class<?>> javacall = stubs("cl.bcs.fondomutuo.service.javacall");
        assertThat(javacall).as("the 132 skeleton services are still in the tree (kept, not deleted)").hasSize(132);
        for (Class<?> c : javacall) assertThat(ctx.getBeansOfType(c)).as(c.getName()).isEmpty();
        for (Class<?> c : stubs("cl.bcs.fondomutuo.service")) assertThat(ctx.getBeansOfType(c)).as(c.getName()).isEmpty();
        Pattern stubs = Pattern.compile(BcsFondoMutuoApplication.STUBS);
        List<String> registrados = new ArrayList<>();
        for (String n : ctx.getBeanNamesForType(Object.class)) {
            Class<?> t = ctx.getType(n);
            if (t != null && stubs.matcher(t.getName()).matches()) registrados.add(n + " -> " + t.getName());
        }
        assertThat(registrados).as("beans of the excluded packages").isEmpty();
    }

    @Test
    void fmKit121_laListaBlancaSonLos2Fqn_yEnEstePasoNingunoTieneHandler() {
        assertThat(JavaCallDispatcher.LISTA_BLANCA).hasSize(12);   // F3 W2: + CargarValorCuota (writes, list closed); F2 W1 front 2: + CrearSolicitudRescateReinversion; F3 W1 front 1: + ConsultarFechaSolicitudAporteRescate; F3 W1 VDO, FMR, IFM, SFM, WS, DET, CPF (all closed by configuration)
        // step 4B: the Manejador handler (real) is registered; step 4A registers Normal's. Nothing outside the allow-list is ever served.
        assertThat(dispatcher.servidas()).contains(JavaCallDispatcher.MANEJADOR).isSubsetOf(JavaCallDispatcher.LISTA_BLANCA);
    }

    @Test
    void fmKit122_todaClase_incluidosLos132StubsYLosDosDeLaLista_503_nuncaEl200DelStub() throws Exception {
        List<String> clases = new ArrayList<>();
        for (Class<?> c : stubs("cl.bcs.fondomutuo.service.javacall")) clases.add("cl.bcs.fondomutuo." + c.getSimpleName().replaceAll("Service$", ""));
        // 4B: the Manejador is served now (FM-MAN tests); a class of the allow-list is 503 here only while it has no handler (4A: Normal)
        if (!dispatcher.servidas().contains(JavaCallDispatcher.CREAR_SOLICITUD_NORMAL)) clases.add(JavaCallDispatcher.CREAR_SOLICITUD_NORMAL);
        clases.add("cl.bcs.plataforma.mer.CalcularParidad");
        clases.add("x.Y");
        clases.removeAll(dispatcher.servidas());   // 4A/4B: a stub whose FQN is now served by a real handler is not a 503 any more (FM-NOR / FM-MAN tests cover it)
        // F2 W1 front 2: RescateReinversion HAS a handler but its list is closed by the shipped configuration: it must still answer 503
        clases.add(JavaCallDispatcher.CREAR_SOLICITUD_RESCATE_REINVERSION);
        // F3 W1 front 1: ConsultarFechaSolicitudAporteRescate HAS a handler too and its list is closed by the shipped configuration: 503
        clases.add(JavaCallDispatcher.CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE);
        // F3 W1 VDO, FMR, IFM: the same (handler present, list closed by the shipped configuration)
        clases.add(JavaCallDispatcher.CONSULTAR_VALOR_DOLAR_FONDO_MUTUO);
        clases.add(JavaCallDispatcher.CONSULTAR_FONDO_MUTUO_REINVERSION);
        clases.add(JavaCallDispatcher.CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS);
        // F3 W1 SFM, WS, DET: the same
        clases.add(JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM);
        clases.add(JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM_WS);
        clases.add(JavaCallDispatcher.CONSULTAR_DETALLE_SOLICITUD);
        // F3 W1 CPF: the same (handler present, list closed; with the list open and the custody closed it is a 503 too)
        clases.add(JavaCallDispatcher.CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO);
        // F3 W2 CVC: handler present, list closed by the shipped configuration (G0 503 before anything is read or written)
        clases.add(JavaCallDispatcher.CARGAR_VALOR_CUOTA);
        assertThat(clases).hasSizeGreaterThanOrEqualTo(132);   // 130 stubs + CalcularParidad + x.Y at least
        for (String c : clases) {
            mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content(cuerpo(c)))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Test
    void fmKit123_sinTokenOTokenMaloOVariantesDeRuta_401() throws Exception {
        mvc.perform(post("/internal/java-call").contentType("application/json").content(cuerpo(JavaCallDispatcher.MANEJADOR))).andExpect(status().isUnauthorized());
        for (String ruta : new String[] {"//internal/java-call", "/internal;x=1/java-call", "/internal/%6Aava-call", "/x/../internal/java-call"}) {
            mvc.perform(post(ruta).header("X-Internal-Token", "malo").contentType("application/json").content(cuerpo(JavaCallDispatcher.MANEJADOR)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void fmKit124_jsonMalo_400() throws Exception {
        for (String c : new String[] {"[1]", "{}", "no", "{\"class\":\"x.Y\",\"params\":[1]}"}) {
            mvc.perform(post("/internal/java-call").header("X-Internal-Token", TOKEN).contentType("application/json").content(c)).andExpect(status().isBadRequest());
        }
    }

    @Test
    void fmKit125_losEndpointsTipadosDelEsqueletoYaNoExisten_404ConToken_401SinToken() throws Exception {
        mvc.perform(post("/api/fondo-mutuo/solicitud/crear").header("X-Internal-Token", TOKEN).contentType("application/json").content("{}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/fondo-mutuo/calcular-cuota").header("X-Internal-Token", TOKEN).contentType("application/json").content("{}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/fondo-mutuo/solicitud/crear").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void fmKit126_salud_sinToken_200() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }

    @Test
    void fmKit127_c1_c1b_unSoloGestorDeTransaccionesSobreElDataSource_conLaBanderaDeTesoreria() {
        assertThat(ctx.getBeansOfType(PlatformTransactionManager.class)).hasSize(1);
        PlatformTransactionManager tm = ctx.getBean(PlatformTransactionManager.class);
        assertThat(tm).isInstanceOf(DataSourceTransactionManager.class);
        assertThat(((DataSourceTransactionManager) tm).isGlobalRollbackOnParticipationFailure()).isFalse();
        assertThat(((DataSourceTransactionManager) tm).getDataSource()).isSameAs(ctx.getBean(javax.sql.DataSource.class));
    }

    @Test
    void fmKit128_compuertas_deConfiguracion_soloNormalEstaServidaYTodoLoDemasCerrado() {
        Compuertas g = ctx.getBean(Compuertas.class);
        assertThat(g.listasServidas()).containsExactly("NORMAL");
        // F2 W1: G1 lifted by configuration (fondomutuo.tesoreria.habilitada=true): Tesoreria runs in process in the transaction of the request
        org.assertj.core.api.Assertions.assertThatCode(g::g1MedioDistintoDeCin).doesNotThrowAnyException();
        org.assertj.core.api.Assertions.assertThatThrownBy(g::g2Spot).isInstanceOf(cl.bcs.fondomutuo.kit.ServicioNoServidoException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(g::g3SaldoFlujoCaja).isInstanceOf(cl.bcs.fondomutuo.kit.ServicioNoServidoException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(g::g4Owp).isInstanceOf(cl.bcs.fondomutuo.kit.ServicioNoServidoException.class);
    }
}
