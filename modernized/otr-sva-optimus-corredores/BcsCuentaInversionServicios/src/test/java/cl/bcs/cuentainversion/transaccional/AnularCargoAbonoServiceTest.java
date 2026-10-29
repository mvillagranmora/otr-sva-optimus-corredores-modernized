package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.AnularCargoAbonoService;
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
 * Characterization of legacy {@code cl.bcs.cuentaInversion.AnularCargoAbono} (production entry point {@code call(conn, json)}) as
 * {@link AnularCargoAbonoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Legacy flow: load the movements to annul into step tables, validate (RevisarRequisitosBasicos, ServicioCumpleRequisito SQL matrix,
 * "requiere autorizacion" mantenedor), send RAUT movements to authorization, then either annul directly (config PERMITEANULARMOVIMIENTO = S:
 * state ANU, inventory book, balance recalculation) or create the counterpart movement through CrearCargoAbono. Collaborators of the legacy
 * (RevisarRequisitosBasicos, CrearEventoAutorizacionMasivo, AgregarLog, ConsultarEmpresa) were recorded as fakes; their inputs are in the
 * goldens ("colaboradores") and verified in {@code src/test-pending/transaccional/AnularCargoAbonoColaboradoresPendingTest}.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-32..47} and {@code CTA-A-108}.
 */
@ExtendWith(ZonaUtc.class)
class AnularCargoAbonoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-32_anula-cargo-certificado",
            "CTA-A-33_anula-abono-certificado",
            "CTA-A-34_anula-cargo-con-calce-restituye-libro-inventario",
            "CTA-A-35_requiere-autorizacion-crea-evento-y-no-anula",
            "CTA-A-36_requiere-autorizacion-con-mantenedor-apagado-anula",
            "CTA-A-37_movimiento-ya-anulado-es-rechazado",
            "CTA-A-38_movimiento-inexistente-es-rechazado",
            "CTA-A-39_configuracion-no-permite-anular-es-rechazado",
            "CTA-A-40_ids-en-cero-no-vienen-movimientos",
            "CTA-A-41_abono-de-transferencia-informado-es-rechazado",
            "CTA-A-42_requisitos-basicos-con-cualquier-mensaje-aborta",
            "CTA-A-43_simular-S-igual-anula",
            "CTA-A-44_ejecuta-autorizacion-N-anula-sin-autorizar",
            "CTA-A-45_sin-reglas-id-inexistente-lanza-npe",
            "CTA-A-46_sin-reglas-anula-sin-validar",
            "CTA-A-47_reglas-nulo-lanza-npe",
            "CTA-A-108_sin-reglas-y-config-no-permite-crea-movimiento-de-contrapartida");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
        ActualizarSaldoCargoAbonoService actualizarSaldo = new ActualizarSaldoCargoAbonoService(caso.jdbc());
        CrearCargoAbonoService crear = new CrearCargoAbonoService(caso.jdbc(), actualizarSaldo, new CalendarioHabil(caso.jdbc()),
                new FormatoMoneda(caso.jdbc()), new ConfiguracionDelCaso(caso), diccionario, colab);
        AnularCargoAbonoService service = new AnularCargoAbonoService(caso.jdbc(), actualizarSaldo, crear, diccionario, colab);
        Verifica.Ejecucion r = ejecutar(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colab.traza(), "P_OUT_MENSAJES");
    }

    /** The service entry point declares checked exceptions; the goldens pin the exact type, so it is rethrown unchanged. */
    private interface Llamada {
        com.google.gson.JsonElement ejecutar() throws Exception;
    }

    private static Verifica.Ejecucion ejecutar(Llamada llamada) {
        return Verifica.Ejecucion.de(() -> {
            try {
                return llamada.ejecutar();
            } catch (Exception e) {
                return AnularCargoAbonoServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AnularCargoAbono");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // AnularCargoAbono.java:334-360,399-452: config permite anular S -> IdEstado ANU, balances recalculated; message UI_CARGO_ANULADO (INFORMACION)
    @Test
    void ctaA32_anulaCargoCertificado() {
        verificar(CASOS.get(0));
    }

    // AnularCargoAbono.java:405-410: UI_ABONO_ANULADO for MOVABN
    @Test
    void ctaA33_anulaAbonoCertificado() {
        verificar(CASOS.get(1));
    }

    // AnularCargoAbono.java:337-340 + Update_Movimiento.sql / Update_LibroInventarioSaldos.sql: the movement matched (calce) with the annulled
    // one gets SaldoLibroInventario += MontoCalce and the inventory row becomes ANU
    @Test
    void ctaA34_anulaCargoConCalceRestituyeLibroInventario() {
        verificar(CASOS.get(2));
    }

    // AnularCargoAbono.java:264-319 + CumpleRequisito_Select.sql: RAUT (config requires authorization, IdEvento < 1): CrearEventoAutorizacionMasivo,
    // IdEvento stored in the movement, row removed from the batch, message AUTORIZACION; nothing annulled and no log (early return :323-326)
    @Test
    void ctaA35_requiereAutorizacionCreaEventoYNoAnula() {
        verificar(CASOS.get(3));
    }

    // AnularCargoAbono.java:458-511: PLA.RequiereAutorizacion ANUCAR <> S turns the RAUT result into OK
    @Test
    void ctaA36_requiereAutorizacionConMantenedorApagadoAnula() {
        verificar(CASOS.get(4));
    }

    // CumpleRequisito_Select.sql MOVIMIENTO_CARGO_ABONO_ANULADO -> RECH: error message, counter 1 (call():548)
    @Test
    void ctaA37_movimientoYaAnuladoEsRechazado() {
        verificar(CASOS.get(5));
    }

    // CumpleRequisito_Select.sql MOVIMIENTO_CARGO_ABONO_NO_EXISTE: COALESCE(IdCargoAbonoMovimiento,0) = 0 -> RECH
    @Test
    void ctaA38_movimientoInexistenteEsRechazado() {
        verificar(CASOS.get(6));
    }

    // CumpleRequisito_Select.sql NO_PERMITIDO_ANULAR_MOVIMIENTO: CRT and PERMITEANULARMOVIMIENTO = N -> RECH
    @Test
    void ctaA39_configuracionNoPermiteAnularEsRechazado() {
        verificar(CASOS.get(7));
    }

    // AnularCargoAbono.java:251-254: no id > 0 -> message ERROR NO_VIENEN_MOVIMIENTOS with id 0; counter 1
    @Test
    void ctaA40_idsEnCeroNoVienenMovimientos() {
        verificar(CASOS.get(8));
    }

    // CumpleRequisito_Select.sql MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA: MOVABN + traspaso S + Informado S -> RECH
    @Test
    void ctaA41_abonoDeTransferenciaInformadoEsRechazado() {
        verificar(CASOS.get(9));
    }

    // AnularCargoAbono.java:243-249: ANY message from RevisarRequisitosBasicos aborts (isEmpty check); counter = size in logica but call() recounts ERRORS only
    @Test
    void ctaA42_requisitosBasicosConCualquierMensajeAborta() {
        verificar(CASOS.get(10));
    }

    // AnularCargoAbono.java:264: p_simularOperacion S only skips the authorization block: the annulment is executed anyway (LEGACY DEFECT)
    @Test
    void ctaA43_simularSIgualAnula() {
        verificar(CASOS.get(11));
    }

    // AnularCargoAbono.java:264: p_EjecutaAutorizacion N skips removal of RAUT rows: annulled without authorization (LEGACY DEFECT)
    @Test
    void ctaA44_ejecutaAutorizacionNAnulaSinAutorizar() {
        verificar(CASOS.get(12));
    }

    // AnularCargoAbono.java:344: no validation (reglas N) and a missing id -> null FechaSistema -> NullPointerException
    @Test
    void ctaA45_sinReglasIdInexistenteLanzaNpe() {
        verificar(CASOS.get(13));
    }

    // AnularCargoAbono.java:239: p_UsaReglasNegocio N skips validations (an already annulled movement is "annulled" again)
    @Test
    void ctaA46_sinReglasAnulaSinValidar() {
        verificar(CASOS.get(14));
    }

    // AnularCargoAbono.java:239: in.p_UsaReglasNegocio.equals(...) with null -> NullPointerException
    @Test
    void ctaA47_reglasNuloLanzaNpe() {
        verificar(CASOS.get(15));
    }

    // AnularCargoAbono.java:364-395: PERMITEANULARMOVIMIENTO <> S -> no annulment: counterpart movement created through CrearCargoAbono
    // (p_EsIngresoManual = p_UsaReglasNegocio: LEGACY DEFECT), message tipo "" (resultado CRT has no type) code UI_ABONO_INGRESADO
    @Test
    void ctaA108_sinReglasYConfigNoPermiteCreaMovimientoDeContrapartida() {
        verificar(CASOS.get(16));
    }
}
