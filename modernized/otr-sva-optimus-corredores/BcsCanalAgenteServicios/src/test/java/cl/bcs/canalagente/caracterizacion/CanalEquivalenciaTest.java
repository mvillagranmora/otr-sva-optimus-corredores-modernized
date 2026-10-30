package cl.bcs.canalagente.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

/**
 * Equivalence of the production units of BcsCanalAgenteServicios with the legacy (cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion: logica and the JavaCall entry call, plus the SELECT of two BD/Servicios procedure texts on H2): every case runs
 * the unit on H2 behind the recording connection with the collaborator of the module (ConsultarCartolaDeMovimiento) scripted by the case, and the output / exception, the SQL sent (text and parameter rows, in order), the collaborator calls and the
 * tables are compared with the golden RECORDED from the unmodified legacy by legacy-oracle/record.sh (same connection, same H2, same scripts). The display name of every case names the rules it pins
 * (B-CA-nn, D-CAN-nn of reglas.json).
 *
 * <p>RED BEFORE CODE: the production classes do not exist yet, so every case fails with "not implemented: the production class ... does not exist yet". A golden that is missing FAILS the case; a run
 * in which no comparison executed FAILS.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CanalEquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger CON_EXCEPCION = new AtomicInteger();

    static Stream<Arguments> casos() {
        List<String> ids = CasoCanal.casos();
        assertTrue(ids.size() >= 85, "the BcsCanalAgenteServicios matrix has at least 85 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCanal.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("BcsCanalAgenteServicios: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCanal.casos()) {
            if (!Files.exists(CasoCanal.directorio().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("BcsCanalAgenteServicios equivalence with the recorded legacy")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoCanal.golden(id);
        JsonObject obtenido = CasoCanal.corrida(id);
        CasoCanal.comparar(id, golden, obtenido);
        if (golden.has("excepcion")) CON_EXCEPCION.incrementAndGet();
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("BcsCanalAgenteServicios: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCanal.casos().size() + " (the legacy threw in " + CON_EXCEPCION.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCanal.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(CON_EXCEPCION.get() > 0, "the cases in which the legacy throws must have run");
    }
}
