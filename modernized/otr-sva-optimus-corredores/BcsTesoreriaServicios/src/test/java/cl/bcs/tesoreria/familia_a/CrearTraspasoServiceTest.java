package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.CrearTraspasoService;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearTraspaso} (entry point call(conn, json)) as {@link CrearTraspasoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-740 .. TES-A-752} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class CrearTraspasoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-740_ctr-traspaso-valido-crea-ingreso-y-egreso-y-los-relaciona",
            "TES-A-741_ctr-ingreso-sin-egreso-asociado-es-error",
            "TES-A-742_ctr-egreso-sin-ingreso-asociado-continua-y-falla-al-crear",
            "TES-A-743_ctr-ingreso-con-egreso-pero-sin-formas-de-pago-requiere-forma-de-pago",
            "TES-A-744_ctr-error-al-crear-el-ingreso-no-crea-el-egreso",
            "TES-A-745_ctr-error-al-crear-el-egreso-deja-el-ingreso-creado",
            "TES-A-746_ctr-simular-S-no-inserta",
            "TES-A-747_ctr-dos-traspasos-en-la-misma-solicitud",
            "TES-A-748_ctr-referencias-con-igual-valor-absoluto-se-asocian-aunque-tengan-el-mismo-signo",
            "TES-A-749_ctr-forma-de-pago-ausente-lanza-NPE",
            "TES-A-750_ctr-movimientos-ausentes-lanza-NPE",
            "TES-A-751_ctr-requisitos-basicos-con-error-en-el-ingreso-corta-el-proceso",
            "TES-A-752_ctr-solo-movimientos-de-egreso-sin-formas-de-pago-matching-no-llega-a-formas-del-egreso");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        try (var ctx = CieContexto.de(caso, d, CrearTraspasoService.class, CrearIngresoEgresoService.class, CrearComprobanteService.class, LiquidarComprobanteService.class)) {
            CrearTraspasoService service = ctx.getBean(CrearTraspasoService.class);
            Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
            EJECUTADOS.ejecutado(nombre);
            Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
        }
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-740 ctr traspaso valido crea ingreso y egreso y los relaciona")
    void tesA740_ctrTraspasoValidoCreaIngresoYEgresoYLosRelaciona() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-741 ctr ingreso sin egreso asociado es error")
    void tesA741_ctrIngresoSinEgresoAsociadoEsError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-742 ctr egreso sin ingreso asociado continua y falla al crear")
    void tesA742_ctrEgresoSinIngresoAsociadoContinuaYFallaAlCrear() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-743 ctr ingreso con egreso pero sin formas de pago requiere forma de pago")
    void tesA743_ctrIngresoConEgresoPeroSinFormasDePagoRequiereFormaDePago() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-744 ctr error al crear el ingreso no crea el egreso")
    void tesA744_ctrErrorAlCrearElIngresoNoCreaElEgreso() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-745 ctr error al crear el egreso deja el ingreso creado")
    void tesA745_ctrErrorAlCrearElEgresoDejaElIngresoCreado() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-746 ctr simular S no inserta")
    void tesA746_ctrSimularSNoInserta() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-747 ctr dos traspasos en la misma solicitud")
    void tesA747_ctrDosTraspasosEnLaMismaSolicitud() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-748 ctr referencias con igual valor absoluto se asocian aunque tengan el mismo signo")
    void tesA748_ctrReferenciasConIgualValorAbsolutoSeAsocianAunqueTenganElMismoSigno() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-749 ctr forma de pago ausente lanza NPE")
    void tesA749_ctrFormaDePagoAusenteLanzaNPE() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-750 ctr movimientos ausentes lanza NPE")
    void tesA750_ctrMovimientosAusentesLanzaNPE() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-751 ctr requisitos basicos con error en el ingreso corta el proceso")
    void tesA751_ctrRequisitosBasicosConErrorEnElIngresoCortaElProceso() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-752 ctr solo movimientos de egreso sin formas de pago matching no llega a formas del egreso")
    void tesA752_ctrSoloMovimientosDeEgresoSinFormasDePagoMatchingNoLlegaAFormasDelEgreso() throws Exception {
        verificar(CASOS.get(12));
    }
}
