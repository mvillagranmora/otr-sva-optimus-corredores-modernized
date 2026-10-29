package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService;
import cl.bcs.cuentainversion.service.CrearCargoAbonoWSService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS}: resolves client + portfolio from document/code (view
 * UI.ConsultarClientePortafolio), validates, maps the web-service movements (MOVTRN becomes a MOVCAR transfer) and delegates to the REAL CrearCargoAbono.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-208..225}. Legacy: {@code webservices.CrearCargoAbonoWS}; modern: {@link CrearCargoAbonoWSService}.
 */
@ExtendWith(ZonaUtc.class)
class CrearCargoAbonoWSServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-208_abono-a-cliente-tercero-delega-en-crear-cargo-abono",
            "CTA-A-209_cargo-a-cliente-tercero",
            "CTA-A-210_abono-a-cuenta-propia",
            "CTA-A-211_movimiento-de-transferencia-se-convierte-en-cargo-con-traspaso",
            "CTA-A-212_certificado-automatico-N-deja-pendiente",
            "CTA-A-213_varios-movimientos-de-clientes-distintos",
            "CTA-A-214_simulacion-S-no-inserta",
            "CTA-A-215_numero-de-cargo-abono-duplicado-es-error-y-corta",
            "CTA-A-216_cliente-o-portafolio-inexistente-es-error",
            "CTA-A-217_cliente-inactivo-es-error",
            "CTA-A-218_cliente-nuevo-prospecto-dentro-del-plazo-opera",
            "CTA-A-219_cliente-inactivo-fuera-del-plazo-de-prospecto-es-error",
            "CTA-A-220_portafolio-inactivo-es-error",
            "CTA-A-221_error-en-el-segundo-movimiento-no-procesa-ninguno",
            "CTA-A-222_filas-duplicadas-en-la-vista-usan-la-primera",
            "CTA-A-223_tipo-de-movimiento-nulo-lanza-NullPointerException",
            "CTA-A-224_regla-de-negocio-de-crear-cargo-abono-se-propaga-concepto-invalido",
            "CTA-A-225_sin-movimientos-delega-con-lista-vacia");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    /** Arrays whose order comes from SQL without ORDER BY. */
    private static final String[] SIN_ORDEN = {"P_OUT_MENSAJES", "P_OUT_CARGOABONOGENERADO"};

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        CrearCargoAbonoWSService service = new CrearCargoAbonoWSService(caso.jdbc(),
                new CrearCargoAbonoService(caso.jdbc(), new ActualizarSaldoCargoAbonoService(caso.jdbc()),
                new CalendarioHabil(caso.jdbc()), new FormatoMoneda(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(), colab),
                new ConfiguracionDelCaso(caso), new DiccionarioDeCaso());
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
                return CrearCargoAbonoWSServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearCargoAbonoWS");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-208: CrearCargoAbonoWS.java:194-205,330-335: abono for a third-party client is delegated to CrearCargoAbono (inline balance update)
    @Test
    void ctaA208_abonoAClienteTerceroDelegaEnCrearCargoAbono() {
        verificar(CASOS.get(0));
    }

    // CTA-A-209: CrearCargoAbonoWS.java:330: cargo for a third-party client
    @Test
    void ctaA209_cargoAClienteTercero() {
        verificar(CASOS.get(1));
    }

    // CTA-A-210: CrearCargoAbonoWS.java:330: abono for an own account (the balance job is scheduled)
    @Test
    void ctaA210_abonoACuentaPropia() {
        verificar(CASOS.get(2));
    }

    // CTA-A-211: CrearCargoAbonoWS.java:282,290: IdTipoMovimiento MOVTRN -> MOVCAR with EsCargoAbonoPorTraspaso S and the destination portfolio
    @Test
    void ctaA211_movimientoDeTransferenciaSeConvierteEnCargoConTraspaso() {
        verificar(CASOS.get(3));
    }

    // CTA-A-212: CrearCargoAbonoWS.java:292: CertificadoAutomatico N leaves the movement pending
    @Test
    void ctaA212_certificadoAutomaticoNDejaPendiente() {
        verificar(CASOS.get(4));
    }

    // CTA-A-213: CrearCargoAbonoWS.java:210-330: several movements for different clients
    @Test
    void ctaA213_variosMovimientosDeClientesDistintos() {
        verificar(CASOS.get(5));
    }

    // CTA-A-214: CrearCargoAbonoWS.java:268: p_SimularOperacion S is passed through (nothing inserted)
    @Test
    void ctaA214_simulacionSNoInserta() {
        verificar(CASOS.get(6));
    }

    // CTA-A-215: CrearCargoAbonoWS.java:219-228: repeated NumeroCargoAbono -> ID_INTERNO_DUPLICADO and the call stops
    @Test
    void ctaA215_numeroDeCargoAbonoDuplicadoEsErrorYCorta() {
        verificar(CASOS.get(7));
    }

    // CTA-A-216: CrearCargoAbonoWS.java:230-236: unknown document/portfolio -> CLIENTE_PORTAFOLIO_NO_EXISTE
    @Test
    void ctaA216_clienteOPortafolioInexistenteEsError() {
        verificar(CASOS.get(8));
    }

    // CTA-A-217: CrearCargoAbonoWS.java:237-243 + SelectClientePortafolio.sql: inactive client -> CLIENTE_NO_SE_ENCUENTRA_ACTIVO
    @Test
    void ctaA217_clienteInactivoEsError() {
        verificar(CASOS.get(9));
    }

    // CTA-A-218: CrearCargoAbonoWS_SelectClientePortafolio.sql: an inactive client created within PLA.HorasOperaCINClienteNuevoBloqueado hours is a 'new prospect' and may operate
    @Test
    void ctaA218_clienteNuevoProspectoDentroDelPlazoOpera() {
        verificar(CASOS.get(10));
    }

    // CTA-A-219: CrearCargoAbonoWS_SelectClientePortafolio.sql: outside the prospect window the inactive client is rejected
    @Test
    void ctaA219_clienteInactivoFueraDelPlazoDeProspectoEsError() {
        verificar(CASOS.get(11));
    }

    // CTA-A-220: CrearCargoAbonoWS.java:244-251: inactive portfolio -> PORTAFOLIO_NO_SE_ENCUENTRA_ACTIVO
    @Test
    void ctaA220_portafolioInactivoEsError() {
        verificar(CASOS.get(12));
    }

    // CTA-A-221: CrearCargoAbonoWS.java:210-252: validation stops at the first error: no movement of the batch is created
    @Test
    void ctaA221_errorEnElSegundoMovimientoNoProcesaNinguno() {
        verificar(CASOS.get(13));
    }

    // CTA-A-222: CrearCargoAbonoWS.java:200-206: duplicate view rows -> the first one wins
    @Test
    void ctaA222_filasDuplicadasEnLaVistaUsanLaPrimera() {
        verificar(CASOS.get(14));
    }

    // CTA-A-223: CrearCargoAbonoWS.java:282 (LEGACY DEFECT): null IdTipoMovimiento -> NullPointerException
    @Test
    void ctaA223_tipoDeMovimientoNuloLanzaNullPointerException() {
        verificar(CASOS.get(15));
    }

    // CTA-A-224: CrearCargoAbonoWS.java:330: the business rules of CrearCargoAbono (unknown concept) are returned through the web service
    @Test
    void ctaA224_reglaDeNegocioDeCrearCargoAbonoSePropagaConceptoInvalido() {
        verificar(CASOS.get(16));
    }

    // CTA-A-225: CrearCargoAbonoWS.java:330: empty list is delegated as is
    @Test
    void ctaA225_sinMovimientosDelegaConListaVacia() {
        verificar(CASOS.get(17));
    }

}
