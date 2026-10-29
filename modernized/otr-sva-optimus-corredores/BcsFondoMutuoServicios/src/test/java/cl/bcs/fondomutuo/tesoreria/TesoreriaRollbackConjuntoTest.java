package cl.bcs.fondomutuo.tesoreria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.adapter.out.http.RouterClient;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;

/**
 * FM-TES-W1 joint rollback (PLAN_FONDOMUTUO_F2 4.1 C1/C1b, 7 "Tesoreria en proceso" layer 2, RULES 11.3 C13/C14): the REAL Spring context of the module with
 * the REAL Tesoreria library (TesoreriaEnProcesoConfiguration), the REAL unit of work (JavaCallUnitOfWorkFilter + the module transaction manager) and a test
 * handler on the Normal FQN that calls Tesoreria and then decides the outcome of the request. Database: Tesoreria's family-A H2 behind its dialect bridge;
 * the connection is in auto-commit outside a Spring transaction, so a row survives a failure ONLY if Tesoreria did not join the transaction of the request.
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Import(TesoreriaRollbackConjuntoTest.Contexto.class)
class TesoreriaRollbackConjuntoTest {

    static final String IE = "BSCL.Tesoreria.BD::TES.IngresoEgreso";
    static final String FP = "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago";

    @TestConfiguration
    static class Contexto {
        @Bean
        @Primary
        DataSource dataSourceTesoreria() throws Exception {
            return TesoreriaH2.nueva();
        }

        @Bean
        @Primary
        cl.bcs.tesoreria.port.out.ConfiguracionSistema configuracionTesoreriaDeCaso() {
            return TesoreriaH2.configuracion();
        }

        @Bean
        @Primary
        cl.bcs.tesoreria.port.out.DiccionarioMensajes diccionarioTesoreriaDeCaso() {
            return TesoreriaH2.diccionario();
        }

        @Bean
        @Primary
        cl.bcs.tesoreria.port.out.Secuencias secuenciasTesoreriaDeCaso() {
            return TesoreriaH2.secuencias();
        }

        @Bean
        @Primary
        TesoreriaH2.Router routerDePrueba() {
            return new TesoreriaH2.Router();
        }

        /**
         * params: via = directo (CrearIngresoEgresoService.logica with the TES-A-20 movement) | helper (the module's ProcesarIngresoEgresoTesoreria with a TRA
         * contribution); then modo = ok0 (counter 0) | ok1 (counter 1) | boom (exception). Answers the rows Tesoreria left on THE connection of the request.
         */
        @Bean
        JavaCallHandler normalJavaCallHandler(CrearIngresoEgresoService crearIngresoEgreso, ProcesosExternos.TesoreriaPort helper, ConexionUnidadDeTrabajo conexion) {
            return new JavaCallHandler() {
                public String fqn() {
                    return JavaCallDispatcher.CREAR_SOLICITUD_NORMAL;
                }

                public JsonObject call(Connection con, JsonObject jin) throws Exception {
                    JsonObject o = new JsonObject();
                    if ("helper".equals(jin.get("via").getAsString())) {
                        ProcesosExternos.EntradaTesoreria e = new ProcesosExternos.EntradaTesoreria();
                        e.p_IdEmpresa = 1L;
                        e.p_IdUsuario = 97L;
                        e.p_IdModulo = "FMM";
                        e.p_SimularOperacion = "N";
                        e.p_UsaReglasNegocio = "S";
                        e.p_EsIngresoAutomatico = "N";
                        e.p_Solicitudes.add(TesoreriaH2.aporteTra());
                        o.add("tesoreria", new Gson().toJsonTree(helper.procesar(e)));
                    } else {
                        var in = new Gson().fromJson(jin.get("entrada"), CrearIngresoEgresoService.CrearIngresoEgreso_Entrada.class);
                        o.add("tesoreria", new Gson().toJsonTree(crearIngresoEgreso.logica(in)));
                    }
                    o.addProperty("filasEnLaUnidadDeTrabajo", (Number) conexion.<Long>usar(c -> {
                        try (var st = c.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM \"" + IE + "\"")) {
                            rs.next();
                            return rs.getLong(1);
                        }
                    }));
                    switch (jin.get("modo").getAsString()) {
                        case "boom" -> throw new IllegalStateException("FondoMutuo fails after Tesoreria wrote");
                        case "ok1" -> o.addProperty("P_OUT_ERRORESCONTADOR", 1);
                        default -> o.addProperty("P_OUT_ERRORESCONTADOR", 0);
                    }
                    return o;
                }
            };
        }
    }

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired DataSource ds;
    @Autowired TesoreriaH2.Router router;
    MockMvc mvc;

    @BeforeEach
    void preparar() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(JavaCallLimitesFilter.class),
                ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
        router.noServido = null;
        router.llamadas.clear();
        try (Connection c = ds.getConnection(); var st = c.createStatement()) {
            st.execute("DELETE FROM \"" + FP + "\"");
            st.execute("DELETE FROM \"" + IE + "\"");
        }
    }

    static final String MOVIMIENTO_TES_A_20 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"TES\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\","
            + "\"p_IdOrigenProceso\":\"UI\",\"p_EsIngresoAutomatico\":\"N\",\"p_EsIngresoUI\":\"S\",\"p_FechaMovimiento\":\"2025-04-01\",\"p_IngresoEgresoGenerar\":[{"
            + "\"NumeroReferencia\":1,\"IdTipoMovimiento\":\"MOVING\",\"IdTipoPersona\":\"CLI\",\"IdPersona\":101,\"IdPortafolio\":8001,\"IdMoneda\":\"CLP\",\"Monto\":10000,"
            + "\"FechaLiquidacion\":\"2025-04-01\",\"IdConcepto\":\"FCOT00\",\"IdTipoPersonaTitularPago\":\"CLI\",\"IdPersonaTitularPago\":101,\"IdPortafolioTitularPago\":8001,"
            + "\"IdCaja\":0,\"Glosa\":\"aporte de prueba\",\"FechaOperacion\":\"2025-04-01\",\"TitularPago\":\"S\",\"MueveCuentaInversion\":\"S\"}],\"p_FormaDePago\":[{"
            + "\"NumeroReferencia\":1,\"NumeroReferenciaFormaDePago\":1,\"IdMedioDePago\":\"TRA\",\"IdModalidadDePago\":\"MODDEP\",\"IdMoneda\":\"CLP\",\"Monto\":10000,"
            + "\"IdCuentaBancariaCorredora\":301,\"IdCuentaBancariaSocio\":401,\"IdTarjetaBancariaCorredora\":0,\"IdTarjetaBancariaSocio\":0}]}";

    MvcResult llamar(String via, String modo) throws Exception {
        String params = "{\"via\":\"" + via + "\",\"modo\":\"" + modo + "\",\"entrada\":" + MOVIMIENTO_TES_A_20 + "}";
        return mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"" + JavaCallDispatcher.CREAR_SOLICITUD_NORMAL + "\",\"params\":" + params + "}")).andReturn();
    }

    static JsonObject cuerpo(MvcResult r) throws Exception {
        return new Gson().fromJson(r.getResponse().getContentAsString(), JsonObject.class);
    }

    @Test
    @DisplayName("FM-TES-W1-01 control: counter 0 commits Tesoreria's movement with the request (and the handler saw it on the SAME connection)")
    void contadorCero_confirmaConLaSolicitud() throws Exception {
        MvcResult r = llamar("directo", "ok0");
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(cuerpo(r).get("filasEnLaUnidadDeTrabajo").getAsLong()).isEqualTo(1);
        assertThat(TesoreriaH2.contar(ds, IE)).isEqualTo(1);
        assertThat(TesoreriaH2.contar(ds, FP)).isEqualTo(1);
    }

    @Test
    @DisplayName("FM-TES-W1-02 joint rollback: the request FAILS after Tesoreria wrote -> 0 rows in TES.IngresoEgreso / FormaDePago")
    void laSolicitudFallaDespuesDeTesoreria_ceroFilas() throws Exception {
        MvcResult r = llamar("directo", "boom");
        assertThat(r.getResponse().getStatus()).isEqualTo(500);
        assertThat(r.getResponse().getContentAsString()).doesNotContain("Tesoreria wrote");
        assertThat(TesoreriaH2.contar(ds, IE)).isZero();
        assertThat(TesoreriaH2.contar(ds, FP)).isZero();
    }

    @Test
    @DisplayName("FM-TES-W1-03 joint rollback by the counter: P_OUT_ERRORESCONTADOR > 0 -> 200 with the body, Tesoreria's rows rolled back")
    void contadorMayorQueCero_rollbackConjunto() throws Exception {
        MvcResult r = llamar("directo", "ok1");
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(cuerpo(r).get("filasEnLaUnidadDeTrabajo").getAsLong()).isEqualTo(1);
        assertThat(TesoreriaH2.contar(ds, IE)).isZero();
        assertThat(TesoreriaH2.contar(ds, FP)).isZero();
    }

    @Test
    @DisplayName("FM-TES-W1-04 through the helper: the movement of a TRA contribution is written in the request and rolled back with it")
    void atravesDelHelper_rollbackConjunto() throws Exception {
        MvcResult ok = llamar("helper", "ok1");
        assertThat(ok.getResponse().getStatus()).as(ok.getResponse().getContentAsString()).isEqualTo(200);
        assertThat(cuerpo(ok).get("filasEnLaUnidadDeTrabajo").getAsLong()).as(cuerpo(ok).toString()).isEqualTo(1);
        assertThat(TesoreriaH2.contar(ds, IE)).isZero();
        MvcResult boom = llamar("helper", "boom");
        assertThat(boom.getResponse().getStatus()).isEqualTo(500);
        assertThat(TesoreriaH2.contar(ds, IE)).isZero();
    }

    @Test
    @DisplayName("FM-TES-W1-05 C1b: a participant of Tesoreria fails and Tesoreria swallows it (CrearComprobante): the request still commits what was written")
    void c1b_fallaTragadaNoDejaLaTransaccionEnRollbackOnly() throws Exception {
        MvcResult r = llamar("helper", "ok0");
        String texto = r.getResponse().getContentAsString();
        assertThat(r.getResponse().getStatus()).as(texto).isEqualTo(200);
        assertThat(texto).as("Tesoreria reported the swallowed failure of the voucher").contains("UI_ERROR_INGRESO_EGRESO");
        assertThat(TesoreriaH2.contar(ds, IE)).as("written before the failure, kept as the legacy did").isEqualTo(1);
    }

    @Test
    @DisplayName("FM-TES-W1-06 DEV-F5: Tesoreria's AgregarLog by HTTP answers 503: Tesoreria swallows it, the helper rethrows -> 503 and nothing stays")
    void devF5_agregarLogNoServido_503YRollback() throws Exception {
        router.noServido = cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp.AGREGAR_LOG;
        MvcResult r = llamar("helper", "ok0");
        assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(503);
        assertThat(router.llamadas).contains("cl.bcs.plataforma.pla.RevisarRequisitosBasicos", cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp.AGREGAR_LOG);
        assertThat(TesoreriaH2.contar(ds, IE)).isZero();
    }

    @Test
    @DisplayName("FM-TES-W1-07 C4: Tesoreria's Plataforma collaborators go through THIS module's router client")
    void c4_colaboradoresPorElRouterDelModulo() throws Exception {
        llamar("directo", "ok0");
        assertThat(router.llamadas).containsExactly("cl.bcs.plataforma.pla.RevisarRequisitosBasicos", cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp.AGREGAR_LOG);
        assertThat(ctx.getBean(RouterClient.class)).isSameAs(router);
    }
}
