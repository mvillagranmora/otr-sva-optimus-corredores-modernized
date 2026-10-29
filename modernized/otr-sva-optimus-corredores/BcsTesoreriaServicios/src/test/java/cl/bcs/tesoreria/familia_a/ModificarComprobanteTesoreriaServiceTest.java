package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ModificarComprobanteTesoreriaService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ModificarComprobanteTesoreria} (entry point call(conn, json)) as {@link ModificarComprobanteTesoreriaService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-420 .. TES-A-430} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ModificarComprobanteTesoreriaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-420_mct-modifica-el-monto-de-un-movimiento-de-ingreso-egreso",
            "TES-A-421_mct-modifica-el-monto-de-la-forma-de-pago-del-movimiento",
            "TES-A-422_mct-modifica-el-monto-de-un-comprobante",
            "TES-A-423_mct-modifica-el-monto-de-un-detalle-de-comprobante",
            "TES-A-424_mct-modifica-el-monto-de-una-forma-de-pago-de-comprobante",
            "TES-A-425_mct-modifica-las-cinco-entidades-a-la-vez",
            "TES-A-426_mct-un-campo-distinto-de-Monto-no-modifica-nada",
            "TES-A-427_mct-listas-vacias-no-hacen-nada",
            "TES-A-428_mct-id-inexistente-no-modifica-nada",
            "TES-A-430_mct-lista-ausente-lanza-NPE");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ModificarComprobanteTesoreriaService service = new ModificarComprobanteTesoreriaService(caso.jdbc());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ModificarComprobanteTesoreria");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-420 mct modifica el monto de un movimiento de ingreso egreso")
    void tesA420_mctModificaElMontoDeUnMovimientoDeIngresoEgreso() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-421 mct modifica el monto de la forma de pago del movimiento")
    void tesA421_mctModificaElMontoDeLaFormaDePagoDelMovimiento() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-422 mct modifica el monto de un comprobante")
    void tesA422_mctModificaElMontoDeUnComprobante() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-423 mct modifica el monto de un detalle de comprobante")
    void tesA423_mctModificaElMontoDeUnDetalleDeComprobante() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-424 mct modifica el monto de una forma de pago de comprobante")
    void tesA424_mctModificaElMontoDeUnaFormaDePagoDeComprobante() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-425 mct modifica las cinco entidades a la vez")
    void tesA425_mctModificaLasCincoEntidadesALaVez() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-426 mct un campo distinto de Monto no modifica nada")
    void tesA426_mctUnCampoDistintoDeMontoNoModificaNada() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-427 mct listas vacias no hacen nada")
    void tesA427_mctListasVaciasNoHacenNada() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-428 mct id inexistente no modifica nada")
    void tesA428_mctIdInexistenteNoModificaNada() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-430 mct lista ausente lanza NPE")
    void tesA430_mctListaAusenteLanzaNPE() throws Exception {
        verificar(CASOS.get(9));
    }
}
