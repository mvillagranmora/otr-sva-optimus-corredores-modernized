package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.ConsultarSaldoCuentaInversionService;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of the money-market valuation of legacy {@code ConsultarSaldoCuentaInversion} (MontoMoneyMarket): custody quantities of the funds marked
 * MoneyMarket (CUS.Cartera, or HIS.Cartera for a past date) multiplied by the unit value obtained from EntregarPreciosTasas (scripted collaborator) and converted with
 * CalcularParidad (scripted collaborator) for non-CLP accounts.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-247..254}. Legacy: {@code ConsultarSaldoCuentaInversion}; modern: {@link ConsultarSaldoCuentaInversionService}.
 */
@ExtendWith(ZonaUtc.class)
class ConsultarSaldoMoneyMarketServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-247_money-market-valoriza-cuotas-en-custodia-por-valor-cuota",
            "CTA-A-248_money-market-fecha-pasada-usa-la-cartera-historica",
            "CTA-A-249_money-market-sin-valor-cuota-no-suma",
            "CTA-A-250_fondo-que-no-es-money-market-no-se-valoriza",
            "CTA-A-251_money-market-cuenta-usd-convierte-con-paridad",
            "CTA-A-252_money-market-suma-varias-carteras-del-portafolio",
            "CTA-A-253_money-market-cartera-no-vigente-se-ignora",
            "CTA-A-254_money-market-serie-de-otra-empresa-lanza-excepcion-de-lista-nula");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    /** Arrays whose order comes from SQL without ORDER BY. */
    private static final String[] SIN_ORDEN = {"P_OUT_SALDOINICIALFINAL", "P_OUT_SALDOPORFECHAEFECTIVA", "P_OUT_SALDOPORFECHAMOVIMIENTO", "P_OUT_ERRORESDETALLE"};

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colaboradores = new ColaboradoresDeCaso(caso);
        ConsultarSaldoCuentaInversionService service = new ConsultarSaldoCuentaInversionService(caso.jdbc(),
                new SaldoCuentaInversionService(caso.jdbc()), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(), colaboradores);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> {
            try {
                return service.execute(caso.entrada().deepCopy());
            } catch (Exception e) {
                // the legacy throws plain java.lang.Exception in some cases: keep its type, Verifica compares it
                throw sneaky(e);
            }
        });
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colaboradores.traza(), SIN_ORDEN);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneaky(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarSaldoCuentaInversion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-247: ConsultarSaldoCuentaInversion.java:707-734,775-781: quantity in custody x unit value (DECIMAL128) = MontoMoneyMarket
    @Test
    void ctaA247_moneyMarketValorizaCuotasEnCustodiaPorValorCuota() {
        verificar(CASOS.get(0));
    }

    // CTA-A-248: ConsultarSaldoCuentaInversion.java:741-749: FechaDesde different from the system date reads HIS.Cartera at FechaRespaldo
    @Test
    void ctaA248_moneyMarketFechaPasadaUsaLaCarteraHistorica() {
        verificar(CASOS.get(1));
    }

    // CTA-A-249: ConsultarSaldoCuentaInversion.java:1004-1056,775-781: no unit value returned -> nothing added
    @Test
    void ctaA249_moneyMarketSinValorCuotaNoSuma() {
        verificar(CASOS.get(2));
    }

    // CTA-A-250: ConsultarSaldoCuentaInversion_Select_Instrumentos_Money_Market.sql: only funds with MoneyMarket = 'S' are valued
    @Test
    void ctaA250_fondoQueNoEsMoneyMarketNoSeValoriza() {
        verificar(CASOS.get(3));
    }

    // CTA-A-251: ConsultarSaldoCuentaInversion.java:787-809: non-CLP account: the CLP valuation is converted with CalcularParidad
    @Test
    void ctaA251_moneyMarketCuentaUsdConvierteConParidad() {
        verificar(CASOS.get(4));
    }

    // CTA-A-252: ConsultarSaldoCuentaInversion_Select_Cantidad_Custodia.sql: quantities of several custody rows are summed
    @Test
    void ctaA252_moneyMarketSumaVariasCarterasDelPortafolio() {
        verificar(CASOS.get(5));
    }

    // CTA-A-253: ConsultarSaldoCuentaInversion_Select_Cantidad_Custodia.sql: only IdEstado VIG custody rows count
    @Test
    void ctaA253_moneyMarketCarteraNoVigenteSeIgnora() {
        verificar(CASOS.get(6));
    }

    // CTA-A-254: ConsultarSaldoCuentaInversion.java:1014-1015 + 734-753 (LEGACY DEFECT): a money-market series that the company filter of SelectSerieFondo does not return makes obtenerPrecios answer null and Utils.List2Hash throws 'List must be not null'
    @Test
    void ctaA254_moneyMarketSerieDeOtraEmpresaLanzaExcepcionDeListaNula() {
        verificar(CASOS.get(7));
    }

}
