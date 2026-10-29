package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.AnularRegistrosService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.AnularRegistros} (entry point call(conn, json)) as {@link AnularRegistrosService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-300 .. TES-A-337} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class AnularRegistrosServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-300_anr-anula-un-movimiento-ingresado-y-su-forma-de-pago",
            "TES-A-301_anr-movimiento-inexistente-es-error",
            "TES-A-302_anr-movimiento-bloqueado-es-error",
            "TES-A-303_anr-movimiento-ya-anulado-es-error",
            "TES-A-304_anr-movimiento-emitido-de-otro-modulo-es-error",
            "TES-A-305_anr-movimiento-emitido-de-mesa-MRF-desde-TES-con-parametro-N-no-se-puede-anular",
            "TES-A-306_anr-movimiento-emitido-de-mesa-MRF-desde-MRF-se-anula",
            "TES-A-307_anr-movimiento-con-evento-vigente-sin-autorizar-es-error",
            "TES-A-308_anr-movimiento-con-evento-vencido-es-error",
            "TES-A-309_anr-movimiento-con-evento-autorizado-se-anula",
            "TES-A-310_anr-movimiento-que-requiere-autorizacion-crea-evento-y-no-se-anula",
            "TES-A-311_anr-error-al-crear-el-evento-de-autorizacion-corta",
            "TES-A-312_anr-dos-movimientos-uno-requiere-autorizacion-y-otro-ya-anulado",
            "TES-A-313_anr-anula-comprobante-ingresado-libera-el-movimiento-original",
            "TES-A-314_anr-comprobante-inexistente-es-error",
            "TES-A-315_anr-comprobante-bloqueado-es-error",
            "TES-A-316_anr-comprobante-ya-anulado-es-error",
            "TES-A-317_anr-comprobante-liquidado-requiere-autorizacion-crea-evento",
            "TES-A-318_anr-comprobante-liquidado-sin-requisito-de-autorizacion-con-evento-vigente-es-error",
            "TES-A-319_anr-comprobante-liquidado-con-evento-rechazado-es-error",
            "TES-A-320_anr-comprobante-liquidado-con-evento-autorizado-se-anula",
            "TES-A-321_anr-comprobante-liquidado-spot-desde-otro-modulo-es-error",
            "TES-A-322_anr-comprobante-liquidado-desde-facturacion-es-error",
            "TES-A-323_anr-anula-comprobante-desde-otro-modulo-obtiene-los-movimientos-y-no-libera",
            "TES-A-324_anr-comprobante-en-PDA-con-evento-vigente-rechaza-la-autorizacion-de-liquidacion",
            "TES-A-325_anr-anula-comprobante-con-deposito-y-anula-el-deposito",
            "TES-A-326_anr-comprobante-con-nomina-de-transferencia-llama-al-servicio-de-nominas",
            "TES-A-327_anr-comprobante-con-cargo-abono-de-cuenta-inversion-reversa-con-el-servicio-de-cuenta-inversion",
            "TES-A-328_anr-reversa-de-cuenta-inversion-con-error-se-suma-al-contador",
            "TES-A-329_anr-liberar-con-modulo-MRF-deja-los-originales-en-ANU",
            "TES-A-330_anr-fecha-de-movimiento-invalida-es-error",
            "TES-A-331_anr-sin-registros-es-error",
            "TES-A-332_anr-registro-con-id-cero-es-error",
            "TES-A-333_anr-entidad-invalida-es-error",
            "TES-A-334_anr-requisitos-basicos-con-error-corta",
            "TES-A-335_anr-sin-reglas-de-negocio-anula-un-movimiento-bloqueado",
            "TES-A-336_anr-movimiento-y-comprobante-en-la-misma-solicitud",
            "TES-A-337_anr-metodo-logica-igual-que-call");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        AnularRegistrosService service = new AnularRegistrosService(caso.jdbc(), d.diccionario(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AnularRegistros");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-300 anr anula un movimiento ingresado y su forma de pago")
    void tesA300_anrAnulaUnMovimientoIngresadoYSuFormaDePago() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-301 anr movimiento inexistente es error")
    void tesA301_anrMovimientoInexistenteEsError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-302 anr movimiento bloqueado es error")
    void tesA302_anrMovimientoBloqueadoEsError() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-303 anr movimiento ya anulado es error")
    void tesA303_anrMovimientoYaAnuladoEsError() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-304 anr movimiento emitido de otro modulo es error")
    void tesA304_anrMovimientoEmitidoDeOtroModuloEsError() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-305 anr movimiento emitido de mesa MRF desde TES con parametro N no se puede anular")
    void tesA305_anrMovimientoEmitidoDeMesaMRFDesdeTESConParametroNNoSePuedeAnular() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-306 anr movimiento emitido de mesa MRF desde MRF se anula")
    void tesA306_anrMovimientoEmitidoDeMesaMRFDesdeMRFSeAnula() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-307 anr movimiento con evento vigente sin autorizar es error")
    void tesA307_anrMovimientoConEventoVigenteSinAutorizarEsError() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-308 anr movimiento con evento vencido es error")
    void tesA308_anrMovimientoConEventoVencidoEsError() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-309 anr movimiento con evento autorizado se anula")
    void tesA309_anrMovimientoConEventoAutorizadoSeAnula() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-310 anr movimiento que requiere autorizacion crea evento y no se anula")
    void tesA310_anrMovimientoQueRequiereAutorizacionCreaEventoYNoSeAnula() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-311 anr error al crear el evento de autorizacion corta")
    void tesA311_anrErrorAlCrearElEventoDeAutorizacionCorta() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-312 anr dos movimientos uno requiere autorizacion y otro ya anulado")
    void tesA312_anrDosMovimientosUnoRequiereAutorizacionYOtroYaAnulado() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-313 anr anula comprobante ingresado libera el movimiento original")
    void tesA313_anrAnulaComprobanteIngresadoLiberaElMovimientoOriginal() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-314 anr comprobante inexistente es error")
    void tesA314_anrComprobanteInexistenteEsError() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-315 anr comprobante bloqueado es error")
    void tesA315_anrComprobanteBloqueadoEsError() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-316 anr comprobante ya anulado es error")
    void tesA316_anrComprobanteYaAnuladoEsError() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-317 anr comprobante liquidado requiere autorizacion crea evento")
    void tesA317_anrComprobanteLiquidadoRequiereAutorizacionCreaEvento() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-318 anr comprobante liquidado sin requisito de autorizacion con evento vigente es error")
    void tesA318_anrComprobanteLiquidadoSinRequisitoDeAutorizacionConEventoVigenteEsError() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-319 anr comprobante liquidado con evento rechazado es error")
    void tesA319_anrComprobanteLiquidadoConEventoRechazadoEsError() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-320 anr comprobante liquidado con evento autorizado se anula")
    void tesA320_anrComprobanteLiquidadoConEventoAutorizadoSeAnula() throws Exception {
        verificar(CASOS.get(20));
    }

    @Test
    @DisplayName("TES-A-321 anr comprobante liquidado spot desde otro modulo es error")
    void tesA321_anrComprobanteLiquidadoSpotDesdeOtroModuloEsError() throws Exception {
        verificar(CASOS.get(21));
    }

    @Test
    @DisplayName("TES-A-322 anr comprobante liquidado desde facturacion es error")
    void tesA322_anrComprobanteLiquidadoDesdeFacturacionEsError() throws Exception {
        verificar(CASOS.get(22));
    }

    @Test
    @DisplayName("TES-A-323 anr anula comprobante desde otro modulo obtiene los movimientos y no libera")
    void tesA323_anrAnulaComprobanteDesdeOtroModuloObtieneLosMovimientosYNoLibera() throws Exception {
        verificar(CASOS.get(23));
    }

    @Test
    @DisplayName("TES-A-324 anr comprobante en PDA con evento vigente rechaza la autorizacion de liquidacion")
    void tesA324_anrComprobanteEnPDAConEventoVigenteRechazaLaAutorizacionDeLiquidacion() throws Exception {
        verificar(CASOS.get(24));
    }

    @Test
    @DisplayName("TES-A-325 anr anula comprobante con deposito y anula el deposito")
    void tesA325_anrAnulaComprobanteConDepositoYAnulaElDeposito() throws Exception {
        verificar(CASOS.get(25));
    }

    @Test
    @DisplayName("TES-A-326 anr comprobante con nomina de transferencia llama al servicio de nominas")
    void tesA326_anrComprobanteConNominaDeTransferenciaLlamaAlServicioDeNominas() throws Exception {
        verificar(CASOS.get(26));
    }

    @Test
    @DisplayName("TES-A-327 anr comprobante con cargo abono de cuenta inversion reversa con el servicio de cuenta inversion")
    void tesA327_anrComprobanteConCargoAbonoDeCuentaInversionReversaConElServicioDeCuentaInversion() throws Exception {
        verificar(CASOS.get(27));
    }

    @Test
    @DisplayName("TES-A-328 anr reversa de cuenta inversion con error se suma al contador")
    void tesA328_anrReversaDeCuentaInversionConErrorSeSumaAlContador() throws Exception {
        verificar(CASOS.get(28));
    }

    @Test
    @DisplayName("TES-A-329 anr liberar con modulo MRF deja los originales en ANU")
    void tesA329_anrLiberarConModuloMRFDejaLosOriginalesEnANU() throws Exception {
        verificar(CASOS.get(29));
    }

    @Test
    @DisplayName("TES-A-330 anr fecha de movimiento invalida es error")
    void tesA330_anrFechaDeMovimientoInvalidaEsError() throws Exception {
        verificar(CASOS.get(30));
    }

    @Test
    @DisplayName("TES-A-331 anr sin registros es error")
    void tesA331_anrSinRegistrosEsError() throws Exception {
        verificar(CASOS.get(31));
    }

    @Test
    @DisplayName("TES-A-332 anr registro con id cero es error")
    void tesA332_anrRegistroConIdCeroEsError() throws Exception {
        verificar(CASOS.get(32));
    }

    @Test
    @DisplayName("TES-A-333 anr entidad invalida es error")
    void tesA333_anrEntidadInvalidaEsError() throws Exception {
        verificar(CASOS.get(33));
    }

    @Test
    @DisplayName("TES-A-334 anr requisitos basicos con error corta")
    void tesA334_anrRequisitosBasicosConErrorCorta() throws Exception {
        verificar(CASOS.get(34));
    }

    @Test
    @DisplayName("TES-A-335 anr sin reglas de negocio anula un movimiento bloqueado")
    void tesA335_anrSinReglasDeNegocioAnulaUnMovimientoBloqueado() throws Exception {
        verificar(CASOS.get(35));
    }

    @Test
    @DisplayName("TES-A-336 anr movimiento y comprobante en la misma solicitud")
    void tesA336_anrMovimientoYComprobanteEnLaMismaSolicitud() throws Exception {
        verificar(CASOS.get(36));
    }

    @Test
    @DisplayName("TES-A-337 anr metodo logica igual que call")
    void tesA337_anrMetodoLogicaIgualQueCall() throws Exception {
        verificar(CASOS.get(37));
    }
}
