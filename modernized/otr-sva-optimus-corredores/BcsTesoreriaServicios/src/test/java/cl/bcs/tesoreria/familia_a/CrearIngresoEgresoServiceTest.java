package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearIngresoEgreso} (entry point call(conn, json)) as {@link CrearIngresoEgresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-20 .. TES-A-79} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class CrearIngresoEgresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-20_cie-ingreso-valido-inserta-movimiento-y-forma-de-pago",
            "TES-A-21_cie-egreso-valido-mensaje-de-egreso",
            "TES-A-22_cie-sin-movimientos-es-error",
            "TES-A-23_cie-sin-formas-de-pago-advierte-y-crea-el-movimiento",
            "TES-A-24_cie-requisitos-basicos-con-error-corta-el-proceso",
            "TES-A-25_cie-simular-S-no-inserta-pero-informa-el-resultado",
            "TES-A-26_cie-fecha-liquidacion-vacia-usa-fecha-de-sistema",
            "TES-A-27_cie-dos-movimientos-secuencias-correlativas",
            "TES-A-28_cie-instrumento-informado-se-reporta-en-el-log",
            "TES-A-29_cie-netea-comprobante-segun-preferencia-del-portafolio-titular",
            "TES-A-30_cie-requiere-autorizacion-crea-en-estado-PDA",
            "TES-A-31_cie-tipo-movimiento-vacio-es-error",
            "TES-A-32_cie-concepto-inexistente-es-error",
            "TES-A-33_cie-moneda-inexistente-es-error",
            "TES-A-34_cie-moneda-inactiva-es-error",
            "TES-A-35_cie-moneda-sin-registro-en-tesoreria-es-error",
            "TES-A-36_cie-monto-cero-es-error",
            "TES-A-37_cie-monto-negativo-es-error",
            "TES-A-38_cie-persona-inexistente-es-error",
            "TES-A-39_cie-persona-inactiva-es-error",
            "TES-A-40_cie-persona-titular-inexistente-es-error",
            "TES-A-41_cie-titular-cliente-sin-portafolio-es-error",
            "TES-A-42_cie-caja-inexistente-es-error",
            "TES-A-43_cie-caja-existente-se-acepta",
            "TES-A-44_cie-secuencia-de-agrupacion-ya-utilizada-es-error",
            "TES-A-45_cie-tipo-producto-tipo-operacion-y-condicion-inexistentes-son-error",
            "TES-A-46_cie-producto-operacion-y-condicion-validos-se-aceptan",
            "TES-A-47_cie-ingreso-de-cliente-bloqueado-por-cumplimiento-es-error",
            "TES-A-48_cie-egreso-de-cliente-bloqueado-por-cumplimiento-se-acepta",
            "TES-A-49_cie-cliente-bloqueado-que-es-la-corredora-no-se-bloquea",
            "TES-A-50_cie-portafolio-bloqueado-solo-advierte",
            "TES-A-51_cie-portafolio-titular-bloqueado-distinto-advierte",
            "TES-A-52_cie-codigo-ejecutivo-digitador-valido",
            "TES-A-53_cie-codigo-ejecutivo-digitador-inexistente-es-error",
            "TES-A-54_cie-usuario-no-empleado-lanza-NPE-y-se-reporta-error-generico",
            "TES-A-55_cie-medio-de-pago-invalido-descarta-forma-y-movimiento",
            "TES-A-56_cie-medio-de-pago-CIN-no-es-valido-en-tesoreria",
            "TES-A-57_cie-modalidad-de-pago-invalida-es-error",
            "TES-A-58_cie-moneda-de-forma-de-pago-distinta-del-movimiento-es-error",
            "TES-A-59_cie-forma-de-pago-no-cuadra-con-el-monto-en-modulo-TES-es-error",
            "TES-A-60_cie-forma-de-pago-no-cuadra-fuera-del-modulo-TES-se-acepta",
            "TES-A-61_cie-forma-de-pago-con-monto-cero-es-error",
            "TES-A-62_cie-cuenta-bancaria-del-socio-inexistente-es-error",
            "TES-A-63_cie-cuenta-bancaria-de-la-corredora-inexistente-es-error",
            "TES-A-64_cie-tarjeta-bancaria-corredora-y-socio-inexistentes-son-error",
            "TES-A-65_cie-tarjeta-bancaria-nula-lanza-NPE-y-se-reporta-error-generico",
            "TES-A-66_cie-forma-de-pago-valida-y-movimiento-invalido-se-descartan-ambos",
            "TES-A-67_cie-un-movimiento-valido-y-uno-invalido-crea-solo-el-valido",
            "TES-A-68_cie-forma-de-pago-invalida-de-un-movimiento-descarta-solo-ese-movimiento",
            "TES-A-69_cie-flujo-caja-manual-insuficiente-es-error",
            "TES-A-70_cie-flujo-caja-manual-suficiente-crea-el-egreso",
            "TES-A-71_cie-flujo-caja-negocio-usa-flujo-CUENTA_INVERSION",
            "TES-A-72_cie-origen-PAC-no-valida-flujo-de-caja",
            "TES-A-73_cie-flujo-caja-manual-solo-aplica-a-egresos",
            "TES-A-74_cie-parametro-de-flujo-de-caja-ausente-se-toma-como-N",
            "TES-A-75_cie-parametro-separacomprobantecomspt-ausente-es-error-generico",
            "TES-A-76_cie-separacomprobantecomspt-S-FMM-COMSPT-no-agrupa",
            "TES-A-77_cie-mensajes-duplicados-se-consolidan-por-descripcion",
            "TES-A-78_cie-monto-minimo-positivo-0-01-se-acepta",
            "TES-A-79_cie-monto-cero-con-forma-de-pago-en-cero-informa-solo-monto-sin-especificar");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        try (var ctx = CieContexto.de(caso, d, CrearIngresoEgresoService.class, CrearComprobanteService.class, LiquidarComprobanteService.class)) {
            CrearIngresoEgresoService service = ctx.getBean(CrearIngresoEgresoService.class);
            Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
            EJECUTADOS.ejecutado(nombre);
            Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
        }
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearIngresoEgreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-20 cie ingreso valido inserta movimiento y forma de pago")
    void tesA20_cieIngresoValidoInsertaMovimientoYFormaDePago() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-21 cie egreso valido mensaje de egreso")
    void tesA21_cieEgresoValidoMensajeDeEgreso() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-22 cie sin movimientos es error")
    void tesA22_cieSinMovimientosEsError() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-23 cie sin formas de pago advierte y crea el movimiento")
    void tesA23_cieSinFormasDePagoAdvierteYCreaElMovimiento() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-24 cie requisitos basicos con error corta el proceso")
    void tesA24_cieRequisitosBasicosConErrorCortaElProceso() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-25 cie simular S no inserta pero informa el resultado")
    void tesA25_cieSimularSNoInsertaPeroInformaElResultado() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-26 cie fecha liquidacion vacia usa fecha de sistema")
    void tesA26_cieFechaLiquidacionVaciaUsaFechaDeSistema() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-27 cie dos movimientos secuencias correlativas")
    void tesA27_cieDosMovimientosSecuenciasCorrelativas() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-28 cie instrumento informado se reporta en el log")
    void tesA28_cieInstrumentoInformadoSeReportaEnElLog() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-29 cie netea comprobante segun preferencia del portafolio titular")
    void tesA29_cieNeteaComprobanteSegunPreferenciaDelPortafolioTitular() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-30 cie requiere autorizacion crea en estado PDA")
    void tesA30_cieRequiereAutorizacionCreaEnEstadoPDA() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-31 cie tipo movimiento vacio es error")
    void tesA31_cieTipoMovimientoVacioEsError() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-32 cie concepto inexistente es error")
    void tesA32_cieConceptoInexistenteEsError() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-33 cie moneda inexistente es error")
    void tesA33_cieMonedaInexistenteEsError() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-34 cie moneda inactiva es error")
    void tesA34_cieMonedaInactivaEsError() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-35 cie moneda sin registro en tesoreria es error")
    void tesA35_cieMonedaSinRegistroEnTesoreriaEsError() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-36 cie monto cero es error")
    void tesA36_cieMontoCeroEsError() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-37 cie monto negativo es error")
    void tesA37_cieMontoNegativoEsError() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-38 cie persona inexistente es error")
    void tesA38_ciePersonaInexistenteEsError() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-39 cie persona inactiva es error")
    void tesA39_ciePersonaInactivaEsError() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-40 cie persona titular inexistente es error")
    void tesA40_ciePersonaTitularInexistenteEsError() throws Exception {
        verificar(CASOS.get(20));
    }

    @Test
    @DisplayName("TES-A-41 cie titular cliente sin portafolio es error")
    void tesA41_cieTitularClienteSinPortafolioEsError() throws Exception {
        verificar(CASOS.get(21));
    }

    @Test
    @DisplayName("TES-A-42 cie caja inexistente es error")
    void tesA42_cieCajaInexistenteEsError() throws Exception {
        verificar(CASOS.get(22));
    }

    @Test
    @DisplayName("TES-A-43 cie caja existente se acepta")
    void tesA43_cieCajaExistenteSeAcepta() throws Exception {
        verificar(CASOS.get(23));
    }

    @Test
    @DisplayName("TES-A-44 cie secuencia de agrupacion ya utilizada es error")
    void tesA44_cieSecuenciaDeAgrupacionYaUtilizadaEsError() throws Exception {
        verificar(CASOS.get(24));
    }

    @Test
    @DisplayName("TES-A-45 cie tipo producto tipo operacion y condicion inexistentes son error")
    void tesA45_cieTipoProductoTipoOperacionYCondicionInexistentesSonError() throws Exception {
        verificar(CASOS.get(25));
    }

    @Test
    @DisplayName("TES-A-46 cie producto operacion y condicion validos se aceptan")
    void tesA46_cieProductoOperacionYCondicionValidosSeAceptan() throws Exception {
        verificar(CASOS.get(26));
    }

    @Test
    @DisplayName("TES-A-47 cie ingreso de cliente bloqueado por cumplimiento es error")
    void tesA47_cieIngresoDeClienteBloqueadoPorCumplimientoEsError() throws Exception {
        verificar(CASOS.get(27));
    }

    @Test
    @DisplayName("TES-A-48 cie egreso de cliente bloqueado por cumplimiento se acepta")
    void tesA48_cieEgresoDeClienteBloqueadoPorCumplimientoSeAcepta() throws Exception {
        verificar(CASOS.get(28));
    }

    @Test
    @DisplayName("TES-A-49 cie cliente bloqueado que es la corredora no se bloquea")
    void tesA49_cieClienteBloqueadoQueEsLaCorredoraNoSeBloquea() throws Exception {
        verificar(CASOS.get(29));
    }

    @Test
    @DisplayName("TES-A-50 cie portafolio bloqueado solo advierte")
    void tesA50_ciePortafolioBloqueadoSoloAdvierte() throws Exception {
        verificar(CASOS.get(30));
    }

    @Test
    @DisplayName("TES-A-51 cie portafolio titular bloqueado distinto advierte")
    void tesA51_ciePortafolioTitularBloqueadoDistintoAdvierte() throws Exception {
        verificar(CASOS.get(31));
    }

    @Test
    @DisplayName("TES-A-52 cie codigo ejecutivo digitador valido")
    void tesA52_cieCodigoEjecutivoDigitadorValido() throws Exception {
        verificar(CASOS.get(32));
    }

    @Test
    @DisplayName("TES-A-53 cie codigo ejecutivo digitador inexistente es error")
    void tesA53_cieCodigoEjecutivoDigitadorInexistenteEsError() throws Exception {
        verificar(CASOS.get(33));
    }

    @Test
    @DisplayName("TES-A-54 cie usuario no empleado lanza NPE y se reporta error generico")
    void tesA54_cieUsuarioNoEmpleadoLanzaNPEYSeReportaErrorGenerico() throws Exception {
        verificar(CASOS.get(34));
    }

    @Test
    @DisplayName("TES-A-55 cie medio de pago invalido descarta forma y movimiento")
    void tesA55_cieMedioDePagoInvalidoDescartaFormaYMovimiento() throws Exception {
        verificar(CASOS.get(35));
    }

    @Test
    @DisplayName("TES-A-56 cie medio de pago CIN no es valido en tesoreria")
    void tesA56_cieMedioDePagoCINNoEsValidoEnTesoreria() throws Exception {
        verificar(CASOS.get(36));
    }

    @Test
    @DisplayName("TES-A-57 cie modalidad de pago invalida es error")
    void tesA57_cieModalidadDePagoInvalidaEsError() throws Exception {
        verificar(CASOS.get(37));
    }

    @Test
    @DisplayName("TES-A-58 cie moneda de forma de pago distinta del movimiento es error")
    void tesA58_cieMonedaDeFormaDePagoDistintaDelMovimientoEsError() throws Exception {
        verificar(CASOS.get(38));
    }

    @Test
    @DisplayName("TES-A-59 cie forma de pago no cuadra con el monto en modulo TES es error")
    void tesA59_cieFormaDePagoNoCuadraConElMontoEnModuloTESEsError() throws Exception {
        verificar(CASOS.get(39));
    }

    @Test
    @DisplayName("TES-A-60 cie forma de pago no cuadra fuera del modulo TES se acepta")
    void tesA60_cieFormaDePagoNoCuadraFueraDelModuloTESSeAcepta() throws Exception {
        verificar(CASOS.get(40));
    }

    @Test
    @DisplayName("TES-A-61 cie forma de pago con monto cero es error")
    void tesA61_cieFormaDePagoConMontoCeroEsError() throws Exception {
        verificar(CASOS.get(41));
    }

    @Test
    @DisplayName("TES-A-62 cie cuenta bancaria del socio inexistente es error")
    void tesA62_cieCuentaBancariaDelSocioInexistenteEsError() throws Exception {
        verificar(CASOS.get(42));
    }

    @Test
    @DisplayName("TES-A-63 cie cuenta bancaria de la corredora inexistente es error")
    void tesA63_cieCuentaBancariaDeLaCorredoraInexistenteEsError() throws Exception {
        verificar(CASOS.get(43));
    }

    @Test
    @DisplayName("TES-A-64 cie tarjeta bancaria corredora y socio inexistentes son error")
    void tesA64_cieTarjetaBancariaCorredoraYSocioInexistentesSonError() throws Exception {
        verificar(CASOS.get(44));
    }

    @Test
    @DisplayName("TES-A-65 cie tarjeta bancaria nula lanza NPE y se reporta error generico")
    void tesA65_cieTarjetaBancariaNulaLanzaNPEYSeReportaErrorGenerico() throws Exception {
        verificar(CASOS.get(45));
    }

    @Test
    @DisplayName("TES-A-66 cie forma de pago valida y movimiento invalido se descartan ambos")
    void tesA66_cieFormaDePagoValidaYMovimientoInvalidoSeDescartanAmbos() throws Exception {
        verificar(CASOS.get(46));
    }

    @Test
    @DisplayName("TES-A-67 cie un movimiento valido y uno invalido crea solo el valido")
    void tesA67_cieUnMovimientoValidoYUnoInvalidoCreaSoloElValido() throws Exception {
        verificar(CASOS.get(47));
    }

    @Test
    @DisplayName("TES-A-68 cie forma de pago invalida de un movimiento descarta solo ese movimiento")
    void tesA68_cieFormaDePagoInvalidaDeUnMovimientoDescartaSoloEseMovimiento() throws Exception {
        verificar(CASOS.get(48));
    }

    @Test
    @DisplayName("TES-A-69 cie flujo caja manual insuficiente es error")
    void tesA69_cieFlujoCajaManualInsuficienteEsError() throws Exception {
        verificar(CASOS.get(49));
    }

    @Test
    @DisplayName("TES-A-70 cie flujo caja manual suficiente crea el egreso")
    void tesA70_cieFlujoCajaManualSuficienteCreaElEgreso() throws Exception {
        verificar(CASOS.get(50));
    }

    @Test
    @DisplayName("TES-A-71 cie flujo caja negocio usa flujo CUENTA_INVERSION")
    void tesA71_cieFlujoCajaNegocioUsaFlujoCUENTAINVERSION() throws Exception {
        verificar(CASOS.get(51));
    }

    @Test
    @DisplayName("TES-A-72 cie origen PAC no valida flujo de caja")
    void tesA72_cieOrigenPACNoValidaFlujoDeCaja() throws Exception {
        verificar(CASOS.get(52));
    }

    @Test
    @DisplayName("TES-A-73 cie flujo caja manual solo aplica a egresos")
    void tesA73_cieFlujoCajaManualSoloAplicaAEgresos() throws Exception {
        verificar(CASOS.get(53));
    }

    @Test
    @DisplayName("TES-A-74 cie parametro de flujo de caja ausente se toma como N")
    void tesA74_cieParametroDeFlujoDeCajaAusenteSeTomaComoN() throws Exception {
        verificar(CASOS.get(54));
    }

    @Test
    @DisplayName("TES-A-75 cie parametro separacomprobantecomspt ausente es error generico")
    void tesA75_cieParametroSeparacomprobantecomsptAusenteEsErrorGenerico() throws Exception {
        verificar(CASOS.get(55));
    }

    @Test
    @DisplayName("TES-A-76 cie separacomprobantecomspt S FMM COMSPT no agrupa")
    void tesA76_cieSeparacomprobantecomsptSFMMCOMSPTNoAgrupa() throws Exception {
        verificar(CASOS.get(56));
    }

    @Test
    @DisplayName("TES-A-77 cie mensajes duplicados se consolidan por descripcion")
    void tesA77_cieMensajesDuplicadosSeConsolidanPorDescripcion() throws Exception {
        verificar(CASOS.get(57));
    }

    @Test
    @DisplayName("TES-A-78 cie monto minimo positivo 0 01 se acepta")
    void tesA78_cieMontoMinimoPositivo001SeAcepta() throws Exception {
        verificar(CASOS.get(58));
    }

    @Test
    @DisplayName("TES-A-79 cie monto cero con forma de pago en cero informa solo monto sin especificar")
    void tesA79_cieMontoCeroConFormaDePagoEnCeroInformaSoloMontoSinEspecificar() throws Exception {
        verificar(CASOS.get(59));
    }
}
