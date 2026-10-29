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
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ban.ConsultarLibretaBancoService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ban.ConsultarLibretaBanco} (entry point {@code call(conn, json)}) as
 * {@link ConsultarLibretaBancoService#execute(com.google.gson.JsonObject)}: bank "libreta" (movements that affected the balance of one
 * bank account on a day) with a simulated running balance and the CLP equivalent of foreign-currency movements (spot, arbitrage, forward,
 * observed dollar).
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-09..22}. Collaborator RevisarRequisitosBasicos is a recorded fake (input in
 * every golden); {@code MonedaFormato} (rounding per currency) ran as the real legacy source in the oracle.
 */
@ExtendWith(ZonaUtc.class)
class ConsultarLibretaBancoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-09_clp-movimientos-saldo-simulado-y-estados-excluidos",
            "TES-B-10_sin-movimientos-saldo-final-cero",
            "TES-B-11_usd-sin-spot-usa-dolar-observado",
            "TES-B-12_usd-spot-no-arbitraje-tipo-cambio-cierre",
            "TES-B-13_eur-arbitraje-usa-dolar-observado-por-paridad",
            "TES-B-14_usd-arbitraje-usa-dolar-observado",
            "TES-B-15_forward-clp-tc-uno",
            "TES-B-16_forward-usd-queda-en-cero",
            "TES-B-17_spot-con-dos-operaciones-se-ignora",
            "TES-B-18_eur-sin-spot-ni-forward-dolar-por-parametro-moneda",
            "TES-B-19_csv-todas-las-cuentas-del-banco",
            "TES-B-20_requisitos-basicos-con-error",
            "TES-B-21_saldo-inicial-nulo-lanza-NullPointerException",
            "TES-B-22_csv-saldo-inicial-nulo-lanza-NullPointerException");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new ConsultarLibretaBancoService(caso.jdbc(), new cl.bcs.tesoreria.javacall.ban.FormatoMoneda(caso.jdbc()), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarLibretaBanco");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ConsultarLibretaBanco.java:419-421,435,447-450 + sql/ConsultarLibretaBanco_CalculaSaldoSimulado.sql: MOD/ANU and out-of-day rows excluded, running balance + initial
    @Test
    void tesB09_movimientosClpConSaldoSimuladoYEstadosExcluidos() {
        verificar(CASOS.get(0));
    }

    // ConsultarLibretaBanco.java:435: no movements -> SaldoFinalSimulado = 0 and an empty list
    @Test
    void tesB10_sinMovimientosElSaldoFinalSimuladoEsCero() {
        verificar(CASOS.get(1));
    }

    // ConsultarLibretaBanco.java:271-279,389-402,555-570: USD movement that is neither spot nor forward uses the observed dollar (rounded by MonedaFormato)
    @Test
    void tesB11_usdSinSpotUsaElDolarObservado() {
        verificar(CASOS.get(2));
    }

    // ConsultarLibretaBanco.java:466-510 + sql/ConsultarLibretaBanco_OperacionesSpot.sql (facturable route): non-arbitrage spot uses TipoCambioCierre
    @Test
    void tesB12_usdSpotSinArbitrajeUsaElTipoDeCambioDeCierre() {
        verificar(CASOS.get(3));
    }

    // ConsultarLibretaBanco.java:499-526 (non-USD arbitrage): Tc = DolarObservado x ParidadCierre (route of fictitious operations)
    @Test
    void tesB13_eurArbitrajeUsaDolarObservadoPorParidad() {
        verificar(CASOS.get(4));
    }

    // ConsultarLibretaBanco.java:499-526 (USD arbitrage): Tc = DolarObservado of the operation date
    @Test
    void tesB14_usdArbitrajeUsaElDolarObservado() {
        verificar(CASOS.get(5));
    }

    // ConsultarLibretaBanco.java:528-543: CLP forward movement -> TcCierre 1 and MontoEquivalente = amount
    @Test
    void tesB15_forwardClpTieneTipoDeCambioUno() {
        verificar(CASOS.get(6));
    }

    // ConsultarLibretaBanco.java:528-543,545-570: USD forward is left at TcCierre 0 / MontoEquivalente 0 (neither converted nor defaulted)
    @Test
    void tesB16_forwardUsdQuedaEnCero() {
        verificar(CASOS.get(7));
    }

    // ConsultarLibretaBanco.java:479: a comprobante resolving to two spot operations is ignored (only groups of size 1 are used)
    @Test
    void tesB17_spotConDosOperacionesSeIgnora() {
        verificar(CASOS.get(8));
    }

    // ConsultarLibretaBanco.java:562-570: non-USD currency without spot/forward = observed dollar x the currency's own financial parameter
    @Test
    void tesB18_eurSinSpotNiForwardUsaDolarPorParametroDeLaMoneda() {
        verificar(CASOS.get(9));
    }

    // ConsultarLibretaBanco.java:282-415 + sql/ConsultarLibretaBanco_CalculaSaldoSimuladoCSV.sql: account id -41 = every account of the bank, output in P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS
    @Test
    void tesB19_cuentaMenos41ListaTodasLasCuentasDelBanco() {
        verificar(CASOS.get(10));
    }

    // ConsultarLibretaBanco.java:236-240: basic requirements with an error short-circuits
    @Test
    void tesB20_requisitosBasicosConErrorCortaElServicio() {
        verificar(CASOS.get(11));
    }

    // ConsultarLibretaBanco.java:421: null p_SaldoInicialSimulado -> NullPointerException on BigDecimal.add(null) (legacy defect)
    @Test
    void tesB21_saldoInicialNuloLanzaNullPointerException() {
        verificar(CASOS.get(12));
    }

    // ConsultarLibretaBanco.java:285: same defect on the CSV path
    @Test
    void tesB22_saldoInicialNuloEnRutaCsvLanzaNullPointerException() {
        verificar(CASOS.get(13));
    }
}
