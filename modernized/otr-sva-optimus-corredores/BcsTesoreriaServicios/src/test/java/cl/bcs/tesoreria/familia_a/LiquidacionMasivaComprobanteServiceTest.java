package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.LiquidacionMasivaComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.LiquidacionMasivaComprobante} (entry point call(conn, json)) as {@link LiquidacionMasivaComprobanteService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-270 .. TES-A-279} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class LiquidacionMasivaComprobanteServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-270_lmc-folio-valido-con-banco-coincidente-modifica-forma-de-pago-y-lista-lo-que-liquidar",
            "TES-A-271_lmc-folio-sin-comprobante-asociado-es-error",
            "TES-A-272_lmc-suma-de-tesoreria-distinta-del-monto-de-la-factura-es-error",
            "TES-A-273_lmc-cantidad-de-factura-distinta-de-la-de-movimientos-es-error",
            "TES-A-274_lmc-comprobante-ya-liquidado-es-error",
            "TES-A-275_lmc-sin-lista-de-bancos-lanza-NPE",
            "TES-A-276_lmc-banco-que-no-coincide-no-modifica-formas-de-pago",
            "TES-A-277_lmc-dos-detalles-del-mismo-folio-suman-contra-la-factura",
            "TES-A-278_lmc-dos-folios-liquida-ambos-comprobantes",
            "TES-A-279_lmc-sin-comprobantes-de-entrada-devuelve-error-de-folio");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        LiquidacionMasivaComprobanteService service = new LiquidacionMasivaComprobanteService(caso.jdbc(), d.configuracion(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("LiquidacionMasivaComprobante");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-270 lmc folio valido con banco coincidente modifica forma de pago y lista lo que liquidar")
    void tesA270_lmcFolioValidoConBancoCoincidenteModificaFormaDePagoYListaLoQueLiquidar() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-271 lmc folio sin comprobante asociado es error")
    void tesA271_lmcFolioSinComprobanteAsociadoEsError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-272 lmc suma de tesoreria distinta del monto de la factura es error")
    void tesA272_lmcSumaDeTesoreriaDistintaDelMontoDeLaFacturaEsError() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-273 lmc cantidad de factura distinta de la de movimientos es error")
    void tesA273_lmcCantidadDeFacturaDistintaDeLaDeMovimientosEsError() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-274 lmc comprobante ya liquidado es error")
    void tesA274_lmcComprobanteYaLiquidadoEsError() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-275 lmc sin lista de bancos lanza NPE")
    void tesA275_lmcSinListaDeBancosLanzaNPE() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-276 lmc banco que no coincide no modifica formas de pago")
    void tesA276_lmcBancoQueNoCoincideNoModificaFormasDePago() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-277 lmc dos detalles del mismo folio suman contra la factura")
    void tesA277_lmcDosDetallesDelMismoFolioSumanContraLaFactura() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-278 lmc dos folios liquida ambos comprobantes")
    void tesA278_lmcDosFoliosLiquidaAmbosComprobantes() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-279 lmc sin comprobantes de entrada devuelve error de folio")
    void tesA279_lmcSinComprobantesDeEntradaDevuelveErrorDeFolio() throws Exception {
        verificar(CASOS.get(9));
    }
}
