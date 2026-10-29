package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import cl.bcs.tesoreria.familia_a.support.Caso;

/**
 * Keeps the characterization suite honest: every recorded case directory is exercised by exactly one compiled test, or is listed in
 * {@code pending-cases.txt} with its reason (the modern class does not exist yet: the test lives in src/test-pending); every case has its golden
 * with provenance; nothing is silently skipped. GENERATED together with the test classes.
 */
class TesoreriaAInventarioTest {

    /** One test class per legacy class; a class that is not (yet) a compiled test is covered by pending-cases.txt instead. */
    private static final List<String> CLASES_DE_PRUEBA = List.of(
            "ValidarTransferenciaDVPServiceTest", "CrearIngresoEgresoServiceTest", "LiquidarComprobanteServiceTest", "CrearComprobanteJavaServiceTest",
            "CrearComprobanteServiceTest", "CrearComprobantePartidaInEgServiceTest", "LiquidacionMasivaComprobanteServiceTest",
            "PreLiquidarComprobanteServiceTest", "LiquidarMovimientoAutomaticoServiceTest", "LiquidarComprobanteTodosServiceTest",
            "ModificarComprobanteTesoreriaServiceTest", "GirarComprobanteIngresoServiceTest", "GirarComprobantesEgresoServiceTest",
            "RequisitosGirarComprobanteIngresoServiceTest", "RequisitosGirarComprobanteEgresoServiceTest", "ConsultarIngresoEgresoServiceTest",
            "ConsultarComprobantesServiceTest", "ConsultarComprobantesWSServiceTest", "AnularRegistrosServiceTest",
            "EjecutarAnulacionIngresoEgresoServiceTest", "EjecutarAnulacionComprobantesServiceTest", "AutorizacionLiquidacionServiceTest",
            "AutorizacionAnularMovimientosTesoreriaServiceTest", "ConfirmarRechazarAnularMovimientosTesoreriaServiceTest",
            "ConfirmarAutorizarAnularMovimientosTesoreriaServiceTest", "EdicionMultipleServiceTest", "SaldoPendienteDeLiquidarServiceTest",
            "CrearTraspasoServiceTest", "ActualizarTransaccionServiceTest", "CuadraturaCierreDiarioServiceTest", "TraspasoPojoPendingTest",
            "TraspasoPojoTest");

    @SuppressWarnings("unchecked")
    private static List<String> casosEjecutados() {
        List<String> todos = new ArrayList<>();
        for (String nombre : CLASES_DE_PRUEBA) {
            try {
                java.lang.reflect.Field casos = Class.forName(TesoreriaAInventarioTest.class.getPackageName() + "." + nombre).getDeclaredField("CASOS");
                casos.setAccessible(true);
                todos.addAll((List<String>) casos.get(null));
            } catch (ClassNotFoundException e) {
                // not a compiled test (yet): its cases must be listed in pending-cases.txt
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(nombre + " has no static CASOS list", e);
            }
        }
        return todos;
    }

    @Test
    void cadaDirectorioDeCasoTieneExactamenteUnTest() throws Exception {
        List<String> ejecutados = casosEjecutados();
        Set<String> pendientes = new TreeSet<>();
        Path p = Caso.recurso(Caso.RAIZ + "/pending-cases.txt");
        for (String l : Files.readAllLines(p)) {
            if (!l.isBlank() && !l.startsWith("#")) {
                pendientes.add(l.split("\\s+")[0]);
            }
        }
        Set<String> sinDuplicados = new TreeSet<>(ejecutados);
        assertThat(sinDuplicados).as("a case is run by more than one test").hasSameSizeAs(ejecutados);
        Set<String> enDisco = new TreeSet<>(Caso.todos());
        Set<String> esperados = new TreeSet<>(sinDuplicados);
        esperados.addAll(pendientes);
        assertThat(enDisco).as("case directories vs cases run (+ pending-cases.txt)").isEqualTo(esperados);
    }

    @Test
    void cadaCasoTieneGoldenGrabadoPorElOraculo() throws Exception {
        for (String nombre : Caso.todos()) {
            Path dir = Caso.recurso(Caso.RAIZ + "/cases/" + nombre);
            assertThat(dir.resolve("case.json")).as(nombre + "/case.json").exists();
            Path exp = dir.resolve("expected.json");
            assertThat(exp).as(nombre + "/expected.json (record it with legacy-oracle/record.sh)").exists();
            assertThat(Files.readString(exp)).as(nombre + " provenance").contains("Recorded by legacy-oracle/runner/OracleRunner.java");
        }
    }

    @Test
    void elNumeroDeCasosEjecutadosNoEsCero() {
        assertThat(casosEjecutados().size()).as("equivalence cases wired to tests").isGreaterThan(0);
    }
}
