package cl.bcs.custodia.flujo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.h2.FlujoCaso;
import cl.bcs.custodia.flujo.h2.FlujoGuion;

/**
 * Equivalence of the NEW ConsultarFlujoCustodia with the goldens recorded from the UNMODIFIED legacy (CUS-FLU-01..36, D-CUS-01..10; every case
 * names its rule ids in its display name). Per case the same shared harness the oracle used (cl.bcs.custodia.flujo.h2.FlujoCaso: fresh H2
 * MODE=LEGACY, schema + seeds, HANA dialect bridge, MatrizUso witness, scripted ValorizarInstrumento, recorded sequences) runs the new
 * implementation and its dump must equal the golden: raw output text byte for byte, exception class (message for a plain Exception), the rows
 * written to / deleted from / left in CUS.FlujoCustodia_MatrizUso, the sequences requested and every ValorizarInstrumento request.
 *
 * <p>Until src/main has the implementation every case FAILS ("implementation missing"); nothing is skipped. The number of cases whose comparison
 * actually ran is printed ("equivalence cases executed: N") and N = 0 is itself a failure.
 */
@DisplayName("ConsultarFlujoCustodia: equivalence with the recorded legacy (CUS-FLU-01..36)")
class ConsultarFlujoCustodiaGoldenTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static Object[] previo;

    static List<GoldenCasos.Caso> casos() {
        return GoldenCasos.casos();
    }

    @BeforeAll
    static void zonaYLocaleDelOraculo() {
        previo = GoldenCasos.fijarZonaYLocale();
    }

    @AfterAll
    static void reporte() {
        GoldenCasos.restaurar(previo);
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + GoldenCasos.casos().size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (0 = nothing proven)").isPositive();
    }

    // pins (per case, see case.json "reglas" and the display name): CUS-FLU-01 CUS-FLU-02 CUS-FLU-03 CUS-FLU-04 CUS-FLU-05 CUS-FLU-06 CUS-FLU-07
    // CUS-FLU-08 CUS-FLU-09 CUS-FLU-10 CUS-FLU-11 CUS-FLU-12 CUS-FLU-13 CUS-FLU-14 CUS-FLU-15 CUS-FLU-16 CUS-FLU-17 CUS-FLU-18 CUS-FLU-19 CUS-FLU-20
    // CUS-FLU-21 CUS-FLU-22 CUS-FLU-23 CUS-FLU-24 CUS-FLU-25 CUS-FLU-26 CUS-FLU-27 CUS-FLU-28 CUS-FLU-29 CUS-FLU-30 CUS-FLU-31 CUS-FLU-32 CUS-FLU-33
    // CUS-FLU-34 CUS-FLU-35 CUS-FLU-36 D-CUS-01 D-CUS-02 D-CUS-03 D-CUS-04 D-CUS-05 D-CUS-06 D-CUS-07 D-CUS-08 D-CUS-09 D-CUS-10
    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void igualAlLegacy(GoldenCasos.Caso caso) throws Exception {
        FlujoCustodiaContrato.verificarImplementacion();
        JsonObject esperado = FlujoCaso.comparable(GoldenCasos.golden(caso));
        JsonObject obtenido = FlujoCaso.comparable(FlujoCaso.ejecutar(GoldenCasos.base(), caso.dir(), ConsultarFlujoCustodiaGoldenTest::nuevo,
                "Dump of the NEW implementation " + FlujoCustodiaContrato.IMPL + " (not a golden)"));
        EJECUTADOS.incrementAndGet();

        String r = caso.reglas() + " " + caso.id() + ": ";
        SoftAssertions s = new SoftAssertions();
        s.assertThat(obtenido.get("excepcion")).as(r + "exception (class; message only for java.lang.Exception)").isEqualTo(esperado.get("excepcion"));
        s.assertThat(texto(obtenido, "salidaCruda")).as(r + "raw output text (key order, nulls, BigDecimal scale, dates, list order)")
                .isEqualTo(texto(esperado, "salidaCruda"));
        s.assertThat(obtenido.get("matrizUso")).as(r + "CUS.FlujoCustodia_MatrizUso written / deleted / left (CUS-FLU-14)").isEqualTo(esperado.get("matrizUso"));
        s.assertThat(obtenido.get("secuenciasSolicitadas")).as(r + "sequences requested (CUS-FLU-36)").isEqualTo(esperado.get("secuenciasSolicitadas"));
        s.assertThat(GoldenCasos.canonico(obtenido.get("colaboradores")))
                .as(r + "ValorizarInstrumento requests and scripted answers (CUS-FLU-33..35), key order ignored")
                .isEqualTo(GoldenCasos.canonico(esperado.get("colaboradores")));
        s.assertThat(obtenido.get("llamadaSinGuion").getAsBoolean()).as(r + "a collaborator call the case did not script").isFalse();
        s.assertAll();
    }

    /** the subject: the new implementation behind the contract, wired to the case script (valuation) and the recorded sequences */
    static JsonObject nuevo(java.sql.Connection con, JsonObject entrada, FlujoGuion guion) throws Exception {
        FlujoCustodiaPort port = FlujoCustodiaContrato.crear(req -> {
            try {
                return guion.valorizar(req);
            } catch (Exception e) {
                throw FlujoCustodiaContrato.sneaky(e);
            }
        }, (nombre, cantidad) -> {
            try {
                return guion.secuencia(con, nombre, cantidad);
            } catch (Exception e) {
                throw FlujoCustodiaContrato.sneaky(e);
            }
        });
        return port.call(con, entrada);
    }

    private static String texto(JsonObject o, String k) {
        return o.get(k) == null || o.get(k).isJsonNull() ? null : o.get(k).getAsString();
    }
}
