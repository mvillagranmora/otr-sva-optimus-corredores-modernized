package cl.bcs.fondomutuo.manejador;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * FMM-MAN-nn: the cases of the oracle (golden recorded by executing the UNMODIFIED legacy Manejador on H2) run through the NEW handler. The cases
 * without rows in the Normal list need no database and no Normal: the scripted destinations answer like the oracle's doubles, and the output text
 * ({@code salidaCruda}, key order included), the exception, the servlet decision and the sequence of calls to the destinations must be IDENTICAL to the
 * golden. The request each destination receives is compared on the fields the facade sets (the legacy destination input classes carry many more
 * constructor defaults: the full comparison is the oracle run with the real legacy classes, see TRANSFORMATION_NOTES Step 4B). Cases with Normal rows
 * need the real Normal over the oracle schema ({@code ManejadorSobreNormalRealTest} (package normal)).
 */
public class ManejadorOraculoTest {

    public static boolean subconjunto(JsonElement mio, JsonElement oro) {
        if (mio == null || mio.isJsonNull()) return true;
        if (oro == null || oro.isJsonNull()) return false;
        if (mio.isJsonObject()) {
            if (!oro.isJsonObject()) return false;
            for (String k : mio.getAsJsonObject().keySet()) {
                if (!subconjunto(mio.getAsJsonObject().get(k), oro.getAsJsonObject().get(k))) return false;
            }
            return true;
        }
        if (mio.isJsonArray()) {
            if (!oro.isJsonArray() || mio.getAsJsonArray().size() != oro.getAsJsonArray().size()) return false;
            for (int i = 0; i < mio.getAsJsonArray().size(); i++) {
                if (!subconjunto(mio.getAsJsonArray().get(i), oro.getAsJsonArray().get(i))) return false;
            }
            return true;
        }
        try {
            return new BigDecimal(mio.getAsString()).compareTo(new BigDecimal(oro.getAsString())) == 0;
        } catch (NumberFormatException e) {
            return mio.getAsString().equals(oro.getAsString());
        }
    }

    public static JsonArray soloDestinos(JsonArray todas) {
        JsonArray r = new JsonArray();
        for (JsonElement e : todas) {
            String n = e.getAsJsonObject().get("colaborador").getAsString();
            if (n.startsWith("CrearSolicitud") && !n.equals("CrearSolicitudAporteRescateNormal")) r.add(e);
        }
        return r;
    }

    public static void comparar(CasoMan c, JsonObject nuevo) {
        JsonObject oro = c.esperado;
        assertThat(nuevo.get("decisionServlet")).as(c.id + " decision").isEqualTo(oro.get("decisionServlet"));
        if (oro.has("excepcion")) {
            String o = oro.get("excepcion").getAsString();
            String n = nuevo.get("excepcion").getAsString();
            // approved difference (as in 3A/3B/4A): NPE = class only (the JVM text names the variable), a plain legacy Exception = its message only
            if (o.startsWith("NullPointerException")) {
                assertThat(n.substring(0, n.indexOf(':'))).as(c.id + " exception class").isEqualTo("NullPointerException");
            } else if (o.startsWith("Exception:")) {
                assertThat(n.substring(n.indexOf(':') + 1)).as(c.id + " exception message").isEqualTo(o.substring(o.indexOf(':') + 1));
            } else {
                assertThat(n).as(c.id + " exception").isEqualTo(o);
            }
        } else {
            assertThat(nuevo.has("excepcion")).as(c.id + " unexpected exception " + nuevo.get("excepcion")).isFalse();
            assertThat(nuevo.get("salidaCruda").getAsString()).as(c.id + " output text").isEqualTo(oro.get("salidaCruda").getAsString());
        }
        JsonArray mios = soloDestinos(nuevo.getAsJsonArray("colaboradores"));
        JsonArray suyos = soloDestinos(oro.getAsJsonArray("colaboradores"));
        assertThat(mios.size()).as(c.id + " calls to the destinations").isEqualTo(suyos.size());
        for (int i = 0; i < mios.size(); i++) {
            JsonObject m = mios.get(i).getAsJsonObject();
            JsonObject s = suyos.get(i).getAsJsonObject();
            assertThat(m.get("colaborador")).as(c.id + " call " + i + " destination (order)").isEqualTo(s.get("colaborador"));
            assertThat(subconjunto(m.getAsJsonArray("argumentos"), s.getAsJsonArray("argumentos"))).as(c.id + " call " + i + " request: " + m.get("argumentos")
                    + " vs golden " + s.get("argumentos")).isTrue();
        }
    }

    @Test
    void fmMan00_losCasosDelOraculoEstanTodosConGolden_yHayAlMenosLosDelPlan() throws Exception {
        List<CasoMan> todos = CasoMan.todos();
        assertThat(todos).extracting(c -> c.id).anyMatch(i -> i.startsWith("FMM-MAN-P01"));
        assertThat(todos.size()).isGreaterThanOrEqualTo(15);
        for (CasoMan c : todos) {
            assertThat(c.esperado.has("decisionServlet")).as(c.id).isTrue();
            assertThat(c.esperado.get("_provenance").getAsString()).as(c.id + " recorded by the legacy").contains("unmodified legacy");
        }
    }

    @TestFactory
    List<DynamicTest> fmManCasos_sinNormal_identicosAlGoldenDelLegacy() throws Exception {
        List<DynamicTest> r = new ArrayList<>();
        for (CasoMan c : CasoMan.todos()) {
            if (c.traeNormal()) continue;   // needs the real Normal: ManejadorSobreNormalRealTest
            r.add(DynamicTest.dynamicTest(c.id, () -> {
                assertThat(c.esperado.getAsJsonObject("tablas").entrySet()).allSatisfy(e -> assertThat(e.getValue().getAsJsonArray()).isEmpty());
                assertThat(c.esperado.getAsJsonArray("secuenciasSolicitadas")).isEmpty();
                comparar(c, c.ejecutar(in -> {
                    throw new AssertionError("this case has no rows in the Normal list");
                }));
            }));
        }
        return r;
    }
}
