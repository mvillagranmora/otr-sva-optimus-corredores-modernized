package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ValidarTransferenciaDVPService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ValidarTransferenciaDVP} (entry point call(conn, json)) as {@link ValidarTransferenciaDVPService#execute(com.google.gson.JsonObject)}.
 * Validates TDP (securities transfer) nominas against the DCV key held by custody.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-01 .. TES-A-17} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ValidarTransferenciaDVPServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-01_dvp-medio-distinto-de-TDP-no-valida",
            "TES-A-02_dvp-lista-vacia-es-valido",
            "TES-A-03_dvp-egreso-manual-sin-tipo-operacion-es-valido",
            "TES-A-04_dvp-tipo-operacion-en-blanco-es-valido",
            "TES-A-05_dvp-sin-clave-DCV-es-error",
            "TES-A-06_dvp-clave-DCV-nula-es-error",
            "TES-A-07_dvp-claves-coinciden-es-valido",
            "TES-A-08_dvp-clave-no-coincide-con-custodia-es-error",
            "TES-A-09_dvp-monto-nomina-distinto-del-monto-custodia-es-error",
            "TES-A-10_dvp-nomina-recepcionada-es-error",
            "TES-A-11_dvp-nomina-anulada-es-error-y-corta-el-ciclo",
            "TES-A-12_dvp-error-seguido-de-nomina-no-TDP-el-ultimo-gana",
            "TES-A-13_dvp-estado-no-contemplado-no-cambia-el-resultado",
            "TES-A-14_dvp-nomina-inexistente-lanza-IndexOutOfBounds",
            "TES-A-15_dvp-medio-de-pago-nulo-lanza-NPE",
            "TES-A-16_dvp-logica-dos-errores-contador-queda-en-1",
            "TES-A-17_dvp-call-dos-errores-contador-recontado-en-2");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ValidarTransferenciaDVPService service = new ValidarTransferenciaDVPService(caso.jdbc(), d.diccionario());
        // the recorded entry point is the legacy typed logica (TES-A-16) or the JSON call (everything else)
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> "logica".equals(caso.metodo())
                ? JsonLegacy.PLATAFORMA.toJsonTree(service.logica(JsonLegacy.PLATAFORMA.fromJson(caso.entrada().deepCopy(), ValidarTransferenciaDVPService.Entrada.class)))
                : service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ValidarTransferenciaDVP");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ValidarTransferenciaDVP.java:96,152-154: a payment medium other than TDP is not validated (valid S, no query)
    @Test
    @DisplayName("TES-A-01 dvp medio distinto de TDP no valida")
    void tesA01_dvpMedioDistintoDeTDPNoValida() throws Exception {
        verificar(CASOS.get(0));
    }

    // ValidarTransferenciaDVP.java:92,94: empty list keeps the initial P_OUT_VALIDO = S
    @Test
    @DisplayName("TES-A-02 dvp lista vacia es valido")
    void tesA02_dvpListaVaciaEsValido() throws Exception {
        verificar(CASOS.get(1));
    }

    // ValidarTransferenciaDVP.java:102-106: ING with a NULL IdTipoOperacion (manual egreso) is valid S
    @Test
    @DisplayName("TES-A-03 dvp egreso manual sin tipo operacion es valido")
    void tesA03_dvpEgresoManualSinTipoOperacionEsValido() throws Exception {
        verificar(CASOS.get(2));
    }

    // ValidarTransferenciaDVP.java:104-106: ING with a blank IdTipoOperacion (trim) is valid S
    @Test
    @DisplayName("TES-A-04 dvp tipo operacion en blanco es valido")
    void tesA04_dvpTipoOperacionEnBlancoEsValido() throws Exception {
        verificar(CASOS.get(3));
    }

    // ValidarTransferenciaDVP.java:108-114: empty ClaveDCV -> N + error UI_DEBE_AGREGAR_CLAVE_DCV
    @Test
    @DisplayName("TES-A-05 dvp sin clave DCV es error")
    void tesA05_dvpSinClaveDCVEsError() throws Exception {
        verificar(CASOS.get(4));
    }

    // ValidarTransferenciaDVP.java:108-114: NULL ClaveDCV -> N + error UI_DEBE_AGREGAR_CLAVE_DCV
    @Test
    @DisplayName("TES-A-06 dvp clave DCV nula es error")
    void tesA06_dvpClaveDCVNulaEsError() throws Exception {
        verificar(CASOS.get(5));
    }

    // ValidarTransferenciaDVP.java:116-131: DCV key, amount and customer match the custody operation -> S
    @Test
    @DisplayName("TES-A-07 dvp claves coinciden es valido")
    void tesA07_dvpClavesCoincidenEsValido() throws Exception {
        verificar(CASOS.get(6));
    }

    // ValidarTransferenciaDVP.java:120-129: DCV key without a matching custody operation -> N + error NO_COINCIDE
    @Test
    @DisplayName("TES-A-08 dvp clave no coincide con custodia es error")
    void tesA08_dvpClaveNoCoincideConCustodiaEsError() throws Exception {
        verificar(CASOS.get(7));
    }

    // ValidarTransferenciaDVP.java:120-129 + ValidarTransferenciaDVP.sql: the nomina amount must equal the custody amount, otherwise no match -> N + error
    @Test
    @DisplayName("TES-A-09 dvp monto nomina distinto del monto custodia es error")
    void tesA09_dvpMontoNominaDistintoDelMontoCustodiaEsError() throws Exception {
        verificar(CASOS.get(8));
    }

    // ValidarTransferenciaDVP.java:135-141: nomina RCP (received) -> N + error NOMINA_TRANSFERENCIA_RECEPCIONADO
    @Test
    @DisplayName("TES-A-10 dvp nomina recepcionada es error")
    void tesA10_dvpNominaRecepcionadaEsError() throws Exception {
        verificar(CASOS.get(9));
    }

    // ValidarTransferenciaDVP.java:142-148: nomina ANU (annulled) -> N + error and break: the second nomina is never looked at
    @Test
    @DisplayName("TES-A-11 dvp nomina anulada es error y corta el ciclo")
    void tesA11_dvpNominaAnuladaEsErrorYCortaElCiclo() throws Exception {
        verificar(CASOS.get(10));
    }

    // ValidarTransferenciaDVP.java:92,135-141,152-154: LEGACY DEFECT: an RCP error followed by a non-TDP item resets P_OUT_VALIDO to S (last item wins), error message stays
    @Test
    @DisplayName("TES-A-12 dvp error seguido de nomina no TDP el ultimo gana")
    void tesA12_dvpErrorSeguidoDeNominaNoTDPElUltimoGana() throws Exception {
        verificar(CASOS.get(11));
    }

    // ValidarTransferenciaDVP.java:102-148: a state other than ING/RCP/ANU matches no branch: nothing changes (S)
    @Test
    @DisplayName("TES-A-13 dvp estado no contemplado no cambia el resultado")
    void tesA13_dvpEstadoNoContempladoNoCambiaElResultado() throws Exception {
        verificar(CASOS.get(12));
    }

    // ValidarTransferenciaDVP.java:102: unknown nomina -> get(0) on an empty list throws IndexOutOfBoundsException (not handled)
    @Test
    @DisplayName("TES-A-14 dvp nomina inexistente lanza IndexOutOfBounds")
    void tesA14_dvpNominaInexistenteLanzaIndexOutOfBounds() throws Exception {
        verificar(CASOS.get(13));
    }

    // ValidarTransferenciaDVP.java:96: NULL IdMedioDePago -> NullPointerException (not handled)
    @Test
    @DisplayName("TES-A-15 dvp medio de pago nulo lanza NPE")
    void tesA15_dvpMedioDePagoNuloLanzaNPE() throws Exception {
        verificar(CASOS.get(14));
    }

    // ValidarTransferenciaDVP.java:109-113,136-140 (logica): every error branch sets P_OUT_ERRORESCONTADOR = 1, two errors still give 1
    @Test
    @DisplayName("TES-A-16 dvp logica dos errores contador queda en 1")
    void tesA16_dvpLogicaDosErroresContadorQuedaEn1() throws Exception {
        verificar(CASOS.get(15));
    }

    // ValidarTransferenciaDVP.java:167 (call): P_OUT_ERRORESCONTADOR is recomputed from the messages (2), unlike logica
    @Test
    @DisplayName("TES-A-17 dvp call dos errores contador recontado en 2")
    void tesA17_dvpCallDosErroresContadorRecontadoEn2() throws Exception {
        verificar(CASOS.get(16));
    }
}
