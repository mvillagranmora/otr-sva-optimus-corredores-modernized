package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteTodosService;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.LiquidarComprobanteTodos} (entry point call(conn, json)) as {@link LiquidarComprobanteTodosService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-400 .. TES-A-412} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class LiquidarComprobanteTodosServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-400_lct-sin-comprobantes-en-la-grilla-advierte",
            "TES-A-401_lct-liquida-todos-los-comprobantes-de-la-grilla",
            "TES-A-402_lct-el-filtro-de-tipo-de-movimiento-excluye-los-otros",
            "TES-A-403_lct-comprobantes-liquidados-no-entran-a-la-grilla",
            "TES-A-404_lct-el-filtro-de-estado-y-el-rango-de-fechas-de-liquidacion-acotan-la-grilla",
            "TES-A-405_lct-filtros-por-persona-portafolio-caja-moneda-concepto-y-medio-de-pago",
            "TES-A-406_lct-filtros-de-banco-alias-y-cuenta-de-la-corredora",
            "TES-A-407_lct-filtro-de-fecha-del-comprobante-origen",
            "TES-A-408_lct-error-de-cuenta-inversion-hace-rollback-de-la-liquidacion",
            "TES-A-409_lct-error-de-saldo-de-banco-hace-rollback-de-la-liquidacion",
            "TES-A-410_lct-comprobante-invalido-sin-contador-hace-rollback-y-devuelve-contador-nulo",
            "TES-A-411_lct-sin-filtros-lanza-NPE",
            "TES-A-412_lct-metodo-logica-igual-que-call");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        LiquidarComprobanteService liquidar = new LiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        LiquidarComprobanteTodosService service = new LiquidarComprobanteTodosService(caso.jdbc(), liquidar);
        // TES-A-412 records the typed logica; for this class it is the same code path as the JSON call
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> "logica".equals(caso.metodo())
                ? JsonLegacy.PLATAFORMA.toJsonTree(service.logica(JsonLegacy.PLATAFORMA.fromJson(caso.entrada().deepCopy(),
                        LiquidarComprobanteTodosService.LiquidarComprobanteTodos_Entrada.class)))
                : service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("LiquidarComprobanteTodos");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-400 lct sin comprobantes en la grilla advierte")
    void tesA400_lctSinComprobantesEnLaGrillaAdvierte() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-401 lct liquida todos los comprobantes de la grilla")
    void tesA401_lctLiquidaTodosLosComprobantesDeLaGrilla() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-402 lct el filtro de tipo de movimiento excluye los otros")
    void tesA402_lctElFiltroDeTipoDeMovimientoExcluyeLosOtros() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-403 lct comprobantes liquidados no entran a la grilla")
    void tesA403_lctComprobantesLiquidadosNoEntranALaGrilla() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-404 lct el filtro de estado y el rango de fechas de liquidacion acotan la grilla")
    void tesA404_lctElFiltroDeEstadoYElRangoDeFechasDeLiquidacionAcotanLaGrilla() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-405 lct filtros por persona portafolio caja moneda concepto y medio de pago")
    void tesA405_lctFiltrosPorPersonaPortafolioCajaMonedaConceptoYMedioDePago() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-406 lct filtros de banco alias y cuenta de la corredora")
    void tesA406_lctFiltrosDeBancoAliasYCuentaDeLaCorredora() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-407 lct filtro de fecha del comprobante origen")
    void tesA407_lctFiltroDeFechaDelComprobanteOrigen() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-408 lct error de cuenta inversion hace rollback de la liquidacion")
    void tesA408_lctErrorDeCuentaInversionHaceRollbackDeLaLiquidacion() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-409 lct error de saldo de banco hace rollback de la liquidacion")
    void tesA409_lctErrorDeSaldoDeBancoHaceRollbackDeLaLiquidacion() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-410 lct comprobante invalido sin contador hace rollback y devuelve contador nulo")
    void tesA410_lctComprobanteInvalidoSinContadorHaceRollbackYDevuelveContadorNulo() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-411 lct sin filtros lanza NPE")
    void tesA411_lctSinFiltrosLanzaNPE() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-412 lct metodo logica igual que call")
    void tesA412_lctMetodoLogicaIgualQueCall() throws Exception {
        verificar(CASOS.get(12));
    }
}
