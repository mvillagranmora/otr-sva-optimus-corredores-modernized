package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.EdicionMultipleService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.EdicionMultiple} (entry point call(conn, json)) as {@link EdicionMultipleService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-800 .. TES-A-809} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class EdicionMultipleServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-800_edm-sin-liquidar-devuelve-las-formas-de-pago-y-los-mensajes-del-procedimiento",
            "TES-A-801_edm-con-errores-del-procedimiento-informa-el-contador-y-no-liquida",
            "TES-A-802_edm-liquida-los-comprobantes-editados-cuando-no-hay-errores-ni-advertencias",
            "TES-A-803_edm-con-advertencias-no-liquida",
            "TES-A-804_edm-con-autorizacion-requerida-no-liquida",
            "TES-A-805_edm-solo-con-informacion-igual-liquida",
            "TES-A-806_edm-error-en-la-liquidacion-suma-sus-errores",
            "TES-A-807_edm-comprobante-bloqueado-no-se-liquida-y-informa-el-error",
            "TES-A-808_edm-LiquidaComprobante-distinto-de-S-no-liquida",
            "TES-A-809_edm-sin-formas-de-pago-ni-registros-igual-llama-al-procedimiento");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {"P_OUT_MENSAJES"};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        EdicionMultipleService service = new EdicionMultipleService(caso.jdbc(),
                new LiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores()), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("EdicionMultiple");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-800 edm sin liquidar devuelve las formas de pago y los mensajes del procedimiento")
    void tesA800_edmSinLiquidarDevuelveLasFormasDePagoYLosMensajesDelProcedimiento() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-801 edm con errores del procedimiento informa el contador y no liquida")
    void tesA801_edmConErroresDelProcedimientoInformaElContadorYNoLiquida() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-802 edm liquida los comprobantes editados cuando no hay errores ni advertencias")
    void tesA802_edmLiquidaLosComprobantesEditadosCuandoNoHayErroresNiAdvertencias() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-803 edm con advertencias no liquida")
    void tesA803_edmConAdvertenciasNoLiquida() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-804 edm con autorizacion requerida no liquida")
    void tesA804_edmConAutorizacionRequeridaNoLiquida() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-805 edm solo con informacion igual liquida")
    void tesA805_edmSoloConInformacionIgualLiquida() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-806 edm error en la liquidacion suma sus errores")
    void tesA806_edmErrorEnLaLiquidacionSumaSusErrores() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-807 edm comprobante bloqueado no se liquida y informa el error")
    void tesA807_edmComprobanteBloqueadoNoSeLiquidaYInformaElError() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-808 edm LiquidaComprobante distinto de S no liquida")
    void tesA808_edmLiquidaComprobanteDistintoDeSNoLiquida() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-809 edm sin formas de pago ni registros igual llama al procedimiento")
    void tesA809_edmSinFormasDePagoNiRegistrosIgualLlamaAlProcedimiento() throws Exception {
        verificar(CASOS.get(9));
    }
}
