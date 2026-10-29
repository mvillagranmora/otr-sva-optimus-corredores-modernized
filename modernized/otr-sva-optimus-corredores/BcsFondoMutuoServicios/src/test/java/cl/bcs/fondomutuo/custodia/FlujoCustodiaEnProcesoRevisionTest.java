package cl.bcs.fondomutuo.custodia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.ColaboradorErrorException;

/**
 * Review of 2026-10-28 on {@link FlujoCustodiaEnProceso}: H2 (the library gets the restricted connection), M1 (guard of the transaction, off for the harness constructor) and
 * M4 (one INFO line per call, counts and keys only). The five cases of {@code FlujoCustodiaEnProcesoTest} keep running through the restricted connection: its goldens are unchanged.
 */
class FlujoCustodiaEnProcesoRevisionTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void capturarLog() {
        logger = (Logger) LoggerFactory.getLogger(FlujoCustodiaEnProceso.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void soltarLog() {
        logger.detachAppender(appender);
    }

    private static ConsultarFlujoCustodiaFlujo bibliotecaQue(Hacer accion) {
        return new ConsultarFlujoCustodiaFlujo(null, null) {
            @Override
            public JsonObject call(Connection con, JsonObject jin) throws Exception {
                return accion.con(con);
            }
        };
    }

    @FunctionalInterface
    interface Hacer {
        JsonObject con(Connection con) throws Exception;
    }

    private static JsonObject solicitud(int carteras, int instrumentos) {
        JsonObject e = new JsonObject();
        e.addProperty("p_ValorizarFlujo", "N");
        JsonArray uso = new JsonArray();
        for (int i = 0; i < carteras; i++) uso.add(new JsonObject());
        JsonArray filtros = new JsonArray();
        for (int i = 0; i < instrumentos; i++) filtros.add(new JsonObject());
        e.add("p_Uso", uso);
        e.add("p_FiltrosBusqueda", filtros);
        return e;
    }

    // ---------------------------------------------------------------------------------------------------------------- H2

    @Test
    @DisplayName("the library receives a restricted connection: commit, rollback and close raise IllegalStateException, the host transaction stays intact")
    void laBibliotecaRecibeLaConexionAcotada() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            List<Throwable> vistos = new ArrayList<>();
            ConsultarFlujoCustodiaFlujo espia = bibliotecaQue(con -> {
                for (org.junit.jupiter.api.function.Executable x : new org.junit.jupiter.api.function.Executable[] { con::commit, con::rollback, con::close,
                        () -> con.setAutoCommit(true) }) {
                    try {
                        x.execute();
                    } catch (Throwable t) {
                        vistos.add(t);
                    }
                }
                return new JsonObject();
            });
            int antes = e.eventos.size();
            e.tx.executeWithoutResult(st -> new FlujoCustodiaEnProceso(e.conexion, espia).consultarFlujo(solicitud(1, 1)));
            assertThat(vistos).hasSize(4).allSatisfy(t -> assertThat(t).isInstanceOf(IllegalStateException.class));
            assertThat(e.eventos.subList(antes, e.eventos.size())).as("only what the transaction manager itself does reached the connection (no call of the library)")
                    .containsExactly("setAutoCommit", "commit", "setAutoCommit", "close");
        }
    }

    @Test
    @DisplayName("each statement of the library carries the remaining time of the host transaction")
    void lasSentenciasDeLaBibliotecaLlevanElPlazoDelAnfitrion() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            int[] plazo = new int[1];
            ConsultarFlujoCustodiaFlujo espia = bibliotecaQue(con -> {
                try (var st = con.prepareStatement("SELECT 1")) {
                    plazo[0] = st.getQueryTimeout();
                }
                return new JsonObject();
            });
            TransactionTemplate t = new TransactionTemplate(e.tx.getTransactionManager());
            t.setTimeout(30);
            t.executeWithoutResult(st -> new FlujoCustodiaEnProceso(e.conexion, espia).consultarFlujo(solicitud(1, 1)));
            assertThat(plazo[0]).isBetween(1, 30);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------- M1

    @Test
    @DisplayName("M1: with the guard on, the call needs an active read-write Spring transaction; the harness constructor has no guard")
    void laGuardiaDeTransaccion() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            boolean[] llamada = new boolean[1];
            ConsultarFlujoCustodiaFlujo espia = bibliotecaQue(con -> {
                llamada[0] = true;
                return new JsonObject();
            });
            FlujoCustodiaEnProceso conGuardia = new FlujoCustodiaEnProceso(e.conexion, espia, true);

            assertThatThrownBy(() -> conGuardia.consultarFlujo(solicitud(1, 1))).as("no transaction").isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("read-write transaction");
            TransactionTemplate soloLectura = new TransactionTemplate(e.tx.getTransactionManager());
            soloLectura.setReadOnly(true);
            assertThatThrownBy(() -> soloLectura.executeWithoutResult(st -> conGuardia.consultarFlujo(solicitud(1, 1)))).as("read-only transaction")
                    .isInstanceOf(IllegalStateException.class);
            assertThat(llamada[0]).as("the library was never called").isFalse();

            e.tx.executeWithoutResult(st -> conGuardia.consultarFlujo(solicitud(1, 1)));
            assertThat(llamada[0]).as("read-write transaction: passes").isTrue();

            llamada[0] = false;
            new FlujoCustodiaEnProceso(e.conexion, espia).consultarFlujo(solicitud(1, 1));   // the constructor the harnesses use: raw connection, no transaction
            assertThat(llamada[0]).isTrue();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------- M4

    private List<String> lineas() {
        List<String> r = new ArrayList<>();
        for (ILoggingEvent ev : new ArrayList<>(appender.list)) r.add(ev.getFormattedMessage());
        return r;
    }

    @Test
    @DisplayName("M4: one INFO line per call with duration, counts of portfolios and instruments, key of the flow and the outcome; no values or database text")
    void unaLineaPorLlamada() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            JsonObject respuesta = new JsonObject();
            respuesta.addProperty("p_out_KeyFlujoCustodia", 777L);
            JsonObject ok = new FlujoCustodiaEnProceso(e.conexion, bibliotecaQue(con -> respuesta)).consultarFlujo(solicitud(3, 2));
            assertThat(ok).isSameAs(respuesta);
            assertThat(lineas()).hasSize(1);
            assertThat(lineas().get(0)).contains("result=ok").contains("carteras=3").contains("instrumentos=2").contains("keyFlujoCustodia=777").containsPattern("ms=\\d+");
            assertThat(appender.list.get(0).getLevel()).isEqualTo(ch.qos.logback.classic.Level.INFO);

            appender.list.clear();
            assertThatThrownBy(() -> new FlujoCustodiaEnProceso(e.conexion, bibliotecaQue(con -> {
                throw new java.sql.SQLException("secret value 4242", "42000", 7);
            })).consultarFlujo(solicitud(1, 0))).isInstanceOf(AccesoDatosException.class);
            assertThat(lineas()).hasSize(1);
            assertThat(lineas().get(0)).contains("result=sql").contains("carteras=1").contains("instrumentos=0").contains("keyFlujoCustodia=-").doesNotContain("4242");

            appender.list.clear();
            assertThatThrownBy(() -> new FlujoCustodiaEnProceso(e.conexion, bibliotecaQue(con -> {
                throw new NullPointerException("secret value 4242");
            })).consultarFlujo(solicitud(2, 2))).isInstanceOf(NullPointerException.class);
            assertThat(lineas().get(0)).contains("result=runtime").doesNotContain("4242");

            appender.list.clear();
            assertThatThrownBy(() -> new FlujoCustodiaEnProceso(e.conexion, bibliotecaQue(con -> {
                throw new Exception("secret value 4242");
            })).consultarFlujo(solicitud(2, 2))).isInstanceOf(ColaboradorErrorException.class);
            assertThat(lineas().get(0)).contains("result=checked").doesNotContain("4242");
        }
    }

    @Test
    @DisplayName("M4: a rejected valued request (p_ValorizarFlujo = S) does not call the library and writes no line")
    void laSolicitudValorizadaNoGeneraLinea() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            JsonObject s = solicitud(1, 1);
            s.addProperty("p_ValorizarFlujo", "S");
            assertThatThrownBy(() -> new FlujoCustodiaEnProceso(e.conexion, bibliotecaQue(con -> new JsonObject())).consultarFlujo(s))
                    .isInstanceOf(cl.bcs.fondomutuo.kit.ServicioNoServidoException.class);
            assertThat(lineas()).isEmpty();
        }
    }

    @Test
    @DisplayName("the five recorded cases still answer the golden through the restricted connection (spot check with a projected case)")
    void unCasoRealSigueIgual() throws Exception {
        String id = "CUS-FLU-28_proyeccion-3-dias";
        Path caso = FlujoCustodiaEnProcesoTest.caso(id);
        JsonObject esperado = cl.bcs.custodia.flujo.h2.FlujoCaso.leer(caso.resolve("expected.json"));
        try (EntornoCustodia e = new EntornoCustodia(caso)) {
            String salida = e.tx.execute(st -> new FlujoCustodiaEnProceso(e.conexion, e.biblioteca(), true).consultarFlujo(EntornoCustodia.entrada(caso)).toString());
            assertThat(salida).isEqualTo(esperado.get("salidaCruda").getAsString());
            assertThat(lineas()).hasSize(1);
            assertThat(lineas().get(0)).contains("result=ok").containsPattern("keyFlujoCustodia=\\d+");
        }
    }
}
