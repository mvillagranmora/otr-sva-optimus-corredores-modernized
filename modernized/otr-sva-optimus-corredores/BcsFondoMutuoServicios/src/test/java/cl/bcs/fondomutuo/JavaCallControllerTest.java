package cl.bcs.fondomutuo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * FM-KIT-140..149: the controller + unit of work + token with TEST handlers registered for the two allow-listed FQN (the real ones arrive in steps 3-4): commit only for
 * counter 0 (CallServlet rule), 503 of a closed gate rolls back, generic 500 with correlationId (DEV-F3), 400, JSON in/out untouched, no state between requests (DEV-F2).
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")   // 4A: the test handler replaces the real "normalJavaCallHandler" (same FQN)
@ActiveProfiles("test")
@Import(JavaCallControllerTest.Manejadores.class)
class JavaCallControllerTest {

    static final String T = "\"BSCL.Plataforma.BD::REG.Ciudad\"";

    @TestConfiguration
    static class Manejadores {
        /** params.modo: ok0 | ok1 (counter 1) | sinContador | noServido (a gate) | boom (exception with a text that must never leave). Writes ONE row first. */
        @Bean
        JavaCallHandler normalJavaCallHandler() {
            return new JavaCallHandler() {
                public String fqn() {
                    return JavaCallDispatcher.CREAR_SOLICITUD_NORMAL;
                }

                public JsonObject call(Connection con, JsonObject jin) throws Exception {
                    try (var st = con.createStatement()) {
                        st.execute("INSERT INTO " + T + " VALUES (" + jin.get("id").getAsLong() + ", '" + jin.get("n").getAsString() + "')");
                    }
                    String modo = jin.get("modo").getAsString();
                    JsonObject o = new JsonObject();
                    switch (modo) {
                        case "noServido" -> throw new ServicioNoServidoException("G1", "closed");
                        case "boom" -> throw new IllegalStateException("secret detail SELECT * FROM X");
                        case "sinContador" -> o.addProperty("otro", 1);
                        case "ok1" -> o.addProperty("P_OUT_ERRORESCONTADOR", 1);
                        default -> o.addProperty("P_OUT_ERRORESCONTADOR", 0);
                    }
                    o.addProperty("eco", jin.get("n").getAsString());
                    return o;
                }
            };
        }
    }

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired DataSource ds;
    @Autowired JavaCallDispatcher dispatcher;
    MockMvc mvc;

    @BeforeEach
    void preparar() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter.class), ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
        try (Connection c = ds.getConnection()) {
            c.createStatement().execute("DROP ALL OBJECTS");
            ScriptUtils.executeSqlScript(c, new ClassPathResource("fondomutuo-kit/schema-kit.sql"));
        }
    }

    private org.springframework.test.web.servlet.ResultActions llamar(String modo, String n) throws Exception {
        return mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"" + JavaCallDispatcher.CREAR_SOLICITUD_NORMAL + "\",\"params\":{\"modo\":\"" + modo + "\",\"n\":\"" + n + "\",\"id\":" + Math.abs(n.hashCode()) + "}}"));
    }

    private long filas() {
        return TestBd.contar(ds, T);
    }

    @Test
    void fmKit140_soloElFqnConHandlerSeSirve_elOtroDeLaListaSigueEn503() throws Exception {
        // 4B: the real Manejador handler is registered next to the test handler of Normal; a class outside the allow-list stays 503
        // F2 W1 front 2: RescateReinversion has a handler too, but its list is not configured as served: it still answers 503 (below)
        assertThat(dispatcher.servidas()).containsExactlyInAnyOrder(JavaCallDispatcher.CREAR_SOLICITUD_NORMAL, JavaCallDispatcher.MANEJADOR,
                JavaCallDispatcher.CREAR_SOLICITUD_RESCATE_REINVERSION, JavaCallDispatcher.CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE,   // F3 W1 front 1: also with a handler, list closed
                JavaCallDispatcher.CONSULTAR_VALOR_DOLAR_FONDO_MUTUO, JavaCallDispatcher.CONSULTAR_FONDO_MUTUO_REINVERSION, JavaCallDispatcher.CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS,   // F3 W1 VDO, FMR, IFM
                JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM, JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM_WS, JavaCallDispatcher.CONSULTAR_DETALLE_SOLICITUD,
                JavaCallDispatcher.CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO,   // F3 W1 SFM, WS, DET, CPF
                JavaCallDispatcher.CARGAR_VALOR_CUOTA);   // F3 W2 CVC: handler present (writes), list closed
        assertThat(mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"cl.bcs.fondomutuo.CrearSolicitudRescateReinversion\"}")).andReturn().getResponse().getStatus()).isEqualTo(503);
        assertThat(mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate\"}")).andReturn().getResponse().getStatus()).isEqualTo(503);
    }

    @Test
    void fmKit141_contadorCero_200ConElJsonDelHandler_yCommit() throws Exception {
        String cuerpo = llamar("ok0", "A").andReturn().getResponse().getContentAsString();
        assertThat(cuerpo).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"eco\":\"A\"}");
        assertThat(filas()).isEqualTo(1);
    }

    @Test
    void fmKit142_contadorUno_200ConElCuerpo_peroRollbackTotal() throws Exception {
        var r = llamar("ok1", "A").andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(r.getContentAsString()).contains("\"P_OUT_ERRORESCONTADOR\":1");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit143_sinContador_rollback() throws Exception {
        assertThat(llamar("sinContador", "A").andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit144_compuertaCerrada_503SinDetalle_yRollback() throws Exception {
        var r = llamar("noServido", "A").andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(503);
        assertThat(r.getContentAsString()).doesNotContain("G1").doesNotContain("closed").doesNotContain("correlationId");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit145_excepcion_500GenericoConCorrelationId_sinEcoDelTextoNiTraza_yRollback_DEVF3() throws Exception {
        var r = llamar("boom", "A").andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(500);
        JsonObject o = com.google.gson.JsonParser.parseString(r.getContentAsString()).getAsJsonObject();
        assertThat(o.get("error").getAsString()).isEqualTo("internal error");
        assertThat(java.util.UUID.fromString(o.get("correlationId").getAsString())).isNotNull();
        assertThat(r.getContentAsString()).doesNotContain("secret").doesNotContain("SELECT").doesNotContain("IllegalState").doesNotContain("at cl.bcs");
        assertThat(filas()).isZero();
    }

    @Test
    void fmKit146_cadaError500TrameSuPropioCorrelationId() throws Exception {
        String a = llamar("boom", "A").andReturn().getResponse().getContentAsString();
        String b = llamar("boom", "B").andReturn().getResponse().getContentAsString();
        assertThat(com.google.gson.JsonParser.parseString(a).getAsJsonObject().get("correlationId"))
                .isNotEqualTo(com.google.gson.JsonParser.parseString(b).getAsJsonObject().get("correlationId"));
    }

    @Test
    void fmKit147_sinEstadoCompartido_16PeticionesEnParalelo_cadaUnaConSuTransaccion() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                final int k = i;
                Callable<Integer> c = () -> llamar(k % 2 == 0 ? "ok0" : "ok1", "P" + k).andReturn().getResponse().getStatus();
                fs.add(pool.submit(c));
            }
            for (Future<Integer> f : fs) assertThat(f.get()).isEqualTo(200);
        } finally {
            pool.shutdownNow();
        }
        assertThat(filas()).as("only the 8 counter-0 requests committed").isEqualTo(8);
    }

    @Test
    void fmKit148_paramsNoObjeto_400_ySinClase_400() throws Exception {
        assertThat(mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"" + JavaCallDispatcher.CREAR_SOLICITUD_NORMAL + "\",\"params\":5}")).andReturn().getResponse().getStatus()).isEqualTo(400);
        assertThat(mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"params\":{}}")).andReturn().getResponse().getStatus()).isEqualTo(400);
    }
}
