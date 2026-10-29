package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.GirarComprobantesEgresoService;
import cl.bcs.tesoreria.javacall.CrearComprobanteJavaService;
import cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.GirarComprobantesEgreso} (entry point call(conn, json)) as {@link GirarComprobantesEgresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-580 .. TES-A-592} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class GirarComprobantesEgresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-580_gce-gira-un-egreso-liquidado-y-asigna-la-partida-a-sus-formas-de-pago",
            "TES-A-581_gce-sin-resultado-ING-del-procedimiento-la-partida-queda-en-cero",
            "TES-A-582_gce-genera-nomina-y-detalle-para-las-formas-de-pago-liquidadas",
            "TES-A-583_gce-genera-transferencia-y-detalle",
            "TES-A-584_gce-genera-nomina-y-transferencia-a-la-vez",
            "TES-A-585_gce-GeneraNomina-ausente-lanza-NPE-que-se-traga-despues-de-actualizar-las-formas-de-pago",
            "TES-A-586_gce-nomina-con-cuenta-inexistente-falla-sin-insertar-la-nomina",
            "TES-A-587_gce-solo-gira-las-formas-de-pago-del-medio-indicado-y-no-las-anuladas-ni-modificadas",
            "TES-A-588_gce-error-del-procedimiento-devuelve-sus-mensajes-sin-limpiar-las-globales-restantes",
            "TES-A-589_gce-dos-comprobantes-comparten-la-misma-partida",
            "TES-A-590_gce-GeneraTransferencia-ausente-lanza-NPE-que-se-traga-despues-de-la-nomina",
            "TES-A-591_gce-formas-de-pago-ausentes-lanza-NPE-que-se-traga",
            "TES-A-592_gce-forma-de-pago-ya-girada-GIR-tambien-se-reasigna");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static CrearComprobanteService crearComprobante(Caso caso, Dobles d) {
        CrearComprobanteProcedimiento procedimiento = new CrearComprobanteProcedimiento(caso.jdbc(), d.colaboradores());
        CrearComprobanteJavaService java = new CrearComprobanteJavaService(caso.jdbc(), d.diccionario(), procedimiento, d.colaboradores());
        return new CrearComprobanteService(d.configuracion(), procedimiento, java);
    }

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        GirarComprobantesEgresoService service = new GirarComprobantesEgresoService(caso.jdbc(), crearComprobante(caso, d));
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GirarComprobantesEgreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-580 gce gira un egreso liquidado y asigna la partida a sus formas de pago")
    void tesA580_gceGiraUnEgresoLiquidadoYAsignaLaPartidaASusFormasDePago() {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-581 gce sin resultado ING del procedimiento la partida queda en cero")
    void tesA581_gceSinResultadoINGDelProcedimientoLaPartidaQuedaEnCero() {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-582 gce genera nomina y detalle para las formas de pago liquidadas")
    void tesA582_gceGeneraNominaYDetalleParaLasFormasDePagoLiquidadas() {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-583 gce genera transferencia y detalle")
    void tesA583_gceGeneraTransferenciaYDetalle() {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-584 gce genera nomina y transferencia a la vez")
    void tesA584_gceGeneraNominaYTransferenciaALaVez() {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-585 gce GeneraNomina ausente lanza NPE que se traga despues de actualizar las formas de pago")
    void tesA585_gceGeneraNominaAusenteLanzaNPEQueSeTragaDespuesDeActualizarLasFormasDePago() {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-586 gce nomina con cuenta inexistente falla sin insertar la nomina")
    void tesA586_gceNominaConCuentaInexistenteFallaSinInsertarLaNomina() {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-587 gce solo gira las formas de pago del medio indicado y no las anuladas ni modificadas")
    void tesA587_gceSoloGiraLasFormasDePagoDelMedioIndicadoYNoLasAnuladasNiModificadas() {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-588 gce error del procedimiento devuelve sus mensajes sin limpiar las globales restantes")
    void tesA588_gceErrorDelProcedimientoDevuelveSusMensajesSinLimpiarLasGlobalesRestantes() {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-589 gce dos comprobantes comparten la misma partida")
    void tesA589_gceDosComprobantesCompartenLaMismaPartida() {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-590 gce GeneraTransferencia ausente lanza NPE que se traga despues de la nomina")
    void tesA590_gceGeneraTransferenciaAusenteLanzaNPEQueSeTragaDespuesDeLaNomina() {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-591 gce formas de pago ausentes lanza NPE que se traga")
    void tesA591_gceFormasDePagoAusentesLanzaNPEQueSeTraga() {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-592 gce forma de pago ya girada GIR tambien se reasigna")
    void tesA592_gceFormaDePagoYaGiradaGIRTambienSeReasigna() {
        verificar(CASOS.get(12));
    }
}
