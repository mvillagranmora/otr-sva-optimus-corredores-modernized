package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.RelojDeCaso;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.RecaudacionBCPService;
import cl.bcs.tesoreria.javacall.bcp.DiaHabilSiguiente;
import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.RecaudacionBCP} (entry point {@code call(conn, json)}) as {@code RecaudacionBCPService#execute(JsonObject)}:
 * receives a BCP payment (PAGO) or reversal (EXTORNO), logs the message, validates clients, creates the ingreso/egreso (collaborator CrearIngresoEgreso)
 * and, inside the BCP schedule, liquidates the comprobantes (collaborator LiquidarComprobante).
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-64..88, 96}. The schedule check reads the wall clock in the legacy ({@code new Date()}), so the cases fix
 * it deterministically (apertura 00:00:00 / cierre 23:59:59 = always inside, apertura 23:59:59 / cierre 23:59:58 = never inside). The system clock
 * ({@code FechaDB}), the business-day calendar ({@code ObtenerDiaSiguiente}, real source) and the two collaborators are fixed/scripted in the oracle; the modern
 * service has no seam for them yet (see README). Collaborator traces: {@code src/test-pending/tesoreria-b/ColaboradoresPendingTest}.
 */
@ExtendWith(ZonaUtc.class)
class RecaudacionBCPServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-64_pago-en-horario-crea-y-liquida-comprobantes",
            "TES-B-65_pago-fuera-de-horario-liquida-dia-habil-siguiente",
            "TES-B-66_extorno-en-horario-usa-concepto-egreso-y-operacion-de-origen",
            "TES-B-67_pago-con-dni-y-ruc-quita-ceros-a-la-izquierda",
            "TES-B-68_sin-operaciones-NO_SE_INGRESARON_SOLICITUDES",
            "TES-B-69_sin-parametros-de-horario-NO_EXISTE_PARAMETRO_HORARIO",
            "TES-B-70_sin-preferencias-de-pago-NO_EXISTE_PREFERENCIAS",
            "TES-B-71_tipo-operacion-consulta-no-tiene-preferencias",
            "TES-B-72_validaciones-de-cliente-persona-inexistente-y-largo-invalido",
            "TES-B-73_validaciones-persona-inactiva-sin-cliente-sin-portafolio",
            "TES-B-74_una-valida-y-una-invalida-solo-procesa-la-valida",
            "TES-B-75_dni-de-certificacion-bcp-fuerza-error-en-pago",
            "TES-B-76_dni-de-certificacion-bcp-lanza-excepcion-en-pago",
            "TES-B-77_dni-de-certificacion-bcp-fuerza-error-en-extorno",
            "TES-B-78_dni-de-certificacion-bcp-lanza-excepcion-en-extorno",
            "TES-B-79_dni-de-certificacion-de-extorno-no-afecta-a-un-pago",
            "TES-B-80_crear-ingreso-egreso-con-error-devuelve-sus-mensajes",
            "TES-B-81_liquidar-comprobante-con-error-devuelve-sus-mensajes",
            "TES-B-82_sin-comprobantes-generados-no-liquida",
            "TES-B-83_solicitud-repetida-toma-el-ultimo-registro-del-log",
            "TES-B-84_preferencia-de-pago-duplicada-lanza-clave-no-unica",
            "TES-B-85_numero-de-operacion-no-numerico-lanza-NumberFormatException",
            "TES-B-86_numero-de-operacion-vacio-falla-por-comparar-con-igual",
            "TES-B-87_fecha-de-sistema-no-configurada-lanza-excepcion",
            "TES-B-88_extorno-sin-numero-de-extorno-lanza-NumberFormatException",
            "TES-B-96_numero-de-operacion-nulo-usa-cero");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new RecaudacionBCPService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), new RelojDeCaso(),
                    new LogMensajeTransaccionBcp(caso.jdbc()), new DiaHabilSiguiente(caso.jdbc()), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("RecaudacionBCP");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // RecaudacionBCP.java:774-963,970-1021: PAGO inside the schedule: ingreso created (concept RECBCP, MOVING), comprobante liquidated, log updated with the output
    @Test
    void tesB64_pagoEnHorarioCreaYLiquidaComprobantes() {
        verificar(CASOS.get(0));
    }

    // RecaudacionBCP.java:565-590,970: PAGO outside the schedule: liquidation date = next business day, comprobantes are not liquidated
    @Test
    void tesB65_pagoFueraDeHorarioFechaLiquidacionDiaHabilSiguiente() {
        verificar(CASOS.get(1));
    }

    // RecaudacionBCP.java:806-811,845-848,889-893: EXTORNO: MOVEGR, glosa with the reversed operation number, IdOperacion = NumeroOperacionExtornoBcp, payment preference of MOVEGR
    @Test
    void tesB66_extornoUsaMovimientoEgresoYOperacionDeOrigen() {
        verificar(CASOS.get(2));
    }

    // RecaudacionBCP.java:524-533,613-622: leading zeros removed from the depositor id; length 8 = DNI, 11 = RUC
    @Test
    void tesB67_pagoConDniYRucQuitaCerosALaIzquierda() {
        verificar(CASOS.get(3));
    }

    // RecaudacionBCP.java:422-426: no operations: error and nothing is logged
    @Test
    void tesB68_sinOperacionesNoSeIngresaronSolicitudes() {
        verificar(CASOS.get(4));
    }

    // RecaudacionBCP.java:504-515: missing schedule parameters -> NO_EXISTE_PARAMETRO_HORARIO_RECAUDACION_BCP (the input log row stays, not updated)
    @Test
    void tesB69_sinParametrosDeHorarioNoExisteParametroHorario() {
        verificar(CASOS.get(5));
    }

    // RecaudacionBCP.java:678-683: no brokerage payment preference for the concept RECBCP
    @Test
    void tesB70_sinPreferenciasDePagoNoExistePreferenciasPagoCorredor() {
        verificar(CASOS.get(6));
    }

    // RecaudacionBCP.java:655-664,678-683: an operation type that is neither PAGO nor EXTORNO resolves no preferences
    @Test
    void tesB71_tipoOperacionConsultaNoTienePreferencias() {
        verificar(CASOS.get(7));
    }

    // RecaudacionBCP.java:728-729: unknown person and an id of invalid length: TIPO_DOCUMENTO_IDENTIDAD_DESCONOCIDO / PERSONA_NO_EXISTE, all with BCP code CP0140
    @Test
    void tesB72_validacionesPersonaInexistenteYLargoInvalido() {
        verificar(CASOS.get(8));
    }

    // RecaudacionBCP.java:730-733: inactive person / no client / inactive client / no default portfolio
    @Test
    void tesB73_validacionesPersonaInactivaSinClienteClienteInactivoSinPortafolio() {
        verificar(CASOS.get(9));
    }

    // RecaudacionBCP.java:756,372-387: invalid operations are removed (limpiarIngresosEgresos) and the valid one is processed
    @Test
    void tesB74_unaValidaYUnaInvalidaSoloProcesaLaValida() {
        verificar(CASOS.get(10));
    }

    // RecaudacionBCP.java:738: hard-coded BCP certification DNI 67412388 forces CLIENTE_NO_ACTIVO on a PAGO (test hook left in production code)
    @Test
    void tesB75_dniDeCertificacionBcpFuerzaErrorEnPago() {
        verificar(CASOS.get(11));
    }

    // RecaudacionBCP.java:739-742: hard-coded BCP certification DNI 44607843 throws Exception on a PAGO (rollback: the log row is not kept)
    @Test
    void tesB76_dniDeCertificacionBcpLanzaExcepcionEnPago() {
        verificar(CASOS.get(12));
    }

    // RecaudacionBCP.java:744: hard-coded DNI 44225588 forces an error on an EXTORNO
    @Test
    void tesB77_dniDeCertificacionBcpFuerzaErrorEnExtorno() {
        verificar(CASOS.get(13));
    }

    // RecaudacionBCP.java:745-748: hard-coded DNI 11225588 throws on an EXTORNO
    @Test
    void tesB78_dniDeCertificacionBcpLanzaExcepcionEnExtorno() {
        verificar(CASOS.get(14));
    }

    // RecaudacionBCP.java:744-748: the EXTORNO certification DNI is harmless on a PAGO
    @Test
    void tesB79_dniDeCertificacionDeExtornoNoAfectaAUnPago() {
        verificar(CASOS.get(15));
    }

    // RecaudacionBCP.java:933-938: CrearIngresoEgreso error: its messages returned, the log keeps a NULL MensajeSalida
    @Test
    void tesB80_crearIngresoEgresoConErrorDevuelveSusMensajes() {
        verificar(CASOS.get(16));
    }

    // RecaudacionBCP.java:995-1000: LiquidarComprobante error: its messages returned
    @Test
    void tesB81_liquidarComprobanteConErrorDevuelveSusMensajes() {
        verificar(CASOS.get(17));
    }

    // RecaudacionBCP.java:954-957,970: no IdComprobante in the creation result: LiquidarComprobante is not called
    @Test
    void tesB82_sinComprobantesGeneradosNoLiquida() {
        verificar(CASOS.get(18));
    }

    // RecaudacionBCP.java:460-469: two log rows with the same RqUID: the latest (ORDER BY id DESC) is updated
    @Test
    void tesB83_solicitudRepetidaTomaElUltimoRegistroDelLog() {
        verificar(CASOS.get(19));
    }

    // RecaudacionBCP.java:672: two active preferences for the same movement type -> List2Hash key field is not unique
    @Test
    void tesB84_preferenciaDePagoDuplicadaLanzaClaveNoUnica() {
        verificar(CASOS.get(20));
    }

    // RecaudacionBCP.java:840: NumeroOperacionBcp is parsed with Long.parseLong
    @Test
    void tesB85_numeroDeOperacionNoNumericoLanzaNumberFormatException() {
        verificar(CASOS.get(21));
    }

    // RecaudacionBCP.java:840: legacy defect: the empty-string check uses == (reference comparison), so an empty string is parsed and fails
    @Test
    void tesB86_numeroDeOperacionVacioLanzaNumberFormatException() {
        verificar(CASOS.get(22));
    }

    // RecaudacionBCP.java:414: PLATAFORMA/PLA.FechaSistema missing: configuration exception
    @Test
    void tesB87_fechaDeSistemaNoConfiguradaLanzaExcepcion() {
        verificar(CASOS.get(23));
    }

    // RecaudacionBCP.java:848: EXTORNO without NumeroOperacionExtornoBcp: Long.parseLong(null)
    @Test
    void tesB88_extornoSinNumeroDeOperacionDeExtornoLanzaNumberFormatException() {
        verificar(CASOS.get(24));
    }

    // RecaudacionBCP.java:840: a null NumeroOperacionBcp becomes comprobante id 0
    @Test
    void tesB96_numeroDeOperacionNuloUsaCero() {
        verificar(CASOS.get(25));
    }

}
