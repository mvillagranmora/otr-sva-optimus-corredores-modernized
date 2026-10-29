package cl.bcs.fondomutuo.custodia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.custodia.flujo.h2.FlujoCaso;
import cl.bcs.fondomutuo.kit.ColaboradorErrorException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * F3 custody in process: the adapter {@link FlujoCustodiaEnProceso} over {@code cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo} on H2 (no HANA), with the
 * host plumbing of FondoMutuo ({@link EntornoCustodia}: Spring transaction manager, {@code ConexionUnidadDeTrabajo}). Cases and goldens are copies of
 * BcsCustodiaServicios (see {@code fondomutuo-custodia-en-proceso/ORIGEN.txt}); the goldens were recorded from the unmodified legacy, so "the adapter returns
 * what the library returns" is checked three ways: against the golden, against the library called directly on a twin database, and for the exceptions.
 * The second half pins the atomicity that is the reason G-CUS can open: the library works on the connection of the host transaction, never opens another,
 * never commits or rolls back, and a rollback of the host leaves nothing of it.
 */
class FlujoCustodiaEnProcesoTest {

    private static Object[] previo;

    @BeforeAll
    static void zonaYLocaleDelOraculo() {
        previo = new Object[] { TimeZone.getDefault(), Locale.getDefault() };
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.US);
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    @AfterAll
    static void restaurar() {
        TimeZone.setDefault((TimeZone) previo[0]);
        Locale.setDefault((Locale) previo[1]);
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    static Path caso(String id) {
        return EntornoCustodia.recursos().resolve("cases").resolve(id);
    }

    static Stream<String> casos() throws Exception {
        try (Stream<Path> s = Files.list(EntornoCustodia.recursos().resolve("cases"))) {
            return s.filter(d -> Files.exists(d.resolve("expected.json"))).map(d -> d.getFileName().toString()).filter(id -> !id.startsWith("CUS-FLU-34"))
                    .sorted().toList().stream();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------- the same answer as the library

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("the adapter answers what the library (and the recorded legacy) answers")
    void igualALaBibliotecaYAlLegacy(String id) throws Exception {
        JsonObject esperado = FlujoCaso.leer(caso(id).resolve("expected.json"));
        JsonObject entrada = EntornoCustodia.entrada(caso(id));
        String claseEsperada = esperado.get("excepcion").isJsonNull() ? null : esperado.getAsJsonObject("excepcion").get("clase").getAsString();

        // the library called directly, on a twin database, in a transaction of its own
        String directa;
        String claseDirecta = null;
        try (EntornoCustodia gemelo = new EntornoCustodia(caso(id))) {
            String r;
            try {
                r = gemelo.tx.execute(st -> gemelo.conexion.usar(con -> {
                    try {
                        return gemelo.biblioteca().call(con, entrada.deepCopy()).toString();
                    } catch (java.sql.SQLException | RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }));
            } catch (RuntimeException e) {
                r = null;
                claseDirecta = e.getClass().getName();
            }
            directa = r;
        }

        try (EntornoCustodia e = new EntornoCustodia(caso(id))) {
            FlujoCustodiaEnProceso adaptador = e.adaptador();
            if (claseEsperada == null) {
                String salida = e.tx.execute(st -> adaptador.consultarFlujo(entrada.deepCopy()).toString());
                assertThat(salida).as(id + ": adapter vs recorded legacy (raw text: key order, nulls, scale, dates)").isEqualTo(esperado.get("salidaCruda").getAsString());
                assertThat(salida).as(id + ": adapter vs the library called directly").isEqualTo(directa);
                assertThat(e.secuenciasSolicitadas).as(id + ": sequences requested (CUS-FLU-36)")
                        .isEqualTo(esperado.getAsJsonArray("secuenciasSolicitadas"));
            } else {
                assertThatThrownBy(() -> e.tx.execute(st -> adaptador.consultarFlujo(entrada.deepCopy()))).as(id + ": exception of the legacy kept")
                        .isInstanceOf(Class.forName(claseEsperada));
                assertThat(claseDirecta).as("the library alone raises the same").isEqualTo(claseEsperada);
            }
            assertThat(e.matrizUsoVisibleDesdeOtraSesion()).as(id + ": work table left empty after the request (CUS-FLU-14)").isZero();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------- the transaction of the host

    @Test
    @DisplayName("same connection and same transaction: no other connection, no commit, no rollback, no setAutoCommit, no close by the library")
    void laBibliotecaUsaLaConexionDelAnfitrion() throws Exception {
        Path c = caso("CUS-FLU-23_fondos-mutuos");
        try (EntornoCustodia e = new EntornoCustodia(c)) {
            FlujoCustodiaEnProceso adaptador = e.adaptador();
            int[] antes = new int[2];
            e.tx.executeWithoutResult(st -> {
                antes[0] = e.conexionesPedidas.get();
                antes[1] = e.eventos.size();
                JsonObject salida = adaptador.consultarFlujo(EntornoCustodia.entrada(c));
                assertThat(salida.getAsJsonArray("p_out_FlujoTotalizado")).isNotEmpty();
                assertThat(e.conexionesPedidas.get()).as("getConnection calls made while the adapter worked").isEqualTo(antes[0]);
                assertThat(e.eventos.subList(antes[1], e.eventos.size())).as("commit / rollback / setAutoCommit / close received while the adapter worked").isEmpty();
                // the work rows the library wrote and deleted are uncommitted: another session sees none of them
                assertThat(e.auditoriaVisibleDesdeOtraSesion()).as("nothing committed by the library").isZero();
            });
            assertThat(e.conexionesPedidas.get()).as("one connection for the whole unit of work (the transaction's)").isEqualTo(antes[0]);
            assertThat(e.eventos).as("the commit came from the transaction manager, once, after the work").contains("commit");
            assertThat(e.auditoriaVisibleDesdeOtraSesion()).as("insert + delete of the work rows are committed WITH the host transaction").isPositive();
            assertThat(e.matrizUsoVisibleDesdeOtraSesion()).isZero();
        }
    }

    @Test
    @DisplayName("a rollback of the host leaves nothing of the custody call (its work rows ride the host transaction)")
    void unRollbackDelAnfitrionNoDejaNada() throws Exception {
        Path c = caso("CUS-FLU-23_fondos-mutuos");
        try (EntornoCustodia e = new EntornoCustodia(c)) {
            FlujoCustodiaEnProceso adaptador = e.adaptador();
            e.tx.executeWithoutResult(st -> {
                adaptador.consultarFlujo(EntornoCustodia.entrada(c));
                st.setRollbackOnly();
            });
            assertThat(e.eventos).as("the host rolled back").contains("rollback").doesNotContain("commit");
            assertThat(e.auditoriaVisibleDesdeOtraSesion()).as("the work rows of the call were rolled back with the host").isZero();
            assertThat(e.matrizUsoVisibleDesdeOtraSesion()).isZero();
        }
    }

    @Test
    @DisplayName("an exception of the host after the call also rolls the call back; the exception of the library rolls the request back")
    void unaExcepcionDelAnfitrionRevierteLaLlamada() throws Exception {
        Path c = caso("CUS-FLU-23_fondos-mutuos");
        try (EntornoCustodia e = new EntornoCustodia(c)) {
            FlujoCustodiaEnProceso adaptador = e.adaptador();
            assertThatThrownBy(() -> e.tx.executeWithoutResult(st -> {
                adaptador.consultarFlujo(EntornoCustodia.entrada(c));
                throw new IllegalStateException("host fails after the custody call");
            })).isInstanceOf(IllegalStateException.class);
            assertThat(e.auditoriaVisibleDesdeOtraSesion()).isZero();
        }
        Path npe = caso("D-CUS-02_npe-cliente-configurado-nulo");
        try (EntornoCustodia e = new EntornoCustodia(npe)) {
            assertThatThrownBy(() -> e.tx.executeWithoutResult(st -> e.adaptador().consultarFlujo(EntornoCustodia.entrada(npe))))
                    .isInstanceOf(NullPointerException.class);
            assertThat(e.eventos).contains("rollback").doesNotContain("commit");
            assertThat(e.auditoriaVisibleDesdeOtraSesion()).as("the library's work rows are gone with the failed request").isZero();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------- failures and closed collaborators

    @Test
    @DisplayName("valued flow (p_ValorizarFlujo = S): refused BEFORE any read or write, 503; the collaborator handed to the library is closed as well")
    void elFlujoValorizadoSeRechaza() throws Exception {
        Path c = caso("CUS-FLU-34_valorizar-merc-y-no-merc");
        JsonObject entrada = EntornoCustodia.entrada(c);
        assertThat(entrada.get("p_ValorizarFlujo").getAsString()).isEqualTo("S");
        try (EntornoCustodia e = new EntornoCustodia(c)) {
            assertThatThrownBy(() -> e.tx.executeWithoutResult(st -> e.adaptador().consultarFlujo(entrada))).isInstanceOf(ServicioNoServidoException.class)
                    .satisfies(x -> assertThat(((ServicioNoServidoException) x).compuerta()).isEqualTo("G-CUS"));
            assertThat(e.auditoriaVisibleDesdeOtraSesion()).as("no effect").isZero();
            assertThat(e.secuenciasSolicitadas).as("no sequence consumed").isEmpty();
        }
        assertThatThrownBy(() -> FlujoCustodiaEnProceso.valorizadorCerrado().apply(new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
    }

    @Test
    @DisplayName("a checked exception of the library becomes ColaboradorErrorException (generic 500, never a 503); a SQL failure is the module's AccesoDatosException")
    void laTraduccionDeLosFallos() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            ConsultarFlujoCustodiaFlujo conChecked = new ConsultarFlujoCustodiaFlujo(null, null) {
                @Override
                public JsonObject call(java.sql.Connection con, JsonObject jin) throws Exception {
                    throw new Exception("key field is not unique");
                }
            };
            assertThatThrownBy(() -> new FlujoCustodiaEnProceso(e.conexion, conChecked).consultarFlujo(new JsonObject()))
                    .isInstanceOf(ColaboradorErrorException.class).hasCauseInstanceOf(Exception.class);
            ConsultarFlujoCustodiaFlujo conSql = new ConsultarFlujoCustodiaFlujo(null, null) {
                @Override
                public JsonObject call(java.sql.Connection con, JsonObject jin) throws Exception {
                    throw new java.sql.SQLException("secret value 4242", "42000", 7);
                }
            };
            assertThatThrownBy(() -> new FlujoCustodiaEnProceso(e.conexion, conSql).consultarFlujo(new JsonObject()))
                    .isInstanceOf(cl.bcs.fondomutuo.kit.AccesoDatosException.class).hasMessageNotContaining("4242").hasMessageContaining("42000");
        }
    }

    @Test
    @DisplayName("the request of the adapter is the one of the FondoMutuo service (RESCFM use, FM filter), sent as is to the library")
    void laSolicitudLlegaTalCualALaBiblioteca() throws Exception {
        try (EntornoCustodia e = new EntornoCustodia(null)) {
            JsonObject[] recibido = new JsonObject[1];
            ConsultarFlujoCustodiaFlujo espia = new ConsultarFlujoCustodiaFlujo(null, null) {
                @Override
                public JsonObject call(java.sql.Connection con, JsonObject jin) {
                    recibido[0] = jin;
                    JsonObject r = new JsonObject();
                    r.addProperty("devuelto", true);
                    return r;
                }
            };
            JsonObject entrada = new JsonObject();
            entrada.addProperty("p_ValorizarFlujo", "N");
            entrada.addProperty("p_IdEmpresa", 1);
            JsonObject salida = new FlujoCustodiaEnProceso(e.conexion, espia).consultarFlujo(entrada);
            assertThat(recibido[0]).isSameAs(entrada);
            assertThat(salida.get("devuelto").getAsBoolean()).isTrue();
        }
    }

    @Test
    @DisplayName("the Spring configuration wires the library with the SecuenciaPort of the host: the beans it builds answer the golden of a projected case")
    void laConfiguracionCableaLasSecuenciasDelAnfitrion() throws Exception {
        String id = "CUS-FLU-28_proyeccion-3-dias";
        JsonObject esperado = FlujoCaso.leer(caso(id).resolve("expected.json"));
        try (EntornoCustodia e = new EntornoCustodia(caso(id))) {
            FlujoCustodiaEnProcesoConfiguracion configuracion = new FlujoCustodiaEnProcesoConfiguracion();
            FlujoCustodiaEnProceso adaptador = configuracion.flujoCustodiaEnProceso(e.conexion, configuracion.consultarFlujoCustodiaFlujo(e.secuencias));
            String salida = e.tx.execute(st -> adaptador.consultarFlujo(EntornoCustodia.entrada(caso(id))).toString());
            assertThat(salida).isEqualTo(esperado.get("salidaCruda").getAsString());
            assertThat(e.secuenciasSolicitadas).as("both sequences of the library were read through the port").isEqualTo(esperado.getAsJsonArray("secuenciasSolicitadas"));
            assertThat(e.secuenciasSolicitadas.size()).isEqualTo(2);
        }
    }

    static List<String> idsDeLosCasos() throws Exception {
        return casos().toList();
    }
}
