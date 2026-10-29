package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.RequisitosGirarComprobanteEgresoService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.RequisitosGirarComprobanteEgreso} (entry point call(conn, json)) as {@link RequisitosGirarComprobanteEgresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-530 .. TES-A-551} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class RequisitosGirarComprobanteEgresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-530_rge-egreso-liquidado-valido-informa-datos-y-medios",
            "TES-A-531_rge-sin-comprobantes-es-error",
            "TES-A-532_rge-comprobante-inexistente-es-invalido-e-inexistente",
            "TES-A-533_rge-comprobante-ingresado-no-esta-liquidado-es-error",
            "TES-A-534_rge-comprobante-bloqueado-es-error",
            "TES-A-535_rge-comprobante-anulado-es-error",
            "TES-A-536_rge-comprobante-de-ingreso-es-error",
            "TES-A-537_rge-partida-de-egreso-PAREGR-es-error",
            "TES-A-538_rge-fechas-de-liquidacion-distintas-es-error",
            "TES-A-539_rge-mueve-cuenta-inversion-distinto-es-error",
            "TES-A-540_rge-forma-de-pago-ya-asignada-a-una-partida-es-error",
            "TES-A-541_rge-cuenta-bancaria-del-socio-inexistente-es-error",
            "TES-A-542_rge-banco-del-socio-inexistente-es-error",
            "TES-A-543_rge-monedas-distintas-es-error",
            "TES-A-544_rge-comprobante-sin-forma-de-pago-es-error",
            "TES-A-545_rge-formas-de-pago-liquidadas-si-cuentan-pero-las-giradas-no",
            "TES-A-546_rge-cajas-distintas-es-error",
            "TES-A-547_rge-cuenta-de-la-corredora-sin-persona-en-el-banco-es-error",
            "TES-A-548_rge-conceptos-distintos-informa-MULCPT",
            "TES-A-549_rge-medios-de-pago-incluyen-formas-de-pago-liquidadas",
            "TES-A-550_rge-varios-errores-de-distintas-validaciones-se-acumulan",
            "TES-A-551_rge-lista-de-comprobantes-ausente-lanza-SQLException-del-batch");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {"P_OUT_ERRORESDETALLE", "p_out_MediosDePagoAGirar"};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        RequisitosGirarComprobanteEgresoService service = new RequisitosGirarComprobanteEgresoService(caso.jdbc(), d.diccionario());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("RequisitosGirarComprobanteEgreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-530 rge egreso liquidado valido informa datos y medios")
    void tesA530_rgeEgresoLiquidadoValidoInformaDatosYMedios() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-531 rge sin comprobantes es error")
    void tesA531_rgeSinComprobantesEsError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-532 rge comprobante inexistente es invalido e inexistente")
    void tesA532_rgeComprobanteInexistenteEsInvalidoEInexistente() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-533 rge comprobante ingresado no esta liquidado es error")
    void tesA533_rgeComprobanteIngresadoNoEstaLiquidadoEsError() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-534 rge comprobante bloqueado es error")
    void tesA534_rgeComprobanteBloqueadoEsError() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-535 rge comprobante anulado es error")
    void tesA535_rgeComprobanteAnuladoEsError() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-536 rge comprobante de ingreso es error")
    void tesA536_rgeComprobanteDeIngresoEsError() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-537 rge partida de egreso PAREGR es error")
    void tesA537_rgePartidaDeEgresoPAREGREsError() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-538 rge fechas de liquidacion distintas es error")
    void tesA538_rgeFechasDeLiquidacionDistintasEsError() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-539 rge mueve cuenta inversion distinto es error")
    void tesA539_rgeMueveCuentaInversionDistintoEsError() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-540 rge forma de pago ya asignada a una partida es error")
    void tesA540_rgeFormaDePagoYaAsignadaAUnaPartidaEsError() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-541 rge cuenta bancaria del socio inexistente es error")
    void tesA541_rgeCuentaBancariaDelSocioInexistenteEsError() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-542 rge banco del socio inexistente es error")
    void tesA542_rgeBancoDelSocioInexistenteEsError() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-543 rge monedas distintas es error")
    void tesA543_rgeMonedasDistintasEsError() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-544 rge comprobante sin forma de pago es error")
    void tesA544_rgeComprobanteSinFormaDePagoEsError() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-545 rge formas de pago liquidadas si cuentan pero las giradas no")
    void tesA545_rgeFormasDePagoLiquidadasSiCuentanPeroLasGiradasNo() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-546 rge cajas distintas es error")
    void tesA546_rgeCajasDistintasEsError() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-547 rge cuenta de la corredora sin persona en el banco es error")
    void tesA547_rgeCuentaDeLaCorredoraSinPersonaEnElBancoEsError() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-548 rge conceptos distintos informa MULCPT")
    void tesA548_rgeConceptosDistintosInformaMULCPT() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-549 rge medios de pago incluyen formas de pago liquidadas")
    void tesA549_rgeMediosDePagoIncluyenFormasDePagoLiquidadas() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-550 rge varios errores de distintas validaciones se acumulan")
    void tesA550_rgeVariosErroresDeDistintasValidacionesSeAcumulan() throws Exception {
        verificar(CASOS.get(20));
    }

    @Test
    @DisplayName("TES-A-551 rge lista de comprobantes ausente lanza SQLException del batch")
    void tesA551_rgeListaDeComprobantesAusenteLanzaSQLExceptionDelBatch() throws Exception {
        verificar(CASOS.get(21));
    }
}
