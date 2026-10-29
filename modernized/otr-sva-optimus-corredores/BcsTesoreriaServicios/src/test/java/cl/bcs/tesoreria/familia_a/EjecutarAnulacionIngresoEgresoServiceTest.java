package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.EjecutarAnulacionIngresoEgresoService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.EjecutarAnulacionIngresoEgreso} (entry point call(conn, json)) as {@link EjecutarAnulacionIngresoEgresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-440 .. TES-A-444} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class EjecutarAnulacionIngresoEgresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-440_eaie-sin-movimientos-informa-que-no-hay-registros",
            "TES-A-441_eaie-anula-los-movimientos-ingresados-hasta-la-fecha-y-sus-formas-de-pago",
            "TES-A-442_eaie-solo-considera-la-empresa-indicada",
            "TES-A-444_eaie-metodo-logica-igual-que-call");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        EjecutarAnulacionIngresoEgresoService service = new EjecutarAnulacionIngresoEgresoService(caso.jdbc(), d.diccionario());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("EjecutarAnulacionIngresoEgreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-440 eaie sin movimientos informa que no hay registros")
    void tesA440_eaieSinMovimientosInformaQueNoHayRegistros() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-441 eaie anula los movimientos ingresados hasta la fecha y sus formas de pago")
    void tesA441_eaieAnulaLosMovimientosIngresadosHastaLaFechaYSusFormasDePago() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-442 eaie solo considera la empresa indicada")
    void tesA442_eaieSoloConsideraLaEmpresaIndicada() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-444 eaie metodo logica igual que call")
    void tesA444_eaieMetodoLogicaIgualQueCall() throws Exception {
        verificar(CASOS.get(3));
    }
}
