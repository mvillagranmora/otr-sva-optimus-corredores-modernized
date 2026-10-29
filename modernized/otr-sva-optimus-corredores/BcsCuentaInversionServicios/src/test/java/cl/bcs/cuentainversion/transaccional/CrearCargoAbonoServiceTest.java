package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.CrearCargoAbono} (entry point {@code call(conn, json)}, Java path
 * {@code PLA.UsaCargoAbonoJava = S}) as {@link CrearCargoAbonoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Legacy flow: load the movements to generate into a step table, resolve account/portfolio/currency/concept/type data in ONE SQL
 * (assigning the movement ids from a sequence), validate each movement (error codes below), optionally check cash flow (manual entry /
 * CMBPTF), insert abonos then cargos (unless simulating), report messages, and either schedule the balance job (broker's own accounts) or
 * recalculate balances inline (third parties). Collaborators (AgregarLog, SaldoFlujoCaja, AgregarPrograma) are recorded fakes whose inputs
 * are part of the goldens ("colaboradores"; verified in {@code src/test-pending/transaccional/CrearCargoAbonoColaboradoresPendingTest}).
 * NOT characterised: the HANA stored-procedure path ({@code PLA.UsaCargoAbonoJava = N}, SQL.call), which H2 cannot run.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-48..76}.
 */
@ExtendWith(ZonaUtc.class)
class CrearCargoAbonoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-48_abono-cuenta-propia-crea-y-agenda-job",
            "CTA-A-49_cargo-cuenta-tercero-actualiza-saldo-en-linea",
            "CTA-A-50_simular-S-no-inserta-pero-agenda-job-en-cuenta-propia",
            "CTA-A-51_simular-nulo-no-inserta",
            "CTA-A-52_sin-cuenta-inversion-se-ignora-en-silencio",
            "CTA-A-53_cuenta-inactiva-es-error",
            "CTA-A-54_moneda-tesoreria-inactiva-es-error",
            "CTA-A-55_moneda-sin-registro-tesoreria-lanza-npe",
            "CTA-A-56_tipo-movimiento-desconocido-es-error",
            "CTA-A-57_monto-negativo-es-error",
            "CTA-A-58_monto-cero-se-acepta",
            "CTA-A-59_fecha-liquidacion-anterior-a-fecha-sistema-es-error",
            "CTA-A-60_fecha-pasada-con-concepto-de-instruccion-se-acepta",
            "CTA-A-61_fecha-pasada-modulo-FMM-se-acepta",
            "CTA-A-62_concepto-inexistente-es-error",
            "CTA-A-63_traspaso-sin-portafolio-destino-es-error",
            "CTA-A-64_traspaso-genera-cargo-y-abono-relacionados",
            "CTA-A-65_abono-a-cliente-bloqueado-por-cumplimiento-es-error",
            "CTA-A-66_cliente-bloqueado-que-es-la-corredora-no-se-bloquea",
            "CTA-A-67_cliente-bloqueado-con-instrumento-FFM-se-permite",
            "CTA-A-68_certificado-automatico-N-queda-pendiente",
            "CTA-A-69_ingreso-manual-cargo-con-saldo-suficiente",
            "CTA-A-70_ingreso-manual-cargo-con-saldo-insuficiente-es-error",
            "CTA-A-71_cambio-portafolio-cargo-sin-flujo-caja-retorna-antes-de-insertar",
            "CTA-A-72_lista-vacia-no-hace-nada",
            "CTA-A-73_un-valido-y-un-invalido-crea-solo-el-valido",
            "CTA-A-74_codigos-de-operacion-producto-instrumento-desconocidos-se-aceptan",
            "CTA-A-75_comprobante-CSC-usa-fecha-local-y-dia-habil-siguiente",
            "CTA-A-76_modulo-nulo-con-fecha-pasada-lanza-npe");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final String[] SIN_ORDEN = {"P_OUT_MENSAJES", "P_OUT_CARGOABONOGENERADO"};

    /** The service declares {@code throws Exception} (the legacy threw plain Exceptions); the exception is handed on unchanged so its type is compared. */
    private static com.google.gson.JsonElement ejecutar(CrearCargoAbonoService service, Caso caso) {
        try {
            return service.execute(caso.entrada().deepCopy());
        } catch (Exception e) {
            return CrearCargoAbonoServiceTest.<RuntimeException>lanzar(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> com.google.gson.JsonElement lanzar(Throwable t) throws E {
        throw (E) t;
    }

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colaboradores = new ColaboradoresDeCaso(caso);
        CrearCargoAbonoService service = new CrearCargoAbonoService(caso.jdbc(), new ActualizarSaldoCargoAbonoService(caso.jdbc()),
                new CalendarioHabil(caso.jdbc()), new FormatoMoneda(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(),
                colaboradores);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> ejecutar(service, caso));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colaboradores.traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearCargoAbono");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CrearCargoAbono.java:472-482,547-621,665-679,741-847: valid abono for a broker's OWN account: inserted CRT, balance job scheduled
    @Test
    void ctaA48_abonoCuentaPropiaCreaYAgendaJob() {
        verificar(CASOS.get(0));
    }

    // CrearCargoAbono.java:826-847: THIRD-PARTY account: ActualizarSaldoCargoAbono inline (no job)
    @Test
    void ctaA49_cargoCuentaTerceroActualizaSaldoEnLinea() {
        verificar(CASOS.get(1));
    }

    // CrearCargoAbono.java:667,722,768-824: p_simularOperacion S: no insert, but the job for own accounts is scheduled anyway (LEGACY DEFECT)
    @Test
    void ctaA50_simularSNoInsertaPeroAgendaJobEnCuentaPropia() {
        verificar(CASOS.get(2));
    }

    // CrearCargoAbono.java:667,722,839: ("N").equals(p_simularOperacion) is false for null: nothing inserted, no balance update
    @Test
    void ctaA51_simularNuloNoInserta() {
        verificar(CASOS.get(3));
    }

    // SelectDatosProcesamiento.sql INNER JOIN CuentaInversion: no account for portfolio+currency -> the movement vanishes silently
    @Test
    void ctaA52_sinCuentaInversionSeIgnoraEnSilencio() {
        verificar(CASOS.get(4));
    }

    // CrearCargoAbono.java:556: IdEstado INA -> CUENTA_INVERSION_NO_ACTIVA
    @Test
    void ctaA53_cuentaInactivaEsError() {
        verificar(CASOS.get(5));
    }

    // CrearCargoAbono.java:611: IdEstadoMonedaTesoreria INA -> MONEDA_NO_ACTIVA
    @Test
    void ctaA54_monedaTesoreriaInactivaEsError() {
        verificar(CASOS.get(6));
    }

    // CrearCargoAbono.java:610: currency without Tesoreria row -> IdMonedaTesoreria null -> NullPointerException
    @Test
    void ctaA55_monedaSinRegistroTesoreriaLanzaNpe() {
        verificar(CASOS.get(7));
    }

    // CrearCargoAbono.java:588-591: unknown movement type -> CUENTA_INVERSION_TRASPASO_INVALIDA + TIPO_MOVIMIENTO_NO_EXISTE
    @Test
    void ctaA56_tipoMovimientoDesconocidoEsError() {
        verificar(CASOS.get(8));
    }

    // CrearCargoAbono.java:566: Monto.compareTo(ZERO) == -1 -> MONTO_MENOR_IGUAL_A_CERO
    @Test
    void ctaA57_montoNegativoEsError() {
        verificar(CASOS.get(9));
    }

    // CrearCargoAbono.java:566: only negatives are flagged: a ZERO amount is accepted although the code says "menor o igual a cero" (LEGACY DEFECT)
    @Test
    void ctaA58_montoCeroSeAcepta() {
        verificar(CASOS.get(10));
    }

    // CrearCargoAbono.java:564: liquidation date < system date (not instruccion concept, not FMM) -> FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO
    @Test
    void ctaA59_fechaLiquidacionAnteriorAFechaSistemaEsError() {
        verificar(CASOS.get(11));
    }

    // CrearCargoAbono.java:394,564: concepts with Uso INGRESOINSTRUCCION may be dated in the past
    @Test
    void ctaA60_fechaPasadaConConceptoDeInstruccionSeAcepta() {
        verificar(CASOS.get(12));
    }

    // CrearCargoAbono.java:564: module FMM may be dated in the past
    @Test
    void ctaA61_fechaPasadaModuloFmmSeAcepta() {
        verificar(CASOS.get(13));
    }

    // CrearCargoAbono.java:607: unknown concept -> CONCEPTO_CUENTA_INVERSION_NO_EXISTE
    @Test
    void ctaA62_conceptoInexistenteEsError() {
        verificar(CASOS.get(14));
    }

    // CrearCargoAbono.java:551: traspaso S without destination portfolio -> CUENTA_INVERSION_TRASPASO_INVALIDA
    @Test
    void ctaA63_traspasoSinPortafolioDestinoEsError() {
        verificar(CASOS.get(15));
    }

    // SelectDatosProcesamiento.sql (two account rows per transfer, flipped type) + CrearCargoAbono.java:663-665,911,961: related ids
    @Test
    void ctaA64_traspasoGeneraCargoYAbonoRelacionados() {
        verificar(CASOS.get(16));
    }

    // CrearCargoAbono.java:586: MOVABN to a client blocked by compliance (indicator BCCUM = S) -> CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO
    @Test
    void ctaA65_abonoAClienteBloqueadoPorCumplimientoEsError() {
        verificar(CASOS.get(17));
    }

    // CrearCargoAbono.java:579-584: the broker itself (document equal to ConsultarCorredoraEmpresa) is never blocked
    @Test
    void ctaA66_clienteBloqueadoQueEsLaCorredoraNoSeBloquea() {
        verificar(CASOS.get(18));
    }

    // CrearCargoAbono.java:586: instrument type FFM is exempt from the compliance block
    @Test
    void ctaA67_clienteBloqueadoConInstrumentoFfmSePermite() {
        verificar(CASOS.get(19));
    }

    // CrearCargoAbono.java:677,960: CertificadoAutomatico N -> PEN and ADVERTENCIA "creado pendiente de certificacion"
    @Test
    void ctaA68_certificadoAutomaticoNQuedaPendiente() {
        verificar(CASOS.get(20));
    }

    // CrearCargoAbono.java:681-716,928,971: manual entry: SaldoLibroInventario 0, FechaCertificacion placeholder; cargo within cash flow
    @Test
    void ctaA69_ingresoManualCargoConSaldoSuficiente() {
        verificar(CASOS.get(21));
    }

    // CrearCargoAbono.java:712: SaldoFinal < 0 -> SALDO_CAJA_INSUFICIENTE (validated after the abonos were inserted)
    @Test
    void ctaA70_ingresoManualCargoConSaldoInsuficienteEsError() {
        verificar(CASOS.get(22));
    }

    // CrearCargoAbono.java:637-660: CMBPTF cargo above the cash flow -> UI_SALDO_INSUFICIENTE_FLUJO_CAJA, returns before any insert
    @Test
    void ctaA71_cambioPortafolioCargoSinFlujoCajaRetornaAntesDeInsertar() {
        verificar(CASOS.get(23));
    }

    // CrearCargoAbono.java:473: empty p_CargoAbonoAGenerar -> nothing happens, no messages
    @Test
    void ctaA72_listaVaciaNoHaceNada() {
        verificar(CASOS.get(24));
    }

    // CrearCargoAbono.java:622,665-679: errored movements are dropped, valid ones are created; counter counts errors only
    @Test
    void ctaA73_unValidoYUnInvalidoCreaSoloElValido() {
        verificar(CASOS.get(25));
    }

    // CrearCargoAbono.java:614-620: the *_NO_EXISTE checks for operation/product/instrument type compare a LEFT JOIN null with "": never raised (LEGACY DEFECT)
    @Test
    void ctaA74_codigosDeOperacionProductoInstrumentoDesconocidosSeAceptan() {
        verificar(CASOS.get(26));
    }

    // CrearCargoAbono.java:866-888,963-971: voucher detail of origin CSC -> FechaSistema/FechaCertificacion from the local date, next business day
    @Test
    void ctaA75_comprobanteCscUsaFechaLocalYDiaHabilSiguiente() {
        verificar(CASOS.get(27));
    }

    // CrearCargoAbono.java:564: p_IdModulo null with a past date -> !in.p_IdModulo.equalsIgnoreCase("FMM") -> NullPointerException
    @Test
    void ctaA76_moduloNuloConFechaPasadaLanzaNpe() {
        verificar(CASOS.get(28));
    }
}
