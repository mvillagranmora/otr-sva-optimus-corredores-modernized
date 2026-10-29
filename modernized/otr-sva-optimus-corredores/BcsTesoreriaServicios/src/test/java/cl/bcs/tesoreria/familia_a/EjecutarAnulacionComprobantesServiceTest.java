package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.AnularRegistrosService;
import cl.bcs.tesoreria.javacall.EjecutarAnulacionComprobantesService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.EjecutarAnulacionComprobantes} (entry point call(conn, json)) as {@link EjecutarAnulacionComprobantesService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-450 .. TES-A-453} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class EjecutarAnulacionComprobantesServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-450_eaco-sin-comprobantes-informa-que-no-hay-registros",
            "TES-A-451_eaco-anula-los-comprobantes-ingresados-con-liquidacion-en-la-fecha",
            "TES-A-452_eaco-comprobante-con-requisito-de-autorizacion-devuelve-los-mensajes-de-anular-registros",
            "TES-A-453_eaco-requisitos-basicos-con-error-corta");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        AnularRegistrosService anular = new AnularRegistrosService(caso.jdbc(), d.diccionario(), d.colaboradores());
        EjecutarAnulacionComprobantesService service = new EjecutarAnulacionComprobantesService(caso.jdbc(), anular);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("EjecutarAnulacionComprobantes");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-450 eaco sin comprobantes informa que no hay registros")
    void tesA450_eacoSinComprobantesInformaQueNoHayRegistros() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-451 eaco anula los comprobantes ingresados con liquidacion en la fecha")
    void tesA451_eacoAnulaLosComprobantesIngresadosConLiquidacionEnLaFecha() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-452 eaco comprobante con requisito de autorizacion devuelve los mensajes de anular registros")
    void tesA452_eacoComprobanteConRequisitoDeAutorizacionDevuelveLosMensajesDeAnularRegistros() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-453 eaco requisitos basicos con error corta")
    void tesA453_eacoRequisitosBasicosConErrorCorta() throws Exception {
        verificar(CASOS.get(3));
    }
}
