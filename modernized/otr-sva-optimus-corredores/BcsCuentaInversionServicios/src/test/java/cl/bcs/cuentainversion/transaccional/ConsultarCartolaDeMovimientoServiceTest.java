package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.infrastructure.adapter.HanaRepository;
import cl.bcs.cuentainversion.service.ConsultarCartolaDeMovimientoService;
import cl.bcs.cuentainversion.service.ConsultarMovimientoCuentaInversionService;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento}: RevisarRequisitosBasicos, the accounts of the filter,
 * the initial balance from SaldoCuentaInversion, the movements from ConsultarMovimientoCuentaInversion, the invoicing nemotecnics (FAC.ComprobanteDetalle) and the
 * mapping to the cartola rows. Collaborator inputs are recorded in the goldens ("colaboradores") and pinned by the pending test (README).
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-189..207}. Legacy: {@code ConsultarCartolaDeMovimiento}; modern: {@link ConsultarCartolaDeMovimientoService}.
 */
@ExtendWith(ZonaUtc.class)
class ConsultarCartolaDeMovimientoServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-189_requisitos-basicos-con-error-devuelve-sus-mensajes",
            "CTA-A-190_sin-cuentas-que-calcen-devuelve-salida-vacia",
            "CTA-A-191_cartola-del-dia-con-saldo-inicial-efectivo",
            "CTA-A-192_saldo-por-fecha-de-movimiento",
            "CTA-A-193_filtro-por-estado-certificado",
            "CTA-A-194_incluye-anulados-N-excluye-anulados",
            "CTA-A-195_incluye-anulados-N-con-filtro-de-estado-pendiente",
            "CTA-A-196_movimiento-de-carga-masiva-toma-descripcion-como-concepto",
            "CTA-A-197_nemotecnicos-de-facturacion-se-concatenan-sin-repetir",
            "CTA-A-198_portafolio-nulo-en-la-entrada-no-devuelve-movimientos",
            "CTA-A-199_filtro-por-cuenta-de-inversion",
            "CTA-A-200_moneda-nula-en-la-entrada-no-devuelve-movimientos",
            "CTA-A-201_filtro-por-producto-agrupador-sin-coincidencia-es-vacio",
            "CTA-A-202_filtro-por-matriz-y-adc-pasa-a-la-consulta-de-movimientos",
            "CTA-A-203_matriz-distinta-no-devuelve-movimientos",
            "CTA-A-204_tramo-historico-con-saldos-incluye-filas-de-saldo",
            "CTA-A-205_tramo-historico-sin-saldos",
            "CTA-A-206_rango-hibrido-con-saldos",
            "CTA-A-207_requisitos-basicos-solo-advertencia-no-bloquea");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        ConsultarCartolaDeMovimientoService service = new ConsultarCartolaDeMovimientoService(caso.jdbc(), new SaldoCuentaInversionService(caso.jdbc()),
                new ConsultarMovimientoCuentaInversionService(new HanaRepository(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso()), colab);
        Verifica.Ejecucion r = ejecutar(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colab.traza());
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
                return ConsultarCartolaDeMovimientoServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarCartolaDeMovimiento");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-189: ConsultarCartolaDeMovimiento.java:217-222: RevisarRequisitosBasicos error messages are returned as they are, counter stays null
    @Test
    void ctaA189_requisitosBasicosConErrorDevuelveSusMensajes() {
        verificar(CASOS.get(0));
    }

    // CTA-A-190: ConsultarCartolaDeMovimiento.java:232-239: no account matches -> empty output, counter 0
    @Test
    void ctaA190_sinCuentasQueCalcenDevuelveSalidaVacia() {
        verificar(CASOS.get(1));
    }

    // CTA-A-191: ConsultarCartolaDeMovimiento.java:254-259,281: initial balance (efectivo) feeds the running balance of the day
    @Test
    void ctaA191_cartolaDelDiaConSaldoInicialEfectivo() {
        verificar(CASOS.get(2));
    }

    // CTA-A-192: ConsultarCartolaDeMovimiento.java:257-259: p_SaldoPorFechaEfectiva N takes SaldoMovimientoInicial
    @Test
    void ctaA192_saldoPorFechaDeMovimiento() {
        verificar(CASOS.get(3));
    }

    // CTA-A-193: ConsultarCartolaDeMovimiento.java:324,552-559: IdEstado filter
    @Test
    void ctaA193_filtroPorEstadoCertificado() {
        verificar(CASOS.get(4));
    }

    // CTA-A-194: ConsultarCartolaDeMovimiento.java:428-433: IncluyeAnulados N removes ANU rows (the running balance already counted them as 0)
    @Test
    void ctaA194_incluyeAnuladosNExcluyeAnulados() {
        verificar(CASOS.get(5));
    }

    // CTA-A-195: ConsultarCartolaDeMovimiento.java:430-433: IncluyeAnulados N with an estado filter
    @Test
    void ctaA195_incluyeAnuladosNConFiltroDeEstadoPendiente() {
        verificar(CASOS.get(6));
    }

    // CTA-A-196: ConsultarCartolaDeMovimiento.java:346-350: IdCarga > 0 -> NombreConcepto = Descripcion; IdCarga 0 keeps the concept name
    @Test
    void ctaA196_movimientoDeCargaMasivaTomaDescripcionComoConcepto() {
        verificar(CASOS.get(7));
    }

    // CTA-A-197: ConsultarCartolaDeMovimiento.java:282-315,382-387 + SelectNemosMovimiento.sql: FAC movements get the distinct nemotecnics joined by ', '; ABNAPV excluded
    @Test
    void ctaA197_nemotecnicosDeFacturacionSeConcatenanSinRepetir() {
        verificar(CASOS.get(8));
    }

    // CTA-A-198: ConsultarCartolaDeMovimiento.java:262 (LEGACY QUIRK): the movements query uses the request p_IdPortafolio: null -> no rows although accounts matched
    @Test
    void ctaA198_portafolioNuloEnLaEntradaNoDevuelveMovimientos() {
        verificar(CASOS.get(9));
    }

    // CTA-A-199: ConsultarCartolaDeMovimiento_SelectPortafolioMoneda.sql: p_IdCuentaInversion filter
    @Test
    void ctaA199_filtroPorCuentaDeInversion() {
        verificar(CASOS.get(10));
    }

    // CTA-A-200: ConsultarCartolaDeMovimiento.java:263 (LEGACY QUIRK): null p_IdMoneda -> the movements join matches nothing
    @Test
    void ctaA200_monedaNulaEnLaEntradaNoDevuelveMovimientos() {
        verificar(CASOS.get(11));
    }

    // CTA-A-201: ConsultarCartolaDeMovimiento_SelectPortafolioMoneda.sql: product filter with no matching account -> empty
    @Test
    void ctaA201_filtroPorProductoAgrupadorSinCoincidenciaEsVacio() {
        verificar(CASOS.get(12));
    }

    // CTA-A-202: ConsultarCartolaDeMovimiento.java:268-269: matrix and ADC filters are passed to the movements query
    @Test
    void ctaA202_filtroPorMatrizYAdcPasaALaConsultaDeMovimientos() {
        verificar(CASOS.get(13));
    }

    // CTA-A-203: ConsultarCartolaDeMovimiento.java:268: a matrix that does not match -> no movements
    @Test
    void ctaA203_matrizDistintaNoDevuelveMovimientos() {
        verificar(CASOS.get(14));
    }

    // CTA-A-204: ConsultarCartolaDeMovimiento.java:271 + ConsultarMovimientoCuentaInversion.java:297-306: historic part with IncluyeSaldo S carries the Saldo rows
    @Test
    void ctaA204_tramoHistoricoConSaldosIncluyeFilasDeSaldo() {
        verificar(CASOS.get(15));
    }

    // CTA-A-205: ConsultarCartolaDeMovimiento.java:271: historic part without balances
    @Test
    void ctaA205_tramoHistoricoSinSaldos() {
        verificar(CASOS.get(16));
    }

    // CTA-A-206: ConsultarCartolaDeMovimiento.java:271 + ConsultarMovimientoCuentaInversion.java:226-247: hybrid range
    @Test
    void ctaA206_rangoHibridoConSaldos() {
        verificar(CASOS.get(17));
    }

    // CTA-A-207: ConsultarCartolaDeMovimiento.java:219: a non-ERROR message from RevisarRequisitosBasicos does not block
    @Test
    void ctaA207_requisitosBasicosSoloAdvertenciaNoBloquea() {
        verificar(CASOS.get(18));
    }

}
