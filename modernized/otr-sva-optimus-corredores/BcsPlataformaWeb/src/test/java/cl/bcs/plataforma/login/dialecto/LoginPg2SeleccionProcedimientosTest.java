package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;
import cl.bcs.plataforma.login.clave.GestionClaveServicio;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Respuesta;
import cl.bcs.plataforma.service.ProcedureDispatcherService;

/**
 * LOGIN-PG round 2, part B (T-33): which implementation of {@code ProcedimientoAlmacenado} the login gets. Decision (API_ASSUMED_LOGINPG2.md A2-2/A2-3): a
 * selector {@code ProcedimientosPorDialecto(SqlDialecto, hanaImpl, postgresImpl)} is the {@code @Primary} bean of that interface; in HANA mode it hands every call,
 * untouched, to the existing {@code ProcedureDispatcherService}; in Postgres mode to {@code ProcedimientosPostgres}. The generic dispatchers themselves are NOT
 * changed: in Postgres mode they keep answering 503 for every name (LoginPgDespachadoresTest), and the eight login procedures do not go through them
 * (Q-LOGINPG2-8 asks whether the generic {@code /call} path should serve them).
 */
class LoginPg2SeleccionProcedimientosTest {

    private static ProcedimientoAlmacenado guion(List<Object[]> llamadas, JsonObject respuesta) {
        return (con, ruta, entrada) -> {
            llamadas.add(new Object[] { con, ruta, entrada });
            return respuesta;
        };
    }

    // ------------------------------------------------------------------------------------------------ the selector (red until it exists)

    @Test
    @DisplayName("LOGIN-PG2 selector with the HANA dialect: every call goes, with the SAME connection, name and JSON, to the HANA implementation and its answer comes back as is; the Postgres implementation is never touched")
    void hana_delega_en_hana() throws Exception {
        List<Object[]> hana = new ArrayList<>();
        List<Object[]> pg = new ArrayList<>();
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("P_OUT_X", "1");
        ProcedimientoAlmacenado sel = ApiLoginPg3.porDialecto(ApiLoginPg3.hana(), guion(hana, respuesta), guion(pg, new JsonObject()));
        Connection con = new JdbcGrabador().conexion();
        JsonObject entrada = new JsonObject();
        entrada.addProperty("a", 1);
        assertSame(respuesta, sel.llamar(con, LoginPg2Recursos.ADMIN, entrada));
        assertEquals(1, hana.size());
        assertSame(con, hana.get(0)[0]);
        assertEquals(LoginPg2Recursos.ADMIN, hana.get(0)[1]);
        assertSame(entrada, hana.get(0)[2]);
        assertThat(pg).isEmpty();
        // also for a name that Postgres would refuse: HANA mode does not judge
        sel.llamar(con, LoginPg2Recursos.OTRO, entrada);
        assertEquals(2, hana.size());
        assertThat(pg).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 selector with the Postgres dialect: every call goes to the Postgres implementation (same connection, name and JSON) and never to the HANA one, so no HANA text can reach Postgres")
    void postgres_delega_en_postgres() throws Exception {
        List<Object[]> hana = new ArrayList<>();
        List<Object[]> pg = new ArrayList<>();
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("P_OUT_X", "2");
        ProcedimientoAlmacenado sel = ApiLoginPg3.porDialecto(ApiLoginPg3.postgres(), guion(hana, new JsonObject()), guion(pg, respuesta));
        Connection con = new JdbcGrabador().conexion();
        JsonObject entrada = new JsonObject();
        assertSame(respuesta, sel.llamar(con, LoginPg2Recursos.FECHAS, entrada));
        assertEquals(1, pg.size());
        assertSame(con, pg.get(0)[0]);
        assertSame(entrada, pg.get(0)[2]);
        assertThat(hana).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 selector: an exception of the chosen implementation reaches the caller as is (the 503 and the SQL errors are not wrapped)")
    void las_excepciones_pasan() {
        UnsupportedOperationException e503 = new UnsupportedOperationException("nope");
        ProcedimientoAlmacenado lanza = (con, ruta, entrada) -> {
            throw e503;
        };
        ProcedimientoAlmacenado sel = ApiLoginPg3.porDialecto(ApiLoginPg3.postgres(), lanza, lanza);
        assertThatThrownBy(() -> sel.llamar(new JdbcGrabador().conexion(), LoginPg2Recursos.ADMIN, new JsonObject())).isSameAs(e503);
    }

    @Test
    @DisplayName("LOGIN-PG2 selector over the REAL Postgres implementation: an unknown procedure answers 503 and the connection is not touched; a login procedure reaches the connection as {call sp_...}")
    void postgres_real_a_traves_del_selector() throws Exception {
        ProcedimientoAlmacenado sel = ApiLoginPg3.porDialecto(ApiLoginPg3.postgres(), (c, r, e) -> {
            throw new AssertionError("HANA implementation used in Postgres mode");
        }, ApiLoginPg3.procedimientosPostgres());
        JdbcGrabador g = new JdbcGrabador();
        assertThatThrownBy(() -> sel.llamar(g.conexion(), LoginPg2Recursos.OTRO, new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
        assertTrue(g.intacta());
        JdbcGrabador g2 = new JdbcGrabador().responder(Respuesta.salidas(Map.of(3, "S")));
        JsonObject out = sel.llamar(g2.conexion(), LoginPg2Recursos.ADMIN, LoginPg2ProcedimientosTest.json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}"));
        assertEquals("S", out.get("P_OUT_ESADMIN").getAsString());
        assertEquals("{call sp_pla_acc_consultar_usuario_admin(?,?,?)}", LoginPg2ProcedimientosTest.norm(g2.unica().sql));
    }

    // ------------------------------------------------------------------------------------------------ the generic dispatchers stay closed in Postgres mode

    private static Connection conexionProhibida(List<String> usos) {
        return (Connection) Proxy.newProxyInstance(LoginPg2SeleccionProcedimientosTest.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            usos.add(m.getName());
            throw new AssertionError("the connection was used: " + m.getName());
        });
    }

    private static DataSource fuenteProhibida(List<String> usos) {
        return (DataSource) Proxy.newProxyInstance(LoginPg2SeleccionProcedimientosTest.class.getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
            usos.add(m.getName());
            throw new AssertionError("the data source was used: " + m.getName());
        });
    }

    static Stream<String> nombresDelDespachadorGenerico() {
        return Stream.of(LoginPg2Recursos.OTRO, "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario", "BSCL.Nada.BD::Nada");
    }

    @ParameterizedTest(name = "LOGIN-PG2 the generic ProcedureDispatcherService in Postgres mode still answers 503 for [{0}] without touching the connection or the data source (llamar and dispatch)")
    @MethodSource("nombresDelDespachadorGenerico")
    void despachador_generico_sigue_cerrado(String nombre) {
        List<String> usos = new ArrayList<>();
        ProcedureDispatcherService svc = new ProcedureDispatcherService(fuenteProhibida(usos), null, ApiLoginPg3.postgres());
        assertThatThrownBy(() -> svc.llamar(conexionProhibida(usos), nombre, new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> svc.dispatch(nombre, new JsonObject(), 1L, 1L)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(usos).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG3 the ApiLoginPg3 bridge itself fails with the standard message for a class that does not exist (guard of the red suite)")
    void el_puente_falla_con_el_mensaje_estandar() {
        assertThatThrownBy(() -> ApiLoginPg3.clase("NoExisteEnProduccion")).isInstanceOf(AssertionError.class)
                .hasMessage("LOGIN-PG3 not implemented: the production class cl.bcs.plataforma.login.dialecto.NoExisteEnProduccion does not exist yet");
    }

    // ------------------------------------------------------------------------------------------------ Spring wiring

    private static final String PG_URL = "spring.datasource.url=jdbc:h2:mem:lpg2sel_pg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    static ProcedimientoAlmacenado delLogin(ApplicationContext ctx) {
        return ctx.getBeanProvider(ProcedimientoAlmacenado.class).getObject();
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=postgres", PG_URL })
    @ActiveProfiles("test")
    class ConPostgres {
        @BeforeAll
        static void existe() {
            ApiLoginPg3.clase("ProcedimientosPostgres");
            ApiLoginPg3.clase("ProcedimientosPorDialecto");
        }

        @Autowired
        ApplicationContext ctx;

        @Test
        @DisplayName("LOGIN-PG2 wiring, postgres: the ProcedimientoAlmacenado the login receives is the selector (a unique, primary bean), and the two login services hold that same bean")
        void el_login_recibe_el_selector() throws Exception {
            ProcedimientoAlmacenado p = delLogin(ctx);
            assertEquals("ProcedimientosPorDialecto", p.getClass().getSimpleName());
            for (Class<?> servicio : List.of(BootstrapServicio.class, GestionClaveServicio.class)) {
                Field f = servicio.getDeclaredField("procedimientos");
                f.setAccessible(true);
                assertSame(p, f.get(ctx.getBean(servicio)), servicio.getSimpleName());
            }
        }

        static Stream<LoginPg2ProcedimientosTest.Caso> casos() {
            return LoginPg2ProcedimientosTest.casosStream();
        }

        @ParameterizedTest(name = "LOGIN-PG2 wiring, postgres: {0} works through the bean of the context (the answer has the legacy keys)")
        @MethodSource("casos")
        void los_ocho_funcionan(LoginPg2ProcedimientosTest.Caso c) throws Exception {
            LoginPg2Recursos.Rutina r = LoginPg2ProcedimientosTest.RUT.rutina(c.pgNombre());
            JdbcGrabador g = new JdbcGrabador().guion(s -> Respuesta.salidas(LoginPg2ProcedimientosTest.porIndice(r, c.salidas())));
            JsonObject out = delLogin(ctx).llamar(g.conexion(), c.hana(), LoginPg2ProcedimientosTest.json(c.entrada()));
            assertEquals(LoginPg2ProcedimientosTest.json(c.esperado()), out);
            assertEquals(c.llamada(), LoginPg2ProcedimientosTest.norm(g.unica().sql));
        }

        @Test
        @DisplayName("LOGIN-PG2 wiring, postgres: ConsultarFormatos works through the bean (SELECT * FROM function) and any other procedure answers 503 with the connection untouched")
        void formatos_y_los_demas() throws Exception {
            JdbcGrabador g = new JdbcGrabador().responder(Respuesta.filas(List.of()));
            JsonObject out = delLogin(ctx).llamar(g.conexion(), LoginPg2Recursos.FORMATOS, new JsonObject());
            assertEquals(LoginPg2ProcedimientosTest.json("{\"P_OUT_MONEDAFORMATO\":[]}"), out);
            JdbcGrabador g2 = new JdbcGrabador();
            assertThatThrownBy(() -> delLogin(ctx).llamar(g2.conexion(), LoginPg2Recursos.OTRO, new JsonObject())).isInstanceOf(UnsupportedOperationException.class);
            assertTrue(g2.intacta());
        }
    }

    @Nested
    @SpringBootTest(properties = { "plataforma.db.dialect=hana", "spring.datasource.url=jdbc:h2:mem:lpg2sel_hana;MODE=LEGACY;DB_CLOSE_DELAY=-1" })
    @ActiveProfiles("test")
    class ConHana {
        @BeforeAll
        static void existe() {
            ApiLoginPg3.clase("ProcedimientosPostgres");
            ApiLoginPg3.clase("ProcedimientosPorDialecto");
        }

        @Autowired
        ApplicationContext ctx;

        @Test
        @DisplayName("LOGIN-PG2 wiring, hana: the login still gets a bean that goes to the existing ProcedureDispatcherService (HANA CALL with introspection); no Postgres call (sp_...) ever reaches the connection, and the dispatcher bean is still there")
        void en_hana_todo_sigue_igual() {
            assertThat(ctx.getBeansOfType(ProcedureDispatcherService.class)).hasSize(1);
            JdbcGrabador g = new JdbcGrabador();
            try {
                delLogin(ctx).llamar(g.conexion(), LoginPg2Recursos.ADMIN, LoginPg2ProcedimientosTest.json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7}"));
            } catch (Throwable e) {
                // H2 has no SYS.PROCEDURE_PARAMETERS, so the HANA path fails when it introspects: what matters is that it was the HANA path
                assertThat(e).isNotInstanceOf(UnsupportedOperationException.class);
            }
            assertThat(g.sentencias.stream().map(s -> s.sql)).noneMatch(sql -> sql.contains("sp_pla"));
        }
    }
}
