package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CertificarCargoAbonoService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.CertificarCargoAbono} (production entry point
 * {@code call(conn, json)}) as {@link CertificarCargoAbonoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>The legacy certifies PEN movements: validates them (RevisarRequisitosBasicos, state, transfer rules, cash flow), computes the
 * effective date with ObtenerDiaSiguiente, updates the movements, logs, recalculates balances (ActualizarSaldoCargoAbono per
 * movement) and notifies by e-mail. Collaborators of the legacy (RevisarRequisitosBasicos, SaldoFlujoCaja, AgregarLog, AgregarEvento,
 * ConsultarImagen) were recorded as scripted fakes; their inputs are part of each golden ("colaboradores") and are verified in
 * {@code src/test-pending/transaccional/CertificarCargoAbonoColaboradoresPendingTest} because the modern service has no collaborator
 * seams yet (see README "Pending tests").
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-12..31}.
 */
@ExtendWith(ZonaUtc.class)
class CertificarCargoAbonoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-12_sin-movimientos-devuelve-error-no-hay-movimientos",
            "CTA-A-13_certifica-cargo-con-reglas",
            "CTA-A-14_certifica-abono-con-reglas",
            "CTA-A-15_sin-reglas-fecha-efectiva-queda-0001-01-01",
            "CTA-A-16_movimiento-ya-certificado-es-error",
            "CTA-A-17_movimiento-anulado-es-error",
            "CTA-A-18_abono-de-transferencia-informado-directo-es-error",
            "CTA-A-19_cargo-de-transferencia-certifica-abono-relacionado",
            "CTA-A-20_ambos-extremos-de-transferencia-listados-lanza-excepcion",
            "CTA-A-21_flujo-caja-insuficiente-es-error",
            "CTA-A-22_flujo-caja-igual-al-cargo-certifica",
            "CTA-A-23_flujo-caja-compara-con-double-acepta-excedente-1e-16",
            "CTA-A-24_flujo-caja-no-valida-abonos",
            "CTA-A-25_requisitos-basicos-con-error-aborta",
            "CTA-A-26_requisitos-basicos-solo-advertencia-no-bloquea",
            "CTA-A-27_fecha-sistema-nula-es-error",
            "CTA-A-28_notificacion-cliente-con-correo-y-formato-de-fecha",
            "CTA-A-29_notificacion-sin-id-evento-genera-advertencia",
            "CTA-A-30_error-en-un-movimiento-certifica-el-otro-y-omite-notificacion",
            "CTA-A-31_usuario-sin-empleado-lanza-indice-fuera-de-rango");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    /** Arrays whose order comes from SQL without ORDER BY (UNION ALL of the select that feeds the loop). */
    private static final String[] SIN_ORDEN = {"P_OUT_INFORMACIONDETALLE", "P_OUT_ERRORESDETALLE", "P_OUT_ADVERTENCIASDETALLE"};

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        CertificarCargoAbonoService service = new CertificarCargoAbonoService(caso.jdbc(), new ActualizarSaldoCargoAbonoService(caso.jdbc()),
                new CalendarioHabil(caso.jdbc()), new FormatoMoneda(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(), colab);
        Verifica.Ejecucion r = ejecutar(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colab.traza(), SIN_ORDEN);
    }

    /** Runs a service call that may throw a checked exception; it reaches Verifica unchanged (its type is pinned by the goldens). */
    @FunctionalInterface
    private interface Llamada {
        com.google.gson.JsonElement ejecutar() throws Exception;
    }

    private static Verifica.Ejecucion ejecutar(Llamada llamada) {
        return Verifica.Ejecucion.de(() -> {
            try {
                return llamada.ejecutar();
            } catch (Exception e) {
                return CertificarCargoAbonoServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CertificarCargoAbono");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CertificarCargoAbono.java:310-313: no rows for the ids -> detail NO_HAY_MOVIMIENTOS (id 0). call() then forces
    // P_OUT_ERRORESCONTADOR = 0 (CertificarCargoAbono.java:663: LEGACY DEFECT, the counter never reflects errors)
    @Test
    void ctaA12_sinMovimientosDevuelveErrorNoHayMovimientos() {
        verificar(CASOS.get(0));
    }

    // CertificarCargoAbono.java:482-505,547-558: PEN cargo -> CRT, FechaEfectiva = calculated date, FechaCertificacion = system UTC clock,
    // SaldoLibroInventario = Monto, balances recalculated; detail UI_CARGO_CERTIFICADO "Cargo N°<id> certificado"
    @Test
    void ctaA13_certificaCargoConReglas() {
        verificar(CASOS.get(1));
    }

    // CertificarCargoAbono.java:497-501: UI_ABONO_CERTIFICADO "Abono N°<id> certificado"
    @Test
    void ctaA14_certificaAbonoConReglas() {
        verificar(CASOS.get(2));
    }

    // CertificarCargoAbono.java:324,344: with p_UsaReglasNegocio != S nothing is validated and FechaCalculada keeps the SQL literal
    // '0001-01-01' (Select_Cargo_Abono_A_Certificar.sql) -> FechaEfectivaMovimiento = 0001-01-01 (LEGACY DEFECT)
    @Test
    void ctaA15_sinReglasFechaEfectivaQuedaEn00010101() {
        verificar(CASOS.get(3));
    }

    // CertificarCargoAbono.java:348 + ProcesaRequisito:231-250: IdEstado CRT -> error MOVIMIENTO_CARGO_ABONO_CERTIFICADO with id null
    @Test
    void ctaA16_movimientoYaCertificadoEsError() {
        verificar(CASOS.get(4));
    }

    // CertificarCargoAbono.java:349: IdEstado ANU -> error MOVIMIENTO_CARGO_ABONO_ANULADO
    @Test
    void ctaA17_movimientoAnuladoEsError() {
        verificar(CASOS.get(5));
    }

    // CertificarCargoAbono.java:350,253-279: MOVABN + traspaso S + Informado S -> error; the related movement is removed from the batch
    @Test
    void ctaA18_abonoDeTransferenciaInformadoDirectoEsError() {
        verificar(CASOS.get(6));
    }

    // Select_Cargo_Abono_A_Certificar.sql second UNION ALL branch (IdCargoAbonoRelacionadoTraspaso = listed id, Informado N)
    @Test
    void ctaA19_cargoDeTransferenciaCertificaElAbonoRelacionado() {
        verificar(CASOS.get(7));
    }

    // CertificarCargoAbono.java:263: Utils.List2Hash on a list with a leg of the transfer twice -> Exception("key field is not unique")
    @Test
    void ctaA20_ambosExtremosDeTransferenciaListadosLanzaExcepcion() {
        verificar(CASOS.get(8));
    }

    // CertificarCargoAbono.java:369-468: VALIDAFLUJOCAJAENCERTIFICACION=S, total cargos > cash flow -> FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO
    @Test
    void ctaA21_flujoCajaInsuficienteEsError() {
        verificar(CASOS.get(9));
    }

    // CertificarCargoAbono.java:467: strictly greater-than: equal amounts pass
    @Test
    void ctaA22_flujoCajaIgualAlCargoCertifica() {
        verificar(CASOS.get(10));
    }

    // CertificarCargoAbono.java:467: comparison through BigDecimal.doubleValue(): 1000.0000000000000002 vs 1000.0000000000000001 is "equal" (LEGACY DEFECT)
    @Test
    void ctaA23_flujoCajaComparaConDoubleYAceptaExcedenteDe1e16() {
        verificar(CASOS.get(11));
    }

    // CertificarCargoAbono.java:467: the cash flow is only checked for MOVCAR
    @Test
    void ctaA24_flujoCajaNoValidaAbonos() {
        verificar(CASOS.get(12));
    }

    // CertificarCargoAbono.java:328-337: errors from RevisarRequisitosBasicos abort with those messages (type/code/description, id 0)
    @Test
    void ctaA25_requisitosBasicosConErrorAborta() {
        verificar(CASOS.get(13));
    }

    // CertificarCargoAbono.java:331: only ERROR messages are counted (MensajeSalida.getNumeroErrores): a warning does not block
    @Test
    void ctaA26_requisitosBasicosSoloAdvertenciaNoBloquea() {
        verificar(CASOS.get(14));
    }

    // CertificarCargoAbono.java:346: FechaSistema null -> FECHA_SISTEMA_INVALIDA
    @Test
    void ctaA27_fechaSistemaNulaEsError() {
        verificar(CASOS.get(15));
    }

    // CertificarCargoAbono.java:561-646: notification with e-mail; MonedaFormato formatting; date pattern dd-MM-YYYY (week-year defect)
    @Test
    void ctaA28_notificacionClienteConCorreoYFormatoDeFecha() {
        verificar(CASOS.get(16));
    }

    // CertificarCargoAbono.java:639-644: IdEvento null -> advertencia "No se envio la notificacion"
    @Test
    void ctaA29_notificacionSinIdEventoGeneraAdvertencia() {
        verificar(CASOS.get(17));
    }

    // CertificarCargoAbono.java:561: notification only when P_OUT_ERRORESCONTADOR == 0; valid movements are still certified
    @Test
    void ctaA30_errorEnUnMovimientoCertificaElOtroYOmiteNotificacion() {
        verificar(CASOS.get(18));
    }

    // CertificarCargoAbono.java:598,618: listaInfoExtra.get(0) with no row -> IndexOutOfBoundsException
    @Test
    void ctaA31_usuarioSinEmpleadoLanzaIndiceFueraDeRango() {
        verificar(CASOS.get(19));
    }
}
