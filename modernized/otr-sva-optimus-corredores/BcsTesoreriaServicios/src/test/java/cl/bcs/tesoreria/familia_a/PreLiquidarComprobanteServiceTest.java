package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.PreLiquidarComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.PreLiquidarComprobante} (entry point call(conn, json)) as {@link PreLiquidarComprobanteService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-290 .. TES-A-298} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class PreLiquidarComprobanteServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-290_pre-liquida-valida-y-hace-rollback-de-todo-al-terminar",
            "TES-A-291_pre-concepto-que-no-valida-saldo-si-valida-el-flujo-de-caja-logica-invertida-insuficiente-es-error-y-no-hace-rollback",
            "TES-A-292_pre-concepto-que-no-valida-saldo-con-flujo-suficiente-termina-con-rollback",
            "TES-A-293_pre-error-al-actualizar-saldo-de-banco-corta-sin-rollback",
            "TES-A-294_pre-comprobante-inexistente-no-genera-mensajes",
            "TES-A-295_pre-comprobante-bloqueado-no-se-valida-y-se-liquida-en-la-transaccion",
            "TES-A-296_pre-parametro-LIQUIDACIONPARCIALENVIAACTA-ausente-lanza-excepcion",
            "TES-A-297_pre-sin-saldo-de-flujo-del-portafolio-lanza-NPE",
            "TES-A-298_pre-egreso-del-cliente-con-concepto-sin-validacion-y-flujo-insuficiente-es-error");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        PreLiquidarComprobanteService service = new PreLiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("PreLiquidarComprobante");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-290 pre liquida valida y hace rollback de todo al terminar")
    void tesA290_preLiquidaValidaYHaceRollbackDeTodoAlTerminar() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-291 pre concepto que no valida saldo si valida el flujo de caja logica invertida insuficiente es error y no hace rollback")
    void tesA291_preConceptoQueNoValidaSaldoSiValidaElFlujoDeCajaLogicaInvertidaInsuficienteEsErrorYNoHaceRollback() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-292 pre concepto que no valida saldo con flujo suficiente termina con rollback")
    void tesA292_preConceptoQueNoValidaSaldoConFlujoSuficienteTerminaConRollback() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-293 pre error al actualizar saldo de banco corta sin rollback")
    void tesA293_preErrorAlActualizarSaldoDeBancoCortaSinRollback() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-294 pre comprobante inexistente no genera mensajes")
    void tesA294_preComprobanteInexistenteNoGeneraMensajes() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-295 pre comprobante bloqueado no se valida y se liquida en la transaccion")
    void tesA295_preComprobanteBloqueadoNoSeValidaYSeLiquidaEnLaTransaccion() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-296 pre parametro LIQUIDACIONPARCIALENVIAACTA ausente lanza excepcion")
    void tesA296_preParametroLIQUIDACIONPARCIALENVIAACTAAusenteLanzaExcepcion() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-297 pre sin saldo de flujo del portafolio lanza NPE")
    void tesA297_preSinSaldoDeFlujoDelPortafolioLanzaNPE() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-298 pre egreso del cliente con concepto sin validacion y flujo insuficiente es error")
    void tesA298_preEgresoDelClienteConConceptoSinValidacionYFlujoInsuficienteEsError() throws Exception {
        verificar(CASOS.get(8));
    }
}
